CREATE TABLE IF NOT EXISTS message (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    message_id INT NOT NULL,
    chat_id INT NOT NULL,
    user_id INT NOT NULL,
    username VARCHAR(128) NOT NULL,
    time TIMESTAMP NOT NULL,
    message VARCHAR(4096),
    caption VARCHAR(2048),
    image TEXT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS default_settings (
    id INTEGER PRIMARY KEY,
    common BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR(128) NOT NULL UNIQUE,
    value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    default_settings_id INTEGER,
    chat_id INT NOT NULL,
    value TEXT NOT NULL
);