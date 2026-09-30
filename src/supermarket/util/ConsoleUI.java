package supermarket.util;

import java.util.Scanner;

/**
 * ConsoleUI — Centralized console display and input helpers.
 * Demonstrates: String.format(), Scanner, input validation loops.
 */
public class ConsoleUI {

    public static final String LINE  = "  " + "═".repeat(54);
    public static final String DLINE = "  " + "─".repeat(54);

    // ── Banner & Menus ────────────────────────────

    public static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                      ║");
        System.out.println("  ║        🛒  FRESH MART SUPERMARKET  🛒               ║");
        System.out.println("  ║           Supermarket Billing System                 ║");
        System.out.println("  ║              Core Java Console App                   ║");
        System.out.println("  ║                                                      ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }

    public static void printMainMenu() {
        System.out.println();
        System.out.println(LINE);
        System.out.println("  📋  MAIN MENU");
        System.out.println(LINE);
        System.out.println("   1.  📦  Product Management");
        System.out.println("   2.  🧾  Create New Bill");
        System.out.println("   3.  📄  View All Bills");
        System.out.println("   4.  🔍  Search Bill by ID");
        System.out.println("   5.  📊  Daily Sales Report");
        System.out.println("   6.  🚪  Exit");
        System.out.println(LINE);
    }

    public static void printProductMenu() {
        System.out.println();
        System.out.println(LINE);
        System.out.println("  📦  PRODUCT MANAGEMENT");
        System.out.println(LINE);
        System.out.println("   1.  ➕  Add New Product");
        System.out.println("   2.  📋  View All Products");
        System.out.println("   3.  🔍  Search Product");
        System.out.println("   4.  ✏️   Update Product");
        System.out.println("   5.  🗑️   Delete Product");
        System.out.println("   6.  📦  Restock Product");
        System.out.println("   7.  ⬅️   Back to Main Menu");
        System.out.println(LINE);
    }

    // ── Table headers ─────────────────────────────

    public static void printProductTableHeader() {
        System.out.println();
        System.out.println("  " + "─".repeat(80));
        System.out.printf("  | %-8s | %-22s | %-12s | %8s | %6s | %-6s |%n",
            "ID", "Name", "Category", "Price", "Stock", "Unit");
        System.out.println("  " + "─".repeat(80));
    }

    public static void printProductTableFooter() {
        System.out.println("  " + "─".repeat(80));
    }

    public static void printBillListHeader() {
        System.out.println();
        System.out.println("  " + "─".repeat(82));
        System.out.printf("  | %-10s | %-18s | %-12s | %5s | %10s | %-6s |%n",
            "Bill ID", "Customer", "Date & Time", "Items", "Total", "Mode");
        System.out.println("  " + "─".repeat(82));
    }

    public static void printBillListFooter() {
        System.out.println("  " + "─".repeat(82));
    }

    // ── Input readers ─────────────────────────────

    /**
     * Reads an integer from the user within [min, max].
     * Loops until valid input is entered.
     */
    public static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print("  " + prompt);
            try {
                String input = sc.nextLine().trim();
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) return val;
                System.out.printf("  ⚠  Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Reads a double (price/amount) from the user. Must be > 0.
     */
    public static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt);
            try {
                double val = Double.parseDouble(sc.nextLine().trim());
                if (val > 0) return val;
                System.out.println("  ⚠  Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid number. Try again.");
            }
        }
    }

    /**
     * Reads a non-empty string from the user.
     */
    public static String readString(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt);
            String val = sc.nextLine().trim();
            if (!val.isEmpty()) return val;
            System.out.println("  ⚠  Input cannot be empty.");
        }
    }

    /**
     * Reads a 10-digit phone number.
     */
    public static String readPhone(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt);
            String val = sc.nextLine().trim();
            if (val.matches("\\d{10}")) return val;
            System.out.println("  ⚠  Please enter a valid 10-digit phone number.");
        }
    }

    /**
     * Reads Y/N from user. Returns true for Y.
     */
    public static boolean readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (Y/N): ");
            String val = sc.nextLine().trim().toUpperCase();
            if (val.equals("Y")) return true;
            if (val.equals("N")) return false;
            System.out.println("  ⚠  Please enter Y or N.");
        }
    }

    // ── Status messages ───────────────────────────

    public static void success(String msg) {
        System.out.println("  ✅  " + msg);
    }

    public static void error(String msg) {
        System.out.println("  ❌  " + msg);
    }

    public static void info(String msg) {
        System.out.println("  ℹ️   " + msg);
    }

    public static void warn(String msg) {
        System.out.println("  ⚠️   " + msg);
    }

    public static void printLine() {
        System.out.println(LINE);
    }

    public static void printDivider() {
        System.out.println(DLINE);
    }

    public static void pressEnterToContinue(Scanner sc) {
        System.out.print("\n  Press Enter to continue...");
        sc.nextLine();
    }
}
