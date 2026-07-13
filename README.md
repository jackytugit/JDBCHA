# JDBCHA — Oracle JDBC High Availability Demo

A Java demo project showing how to build **highly available database connectivity** to Oracle Database using the Oracle JDBC driver, **Universal Connection Pool (UCP)**, and **Fast Application Notification (FAN)** via Oracle Notification Service (ONS).

The goal is to demonstrate how a Java application can survive database outages, RAC instance failures, and planned maintenance with minimal disruption — connections are pooled, failed nodes are detected proactively through FAN events, and workload is rebalanced automatically.

## Features

- **Connection pooling with UCP** — efficient reuse of connections, pool sizing, and connection validation.
- **Fast Application Notification (FAN)** — the pool subscribes to ONS events so it learns about node DOWN/UP and service relocation events immediately, instead of waiting for TCP timeouts.
- **Fast Connection Failover (FCF)** — dead connections are purged from the pool as soon as a DOWN event arrives, and new work is routed to surviving instances.
- **Runtime Load Balancing** — FAN load advisories let UCP distribute borrowed connections across RAC instances based on real service performance.

## Tech Stack

| Component | Version |
|---|---|
| Java | 21 |
| Build tool | Apache Maven |
| Oracle JDBC driver (`ojdbc17`) | 23.26.1.0.0 (23ai) |
| Universal Connection Pool (`ucp17`) | 23.26.1.0.0 |
| SimpleFAN (`simplefan`) | 23.26.1.0.0 |
| ONS client (`ons`) | 23.26.1.0.0 |

Dependency versions are managed centrally through the Oracle `ojdbc-bom`.

## Project Structure

```
JDBCHA/
├── pom.xml                     # Maven build file with Oracle HA dependencies
├── src/
│   └── main/
│       └── java/
│           └── hademo/         # Demo source code
└── .idea/                      # IntelliJ IDEA project files
```

## Prerequisites

- **JDK 21** or later
- **Apache Maven** 3.8+
- Access to an **Oracle Database** — ideally a RAC cluster, Data Guard configuration, or Autonomous Database, since HA features are best demonstrated against a multi-instance/service-based setup. A single instance works for basic pooling.
- For FAN/FCF: ONS reachable from the client (auto-configured with 23ai drivers when connecting to a database service that publishes FAN events).

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/jackytugit/JDBCHA.git
   cd JDBCHA
   ```

2. **Configure the database connection**

   Update the connection URL, service name, and credentials in the demo class(es) under `src/main/java/hademo/` to point at your database. For HA scenarios, connect to a **database service** (not a SID) using a long-format or LDAP/tnsnames URL, e.g.:

   ```
   jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=host1)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=host2)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=my_ha_service)))
   ```

3. **Build**

   ```bash
   mvn clean package
   ```

4. **Run**

   Run the main class in the `hademo` package from your IDE, or with Maven:

   ```bash
   mvn exec:java -Dexec.mainClass="hademo.<MainClassName>"
   ```

## Testing High Availability

With the demo running against a RAC cluster or service-based setup, try:

- Stopping one instance (`srvctl stop instance ...`) and watching the pool fail over without application errors.
- Relocating the service to another node and observing FAN-driven rebalancing.
- Performing a planned maintenance drain and confirming in-flight work completes.

## References

- [Oracle Universal Connection Pool Developer's Guide](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjucp/)
- [Oracle JDBC Developer's Guide](https://docs.oracle.com/en/database/oracle/oracle-database/23/jjdbc/)
- [Application Continuity and FAN](https://www.oracle.com/database/technologies/high-availability.html)

## License

No license has been specified for this project.
