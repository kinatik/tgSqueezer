package ru.novik.tgsqueezer.db.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class Message {
    private long id;
    private Integer messageId;
    private Long chatId;
    private Long userId;
    private String username;
    private Timestamp time;
    private String message;
    private String caption;
    private String image;
    private boolean read;
}

