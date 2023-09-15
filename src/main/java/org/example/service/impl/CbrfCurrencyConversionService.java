package org.example.service.impl;

import lombok.SneakyThrows;
import org.example.entity.Currency;
import org.example.service.CurrencyConversionService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class CbrfCurrencyConversionService implements CurrencyConversionService {
    @Override
    public double getConversionRatio(Currency original, Currency target) {

        double originalRate = getRate(original);
        double targetRate = getRate(target);
        return originalRate / targetRate;
    }

    @SneakyThrows
    private double getRate(Currency currency) {

        if (currency == Currency.RUB) {
            return 1.0;
        }
        String currencyId = currency.getId();
        String value = "";
        String nominal = "";
        try {
            Document doc = Jsoup.connect("https://www.cbr.ru/scripts/XML_daily.asp").get();
            String selector = String.format("Valute[ID=%s]", currencyId);
            Element currencyElement = doc.select(selector).first();
            value = currencyElement.getElementsByTag("Value").text();
            nominal = currencyElement.getElementsByTag("Nominal").text();
        }
        catch (IOException e) {
            System.out.println(e);
        }
        value = value.replace(",", ".");
        return Double.parseDouble(value) / Double.parseDouble(nominal);
    }
}
