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
    modifier: Modifier = Modifier
) {
    var isUploading by remember { mutableStateOf(false) }
    var uploadResult by remember { mutableStateOf<InvoiceUploadResponse?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepSlateBlue, SlateBlue800, DeepSlateBlue)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
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
                            .background(GlassWhite5)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            "Upload Invoice",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            "Upload PDF for automated extraction & validation",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoolGray
                        )
                    }
                }
            }

            // ── Upload Zone ──
            item {
                UploadDropZone(
                    isUploading = isUploading,
                    selectedFileName = selectedFileName,
                    onFileSelected = { fileName ->
                        selectedFileName = fileName
                        isUploading = true
                        // In a real app, this would trigger the actual file upload
                        // For now, we simulate the upload flow
                    }
                )
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
                                color = PureWhite
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
                            title = "OCR Extraction",
                            description = "AI extracts PO#, line items, vendor & totals",
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
                    UploadResultCard(result = uploadResult!!)
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
                                color = PureWhite,
                                fontSize = 14.sp
                            )
                            Text(
                                "Vendors can also email invoices to invoices@reconix.com — they'll be auto-detected and processed",
                                color = CoolGray,
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
    selectedFileName: String?,
    onFileSelected: (String) -> Unit
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
                .clickable(enabled = !isUploading) { onFileSelected("Invoice_Sample.pdf") }
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = NeonCyan,
                        strokeWidth = 3.dp
                    )
                    Text(
                        "Processing...",
                        color = NeonCyanLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (selectedFileName != null) {
                        Text(
                            selectedFileName,
                            color = CoolGray,
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
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "or tap to select file",
                        color = CoolGray,
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
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SlateBlue700
    ) {
        Text(
            type,
            color = CoolGray,
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
                color = PureWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                description,
                color = CoolGray,
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
        else -> CoolGray
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (result.validationResult?.status) {
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
                        "Extraction Complete",
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        fontSize = 16.sp
                    )
                    Text(
                        result.message,
                        color = CoolGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Show extracted data
            result.extractedData?.let { data ->
                HorizontalDivider(color = GlassBorder)

                if (data.detectedPoNumber != null) {
                    ExtractedField("PO Reference", data.detectedPoNumber!!)
                }
                if (data.vendorName != null) {
                    ExtractedField("Vendor", data.vendorName!!)
                }
                if (data.totalAmount != null) {
                    ExtractedField("Total Amount", "$${String.format("%.2f", data.totalAmount)}")
                }
                ExtractedField("Line Items", "${data.lineItems.size} items detected")
                ExtractedField("Confidence", "${String.format("%.0f", data.confidenceScore)}%")
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
        Text(label, color = CoolGray, fontSize = 13.sp)
        Text(
            value,
            color = PureWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
