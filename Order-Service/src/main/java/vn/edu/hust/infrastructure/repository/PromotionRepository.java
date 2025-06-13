package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hust.infrastructure.entity.PromotionEntity;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {


}
