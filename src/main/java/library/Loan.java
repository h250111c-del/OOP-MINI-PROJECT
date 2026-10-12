package library;

import java.time.LocalDate;
import java.util.Objects;

public final class Loan {
    private final String itemId;
    private final String memberId;
    private final LocalDate borrowedOn;
    private final LocalDate dueDate;
    private LocalDate returnedOn;

    public Loan(String itemId, String memberId, LocalDate borrowedOn, LocalDate dueDate) {
        this.itemId = LibraryItem.requireText(itemId, "itemId");
        this.memberId = LibraryItem.requireText(memberId, "memberId");
        this.borrowedOn = Objects.requireNonNull(borrowedOn, "borrowedOn cannot be null");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate cannot be null");
        if (dueDate.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("dueDate cannot be before borrowedOn");
        }
    }

    public String getItemId() {
        return itemId;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getBorrowedOn() {
        return borrowedOn;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnedOn() {
        return returnedOn;
    }

    public boolean isReturned() {
        return returnedOn != null;
    }

    public boolean isOverdue(LocalDate today) {
        return !isReturned() && dueDate.isBefore(today);
    }

    void markReturned(LocalDate date) {
        Objects.requireNonNull(date, "return date cannot be null");
        if (isReturned()) {
            throw new IllegalStateException("Loan has already been returned");
        }
        if (date.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("return date cannot be before borrowedOn");
        }
        returnedOn = date;
    }

    void restoreReturnedOn(LocalDate date) {
        if (date != null && date.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("return date cannot be before borrowedOn");
        }
        returnedOn = date;
    }

    @Override
    public String toString() {
        return "Loan{itemId='" + itemId + "', memberId='" + memberId + "', borrowedOn="
                + borrowedOn + ", dueDate=" + dueDate + ", returnedOn=" + returnedOn + "}";
    }
}
