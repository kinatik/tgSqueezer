package ru.novik.tgsqueezer.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
    private long imageFrequencyPerUserInMins = 0L;
    private long imageFrequencyPerChatInMins = 0L;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("\nBotConfig");
        sb.append("\nname='").append(name).append('\'');
        sb.append("\nchatgptModel='").append(chatgptModel).append('\'');
        sb.append("\nchatgptTemperature=").append(chatgptTemperature);
        sb.append("\nchatgptMaxTokens=").append(chatgptMaxTokens);
        sb.append("\nchatgptFrequencyPenalty=").append(chatgptFrequencyPenalty);
        sb.append("\nchatgptPresencePenalty=").append(chatgptPresencePenalty);
        sb.append("\nchatgptTopP=").append(chatgptTopP);
        sb.append("\nchatgptPrompt='").append(chatgptPrompt).append('\'');
        sb.append("\nminMessageStack=").append(minMessageStack);
        sb.append("\nmaxMessageStack=").append(maxMessageStack);
        sb.append("\nminMessageStackNotReachedMessage='").append(minMessageStackNotReachedMessage).append('\'');
        sb.append("\naboutMessage='").append(aboutMessage).append('\'');
        sb.append("\ninBotStartMessage='").append(inBotStartMessage).append('\'');
        sb.append("\ninChatStartNotAllowedMessage='").append(inChatStartNotAllowedMessage).append('\'');
        sb.append("\ninChatStartAllowedMessage='").append(inChatStartAllowedMessage).append('\'');
        sb.append("\nversionMessage='").append(versionMessage).append('\'');
        sb.append("\nnoMessageToDisplayMessage='").append(noMessageToDisplayMessage).append('\'');
        sb.append("\nnotAllowedMessage='").append(notAllowedMessage).append('\'');
        sb.append("\nerrorMessage='").append(errorMessage).append('\'');
        sb.append("\nallowedChatIds=").append(allowedChatIds);
        sb.append("\nmaxRequestsPerNotAllowedChat=").append(maxRequestsPerNotAllowedChat);
        sb.append("\nmaxImageSize=").append(maxImageSize);
        sb.append("\nsomebodySentImagePrompt='").append(somebodySentImagePrompt).append('\'');
        sb.append("\nsomebodyCaptionedImagePrompt='").append(somebodyCaptionedImagePrompt).append('\'');
        sb.append("\ndescribeImagePrompt='").append(describeImagePrompt).append('\'');
        sb.append("\ndescribeImageImmediatelyPrompt='").append(describeImageImmediatelyPrompt).append('\'');
        sb.append("\nimageFrequencyPerUserInMins=").append(imageFrequencyPerUserInMins);
        sb.append("\nimageFrequencyPerChatInMins=").append(imageFrequencyPerChatInMins);
        return sb.toString();
    }
}
