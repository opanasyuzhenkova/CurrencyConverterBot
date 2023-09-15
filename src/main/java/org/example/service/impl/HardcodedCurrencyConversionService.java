package org.example.service.impl;


import org.example.entity.Currency;
import org.example.service.CurrencyConversionService;
public class HardcodedCurrencyConversionService implements CurrencyConversionService {
    @Override
    public double getConversionRatio(Currency from, Currency to) {
        switch (from) {
            case RUB:
                switch (to) {
                    case USD:
                        return 1 / 93.4;
                    case EUR:
                        return 1 / 101.4;
                    case RUB:
                        return 1;
                }
            case EUR:
                switch (to) {
                    case USD:
                        return 1 / 0.9;
                    case EUR:
                        return 1;
                    case RUB:
                        return 1 / 101.4;
                }
            case USD:
                switch (to) {
                    case USD:
                        return 1;
                    case EUR:
                        return 1 / 0.9;
                    case RUB:
                        return 1 / 93.4;
                }
        }
        return 1;
    }
}

