package vn.edu.hust.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hust.infrastructure.service.BookingExpirationService;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private BookingExpirationService cleanupService;

    @GetMapping("/cleanup-stats")
    public ResponseEntity<BookingExpirationService.CleanupStatistics> getCleanupStatistics() {
        BookingExpirationService.CleanupStatistics stats = cleanupService.getCleanupStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/cleanup-health")
    public ResponseEntity<HealthStatus> getCleanupHealth() {
        BookingExpirationService.CleanupStatistics stats = cleanupService.getCleanupStatistics();
        boolean isHealthy = stats.expiredPendingBookings() < 100 &&
                stats.expiredHeldTickets() < 500;
        String status = isHealthy ? "HEALTHY" : "DEGRADED";
        String message = isHealthy ?
                "Cleanup operations are running normally" :
                "High number of expired items detected - cleanup may be lagging";
        return ResponseEntity.ok(new HealthStatus(status, message, stats));
    }

    @PostMapping("/emergency-cleanup")
    public ResponseEntity<String> triggerEmergencyCleanup() {
        try {
            cleanupService.emergencyCleanup();
            return ResponseEntity.ok("Emergency cleanup completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Emergency cleanup failed: " + e.getMessage());
        }
    }

    public record HealthStatus(String status, String message, BookingExpirationService.CleanupStatistics statistics) {
    }
}
