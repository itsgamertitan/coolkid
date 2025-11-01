import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SupervisorLogic {
    private static final String APPOINTMENTS_FILE = "appointments.txt";
    private final String appointmentsFile;

    public SupervisorLogic() {
        this(APPOINTMENTS_FILE);
    }

    public SupervisorLogic(String appointmentsFile) {
    this.appointmentsFile = appointmentsFile;
    }
    public void supervisorPortal(Supervisor supervisor) {
        new SupervisorPortalGUI(supervisor, this);
    }

    public List<Appointment> getAppointments() {
    return FileHandling.loadAppointments(appointmentsFile);
    }

    public void approveOrRejectAppointment(Supervisor supervisor, String appointmentId, String decision) {
        List<Appointment> appointments = FileHandling.loadAppointments(appointmentsFile);
        boolean found = false;
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)
                && appointment.getSupervisorUsername().equals(supervisor.getUsername())) {
                switch (decision.toUpperCase()) {
                    case "A" -> appointment.setStatus("Approved");
                    case "R" -> appointment.setStatus("Rejected");
                    default -> {
                        Log.writeLog("Invalid decision attempted: " + decision);
                        return;
                    }
                }
                found = true;
                Log.writeLog("Appointment " + appointmentId + " " + 
                    (decision.equalsIgnoreCase("A") ? "approved" : "rejected") + 
                    " by supervisor " + supervisor.getUsername());
                break;
            }
        }
        if (found) {
            FileHandling.saveAllAppointments(appointments, appointmentsFile);
            Log.writeLog("Appointment status update saved successfully");
        } else {
            Log.writeLog("Appointment not found or not authorized: " + appointmentId);
        }
    }

    public String getFeedback(String appointmentId) {
        List<Appointment> appointments = FileHandling.loadAppointments(appointmentsFile);
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                return appointment.getFeedback();
            }
        }
        return null;
    }

    public void updateFeedback(String appointmentId, String feedback) {
        List<Appointment> appointments = FileHandling.loadAppointments(appointmentsFile);
        boolean found = false;
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                appointment.setFeedback(feedback);
                found = true;
                Log.writeLog("Feedback updated for appointment: " + appointmentId);
                break;
            }
        }
        if (found) {
            FileHandling.saveAllAppointments(appointments, appointmentsFile);
            Log.writeLog("Feedback update saved successfully");
        } else {
            Log.writeLog("Appointment not found for feedback update: " + appointmentId);
        }
    }

    public void updateAppointmentStatus(String appointmentId, String status) {
        List<Appointment> appointments = FileHandling.loadAppointments(appointmentsFile);
        boolean found = false;
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                appointment.setStatus(status);
                found = true;
                Log.writeLog("Status updated for appointment: " + appointmentId + " to: " + status);
                break;
            }
        }
        if (found) {
            FileHandling.saveAllAppointments(appointments, appointmentsFile);
            Log.writeLog("Status update saved successfully");
        } else {
            Log.writeLog("Appointment not found for status update: " + appointmentId);
        }
    }

    public void addFeedback(String appointmentId, String feedback) {
    updateFeedback(appointmentId, feedback);
    }
}