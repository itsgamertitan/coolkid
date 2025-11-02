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
    private JComboBox<String> appointmentIdDropdown;
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
        JButton viewAssignedStudentsBtn = new JButton("View Assigned Students");
        JButton uploadTimeslotBtn = new JButton("Upload Timeslot");
        JButton viewTimeslotsBtn = new JButton("View/Remove Timeslots");
        JButton logoutBtn = new JButton("Logout");

        viewAppointmentsBtn.addActionListener(_ -> {
            refreshAppointmentTable();
            cardLayout.show(mainPanel, "viewAppointments");
        });

        viewAssignedStudentsBtn.addActionListener(_ -> showAssignedStudentsDialog());
        uploadTimeslotBtn.addActionListener(_ -> showUploadTimeslotDialog());
        viewTimeslotsBtn.addActionListener(_ -> showViewTimeslotsDialog());

        logoutBtn.addActionListener(_ -> {
            dispose();
            new LoginGUI().setVisible(true);
            Log.writeLog("Supervisor logged out: " + currentSupervisor.getUserId());
        });

        menuPanel.add(welcomeLabel, gbc);
        menuPanel.add(Box.createVerticalStrut(30), gbc);
        menuPanel.add(viewAppointmentsBtn, gbc);
        menuPanel.add(viewAssignedStudentsBtn, gbc);
        menuPanel.add(uploadTimeslotBtn, gbc);
        menuPanel.add(viewTimeslotsBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(50), gbc);
        menuPanel.add(logoutBtn, gbc);

        mainPanel.add(menuPanel, "menu");
    private void showUploadTimeslotDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Date (yyyy-MM-dd):"));
        JTextField dateField = new JTextField();
        panel.add(dateField);
        panel.add(new JLabel("Time (HH:mm):"));
        JTextField timeField = new JTextField();
        panel.add(timeField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Upload Timeslot", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            if (date.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both date and time are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String timeslot = currentSupervisor.getUsername() + "|" + date + " " + time;
            try (java.io.FileWriter writer = new java.io.FileWriter("timeslots.txt", true)) {
                writer.write(timeslot + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving timeslot: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Timeslot uploaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showViewTimeslotsDialog() {
        java.util.List<String> timeslots = new java.util.ArrayList<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("timeslots.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(currentSupervisor.getUsername() + "|")) {
                    timeslots.add(line.substring(line.indexOf("|") + 1));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading timeslots: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (timeslots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No timeslots uploaded.", "Timeslots", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JList<String> timeslotList = new JList<>(timeslots.toArray(new String[0]));
        timeslotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(timeslotList);
        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Your Timeslots (Select to Remove)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int selected = timeslotList.getSelectedIndex();
            if (selected >= 0) {
                String toRemove = currentSupervisor.getUsername() + "|" + timeslots.get(selected);
                // Remove from file
                java.util.List<String> allLines = new java.util.ArrayList<>();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("timeslots.txt"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.equals(toRemove)) {
                            allLines.add(line);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating timeslots: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try (java.io.FileWriter writer = new java.io.FileWriter("timeslots.txt", false)) {
                    for (String l : allLines) writer.write(l + "\n");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating timeslots: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Timeslot removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    private void showAssignedStudentsDialog() {
        List<String> students = currentSupervisor.getStudent();
        StringBuilder sb = new StringBuilder();
        if (students.isEmpty()) {
            sb.append("No students assigned.");
        } else {
            sb.append("Assigned Students:\n");
            for (String s : students) {
                sb.append("- ").append(s).append("\n");
            }
        }
        JOptionPane.showMessageDialog(this,
            sb.toString(),
            "Assigned Students",
            JOptionPane.INFORMATION_MESSAGE);
    }
    }

    private void createViewAppointmentsPanel() {
        JPanel viewPanel = new JPanel(new BorderLayout(10, 10));
        viewPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        appointmentsTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    appointmentIdDropdown = new JComboBox<>();
    buttonPanel.add(new JLabel("Select Appointment ID:"));
    buttonPanel.add(appointmentIdDropdown);
    JButton backBtn = new JButton("Back to Menu");
    JButton approveBtn = new JButton("Approve Selected");
    JButton rejectBtn = new JButton("Reject Selected");
    JButton addFeedbackBtn = new JButton("Add Feedback");

    backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
    approveBtn.addActionListener(_ -> handleAppointmentActionDropdown("Approved"));
    rejectBtn.addActionListener(_ -> handleAppointmentActionDropdown("Rejected"));
    addFeedbackBtn.addActionListener(_ -> handleAddFeedbackDropdown());

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
        appointmentIdDropdown.removeAllItems();
        List<Appointment> appointments = logic.getAppointments();
        for (Appointment appointment : appointments) {
            if (appointment.getSupervisorUsername().equals(currentSupervisor.getUsername())) {
                tableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getDateTime(),
                    appointment.getStudentUsername(),
                    appointment.getStatus(),
                    appointment.getFeedback()
                });
                appointmentIdDropdown.addItem(appointment.getAppointmentId());
            }
        }
    }

    private void handleAppointmentActionDropdown(String action) {
        String appointmentId = (String) appointmentIdDropdown.getSelectedItem();
        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this.getContentPane(),
                "Please select an appointment ID",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Find status
        int row = -1;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (appointmentId.equals(tableModel.getValueAt(i, 0))) {
                row = i;
                break;
            }
        }
        if (row == -1) {
            JOptionPane.showMessageDialog(this.getContentPane(),
                "Appointment not found in table.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        String currentStatus = (String) tableModel.getValueAt(row, 3);
        if (!currentStatus.equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this.getContentPane(),
                "Can only modify pending appointments",
                "Invalid Action",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this.getContentPane(),
            "Are you sure you want to " + action.toLowerCase() + " this appointment?",
            "Confirm " + action,
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            logic.updateAppointmentStatus(appointmentId, action);
            refreshAppointmentTable();
            JOptionPane.showMessageDialog(this.getContentPane(),
                "Appointment " + action.toLowerCase() + " successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleAddFeedbackDropdown() {
        String appointmentId = (String) appointmentIdDropdown.getSelectedItem();
        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this.getContentPane(),
                "Please select an appointment ID",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Find feedback
        int row = -1;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (appointmentId.equals(tableModel.getValueAt(i, 0))) {
                row = i;
                break;
            }
        }
        if (row == -1) {
            JOptionPane.showMessageDialog(this.getContentPane(),
                "Appointment not found in table.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        String currentFeedback = (String) tableModel.getValueAt(row, 4);
        JTextArea feedbackArea = new JTextArea(currentFeedback, 5, 30);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        int result = JOptionPane.showConfirmDialog(this.getContentPane(),
            scrollPane,
            "Add/Edit Feedback",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String newFeedback = feedbackArea.getText().trim();
            if (!newFeedback.equals(currentFeedback)) {
                logic.addFeedback(appointmentId, newFeedback);
                refreshAppointmentTable();
                JOptionPane.showMessageDialog(this.getContentPane(),
                    "Feedback updated successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    }
