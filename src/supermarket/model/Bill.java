package supermarket.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete customer bill.
 *
 * Stored in: data/bills/BILL-XXXX.txt
 *
 * Discount slabs:
 *   subtotal >= 2000  → 15% off
 *   subtotal >= 1000  → 10% off
 *   subtotal >=  500  →  5% off
 *   subtotal <   500  →  0% off
 *
 * GST: flat 5% on discounted amount
 */
public class Bill {

    // Discount slabs
    private static final double DISCOUNT_15_THRESHOLD = 2000.0;
    private static final double DISCOUNT_10_THRESHOLD = 1000.0;
    private static final double DISCOUNT_5_THRESHOLD  =  500.0;
    private static final double GST_RATE              =  0.05;

    private final String         billId;
    private final String         customerName;
    private final String         customerPhone;
    private final LocalDateTime  billDate;
    private final List<BillItem> items;
    private final String         paymentMode;  // Cash / Card / UPI

    public Bill(String billId, String customerName, String customerPhone, String paymentMode) {
        this.billId        = billId;
        this.customerName  = customerName;
        this.customerPhone = customerPhone;
        this.paymentMode   = paymentMode;
        this.billDate      = LocalDateTime.now();
        this.items         = new ArrayList<>();
    }

    // ── Item management ──────────────────────────

    public void addItem(BillItem item)  { items.add(item); }
    public List<BillItem> getItems()    { return items; }
    public int getTotalItems()          { return items.size(); }

    // ── Financial calculations ────────────────────

    public double getSubtotal() {
        return items.stream().mapToDouble(BillItem::getSubtotal).sum();
    }

    public double getDiscountPercent() {
        double sub = getSubtotal();
        if (sub >= DISCOUNT_15_THRESHOLD) return 15.0;
        if (sub >= DISCOUNT_10_THRESHOLD) return 10.0;
        if (sub >= DISCOUNT_5_THRESHOLD)  return  5.0;
        return 0.0;
    }

    public double getDiscountAmount() {
        return getSubtotal() * getDiscountPercent() / 100.0;
    }

    public double getAfterDiscount() {
        return getSubtotal() - getDiscountAmount();
    }

    public double getGstAmount() {
        return getAfterDiscount() * GST_RATE;
    }

    public double getGrandTotal() {
        return getAfterDiscount() + getGstAmount();
    }

    // ── Bill formatting ───────────────────────────

    public String generateBillText() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy  hh:mm a");
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("  ╔══════════════════════════════════════════════════╗\n");
        sb.append("  ║           FRESH MART SUPERMARKET                 ║\n");
        sb.append("  ║        123, MG Road, Indore - 452001             ║\n");
        sb.append("  ║    Phone: 0731-4567890  GSTIN: 23ABCDE1234F1Z5   ║\n");
        sb.append("  ╠══════════════════════════════════════════════════╣\n");
        sb.append(String.format("  ║  Bill No : %-38s║\n", billId));
        sb.append(String.format("  ║  Date    : %-38s║\n", billDate.format(dtf)));
        sb.append(String.format("  ║  Customer: %-38s║\n", customerName));
        sb.append(String.format("  ║  Phone   : %-38s║\n", customerPhone));
        sb.append(String.format("  ║  Payment : %-38s║\n", paymentMode));
        sb.append("  ╠══════════════════════════════════════════════════╣\n");
        sb.append("  ║  Item                       Qty   Rate      Amt  ║\n");
        sb.append("  ╠══════════════════════════════════════════════════╣\n");

        for (BillItem item : items) {
            sb.append("  ║").append(item.toBillLine()).append("  ║\n");
        }

        sb.append("  ╠══════════════════════════════════════════════════╣\n");
        sb.append(String.format("  ║  %-30s %18s  ║\n",
            "Subtotal (" + items.size() + " items):",
            String.format("Rs. %,.2f", getSubtotal())));

        if (getDiscountPercent() > 0) {
            sb.append(String.format("  ║  %-30s %18s  ║\n",
                "Discount (" + (int)getDiscountPercent() + "%):",
                String.format("- Rs. %,.2f", getDiscountAmount())));
            sb.append(String.format("  ║  %-30s %18s  ║\n",
                "After Discount:",
                String.format("Rs. %,.2f", getAfterDiscount())));
        }

        sb.append(String.format("  ║  %-30s %18s  ║\n",
            "GST (5%):",
            String.format("Rs. %,.2f", getGstAmount())));
        sb.append("  ╠══════════════════════════════════════════════════╣\n");
        sb.append(String.format("  ║  %-30s %18s  ║\n",
            "*** GRAND TOTAL ***",
            String.format("Rs. %,.2f", getGrandTotal())));
        sb.append("  ╠══════════════════════════════════════════════════╣\n");

        // Savings message
        if (getDiscountPercent() > 0) {
            sb.append(String.format("  ║  🎉 You saved Rs. %-32s║\n",
                String.format("%,.2f on this bill!", getDiscountAmount())));
        }
        sb.append("  ║  Thank you for shopping at Fresh Mart!           ║\n");
        sb.append("  ║  Visit again! Have a great day! 🛒               ║\n");
        sb.append("  ╚══════════════════════════════════════════════════╝\n");

        return sb.toString();
    }

    /** Compact summary line for listing bills. */
    public String toSummaryLine() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return String.format("| %-10s | %-18s | %-12s | %5d | %10.2f | %-6s |",
            billId, customerName, billDate.format(dtf),
            items.size(), getGrandTotal(), paymentMode);
    }

    /** One-line format for writing to bills index file. */
    public String toIndexLine() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return billId + "|" + customerName + "|" + customerPhone + "|"
             + billDate.format(dtf) + "|" + items.size()
             + "|" + String.format("%.2f", getGrandTotal()) + "|" + paymentMode;
    }

    // ── Getters ───────────────────────────────────

    public String        getBillId()        { return billId; }
    public String        getCustomerName()  { return customerName; }
    public String        getCustomerPhone() { return customerPhone; }
    public LocalDateTime getBillDate()      { return billDate; }
    public String        getPaymentMode()   { return paymentMode; }
}
