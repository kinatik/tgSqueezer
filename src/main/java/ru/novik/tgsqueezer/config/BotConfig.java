package ru.novik.tgsqueezer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "bot")
@Getter
@Setter
public class BotConfig {
    private String token;
    private String name;
    private String chatgptApiKey;
    private String chatgptModel;
    private int chatgptTemperature;
    private int chatgptMaxTokens;
    private int chatgptFrequencyPenalty;
    private int chatgptPresencePenalty;
    private int chatgptTopP;
    private String chatgptPrompt;
    private int minMessageStack;
    private int maxMessageStack;
    private String minMessageStackNotReachedMessage;
    private String aboutMessage;
    private String inBotStartMessage;
    private String inChatStartNotAllowedMessage;
    private String inChatStartAllowedMessage;
    private String versionMessage;
    private String noMessageToDisplayMessage;
    private String notAllowedMessage;
    private String errorMessage;
    private Set<Long> allowedChatIds;
    private Integer maxRequestsPerNotAllowedChat;
    private Integer maxImageSize;
    private String somebodySentImagePrompt;
    private String somebodyCaptionedImagePrompt;
    private String describeImagePrompt;
    private String describeImageImmediatelyPrompt;
}
