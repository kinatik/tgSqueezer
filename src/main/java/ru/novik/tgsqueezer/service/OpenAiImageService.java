package ru.novik.tgsqueezer.service;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.db.repository.ChatSettingsRepository;
import ru.novik.tgsqueezer.model.*;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class OpenAiImageService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final ChatSettingsRepository chatSettingsRepository;
    private final OkHttpClient client;

    private static final Gson gson = new Gson();

    public String describe(String conversation, Long chatId) throws IOException {
        GptImageRequest gptRequest = getRequest(conversation, chatId);
        String json = gson.toJson(gptRequest);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + chatSettingsRepository.getChatgptApiKey(chatId))
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

    private GptImageRequest getRequest(String base64, Long chatId) {
        return GptImageRequest.builder()
                .model(chatSettingsRepository.getChatgptModel(chatId))
                .messages(List.of(
                        GptImageMessage.builder()
                                .role("user")
                                .content(List.of(
                                        GptImageContent.builder()
                                                .type("text")
                                                .text(chatSettingsRepository.getDescribeImagePrompt(chatId))
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
                .maxTokens(chatSettingsRepository.getChatgptMaxTokens(chatId))
                .build();
    }

    private GptResponse getResponse(String json) {
        return gson.fromJson(json, GptResponse.class);
    }
}
