import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileHandling {

    // ==========================================================
    // --- LOAD METHODS (Reading files) ---
    // ==========================================================

    public static List<Student> loadStudents(String filename){
        List<Student> students =new ArrayList<>();
        try(BufferedReader reader=new BufferedReader(new FileReader(filename))){
            String line; 
            while((line=reader.readLine())!=null){
                if(line.trim().isEmpty()) continue;
                String [] parts=line.split("\\|");
                Student student=new Student(parts);
                students.add(student);
            }
        }
        catch(IOException e){
            System.out.println("Error getting student info");
        }
        return students;
    }

    public static List<User> loadUsers(String filename){
        List<User> users=new ArrayList<>();
        try(BufferedReader reader=new BufferedReader(new FileReader(filename))){
            String Userline; 
            while((Userline=reader.readLine())!=null){
                if(Userline.trim().isEmpty()) continue;
                String [] parts=Userline.split("\\|");
                User user=new User(parts);
                users.add(user);
            }
        }
        catch(IOException e){
            System.out.println("Error getting user info");
        }
        return users;
    }

    public static List<Supervisor> loadSupervisors (String filename){
        List<Supervisor> supervisors=new ArrayList<>();
        try(BufferedReader reader=new BufferedReader(new FileReader(filename))){
            String line; 
            while((line=reader.readLine())!=null){
                if(line.trim().isEmpty()) continue;
                String [] parts=line.split("\\|");
                if(parts.length>=4){
                    Supervisor supervisor=new Supervisor(parts);
                    supervisors.add(supervisor);
                }
            }
        }
        catch(IOException e){
            System.out.println("Error fetching supervisor data");
        }
        return supervisors;
    }
    
public static List<FacultyAdmin> loadFacultyAdmin(String filename) {
    List<FacultyAdmin> facultyAdmins = new ArrayList<>();
    
    try (Scanner scanner = new Scanner(new File(filename))) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split("\\|"); 
            
            if (data.length >= 3) {
                String userId = data[0].trim();
                String username = data[1].trim();
                String password = data[2].trim();
                
                FacultyAdmin admin = new FacultyAdmin(userId, username, password); 
                facultyAdmins.add(admin);
            } else {
                System.err.println("Skipping invalid FacultyAdmin record: " + Arrays.toString(data));
            }
        }
    } catch (FileNotFoundException e) {
        System.err.println("Faculty Admin file not found: " + filename);
    }
    
    return facultyAdmins;
}

    public static List<SystemAdmin> loadSystemAdmin(String filename){
        List<SystemAdmin> systemAdmins=new ArrayList<>();
        try(BufferedReader reader=new BufferedReader(new FileReader(filename))){
            String Userline; 
            while((Userline=reader.readLine())!=null){
                if(Userline.trim().isEmpty()) continue;
                
                String [] parts=Userline.split("\\|");
                if (parts.length >= 3) {
                    
                    SystemAdmin systemadmin = new SystemAdmin(parts[0], parts[1], parts[2]);
                    systemAdmins.add(systemadmin);
                } else {
                    System.err.println("Skipping malformed SystemAdmin entry: " + Userline);
                }
            }
        }
        catch(IOException e){
            System.out.println("Error getting SystemAdmin info");
        }
        return systemAdmins;
    }

    
    public static void saveToStudent(Student student, String filename){
        // 'true' flag ensures APPEND mode
        try(FileWriter writer=new FileWriter(filename, true)){
            writer.write(student.toFileString() + "\n");
        }
        
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToUser(User user, String filename){
        // 'true' flag ensures APPEND mode
        try(FileWriter userWriter=new FileWriter(filename, true)){
            userWriter.write(user.toFileString() + "\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToSupervisor(Supervisor supervisor, String filename){
        // 'true' flag ensures APPEND mode
        try(FileWriter writer=new FileWriter(filename, true)){
            writer.write(supervisor.toFileString() + "\n");
        }
        
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToFacultyAdmin(FacultyAdmin facultyAdmin, String filename){
        // 'true' flag ensures APPEND mode
        try(FileWriter writer=new FileWriter(filename,true)){
            writer.write(facultyAdmin.toFileString() +"\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveToSystemAdmin(SystemAdmin systemAdmin, String filename){
        // 'true' flag ensures APPEND mode
        try(FileWriter writer=new FileWriter(filename,true)){
            writer.write(systemAdmin.toFileString() +"\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void saveAllUsers(List<User> users, String userFile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(userFile, false))) {
            for (User user : users) {
                pw.println(user.toFileString()); 
            }
            Log.writeLog("User list successfully saved to " + userFile);
        } catch (IOException e) {
            System.err.println("Error saving user file: " + e.getMessage());
            Log.writeLog("ERROR: Failed to save users to " + userFile + ": " + e.getMessage());
        }
    }
    
    public static void saveAllStudents(List<Student> students, String filename) {
        // 'false' flag ensures OVERWRITE mode
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) { 
            for (Student student : students) {
                pw.println(student.toFileString());
            }
            Log.writeLog("Edited student list successfully saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving student file: " + e.getMessage());
            Log.writeLog("Saving failed.."+ filename);
        }
    }

    public static void saveAllSupervisors(List<Supervisor> supervisors, String filename) {
        // 'false' flag ensures OVERWRITE mode
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) {
            for (Supervisor supervisor : supervisors) {
                pw.println(supervisor.toFileString());
            }
            Log.writeLog("Edited supervisor list successfully saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving supervisor file: " + e.getMessage());
            Log.writeLog("Saving failed..."+ filename);
        }
    }

    public static void saveAllFacultyAdmins(List<FacultyAdmin> facultyAdmins, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) {
            for (FacultyAdmin facultyAdmin : facultyAdmins) {
                pw.println(facultyAdmin.toFileString());
            }
            Log.writeLog("Edited faculty admin list successfully saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving faculty admin file: " + e.getMessage());
            Log.writeLog("Saving failed..."+ filename);
        }
    }

    public static void saveAllSystemAdmins(List<SystemAdmin> systemAdmins, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) {
            for (SystemAdmin systemAdmin : systemAdmins) {
                pw.println(systemAdmin.toFileString());
            }
            Log.writeLog("Edited system admins list successfully saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving system admin file: " + e.getMessage());
            Log.writeLog("Saving failed..."+ filename);
        }
    }
    
    public static List<Appointment> loadAppointments(String filename) {
        List<Appointment> appointments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 6) {
                    Log.writeLog("Skipping invalid appointment line: " + line);
                    continue;
                }
                try {
                    Appointment appointment = new Appointment(parts);
                    appointments.add(appointment);
                } catch (Exception ex) {
                    Log.writeLog("Error parsing appointment: " + line + " - " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error getting appointment info");
        }
        return appointments;
    }

    public static void saveToAppointment(Appointment appointment, String filename) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(appointment.toFileString() + "\n");
            Log.writeLog("Appointment saved to " + filename + ": " + appointment.toFileString());
        } catch (IOException e) {
            Log.writeLog("Error saving appointment to " + filename + ": " + e.getMessage());
            e.printStackTrace();    
        }
    }

    public static void saveAllAppointments(List<Appointment> appointments, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) {
            for (Appointment appoint : appointments) {
                pw.println(appoint.toFileString());
            }
        } catch (IOException e) {
            System.err.println("Error saving appointment file: " + e.getMessage());
        }
    }

    public Student findStudentById(String studentId) {
        List<Student> students = loadStudents("students.txt");
        for (Student student : students) {
            if (student.getUserId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    public Supervisor findSupervisorByUsername(String username) {
        List<Supervisor> supervisors = loadSupervisors("supervisors.txt");
        for (Supervisor supervisor : supervisors) {
            if (supervisor.getUsername().equals(username)) {
                return supervisor;
            }
        }
        return null;
    }
}

