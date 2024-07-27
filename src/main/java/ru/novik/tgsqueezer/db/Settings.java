package ru.novik.tgsqueezer.db;

import lombok.Getter;

@Getter
public enum Settings {
    chatgpt_api_key,
    chatgpt_model,
    chatgpt_temperature,
    chatgpt_max_tokens,
    chatgpt_frequency_penalty,
    chatgpt_presence_penalty,
    chatgpt_top_p,
    chatgpt_prompt,
    min_message_stack,
    max_message_stack,
    min_message_stack_not_reached_message,
    about_message,
    version_message,
    in_bot_start_message,
    in_chat_start_allowed_message,
    in_chat_start_not_allowed_message,
    no_message_to_display_message,
    not_allowed_message,
    error_message,
    allowed_chat_ids,
    max_requests_per_not_allowed_chat,
    max_image_size,
    somebody_sent_image_prompt,
    somebody_captioned_image_prompt,
    describe_image_prompt,
    describe_image_immediately_prompt,
    image_frequency_per_user_in_mins,
    image_frequency_per_chat_in_mins,
    command_to_squeeze
}
