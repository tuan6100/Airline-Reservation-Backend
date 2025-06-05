package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hust.infrastructure.entity.SeatClassEntity;

public interface SeatClassJpaRepository extends JpaRepository<SeatClassEntity, Integer> {
}
