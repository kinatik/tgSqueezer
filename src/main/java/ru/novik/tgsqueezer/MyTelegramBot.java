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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.db.DbService;
import ru.novik.tgsqueezer.db.repository.ChatSettingsRepository;
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

    private final ChatSettingsRepository settings;
    private final OpenAiService openAiService;
    private final OpenAiImageService openAiImageService;
    private final MessageRepository messageRepository;
    private final DbService dbService;

    private final Map<Long, Integer> requestCounter = new HashMap<>();
    private final Map<Long, Long> imagePerUserCountdown = new HashMap<>();
    private final Map<Long, Long> imagePerChatCountdown = new HashMap<>();

    @Autowired
    public MyTelegramBot(OpenAiService openAiService, OpenAiImageService openAiImageService, BotConfig botConfig,
                         MessageRepository messageRepository, ChatSettingsRepository settings, DbService dbService) {
        super(botConfig.getToken());
        this.openAiService = openAiService;
        this.openAiImageService = openAiImageService;
        this.botConfig = botConfig;
        this.messageRepository = messageRepository;
        this.settings = settings;
        this.dbService = dbService;
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
            if (isImageDescriptionAllowedForUser(userId, chatId) && isImageDescriptionAllowedForChat(chatId)
                    && settings.getMaxImageSize(chatId) > 0) {
                try {
                    File imageFileFromUpdate = getImageFileFromUpdate(message, chatId);
                    java.io.File downloadedFile = downloadedFile(imageFileFromUpdate);
                    String base64 = getBase64FromImageFile(downloadedFile);
                    String describe =  openAiImageService.describe(base64, chatId);
                    addMessage(message, settings.getSomebodySentImagePrompt(chatId) + " " + describe, null);
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

            requestCounter.putIfAbsent(chatId, settings.getMaxRequestsPerNotAllowedChat(chatId));

            String adminScenarios = executeAdminScenarios(userId, chatId, messageText);
            if (adminScenarios != null) {
                sendMessage(chatId, adminScenarios, false);
                return;
            }

            if (chatId > 0) {
                sendMessage(chatId, settings.getInBotStartMessage(chatId));
                return;
            }

            if (messageText.startsWith("/start")) {
                dbService.start(chatId);

                if (settings.getAllowedChatIds(chatId).contains(chatId)) {
                    sendMessage(chatId, String.format(settings.getInChatStartAllowedMessage(chatId),
                            chatId,
                            settings.getMinMessageStack(chatId),
                            settings.getMaxMessageStack(chatId)
                    ));
                } else {
                    sendMessage(chatId, String.format(settings.getInChatStartNotAllowedMessage(chatId),
                            chatId,
                            requestCounter.get(chatId),
                            settings.getMinMessageStack(chatId),
                            settings.getMaxMessageStack(chatId)
                    ));
                }
                return;
            }

            // describe image immediately command for debug purposes
            if (message.isReply()) {
                if (message.getReplyToMessage().hasPhoto()
                        && messageStartsWithText(messageText, settings.getDescribeImageImmediatelyPrompt(chatId))) {
                    if (settings.getMaxImageSize(chatId) > 0) {
                        try {
                            File imageFileFromUpdate = getImageFileFromUpdate(message.getReplyToMessage(), chatId);
                            java.io.File downloadedFile = downloadedFile(imageFileFromUpdate);
                            String base64 = getBase64FromImageFile(downloadedFile);
                            String describe =  openAiImageService.describe(base64, chatId);
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

            if (!messageText.startsWith("/squeeze") && !messageStartsWithText(messageText, settings.getCommandToSqueeze(chatId))) {
                addMessage(message, messageText, null);
            }

            if (messageText.startsWith("/squeeze") || messageStartsWithText(messageText, settings.getCommandToSqueeze(chatId))) {
                sendSummary(chatId, extractInt(messageText));
            } else if (messageText.startsWith("/version")) {
                sendMessage(chatId, settings.getVersionMessage(chatId));
            } else if (messageText.startsWith("/about")) {
                sendMessage(chatId, settings.getAboutMessage(chatId));
            }
        }
    }

    private static final String GET_DEFAULT_SETTINGS = "get default settings";
    private static final String GET_DEFAULT_SETTING = "get default setting";
    private static final String SET_DEFAULT_SETTING = "set default setting";
    private static final String GET_CHAT_IDS = "get chat ids";
    private static final String GET_CHAT_SETTINGS = "get chat settings";
    private static final String GET_CHAT_SETTING = "get chat setting";
    private static final String SET_CHAT_SETTING = "set chat setting";

    private String executeAdminScenarios(Long userId, Long chatId, String messageText) {
        if (botConfig.getSuperUserId().equals(userId) && chatId > 0) {
            String lowerCaseMessage = messageText.trim().toLowerCase();
            if (lowerCaseMessage.startsWith("info")) {
                return String.format("""
                    %s - get all default settings names
                    %s <setting number> - get default setting value
                    %s <setting number> <value> - set default setting value
                    %s - get all chat ids
                    %s <chat number> - get all chat settings names for chat
                    %s <chat number> <setting number> - get chat setting value for chat
                    %s <chat number> <setting number> <value> - set chat setting value for chat
                    """,
                    GET_DEFAULT_SETTINGS, GET_DEFAULT_SETTING, SET_DEFAULT_SETTING,
                    GET_CHAT_IDS, GET_CHAT_SETTINGS, GET_CHAT_SETTING, SET_CHAT_SETTING).trim();

            } else if (lowerCaseMessage.startsWith(GET_DEFAULT_SETTINGS)) {
                return dbService.getDefaultSettings();

            } else if (lowerCaseMessage.startsWith(GET_DEFAULT_SETTING)) {
                return dbService.getDefaultSetting(messageText.substring(GET_DEFAULT_SETTING.length()).trim());

            } else if (lowerCaseMessage.startsWith(SET_DEFAULT_SETTING)) {
                String[] split = messageText.substring(SET_DEFAULT_SETTING.length()).trim().split(" ", 2);
                return dbService.setDefaultSetting(split[0], split[1]);

            } else if (lowerCaseMessage.startsWith(GET_CHAT_IDS)) {
                return dbService.getChatIds();

            } else if (lowerCaseMessage.startsWith(GET_CHAT_SETTINGS)) {
                String chatNumberString = messageText.substring(GET_CHAT_SETTINGS.length()).trim();
                return dbService.getChatSettings(chatNumberString);

            } else if (lowerCaseMessage.startsWith(GET_CHAT_SETTING)) {
                String[] split = messageText.substring(GET_CHAT_SETTING.length()).trim().split(" ", 2);
                return dbService.getChatSetting(split[0].trim(), split[1].trim());

            } else if (lowerCaseMessage.startsWith(SET_CHAT_SETTING)) {
                String[] split = messageText.substring(SET_CHAT_SETTING.length()).trim().split(" ", 3);
                return dbService.setChatSetting(split[0].trim(), split[1].trim(), split[2].trim());

            }
        }
        return null;
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
        return message.replaceAll("[^a-zA-Zа-яА-ЯёЁ]", "").toLowerCase().startsWith(text.replaceAll("[^a-zA-Zа-яА-ЯёЁ]", "").toLowerCase());
    }

    private File getImageFileFromUpdate(Message message, Long chatId) throws TelegramApiException {
        List<PhotoSize> photos = message.getPhoto();
        String fileId = photos.get(Math.min(photos.size() - 1, settings.getMaxImageSize(chatId) - 1)).getFileId();
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
            count = Math.min(count, settings.getMaxMessageStack(chatId));
            messages = messageRepository.getLastMessages(chatId, count);
        }

        List<String> strings = getStrings(messages);

        if (strings.isEmpty()) {
            sendMessage(chatId, settings.getNoMessageToDisplayMessage(chatId));
            return;
        }

        if (strings.size() <= settings.getMinMessageStack(chatId)) {
            sendMessage(chatId, settings.getMinMessageStackNotReachedMessage(chatId));
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string).append("\n");
        }

        String message;
        try {
            if (settings.getAllowedChatIds(chatId).contains(chatId) || requestCounter.get(chatId) > 0) {
                message = openAiService.summarize(settings.getChatgptApiKey(chatId), stringBuilder.toString(), chatId);
                requestCounter.put(chatId, requestCounter.get(chatId) - 1);
            } else if (settings.getAllowedChatIds(chatId).contains(chatId) || requestCounter.get(chatId) <= 0) {
                message = String.format(settings.getNotAllowedMessage(chatId), chatId);
            } else {
                message = openAiService.summarize(settings.getChatgptApiKey(chatId), stringBuilder.toString(), chatId);
            }
        } catch (IOException e) {
            message = String.format(settings.getErrorMessage(chatId), e.getMessage());
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
        sendMessage(chatId, text, false);
    }

    private void sendMessage(long chatId, String text, boolean isMarkdown) {
        SendMessage message = new SendMessage();
        if (isMarkdown) {
            message.enableMarkdown(true);
        }
        message.setChatId(chatId);
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

    private boolean isImageDescriptionAllowedForUser(Long userId, Long chatId) {
        long currentTimeMillis = System.currentTimeMillis();
        long interval = settings.getImageFrequencyPerUserInMins(chatId) * 60 * 1000;
        Long lastAllowedTime = imagePerUserCountdown.getOrDefault(userId, currentTimeMillis - interval);

        if (interval > 0 && currentTimeMillis - lastAllowedTime >= interval) {
            imagePerUserCountdown.put(userId, currentTimeMillis);
            return true;
        }
        return false;
    }

    private boolean isImageDescriptionAllowedForChat(Long chatId) {
        long currentTimeMillis = System.currentTimeMillis();
        long interval = settings.getImageFrequencyPerChatInMins(chatId) * 60 * 1000;
        long lastAllowedTime = imagePerChatCountdown.getOrDefault(chatId, currentTimeMillis - interval);

        if (interval > 0 && currentTimeMillis - lastAllowedTime >= interval) {
            imagePerChatCountdown.put(chatId, currentTimeMillis);
            return true;
        }
        return false;
    }

}

