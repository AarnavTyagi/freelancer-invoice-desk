package com.freelancedesk.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Writes reports to CSV files. The three reports are exported in parallel. */
public class ReportExporter {
    private static final Path EXPORT_DIR = Path.of("exports");

    private final ReportService reports;

    public ReportExporter(ReportService reports) {
        this.reports = reports;
    }

    public List<Path> exportAll(int freelancerId) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            List<Callable<Path>> tasks = List.of(
                    () -> write("client_outstanding.csv", "Client", reports.clientOutstanding()),
                    () -> write("monthly_earnings.csv", "Month", reports.monthlyEarnings(freelancerId)),
                    () -> write("collection_summary.csv", "Metric", reports.collectionSummary()));
            List<Path> files = new ArrayList<>();
            for (Future<Path> result : pool.invokeAll(tasks)) {
                files.add(result.get());
            }
            return files;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Export was interrupted.", e);
        } catch (ExecutionException e) {
            throw new IOException("Export failed: " + e.getCause().getMessage(), e.getCause());
        } finally {
            pool.shutdown();
        }
    }

    private Path write(String fileName, String labelHeader, Map<String, BigDecimal> data) throws IOException {
        Files.createDirectories(EXPORT_DIR);
        Path file = EXPORT_DIR.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(labelHeader + ",Amount");
            writer.newLine();
            for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
                writer.write(escape(entry.getKey()) + "," + entry.getValue().toPlainString());
                writer.newLine();
            }
        }
        return file;
    }

    private String escape(String value) {
        return value.contains(",") ? "\"" + value + "\"" : value;
    }
}
