package com.childprotection.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public class JoinFamilyRequest {
    @NotBlank
    private String inviteCode;

    @NotBlank
    private String displayName;

    private String email;
    private String password;

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String c) { this.inviteCode = c; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String n) { this.displayName = n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
}
