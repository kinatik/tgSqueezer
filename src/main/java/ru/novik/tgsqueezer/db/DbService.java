package ru.novik.tgsqueezer.db;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.novik.tgsqueezer.db.model.ChatSettings;
import ru.novik.tgsqueezer.db.model.DefaultSettings;
import ru.novik.tgsqueezer.db.repository.ChatSettingsRepository;
import ru.novik.tgsqueezer.db.repository.DefaultSettingsRepository;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class DbService {

    private final ChatSettingsRepository chatSettingsRepository;
    private final DefaultSettingsRepository defaultSettingsRepository;

    public void start(Long chatId) {
        Set<ChatSettings> allChatSettings = Set.copyOf(chatSettingsRepository.getAllByChatId(chatId));
        Set<DefaultSettings> allDefaultSettings = Set.copyOf(defaultSettingsRepository.getAll(false));
        allDefaultSettings.forEach(defaultSettings -> {
            if (allChatSettings.stream().noneMatch(chatSettings -> chatSettings.getDefaultSettings().equals(defaultSettings))) {
                ChatSettings chatSettings = new ChatSettings();
                chatSettings.setId(chatSettingsRepository.getNextId());
                chatSettings.setChatId(chatId);
                chatSettings.setDefaultSettings(defaultSettings);
                chatSettings.setValue(defaultSettings.getValue());
                chatSettingsRepository.save(chatSettings);
            }
        });
        allChatSettings.forEach(chatSettings -> {
            if (allDefaultSettings.stream().noneMatch(defaultSettings -> chatSettings.getDefaultSettings().equals(defaultSettings))) {
                chatSettingsRepository.delete(chatSettings.getId());
            }
        });
    }

    public boolean isEmpty(Long chatId) {
        return chatSettingsRepository.isEmpty(chatId);
    }

    public String getDefaultSettings() {
        List<DefaultSettings> all = defaultSettingsRepository.getAll();
        return all.stream()
                .map(DefaultSettings::toString)
                .sorted()
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse("No default settings");
    }

    public String getDefaultSetting(String defaultSettingName) {
        String value = defaultSettingsRepository.getValue(defaultSettingName);
        return value == null ? "No default setting with name: " + defaultSettingName : value;
    }

    public String setDefaultSetting(String defaultSettingName, String defaultSettingValue) {
        DefaultSettings defaultSettings = defaultSettingsRepository.getByName(defaultSettingName);
        if (defaultSettings == null) {
            return "No default setting with name: " + defaultSettingName;
        }
        defaultSettings.setValue(defaultSettingValue);
        defaultSettingsRepository.save(defaultSettings);
        return "Default setting with name: " + defaultSettingName + " set to value: " + defaultSettingValue;
    }

    public String getChatSettings(String chatIdString) {
        return chatSettingsRepository.getAllByChatId(Long.parseLong(chatIdString)).stream()
                .map(ChatSettings::toString)
                .sorted()
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse("No chat settings for chatId: " + chatIdString);
    }

    public String getChatSetting(String chatIdString, String chatSettingName) {
        String value = chatSettingsRepository.getValue(chatSettingName, Long.parseLong(chatIdString));
        return value == null ? "No chat setting with name: " + chatSettingName + " for chatId: " + chatIdString : value;
    }

    public String setChatSetting(String chatId, String name, String value) {
        ChatSettings chatSettings = chatSettingsRepository.getByName(name, Long.parseLong(chatId));
        if (chatSettings == null) {
            return "No chat setting with name: " + name + " for chatId: " + chatId;
        }
        chatSettings.setValue(value);
        chatSettingsRepository.save(chatSettings);
        return "Chat setting with name: " + name + " for chatId: " + chatId + " set to value: " + value;
    }
}
