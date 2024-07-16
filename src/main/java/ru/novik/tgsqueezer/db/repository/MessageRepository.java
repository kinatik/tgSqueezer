package ru.novik.tgsqueezer.db.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.novik.tgsqueezer.db.model.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MessageRepository {

    private final JdbcTemplate jdbcTemplate;

    public MessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class MessageRowMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Message message = new Message();
            message.setId(rs.getLong("id"));
            message.setMessageId(rs.getInt("message_id"));
            message.setChatId(rs.getLong("chat_id"));
            message.setUserId(rs.getLong("user_id"));
            message.setTime(rs.getTimestamp("time"));
            message.setMessage(rs.getString("message"));
            message.setImage(rs.getString("image"));
            return message;
        }
    }

    public List<Message> findAll() {
        return jdbcTemplate.query("SELECT * FROM message", new MessageRowMapper());
    }

    public void insertMessage(Message message) {
        String sql = "INSERT INTO message (id, message_id, chat_id, user_id, username, time, message, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, getNextMessageId(), message.getMessageId(), message.getChatId(), message.getUserId(), message.getUsername(), message.getTime(), message.getMessage(), message.getImage());
    }

    public Message findById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM message WHERE id = ?",
                new Object[]{id},
                new MessageRowMapper());
    }

    public void updateMessage(Message message) {
        String sql = "UPDATE message SET chat_id = ?, user_id = ?, time = ?, message = ?, image = ? WHERE id = ?";
        jdbcTemplate.update(sql, message.getChatId(), message.getUserId(), message.getTime(), message.getMessage(), message.getImage(), message.getId());
    }

    public void deleteMessage(long id) {
        jdbcTemplate.update("DELETE FROM message WHERE id = ?", id);
    }

    public long getNextMessageId() {
        return jdbcTemplate.queryForObject("SELECT sqlite_sequence.seq FROM sqlite_sequence WHERE name = 'message'", Long.class) + 1;
    }
}
