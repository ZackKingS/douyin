package com.douyin.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "username不能为空")
    @Size(min = 4, max = 20, message = "username长度需要在4到20之间")
    private String username;

    @NotBlank(message = "password不能为空")
    @Size(min = 6, max = 32, message = "password长度需要在6到32之间")
    private String password;

    @NotBlank(message = "nickname不能为空")
    private String nickname;

    private String phone;
    private String email;
    private String inviteCode;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
