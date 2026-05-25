package hademo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.replay.OracleDataSourceImpl;

public class ACDemo {

    public static void main(String[] args) throws Exception {
        //String serviceName = "ftselect";
        String serviceName = "acservice";
        String url = "jdbc:oracle:thin:@cluster01-scan.example.com:1521/" + serviceName;

        String user = "hr";
        String password = "oracle_4U";

        OracleDataSourceImpl ods = new OracleDataSourceImpl();
        ods.setURL(url);
        ods.setUser(user);
        ods.setPassword(password);

        try (Connection conn = ods.getConnection()) {
            conn.setAutoCommit(false);

            OracleConnection oracleConn = conn.unwrap(OracleConnection.class);
            oracleConn.beginRequest();

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO emps select * from employees where job_id = ?")) {
                    ps.setString(1, "SA_REP");
                    ps.executeUpdate();
                }
                System.out.println("Shutdown the instance where " + serviceName + " is running");
                Thread.sleep(30000);
                conn.commit();
                System.out.println("AC work completed and committed.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                oracleConn.endRequest();
            }
        }
    }
}