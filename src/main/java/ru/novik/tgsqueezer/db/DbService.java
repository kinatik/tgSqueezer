package ru.novik.tgsqueezer.db;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.novik.tgsqueezer.db.model.ChatSettings;
import ru.novik.tgsqueezer.db.model.DefaultSettings;
import ru.novik.tgsqueezer.db.repository.ChatSettingsRepository;
import ru.novik.tgsqueezer.db.repository.DefaultSettingsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import static ru.novik.tgsqueezer.db.Settings.*;

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

    public String getChatSetting(String chatNumString, String settingNum) {
        try {
            ChatSettings chatSettings = getChatSettings(chatNumString, settingNum);
            return chatSettings.getDefaultSettings().getName() + ": " + chatSettings.getValue();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

    }

    public String setChatSetting(String chatNumString, String settingNum, String value) {
        try {
            ChatSettings chatSettings = getChatSettings(chatNumString, settingNum);
            chatSettings.setValue(value);
            validateValue(chatSettings);
            chatSettingsRepository.save(chatSettings);
            return "Chat setting with name: " + chatSettings.getDefaultSettings().getName() + " set to value: " + chatSettings.getValue();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private void validateValue(ChatSettings chatSettings) {
        boolean needToValidate = chatSettings.getDefaultSettings().getName().equals(chatgpt_top_p.name()) ||
                chatSettings.getDefaultSettings().getName().equals(chatgpt_temperature.name()) ||
                chatSettings.getDefaultSettings().getName().equals(chatgpt_frequency_penalty.name()) ||
                chatSettings.getDefaultSettings().getName().equals(chatgpt_presence_penalty.name());
        if (!needToValidate) {
            return;
        }

        BigDecimal bigDecimal = new BigDecimal(chatSettings.getValue());
        bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
        if (chatSettings.getDefaultSettings().getName().equals(chatgpt_top_p.name())) {
            if (bigDecimal.abs().compareTo(new BigDecimal(2)) > 0) {
                throw new IllegalArgumentException("top_p should be between -2.0 and 2.0");
            }
        }
        boolean isValid = bigDecimal.compareTo(BigDecimal.ZERO) >= 0 && bigDecimal.compareTo(BigDecimal.ONE) <= 0;
        if (chatSettings.getDefaultSettings().getName().equals(chatgpt_temperature.name())) {
            if (!isValid) {
                throw new IllegalArgumentException("Temperature should be between 0 and 1.0");
            }
        }
        if (chatSettings.getDefaultSettings().getName().equals(chatgpt_frequency_penalty.name())) {
            if (!isValid) {
                throw new IllegalArgumentException("Frequency penalty should be between 0 and 1.0");
            }
            chatSettings.setValue(bigDecimal.toString());
        }
        if (chatSettings.getDefaultSettings().getName().equals(chatgpt_presence_penalty.name())) {
            if (!isValid) {
                throw new IllegalArgumentException("Presence penalty should be between 0 and 1.0");
            }
        }
        chatSettings.setValue(bigDecimal.toString());
    }

    private ChatSettings getChatSettings(String chatNumString, String settingNum) {
        if (chatNumString == null || chatNumString.isEmpty()) {
            throw new IllegalArgumentException("No chat number provided");
        }
        if (settingNum == null || settingNum.isEmpty()) {
            throw new IllegalArgumentException("No chat setting number provided");
        }

        int chatNumber = Integer.parseInt(chatNumString);
        List<Long> chatIds = chatSettingsRepository.getAllChatIds();
        if (chatNumber < 0 || chatNumber > chatIds.size()) {
            throw new IllegalArgumentException("No chat with number: " + chatNumString);
        }
        Long chatId = chatIds.get(chatNumber - 1);

        int chatSettingNumber = Integer.parseInt(settingNum);
        List<ChatSettings> all = chatSettingsRepository.getAllByChatId(chatId);
        if (chatSettingNumber < 0 || chatSettingNumber > all.size()) {
            throw new IllegalArgumentException("No chat setting with number: " + settingNum);
        }
        return all.get(chatSettingNumber - 1);
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
