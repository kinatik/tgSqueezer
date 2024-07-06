package ru.novik.tgsqueezer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.service.OpenAiImageService;
import ru.novik.tgsqueezer.service.OpenAiService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final OpenAiService openAiService;
    private final OpenAiImageService openAiImageService;

    private final Map<Long, List<String>> messages = new HashMap<>();
    private final Map<Long, Integer> requestCounter = new HashMap<>();
    private final Map<Long, Long> imagePerUserCountdown = new HashMap<>();
    private final Map<Long, Long> imagePerChatCountdown = new HashMap<>();

    @Autowired
    public MyTelegramBot(OpenAiService openAiService, OpenAiImageService openAiImageService, BotConfig botConfig) {
        super(botConfig.getToken());
        this.openAiService = openAiService;
        this.openAiImageService = openAiImageService;
        this.botConfig = botConfig;
        log.info("Bot config loaded: {}", botConfig);
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage() || update.getMessage() == null) {
            return;
        }

        Message message = update.getMessage();
        String userName = getUserName(update);
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
                    addMessage(chatId, userName,
                            botConfig.getSomebodySentImagePrompt() + " " + describe);
                } catch (IOException e) {
                    log.error("Error describing image", e);
                } catch (TelegramApiException e) {
                    log.error("Error getting image file from telegram API", e);
                }
            }
            if (message.getCaption() != null) {
                addMessage(chatId, userName,
                        botConfig.getSomebodyCaptionedImagePrompt() + message.getCaption());
            }

        }
        if (message.hasText()) {
            String messageText = message.getText();

            log.info("ChatID: {}, username: {}, message: {}", chatId, userName, messageText);

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
                        && isMessageStartWithText(messageText, botConfig.getDescribeImagePrompt())) {
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

            if (!messageText.startsWith("/squeeze")) {
                addMessage(chatId, userName, messageText);
            }

            if (messageText.startsWith("/squeeze")) {
                sendSummary(chatId);
            } else if (messageText.startsWith("/version")) {
                sendMessage(chatId, botConfig.getVersionMessage());
            } else if (messageText.startsWith("/about")) {
                sendMessage(chatId, botConfig.getAboutMessage());
            } else if (messageText.toLowerCase().contains("special phrase")) {
                sendMessage(chatId, "Special phrase detected!");
            }
        }
    }

    private boolean isMessageStartWithText(String message, String text) {
        return message != null && text != null && message.replaceAll("\\W", "").toLowerCase()
                .startsWith(text.toLowerCase().replaceAll("\\W", ""));
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

    private void addMessage(Long chatId, String userName, String messageText) {
        messages.putIfAbsent(chatId, new ArrayList<>());
        messages.get(chatId).add(userName + ": " + messageText);
    }

    private String getUserName(Update update) {
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();
        return firstName + (lastName == null ? "" : " " + lastName);
    }

    private void sendSummary(long chatId) {
        List<String> strings = messages.get(chatId);
        if (strings == null || strings.isEmpty()) {
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
        strings.clear();
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

