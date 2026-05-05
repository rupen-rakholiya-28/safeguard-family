package com.childprotection.api.controller;

import com.childprotection.api.dto.request.RegisterDeviceRequest;
import com.childprotection.api.dto.response.ApiResponse;
import com.childprotection.api.model.Device;
import com.childprotection.api.model.User;
import com.childprotection.api.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterDeviceRequest request,
            @AuthenticationPrincipal User user) {
        Device device = deviceService.registerDevice(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Device registered", toMap(device)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDevice(@PathVariable UUID id) {
        Device device = deviceService.getDevice(id);
        return ResponseEntity.ok(ApiResponse.ok("Device found", toMap(device)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> unlinkDevice(
            @PathVariable UUID id, @AuthenticationPrincipal User user) {
        deviceService.unlinkDevice(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Device unlinked"));
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<ApiResponse<Void>> heartbeat(
            @PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        deviceService.updateHeartbeat(id, body.get("batteryLevel"));
        return ResponseEntity.ok(ApiResponse.ok("Heartbeat recorded"));
    }

    private Map<String, Object> toMap(Device d) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", d.getId());
        m.put("deviceName", d.getDeviceName());
        m.put("deviceModel", d.getDeviceModel());
        m.put("status", d.getStatus());
        m.put("batteryLevel", d.getBatteryLevel());
        m.put("lastSeenAt", d.getLastSeenAt());
        return m;
    }
}
