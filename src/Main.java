import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.List;
import java.util.Arrays;

/**
 * Main class to launch the application.
 */
public class Main {
    public static void main(String[] args) {
        // Ensures GUI is created on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}

/**
 * Main GUI class that handles login and main menu transitions.
 */
class MainGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JPanel loginPanel, mainPanel;

    public MainGUI() {
        setTitle("MotorPH Payroll System");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initLoginScreen(); // Initialize login UI
    }

    /**
     * Sets up the login screen UI components.
     */
    private void initLoginScreen() {
        loginPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        loginButton.addActionListener(e -> attemptLogin());

        add(loginPanel, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);
    }

    /**
     * Attempts to authenticate the user based on input credentials.
     */
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (authenticateUser(username, password)) {
            showMainMenu(); // Proceed to main menu on successful login
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verifies credentials against a local file.
     */
    private boolean authenticateUser(String username, String password) {
        File file = new File("login_credentials.txt");
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not read login file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Displays the main menu after successful login.
     */
    private void showMainMenu() {
        getContentPane().removeAll();

        mainPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JButton EmployeeView = new JButton("Employee Viewer");
        EmployeeView.addActionListener(e -> new EmployeeListFrame().setVisible(true));

        JButton ExportCSV = new JButton("Export to CSV");
        ExportCSV.addActionListener(e -> exportCSV());

        mainPanel.add(EmployeeView);
        mainPanel.add(ExportCSV);

        add(mainPanel);
        revalidate();
        repaint();
    }

    /**
     * Exports employee data from the source file to a new CSV file.
     */
    private void exportCSV() {
        File inputFile = new File("employees.csv");
        File csvFile = new File("employee_export_data.csv");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            PrintWriter writer = new PrintWriter(new FileWriter(csvFile))
        ) {
            writer.println("Employee ID, Last Name, First Name, Birthday, Address, Phone No, SSS No, PhilHealth No, TIN, Pag-IBIG No, Status, Position, Immediate Supervisor, Basic Salary, Rice Subsidy, Phone Allowance, Clothing Allowance, Gross Semi-monthly Rate, Hourly Rate");
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
            JOptionPane.showMessageDialog(this, "Data exported to employee_export_data.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting to CSV.");
        }
    }
}

/**
 * Frame to list all employees and handle view, add, and delete actions.
 */
class EmployeeListFrame extends JFrame {
    private JTable employeeTable;
    private DefaultTableModel tableModel;

    public EmployeeListFrame() {
        setTitle("All Employees");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columns = {"Employee ID", "Last Name", "First Name", "Birthday", "Address", "Phone No", "SSS No", "PhilHealth No", "TIN", "Pag-IBIG No", "Status", "Position", "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"};
        tableModel = new DefaultTableModel(columns, 0);
        employeeTable = new JTable(tableModel);
        loadEmployeeData();

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View Employee");
        JButton addButton = new JButton("New Employee");
        JButton deleteButton = new JButton("Delete Employee");
        buttonPanel.add(viewButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        viewButton.addActionListener(e -> viewSelectedEmployee());
        addButton.addActionListener(e -> new NewEmployeeFrame(this).setVisible(true));
        deleteButton.addActionListener(e -> deleteSelectedEmployee());
    }

    
    /**
     * Loads employee data from the CSV file into the table.
     */
   private void loadEmployeeData() {
    tableModel.setRowCount(0);
    try (CSVReader reader = new CSVReader(new FileReader("employees.csv"))) {
        List<String[]> allRows = reader.readAll();
        for (String[] row : allRows) {
            if (row.length >= 19) {
                tableModel.addRow(Arrays.copyOfRange(row, 0, 19));
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage());
    }
}
    /**
     * Opens a detail view for the selected employee.
     */
    private void viewSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an employee to view.");
            return;
        }

        String empId = (String) tableModel.getValueAt(selectedRow, 0);
        String fullName = tableModel.getValueAt(selectedRow, 2) + " " + tableModel.getValueAt(selectedRow, 1);
        new EmployeeDetailFrame(empId, fullName).setVisible(true);
    }

    /**
     * Deletes the selected employee from the CSV and updates the table.
     */
    private void deleteSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an employee to delete.");
            return;
        }

        String empIdToDelete = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete employee ID: " + empIdToDelete + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        File inputFile = new File("employees.csv");
        File tempFile = new File("employee_data_temp.csv");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(empIdToDelete + ",")) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error deleting employee record.");
            return;
        }

        if (inputFile.delete()) {
            tempFile.renameTo(inputFile);
            JOptionPane.showMessageDialog(this, "Employee deleted.");
            refreshTable(); // Refresh the table after deletion
        } else {
            JOptionPane.showMessageDialog(this, "Could not complete deletion.");
        }
    }

    /**
     * Reloads the employee data table.
     */
    public void refreshTable() {
        loadEmployeeData();
    }
}

/**
 * Displays employee details and allows salary computation input.
 */
class EmployeeDetailFrame extends JFrame {
    private String employeeId;
    private String employeeName;
    private JTextArea detailArea;
    private JTextField monthField;

    public EmployeeDetailFrame(String empId, String empName) {
        this.employeeId = empId;
        this.employeeName = empName;
        setTitle("Employee Details");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        add(new JScrollPane(detailArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("Month (YYYY-MM):"));
        monthField = new JTextField(10);
        JButton computeBtn = new JButton("Compute");
        bottomPanel.add(monthField);
        bottomPanel.add(computeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadEmployeeData();

        computeBtn.addActionListener(e -> computeSalary());
    }

    /**
     * Loads and displays employee info based on employee ID.
     */
    private void loadEmployeeData() {
        try (Scanner scanner = new Scanner(new File("employees.csv"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(employeeId + ",")) {
                    detailArea.setText("Employee Info:\n" + line + "\n");
                    return;
                }
            }
        } catch (IOException e) {
            detailArea.setText("Error loading employee data.");
        }
    }
    
    /**
     * Computes salary (currently static) and displays it.
     */
    private void computeSalary() {
        String month = monthField.getText().trim();
        if (month.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter month for salary computation.");
            return;
        }
        detailArea.append("\nPayroll for " + employeeName + " (ID: " + employeeId + ") in " + month + ":\nGross: 5000.00\nNet: 4500.00");
    }
}

/**
 * UI form for adding a new employee.
 */
class NewEmployeeFrame extends JFrame {
    private JTextField[] fields = new JTextField[7];
    private EmployeeListFrame parent;

    public NewEmployeeFrame(EmployeeListFrame parent) {
        this.parent = parent;
        setTitle("Add New Employee");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(8, 2));

        String[] labels = {"Employee ID", "Last Name", "First Name", "SSS No", "PhilHealth No", "TIN", "Pag-IBIG No"};
        for (int i = 0; i < labels.length; i++) {
            add(new JLabel(labels[i]));
            fields[i] = new JTextField();
            add(fields[i]);
        }

        JButton submit = new JButton("Submit");
        submit.addActionListener(e -> addEmployee());
        add(new JLabel());
        add(submit);
    }

    /**
     * Gathers input, validates, and appends a new employee record.
     */
    private void addEmployee() {
        StringBuilder sb = new StringBuilder();
        for (JTextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            sb.append(field.getText().trim()).append(",");
        }

        sb.setLength(sb.length() - 1); // Remove trailing comma

        try (FileWriter fw = new FileWriter("employees.csv", true)) {
            fw.write(sb.toString() + "\n");
            JOptionPane.showMessageDialog(this, "Employee added.");
            parent.refreshTable();
            dispose(); // Close the add employee window
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data.");
        }
    }
}
