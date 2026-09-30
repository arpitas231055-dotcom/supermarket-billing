package supermarket.service;

import supermarket.util.ConsoleUI;
import supermarket.util.FileHandler;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ReportService — Generates daily sales reports from the bill index file.
 *
 * Report includes:
 *   - Total bills count
 *   - Total revenue
 *   - Revenue by payment mode (Cash / Card / UPI)
 *   - Average bill value
 *   - Highest and lowest bill
 *   - Bill-wise summary table
 *
 * Saved to: data/report_DD-MM-YYYY.txt
 *
 * Concepts: File reading, String.split(), Math operations,
 *           Collections, formatted output.
 */
public class ReportService {

    private final BillingService billingService;

    public ReportService(BillingService billingService) {
        this.billingService = billingService;
    }

    public void generateDailyReport() {
        ConsoleUI.printLine();
        System.out.println("  📊  DAILY SALES REPORT");
        ConsoleUI.printDivider();

        List<String> indexLines = FileHandler.readBillIndex();
        if (indexLines.isEmpty()) {
            ConsoleUI.warn("No bills found. Cannot generate report.");
            return;
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // ── Parse bill index ──────────────────────
        int    totalBills     = 0;
        double totalRevenue   = 0;
        double highestBill    = 0;
        double lowestBill     = Double.MAX_VALUE;
        String highestBillId  = "";
        String lowestBillId   = "";

        Map<String, Double>  revenueByMode  = new LinkedHashMap<>();
        Map<String, Integer> countByMode    = new LinkedHashMap<>();
        revenueByMode.put("Cash", 0.0);
        revenueByMode.put("Card", 0.0);
        revenueByMode.put("UPI",  0.0);
        countByMode.put("Cash", 0);
        countByMode.put("Card", 0);
        countByMode.put("UPI",  0);

        List<String[]> billRows = new ArrayList<>();

        for (String line : indexLines) {
            // Format: billId|name|phone|dateTime|items|total|payMode
            String[] parts = line.split("\\|");
            if (parts.length < 7) continue;

            try {
                String billId    = parts[0].trim();
                String customer  = parts[1].trim();
                String dateTime  = parts[3].trim();
                int    items     = Integer.parseInt(parts[4].trim());
                double amount    = Double.parseDouble(parts[5].trim());
                String mode      = parts[6].trim();

                totalBills++;
                totalRevenue += amount;

                if (amount > highestBill) { highestBill = amount; highestBillId = billId; }
                if (amount < lowestBill)  { lowestBill  = amount; lowestBillId  = billId; }

                revenueByMode.merge(mode, amount, Double::sum);
                countByMode.merge(mode, 1, Integer::sum);

                billRows.add(new String[]{billId, customer, dateTime,
                    String.valueOf(items), String.format("%.2f", amount), mode});

            } catch (NumberFormatException ignored) {}
        }

        if (totalBills == 0) {
            ConsoleUI.warn("No valid bill records found.");
            return;
        }

        double avgBill = totalRevenue / totalBills;

        // ── Build report text ─────────────────────
        StringBuilder report = new StringBuilder();
        String genTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"));

        report.append("\n");
        report.append("  ╔══════════════════════════════════════════════════════╗\n");
        report.append("  ║           FRESH MART — DAILY SALES REPORT           ║\n");
        report.append(String.format("  ║  Date    : %-43s║\n", today));
        report.append(String.format("  ║  Generated: %-42s║\n", genTime));
        report.append("  ╠══════════════════════════════════════════════════════╣\n");
        report.append("  ║                  SALES SUMMARY                      ║\n");
        report.append("  ╠══════════════════════════════════════════════════════╣\n");
        report.append(String.format("  ║  Total Bills      : %-33d║\n", totalBills));
        report.append(String.format("  ║  Total Revenue    : Rs. %-29s║\n",
            String.format("%,.2f", totalRevenue)));
        report.append(String.format("  ║  Average Bill     : Rs. %-29s║\n",
            String.format("%,.2f", avgBill)));
        report.append(String.format("  ║  Highest Bill     : Rs. %-19s (%s)  ║\n",
            String.format("%,.2f", highestBill), highestBillId));
        report.append(String.format("  ║  Lowest Bill      : Rs. %-19s (%s)  ║\n",
            String.format("%,.2f", lowestBill), lowestBillId));
        report.append("  ╠══════════════════════════════════════════════════════╣\n");
        report.append("  ║               PAYMENT MODE BREAKDOWN                ║\n");
        report.append("  ╠══════════════════════════════════════════════════════╣\n");

        for (String mode : new String[]{"Cash", "Card", "UPI"}) {
            double rev   = revenueByMode.getOrDefault(mode, 0.0);
            int    count = countByMode.getOrDefault(mode, 0);
            double pct   = totalRevenue > 0 ? (rev / totalRevenue * 100) : 0;
            report.append(String.format("  ║  %-6s : %3d bills  Rs. %-12s  (%5.1f%%)   ║\n",
                mode, count, String.format("%,.2f", rev), pct));
        }

        report.append("  ╠══════════════════════════════════════════════════════╣\n");
        report.append("  ║                 BILL-WISE DETAILS                   ║\n");
        report.append("  ╠══════════════════════════════════════════════════════╣\n");
        report.append(String.format("  ║  %-10s  %-16s  %-5s  %-9s  %-5s║\n",
            "Bill ID", "Customer", "Items", "Amount", "Mode"));
        report.append("  ║  ────────────────────────────────────────────────   ║\n");

        for (String[] row : billRows) {
            String custShort = row[1].length() > 14 ? row[1].substring(0, 14) : row[1];
            report.append(String.format("  ║  %-10s  %-16s  %5s  Rs.%-7s  %-5s║\n",
                row[0], custShort, row[3], row[4], row[5]));
        }

        report.append("  ╠══════════════════════════════════════════════════════╣\n");
        report.append(String.format("  ║  TOTAL REVENUE : Rs. %-33s║\n",
            String.format("%,.2f", totalRevenue)));
        report.append("  ╚══════════════════════════════════════════════════════╝\n");

        // ── Print to console ──────────────────────
        System.out.println(report);

        // ── Save to file ──────────────────────────
        String filename = "report_" + today + ".txt";
        String savedPath = FileHandler.saveReport(report.toString(), filename);
        if (savedPath != null) {
            ConsoleUI.success("Report saved to: " + savedPath);
        }
    }
}
