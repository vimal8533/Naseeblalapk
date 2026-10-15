import sys

class SimplePDF:
    def __init__(self):
        self.pages = []
        self.current_page_commands = []
        self.y = 790
        self.left = 50
        self.right = 545
        self.top = 790
        self.bottom = 55
        self.page_number = 1

    def new_page(self):
        if self.current_page_commands:
            self._add_footer()
            self.pages.append(self.current_page_commands)
            self.current_page_commands = []
            self.page_number += 1
        self.y = self.top
        self._add_header()

    def _add_header(self):
        # Header rule and title
        cmd = """
0.8 0.8 0.8 RG
0.5 w
50 805 m 545 805 l S
BT
/F3 8 Tf
0.4 0.4 0.4 rg
50 810 Td
(Naseeb Lal Market Suite - System Documentation & Interview Playbook) Tj
ET
"""
        self.current_page_commands.append(cmd.strip())

    def _add_footer(self):
        cmd = f"""
0.8 0.8 0.8 RG
0.5 w
50 45 m 545 45 l S
BT
/F3 8 Tf
0.4 0.4 0.4 rg
50 35 Td
(Confidential - Enterprise Architecture & Technical Specification) Tj
480 35 Td
(Page {self.page_number}) Tj
ET
"""
        self.current_page_commands.append(cmd.strip())

    def check_space(self, needed):
        if self.y - needed < self.bottom:
            self.new_page()

    def add_title_banner(self, title, subtitle):
        self.check_space(80)
        # Background box
        bg_cmd = f"""
0.05 0.1 0.2 rg
50 {self.y - 65} 495 70 re f
BT
/F2 16 Tf
1 0.8 0.2 rg
65 {self.y - 25} Td
({self.escape(title)}) Tj
/F1 10 Tf
1 1 1 rg
65 {self.y - 45} Td
({self.escape(subtitle)}) Tj
ET
"""
        self.current_page_commands.append(bg_cmd.strip())
        self.y -= 80

    def add_section_header(self, title):
        self.check_space(35)
        cmd = f"""
0.85 0.5 0.1 rg
50 {self.y - 18} 495 20 re f
BT
/F2 11 Tf
1 1 1 rg
58 {self.y - 14} Td
({self.escape(title)}) Tj
ET
"""
        self.current_page_commands.append(cmd.strip())
        self.y -= 28

    def add_sub_header(self, title):
        self.check_space(24)
        cmd = f"""
BT
/F2 10.5 Tf
0.1 0.18 0.35 rg
50 {self.y - 12} Td
({self.escape(title)}) Tj
ET
0.2 0.4 0.7 RG
1 w
50 {self.y - 15} m 200 {self.y - 15} l S
"""
        self.current_page_commands.append(cmd.strip())
        self.y -= 24

    def add_paragraph(self, text, font="F1", size=9, leading=12, color=(0.1, 0.1, 0.1), indent=0):
        lines = self.wrap_text(text, 495 - indent, font, size)
        self.check_space(len(lines) * leading + 4)
        for line in lines:
            cmd = f"""
BT
/{font} {size} Tf
{color[0]} {color[1]} {color[2]} rg
{self.left + indent} {self.y - leading} Td
({self.escape(line)}) Tj
ET
"""
            self.current_page_commands.append(cmd.strip())
            self.y -= leading
        self.y -= 4

    def add_bullet(self, bold_prefix, text, size=8.5, leading=11.5):
        full_text = f"- {bold_prefix} {text}"
        lines = self.wrap_text(full_text, 480, "F1", size)
        self.check_space(len(lines) * leading + 3)
        for i, line in enumerate(lines):
            indent = 50 if i == 0 else 60
            cmd = f"""
BT
/F1 {size} Tf
0.1 0.1 0.1 rg
{indent} {self.y - leading} Td
({self.escape(line)}) Tj
ET
"""
            self.current_page_commands.append(cmd.strip())
            self.y -= leading
        self.y -= 2

    def add_callout(self, title, text, bg=(0.95, 0.97, 1.0), border=(0.3, 0.5, 0.8)):
        lines = self.wrap_text(text, 470, "F1", 8.5)
        box_height = len(lines) * 11 + 24
        self.check_space(box_height + 6)
        cmd = f"""
{bg[0]} {bg[1]} {bg[2]} rg
{border[0]} {border[1]} {border[2]} RG
1 w
50 {self.y - box_height} 495 {box_height} re B
BT
/F2 9.5 Tf
{border[0]} {border[1]} {border[2]} rg
60 {self.y - 14} Td
({self.escape(title)}) Tj
ET
"""
        self.current_page_commands.append(cmd.strip())
        cur_y = self.y - 26
        for line in lines:
            line_cmd = f"""
BT
/F1 8.5 Tf
0.15 0.15 0.15 rg
60 {cur_y} Td
({self.escape(line)}) Tj
ET
"""
            self.current_page_commands.append(line_cmd.strip())
            cur_y -= 11
        self.y -= (box_height + 8)

    def add_qa_card(self, q_num, question, answer):
        q_lines = self.wrap_text(f"Q{q_num}: {question}", 475, "F2", 9.5)
        a_lines = self.wrap_text(answer, 475, "F1", 8.5)
        total_h = len(q_lines) * 13 + len(a_lines) * 11 + 16
        self.check_space(total_h + 8)

        # Border box
        box_cmd = f"""
0.98 0.98 0.98 rg
0.8 0.8 0.8 RG
0.5 w
50 {self.y - total_h} 495 {total_h} re B
0.1 0.3 0.6 rg
50 {self.y - total_h} 4 {total_h} re f
"""
        self.current_page_commands.append(box_cmd.strip())
        
        cur_y = self.y - 14
        for ql in q_lines:
            cmd = f"""
BT
/F2 9.5 Tf
0.1 0.25 0.5 rg
62 {cur_y} Td
({self.escape(ql)}) Tj
ET
"""
            self.current_page_commands.append(cmd.strip())
            cur_y -= 13

        cur_y -= 3
        for al in a_lines:
            cmd = f"""
BT
/F1 8.5 Tf
0.2 0.2 0.2 rg
62 {cur_y} Td
({self.escape(al)}) Tj
ET
"""
            self.current_page_commands.append(cmd.strip())
            cur_y -= 11
            
        self.y -= (total_h + 8)

    def wrap_text(self, text, max_width, font="F1", size=9):
        # Approx character width
        char_width = size * 0.52
        chars_per_line = int(max_width / char_width)
        words = text.split(' ')
        lines = []
        cur_line = []
        cur_len = 0
        for w in words:
            if cur_len + len(w) + 1 <= chars_per_line:
                cur_line.append(w)
                cur_len += len(w) + 1
            else:
                if cur_line:
                    lines.append(' '.join(cur_line))
                cur_line = [w]
                cur_len = len(w)
        if cur_line:
            lines.append(' '.join(cur_line))
        return lines

    def escape(self, text):
        return text.replace('\\', '\\\\').replace('(', '\\(').replace(')', '\\)')

    def finish(self, filepath):
        if self.current_page_commands:
            self._add_footer()
            self.pages.append(self.current_page_commands)

        pdf_objs = []
        # 1: Catalog
        # 2: Pages
        # Page objs: 3, 4, ...
        # Fonts: F1, F2, F3
        # Content stream objs:
        total_pages = len(self.pages)
        page_obj_ids = [3 + i * 2 for i in range(total_pages)]
        content_obj_ids = [4 + i * 2 for i in range(total_pages)]
        font1_id = 3 + total_pages * 2
        font2_id = font1_id + 1
        font3_id = font2_id + 1

        catalog = "<< /Type /Catalog /Pages 2 0 R >>"
        kids_str = " ".join([f"{pid} 0 R" for pid in page_obj_ids])
        pages_obj = f"<< /Type /Pages /Kids [{kids_str}] /Count {total_pages} >>"

        objs = [catalog, pages_obj]

        for i in range(total_pages):
            pid = page_obj_ids[i]
            cid = content_obj_ids[i]
            page_dict = f"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595.28 841.89] /Resources << /Font << /F1 {font1_id} 0 R /F2 {font2_id} 0 R /F3 {font3_id} 0 R >> >> /Contents {cid} 0 R >>"
            objs.append(page_dict)

            # Stream
            stream_body = "\n".join(self.pages[i]).encode('utf-8')
            stream_obj = f"<< /Length {len(stream_body)} >>\nstream\n{stream_body.decode('latin1')}\nendstream"
            objs.append(stream_obj)

        font1_obj = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
        font2_obj = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>"
        font3_obj = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Oblique >>"
        objs.extend([font1_obj, font2_obj, font3_obj])

        # Write out
        out = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
        xref = [0]
        for i, obj in enumerate(objs, start=1):
            xref.append(len(out))
            out.extend(f"{i} 0 obj\n{obj}\nendobj\n".encode('latin1'))

        startxref = len(out)
        out.extend(f"xref\n0 {len(objs)+1}\n0000000000 65535 f \n".encode('latin1'))
        for offset in xref[1:]:
            out.extend(f"{offset:010d} 00000 n \n".encode('latin1'))
        out.extend(f"trailer\n<< /Size {len(objs)+1} /Root 1 0 R >>\nstartxref\n{startxref}\n%%EOF\n".encode('latin1'))

        with open(filepath, "wb") as f:
            f.write(out)
        print(f"Successfully generated PDF: {filepath} ({len(out)} bytes, {total_pages} pages)")

def generate_documentation():
    pdf = SimplePDF()
    pdf.new_page()

    # Title
    pdf.add_title_banner(
        "Naseeb Lal Market - Commercial Property Ecosystem",
        "Full-Stack Android (Compose) & Cloud-Synced Tenant Web Portal | Technical Documentation"
    )

    pdf.add_section_header("1. EXECUTIVE SUMMARY & PROBLEM STATEMENT")
    pdf.add_paragraph(
        "In commercial markets and complex retail spaces, managing monthly rent collections, electrical sub-meter utility readings, and municipal taxes across dozens of units is overwhelmingly manual and prone to severe revenue leakage. Landlords face persistent friction due to fraudulent online transaction claims, fake UPI screenshots, misplaced paper slips, and cumbersome dispute resolution."
    )
    pdf.add_paragraph(
        "The Solution: Naseeb Lal Market is an integrated commercial property management suite that pairs a native Android Admin Application (for property owners and collectors) with a zero-barrier, mobile-first Web Portal (for tenants). Through real-time cloud synchronization, automated utility calculations, and an anti-fraud reconciliation queue, it establishes an airtight, paperless property workflow."
    )

    pdf.add_section_header("2. SYSTEM ARCHITECTURE & TECHNOLOGY STACK")
    pdf.add_bullet("Native Mobile Frontend:", "100% Jetpack Compose with Material 3 dynamic styling, custom canvas graphics, and adaptive edge-to-edge layouts.")
    pdf.add_bullet("Programming Language:", "Modern Kotlin 2.x utilizing Coroutines, StateFlow, and Clean MVVM architecture.")
    pdf.add_bullet("Cloud Database:", "Google Firebase Realtime Database (RTDB) providing sub-second bidirectional synchronization, local disk persistence, and optimistic offline updates.")
    pdf.add_bullet("Tenant Web Portal:", "Zero-dependency, high-speed HTML5 / CSS3 / ES6 web portal deployed via Firebase Hosting CDN with query-slug routing (?shop=X).")
    pdf.add_bullet("Communication & SMS:", "Fast2SMS REST gateway integration for automated 1-click batch SMS reminders with native SIM selection fallback; WhatsApp direct deep-link dispatch.")
    pdf.add_bullet("Document Engine:", "Android native PdfDocument vector engine and Apache POI spreadsheet generator for automated tax clearance slips and audit logs.")

    pdf.add_section_header("3. CORE FUNCTIONAL MODULES")
    pdf.add_sub_header("A. Dual-Workspace Management")
    pdf.add_paragraph("Features 1-tap workspace switching between Public Commercial Market (retail shops) and Private Residential Flats, providing segregated analytics while maintaining single database consistency.")
    
    pdf.add_sub_header("B. Role-Based Access Control (RBAC)")
    pdf.add_paragraph("Super-Admin maintains full administrative control over rent corrections, activity audits, and sub-admin accounts. Field Sub-Admins are limited strictly to recording payments and logging meter readings.")

    pdf.add_sub_header("C. Utility Meter & Municipal Tax Billing Engine")
    pdf.add_paragraph("Automates electricity sub-meter calculation using the formula: Due = (Current Reading - Previous Reading) x Tariff Rate. Tracks Patna Municipal Corporation (PMC) holding tax clearances with automated reminder windows.")

    pdf.add_sub_header("D. Anti-Fraud Payment Verification Pipeline (Tenant Echo)")
    pdf.add_paragraph("Eliminates fake payment claims. When a tenant enters an online UTR or cash note on the web portal, the form immediately locks to prevent duplicates. The claim is routed to the Admin dashboard's dedicated QUEUE badge. Only after manual bank reconciliation does the admin tap 'Verify & Mark Full Paid', which cryptographically provisions a tamper-evident receipt number and unlocks the official receipt on the tenant's browser.")

    # Page 2
    pdf.new_page()
    pdf.add_section_header("4. INTERVIEW PRESENTATION PLAYBOOK (FOR CANDIDATES)")

    pdf.add_sub_header("The 30-Second Elevator Pitch")
    pdf.add_callout(
        "How to introduce this project in an interview:",
        "\"I designed and developed Naseeb Lal Market, a full-stack commercial property management suite built with Jetpack Compose on Android and a real-time web portal on Firebase. It solves real-world rent tracking, utility sub-meter calculations, and payment fraud across commercial complexes. Its standout capability is an Anti-Fraud Verification Pipeline: tenants submit UTR proofs via zero-install deep links, and upon admin approval in the Android app, verified PDF receipts unlock dynamically in real time without refreshing the page.\""
    )

    pdf.add_sub_header("The 2-Minute In-Depth Walkthrough (STAR Method)")
    pdf.add_bullet("Situation (Problem):", "Commercial landlords struggle with manual notebooks, disputed utility calculations, and fraudulent UPI payment claims from retail tenants.")
    pdf.add_bullet("Task (Objective):", "Build a high-reliability, fraud-proof ecosystem that requires zero onboarding friction for non-tech-savvy tenants while giving property owners real-time ledger visibility.")
    pdf.add_bullet("Action (Engineering):", "Implemented clean MVVM on Android using Kotlin Coroutines and StateFlow for state management; established bidirectional WebSocket sync via Firebase RTDB; built a lightweight responsive tenant portal with automatic state polling; and implemented an on-device PDF generation engine.")
    pdf.add_bullet("Result (Impact):", "Completely eliminated billing reconciliation disputes, enabled automated 1-click batch reminder dispatch, and achieved instant verified digital receipt generation.")

    pdf.add_section_header("5. TOP TECHNICAL INTERVIEW QUESTIONS & ANSWERS")

    pdf.add_qa_card(
        1,
        "Why did you choose Jetpack Compose over traditional XML Layouts?",
        "Compose uses a declarative paradigm where UI is a pure function of state (UI = f(State)). In this application, real-time sync indicators, meter reading cards, and payment queue badges undergo frequent live state mutations. With Compose and StateFlow (observed via collectAsStateWithLifecycle), the framework performs intelligent recomposition on only the modified nodes. This eliminated complex View-binding boilerplate, reduced lines of code by over 40%, and completely eradicated null-pointer exceptions."
    )

    pdf.add_qa_card(
        2,
        "How did you achieve real-time synchronization between Android and the Web Portal?",
        "We leveraged Firebase Realtime Database as the shared Single Source of Truth. The Android app maintains persistent WebSocket streams that emit snapshots into StateFlow pipelines. When a tenant submits a UTR on the web portal, an atomic REST PATCH updates the /rents/{id} node with tenantClaimedPaid: true. The Android dashboard immediately reflects this in the Queue filter. When approved, the status transitions to PAID. The web portal features an active polling observer that detects the change, transitions out of the pending state, and renders the official receipt."
    )

    pdf.add_qa_card(
        3,
        "Why a Web Portal for tenants instead of a Tenant Mobile App?",
        "This was a deliberate UX and product strategy decision. Retail shopkeepers resist installing 50MB mobile applications and frequently forget credentials. By building a mobile-first Web Portal accessible via query parameters (?shop=5), we achieved zero barrier to entry. The tenant taps a monthly WhatsApp link, reviews their bill, enters their transaction reference, and saves their receipt. It maximizes compliance and adoption."
    )

    pdf.add_qa_card(
        4,
        "How did you prevent fraudulent payment submissions?",
        "We enforced a two-stage verification barrier. A tenant submission is strictly categorized as an unverified claim (UNDER_REVIEW) and locks the input fields. The transaction appears in the Admin's dedicated 'Queue' badge. The property manager verifies the exact UTR against their live bank statement. Only when the manager clicks 'Verify & Mark Full Paid' does the system generate the official receipt number (NLM-YYYY-UNIT-XXXX) and unlock the sealed digital receipt on the tenant's device."
    )

    pdf.add_qa_card(
        5,
        "How does the system handle offline environments and network dropouts?",
        "The Android app employs Firebase local disk persistence and SharedPreferences-backed data caching. If a field collector records a cash payment in a basement with zero connectivity, the local ledger updates immediately without freezing the UI. Once network connectivity is restored, Firebase's offline queue automatically synchronizes the transactions with cloud timestamps and activity audit logs."
    )

    pdf.finish("public/Naseeb_Lal_Market_Project_Documentation.pdf")

if __name__ == "__main__":
    generate_documentation()
