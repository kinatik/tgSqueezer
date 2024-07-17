package ru.novik.tgsqueezer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.db.repository.MessageRepository;
import ru.novik.tgsqueezer.service.OpenAiImageService;
import ru.novik.tgsqueezer.service.OpenAiService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final OpenAiService openAiService;
    private final OpenAiImageService openAiImageService;
    private final MessageRepository messageRepository;

//    private final Map<Long, List<String>> messages = new HashMap<>();
    private final Map<Long, Integer> requestCounter = new HashMap<>();
    private final Map<Long, Long> imagePerUserCountdown = new HashMap<>();
    private final Map<Long, Long> imagePerChatCountdown = new HashMap<>();

    @Autowired
    public MyTelegramBot(OpenAiService openAiService, OpenAiImageService openAiImageService, BotConfig botConfig, MessageRepository messageRepository) {
        super(botConfig.getToken());
        this.openAiService = openAiService;
        this.openAiImageService = openAiImageService;
        this.botConfig = botConfig;
        this.messageRepository = messageRepository;
        log.info("Bot config loaded: {}", botConfig);
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if ((!update.hasMessage() || update.getMessage() == null) && !update.hasEditedMessage()) {
            return;
        }

        if (update.hasEditedMessage()) {
            Message editedMessage = update.getEditedMessage();
            if (editedMessage.hasText()) {
                String messageText = editedMessage.getText();
                messageRepository.editMessage(editedMessage.getChatId(), editedMessage.getMessageId(), messageText);
            }
            if (editedMessage.hasPhoto()) {
                String caption = editedMessage.getCaption();
                messageRepository.editMessageCaption(editedMessage.getChatId(), editedMessage.getMessageId(), caption);
            }
        }

        Message message = update.getMessage();
        Long userId = getUserId(update);
        Long chatId = getChatId(update);

        if (message.hasPhoto()) {
            if (isImageDescriptionAllowedForUser(userId) && isImageDescriptionAllowedForChat(chatId)
                    && botConfig.getMaxImageSize() != null && botConfig.getMaxImageSize() > 0) {
                try {
                    File imageFileFromUpdate = getImageFileFromUpdate(message);
                    java.io.File downloadedFile = downloadedFile(imageFileFromUpdate);
                    String base64 = getBase64FromImageFile(downloadedFile);
                    String describe =  openAiImageService.describe(base64);
                    log.info("Image description: {}", describe);
                    addMessage(message, botConfig.getSomebodySentImagePrompt() + " " + describe, null);
                } catch (IOException e) {
                    log.error("Error describing image", e);
                } catch (TelegramApiException e) {
                    log.error("Error getting image file from telegram API", e);
                }
            }
            if (message.getCaption() != null) {
                messageRepository.editMessageCaption(chatId, message.getMessageId(), message.getCaption());
            }

        }
        if (message.hasText()) {
            String messageText = message.getText();

            requestCounter.putIfAbsent(chatId, botConfig.getMaxRequestsPerNotAllowedChat());

            if (chatId > 0) {
                sendMessage(chatId, botConfig.getInBotStartMessage());
                return;
            }

            if (messageText.startsWith("/start")) {
                if (botConfig.getAllowedChatIds().contains(chatId)) {
                    sendMessage(chatId, String.format(botConfig.getInChatStartAllowedMessage(),
                            chatId,
                            botConfig.getMinMessageStack(),
                            botConfig.getMaxMessageStack()
                    ));
                } else {
                    sendMessage(chatId, String.format(botConfig.getInChatStartNotAllowedMessage(),
                            chatId,
                            requestCounter.get(chatId),
                            botConfig.getMinMessageStack(),
                            botConfig.getMaxMessageStack()
                    ));
                }
                return;
            }

            // describe image immediately command for debug purposes
            if (message.isReply()) {
                if (message.getReplyToMessage().hasPhoto()
                        && messageStartsWithText(messageText, botConfig.getDescribeImagePrompt())) {
                    if (botConfig.getMaxImageSize() != null && botConfig.getMaxImageSize() > 0) {
                        try {
                            File imageFileFromUpdate = getImageFileFromUpdate(message.getReplyToMessage());
                            java.io.File downloadedFile = downloadedFile(imageFileFromUpdate);
                            String base64 = getBase64FromImageFile(downloadedFile);
                            String describe =  openAiImageService.describe(base64);
                            sendMessage(chatId, describe);
                        } catch (IOException e) {
                            log.error("Error describing image", e);
                        } catch (TelegramApiException e) {
                            log.error("Error getting image file from telegram API", e);
                        }
                    }
                    return;
                }
            }

            if (!messageText.startsWith("/squeeze") && !messageStartsWithText(messageText, botConfig.getCommandToSqueeze())) {
                addMessage(message, messageText, null);
            }

            if (messageText.startsWith("/squeeze") || messageStartsWithText(messageText, botConfig.getCommandToSqueeze())) {
                sendSummary(chatId, extractInt(messageText));
            } else if (messageText.startsWith("/version")) {
                sendMessage(chatId, botConfig.getVersionMessage());
            } else if (messageText.startsWith("/about")) {
                sendMessage(chatId, botConfig.getAboutMessage());
            } else if (messageText.toLowerCase().contains("special phrase")) {
                sendMessage(chatId, "Special phrase detected!");
            }
        }
    }

    private Integer extractInt(String message) {
        String[] words = message.split(" ");
        for (String word : words) {
            try {
                return Integer.parseInt(word);
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return null;
    }

    private boolean messageStartsWithText(String message, String text) {
        return message != null && text != null && message.toLowerCase().replaceAll("[^a-zA-Zа-яА-Я]", "")
                .startsWith(text.toLowerCase().replaceAll("[^a-zA-Zа-яА-Я]", ""));
    }

    private File getImageFileFromUpdate(Message message) throws TelegramApiException {
        List<PhotoSize> photos = message.getPhoto();
        String fileId = photos.get(Math.min(photos.size() - 1, botConfig.getMaxImageSize() - 1)).getFileId();
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        return execute(getFileMethod);
    }

    private java.io.File downloadedFile(File file) throws IOException, TelegramApiException {
        Path tempFile = Files.createTempFile("image", ".jpg");
        log.info("Temp file created: {}", tempFile.toAbsolutePath());
        return  downloadFile(file, tempFile.toFile());
    }

    private String getBase64FromImageFile(java.io.File downloadedFile) throws IOException {
        byte[] fileContent = Files.readAllBytes(downloadedFile.toPath());
        if (!downloadedFile.delete()) {
            log.warn("Temp file not deleted: {}", downloadedFile.getAbsolutePath());
        } else {
            log.info("Temp file deleted: {}", downloadedFile.getAbsolutePath());
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

    private void addMessage(Message message, String messageText, String caption) {
        ru.novik.tgsqueezer.db.model.Message messageModel = new ru.novik.tgsqueezer.db.model.Message();
        messageModel.setMessageId(message.getMessageId());
        messageModel.setChatId(message.getChatId());
        messageModel.setUserId(message.getFrom().getId());
        messageModel.setUsername(getUserName(message));
        messageModel.setTime(new Timestamp(System.currentTimeMillis()));
        messageModel.setMessage(messageText);
        messageModel.setCaption(caption);
        messageRepository.insertMessage(messageModel);
    }

    private String getUserName(Message message) {
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        return firstName + (lastName == null ? "" : " " + lastName);
    }

    private void sendSummary(long chatId, Integer count) {
        List<ru.novik.tgsqueezer.db.model.Message> messages;
        if (count == null || count <= 0) {
            messages = messageRepository.getUnreadMessages(chatId);
            messageRepository.setMessagesRead(chatId);
        } else {
            count = Math.min(count, botConfig.getMaxMessageStack());
            messages = messageRepository.getLastMessages(chatId, count);
        }

        List<String> strings = getStrings(messages);

        if (strings.isEmpty()) {
            sendMessage(chatId, botConfig.getNoMessageToDisplayMessage());
            return;
        }

        if (strings.size() <= botConfig.getMinMessageStack()) {
            sendMessage(chatId, botConfig.getMinMessageStackNotReachedMessage());
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string).append("\n");
        }

        String message;
        try {
            if (botConfig.getAllowedChatIds().contains(chatId) || requestCounter.get(chatId) > 0) {
                message = openAiService.summarize(botConfig.getChatgptApiKey(), stringBuilder.toString());
                requestCounter.put(chatId, requestCounter.get(chatId) - 1);
            } else if (botConfig.getAllowedChatIds().contains(chatId) || requestCounter.get(chatId) <= 0) {
                message = String.format(botConfig.getNotAllowedMessage(), chatId);
            } else {
                message = openAiService.summarize(botConfig.getChatgptApiKey(), stringBuilder.toString());
            }
        } catch (IOException e) {
            message = String.format(botConfig.getErrorMessage(), e.getMessage());
        }

        sendMessage(chatId, message);
    }

    private static @NotNull List<String> getStrings(List<ru.novik.tgsqueezer.db.model.Message> messages) {
        return messages.stream()
                .flatMap(msg -> Stream.of(msg.getMessage(), msg.getCaption())
                        .filter(Objects::nonNull)
                        .map(content -> String.format("%s: %s", msg.getUsername(), content)))
                .collect(Collectors.toList());
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }

    private Long getUserId(Update update) {
        if (update.getMessage() != null) {
            return update.getMessage().getFrom().getId();
        }
        return 0L;
    }

    private Long getChatId(Update update) {
        if (update.getMessage() != null) {
            return update.getMessage().getChatId();
        }
        return 0L;
    }

    private boolean isImageDescriptionAllowedForUser(Long userId) {
        long currentTimeMillis = System.currentTimeMillis();
        Long lastAllowedTime = imagePerUserCountdown.getOrDefault(userId, currentTimeMillis);
        long interval = botConfig.getImageFrequencyPerUserInMins() * 60 * 1000;

        if (interval > 0 && currentTimeMillis - lastAllowedTime >= interval) {
            imagePerUserCountdown.put(userId, currentTimeMillis);
            return true;
        }
        return false;
    }

    private boolean isImageDescriptionAllowedForChat(Long chatId) {
        long currentTime = System.currentTimeMillis();
        long lastAllowedTime = imagePerChatCountdown.getOrDefault(chatId, currentTime);
        long interval = botConfig.getImageFrequencyPerChatInMins() * 60 * 1000;

        if (interval > 0 && currentTime - lastAllowedTime >= interval) {
            imagePerChatCountdown.put(chatId, currentTime);
            return true;
        }
        return false;
    }

}

