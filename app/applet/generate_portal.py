import base64

b64_logo = base64.b64encode(open('/app/applet/app/src/main/res/drawable/market_logo.png', 'rb').read()).decode('utf-8')
data_uri = f'data:image/png;base64,{b64_logo}'

html_template = """<!DOCTYPE html>
<html lang="hi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Naseeb Lal Market - Kirayedaar Self-Service Portal</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg-page: #F8FAFC;
      --card-bg: #FFFFFF;
      --card-border: #E2E8F0;
      --gold-dark: #B45309;
      --gold-primary: #D97706;
      --gold-light: #FEF3C7;
      --text-main: #0F172A;
      --text-muted: #64748B;
      --text-sub: #334155;
      --accent-green: #059669;
      --accent-red: #DC2626;
      --accent-blue: #2563EB;
      --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.05);
      --shadow-md: 0 4px 20px rgba(0, 0, 0, 0.06);
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-tap-highlight-color: transparent;
    }

    body {
      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background-color: var(--bg-page);
      background-image: radial-gradient(#E2E8F0 1.2px, transparent 1.2px);
      background-size: 20px 20px;
      color: var(--text-main);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16px 12px 48px;
    }

    .main-container {
      width: 100%;
      max-width: 600px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    /* Header & Exact Logo */
    .header-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-top: 5px solid var(--gold-primary);
      border-radius: 18px;
      padding: 24px 16px 20px;
      text-align: center;
      box-shadow: var(--shadow-md);
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    /* EXACT ORIGINAL LOGO IMAGE: NO CIRCULAR CLIPPING, 100% ORIGINAL */
    .logo-container {
      margin-bottom: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .logo-exact {
      max-width: 160px;
      max-height: 160px;
      width: auto;
      height: auto;
      object-fit: contain;
      display: block;
      filter: drop-shadow(0 3px 8px rgba(0, 0, 0, 0.08));
    }

    .brand-title {
      font-size: 1.45rem;
      font-weight: 800;
      letter-spacing: 0.5px;
      color: #0F172A;
      margin-bottom: 4px;
      text-transform: uppercase;
    }

    .brand-subtitle {
      font-size: 0.88rem;
      color: var(--gold-dark);
      font-weight: 700;
      letter-spacing: 0.5px;
      text-transform: uppercase;
    }

    .brand-contact {
      margin-top: 10px;
      font-size: 0.82rem;
      color: var(--text-muted);
      background: #F8FAFC;
      border: 1px solid #E2E8F0;
      padding: 6px 16px;
      border-radius: 20px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }

    .brand-contact strong {
      color: #0F172A;
    }

    /* Search Bar fallback */
    .search-box {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      box-shadow: var(--shadow-sm);
    }

    .search-row {
      display: flex;
      gap: 8px;
    }

    .input-field {
      width: 100%;
      background: #FFFFFF;
      border: 1px solid #CBD5E1;
      border-radius: 12px;
      padding: 12px 14px;
      font-size: 0.95rem;
      color: #0F172A;
      outline: none;
      transition: all 0.2s;
    }

    .input-field:focus {
      border-color: var(--gold-primary);
      box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.15);
    }

    .btn-search {
      background: var(--gold-dark);
      color: #FFFFFF;
      font-weight: 700;
      border: none;
      border-radius: 12px;
      padding: 0 20px;
      font-size: 0.95rem;
      cursor: pointer;
      white-space: nowrap;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 48px;
    }

    /* Bill Card */
    .bill-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 20px 18px;
      box-shadow: var(--shadow-md);
    }

    .card-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      border-bottom: 1px solid #F1F5F9;
      padding-bottom: 14px;
      margin-bottom: 16px;
    }

    .tenant-info h2 {
      font-size: 1.28rem;
      font-weight: 800;
      color: #0F172A;
      margin-bottom: 4px;
    }

    .unit-tag {
      display: inline-block;
      font-size: 0.82rem;
      font-weight: 700;
      background: #FEF3C7;
      color: #92400E;
      border: 1px solid #FDE68A;
      padding: 4px 10px;
      border-radius: 8px;
    }

    .status-badge {
      font-size: 0.75rem;
      font-weight: 800;
      letter-spacing: 0.5px;
      padding: 6px 12px;
      border-radius: 20px;
      text-transform: uppercase;
    }

    .status-pending {
      background: #FEE2E2;
      color: #B91C1C;
      border: 1px solid #FECACA;
    }

    .status-paid {
      background: #D1FAE5;
      color: #047857;
      border: 1px solid #A7F3D0;
    }

    .status-partial {
      background: #FEF3C7;
      color: #B45309;
      border: 1px solid #FDE68A;
    }

    /* Due Amounts Grid */
    .amount-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      margin-bottom: 16px;
    }

    .amount-item {
      background: #F8FAFC;
      border: 1px solid #E2E8F0;
      border-radius: 14px;
      padding: 12px 14px;
    }

    .amount-label {
      font-size: 0.75rem;
      color: var(--text-muted);
      text-transform: uppercase;
      font-weight: 700;
      margin-bottom: 4px;
    }

    .amount-val {
      font-size: 1.15rem;
      font-weight: 800;
      color: #0F172A;
    }

    .amount-due-highlight {
      grid-column: 1 / -1;
      background: linear-gradient(135deg, #FFFBEB, #FEF3C7);
      border: 1.5px solid #FCD34D;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 18px;
      border-radius: 14px;
    }

    .amount-due-highlight .amount-label {
      color: #92400E;
    }

    .amount-due-highlight .amount-val {
      font-size: 1.7rem;
      color: #B45309;
      font-weight: 800;
    }

    /* Meta Details */
    .meta-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.85rem;
      color: var(--text-muted);
      padding: 8px 0;
      border-bottom: 1px solid #F1F5F9;
    }

    .meta-row strong {
      color: #1E293B;
      font-weight: 600;
    }

    /* Promise Alert Banner */
    .promise-banner {
      background: #EFF6FF;
      border: 1px solid #BFDBFE;
      border-radius: 12px;
      padding: 12px 14px;
      margin-top: 14px;
      display: flex;
      align-items: flex-start;
      gap: 10px;
      font-size: 0.85rem;
      color: #1E40AF;
    }

    /* Action Accordion */
    .action-container {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    .action-card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 18px 16px;
      box-shadow: var(--shadow-sm);
    }

    .action-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 14px;
    }

    .action-icon {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.2rem;
    }

    .icon-blue { background: #DBEAFE; color: #1E40AF; }
    .icon-green { background: #D1FAE5; color: #065F46; }

    .action-title {
      font-size: 1rem;
      font-weight: 700;
      color: #0F172A;
    }

    .action-subtitle {
      font-size: 0.78rem;
      color: var(--text-muted);
    }

    .form-group {
      margin-bottom: 12px;
    }

    .form-label {
      display: block;
      font-size: 0.82rem;
      color: #334155;
      font-weight: 600;
      margin-bottom: 6px;
    }

    .radio-group {
      display: flex;
      gap: 12px;
      margin-bottom: 12px;
    }

    .radio-option {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;
      background: #F8FAFC;
      border: 1px solid #CBD5E1;
      border-radius: 10px;
      padding: 10px;
      font-size: 0.85rem;
      font-weight: 600;
      color: #334155;
      cursor: pointer;
    }

    .radio-option input {
      accent-color: var(--gold-primary);
    }

    .btn-submit {
      width: 100%;
      min-height: 48px;
      border-radius: 12px;
      border: none;
      font-size: 0.95rem;
      font-weight: 700;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: all 0.2s;
    }

    .btn-blue {
      background: #2563EB;
      color: #FFFFFF;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
    }

    .btn-green {
      background: #059669;
      color: #FFFFFF;
      box-shadow: 0 4px 12px rgba(5, 150, 105, 0.25);
    }

    .btn-submit:active {
      transform: scale(0.98);
    }

    /* Anti Fraud Warning (Light Mode) */
    .fraud-alert {
      background: #FFFBEB;
      border: 1px solid #FCD34D;
      border-radius: 16px;
      padding: 16px;
      font-size: 0.83rem;
      color: #92400E;
      line-height: 1.5;
    }

    .fraud-alert h4 {
      font-size: 0.92rem;
      font-weight: 800;
      color: #B45309;
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 6px;
    }

    .fraud-alert ul {
      margin-left: 18px;
      margin-top: 6px;
    }

    .fraud-alert li {
      margin-bottom: 4px;
    }

    /* Quick Contact Row */
    .quick-contacts {
      display: flex;
      gap: 10px;
    }

    .btn-contact {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 12px;
      border-radius: 12px;
      text-decoration: none;
      font-size: 0.88rem;
      font-weight: 700;
      min-height: 48px;
    }

    .btn-whatsapp {
      background: #25D366;
      color: #FFFFFF;
      box-shadow: 0 4px 12px rgba(37, 211, 102, 0.25);
    }

    .btn-phone {
      background: #FFFFFF;
      border: 1px solid #CBD5E1;
      color: #0F172A;
      box-shadow: var(--shadow-sm);
    }

    /* Toast Notification */
    .toast {
      position: fixed;
      bottom: 24px;
      left: 50%;
      transform: translateX(-50%) translateY(100px);
      background: #0F172A;
      color: #FFFFFF;
      font-weight: 600;
      font-size: 0.9rem;
      padding: 12px 24px;
      border-radius: 30px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      z-index: 999;
      opacity: 0;
      pointer-events: none;
      text-align: center;
      max-width: 90vw;
    }

    .toast.show {
      transform: translateX(-50%) translateY(0);
      opacity: 1;
    }

    @media print {
      body {
        background: #FFF !important;
        color: #000 !important;
      }
      .action-container, .fraud-alert, .quick-contacts, .search-box {
        display: none !important;
      }
      .header-card, .bill-card {
        border: 1px solid #CCC !important;
        box-shadow: none !important;
        background: #FFF !important;
        color: #000 !important;
      }
    }
  </style>
</head>
<body>

  <div class="main-container">

    <!-- Header Section with Exact Original Logo (No Circle / Round Cropping) -->
    <header class="header-card">
      <div class="logo-container">
        <img src="%%DATA_URI%%" alt="Naseeb Lal Market Logo" class="logo-exact">
      </div>
      <h1 class="brand-title">NASEEB LAL MARKET</h1>
      <p class="brand-subtitle">Kirayedaar Self-Service Portal</p>
      <div class="brand-contact">
        <span>Authorized Management:</span>
        <strong>Vimal Kumar (Mob: 7654138539)</strong>
      </div>
    </header>

    <!-- Search fallback bar (used if slug is missing or invalid) -->
    <div id="searchSection" class="search-box" style="display: none;">
      <p style="font-size: 0.85rem; color: var(--text-muted); font-weight: 600;">Apna Shop / Flat Number daalkar bill khojein:</p>
      <div class="search-row">
        <input type="text" id="shopSearchInput" class="input-field" placeholder="e.g. 55 ya Pappu Kumar" />
        <button id="btnSearch" class="btn-search">Search Bill</button>
      </div>
    </div>

    <!-- Loading State -->
    <div id="loadingBox" class="bill-card" style="text-align: center; padding: 40px 20px;">
      <div style="font-size: 2rem; margin-bottom: 12px;">⏳</div>
      <p style="color: var(--gold-dark); font-weight: 700;">Kiraya Record Load Ho Raha Hai...</p>
      <p style="font-size: 0.8rem; color: var(--text-muted); margin-top: 6px;">Firebase Database se jod rahe hain...</p>
    </div>

    <!-- Error State -->
    <div id="errorBox" class="bill-card" style="display: none; text-align: center; border-color: #FECACA;">
      <div style="font-size: 2rem; margin-bottom: 8px;">⚠️</div>
      <h3 style="color: #DC2626; margin-bottom: 6px;">Record Nahi Mila</h3>
      <p id="errorMsg" style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 14px;">Diya gaya record uplabdh nahi hai.</p>
      <button onclick="showSearchMode()" class="btn-submit btn-blue" style="max-width: 200px; margin: 0 auto;">Shop No. se Khojein</button>
    </div>

    <!-- Live Bill Details Card -->
    <main id="billContent" style="display: none;" class="bill-card">
      <div class="card-top">
        <div class="tenant-info">
          <h2 id="dispTenantName">Loading...</h2>
          <span id="dispUnitNumber" class="unit-tag">Shop No. --</span>
        </div>
        <div id="dispStatusBadge" class="status-badge status-pending">PENDING</div>
      </div>

      <div class="amount-grid">
        <div class="amount-due-highlight">
          <div>
            <div class="amount-label">Kul Pending Kiraya</div>
            <div id="dispPendingAmount" class="amount-val">₹0</div>
          </div>
          <div style="font-size: 1.8rem;">⏳</div>
        </div>

        <div class="amount-item">
          <div class="amount-label">Kul Bill / Due</div>
          <div id="dispTotalDue" class="amount-val">₹0</div>
        </div>

        <div class="amount-item">
          <div class="amount-label">Jama Kiya / Paid</div>
          <div id="dispAmountPaid" class="amount-val" style="color: #059669;">₹0</div>
        </div>
      </div>

      <!-- Bill Metadata Breakdown -->
      <div class="meta-row">
        <span>Mahina / Avadhi:</span>
        <strong id="dispBillingPeriod">--</strong>
      </div>
      <div class="meta-row">
        <span>Payment Due Date:</span>
        <strong id="dispDueDate">--</strong>
      </div>
      <div class="meta-row" id="rowPmTax" style="display: none;">
        <span>PMC Market Tax:</span>
        <strong id="dispPmcTax">₹0</strong>
      </div>
      <div class="meta-row" id="rowElecBill" style="display: none;">
        <span>Electricity Bill:</span>
        <strong id="dispElecBill">₹0</strong>
      </div>

      <!-- Vada / Promise Date Alert if present -->
      <div id="dispPromiseAlert" class="promise-banner" style="display: none;">
        <span style="font-size: 1.2rem;">📅</span>
        <div>
          <div id="dispPromiseTitle" style="font-weight: 700;">Payment Vada Darj Hai</div>
          <div id="dispPromiseDesc" style="font-size: 0.8rem; margin-top: 2px;">Aapne is tarikh tak bhugtan ka vada kiya hai.</div>
        </div>
      </div>
    </main>

    <!-- Tenant Action Section -->
    <div id="actionSection" class="action-container" style="display: none;">

      <!-- Action 1: Vada Karein / Promise Date -->
      <div class="action-card">
        <div class="action-header">
          <div class="action-icon icon-blue">📅</div>
          <div>
            <div class="action-title">Expected Payment Date (Vada) Karein</div>
            <div class="action-subtitle">Aap kis tarikh tak kiraya jama karenge?</div>
          </div>
        </div>

        <form id="promiseForm" onsubmit="handlePromiseSubmit(event)">
          <div class="form-group">
            <label class="form-label" for="promiseDate">Expected Date Chunein:</label>
            <input type="date" id="promiseDate" class="input-field" required>
          </div>
          <div class="form-group">
            <label class="form-label" for="promiseNote">Koi Chhota Note / Wajah (Optional):</label>
            <input type="text" id="promiseNote" class="input-field" placeholder="e.g. Bank se paise aane par jama karenge">
          </div>
          <button type="submit" id="btnSubmitPromise" class="btn-submit btn-blue">
            <span>📅</span> Vada / Expected Date Submit Karein
          </button>
        </form>
      </div>

      <!-- Action 2: I Have Paid / Bhugtan Jama Kiya Hai -->
      <div class="action-card">
        <div class="action-header">
          <div class="action-icon icon-green">💳</div>
          <div>
            <div class="action-title">Maine Kiraya Jama Kar Diya Hai</div>
            <div class="action-subtitle">Receipt paane ke liye payment proof / UTR darj karein</div>
          </div>
        </div>

        <form id="paymentClaimForm" onsubmit="handleClaimSubmit(event)">
          <div class="radio-group">
            <label class="radio-option">
              <input type="radio" name="payMode" value="Online" checked onchange="toggleModeFields('Online')">
              <span>Online (UPI/Bank)</span>
            </label>
            <label class="radio-option">
              <input type="radio" name="payMode" value="Cash" onchange="toggleModeFields('Cash')">
              <span>Cash (Rokad)</span>
            </label>
          </div>

          <div class="form-group" id="groupUtr">
            <label class="form-label" for="claimUtr">Bank UTR / Transaction Ref No.:</label>
            <input type="text" id="claimUtr" class="input-field" placeholder="e.g. 426819284756 (12 Digits)">
          </div>

          <div class="form-group" id="groupCashNote" style="display: none;">
            <label class="form-label" for="claimCashNote">Cash Kisko Diya / Detail:</label>
            <input type="text" id="claimCashNote" class="input-field" placeholder="e.g. Vimal Kumar ji ko office me diya">
          </div>

          <button type="submit" id="btnSubmitClaim" class="btn-submit btn-green">
            <span>✅</span> Payment Verify Suchna Bhejein
          </button>
        </form>
      </div>

      <!-- Anti-Fraud Security Notice -->
      <div class="fraud-alert">
        <h4>🛡️ Anti-Fraud & Security Policy</h4>
        <p>Naseeb Lal Market Management ki taraf se spasht chetavni:</p>
        <ul>
          <li>Kisi bhi anjaan UPI link ya online payment gateway par bhugtan <strong>KADAPI NA KAREIN</strong>.</li>
          <li>Market ka koi automatic payment link nahi hai. Bhugtan sirf authorized prabandhak ko hi karein.</li>
          <li>Online transfer ke baad upar apna UTR number darj karein taaki fraud-check verify ho sake.</li>
        </ul>
      </div>

      <!-- Quick WhatsApp / Phone Call Buttons -->
      <div class="quick-contacts">
        <a id="linkCallManagement" href="tel:+917654138539" class="btn-contact btn-phone">
          <span>📞</span> Call Management
        </a>
        <a id="linkWaManagement" href="https://wa.me/917654138539" target="_blank" class="btn-contact btn-whatsapp">
          <span>💬</span> WhatsApp
        </a>
      </div>

    </div>

  </div>

  <!-- Toast Notification -->
  <div id="toast" class="toast">Aapka anurodh safal raha!</div>

  <script>
    const FIREBASE_BASE = "https://nasseblalmarkt-default-rtdb.firebaseio.com";
    let currentRent = null;
    let currentRentKey = null;
    let allRents = {};
    let allTenants = {};

    function formatCurrency(val) {
      const num = Number(val) || 0;
      return '₹' + Math.round(num).toLocaleString('en-IN');
    }

    function showToast(msg) {
      const t = document.getElementById('toast');
      t.innerText = msg;
      t.classList.add('show');
      setTimeout(() => t.classList.remove('show'), 3500);
    }

    function toggleModeFields(mode) {
      document.getElementById('groupUtr').style.display = mode === 'Online' ? 'block' : 'none';
      document.getElementById('groupCashNote').style.display = mode === 'Cash' ? 'block' : 'none';
    }

    function showSearchMode() {
      document.getElementById('loadingBox').style.display = 'none';
      document.getElementById('errorBox').style.display = 'none';
      document.getElementById('billContent').style.display = 'none';
      document.getElementById('actionSection').style.display = 'none';
      document.getElementById('searchSection').style.display = 'block';
    }

    function getQueryParam(name) {
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has(name)) return urlParams.get(name);
      if (window.location.hash.includes(name + '=')) {
        const parts = window.location.hash.substring(1).split('&');
        for (const p of parts) {
          const [k, v] = p.split('=');
          if (k === name) return decodeURIComponent(v);
        }
      }
      return null;
    }

    async function initPortal() {
      const echoSlug = getQueryParam('echo') || getQueryParam('slug') || getQueryParam('id');
      const shopParam = getQueryParam('shop') || getQueryParam('unit');

      try {
        const [rentsRes, tenantsRes] = await Promise.all([
          fetch(`${FIREBASE_BASE}/rents.json`).then(r => r.json()),
          fetch(`${FIREBASE_BASE}/tenants.json`).then(r => r.json())
        ]);

        allRents = rentsRes || {};
        allTenants = tenantsRes || {};

        if (echoSlug) {
          findAndDisplayRentBySlug(echoSlug);
        } else if (shopParam) {
          findAndDisplayRentByShop(shopParam);
        } else {
          showSearchMode();
        }
      } catch (err) {
        console.error('Firebase load error:', err);
        document.getElementById('loadingBox').style.display = 'none';
        document.getElementById('errorBox').style.display = 'block';
        document.getElementById('errorMsg').innerText = "Database connection me samasya aayi: " + err.message;
      }
    }

    function findAndDisplayRentBySlug(slug) {
      const cleanSlug = slug.trim().toLowerCase();
      let foundKey = null;
      let foundRent = null;

      for (const [key, r] of Object.entries(allRents)) {
        if (!r) continue;
        const testSlug1 = `${r.tenantId}_${r.month}_${r.year}`.toLowerCase();
        const testSlug2 = `t${r.shopNumber}_${r.month}_${r.year}`.toLowerCase();
        const testSlug3 = `shop_${r.shopNumber}_${r.month}_${r.year}`.toLowerCase();
        const testKey = key.toLowerCase();

        if (cleanSlug === testSlug1 || cleanSlug === testSlug2 || cleanSlug === testSlug3 || cleanSlug === testKey) {
          foundKey = key;
          foundRent = r;
          break;
        }
      }

      if (!foundRent) {
        for (const [key, r] of Object.entries(allRents)) {
          if (!r) continue;
          if (cleanSlug.includes(String(r.shopNumber).toLowerCase()) && 
              cleanSlug.includes(String(r.month).toLowerCase())) {
            foundKey = key;
            foundRent = r;
            break;
          }
        }
      }

      if (foundRent) {
        renderRent(foundKey, foundRent);
      } else {
        document.getElementById('loadingBox').style.display = 'none';
        document.getElementById('errorBox').style.display = 'block';
        document.getElementById('errorMsg').innerText = `Rent record slug "${slug}" nahi mila. Kripya apna Shop number search karein.`;
      }
    }

    function findAndDisplayRentByShop(query) {
      const q = query.trim().toLowerCase();
      let bestKey = null;
      let bestRent = null;

      for (const [key, r] of Object.entries(allRents)) {
        if (!r) continue;
        const sNum = String(r.shopNumber).toLowerCase();
        const tName = String(r.tenantName).toLowerCase();
        if (sNum === q || tName.includes(q)) {
          bestKey = key;
          bestRent = r;
          if (r.pendingAmount > 0) break;
        }
      }

      if (bestRent) {
        renderRent(bestKey, bestRent);
      } else {
        showToast("Koi record nahi mila. Shop number dobara check karein.");
        showSearchMode();
      }
    }

    function renderRent(key, rent) {
      currentRentKey = key;
      currentRent = rent;

      document.getElementById('loadingBox').style.display = 'none';
      document.getElementById('errorBox').style.display = 'none';
      document.getElementById('searchSection').style.display = 'none';
      document.getElementById('billContent').style.display = 'block';
      document.getElementById('actionSection').style.display = 'flex';

      document.getElementById('dispTenantName').innerText = rent.tenantName || 'Kirayedaar';
      document.getElementById('dispUnitNumber').innerText = rent.isPersonal ? `Flat No. ${rent.shopNumber}` : `Shop No. ${rent.shopNumber}`;

      const statusBadge = document.getElementById('dispStatusBadge');
      const st = (rent.status || 'PENDING').toUpperCase();
      statusBadge.innerText = st;
      statusBadge.className = 'status-badge ' + (st === 'PAID' ? 'status-paid' : st === 'PARTIAL' ? 'status-partial' : 'status-pending');

      document.getElementById('dispPendingAmount').innerText = formatCurrency(rent.pendingAmount);
      document.getElementById('dispTotalDue').innerText = formatCurrency(rent.amountDue);
      document.getElementById('dispAmountPaid').innerText = formatCurrency(rent.amountPaid);

      const periodLabel = (rent.month.toLowerCase().includes('yearly') || rent.month.toLowerCase().includes('varshik'))
        ? 'Varshik (Yearly) ' + rent.year
        : `${rent.month} ${rent.year}`;
      document.getElementById('dispBillingPeriod').innerText = periodLabel;
      document.getElementById('dispDueDate').innerText = rent.dueDate || '10 Tarikh';

      if (rent.pmcTax && rent.pmcTax > 0) {
        document.getElementById('rowPmTax').style.display = 'flex';
        document.getElementById('dispPmcTax').innerText = formatCurrency(rent.pmcTax);
      }
      if (rent.electricityBill && rent.electricityBill > 0) {
        document.getElementById('rowElecBill').style.display = 'flex';
        document.getElementById('dispElecBill').innerText = formatCurrency(rent.electricityBill);
      }

      const promiseAlert = document.getElementById('dispPromiseAlert');
      if (rent.promisedDate && rent.promisedDate.trim() !== '') {
        promiseAlert.style.display = 'flex';
        document.getElementById('dispPromiseTitle').innerText = `Payment Vada: ${rent.promisedDate}`;
        document.getElementById('dispPromiseDesc').innerText = rent.promiseNote ? `Note: "${rent.promiseNote}"` : `Aapne is tarikh tak bhugtan ka vada kiya hai.`;
      } else {
        promiseAlert.style.display = 'none';
      }

      const todayIso = new Date().toISOString().split('T')[0];
      document.getElementById('promiseDate').min = todayIso;
    }

    async function handlePromiseSubmit(e) {
      e.preventDefault();
      if (!currentRentKey) return;

      const dateVal = document.getElementById('promiseDate').value;
      const noteVal = document.getElementById('promiseNote').value.trim();
      const btn = document.getElementById('btnSubmitPromise');

      if (!dateVal) {
        alert("Kripya ek valid tarikh chunein.");
        return;
      }

      const dObj = new Date(dateVal);
      const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
      const formattedDate = `${dObj.getDate()} ${months[dObj.getMonth()]} ${dObj.getFullYear()}`;

      btn.disabled = true;
      btn.innerText = "Saving to Database...";

      try {
        const updatePayload = {
          promisedDate: formattedDate,
          promiseNote: noteVal || 'Tenant portal se vada darj kiya'
        };

        await fetch(`${FIREBASE_BASE}/rents/${currentRentKey}.json`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updatePayload)
        });

        await fetch(`${FIREBASE_BASE}/tenant_echo/${currentRentKey}.json`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            tenantId: currentRent.tenantId,
            shopNumber: currentRent.shopNumber,
            tenantName: currentRent.tenantName,
            promisedDate: formattedDate,
            promiseNote: noteVal,
            updatedAt: new Date().toISOString()
          })
        });

        currentRent.promisedDate = formattedDate;
        currentRent.promiseNote = noteVal;

        renderRent(currentRentKey, currentRent);
        showToast(`✅ Vada safaltapoorvak darj! Expected Date: ${formattedDate}`);
      } catch (err) {
        alert("Server error: " + err.message);
      } finally {
        btn.disabled = false;
        btn.innerHTML = `<span>📅</span> Vada / Expected Date Submit Karein`;
      }
    }

    async function handleClaimSubmit(e) {
      e.preventDefault();
      if (!currentRentKey) return;

      const mode = document.querySelector('input[name="payMode"]:checked').value;
      const utr = document.getElementById('claimUtr').value.trim();
      const cashNote = document.getElementById('claimCashNote').value.trim();
      const btn = document.getElementById('btnSubmitClaim');

      if (mode === 'Online' && !utr) {
        alert("Kripya apna 12-digit UTR ya Bank Transaction Reference number dalein.");
        return;
      }

      btn.disabled = true;
      btn.innerText = "Submitting Proof...";

      try {
        const claimPayload = {
          claimedPaid: true,
          claimedPaidMode: mode,
          claimedPaidRef: mode === 'Online' ? utr : cashNote,
          claimedPaidAt: new Date().toISOString(),
          tenantId: currentRent.tenantId,
          shopNumber: currentRent.shopNumber,
          tenantName: currentRent.tenantName
        };

        await fetch(`${FIREBASE_BASE}/tenant_echo/${currentRentKey}.json`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(claimPayload)
        });

        await fetch(`${FIREBASE_BASE}/rents/${currentRentKey}.json`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            promiseNote: `[Payment Claimed via ${mode}: ${mode === 'Online' ? utr : cashNote}]`
          })
        });

        showToast("✅ Payment suchna bhej di gayi hai! Management UTR verify karegi.");
        alert("Aapka payment proof submit ho gaya hai! Management se verification ke baad aapki digital receipt unlock ho jayegi. Shukriya - Naseeb Lal Market");
      } catch (err) {
        alert("Submission failed: " + err.message);
      } finally {
        btn.disabled = false;
        btn.innerHTML = `<span>✅</span> Payment Verify Suchna Bhejein`;
      }
    }

    document.getElementById('btnSearch').addEventListener('click', () => {
      const q = document.getElementById('shopSearchInput').value;
      if (!q.trim()) {
        alert("Kripya Shop No. ya Naam dalein.");
        return;
      }
      findAndDisplayRentByShop(q);
    });

    document.getElementById('shopSearchInput').addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        document.getElementById('btnSearch').click();
      }
    });

    window.addEventListener('DOMContentLoaded', initPortal);
  </script>
</body>
</html>"""

final_html = html_template.replace('%%DATA_URI%%', data_uri)

for path in ['/app/applet/public/index.html', '/app/applet/app/public/index.html']:
    with open(path, 'w') as f:
        f.write(final_html)

print('Success: Generated White Theme with Exact Original Logo and no app open button!')
