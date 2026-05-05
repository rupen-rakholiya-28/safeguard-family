package com.childprotection.api.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UsageEventRequest {
    private UUID deviceId;
    private List<AppUsageEntry> events;

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID d) { this.deviceId = d; }
    public List<AppUsageEntry> getEvents() { return events; }
    public void setEvents(List<AppUsageEntry> e) { this.events = e; }

    public static class AppUsageEntry {
        private String packageName;
        private String appName;
        private Long usageDurationMs;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public String getPackageName() { return packageName; }
        public void setPackageName(String p) { this.packageName = p; }
        public String getAppName() { return appName; }
        public void setAppName(String a) { this.appName = a; }
        public Long getUsageDurationMs() { return usageDurationMs; }
        public void setUsageDurationMs(Long u) { this.usageDurationMs = u; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime s) { this.startTime = s; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime e) { this.endTime = e; }
    }
}
