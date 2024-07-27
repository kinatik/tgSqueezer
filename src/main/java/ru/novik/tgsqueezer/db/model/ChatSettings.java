package ru.novik.tgsqueezer.db.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(of = "defaultSettings")
@AllArgsConstructor
@NoArgsConstructor
public class ChatSettings {
    private long id;
    private Long chatId;
    private DefaultSettings defaultSettings;
    private String value;

    @Override
    public String toString() {
        return defaultSettings.getName() + (defaultSettings.isCommon() ? " - common" : "");
    }
}
