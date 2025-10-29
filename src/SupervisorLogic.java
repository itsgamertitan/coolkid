import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SupervisorLogic{
    private static final Scanner scanner=new Scanner(System.in);
    private static final String APPOINTMENTS_FILE="appointments.txt";
    private static List<Appointment> appointments=FileHandling.loadAppointments("appointment.txt");
    private List<String> assignedStudent;
    
    public void supervisorPortal(Supervisor supervisor) {
        if (supervisor == null) {
            System.out.println("is null");
        };
        while (true) {
            System.out.println("===============================================");
            System.out.println("             SUPERVISOR PORTAL                 ");
            System.out.println("===============================================");
            System.out.println("1. View Assigned Students");
            System.out.println("2. Upload available timeslot");
            System.out.println("3. Approve / Reject Appointment");
            System.out.println("4. Add Feedback for Appointment");
            System.out.println("5. Logout");
            System.out.println("===============================================");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Assigned Students: " + supervisor.getStudent());
                    Log.writeLog("case 1");
                    break;
                case 2:
                    Uploadavailabletimeslot(supervisor);
                    break;
                case 3:
                    Log.writeLog("case3 before");
                    approveOrRejectAppointment(supervisor);
                    Log.writeLog("case 3");
                    break;
                case 4:
                    UpdateFeedback();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void Uploadavailabletimeslot(Supervisor supervisor) {
        boolean e= true;
    }

    public List<String> getStudent(){
        return assignedStudent;
    }

    private static void approveOrRejectAppointment(Supervisor supervisor) {
        List<Appointment> appointments = FileHandling.loadAppointments(APPOINTMENTS_FILE);
        System.out.print("Enter Appointment ID to review: ");
        String appointmentId = scanner.nextLine();
        boolean found = false;
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)
                    && appointment.getSupervisorUsername().equals(supervisor.getUsername())) {

                System.out.println("Appointment found: " + appointment);
                System.out.print("Approve or Reject (A/R): ");
                String decision = scanner.nextLine().trim().toUpperCase();

                switch (decision) {
                    case "A" -> appointment.setStatus("Approved");
                    case "R" -> appointment.setStatus("Rejected");
                    default -> {
                        System.out.println("Invalid input. Please enter A or R.");
                        return;
                    }
                }

                rewriteAppointmentsFile(appointments);
                System.out.println("Appointment status updated successfully.");
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Appointment not found.");
        }
    }

    private static void rewriteAppointmentsFile(List<Appointment> appointments) {
        try (FileWriter writer = new FileWriter(APPOINTMENTS_FILE, false)) {
            for (Appointment a : appointments) {
                writer.write(a.toFileString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error file handling: " + e.getMessage());
        }
    }

    public static void UpdateFeedback() {
    System.out.println("Enter Appointment ID:");
    String appointmentId = scanner.nextLine().trim();

    boolean found = false;
    List<Appointment> appointments = FileHandling.loadAppointments("appointments.txt");
    if (appointments == null || appointments.isEmpty()) {
        System.out.println("No appointments found.");
        Log.writeLog("No appointments found in UpdateFeedback.");
        return;
    }

    for (Appointment appointment : appointments) {
        Log.writeLog("Checking appointment: " + appointment.getAppointmentId());

        if (appointment.getAppointmentId().trim().equalsIgnoreCase(appointmentId)) {
            found = true;
            boolean control = true;

            while (control) {
                System.out.println("\n==============================");
                System.out.println("  FEEDBACK MENU");
                System.out.println("==============================");
                System.out.println("1. Add Feedback");
                System.out.println("2. View Feedback");
                System.out.print("Enter your choice (1 or 2): ");

                int choice;

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                } else {
                    System.out.println("Invalid input. Please enter 1 or 2.");
                    scanner.nextLine();
                    continue;
                }

                switch (choice) {
                    case 1 -> {
                        System.out.println("Enter your feedback:");
                        String updatedFeedback = scanner.nextLine().trim();
                        appointment.setFeedback(updatedFeedback);
                        FileHandling.saveAllAppointments(appointments, "appointments.txt");
                        System.out.println("Feedback successfully updated.");
                        Log.writeLog("Feedback updated for Appointment ID: " + appointmentId);
                        control = false;
                    }
                    case 2 -> {
                        String feedback = appointment.getFeedback();
                        if (feedback == null || feedback.isEmpty()) {
                            System.out.println("No feedback available for this appointment.");
                        } else {
                            System.out.println("Existing Feedback: " + feedback);
                        }
                        control = false;
                    }
                    default -> System.out.println("Please choose between 1 and 2.");
                }
            }

            break;
        }
    }

    if (!found) {
        System.out.println("Appointment ID '" + appointmentId + "' not found.");
        Log.writeLog("Appointment not found in UpdateFeedback: " + appointmentId);
    }
}


}