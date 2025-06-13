package vn.edu.hust.infrastructure.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import vn.edu.hust.domain.model.enumeration.DiscountType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotions")
public class PromotionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Integer promotionId;
    
    @Column(name = "promotion_name", length = Integer.MAX_VALUE)
    private String promotionName;
    
    @ColumnDefault("0")
    @Column(name = "discount")
    private Long discount = 0L;
    
    @Column(name = "discount_type")
    @Enumerated(EnumType.ORDINAL)
    private DiscountType discountType;
    
    @Column(name = "amount")
    private Integer amount;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "expire_at")
    private Instant expireAt;

    @Column(name = "activate_at")
    private Instant activateAt;

}
