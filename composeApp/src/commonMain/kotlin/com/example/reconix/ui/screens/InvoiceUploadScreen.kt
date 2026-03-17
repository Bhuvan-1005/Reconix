package com.example.reconix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.shared.InvoiceStatus
import com.example.reconix.shared.InvoiceUploadResponse
import com.example.reconix.shared.OcrExtractedData
import com.example.reconix.ui.components.GlassCard
import com.example.reconix.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reconix.viewmodel.FinanceViewModel
import com.example.reconix.viewmodel.UploadUiState
import com.example.reconix.BackHandler

/**
 * ═══════════════════════════════════════════════════════════════
 *  Invoice Upload Screen — Finance/Validator Portal
 *  Upload invoice PDFs for OCR extraction and validation
 * ═══════════════════════════════════════════════════════════════
 */

@Composable
fun InvoiceUploadScreen(
    onBack: () -> Unit = {},
    onUploadComplete: (InvoiceUploadResponse) -> Unit = {},
    onRequestFilePick: ((fileName: String, bytes: ByteArray) -> Unit) -> Unit = { _ -> },
    viewModel: FinanceViewModel = viewModel { FinanceViewModel() },
    modifier: Modifier = Modifier
) {
    val uploadState by viewModel.uploadState.collectAsState()

    // Derive local state from ViewModel
    val isUploading   = uploadState is UploadUiState.Uploading
    val uploadResult  = (uploadState as? UploadUiState.Success)?.result
    val uploadError   = (uploadState as? UploadUiState.Error)?.message

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    // true = route through Gemini AI; false = OCR.space quick-scan (default)
    var useGeminiMode by remember { mutableStateOf(false) }
    BackHandler { onBack() }

    // Fire onUploadComplete callback once the upload finishes
    LaunchedEffect(uploadState) {
        if (uploadState is UploadUiState.Success) {
            onUploadComplete((uploadState as UploadUiState.Success).result)
        }
    }

    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isDark) Color.Black else Color(0xFFF2F4F8)
            )
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            "Upload Invoice",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            "Upload PDF for automated extraction & validation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Extraction Mode Selector ──
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            "Extraction Method",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !useGeminiMode,
                                onClick = { useGeminiMode = false },
                                label = { Text("OCR Quick Scan", fontSize = 13.sp) },
                                leadingIcon = {
                                    if (!useGeminiMode)
                                        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp))
                                    else
                                        Icon(Icons.Default.CameraAlt, null, Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = useGeminiMode,
                                onClick = { useGeminiMode = true },
                                label = { Text("Gemini AI Extract", fontSize = 13.sp) },
                                leadingIcon = {
                                    if (useGeminiMode)
                                        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp))
                                    else
                                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (useGeminiMode) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "✨ Gemini 1.5 Flash reads the PDF and returns richer data (Invoice#, Date, Tax)",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricIndigo
                            )
                        }
                    }
                }
            }

            // ── Upload Zone ──
            item {
                UploadDropZone(
                    isUploading = isUploading,
                    isGemini = useGeminiMode,
                    selectedFileName = selectedFileName,
                    onPickFile = {
                        onRequestFilePick { fileName, bytes ->
                            selectedFileName = fileName
                            viewModel.uploadInvoice(bytes, fileName, useGeminiMode)
                        }
                    }
                )
            }

            // ── No Invoice Selected State ──
            if (selectedFileName == null && !isUploading && uploadResult == null) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                "No invoice selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color.Black
                            )
                            Text(
                                "Select a PDF file above to read and extract its contents",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Upload Error ──
            if (uploadError != null) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning)
                            Text(uploadError, color = SilverText, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── How It Works ──
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "How It Works",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                        }

                        ProcessStep(
                            number = "1",
                            title = "Upload Invoice PDF",
                            description = "Drag & drop or select your invoice file",
                            color = NeonCyan
                        )
                        ProcessStep(
                            number = "2",
                            title = if (useGeminiMode) "Gemini AI Extraction" else "OCR Extraction",
                            description = if (useGeminiMode)
                                "Gemini 1.5 Flash reads the PDF — extracts Invoice#, Date, Tax & line items"
                            else
                                "OCR extracts PO#, line items, vendor & totals",
                            color = ElectricIndigo
                        )
                        ProcessStep(
                            number = "3",
                            title = "3-Way Match Validation",
                            description = "System compares against PO & GRN data",
                            color = NeonMint
                        )
                        ProcessStep(
                            number = "4",
                            title = "Review & Approve",
                            description = "Auto-approve matches, flag mismatches",
                            color = AmberWarning
                        )
                    }
                }
            }

            // ── Upload Result ──
            if (uploadResult != null) {
                item {
                    UploadResultCard(result = uploadResult)
                }
            }

            // ── Email Channel Info ──
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = NeonCyanLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Email Channel Active",
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                "Vendors can also email invoices to invoices@reconix.com \u2014 they'll be auto-detected and processed",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = NeonMint.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "AUTO",
                                color = NeonMintLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ── Upload Drop Zone ─────────────────────────────────────────
@Composable
private fun UploadDropZone(
    isUploading: Boolean,
    isGemini: Boolean = false,
    selectedFileName: String?,
    onPickFile: () -> Unit
) {
    // Animated border
    val infiniteTransition = rememberInfiniteTransition()
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = borderAlpha),
                            ElectricIndigo.copy(alpha = borderAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = !isUploading) { onPickFile() }
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isUploading) {
                    // Branded pulsing upload indicator
                    val uploadPulse by rememberInfiniteTransition(label = "uploadPulse")
                        .animateFloat(
                            initialValue = 0.9f,
                            targetValue  = 1.35f,
                            animationSpec = infiniteRepeatable(
                                tween(700, easing = FastOutSlowInEasing),
                                RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )
                    val uploadAlpha by rememberInfiniteTransition(label = "uploadAlpha")
                        .animateFloat(
                            initialValue = 0.4f,
                            targetValue  = 1f,
                            animationSpec = infiniteRepeatable(
                                tween(700, easing = FastOutSlowInEasing),
                                RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                        // Outer ripple
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(uploadPulse)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = uploadAlpha * 0.18f))
                        )
                        // Core circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(NeonCyan.copy(alpha = 0.9f), NeonCyan.copy(alpha = 0.3f))
                                    )
                                )
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFF060E20),
                                modifier = Modifier.size(22.dp).align(Alignment.Center)
                            )
                        }
                    }
                    Text(
                        if (isGemini) "Analyzing Invoice with AI..." else "Scanning Invoice...",
                        color = if (isGemini) ElectricIndigo else NeonCyanLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (selectedFileName != null) {
                        Text(
                            selectedFileName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        "Drop Invoice PDF Here",
                        color = if (LocalIsDarkTheme.current) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "or tap to select file",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FileTypeBadge("PDF")
                        FileTypeBadge("PNG")
                        FileTypeBadge("JPG")
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTypeBadge(type: String) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE8E8E8)
    ) {
        Text(
            type,
            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── Process Step ──────────────────────────────────────────────
@Composable
private fun ProcessStep(
    number: String,
    title: String,
    description: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                title,
                color = if (LocalIsDarkTheme.current) Color.White else Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

// ── Upload Result Card ────────────────────────────────────────
@Composable
private fun UploadResultCard(result: InvoiceUploadResponse) {
    val statusColor = when (result.validationResult?.status) {
        InvoiceStatus.MATCHED -> NeonMint
        InvoiceStatus.MISMATCH -> VividRose
        InvoiceStatus.MANUAL_REVIEW -> AmberWarning
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val isGemini = result.extractedData?.let {
        it.invoiceNumber != null || it.date != null
    } == true

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (result.validationResult?.status) {
                        InvoiceStatus.MATCHED -> Icons.Default.CheckCircle
                        InvoiceStatus.MISMATCH -> Icons.Default.Error
                        InvoiceStatus.MANUAL_REVIEW -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isGemini) "Gemini AI Extraction Complete" else "Extraction Complete",
                        fontWeight = FontWeight.Bold,
                        color = if (LocalIsDarkTheme.current) Color.White else Color.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = result.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // Extracted data fields
            val data = result.extractedData
            if (data != null) {
                HorizontalDivider(
                    color = if (LocalIsDarkTheme.current)
                        Color.White.copy(alpha = 0.08f)
                    else
                        Color.Black.copy(alpha = 0.06f)
                )
                if (data.detectedPoNumber != null) {
                    ExtractedField(label = "PO Reference", value = data.detectedPoNumber!!)
                }
                if (data.vendorName != null) {
                    ExtractedField(label = "Vendor", value = data.vendorName!!)
                }
                if (data.totalAmount != null) {
                    val totalStr = "%.2f".format(data.totalAmount)
                    ExtractedField(label = "Total Amount", value = "\$$totalStr")
                }
                ExtractedField(label = "Line Items", value = "${data.lineItems.size} items detected")
                ExtractedField(label = "Confidence", value = "${"%.0f".format(data.confidenceScore)}%")
                if (data.invoiceNumber != null) {
                    ExtractedField(label = "Invoice Number", value = data.invoiceNumber!!)
                }
                if (data.date != null) {
                    ExtractedField(label = "Invoice Date", value = data.date!!)
                }
                if (data.taxAmount != null) {
                    val taxStr = "%.2f".format(data.taxAmount)
                    ExtractedField(label = "Tax Amount", value = "\$$taxStr")
                }
            }
        }
    }
}

@Composable
private fun ExtractedField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(
            value,
            color = if (LocalIsDarkTheme.current) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
