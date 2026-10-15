const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');

function buildDocumentationPDF(outputPath) {
    const doc = new PDFDocument({
        size: 'A4',
        margins: { top: 40, bottom: 45, left: 45, right: 45 },
        bufferPages: true
    });

    const stream = fs.createWriteStream(outputPath);
    doc.pipe(stream);

    // Color Palette
    const cNavy = '#0A192F';
    const cBlue = '#1E3A8A';
    const cLightBlue = '#EFF6FF';
    const cGold = '#D97706';
    const cGoldBg = '#FEF3C7';
    const cDark = '#0F172A';
    const cGray = '#475569';
    const cLightGray = '#F8FAFC';
    const cBorder = '#E2E8F0';
    const cGreen = '#059669';

    function addHeader(title, subtitle) {
        // Navy top bar
        doc.rect(0, 0, 595, 75).fill(cNavy);
        doc.rect(0, 75, 595, 4).fill(cGold);

        doc.fillColor('#FFFFFF').fontSize(16).font('Helvetica-Bold')
           .text('NASEEB LAL MARKET — COMMERCIAL BILLING SUITE', 45, 22);
        
        doc.fillColor('#FDE68A').fontSize(10).font('Helvetica')
           .text('Official Technical Architecture, Database Specifications & System Lifecycle Documentation', 45, 42);

        doc.fillColor('#94A3B8').fontSize(8)
           .text('Target: Android 14+ | Kotlin 2.0 & Jetpack Compose | Google Firebase Cloud | Enterprise Version 2.4', 45, 58);

        doc.y = 95;
    }

    function addSection(title) {
        if (doc.y > 700) doc.addPage();
        doc.moveDown(0.6);
        const y = doc.y;
        doc.rect(45, y, 505, 22).fill(cNavy);
        doc.rect(45, y, 4, 22).fill(cGold);
        doc.fillColor('#FFFFFF').fontSize(10.5).font('Helvetica-Bold')
           .text(title, 56, y + 6);
        doc.y = y + 28;
    }

    function addSubSection(title) {
        if (doc.y > 720) doc.addPage();
        doc.moveDown(0.4);
        doc.fillColor(cBlue).fontSize(10).font('Helvetica-Bold')
           .text(title, 45);
        doc.moveDown(0.2);
    }

    function addParagraph(text) {
        if (doc.y > 740) doc.addPage();
        doc.fillColor(cDark).fontSize(8.5).font('Helvetica')
           .text(text, 45, doc.y, { align: 'justify', lineGap: 2.5 });
        doc.moveDown(0.4);
    }

    function addBullet(title, desc) {
        if (doc.y > 745) doc.addPage();
        doc.fillColor(cDark).fontSize(8.5).font('Helvetica-Bold')
           .text(`• ${title}: `, 48, doc.y, { continued: true });
        doc.fillColor(cGray).font('Helvetica')
           .text(desc, { align: 'justify', lineGap: 2 });
        doc.moveDown(0.3);
    }

    function addCardBox(title, bodyText, bgColor = cLightBlue, borderColor = '#BFDBFE') {
        if (doc.y > 690) doc.addPage();
        const startY = doc.y;
        doc.font('Helvetica').fontSize(8.5);
        const height = doc.heightOfString(bodyText, { width: 485, lineGap: 2 }) + 26;

        doc.rect(45, startY, 505, height).fill(bgColor);
        doc.rect(45, startY, 505, height).stroke(borderColor);

        doc.fillColor(cBlue).fontSize(9.5).font('Helvetica-Bold')
           .text(title, 55, startY + 6);

        doc.fillColor(cDark).fontSize(8.5).font('Helvetica')
           .text(bodyText, 55, startY + 20, { width: 485, lineGap: 2 });

        doc.y = startY + height + 8;
    }

    function addTable(headers, rows, colWidths) {
        if (doc.y > 670) doc.addPage();
        const startX = 45;
        let y = doc.y;

        // Header Row
        doc.rect(startX, y, 505, 18).fill('#E2E8F0');
        doc.rect(startX, y, 505, 18).stroke(cBorder);

        let curX = startX;
        headers.forEach((h, i) => {
            doc.fillColor(cDark).fontSize(8).font('Helvetica-Bold')
               .text(h, curX + 4, y + 5, { width: colWidths[i] - 8, align: 'left' });
            curX += colWidths[i];
        });

        y += 18;

        // Data Rows
        rows.forEach((row, rIdx) => {
            if (y > 750) {
                doc.addPage();
                y = 45;
            }
            const isAlt = rIdx % 2 === 1;
            if (isAlt) {
                doc.rect(startX, y, 505, 16).fill('#F8FAFC');
            }
            doc.rect(startX, y, 505, 16).stroke(cBorder);

            curX = startX;
            row.forEach((cell, cIdx) => {
                doc.fillColor(cIdx === 0 ? cBlue : cDark).fontSize(7.5).font(cIdx === 0 ? 'Helvetica-Bold' : 'Helvetica')
                   .text(cell, curX + 4, y + 4, { width: colWidths[cIdx] - 8, align: 'left' });
                curX += colWidths[cIdx];
            });
            y += 16;
        });

        doc.y = y + 8;
    }

    // ================= PAGE 1 =================
    addHeader();

    addSection('1. EXECUTIVE OVERVIEW & BUSINESS PURPOSE');
    addParagraph(
        'Naseeb Lal Market (NLM) Commercial Billing Suite is an enterprise Android ERP built for property managers, market owners, and administrators. It automates commercial shops and residential flats billing, eliminating paper registers, untracked partial dues, delayed electric meter billing, and payment disputes through real-time cloud data and self-service portals.'
    );

    addCardBox(
        '🎯 Core Mission & Problem It Solves',
        'Manual paper registers cause lost revenue from untracked partial payments, forgotten annual rent increments, delayed sub-meter readings, and dispute-prone manual receipts. Naseeb Lal Market Suite unifies billing, sub-meter electric utilities, municipal taxes, automated WhatsApp/SMS reminders, and QR-authenticated PDF receipts into a single synchronized mobile and cloud ecosystem.',
        cGoldBg, '#FCD34D'
    );

    addSection('2. COMPLETE DATABASE ARCHITECTURE & CLOUD STORAGE');
    addParagraph(
        'The application employs Google Firebase Realtime Database as its cloud backend. It operates an offline-first architecture with Android SharedPreferences JSON caching and Server-Sent Events (SSE) streaming for sub-second synchronization across all administrator devices.'
    );

    addSubSection('Database Identity & Infrastructure');
    addBullet('Database Engine', 'Google Firebase Realtime Database (Cloud NoSQL JSON Document Store)');
    addBullet('Live Backend URL', 'https://nasseblalmarkt-default-rtdb.firebaseio.com/');
    addBullet('Sync Protocol', 'HTTP/2 Streaming via Server-Sent Events (SSE) /text/event-stream + REST API via OkHttp3');
    addBullet('Offline Persistence', 'Local Android SharedPreferences cache ("naseeb_lal_market_cache") guarantees full functionality with zero network');
    addBullet('Network Reconnect', 'Android ConnectivityManager.NetworkCallback triggers automated snapshot pull and SSE stream re-establishment');

    addSubSection('Cloud Database Collections & Schemas');
    addParagraph('All application state is structured into 10 root nodes in Firebase:');

    const schemaTable = [
        ['/shops', 'Master physical units', 'id, shopNumber, floor, baseRent, isOccupied, currentTenantId, isPersonal, notes'],
        ['/tenants', 'Master tenant profiles', 'id, name, businessName, phone, shopNumber, shopCount, monthlyRent, deposit, cycleMonths, incPct, elecBill, previousDues, isPersonal'],
        ['/rents', 'Monthly ledger & billing', 'id, tenantId, month, year, amountDue, amountPaid, pendingAmount, status, isPartial, elecUnits, elecBill, pmcTax, priorArrears, receiptNumber'],
        ['/tenant_echo', 'Two-Way Web Portals', 'echoKey, claimedPaid, claimedPaidNote, promisedDate, promisedNote, lastInteractionTime, lastPortalAccessTime'],
        ['/activity_logs', 'Immutable Audit Trail', 'id, timestamp, action, details, performedBy, userRole, severity'],
        ['/sub_admins', 'RBAC User Accounts', 'id, username, passwordHash, displayName, phone, permissions, isActive, createdAt, lastLoginAt'],
        ['/monthly_reminders', 'Batch Reminder Tracker', 'month_year key, sentTimestamp, sentCount, skippedCount, channel'],
        ['/market_notices', 'Broadcast Notice Board', 'id, title, content, datePosted, priority, isUrgent, expiresAt'],
        ['/pmc_tax_clearance', 'Municipal Tax Clearances', 'shopId, financialYear, amountPaid, receiptNo, clearedDate, verifiedBy'],
        ['/app_updates', 'Remote Version Release', 'versionCode, versionName, downloadUrl, releaseNotes, isMandatory, publishTimestamp']
    ];
    addTable(['Root Node', 'Entity Purpose', 'Key Data Fields'], schemaTable, [100, 120, 285]);

    // ================= PAGE 2 =================
    doc.addPage();
    addHeader();

    addSection('3. PRODUCTION TECHNICAL STACK & DEPENDENCIES');
    addParagraph('The suite is engineered using the modern Android native development toolkit:');

    const stackTable = [
        ['Platform & Language', 'Android 14+ (API 34) | Kotlin 2.0+ (100% Coroutines & StateFlow)'],
        ['User Interface', 'Jetpack Compose with Material Design 3 (M3) & Edge-to-Edge WindowInsets'],
        ['Architecture', 'MVVM (Model-View-ViewModel) with Unidirectional Data Flow & StateFlow reactive streams'],
        ['Cloud Database', 'Google Firebase Realtime Database with SSE live event streams & OkHttp3 client'],
        ['Local Storage', 'Encrypted SharedPreferences local JSON cache for instant offline startup'],
        ['Document Engine', 'Native Android Canvas PdfDocument (High-fidelity A4 vector rendering for receipts & reports)'],
        ['Data Export', 'CSV Spreadsheet Engine for 1-click Microsoft Excel / Google Sheets monthly ledgers'],
        ['Messaging & Queue', 'Zero-Permission ACTION_SENDTO Intent Queue (WhatsApp/SMS) + Fast2SMS REST API'],
        ['QR Code & Verification', 'ZXing Embedded Android Engine (HMAC / timestamp authenticated receipt QR codes)'],
        ['Quality Assurance', 'JUnit4 & Robolectric JVM automated testing for dues calculations and state invariants']
    ];
    addTable(['Engineering Domain', 'Selected Technology & Architecture Specification'], stackTable, [130, 375]);

    addSection('4. END-TO-END OPERATIONAL WORKFLOWS');

    addSubSection('Workflow 1: Tenant & Shop Onboarding');
    addParagraph(
        '1. Admin selects Workspace: Commercial Market (Shops) or Personal Workspace (Flats & Residential).\n' +
        '2. Admin configures tenant details: Name, Business Name, Phone Number, Assigned Unit(s), Monthly Rent, Advance Security Deposit, Billing Cycle (Monthly, 3-Mo, 6-Mo, Yearly).\n' +
        '3. Defines Annual Increment Clause: e.g., 5% after 1 year. The app automatically computes next scheduled increment.\n' +
        '4. Defines Initial Sub-Meter Electricity Reading and any initial opening balance arrears.'
    );

    addSubSection('Workflow 2: Sub-Meter Electricity & Utility Billing');
    addParagraph(
        '• Admin opens "Batch Electricity Reading" modal displaying all shops with previous meter values.\n' +
        '• Enters Current Meter Reading. The app instantaneously calculates: Consumption = Current - Previous.\n' +
        '• Applies tariff rate (₹10/unit). Total electricity charge is merged directly into the monthly bill.\n' +
        '• Handles residential flats with flat monthly electricity rates or commercial sub-metered tariffs.'
    );

    addSubSection('Workflow 3: Smart Dues & Itemized Partial Payments');
    addParagraph(
        '• Distinguishes between Paid, Partially Paid, and Pending records.\n' +
        '• Itemizes every rupee: Base Rent + Electricity Bill + PMC Property Tax + Prior Uncollected Arrears.\n' +
        '• When partial payment is accepted (e.g. ₹6,000 paid against ₹10,000 due), app logs receipt for ₹6,000 and retains ₹4,000 in the live dues ledger.'
    );

    // ================= PAGE 3 =================
    doc.addPage();
    addHeader();

    addSection('5. AUTOMATED REMINDERS & ZERO-PERMISSION ARCHITECTURE');
    addParagraph(
        'To comply with Google Play Developer Policies and eliminate dangerous SMS permissions, Naseeb Lal Market Suite uses a dual-channel messaging model:'
    );

    addBullet('Channel A: Free Sequential Intent Auto-Queue',
        'Uses standard Android ACTION_SENDTO intents with pre-filled, itemized Hindi text. Advances automatically to the next tenant upon dispatch, delivering 100% free delivery without any runtime permissions.'
    );
    addBullet('Channel B: Fast2SMS Cloud API Gateway',
        'Direct cloud SMS gateway integration using secure API tokens. Dispatches bulk SMS with 1-click cloud confirmation.'
    );
    addBullet('Itemized Hindi Messaging Format',
        'Specifies tenant name, shop number, total bill, amount received, and exact remaining balance with breakdown for rent, electricity, PMC tax, and prior arrears.'
    );

    addSection('6. TWO-WAY "TENANT ECHO" SELF-SERVICE PROTOCOL');
    addParagraph(
        'Tenant Echo is a proprietary two-way interaction layer connecting tenants without requiring app installation:'
    );

    addCardBox(
        '🌐 Public Web Portal URL',
        'Official Portal: https://naseeblalmarket.web.app/portal\nDedicated Tenant URL: https://naseeblalmarket.web.app/portal?echo={tenantId}_{month}_{year}',
        cLightBlue, '#93C5FD'
    );

    addBullet('Public Ledger Transparency', 'Tenant views real-time itemized dues, electricity consumption, and payment history.');
    addBullet('Digital Promise-to-Pay', 'Tenant commits to an expected payment date. When saved, the Admin dashboard pauses reminders until that date.');
    addBullet('Anti-Fraud Claimed Queue', 'Tenant submits payment reference / UTR. The bill is flagged in an Amber verification queue on the Admin dashboard.');
    addBullet('Instant QR-Verified Receipts', 'Tenants download verified digital PDF receipts signed by Naseeb Lal Market.');

    addSection('7. SECURITY, RBAC & IMMUTABLE AUDIT TRAILS');
    addBullet('Role-Based Access Control (RBAC)', 'Master Administrator (Vimal Kumar) has complete permissions. Sub-Admins have restricted collection and meter-entry rights.');
    addBullet('Immutable Audit Trail', 'Every financial action creates an un-editable ActivityLog with timestamp, admin identity, and before/after delta.');
    addBullet('Safe Soft-Delete Archive', 'Tenants are never accidentally erased; deletion moves them into a 30-day recoverable Deleted Archive.');

    // Page Numbers & Footer on all pages
    const pageCount = doc.bufferedPageRange().count;
    for (let i = 0; i < pageCount; i++) {
        doc.switchToPage(i);
        doc.rect(45, 800, 505, 0.5).fill(cBorder);
        doc.fillColor(cGray).fontSize(7.5).font('Helvetica')
           .text('Naseeb Lal Market Commercial Suite • Master Administrator: Vimal Kumar (Mob: 7654138539)', 45, 808);
        doc.fillColor(cBlue).fontSize(7.5).font('Helvetica-Bold')
           .text(`Page ${i + 1} of ${pageCount}`, 490, 808, { align: 'right' });
    }

    doc.end();
}

const targetPath = process.argv[2] || 'public/Naseeb_Lal_Market_Complete_Documentation.pdf';
console.log('Generating documentation PDF at:', targetPath);
buildDocumentationPDF(targetPath);
console.log('PDF generation complete!');
