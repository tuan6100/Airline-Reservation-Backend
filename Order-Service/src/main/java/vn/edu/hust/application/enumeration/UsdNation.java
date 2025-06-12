package vn.edu.hust.application.enumeration;


import lombok.Getter;

@Getter
public enum UsdNation {
    UNITED_STATES("United States"),
    AMERICAN_SAMOA("American Samoa"),
    BRITISH_VIRGIN_ISLANDS("British Virgin Islands"),
    ECUADOR("Ecuador"),
    EL_SALVADOR("El Salvador"),
    PALAU("Palau"),
    PANAMA("Panama"),
    TIMOR_LESTE("Timor-Leste"),
    Zimbabwe("Zimbabwe");

    private final String name;

    UsdNation(String name) {
        this.name = name;
    }
}