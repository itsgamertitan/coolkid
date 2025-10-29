import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Supervisor extends User {
    private List<String> assignedStudent;
    private String timeSlot;

    public Supervisor(String supervisorID, String username, String password, List<String> assignedStudent) {
        super(supervisorID, username, password, "Supervisor");
        this.assignedStudent = new ArrayList<>(assignedStudent != null ? assignedStudent : new ArrayList<>());
    }

    public Supervisor(String[] parts) {
        super(Arrays.copyOfRange(parts, 0, 4));
        String studentString = parts.length > 4 ? parts[4].trim() : "";
        if (studentString.isEmpty()) {
            this.assignedStudent = new ArrayList<>();
            Log.writeLog("Supervisor with no assigned students initialized");
        } else {
            this.assignedStudent = new ArrayList<>(Arrays.asList(studentString.split(",")));
            Log.writeLog("Supervisor initialized with students: " + this.assignedStudent);
        }
    }

    @Override
    public String toFileString() {
        String assignedStudentString = String.join(",", assignedStudent);
        return super.toFileString() + "|" + assignedStudentString;
    }

    public List<String> getStudent() {
        if (assignedStudent == null) {
            assignedStudent = new ArrayList<>();
            Log.writeLog("Assigned student list was null, reinitialized");
        }
        return assignedStudent;
    }

    public void setStudent(String newStudent) {
        if (assignedStudent == null) {
            assignedStudent = new ArrayList<>();
            Log.writeLog("Assigned student list was null, reinitialized");
        }
        for (String s : assignedStudent) {
            if (s.equalsIgnoreCase(newStudent)) {
                Log.writeLog("Student already assigned: " + newStudent);
                return;
            }
        }
        assignedStudent.add(newStudent);
        Log.writeLog("New student assigned successfully: " + newStudent);
    }

    public void removeStudent(String studentBad) {
        if (assignedStudent == null) {
            assignedStudent = new ArrayList<>();
            Log.writeLog("Assigned student list was null, reinitialized");
            return;
        }
        if (assignedStudent.removeIf(s -> s.equalsIgnoreCase(studentBad))) {
            Log.writeLog("Removed student: " + studentBad);
        } else {
            Log.writeLog("Student not found for removal: " + studentBad);
        }
    }
}
