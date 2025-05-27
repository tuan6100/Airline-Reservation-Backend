package vn.edu.hust.infrastructure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.hust.domain.model.valueobj.OrderId;
import vn.edu.hust.infrastructure.dto.InitiatePaymentRequest;
import vn.edu.hust.infrastructure.dto.PaymentResponseDTO;

import java.math.BigDecimal;


@Service
public class PaymentServiceClient {

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;

    @Autowired
    public PaymentServiceClient(
            RestTemplate restTemplate,
            @Value("${services.payment.url}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
    }

    public PaymentResponseDTO initiatePayment(OrderId orderId, BigDecimal amount, String currency, Long customerId) {
        String url = paymentServiceUrl + "/api/payments/initiate";
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setOrderId(orderId.value());
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setCustomerId(customerId);
        ResponseEntity<PaymentResponseDTO> response = restTemplate.postForEntity(url, request, PaymentResponseDTO.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to initiate payment: " + response.getStatusCode());
        }
    }

    public PaymentResponseDTO getPaymentStatus(Long paymentId) {
        String url = paymentServiceUrl + "/api/payments/" + paymentId;
        ResponseEntity<PaymentResponseDTO> response = restTemplate.getForEntity(url, PaymentResponseDTO.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get payment status: " + response.getStatusCode());
        }
    }
}