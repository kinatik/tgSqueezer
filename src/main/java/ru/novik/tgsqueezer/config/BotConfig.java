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
    private Long superUserId;
}
