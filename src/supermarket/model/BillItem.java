package supermarket.model;

/**
 * Represents a single line item in a customer bill.
 * Holds product reference, quantity ordered, and computed subtotal.
 */
public class BillItem {

    private final Product product;
    private       int     quantity;
    private final double  unitPrice;   // price at time of billing (snapshot)

    public BillItem(Product product, int quantity) {
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = product.getPrice();
    }

    // ── Calculations ─────────────────────────────

    /** quantity × unitPrice */
    public double getSubtotal() {
        return quantity * unitPrice;
    }

    // ── Bill line formatting ──────────────────────

    /**
     * Returns a formatted bill line:
     *  Rice (kg)         5  x  Rs.  55.00  =  Rs.  275.00
     */
    public String toBillLine() {
        String itemName = product.getName() + " (" + product.getUnit() + ")";
        return String.format("  %-26s %3d  x  Rs.%8.2f  =  Rs.%10.2f",
            itemName, quantity, unitPrice, getSubtotal());
    }

    // ── Getters ───────────────────────────────────

    public Product getProduct()  { return product; }
    public int     getQuantity() { return quantity; }
    public double  getUnitPrice(){ return unitPrice; }

    public void setQuantity(int qty) { this.quantity = qty; }
}
