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
public class GptResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<GptChoice> choices;
    private GptUsage usage;
    @SerializedName("system_fingerprint")
    private String systemFingerprint;
}
