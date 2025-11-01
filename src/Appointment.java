import java.io.Serializable;

public class Appointment implements Serializable {
    private String appointmentId;
    private String studentId;
    private String studentUsername;
    private String supervisorName;
    private String dateTime;
    private String feedback;
    private String status; // e.g., "Pending", "Approved", "Rejected"

    public Appointment(String appointmentId, String studentId, String studentUsername, String supervisorName, String dateTime, String status,
            String feedback) {
        this.appointmentId = appointmentId;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.supervisorName = supervisorName;
        this.dateTime = dateTime;
        this.status = status;
        this.feedback = feedback;
    }

    public Appointment(String[] parts) {
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid appointment data: " + String.join("|", parts));
        }
        this.appointmentId = parts[0];
        this.studentId = parts[1];
        this.studentUsername = parts[2];
        this.supervisorName = parts[3];
        this.dateTime = parts[4];
        this.status = parts[5];
        this.feedback = (parts.length >= 7) ? parts[6] : ""; // aman meski feedback kosong
    }

    public String toFileString() {
        return appointmentId + "|" + studentId + "|" + studentUsername + "|" + supervisorName + "|" + dateTime + "|" + status + "|" + feedback;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", studentUsername='" + studentUsername + '\'' +
                ", supervisorName='" + supervisorName + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", status='" + status + '\'' +
                ", feedback='" + feedback + '\'' +
                '}';
    }

    // Getters and Setters
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSupervisorUsername() {
        return supervisorName;
    }


    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentUsername() {
        FileHandling fileHandler = new FileHandling();
        Student student = fileHandler.findStudentById(studentId);
        return student != null ? student.getUsername() : "Unknown";
    }

    public String getSupervisorId() {
        FileHandling fileHandler = new FileHandling();
        Supervisor supervisor = fileHandler.findSupervisorByUsername(supervisorName);
        return supervisor != null ? supervisor.getUserId() : "Unknown";
    }
}