import java.util.List;
import java.util.Scanner;

public class App {

    private static final String STUDENTS_FILE = "students.txt";
    private static final String USER_FILE="user.txt";
    private static List<User> users=FileHandling.loadUsers(USER_FILE);
    private static final String SUPERVISORS_FILE = "supervisors.txt";
    private static final Scanner scanner=new Scanner(System.in);
    private static final String FACULTYADMIN_FILE="facultyAdmin.txt";
    private static final String SYSTEMADMIN_FILE="systemAdmin.txt";
    private static List<SystemAdmin> systemAdmins=FileHandling.loadSystemAdmin(SYSTEMADMIN_FILE);
    private static List<FacultyAdmin> facultyAdmins=FileHandling.loadFacultyAdmin(FACULTYADMIN_FILE);
    private static List<Student> students=FileHandling.loadStudents(STUDENTS_FILE);
    private static List<Supervisor> supervisors=FileHandling.loadSupervisors(SUPERVISORS_FILE);
    private static final String APPOINTMENTS_FILE="appointments.txt";
    public static void main(String[] args) { 
        Log.writeLog("Application started.");
        initializeSystemAdmins();
        while(true){
            System.out.println("===============================================");
            System.out.println("         WELCOME TO SUPERVISION HUB            ");
            System.out.println("===============================================");
            System.out.println("   A Smart Supervision Management System       ");
            System.out.println("Get Started by choosing one of the three");
            System.out.println("1. Login");
            System.out.println("2. Create New User(Only applicable for system admin)");
            System.out.println("3. Log Out");
            System.out.println("===============================================");

            int choice;
            while(true){
                System.out.print("Select between 1-3: ");
                if(scanner.hasNextInt()){
                    choice = scanner.nextInt();
                    scanner.nextLine();
                    break;
                }
                else{
                    System.out.println("Invalid input, please enter a number: ");
                    Log.writeLog("Invalid non-numeric input on main menu.");
                    scanner.nextLine();
                }
            }
            switch(choice){
                case 1:
                    System.out.println("Login selected. Redirecting to login screen...");
                    login();
                    break;
                case 2:
                    System.out.println("Create New User selected. Redirecting to system admin...");
                    createUser();
                    break;
                case 3:
                    System.out.println("Logging out...");
                    Log.writeLog("User selected Log Out. Application shutting down.");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid options, please choose again.");
                    Log.writeLog("Invalid menu choice selected: " + choice);
            }
        }
    }
    
    public static void createUser(){
        while(true){
            System.out.println("===============================================");
            System.out.println("            ADMIN ACCESS REQUIRED              ");
            System.out.println("===============================================");
            System.out.println("      Welcome to the User Creation Portal      ");
            System.out.println("===============================================");
            System.out.println(" Only authorized system administrators may     ");
            System.out.println(" create new user accounts. Please log in.      ");
            System.out.println("===============================================");

            System.out.print("Enter Admin Username: ");
            String adminUsername = scanner.nextLine(); 
            System.out.print("Enter Password: ");
            String password = scanner.nextLine(); 
            Log.writeLog("Admin trying to login, registering data...");
            User loggedInUser=null;
            if(VerifyAdmin(adminUsername,password)){
                for(User user:users){
                    if(user.getUsername().equals(adminUsername) && user.getPassword().equals(password) && user.getRole().equals("SystemAdmin")){
                        loggedInUser=user;
                    }
                }
                System.out.println("Welcome back " + adminUsername);
                Log.writeLog("Admin log in successful for: " + adminUsername);
                systemAdminPortal(loggedInUser);
                Log.writeLog("Redirected to System Admin Portal.");
                break;
            }
            else{
                System.out.println("Login Failed. Please try again.");
                Log.writeLog("Admin log in failed for username: " + adminUsername);
                SystemAdminLogic.failedLogin(adminUsername, password);
            }
        }
    }
    
    public static void login(){
        Log.writeLog("User trying to login");
        
        System.out.println("===============================================");
        System.out.println("              USER LOGIN SCREEN                ");
        System.out.println("===============================================");
        
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        User loggedInUser = verifyUser(username, password);
        
        if (loggedInUser != null) {
            System.out.println("\nLogin Successful! Welcome, " + loggedInUser.getUsername() + ".");
            Log.writeLog("Login successful for user: " + loggedInUser.getUsername());
            redirectToPortal(loggedInUser); 
        } else {
            System.out.println("\nLogin Failed. Invalid username or password.");
            Log.writeLog("Login failed for username: " + username);
        }
}
    
    
    
    
    public static boolean VerifyAdmin(String adminUsername, String Password){
        for(User user: users){
            if(user.getUsername().equals(adminUsername) && user.getPassword().equals(Password) && user.getRole().equals("SystemAdmin")){
                Log.writeLog("Admin verification successful for username: " + adminUsername);
                return true;
            }
        }
        Log.writeLog("Admin verification failed for username: " + adminUsername);
        return false;
    }

    public static void redirectToPortal(User loggedInUser){
        String currentRole=loggedInUser.getRole();
        Log.writeLog("Attempting to redirect user " + loggedInUser.getUsername() + " to portal for role: " + currentRole);
        switch(currentRole){
            case "Student":
            studentPortall(loggedInUser);
            Log.writeLog("Redirected to Student Portal.");
            break;
            case "Supervisor":
            supervisorPortal();
            Log.writeLog("Redirected to Supervisor Portal.");
            break;
            case "SystemAdmin":
            systemAdminPortal(loggedInUser);
            Log.writeLog("Redirected to System Admin Portal.");
            break;
            case "FacultyAdmin":
            facultyAdminPortal();
            Log.writeLog("Redirected to Faculty Admin Portal.");
            break;
            default:
            System.out.println("Role does not exist!");
            Log.writeLog("Failed to redirect: Unknown role found for user: " + loggedInUser.getUsername());
        }
    }

    public static void supervisorPortal(){
        System.out.println("Insert here");
    }
    public static void facultyAdminPortal(){
        System.out.println("Insert here");
    }
    public static void systemAdminPortal(User loggedInUser){
        SystemAdminLogic adminLogic=new SystemAdminLogic(users,scanner,STUDENTS_FILE,SUPERVISORS_FILE,FACULTYADMIN_FILE,SYSTEMADMIN_FILE,USER_FILE,loggedInUser,systemAdmins,students,facultyAdmins,supervisors);
        adminLogic.runAdminPortal();
    }
    public static User verifyUser(String username, String password){
        for(User user:users){
            if(user.getUsername().equals(username) && user.getPassword().equals(password)){
                System.out.println("User found, login in...");
                Log.writeLog("Login successful for user: "+ username);
                return user;
            }
        }
        Log.writeLog("Login failed, user information don't match for username: "+ username + " with password: " + password);
        SystemAdminLogic.failedLogin(username,password);
        return null;
    }
    private static void initializeSystemAdmins() {
    List<SystemAdmin> systemAdmins = FileHandling.loadSystemAdmin(SYSTEMADMIN_FILE);

        if (systemAdmins.isEmpty()) {
            System.out.println("\n*** CRITICAL SYSTEM NOTICE ***");
            System.out.println("No System Administrators found. Creating default Ultimate Admin.");
            
            String defaultID = "SA9999";
            String defaultUser = "ultimateAdmin";
            String defaultPass = "SystemAdminPass123";

            SystemAdmin defaultSA = new SystemAdmin(defaultID, defaultUser, defaultPass);
            User defaultUserAccount = new User(defaultID, defaultUser, defaultPass, "SystemAdmin");
            
            FileHandling.saveToSystemAdmin(defaultSA, SYSTEMADMIN_FILE);
            FileHandling.saveToUser(defaultUserAccount, USER_FILE);
            
        
            users.add(defaultUserAccount); 
            
            System.out.println("Default Ultimate Admin created: Username: " + defaultUser + ", ID: " + defaultID);
            System.out.println("PLEASE CHANGE PASSWORD IMMEDIATELY AFTER FIRST LOGIN!");
            Log.writeLog("CRITICAL: Auto-created default Ultimate System Admin account.");
    }
}
    public static void studentPortall(User loggedInUser){
        Log.writeLog("Student portal access initiated for user: " + loggedInUser.getUsername());
        Student tempStudent=null;
        for(Student student:students){
            if(student.getUserId().equals(loggedInUser.getUserId())){
                tempStudent=student;
                break;
            }
        }
        studentLogic studentGate=new studentLogic(APPOINTMENTS_FILE); 
        studentGate.studentPortal(tempStudent);
    }
}
