package ru.novik.tgsqueezer.db.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.novik.tgsqueezer.db.model.ChatSettings;
import ru.novik.tgsqueezer.db.model.DefaultSettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
@Slf4j
public class ChatSettingsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DefaultSettingsRepository defaultSettingsRepository;

    public String getValue(String name, Long chatId) {
        String value = null;
        try {
            String query = "SELECT cs.value FROM chat_settings cs JOIN default_settings ds on ds.id = cs.default_settings_id WHERE name = ? AND chat_id = ?";
            value = jdbcTemplate.queryForObject(query, new Object[]{name, chatId}, new int[]{Types.VARCHAR, Types.BIGINT}, String.class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Calling getValue with name: {} and chatId: {} got empty result", name, chatId);
        }
        if (value == null) {
            log.info("Getting default value with name: {}", name);
            value = defaultSettingsRepository.getValue(name);
        }
        return value;
    }

    public int getMaxImageSize(Long chatId) {
        try {
            return Integer.parseInt(getValue("max_image_size", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getSomebodySentImagePrompt(Long chatId) {
        return getValue("somebody_sent_image_prompt", chatId);
    }

    public Integer getMaxRequestsPerNotAllowedChat(Long chatId) {
        try {
            return Integer.parseInt(getValue("max_requests_per_not_allowed_chat", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getInBotStartMessage(Long chatId) {
        return getValue("in_bot_start_message", chatId);
    }

    public Set<Long> getAllowedChatIds(Long chatId) {
        return Arrays.stream(getValue("allowed_chat_ids", chatId).split(","))
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public String getInChatStartAllowedMessage(Long chatId) {
        return getValue("in_chat_start_allowed_message", chatId);
    }

    public int getMinMessageStack(Long chatId) {
        try {
            return Integer.parseInt(getValue("min_message_stack", chatId));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    public int getMaxMessageStack(Long chatId) {
        try {
            return Integer.parseInt(getValue("max_message_stack", chatId));
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    public String getInChatStartNotAllowedMessage(Long chatId) {
        return getValue("in_chat_start_not_allowed_message", chatId);
    }

    public String getDescribeImagePrompt(Long chatId) {
        return getValue("describe_image_prompt", chatId);
    }

    public String getCommandToSqueeze(Long chatId) {
        return getValue("command_to_squeeze", chatId);
    }

    public String getVersionMessage(Long chatId) {
        return getValue("version_message", chatId);
    }

    public String getAboutMessage(Long chatId) {
        return getValue("about_message", chatId);
    }

    public String getNoMessageToDisplayMessage(Long chatId) {
        return getValue("no_message_to_display_message", chatId);
    }

    public String getMinMessageStackNotReachedMessage(Long chatId) {
        return getValue("min_message_stack_not_reached_message", chatId);
    }

    public String getChatgptApiKey(Long chatId) {
        return getValue("chatgpt_api_key", chatId);
    }

    public String getNotAllowedMessage(Long chatId) {
        return getValue("not_allowed_message", chatId);
    }

    public String getErrorMessage(Long chatId) {
        return getValue("error_message", chatId);
    }

    public long getImageFrequencyPerUserInMins(Long chatId) {
        try {
            return Long.parseLong(getValue("image_frequency_per_user_in_mins", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public long getImageFrequencyPerChatInMins(Long chatId) {
        try {
            return Long.parseLong(getValue("image_frequency_per_chat_in_mins", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getChatgptModel(Long chatId) {
        return getValue("chatgpt_model", chatId);
    }

    public String getChatgptPrompt(Long chatId) {
        return getValue("chatgpt_prompt", chatId);
    }

    public int getChatgptTemperature(Long chatId) {
        try {
            return Integer.parseInt(getValue("chatgpt_temperature", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getChatgptMaxTokens(Long chatId) {
        try {
            return Integer.parseInt(getValue("chatgpt_max_tokens", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getChatgptTopP(Long chatId) {
        try {
            return Integer.parseInt(getValue("chatgpt_top_p", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getChatgptFrequencyPenalty(Long chatId) {
        try {
            return Integer.parseInt(getValue("chatgpt_frequency_penalty", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getChatgptPresencePenalty(Long chatId) {
        try {
            return Integer.parseInt(getValue("chatgpt_presence_penalty", chatId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getDescribeImageImmediatelyPrompt(Long chatId) {
        return getValue("describe_image_immediately_prompt", chatId);
    }

    public List<ChatSettings> getAllByChatId(Long chatId) {
        try {
            String query = """
                    SELECT ch.id      AS "c_id",
                           ch.chat_id AS "ch_chat_id",
                           ch.value   AS "ch_value",
                           d.id       AS "d_id",
                           d.common   AS "d_common",
                           d.name     AS "d_name",
                           d.value    AS "d_value"
                    FROM chat_settings ch
                             LEFT JOIN default_settings d ON ch.default_settings_id = d.id
                    WHERE chat_id = ?;
                    """;
            return jdbcTemplate.query(query, new Object[]{chatId}, new int[]{Types.BIGINT}, new ChatSettingsRowMapper());
        } catch (EmptyResultDataAccessException e) {
            log.warn("Calling getAllByChatId with chatId: {} got empty result", chatId);
            return List.of();
        }
    }

    public long getNextId() {
        Long lastId = null;
        try {
            lastId = jdbcTemplate.queryForObject("SELECT seq FROM sqlite_sequence WHERE name = 'chat_settings'", Long.class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Calling getNextId got empty result");
        }
        return lastId == null ? 1 : lastId + 1;
    }

    public void save(ChatSettings chatSettings) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_settings WHERE chat_id = ? AND default_settings_id = ?",
                new Object[]{chatSettings.getChatId(), chatSettings.getDefaultSettings().getId()}, new int[]{Types.BIGINT, Types.BIGINT}, Integer.class);
        if (count != null && count > 0) {
            jdbcTemplate.update("UPDATE chat_settings SET value = ? WHERE chat_id = ? AND default_settings_id = ?",
                    chatSettings.getValue(), chatSettings.getChatId(), chatSettings.getDefaultSettings().getId());
        } else {
            jdbcTemplate.update("INSERT INTO chat_settings (id, chat_id, default_settings_id, value) VALUES (?, ?, ?, ?)",
                    chatSettings.getId(), chatSettings.getChatId(), chatSettings.getDefaultSettings().getId(), chatSettings.getValue());
        }
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM chat_settings WHERE id = ?", id);
    }

    public boolean isEmpty(Long chatId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_settings WHERE chat_id = ?", new Object[]{chatId}, new int[]{Types.BIGINT}, Integer.class);
        return count == null || count == 0;
    }

    public ChatSettings getByName(String name, long chatId) {
        return getAllByChatId(chatId).stream()
                .filter(chatSettings -> chatSettings.getDefaultSettings().getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static class ChatSettingsRowMapper implements RowMapper<ChatSettings> {
        @Override
        public ChatSettings mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatSettings chatSettings = new ChatSettings();
            chatSettings.setId(rs.getLong("c_id"));
            chatSettings.setChatId(rs.getLong("ch_chat_id"));
            chatSettings.setValue(rs.getString("ch_value"));
            DefaultSettings defaultSettings = new DefaultSettings();
            defaultSettings.setId(rs.getLong("d_id"));
            defaultSettings.setCommon(rs.getBoolean("d_common"));
            defaultSettings.setName(rs.getString("d_name"));
            defaultSettings.setValue(rs.getString("d_value"));
            chatSettings.setDefaultSettings(defaultSettings);
            return chatSettings;
        }
    }
}
