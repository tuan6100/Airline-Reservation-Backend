package vn.edu.hust.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hust.application.dto.command.ExpireBookingCommand;
import vn.edu.hust.domain.model.enumeration.BookingStatus;
import vn.edu.hust.domain.model.enumeration.TicketStatus;
import vn.edu.hust.infrastructure.repository.BookingJpaRepository;
import vn.edu.hust.infrastructure.repository.TicketJpaRepository;
import vn.edu.hust.infrastructure.entity.BookingEntity;
import vn.edu.hust.infrastructure.entity.TicketEntity;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class BookingExpirationService {

    @Autowired
    private BookingJpaRepository bookingRepository;

    @Autowired
    private TicketJpaRepository ticketRepository;

    @Autowired
    private CommandGateway commandGateway;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireBookingsAndReleaseTickets() {
        LocalDateTime now = LocalDateTime.now();
        List<BookingEntity> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING, now);
        log.info("Found {} expired bookings to process", expiredBookings.size());
        for (BookingEntity booking : expiredBookings) {
            try {
                expireBookingAndReleaseTickets(booking);
            } catch (Exception e) {
                log.error("Failed to expire booking: {}", booking.getBookingId(), e);
            }
        }
        releaseOrphanedHeldTickets();
    }

    @Transactional
    protected void expireBookingAndReleaseTickets(BookingEntity booking) {
        try {
            List<TicketEntity> associatedTickets = ticketRepository.findByBookingId(booking.getBookingId());
            ExpireBookingCommand expireCommand = new ExpireBookingCommand();
            expireCommand.setBookingId(booking.getBookingId());
            commandGateway.sendAndWait(expireCommand);
            if (!associatedTickets.isEmpty()) {
                int releasedCount = ticketRepository.bulkReleaseTicketsByBooking(
                        booking.getBookingId(),
                        LocalDateTime.now()
                );
                log.info("Successfully expired booking {} and released {} tickets",
                        booking.getBookingId(), releasedCount);
            }

        } catch (Exception e) {
            log.error("Failed to expire booking and release tickets for booking: {}",
                    booking.getBookingId(), e);
            throw e;
        }
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void releaseOrphanedHeldTickets() {
        try {
            List<TicketEntity> heldTickets = ticketRepository.findCurrentlyHeldTickets();
            log.info("Found {} currently held tickets to check", heldTickets.size());
            int orphanedCount = 0;
            LocalDateTime now = LocalDateTime.now();
            for (TicketEntity ticket : heldTickets) {
                try {
                    boolean shouldRelease = ticket.getUpdatedAt() != null &&
                            ticket.getUpdatedAt().isBefore(now.minusMinutes(20));
                    if (ticket.getBookingId() != null) {
                        BookingEntity booking = bookingRepository.findById(ticket.getBookingId()).orElse(null);
                        if (booking == null ||
                                booking.getStatus() != BookingStatus.PENDING ||
                                booking.isExpired()) {
                            shouldRelease = true;
                        }
                    } else {
                        shouldRelease = true;
                    }
                    if (shouldRelease) {
                        int released = ticketRepository.releaseTicketAtomic(
                                ticket.getTicketId(),
                                TicketStatus.HELD,
                                now
                        );
                        if (released > 0) {
                            orphanedCount++;
                            log.debug("Released orphaned held ticket: {}", ticket.getTicketId());
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to check/release ticket: {}", ticket.getTicketId(), e);
                }
            }
            if (orphanedCount > 0) {
                log.info("Released {} orphaned held tickets", orphanedCount);
            }
        } catch (Exception e) {
            log.error("Failed to process orphaned held tickets", e);
        }
    }

    @Scheduled(cron = "0 0 5 * * *")
    @Transactional
    public void bulkCleanupExpiredBookingsAndTickets() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredBefore = now.minusHours(1);
        try {
            int expiredBookingsCount = 0;
            int releasedTicketsCount = 0;
            List<BookingEntity> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                    BookingStatus.PENDING, now);
            for (BookingEntity booking : expiredBookings) {
                try {
                    ExpireBookingCommand command = new ExpireBookingCommand();
                    command.setBookingId(booking.getBookingId());
                    commandGateway.sendAndWait(command);
                    expiredBookingsCount++;
                    int released = ticketRepository.bulkReleaseTicketsByBooking(
                            booking.getBookingId(), now);
                    releasedTicketsCount += released;

                } catch (Exception e) {
                    log.error("Failed to bulk process booking: {}", booking.getBookingId(), e);
                }
            }
            try {
                int bulkReleased = ticketRepository.bulkReleaseExpiredHeldTickets(expiredBefore, now);
                releasedTicketsCount += bulkReleased;
            } catch (Exception e) {
                log.warn("Bulk release method not working, using individual processing: {}", e.getMessage());
            }

            log.info("Bulk cleanup completed - Expired bookings: {}, Released tickets: {}",
                    expiredBookingsCount, releasedTicketsCount);

        } catch (Exception e) {
            log.error("Failed to perform bulk cleanup", e);
        }
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional(readOnly = true)
    public void consistencyHealthCheck() {
        try {
            List<TicketEntity> heldTickets = ticketRepository.findCurrentlyHeldTickets();
            int inconsistentCount = 0;
            for (TicketEntity ticket : heldTickets) {
                if (ticket.getBookingId() != null) {
                    BookingEntity booking = bookingRepository.findById(ticket.getBookingId()).orElse(null);
                    if (booking == null) {
                        inconsistentCount++;
                        log.warn("Found ticket {} with non-existent booking ID: {}",
                                ticket.getTicketId(), ticket.getBookingId());
                    } else if (booking.getStatus() != BookingStatus.PENDING) {
                        inconsistentCount++;
                        log.warn("Found ticket {} held for non-pending booking: {} (status: {})",
                                ticket.getTicketId(), ticket.getBookingId(), booking.getStatus());
                    }
                }
            }
            if (inconsistentCount > 0) {
                log.error("Consistency check found {} tickets with invalid booking references",
                        inconsistentCount);
            } else {
                log.info("Consistency check passed - no inconsistent tickets found");
            }
            long totalHeldTickets = heldTickets.size();
            long ticketsWithValidBookings = heldTickets.stream()
                    .filter(t -> t.getBookingId() != null)
                    .filter(t -> bookingRepository.findById(t.getBookingId()).isPresent())
                    .count();
            log.info("Health check summary - Total held tickets: {}, With valid bookings: {}, Inconsistent: {}",
                    totalHeldTickets, ticketsWithValidBookings, inconsistentCount);
        } catch (Exception e) {
            log.error("Failed to perform consistency health check", e);
        }
    }

    @Transactional
    public void emergencyCleanup() {
        try {
            log.info("Starting emergency cleanup...");
            LocalDateTime now = LocalDateTime.now();
            List<TicketEntity> longHeldTickets = ticketRepository.findCurrentlyHeldTickets()
                    .stream()
                    .filter(t -> t.getUpdatedAt() != null &&
                            t.getUpdatedAt().isBefore(now.minusMinutes(30)))
                    .toList();

            int emergencyReleased = 0;
            for (TicketEntity ticket : longHeldTickets) {
                try {
                    int released = ticketRepository.releaseTicketAtomic(
                            ticket.getTicketId(),
                            TicketStatus.HELD,
                            now
                    );
                    if (released > 0) {
                        emergencyReleased++;
                    }
                } catch (Exception e) {
                    log.error("Failed to emergency release ticket: {}", ticket.getTicketId(), e);
                }
            }
            List<BookingEntity> overdueBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                    BookingStatus.PENDING, now.minusMinutes(5));
            int emergencyExpired = 0;
            for (BookingEntity booking : overdueBookings) {
                try {
                    ExpireBookingCommand command = new ExpireBookingCommand();
                    command.setBookingId(booking.getBookingId());
                    commandGateway.sendAndWait(command);
                    emergencyExpired++;
                } catch (Exception e) {
                    log.error("Failed to emergency expire booking: {}", booking.getBookingId(), e);
                }
            }
            log.info("Emergency cleanup completed - Released {} tickets, Expired {} bookings",
                    emergencyReleased, emergencyExpired);
        } catch (Exception e) {
            log.error("Emergency cleanup failed", e);
        }
    }

    public CleanupStatistics getCleanupStatistics() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<BookingEntity> pendingBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                    BookingStatus.PENDING, now.plusDays(1));
            long expiredPendingCount = pendingBookings.stream()
                    .filter(b -> b.getExpiresAt().isBefore(now))
                    .count();
            List<TicketEntity> heldTickets = ticketRepository.findCurrentlyHeldTickets();
            long expiredHeldCount = heldTickets.stream()
                    .filter(t -> t.getUpdatedAt() != null &&
                            t.getUpdatedAt().isBefore(now.minusMinutes(20)))
                    .count();
            return new CleanupStatistics(
                    pendingBookings.size(),
                    expiredPendingCount,
                    heldTickets.size(),
                    expiredHeldCount,
                    now
            );
        } catch (Exception e) {
            log.error("Failed to get cleanup statistics", e);
            return new CleanupStatistics(0, 0, 0, 0, LocalDateTime.now());
        }
    }

    public record CleanupStatistics(long totalPendingBookings, long expiredPendingBookings, long totalHeldTickets,
                                             long expiredHeldTickets, LocalDateTime timestamp) {

        @Override
            public String toString() {
                return String.format(
                        "CleanupStats{pendingBookings=%d, expiredPending=%d, heldTickets=%d, expiredHeld=%d, time=%s}",
                        totalPendingBookings, expiredPendingBookings, totalHeldTickets, expiredHeldTickets, timestamp
                );
            }
        }
}