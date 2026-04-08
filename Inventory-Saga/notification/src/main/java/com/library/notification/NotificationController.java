package com.library.notification;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    // Step 3 — Send confirmation
   /* @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendNotification(
            @RequestBody Map<String, String> body) {

        String userId        = body.get("userId");
        String bookId        = body.get("bookId");
        String reservationId = body.get("reservationId");

        // Simulate failure — uncomment THIS to trigger rollback of steps 1 and 2
        // throw new RuntimeException("Email server is down!");

        System.out.println("[NOTIFICATION] ✓ Sending confirmation to user: " + userId);
        System.out.println("[NOTIFICATION] ✓ Book: " + bookId
                + " | Reservation: " + reservationId);

        return ResponseEntity.ok(Map.of(
                "message", "Notification sent to " + userId,
                "reservationId", reservationId
        ));
    }
*/
    @PostMapping("/send")
    public void sendNotification(
            @RequestBody Map<String, String> body) {

        String userId        = body.get("userId");
        String bookId        = body.get("bookId");
        String reservationId = body.get("reservationId");

        // Simulate failure — uncomment THIS to trigger rollback of steps 1 and 2
        throw new RuntimeException("Email server is down!");

        //System.out.println("[NOTIFICATION] ✓ Sending confirmation to user: " + userId);
        //System.out.println("[NOTIFICATION] ✓ Book: " + bookId
        //      + " | Reservation: " + reservationId);


    }
}