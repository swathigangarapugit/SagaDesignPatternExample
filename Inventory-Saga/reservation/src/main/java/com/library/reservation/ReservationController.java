package com.library.reservation;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();

    // Step 2 — Create a reservation
    @PostMapping
    public ResponseEntity<Map<String, String>> createReservation(
            @RequestBody Map<String, String> body) {

        String bookId = body.get("bookId");
        String userId = body.get("userId");

        // Simulate occasional failure — uncomment to test rollback
        // if (Math.random() < 0.5) {
        //     return ResponseEntity.status(500)
        //         .body(Map.of("message", "Reservation service temporarily unavailable"));
        // }

        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reservation res = new Reservation(reservationId, bookId, userId, "ACTIVE");
        reservations.put(reservationId, res);

        System.out.println("[RESERVATION] Created reservation " + reservationId
                + " for user " + userId + " — book " + bookId);

        return ResponseEntity.ok(Map.of(
                "reservationId", reservationId,
                "status", "ACTIVE",
                "message", "Reservation created"
        ));
    }

    // COMPENSATING ACTION — cancel a reservation
    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<Map<String, String>> cancelReservation(
            @PathVariable String reservationId) {

        Reservation existing = reservations.get(reservationId);
        if (existing == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Reservation not found"));
        }

        // Replace record with cancelled version
        reservations.put(reservationId,
                new Reservation(reservationId, existing.bookId(), existing.userId(), "CANCELLED"));

        System.out.println("[RESERVATION] Cancelled reservation " + reservationId);
        return ResponseEntity.ok(Map.of("message", "Reservation cancelled: " + reservationId));
    }

    // Helper — see all reservations
    @GetMapping
    public Collection<Reservation> getAll() {
        return reservations.values();
    }
}