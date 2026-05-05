package com.childprotection.api.dto.request;

import com.childprotection.api.model.enums.ConsentFeature;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ConsentGrantRequest {
    @NotNull
    private UUID childId;

    @NotNull
    private ConsentFeature featureName;

    private UUID deviceId;
    private String policyVersion;

    public UUID getChildId() { return childId; }
    public void setChildId(UUID c) { this.childId = c; }
    public ConsentFeature getFeatureName() { return featureName; }
    public void setFeatureName(ConsentFeature f) { this.featureName = f; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID d) { this.deviceId = d; }
    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String v) { this.policyVersion = v; }
}
