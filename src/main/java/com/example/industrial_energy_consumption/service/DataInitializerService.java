package com.example.industrial_energy_consumption.service;

import com.example.industrial_energy_consumption.dto.PurchaseOrderDto;
import com.example.industrial_energy_consumption.dto.SalesOrderDto;
import com.example.industrial_energy_consumption.entity.*;
import com.example.industrial_energy_consumption.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializerService implements CommandLineRunner {

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final ZoneRepository zoneRepository;
    private final StreetLightRepository streetLightRepository;
    private final TelemetryRepository telemetryRepository;
    private final FaultTicketRepository faultTicketRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorBillRepository vendorBillRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final ProcurementAndBillingService billingService;

    @Override
    @Transactional
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            seedMasterData();
        }
    }

    @Transactional
    public void resetAndSeedData() {
        telemetryRepository.deleteAll();
        faultTicketRepository.deleteAll();
        paymentRepository.deleteAll();
        vendorBillRepository.deleteAll();
        customerInvoiceRepository.deleteAll();
        purchaseOrderRepository.deleteAll();
        salesOrderRepository.deleteAll();
        streetLightRepository.deleteAll();
        zoneRepository.deleteAll();
        journalEntryRepository.deleteAll();
        journalRepository.deleteAll();
        productRepository.deleteAll();
        contactRepository.deleteAll();
        accountRepository.deleteAll();

        telemetryRepository.flush();
        faultTicketRepository.flush();
        paymentRepository.flush();
        vendorBillRepository.flush();
        customerInvoiceRepository.flush();
        purchaseOrderRepository.flush();
        salesOrderRepository.flush();
        streetLightRepository.flush();
        zoneRepository.flush();
        journalEntryRepository.flush();
        journalRepository.flush();
        productRepository.flush();
        contactRepository.flush();
        accountRepository.flush();

        seedMasterData();
    }

    @Transactional
    public void seedMasterData() {
        // ==========================================
        // 1. Chart of Accounts Master
        // ==========================================
        Account bankAcc = accountRepository.save(Account.builder()
                .code("1010")
                .name("Cash & Bank Reserves")
                .accountType(AccountType.ASSET)
                .balance(new BigDecimal("85000.00"))
                .build());

        Account escrowAcc = accountRepository.save(Account.builder()
                .code("1020")
                .name("Municipal Treasury Escrow")
                .accountType(AccountType.ASSET)
                .balance(new BigDecimal("30000.00"))
                .build());

        accountRepository.save(Account.builder()
                .code("1510")
                .name("Lighting Grid Infrastructure")
                .accountType(AccountType.ASSET)
                .balance(new BigDecimal("320000.00"))
                .build());

        accountRepository.save(Account.builder()
                .code("1520")
                .name("Solar Generation & Microgrid Storage")
                .accountType(AccountType.ASSET)
                .balance(new BigDecimal("65000.00"))
                .build());

        accountRepository.save(Account.builder()
                .code("1530")
                .name("Smart IoT Sensor & Telemetry Nodes")
                .accountType(AccountType.ASSET)
                .balance(new BigDecimal("24500.00"))
                .build());

        Account payablesAcc = accountRepository.save(Account.builder()
                .code("2010")
                .name("Power Utility Payables")
                .accountType(AccountType.LIABILITY)
                .balance(BigDecimal.ZERO)
                .build());

        accountRepository.save(Account.builder()
                .code("2020")
                .name("Maintenance Contractor Payables")
                .accountType(AccountType.LIABILITY)
                .balance(BigDecimal.ZERO)
                .build());

        accountRepository.save(Account.builder()
                .code("4010")
                .name("Municipal Grid Service Fee")
                .accountType(AccountType.INCOME)
                .balance(BigDecimal.ZERO)
                .build());

        accountRepository.save(Account.builder()
                .code("4020")
                .name("Industrial Microgrid & Solar Feed-in Surcharge")
                .accountType(AccountType.INCOME)
                .balance(BigDecimal.ZERO)
                .build());

        accountRepository.save(Account.builder()
                .code("5010")
                .name("Electricity Consumption Expense")
                .accountType(AccountType.EXPENSE)
                .balance(BigDecimal.ZERO)
                .build());

        accountRepository.save(Account.builder()
                .code("5020")
                .name("Repair & Maintenance Expense")
                .accountType(AccountType.EXPENSE)
                .balance(BigDecimal.ZERO)
                .build());

        accountRepository.save(Account.builder()
                .code("5030")
                .name("IoT Grid Telemetry Network Expense")
                .accountType(AccountType.EXPENSE)
                .balance(BigDecimal.ZERO)
                .build());

        // ==========================================
        // 2. Journals
        // ==========================================
        journalRepository.save(Journal.builder().name("Purchase Journal").type(JournalType.PURCHASE).build());
        journalRepository.save(Journal.builder().name("Sales Journal").type(JournalType.SALES).build());
        journalRepository.save(Journal.builder().name("Bank/Cash Journal").type(JournalType.BANK).build());

        // ==========================================
        // 3. Contact Master
        // ==========================================
        Contact vendorUtility = contactRepository.save(Contact.builder()
                .name("National Electric Utility Provider")
                .type(ContactType.VENDOR_UTILITY)
                .email("billing@nationalutility.com")
                .phone("+1-800-555-POWER")
                .address("100 Grid Way, Power City")
                .build());

        Contact vendorSolar = contactRepository.save(Contact.builder()
                .name("Apex Clean Energy & Solar Grid")
                .type(ContactType.VENDOR_UTILITY)
                .email("accounts@apexsolar.io")
                .phone("+1-800-555-SOLAR")
                .address("220 Sun Valley Rd, Green Valley")
                .build());

        Contact vendorHydro = contactRepository.save(Contact.builder()
                .name("Metro Hydroelectric Distribution Co.")
                .type(ContactType.VENDOR_UTILITY)
                .email("supply@metrohydro.org")
                .phone("+1-800-555-HYDRO")
                .address("45 Dam Cascade Parkway, River City")
                .build());

        Contact contractorMaint = contactRepository.save(Contact.builder()
                .name("City Tech Maintenance Contractors")
                .type(ContactType.VENDOR_CONTRACTOR)
                .email("support@citytechmaint.com")
                .phone("+1-800-555-FIXIT")
                .address("45 Maintenance Blvd, Industrial Park")
                .build());

        Contact contractorVolt = contactRepository.save(Contact.builder()
                .name("VoltGuard Smart Pole Services")
                .type(ContactType.VENDOR_CONTRACTOR)
                .email("dispatch@voltguardservices.com")
                .phone("+1-800-555-VOLT")
                .address("12 Substation Road, Sector 4")
                .build());

        Contact contractorOmni = contactRepository.save(Contact.builder()
                .name("OmniNet IoT Grid Specialists")
                .type(ContactType.VENDOR_CONTRACTOR)
                .email("support@omninetgrid.net")
                .phone("+1-800-555-OMNI")
                .address("708 Sensor Ave, Innovation Hub")
                .build());

        Contact clientMetro = contactRepository.save(Contact.builder()
                .name("Metropolitan Municipal Council Authority")
                .type(ContactType.CUSTOMER_MUNICIPALITY)
                .email("lighting@municipality.gov")
                .phone("+1-800-555-CITY")
                .address("1 City Hall Plaza, Central City")
                .build());

        Contact clientPort = contactRepository.save(Contact.builder()
                .name("East Bay Port & Harbor Authority")
                .type(ContactType.CUSTOMER_MUNICIPALITY)
                .email("logistics@eastbayport.org")
                .phone("+1-800-555-PORT")
                .address("Pier 9 Maritime Center, East Bay")
                .build());

        Contact clientHighway = contactRepository.save(Contact.builder()
                .name("State Highway Infrastructure Dept")
                .type(ContactType.CUSTOMER_MUNICIPALITY)
                .email("highways@state.gov")
                .phone("+1-800-555-ROADS")
                .address("500 Capitol Express Way, Capital City")
                .build());

        Contact clientTech = contactRepository.save(Contact.builder()
                .name("North Valley Innovation District Council")
                .type(ContactType.CUSTOMER_MUNICIPALITY)
                .email("smartcity@northvalleydistrict.org")
                .phone("+1-800-555-TECH")
                .address("300 Venture Blvd, Tech District")
                .build());

        // ==========================================
        // 4. Product & Service Master
        // ==========================================
        Product prodPole100 = productRepository.save(Product.builder()
                .code("POLE-LED-100W")
                .name("Smart LED Pole Assembly 100W")
                .productType(ProductType.GOODS)
                .unitPrice(new BigDecimal("500.00"))
                .build());

        Product prodPole150 = productRepository.save(Product.builder()
                .code("POLE-LED-150W")
                .name("High-Output Expressway LED Fixture 150W")
                .productType(ProductType.GOODS)
                .unitPrice(new BigDecimal("750.00"))
                .build());

        Product prodSolarBatt = productRepository.save(Product.builder()
                .code("SOLAR-BATT-2KWH")
                .name("Smart Lithium Battery Backup 2kWh")
                .productType(ProductType.GOODS)
                .unitPrice(new BigDecimal("1250.00"))
                .build());

        Product prodSensorNode = productRepository.save(Product.builder()
                .code("IOT-SENSOR-NODE")
                .name("LoRaWAN Grid Telemetry Gateway Node")
                .productType(ProductType.GOODS)
                .unitPrice(new BigDecimal("220.00"))
                .build());

        Product prodKwh = productRepository.save(Product.builder()
                .code("ELEC-KWH")
                .name("Grid Electricity kWh")
                .productType(ProductType.GOODS)
                .unitPrice(new BigDecimal("0.15"))
                .build());

        Product prodSolarKwh = productRepository.save(Product.builder()
                .code("ELEC-CLEAN-KWH")
                .name("Certified Solar Renewable Power kWh")
                .productType(ProductType.GOODS)
                .unitPrice(new BigDecimal("0.18"))
                .build());

        Product prodMaint = productRepository.save(Product.builder()
                .code("MAINT-SVC")
                .name("Standard Pole Repair & Maintenance Service")
                .productType(ProductType.SERVICE)
                .unitPrice(new BigDecimal("150.00"))
                .build());

        Product prodEmerg = productRepository.save(Product.builder()
                .code("EMERG-CALL-SVC")
                .name("24/7 Emergency Fault Response Service")
                .productType(ProductType.SERVICE)
                .unitPrice(new BigDecimal("320.00"))
                .build());

        Product prodAudit = productRepository.save(Product.builder()
                .code("GRID-AUDIT-SVC")
                .name("Industrial Energy Efficiency & Photometric Audit")
                .productType(ProductType.SERVICE)
                .unitPrice(new BigDecimal("1800.00"))
                .build());

        // ==========================================
        // 5. Zone Master (Analytic Accounts)
        // ==========================================
        Zone zone1 = zoneRepository.save(Zone.builder()
                .zoneCode("ZONE-1")
                .name("Downtown Commercial Corridor")
                .plannedBudget(new BigDecimal("35000.00"))
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build());

        Zone zone2 = zoneRepository.save(Zone.builder()
                .zoneCode("ZONE-2")
                .name("Harbor Industrial Freight Terminal")
                .plannedBudget(new BigDecimal("28000.00"))
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build());

        Zone zone3 = zoneRepository.save(Zone.builder()
                .zoneCode("ZONE-3")
                .name("East Metro Residential & Transit Hub")
                .plannedBudget(new BigDecimal("18500.00"))
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build());

        Zone zone4 = zoneRepository.save(Zone.builder()
                .zoneCode("ZONE-4")
                .name("North Tech Park & Innovation District")
                .plannedBudget(new BigDecimal("22000.00"))
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build());

        Zone zone5 = zoneRepository.save(Zone.builder()
                .zoneCode("ZONE-5")
                .name("Airport Express Corridor & Ring Road")
                .plannedBudget(new BigDecimal("30000.00"))
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build());

        Zone zone6 = zoneRepository.save(Zone.builder()
                .zoneCode("ZONE-6")
                .name("West Suburb Greenways & Parkways")
                .plannedBudget(new BigDecimal("15000.00"))
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build());

        Zone zone7 = zoneRepository.save(Zone.builder()
                .zoneCode("ZONE-7")
                .name("Highway Zone 7 Smart Grid")
                .plannedBudget(new BigDecimal("12000.00"))
                .actualPowerExpense(BigDecimal.ZERO)
                .actualRepairExpense(BigDecimal.ZERO)
                .build());

        // ==========================================
        // 6. Street Lights (22 Poles Across 7 Zones)
        // ==========================================
        List<StreetLight> lights = new ArrayList<>();

        // Zone 1: Downtown Commercial Corridor
        lights.add(StreetLight.builder().poleCode("POLE-101").location("Downtown Central Plaza - North Gate").zone(zone1).dimmingPercentage(90).powerDrawWatts(135.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-102").location("Downtown Central Plaza - South Pedestrian Walk").zone(zone1).dimmingPercentage(100).powerDrawWatts(150.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-103").location("Downtown 4th Avenue & Broadway").zone(zone1).dimmingPercentage(75).powerDrawWatts(112.5).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        StreetLight pole104 = StreetLight.builder().poleCode("POLE-104").location("Downtown City Hall Esplanade").zone(zone1).dimmingPercentage(80).powerDrawWatts(0.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.FAULT).build();
        lights.add(pole104);

        // Zone 2: Harbor Industrial Freight Terminal
        lights.add(StreetLight.builder().poleCode("POLE-201").location("Harbor Freight Gate A - Heavy Truck Lane").zone(zone2).dimmingPercentage(100).powerDrawWatts(150.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-202").location("Harbor Terminal Cranes Pier 4").zone(zone2).dimmingPercentage(100).powerDrawWatts(150.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-203").location("Container Depot Logistics Yard East").zone(zone2).dimmingPercentage(60).powerDrawWatts(90.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        StreetLight pole204 = StreetLight.builder().poleCode("POLE-204").location("Harbor Bulk Fuel Storage Perimeter").zone(zone2).dimmingPercentage(70).powerDrawWatts(0.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.FAULT).build();
        lights.add(pole204);

        // Zone 3: East Metro Residential & Transit Hub
        lights.add(StreetLight.builder().poleCode("POLE-301").location("East Metro Transit Station Bus Loop").zone(zone3).dimmingPercentage(85).powerDrawWatts(127.5).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-302").location("East Parkside Residential Blvd").zone(zone3).dimmingPercentage(40).powerDrawWatts(60.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-303").location("Metro Elementary School Crosswalk").zone(zone3).dimmingPercentage(50).powerDrawWatts(75.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());

        // Zone 4: North Tech Park & Innovation District
        lights.add(StreetLight.builder().poleCode("POLE-401").location("Innovation Way - Quantum Labs Entrance").zone(zone4).dimmingPercentage(80).powerDrawWatts(120.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-402").location("Cybersecurity Center Ring Road").zone(zone4).dimmingPercentage(70).powerDrawWatts(105.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-403").location("Clean Energy Campus Solar Canopy Pole").zone(zone4).dimmingPercentage(0).powerDrawWatts(0.0).activeStatus(ActiveStatus.DAY).status(PoleStatus.OFFLINE).build());

        // Zone 5: Airport Express Corridor & Ring Road
        lights.add(StreetLight.builder().poleCode("POLE-501").location("Airport Parkway - Mile 3 Flyover").zone(zone5).dimmingPercentage(100).powerDrawWatts(150.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-502").location("Cargo Terminal Access Road Junction").zone(zone5).dimmingPercentage(95).powerDrawWatts(142.5).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        StreetLight pole503 = StreetLight.builder().poleCode("POLE-503").location("Runway Approach Safety Buffer Outer Ring").zone(zone5).dimmingPercentage(90).powerDrawWatts(0.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.FAULT).build();
        lights.add(pole503);

        // Zone 6: West Suburb Greenways & Parkways
        lights.add(StreetLight.builder().poleCode("POLE-601").location("Oakridge Greenway Bicycle Trailhead").zone(zone6).dimmingPercentage(30).powerDrawWatts(45.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-602").location("Meadow Creek Parkway Crossing").zone(zone6).dimmingPercentage(50).powerDrawWatts(75.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());

        // Zone 7: Highway Zone 7 Smart Grid
        lights.add(StreetLight.builder().poleCode("POLE-701").location("Highway Zone 7 - Milestone 1").zone(zone7).dimmingPercentage(80).powerDrawWatts(120.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        StreetLight pole702 = StreetLight.builder().poleCode("POLE-702").location("Highway Zone 7 - Milestone 2").zone(zone7).dimmingPercentage(100).powerDrawWatts(150.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build();
        lights.add(pole702);
        lights.add(StreetLight.builder().poleCode("POLE-703").location("Highway Zone 7 - Exit Ramp").zone(zone7).dimmingPercentage(50).powerDrawWatts(75.0).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());
        lights.add(StreetLight.builder().poleCode("POLE-704").location("Highway Zone 7 - Rest Area Interchange").zone(zone7).dimmingPercentage(85).powerDrawWatts(127.5).activeStatus(ActiveStatus.NIGHT).status(PoleStatus.NORMAL).build());

        List<StreetLight> savedLights = streetLightRepository.saveAll(lights);

        // Extract saved references with IDs
        StreetLight savedPole104 = savedLights.stream().filter(l -> "POLE-104".equals(l.getPoleCode())).findFirst().orElse(null);
        StreetLight savedPole204 = savedLights.stream().filter(l -> "POLE-204".equals(l.getPoleCode())).findFirst().orElse(null);
        StreetLight savedPole503 = savedLights.stream().filter(l -> "POLE-503".equals(l.getPoleCode())).findFirst().orElse(null);
        StreetLight savedPole702 = savedLights.stream().filter(l -> "POLE-702".equals(l.getPoleCode())).findFirst().orElse(null);

        // ==========================================
        // 7. Telemetry Historical Readings
        // ==========================================
        LocalDateTime now = LocalDateTime.now();
        List<TelemetryData> telemetryList = new ArrayList<>();

        for (StreetLight sl : savedLights) {
            telemetryList.add(TelemetryData.builder()
                    .streetLight(sl)
                    .ambientLightLux(sl.getActiveStatus() == ActiveStatus.DAY ? 520.0 : 8.5)
                    .powerDrawWatts(sl.getPowerDrawWatts())
                    .dimmingPercentage(sl.getDimmingPercentage())
                    .recordedAt(now.minusHours(1))
                    .build());

            telemetryList.add(TelemetryData.builder()
                    .streetLight(sl)
                    .ambientLightLux(sl.getActiveStatus() == ActiveStatus.DAY ? 480.0 : 4.2)
                    .powerDrawWatts(sl.getPowerDrawWatts())
                    .dimmingPercentage(sl.getDimmingPercentage())
                    .recordedAt(now.minusMinutes(15))
                    .build());
        }
        telemetryRepository.saveAll(telemetryList);

        // ==========================================
        // 8. Automated Fault Tickets
        // ==========================================
        if (savedPole104 != null) {
            faultTicketRepository.save(FaultTicket.builder()
                    .ticketNumber("TKT-104-FLT")
                    .streetLight(savedPole104)
                    .issueDescription("Zero wattage draw (0.0W) recorded during peak night cycle at 80% dimming. Suspected LED driver unit burnout.")
                    .status(TicketStatus.OPEN)
                    .createdAt(now.minusHours(3))
                    .build());
        }

        if (savedPole204 != null) {
            faultTicketRepository.save(FaultTicket.builder()
                    .ticketNumber("TKT-204-FLT")
                    .streetLight(savedPole204)
                    .issueDescription("Complete power outage detected on Harbor perimeter luminaire. VoltGuard contractor dispatched with bucket truck.")
                    .status(TicketStatus.IN_PROGRESS)
                    .createdAt(now.minusHours(5))
                    .build());
        }

        if (savedPole503 != null) {
            faultTicketRepository.save(FaultTicket.builder()
                    .ticketNumber("TKT-503-FLT")
                    .streetLight(savedPole503)
                    .issueDescription("0W draw on runway approach buffer light during active dusk flight schedule. Urgent safety priority inspection.")
                    .status(TicketStatus.OPEN)
                    .createdAt(now.minusHours(1))
                    .build());
        }

        if (savedPole702 != null) {
            faultTicketRepository.save(FaultTicket.builder()
                    .ticketNumber("TKT-702-RES")
                    .streetLight(savedPole702)
                    .issueDescription("Resolved: Replaced transient surge protector and re-seated photocell sensor. Restored 150W draw.")
                    .status(TicketStatus.RESOLVED)
                    .createdAt(now.minusDays(1))
                    .build());
        }

        // ==========================================
        // 9. Procurement, Vendor Bills & Ledger Movements
        // ==========================================
        // PO 1: Grid Power for Highway Zone 7 -> Bill -> Paid
        PurchaseOrder po1 = billingService.createPurchaseOrder(PurchaseOrderDto.builder()
                .vendorId(vendorUtility.getId())
                .productId(prodKwh.getId())
                .zoneId(zone7.getId())
                .quantity(40000)
                .unitPrice(new BigDecimal("0.15"))
                .build());
        VendorBill bill1 = billingService.convertPoToVendorBill(po1.getId());
        billingService.payVendorBill(bill1.getId(), bankAcc.getId());

        // PO 2: Solar Power for Downtown Zone 1 -> Bill -> Paid
        PurchaseOrder po2 = billingService.createPurchaseOrder(PurchaseOrderDto.builder()
                .vendorId(vendorSolar.getId())
                .productId(prodSolarKwh.getId())
                .zoneId(zone1.getId())
                .quantity(54166)
                .unitPrice(new BigDecimal("0.18"))
                .build());
        VendorBill bill2 = billingService.convertPoToVendorBill(po2.getId());
        billingService.payVendorBill(bill2.getId(), bankAcc.getId());

        // PO 3: Hydro Power for Harbor Zone 2 -> Bill -> UNPAID (creates $7,500 payable)
        PurchaseOrder po3 = billingService.createPurchaseOrder(PurchaseOrderDto.builder()
                .vendorId(vendorHydro.getId())
                .productId(prodKwh.getId())
                .zoneId(zone2.getId())
                .quantity(50000)
                .unitPrice(new BigDecimal("0.15"))
                .build());
        billingService.convertPoToVendorBill(po3.getId());

        // PO 4: Maintenance Service for Harbor Zone 2 -> Bill -> Paid
        PurchaseOrder po4 = billingService.createPurchaseOrder(PurchaseOrderDto.builder()
                .vendorId(contractorMaint.getId())
                .productId(prodMaint.getId())
                .zoneId(zone2.getId())
                .quantity(12)
                .unitPrice(new BigDecimal("150.00"))
                .build());
        VendorBill bill4 = billingService.convertPoToVendorBill(po4.getId());
        billingService.payVendorBill(bill4.getId(), bankAcc.getId());

        // PO 5: Emergency Response Service for Airport Zone 5 -> Bill -> UNPAID
        PurchaseOrder po5 = billingService.createPurchaseOrder(PurchaseOrderDto.builder()
                .vendorId(contractorVolt.getId())
                .productId(prodEmerg.getId())
                .zoneId(zone5.getId())
                .quantity(6)
                .unitPrice(new BigDecimal("320.00"))
                .build());
        billingService.convertPoToVendorBill(po5.getId());

        // PO 6: IoT Telemetry Maintenance for East Metro Zone 3 -> Bill -> Paid
        PurchaseOrder po6 = billingService.createPurchaseOrder(PurchaseOrderDto.builder()
                .vendorId(contractorOmni.getId())
                .productId(prodMaint.getId())
                .zoneId(zone3.getId())
                .quantity(8)
                .unitPrice(new BigDecimal("150.00"))
                .build());
        VendorBill bill6 = billingService.convertPoToVendorBill(po6.getId());
        billingService.payVendorBill(bill6.getId(), bankAcc.getId());

        // PO 7: 10 LED Fixtures for Tech Park Zone 4 -> Order Confirmed
        billingService.createPurchaseOrder(PurchaseOrderDto.builder()
                .vendorId(vendorUtility.getId())
                .productId(prodPole150.getId())
                .zoneId(zone4.getId())
                .quantity(10)
                .unitPrice(new BigDecimal("750.00"))
                .build());

        // ==========================================
        // 10. Municipal Sales Orders, Invoices & Receipts
        // ==========================================
        // SO 1: City Council Grid Management Service -> Invoice -> Paid
        SalesOrder so1 = billingService.createSalesOrder(SalesOrderDto.builder()
                .customerId(clientMetro.getId())
                .productId(prodAudit.getId())
                .quantity(14)
                .unitPrice(new BigDecimal("1800.00"))
                .build());
        CustomerInvoice inv1 = billingService.convertSoToCustomerInvoice(so1.getId());
        billingService.payCustomerInvoice(inv1.getId(), bankAcc.getId());

        // SO 2: East Bay Port Grid Operation -> Invoice -> Paid
        SalesOrder so2 = billingService.createSalesOrder(SalesOrderDto.builder()
                .customerId(clientPort.getId())
                .productId(prodAudit.getId())
                .quantity(10)
                .unitPrice(new BigDecimal("1800.00"))
                .build());
        CustomerInvoice inv2 = billingService.convertSoToCustomerInvoice(so2.getId());
        billingService.payCustomerInvoice(inv2.getId(), bankAcc.getId());

        // SO 3: State Highway Corridor Operations -> Invoice -> UNPAID
        SalesOrder so3 = billingService.createSalesOrder(SalesOrderDto.builder()
                .customerId(clientHighway.getId())
                .productId(prodAudit.getId())
                .quantity(8)
                .unitPrice(new BigDecimal("1800.00"))
                .build());
        billingService.convertSoToCustomerInvoice(so3.getId());

        // SO 4: Tech District Innovation Grid -> Sales Order Confirmed
        billingService.createSalesOrder(SalesOrderDto.builder()
                .customerId(clientTech.getId())
                .productId(prodAudit.getId())
                .quantity(7)
                .unitPrice(new BigDecimal("1800.00"))
                .build());
    }
}
