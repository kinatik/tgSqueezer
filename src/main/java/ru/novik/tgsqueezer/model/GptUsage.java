package ru.novik.tgsqueezer.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
public class GptUsage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @SerializedName("prompt_tokens")
    private int promptTokens;
    @SerializedName("completion_tokens")
    private int completionTokens;
    @SerializedName("total_tokens")
    private int totalTokens;
}
