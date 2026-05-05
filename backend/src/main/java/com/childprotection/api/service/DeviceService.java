package com.childprotection.api.service;

import com.childprotection.api.dto.request.RegisterDeviceRequest;
import com.childprotection.api.model.*;
import com.childprotection.api.model.enums.DeviceStatus;
import com.childprotection.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final FamilyRepository familyRepository;
    private final AuditLogService auditLogService;

    public DeviceService(DeviceRepository deviceRepository,
                         FamilyRepository familyRepository,
                         AuditLogService auditLogService) {
        this.deviceRepository = deviceRepository;
        this.familyRepository = familyRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Device registerDevice(RegisterDeviceRequest request, User child) {
        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new RuntimeException("Family not found"));

        Device device = new Device(child, family, request.getDeviceName());
        device.setDeviceModel(request.getDeviceModel());
        device.setOsVersion(request.getOsVersion());
        device.setAppVersion(request.getAppVersion());
        device.setFcmToken(request.getFcmToken());
        device = deviceRepository.save(device);

        auditLogService.log(family.getId(), child.getId(), "REGISTER_DEVICE",
                "DEVICE", device.getId().toString(),
                "Device registered: " + request.getDeviceName());

        return device;
    }

    public Device getDevice(UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
    }

    public List<Device> getDevicesByChild(UUID childId) {
        return deviceRepository.findByChildId(childId);
    }

    public List<Device> getDevicesByFamily(UUID familyId) {
        return deviceRepository.findByFamilyId(familyId);
    }

    @Transactional
    public void unlinkDevice(UUID deviceId, User user) {
        Device device = getDevice(deviceId);
        device.setStatus(DeviceStatus.UNLINKED);
        deviceRepository.save(device);

        auditLogService.log(device.getFamily().getId(), user.getId(), "UNLINK_DEVICE",
                "DEVICE", deviceId.toString(), "Device unlinked");
    }

    @Transactional
    public void updateHeartbeat(UUID deviceId, Integer batteryLevel) {
        Device device = getDevice(deviceId);
        device.setLastSeenAt(LocalDateTime.now());
        if (batteryLevel != null) device.setBatteryLevel(batteryLevel);
        deviceRepository.save(device);
    }
}
