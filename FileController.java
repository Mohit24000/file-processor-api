package com.learnSpringBoot.File.Processor.controller;

import com.learnSpringBoot.File.Processor.dto.UploadResponse;
import com.learnSpringBoot.File.Processor.repo.InMemoryTaskRepo;
import com.learnSpringBoot.File.Processor.service.FileProcessingService;
import com.learnSpringBoot.File.Processor.model.TaskStatus;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileProcessingService service;
    private final InMemoryTaskRepo repo;

    public FileController(FileProcessingService service, InMemoryTaskRepo repo) {
        this.service = service;
        this.repo = repo;
    }

    /** Upload file -> returns 202 Accepted with taskId */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(new UploadResponse("", "Empty file"));
        String id = service.storeAndCreateTask(file);
        return ResponseEntity.accepted().body(new UploadResponse(id, "PENDING"));
    }

    /** Check task status */
    @GetMapping("/status/{id}")
    public ResponseEntity<?> status(@PathVariable String id) {
        InMemoryTaskRepo.TaskRecord r = service.getTask(id);
        if (r == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error","Task not found"));
        return ResponseEntity.ok(Map.of(
                "id", r.id,
                "filename", r.originalFilename,
                "status", r.status,
                "message", r.message
        ));
    }

    /** List tasks */
    @GetMapping("/tasks")
    public ResponseEntity<List<Object>> list() {
        return ResponseEntity.ok(service.listAll().values().stream().map(r ->
                Map.of("id", r.id, "filename", r.originalFilename, "status", r.status, "message", r.message)
        ).collect(Collectors.toList()));
    }

    /** Download processed file (if completed) */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        InMemoryTaskRepo.TaskRecord r = service.getTask(id);
        if (r == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (r.status != TaskStatus.COMPLETED) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Path p = Paths.get(r.processedPath);
        if (!Files.exists(p)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Resource res = new ByteArrayResource(Files.readAllBytes(p));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(p.getFileName().toString()).build());
        return ResponseEntity.ok().headers(headers).contentLength(Files.size(p)).contentType(MediaType.APPLICATION_OCTET_STREAM).body(res);
    }

    /** Cancel a task */
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        boolean ok = service.cancel(id);
        return ok ? ResponseEntity.ok(Map.of("status","canceled")) : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","cannot cancel"));
    }
}
