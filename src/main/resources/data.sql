-- Chatgpt api key
INSERT INTO default_settings (id, common, name, value)
VALUES (1000, 1, 'chatgpt_api_key', 'chatgpt_api_key');

-- Chatgpt model
INSERT INTO default_settings (id, common, name, value)
VALUES (1001, 0, 'chatgpt_model', 'gpt-4o');

-- Chatgpt temperature
INSERT INTO default_settings (id, common, name, value)
VALUES (1002, 0, 'chatgpt_temperature', '0');

-- Chatgpt max tokens
INSERT INTO default_settings (id, common, name, value)
VALUES (1003, 0, 'chatgpt_max_tokens', '256');

-- Chatgpt frequency penalty
INSERT INTO default_settings (id, common, name, value)
VALUES (1004, 0, 'chatgpt_frequency_penalty', '0');

-- Chatgpt presence penalty
INSERT INTO default_settings (id, common, name, value)
VALUES (1005, 0, 'chatgpt_presence_penalty', '0');

-- Chatgpt top p
INSERT INTO default_settings (id, common, name, value)
VALUES (1006, 0, 'chatgpt_top_p', '1');

-- Chatgpt prompt
INSERT INTO default_settings (id, common, name, value)
VALUES (1007, 0, 'chatgpt_prompt', 'You will be provided with a transcript from a telegram chat, your task is to summarize what was discussed:
      Make a brief summary of the chat
      If appropriate, suggest a new topic, advice, or idea to talk about');

-- Minimum message stack to summarize
INSERT INTO default_settings (id, common, name, value)
VALUES (1008, 0, 'min_message_stack', '1');

-- Maximum message stack to summarize (not implemented yet)
INSERT INTO default_settings (id, common, name, value)
VALUES (1009, 0, 'max_message_stack', '100');

-- Message when the minimum message stack is not reached
INSERT INTO default_settings (id, common, name, value)
VALUES (1010, 0, 'min_message_stack_not_reached_message',
        'There are too few messages to display. You can read them yourself');

-- Message about the bot
INSERT INTO default_settings (id, common, name, value)
VALUES (1011, 0, 'about_message', 'I was created to summarize the latest messages in the chat.');

-- Message about the bot version
INSERT INTO default_settings (id, common, name, value)
VALUES (1012, 1, 'version_message', 'Version 0.0.1');

-- Message when the bot is started in the bot chat
INSERT INTO default_settings (id, common, name, value)
VALUES (1013, 1, 'in_bot_start_message', 'I was created to summarize the latest messages in the chat.
      1) Add me to your chat
      2) Make me an administrator
      3) Enter the command /start and see what I can do');

-- Message when the bot is started in the group chat and the chat ID is allowed
INSERT INTO default_settings (id, common, name, value)
VALUES (1014, 0, 'in_chat_start_allowed_message', 'I was created to summarize the latest messages in the chat.
    @kinatik has already added your chat ID "%d" to the allowed list and the number of attempts to summarize is limited only by your budget.
    Send me the /squeeze command and I will send you a brief summary of the latest chat messages.
    I summarize at least %d and no more than %d last messages.');

-- Message when the bot is started in the group chat and the chat ID is not allowed yet
INSERT INTO default_settings (id, common, name, value)
VALUES (1015, 0, 'in_chat_start_not_allowed_message', 'I was created to summarize the latest messages in the chat.
    Ask @kinatik to add your chat ID "%s" to the allowed list to remove some restrictions.
    You already have %d free attempts to summarize your chat.
    Send me the /squeeze command and I will send you a brief summary of the latest chat messages.
    I summarize at least %d and no more than %d last messages.');

-- Message when there are no new messages to summarize
INSERT INTO default_settings (id, common, name, value)
VALUES (1016, 0, 'no_message_to_display_message', 'You have no new messages to display.');

-- Message when the chat ID is not allowed
INSERT INTO default_settings (id, common, name, value)
VALUES (1017, 0, 'not_allowed_message', 'Your chat ID "%d" is not on the list of allowed chat IDs for this bot and you have run out of free attempts to summarize.
    Ask @kinatik to add your chat to the allowed list.');

-- Message when an error occurs
INSERT INTO default_settings (id, common, name, value)
VALUES (1018, 0, 'error_message', 'An error occurred while trying to summarize: %s
    Ask @kinatik to deal with this.');

-- Allowed chat ids, where the bot can work without restrictions
INSERT INTO default_settings (id, common, name, value)
VALUES (1019, 1, 'allowed_chat_ids', '123456789,987654321');

-- Maximum free requests per not allowed chat
INSERT INTO default_settings (id, common, name, value)
VALUES (1020, 0, 'max_requests_per_not_allowed_chat', '10');

-- Maximum image size. 0 - images are sent to GPT API, 1-4 - image size to send to GPT API, 1 - low quality, 4 - high quality
INSERT INTO default_settings (id, common, name, value)
VALUES (1021, 0, 'max_image_size', '2');

-- A part of the prompt when somebody sent an image
INSERT INTO default_settings (id, common, name, value)
VALUES (1022, 0, 'somebody_sent_image_prompt', '[sent an image]');

-- A part of the prompt when somebody captioned an image
INSERT INTO default_settings (id, common, name, value)
VALUES (1023, 0, 'somebody_captioned_image_prompt', '[captioned an image]');

-- A part of the prompt when the bot describes an image
INSERT INTO default_settings (id, common, name, value)
VALUES (1024, 0, 'describe_image_prompt', 'Describe the image');

-- A part of the prompt when the bot describes an image immediately fot debugging, if empty the feature is disabled
INSERT INTO default_settings (id, common, name, value)
VALUES (1025, 0, 'describe_image_immediately_prompt', 'Bot, describe');

-- Image frequency per user in minutes, not set or 0 is disabled
INSERT INTO default_settings (id, common, name, value)
VALUES (1026, 0, 'image_frequency_per_user_in_mins', '5');

-- Image frequency per chat in minutes, not set or 0 is disabled
INSERT INTO default_settings (id, common, name, value)
VALUES (1027, 0, 'image_frequency_per_chat_in_mins', '1');

-- Command to squeeze the chat
INSERT INTO default_settings (id, common, name, value)
VALUES (1028, 0, 'command_to_squeeze', 'Bot, squeeze');
