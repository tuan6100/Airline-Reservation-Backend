package vn.edu.hust.infrastructure.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.domain.model.enumeration.SeatStatus;
import vn.edu.hust.infrastructure.entity.SeatEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {

    @Query("SELECT s FROM SeatEntity s WHERE s.aircraftId = :aircraftId AND s.status = :status ORDER BY s.seatCode")
    List<SeatEntity> findByAircraftIdAndStatus(@Param("aircraftId") Long aircraftId, @Param("status") SeatStatus status);

    default List<SeatEntity> findAvailableByAircraftId(Long aircraftId) {
        return findByAircraftIdAndStatus(aircraftId, SeatStatus.AVAILABLE);
    }

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT s FROM SeatEntity s WHERE s.seatId = :seatId")
    SeatEntity findByIdWithLock(@Param("seatId") Long seatId);

    @Modifying
    @Query(value = """
        UPDATE seat SET
            status = 'ON_HOLD',
            updated_at = :updateTime,
            version = version + 1
        WHERE seat_id = :seatId
        AND status = 'AVAILABLE'
        """, nativeQuery = true)
    int holdSeatAtomic(
            @Param("seatId") Long seatId,
            @Param("updateTime") LocalDateTime updateTime
    );

    default boolean canHoldSeatAtomic(Long seatId, SeatStatus ignoredExpectedStatus, Long ignoredCustomerId) {
        int updatedRows = holdSeatAtomic(seatId, LocalDateTime.now());
        return updatedRows > 0;
    }

    @Modifying
    @Query(value = """
        UPDATE seat SET
            status = 'AVAILABLE',
            updated_at = :updateTime,
            version = version + 1
        WHERE seat_id = :seatId 
        AND status = 'ON_HOLD'
        """, nativeQuery = true)
    int releaseSeatAtomic(
            @Param("seatId") Long seatId,
            @Param("updateTime") LocalDateTime updateTime
    );

    @Modifying
    @Query(value = """
        UPDATE seat SET
            status = 'AVAILABLE',
            updated_at = :updateTime,
            version = version + 1
        WHERE status = 'ON_HOLD'
        AND updated_at < :expiredBefore
        """, nativeQuery = true)
    int bulkReleaseExpiredHeldSeats(
            @Param("expiredBefore") LocalDateTime expiredBefore,
            @Param("updateTime") LocalDateTime updateTime
    );

    @Query("SELECT s FROM SeatEntity s WHERE s.aircraftId = :aircraftId AND s.seatClass.id = :seatClassId AND s.status = :status")
    List<SeatEntity> findByAircraftIdAndSeatClassAndStatus(
            @Param("aircraftId") Long aircraftId,
            @Param("seatClassId") Long seatClassId,
            @Param("status") SeatStatus status
    );

    @Query("SELECT COUNT(s) FROM SeatEntity s WHERE s.aircraftId = :aircraftId AND s.seatClass.id = :seatClassId AND s.status = 'AVAILABLE'")
    long countAvailableByAircraftIdAndSeatClass(@Param("aircraftId") Long aircraftId, @Param("seatClassId") Long seatClassId);
}