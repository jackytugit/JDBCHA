package hademo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.ValidConnection;

public class FCFDemo
{
//    private static final String RAC_URL =
//            "jdbc:oracle:thin:@(DESCRIPTION="
//                    + "  (LOAD_BALANCE=on)"
//                    + "  (FAILOVER=on)"
//                    + "  (ADDRESS=(PROTOCOL=TCP)(HOST=host01-vip.example.com)(PORT=1521))"
//                    + "  (ADDRESS=(PROTOCOL=TCP)(HOST=host02-vip.example.com)(PORT=1521))"
//                    + "  (ADDRESS=(PROTOCOL=TCP)(HOST=host03-vip.example.com)(PORT=1521))"
//                    + "  (CONNECT_DATA=(SERVICE_NAME=orclpdb1))"
//                    + ")";

    private static final String RAC_URL = "jdbc:oracle:thin:@cluster01-scan.example.com:1521/orclpdb1";

    private static final String USER = "system";
    private static final String PASSWORD = "oracle_4U";
    private static int POOL_SIZE = 10;
    private static Connection[] connections = new Connection[POOL_SIZE];

    public static void main(String[] args) throws Exception
    {
        demoFcfWithUcp();
    }

    private static void demoFcfWithUcp() throws SQLException
    {
        System.out.println("=== FCF demo ===");

        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionPoolName("FCFSamplePool");
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        //pds.setConnectionFactoryClassName("oracle.jdbc.datasource.impl.OracleDataSource");
        pds.setURL(RAC_URL);
        pds.setUser(USER);
        pds.setPassword(PASSWORD);
        pds.setInitialPoolSize(POOL_SIZE);
        pds.setMinPoolSize(POOL_SIZE);
        pds.setMaxPoolSize(POOL_SIZE);

        // FCF is disabled by default; enable it explicitly.
        pds.setFastConnectionFailoverEnabled(true);

        // Example remote ONS config for RAC nodes.
        // Replace hosts/ports with your ONS endpoints.
        pds.setONSConfiguration("nodes=host01:6200,host02:6200,host03:6200");

        // 1st iteration before instance shutdown
        for (int i =0; i<POOL_SIZE; i++)
        {
            System.out.printf("%d: Connecting to database...", i+1);
            connections[i] = pds.getConnection();
            try (Statement stmt = connections[i].createStatement();
                 ResultSet rs = stmt.executeQuery("select sys_context('USERENV','INSTANCE_NAME') from dual"))
            {

                if (connections[i] instanceof ValidConnection) {
                    System.out.println("Pool connection valid? "
                            + ((ValidConnection) connections[i]).isValid());
                }

                while (rs.next())
                {
                    System.out.println("Pool is using instance=" + rs.getString(1));
                }
            }
            catch (SQLException ex) {
                System.out.println("FCF caught SQLException: " + ex.getMessage());
                throw ex;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (Connection connection : connections)
        {
            connection.close();
        }

        System.out.println("shutdown a instance now");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        // 2nd iteration after instance shutdown
        for (int i =0; i<POOL_SIZE; i++)
        {
            System.out.printf("%d: Connecting to database...", i+1);
            connections[i] = pds.getConnection();
            try (Statement stmt = connections[i].createStatement();
                 ResultSet rs = stmt.executeQuery("select sys_context('USERENV','INSTANCE_NAME') from dual"))
            {

                if (connections[i] instanceof ValidConnection) {
                    System.out.println("Pool connection valid? "
                            + ((ValidConnection) connections[i]).isValid());
                }

                while (rs.next())
                {
                    System.out.println("Pool is using instance=" + rs.getString(1));
                }
            }
            catch (SQLException ex) {
                System.out.println("FCF caught SQLException: " + ex.getMessage());
                throw ex;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        for (Connection connection : connections)
        {
            connection.close();
        }
    }

}
