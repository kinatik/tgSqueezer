package ru.novik.tgsqueezer.db.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "name")
public class DefaultSettings {
    private long id;
    private boolean common;
    private String name;
    private String value;

    @Override
    public String toString() {
        return name + (common ? " - common" : "");
    }
}
