package com.library.reservation;

public record Reservation(
        String reservationId,
        String bookId,
        String userId,
        String status   // ACTIVE or CANCELLED
) {}