package ru.novik.tgsqueezer.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
public class GptChoice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int index;
    private GptMessage message;
    private String logprobs;
    private String finishReason;
}
