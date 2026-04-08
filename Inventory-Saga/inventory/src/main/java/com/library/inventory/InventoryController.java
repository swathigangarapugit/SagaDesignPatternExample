package com.library.inventory;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    // In-memory stock — bookId → available copies
    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
            "BOOK-001", 3,   // Clean Code — 3 copies
            "BOOK-002", 0,   // Design Patterns — out of stock
            "BOOK-003", 1    // Pragmatic Programmer — 1 copy
    ));

    // Step 1 — Check and hold a copy
    // Called by orchestrator at the START of the saga
    @PostMapping("/check/{bookId}")
    public ResponseEntity<Map<String, Object>> checkAndHold(@PathVariable String bookId) {
        Integer copies = stock.get(bookId);

        if (copies == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "available", false,
                    "message", "Book not found: " + bookId
            ));
        }

        if (copies <= 0) {
            return ResponseEntity.status(409).body(Map.of(
                    "available", false,
                    "message", "Book out of stock: " + bookId
            ));
        }

        // Reduce stock — the book is now on hold
        stock.put(bookId, copies - 1);
        System.out.println("[INVENTORY] Held 1 copy of " + bookId + ". Remaining: " + (copies - 1));

        return ResponseEntity.ok(Map.of(
                "available", true,
                "message", "Book held successfully",
                "remaining", copies - 1
        ));
    }

    // COMPENSATING ACTION — release the hold
    // Called by orchestrator if a LATER step fails
    @PostMapping("/release/{bookId}")
    public ResponseEntity<Map<String, String>> release(@PathVariable String bookId) {
        stock.merge(bookId, 1, Integer::sum);  // add the copy back
        System.out.println("[INVENTORY] Released hold on " + bookId);
        return ResponseEntity.ok(Map.of("message", "Hold released for " + bookId));
    }

    // Helper — see current stock (useful for testing)
    @GetMapping("/stock")
    public Map<String, Integer> getStock() {
        return stock;
    }
}