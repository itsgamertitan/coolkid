import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FacultyAdminPortalGUI extends JFrame {
    private void createReassignPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Assign or Reassign Students (GUI not yet implemented)"), BorderLayout.CENTER);
        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        panel.add(backBtn, BorderLayout.SOUTH);
        mainPanel.add(panel, "reassign");
    }

    private void createFilterProgramPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Enter program:");
        JTextField programField = new JTextField(15);
        JButton filterBtn = new JButton("Filter");
        topPanel.add(label);
        topPanel.add(programField);
        topPanel.add(filterBtn);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Student Username"}, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        filterBtn.addActionListener(_ -> {
            String program = programField.getText().trim();
            model.setRowCount(0);
            boolean found = false;
            for (Student s : logic.getStudents()) {
                if (s.getProgram().equalsIgnoreCase(program)) {
                    model.addRow(new Object[]{s.getUsername()});
                    found = true;
                }
            }
            if (!found) {
                JOptionPane.showMessageDialog(panel, "No students found for that program.");
            }
        });

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);
        mainPanel.add(panel, "filterProgram");
    }

    private void createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Select report type:");
        String[] options = {"Students per Program", "Students per Supervisor"};
        JComboBox<String> reportType = new JComboBox<>(options);
        JButton generateBtn = new JButton("Generate");
        topPanel.add(label);
        topPanel.add(reportType);
        topPanel.add(generateBtn);

        JTextArea reportArea = new JTextArea(15, 40);
        reportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportArea);

        generateBtn.addActionListener(_ -> {
            reportArea.setText("");
            String selected = (String) reportType.getSelectedItem();
            if (selected.equals("Students per Program")) {
                java.util.Map<String, java.util.List<String>> programMap = new java.util.HashMap<>();
                for (Student s : logic.getStudents()) {
                    programMap.computeIfAbsent(s.getProgram(), k -> new java.util.ArrayList<>()).add(s.getUsername());
                }
                for (String program : programMap.keySet()) {
                    reportArea.append("Program: " + program + "\n");
                    for (String student : programMap.get(program)) {
                        reportArea.append("  - " + student + "\n");
                    }
                    reportArea.append("\n");
                }
            } else if (selected.equals("Students per Supervisor")) {
                java.util.Map<String, java.util.List<String>> supervisorMap = new java.util.HashMap<>();
                for (Student s : logic.getStudents()) {
                    supervisorMap.computeIfAbsent(s.getSupervisor(), k -> new java.util.ArrayList<>()).add(s.getUsername());
                }
                for (String supervisor : supervisorMap.keySet()) {
                    reportArea.append("Supervisor: " + supervisor + "\n");
                    for (String student : supervisorMap.get(supervisor)) {
                        reportArea.append("  - " + student + "\n");
                    }
                    reportArea.append("\n");
                }
            }
        });

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);
        mainPanel.add(panel, "report");
    }
    private final FacultyAdmin currentFacultyAdmin;
    private final FacultyAdminLogic logic;
    private final JTable appointmentsTable;
    private final DefaultTableModel tableModel;
    private final JTable assignmentTable;
    private final DefaultTableModel assignmentTableModel;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    public FacultyAdminPortalGUI(FacultyAdmin facultyAdmin) {
        this.currentFacultyAdmin = facultyAdmin;
        this.logic = new FacultyAdminLogic();
        
    // Setup main window
    setTitle("Faculty Admin Portal - " + facultyAdmin.getUsername());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setMinimumSize(new Dimension(800, 600));
    setMaximumSize(new Dimension(800, 600));
    setPreferredSize(new Dimension(800, 600));
    setLocationRelativeTo(null);
        
        // Create main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize table model for appointments
        String[] columnNames = {"ID", "Date/Time", "Student", "Supervisor", "Status", "Feedback"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentsTable = new JTable(tableModel);
        
        // Assignment table
        String[] assignmentColumns = {"Student", "Supervisor"};
        assignmentTableModel = new DefaultTableModel(assignmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        assignmentTable = new JTable(assignmentTableModel);
        
        // Create panels
        createMainMenu();
        createViewAppointmentsPanel();
        createAssignmentPanel();
        createReassignPanel();
        createFilterProgramPanel();
        createReportPanel();
        
        // Add main panel to frame
        add(mainPanel);
        
        // Show initial panel
        cardLayout.show(mainPanel, "menu");
        setVisible(true);
    }

    private void createMainMenu() {
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        JLabel welcomeLabel = new JLabel("Welcome " + currentFacultyAdmin.getUsername() + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JButton viewAppointmentsBtn = new JButton("View All Appointments");
        JButton viewAssignmentsBtn = new JButton("View Supervisor-Student Assignments");
        JButton reassignBtn = new JButton("Assign/Reassign Students");
        JButton filterProgramBtn = new JButton("Filter by Program");
        JButton reportBtn = new JButton("Generate Reports");
        JButton logoutBtn = new JButton("Logout");

        viewAppointmentsBtn.addActionListener(_ -> {
            refreshAppointmentTable();
            cardLayout.show(mainPanel, "viewAppointments");
        });

        viewAssignmentsBtn.addActionListener(_ -> {
            refreshAssignmentTable();
            cardLayout.show(mainPanel, "viewAssignments");
        });

        reassignBtn.addActionListener(_ -> cardLayout.show(mainPanel, "reassign"));
        filterProgramBtn.addActionListener(_ -> cardLayout.show(mainPanel, "filterProgram"));
        reportBtn.addActionListener(_ -> cardLayout.show(mainPanel, "report"));

        logoutBtn.addActionListener(_ -> {
            Log.writeLog("Faculty Admin logged out: " + currentFacultyAdmin.getUserId());
            SwingUtilities.invokeLater(() -> {
                dispose();
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.pack();
                loginGUI.setLocationRelativeTo(null);
                loginGUI.setVisible(true);
            });
        });

        menuPanel.add(welcomeLabel, gbc);
        menuPanel.add(Box.createVerticalStrut(30), gbc);
        menuPanel.add(viewAppointmentsBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(10), gbc);
        menuPanel.add(viewAssignmentsBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(10), gbc);
        menuPanel.add(reassignBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(10), gbc);
        menuPanel.add(filterProgramBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(10), gbc);
        menuPanel.add(reportBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(50), gbc);
        menuPanel.add(logoutBtn, gbc);

        mainPanel.add(menuPanel, "menu");
        
        mainPanel.add(menuPanel, "menu");
    }

    private void createViewAppointmentsPanel() {
        JPanel viewPanel = new JPanel(new BorderLayout(10, 10));
        viewPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        appointmentsTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "All", "Pending", "Approved", "Rejected", "Cancelled"
        });
        filterPanel.add(new JLabel("Filter by Status: "));
        filterPanel.add(statusFilter);
        statusFilter.addActionListener(e -> refreshAppointmentTable(statusFilter.getSelectedItem().toString()));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backBtn = new JButton("Back to Menu");
        JButton exportBtn = new JButton("Export to CSV");
        JButton editDateBtn = new JButton("Edit Selected Date");
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        exportBtn.addActionListener(_ -> handleExport());
        editDateBtn.addActionListener(_ -> showEditDateDialog());
        buttonPanel.add(backBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(editDateBtn);
        viewPanel.add(filterPanel, BorderLayout.NORTH);
        viewPanel.add(scrollPane, BorderLayout.CENTER);
        viewPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(viewPanel, "viewAppointments");
    }

    private void createAssignmentPanel() {
        JPanel assignmentPanel = new JPanel(new BorderLayout(10, 10));
        assignmentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        assignmentTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        assignmentPanel.add(scrollPane, BorderLayout.CENTER);
        assignmentPanel.add(backBtn, BorderLayout.SOUTH);
        mainPanel.add(assignmentPanel, "viewAssignments");
    }

    private void refreshAppointmentTable() {
        refreshAppointmentTable("All");
    }

    private void refreshAppointmentTable(String statusFilter) {
        tableModel.setRowCount(0);
        List<Appointment> appointments = logic.getAppointmentsForFaculty(currentFacultyAdmin.getUsername());
        for (Appointment appointment : appointments) {
            if (statusFilter.equals("All") || appointment.getStatus().equals(statusFilter)) {
                tableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getDateTime(),
                    appointment.getStudentUsername(),
                    appointment.getSupervisorUsername(),
                    appointment.getStatus(),
                    appointment.getFeedback()
                });
            }
        }
    }

    private void refreshAssignmentTable() {
        assignmentTableModel.setRowCount(0);
        List<Student> students = logic.getStudents();
        for (Student student : students) {
            assignmentTableModel.addRow(new Object[]{
                student.getUsername(),
                student.getSupervisor()
            });
        }
    }

    private void handleExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new java.io.File("appointments_export.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try {
                logic.exportToCSV(filePath);
                JOptionPane.showMessageDialog(this,
                    "Export completed successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting file: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDateDialog() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String currentDateTime = (String) appointmentsTable.getValueAt(selectedRow, 1);
        // Assume format: dd/MM/yyyy HH:mm
        String[] dateTimeParts = currentDateTime.split(" ");
        String[] dateParts = dateTimeParts[0].split("/");
        String timePart = dateTimeParts.length > 1 ? dateTimeParts[1] : "";
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        JTextField dayField = new JTextField(dateParts.length > 0 ? dateParts[0] : "");
        JTextField monthField = new JTextField(dateParts.length > 1 ? dateParts[1] : "");
        JTextField yearField = new JTextField(dateParts.length > 2 ? dateParts[2] : "");
        JTextField timeField = new JTextField(timePart);
        panel.add(new JLabel("Day:"));
        panel.add(dayField);
        panel.add(new JLabel("Month:"));
        panel.add(monthField);
        panel.add(new JLabel("Year:"));
        panel.add(yearField);
        panel.add(new JLabel("Time (HH:mm):"));
        panel.add(timeField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Appointment Date/Time", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newDateTime = String.format("%s/%s/%s %s", dayField.getText(), monthField.getText(), yearField.getText(), timeField.getText());
            // Update in backend
            String appointmentId = (String) appointmentsTable.getValueAt(selectedRow, 0);
            updateAppointmentDateTime(appointmentId, newDateTime);
            refreshAppointmentTable();
        }
    }

    private void updateAppointmentDateTime(String appointmentId, String newDateTime) {
        List<Appointment> appointments = logic.getAllAppointments();
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                appointment.setDateTime(newDateTime);
                break;
            }
        }
        // Save changes to file
        try {
            FileHandling.saveAllAppointments(appointments, logic.getAppointmentsFile());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving appointment: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}