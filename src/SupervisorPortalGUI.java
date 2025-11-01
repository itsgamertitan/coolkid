import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SupervisorPortalGUI extends JFrame {
    private final Supervisor currentSupervisor;
    private final SupervisorLogic logic;
    private final JTable appointmentsTable;
    private final DefaultTableModel tableModel;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    public SupervisorPortalGUI(Supervisor supervisor, SupervisorLogic logic) {
        this.currentSupervisor = supervisor;
        this.logic = logic;
        
        // Setup main window
        setTitle("Supervisor Portal - " + supervisor.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize table model for appointments
        String[] columnNames = {"ID", "Date/Time", "Student", "Status", "Feedback"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentsTable = new JTable(tableModel);
        
        // Create panels
        createMainMenu();
        createViewAppointmentsPanel();
        
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
        
        JLabel welcomeLabel = new JLabel("Welcome " + currentSupervisor.getUsername() + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JButton viewAppointmentsBtn = new JButton("View Appointments");
        JButton logoutBtn = new JButton("Logout");
        
        viewAppointmentsBtn.addActionListener(_ -> {
            refreshAppointmentTable();
            cardLayout.show(mainPanel, "viewAppointments");
        });
        
        logoutBtn.addActionListener(_ -> {
            dispose();
            new LoginGUI().setVisible(true);
            Log.writeLog("Supervisor logged out: " + currentSupervisor.getUserId());
        });
        
        menuPanel.add(welcomeLabel, gbc);
        menuPanel.add(Box.createVerticalStrut(30), gbc);
        menuPanel.add(viewAppointmentsBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(50), gbc);
        menuPanel.add(logoutBtn, gbc);
        
        mainPanel.add(menuPanel, "menu");
    }

    private void createViewAppointmentsPanel() {
        JPanel viewPanel = new JPanel(new BorderLayout(10, 10));
        viewPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        appointmentsTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backBtn = new JButton("Back to Menu");
        JButton approveBtn = new JButton("Approve Selected");
        JButton rejectBtn = new JButton("Reject Selected");
        JButton addFeedbackBtn = new JButton("Add Feedback");
        
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        approveBtn.addActionListener(_ -> handleAppointmentAction("Approved"));
        rejectBtn.addActionListener(_ -> handleAppointmentAction("Rejected"));
        addFeedbackBtn.addActionListener(_ -> handleAddFeedback());
        
        buttonPanel.add(backBtn);
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(addFeedbackBtn);
        
        viewPanel.add(scrollPane, BorderLayout.CENTER);
        viewPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(viewPanel, "viewAppointments");
    }

    private void refreshAppointmentTable() {
        tableModel.setRowCount(0);
        List<Appointment> appointments = logic.getAppointments();
        
        for (Appointment appointment : appointments) {
            if (appointment.getSupervisorId().equals(currentSupervisor.getUserId())) {
                tableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getDateTime(),
                    appointment.getStudentUsername(),
                    appointment.getStatus(),
                    appointment.getFeedback()
                });
            }
        }
    }

    private void handleAppointmentAction(String action) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String appointmentId = (String) appointmentsTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) appointmentsTable.getValueAt(selectedRow, 3);
        
        if (!currentStatus.equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this,
                "Can only modify pending appointments",
                "Invalid Action",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to " + action.toLowerCase() + " this appointment?",
            "Confirm " + action,
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            logic.updateAppointmentStatus(appointmentId, action);
            refreshAppointmentTable();
            JOptionPane.showMessageDialog(this,
                "Appointment " + action.toLowerCase() + " successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleAddFeedback() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String appointmentId = (String) appointmentsTable.getValueAt(selectedRow, 0);
        String currentFeedback = (String) appointmentsTable.getValueAt(selectedRow, 4);
        
        JTextArea feedbackArea = new JTextArea(currentFeedback, 5, 30);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setLineWrap(true);
        
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        
        int result = JOptionPane.showConfirmDialog(this,
            scrollPane,
            "Add/Edit Feedback",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newFeedback = feedbackArea.getText().trim();
            if (!newFeedback.equals(currentFeedback)) {
                logic.addFeedback(appointmentId, newFeedback);
                refreshAppointmentTable();
                JOptionPane.showMessageDialog(this,
                    "Feedback updated successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}