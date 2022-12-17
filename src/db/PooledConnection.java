package db;

import java.sql.Connection;

final class PooledConnection {
	private final Connection con;
	private final long created_ms;

	public PooledConnection(final Connection con) {
		this.con = con;
		created_ms = System.currentTimeMillis();
	}

	public long getAgeInMs() {
		return System.currentTimeMillis() - created_ms;
	}

	public Connection getConnection() {
		return con;
	}

}
