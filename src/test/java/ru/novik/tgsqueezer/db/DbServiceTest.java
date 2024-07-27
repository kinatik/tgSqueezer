package ru.novik.tgsqueezer.db;

import org.junit.jupiter.api.Test;
import ru.novik.tgsqueezer.db.model.ChatSettings;
import ru.novik.tgsqueezer.db.model.DefaultSettings;
import ru.novik.tgsqueezer.db.repository.ChatSettingsRepository;
import ru.novik.tgsqueezer.db.repository.DefaultSettingsRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DbServiceTest {

    private final ChatSettingsRepository chatSettingsRepository = mock(ChatSettingsRepository.class);
    private final DefaultSettingsRepository defaultSettingsRepository = mock(DefaultSettingsRepository.class);
    private final DbService dbService = new DbService(chatSettingsRepository, defaultSettingsRepository);

    @Test
    void getDefaultSettings_returnsFormattedDefaultSettings() {
        List<DefaultSettings> defaultSettingsList = List.of(
                new DefaultSettings("Setting1", "Value1"),
                new DefaultSettings("Setting2", "Value2")
        );
        when(defaultSettingsRepository.getAllSorted()).thenReturn(defaultSettingsList);

        String result = dbService.getDefaultSettings();

        assertEquals("1. Setting1\n2. Setting2\n", result);
    }

    @Test
    void getDefaultSetting_withValidNumber_returnsDefaultSetting() {
        List<DefaultSettings> defaultSettingsList = List.of(
                new DefaultSettings("Setting1", "Value1"),
                new DefaultSettings("Setting2", "Value2")
        );
        when(defaultSettingsRepository.getAllSorted()).thenReturn(defaultSettingsList);

        String result = dbService.getDefaultSetting("2");

        assertEquals("Setting2: Value2", result);
    }

    @Test
    void getDefaultSetting_withInvalidNumber_returnsErrorMessage() {
        List<DefaultSettings> defaultSettingsList = List.of(
                new DefaultSettings("Setting1", "Value1")
        );
        when(defaultSettingsRepository.getAllSorted()).thenReturn(defaultSettingsList);

        String result = dbService.getDefaultSetting("2");

        assertEquals("No default setting with number: 2", result);
    }

    @Test
    void setDefaultSetting_withValidNumber_updatesDefaultSetting() {
        List<DefaultSettings> defaultSettingsList = List.of(
                new DefaultSettings("Setting1", "Value1"),
                new DefaultSettings("Setting2", "Value2")
        );
        when(defaultSettingsRepository.getAllSorted()).thenReturn(defaultSettingsList);

        String result = dbService.setDefaultSetting("2", "NewValue");

        assertEquals("Default setting with name: Setting2 set to value: NewValue", result);
        verify(defaultSettingsRepository).save(defaultSettingsList.get(1));
    }

    @Test
    void setDefaultSetting_withInvalidNumber_returnsErrorMessage() {
        List<DefaultSettings> defaultSettingsList = List.of(
                new DefaultSettings("Setting1", "Value1")
        );
        when(defaultSettingsRepository.getAllSorted()).thenReturn(defaultSettingsList);

        String result = dbService.setDefaultSetting("2", "NewValue");

        assertEquals("No default setting with number: 2", result);
    }

    @Test
    void getChatSettings_withValidNumber_returnsFormattedChatSettings() {
        List<Long> chatIds = List.of(1L, 2L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("Setting1", "Value1"), "Value1"),
                new ChatSettings(2L, 1L, new DefaultSettings("Setting2", "Value2"), "Value2")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.getChatSettings("1");

        assertEquals("1. Setting1\n2. Setting2\n", result);
    }

    @Test
    void getChatSettings_withInvalidNumber_returnsErrorMessage() {
        List<Long> chatIds = List.of(1L);
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);

        String result = dbService.getChatSettings("2");

        assertEquals("No chat with number: 2", result);
    }

    @Test
    void getChatSetting_withValidNumbers_returnsChatSetting() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("Setting1", "Value1"), "Value1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.getChatSetting("1", "1");

        assertEquals("Setting1: Value1", result);
    }

    @Test
    void getChatSetting_withInvalidChatNumber_returnsErrorMessage() {
        List<Long> chatIds = List.of(1L);
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);

        String result = dbService.getChatSetting("2", "1");

        assertEquals("No chat with number: 2", result);
    }

    @Test
    void getChatSetting_withInvalidSettingNumber_returnsErrorMessage() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("Setting1", "Value1"), "Value1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.getChatSetting("1", "2");

        assertEquals("No chat setting with number: 2", result);
    }

    @Test
    void setChatSetting_withValidNumbers_updatesChatSetting() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("Setting1", "Value1"), "Value1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "NewValue");

        assertEquals("Chat setting with name: Setting1 set to value: NewValue", result);
        verify(chatSettingsRepository).save(chatSettingsList.get(0));
    }

    @Test
    void setChatSetting_withInvalidChatNumber_returnsErrorMessage() {
        List<Long> chatIds = List.of(1L);
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);

        String result = dbService.setChatSetting("2", "1", "NewValue");

        assertEquals("No chat with number: 2", result);
    }

    @Test
    void setChatSetting_withInvalidSettingNumber_returnsErrorMessage() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("Setting1", "Value1"), "Value1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "2", "NewValue");

        assertEquals("No chat setting with number: 2", result);
    }

    @Test
    void setChatSetting_withValid_chatgpt_top_p_updatesChatSetting() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("chatgpt_top_p", "1"), "1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "1.5");

        assertEquals("Chat setting with name: chatgpt_top_p set to value: 1.5", result);
    }

    @Test
    void setChatSetting_withInvalid_chatgpt_top_p_returnsErrorMessage() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("chatgpt_top_p", "1"), "1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "2.5");

        assertEquals("top_p should be between -2.0 and 2.0", result);
    }

    @Test
    void setChatSetting_withValid_negative_chatgpt_top_p_updatesChatSetting() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("chatgpt_top_p", "1"), "1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "-1.5");

        assertEquals("Chat setting with name: chatgpt_top_p set to value: -1.5", result);
    }

    @Test
    void setChatSetting_withValid_chatgpt_temperature_updatesChatSetting() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("chatgpt_temperature", "1"), "1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "0.3");

        assertEquals("Chat setting with name: chatgpt_temperature set to value: 0.3", result);
    }

    @Test
    void setChatSetting_withInvalid_chatgpt_temperature_returnsErrorMessage() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("chatgpt_temperature", "1"), "1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "-0.3");

        assertEquals("Temperature should be between 0 and 1.0", result);
    }

    @Test
    void setChatSetting_withValid_roundHalfUp_chatgpt_frequency_penalty_updatesChatSetting() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("chatgpt_frequency_penalty", "1"), "1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "0.95");

        assertEquals("Chat setting with name: chatgpt_frequency_penalty set to value: 1.0", result);
    }

    @Test
    void setChatSetting_withValid_roundHalfUp_chatgpt_presence_penalty_updatesChatSetting() {
        List<Long> chatIds = List.of(1L);
        List<ChatSettings> chatSettingsList = List.of(
                new ChatSettings(1L, 1L, new DefaultSettings("chatgpt_presence_penalty", "1"), "1")
        );
        when(chatSettingsRepository.getAllChatIds()).thenReturn(chatIds);
        when(chatSettingsRepository.getAllByChatId(1L)).thenReturn(chatSettingsList);

        String result = dbService.setChatSetting("1", "1", "1.04");

        assertEquals("Chat setting with name: chatgpt_presence_penalty set to value: 1.0", result);
    }


}