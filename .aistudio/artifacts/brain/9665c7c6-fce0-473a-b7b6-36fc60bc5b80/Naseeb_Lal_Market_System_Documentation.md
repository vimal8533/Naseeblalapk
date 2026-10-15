# Naseeb Lal Market — Commercial Billing Suite
## Complete System Architecture, Database Schema & Operational Documentation

---

### 📥 Direct PDF Download Links:
* **Direct PDF File**: [Download Naseeb_Lal_Market_Complete_Documentation.pdf](https://ais-dev-5zu5bv62xf7z25b3nduoiw-778355822033.asia-southeast1.run.app/Naseeb_Lal_Market_Complete_Documentation.pdf)
* **Web Documentation Viewer**: [Open project_docs.html](https://ais-dev-5zu5bv62xf7z25b3nduoiw-778355822033.asia-southeast1.run.app/project_docs.html)
* **Inside Mobile App**: Open **Profile** Screen → Click **"Download Documentation PDF (3 Pages)"**

---

## 1. Executive Summary & Business Purpose

**Naseeb Lal Market (NLM) Commercial Billing Suite** is a production-grade enterprise Android mobile management solution built for modern commercial real estate operators and landlords.

### Real-World Problems Solved:
1. **Elimination of Paper Registers**: Manual paper ledgers often suffer from forgotten rent increments, uncollected past arrears, calculation mistakes, and physical page tearing or misplacement.
2. **Sub-Metered Electricity Automation**: Eliminates manual calculation of tenant electricity charges (`Current Reading - Previous Reading × Rate`).
3. **Itemized Partial Dues Reconciliation**: Clear segregation of Base Rent, Electricity Units, Municipal PMC Taxes, and Prior Arrears so tenants always understand remaining balances.
4. **Zero-Permission Reminder Queues**: Dispatches professional itemized reminders via WhatsApp and SMS without requiring dangerous Android telephony permissions.
5. **Two-Way Tenant Self-Service (Tenant Echo)**: Eliminates disputes by providing tenants a dedicated web link (`https://naseeblalmarket.web.app/portal`) to check their bill, register payment promises, and download authentic QR-verified PDF receipts.

---

## 2. Database Architecture (Kaha Hai Aur Kya Hai)

### 2.1 Database Overview
* **Database Provider**: Google Firebase Realtime Database (Cloud NoSQL JSON Document Store)
* **Cloud Database URL**: `https://nasseblalmarkt-default-rtdb.firebaseio.com/`
* **Real-time Protocol**: HTTP/2 Server-Sent Events (SSE) streaming (`text/event-stream`) via OkHttp3. Any change made by one admin reflects on all other devices in sub-second latency.
* **Offline-First Resilience**: Local Android `SharedPreferences` cache (`naseeb_lal_market_cache`) stores complete snapshots. When network disconnects, the app functions smoothly in offline mode and synchronizes automatically upon reconnection via `ConnectivityManager.NetworkCallback`.

---

### 2.2 Root Collections & Data Schemas

#### 1. `/shops` (Physical Units Master Registry)
Stores all physical commercial shops and residential flats in the market.
* `id` (*String*): Unique shop identifier (e.g., `"shop_1"`)
* `shopNumber` (*String*): Shop/Room number (e.g., `"Shop 1"`, `"Flat 101"`)
* `floor` (*String*): Floor location (e.g., `"Ground Floor"`, `"First Floor"`)
* `baseRent` (*Double*): Default base monthly rent
* `isOccupied` (*Boolean*): Whether currently rented out
* `currentTenantId` (*String*): ID of active tenant (or blank if vacant)
* `isPersonal` (*Boolean*): `false` for Commercial Market, `true` for Residential Flat
* `notes` (*String*): Dimensions, meter location, or notes

#### 2. `/tenants` (Active & Archived Tenant Profiles)
Stores profiles of all enrolled business owners and residents.
* `id` (*String*): Unique tenant UUID
* `name` (*String*): Legal name of tenant
* `businessName` (*String*): Shop name / board trade name
* `phone` (*String*): 10-digit mobile number for reminders & receipts
* `shopNumber` (*String*): Comma-separated assigned shops (e.g., `"Shop 1, Shop 2"`)
* `shopCount` (*Int*): Number of units leased
* `monthlyRent` (*Double*): Current active monthly rent
* `advanceDeposit` (*Double*): Refundable security deposit
* `cycleMonths` (*Int*): `1` (Monthly), `3` (Quarterly), `6` (Half-Yearly), `12` (Yearly)
* `incrementYears` (*Int*): Increment interval (e.g., every 1 year)
* `incrementPercent` (*Double*): Agreed rent increase % (e.g., 5.0% or 10.0%)
* `electricityBill` (*Double*): Fixed monthly electricity rate (for flats without sub-meter)
* `previousDues` (*Double*): Initial opening balance from older paper records
* `isPersonal` (*Boolean*): Commercial vs Personal workspace tag
* `isDeleted` (*Boolean*): Soft-delete flag (moves to Deleted Archive for 30 days)
* `deletedAt` (*Long*): Deletion timestamp
* `dateJoined` (*String*): Lease agreement start date

#### 3. `/rents` (Monthly Billing Ledger & Records)
Stores transaction records for every tenant for each billing cycle.
* `id` (*String*): Unique record key (e.g., `"rent_tenantId_month_year"`)
* `tenantId` (*String*): Foreign key referencing `/tenants`
* `tenantName` (*String*): Tenant name
* `shopNumber` (*String*): Unit number
* `month` (*String*): Billing month (e.g., `"October"`)
* `year` (*Int*): Billing year (e.g., `2026`)
* `amountDue` (*Double*): Total bill amount
* `amountPaid` (*Double*): Amount received so far
* `pendingAmount` (*Double*): Remaining unpaid balance (`amountDue - amountPaid`)
* `status` (*String*): `"PAID"`, `"PARTIAL"`, `"PENDING"`
* `isPartial` (*Boolean*): `true` if partial payment was recorded
* `electricityUnits` (*Double*): Units consumed in the month (`Current - Previous`)
* `electricityRate` (*Double*): Tariff per unit (e.g., ₹10.0)
* `electricityBill` (*Double*): Total electric charge (`Units × Rate`)
* `previousReading` (*Double*): Starting meter reading
* `currentReading` (*Double*): Ending meter reading
* `pmcTax` (*Double*): Municipal property tax share
* `priorArrears` (*Double*): Unpaid balance carried over from previous months
* `receiptNumber` (*String*): Formal receipt reference (e.g., `"NLM-2026-104"`)
* `paymentDate` (*String*): Date when payment was received
* `paymentMode` (*String*): `"CASH"`, `"UPI"`, `"BANK_TRANSFER"`, `"CHEQUE"`
* `promisedDate` (*String*): Date committed by tenant via Tenant Echo
* `promisedNote` (*String*): Note left by tenant
* `tenantClaimedPaid` (*Boolean*): Flagged if tenant claims to have paid
* `tenantClaimedNote` (*String*): UTR / transaction reference submitted by tenant

#### 4. `/tenant_echo` (Two-Way Self-Service Interactions)
Keyed by `${month}_${year}_${tenantId}`:
* `echoKey` (*String*): Composite key
* `claimedPaid` (*Boolean*): Anti-fraud claim status
* `claimedPaidNote` (*String*): Tenant's payment proof text
* `promisedDate` (*String*): Selected promise-to-pay date
* `promisedNote` (*String*): Reason or commitment note
* `lastInteractionTime` (*Long*): Interaction timestamp

#### 5. `/activity_logs` (Immutable Audit Trail)
* `id` (*String*): Unique log UUID
* `timestamp` (*Long*): Epoch millisecond timestamp
* `action` (*String*): Action code (e.g., `"COLLECT_RENT"`, `"METER_READING"`)
* `details` (*String*): Description of change with before/after amounts
* `performedBy` (*String*): Username of admin or sub-admin
* `userRole` (*String*): `"ADMIN"` or `"SUB_ADMIN"`

#### 6. `/sub_admins` (Role-Based Delegated Users)
* `id` (*String*): Account ID
* `username` (*String*): Login identifier
* `passwordHash` (*String*): Secure credentials hash
* `displayName` (*String*): Full name
* `phone` (*String*): Contact number
* `isActive` (*Boolean*): Account status

---

## 3. Technology Stack & Architecture

| Layer | Technology | Engineering Role |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ | Coroutines, Flow, StateFlow, Unidirectional Data Flow |
| **UI Framework** | Jetpack Compose (Material 3) | Modern declarative UI, dynamic typography, edge-to-edge support |
| **State Management** | Android MVVM Architecture | ViewModel with reactive StateFlow streams and derivedStateOf caches |
| **Cloud Database** | Firebase Realtime Database | Realtime HTTP/2 SSE streaming with OkHttp3 client |
| **Local Cache** | Android SharedPreferences JSON | Instant offline launch without network reliance |
| **PDF Generation** | Native Android `PdfDocument` & `Canvas` | Vector A4 rendering for receipts and 3-page system reports |
| **Excel Export** | Native CSV Generator | 1-Click financial ledger export for Microsoft Excel & Google Sheets |
| **Messaging** | Zero-Permission `ACTION_SENDTO` Queue | Bypasses dangerous `SEND_SMS` permissions for Google Play compliance |
| **Cloud SMS** | Fast2SMS REST API | Optional bulk SMS broadcast with cloud delivery status |
| **QR Verification** | ZXing Embedded Engine | Generates tamper-proof authentication QR codes on digital receipts |
| **Automated Testing** | Robolectric & JUnit4 | Local JVM tests verifying financial calculation invariants |

---

## 4. End-to-End Operational Workflows

### 4.1 Onboarding & Setup
1. Administrator launches app and toggles workspace: **Commercial Market (Shops)** or **Personal (Flats)**.
2. Clicks **"+"** (Add Tenant) to enter tenant profile, monthly rent, deposit, billing frequency, and contract increment rule (e.g. 5% annually).
3. System validates shop availability and maps tenant to the active unit.

### 4.2 Monthly Cycle & Sub-Meter Reading
1. App opens dynamically to current calendar month (e.g., **October 2026**).
2. Administrator opens **Batch Electricity Reading** dialog.
3. Enters new meter readings for occupied shops. The app calculates consumption (`Current - Previous`) and applies ₹10/unit tariff.
4. Total utility charges automatically merge into each tenant's monthly ledger.

### 4.3 Dues & Partial Reconciliation
1. When tenant pays rent, admin clicks **"Collect Rent"**.
2. If full amount is paid, status marks `"PAID"` and generates digital receipt.
3. If partial amount is paid (e.g. ₹6,000 paid out of ₹10,000), status marks `"PARTIAL"`.
4. The remaining balance (₹4,000) stays in live dues tracking with explicit itemized breakdown (Rent + Light + PMC Tax + Arrears).

### 4.4 Automated Reminders
1. Admin clicks **Batch Reminders** or **WhatsApp Reminder** button.
2. System formats a polite Hindi message with full breakdown:
   * Tenant Name & Shop Number
   * Billing Month & Year
   * Total Bill, Amount Received, Remaining Dues
   * Breakdown: Room Rent, Electricity Units, Municipal Tax, Prior Arrears
   * Dedicated Self-Service Link: `https://naseeblalmarket.web.app/portal?echo=...`

### 4.5 Digital Receipt & Verification
1. Receipts are rendered via high-resolution native Android Canvas into standard A4 PDF files.
2. Each receipt includes market header, administration stamp, tenant details, itemized breakdown, and encrypted verification QR code.
3. Admin shares receipt directly to tenant's WhatsApp in 1-click.

---

## 5. Security & Google Play Policy Compliance

* **Zero Dangerous Telephony Permissions**: Eliminates `READ_PHONE_STATE`, `SEND_SMS`, or `RECEIVE_SMS`. Uses Android standard `ACTION_SENDTO` intents.
* **Zero Storage Permissions**: Employs Scoped Storage and MediaStore API (`Download/`) without requiring `READ_EXTERNAL_STORAGE`.
* **RBAC Enforcement**: Sub-admins cannot permanently delete records or alter core tenant contracts; every change is immutably recorded in `/activity_logs`.
