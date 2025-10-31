package com.learnSpringBoot.File.Processor.dto;

import com.learnSpringBoot.File.Processor.model.TaskStatus;

public class TaskInfo {
    private final String id;
    private final String originalFilename;
    private final TaskStatus status;
    private final String message;
    private final String storedPath; // where file is stored
    private final String processedPath; // where processed file is stored (if any)

    public TaskInfo(String id, String originalFilename, TaskStatus status, String message, String storedPath, String processedPath) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.status = status;
        this.message = message;
        this.storedPath = storedPath;
        this.processedPath = processedPath;
    }

    public String getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public TaskStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public String getStoredPath() { return storedPath; }
    public String getProcessedPath() { return processedPath; }
}
