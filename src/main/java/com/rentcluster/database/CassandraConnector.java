package com.rentcluster.database;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;

public class CassandraConnector {
    // Main session object to communicate with the database
    private CqlSession session;

    public void connect() {
        // Connecting to Node 1 on Docker (127.0.0.1:9042)
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                .withLocalDatacenter("dc1")
                .withKeyspace("rent_cluster") // Keyspace we created earlier via cqlsh
                .build();
        System.out.println("Successfully connected to the Cassandra cluster!");
    }

    public CqlSession getSession() {
        return session;
    }

    public void close() {
        if (session != null) {
            session.close();
            System.out.println("Connection closed safely.");
        }
    }
}