package ru.novik.tgsqueezer.service;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.model.*;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class OpenAiImageService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final BotConfig botConfig;

    private static final Gson gson = new Gson();

    public String describe(String conversation) throws IOException {
        GptImageRequest gptRequest = getRequest(conversation);
        String json = gson.toJson(gptRequest);
        log.info("Request to Chat GPT: {}", json);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + botConfig.getChatgptApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            if (response.body() == null) {
                throw new IOException("Response body is null");
            }
            return getResponse(response.body().string()).getChoices().get(0).getMessage().getContent();
        }
    }

    private GptImageRequest getRequest(String base64) {
        return GptImageRequest.builder()
                .model(botConfig.getChatgptModel())
                .messages(List.of(
                        GptImageMessage.builder()
                                .role("user")
                                .content(List.of(
                                        GptImageContent.builder()
                                                .type("text")
                                                .text(botConfig.getDescribeImagePrompt())
                                                .build(),
                                        GptImageContent.builder()
                                                .type("image_url")
                                                .imageUrl(GptImageUrl.builder()
                                                        .url(String.format("data:image/jpeg;base64,{%s}", base64))
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .maxTokens(300)
                .build();
    }

    private GptResponse getResponse(String json) {
        return gson.fromJson(json, GptResponse.class);
    }
}
