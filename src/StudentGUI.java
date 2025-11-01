import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
// Removed unused import
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class StudentGUI extends JFrame {
    private final studentLogic logic;
    private final Student currentStudent;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;
    private DefaultTableModel appointmentTableModel;
    private JTable appointmentTable;
    
    public StudentGUI(Student student, List<Appointment> appointments) {
        this.currentStudent = student;
        this.logic = new studentLogic("appointments.txt");
        
        // Setup main window
        setTitle("Student Portal - " + student.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create card layout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create menu panel
        createMenuPanel();
        
        // Create appointments panel
        createAppointmentsPanel();
        
        // Create new appointment panel
        createNewAppointmentPanel();

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

        JLabel welcomeLabel = new JLabel("Welcome " + currentStudent.getUsername() + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JButton viewAppointmentsBtn = new JButton("View My Appointments");
        JButton newAppointmentBtn = new JButton("Make New Appointment");
        JButton logoutBtn = new JButton("Logout");

        // Style buttons
        Dimension buttonSize = new Dimension(200, 40);
        viewAppointmentsBtn.setPreferredSize(buttonSize);
        newAppointmentBtn.setPreferredSize(buttonSize);
        logoutBtn.setPreferredSize(buttonSize);

        // Add action listeners
        viewAppointmentsBtn.addActionListener(_ -> cardLayout.show(mainPanel, "appointments"));
        newAppointmentBtn.addActionListener(_ -> cardLayout.show(mainPanel, "newAppointment"));
        logoutBtn.addActionListener(_ -> {
            dispose();
            Log.writeLog("Student " + currentStudent.getUserId() + " logged out");
        });

        menuPanel.add(welcomeLabel, gbc);
        menuPanel.add(Box.createVerticalStrut(30), gbc);
        menuPanel.add(viewAppointmentsBtn, gbc);
        menuPanel.add(newAppointmentBtn, gbc);
        menuPanel.add(Box.createVerticalStrut(50), gbc);
        menuPanel.add(logoutBtn, gbc);

        mainPanel.add(menuPanel, "menu");
    }

    private void createAppointmentsPanel() {
        JPanel appointmentsPanel = new JPanel(new BorderLayout(10, 10));
        appointmentsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model
        String[] columnNames = {"ID", "Date/Time", "Status", "Feedback"};
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
        JButton rescheduleBtn = new JButton("Reschedule");
        JButton cancelBtn = new JButton("Cancel Appointment");

        backBtn.addActionListener(_ -> {
            cardLayout.show(mainPanel, "menu");
            refreshAppointmentTable();
        });

        rescheduleBtn.addActionListener(_ -> handleReschedule());
        cancelBtn.addActionListener(_ -> handleCancel());

        buttonsPanel.add(backBtn);
        buttonsPanel.add(rescheduleBtn);
        buttonsPanel.add(cancelBtn);

        // Add components to panel
        appointmentsPanel.add(scrollPane, BorderLayout.CENTER);
        appointmentsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        mainPanel.add(appointmentsPanel, "appointments");
        
        // Initial load of appointments
        refreshAppointmentTable();
    }

    private void createNewAppointmentPanel() {
        JPanel newAppointmentPanel = new JPanel(new GridBagLayout());
        newAppointmentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        JLabel titleLabel = new JLabel("Make New Appointment", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel dateTimeLabel = new JLabel("Date and Time (YYYY-MM-DD HH:mm):");
        JTextField dateTimeField = new JTextField(20);

        JButton submitBtn = new JButton("Submit");
        JButton backBtn = new JButton("Back to Menu");

        submitBtn.addActionListener(_ -> {
            String dateTime = dateTimeField.getText().trim();
            if (validateDateTime(dateTime)) {
                logic.makeAppointment(currentStudent, dateTime);
                JOptionPane.showMessageDialog(this, "Appointment requested successfully!");
                dateTimeField.setText("");
                cardLayout.show(mainPanel, "menu");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid future date and time in format: YYYY-MM-DD HH:mm",
                    "Invalid Date/Time",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(_ -> cardLayout.show(mainPanel, "menu"));

        newAppointmentPanel.add(titleLabel, gbc);
        newAppointmentPanel.add(Box.createVerticalStrut(20), gbc);
        newAppointmentPanel.add(dateTimeLabel, gbc);
        newAppointmentPanel.add(dateTimeField, gbc);
        newAppointmentPanel.add(Box.createVerticalStrut(20), gbc);
        newAppointmentPanel.add(submitBtn, gbc);
        newAppointmentPanel.add(backBtn, gbc);

        mainPanel.add(newAppointmentPanel, "newAppointment");
    }

    private void refreshAppointmentTable() {
        appointmentTableModel.setRowCount(0);
        List<Appointment> appointments = FileHandling.loadAppointments("appointments.txt");
        
        for (Appointment appointment : appointments) {
            if (appointment.getStudentId().equals(currentStudent.getUserId())) {
                appointmentTableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    appointment.getDateTime(),
                    appointment.getStatus(),
                    appointment.getFeedback()
                });
            }
        }
    }

    private void handleReschedule() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment to reschedule",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = (String) appointmentTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) appointmentTable.getValueAt(selectedRow, 2);

        if (!currentStatus.equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this,
                "Can only reschedule pending appointments",
                "Invalid Operation",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newDateTime = JOptionPane.showInputDialog(this,
            "Enter new date and time (YYYY-MM-DD HH:mm):");
        
        if (newDateTime != null && validateDateTime(newDateTime)) {
            logic.rescheduleAppointment(currentStudent, appointmentId, newDateTime);
            refreshAppointmentTable();
            JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully!");
        } else if (newDateTime != null) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid future date and time",
                "Invalid Date/Time",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancel() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment to cancel",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = (String) appointmentTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) appointmentTable.getValueAt(selectedRow, 2);

        if (currentStatus.equalsIgnoreCase("Cancelled")) {
            JOptionPane.showMessageDialog(this,
                "This appointment is already cancelled",
                "Invalid Operation",
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
            JOptionPane.showMessageDialog(this, "Appointment cancelled successfully!");
        }
    }

    private boolean validateDateTime(String dateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setLenient(false);
            Date date = sdf.parse(dateTime);
            
            // Check if date is in the future
            return date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }
}