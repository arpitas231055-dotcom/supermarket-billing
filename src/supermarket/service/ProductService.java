package supermarket.service;

import supermarket.model.Product;
import supermarket.util.ConsoleUI;
import supermarket.util.FileHandler;
import java.util.*;

/**
 * ProductService — Handles all product-related operations.
 *
 * Concepts: ArrayList, Iterator, String formatting,
 *           linear search, file persistence.
 */
public class ProductService {

    private List<Product> products;

    public ProductService() {
        this.products = FileHandler.readAllProducts();
        System.out.println("  ✅  Loaded " + products.size() + " products from file.");
    }

    // ── Product Menu ──────────────────────────────

    public void productMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            ConsoleUI.printProductMenu();
            int choice = ConsoleUI.readInt(scanner, "Enter your choice: ", 1, 7);
            switch (choice) {
                case 1: addProduct(scanner); break;
                case 2: viewAllProducts(scanner); break;
                case 3: searchProduct(scanner); break;
                case 4: updateProduct(scanner); break;
                case 5: deleteProduct(scanner); break;
                case 6: restockProduct(scanner); break;
                case 7: back = true; break;
            }
        }
    }

    // ── 1. Add Product ────────────────────────────

    private void addProduct(Scanner sc) {
        ConsoleUI.printLine();
        System.out.println("  ➕  ADD NEW PRODUCT");
        ConsoleUI.printDivider();

        String newId = generateNextId();
        System.out.println("  Auto-generated Product ID: " + newId);

        String name     = ConsoleUI.readString(sc, "Product Name     : ");
        System.out.println("  Categories: Grocery | Dairy | Beverages | Snacks | Personal Care | Other");
        String category = ConsoleUI.readString(sc, "Category         : ");
        double price    = ConsoleUI.readDouble(sc, "Price (Rs.)      : ");
        int    stock    = ConsoleUI.readInt(sc,    "Initial Stock    : ", 0, 99999);
        System.out.println("  Units: kg | pcs | litre | pack | box | dozen");
        String unit     = ConsoleUI.readString(sc, "Unit             : ");

        Product p = new Product(newId, name, category, price, stock, unit);
        products.add(p);
        FileHandler.writeAllProducts(products);

        ConsoleUI.success("Product '" + name + "' added successfully with ID: " + newId);
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── 2. View All Products ──────────────────────

    public void viewAllProducts(Scanner sc) {
        if (products.isEmpty()) {
            ConsoleUI.warn("No products found in inventory.");
            ConsoleUI.pressEnterToContinue(sc);
            return;
        }

        System.out.println("\n  📦  ALL PRODUCTS (" + products.size() + " total)");

        Map<String, List<Product>> byCategory = new LinkedHashMap<>();
        for (Product p : products) {
            byCategory.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<String, List<Product>> entry : byCategory.entrySet()) {
            System.out.println("\n  [ " + entry.getKey() + " ]");
            ConsoleUI.printProductTableHeader();
            for (Product p : entry.getValue()) {
                System.out.println("  " + p.toTableRow());
                if (p.getStock() <= 5) {
                    System.out.println("    ⚠️  LOW STOCK ALERT!");
                }
            }
            ConsoleUI.printProductTableFooter();
        }
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── 3. Search Product ─────────────────────────

    private void searchProduct(Scanner sc) {
        ConsoleUI.printLine();
        System.out.println("  🔍  SEARCH PRODUCT");
        ConsoleUI.printDivider();
        String keyword = ConsoleUI.readString(sc, "Enter product name or ID: ").toLowerCase();

        List<Product> found = new ArrayList<>();
        for (Product p : products) {
            if (p.getProductId().toLowerCase().contains(keyword)
                    || p.getName().toLowerCase().contains(keyword)
                    || p.getCategory().toLowerCase().contains(keyword)) {
                found.add(p);
            }
        }

        if (found.isEmpty()) {
            ConsoleUI.error("No products found matching '" + keyword + "'.");
        } else {
            System.out.println("  Found " + found.size() + " result(s):");
            ConsoleUI.printProductTableHeader();
            for (Product p : found) {
                System.out.println("  " + p.toTableRow());
            }
            ConsoleUI.printProductTableFooter();
        }
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── 4. Update Product ─────────────────────────

    private void updateProduct(Scanner sc) {
        ConsoleUI.printLine();
        System.out.println("  ✏️   UPDATE PRODUCT");
        ConsoleUI.printDivider();
        String id = ConsoleUI.readString(sc, "Enter Product ID to update: ").toUpperCase();

        Product p = findById(id);
        if (p == null) {
            ConsoleUI.error("Product ID '" + id + "' not found.");
            ConsoleUI.pressEnterToContinue(sc);
            return;
        }

        System.out.println("  Current: " + p.toTableRow());
        ConsoleUI.printDivider();
        System.out.println("  1. Name    2. Category    3. Price    4. Unit    5. Cancel");
        int choice = ConsoleUI.readInt(sc, "Enter choice: ", 1, 5);

        switch (choice) {
            case 1: p.setName(ConsoleUI.readString(sc, "New Name: ")); break;
            case 2: p.setCategory(ConsoleUI.readString(sc, "New Category: ")); break;
            case 3: p.setPrice(ConsoleUI.readDouble(sc, "New Price (Rs.): ")); break;
            case 4: p.setUnit(ConsoleUI.readString(sc, "New Unit: ")); break;
            case 5: ConsoleUI.info("Update cancelled."); ConsoleUI.pressEnterToContinue(sc); return;
        }

        FileHandler.writeAllProducts(products);
        ConsoleUI.success("Product '" + p.getName() + "' updated successfully.");
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── 5. Delete Product ─────────────────────────

    private void deleteProduct(Scanner sc) {
        ConsoleUI.printLine();
        System.out.println("  🗑️   DELETE PRODUCT");
        ConsoleUI.printDivider();
        String id = ConsoleUI.readString(sc, "Enter Product ID to delete: ").toUpperCase();

        Product p = findById(id);
        if (p == null) {
            ConsoleUI.error("Product ID '" + id + "' not found.");
            ConsoleUI.pressEnterToContinue(sc);
            return;
        }

        System.out.println("  Product: " + p.toTableRow());
        boolean confirm = ConsoleUI.readYesNo(sc, "Are you sure you want to delete this product?");
        if (confirm) {
            products.remove(p);
            FileHandler.writeAllProducts(products);
            ConsoleUI.success("Product '" + p.getName() + "' deleted successfully.");
        } else {
            ConsoleUI.info("Deletion cancelled.");
        }
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── 6. Restock Product ────────────────────────

    private void restockProduct(Scanner sc) {
        ConsoleUI.printLine();
        System.out.println("  📦  RESTOCK PRODUCT");
        ConsoleUI.printDivider();
        String id = ConsoleUI.readString(sc, "Enter Product ID: ").toUpperCase();

        Product p = findById(id);
        if (p == null) {
            ConsoleUI.error("Product ID '" + id + "' not found.");
            ConsoleUI.pressEnterToContinue(sc);
            return;
        }

        System.out.printf("  Current stock of '%s': %d %s%n", p.getName(), p.getStock(), p.getUnit());
        int qty = ConsoleUI.readInt(sc, "Add quantity: ", 1, 99999);
        p.addStock(qty);
        FileHandler.writeAllProducts(products);
        ConsoleUI.success(qty + " " + p.getUnit() + " added. New stock: " + p.getStock() + " " + p.getUnit());
        ConsoleUI.pressEnterToContinue(sc);
    }

    // ── Public helpers ────────────────────────────

    public Product findById(String id) {
        for (Product p : products) {
            if (p.getProductId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(products);
    }

    public void saveProducts() {
        FileHandler.writeAllProducts(products);
    }

    // ── Private helpers ───────────────────────────

    private String generateNextId() {
        int maxNum = 0;
        for (Product p : products) {
            try {
                String numPart = p.getProductId().replaceAll("[^0-9]", "");
                int num = Integer.parseInt(numPart);
                if (num > maxNum) maxNum = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("P%03d", maxNum + 1);
    }
}
