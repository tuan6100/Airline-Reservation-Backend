package vn.edu.hust.application.dto.command;

import lombok.Data;

/**
 * Command for updating an existing order
 */
@Data
public class UpdateOrderCommand {
    private Long orderId;
    private Long promotionId;
    // Add other fields that can be updated
    // This is a simplified implementation - actual update fields would depend on business requirements
}