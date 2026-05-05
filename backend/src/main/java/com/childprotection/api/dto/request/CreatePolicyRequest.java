package com.childprotection.api.dto.request;

import com.childprotection.api.model.enums.PolicyType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public class CreatePolicyRequest {
    @NotNull
    private UUID childId;

    @NotNull
    private PolicyType policyType;

    private Integer dailyLimitMinutes;
    private String appPackages;
    private LocalTime startTime;
    private LocalTime endTime;
    private String daysOfWeek;

    public UUID getChildId() { return childId; }
    public void setChildId(UUID c) { this.childId = c; }
    public PolicyType getPolicyType() { return policyType; }
    public void setPolicyType(PolicyType p) { this.policyType = p; }
    public Integer getDailyLimitMinutes() { return dailyLimitMinutes; }
    public void setDailyLimitMinutes(Integer d) { this.dailyLimitMinutes = d; }
    public String getAppPackages() { return appPackages; }
    public void setAppPackages(String a) { this.appPackages = a; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime s) { this.startTime = s; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime e) { this.endTime = e; }
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String d) { this.daysOfWeek = d; }
}
