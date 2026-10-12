package library;

public interface Borrowable {
    void borrow(Member member) throws ItemNotAvailableException;

    void giveBack();
}
