import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FacultyAdminGUI extends JFrame {
    private final FacultyAdminLogic logic;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private DefaultTableModel assignmentTableModel;
    private DefaultTableModel studentTableModel;
    private JTable assignmentTable;
    private JTable studentTable;
    private JComboBox<String> programFilterCombo;
    
    public FacultyAdminGUI(String studentsFile, String supervisorsFile, String appointmentsFile,
                          List<Student> students, List<Supervisor> supervisors) {
        this.logic = new FacultyAdminLogic(studentsFile, supervisorsFile, appointmentsFile, students, supervisors);
        
        // Setup main window
        setTitle("Faculty Admin Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create card layout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create menu panel
        createMenuPanel();
        
        // Create assignments panel
        createAssignmentsPanel();
        
        // Create reassignment panel
        createReassignmentPanel();
        
        // Create reports panel
        createReportsPanel();

        add(mainPanel);
        pack();
        setVisible(true);
    }

    private void createMenuPanel() {
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel titleLabel = new JLabel("Faculty Admin Portal", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JButton viewAssignmentsBtn = new JButton("View Supervisor-Student Assignments");
        JButton reassignBtn = new JButton("Reassign Students");
        JButton reportsBtn = new JButton("Generate Reports");
        JButton logoutBtn = new JButton("Logout");

        // Style buttons
        Dimension buttonSize = new Dimension(250, 40);
        viewAssignmentsBtn.setPreferredSize(buttonSize);
        reassignBtn.setPreferredSize(buttonSize);
        reportsBtn.setPreferredSize(buttonSize);
        logoutBtn.setPreferredSize(buttonSize);

        // Add action listeners
        viewAssignmentsBtn.addActionListener(_ -> {
            refreshAssignmentTable();
            cardLayout.show(mainPanel, "assignments");
        });
        reassignBtn.addActionListener(_ -> cardLayout.show(mainPanel, "reassign"));
        reportsBtn.addActionListener(_ -> cardLayout.show(mainPanel, "reports"));
        logoutBtn.addActionListener(_ -> {
            dispose();
            Log.writeLog("Faculty Admin logged out");
        });

        menuPanel.add(titleLabel, gbc);
        menuPanel.add(Box.createVerticalStrut(30), gbc);
        menuPanel.add(viewAssignmentsBtn, gbc);
        menuPanel.add(reassignBtn, gbc);
        menuPanel.add(reportsBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(50), gbc);
        menuPanel.add(logoutBtn, gbc);

        mainPanel.add(menuPanel, "menu");
    }

    private void createAssignmentsPanel() {
        JPanel assignmentsPanel = new JPanel(new BorderLayout(10, 10));
        assignmentsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model
        String[] columnNames = {"Student", "Supervisor"};
        assignmentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        assignmentTable = new JTable(assignmentTableModel);
        assignmentTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(assignmentTable);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backBtn = new JButton("Back to Menu");
        JButton refreshBtn = new JButton("Refresh");

        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        refreshBtn.addActionListener(_ -> refreshAssignmentTable());

        buttonsPanel.add(backBtn);
        buttonsPanel.add(refreshBtn);

        // Add components to panel
        assignmentsPanel.add(scrollPane, BorderLayout.CENTER);
        assignmentsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        mainPanel.add(assignmentsPanel, "assignments");
    }

    private void createReassignmentPanel() {
        JPanel reassignPanel = new JPanel(new GridBagLayout());
        reassignPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel titleLabel = new JLabel("Reassign Students", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel studentLabel = new JLabel("Student:");
        JTextField studentField = new JTextField(20);
        JLabel supervisorLabel = new JLabel("New Supervisor:");
        JTextField supervisorField = new JTextField(20);

        JButton submitBtn = new JButton("Submit Reassignment");
        JButton backBtn = new JButton("Back to Menu");

        submitBtn.addActionListener(_ -> {
            String studentName = studentField.getText().trim();
            String supervisorName = supervisorField.getText().trim();
            
            if (studentName.isEmpty() || supervisorName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please fill in both student and supervisor fields",
                    "Missing Information",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            logic.Reassign();
            JOptionPane.showMessageDialog(this,
                "Student reassigned successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            studentField.setText("");
            supervisorField.setText("");
        });

        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));

        reassignPanel.add(titleLabel, gbc);
        reassignPanel.add(Box.createVerticalStrut(20), gbc);
        reassignPanel.add(studentLabel, gbc);
        reassignPanel.add(studentField, gbc);
        reassignPanel.add(Box.createVerticalStrut(10), gbc);
        reassignPanel.add(supervisorLabel, gbc);
        reassignPanel.add(supervisorField, gbc);
        reassignPanel.add(Box.createVerticalStrut(20), gbc);
        reassignPanel.add(submitBtn, gbc);
        reassignPanel.add(Box.createVerticalStrut(10), gbc);
        reassignPanel.add(backBtn, gbc);

        mainPanel.add(reassignPanel, "reassign");
    }

    private void createReportsPanel() {
        JPanel reportsPanel = new JPanel(new BorderLayout(10, 10));
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // North panel for filter controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    programFilterCombo = new JComboBox<>(new String[]{"All Programs", "IT-SE", "CS", "DS"});
        JButton filterBtn = new JButton("Filter");

        filterPanel.add(new JLabel("Program:"));
        filterPanel.add(programFilterCombo);
        filterPanel.add(filterBtn);

        // Center panel for student table
        String[] columnNames = {"Student ID", "Program", "Supervisor"};
        studentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(studentTableModel);
        studentTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(studentTable);

        // South panel for buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backBtn = new JButton("Back to Menu");
        JButton exportBtn = new JButton("Export Report");

        filterBtn.addActionListener(_ -> {
            String selectedProgram = (String) programFilterCombo.getSelectedItem();
            if (selectedProgram != null && !selectedProgram.equals("All Programs")) {
                logic.filterprogram();
                refreshStudentTable(selectedProgram);
            } else {
                refreshStudentTable(null);
            }
        });

        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        exportBtn.addActionListener(_ -> logic.viewReport());

        buttonsPanel.add(backBtn);
        buttonsPanel.add(exportBtn);

        reportsPanel.add(filterPanel, BorderLayout.NORTH);
        reportsPanel.add(scrollPane, BorderLayout.CENTER);
        reportsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        mainPanel.add(reportsPanel, "reports");
    }

    private void refreshAssignmentTable() {
        assignmentTableModel.setRowCount(0);
        logic.viewSupervisorXStudents();
    }

    private void refreshStudentTable(String programFilter) {
        studentTableModel.setRowCount(0);
        // Logic to load and filter students will be implemented here
    }
}