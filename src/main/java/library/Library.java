package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class Library {
    public static final int MAX_LOANS_PER_MEMBER = 3;

    private final Map<String, LibraryItem> items = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    public void addItem(LibraryItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (items.putIfAbsent(item.getId(), item) != null) {
            throw new IllegalArgumentException("An item already exists with id: " + item.getId());
        }
    }

    public void registerMember(Member member) {
        Objects.requireNonNull(member, "member cannot be null");
        if (members.putIfAbsent(member.getId(), member) != null) {
            throw new IllegalArgumentException("A member already exists with id: " + member.getId());
        }
    }

    public void borrow(String memberId, String itemId)
            throws ItemNotAvailableException, MemberLimitExceededException {
        Member member = getMemberOrThrow(memberId);
        LibraryItem item = getItemOrThrow(itemId);
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("Item is not available: " + itemId);
        }
        if (findActiveLoan(memberId, itemId) != null) {
            throw new IllegalStateException(
                    "Member " + memberId + " already has an active loan for item " + itemId);
        }
        if (member.getActiveLoanCount() >= MAX_LOANS_PER_MEMBER) {
            throw new MemberLimitExceededException(
                    "Member " + memberId + " already has the maximum of "
                            + MAX_LOANS_PER_MEMBER + " active loans");
        }

        LocalDate borrowedOn = LocalDate.now();
        Loan loan = new Loan(item.getId(), member.getId(), borrowedOn,
                borrowedOn.plusDays(item.loanPeriodDays()));
        item.borrow(member);
        loans.add(loan);
        member.addLoan(loan);
    }

    public void returnItem(String memberId, String itemId) {
        Member member = getMemberOrThrow(memberId);
        LibraryItem item = getItemOrThrow(itemId);
        Loan activeLoan = findActiveLoan(memberId, itemId);
        if (activeLoan == null) {
            throw new IllegalArgumentException(
                    "No active loan exists for member " + memberId + " and item " + itemId);
        }
        if (!memberId.equals(item.getBorrowerId())) {
            throw new IllegalStateException("Item loan state does not match its active loan");
        }
        item.giveBack();
        activeLoan.markReturned(LocalDate.now());
    }

    public List<LibraryItem> searchByTitle(String text) {
        String searchText = Objects.requireNonNull(text, "search text cannot be null")
                .toLowerCase(Locale.ROOT);
        return items.values().stream()
                .filter(item -> item.getTitle().toLowerCase(Locale.ROOT).contains(searchText))
                .sorted(Comparator.comparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Loan> listOverdueItems() {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(loan -> loan.isOverdue(today))
                .sorted(Comparator.comparing(Loan::getDueDate))
                .toList();
    }

    public List<LibraryItem> getItems() {
        return items.values().stream()
                .sorted(Comparator.comparing(LibraryItem::getId))
                .toList();
    }

    public List<Member> getMembers() {
        return members.values().stream()
                .sorted(Comparator.comparing(Member::getId))
                .toList();
    }

    public List<Loan> getLoans() {
        return List.copyOf(loans);
    }

    private Loan findActiveLoan(String memberId, String itemId) {
        return loans.stream()
                .filter(loan -> loan.getMemberId().equals(memberId)
                        && loan.getItemId().equals(itemId) && !loan.isReturned())
                .findFirst()
                .orElse(null);
    }

    Member getMemberOrThrow(String memberId) {
        Member member = members.get(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Unknown member id: " + memberId);
        }
        return member;
    }

    LibraryItem getItemOrThrow(String itemId) {
        LibraryItem item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item id: " + itemId);
        }
        return item;
    }

    void addLoadedLoan(Loan loan) throws ItemNotAvailableException {
        Member member = getMemberOrThrow(loan.getMemberId());
        LibraryItem item = getItemOrThrow(loan.getItemId());
        if (!loan.isReturned()) {
            if (member.getActiveLoanCount() >= MAX_LOANS_PER_MEMBER) {
                throw new IllegalArgumentException(
                        "Persisted data exceeds the active loan limit for member " + member.getId());
            }
            item.borrow(member);
        }
        loans.add(loan);
        member.addLoan(loan);
    }
}
