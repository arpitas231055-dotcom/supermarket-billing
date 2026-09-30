package supermarket.model;

/**
 * Represents a product in the supermarket inventory.
 * Stored in: data/products.txt  (pipe-delimited)
 * Format   : id|name|category|price|stock|unit
 */
public class Product {

    private String productId;
    private String name;
    private String category;
    private double price;
    private int    stock;
    private String unit;       // e.g. "kg", "pcs", "litre", "pack"

    // ── Constructors ─────────────────────────────

    public Product() {}

    public Product(String productId, String name, String category,
                   double price, int stock, String unit) {
        this.productId = productId;
        this.name      = name;
        this.category  = category;
        this.price     = price;
        this.stock     = stock;
        this.unit      = unit;
    }

    // ── File serialization ───────────────────────

    /** Converts product to pipe-delimited string for file storage. */
    public String toFileString() {
        return productId + "|" + name + "|" + category + "|"
             + price + "|" + stock + "|" + unit;
    }

    /** Parses a pipe-delimited line back into a Product object. */
    public static Product fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 6) return null;
        try {
            return new Product(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                Double.parseDouble(parts[3].trim()),
                Integer.parseInt(parts[4].trim()),
                parts[5].trim()
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Display ──────────────────────────────────

    /** Returns a formatted table row for console display. */
    public String toTableRow() {
        return String.format("| %-8s | %-22s | %-12s | %8.2f | %6d | %-6s |",
            productId, name, category, price, stock, unit);
    }

    /** Short display line for bill items. */
    public String toShortString() {
        return String.format("%-22s  %-6s  Rs.%8.2f", name, unit, price);
    }

    // ── Getters & Setters ────────────────────────

    public String getProductId()              { return productId; }
    public String getName()                   { return name; }
    public String getCategory()               { return category; }
    public double getPrice()                  { return price; }
    public int    getStock()                  { return stock; }
    public String getUnit()                   { return unit; }

    public void setProductId(String productId){ this.productId = productId; }
    public void setName(String name)          { this.name = name; }
    public void setCategory(String category)  { this.category = category; }
    public void setPrice(double price)        { this.price = price; }
    public void setStock(int stock)           { this.stock = stock; }
    public void setUnit(String unit)          { this.unit = unit; }

    /** Reduce stock after a sale. Returns false if insufficient stock. */
    public boolean reduceStock(int qty) {
        if (qty > stock) return false;
        stock -= qty;
        return true;
    }

    /** Restock product. */
    public void addStock(int qty) {
        this.stock += qty;
    }

    @Override
    public String toString() {
        return String.format("Product[%s | %s | Rs.%.2f | Stock:%d %s]",
            productId, name, price, stock, unit);
    }
}
