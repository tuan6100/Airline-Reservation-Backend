package vn.edu.hust.application.enumeration;

import lombok.Getter;

@Getter
public enum EurozoneNation {
    AUSTRIA("Austria"),
    BELGIUM("Belgium"),
    CROATIA("Croatia"),
    CYPRUS("Cyprus"),
    ESTONIA("Estonia"),
    FINLAND("Finland"),
    FRANCE("France"),
    GERMANY("Germany"),
    GREECE("Greece"),
    IRELAND("Ireland"),
    ITALY("Italy"),
    LATVIA("Latvia"),
    LITHUANIA("Lithuania"),
    LUXEMBOURG("Luxembourg"),
    MALTA("Malta"),
    NETHERLANDS("Netherlands"),
    PORTUGAL("Portugal"),
    SLOVAKIA("Slovakia"),
    SLOVENIA("Slovenia"),
    SPAIN("Spain");

    private final String name;

    EurozoneNation(String name) {
        this.name = name;
    }
}
