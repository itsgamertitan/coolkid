import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.RowFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.regex.Pattern;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import javax.swing.SpinnerDateModel;
import java.awt.image.BufferedImage;

public class AppGUI extends JFrame {

    private static final String STUDENTS_FILE = "students.txt";
    private static final String USER_FILE = "user.txt";
    private static final String SUPERVISORS_FILE = "supervisors.txt";
    // faculty/system admin files are not required by current UI
    private static final String APPOINTMENTS_FILE = "appointments.txt";

    private List<User> users;
    private List<Student> students;
    private List<Supervisor> supervisors;
    // faculty and system admin lists were removed (not referenced in UI logic)
    private List<Appointment> appointments;

    private CardLayout cards = new CardLayout();
    private JPanel mainPanel = new JPanel(cards);

    private User currentUser;

    // Prevent table listeners from reacting to programmatic updates
    private boolean suppressTableEvents = false;

    // Login components
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);

    // Student components (table)
    private DefaultTableModel studentTableModel = new DefaultTableModel(new String[]{"ID","Supervisor","DateTime","Status","Feedback"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private JTable studentTable = new JTable(studentTableModel);

    // Supervisor components (table)
    private DefaultTableModel supervisorTableModel = new DefaultTableModel(new String[]{"ID","StudentID","DateTime","Status","Feedback"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private JTable supervisorTable = new JTable(supervisorTableModel);

    // Faculty components (table)
    // Editable model toggled when entering edit mode
    private class EditableTableModel extends DefaultTableModel {
        private boolean editable = false;
        public EditableTableModel(Object[] columnNames, int rowCount) { super(columnNames, rowCount); }
        public void setEditable(boolean e) { this.editable = e; }
        @Override public boolean isCellEditable(int row, int col) { return editable; }
    }
    private EditableTableModel facultyStudentsModel = new EditableTableModel(new String[]{"ID","Username","Program","Supervisor"}, 0);
    private JTable facultyStudentsTable = new JTable(facultyStudentsModel);

    // System Admin components (table)
    private EditableTableModel usersTableModel = new EditableTableModel(new String[]{"ID","Username","Role"}, 0);
    private JTable usersTable = new JTable(usersTableModel);

    // Row sorters for filtering/sorting
    private TableRowSorter<DefaultTableModel> usersSorter;
    private TableRowSorter<DefaultTableModel> facultySorter;
    private TableRowSorter<DefaultTableModel> supervisorSorter;
    private TableRowSorter<DefaultTableModel> studentSorter;

    // Backup rows for undo during edit mode
    private java.util.List<Object[]> usersBackupRows = new java.util.ArrayList<>();
    private java.util.List<Object[]> facultyBackupRows = new java.util.ArrayList<>();

    public AppGUI() {
        super("Supervision Hub - GUI");
        // Use default look-and-feel (keep platform appearance) and apply light styling
        loadAllData();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        mainPanel.add(buildLoginPanel(), "login");
        mainPanel.add(buildStudentPanel(), "student");
        mainPanel.add(buildSupervisorPanel(), "supervisor");
        mainPanel.add(buildFacultyPanel(), "faculty");
        mainPanel.add(buildSystemAdminPanel(), "systemadmin");

        // Setup table behaviors (editors, listeners, renderers)
        setupTableBehavior();

    // Setup sorters for filtering and sorting
    usersSorter = new TableRowSorter<>(usersTableModel);
    usersTable.setRowSorter(usersSorter);
    facultySorter = new TableRowSorter<>((DefaultTableModel) facultyStudentsModel);
    facultyStudentsTable.setRowSorter(facultySorter);
    supervisorSorter = new TableRowSorter<>(supervisorTableModel);
    supervisorTable.setRowSorter(supervisorSorter);
    studentSorter = new TableRowSorter<>(studentTableModel);
    studentTable.setRowSorter(studentSorter);

        add(mainPanel);
        cards.show(mainPanel, "login");
    }

    /**
     * Apply a small set of modern visual tweaks to Swing components so the UI feels
     * a bit fresher without adding external look-and-feel libraries.
     */
    private void applyModernStyling(Component root) {
        Color primary = new Color(45, 106, 179); // blue
        Color accent = new Color(67, 142, 219);
        Font base = new Font("SansSerif", Font.PLAIN, 13);

        // Set some UI defaults for future components
        UIManager.put("Button.font", base.deriveFont(Font.BOLD));
        UIManager.put("Label.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("Table.font", base);

        // Recursively style existing components
        styleComponentRecursively(root, primary, accent, base);
    }

    /**
     * Create a small circular icon with a letter (used as a lightweight visual affordance).
     */
    private Icon createCircleIcon(Color fill, int size, String letter) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(fill);
        g.fillOval(0,0,size-1,size-1);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, size/2)));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(letter);
        int h = fm.getAscent();
        g.drawString(letter, (size-w)/2, (size+h)/2 - 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private void styleComponentRecursively(Component c, Color primary, Color accent, Font base) {
        if (c instanceof JButton) {
            JButton b = (JButton) c;
            b.setFont(base.deriveFont(Font.BOLD));
            b.setBackground(primary);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createLineBorder(primary.darker(), 1, true));
        } else if (c instanceof JTextField) {
            JTextField tf = (JTextField) c;
            tf.setFont(base);
            tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1,true), BorderFactory.createEmptyBorder(4,6,4,6)));
        } else if (c instanceof JPasswordField) {
            JPasswordField pf = (JPasswordField) c;
            pf.setFont(base);
            pf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1,true), BorderFactory.createEmptyBorder(4,6,4,6)));
        } else if (c instanceof JTable) {
            JTable t = (JTable) c;
            t.setFont(base);
            t.setRowHeight(26);
            t.setShowGrid(false);
            t.setIntercellSpacing(new Dimension(0, 0));
            t.setSelectionBackground(accent.darker());
            t.setSelectionForeground(Color.WHITE);
            t.setFillsViewportHeight(true);
            // Header styling
            JTableHeader header = t.getTableHeader();
            header.setBackground(primary);
            header.setForeground(Color.WHITE);
            header.setFont(base.deriveFont(Font.BOLD));
            header.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            // Alternating row renderer
            DefaultTableCellRenderer alt = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (!isSelected) comp.setBackground((row % 2 == 0) ? Color.WHITE : new Color(245,247,250));
                    return comp;
                }
            };
            for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(alt);
        } else if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize()));
            l.setForeground(new Color(34,34,34));
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                styleComponentRecursively(child, primary, accent, base);
            }
        }
    }

    private void loadAllData() {
        users = FileHandling.loadUsers(USER_FILE);
        students = FileHandling.loadStudents(STUDENTS_FILE);
        supervisors = FileHandling.loadSupervisors(SUPERVISORS_FILE);
        // faculty/system admin data not required in current UI flows
        appointments = FileHandling.loadAppointments(APPOINTMENTS_FILE);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome to Supervision Hub");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> doLogin());

        return panel;
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (User u : users) {
            if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                currentUser = u;
                switch (u.getRole()) {
                    case "Student":
                        refreshStudentAppointments();
                        cards.show(mainPanel, "student");
                        return;
                    case "Supervisor":
                        refreshSupervisorAppointments();
                        cards.show(mainPanel, "supervisor");
                        return;
                    case "FacultyAdmin":
                        refreshStudentsList();
                        cards.show(mainPanel, "faculty");
                        return;
                    case "SystemAdmin":
                        refreshUsersList();
                        cards.show(mainPanel, "systemadmin");
                        return;
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel buildStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        JPanel top = createTopBar(() -> { refreshStudentAppointments(); }, "Student Dashboard");
        panel.add(top, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(8,8));
        center.setBorder(new EmptyBorder(8,8,8,8));
        JLabel label = new JLabel("Your Appointments:");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        center.add(label, BorderLayout.NORTH);
        studentTable.setFillsViewportHeight(true);
        center.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JButton newApp = new JButton("New Appointment");
    newApp.setIcon(createCircleIcon(new Color(75,180,120), 16, "+"));
    newApp.setHorizontalTextPosition(SwingConstants.RIGHT);
    newApp.addActionListener(e -> showNewAppointmentDialog());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(newApp);
        center.add(south, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStudentAppointments() {
        // clear table
        studentTableModel.setRowCount(0);
        if (!(currentUser instanceof Student)) {
            // try to find student by userId
            for (Student s : students) {
                if (s.getUserId().equals(currentUser.getUserId())) {
                    currentUser = s;
                    break;
                }
            }
        }
        for (Appointment a : appointments) {
            if (a.getStudentId().equals(currentUser.getUserId())) {
                studentTableModel.addRow(new Object[]{a.getAppointmentId(), a.getSupervisorUsername(), a.getDateTime(), a.getStatus(), a.getFeedback()});
            }
        }
    }

    private void showNewAppointmentDialog() {
        JPanel p = new JPanel(new GridLayout(0,1,6,6));
        p.setBorder(new EmptyBorder(8,8,8,8));
        JComboBox<String> supBox = new JComboBox<>();
        for (Supervisor s : supervisors) supBox.addItem(s.getUsername());

        // Date/time spinner
        SpinnerDateModel sdm = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner dateSpinner = new JSpinner(sdm);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm");
        dateSpinner.setEditor(editor);

        p.add(new JLabel("Choose Supervisor:")); p.add(supBox);
        p.add(new JLabel("Date & Time:")); p.add(dateSpinner);
        int res = JOptionPane.showConfirmDialog(this, p, "New Appointment", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String sup = (String) supBox.getSelectedItem();
            Date dtVal = (Date) dateSpinner.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dt = sdf.format(dtVal);
            String id = "A" + System.currentTimeMillis();
            Appointment a = new Appointment(id, currentUser.getUserId(), sup, dt, "Pending", "");
            appointments.add(a);
            FileHandling.saveToAppointment(a, APPOINTMENTS_FILE);
            refreshStudentAppointments();
            JOptionPane.showMessageDialog(this, "Appointment created (Pending).", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel buildSupervisorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        JPanel top = createTopBar(() -> { refreshSupervisorAppointments(); }, "Supervisor Dashboard");
        panel.add(top, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(8,8));
        JLabel label = new JLabel("Appointments assigned to you:");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        center.add(label, BorderLayout.NORTH);
        supervisorTable.setFillsViewportHeight(true);
        center.add(new JScrollPane(supervisorTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton feedback = new JButton("Add Feedback");
    approve.setIcon(createCircleIcon(new Color(34,139,34), 14, "✓"));
    approve.setHorizontalTextPosition(SwingConstants.RIGHT);
    reject.setIcon(createCircleIcon(new Color(204,0,0), 14, "✕"));
    reject.setHorizontalTextPosition(SwingConstants.RIGHT);
    feedback.setIcon(createCircleIcon(new Color(67,142,219), 14, "i"));
    feedback.setHorizontalTextPosition(SwingConstants.RIGHT);
    south.add(feedback); south.add(approve); south.add(reject);
        panel.add(center, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        approve.addActionListener(e -> changeSelectedSupervisorAppointmentStatus("Approved"));
        reject.addActionListener(e -> changeSelectedSupervisorAppointmentStatus("Rejected"));
        feedback.addActionListener(e -> addFeedbackToSelectedAppointment());

        return panel;
    }

    private void refreshSupervisorAppointments() {
        supervisorTableModel.setRowCount(0);
        String supervisorName = currentUser.getUsername();
        for (Appointment a : appointments) {
            if (a.getSupervisorUsername().equals(supervisorName)) {
                supervisorTableModel.addRow(new Object[]{a.getAppointmentId(), a.getStudentId(), a.getDateTime(), a.getStatus(), a.getFeedback()});
            }
        }
    }

    private void changeSelectedSupervisorAppointmentStatus(String status) {
        int row = supervisorTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
        String id = supervisorTableModel.getValueAt(row, 0).toString();
        for (Appointment a : appointments) {
            if (a.getAppointmentId().equals(id)) {
                a.setStatus(status);
                FileHandling.saveAllAppointments(appointments, APPOINTMENTS_FILE);
                refreshSupervisorAppointments();
                JOptionPane.showMessageDialog(this, "Appointment " + status + ".");
                return;
            }
        }
    }

    private void addFeedbackToSelectedAppointment() {
        int row = supervisorTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
        String id = supervisorTableModel.getValueAt(row, 0).toString();
        String existing = supervisorTableModel.getValueAt(row, 4) == null ? "" : supervisorTableModel.getValueAt(row, 4).toString();
        String fb = JOptionPane.showInputDialog(this, "Enter feedback:", existing);
        if (fb != null) {
            for (Appointment a : appointments) {
                if (a.getAppointmentId().equals(id)) {
                    a.setFeedback(fb);
                    FileHandling.saveAllAppointments(appointments, APPOINTMENTS_FILE);
                    refreshSupervisorAppointments();
                    JOptionPane.showMessageDialog(this, "Feedback saved.");
                    return;
                }
            }
        }
    }

    private void setupTableBehavior() {
        // Users: allow editing username and role inline
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Student","Supervisor","FacultyAdmin","SystemAdmin"});
        usersTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(roleCombo));

        usersTableModel.addTableModelListener(e -> {
            if (suppressTableEvents) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row < 0 || col < 0) return;
            String id = usersTableModel.getValueAt(row, 0).toString();
            for (User u : users) {
                if (u.getUserId().equals(id)) {
                    if (col == 1) u.setUsername(usersTableModel.getValueAt(row, col).toString());
                    if (col == 2) u.setRole(usersTableModel.getValueAt(row, col).toString());
                    FileHandling.saveAllUsers(users, USER_FILE);
                    suppressTableEvents = true;
                    refreshUsersList();
                    suppressTableEvents = false;
                    return;
                }
            }
        });

        // Faculty students: allow editing username, program, supervisor inline
        // Supervisor column uses combobox editor
        JComboBox<String> supCombo = new JComboBox<>();
        for (Supervisor s : supervisors) supCombo.addItem(s.getUsername());
        facultyStudentsTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(supCombo));

        facultyStudentsModel.addTableModelListener(e -> {
            if (suppressTableEvents) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row < 0 || col < 0) return;
            String id = facultyStudentsModel.getValueAt(row, 0).toString();
            for (Student s : students) {
                if (s.getUserId().equals(id)) {
                    if (col == 1) s.setUsername(facultyStudentsModel.getValueAt(row, col).toString());
                    if (col == 2) s.setProgram(facultyStudentsModel.getValueAt(row, col).toString());
                    if (col == 3) s.setSupervisor(facultyStudentsModel.getValueAt(row, col).toString());
                    FileHandling.saveAllStudents(students, STUDENTS_FILE);
                    suppressTableEvents = true;
                    refreshStudentsList();
                    suppressTableEvents = false;
                    return;
                }
            }
        });

        // Render status with colors
        TableCellRenderer statusRenderer = (table, value, isSelected, hasFocus, row, column) -> {
            JLabel lbl = new JLabel(value == null ? "" : value.toString());
            lbl.setOpaque(true);
            String s = value == null ? "" : value.toString();
            switch (s) {
                case "Approved": lbl.setBackground(new Color(198, 239, 206)); lbl.setForeground(new Color(21,87,36)); break;
                case "Rejected": lbl.setBackground(new Color(255, 199, 206)); lbl.setForeground(new Color(156,0,6)); break;
                default: lbl.setBackground(new Color(255, 235, 156)); lbl.setForeground(Color.BLACK); break;
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
            return lbl;
        };
        // status column index 3 for both student and supervisor tables
        studentTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);
        supervisorTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);

        // Some column widths for nicer formatting
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        supervisorTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        supervisorTable.getColumnModel().getColumn(1).setPreferredWidth(120);
    }

    private JPanel buildFacultyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        JPanel top = createTopBar(() -> { refreshStudentsList(); }, "Faculty Admin");
        panel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8,8));
        JLabel label = new JLabel("Students:");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        center.add(label, BorderLayout.NORTH);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search:"));
        JTextField facultySearch = new JTextField(20);
        searchBar.add(facultySearch);
        center.add(searchBar, BorderLayout.BEFORE_FIRST_LINE);

        facultyStudentsTable.setFillsViewportHeight(true);
        center.add(new JScrollPane(facultyStudentsTable), BorderLayout.CENTER);

    JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton editMode = new JButton("Enter Edit Mode");
    editMode.setIcon(createCircleIcon(new Color(67,142,219), 14, "E"));
    JButton applyChanges = new JButton("Apply Changes"); applyChanges.setEnabled(false);
    applyChanges.setIcon(createCircleIcon(new Color(34,139,34), 14, "✓"));
    JButton undoChanges = new JButton("Undo Changes"); undoChanges.setEnabled(false);
    undoChanges.setIcon(createCircleIcon(new Color(200,75,75), 14, "↺"));
    JButton reassign = new JButton("Reassign Supervisor");
    reassign.setIcon(createCircleIcon(new Color(67,142,219), 14, "⤴"));
    south.add(editMode); south.add(applyChanges); south.add(undoChanges); south.add(reassign);
        center.add(south, BorderLayout.SOUTH);

        // Search filter
        facultySearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = facultySearch.getText().trim();
                if (text.isEmpty()) {
                    facultySorter.setRowFilter(null);
                } else {
                    facultySorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                }
            }
        });

        // Edit mode behavior
        editMode.addActionListener(ae -> {
            // backup rows
            facultyBackupRows.clear();
            for (int r = 0; r < facultyStudentsModel.getRowCount(); r++) {
                int c = facultyStudentsModel.getColumnCount();
                Object[] row = new Object[c];
                for (int j = 0; j < c; j++) row[j] = facultyStudentsModel.getValueAt(r,j);
                facultyBackupRows.add(row);
            }
            facultyStudentsModel.setEditable(true);
            editMode.setEnabled(false);
            applyChanges.setEnabled(true);
            undoChanges.setEnabled(true);
        });

        applyChanges.addActionListener(ae -> {
            // persist edits from model to students list
            for (int r = 0; r < facultyStudentsModel.getRowCount(); r++) {
                String id = facultyStudentsModel.getValueAt(r,0).toString();
                for (Student s : students) {
                    if (s.getUserId().equals(id)) {
                        s.setUsername(facultyStudentsModel.getValueAt(r,1).toString());
                        s.setProgram(facultyStudentsModel.getValueAt(r,2).toString());
                        s.setSupervisor(facultyStudentsModel.getValueAt(r,3).toString());
                        break;
                    }
                }
            }
            FileHandling.saveAllStudents(students, STUDENTS_FILE);
            facultyStudentsModel.setEditable(false);
            editMode.setEnabled(true);
            applyChanges.setEnabled(false);
            undoChanges.setEnabled(false);
            refreshStudentsList();
            JOptionPane.showMessageDialog(this, "Changes applied.");
        });

        undoChanges.addActionListener(ae -> {
            // restore from backup
            facultyStudentsModel.setRowCount(0);
            for (Object[] row : facultyBackupRows) facultyStudentsModel.addRow(row);
            facultyStudentsModel.setEditable(false);
            editMode.setEnabled(true);
            applyChanges.setEnabled(false);
            undoChanges.setEnabled(false);
        });

        reassign.addActionListener(e -> reassignSelectedStudent());

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStudentsList() {
        facultyStudentsModel.setRowCount(0);
        for (Student s : students) {
            facultyStudentsModel.addRow(new Object[]{s.getUserId(), s.getUsername(), s.getProgram(), s.getSupervisor()});
        }
    }

    private void reassignSelectedStudent() {
        int row = facultyStudentsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a student."); return; }
        String studentId = facultyStudentsModel.getValueAt(row, 0).toString();
        Student target = null;
        for (Student s : students) if (s.getUserId().equals(studentId)) { target = s; break; }
        if (target == null) return;

        JComboBox<String> supBox = new JComboBox<>();
        for (Supervisor s : supervisors) supBox.addItem(s.getUsername());
        int res = JOptionPane.showConfirmDialog(this, supBox, "Select new supervisor", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String newSup = (String) supBox.getSelectedItem();
            target.setSupervisor(newSup);
            FileHandling.saveAllStudents(students, STUDENTS_FILE);
            refreshStudentsList();
            JOptionPane.showMessageDialog(this, "Supervisor reassigned.");
        }
    }


    private JPanel buildSystemAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        JPanel top = createTopBar(() -> { refreshUsersList(); }, "System Admin");
        panel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8,8));
        JLabel label = new JLabel("Users:");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        center.add(label, BorderLayout.NORTH);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search:"));
        JTextField userSearch = new JTextField(20);
        searchBar.add(userSearch);
        center.add(searchBar, BorderLayout.BEFORE_FIRST_LINE);

        usersTable.setFillsViewportHeight(true);
        center.add(new JScrollPane(usersTable), BorderLayout.CENTER);

    JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton addUser = new JButton("Add User"); addUser.setIcon(createCircleIcon(new Color(75,180,120), 14, "+"));
    JButton delUser = new JButton("Delete User"); delUser.setIcon(createCircleIcon(new Color(200,75,75), 14, "-"));
    JButton enterEdit = new JButton("Enter Edit Mode"); enterEdit.setIcon(createCircleIcon(new Color(67,142,219), 14, "E"));
    JButton apply = new JButton("Apply Changes"); apply.setEnabled(false); apply.setIcon(createCircleIcon(new Color(34,139,34), 14, "✓"));
    JButton undo = new JButton("Undo Changes"); undo.setEnabled(false); undo.setIcon(createCircleIcon(new Color(200,75,75), 14, "↺"));
    south.add(addUser); south.add(delUser); south.add(enterEdit); south.add(apply); south.add(undo);
        center.add(south, BorderLayout.SOUTH);

        // Search filter
        userSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = userSearch.getText().trim();
                if (text.isEmpty()) usersSorter.setRowFilter(null);
                else usersSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
            }
        });

        addUser.addActionListener(e -> showAddUserDialog());
        delUser.addActionListener(e -> deleteSelectedUser());

        enterEdit.addActionListener(ae -> {
            usersBackupRows.clear();
            for (int r = 0; r < usersTableModel.getRowCount(); r++) {
                int c = usersTableModel.getColumnCount();
                Object[] row = new Object[c];
                for (int j = 0; j < c; j++) row[j] = usersTableModel.getValueAt(r,j);
                usersBackupRows.add(row);
            }
            usersTableModel.setEditable(true);
            enterEdit.setEnabled(false);
            apply.setEnabled(true);
            undo.setEnabled(true);
        });

        apply.addActionListener(ae -> {
            for (int r = 0; r < usersTableModel.getRowCount(); r++) {
                String id = usersTableModel.getValueAt(r,0).toString();
                for (User u : users) if (u.getUserId().equals(id)) {
                    u.setUsername(usersTableModel.getValueAt(r,1).toString());
                    u.setRole(usersTableModel.getValueAt(r,2).toString());
                    break;
                }
            }
            FileHandling.saveAllUsers(users, USER_FILE);
            usersTableModel.setEditable(false);
            enterEdit.setEnabled(true);
            apply.setEnabled(false);
            undo.setEnabled(false);
            refreshUsersList();
            JOptionPane.showMessageDialog(this, "User changes applied.");
        });

        undo.addActionListener(ae -> {
            usersTableModel.setRowCount(0);
            for (Object[] row : usersBackupRows) usersTableModel.addRow(row);
            usersTableModel.setEditable(false);
            enterEdit.setEnabled(true);
            apply.setEnabled(false);
            undo.setEnabled(false);
        });

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create a small top bar with a title and Refresh / Logout buttons.
     */
    private JPanel createTopBar(Runnable refreshAction, String titleText) {
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel(titleText);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        top.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton refresh = new JButton("Refresh");
        Color ic = new Color(67,142,219);
        refresh.setIcon(createCircleIcon(ic, 18, "R"));
        refresh.setHorizontalTextPosition(SwingConstants.RIGHT);
        refresh.addActionListener(e -> refreshAction.run());
        JButton logout = new JButton("Logout");
        logout.setIcon(createCircleIcon(new Color(200,75,75), 18, "L"));
        logout.setHorizontalTextPosition(SwingConstants.RIGHT);
        logout.addActionListener(e -> { currentUser = null; cards.show(mainPanel, "login"); });
        right.add(refresh);
        right.add(logout);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private void refreshUsersList() {
        usersTableModel.setRowCount(0);
        for (User u : users) usersTableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole()});
    }

    private void showAddUserDialog() {
        JPanel p = new JPanel(new GridLayout(0,1,6,6));
        // ID input: small 'TP' label with a numeric text field
        JTextField idField = new JTextField();
        JPanel idPanel = createTpIdPanel(idField);

        JTextField uname = new JTextField();
        JTextField pass = new JTextField();
        JComboBox<String> role = new JComboBox<>(new String[]{"Student","Supervisor","FacultyAdmin","SystemAdmin"});
        p.add(new JLabel("ID (6 digits):")); p.add(idPanel);
        p.add(new JLabel("Username:")); p.add(uname);
        p.add(new JLabel("Password:")); p.add(pass);
        p.add(new JLabel("Role:")); p.add(role);
        int res = JOptionPane.showConfirmDialog(this, p, "Create User", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String raw = idField.getText().trim();
            String nu = uname.getText().trim();
            String np = pass.getText().trim();
            String r = (String) role.getSelectedItem();
            // enforce exactly 6 digits
            String nid = IdUtils.normalizeTpId(raw, 6);
            if (nid == null || nu.isEmpty() || np.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required and ID must be exactly 6 digits.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Ensure uniqueness of user IDs
            for (User uu : users) {
                if (uu.getUserId().equalsIgnoreCase(nid)) {
                    JOptionPane.showMessageDialog(this, "User ID already exists: " + nid, "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            User created = new User(nid, nu, np, r);
            users.add(created);
            FileHandling.saveAllUsers(users, USER_FILE);
            refreshUsersList();
            JOptionPane.showMessageDialog(this, "User created and saved (ID: " + nid + ").");
        }
    }

    /**
     * Helper that creates a small ID input panel with a static "TP" label on the left
     * and the given text field on the right. Reusable for future creation dialogs.
     */
    private JPanel createTpIdPanel(JTextField idField) {
        JPanel idPanel = new JPanel(new BorderLayout(4,0));
        JLabel tpLabel = new JLabel("TP");
        tpLabel.setOpaque(true);
        tpLabel.setBackground(new Color(230,230,230));
        tpLabel.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        idField.setColumns(10);
        idPanel.add(tpLabel, BorderLayout.WEST);
        idPanel.add(idField, BorderLayout.CENTER);
        return idPanel;
    }

    // Inline editing is used for users now; legacy dialog removed.

    private void deleteSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user to delete."); return; }
        String id = usersTableModel.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            users.removeIf(u -> u.getUserId().equals(id));
            FileHandling.saveAllUsers(users, USER_FILE);
            refreshUsersList();
            JOptionPane.showMessageDialog(this, "User deleted.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppGUI gui = new AppGUI();
            // Apply styling now that the constructor finished
            gui.applyModernStyling(gui);
            gui.setVisible(true);
        });
    }
}
