"""
services/email_service.py - Send PO emails via SMTP, receive invoices via IMAP
Equivalent to server/.../service/EmailService.kt
"""
import imaplib
import os
import smtplib
import threading
import time
from email import message_from_bytes
from email.mime.application import MIMEApplication
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from typing import Callable, Optional

SMTP_HOST = os.getenv("SMTP_HOST", "")
SMTP_PORT = int(os.getenv("SMTP_PORT", "587"))
SMTP_USER = os.getenv("SMTP_USER", "")
SMTP_PASSWORD = os.getenv("SMTP_PASSWORD", "")

IMAP_HOST = os.getenv("IMAP_HOST", "")
IMAP_PORT = int(os.getenv("IMAP_PORT", "993"))
IMAP_USER = os.getenv("IMAP_USER", "")
IMAP_PASSWORD = os.getenv("IMAP_PASSWORD", "")

UPLOAD_DIR = "uploads/invoices"
os.makedirs(UPLOAD_DIR, exist_ok=True)

_is_smtp_configured = bool(SMTP_HOST and SMTP_USER)
_is_imap_configured = bool(IMAP_HOST and IMAP_USER)

_listener_thread: Optional[threading.Thread] = None
_listener_stop_event = threading.Event()


def send_po_to_vendor(vendor_email: str, po_id: str, pdf_path: str) -> bool:
    if not _is_smtp_configured:
        print(f"📧 [SMTP NOT CONFIGURED] Would send PO {po_id} to {vendor_email}")
        return False
    try:
        msg = MIMEMultipart()
        msg["From"] = f"Reconix Procurement <{SMTP_USER}>"
        msg["To"] = vendor_email
        msg["Subject"] = f"Purchase Order {po_id} - Reconix"

        html_body = f"""
        <html><body>
        <h2>Purchase Order {po_id}</h2>
        <p>Dear Vendor,</p>
        <p>Please find attached the Purchase Order <strong>{po_id}</strong> for your reference.</p>
        <p>Kindly review the order details and send your invoice referencing this PO number.</p>
        <br><p>Best regards,<br>Reconix Procurement Team</p>
        </body></html>
        """
        msg.attach(MIMEText(html_body, "html"))

        if os.path.exists(pdf_path):
            with open(pdf_path, "rb") as f:
                attachment = MIMEApplication(f.read(), _subtype="pdf")
                attachment.add_header("Content-Disposition", "attachment", filename=f"{po_id}.pdf")
                msg.attach(attachment)

        with smtplib.SMTP(SMTP_HOST, SMTP_PORT) as server:
            server.ehlo()
            server.starttls()
            server.login(SMTP_USER, SMTP_PASSWORD)
            server.sendmail(SMTP_USER, vendor_email, msg.as_string())

        print(f"📧 ✅ Email sent: PO {po_id} -> {vendor_email}")
        return True
    except Exception as exc:
        print(f"📧 ❌ Failed to send email: {exc}")
        return False


def start_email_listener(on_invoice_received: Callable[[str, bytes], None]) -> None:
    if not _is_imap_configured:
        print("📬 [IMAP NOT CONFIGURED] Email listener not started")
        return
    _listener_stop_event.clear()
    thread = threading.Thread(
        target=_email_listener_loop,
        args=(on_invoice_received,),
        daemon=True,
        name="EmailListener",
    )
    thread.start()
    global _listener_thread
    _listener_thread = thread
    print("📬 Email listener started - checking every 5 minutes")


def stop_email_listener() -> None:
    _listener_stop_event.set()
    global _listener_thread
    _listener_thread = None
    print("📬 Email listener stopped")


def _email_listener_loop(on_invoice_received: Callable[[str, bytes], None]) -> None:
    while not _listener_stop_event.is_set():
        try:
            _check_for_new_emails(on_invoice_received)
        except Exception as exc:
            print(f"📬 ❌ Email check error: {exc}")
        _listener_stop_event.wait(timeout=5 * 60)


def _check_for_new_emails(on_invoice_received: Callable[[str, bytes], None]) -> None:
    mail = imaplib.IMAP4_SSL(IMAP_HOST, IMAP_PORT)
    try:
        mail.login(IMAP_USER, IMAP_PASSWORD)
        mail.select("INBOX")
        _, data = mail.search(None, "UNSEEN")
        ids = data[0].split()
        print(f"📬 Found {len(ids)} unread emails")
        for uid in ids:
            _, msg_data = mail.fetch(uid, "(RFC822)")
            raw = msg_data[0][1]
            _process_email_message(raw, on_invoice_received)
            mail.store(uid, "+FLAGS", "\\Seen")
    finally:
        mail.logout()


def _process_email_message(raw: bytes, on_invoice_received: Callable[[str, bytes], None]) -> None:
    msg = message_from_bytes(raw)
    if msg.is_multipart():
        for part in msg.walk():
            content_disposition = part.get("Content-Disposition", "")
            if content_disposition.lower().startswith(("attachment", "inline")):
                file_name = part.get_filename() or f"unknown_{int(time.time())}.pdf"
                lower_name = file_name.lower()
                if any(lower_name.endswith(ext) for ext in (".pdf", ".png", ".jpg", ".jpeg")):
                    file_bytes = part.get_payload(decode=True) or b""
                    print(f"📬 📎 Found attachment: {file_name} ({len(file_bytes)} bytes)")
                    save_path = os.path.join(UPLOAD_DIR, file_name)
                    with open(save_path, "wb") as f:
                        f.write(file_bytes)
                    on_invoice_received(file_name, file_bytes)
