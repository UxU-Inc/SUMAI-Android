package co.kr.sumai.net;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String toString() {
        return email+password;
    }
}
