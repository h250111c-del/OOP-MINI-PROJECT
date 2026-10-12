package library;

public final class Book extends LibraryItem {
    public Book(String id, String title) {
        super(id, title);
    }

    @Override
    public int loanPeriodDays() {
        return 21;
    }

    @Override
    public String toString() {
        return "Book{id='" + getId() + "', title='" + getTitle()
                + "', available=" + isAvailable() + "}";
    }
}
