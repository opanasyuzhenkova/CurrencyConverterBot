package org.example.service;

import org.example.entity.Currency;
import org.example.service.impl.CbrfCurrencyConversionService;

public interface CurrencyConversionService {
    static CurrencyConversionService getInstance() {
        return new CbrfCurrencyConversionService();
    }

    double getConversionRatio(Currency original, Currency target);
}
