package ru.novik.tgsqueezer.db.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "defaultSettings")
public class ChatSettings {
    private long id;
    private DefaultSettings defaultSettings;
    private Long chatId;
    private String value;

    @Override
    public String toString() {
        return defaultSettings.getName() + (defaultSettings.isCommon() ? " - common" : "");
    }
}
