package supermarket.service;

import supermarket.model.*;
import supermarket.util.ConsoleUI;
import supermarket.util.FileHandler;
import java.util.*;

/**
 * BillingService — Handles the complete customer billing workflow.
 *
 * Concepts: ArrayList, loops, String.format(),
 *           file writing, exception handling.
 */
public class BillingService {

    private final ProductService productService;
    private final List<Bill>     billsCache;

    public BillingService(ProductService productService) {
        this.productService = productService;
        this.billsCache     = new ArrayList<>();
    }

    // ── Create New Bill ───────────────────────────

    public void createNewBill(Scanner sc) {
        ConsoleUI.printLine();
        System.out.println("  🧾  CREATE NEW BILL");
        ConsoleUI.printDivider();

        // Step 1: Customer Details
        System.out.println("  Step 1: Customer Details");
        String customerName  = ConsoleUI.readString(sc, "Customer Name  : ");
        String customerPhone = ConsoleUI.readPhone(sc,  "Customer Phone : ");

        System.out.println("\n  Payment Mode:  1. Cash    2. Card    3. UPI");
        int payMode = ConsoleUI.readInt(sc, "Select: ", 1, 3);
        String paymentMode;
        switch (payMode) {
            case 1:  paymentMode = "Cash"; break;
            case 2:  paymentMode = "Card"; break;
            default: paymentMode = "UPI";  break;
        }

        // Step 2: Create Bill Object
        String billId = FileHandler.getNextBillId();
        Bill bill = new Bill(billId, customerName, customerPhone, paymentMode);

        // Step 3: Add Items
        System.out.println("\n  Step 2: Add Items to Bill");
        ConsoleUI.printDivider();

        boolean addingItems = true;
        while (addingItems) {
            showProductCatalog();

            System.out.println("\n  --- ADD ITEM ---");
            String productId = ConsoleUI.readString(sc, "Enter Product ID (or 0 to finish): ").toUpperCase();

            if (productId.equals("0")) {
                if (bill.getItems().isEmpty()) {
                    ConsoleUI.warn("No items added. Bill cancelled.");
                    return;
                }
                addingItems = false;
                continue;
            }

            Product product = productService.findById(productId);
            if (product == null) {
                ConsoleUI.error("Product ID '" + productId + "' not found. Try again.");
                continue;
            }

            if (product.getStock() == 0) {
                ConsoleUI.error("'" + product.getName() + "' is OUT OF STOCK.");
                continue;
            }

            System.out.printf("  Product : %s%n", product.getName());
            System.out.printf("  Price   : Rs. %.2f per %s%n", product.getPrice(), product.getUnit());
            System.out.printf("  Stock   : %d %s available%n", product.getStock(), product.getUnit());

            int qty = ConsoleUI.readInt(sc, "Enter Quantity: ", 1, product.getStock());

            // Check if product already in bill → update quantity
            boolean found = false;
            for (BillItem existing : bill.getItems()) {
                if (existing.getProduct().getProductId().equals(productId)) {
                    int newQty = existing.getQuantity() + qty;
                    if (newQty > product.getStock()) {
                        ConsoleUI.error("Total qty exceeds available stock (" + product.getStock() + ").");
                    } else {
                        existing.setQuantity(newQty);
                        ConsoleUI.success("Quantity updated to " + newQty + " for '" + product.getName() + "'.");
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                bill.addItem(new BillItem(product, qty));
                ConsoleUI.success("Added: " + qty + " x " + product.getName());
            }

            System.out.printf("%n  -- Running Total: Rs. %,.2f (%d items) --%n",
                bill.getSubtotal(), bill.getTotalItems());

            boolean addMore = ConsoleUI.readYesNo(sc, "Add another item?");
            if (!addMore) addingItems = false;
        }

        // Step 4: Show Summary
        System.out.println();
        ConsoleUI.printDivider();
        System.out.println("  BILL SUMMARY");
        ConsoleUI.printDivider();
        System.out.printf("  Items       : %d%n", bill.getTotalItems());
        System.out.printf("  Subtotal    : Rs. %,.2f%n", bill.getSubtotal());
        if (bill.getDiscountPercent() > 0) {
            System.out.printf("  Discount    : %.0f%% (Rs. %,.2f)%n",
                bill.getDiscountPercent(), bill.getDiscountAmount());
        } else {
            System.out.println("  Discount    : None (spend Rs.500+ for discount)");
        }
        System.out.printf("  GST (5%%)    : Rs. %,.2f%n", bill.getGstAmount());
        System.out.printf("  Grand Total : Rs. %,.2f%n", bill.getGrandTotal());
        ConsoleUI.printDivider();

        // Step 5: Confirm
        boolean confirm = ConsoleUI.readYesNo(sc, "Confirm and generate bill?");
        if (!confirm) {
            ConsoleUI.info("Bill cancelled. Stock not updated.");
            return;
        }

        // Deduct stock
        for (BillItem item : bill.getItems()) {
            item.getProduct().reduceStock(item.getQuantity());
        }
        productService.saveProducts();

        // Save bill
        String billText = bill.generateBillText();
        FileHandler.saveBillFile(billId, billText);
        FileHandler.appendBillIndex(bill.toIndexLine());
        billsCache.add(bill);

        System.out.println(billText);
        ConsoleUI.success("Bill saved to: data/bills/" + billId + ".txt");
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── View All Bills ────────────────────────────

    public void viewAllBills() {
        List<String> indexLines = FileHandler.readBillIndex();
        if (indexLines.isEmpty()) {
            ConsoleUI.warn("No bills found. Create a bill first.");
            return;
        }

        System.out.println("\n  📄  ALL BILLS (" + indexLines.size() + " total)");
        ConsoleUI.printBillListHeader();

        double totalRevenue = 0;
        for (String line : indexLines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 7) {
                System.out.printf("  | %-10s | %-18s | %-12s | %5s | %10s | %-6s |%n",
                    parts[0], parts[1], parts[3], parts[4],
                    "Rs." + parts[5], parts[6]);
                try { totalRevenue += Double.parseDouble(parts[5]); } catch (Exception ignored) {}
            }
        }
        ConsoleUI.printBillListFooter();
        System.out.printf("  Total Revenue: Rs. %,.2f%n%n", totalRevenue);
    }

    // ── Search Bill ───────────────────────────────

    public void searchBill(Scanner sc) {
        ConsoleUI.printLine();
        System.out.println("  🔍  SEARCH BILL");
        ConsoleUI.printDivider();
        String billId = ConsoleUI.readString(sc, "Enter Bill ID (e.g. BILL-0001): ").toUpperCase();

        String billText = FileHandler.readBillFile(billId);
        if (billText == null) {
            ConsoleUI.error("Bill '" + billId + "' not found.");
        } else {
            System.out.println(billText);
        }
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── Helpers ───────────────────────────────────

    private void showProductCatalog() {
        List<Product> allProducts = productService.getAllProducts();
        System.out.println("\n  📦  PRODUCT CATALOG");
        ConsoleUI.printProductTableHeader();
        for (Product p : allProducts) {
            System.out.println("  " + p.toTableRow());
        }
        ConsoleUI.printProductTableFooter();
    }

    public List<Bill> getBillsCache() { return billsCache; }
}
