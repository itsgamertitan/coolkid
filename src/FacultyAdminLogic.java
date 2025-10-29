import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class FacultyAdminLogic {
    private final String studentsFile;
    private final String supervisorsFile;
    private List<Student> students;
    private List<Supervisor> supervisors;
    private static final Scanner scanner = new Scanner(System.in);
    public FacultyAdminLogic(String studentsFile, String supervisorsFile, List<Student> students, List<Supervisor> supervisors) {
        this.studentsFile = studentsFile;
        this.supervisorsFile = supervisorsFile;
        this.students=students;
        this.supervisors=supervisors;
    }

    public void runFacultyAdminPortal() {
        int choice;
        while (true) {
            System.out.println("==================================================");
            System.out.println("               FACULTY ADMIN PORTAL               ");
            System.out.println("==================================================");
            System.out.println("1. View supervisor-student assignment list");
            System.out.println("2. Assign or Reassign Students");
            System.out.println("3. Filter by Program");
            System.out.println("4. Generate Reports");
            System.out.println("5. Logout");
            System.out.println("==================================================");

            System.out.print("Select an option (1-5): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); 
                continue;
            }

            switch (choice) {
                case 1 -> viewSupervisorXStudents();
                case 2 -> {
                    Reassign();
                    Log.writeLog("redirecting to reassign");
                }
                case 3 -> filterprogram();
                case 4 -> viewReport();
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid option. Please choose between 1 and 5.");
            }
        }
    }

    public void viewSupervisorXStudents() {
        try (BufferedReader reader = new BufferedReader(new FileReader(studentsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                System.out.println(parts[1] + " | " + parts[5]);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void Reassign() {
        boolean ok = true;

        while (ok) {
            System.out.println("Enter Student Name: ");
            String studentName = scanner.nextLine();
            System.out.println("Enter New Supervisor: ");
            String supervisorName = scanner.nextLine();
            Supervisor originalSupervisor = null;
            boolean confirm = false;
            boolean control = false;
            Student StudentToModify = null;
            Supervisor SupervisorToModify = null;

            for (Student student : students) {
                Log.writeLog("Checking student: [" + student.getUsername() + "]");
                if (student.getUsername().trim().equalsIgnoreCase(studentName.trim())) {
                    StudentToModify = student;
                    Log.writeLog("Student found and modified: " + student.getUsername());
                    confirm = true;
                    break;
                }
            }


            for (Supervisor supervisor : supervisors) {
                Log.writeLog("Checking supervisor: [" + supervisor.getUsername() + "]");
                if (supervisor.getUsername().trim().equalsIgnoreCase(supervisorName.trim())) {
                    SupervisorToModify = supervisor;
                    control = true;
                    Log.writeLog("Supervisor found: " + supervisor.getUsername());
                    break;
                }
            }

            if (!control || !confirm) {
                Log.writeLog("Student or Supervisor not found");
            } else {
                String temp = StudentToModify.getSupervisor();
                for (Supervisor supervisor:supervisors) {
                    if (supervisor.getUsername().equals(temp)) {
                        originalSupervisor = supervisor;
                    }
                }
                originalSupervisor.removeStudent(studentName);
                StudentToModify.setSupervisor(supervisorName);
                SupervisorToModify.setStudent(studentName);
                FileHandling.saveAllStudents(students, studentsFile);
                FileHandling.saveAllSupervisors(supervisors, supervisorsFile);
                Log.writeLog("Reassignment completed");
            }

            ok = false; 
        }
    }

    public void filterprogram() {
        System.out.println("Please enter a program: ");

        if (!scanner.hasNextLine()) {
            System.out.println("No input available.");
            return;
        }

        String programInput = scanner.nextLine().trim();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(studentsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] fields = line.split("\\|");
                if (fields.length >= 5 && fields[4].equalsIgnoreCase(programInput)) {
                    System.out.println(fields[1]);
                    found = true;
                }
            }

            if (!found) {
                System.out.println("No students found for that program.");
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        Log.writeLog("program filtered");
    }

    public void viewReport() {
        Log.writeLog("start of");
        int choice;
        boolean control = true;
        for (Student S : students) {
                while (control) {
                    Log.writeLog("while control loop");
                    System.out.println("ENTER 1 TO VIEW STUDENTS PER PROGRAM: ");
                    System.out.println("ENTER 2 TO VIEW STUDENTS PER SUPERVISOR: ");

                    if (!scanner.hasNextInt()) {
                        System.out.println("Invalid input. Enter 1 or 2.");
                        scanner.nextLine();
                        continue;
                    }

                    choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1 -> {
                            System.out.println("Program: " + S.getProgram());
                            Log.writeLog("students viewed for program");
                            control = false;
                        }
                        case 2 -> {
                            System.out.println("Supervisor: " + S.getSupervisor());
                            Log.writeLog("students view supervisor viewed");
                            control = false;
                        }
                        default -> {
                            System.out.println("Please choose between 1-2");
                            continue;
                        }
                    }
                }
            
        }
    }
}
