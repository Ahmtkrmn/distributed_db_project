package com.rentcluster;

import com.rentcluster.database.CassandraConnector;
import com.rentcluster.database.ReservationDAO;
import com.rentcluster.gui.MainFrame;
import javax.swing.SwingUtilities;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting the system...");
        Locale.setDefault(Locale.ENGLISH);
        CassandraConnector dbConnector = new CassandraConnector();

        try {
            // Establishing the database connection
            dbConnector.connect();
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return; // Stop execution if we can't connect to the database
        }

        // Launching the GUI safely on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Passing the active session to our DAO
            ReservationDAO dao = new ReservationDAO(dbConnector.getSession());

            // Initializing and displaying the main interface
            MainFrame mainFrame = new MainFrame(dao);
            mainFrame.display();
        });

        // IMPORTANT: We do NOT call dbConnector.close() here anymore!
        // The session must remain open as long as the GUI is running.
    }
}