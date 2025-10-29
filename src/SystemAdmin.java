
public class SystemAdmin extends User {
    
    public SystemAdmin(String adminID , String username, String password){
        super(adminID, username, password, "SystemAdmin");
    }
    
    
    public String toFileString() {
        return super.toFileString();
    }
    
    public String toString() {
        
        return "SystemAdmin{" +
            "AdminID='" + getUserId() + '\'' +
            ", username='" + getUsername() + '\'' +
            ", password='" + getPassword() + '\'' +
            ", role='" + getRole() + '\'' + 
            '}'; 
    }
}