import java.util.Arrays;

public class Student extends User {

    private String program;
    private String supervisor;
    public Student(String studentId, String username, String password, String program, String supervisor) {
        super(studentId, username, password, "Student");
        this.program = program;
        this.supervisor=supervisor;
    }

    public Student(String[] parts) {
        super(Arrays.copyOfRange(parts, 0, 4));
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid student data: " + Arrays.toString(parts));
        }
        this.program = parts[4];
        this.supervisor=parts[5];
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + program + "|" + supervisor;
    }

    @Override
   public String toString() {
    return "Student{" +
            "userId='" + getUserId() + '\'' +
            ", username='" + getUsername() + '\'' +
            ", password='" + getPassword() + '\'' +
            ", role='" + getRole() + '\'' +
            ", program='" + program + '\'' +
            ", supervisor='" + supervisor + '\'' + 
            '}'; 
}

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }
    
    public String getSupervisor() {
        return supervisor;
    }

}