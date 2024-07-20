package ru.novik.tgsqueezer.db.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.novik.tgsqueezer.db.model.DefaultSettings;

import java.sql.Types;
import java.util.List;

@Repository
@AllArgsConstructor
@Slf4j
public class DefaultSettingsRepository {

    private final JdbcTemplate jdbcTemplate;

    public String getValue(String name) {
        try {
            return jdbcTemplate.queryForObject("SELECT value FROM default_settings WHERE name = ?", new Object[]{name}, new int[]{Types.VARCHAR}, String.class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Calling getValue with name: {} got empty result", name);
            return null;
        }
    }

    public List<DefaultSettings> getAll(boolean b) {
        try {
            return jdbcTemplate.query("SELECT * FROM default_settings WHERE common = ?", new DefaultSettingsRowMapper(), b);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Calling getAll with common: {} got empty result", b);
            return List.of();
        }
    }

    public List<DefaultSettings> getAll() {
        try {
            return jdbcTemplate.query("SELECT * FROM default_settings", new DefaultSettingsRowMapper());
        } catch (EmptyResultDataAccessException e) {
            log.warn("Calling getAll got empty result");
            return List.of();
        }
    }

    public DefaultSettings getByName(String defaultSettingName) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM default_settings WHERE name = ?", new DefaultSettingsRowMapper(), defaultSettingName);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Calling getByName with name: {} got empty result", defaultSettingName);
            return null;
        }
    }

    public void save(DefaultSettings defaultSettings) {
        jdbcTemplate.update("UPDATE default_settings SET value = ? WHERE id = ?", defaultSettings.getValue(), defaultSettings.getId());
    }


    private static class DefaultSettingsRowMapper implements RowMapper<DefaultSettings> {
        @Override
        public DefaultSettings mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            DefaultSettings defaultSettings = new DefaultSettings();
            defaultSettings.setId(rs.getLong("id"));
            defaultSettings.setCommon(rs.getBoolean("common"));
            defaultSettings.setName(rs.getString("name"));
            defaultSettings.setValue(rs.getString("value"));
            return defaultSettings;
        }
    }

}
