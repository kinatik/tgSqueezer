CREATE TABLE IF NOT EXISTS message (
    id BIGINT PRIMARY KEY,
    message_id INT NOT NULL,
    chat_id INT NOT NULL,
    user_id INT NOT NULL,
    time TIMESTAMP NOT NULL,
    message VARCHAR(4096),
    capture VARCHAR(2048),
    image TEXT NULL
);