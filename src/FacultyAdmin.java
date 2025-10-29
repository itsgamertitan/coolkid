import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FacultyAdmin extends User {
    private List<Map<String, String>> SupervisorXStudent;
    public FacultyAdmin(String adminID , String username, String password){
        super(adminID,username,password, "FacultyAdmin");
    }
    public FacultyAdmin(String[] parts){
        super(Arrays.copyOfRange(parts,0,4));
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid student data: " + Arrays.toString(parts));
        }
    }
    public String toFileString() {
        return super.toFileString();
    }
    public String toString() {
    
        return "FacultyAdmin{" +
            "FacultyAdminID='" + getUserId() + '\'' +
            ", username='" + getUsername() + '\'' +
            ", password='" + getPassword() + '\'' +
            ", role='" + getRole() + '\'' + 
            '}'; 
}

}
