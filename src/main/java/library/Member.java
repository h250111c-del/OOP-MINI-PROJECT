package library;

import java.util.ArrayList;
import java.util.List;

public final class Member {
    private final String id;
    private final String name;
    private final String email;
    private final List<Loan> loans = new ArrayList<>();

    public Member(String id, String name, String email) {
        this.id = LibraryItem.requireText(id, "id");
        this.name = LibraryItem.requireText(name, "name");
        this.email = LibraryItem.requireText(email, "email");
        if (!this.email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("email address is invalid");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Loan> getLoans() {
        return List.copyOf(loans);
    }

    public long getActiveLoanCount() {
        return loans.stream().filter(loan -> !loan.isReturned()).count();
    }

    void addLoan(Loan loan) {
        loans.add(loan);
    }

    @Override
    public String toString() {
        return "Member{id='" + id + "', name='" + name + "', email='" + email
                + "', activeLoans=" + getActiveLoanCount() + "}";
    }
}
