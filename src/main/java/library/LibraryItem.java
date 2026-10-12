package library;

import java.util.Objects;

public abstract class LibraryItem implements Borrowable {
    private final String id;
    private final String title;
    private boolean available = true;
    private String borrowerId;

    protected LibraryItem(String id, String title) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");
    }

    public abstract int loanPeriodDays();

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    @Override
    public void borrow(Member member) throws ItemNotAvailableException {
        Objects.requireNonNull(member, "member cannot be null");
        if (!available) {
            throw new ItemNotAvailableException("Item is already on loan: " + id);
        }
        available = false;
        borrowerId = member.getId();
    }

    @Override
    public void giveBack() {
        if (available) {
            throw new IllegalStateException("Item is not currently on loan: " + id);
        }
        available = true;
        borrowerId = null;
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(fieldName + " cannot contain line breaks");
        }
        return value.trim();
    }
}
