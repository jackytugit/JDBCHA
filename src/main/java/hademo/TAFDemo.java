package hademo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleOCIFailover;
import oracle.jdbc.pool.OracleDataSource;


public class TAFDemo
{
    //private static final String TAF_URL = "jdbc:oracle:oci:@ftselect";
    private static final String TAF_URL = "jdbc:oracle:oci:@prmy";
    private static final String USER = "hr";
    private static final String PASSWORD = "oracle_4U";

    public static void main(String args[]) throws Exception
    {
        demoTafWithOci();
    }

    private static void demoTafWithOci() throws SQLException
    {
        System.out.println("=== TAF demo ===");

        OracleDataSource ods = new OracleDataSource();
        ods.setURL(TAF_URL);
        ods.setUser(USER);
        ods.setPassword(PASSWORD);

        try (Connection raw = ods.getConnection())
        {
            OracleConnection conn = raw.unwrap(OracleConnection.class);

            conn.registerTAFCallback(
                new OracleOCIFailover()
                {
                    @Override
                    public int callbackFn(Connection c, Object ctxt, int type, int event)
                    {
                        String typeName = tafTypeName(type);
                        String eventName = tafEventName(event);
                        System.out.printf("TAF callback: type=%s event=%s%n", typeName, eventName);

                        // Simple sample policy:
                        // retry on temporary error, abort on abort, otherwise continue.
                        switch (event) {
                            case OracleOCIFailover.FO_BEGIN:
                                System.out.println("failover begins");
                                return OracleOCIFailover.FO_NONE;
                                //break;
                            case OracleOCIFailover.FO_END:
                                System.out.println("failover ends");
                                return OracleOCIFailover.FO_NONE;
                            //break;
                            case OracleOCIFailover.FO_REAUTH:
                                System.out.println("TAF re-auth");
                                return OracleOCIFailover.FO_NONE;
                            case OracleOCIFailover.FO_ERROR:
                                System.out.println("failover error, about to retry");
                                try
                                {
                                    Thread.sleep(1000);
                                }
                                catch (InterruptedException e)
                                {
                                    return OracleOCIFailover.FO_ABORT;
                                }
                                break;
                            case OracleOCIFailover.FO_ABORT:
                                System.out.println("failover aborts");
                                return OracleOCIFailover.FO_ABORT;
                        }
                        return OracleOCIFailover.FO_RETRY;
                    }
                }, null);


            String sql = "select e.last_name, j.job_title, d.department_name " +
                    "from employees e join jobs j using (job_id) join departments d using (department_id) " +
                    "order by 3, 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql))
            {
                while (rs.next())
                {
                    // System.out.printf("name: %s, job: %s, department: %s, instance: %s\n",
                    System.out.printf("name: %s, job: %s, department: %s\n",
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3));
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static String tafTypeName(int type)
    {
        switch (type) {
            case OracleOCIFailover.FO_SESSION: return "FO_SESSION";
            case OracleOCIFailover.FO_SELECT:  return "FO_SELECT";
            case OracleOCIFailover.FO_NONE:    return "FO_NONE";
            default:                           return "UNKNOWN_TYPE";
        }
    }

    private static String tafEventName(int event)
    {
        switch (event) {
            case OracleOCIFailover.FO_BEGIN:        return "FO_BEGIN";
            case OracleOCIFailover.FO_END:          return "FO_END";
            case OracleOCIFailover.FO_ABORT:        return "FO_ABORT";
            case OracleOCIFailover.FO_REAUTH:       return "FO_REAUTH";
            case OracleOCIFailover.FO_ERROR:        return "FO_ERROR";
            case OracleOCIFailover.FO_RETRY:        return "FO_RETRY";
            case OracleOCIFailover.FO_EVENT_UNKNOWN:return "FO_EVENT_UNKNOWN";
            default:                                return "UNKNOWN_EVENT";
        }
    }

}
