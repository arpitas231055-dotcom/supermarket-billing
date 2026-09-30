package supermarket;

import supermarket.service.BillingService;
import supermarket.service.ProductService;
import supermarket.service.ReportService;
import supermarket.util.ConsoleUI;
import supermarket.util.FileHandler;
import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════╗
 *   SUPERMARKET BILLING SYSTEM — Core Java
 *   Concepts: OOP, Collections, File Handling,
 *             String formatting, Exception handling
 * ╚══════════════════════════════════════════════╝
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        FileHandler.initDirectories();

        ProductService productService = new ProductService();
        BillingService billingService = new BillingService(productService);
        ReportService  reportService  = new ReportService(billingService);

        ConsoleUI.printBanner();

        boolean running = true;
        while (running) {
            ConsoleUI.printMainMenu();
            int choice = ConsoleUI.readInt(scanner, "Enter your choice: ", 1, 6);

            switch (choice) {
                case 1: productService.productMenu(scanner); break;
                case 2: billingService.createNewBill(scanner); break;
                case 3: billingService.viewAllBills(); break;
                case 4: billingService.searchBill(scanner); break;
                case 5: reportService.generateDailyReport(); break;
                case 6:
                    ConsoleUI.printLine();
                    System.out.println("  Thank you for using Supermarket Billing System!");
                    System.out.println("  Goodbye! Have a great day. 🛒");
                    ConsoleUI.printLine();
                    running = false;
                    break;
            }
        }
        scanner.close();
    }
}
