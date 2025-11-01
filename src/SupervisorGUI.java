import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupervisorGUI extends JFrame {
    private final SupervisorLogic logic;
    private final Supervisor currentSupervisor;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel appointmentTableModel;
    private JTable studentTable;
    private JTable appointmentTable;
    
    public SupervisorGUI(Supervisor supervisor) {
        this.currentSupervisor = supervisor;
        this.logic = new SupervisorLogic();
        
    // Setup main window
    setTitle("Supervisor Portal - " + supervisor.getUsername());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setMinimumSize(new Dimension(800, 600));
    setMaximumSize(new Dimension(800, 600));
    setPreferredSize(new Dimension(800, 600));
    setLocationRelativeTo(null);

        // Create card layout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create menu panel
        createMenuPanel();
        
        // Create students panel
        createStudentsPanel();
        
        // Create appointments panel
        createAppointmentsPanel();
        
        // Create feedback panel
        createFeedbackPanel();

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

        JLabel welcomeLabel = new JLabel("Welcome " + currentSupervisor.getUsername() + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JButton viewStudentsBtn = new JButton("View Assigned Students");
        JButton appointmentsBtn = new JButton("Manage Appointments");
        JButton feedbackBtn = new JButton("Add/View Feedback");
        JButton logoutBtn = new JButton("Logout");

        // Style buttons
        Dimension buttonSize = new Dimension(200, 40);
        viewStudentsBtn.setPreferredSize(buttonSize);
        appointmentsBtn.setPreferredSize(buttonSize);
        feedbackBtn.setPreferredSize(buttonSize);
        logoutBtn.setPreferredSize(buttonSize);

        // Add action listeners
        viewStudentsBtn.addActionListener(_ -> cardLayout.show(mainPanel, "students"));
        appointmentsBtn.addActionListener(_ -> {
            refreshAppointmentTable();
            cardLayout.show(mainPanel, "appointments");
        });
        feedbackBtn.addActionListener(_ -> cardLayout.show(mainPanel, "feedback"));
        logoutBtn.addActionListener(_ -> {
            Log.writeLog("Supervisor " + currentSupervisor.getUserId() + " logged out");
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
        menuPanel.add(viewStudentsBtn, gbc);
        menuPanel.add(appointmentsBtn, gbc);
        menuPanel.add(feedbackBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(50), gbc);
        menuPanel.add(logoutBtn, gbc);

        mainPanel.add(menuPanel, "menu");
    }

    private void createStudentsPanel() {
        JPanel studentsPanel = new JPanel(new BorderLayout(10, 10));
        studentsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model
        String[] columnNames = {"Student ID", "Username", "Program"};
        studentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        studentTable = new JTable(studentTableModel);
        studentTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(studentTable);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backBtn = new JButton("Back to Menu");
        
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        
        buttonsPanel.add(backBtn);

        // Add components to panel
        studentsPanel.add(scrollPane, BorderLayout.CENTER);
        studentsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        mainPanel.add(studentsPanel, "students");
        
        // Initial load of students
        refreshStudentTable();
    }

    private void createAppointmentsPanel() {
        JPanel appointmentsPanel = new JPanel(new BorderLayout(10, 10));
        appointmentsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model
        String[] columnNames = {"Appointment ID", "Student ID", "Date/Time", "Status"};
        appointmentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        appointmentTable = new JTable(appointmentTableModel);
        appointmentTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(appointmentTable);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backBtn = new JButton("Back to Menu");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");

        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        approveBtn.addActionListener(_ -> handleAppointmentDecision("Approve"));
        rejectBtn.addActionListener(_ -> handleAppointmentDecision("Reject"));

        buttonsPanel.add(backBtn);
        buttonsPanel.add(approveBtn);
        buttonsPanel.add(rejectBtn);

        // Add components to panel
        appointmentsPanel.add(scrollPane, BorderLayout.CENTER);
        appointmentsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        mainPanel.add(appointmentsPanel, "appointments");
    }

    private void createFeedbackPanel() {
        JPanel feedbackPanel = new JPanel(new GridBagLayout());
        feedbackPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel titleLabel = new JLabel("Add/View Appointment Feedback", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel appointmentIdLabel = new JLabel("Appointment ID:");
        JTextField appointmentIdField = new JTextField(20);
        JTextArea feedbackArea = new JTextArea(5, 40);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);

        JButton viewBtn = new JButton("View Feedback");
        JButton submitBtn = new JButton("Submit Feedback");
        JButton backBtn = new JButton("Back to Menu");

        viewBtn.addActionListener(_ -> {
            String appointmentId = appointmentIdField.getText().trim();
            String feedback = logic.getFeedback(appointmentId);
            if (feedback != null) {
                feedbackArea.setText(feedback);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No feedback found for this appointment",
                    "View Feedback",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        submitBtn.addActionListener(_ -> {
            String appointmentId = appointmentIdField.getText().trim();
            String feedback = feedbackArea.getText().trim();
            if (appointmentId.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter an appointment ID",
                    "Submit Feedback",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (feedback.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter feedback",
                    "Submit Feedback",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            logic.updateFeedback(appointmentId, feedback);
            JOptionPane.showMessageDialog(this,
                "Feedback submitted successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            appointmentIdField.setText("");
            feedbackArea.setText("");
        });

        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));

        feedbackPanel.add(titleLabel, gbc);
        feedbackPanel.add(Box.createVerticalStrut(20), gbc);
        feedbackPanel.add(appointmentIdLabel, gbc);
        feedbackPanel.add(appointmentIdField, gbc);
        feedbackPanel.add(Box.createVerticalStrut(10), gbc);
        feedbackPanel.add(feedbackScrollPane, gbc);
        feedbackPanel.add(Box.createVerticalStrut(20), gbc);
        feedbackPanel.add(viewBtn, gbc);
        feedbackPanel.add(submitBtn, gbc);
        feedbackPanel.add(Box.createVerticalStrut(20), gbc);
        feedbackPanel.add(backBtn, gbc);

        mainPanel.add(feedbackPanel, "feedback");
    }

    private void refreshStudentTable() {
        studentTableModel.setRowCount(0);
        List<String> assignedStudents = currentSupervisor.getStudent();
        if (assignedStudents != null) {
            List<Student> students = FileHandling.loadStudents("students.txt");
            for (Student student : students) {
                if (assignedStudents.contains(student.getUserId())) {
                    studentTableModel.addRow(new Object[]{
                        student.getUserId(),
                        student.getUsername(),
                        student.getProgram()
                    });
                }
            }
        }
    }

    private void refreshAppointmentTable() {
        appointmentTableModel.setRowCount(0);
        List<Appointment> appointments = FileHandling.loadAppointments("appointments.txt");
        for (Appointment appointment : appointments) {
            if (appointment.getSupervisorUsername().equals(currentSupervisor.getUsername())) {
                appointmentTableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getStudentId(),
                    appointment.getDateTime(),
                    appointment.getStatus()
                });
            }
        }
    }

    private void handleAppointmentDecision(String decision) {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = (String) appointmentTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) appointmentTable.getValueAt(selectedRow, 3);

        if (!currentStatus.equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this,
                "Can only process pending appointments",
                "Invalid Operation",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to " + decision.toLowerCase() + " this appointment?",
            "Confirm " + decision,
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Use logic to update status and save
            String status = decision.equalsIgnoreCase("Approve") ? "Approved" : "Rejected";
            logic.updateAppointmentStatus(appointmentId, status);
            refreshAppointmentTable();
            JOptionPane.showMessageDialog(this,
                "Appointment " + status.toLowerCase() + " successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}