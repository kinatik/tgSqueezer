package ru.novik.tgsqueezer.service;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.db.repository.ChatSettingsRepository;
import ru.novik.tgsqueezer.model.GptMessage;
import ru.novik.tgsqueezer.model.GptRequest;
import ru.novik.tgsqueezer.model.GptResponse;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class OpenAiService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final ChatSettingsRepository settings;
    private final OkHttpClient client;

    private static final Gson gson = new Gson();

    public String summarize(String apiKey, String conversation, Long chatId) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        GptRequest gptRequest = getRequest(conversation, chatId);
        String json = gson.toJson(gptRequest);
        log.info("Request to Chat GPT: {}", json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
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

    private GptRequest getRequest(String content, Long chatId) {
        return GptRequest.builder()
                .model(settings.getChatgptModel(chatId))
                .messages(List.of(
                        GptMessage.builder()
                                .role("system")
                                .content(settings.getChatgptPrompt(chatId))
                                .build(),
                        GptMessage.builder()
                                .role("user")
                                .content(content)
                                .build()
                ))
                .temperature(settings.getChatgptTemperature(chatId))
                .maxTokens(settings.getChatgptMaxTokens(chatId))
                .topP(settings.getChatgptTopP(chatId))
                .frequencyPenalty(settings.getChatgptFrequencyPenalty(chatId))
                .presencePenalty(settings.getChatgptPresencePenalty(chatId))
                .build();
    }

    private GptResponse getResponse(String json) {
        return gson.fromJson(json, GptResponse.class);
    }
}
