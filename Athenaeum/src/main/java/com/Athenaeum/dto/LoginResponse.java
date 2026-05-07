package com.Athenaeum.dto;

public class LoginResponse {

    private String token;
    private String username;
    private String email;
    private String message;
    private boolean success;

    public LoginResponse() {}

    public LoginResponse(String token, String username, String email, boolean success) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.success = success;
        this.message = success ? "Login successful" : "Login failed";
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}