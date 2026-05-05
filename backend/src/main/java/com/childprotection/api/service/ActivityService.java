package com.childprotection.api.service;

import com.childprotection.api.dto.request.UsageEventRequest;
import com.childprotection.api.model.*;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ActivityService {

    private final AppUsageEventRepository appUsageEventRepository;
    private final LocationPingRepository locationPingRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public ActivityService(AppUsageEventRepository appUsageEventRepository,
                           LocationPingRepository locationPingRepository,
                           DeviceRepository deviceRepository,
                           UserRepository userRepository) {
        this.appUsageEventRepository = appUsageEventRepository;
        this.locationPingRepository = locationPingRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordUsageEvents(UsageEventRequest request, User child) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found"));

        for (UsageEventRequest.AppUsageEntry entry : request.getEvents()) {
            AppUsageEvent event = new AppUsageEvent();
            event.setChild(child);
            event.setDevice(device);
            event.setPackageName(entry.getPackageName());
            event.setAppName(entry.getAppName());
            event.setUsageDurationMs(entry.getUsageDurationMs());
            event.setStartTime(entry.getStartTime());
            event.setEndTime(entry.getEndTime());
            appUsageEventRepository.save(event);
        }
    }

    public List<AppUsageEvent> getTimeline(UUID childId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return appUsageEventRepository.findByChildIdAndDateRange(childId, start, end);
    }

    public Map<String, Object> getScreenTimeSummary(UUID childId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Object[]> summary = appUsageEventRepository
                .getUsageSummaryByChild(childId, start, end);

        long totalMs = 0;
        List<Map<String, Object>> apps = new ArrayList<>();
        for (Object[] row : summary) {
            String appName = (String) row[0];
            long durationMs = (Long) row[1];
            totalMs += durationMs;
            Map<String, Object> app = new HashMap<>();
            app.put("appName", appName);
            app.put("durationMs", durationMs);
            app.put("durationMinutes", durationMs / 60000);
            apps.add(app);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("date", date.toString());
        result.put("totalMs", totalMs);
        result.put("totalMinutes", totalMs / 60000);
        result.put("apps", apps);
        return result;
    }

    @Transactional
    public void recordLocationPing(UUID childId, UUID deviceId,
                                   Double lat, Double lng, Double accuracy) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        LocationPing ping = new LocationPing();
        ping.setChild(child);
        ping.setDevice(device);
        ping.setLatitude(lat);
        ping.setLongitude(lng);
        ping.setAccuracy(accuracy);
        ping.setRecordedAt(LocalDateTime.now());
        locationPingRepository.save(ping);
    }

    public List<LocationPing> getLocationHistory(UUID childId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return locationPingRepository.findByChildIdAndRecordedAtBetween(childId, start, end);
    }
    
    public Map<String, Object> getLatestLocation(UUID childId) {
        List<LocationPing> latestList = locationPingRepository
                .findByChildIdOrderByRecordedAtDesc(childId);
        
        if (latestList.isEmpty()) {
            return null;
        }
        
        LocationPing ping = latestList.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("latitude", ping.getLatitude());
        result.put("longitude", ping.getLongitude());
        result.put("accuracy", ping.getAccuracy());
        result.put("timestamp", ping.getRecordedAt().toString());
        return result;
    }
}
