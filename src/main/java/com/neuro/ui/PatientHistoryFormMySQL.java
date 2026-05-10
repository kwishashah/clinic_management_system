package com.neuro.ui;

import com.neuro.dao.PatientDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;

public class PatientHistoryFormMySQL extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(PatientHistoryFormMySQL.class);
    private JTextField txtName, txtMobile, txtAge, txtOccupation;
    private JTextField txtBloodGroup, txtHeight, txtWeight, txtDuration;

    private JTextField txtBP, txtPulse, txtO2, txtTemp;

    private JTextArea txtAddress, txtMainDisease, txtComplications;
    private JTextArea txtSymptoms, txtAllergy, txtRemarks;
    private JTextArea txtPreviousTreatment, txtMedicines, txtDetailedHistory;
    private JTextArea txtExamination, txtReportAnalysis;

    private JTextField txtReportPath;
    private JButton btnUploadReport;

    private JComboBox<String>[] painFields = new JComboBox[18];
    private JComboBox<String> left4th, right4th;

    private JComboBox<String> cmbGender, cmbMarital;

    // ✅ ONLY ONE userId
    private int userId;

    // ✅ Constructor receives userId
    public PatientHistoryFormMySQL(int userId) {

        this.userId = userId;

        logger.info("Opening Patient History Form for userId={}", userId);

        setTitle("Patient History Form");
        setSize(950, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // ===== Fields =====
        txtName = new JTextField(20);
        txtMobile = new JTextField(20);
        txtAge = new JTextField(5);

        cmbGender = new JComboBox<>(new String[]{"Male","Female","Other"});
        cmbMarital = new JComboBox<>(new String[]{"Single","Married"});

        txtAddress = createArea();
        txtOccupation = new JTextField(20);
        txtBloodGroup = new JTextField();

        txtHeight = new JTextField(5);
        txtWeight = new JTextField(5);
        txtDuration = new JTextField();

        txtMainDisease = createArea();
        txtComplications = createArea();
        txtSymptoms = createArea();

        txtPreviousTreatment = createArea();
        txtMedicines = createArea();
        txtDetailedHistory = createArea();
        txtExamination = createArea();

        txtReportAnalysis = createArea();
        txtAllergy = createArea();
        txtRemarks = createArea();

        txtReportPath = new JTextField(20);
        txtReportPath.setEditable(false);

        btnUploadReport = new JButton("Upload Report");

        txtBP = new JTextField(8);
        txtPulse = new JTextField(5);
        txtO2 = new JTextField(5);
        txtTemp = new JTextField(5);

        // ===== Layout =====
        y = addRow(panel, gbc, y, "Name", txtName);
        y = addRow(panel, gbc, y, "Mobile", txtMobile);
        y = addRow(panel, gbc, y, "Age", txtAge);
        y = addRow(panel, gbc, y, "Gender", cmbGender);
        y = addRow(panel, gbc, y, "Marital", cmbMarital);
        y = addRow(panel, gbc, y, "Address", txtAddress);
        y = addRow(panel, gbc, y, "Occupation", txtOccupation);
        y = addRow(panel, gbc, y, "Blood Group", txtBloodGroup);

        JPanel hw = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hw.add(txtHeight); hw.add(new JLabel("cm"));
        hw.add(txtWeight); hw.add(new JLabel("kg"));
        y = addRow(panel, gbc, y, "Height / Weight", hw);

        y = addRow(panel, gbc, y, "Duration", txtDuration);
        y = addRow(panel, gbc, y, "Main Disease", txtMainDisease);
        y = addRow(panel, gbc, y, "Complications", txtComplications);
        y = addRow(panel, gbc, y, "Symptoms", txtSymptoms);

        // ===== Pain Points =====
        JPanel painPanel = new JPanel(new GridLayout(0,2));

        String[] names = {
                "Pan","Gas","Gast","WD","Gal","Spl","Liv","Mu",
                "Rtov","Ltov","Dys","Const","Liv0","Mul0","Follic","Thia","B12","Nia"
        };

        String[] scale = {"0","1","2","3","4"};

        for(int i=0;i<names.length;i++){
            painFields[i] = new JComboBox<>(scale);
            painPanel.add(new JLabel(names[i]));
            painPanel.add(painFields[i]);
        }

        left4th = new JComboBox<>(new String[]{"No","Yes"});
        right4th = new JComboBox<>(new String[]{"No","Yes"});

        painPanel.add(new JLabel("Left 4th"));
        painPanel.add(left4th);
        painPanel.add(new JLabel("Right 4th"));
        painPanel.add(right4th);

        y = addRow(panel, gbc, y, "Pain Points", painPanel);

        // ===== Vitals =====
        JPanel vitals = new JPanel(new FlowLayout(FlowLayout.LEFT));
        vitals.add(new JLabel("BP")); vitals.add(txtBP);
        vitals.add(new JLabel("Pulse")); vitals.add(txtPulse);
        vitals.add(new JLabel("O2")); vitals.add(txtO2);
        vitals.add(new JLabel("Temp")); vitals.add(txtTemp);

        y = addRow(panel, gbc, y, "Vitals", vitals);

        y = addRow(panel, gbc, y, "Previous Treatment", txtPreviousTreatment);
        y = addRow(panel, gbc, y, "Medicines", txtMedicines);
        y = addRow(panel, gbc, y, "Detailed History", txtDetailedHistory);
        y = addRow(panel, gbc, y, "Examination", txtExamination);

        y = addRow(panel, gbc, y, "Report Analysis", txtReportAnalysis);
        y = addRow(panel, gbc, y, "Allergy", txtAllergy);
        y = addRow(panel, gbc, y, "Remarks", txtRemarks);

        // ===== SAVE BUTTON =====
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveData());

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(btnSave, gbc);
    }

    // ================= SAVE =================
    private void saveData() {

        try {

            logger.info(
                    "Saving patient started name={} mobile={} userId={}",
                    txtName.getText(),
                    txtMobile.getText(),
                    userId
            );

            if(txtName.getText().trim().isEmpty()){
                logger.warn("Save blocked: patient name empty");
                JOptionPane.showMessageDialog(this,"Patient name required");
                return;
            }

            Float height =
                    txtHeight.getText().isEmpty()
                            ? null
                            : Float.parseFloat(txtHeight.getText());

            Float weight =
                    txtWeight.getText().isEmpty()
                            ? null
                            : Float.parseFloat(txtWeight.getText());

            logger.debug(
                    "Parsed vitals height={} weight={} bp={}",
                    height,
                    weight,
                    txtBP.getText()
            );


            StringBuilder pain = new StringBuilder();

            for (JComboBox<String> field : painFields) {
                pain.append(field.getSelectedItem()).append(",");
            }

            pain.append("L4=")
                    .append(left4th.getSelectedItem())
                    .append(",");

            pain.append("R4=")
                    .append(right4th.getSelectedItem());

            logger.debug("Pain points captured={}", pain);


            PatientDAO.savePatient(
                    txtName.getText(),
                    txtMobile.getText(),
                    txtAge.getText().isEmpty()
                            ? null
                            : Integer.parseInt(txtAge.getText()),

                    (String) cmbGender.getSelectedItem(),
                    (String) cmbMarital.getSelectedItem(),

                    txtAddress.getText(),
                    txtOccupation.getText(),
                    txtBloodGroup.getText(),

                    height,
                    weight,

                    txtDuration.getText(),
                    txtMainDisease.getText(),
                    txtComplications.getText(),
                    txtSymptoms.getText(),

                    pain.toString(),

                    "", "", "", "", "", "",

                    txtPreviousTreatment.getText(),
                    txtMedicines.getText(),
                    txtDetailedHistory.getText(),
                    txtExamination.getText(),

                    txtBP.getText(),
                    txtPulse.getText(),
                    txtO2.getText(),
                    txtTemp.getText(),

                    userId,

                    txtReportPath.getText(),
                    "",
                    txtReportAnalysis.getText(),
                    txtRemarks.getText(),

                    new java.sql.Timestamp(
                            System.currentTimeMillis()
                    )
            );

            logger.info(
                    "Patient saved successfully name={} userId={}",
                    txtName.getText(),
                    userId
            );

            JOptionPane.showMessageDialog(this,"Saved!");
            dispose();

        }
        catch(NumberFormatException e){
            logger.warn("Invalid numeric input while saving patient", e);
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid age/height/weight values"
            );
        }
        catch(Exception e){
            logger.error(
                    "Patient save failed userId={}",
                    userId,
                    e
            );
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage()
            );
        }
    }
    private JTextArea createArea() {
        JTextArea a = new JTextArea(3,20);
        a.setLineWrap(true);
        return a;
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int y, String label, Component field){
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field instanceof JTextArea ? new JScrollPane(field) : field, gbc);

        return y + 1;
    }
}