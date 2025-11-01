import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SupervisorLogic {
    private static final String APPOINTMENTS_FILE = "appointments.txt";
    private final List<Appointment> appointments;
    private final String appointmentsFile;

    public SupervisorLogic() {
        this(APPOINTMENTS_FILE);
    }

    public SupervisorLogic(String appointmentsFile) {
        this.appointmentsFile = appointmentsFile;
        this.appointments = FileHandling.loadAppointments(appointmentsFile);
    }
    public void supervisorPortal(Supervisor supervisor) {
        new SupervisorPortalGUI(supervisor, this);
    }

    public List<Appointment> getAppointments() {
        return FileHandling.loadAppointments(appointmentsFile);
    }

    public void approveOrRejectAppointment(Supervisor supervisor, String appointmentId, String decision) {
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
            try (FileWriter writer = new FileWriter(APPOINTMENTS_FILE, false)) {
                for (Appointment a : appointments) {
                    writer.write(a.toFileString() + "\n");
                }
                Log.writeLog("Appointment status update saved successfully");
            } catch (IOException e) {
                Log.writeLog("Error updating appointment file: " + e.getMessage());
            }
        } else {
            Log.writeLog("Appointment not found or not authorized: " + appointmentId);
        }
    }

    public String getFeedback(String appointmentId) {
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                return appointment.getFeedback();
            }
        }
        return null;
    }

    public void updateFeedback(String appointmentId, String feedback) {
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