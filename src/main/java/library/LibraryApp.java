package library;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public final class LibraryApp {
    private final Library library;
    private final LibraryCsvStorage storage;
    private final Path dataDirectory;
    private final Scanner scanner = new Scanner(System.in);

    private LibraryApp(Library library, LibraryCsvStorage storage, Path dataDirectory) {
        this.library = library;
        this.storage = storage;
        this.dataDirectory = dataDirectory;
    }

    public static void main(String[] args) {
        Path dataDirectory = args.length > 0 ? Path.of(args[0]) : Path.of("data");
        LibraryCsvStorage storage = new LibraryCsvStorage();
        try {
            Library library = storage.load(dataDirectory);
            System.out.println("Loaded " + library.getItems().size() + " items and "
                    + library.getMembers().size() + " members.");
            new LibraryApp(library, storage, dataDirectory).run();
        } catch (IOException exception) {
            System.err.println("Could not load library data: " + exception.getMessage());
        }
    }

    private void run() {
        boolean running = true;
        try {
            while (running) {
                printMenu();
                String choice = prompt("Choose an option: ");
                try {
                    switch (choice) {
                        case "1" -> addItem();
                        case "2" -> registerMember();
                        case "3" -> borrowItem();
                        case "4" -> returnItem();
                        case "5" -> listOverdueItems();
                        case "6" -> searchByTitle();
                        case "7" -> listItems();
                        case "8" -> listMembers();
                        case "0" -> running = false;
                        default -> System.out.println("Choose a listed menu option.");
                    }
                } catch (ItemNotAvailableException | MemberLimitExceededException
                         | IllegalArgumentException | IllegalStateException exception) {
                    System.out.println("Operation failed: " + exception.getMessage());
                }
            }
        } finally {
            try {
                storage.save(dataDirectory, library);
                System.out.println("Library data saved to " + dataDirectory.toAbsolutePath());
            } catch (IOException exception) {
                System.err.println("Could not save library data: " + exception.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("""

                === Library Management System ===
                1. Add item
                2. Register member
                3. Borrow item
                4. Return item
                5. List overdue items
                6. Search by title
                7. List items
                8. List members
                0. Save and exit
                """);
    }

    private void addItem() {
        String type = prompt("Item type (book/dvd): ").trim().toLowerCase();
        String id = prompt("Item id: ");
        String title = prompt("Title: ");
        LibraryItem item = switch (type) {
            case "book" -> new Book(id, title);
            case "dvd" -> new Dvd(id, title);
            default -> throw new IllegalArgumentException("Item type must be book or dvd");
        };
        library.addItem(item);
        System.out.println("Added: " + item);
    }

    private void registerMember() {
        Member member = new Member(prompt("Member id: "), prompt("Name: "), prompt("Email: "));
        library.registerMember(member);
        System.out.println("Registered: " + member);
    }

    private void borrowItem() throws ItemNotAvailableException, MemberLimitExceededException {
        String memberId = prompt("Member id: ");
        String itemId = prompt("Item id: ");
        library.borrow(memberId, itemId);
        System.out.println("Item borrowed successfully.");
    }

    private void returnItem() {
        String memberId = prompt("Member id: ");
        String itemId = prompt("Item id: ");
        library.returnItem(memberId, itemId);
        System.out.println("Item returned successfully.");
    }

    private void listOverdueItems() {
        List<Loan> overdue = library.listOverdueItems();
        if (overdue.isEmpty()) {
            System.out.println("No overdue items.");
            return;
        }
        overdue.forEach(loan -> System.out.println(loan + " | " + library.getItemOrThrow(loan.getItemId())
                .getTitle() + " | member " + loan.getMemberId()));
    }

    private void searchByTitle() {
        List<LibraryItem> matches = library.searchByTitle(prompt("Title contains: "));
        if (matches.isEmpty()) {
            System.out.println("No matching items.");
        } else {
            matches.forEach(System.out::println);
        }
    }

    private void listItems() {
        library.getItems().forEach(System.out::println);
    }

    private void listMembers() {
        library.getMembers().forEach(System.out::println);
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
