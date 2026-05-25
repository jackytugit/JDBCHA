package hademo;

import oracle.jdbc.OracleConnection;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ACFCFDemo
{

    public static void main(String[] args) throws Exception {
        String serviceName = "acservice";
        String RAC_URL = "jdbc:oracle:thin:@cluster01-scan.example.com:1521/" + serviceName;

        String USER = "hr";
        String PASSWORD = "oracle_4U";
        int POOL_SIZE = 1;

        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionPoolName("ACFCFSamplePool");
        pds.setConnectionFactoryClassName("oracle.jdbc.replay.OracleDataSourceImpl");
        pds.setURL(RAC_URL);
        pds.setUser(USER);
        pds.setPassword(PASSWORD);
        pds.setInitialPoolSize(POOL_SIZE);
        pds.setMinPoolSize(POOL_SIZE);
        pds.setMaxPoolSize(POOL_SIZE);

        // FCF is disabled by default; enable it explicitly.
        pds.setFastConnectionFailoverEnabled(true);
        pds.setONSConfiguration("nodes=host01:6200,host02:6200,host03:6200");

        try (Connection conn = pds.getConnection()) {
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