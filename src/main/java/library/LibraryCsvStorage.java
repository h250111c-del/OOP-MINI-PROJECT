package library;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class LibraryCsvStorage {
    private static final String ITEMS_FILE = "items.csv";
    private static final String MEMBERS_FILE = "members.csv";
    private static final String LOANS_FILE = "loans.csv";

    public Library load(Path directory) throws IOException {
        Library library = new Library();
        loadItems(directory.resolve(ITEMS_FILE), library);
        loadMembers(directory.resolve(MEMBERS_FILE), library);
        loadLoans(directory.resolve(LOANS_FILE), library);
        return library;
    }

    public void save(Path directory, Library library) throws IOException {
        Files.createDirectories(directory);
        write(directory.resolve(ITEMS_FILE), List.of("type,id,title"), library.getItems().stream()
                .map(item -> Csv.encode(List.of(item instanceof Book ? "BOOK" : "DVD",
                        item.getId(), item.getTitle())))
                .toList());
        write(directory.resolve(MEMBERS_FILE), List.of("id,name,email"), library.getMembers().stream()
                .map(member -> Csv.encode(List.of(member.getId(), member.getName(), member.getEmail())))
                .toList());
        write(directory.resolve(LOANS_FILE),
                List.of("itemId,memberId,borrowedOn,dueDate,returnedOn"),
                library.getLoans().stream()
                        .map(loan -> Csv.encode(List.of(loan.getItemId(), loan.getMemberId(),
                                loan.getBorrowedOn().toString(), loan.getDueDate().toString(),
                                loan.getReturnedOn() == null ? "" : loan.getReturnedOn().toString())))
                        .toList());
    }

    private void loadItems(Path file, Library library) throws IOException {
        readRows(file, 3, (lineNumber, fields) -> {
            String type = fields.get(0);
            LibraryItem item = switch (type) {
                case "BOOK" -> new Book(fields.get(1), fields.get(2));
                case "DVD" -> new Dvd(fields.get(1), fields.get(2));
                default -> throw new IllegalArgumentException("Unknown item type: " + type);
            };
            library.addItem(item);
        });
    }

    private void loadMembers(Path file, Library library) throws IOException {
        readRows(file, 3, (lineNumber, fields) ->
                library.registerMember(new Member(fields.get(0), fields.get(1), fields.get(2))));
    }

    private void loadLoans(Path file, Library library) throws IOException {
        readRows(file, 5, (lineNumber, fields) -> {
            Loan loan = new Loan(fields.get(0), fields.get(1),
                    LocalDate.parse(fields.get(2)), LocalDate.parse(fields.get(3)));
            if (!fields.get(4).isBlank()) {
                loan.restoreReturnedOn(LocalDate.parse(fields.get(4)));
            }
            try {
                library.addLoadedLoan(loan);
            } catch (ItemNotAvailableException exception) {
                throw new IllegalArgumentException(exception.getMessage(), exception);
            }
        });
    }

    private void readRows(Path file, int expectedFields, CsvRowConsumer consumer) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    List<String> fields = Csv.parse(line);
                    if (fields.size() != expectedFields) {
                        throw new IllegalArgumentException(
                                "Expected " + expectedFields + " fields but found " + fields.size());
                    }
                    consumer.accept(lineNumber, fields);
                } catch (RuntimeException exception) {
                    throw new IOException("Invalid data in " + file + " at line " + lineNumber
                            + ": " + exception.getMessage(), exception);
                }
            }
        }
    }

    private void write(Path file, List<String> headers, List<String> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String header : headers) {
                writer.write(header);
                writer.newLine();
            }
            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
        }
    }

    @FunctionalInterface
    private interface CsvRowConsumer {
        void accept(int lineNumber, List<String> fields);
    }
}
