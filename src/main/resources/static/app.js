// LumiGrid Smart Street Light Controller - Frontend Application Script

const API_BASE = '/api';

// Application State
let state = {
    streetLights: [],
    faultTickets: [],
    contacts: [],
    products: [],
    accounts: [],
    zones: [],
    purchaseOrders: [],
    vendorBills: [],
    salesOrders: [],
    customerInvoices: [],
    powerSummary: null
};

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    initClock();
    setupNavigation();
    setupModals();
    setupForms();
    setupSimulationButtons();
    loadAllData();

    // Auto refresh telemetry every 15 seconds
    setInterval(() => {
        refreshKPIsAndTelemetry();
    }, 15000);
});

// Realtime Clock in Sidebar
function initClock() {
    const timeEl = document.getElementById('liveTimestamp');
    const updateTime = () => {
        const now = new Date();
        timeEl.innerText = now.toLocaleTimeString() + ' | ' + now.toLocaleDateString();
    };
    updateTime();
    setInterval(updateTime, 1000);
}

// Navigation Tab Switcher
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const targetTab = item.getAttribute('data-tab');
            switchTab(targetTab);
        });
    });

    document.getElementById('btnRefreshData').addEventListener('click', () => {
        loadAllData(true);
    });

    const btnSeed = document.getElementById('btnSeedDemoData');
    if (btnSeed) {
        btnSeed.addEventListener('click', async () => {
            btnSeed.disabled = true;
            btnSeed.innerText = 'Loading...';
            try {
                const res = await fetch(`${API_BASE}/demo/reset-and-seed`, { method: 'POST' });
                const json = await res.json();
                showToast(json.message || 'Dataset loaded successfully!', 'success');
                await loadAllData(false);
            } catch (err) {
                showToast('Failed to load dataset: ' + err.message, 'error');
            } finally {
                btnSeed.disabled = false;
                btnSeed.innerHTML = `
                    <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
                        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                        <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                        <line x1="12" y1="22.08" x2="12" y2="12"></line>
                    </svg>
                    Reload Dataset
                `;
            }
        });
    }
}

function switchTab(tabId) {
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));

    const activeNav = document.querySelector(`.nav-item[data-tab="${tabId}"]`);
    const activePane = document.getElementById(tabId);

    if (activeNav) activeNav.classList.add('active');
    if (activePane) activePane.classList.add('active');

    // Update Titles
    const titles = {
        'tab-overview': ['Grid Overview & Telemetry', 'Real-time municipal lighting telemetry, automated dimming, and ledger accounting'],
        'tab-lights': ['Street Light Grid Management', 'Individual pole configuration, dimming thresholds, and automated fault monitoring'],
        'tab-tickets': ['Automated Fault Maintenance Tickets', 'Zero-power fault tickets logged automatically by telemetry engine'],
        'tab-procurement': ['Utility Procurement & Vendor Bills', 'Issue POs for power/contractor maintenance and settle via Bank'],
        'tab-sales': ['Municipal Grid Billing', 'Bill municipal authority for street lighting operations and register receipts'],
        'tab-budget': ['Municipal Budgets & Financial Ledgers', 'Analytic Zone budget variance, Balance Sheet, and Profit & Loss report'],
        'tab-master': ['Master Data Modules', 'Contact master, Product/Service master, and Chart of Accounts ledger']
    };

    if (titles[tabId]) {
        document.getElementById('pageTitle').innerText = titles[tabId][0];
        document.getElementById('pageCaption').innerText = titles[tabId][1];
    }
}

// Modal Handlers
function setupModals() {
    const modal = document.getElementById('modalNewPole');
    const btnOpen = document.getElementById('btnOpenNewPoleModal');
    const btnClose = document.getElementById('btnCloseNewPoleModal');
    const btnCancel = document.getElementById('btnCancelNewPole');

    const openModal = () => modal.classList.add('active');
    const closeModal = () => modal.classList.remove('active');

    btnOpen.addEventListener('click', openModal);
    btnClose.addEventListener('click', closeModal);
    btnCancel.addEventListener('click', closeModal);

    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
}

// Form Handlers
function setupForms() {
    // 1. Register Pole Form
    document.getElementById('formNewPole').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            poleCode: document.getElementById('newPoleCode').value.trim(),
            location: document.getElementById('newPoleLocation').value.trim(),
            zoneCode: document.getElementById('newPoleZone').value.trim(),
            dimmingPercentage: parseInt(document.getElementById('newPoleDimming').value),
            powerDrawWatts: parseFloat(document.getElementById('newPoleWatts').value),
            activeStatus: 'NIGHT'
        };

        try {
            const res = await fetch(`${API_BASE}/streetlights`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error(await res.text());
            showToast('Street Light pole registered successfully!', 'success');
            document.getElementById('modalNewPole').classList.remove('active');
            document.getElementById('formNewPole').reset();
            loadAllData();
        } catch (err) {
            showToast('Error registering pole: ' + err.message, 'error');
        }
    });

    // 2. Create Purchase Order Form
    document.getElementById('formCreatePO').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            vendorId: parseInt(document.getElementById('poVendorSelect').value),
            productId: parseInt(document.getElementById('poProductSelect').value),
            zoneId: parseInt(document.getElementById('poZoneSelect').value),
            quantity: parseInt(document.getElementById('poQuantity').value),
            unitPrice: parseFloat(document.getElementById('poUnitPrice').value)
        };

        try {
            const res = await fetch(`${API_BASE}/purchase-orders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error(await res.text());
            showToast('Purchase Order created successfully!', 'success');
            loadProcurementData();
            loadReports();
        } catch (err) {
            showToast('Failed to create PO: ' + err.message, 'error');
        }
    });

    // 3. Create Sales Order Form
    document.getElementById('formCreateSO').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            customerId: parseInt(document.getElementById('soCustomerSelect').value),
            productId: parseInt(document.getElementById('soProductSelect').value),
            quantity: parseInt(document.getElementById('soQuantity').value),
            unitPrice: parseFloat(document.getElementById('soUnitPrice').value)
        };

        try {
            const res = await fetch(`${API_BASE}/sales-orders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error(await res.text());
            showToast('Municipal Sales Order created!', 'success');
            loadSalesData();
            loadReports();
        } catch (err) {
            showToast('Failed to create SO: ' + err.message, 'error');
        }
    });

    document.getElementById('btnRefreshTickets').addEventListener('click', loadFaultTickets);
    document.getElementById('btnRefreshBudget').addEventListener('click', loadReports);

    // Search and filter listeners
    document.getElementById('inputSearchLights').addEventListener('input', renderStreetLights);
    document.getElementById('selectFilterStatus').addEventListener('change', renderStreetLights);
}

// Simulation Control Center
function setupSimulationButtons() {
    // Simulate Night
    document.getElementById('btnSimulateLuxNight').addEventListener('click', async () => {
        if (!state.streetLights.length) return;
        const pole = state.streetLights[0];
        try {
            await fetch(`${API_BASE}/streetlights/telemetry`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    streetLightId: pole.id,
                    ambientLightLux: 10.0,
                    powerDrawWatts: 150.0,
                    dimmingPercentage: 100
                })
            });
            showToast(`Night telemetry ingested for ${pole.poleCode}: 100% dimming, 150W draw.`, 'info');
            loadStreetLights();
        } catch (err) {
            showToast('Simulation error: ' + err.message, 'error');
        }
    });

    // Simulate Day
    document.getElementById('btnSimulateLuxDay').addEventListener('click', async () => {
        if (!state.streetLights.length) return;
        const pole = state.streetLights[0];
        try {
            await fetch(`${API_BASE}/streetlights/telemetry`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    streetLightId: pole.id,
                    ambientLightLux: 500.0,
                    powerDrawWatts: 0.0,
                    dimmingPercentage: 0
                })
            });
            showToast(`Daylight telemetry ingested for ${pole.poleCode}: Dimmed to 0%.`, 'info');
            loadStreetLights();
        } catch (err) {
            showToast('Simulation error: ' + err.message, 'error');
        }
    });

    // Simulate 0W Power Fault
    document.getElementById('btnSimulateFault').addEventListener('click', async () => {
        if (!state.streetLights.length) return;
        const pole = state.streetLights[0];
        try {
            await fetch(`${API_BASE}/streetlights/${pole.id}/dimming`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    dimmingPercentage: 80,
                    currentPowerDrawWatts: 0.0 // FAULT!
                })
            });
            showToast(`💥 Fault Triggered on ${pole.poleCode}! Zero power draw logged during night status. Ticket auto-created!`, 'error');
            loadAllData();
            switchTab('tab-tickets');
        } catch (err) {
            showToast('Fault injection failed: ' + err.message, 'error');
        }
    });
}

// Load All System Data
async function loadAllData(notify = false) {
    try {
        await Promise.all([
            loadStreetLights(),
            loadFaultTickets(),
            loadMasterData(),
            loadProcurementData(),
            loadSalesData(),
            loadReports(),
            loadKPIs()
        ]);
        if (notify) showToast('Grid data refreshed successfully', 'info');
    } catch (err) {
        console.error('Data load error:', err);
    }
}

// Refresh KPIs & Telemetry
async function refreshKPIsAndTelemetry() {
    await loadKPIs();
    await loadStreetLights();
    await loadFaultTickets();
}

// 1. Street Lights
async function loadStreetLights() {
    try {
        const res = await fetch(`${API_BASE}/streetlights`);
        state.streetLights = await res.json();
        renderStreetLights();
        renderOverviewQuickLights();
        document.getElementById('badgeTotalLights').innerText = state.streetLights.length;
    } catch (err) {
        console.error('Failed to load streetlights:', err);
    }
}

function renderStreetLights() {
    const container = document.getElementById('streetLightsGrid');
    const search = document.getElementById('inputSearchLights').value.toLowerCase();
    const filter = document.getElementById('selectFilterStatus').value;

    const filtered = state.streetLights.filter(light => {
        const matchSearch = light.poleCode.toLowerCase().includes(search) ||
                            (light.location && light.location.toLowerCase().includes(search)) ||
                            (light.zone && light.zone.name.toLowerCase().includes(search));
        const matchFilter = filter === 'ALL' || light.status === filter;
        return matchSearch && matchFilter;
    });

    if (filtered.length === 0) {
        container.innerHTML = '<div class="skeleton-loader">No street lights matching your criteria.</div>';
        return;
    }

    container.innerHTML = filtered.map(light => {
        const isFault = light.status === 'FAULT';
        const isNormal = light.status === 'NORMAL';
        const isGlowing = light.powerDrawWatts > 0 && light.dimmingPercentage > 0;
        const statusClass = isFault ? 'status-pill-fault' : (isNormal ? 'status-pill-normal' : 'status-pill-offline');
        const glowClass = isFault ? 'fault' : (isGlowing ? 'glow' : '');

        return `
            <div class="pole-card ${isFault ? 'status-fault' : ''}">
                <div class="pole-top">
                    <div class="pole-identity">
                        <div class="led-indicator ${glowClass}" id="led-${light.id}"></div>
                        <div>
                            <div class="pole-code">${light.poleCode}</div>
                            <div class="pole-zone">${light.zone ? light.zone.name : 'Unassigned Zone'}</div>
                        </div>
                    </div>
                    <span class="pole-status-pill ${statusClass}">${light.status}</span>
                </div>

                <div class="pole-telemetry-row">
                    <div class="telemetry-item">
                        <span class="telemetry-label">Power Draw</span>
                        <span class="telemetry-value" id="val-watts-${light.id}">${light.powerDrawWatts.toFixed(1)} W</span>
                    </div>
                    <div class="telemetry-item">
                        <span class="telemetry-label">Schedule</span>
                        <span class="telemetry-value">${light.activeStatus}</span>
                    </div>
                    <div class="telemetry-item">
                        <span class="telemetry-label">Location</span>
                        <span class="telemetry-value">${light.location || 'Pole Site'}</span>
                    </div>
                </div>

                <div class="dimming-control-group">
                    <div class="dimming-slider-header">
                        <span>Dimming Level</span>
                        <span class="dimming-value-text" id="val-dimming-${light.id}">${light.dimmingPercentage}%</span>
                    </div>
                    <input type="range" min="0" max="100" value="${light.dimmingPercentage}" class="range-slider" 
                        oninput="onDimmingSliderInput(${light.id}, this.value)"
                        onchange="onDimmingSliderChange(${light.id}, this.value)">
                </div>

                <div class="pole-actions">
                    <button class="btn btn-sm btn-outline" style="flex:1" onclick="injectQuickTelemetry(${light.id})">
                        📡 Telemetry Lux
                    </button>
                    <button class="btn btn-sm btn-danger-outline" style="flex:1" onclick="injectZeroPowerFault(${light.id})">
                        💥 0W Fault
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function renderOverviewQuickLights() {
    const container = document.getElementById('overviewQuickLights');
    if (!state.streetLights.length) {
        container.innerHTML = '<div class="skeleton-loader">No active poles detected.</div>';
        return;
    }

    container.innerHTML = state.streetLights.slice(0, 4).map(l => `
        <div style="display:flex; justify-content:space-between; align-items:center; padding:12px; background:rgba(0,0,0,0.2); border-radius:8px; margin-bottom:10px;">
            <div style="display:flex; align-items:center; gap:10px;">
                <span style="font-size:1.2rem;">${l.status === 'FAULT' ? '🚨' : '💡'}</span>
                <div>
                    <strong style="color:#fff;">${l.poleCode}</strong>
                    <div style="font-size:0.75rem; color:var(--text-muted);">${l.location}</div>
                </div>
            </div>
            <div style="text-align:right;">
                <div style="color:var(--primary); font-family:var(--font-mono); font-weight:600;">${l.dimmingPercentage}% Dim | ${l.powerDrawWatts}W</div>
                <div style="font-size:0.72rem; color:${l.status === 'FAULT' ? '#f87171' : '#34d399'};">${l.status}</div>
            </div>
        </div>
    `).join('');
}

// Live Dimming Slider Actions
function onDimmingSliderInput(id, val) {
    document.getElementById(`val-dimming-${id}`).innerText = `${val}%`;
    const estimatedWatts = (150.0 * val / 100.0).toFixed(1);
    document.getElementById(`val-watts-${id}`).innerText = `${estimatedWatts} W`;
}

async function onDimmingSliderChange(id, val) {
    try {
        const res = await fetch(`${API_BASE}/streetlights/${id}/dimming`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                dimmingPercentage: parseInt(val)
            })
        });
        if (!res.ok) throw new Error(await res.text());
        showToast(`Dimming updated to ${val}%`, 'success');
        await loadStreetLights();
        await loadKPIs();
    } catch (err) {
        showToast('Error changing dimming: ' + err.message, 'error');
    }
}

async function injectQuickTelemetry(id) {
    try {
        await fetch(`${API_BASE}/streetlights/telemetry`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                streetLightId: id,
                ambientLightLux: Math.floor(Math.random() * 40) + 5,
                powerDrawWatts: 140.0,
                dimmingPercentage: 90
            })
        });
        showToast('Telemetry updated with ambient Lux reading', 'info');
        loadAllData();
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

async function injectZeroPowerFault(id) {
    try {
        await fetch(`${API_BASE}/streetlights/${id}/dimming`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                dimmingPercentage: 100,
                currentPowerDrawWatts: 0.0 // Zero power during active status
            })
        });
        showToast('🚨 0W Fault Detected! Automated maintenance ticket generated.', 'error');
        loadAllData();
        switchTab('tab-tickets');
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

// 2. Fault Tickets
async function loadFaultTickets() {
    try {
        const res = await fetch(`${API_BASE}/fault-tickets`);
        state.faultTickets = await res.json();
        renderFaultTickets();
        const openCount = state.faultTickets.filter(t => t.status !== 'RESOLVED').length;
        document.getElementById('badgeOpenTickets').innerText = openCount;
        document.getElementById('kpiFaultTickets').innerText = openCount;
        document.getElementById('kpiTicketSub').innerText = `${openCount} requiring contractor dispatch`;
    } catch (err) {
        console.error('Failed to load tickets:', err);
    }
}

function renderFaultTickets() {
    const tbody = document.getElementById('ticketsTableBody');
    if (!state.faultTickets.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="color:#34d399; padding:24px;">✨ Grid is fully operational! No active fault tickets detected.</td></tr>';
        return;
    }

    tbody.innerHTML = state.faultTickets.map(t => {
        const isResolved = t.status === 'RESOLVED';
        const badge = isResolved ? '<span class="pole-status-pill status-pill-normal">RESOLVED</span>' : '<span class="pole-status-pill status-pill-fault">OPEN</span>';
        const actionBtn = isResolved 
            ? '<span style="color:var(--text-muted); font-size:0.8rem;">Completed</span>' 
            : `<button class="btn btn-sm btn-primary" onclick="resolveTicket(${t.id})">Dispatch & Resolve</button>`;

        return `
            <tr>
                <td><strong>${t.ticketNumber}</strong></td>
                <td><span style="font-family:var(--font-mono); color:var(--primary);">${t.streetLight ? t.streetLight.poleCode : 'N/A'}</span></td>
                <td>${t.streetLight ? t.streetLight.location : 'Grid Location'}</td>
                <td style="max-width:280px; font-size:0.82rem;">${t.issueDescription}</td>
                <td style="font-size:0.8rem; color:var(--text-muted);">${new Date(t.createdAt).toLocaleString()}</td>
                <td>${badge}</td>
                <td>${actionBtn}</td>
            </tr>
        `;
    }).join('');
}

async function resolveTicket(ticketId) {
    try {
        const res = await fetch(`${API_BASE}/fault-tickets/${ticketId}/status?status=RESOLVED`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error(await res.text());
        showToast('Ticket marked RESOLVED. Street light status restored to NORMAL.', 'success');
        loadAllData();
    } catch (err) {
        showToast('Error resolving ticket: ' + err.message, 'error');
    }
}

// 3. Procurement & Utility Billing
async function loadProcurementData() {
    try {
        const [poRes, billsRes] = await Promise.all([
            fetch(`${API_BASE}/purchase-orders`),
            fetch(`${API_BASE}/vendor-bills`)
        ]);
        state.purchaseOrders = await poRes.json();
        state.vendorBills = await billsRes.json();
        renderProcurementTables();
    } catch (err) {
        console.error('Failed to load procurement:', err);
    }
}

function renderProcurementTables() {
    const poTbody = document.getElementById('poTableBody');
    if (!state.purchaseOrders.length) {
        poTbody.innerHTML = '<tr><td colspan="4" class="text-center">No Purchase Orders yet</td></tr>';
    } else {
        poTbody.innerHTML = state.purchaseOrders.map(po => `
            <tr>
                <td><strong>${po.poNumber}</strong></td>
                <td>${po.vendor ? po.vendor.name : 'Utility Vendor'}</td>
                <td style="font-family:var(--font-mono); color:#fff;">$${po.totalAmount.toFixed(2)}</td>
                <td>
                    <button class="btn btn-sm btn-outline" onclick="convertPoToBill(${po.id})">
                        Convert to Bill
                    </button>
                </td>
            </tr>
        `).join('');
    }

    const billsTbody = document.getElementById('vendorBillsTableBody');
    if (!state.vendorBills.length) {
        billsTbody.innerHTML = '<tr><td colspan="5" class="text-center">No Vendor Bills yet</td></tr>';
    } else {
        billsTbody.innerHTML = state.vendorBills.map(b => {
            const isPaid = b.status === 'PAID';
            return `
                <tr>
                    <td><strong>${b.billNumber}</strong></td>
                    <td>${b.vendor ? b.vendor.name : 'Vendor'}</td>
                    <td style="font-family:var(--font-mono); font-weight:600; color:#fff;">$${b.totalAmount.toFixed(2)}</td>
                    <td>
                        <span class="pole-status-pill ${isPaid ? 'status-pill-normal' : 'status-pill-fault'}">${b.status}</span>
                    </td>
                    <td>
                        ${isPaid ? '<span style="color:#34d399; font-size:0.8rem;">Paid via Bank</span>' : `
                            <button class="btn btn-sm btn-primary" onclick="payVendorBill(${b.id})">
                                Pay via Bank
                            </button>
                        `}
                    </td>
                </tr>
            `;
        }).join('');
    }
}

async function convertPoToBill(poId) {
    try {
        const res = await fetch(`${API_BASE}/vendor-bills/from-po/${poId}`, { method: 'POST' });
        if (!res.ok) throw new Error(await res.text());
        showToast('Converted PO to Vendor Bill & logged double-entry expenses!', 'success');
        loadProcurementData();
        loadReports();
    } catch (err) {
        showToast('Error converting PO: ' + err.message, 'error');
    }
}

async function payVendorBill(billId) {
    try {
        const bankAccount = state.accounts.find(a => a.code === '1010') || state.accounts[0];
        const res = await fetch(`${API_BASE}/payments/vendor-bill/${billId}?bankAccountId=${bankAccount.id}`, { method: 'POST' });
        if (!res.ok) throw new Error(await res.text());
        showToast('Payment settled via Bank (Account 1010)! Ledger updated.', 'success');
        loadProcurementData();
        loadReports();
        loadMasterData();
    } catch (err) {
        showToast('Payment failed: ' + err.message, 'error');
    }
}

// 4. Municipal Customer Billing
async function loadSalesData() {
    try {
        const [soRes, invRes] = await Promise.all([
            fetch(`${API_BASE}/sales-orders`),
            fetch(`${API_BASE}/customer-invoices`)
        ]);
        state.salesOrders = await soRes.json();
        state.customerInvoices = await invRes.json();
        renderSalesTable();
    } catch (err) {
        console.error('Failed to load sales data:', err);
    }
}

function renderSalesTable() {
    const tbody = document.getElementById('salesTableBody');
    const records = [];

    state.salesOrders.forEach(so => {
        records.push({
            id: so.id,
            type: 'SALES_ORDER',
            ref: so.soNumber,
            client: so.customer ? so.customer.name : 'Municipal Authority',
            amount: so.totalAmount,
            status: so.status,
            action: `<button class="btn btn-sm btn-outline" onclick="convertSoToInvoice(${so.id})">Generate Invoice</button>`
        });
    });

    state.customerInvoices.forEach(inv => {
        const isPaid = inv.status === 'PAID';
        records.push({
            id: inv.id,
            type: 'INVOICE',
            ref: inv.invoiceNumber,
            client: inv.customer ? inv.customer.name : 'Municipal Authority',
            amount: inv.totalAmount,
            status: inv.status,
            action: isPaid 
                ? '<span style="color:#34d399; font-size:0.8rem;">Receipt Registered</span>'
                : `<button class="btn btn-sm btn-primary" onclick="payCustomerInvoice(${inv.id})">Register Bank Receipt</button>`
        });
    });

    if (!records.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">No municipal sales orders or invoices yet</td></tr>';
        return;
    }

    tbody.innerHTML = records.map(r => `
        <tr>
            <td><strong>${r.ref}</strong> <span style="font-size:0.7rem; color:var(--text-muted);">(${r.type})</span></td>
            <td>${r.client}</td>
            <td style="font-family:var(--font-mono); font-weight:600; color:#fff;">$${r.amount.toFixed(2)}</td>
            <td>
                <span class="pole-status-pill ${r.status === 'PAID' ? 'status-pill-normal' : 'status-pill-offline'}">${r.status}</span>
            </td>
            <td>${r.action}</td>
        </tr>
    `).join('');
}

async function convertSoToInvoice(soId) {
    try {
        const res = await fetch(`${API_BASE}/customer-invoices/from-so/${soId}`, { method: 'POST' });
        if (!res.ok) throw new Error(await res.text());
        showToast('Generated Customer Invoice & credited municipal fee income!', 'success');
        loadSalesData();
        loadReports();
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

async function payCustomerInvoice(invoiceId) {
    try {
        const bankAccount = state.accounts.find(a => a.code === '1010') || state.accounts[0];
        const res = await fetch(`${API_BASE}/payments/customer-invoice/${invoiceId}?bankAccountId=${bankAccount.id}`, { method: 'POST' });
        if (!res.ok) throw new Error(await res.text());
        showToast('Municipal Payment receipt registered in Bank ledger!', 'success');
        loadSalesData();
        loadReports();
        loadMasterData();
    } catch (err) {
        showToast('Payment receipt error: ' + err.message, 'error');
    }
}

// 5. Reports & Budgets
async function loadReports() {
    try {
        const [budgetRes, bsRes, plRes] = await Promise.all([
            fetch(`${API_BASE}/reports/budget`),
            fetch(`${API_BASE}/reports/balance-sheet`),
            fetch(`${API_BASE}/reports/profit-loss`)
        ]);

        const budgetData = await budgetRes.json();
        const bsData = await bsRes.json();
        const plData = await plRes.json();

        renderBudgetReport(budgetData);
        renderBalanceSheet(bsData);
        renderProfitLoss(plData);
    } catch (err) {
        console.error('Failed to load reports:', err);
    }
}

function renderBudgetReport(data) {
    const container = document.getElementById('budgetReportContent');
    const overviewContainer = document.getElementById('overviewBudgetSnapshot');

    if (!data.zoneSummaries || !data.zoneSummaries.length) {
        container.innerHTML = '<div class="skeleton-loader">No active grid zones configured.</div>';
        return;
    }

    const html = data.zoneSummaries.map(z => {
        const pctUsed = z.plannedBudget > 0 ? ((z.totalActualExpense / z.plannedBudget) * 100).toFixed(1) : 0;
        const isWarning = z.status === 'OVER_BUDGET' || pctUsed > 85;

        return `
            <div style="background:rgba(0,0,0,0.2); padding:18px; border-radius:10px; margin-bottom:14px; border:1px solid var(--border-color);">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <strong style="font-size:1.05rem; color:#fff;">${z.zoneName} (${z.zoneCode})</strong>
                        <div style="font-size:0.78rem; color:var(--text-secondary); margin-top:2px;">
                            Actual Power: $${z.actualPowerExpense.toFixed(2)} | Repairs: $${z.actualRepairExpense.toFixed(2)}
                        </div>
                    </div>
                    <div style="text-align:right;">
                        <span class="pole-status-pill ${z.status === 'OVER_BUDGET' ? 'status-pill-fault' : 'status-pill-normal'}">${z.status}</span>
                        <div style="font-family:var(--font-mono); font-size:1.1rem; font-weight:700; color:#fff; margin-top:4px;">
                            Variance: $${z.budgetVariance.toFixed(2)}
                        </div>
                    </div>
                </div>

                <div class="progress-bar-container">
                    <div class="progress-bar-fill ${isWarning ? 'warning' : ''}" style="width: ${Math.min(pctUsed, 100)}%;"></div>
                </div>
                <div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-muted);">
                    <span>Spent: $${z.totalActualExpense.toFixed(2)} (${pctUsed}%)</span>
                    <span>Budget Limit: $${z.plannedBudget.toFixed(2)}</span>
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = html;
    if (overviewContainer) overviewContainer.innerHTML = html;
}

function renderBalanceSheet(data) {
    const container = document.getElementById('balanceSheetContent');
    container.innerHTML = `
        <table class="data-table">
            <tbody>
                <tr>
                    <td><strong>Assets</strong></td>
                    <td style="text-align:right;"></td>
                </tr>
                <tr>
                    <td style="padding-left:24px; color:var(--text-secondary);">Lighting Grid Infrastructure</td>
                    <td style="text-align:right; font-family:var(--font-mono);">$${data.gridInfrastructureAssetValue.toFixed(2)}</td>
                </tr>
                <tr>
                    <td style="padding-left:24px; color:var(--text-secondary);">Bank & Cash Reserves</td>
                    <td style="text-align:right; font-family:var(--font-mono); color:#34d399;">$${data.bankReserves.toFixed(2)}</td>
                </tr>
                <tr style="border-top:1px solid var(--border-color); font-weight:700;">
                    <td>Total Assets</td>
                    <td style="text-align:right; font-family:var(--font-mono); color:var(--primary); font-size:1.05rem;">$${data.totalAssets.toFixed(2)}</td>
                </tr>

                <tr>
                    <td style="padding-top:20px;"><strong>Liabilities & Equity</strong></td>
                    <td style="text-align:right;"></td>
                </tr>
                <tr>
                    <td style="padding-left:24px; color:var(--text-secondary);">Power Utility Payables</td>
                    <td style="text-align:right; font-family:var(--font-mono); color:#f87171;">$${data.utilityPayables.toFixed(2)}</td>
                </tr>
                <tr>
                    <td style="padding-left:24px; color:var(--text-secondary);">Retained Earnings / Surplus</td>
                    <td style="text-align:right; font-family:var(--font-mono);">$${data.retainedEarnings.toFixed(2)}</td>
                </tr>
                <tr style="border-top:1px solid var(--border-color); font-weight:700;">
                    <td>Total Liabilities & Equity</td>
                    <td style="text-align:right; font-family:var(--font-mono); font-size:1.05rem;">$${data.totalLiabilitiesAndEquity.toFixed(2)}</td>
                </tr>
            </tbody>
        </table>
    `;
}

function renderProfitLoss(data) {
    const container = document.getElementById('profitLossContent');
    const isProfit = data.netProfitLoss >= 0;

    container.innerHTML = `
        <table class="data-table">
            <tbody>
                <tr>
                    <td><strong>Income</strong></td>
                    <td style="text-align:right;"></td>
                </tr>
                <tr>
                    <td style="padding-left:24px; color:var(--text-secondary);">Municipal Grid Service Fees</td>
                    <td style="text-align:right; font-family:var(--font-mono); color:#34d399;">$${data.municipalServiceFeesIncome.toFixed(2)}</td>
                </tr>
                <tr style="border-top:1px solid var(--border-color); font-weight:700;">
                    <td>Total Income</td>
                    <td style="text-align:right; font-family:var(--font-mono); color:#34d399; font-size:1.05rem;">$${data.totalIncome.toFixed(2)}</td>
                </tr>

                <tr>
                    <td style="padding-top:20px;"><strong>Operating Expenses</strong></td>
                    <td style="text-align:right;"></td>
                </tr>
                <tr>
                    <td style="padding-left:24px; color:var(--text-secondary);">Electricity Consumption Expense</td>
                    <td style="text-align:right; font-family:var(--font-mono);">$${data.electricityConsumptionExpense.toFixed(2)}</td>
                </tr>
                <tr>
                    <td style="padding-left:24px; color:var(--text-secondary);">Repair & Maintenance Expense</td>
                    <td style="text-align:right; font-family:var(--font-mono);">$${data.repairMaintenanceExpense.toFixed(2)}</td>
                </tr>
                <tr style="border-top:1px solid var(--border-color); font-weight:700;">
                    <td>Total Expenses</td>
                    <td style="text-align:right; font-family:var(--font-mono); color:#f87171; font-size:1.05rem;">$${data.totalExpenses.toFixed(2)}</td>
                </tr>

                <tr style="border-top:2px solid var(--border-highlight); font-weight:800; font-size:1.1rem; background:rgba(0,0,0,0.25);">
                    <td>Net Profit / (Loss)</td>
                    <td style="text-align:right; font-family:var(--font-mono); color:${isProfit ? '#34d399' : '#f87171'};">
                        ${isProfit ? '$' : '-$'}${Math.abs(data.netProfitLoss).toFixed(2)}
                    </td>
                </tr>
            </tbody>
        </table>
    `;
}

// 6. Master Data
async function loadMasterData() {
    try {
        const [contactsRes, productsRes, accountsRes, zonesRes] = await Promise.all([
            fetch(`${API_BASE}/contacts`),
            fetch(`${API_BASE}/products`),
            fetch(`${API_BASE}/accounts`),
            fetch(`${API_BASE}/zones`)
        ]);

        state.contacts = await contactsRes.json();
        state.products = await productsRes.json();
        state.accounts = await accountsRes.json();
        state.zones = await zonesRes.json();

        renderMasterDataTables();
        populateFormDropdowns();
    } catch (err) {
        console.error('Failed to load master data:', err);
    }
}

function renderMasterDataTables() {
    // Contacts Table
    const contactsTbody = document.getElementById('masterContactsTable');
    contactsTbody.innerHTML = state.contacts.map(c => `
        <tr>
            <td><strong>${c.name}</strong></td>
            <td><span class="pole-status-pill status-pill-offline">${c.type}</span></td>
            <td>${c.email || '-'}</td>
            <td>${c.phone || '-'}</td>
            <td>${c.address || '-'}</td>
        </tr>
    `).join('');

    // Products Table
    const productsTbody = document.getElementById('masterProductsTable');
    productsTbody.innerHTML = state.products.map(p => `
        <tr>
            <td><span style="font-family:var(--font-mono); color:var(--primary); font-weight:600;">${p.code}</span></td>
            <td><strong>${p.name}</strong></td>
            <td><span class="pole-status-pill status-pill-offline">${p.productType}</span></td>
            <td style="font-family:var(--font-mono); font-weight:600;">$${p.unitPrice.toFixed(2)}</td>
        </tr>
    `).join('');

    // Accounts Table
    const accountsTbody = document.getElementById('masterAccountsTable');
    accountsTbody.innerHTML = state.accounts.map(a => `
        <tr>
            <td><span style="font-family:var(--font-mono); color:var(--primary); font-weight:600;">${a.code}</span></td>
            <td><strong>${a.name}</strong></td>
            <td><span class="pole-status-pill status-pill-offline">${a.accountType}</span></td>
            <td style="font-family:var(--font-mono); font-weight:600; color:#fff;">$${a.balance.toFixed(2)}</td>
        </tr>
    `).join('');
}

function populateFormDropdowns() {
    // Vendors
    const poVendor = document.getElementById('poVendorSelect');
    poVendor.innerHTML = state.contacts
        .filter(c => c.type.includes('VENDOR'))
        .map(v => `<option value="${v.id}">${v.name} (${v.type})</option>`).join('');

    // Products
    const poProduct = document.getElementById('poProductSelect');
    poProduct.innerHTML = state.products
        .map(p => `<option value="${p.id}">${p.name} - $${p.unitPrice}</option>`).join('');

    // Zones
    const poZone = document.getElementById('poZoneSelect');
    poZone.innerHTML = state.zones
        .map(z => `<option value="${z.id}">${z.name} (${z.zoneCode})</option>`).join('');

    // Customers
    const soCustomer = document.getElementById('soCustomerSelect');
    soCustomer.innerHTML = state.contacts
        .filter(c => c.type.includes('CUSTOMER'))
        .map(c => `<option value="${c.id}">${c.name}</option>`).join('');

    // SO Products
    const soProduct = document.getElementById('soProductSelect');
    soProduct.innerHTML = state.products
        .map(p => `<option value="${p.id}">${p.name}</option>`).join('');
}

// 7. KPIs
async function loadKPIs() {
    try {
        const res = await fetch(`${API_BASE}/streetlights/power-summary`);
        state.powerSummary = await res.json();

        document.getElementById('kpiTotalPoles').innerText = state.powerSummary.totalStreetLights;
        document.getElementById('kpiActivePoles').innerText = `${state.powerSummary.activeStreetLights} active | ${state.powerSummary.faultStreetLights} fault`;
        document.getElementById('kpiCurrentLoad').innerHTML = `${state.powerSummary.estimatedHourlyKWh.toFixed(2)} <span class="unit">kW</span>`;
        document.getElementById('kpiAvgDimming').innerText = `Avg. Dimming: ${state.powerSummary.averageDimmingPercentage.toFixed(0)}%`;
        document.getElementById('kpiMonthlyCost').innerText = `$${state.powerSummary.estimatedMonthlyPowerCost.toFixed(2)}`;
    } catch (err) {
        console.error('Failed to load power summary:', err);
    }
}

// Toast Notifications Helper
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '🚨';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}
