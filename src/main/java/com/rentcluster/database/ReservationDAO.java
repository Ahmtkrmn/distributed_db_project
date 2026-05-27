package com.rentcluster.database;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private final CqlSession session;

    public ReservationDAO(CqlSession session) {
        this.session = session;
    }

    public List<String> getAllProperties() {
        List<String> properties = new ArrayList<>();
        String query = "SELECT property_id FROM properties";
        ResultSet rs = session.execute(query);
        for (Row row : rs) {
            properties.add(row.getString("property_id"));
        }
        return properties;
    }

    public List<String> getBookedDatesForProperty(String propertyId) {
        List<String> bookedDates = new ArrayList<>();
        String query = String.format("SELECT reservation_date FROM reservations WHERE property_id = '%s'", propertyId);
        ResultSet rs = session.execute(query);
        for (Row row : rs) {
            bookedDates.add(row.getLocalDate("reservation_date").toString());
        }
        return bookedDates;
    }

    // Returns true if ALL days were successfully booked. Returns false if ANY day was already booked.
    public boolean makeReservation(String propertyId, String startDateStr, String endDateStr, String userId) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

            if (daysBetween < 0) return false;

            boolean allSuccess = true;

            for (int i = 0; i <= daysBetween; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                String query = String.format(
                        "INSERT INTO reservations (property_id, reservation_date, user_id, status) VALUES ('%s', '%s', '%s', 'BOOKED') IF NOT EXISTS",
                        propertyId, currentDate.toString(), userId
                );
                ResultSet rs = session.execute(query);

                // wasApplied() checks if the IF NOT EXISTS condition passed
                if (!rs.wasApplied()) {
                    allSuccess = false;
                }
            }
            return allSuccess;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String viewReservations() {
        String query = "SELECT * FROM reservations";
        ResultSet rs = session.execute(query);
        StringBuilder sb = new StringBuilder();
        for (Row row : rs) {
            sb.append("Property ID: ").append(row.getString("property_id"))
                    .append(" | Date: ").append(row.getLocalDate("reservation_date"))
                    .append(" | User: ").append(row.getString("user_id"))
                    .append(" | Status: ").append(row.getString("status"))
                    .append("\n");
        }
        return sb.length() > 0 ? sb.toString() : "No reservations found.";
    }

    // Added IF EXISTS and explicit status update to prevent 'null' status issues
    public boolean updateReservation(String propertyId, String reservationDate, String newUserId) {
        String query = String.format(
                "UPDATE reservations SET user_id = '%s', status = 'BOOKED' WHERE property_id = '%s' AND reservation_date = '%s' IF EXISTS",
                newUserId, propertyId, reservationDate
        );
        ResultSet rs = session.execute(query);
        return rs.wasApplied();
    }

    // Added IF EXISTS to prevent fake successful deletions
    public boolean cancelReservation(String propertyId, String reservationDate) {
        String query = String.format(
                "DELETE FROM reservations WHERE property_id = '%s' AND reservation_date = '%s' IF EXISTS",
                propertyId, reservationDate
        );
        ResultSet rs = session.execute(query);
        return rs.wasApplied();
    }
}