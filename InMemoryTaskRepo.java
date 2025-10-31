package com.learnSpringBoot.File.Processor.repo;

import com.learnSpringBoot.File.Processor.model.TaskStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTaskRepo {
    // store minimal info per task
    public static class TaskRecord {
        public final String id;
        public final String originalFilename;
        public volatile TaskStatus status;
        public volatile String message;
        public volatile String storedPath;
        public volatile String processedPath;

        public TaskRecord(String id, String originalFilename) {
            this.id = id;
            this.originalFilename = originalFilename;
            this.status = TaskStatus.PENDING;
            this.message = "";
        }
    }

    private final Map<String, TaskRecord> store = new ConcurrentHashMap<>();

    public void put(TaskRecord r) { store.put(r.id, r); }
    public TaskRecord get(String id) { return store.get(id); }
    public Map<String, TaskRecord> all() { return store; }
    public void remove(String id) { store.remove(id); }
}
