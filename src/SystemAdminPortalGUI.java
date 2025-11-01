import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SystemAdminPortalGUI extends JFrame {
    private final SystemAdminLogic logic;
    private final User currentAdmin;

    public SystemAdminPortalGUI(User user, List<User> users, List<SystemAdmin> systemAdmins, List<Student> students, List<FacultyAdmin> facultyAdmins, List<Supervisor> supervisors) {
        this.currentAdmin = user;
        this.logic = new SystemAdminLogic(users, new java.util.Scanner(System.in), "students.txt", "supervisors.txt", "facultyAdmin.txt", "systemAdmin.txt", "user.txt", user, systemAdmins, students, facultyAdmins, supervisors);
    setTitle("System Admin Portal - " + user.getUsername());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setMinimumSize(new Dimension(800, 600));
    setMaximumSize(new Dimension(800, 600));
    setPreferredSize(new Dimension(800, 600));
    setLocationRelativeTo(null);
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        JLabel titleLabel = new JLabel("System Admin Portal", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JButton viewUsersBtn = new JButton("View All Users and Roles");
        JButton manageAccountsBtn = new JButton("Manage User Accounts");
        JButton resetPasswordBtn = new JButton("Reset User Password");
        JButton wipeDataBtn = new JButton("Wipe All User Role Data");
        JButton logoutBtn = new JButton("Logout");
        viewUsersBtn.addActionListener(_ -> showUsersTable());
        manageAccountsBtn.addActionListener(e -> showManageUserDialog());
        resetPasswordBtn.addActionListener(e -> showResetPasswordDialog());
        wipeDataBtn.addActionListener(e -> logic.wipeAllUsers());
        logoutBtn.addActionListener(e -> {
            Log.writeLog("System Admin logged out: " + currentAdmin.getUserId());
            SwingUtilities.invokeLater(() -> {
                dispose();
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.pack();
                loginGUI.setLocationRelativeTo(null);
                loginGUI.setVisible(true);
            });
        });
        menuPanel.add(titleLabel, gbc);
        menuPanel.add(Box.createVerticalStrut(30), gbc);
        menuPanel.add(viewUsersBtn, gbc);
        menuPanel.add(manageAccountsBtn, gbc);
        menuPanel.add(resetPasswordBtn, gbc);
        menuPanel.add(wipeDataBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(50), gbc);
        menuPanel.add(logoutBtn, gbc);
        add(menuPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // --- Helper Methods for GUI Actions ---
    private void showUsersTable() {
        logic.synchronizeGlobalLists(); // Always reload from files
        List<User> userList = logic.getUsers();
        String[] columns = {"USER ID", "USERNAME", "ROLE"};
        Object[][] data = new Object[userList.size()][3];
        for (int i = 0; i < userList.size(); i++) {
            User u = userList.get(i);
            data[i][0] = u.getUserId();
            data[i][1] = u.getUsername();
            data[i][2] = u.getRole();
        }
        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        JOptionPane.showMessageDialog(this, scrollPane, "All Users and Roles", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showManageUserDialog() {
        String[] options = {"Create New User", "Edit Existing User", "Delete User", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Select an action:", "Manage User Accounts",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        switch (choice) {
            case 0 -> showCreateUserDialog();
            case 1 -> showEditUserDialog();
            case 2 -> showDeleteUserDialog();
            default -> {}
        }
    }

    private void showCreateUserDialog() {
        String[] roles = {"Student", "Supervisor", "FacultyAdmin", "SystemAdmin"};
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Role:"));
        JComboBox<String> roleBox = new JComboBox<>(roles);
        panel.add(roleBox);
        panel.add(new JLabel("User ID:"));
        JTextField idField = new JTextField();
        panel.add(idField);
        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        JTextField passwordField = new JTextField();
        panel.add(passwordField);

        // Supervisor dropdown for Student creation
        JComboBox<String> supervisorBox = new JComboBox<>();
        for (Supervisor s : logic.getSupervisors()) supervisorBox.addItem(s.getUsername());
        JTextField programField = new JTextField();

        roleBox.addActionListener(e -> {
            if (roleBox.getSelectedItem().equals("Student")) {
                if (panel.getComponentCount() < 12) {
                    panel.add(new JLabel("Program:"));
                    panel.add(programField);
                    panel.add(new JLabel("Supervisor:"));
                    panel.add(supervisorBox);
                    panel.revalidate();
                    panel.repaint();
                }
            } else {
                while (panel.getComponentCount() > 10) {
                    panel.remove(panel.getComponentCount()-1);
                }
                panel.revalidate();
                panel.repaint();
            }
        });
        if (roleBox.getSelectedItem().equals("Student")) {
            panel.add(new JLabel("Program:"));
            panel.add(programField);
            panel.add(new JLabel("Supervisor:"));
            panel.add(supervisorBox);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String role = (String) roleBox.getSelectedItem();
            String userId = idField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (userId.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (logic.checkDuplicate(username, userId)) {
                JOptionPane.showMessageDialog(this, "Duplicate username or user ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Always write exactly 4 fields to user.txt
            try (java.io.FileWriter userWriter = new java.io.FileWriter("user.txt", true)) {
                userWriter.write(userId + "|" + username + "|" + password + "|" + role + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error writing to user.txt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            logic.getUsers().add(new User(userId, username, password, role));
            if (role.equals("Student")) {
                String supervisor = (String) supervisorBox.getSelectedItem();
                String program = programField.getText().trim();
                Student newStudent = new Student(userId, username, password, program, supervisor);
                FileHandling.saveToStudent(newStudent, "students.txt");
                // Update supervisor's assignedStudent list
                List<Supervisor> supervisors = FileHandling.loadSupervisors("supervisors.txt");
                for (Supervisor sup : supervisors) {
                    if (sup.getUsername().equals(supervisor)) {
                        sup.setStudent(username); // or userId if you want to use ID
                        break;
                    }
                }
                FileHandling.saveAllSupervisors(supervisors, "supervisors.txt");
            } else if (role.equals("Supervisor")) {
                Supervisor newSupervisor = new Supervisor(userId, username, password, new java.util.ArrayList<>());
                FileHandling.saveToSupervisor(newSupervisor, "supervisors.txt");
            } else if (role.equals("FacultyAdmin")) {
                FacultyAdmin newAdmin = new FacultyAdmin(userId, username, password);
                FileHandling.saveToFacultyAdmin(newAdmin, "facultyAdmin.txt");
            } else if (role.equals("SystemAdmin")) {
                SystemAdmin newSysAdmin = new SystemAdmin(userId, username, password);
                FileHandling.saveToSystemAdmin(newSysAdmin, "systemAdmin.txt");
            }
            JOptionPane.showMessageDialog(this, "User created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showEditUserDialog() {
        logic.synchronizeGlobalLists(); // Always reload from files
        String username = JOptionPane.showInputDialog(this, "Enter the username of the user to edit:");
        if (username == null || username.trim().isEmpty()) return;
        User userToEdit = logic.getUsers().stream().filter(u -> u.getUsername().equalsIgnoreCase(username.trim())).findFirst().orElse(null);
        if (userToEdit == null) {
            JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("New Username:"));
        JTextField usernameField = new JTextField(userToEdit.getUsername());
        panel.add(usernameField);
        panel.add(new JLabel("New User ID:"));
        JTextField idField = new JTextField(userToEdit.getUserId());
        panel.add(idField);
        panel.add(new JLabel("New Role:"));
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Supervisor", "FacultyAdmin", "SystemAdmin"});
        roleBox.setSelectedItem(userToEdit.getRole());
        panel.add(roleBox);
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newUsername = usernameField.getText().trim();
            String newUserId = idField.getText().trim();
            String newRole = (String) roleBox.getSelectedItem();
            String oldRole = userToEdit.getRole();
            if (newUsername.isEmpty() || newUserId.isEmpty() || newRole.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            userToEdit.setUsername(newUsername);
            userToEdit.setUserID(newUserId);
            userToEdit.setRole(newRole);
            FileHandling.saveAllUsers(logic.getUsers(), "user.txt");
            logic.synchronizeGlobalLists();
            // If role changed, remove from old role file and add to new role file
            if (!oldRole.equals(newRole)) {
                String originalUserId = userToEdit.getUserId();
                // Remove student from old supervisor if reassigned
                if (oldRole.equals("Student")) {
                    Student oldStudent = logic.getStudents().stream().filter(s -> s.getUserId().equals(originalUserId)).findFirst().orElse(null);
                    if (oldStudent != null) {
                        String oldSupervisorUsername = oldStudent.getSupervisor();
                        List<Supervisor> supervisors = FileHandling.loadSupervisors("supervisors.txt");
                        for (Supervisor sup : supervisors) {
                            if (sup.getUsername().equals(oldSupervisorUsername)) {
                                sup.removeStudent(oldStudent.getUsername());
                                break;
                            }
                        }
                        FileHandling.saveAllSupervisors(supervisors, "supervisors.txt");
                    }
                }
                switch (oldRole) {
                    case "Student" -> {
                        logic.getStudents().removeIf(s -> s.getUserId().equals(originalUserId));
                        FileHandling.saveAllStudents(logic.getStudents(), "students.txt");
                    }
                    case "Supervisor" -> {
                        logic.getSupervisors().removeIf(s -> s.getUserId().equals(originalUserId));
                        FileHandling.saveAllSupervisors(logic.getSupervisors(), "supervisors.txt");
                    }
                    case "FacultyAdmin" -> {
                        logic.getFacultyAdmins().removeIf(f -> f.getUserId().equals(originalUserId));
                        FileHandling.saveAllFacultyAdmins(logic.getFacultyAdmins(), "facultyAdmin.txt");
                    }
                    case "SystemAdmin" -> {
                        logic.getSystemAdmins().removeIf(s -> s.getUserId().equals(originalUserId));
                        FileHandling.saveAllSystemAdmins(logic.getSystemAdmins(), "systemAdmin.txt");
                    }
                }
                // Add to new role file
                switch (newRole) {
                    case "Student" -> {
                        // Default program and supervisor
                        Student newStudent = new Student(newUserId, newUsername, userToEdit.getPassword(), "", "");
                        logic.getStudents().add(newStudent);
                        FileHandling.saveAllStudents(logic.getStudents(), "students.txt");
                    }
                    case "Supervisor" -> {
                        Supervisor newSupervisor = new Supervisor(newUserId, newUsername, userToEdit.getPassword(), new java.util.ArrayList<>());
                        logic.getSupervisors().add(newSupervisor);
                        FileHandling.saveAllSupervisors(logic.getSupervisors(), "supervisors.txt");
                    }
                    case "FacultyAdmin" -> {
                        FacultyAdmin newAdmin = new FacultyAdmin(newUserId, newUsername, userToEdit.getPassword());
                        logic.getFacultyAdmins().add(newAdmin);
                        FileHandling.saveAllFacultyAdmins(logic.getFacultyAdmins(), "facultyAdmin.txt");
                    }
                    case "SystemAdmin" -> {
                        SystemAdmin newSysAdmin = new SystemAdmin(newUserId, newUsername, userToEdit.getPassword());
                        logic.getSystemAdmins().add(newSysAdmin);
                        FileHandling.saveAllSystemAdmins(logic.getSystemAdmins(), "systemAdmin.txt");
                    }
                }
            } else {
                // If role did not change, just update username in role file
                switch (newRole) {
                    case "Student" -> {
                        logic.getStudents().stream().filter(s -> s.getUserId().equals(newUserId)).findFirst().ifPresent(s -> s.setUsername(newUsername));
                        FileHandling.saveAllStudents(logic.getStudents(), "students.txt");
                    }
                    case "Supervisor" -> {
                        logic.getSupervisors().stream().filter(s -> s.getUserId().equals(newUserId)).findFirst().ifPresent(s -> s.setUsername(newUsername));
                        FileHandling.saveAllSupervisors(logic.getSupervisors(), "supervisors.txt");
                    }
                    case "FacultyAdmin" -> {
                        logic.getFacultyAdmins().stream().filter(f -> f.getUserId().equals(newUserId)).findFirst().ifPresent(f -> f.setUsername(newUsername));
                        FileHandling.saveAllFacultyAdmins(logic.getFacultyAdmins(), "facultyAdmin.txt");
                    }
                    case "SystemAdmin" -> {
                        logic.getSystemAdmins().stream().filter(s -> s.getUserId().equals(newUserId)).findFirst().ifPresent(s -> s.setUsername(newUsername));
                        FileHandling.saveAllSystemAdmins(logic.getSystemAdmins(), "systemAdmin.txt");
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "User updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDeleteUserDialog() {
        String username = JOptionPane.showInputDialog(this, "Enter the username of the user to delete:");
        if (username == null || username.trim().isEmpty()) return;
        User userToDelete = logic.getUsers().stream().filter(u -> u.getUsername().equalsIgnoreCase(username.trim())).findFirst().orElse(null);
        if (userToDelete == null) {
            JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user '" + userToDelete.getUsername() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            logic.getUsers().remove(userToDelete);
            // Remove from role file as well
            switch (userToDelete.getRole()) {
                case "Student" -> {
                    logic.getStudents().removeIf(s -> s.getUserId().equals(userToDelete.getUserId()));
                    FileHandling.saveAllStudents(logic.getStudents(), "students.txt");
                }
                case "Supervisor" -> {
                    logic.getSupervisors().removeIf(s -> s.getUserId().equals(userToDelete.getUserId()));
                    FileHandling.saveAllSupervisors(logic.getSupervisors(), "supervisors.txt");
                }
                case "FacultyAdmin" -> {
                    logic.getFacultyAdmins().removeIf(f -> f.getUserId().equals(userToDelete.getUserId()));
                    FileHandling.saveAllFacultyAdmins(logic.getFacultyAdmins(), "facultyAdmin.txt");
                }
                case "SystemAdmin" -> {
                    logic.getSystemAdmins().removeIf(s -> s.getUserId().equals(userToDelete.getUserId()));
                    FileHandling.saveAllSystemAdmins(logic.getSystemAdmins(), "systemAdmin.txt");
                }
            }
            FileHandling.saveAllUsers(logic.getUsers(), "user.txt");
            JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showResetPasswordDialog() {
        String username = JOptionPane.showInputDialog(this, "Enter the username of the user to reset password for:");
        if (username == null || username.trim().isEmpty()) return;
        User userToReset = logic.getUsers().stream().filter(u -> u.getUsername().equalsIgnoreCase(username.trim())).findFirst().orElse(null);
        if (userToReset == null) {
            JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String newPassword = JOptionPane.showInputDialog(this, "Enter the new password:");
        if (newPassword == null || newPassword.trim().isEmpty()) return;
        userToReset.setPassword(newPassword.trim());
        logic.synchronizeGlobalLists();
        // Also update role file
        switch (userToReset.getRole()) {
            case "Student" -> {
                logic.getStudents().stream().filter(s -> s.getUserId().equals(userToReset.getUserId())).findFirst().ifPresent(s -> s.setPassword(newPassword.trim()));
                FileHandling.saveAllStudents(logic.getStudents(), "students.txt");
            }
            case "Supervisor" -> {
                logic.getSupervisors().stream().filter(s -> s.getUserId().equals(userToReset.getUserId())).findFirst().ifPresent(s -> s.setPassword(newPassword.trim()));
                FileHandling.saveAllSupervisors(logic.getSupervisors(), "supervisors.txt");
            }
            case "FacultyAdmin" -> {
                logic.getFacultyAdmins().stream().filter(f -> f.getUserId().equals(userToReset.getUserId())).findFirst().ifPresent(f -> f.setPassword(newPassword.trim()));
                FileHandling.saveAllFacultyAdmins(logic.getFacultyAdmins(), "facultyAdmin.txt");
            }
            case "SystemAdmin" -> {
                logic.getSystemAdmins().stream().filter(s -> s.getUserId().equals(userToReset.getUserId())).findFirst().ifPresent(s -> s.setPassword(newPassword.trim()));
                FileHandling.saveAllSystemAdmins(logic.getSystemAdmins(), "systemAdmin.txt");
            }
        }
        FileHandling.saveAllUsers(logic.getUsers(), "user.txt");
        JOptionPane.showMessageDialog(this, "Password reset successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
