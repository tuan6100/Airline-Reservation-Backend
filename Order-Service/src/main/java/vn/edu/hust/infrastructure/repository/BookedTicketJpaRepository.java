package vn.edu.hust.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hust.infrastructure.entity.BookedTicketEntity;

import java.util.List;

@Repository
public interface BookedTicketJpaRepository extends JpaRepository<BookedTicketEntity, Long> {

    List<BookedTicketEntity> findByOrderId(Long orderId);

    @Query("SELECT bt FROM BookedTicketEntity bt WHERE bt.ticketId = :ticketId")
    BookedTicketEntity findByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT bt FROM BookedTicketEntity bt JOIN bt.ticket t WHERE t.flightId = :flightId")
    List<BookedTicketEntity> findByFlightId(@Param("flightId") Long flightId);
}
