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
        List<DefaultSettings> all = defaultSettingsRepository.getAllSorted();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            sb.append(String.format("%d. ", i + 1)).append(all.get(i)).append("\n");
        }

        return sb.toString();
    }

    public String getDefaultSetting(String defaultSettingNum) {
        List<DefaultSettings> all = defaultSettingsRepository.getAllSorted();
        if (Integer.parseInt(defaultSettingNum) < 1 || Integer.parseInt(defaultSettingNum) > all.size()) {
            return "No default setting with number: " + defaultSettingNum;
        }
        DefaultSettings defaultSettings = all.get(Integer.parseInt(defaultSettingNum) - 1);

        return defaultSettings.getName() + ": " + defaultSettings.getValue();
    }

    public String setDefaultSetting(String defaultSettingNum, String defaultSettingValue) {
        List<DefaultSettings> all = defaultSettingsRepository.getAllSorted();
        if (Integer.parseInt(defaultSettingNum) < 1 || Integer.parseInt(defaultSettingNum) > all.size()) {
            return "No default setting with number: " + defaultSettingNum;
        }
        DefaultSettings defaultSettings = all.get(Integer.parseInt(defaultSettingNum) - 1);
        defaultSettings.setValue(defaultSettingValue);
        defaultSettingsRepository.save(defaultSettings);
        return "Default setting with name: " + defaultSettings.getName() + " set to value: " + defaultSettingValue;
    }

    public String getChatSettings(String chatNumberString) {
        if (chatNumberString == null || chatNumberString.isEmpty()) {
            return "No chat number provided";
        }
        int chatNumber = Integer.parseInt(chatNumberString);
        List<Long> chatIds = chatSettingsRepository.getAllChatIds();
        if (chatNumber < 0 || chatNumber > chatIds.size()) {
            return "No chat with number: " + chatNumberString;
        }
        Long chatId = chatIds.get(chatNumber - 1);

        List<ChatSettings> all = chatSettingsRepository.getAllByChatId(chatId);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            sb.append(String.format("%d. ", i + 1)).append(all.get(i)).append("\n");
        }
        return sb.toString();
    }

    public String getChatSetting(String chatNumberString, String chatSettingNum) {
        if (chatNumberString == null || chatNumberString.isEmpty()) {
            return "No chat number provided";
        }
        if (chatSettingNum == null || chatSettingNum.isEmpty()) {
            return "No chat setting number provided";
        }

        int chatNumber = Integer.parseInt(chatNumberString);
        List<Long> chatIds = chatSettingsRepository.getAllChatIds();
        if (chatNumber < 0 || chatNumber > chatIds.size()) {
            return "No chat with number: " + chatNumberString;
        }
        Long chatId = chatIds.get(chatNumber - 1);

        int chatSettingNumber = Integer.parseInt(chatSettingNum);
        List<ChatSettings> all = chatSettingsRepository.getAllByChatId(chatId);
        if (chatSettingNumber < 0 || chatSettingNumber > all.size()) {
            return "No chat setting with number: " + chatSettingNum;
        }
        ChatSettings chatSettings = all.get(chatSettingNumber - 1);

        return chatSettings.getDefaultSettings().getName() + ": " + chatSettings.getValue();
    }

    public String setChatSetting(String chatNumString, String settingNum, String value) {
        if (chatNumString == null || chatNumString.isEmpty()) {
            return "No chat number provided";
        }
        if (settingNum == null || settingNum.isEmpty()) {
            return "No chat setting number provided";
        }
        int chatNumber = Integer.parseInt(chatNumString);
        List<Long> chatIds = chatSettingsRepository.getAllChatIds();
        if (chatNumber < 0 || chatNumber > chatIds.size()) {
            return "No chat with number: " + chatNumString;
        }
        Long chatId = chatIds.get(chatNumber - 1);

        int settingNumber = Integer.parseInt(settingNum);
        List<ChatSettings> all = chatSettingsRepository.getAllByChatId(chatId);
        if (settingNumber < 0 || settingNumber > all.size()) {
            return "No chat setting with number: " + settingNum;
        }
        ChatSettings chatSettings = all.get(settingNumber - 1);
        chatSettings.setValue(value);

        chatSettingsRepository.save(chatSettings);
        return "Chat setting with name: " + chatSettings.getDefaultSettings().getName() + " for chatId: " + chatId + " set to value: " + value;
    }

    public String getChatIds() {
        List<Long> all = chatSettingsRepository.getAllChatIds();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            sb.append(String.format("%d. ", i + 1)).append(all.get(i)).append("\n");
        }
        return sb.toString();
    }
}
