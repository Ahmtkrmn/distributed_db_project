package com.rentcluster.gui;

import com.rentcluster.database.ReservationDAO;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private final ReservationDAO dao;
    private String[] dynamicProperties;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // Global UI Components so they can be refreshed from anywhere
    private JTextArea dashboardDisplayArea;
    private JComboBox<String> dashboardPropertyBox;
    private BookedDateEvaluator startEvaluator;
    private BookedDateEvaluator endEvaluator;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    public MainFrame(ReservationDAO dao) {
        this.dao = dao;
        List<String> propList = dao.getAllProperties();
        dynamicProperties = propList.toArray(new String[0]);

        setTitle("RentCluster - Distributed Management System");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Operations", createOperationsPanel());
        tabbedPane.addTab("Stress Tests", createStressTestPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // Central Refresh Method
    // Central Refresh Method (Safe against Null Pointers)
    private void refreshSystem() {
        // 1. Refresh the text list on the dashboard (ONLY if it has been created)
        if (dashboardDisplayArea != null) {
            dashboardDisplayArea.setText(dao.viewReservations());
        }

        // 2. Refresh the calendar colors dynamically (ONLY if components are ready)
        if (dashboardPropertyBox != null && dashboardPropertyBox.getSelectedItem() != null
                && startEvaluator != null && endEvaluator != null) {

            String selectedProp = dashboardPropertyBox.getSelectedItem().toString();
            List<String> bookedDates = dao.getBookedDatesForProperty(selectedProp);

            startEvaluator.setBookedDates(bookedDates);
            endEvaluator.setBookedDates(bookedDates);

            if (startDateChooser != null && startDateChooser.getJCalendar() != null) {
                startDateChooser.getJCalendar().repaint();
            }
            if (endDateChooser != null && endDateChooser.getJCalendar() != null) {
                endDateChooser.getJCalendar().repaint();
            }
        }
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        dashboardPropertyBox = new JComboBox<>(dynamicProperties);
        startDateChooser = new JDateChooser();
        endDateChooser = new JDateChooser();
        JTextField userIdField = new JTextField();
        JButton reserveBtn = new JButton("Reserve Range");

        startEvaluator = new BookedDateEvaluator(new ArrayList<>());
        endEvaluator = new BookedDateEvaluator(new ArrayList<>());

        startDateChooser.getJCalendar().getDayChooser().addDateEvaluator(startEvaluator);
        endDateChooser.getJCalendar().getDayChooser().addDateEvaluator(endEvaluator);

        dashboardPropertyBox.addActionListener(e -> refreshSystem());
        if (dashboardPropertyBox.getItemCount() > 0) dashboardPropertyBox.setSelectedIndex(0);

        formPanel.add(new JLabel("Property ID:"));
        formPanel.add(dashboardPropertyBox);
        formPanel.add(new JLabel("User ID:"));
        formPanel.add(userIdField);

        formPanel.add(new JLabel("Start Date:"));
        formPanel.add(startDateChooser);
        formPanel.add(new JLabel("End Date:"));
        formPanel.add(endDateChooser);

        JPanel btnPanel = new JPanel();
        btnPanel.add(reserveBtn);

        dashboardDisplayArea = new JTextArea();
        dashboardDisplayArea.setEditable(false);
        dashboardDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(dashboardDisplayArea);

        reserveBtn.addActionListener(e -> {
            if (dashboardPropertyBox.getSelectedItem() != null && startDateChooser.getDate() != null && endDateChooser.getDate() != null) {
                String propId = dashboardPropertyBox.getSelectedItem().toString();
                String startStr = sdf.format(startDateChooser.getDate());
                String endStr = sdf.format(endDateChooser.getDate());
                String user = userIdField.getText().trim();

                if (!user.isEmpty()) {
                    boolean success = dao.makeReservation(propId, startStr, endStr, user);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Reservation Successful!");
                    } else {
                        JOptionPane.showMessageDialog(this, "WARNING: Some or all selected dates are already booked!", "Booking Error", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshSystem(); // Instantly update UI
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a User ID.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill all fields and select dates.");
            }
        });

        refreshSystem();

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(formPanel, BorderLayout.CENTER);
        northPanel.add(btnPanel, BorderLayout.EAST);
        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 5, 5, 5));

        JComboBox<String> opPropertyIdBox = new JComboBox<>(dynamicProperties);
        JDateChooser targetDateChooser = new JDateChooser();
        JTextField newUserIdField = new JTextField();
        JButton updateBtn = new JButton("Update User");
        JButton cancelBtn = new JButton("Cancel Reservation");

        formPanel.add(new JLabel("Property ID:"));
        formPanel.add(opPropertyIdBox);
        formPanel.add(new JLabel("Target Date:"));
        formPanel.add(targetDateChooser);
        formPanel.add(new JLabel(""));

        formPanel.add(new JLabel("User ID:"));
        formPanel.add(newUserIdField);
        formPanel.add(updateBtn);
        formPanel.add(cancelBtn);
        formPanel.add(new JLabel(""));

        panel.add(formPanel, BorderLayout.NORTH);

        JTextArea feedbackArea = new JTextArea("Operation results will appear here...\n");
        feedbackArea.setEditable(false);
        feedbackArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);

        updateBtn.addActionListener(e -> {
            if (opPropertyIdBox.getSelectedItem() != null && targetDateChooser.getDate() != null) {
                String propId = opPropertyIdBox.getSelectedItem().toString();
                String date = sdf.format(targetDateChooser.getDate());
                String newUser = newUserIdField.getText().trim();

                if (!newUser.isEmpty()) {
                    boolean success = dao.updateReservation(propId, date, newUser);
                    if (success) {
                        feedbackArea.append("SUCCESS - Updated: " + propId + " on " + date + "\n");
                    } else {
                        feedbackArea.append("FAILED - No existing reservation found for " + propId + " on " + date + " to update.\n");
                    }
                    refreshSystem(); // Update Dashboard instantly
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a New User ID.");
                }
            }
        });

        cancelBtn.addActionListener(e -> {
            if (opPropertyIdBox.getSelectedItem() != null && targetDateChooser.getDate() != null) {
                String propId = opPropertyIdBox.getSelectedItem().toString();
                String date = sdf.format(targetDateChooser.getDate());

                boolean success = dao.cancelReservation(propId, date);
                if (success) {
                    feedbackArea.append("SUCCESS - Cancelled: " + propId + " on " + date + "\n");
                } else {
                    feedbackArea.append("FAILED - No existing reservation found for " + propId + " on " + date + " to cancel.\n");
                }
                refreshSystem(); // Update Dashboard instantly
            }
        });

        return panel;
    }

    private JPanel createStressTestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("System Load & Stress Tests (Coming Soon)", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    public void display() {
        setVisible(true);
    }
}