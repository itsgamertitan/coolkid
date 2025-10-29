import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class studentLogic {
    private static final Scanner scanner = new Scanner(System.in);
    private final String appointmentFile;
    private List<Appointment> appointments = FileHandling.loadAppointments("appointments.txt");

    public studentLogic(String appointmentFile) {
        this.appointmentFile = appointmentFile;
    }

    public void studentPortal(Student student) {
        
        Student currentStudent = student;
        Log.writeLog("Student Portal entered for user: " + currentStudent.getUserId());
        
        while (true) {
            System.out.println("===============================================");
            System.out.println("            STUDENT PORTAL - Welcome, " + currentStudent.getUserId());
            System.out.println("===============================================");
            System.out.println("1. View My Appointments");
            System.out.println("2. Make a New Appointment");
            System.out.println("3. Reschedule Appointment");
            System.out.println("4. Cancel Appointments");
            System.out.println("5. Logout");
            System.out.println("===============================================");
            
            int choice;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Invalid input. Please enter a number.");
                Log.writeLog("Invalid non-numeric input in student portal menu for user: " + currentStudent.getUserId());
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1 -> viewAppointments(currentStudent);
                case 2 -> makeAppointment(currentStudent);
                case 3 -> rescheduleAppointment(currentStudent);
                case 4 -> cancelAppointment(currentStudent);
                case 5 -> {
                    Log.writeLog("Student " + currentStudent.getUserId() + " logged out.");
                    return; 
                }
                default -> {
                    System.out.println("Invalid option.");
                    Log.writeLog("Invalid menu choice selected by student: " + currentStudent.getUserId() + " choice: " + choice);
                }
            }
        }
    }

    private void viewAppointments(Student student) {
        Log.writeLog("Student " + student.getUserId() + " viewed their appointments.");
        System.out.println("Your Appointments:");
        boolean foundAppointment = false;
        for (Appointment appointment : appointments) {
            if (appointment.getStudentId().equals(student.getUserId())) {
                System.out.println(appointment);
                foundAppointment = true;
            }
        }
        if (!foundAppointment) {
             System.out.println("You currently have no appointments.");
        }
    }

    private void makeAppointment(Student student) {
        Log.writeLog("Student " + student.getUserId() + " initiated appointment creation.");
        System.out.print("Enter Date and Time (e.g., 2025-10-25 10:00): ");
        String dateTime = scanner.nextLine();

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
                student.getSupervisor(),
                dateTime,
                "Pending",
                "");

        FileHandling.saveToAppointment(newAppointment, appointmentFile);
        appointments.add(newAppointment); 
        
        System.out.println("Appointment requested successfully with ID: " + appointmentId);
        Log.writeLog("New appointment created: ID " + appointmentId + " by student " + student.getUserId() + " for time: " + dateTime);
    }

    private void rescheduleAppointment(Student student) {
        Log.writeLog("Student " + student.getUserId() + " attempting to reschedule appointment.");
        System.out.print("Enter Appointment ID to reschedule: ");
        String appointmentId = scanner.nextLine();
        boolean found = false;

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId) && appointment.getStudentId().equals(student.getUserId())) {

                if (appointment.getStatus().equalsIgnoreCase("Cancelled")) {
                    System.out.println("Cannot reschedule a cancelled appointment.");
                    Log.writeLog("Reschedule failed: Appointment " + appointmentId + " is already cancelled.");
                    return;
                } else if (appointment.getStatus().equalsIgnoreCase("Approved")) {
                    System.out.println("Cannot reschedule an approved appointment.");
                    Log.writeLog("Reschedule failed: Appointment " + appointmentId + " is already approved.");
                    return;
                } else if (appointment.getStatus().equalsIgnoreCase("Rejected")) {
                    System.out.println("Cannot reschedule a rejected appointment.");
                    Log.writeLog("Reschedule failed: Appointment " + appointmentId + " is already rejected.");
                    return;
                }

                System.out.print("Enter new Date and Time (e.g., 2023-10-01 10:00): ");
                String newDateTime = scanner.nextLine();
                
                String oldDateTime = appointment.getDateTime();
                appointment.setDateTime(newDateTime);
                appointment.setStatus("Pending");
                found = true;
                
                Log.writeLog("Reschedule initiated: Appointment " + appointmentId + " moved from " + oldDateTime + " to " + newDateTime + ". Status reset to Pending.");
                break;
            }
        }

        if (found) {
            try (FileWriter writer = new FileWriter(appointmentFile, false)) {
                for (Appointment a : appointments) {
                    writer.write(a.toFileString() + "\n");
                }
                System.out.println("Appointment rescheduled successfully (Awaiting Supervisor approval).");
            } catch (IOException e) {
                System.out.println("Error file handling during rewrite: " + e.getMessage());
                Log.writeLog("FATAL FILE ERROR: Failed to rewrite appointment file after rescheduling " + appointmentId + ": " + e.getMessage());
            }
        } else {
            System.out.println("Appointment not found or you do not have permission to modify it.");
            Log.writeLog("Reschedule failed: Appointment ID " + appointmentId + " not found for student ID: " + student.getUserId());
        }
    }

    private void cancelAppointment(Student student) {
        Log.writeLog("Student " + student.getUserId() + " attempting to cancel appointment.");
        System.out.print("Enter Appointment ID to cancel: ");
        String appointmentId = scanner.nextLine();
        boolean found = false;

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)
                    && appointment.getStudentId().equals(student.getUserId())) {
                
                if (appointment.getStatus().equalsIgnoreCase("Cancelled")) {
                     System.out.println("Appointment is already cancelled. No action taken.");
                     return;
                }
                
                appointment.setStatus("Cancelled");
                found = true;
                Log.writeLog("Appointment " + appointmentId + " cancelled by student " + student.getUserId() + ".");
                break;
            }
        }

        if (found) {
            try (FileWriter writer = new FileWriter(appointmentFile, false)) {
                for (Appointment a : appointments) {
                    writer.write(a.toFileString() + "\n");
                }
                System.out.println("Appointment cancelled successfully (status set to 'Cancelled').");
            } catch (IOException e) {
                System.out.println("Error file handling during rewrite: " + e.getMessage());
                Log.writeLog("FATAL FILE ERROR: Failed to rewrite appointment file after cancelling " + appointmentId + ": " + e.getMessage());
            }
        } else {
            System.out.println("Appointment not found or you do not have permission to modify it.");
            Log.writeLog("Cancellation failed: Appointment ID " + appointmentId + " not found for student ID: " + student.getUserId());
        }
    }
}
