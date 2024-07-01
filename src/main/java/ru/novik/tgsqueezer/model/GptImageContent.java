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
public class GptImageContent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String type;
    private String text;
    @SerializedName("image_url")
    private GptImageUrl imageUrl;
}
