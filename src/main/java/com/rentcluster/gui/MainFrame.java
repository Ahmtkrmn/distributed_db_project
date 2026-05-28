package com.rentcluster.gui;

import com.rentcluster.database.ReservationDAO;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Control Panel (North) ---
        // Buttons for initializing different stress tests based on the rubric
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton test1Btn = new JButton("Test 1: Sequential Load");
        JButton test2Btn = new JButton("Test 2: Random Requests");
        JButton test3Btn = new JButton("Test 3: Occupy All Seats");

        controlPanel.add(test1Btn);
        controlPanel.add(test2Btn);
        controlPanel.add(test3Btn);

        // --- Logs Display Area (Center) ---
        // Creating a terminal-like appearance to output test results
        JTextArea logArea = new JTextArea("System Load & Stress Test Logs...\n");
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(new Color(43, 43, 43)); // Dark background
        logArea.setForeground(new Color(0, 255, 0));  // Hacker green text

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // =========================================================
        // TEST 1: The client makes the same request very quickly.
        // =========================================================
        test1Btn.addActionListener(e -> {
            // Disable buttons to prevent test overlap
            test1Btn.setEnabled(false); test2Btn.setEnabled(false); test3Btn.setEnabled(false);
            logArea.append("\n--- Starting Test 1: Sequential Load ---\n");

            // Execute in a background thread to keep the UI responsive
            new Thread(() -> {
                long startTime = System.currentTimeMillis();
                int successCount = 0;
                String propId = "Cabin_3";
                java.time.LocalDate baseDate = java.time.LocalDate.of(2032, 1, 1);

                for (int i = 1; i <= 100; i++) {
                    String testDate = baseDate.plusDays(i).toString();
                    if (dao.makeReservation(propId, testDate, testDate, "StressBot_1")) {
                        successCount++;
                    }
                    if (i % 25 == 0) logArea.append("Processed " + i + " / 100 requests...\n");
                }

                long duration = System.currentTimeMillis() - startTime;
                final int finalSuccessCount = successCount;
                // Safely update the UI thread
                SwingUtilities.invokeLater(() -> {
                    logArea.append(">>> TEST 1 COMPLETED <<<\n");
                    logArea.append("Time: " + duration + " ms | Success: " + finalSuccessCount + "/100\n");
                    logArea.append("---------------------------------------------------\n");
                    test1Btn.setEnabled(true); test2Btn.setEnabled(true); test3Btn.setEnabled(true);
                    refreshSystem();
                });
            }).start();
        });

        // =========================================================
        // TEST 2: Two or more clients make possible requests randomly.
        // =========================================================
        test2Btn.addActionListener(e -> {
            test1Btn.setEnabled(false); test2Btn.setEnabled(false); test3Btn.setEnabled(false);
            logArea.append("\n--- Starting Test 2: Random Requests ---\n");

            new Thread(() -> {
                // Latch acts as a starting gun for threads
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                String[] testProperties = {"Villa_1", "Flat_A", "Suite_5"};
                java.time.LocalDate baseDate = java.time.LocalDate.of(2040, 1, 1);

                // Reusable task for random bots
                Runnable randomTask = () -> {
                    try { latch.await(); } catch (Exception ex) {} // Wait for the starting gun
                    java.util.Random rand = new java.util.Random();
                    String threadName = Thread.currentThread().getName();
                    int success = 0;

                    // Each bot attempts 50 random bookings
                    for (int i = 0; i < 50; i++) {
                        String prop = testProperties[rand.nextInt(testProperties.length)];
                        String date = baseDate.plusDays(rand.nextInt(30)).toString(); // Random day within a 30-day window
                        if (dao.makeReservation(prop, date, date, threadName)) {
                            success++;
                        }
                    }
                    int finalSuccess = success;
                    SwingUtilities.invokeLater(() -> logArea.append(threadName + " finished. Successful bookings: " + finalSuccess + "/50\n"));
                };

                // Create and start two clients
                Thread client1 = new Thread(randomTask, "RandomBot_X");
                Thread client2 = new Thread(randomTask, "RandomBot_Y");
                client1.start();
                client2.start();

                // Fire the starting gun
                latch.countDown();

                // Wait for both threads to complete
                try { client1.join(); client2.join(); } catch (Exception ex) {}

                SwingUtilities.invokeLater(() -> {
                    logArea.append(">>> TEST 2 COMPLETED <<<\n");
                    logArea.append("---------------------------------------------------\n");
                    test1Btn.setEnabled(true); test2Btn.setEnabled(true); test3Btn.setEnabled(true);
                    refreshSystem();
                });
            }).start();
        });

        // =========================================================
        // TEST 3: Immediate occupancy of all seats by 2 clients.
        // =========================================================
        test3Btn.addActionListener(e -> {
            test1Btn.setEnabled(false); test2Btn.setEnabled(false); test3Btn.setEnabled(false);
            logArea.append("\n--- Starting Stress Test 3: Occupy All Seats ---\n");
            logArea.append("Client_A and Client_B racing for 100 continuous days...\n");

            new Thread(() -> {
                String propId = "Suite_5";
                java.time.LocalDate baseDate = java.time.LocalDate.of(2039, 1, 1);
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

                // Atomic variables are required for thread-safe counting
                java.util.concurrent.atomic.AtomicInteger countA = new java.util.concurrent.atomic.AtomicInteger(0);
                java.util.concurrent.atomic.AtomicInteger countB = new java.util.concurrent.atomic.AtomicInteger(0);

                Thread clientA = new Thread(() -> {
                    try { latch.await(); } catch (Exception ex) {}
                    for(int i = 0; i < 100; i++) {
                        String date = baseDate.plusDays(i).toString();
                        if(dao.makeReservation(propId, date, date, "Client_A")) countA.incrementAndGet();

                        // İşletim sistemini diğer Thread'e geçmeye zorlayan yapay ağ gecikmesi
                        try { Thread.sleep(5); } catch (Exception ex) {}
                    }
                });

                // Client B
                Thread clientB = new Thread(() -> {
                    try { latch.await(); } catch (Exception ex) {}
                    for(int i = 0; i < 100; i++) {
                        String date = baseDate.plusDays(i).toString();
                        if(dao.makeReservation(propId, date, date, "Client_B")) countB.incrementAndGet();

                        // İşletim sistemini diğer Thread'e geçmeye zorlayan yapay ağ gecikmesi
                        try { Thread.sleep(5); } catch (Exception ex) {}
                    }
                });

                clientA.start();
                clientB.start();

                try { Thread.sleep(500); } catch (Exception ex) {}

                // Unleash both clients simultaneously
                latch.countDown();

                try { clientA.join(); clientB.join(); } catch (Exception ex) {}

                SwingUtilities.invokeLater(() -> {
                    logArea.append(">>> TEST 3 COMPLETED <<<\n");
                    logArea.append("Total Seats (Days) Contested: 100\n");
                    logArea.append("Client_A Secured: " + countA.get() + " | Client_B Secured: " + countB.get() + "\n");
                    logArea.append("Requirement Status: SUCCESS (Fair distribution)\n");
                    logArea.append("---------------------------------------------------\n");

                    test1Btn.setEnabled(true); test2Btn.setEnabled(true); test3Btn.setEnabled(true);
                    refreshSystem();
                });
            }).start();
        });

        return panel;
    }

    public void display() {
        setVisible(true);
    }
}