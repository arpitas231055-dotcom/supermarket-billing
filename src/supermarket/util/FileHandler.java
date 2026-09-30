package supermarket.util;

import supermarket.model.Product;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileHandler — Centralizes all file I/O operations.
 *
 * File structure:
 *   data/
 *   ├── products.txt        → pipe-delimited product records
 *   ├── bill_index.txt      → one-line summary per bill
 *   ├── bill_counter.txt    → last bill number used
 *   └── bills/
 *       ├── BILL-0001.txt   → full formatted bill receipt
 *       ├── BILL-0002.txt
 *       └── ...
 *
 * Concepts demonstrated: FileWriter, BufferedReader, File,
 *   PrintWriter, IOException, try-with-resources
 */
public class FileHandler {

    public static final String DATA_DIR         = "data";
    public static final String PRODUCTS_FILE    = DATA_DIR + "/products.txt";
    public static final String BILL_INDEX_FILE  = DATA_DIR + "/bill_index.txt";
    public static final String BILL_COUNTER_FILE= DATA_DIR + "/bill_counter.txt";
    public static final String BILLS_DIR        = DATA_DIR + "/bills";

    // ── Initialization ────────────────────────────

    /**
     * Creates required directories and seeds default products on first run.
     */
    public static void initDirectories() {
        new File(DATA_DIR).mkdirs();
        new File(BILLS_DIR).mkdirs();

        // Seed products if file doesn't exist or is empty
        File pf = new File(PRODUCTS_FILE);
        if (!pf.exists() || pf.length() == 0) {
            seedDefaultProducts();
        }

        // Initialize counter file
        File cf = new File(BILL_COUNTER_FILE);
        if (!cf.exists()) {
            writeFile(BILL_COUNTER_FILE, "0");
        }

        // Initialize bill index
        File bi = new File(BILL_INDEX_FILE);
        if (!bi.exists()) {
            try { bi.createNewFile(); } catch (IOException ignored) {}
        }
    }

    // ── Product File Operations ───────────────────

    /** Reads all products from products.txt into a List. */
    public static List<Product> readAllProducts() {
        List<Product> list = new ArrayList<>();
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Product p = Product.fromFileString(line);
                if (p != null) list.add(p);
            }
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not read products file: " + e.getMessage());
        }
        return list;
    }

    /** Writes entire product list back to products.txt (overwrite). */
    public static void writeAllProducts(List<Product> products) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PRODUCTS_FILE, false))) {
            pw.println("# FRESH MART — Product Master File");
            pw.println("# Format: productId|name|category|price|stock|unit");
            pw.println("# -----------------------------------------------");
            for (Product p : products) {
                pw.println(p.toFileString());
            }
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not save products: " + e.getMessage());
        }
    }

    // ── Bill File Operations ──────────────────────

    /** Generates the next bill ID (BILL-0001, BILL-0002, ...). */
    public static String getNextBillId() {
        int counter = 0;
        try {
            String content = readFile(BILL_COUNTER_FILE).trim();
            counter = Integer.parseInt(content);
        } catch (NumberFormatException ignored) {}
        counter++;
        writeFile(BILL_COUNTER_FILE, String.valueOf(counter));
        return String.format("BILL-%04d", counter);
    }

    /** Saves a full bill receipt as a text file in data/bills/. */
    public static void saveBillFile(String billId, String billText) {
        String filePath = BILLS_DIR + "/" + billId + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.print(billText);
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not save bill file: " + e.getMessage());
        }
    }

    /** Appends a summary line to the bill index file. */
    public static void appendBillIndex(String indexLine) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BILL_INDEX_FILE, true))) {
            pw.println(indexLine);
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not update bill index: " + e.getMessage());
        }
    }

    /** Reads all lines from the bill index file. */
    public static List<String> readBillIndex() {
        List<String> lines = new ArrayList<>();
        File file = new File(BILL_INDEX_FILE);
        if (!file.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not read bill index: " + e.getMessage());
        }
        return lines;
    }

    /** Reads and returns the full receipt text of a specific bill. */
    public static String readBillFile(String billId) {
        String filePath = BILLS_DIR + "/" + billId + ".txt";
        File file = new File(filePath);
        if (!file.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    // ── Report File Operations ────────────────────

    /** Writes a report to data/report_<date>.txt and returns the file path. */
    public static String saveReport(String reportText, String filename) {
        String filePath = DATA_DIR + "/" + filename;
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.print(reportText);
            return filePath;
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not save report: " + e.getMessage());
            return null;
        }
    }

    // ── Generic helpers ───────────────────────────

    private static String readFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (IOException e) {
            return "0";
        }
    }

    private static void writeFile(String path, String content) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, false))) {
            pw.print(content);
        } catch (IOException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    // ── Seed data ─────────────────────────────────

    private static void seedDefaultProducts() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PRODUCTS_FILE, false))) {
            pw.println("# FRESH MART — Product Master File");
            pw.println("# Format: productId|name|category|price|stock|unit");
            pw.println("# -----------------------------------------------");
            // Grocery
            pw.println("P001|Basmati Rice|Grocery|65.00|200|kg");
            pw.println("P002|Wheat Flour (Atta)|Grocery|45.00|150|kg");
            pw.println("P003|Refined Sugar|Grocery|42.00|180|kg");
            pw.println("P004|Toor Dal|Grocery|110.00|100|kg");
            pw.println("P005|Mustard Oil|Grocery|120.00|80|litre");
            pw.println("P006|Salt (Tata)|Grocery|20.00|200|kg");
            // Dairy
            pw.println("P007|Full Cream Milk|Dairy|58.00|100|litre");
            pw.println("P008|Amul Butter|Dairy|55.00|60|pack");
            pw.println("P009|Paneer|Dairy|85.00|40|pack");
            pw.println("P010|Curd (Plain)|Dairy|35.00|70|pack");
            // Beverages
            pw.println("P011|Tata Tea Gold|Beverages|180.00|50|pack");
            pw.println("P012|Nescafe Classic|Beverages|250.00|40|pack");
            pw.println("P013|Cold Drink (600ml)|Beverages|40.00|120|pcs");
            pw.println("P014|Packaged Water (1L)|Beverages|20.00|200|pcs");
            // Snacks
            pw.println("P015|Lays Chips|Snacks|20.00|150|pcs");
            pw.println("P016|Biscuits (Parle-G)|Snacks|10.00|300|pcs");
            pw.println("P017|Kurkure|Snacks|20.00|100|pcs");
            pw.println("P018|Dark Fantasy Cookies|Snacks|35.00|80|pcs");
            // Personal Care
            pw.println("P019|Dove Soap|Personal Care|45.00|90|pcs");
            pw.println("P020|Colgate Toothpaste|Personal Care|80.00|60|pcs");
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not seed products: " + e.getMessage());
        }
    }
}
