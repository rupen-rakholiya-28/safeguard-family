package com.childprotection.api.model;

import com.childprotection.api.model.enums.DeviceStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "app_version")
    private String appVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.ACTIVE;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        lastSeenAt = LocalDateTime.now();
    }

    // Constructors
    public Device() {}

    public Device(User child, Family family, String deviceName) {
        this.child = child;
        this.family = family;
        this.deviceName = deviceName;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getChild() { return child; }
    public void setChild(User child) { this.child = child; }

    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public Integer getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = batteryLevel; }

    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
