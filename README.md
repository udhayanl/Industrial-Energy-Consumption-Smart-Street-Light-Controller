# Industrial Energy Consumption & Smart Street Light Controller

A municipal smart lighting grid management system built using **Spring Boot**, **Java 17/21**, **Maven**, **MySQL**, and **Postman**.

---

## 🌟 Key Features

1. **Smart Street Light & Telemetry Management**:
   - Register street light poles with grid zones (`POST /api/streetlights`).
   - Control LED dimming percentage (0-100%) (`PUT /api/streetlights/{id}/dimming`).
   - Ingest ambient light and power draw telemetry (`POST /api/streetlights/telemetry`).
   - Automated fault ticket generation when power draw is 0W during active/night hours.
   - Live power summary & estimated monthly electricity cost calculation (`GET /api/streetlights/power-summary`).

2. **Double-Entry Financial Accounting (Chart of Accounts)**:
   - **Contact Master**: Electric Utility Provider (Vendor), Maintenance Contractor (Vendor), Municipal Client (Customer).
   - **Product Master**: Smart LED Pole Assembly (Goods), Grid Electricity kWh (Goods), Repair Maintenance Service (Service).
   - **Chart of Accounts Master**:
     - **Assets**: Lighting Grid Infrastructure, Cash & Bank Reserves.
     - **Liabilities**: Power Utility Payables.
     - **Income**: Municipal Grid Service Fee.
     - **Expenses**: Electricity Consumption Expense, Repair & Maintenance Expense.
   - **Double-Entry Journal Entries**: Purchase Journal, Sales Journal, Bank/Cash Journal.

3. **Procurement, Utility Billing & Customer Invoicing**:
   - **Purchase Order (PO)**: Issue PO to electric utility provider or contractor.
   - **Vendor Bill**: Convert PO to Vendor Bill & post double-entry ledger movement.
   - **Customer Invoicing**: Convert Sales Order to Customer Invoice for municipal authority.
   - **Bank Payment Registration**: Settle vendor bills & receive customer payments via Bank.

4. **Municipal Budget & Financial Reporting**:
   - **Analytic Account (Lighting Grid Zone)**: Track planned energy budget vs actual power & repair costs.
   - **Balance Sheet Report**: Grid infrastructure asset value & bank reserves vs utility payables.
   - **Profit & Loss Account**: Municipal service fees minus electricity & repair costs.
   - **Budget Report**: Zone energy consumption budget variance report.

---

## 🚀 How to Run the Application

### Prerequisites
- **Java 17 or Java 21**
- **Maven 3.8+**
- **MySQL Database Server** (running on port `3306`)

### 1. Database Setup (MySQL)
Create the MySQL database (or let Spring Boot automatically create it):
```sql
CREATE DATABASE industrial_energy_db;
```
Configure your MySQL credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/industrial_energy_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

### 2. Build & Run
Run the Maven spring-boot plugin:
```bash
mvn clean spring-boot:run
```
Or run the packaged JAR:
```bash
mvn clean package
java -jar target/industrial-energy-consumption-0.0.1-SNAPSHOT.jar
```

The application automatically seeds initial Master Data (Contacts, Products, Chart of Accounts, Zones, Street Lights) out of the box!

---

## 📬 Postman API Testing

Import the included Postman Collection file into Postman:
📄 `postman_collection.json`

### Key API Endpoints:

| Module | Method | Endpoint | Description |
|---|---|---|---|
| **Street Lights** | `POST` | `/api/streetlights` | Register new street light pole |
| **Street Lights** | `GET` | `/api/streetlights` | List all street light poles |
| **Dimming** | `PUT` | `/api/streetlights/{id}/dimming` | Adjust LED dimming level |
| **Telemetry** | `POST` | `/api/streetlights/telemetry` | Ingest Lux/Power telemetry (Auto Fault Ticket on 0W) |
| **Power Summary**| `GET` | `/api/streetlights/power-summary`| Get grid power consumption & cost summary |
| **Procurement** | `POST` | `/api/purchase-orders` | Issue Purchase Order for power/maintenance |
| **Vendor Bill** | `POST` | `/api/vendor-bills/from-po/{poId}` | Convert PO to Vendor Bill & post journal |
| **Payments** | `POST` | `/api/payments/vendor-bill/{billId}?bankAccountId=1` | Execute Vendor Payment via Bank |
| **Sales Order** | `POST` | `/api/sales-orders` | Create Sales Order for Municipal Grid |
| **Customer Invoice**| `POST` | `/api/customer-invoices/from-so/{soId}`| Convert SO to Customer Invoice |
| **Receipts** | `POST` | `/api/payments/customer-invoice/{invoiceId}?bankAccountId=1` | Register Municipal Payment Receipt |
| **Balance Sheet**| `GET` | `/api/reports/balance-sheet` | Generate Balance Sheet Report |
| **Profit & Loss** | `GET` | `/api/reports/profit-loss` | Generate P&L Account Report |
| **Budget Report**| `GET` | `/api/reports/budget` | Generate Zone Energy Budget Variance Report |
| **Fault Tickets** | `GET` | `/api/fault-tickets` | List automated maintenance tickets |
