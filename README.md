# TgSqueezer Bot

TgSqueezer is a Telegram bot designed to provide concise summaries of the latest messages in a chat. This bot helps users stay updated with important conversations without having to read through every single message.

## Features

- **Automated Summarization**: Quickly summarizes the latest messages in your Telegram chat.
- **Customizable Limits**: Summarizes a minimum of 10 and a maximum of 50 latest messages.
- **Unlimited Attempts**: The number of summarization attempts is unlimited for allowed telegram chats.

## Commands

- **/start**
    - Use this command to get your chat ID. This is necessary for the bot to work in your chat without restrictions.

- **/squeeze**
    - Use this command to get a brief summary of the latest messages in the chat. Note that there are limits on the minimum and maximum number of messages.

- **/about**
    - Tells you about the bot and how it can help you in the chat.

- **/version**
    - Shows the current version of the bot.

## Getting Started

To get started with TgSqueezer, follow these steps:

1. **Add the Bot to Your Chat**:
    - Search for TgSqueezer in Telegram and add it to your chat.

2. **Set Up the Bot**:
    - Use the `/start` command to get your chat ID and ensure it is added to the allowed list.

3. **Use the Bot**:
    - Send the `/squeeze` command to get a summary of the latest messages.

## Installation with Docker

To run TgSqueezer using Docker, follow these steps:

1. **Create the Docker image**
    ```sh
    docker build -t your-dockerhub-username/tgSqueezer .
    ```
2. **Push the Docker image**
    ```sh
    docker push your-dockerhub-username/tgSqueezer
    ``` 
3. **Prepare your host**
   - Edit the configuration file bot.yaml and place it to your host at /opt/bot.yaml. Make sure you have the following configuration values: telegram bot token, name and openai api key.
   - Place compose.yaml to your host at /opt/compose.yaml.
   - Pull the Docker Image from Docker Hub:
    ```sh
    docker pull your-dockerhub-username/tgSqueezer
    ```
4. **Run the Docker Container**:
    ```sh
    docker-compose up -d
    ```
5. **Check the Logs**:
    ```sh
    docker logs -f tg-squeezer-bot
    ```

## Contributing

We welcome contributions! Please feel free to submit issues, fork the repository and create pull requests.

## License

This project is licensed under the MIT License

## Contact

If you have any questions or feedback, feel free to reach out to me at [kinatik](https://t.me/kinatik).

## Future Functionality
    - Add support for describing images in the chat.
    - Save data to a database to prevent data loss.
    - Add suppport for message answering.
    - Add support for reading links summaries.
    - Add support for reactions to messages.
    - Add better support for multiple chats.
    - Add support for multiple languages.
