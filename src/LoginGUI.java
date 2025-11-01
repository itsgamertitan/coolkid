import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private static final String STUDENTS_FILE = "students.txt";
    private static final String USER_FILE = "user.txt";
    private static final String SUPERVISORS_FILE = "supervisors.txt";
        private static final String APPOINTMENTS_FILE = "appointments.txt";
    private static final String FACULTYADMIN_FILE = "facultyAdmin.txt";
    private static final String SYSTEMADMIN_FILE = "systemAdmin.txt";
    
    public LoginGUI() {
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeUI() {
        setTitle("Login System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        // Title label
        JLabel titleLabel = new JLabel("Student Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, gbc);
        mainPanel.add(Box.createVerticalStrut(30), gbc);
        
        // Username field
        JPanel usernamePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        usernamePanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        usernamePanel.add(usernameField);
        mainPanel.add(usernamePanel, gbc);
        
        // Password field
        JPanel passwordPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        passwordPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        passwordPanel.add(passwordField);
        mainPanel.add(passwordPanel, gbc);
        
        mainPanel.add(Box.createVerticalStrut(20), gbc);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginButton = new JButton("Login");
        exitButton = new JButton("Exit");
        buttonsPanel.add(loginButton);
        buttonsPanel.add(exitButton);
        mainPanel.add(buttonsPanel, gbc);
        
        add(mainPanel);
    }
    
    private void setupEventListeners() {
        loginButton.addActionListener(e -> handleLogin());
        exitButton.addActionListener(e -> System.exit(0));
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        System.out.println("[DEBUG] handleLogin called");
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            System.out.println("[DEBUG] Username or password empty");
            return;
        }

        System.out.println("[DEBUG] Attempting login with username: " + username);

        List<User> users = FileHandling.loadUsers(USER_FILE);
        if (users.isEmpty()) {
            System.out.println("[DEBUG] No users loaded from " + USER_FILE);
            JOptionPane.showMessageDialog(this, "Error loading user data", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Student> students = FileHandling.loadStudents(STUDENTS_FILE);
        List<Supervisor> supervisors = FileHandling.loadSupervisors(SUPERVISORS_FILE);
        List<FacultyAdmin> facultyAdmins = FileHandling.loadFacultyAdmin(FACULTYADMIN_FILE);
        List<SystemAdmin> systemAdmins = FileHandling.loadSystemAdmin(SYSTEMADMIN_FILE);
        boolean loginSuccess = false;

        for (User user : users) {
            System.out.println("[DEBUG] Checking user: " + user.getUsername() + ", Role: " + user.getRole());
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                System.out.println("[DEBUG] Login successful for user: " + username + " with role: " + user.getRole());
                loginSuccess = true;
                switch (user.getRole()) {
                    case "Student" -> {
                        Student student = students.stream().filter(s -> s.getUserId().equals(user.getUserId())).findFirst().orElse(null);
                        if (student != null) {
                            System.out.println("[DEBUG] Found matching student record, opening portal");
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    StudentPortalGUI portal = new StudentPortalGUI(student, new studentLogic(APPOINTMENTS_FILE));
                                    portal.pack();
                                    portal.setLocationRelativeTo(null);
                                    portal.setVisible(true);
                                    dispose();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(this, "Error opening Student Portal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } else {
                            System.out.println("[DEBUG] Student record not found for userId: " + user.getUserId());
                            JOptionPane.showMessageDialog(this, "Student record not found.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    case "Supervisor" -> {
                        System.out.println("[DEBUG] Loaded supervisors: " + supervisors);
                        Supervisor supervisor = supervisors.stream().filter(s -> s.getUserId().equals(user.getUserId())).findFirst().orElse(null);
                        if (supervisor != null) {
                            System.out.println("[DEBUG] Found matching supervisor record: " + supervisor);
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    SupervisorGUI portal = new SupervisorGUI(supervisor);
                                    portal.pack();
                                    portal.setLocationRelativeTo(null);
                                    portal.setVisible(true);
                                    dispose();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(this, "Error opening Supervisor Portal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } else {
                            System.out.println("[DEBUG] Supervisor record not found for userId: " + user.getUserId());
                            JOptionPane.showMessageDialog(this, "Supervisor record not found for userId: " + user.getUserId(), "Login Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    case "FacultyAdmin" -> {
                        FacultyAdmin facultyAdmin = facultyAdmins.stream().filter(f -> f.getUserId().equals(user.getUserId())).findFirst().orElse(null);
                        if (facultyAdmin != null) {
                            System.out.println("[DEBUG] Found matching faculty admin record, opening portal");
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    FacultyAdminPortalGUI portal = new FacultyAdminPortalGUI(facultyAdmin);
                                    portal.pack();
                                    portal.setLocationRelativeTo(null);
                                    portal.setVisible(true);
                                    dispose();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(this, "Error opening Faculty Admin Portal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } else {
                            System.out.println("[DEBUG] Faculty Admin record not found for userId: " + user.getUserId());
                            JOptionPane.showMessageDialog(this, "Faculty Admin record not found.", "Login Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    case "SystemAdmin" -> {
                        System.out.println("[DEBUG] Opening system admin portal");
                        SwingUtilities.invokeLater(() -> {
                            try {
                                SystemAdminPortalGUI portal = new SystemAdminPortalGUI(user, users, systemAdmins, students, facultyAdmins, supervisors);
                                portal.pack();
                                portal.setLocationRelativeTo(null);
                                portal.setVisible(true);
                                dispose();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(this, "Error opening System Admin Portal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                    default -> {
                        System.out.println("[DEBUG] Unknown role: " + user.getRole());
                        JOptionPane.showMessageDialog(this, "Unknown role: " + user.getRole(), "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            }
        }

        if (!loginSuccess) {
            System.out.println("[DEBUG] No matching user found for username: " + username + " and password: " + password);
            JOptionPane.showMessageDialog(this,
                "Invalid username or password",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            Log.writeLog("Failed login attempt for username: " + username);
            SystemAdminLogic.failedLogin(username, password);
            passwordField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
        });
    }
}