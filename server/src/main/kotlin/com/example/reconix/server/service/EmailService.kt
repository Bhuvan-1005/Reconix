package com.example.reconix.server.service

import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.search.FlagTerm
import kotlinx.coroutines.*
import java.io.File
import java.util.Properties

/**
 * Email Service - Handles SMTP sending and IMAP email monitoring
 *
 * Environment Variables:
 * - SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD
 * - IMAP_HOST, IMAP_PORT, IMAP_USER, IMAP_PASSWORD
 *
 * All methods gracefully no-op if credentials are not configured.
 */
class EmailService {

    private val smtpHost = System.getenv("SMTP_HOST")
    private val smtpPort = System.getenv("SMTP_PORT") ?: "587"
    private val smtpUser = System.getenv("SMTP_USER")
    private val smtpPassword = System.getenv("SMTP_PASSWORD")

    private val imapHost = System.getenv("IMAP_HOST")
    private val imapPort = System.getenv("IMAP_PORT") ?: "993"
    private val imapUser = System.getenv("IMAP_USER")
    private val imapPassword = System.getenv("IMAP_PASSWORD")

    private val uploadDir = "uploads/invoices"
    private var emailListenerJob: Job? = null

    val isSmtpConfigured: Boolean get() = !smtpHost.isNullOrBlank() && !smtpUser.isNullOrBlank()
    val isImapConfigured: Boolean get() = !imapHost.isNullOrBlank() && !imapUser.isNullOrBlank()

    init {
        File(uploadDir).mkdirs()
    }

    /**
     * Send PO PDF to vendor via email
     * @return true if email sent successfully, false if not configured or failed
     */
    fun sendPoToVendor(vendorEmail: String, poId: String, pdfPath: String): Boolean {
        if (!isSmtpConfigured) {
            println("📧 [SMTP NOT CONFIGURED] Would send PO $poId to $vendorEmail")
            println("   Set SMTP_HOST, SMTP_USER, SMTP_PASSWORD environment variables to enable")
            return false
        }

        return try {
            val props = Properties().apply {
                put("mail.smtp.host", smtpHost!!)
                put("mail.smtp.port", smtpPort)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(smtpUser, smtpPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(smtpUser, "Reconix Procurement"))
                setRecipient(Message.RecipientType.TO, InternetAddress(vendorEmail))
                subject = "Purchase Order $poId - Reconix"
            }

            // Build multipart message: text body + PDF attachment
            val multipart = MimeMultipart()

            // Text body
            val textPart = MimeBodyPart()
            textPart.setContent("""
                <html>
                <body>
                <h2>Purchase Order $poId</h2>
                <p>Dear Vendor,</p>
                <p>Please find attached the Purchase Order <strong>$poId</strong> for your reference.</p>
                <p>Kindly review the order details and send your invoice referencing this PO number.</p>
                <br>
                <p>Best regards,<br>Reconix Procurement Team</p>
                </body>
                </html>
            """.trimIndent(), "text/html")
            multipart.addBodyPart(textPart)

            // PDF attachment
            val pdfFile = File(pdfPath)
            if (pdfFile.exists()) {
                val attachmentPart = MimeBodyPart()
                attachmentPart.attachFile(pdfFile)
                attachmentPart.fileName = "$poId.pdf"
                multipart.addBodyPart(attachmentPart)
            }

            message.setContent(multipart)
            Transport.send(message)

            println("📧 ✅ Email sent: PO $poId -> $vendorEmail")
            true
        } catch (e: Exception) {
            println("📧 ❌ Failed to send email: ${e.message}")
            false
        }
    }

    /**
     * Start the email listener background service
     * Checks for new invoice emails every 5 minutes
     * @param onInvoiceReceived Callback when an invoice PDF is found
     */
    fun startEmailListener(
        scope: CoroutineScope,
        onInvoiceReceived: (fileName: String, fileBytes: ByteArray) -> Unit
    ) {
        if (!isImapConfigured) {
            println("📬 [IMAP NOT CONFIGURED] Email listener not started")
            println("   Set IMAP_HOST, IMAP_USER, IMAP_PASSWORD environment variables to enable")
            return
        }

        emailListenerJob = scope.launch(Dispatchers.IO) {
            println("📬 Email listener started - checking every 5 minutes")
            while (isActive) {
                try {
                    checkForNewEmails(onInvoiceReceived)
                } catch (e: Exception) {
                    println("📬 ❌ Email check error: ${e.message}")
                }
                delay(5 * 60 * 1000L) // 5 minutes
            }
        }
    }

    /**
     * Check IMAP mailbox for new emails with PDF attachments
     */
    private fun checkForNewEmails(onInvoiceReceived: (String, ByteArray) -> Unit) {
        val props = Properties().apply {
            put("mail.imap.host", imapHost!!)
            put("mail.imap.port", imapPort)
            put("mail.imap.ssl.enable", "true")
        }

        val session = Session.getInstance(props)
        val store = session.getStore("imaps")

        try {
            store.connect(imapHost, imapUser, imapPassword)
            val inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_WRITE)

            // Search for unread messages
            val unreadMessages = inbox.search(FlagTerm(Flags(Flags.Flag.SEEN), false))
            println("📬 Found ${unreadMessages.size} unread emails")

            for (message in unreadMessages) {
                processEmailMessage(message, onInvoiceReceived)
                // Mark as read
                message.setFlag(Flags.Flag.SEEN, true)
            }

            inbox.close(false)
        } finally {
            store.close()
        }
    }

    /**
     * Process a single email message - extract PDF attachments
     */
    private fun processEmailMessage(
        message: Message,
        onInvoiceReceived: (String, ByteArray) -> Unit
    ) {
        val content = message.content

        if (content is Multipart) {
            for (i in 0 until content.count) {
                val bodyPart = content.getBodyPart(i)
                val disposition = bodyPart.disposition

                if (disposition != null &&
                    (disposition.equals(Part.ATTACHMENT, ignoreCase = true) ||
                     disposition.equals(Part.INLINE, ignoreCase = true))
                ) {
                    val fileName = bodyPart.fileName ?: "unknown_${System.currentTimeMillis()}.pdf"
                    if (fileName.lowercase().endsWith(".pdf") ||
                        fileName.lowercase().endsWith(".png") ||
                        fileName.lowercase().endsWith(".jpg") ||
                        fileName.lowercase().endsWith(".jpeg")
                    ) {
                        val fileBytes = bodyPart.inputStream.readBytes()
                        println("📬 📎 Found attachment: $fileName (${fileBytes.size} bytes)")

                        // Save to disk
                        val savedPath = "$uploadDir/$fileName"
                        File(savedPath).writeBytes(fileBytes)

                        // Trigger processing callback
                        onInvoiceReceived(fileName, fileBytes)
                    }
                }
            }
        }
    }

    fun stopEmailListener() {
        emailListenerJob?.cancel()
        emailListenerJob = null
        println("📬 Email listener stopped")
    }
}
