import java.io.Serializable;
import java.util.Arrays;

public class User implements Serializable {

    protected String userId;
    protected String username;
    protected String password;
    protected String role;

    public User(String userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid user data: " + Arrays.toString(parts));
        }
        this.userId = parts[0];
        this.username = parts[1];
        this.password = parts[2];
        this.role = parts[3];
    }

    public String toFileString() {
        return userId + "|" + username + "|" + password + "|" + role;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setUserID(String userId){
        this.userId=userId;
    }
    public void setRole(String userRole){
        this.role=userRole;
    }


}

