package com.learnSpringBoot.File.Processor.service;

import com.learnSpringBoot.File.Processor.model.TaskStatus;
import com.learnSpringBoot.File.Processor.repo.InMemoryTaskRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileProcessingService {

    private final InMemoryTaskRepo repo;
    private final Path storageRoot;
    private final VirusScanner virusScanner; // may be null if bean absent
    private final TaskExecutor taskExecutor;

    public FileProcessingService(InMemoryTaskRepo repo,
                                 @Value("${app.storage.path:./storage}") String storagePath,
                                 org.springframework.beans.factory.ObjectProvider<VirusScanner> virusScannerProvider,
                                 TaskExecutor taskExecutor) throws IOException {
        this.repo = repo;
        this.storageRoot = Paths.get(storagePath).toAbsolutePath();
        Files.createDirectories(storageRoot);
        this.virusScanner = virusScannerProvider.getIfAvailable();
        this.taskExecutor = taskExecutor;
    }

    public String storeAndCreateTask(MultipartFile file) throws IOException {
        String id = UUID.randomUUID().toString();
        String safeName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        Path taskDir = storageRoot.resolve(id);
        Files.createDirectories(taskDir);
        Path stored = taskDir.resolve(safeName);
        Files.copy(file.getInputStream(), stored, StandardCopyOption.REPLACE_EXISTING);

        InMemoryTaskRepo.TaskRecord rec = new InMemoryTaskRepo.TaskRecord(id, safeName);
        rec.storedPath = stored.toString();
        repo.put(rec);

        // submit background work
        processAsync(id);
        return id;
    }

    // Async wrapper that will run independently
    @Async("fileProcessorExecutor")
    public void processAsync(String id) {
        InMemoryTaskRepo.TaskRecord rec = repo.get(id);
        if (rec == null) return;
        rec.status = TaskStatus.PROCESSING;
        try {
            Path stored = Paths.get(rec.storedPath);

            // 1) optional virus-scan
            if (virusScanner != null) {
                boolean ok = virusScanner.scan(stored);
                if (!ok) {
                    rec.status = TaskStatus.FAILED;
                    rec.message = "File failed virus scan";
                    return;
                }
            }

            // 2) simulate processing (e.g., convert to uppercase text if text file)
            // For demo: copy to processed.txt and append "processed" suffix
            Path processed = stored.getParent().resolve("processed_" + rec.originalFilename);
            // naive processing: if text file, uppercase lines, else copy
            String lower = rec.originalFilename.toLowerCase();
            if (lower.endsWith(".txt") || lower.endsWith(".log") || lower.endsWith(".csv")) {
                // read -> uppercase -> write
                java.util.List<String> lines = Files.readAllLines(stored);
                java.util.List<String> out = new java.util.ArrayList<>();
                out.add("// processed by FileProcessor");
                for (String ln : lines) out.add(ln.toUpperCase());
                Files.write(processed, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                // just copy as "processed"
                Files.copy(stored, processed, StandardCopyOption.REPLACE_EXISTING);
            }

            // simulate CPU work
            Thread.sleep(700);

            rec.processedPath = processed.toString();
            rec.status = TaskStatus.COMPLETED;
            rec.message = "Processed successfully";
        } catch (Exception e) {
            rec.status = TaskStatus.FAILED;
            rec.message = "Processing failed: " + e.getMessage();
            e.printStackTrace();
        }
    }

    public InMemoryTaskRepo.TaskRecord getTask(String id) { return repo.get(id); }

    public java.util.Map<String, InMemoryTaskRepo.TaskRecord> listAll() { return repo.all(); }

    public boolean cancel(String id) {
        InMemoryTaskRepo.TaskRecord r = repo.get(id);
        if (r == null) return false;
        if (r.status == TaskStatus.COMPLETED) return false;
        r.status = TaskStatus.CANCELED;
        r.message = "Canceled by user";
        // optionally delete stored files
        try {
            Path p = Paths.get(r.storedPath).getParent();
            if (p != null) FileSystemUtils.deleteRecursively(p);
        } catch (Exception ignored) {}
        repo.remove(id);
        return true;
    }
}
