import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Supervisor extends User {
    private List<String> assignedStudent;
    private String timeSlot;
    public Supervisor(String supervisorID , String username, String password, List<String> assignedStudent){
        super(supervisorID,username,password, "Supervisor");
        this.assignedStudent=assignedStudent;
    }
    public Supervisor(String[] parts){
        super(Arrays.copyOfRange(parts,0,4));
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid student data: " + Arrays.toString(parts));
        }
        String studentString=parts[4];
        if(studentString.isEmpty()){
            this.assignedStudent=new ArrayList<>();
        }
        else{this.assignedStudent=Arrays.asList(studentString.split(","));}
    }
    public String toFileString() {
        String assignedStudentString=String.join(",", assignedStudent);
        return super.toFileString() + "|" + assignedStudentString;
    }
    public String toString() {
    
        return "Supervisor{" +
            "userId='" + getUserId() + '\'' +
            ", username='" + getUsername() + '\'' +
            ", password='" + getPassword() + '\'' +
            ", role='" + getRole() + '\'' +
            ", student='" + assignedStudent + '\'' + 
            '}'; 
}
    public List<String> getStudent(){
        return assignedStudent;
    }
    
    public String getTime(){
        return timeSlot;
    }
}
