package com.childprotection.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class RegisterDeviceRequest {
    @NotBlank
    private String deviceName;

    private String deviceModel;
    private String osVersion;
    private String appVersion;
    private String fcmToken;
    private UUID familyId;

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String d) { this.deviceName = d; }
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String d) { this.deviceModel = d; }
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String o) { this.osVersion = o; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String a) { this.appVersion = a; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String f) { this.fcmToken = f; }
    public UUID getFamilyId() { return familyId; }
    public void setFamilyId(UUID f) { this.familyId = f; }
}
