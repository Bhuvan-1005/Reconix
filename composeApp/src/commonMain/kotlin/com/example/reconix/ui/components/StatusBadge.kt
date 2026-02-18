package com.example.reconix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reconix.shared.InvoiceStatus
import com.example.reconix.ui.theme.*

// ─── private design tokens (avoid import-cycle with theme) ─────────────────
private val NavyCard       = Color(0xFF0D1B3E)
private val Emerald        = Color(0xFF00C896)
private val EmeraldBg      = Color(0xFF00C896).copy(alpha = 0.12f)
private val Crimson        = Color(0xFFFF2D5B)
private val CrimsonBg      = Color(0xFFFF2D5B).copy(alpha = 0.12f)
private val Gold           = Color(0xFFFFB800)
private val GoldBg         = Color(0xFFFFB800).copy(alpha = 0.12f)
private val Royal          = Color(0xFF4F7FFF)
private val RoyalBg        = Color(0xFF4F7FFF).copy(alpha = 0.12f)
private val PillShape      = RoundedCornerShape(100.dp)

// ─── data class for badge config ───────────────────────────────────────────
private data class BadgeConfig(
    val label: String,
    val textColor: Color,
    val bgColor: Color,
    val borderColor: Color,
    val dotColor: Color
)

private fun statusConfig(status: InvoiceStatus): BadgeConfig = when (status) {
    InvoiceStatus.MATCHED       -> BadgeConfig("MATCHED",       Emerald, EmeraldBg, Emerald.copy(alpha = 0.4f), Emerald)
    InvoiceStatus.MISMATCH      -> BadgeConfig("MISMATCH",      Crimson, CrimsonBg, Crimson.copy(alpha = 0.4f), Crimson)
    InvoiceStatus.PENDING       -> BadgeConfig("PENDING",       Gold,    GoldBg,    Gold.copy(alpha = 0.4f),    Gold)
    InvoiceStatus.MANUAL_REVIEW -> BadgeConfig("REVIEW",        Gold,    GoldBg,    Gold.copy(alpha = 0.4f),    Gold)
}

/**
 * ═══════════════════════════════════════════════════════════════
 *  StatusBadge — Premium pill badge for InvoiceStatus
 *
 *  • Live-color dot + ALL-CAPS label
 *  • Tinted background + hairline border
 *  • Pill shape  |  11 sp  |  Bold  |  0.5 sp letter-spacing
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun StatusBadge(
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) {
    val cfg = statusConfig(status)

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(cfg.bgColor)
            .border(width = 1.dp, color = cfg.borderColor, shape = PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // live-color dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(cfg.dotColor)
        )
        Text(
            text = cfg.label,
            color = cfg.textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            fontFamily = FontFamily.Default
        )
    }
}

/**
 * ActionStatusBadge — Pill badge for finance-manager approval states
 */
@Composable
fun ActionStatusBadge(
    actionType: String,
    modifier: Modifier = Modifier
) {
    val (textColor, bgColor, borderColor, label) = when (actionType.uppercase()) {
        "APPROVED"      -> listOf(Emerald, EmeraldBg, Emerald.copy(alpha=0.4f), "APPROVED")
        "REJECTED"      -> listOf(Crimson, CrimsonBg, Crimson.copy(alpha=0.4f), "REJECTED")
        "PENDING"       -> listOf(Gold,    GoldBg,    Gold.copy(alpha=0.4f),    "PENDING")
        "PAID"          -> listOf(Emerald, EmeraldBg, Emerald.copy(alpha=0.4f), "PAID")
        "MANUAL_REVIEW",
        "REVIEW"        -> listOf(Gold,    GoldBg,    Gold.copy(alpha=0.4f),    "REVIEW")
        else            -> listOf(Royal,   RoyalBg,   Royal.copy(alpha=0.4f),   actionType.uppercase())
    }.let { Triple(it[0] as Color, it[1] as Color, it[2] as Color).let { t -> listOf(t.first, t.second, t.third, it[3]) } }

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(bgColor as Color)
            .border(width = 1.dp, color = borderColor as Color, shape = PillShape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor as Color)
        )
        Text(
            text = label as String,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * MatchIndicator — Dot + label row for 3-way match result cells
 */
@Composable
fun MatchIndicator(
    isMatch: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    val color = if (isMatch) Emerald else Crimson
    val bg    = if (isMatch) EmeraldBg else CrimsonBg

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.3.sp
        )
    }
}

/**
 * PriorityBadge — Urgency tag used on invoice cards
 */
@Composable
fun PriorityBadge(
    priority: String = "HIGH",
    modifier: Modifier = Modifier
) {
    val (bg, textColor) = when (priority.uppercase()) {
        "HIGH"   -> Pair(CrimsonBg, Crimson)
        "MEDIUM" -> Pair(GoldBg,    Gold)
        else     -> Pair(RoyalBg,   Royal)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Text(
            text = priority.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}
