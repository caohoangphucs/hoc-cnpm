package vn.id.caohoangphuc.cnpm.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Releases expired bed reservations independently of later user requests. */
@Component
public class BookingHoldScheduler {
    private final BookingService bookings;

    /** Uses the booking service so expiry follows the same locking and audit rules as API actions. */
    public BookingHoldScheduler(BookingService bookings) {
        this.bookings = bookings;
    }

    /** Checks every minute and expires a bounded batch of overdue reservations. */
    @Scheduled(fixedDelayString = "${booking.hold-expiry-interval-ms:60000}")
    public void expireOverdueHolds() {
        bookings.expireHoldsFromScheduler();
    }
}
