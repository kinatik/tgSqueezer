package ru.novik.tgsqueezer.service;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ru.novik.tgsqueezer.config.BotConfig;
import ru.novik.tgsqueezer.model.GptMessage;
import ru.novik.tgsqueezer.model.GptRequest;
import ru.novik.tgsqueezer.model.GptResponse;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class OpenAiService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final BotConfig botConfig;

    private static final Gson gson = new Gson();

    public String summarize(String apiKey, String conversation) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        GptRequest gptRequest = getRequest(conversation);
        String json = gson.toJson(gptRequest);

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

    private GptRequest getRequest(String content) {
        return GptRequest.builder()
                .model(botConfig.getChatgptModel())
                .messages(List.of(
                        GptMessage.builder()
                                .role("system")
                                .content(botConfig.getChatgptPrompt())
                                .build(),
                        GptMessage.builder()
                                .role("user")
                                .content(content)
                                .build()
                ))
                .temperature(botConfig.getChatgptTemperature())
                .maxTokens(botConfig.getChatgptMaxTokens())
                .topP(botConfig.getChatgptTopP())
                .frequencyPenalty(botConfig.getChatgptFrequencyPenalty())
                .presencePenalty(botConfig.getChatgptPresencePenalty())
                .build();
    }

    private GptResponse getResponse(String json) {
        return gson.fromJson(json, GptResponse.class);
    }
}
