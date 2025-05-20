package vn.edu.hust.domain.model.valueobj;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing a seat class in a flight.
 * Examples: Economy, Business, First Class
 */
public final class SeatClass {
    private final String name;
    private final String code;
    private final int priority; // Thứ tự ưu tiên (số thấp hơn = ưu tiên cao hơn)

    // Danh sách các loại ghế tiêu chuẩn
    private static final List<String> STANDARD_CLASSES = Arrays.asList(
            "ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST_CLASS"
    );

    /**
     * Constructor with full details
     */
    public SeatClass(String name, String code, int priority) {
        Objects.requireNonNull(name, "Seat class name cannot be null");
        Objects.requireNonNull(code, "Seat class code cannot be null");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Seat class name cannot be empty");
        }

        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("Seat class code cannot be empty");
        }

        this.name = name;
        this.code = code;
        this.priority = priority;
    }

    /**
     * Constructor with just the name
     * Code will be derived from name, and priority will be set based on standard classes
     */
    public SeatClass(String name) {
        Objects.requireNonNull(name, "Seat class name cannot be null");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Seat class name cannot be empty");
        }

        this.name = name.toUpperCase();
        this.code = deriveCodeFromName(name);
        this.priority = derivePriorityFromName(name);
    }

    /**
     * Factory method for creating standard seat classes
     */
    public static SeatClass economy() {
        return new SeatClass("ECONOMY", "Y", 4);
    }

    public static SeatClass premiumEconomy() {
        return new SeatClass("PREMIUM_ECONOMY", "W", 3);
    }

    public static SeatClass business() {
        return new SeatClass("BUSINESS", "C", 2);
    }

    public static SeatClass firstClass() {
        return new SeatClass("FIRST_CLASS", "F", 1);
    }

    /**
     * Create a SeatClass from a seat class ID
     */
    public static SeatClass fromId(SeatClassId seatClassId) {
        switch (seatClassId.value().intValue()) {
            case 1:
                return economy();
            case 2:
                return premiumEconomy();
            case 3:
                return business();
            case 4:
                return firstClass();
            default:
                return economy();
        }
    }

    /**
     * Derive a code from a seat class name
     */
    private String deriveCodeFromName(String name) {
        String upperName = name.toUpperCase().trim();

        if (upperName.equals("ECONOMY") || upperName.equals("ECONOMY_CLASS")) {
            return "Y";
        } else if (upperName.equals("PREMIUM_ECONOMY")) {
            return "W";
        } else if (upperName.equals("BUSINESS") || upperName.equals("BUSINESS_CLASS")) {
            return "C";
        } else if (upperName.equals("FIRST") || upperName.equals("FIRST_CLASS")) {
            return "F";
        } else {
            // Default to first character if not standard
            return upperName.substring(0, 1);
        }
    }

    /**
     * Derive a priority from a seat class name
     */
    private int derivePriorityFromName(String name) {
        String upperName = name.toUpperCase().trim();

        if (upperName.equals("ECONOMY") || upperName.equals("ECONOMY_CLASS")) {
            return 4;
        } else if (upperName.equals("PREMIUM_ECONOMY")) {
            return 3;
        } else if (upperName.equals("BUSINESS") || upperName.equals("BUSINESS_CLASS")) {
            return 2;
        } else if (upperName.equals("FIRST") || upperName.equals("FIRST_CLASS")) {
            return 1;
        } else {
            // Default to lowest priority if not standard
            return 5;
        }
    }

    /**
     * Check if this is a standard class
     */
    public boolean isStandardClass() {
        return STANDARD_CLASSES.contains(name);
    }

    /**
     * Get the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Compare with another seat class based on priority
     */
    public int compareTo(SeatClass other) {
        return Integer.compare(this.priority, other.priority);
    }

    /**
     * Check if this seat class is higher than another
     */
    public boolean isHigherThan(SeatClass other) {
        return this.priority < other.priority; // Lower number = higher priority
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatClass seatClass = (SeatClass) o;
        return Objects.equals(name, seatClass.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}