package db.test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import db.Db;
import db.DbTransaction;

// prints column types of user
public class print_column_types extends TestCase {
	@Override
	protected boolean isRunWithoutCache() {
		return false;
	}

	@Override
	public void doRun() throws Throwable {
		final DbTransaction tn = Db.currentTransaction();
		final Statement stmt = tn.getJdbcStatement();
		final String sql = "select t1.* from TestObj as t1 limit 0,1";
		System.out.println(sql);
		final ResultSet rs = stmt.executeQuery(sql);
		final ResultSetMetaData rsm = rs.getMetaData();
		final int ncols = rsm.getColumnCount();
		for (int i = 0; i < ncols; i++) {
			System.out.println("  " + (i + 1) + ": " + rsm.getColumnName(i + 1) + " " + rsm.getColumnTypeName(i + 1)
					+ " " + rsm.getColumnClassName(i + 1));
		}
		rs.close();
	}

}