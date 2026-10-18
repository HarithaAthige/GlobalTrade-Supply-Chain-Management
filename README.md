# GlobalTrade Supply Chain Management System



A Jakarta EE enterprise application for managing global supply-chain operations. The system supports vendor, inventory, and shipment management while demonstrating core Enterprise JavaBeans (EJB) concepts including timer services, transaction management, interceptors, declarative security, exception handling, and automated testing.



## Features



- Vendor management with performance tracking and status control

- Inventory management with stock-level and reorder-threshold monitoring

- Shipment tracking, customs-deadline handling, and delivery-status updates

- Programmatic and declarative EJB timer services

- Container-Managed Transactions (CMT) and a Business-Managed Transaction (BMT) example

- Audit logging and performance-monitoring interceptors

- Role-based access control using WildFly Elytron and HTTP Basic authentication

- Custom application exceptions for invalid states and missing records

- Automated testing using JUnit 5 and Arquillian



## Technology Stack



- Java 17

- Jakarta EE 10

- Enterprise JavaBeans (EJB)

- WildFly 31

- Maven multi-module project

- MySQL

- JUnit 5 and Arquillian



## Project Structure



```text

supply-chain-parent/

├── supply-chain-ejb-client/   # Remote interfaces, DTOs, and exceptions

├── supply-chain-ejb/          # EJB services, entities, timers, and interceptors

├── supply-chain-web/          # Web test servlets

└── supply-chain-ear/          # Enterprise archive packaging module

```



## Requirements



Before running the project, ensure that you have:



- JDK 17

- WildFly 31 or a compatible Jakarta EE application server

- MySQL Server

- Maven or an IDE with Maven support

- A MySQL database named `globaltrade\_scm`

- A configured WildFly datasource named `java:/SupplyChainDS`



## Build and Deployment



1. Configure the MySQL database and WildFly datasource.

2. Build the Maven project from the parent directory.

3. Deploy the generated EAR file to WildFly.

4. Access the application test endpoints.





## Security



The application uses WildFly Elytron for authentication and role-based authorization. Access to system operations is controlled through roles such as:



- `SYSTEM_ADMIN`

- `LOGISTICS_COORDINATOR`

- `WAREHOUSE_MANAGER`

- `CUSTOMS_AGENT`

- `VENDOR_REP`



## Testing



The project includes automated tests with JUnit 5 and Arquillian. Tests cover deployment, authentication and authorization, inventory and shipment operations, custom exceptions, and transaction rollback behaviour.



## Project Showcase



Check out the project demonstration on LinkedIn: (https://lnkd.in/p/gvDRdx2j)

