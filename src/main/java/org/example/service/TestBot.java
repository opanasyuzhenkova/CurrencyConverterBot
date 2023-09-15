package org.example.service;

import lombok.SneakyThrows;
import org.example.entity.Currency;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class TestBot extends TelegramLongPollingBot {

    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    private final CurrencyConversionService currencyConversionService = CurrencyConversionService.getInstance();

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update)  {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }


    @SneakyThrows
    public void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String[] param = callbackQuery.getData().split(":");
        String action = param[0];
        Currency newCurrency = Currency.valueOf(param[1]);
        switch (action) {
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            case "TARGET":
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;

        }
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
        for (Currency currency : org.example.entity.Currency.values()) {
            buttons.add(Arrays.asList(
                    InlineKeyboardButton.builder()
                            .text(getCurrencyButton(originalCurrency, currency))
                            .callbackData("ORIGINAL:" + currency)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text(getCurrencyButton(targetCurrency, currency))
                            .callbackData("TARGET:" + currency)
                            .build()));
        }
        try {
            execute(EditMessageReplyMarkup.builder()
                    .chatId(message.getChatId().toString())
                    .messageId(message.getMessageId())
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                    .build());
        } catch (TelegramApiException e) {
            System.out.println(e);
        }

    }


    @SneakyThrows
    private void handleMessage(Message message) {
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                try {
                    String command =
                            message
                                    .getText()
                                    .substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                    switch (command) {
                        case "/start":
                            String answer = "Здравствуйте, " + message.getFrom().getFirstName() + "!" + "\n" +
                                    "Чтобы начать, выберите команду в меню";

                            execute(SendMessage.builder()
                                    .chatId(message.getChatId().toString())
                                    .text(answer)
                                    .build());
                            return;
                        case "/set_currency":
                            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                            Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
                            Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
                            for (Currency currency : org.example.entity.Currency.values()) {
                                buttons.add(Arrays.asList(
                                        InlineKeyboardButton.builder()
                                                .text(getCurrencyButton(originalCurrency, currency))
                                                .callbackData("ORIGINAL:" + currency)
                                                .build(),
                                        InlineKeyboardButton.builder()
                                                .text(getCurrencyButton(targetCurrency, currency))
                                                .callbackData("TARGET:" + currency)
                                                .build()));
                            }
                            execute(SendMessage.builder()
                                    .text("Выберите исходную, целевую валюту и введите количество:")
                                    .chatId(message.getChatId().toString())
                                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                    .build());
                            return;
                        case "/dollar_exch_rate":
                            Double dollarRate = currencyConversionService.getConversionRatio(Currency.USD, Currency.RUB);
                            execute(SendMessage.builder()
                                    .chatId(message.getChatId().toString())
                                    .text(
                                            String.format(
                                                    "Текущий курс доллара: %4.2f руб.", dollarRate))
                                    .build());
                            return;

                        case "/euro_exch_rate":
                            Double euroRate = currencyConversionService.getConversionRatio(Currency.EUR, Currency.RUB);
                            execute(SendMessage.builder()
                                    .chatId(message.getChatId().toString())
                                    .text(
                                            String.format(
                                                    "Текущий курс евро: %4.2f руб.", euroRate))
                                    .build());
                            return;
                    }
                } catch (Exception e) {
                    System.out.println("error");
                }
            }
        }
        if (message.hasText()) {
            String messageText = message.getText();
            Optional<Double> value = parseDouble(messageText);
            Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
            Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

            Double ratio = CurrencyConversionService.getInstance().getConversionRatio(originalCurrency, targetCurrency);
            if (value.isPresent()) {
                try {
                    execute(
                            SendMessage.builder()
                                    .chatId(message.getChatId().toString())
                                    .text(
                                            String.format(
                                                    "%4.2f %s =  %4.2f %s",
                                                    value.get(), originalCurrency, value.get() * ratio, targetCurrency))
                                    .build());

                } catch (Exception e) {
                    System.out.println("error");
                }
            }
        }
    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getCurrencyButton(Currency saved, Currency current) {
        return saved == current ? current + "✔\uFE0F" : current.name();
    }


    @Override
    @SneakyThrows
    public String getBotUsername() {
        return "@JBTTestBot";
    }



    @SneakyThrows
    public static void main(String[] args) {
        try {
            TestBot bot = new TestBot();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);

        } catch (TelegramApiException e) {
            System.out.println(e);
        }


    }
}