package com.childprotection.api.dto.response;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserInfo user;

    public AuthResponse(String accessToken, String refreshToken, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public UserInfo getUser() { return user; }

    public static class UserInfo {
        private String id;
        private String email;
        private String displayName;
        private String role;

        public UserInfo(String id, String email, String displayName, String role) {
            this.id=id; this.email=email; this.displayName=displayName; this.role=role;
        }
        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getDisplayName() { return displayName; }
        public String getRole() { return role; }
    }
}
