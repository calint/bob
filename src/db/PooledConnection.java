//
// reviewed: 2024-08-05
//           2025-04-28
//
package db;

import java.sql.Connection;

final class PooledConnection {

    private final Connection con;
    private final long createdMs;

    public PooledConnection(final Connection con) {
        this.con = con;
        createdMs = System.currentTimeMillis();
    }

    public long getAgeInMs() {
        return System.currentTimeMillis() - createdMs;
    }

    public Connection getConnection() {
        return con;
    }

}
