package ru.novik.tgsqueezer.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class GptImageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String model;
    @SerializedName("max_tokens")
    private int maxTokens;
    private List<GptImageMessage> messages;
}
