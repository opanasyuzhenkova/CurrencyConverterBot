package org.example.entity;


import lombok.Getter;

@Getter
public enum Currency {

    USD("R01235"), EUR("R01239"), RUB ("0"), BYN("R01090B"), GBP("R01035"), CNY("R01375"), JPY("R01820");

    private final String id;

    Currency(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
