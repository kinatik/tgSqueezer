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
public class GptRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String model;
    @SerializedName("temperature")
    private double temperature;
    @SerializedName("max_tokens")
    private int maxTokens;
    @SerializedName("top_p")
    private double topP;
    @SerializedName("frequency_penalty")
    private double frequencyPenalty;
    @SerializedName("presence_penalty")
    private double presencePenalty;
    private List<GptMessage> messages;
}
