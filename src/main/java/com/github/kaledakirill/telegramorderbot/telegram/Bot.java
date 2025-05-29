package com.github.kaledakirill.telegramorderbot.telegram;

import com.github.kaledakirill.telegramorderbot.config.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final BotProperties botProperties;

    public Bot(BotProperties botProperties) {
        this.botProperties = botProperties;
        this.telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        log.debug("Received update: id={}", update.getUpdateId());

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            String username = update.getMessage().getFrom().getUserName();

            log.debug("User {} (chatId={}) sent message: {}", username != null ? username : "unknown", chatId, messageText);

            SendMessage message = new SendMessage(chatId, messageText);
            try {
                telegramClient.execute(message);
                log.info("Sent response to user {} (chatId={}): {}", username != null ? username : "unknown", chatId, messageText);
            } catch (TelegramApiException e) {
                log.error("Error sending message to chatId={}: {}", chatId, e.getMessage(), e);
            }
        } else {
            log.warn("Unsupported update type received: id={}", update.getUpdateId());
        }
    }
}