package utils.enums;

public enum DocumentFilter {
    TODAY("Today", 0),
    LAST_WEEK("Last Week", 7),
    LAST_MONTH("Last Month", 30),
    LAST_YEAR("Last Year", 365),
    ALL("All", -1);

    private final String label;
    private final int days;

    DocumentFilter(String label, int days) {
        this.label = label;
        this.days = days;
    }

    public String getLabel() {
        return label;
    }

    public int getDays() {
        return days;
    }
    @Override
    public String toString() {
        return label;
    }
}
