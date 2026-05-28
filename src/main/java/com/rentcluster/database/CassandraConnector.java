package com.rentcluster.database;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;

public class CassandraConnector {
    private CqlSession session;

    public void connect() {
        System.out.println("Connecting to the Cassandra cluster...");

        // 1. Connect globally WITHOUT specifying a keyspace initially
        // This prevents crashes if the keyspace doesn't exist yet.
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                .withLocalDatacenter("dc1")
                .build();

        System.out.println("Connected successfully! Running Auto-Initialization...");

        // 2. Create the Keyspace if it doesn't exist (Replication factor 3 for distributed architecture)
        session.execute("CREATE KEYSPACE IF NOT EXISTS rent_cluster " +
                "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'};");

        // 3. Switch to our specific keyspace
        session.execute("USE rent_cluster;");

        // 4. Create Tables if they don't exist
        session.execute("CREATE TABLE IF NOT EXISTS properties (" +
                "property_id text PRIMARY KEY);");

        session.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                "property_id text, " +
                "reservation_date date, " +
                "user_id text, " +
                "status text, " +
                "PRIMARY KEY (property_id, reservation_date));");

        // 5. Insert default properties to ensure the UI dropdowns are populated
        // Using IF NOT EXISTS ensures we don't unnecessarily overwrite data on every restart
        String[] defaultProperties = {"Villa_1", "Flat_A", "Suite_5", "Penthouse_9", "Cabin_3"};
        for (String prop : defaultProperties) {
            session.execute("INSERT INTO properties (property_id) VALUES ('" + prop + "') IF NOT EXISTS;");
        }

        System.out.println("Auto-Initialization complete! Database is fully structured and ready.");
    }

    public CqlSession getSession() {
        return session;
    }
}