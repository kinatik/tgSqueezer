package ru.novik.tgsqueezer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.service.OpenAiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final OpenAiService openAiService;

    private final Map<Long, List<String>> messages = new HashMap<>();
    private final Map<Long, Integer> requestCounter = new HashMap<>();

    @Autowired
    public MyTelegramBot(OpenAiService openAiService, BotConfig botConfig) {
        super(botConfig.getToken());
        this.openAiService = openAiService;
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();

            String messageText = update.getMessage().getText();
            String userName = userName(update);

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

            if (!messageText.startsWith("/squeeze")) {
                messages.putIfAbsent(chatId, new ArrayList<>());
                messages.get(chatId).add(userName + ": " + messageText);
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

    private String userName(Update update) {
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
}

