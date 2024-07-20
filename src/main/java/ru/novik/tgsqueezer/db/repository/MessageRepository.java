package ru.novik.tgsqueezer.db.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.novik.tgsqueezer.db.model.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

@Repository
@AllArgsConstructor
@Slf4j
public class MessageRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Message> getUnreadMessages(long chatId) {
        return jdbcTemplate.query("SELECT * FROM message WHERE chat_id = ? AND read = 0 ORDER BY time", new MessageRowMapper(), chatId);
    }

    public void setMessagesRead(long chatId) {
        jdbcTemplate.update("UPDATE message SET read = 1 WHERE chat_id = ?", chatId);
    }

    public List<Message> getLastMessages(long chatId, Integer count) {
        List<Message> messages = null;
        try {
            messages = jdbcTemplate.query("SELECT * FROM message WHERE chat_id = ? ORDER BY time DESC LIMIT ?", new MessageRowMapper(), chatId, count);
        } catch (Exception e) {
            log.warn("Error while getting last messages", e);
        }
        if (messages == null) {
            return List.of();
        }
        return messages.stream().sorted(Comparator.comparing(Message::getTime)).toList();
    }

    public void editMessage(Long chatId, Integer messageId, String messageText) {
        jdbcTemplate.update("UPDATE message SET message = ? WHERE chat_id = ? AND message_id = ?", messageText, chatId, messageId);
    }

    public void editMessageCaption(Long chatId, Integer messageId, String caption) {
        jdbcTemplate.update("UPDATE message SET caption = ? WHERE chat_id = ? AND message_id = ?", caption, chatId, messageId);
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Message message = new Message();
            message.setId(rs.getLong("id"));
            message.setMessageId(rs.getInt("message_id"));
            message.setChatId(rs.getLong("chat_id"));
            message.setUserId(rs.getLong("user_id"));
            message.setUsername(rs.getString("username"));
            message.setTime(rs.getTimestamp("time"));
            message.setMessage(rs.getString("message"));
            message.setCaption(rs.getString("caption"));
            message.setImage(rs.getString("image"));
            message.setRead(rs.getBoolean("read"));
            return message;
        }
    }

    public void insertMessage(Message message) {
        String sql = "INSERT INTO message (id, message_id, chat_id, user_id, username, time, message, caption, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, getNextMessageId(), message.getMessageId(), message.getChatId(), message.getUserId(), message.getUsername(), message.getTime(), message.getMessage(), message.getImage());
    }

    public long getNextMessageId() {
        return jdbcTemplate.queryForObject("SELECT sqlite_sequence.seq FROM sqlite_sequence WHERE name = 'message'", Long.class) + 1;
    }
}
