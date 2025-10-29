import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SystemAdminLogic {
    
    private List<User> users;
    private final Scanner scanner;
    private static final String FailedLogin="FailedLogin.txt";
    private final String studentsFile;
    private final String supervisorsFile;
    private final String facultyAdminFile;
    private final String systemAdminFile;
    private final String userFile;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private User loggedInUser;
    private List<Student> students;
    private List<Supervisor> supervisors;
    private List<SystemAdmin> systemAdmins;
    private List<FacultyAdmin> facultyAdmins;


    public SystemAdminLogic(List<User> users, Scanner scanner, String studentsFile, String supervisorsFile, String facultyAdminFile, String systemAdminFile, String userFile, User loggedInUser, List<SystemAdmin> systemAdmins, List<Student> students, List<FacultyAdmin> facultyAdmins, List<Supervisor> supervisors) {
        this.users = users;
        this.scanner = scanner;
        this.studentsFile = studentsFile;
        this.supervisorsFile = supervisorsFile;
        this.facultyAdminFile = facultyAdminFile;
        this.systemAdminFile = systemAdminFile;
        this.userFile = userFile;
        this.loggedInUser=loggedInUser;
        this.students=students;
        this.facultyAdmins=facultyAdmins;
        this.systemAdmins=systemAdmins;
        this.supervisors=supervisors;
    }
    
    private String getNonBlankInput(String prompt, String fieldName) {
        String input = "";
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isBlank()) {
                System.out.println("Error: " + fieldName + " cannot be left blank. Please re-enter.");
            } else {
                return input;
            }
        }
    }

    private void synchronizeGlobalLists() {
        this.students = FileHandling.loadStudents(studentsFile);
        this.supervisors = FileHandling.loadSupervisors(supervisorsFile);
        this.facultyAdmins = FileHandling.loadFacultyAdmin(facultyAdminFile);
        this.systemAdmins = FileHandling.loadSystemAdmin(systemAdminFile);
        this.users = FileHandling.loadUsers(userFile);
    }

    public void runAdminPortal() {
        Log.writeLog("Current Admin editing: "+loggedInUser);
        int choice;
        while(true){
            System.out.println("==================================================");
            System.out.println("             SYSTEM ADMINISTRATOR PORTAL           ");
            System.out.println("==================================================");
            System.out.println("1. View All Users and Roles");
            System.out.println("2. Manage User Accounts (Create/Edit/Delete)");
            System.out.println("3. Monitor Failed Login Attempts");
            System.out.println("4. Reset User Password");
            System.out.println("5. Wipe All User Role Data (DANGEROUS)");
            System.out.println("6. View Failed Logins");
            System.out.println("7. Return to Main Menu");
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

            switch(choice){
                case 1:
                    viewAllUsers();
                    break;
                case 2:
                    manageUserAccount();
                    break;
                case 3:
                    monitorFailedLogins();
                    break;
                case 4:
                    resetUserPassword();
                    break;
                case 5:
                    wipeAllUsers();
                    break;
                case 6:
                    viewFailedLogin(); 
                case 7:
                    Log.writeLog("System Admin "+ loggedInUser.getUsername()+" exited Admin Portal.");
                    return;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 5.");
            }
        }
    }

    public void viewAllUsers() {
        System.out.println("\n==================================================");
        System.out.println("             LIST OF ALL SYSTEM USERS              ");
        System.out.println("==================================================");
        if (users.isEmpty()) {
            System.out.println("No users found in the system.");
            return;
        }

        System.out.printf("%-15s %-20s %s\n", "USER ID", "USERNAME", "ROLE");
        System.out.println("--------------------------------------------------");
        for (User user : users) {
            System.out.printf("%-15s %-20s %s\n", 
                user.getUserId(), 
                user.getUsername(), 
                user.getRole());
        }
        System.out.println("--------------------------------------------------");
    }

    public void manageUserAccount() {
        int choice;
        while(true){
            System.out.println("\n==================================================");
            System.out.println("             USER ACCOUNT MANAGEMENT               ");
            System.out.println("==================================================");
            System.out.println("1. Create New User");
            System.out.println("2. Edit Existing User Account");
            System.out.println("3. Delete User Account");
            System.out.println("4. Back to Admin Portal");
            System.out.println("==================================================");

            System.out.print("Select an option (1-4): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            switch(choice){
                case 1:
                    createNewUser();
                    break; 
                case 2:
                    editUser();
                    break;
                case 3:
                    deleteUser();
                    break;
                case 4:
                    return; 
                default:
                    System.out.println("Invalid option. Please choose between 1 and 4.");
            }
        }
    }

    public void createNewUser(){
        while(true){
            System.out.println("==================================================");
            System.out.println("              NEW USER ACCOUNT CREATION            ");
            System.out.println("==================================================");
            
            String userRoleInput = getNonBlankInput("Enter the role of new user( e.g , Student, Supervisor, Faculty Admin, System Admin: )", "User Role");
            String userRole = userRoleInput.replaceAll("\\s+", "");
            userRole = userRole.substring(0, 1).toUpperCase() + userRole.substring(1).toLowerCase();
            
            switch(userRole){
                case "Student":
                    createStudent();
                    return;
                case "Supervisor":
                    createSupervisor();
                    return;
                case "Facultyadmin":
                    createFacultyAdmin();
                    return;
                case "Systemadmin":
                    createSystemAdmin();
                    return;
                default:
                    System.out.println("Invalid role, please try again.");
                    continue;
            }
        }
    }
    
    public void createStudent(){
        Boolean control=true;
        List<Student> students = FileHandling.loadStudents(studentsFile);
        while(control){
        
        System.out.println("\nPlease enter new student details:");
        String studentId = getNonBlankInput("Enter Student ID: ", "Student ID");
        String username = getNonBlankInput("Enter Username: ", "Username"); 
        String password = getNonBlankInput("Enter Password: ", "Password"); 
        String program = getNonBlankInput("Enter Program (e.g., IT-SE, CS): ", "Program"); 
        String supervisor = getNonBlankInput("Enter Supervisor: ", "Supervisor Name"); 
        if(checkDuplicate(username, studentId)){
            System.out.println("Duplicate or User information found!! Cannot create user, please try again.");
            Log.writeLog("Duplicate or User information found!!");
            continue;
        }
        Student newStudent = new Student(studentId, username, password, program, supervisor);
        User newUser = new User(studentId,username,password,"Student");
        
        students.add(newStudent);
        users.add(newUser);

        FileHandling.saveAllStudents(students, studentsFile);
        FileHandling.saveAllUsers(users, userFile); 
        
        synchronizeGlobalLists(); 

        System.out.println("\nStudent account for " + username + " successfully created and saved.");
        control=false;}
    }
    
    public void createSupervisor(){
        List<Supervisor> supervisors = FileHandling.loadSupervisors(supervisorsFile);
        boolean control=true;
        while(control){
        System.out.println("\nPlease enter new supervisor details:");
        String supervisorID = getNonBlankInput("Enter Supervisor ID: ", "Supervisor ID");
        String username = getNonBlankInput("Enter Username: ", "Username"); 
        String password = getNonBlankInput("Enter Password: ", "Password"); 
        if(checkDuplicate(username, supervisorID)){
            System.out.println("Duplicate or User information found!! Cannot create user, please try again.");
            Log.writeLog("Duplicate or User information found!!");
            continue;
        }
        
        System.out.println("Enter Assigned Students (Press enter with nothing to stop): ");
        List<String> assignedStudents = new ArrayList<>();
        String studentInput;
        while (true) {
            System.out.print("Enter Student ID: ");
            studentInput = scanner.nextLine(); 
            if (studentInput.isBlank()) {
                break; 
            }
            assignedStudents.add(studentInput);
        }
        
        Supervisor newSupervisor = new Supervisor(supervisorID, username, password, assignedStudents);
        User newUser = new User(supervisorID,username,password,"Supervisor");
        
        supervisors.add(newSupervisor);
        users.add(newUser);

        FileHandling.saveAllSupervisors(supervisors, supervisorsFile);
        FileHandling.saveAllUsers(users, userFile); 
        
        synchronizeGlobalLists(); 

        System.out.println("\nSupervisor account for " + username + " successfully created and saved.");
        control=false;}
    }
    
    public void createFacultyAdmin() {
        List<FacultyAdmin> facultyAdmins = FileHandling.loadFacultyAdmin(facultyAdminFile);
        boolean control=true;
        while(control){
        System.out.println("\nPlease enter new faculty admin details:");
        String facultyAdminID = getNonBlankInput("Enter Faculty Admin ID: ", "Faculty Admin ID");
        String username = getNonBlankInput("Enter Username: ", "Username");
        String password = getNonBlankInput("Enter Password: ", "Password"); 
        if(checkDuplicate(username, facultyAdminID)){
            System.out.println("Duplicate or User information found!! Cannot create user, please try again.");
            Log.writeLog("Duplicate or User information found!!");
            continue;
        }
        FacultyAdmin newFacultyAdmin=new FacultyAdmin(facultyAdminID, username, password);
        User newUser=new User(facultyAdminID, username, password, "FacultyAdmin");
        
        facultyAdmins.add(newFacultyAdmin);
        users.add(newUser);
        
        FileHandling.saveAllFacultyAdmins(facultyAdmins, facultyAdminFile);
        FileHandling.saveAllUsers(users, userFile);
        
        synchronizeGlobalLists(); 

        System.out.println("\nFaculty Admin account for " + username + " successfully created and saved.");
        control=false;}
    }
    
    public void createSystemAdmin(){
        List<SystemAdmin> systemAdmins = FileHandling.loadSystemAdmin(systemAdminFile);
        boolean control=true;
        while(control){
        System.out.println("\nPlease enter new system admin details:");
        String systemAdminID = getNonBlankInput("Enter System Admin ID: ", "System Admin ID");
        String username = getNonBlankInput("Enter Username: ", "Username");
        String password = getNonBlankInput("Enter Password: ", "Password");
        if(checkDuplicate(username, systemAdminID)){
            System.out.println("Duplicate or User information found!! Cannot create user, please try again.");
            Log.writeLog("Duplicate or User information found!!");
            continue;
        }
        SystemAdmin newSystemAdmin=new SystemAdmin(systemAdminID, username, password);
        User newUser=new User(systemAdminID,username,password, "SystemAdmin");
        
        systemAdmins.add(newSystemAdmin);
        users.add(newUser);
        
        FileHandling.saveAllSystemAdmins(systemAdmins,systemAdminFile);
        FileHandling.saveAllUsers(users,userFile);
        
        synchronizeGlobalLists(); 

        System.out.println("\nSystem Admin account for " + username + " successfully created and saved.");control=false;}
    }
    

    public void editUser() {
        System.out.println("\n==================================================");
        System.out.println("              EDIT EXISTING USER                   ");
        System.out.println("==================================================");
        
        String usernameToEdit = getNonBlankInput("Enter the Username of the user to edit: ", "Username for Editing");

        User userToModify = null;
        Student studentToModify=null;
        Supervisor supervisorToModify=null;
        SystemAdmin systemAdminToModify=null;
        FacultyAdmin facultyAdminToModify=null;
        String tempRole=null;        
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(usernameToEdit)) {
                userToModify = user;
                tempRole=user.getRole();
                switch(tempRole){
                    case "Student":
                    for(Student student: students){
                        if(student.getUsername().equalsIgnoreCase(usernameToEdit)){
                            studentToModify=student;
                        }
                    }
                    break;
                    case"SystemAdmin":
                    for(SystemAdmin systemAdmin:systemAdmins){
                        if(systemAdmin.getUsername().equalsIgnoreCase(usernameToEdit)){
                            systemAdminToModify=systemAdmin;
                        }
                    }
                    break;
                    case"FacultyAdmin":
                    for(FacultyAdmin facultyAdmin:facultyAdmins){
                        if(facultyAdmin.getUsername().equalsIgnoreCase(usernameToEdit)){
                            facultyAdminToModify=facultyAdmin;
                        }
                    }
                    break;
                    case"Supervisor":
                    for(Supervisor supervisor:supervisors){
                        if(supervisor.getUsername().equalsIgnoreCase(usernameToEdit)){
                            supervisorToModify=supervisor;
                        }
                    }
                    break;
                }
                break;
            }
        }

        if (userToModify == null) {
            System.out.println("Error: User with that username was not found.");
            Log.writeLog("EDIT FAILED: User not found for username: " + usernameToEdit);
            return;
        }

        final String originalUserId = userToModify.getUserId(); 
        final String originalRole = userToModify.getRole(); 
        final String originalUsername = userToModify.getUsername();
        
        Log.writeLog("Starting edit for User ID: " + originalUserId + ", Username: " + originalUsername + ", Role: " + originalRole);

        boolean editing = true;
        while (editing) {
            System.out.println("\n--- Editing User: " + userToModify.getUsername() + " ---");
            System.out.println("Current ID:   " + userToModify.getUserId());
            System.out.println("Current Role: " + userToModify.getRole());
            System.out.println("--------------------------------------------------");
            System.out.println("Select field to edit:");
            System.out.println("1. Username");
            System.out.println("2. User ID");
            System.out.println("3. Role (Change User Type)");
            System.out.println("4. FINISH EDITING and Return");
            System.out.println("--------------------------------------------------");
            
            int choice;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); 
                continue;
            }
            
            String newValue;
            boolean modified = false;
            
            switch (choice) {
                case 1: 
                    newValue = getNonBlankInput("Enter NEW Username: ", "New Username");
                    users.remove(userToModify);
                    if(checkDuplicate(newValue,userToModify.getUserId())){
                        System.out.println("Username already exists!! Please choose a different one.");
                        Log.writeLog("Can't create user!!! Username already exists");
                        continue;
                    }
                    users.add(userToModify);
                    userToModify.setUsername(newValue);
                    System.out.println("Username successfully updated.");
                    Log.writeLog("FIELD CHANGE: Username changed from " + originalUsername + " to " + newValue);
                    modified = true;
                    switch(tempRole){
                        case "Student":
                            studentToModify.setUsername(newValue);
                            Log.writeLog("Student's username changed " + originalUsername + "-->" + newValue);
                            break;
                        case"SystemAdmin":
                            systemAdminToModify.setUsername(newValue);
                            Log.writeLog("SystemAdmin's username changed " + originalUsername + "-->" + newValue);
                            break;
                        case"FacultyAdmin":
                            facultyAdminToModify.setUsername(newValue);
                            Log.writeLog("FacultyAdmin's username changed " + originalUsername + "-->" + newValue);
                            break;
                        case "Supervisor":
                            supervisorToModify.setUsername(newValue);
                            Log.writeLog("Supervisor's username changed " + originalUsername + "-->" + newValue);
                            break;
                    }
                    break;

                case 2: 
                    newValue = getNonBlankInput("Enter NEW User ID: ", "New User ID");
                    users.remove(userToModify);
                     if(checkDuplicate(newValue,userToModify.getUserId())){
                        System.out.println("UserID already exists!! Please choose a different one.");
                        Log.writeLog("Can't create user!!! UserID already exists");
                        continue;
                    }
                    users.add(userToModify);
                    userToModify.setUserID(newValue);
                    switch(tempRole){
                        case "SystemAdmin":
                        systemAdminToModify.setUserID(newValue);
                        Log.writeLog("System Admin new userID changed from "+ originalUserId + "--> "+ newValue);
                        break;
                        case"Student":
                        studentToModify.setUserID(newValue);
                        Log.writeLog("Student new userID changed from "+ originalUserId + "--> "+ newValue);
                        break;
                        case"FacultyAdmin":
                        facultyAdminToModify.setUserID(newValue);
                        Log.writeLog("Faculty Admin new userID changed from "+ originalUserId + "--> "+ newValue);
                        break;
                        case"Supervisor":
                        supervisorToModify.setUserID(newValue);
                        Log.writeLog("Supervisor new userID changed from "+ originalUserId + "--> "+ newValue);
        
                    }
                    System.out.println("User ID successfully updated.");
                    Log.writeLog("FIELD CHANGE: User ID changed from " + originalUserId + " to " + newValue);
                    modified = true;
                    break;
                    
                case 3: 
                    newValue = getNonBlankInput("Enter NEW Role (Student/Supervisor/FacultyAdmin/SystemAdmin): ", "New Role");
                    String newRole = newValue.replaceAll("\\s+", "");
                    newRole = newRole.substring(0, 1).toUpperCase() + newRole.substring(1).toLowerCase();
                    
                    if (newRole.equals("Student") || newRole.equals("Supervisor") || newRole.equals("Facultyadmin") || newRole.equals("Systemadmin")) {
                            userToModify.setRole(newRole);
                            System.out.println("Role successfully updated to " + newRole + ".");
                            
                            System.out.println("\n*** NOTICE: Role change applied. Specific data for the OLD role will be DELETED from its file.");
                            System.out.println("You MUST manually create a new entry via the 'Create New User' menu if you wish to define details for the new role (e.g., student program, supervisor list). ***");
                            Log.writeLog("FIELD CHANGE: Role changed from " + originalRole + " to " + newRole);
                            modified = true;
                    } else {
                            System.out.println("Invalid role entered. Role not changed.");
                            Log.writeLog("FIELD CHANGE FAILED: Invalid role attempted: " + newValue);
                    }
                    break;
                    
                case 4:
                    editing = false;
                    System.out.println("\nEdit session finalized. Returning to User Account Management.");
                    Log.writeLog("EDIT SESSION END: Finalized changes for user " + userToModify.getUsername());
                    break;
                    
                default:
                    System.out.println("Invalid option. Please choose between 1 and 4.");
            }
            
            if (modified) {
                FileHandling.saveAllUsers(users, userFile); 
                switch(tempRole){
                    case"SystemAdmin":
                    Log.writeLog("File Update: systemAdmin.txt saved.");
                    FileHandling.saveAllSystemAdmins(systemAdmins,systemAdminFile);
                    synchronizeGlobalLists();
                    break;
                    case"Student":
                    Log.writeLog("File Update: student.txt saved.");
                    FileHandling.saveAllStudents(students, studentsFile);
                    synchronizeGlobalLists();
                    break;
                    case"FacultyAdmin":
                    Log.writeLog("File Update: facultyAdmin.txt saved.");
                    FileHandling.saveAllFacultyAdmins(facultyAdmins, facultyAdminFile);
                    synchronizeGlobalLists();
                    break;
                    case"Supervisor":
                    Log.writeLog("File Update: supervisor.txt saved.");
                    FileHandling.saveAllSupervisors(supervisors, supervisorsFile);
                    synchronizeGlobalLists();
                    break;
                }
                Log.writeLog("File Update: users.txt saved.");
                
                
                synchronizeGlobalLists();
                
                System.out.println("All user changes saved permanently to file and memory synchronized.");
            }
        }
    }



    public void deleteUser() {
        System.out.println("\n==================================================");
        System.out.println("              DELETE USER ACCOUNT                  ");
        System.out.println("==================================================");
        
        String usernameToDelete = getNonBlankInput("Enter the Username of the user to delete: ", "Username for Deletion");

        User userToRemove = null;
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(usernameToDelete)) {
                userToRemove = user;
                break;
            }
        }

        if (userToRemove != null) {
            Log.writeLog("DELETE: Attempting to delete user: " + userToRemove.getUsername() + ", ID: " + userToRemove.getUserId());
            System.out.printf("Are you sure you want to permanently delete user '%s' (%s)? (yes/no): ", 
            userToRemove.getUsername(), userToRemove.getRole());
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (confirmation.equals("yes")) { 
                
                String role = userToRemove.getRole();
                String userId = userToRemove.getUserId();

                users.remove(userToRemove);
                
                switch(role){
                    case "Student":
                        students.removeIf(s -> s.getUserId().equals(userId));
                        FileHandling.saveAllStudents(students, studentsFile);
                        Log.writeLog("DELETE SUCCESS: Removed from students file.");
                        break;
                    case "Supervisor":
                        supervisors.removeIf(s -> s.getUserId().equals(userId));
                        FileHandling.saveAllSupervisors(supervisors, supervisorsFile);
                        Log.writeLog("DELETE SUCCESS: Removed from supervisors file.");
                        break;
                    case "Facultyadmin":
                        facultyAdmins.removeIf(f -> f.getUserId().equals(userId));
                        FileHandling.saveAllFacultyAdmins(facultyAdmins, facultyAdminFile);
                        Log.writeLog("DELETE SUCCESS: Removed from facultyadmin file.");
                        break;
                    case "Systemadmin":
                        systemAdmins.removeIf(s -> s.getUserId().equals(userId));
                        FileHandling.saveAllSystemAdmins(systemAdmins, systemAdminFile);
                        Log.writeLog("DELETE SUCCESS: Removed from systemadmin file.");
                        break;
                    default:
                        System.err.println("Warning: Could not delete user from specific role file. Role not recognised: " + role);
                        Log.writeLog("DELETE WARNING: Role not recognised: " + role);
                }

                FileHandling.saveAllUsers(users, userFile); 
                Log.writeLog("DELETE SUCCESS: Removed from users.txt.");
                
                synchronizeGlobalLists(); 
                
                System.out.printf("User '%s' (%s) successfully deleted from the system and all files.%n", 
                userToRemove.getUsername(), userToRemove.getRole());
            } else {
                System.out.println("User deletion cancelled.");
                Log.writeLog("DELETE CANCELLED by user.");
            }
        } else {
            System.out.println("Error: User with that username was not found.");
            Log.writeLog("DELETE FAILED: User not found for username: " + usernameToDelete);
        }
    }
    
    public void monitorFailedLogins() {
        System.out.println("\n--- Monitor Failed Logins (TO BE IMPLEMENTED) ---");
        Log.writeLog("ACTION: Monitor Failed Logins selected (Not implemented).");
    }

    public void resetUserPassword() {
        System.out.println("\n==================================================");
        System.out.println("              RESET USER PASSWORD                  ");
        System.out.println("==================================================");
        
        String usernameToReset = getNonBlankInput("Enter the Username of the user to reset password for: ", "Username for Password Reset");
        String tempRole=null;
        Student studentToModify=null;
        SystemAdmin systemAdminToModify=null;
        FacultyAdmin facultyAdminToModify=null;
        Supervisor supervisorToModify=null;
        User userToModify = null;
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(usernameToReset)) {
                userToModify = user;
                tempRole=user.getRole();
                break;
            }
        }
        switch(tempRole){
            case "Student":
            for(Student student:students){
                if(student.getUsername().equals(userToModify.getUsername())){
                    studentToModify=student;
                    break;
                }
            }
            break;
            case "SystemAdmin":
                for(SystemAdmin systemAdmin:systemAdmins){
                if(systemAdmin.getUsername().equals(userToModify.getUsername())){
                    systemAdminToModify=systemAdmin;
                    break;
                }
                
            }
            break;
            case "FacultyAdmin":
                for(FacultyAdmin facultyAdmin:facultyAdmins){
                if(facultyAdmin.getUsername().equals(userToModify.getUsername())){
                    facultyAdminToModify=facultyAdmin;
                    break;
                }
                
            }
            break;
            case "Supervisor":
                for(Supervisor supervisor:supervisors){
                    if(supervisor.getUsername().equals(userToModify.getUsername())){
                        supervisorToModify=supervisor;
                        break;
                    }
                }
                break;
        }

        if (userToModify != null) {
            System.out.printf("Current user details: ID=%s, Role=%s%n", userToModify.getUserId(), userToModify.getRole());
            Log.writeLog("PASSWORD RESET: Found user " + usernameToReset);
            
            String newPassword = getNonBlankInput("Enter the NEW password: ", "New Password");

            userToModify.setPassword(newPassword);
            switch(tempRole){
                case "Student":
                studentToModify.setPassword(newPassword);
                FileHandling.saveAllStudents(students, studentsFile);
                Log.writeLog(studentToModify.getUsername()+"'s password has been updated.");
                break;
                case "SystemAdmin":
                systemAdminToModify.setPassword(newPassword);
                FileHandling.saveAllSystemAdmins(systemAdmins, systemAdminFile);
                Log.writeLog(systemAdminToModify.getUsername()+"'s password has been updated.");
                break;
                case "Supervisor":
                supervisorToModify.setPassword(newPassword);
                FileHandling.saveAllSupervisors(supervisors, supervisorsFile);
                Log.writeLog(supervisorToModify.getUsername()+"'s password has been updated.");
                break;
                case "FacultyAdmin":
                facultyAdminToModify.setPassword(newPassword);
                FileHandling.saveAllFacultyAdmins(facultyAdmins, facultyAdminFile);
                Log.writeLog(facultyAdminToModify.getUsername()+"'s password has been updated.");
                break;
            }

            FileHandling.saveAllUsers(users, userFile); 
            Log.writeLog("PASSWORD RESET SUCCESS: Password saved to users.txt for user " + usernameToReset);
            
            System.out.printf("Password for user '%s' successfully reset and saved permanently to file.%n", userToModify.getUsername());
        } else {
            System.out.println("Error: User with that username was not found.");
            Log.writeLog("PASSWORD RESET FAILED: User not found: " + usernameToReset);
        }
    }


public void wipeAllUsers() {
    System.out.println("\n==================================================");
    System.out.println("  WIPE ALL USER ROLE DATA ");
    System.out.println("==================================================");
    System.out.println("THIS WILL PERMANENTLY DELETE ALL STUDENTS, SUPERVISORS,");
    System.out.println("AND FACULTY ADMINS from files and memory.");
    
    System.out.print("Type 'WIPE' to confirm permanent deletion: ");
    String confirmation = scanner.nextLine().trim();

    if (!confirmation.equals("WIPE")) {
        System.out.println("Deletion cancelled. Data remains intact.");
        Log.writeLog("WIPE CANCELLED: Confirmation failed.");
        return;
    }
    
    this.students.clear();
    FileHandling.saveAllStudents(this.students, studentsFile);
    Log.writeLog("WIPE SUCCESS: Cleared and saved " + studentsFile + ".");


    this.supervisors.clear();
    FileHandling.saveAllSupervisors(this.supervisors, supervisorsFile);
    Log.writeLog("WIPE SUCCESS: Cleared and saved " + supervisorsFile + ".");


    this.facultyAdmins.clear();
    FileHandling.saveAllFacultyAdmins(this.facultyAdmins, facultyAdminFile);
    Log.writeLog("WIPE SUCCESS: Cleared and saved " + facultyAdminFile + ".");
    
    System.out.println("All non-admin role files have been successfully cleared.");

    this.systemAdmins.clear();
    String defaultID = "SA9999";
    String defaultUser = "ultimateAdmin";
    String defaultPass = "SystemAdminPass123";
    SystemAdmin defaultAdmin=new SystemAdmin(defaultID,defaultUser,defaultPass);
    systemAdmins.add(defaultAdmin);
    FileHandling.saveAllSystemAdmins(systemAdmins, systemAdminFile);
    Log.writeLog("All admins except ultimate admin are wiped");

    this.users.clear();
    User defaultSystemUser=new User(defaultID,defaultUser,defaultPass,"SystemAdmin");
    users.add(defaultSystemUser);
    FileHandling.saveAllUsers(users,userFile);
    Log.writeLog("All users has been wiped except ultimate admin");

    synchronizeGlobalLists();
    
    System.out.println("\n WIPE COMPLETE. Only System Admin accounts remain in the system.");
    }
    
    public boolean checkDuplicate(String username, String userID){
        for(User user:users){
            if(user.getUsername().equals(username) || user.getUserId().equals(userID)){
                return true;
            }
        }
        return false;
    }

    public static void failedLogin(String username, String password) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = "[" + timestamp + "] " + "User: "+username+" tried to login with password of: "+password;

        try (FileWriter writer = new FileWriter(FailedLogin, true)) {
            writer.write(logEntry + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewFailedLogin() {
    Log.writeLog("VIEW FAILED LOGIN: System Admin viewed failed login attempts. Admin: "+ loggedInUser.getUsername());
    System.out.println("\n==================================================");
    System.out.println("              SYSTEM FAIL LOGINS CONTENTS            ");
    System.out.println("==================================================");

    try (BufferedReader reader = new BufferedReader(new FileReader(FailedLogin))) {
        String line;
        
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        
    } catch (IOException e) {
        System.out.println("Error reading log file: " + e.getMessage());
    } finally {
        System.out.println("==================================================");
        System.out.println("            END OF LOG FILE                       ");
        System.out.println("==================================================");
    }
}
}