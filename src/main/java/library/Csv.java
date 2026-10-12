package library;

import java.util.ArrayList;
import java.util.List;

final class Csv {
    private Csv() {
    }

    static String encode(List<String> fields) {
        return fields.stream().map(Csv::escape).reduce((left, right) -> left + "," + right).orElse("");
    }

    static List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (quoted) {
                if (current == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        closedQuote = true;
                    }
                } else {
                    field.append(current);
                }
            } else if (current == ',' ) {
                fields.add(field.toString());
                field.setLength(0);
                closedQuote = false;
            } else if (current == '"' && field.isEmpty() && !closedQuote) {
                quoted = true;
            } else if (closedQuote) {
                throw new IllegalArgumentException("Unexpected character after quoted CSV field");
            } else {
                field.append(current);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("Unterminated quoted CSV field");
        }
        fields.add(field.toString());
        return fields;
    }

    private static String escape(String field) {
        if (field == null) {
            throw new IllegalArgumentException("CSV fields cannot be null");
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
