package vn.edu.hust.integration.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationRequest {
    private Long customerId;
    private String type;
    private String title;
    private String message;
    private String priority;
    private String category;
    private Map<String, String> metadata;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private String channel = "ALL";
}