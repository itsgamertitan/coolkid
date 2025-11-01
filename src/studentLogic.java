import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class studentLogic {
    private final String appointmentFile;
    private List<Appointment> appointments = FileHandling.loadAppointments("appointments.txt");

    public void studentPortal(Student student) {
        new StudentPortalGUI(student, this);
    }

    public studentLogic(String appointmentFile) {
        System.out.println("[DEBUG] Entered studentLogic constructor");
        this.appointmentFile = appointmentFile;
    }

    public void viewAppointments(Student student) {
        Log.writeLog("Student " + student.getUserId() + " viewed their appointments.");
        boolean foundAppointment = false;
        for (Appointment appointment : appointments) {
            if (appointment.getStudentId().equals(student.getUserId())) {
                foundAppointment = true;
            }
        }
        if (!foundAppointment) {
            Log.writeLog("No appointments found for student " + student.getUserId());
        }
    }

    public void makeAppointment(Student student, String dateTime, String supervisorUsername) {
        Log.writeLog("Student " + student.getUserId() + " initiated appointment creation.");

        int nextNum = 1;
        
        if (!appointments.isEmpty()) {
            for (Appointment a : appointments) {
                String id = a.getAppointmentId();
                if (id != null && id.toLowerCase().startsWith("apt-")) {
                    try {
                        int num = Integer.parseInt(id.substring(4)); 
                        if (num >= nextNum) {
                            nextNum = num + 1;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } else {
             try (BufferedReader reader = new BufferedReader(new FileReader(appointmentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split("\\|");
                    if (parts.length > 0 && parts[0].toLowerCase().startsWith("apt-")) {
                        try {
                            int num = Integer.parseInt(parts[0].substring(4));
                            if (num >= nextNum) nextNum = num + 1;
                        } catch (NumberFormatException ignored) { }
                    }
                }
            } catch (IOException e) {
            }
        }
        String appointmentId = String.format("Apt-%03d", nextNum);
        Appointment newAppointment = new Appointment(
            appointmentId,
            student.getUserId(),
            student.getUsername(),
            supervisorUsername,
            dateTime,
            "Pending",
            "");
        FileHandling.saveToAppointment(newAppointment, appointmentFile);
        appointments.add(newAppointment); 
        Log.writeLog("New appointment created: ID " + appointmentId + " by student " + student.getUserId() + " for time: " + dateTime + " with supervisor: " + supervisorUsername);
    }

    public void rescheduleAppointment(Student student, String appointmentId, String newDateTime) {
        Log.writeLog("Student " + student.getUserId() + " attempting to reschedule appointment.");
        boolean found = false;

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId) && 
                appointment.getStudentId().equals(student.getUserId())) {

                if (!isAppointmentModifiable(appointment)) {
                    Log.writeLog("Cannot reschedule appointment " + appointmentId + " with status: " + appointment.getStatus());
                    return;
                }

                String oldDateTime = appointment.getDateTime();
                appointment.setDateTime(newDateTime);
                appointment.setStatus("Pending");
                found = true;
                
                Log.writeLog("Reschedule: Appointment " + appointmentId + " moved from " + oldDateTime + 
                           " to " + newDateTime + ". Status reset to Pending.");
                break;
            }
        }

        if (found) {
            try (FileWriter writer = new FileWriter(appointmentFile, false)) {
                for (Appointment a : appointments) {
                    writer.write(a.toFileString() + "\n");
                }
                Log.writeLog("Appointment rescheduling saved successfully for " + appointmentId);
            } catch (IOException e) {
                Log.writeLog("FATAL FILE ERROR: Failed to rewrite appointment file after rescheduling " + 
                           appointmentId + ": " + e.getMessage());
            }
        } else {
            Log.writeLog("Reschedule failed: Appointment ID " + appointmentId + 
                        " not found for student ID: " + student.getUserId());
        }
    }

    public void cancelAppointment(Student student, String appointmentId) {
        Log.writeLog("Student " + student.getUserId() + " attempting to cancel appointment.");
        boolean found = false;

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId) && 
                appointment.getStudentId().equals(student.getUserId())) {
                
                if (appointment.getStatus().equalsIgnoreCase("Cancelled")) {
                    Log.writeLog("Appointment " + appointmentId + " is already cancelled.");
                    return;
                }
                
                appointment.setStatus("Cancelled");
                found = true;
                Log.writeLog("Appointment " + appointmentId + " cancelled by student " + student.getUserId());
                break;
            }
        }

        if (found) {
            try (FileWriter writer = new FileWriter(appointmentFile, false)) {
                for (Appointment a : appointments) {
                    writer.write(a.toFileString() + "\n");
                }
                Log.writeLog("Appointment cancellation saved successfully for " + appointmentId);
            } catch (IOException e) {
                Log.writeLog("FATAL FILE ERROR: Failed to rewrite appointment file after cancelling " + 
                           appointmentId + ": " + e.getMessage());
            }
        } else {
            Log.writeLog("Cancellation failed: Appointment ID " + appointmentId + 
                        " not found for student ID: " + student.getUserId());
        }
    }

    private boolean isAppointmentModifiable(Appointment appointment) {
        String status = appointment.getStatus().toLowerCase();
        return !status.equals("cancelled") && 
               !status.equals("approved") && 
               !status.equals("rejected");
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }
}
