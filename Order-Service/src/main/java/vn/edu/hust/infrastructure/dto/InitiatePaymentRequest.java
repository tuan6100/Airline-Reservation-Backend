package vn.edu.hust.infrastructure.dto;


import vn.edu.hust.application.enumeration.CurrencyUnit;



public record InitiatePaymentRequest (
        Long orderId,
        Long customerId,
        Long totalPrice,
        CurrencyUnit currency
) {
}
