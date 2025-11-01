import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StudentPortalGUI extends JFrame {
    private final Student currentStudent;
    private final JTable appointmentsTable;
    private final DefaultTableModel tableModel;
    private final studentLogic logic;
    private final JSpinner dateSpinner;
    private final JSpinner timeSpinner;
    private JComboBox<String> supervisorDropdown;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    
    public StudentPortalGUI(Student student, studentLogic logic) {
        System.out.println("[DEBUG] Entered StudentPortalGUI constructor");
        this.currentStudent = student;
        this.logic = logic;
        
    // Setup main window
    setTitle("Student Portal - " + student.getUsername());
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
        String[] columnNames = {"ID", "Date/Time", "Supervisor", "Status", "Feedback"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentsTable = new JTable(tableModel);
        
        // Initialize date and time spinners
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        
        // Create panels
        createMainMenu();
        createViewAppointmentsPanel();
        createNewAppointmentPanel();
        
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
        
        JLabel welcomeLabel = new JLabel("Welcome " + currentStudent.getUsername() + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JButton viewAppointmentsBtn = new JButton("View My Appointments");
        JButton newAppointmentBtn = new JButton("Make New Appointment");
        JButton logoutBtn = new JButton("Logout");
        
        viewAppointmentsBtn.addActionListener(_ -> {
            refreshAppointmentTable();
            cardLayout.show(mainPanel, "viewAppointments");
        });
        
        newAppointmentBtn.addActionListener(_ -> cardLayout.show(mainPanel, "newAppointment"));
        
        logoutBtn.addActionListener(_ -> {
            Log.writeLog("Student logged out: " + currentStudent.getUserId());
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
        menuPanel.add(newAppointmentBtn, gbc);
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
        JButton cancelBtn = new JButton("Cancel Selected Appointment");
        JButton rescheduleBtn = new JButton("Reschedule Selected Appointment");
        
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        
        cancelBtn.addActionListener(_ -> handleCancelAppointment());
        
        rescheduleBtn.addActionListener(_ -> handleRescheduleAppointment());
        
        buttonPanel.add(backBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(rescheduleBtn);
        
        viewPanel.add(scrollPane, BorderLayout.CENTER);
        viewPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(viewPanel, "viewAppointments");
    }
    
    private void createNewAppointmentPanel() {
        JPanel newAppointmentPanel = new JPanel(new GridBagLayout());
        newAppointmentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        JLabel titleLabel = new JLabel("Schedule New Appointment", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        JPanel dateTimePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        dateTimePanel.add(new JLabel("Date:"));
        dateTimePanel.add(dateSpinner);
        dateTimePanel.add(new JLabel("Time:"));
        dateTimePanel.add(timeSpinner);
        dateTimePanel.add(new JLabel("Supervisor:"));
        supervisorDropdown = new JComboBox<>();
        for (Supervisor s : FileHandling.loadSupervisors("supervisors.txt")) {
            supervisorDropdown.addItem(s.getUsername());
        }
        dateTimePanel.add(supervisorDropdown);
        
        JButton submitBtn = new JButton("Submit");
        JButton backBtn = new JButton("Back to Menu");
        
        submitBtn.addActionListener(_ -> handleNewAppointment());
        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));
        
        newAppointmentPanel.add(titleLabel, gbc);
        newAppointmentPanel.add(Box.createVerticalStrut(20), gbc);
        newAppointmentPanel.add(dateTimePanel, gbc);
        newAppointmentPanel.add(Box.createVerticalStrut(20), gbc);
        newAppointmentPanel.add(submitBtn, gbc);
        newAppointmentPanel.add(Box.createVerticalStrut(10), gbc);
        newAppointmentPanel.add(backBtn, gbc);
        
        mainPanel.add(newAppointmentPanel, "newAppointment");
    }
    
    private void refreshAppointmentTable() {
        tableModel.setRowCount(0);
        List<Appointment> appointments = logic.getAppointments();
        
        for (Appointment appointment : appointments) {
            if (appointment.getStudentId().equals(currentStudent.getUserId())) {
                tableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getDateTime(),
                    appointment.getSupervisorUsername(),
                    appointment.getStatus(),
                    appointment.getFeedback()
                });
            }
        }
    }
    
    private void handleNewAppointment() {
        Date date = (Date) dateSpinner.getValue();
        Date time = (Date) timeSpinner.getValue();
        if (date.before(new Date())) {
            JOptionPane.showMessageDialog(this,
                "Cannot schedule appointments in the past",
                "Invalid Date",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String dateTime = dateFormat.format(date) + " " + timeFormat.format(time);
        String selectedSupervisor = (String) supervisorDropdown.getSelectedItem();
        logic.makeAppointment(currentStudent, dateTime, selectedSupervisor);
        JOptionPane.showMessageDialog(this,
            "Appointment scheduled successfully",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, "menu");
    }
    
    private void handleCancelAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment to cancel",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String appointmentId = (String) appointmentsTable.getValueAt(selectedRow, 0);
        String status = (String) appointmentsTable.getValueAt(selectedRow, 3);
        
        if (status.equalsIgnoreCase("Cancelled")) {
            JOptionPane.showMessageDialog(this,
                "This appointment is already cancelled",
                "Cannot Cancel",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this appointment?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            logic.cancelAppointment(currentStudent, appointmentId);
            refreshAppointmentTable();
            JOptionPane.showMessageDialog(this,
                "Appointment cancelled successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void handleRescheduleAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment to reschedule",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String appointmentId = (String) appointmentsTable.getValueAt(selectedRow, 0);
        String status = (String) appointmentsTable.getValueAt(selectedRow, 3);
        
        if (!status.equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this,
                "Can only reschedule pending appointments",
                "Cannot Reschedule",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel reschedulePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JSpinner newDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner newTimeSpinner = new JSpinner(new SpinnerDateModel());
        
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(newDateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(newTimeSpinner, "HH:mm");
        newDateSpinner.setEditor(dateEditor);
        newTimeSpinner.setEditor(timeEditor);
        
        reschedulePanel.add(new JLabel("New Date:"));
        reschedulePanel.add(newDateSpinner);
        reschedulePanel.add(new JLabel("New Time:"));
        reschedulePanel.add(newTimeSpinner);
        
        int result = JOptionPane.showConfirmDialog(this,
            reschedulePanel,
            "Reschedule Appointment",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            Date newDate = (Date) newDateSpinner.getValue();
            Date newTime = (Date) newTimeSpinner.getValue();
            
            if (newDate.before(new Date())) {
                JOptionPane.showMessageDialog(this,
                    "Cannot reschedule to a past date",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String newDateTime = dateFormat.format(newDate) + " " + timeFormat.format(newTime);
            
            logic.rescheduleAppointment(currentStudent, appointmentId, newDateTime);
            refreshAppointmentTable();
            JOptionPane.showMessageDialog(this,
                "Appointment rescheduled successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}