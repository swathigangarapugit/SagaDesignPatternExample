package com.library.orchestrator;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class SagaOrchestrator {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String INVENTORY_URL    = "http://localhost:8081/inventory";
    private static final String RESERVATION_URL  = "http://localhost:8082/reservations";
    private static final String NOTIFICATION_URL = "http://localhost:8083/notifications";

    public Map<String, Object> executeReservationSaga(ReservationRequest request) {
        String bookId = request.bookId();
        String userId = request.userId();
        String reservationId = null;

        // ── STEP 1: Check inventory ──────────────────────────────────────
        System.out.println("\n[SAGA] Starting reservation saga");
        System.out.println("[SAGA] Step 1 → Checking inventory for book: " + bookId);

        try {
            ResponseEntity<Map> inventoryResponse = restTemplate.postForEntity(
                    INVENTORY_URL + "/check/" + bookId, null, Map.class
            );

            if (!inventoryResponse.getStatusCode().is2xxSuccessful()
                    || !(Boolean) inventoryResponse.getBody().get("available")) {
                // Book not available — no compensation needed, nothing was changed yet
                System.out.println("[SAGA] Step 1 FAILED — book not available. Saga aborted.");
                return Map.of("success", false, "step", "inventory",
                        "message", "Book not available: " + bookId);
            }

            System.out.println("[SAGA] Step 1 SUCCESS — book is available");

        } catch (Exception e) {
            System.out.println("[SAGA] Step 1 FAILED — inventory service down");
            return Map.of("success", false, "step", "inventory",
                    "message", "Inventory service unavailable");
        }

        // ── STEP 2: Create reservation ───────────────────────────────────
        System.out.println("[SAGA] Step 2 → Creating reservation");

        try {
            ResponseEntity<Map> reservationResponse = restTemplate.postForEntity(
                    RESERVATION_URL,
                    Map.of("bookId", bookId, "userId", userId),
                    Map.class
            );

            reservationId = (String) reservationResponse.getBody().get("reservationId");
            System.out.println("[SAGA] Step 2 SUCCESS — reservationId: " + reservationId);

        } catch (Exception e) {
            // Step 2 failed — compensate step 1 (release the inventory hold)
            System.out.println("[SAGA] Step 2 FAILED — compensating step 1");
            compensateInventory(bookId);

            return Map.of("success", false, "step", "reservation",
                    "message", "Reservation failed — inventory hold released");
        }

        // ── STEP 3: Send notification ────────────────────────────────────
        System.out.println("[SAGA] Step 3 → Sending notification to user: " + userId);

        try {
            restTemplate.postForEntity(
                    NOTIFICATION_URL + "/send",
                    Map.of("userId", userId, "bookId", bookId, "reservationId", reservationId),
                    Map.class
            );

            System.out.println("[SAGA] Step 3 SUCCESS");
            System.out.println("[SAGA] ✓ Saga completed successfully!\n");

            return Map.of(
                    "success", true,
                    "reservationId", reservationId,
                    "message", "Book reserved successfully!"
            );

        } catch (Exception e) {
            // Step 3 failed — compensate step 2 and step 1 (reverse order!)
            System.out.println("[SAGA] Step 3 FAILED — compensating steps 2 and 1");
            compensateReservation(reservationId);
            compensateInventory(bookId);

            return Map.of("success", false, "step", "notification",
                    "message", "Notification failed — reservation and inventory hold rolled back");
        }
    }

    // ── Compensating actions ─────────────────────────────────────────────

    private void compensateInventory(String bookId) {
        try {
            restTemplate.postForEntity(
                    INVENTORY_URL + "/release/" + bookId, null, Map.class
            );
            System.out.println("[SAGA] Compensation: inventory hold released for " + bookId);
        } catch (Exception e) {
            // Log but continue — compensations are best-effort
            System.out.println("[SAGA] WARNING: Failed to compensate inventory for " + bookId);
        }
    }

    private void compensateReservation(String reservationId) {
        try {
            restTemplate.postForEntity(
                    RESERVATION_URL + "/cancel/" + reservationId, null, Map.class
            );
            System.out.println("[SAGA] Compensation: reservation cancelled " + reservationId);
        } catch (Exception e) {
            System.out.println("[SAGA] WARNING: Failed to compensate reservation " + reservationId);
        }
    }
}