package ru.novik.tgsqueezer.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class GptImageMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String role;
    private List<GptImageContent> content;
}
