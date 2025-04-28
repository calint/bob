package db.test;

import java.sql.ResultSet;
import java.sql.Statement;

import db.Db;

public class jdbc_select_books extends TestCase {
    @Override
    public void doRun() throws Throwable {
        final Statement stmt = Db.currentTransaction().getJdbcStatement();
        final int nreq = 100;
        final String sql = "select t1.* from Book as t1 limit 0,1000000";
        for (int i = 1; i <= nreq; i++) {
            System.out.println(sql);
            final ResultSet rs = stmt.executeQuery(sql);
            final int ncols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int j = 0; j < ncols; j++) {
                    rs.getObject(j + 1);
                }
            }
            rs.close();
            System.out.println("requests: " + i);
        }
    }
}