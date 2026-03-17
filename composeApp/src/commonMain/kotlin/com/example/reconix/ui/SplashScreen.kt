package com.example.reconix.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Brand Colors — pure black palette ──────────────────────
private val MidnightNavy  = Color(0xFF000000)
private val NavyCard       = Color(0xFF0D0D0D)
private val NavyBorder     = Color(0xFF1A1A1A)
private val RoyalBlue      = Color(0xFF4F7FFF)
private val RoyalBlueLight = Color(0xFF7EA9FF)
private val EmeraldGreen   = Color(0xFF00C896)
private val EmeraldGlow    = Color(0xFF00C896).copy(alpha = 0.25f)
private val SilverText     = Color(0xFFB8C8E8)
private val PureWhite      = Color(0xFFFFFFFF)
private val NeonCyan       = Color(0xFF00D9FF)

/**
 * ═══════════════════════════════════════════════════════════════
 *  Animated Splash Screen — Premium Fintech Edition
 *
 *  Animation timeline:
 *    0ms   – Glow pulse starts (infinite)
 *    0ms   – Logo scales in with spring overshoot
 *    600ms – Grid circuit lines draw in
 *    900ms – Title fades + slides up
 *    1100ms– Subtitle fades in
 *    2600ms– navigate to login
 * ═══════════════════════════════════════════════════════════════
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // ── Animation states ────────────────────────────────────
    val logoScale   = remember { Animatable(0f) }
    val textAlpha   = remember { Animatable(0f) }
    val textOffset  = remember { Animatable(32f) }
    val subAlpha    = remember { Animatable(0f) }
    val circuitAlpha= remember { Animatable(0f) }
    val progressWidth = remember { Animatable(0f) }  // 0 → 1 for loading bar

    // Infinite pulsing glow behind logo
    val glowPulse by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.4f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Pulsing ring around progress dot
    val ringScale by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 1f,
        targetValue  = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    // ── Live effect animations ────────────────────────────────
    val liveTransition = rememberInfiniteTransition(label = "live")

    // Horizontal scan line sweeping top → bottom
    val scanY by liveTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "scan"
    )
    // Orbit angle for dots circling the logo (0→360°)
    val orbitAngle by liveTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "orbit"
    )
    // Upward-drifting particles at different speeds and horizontal positions
    val p1y by liveTransition.animateFloat(
        initialValue = 1.15f, targetValue = -0.15f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing)),
        label = "p1y"
    )
    val p2y by liveTransition.animateFloat(
        initialValue = 1.15f, targetValue = -0.15f,
        animationSpec = infiniteRepeatable(
            tween(3400, easing = LinearEasing),
            initialStartOffset = StartOffset(900)
        ),
        label = "p2y"
    )
    val p3y by liveTransition.animateFloat(
        initialValue = 1.15f, targetValue = -0.15f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = LinearEasing),
            initialStartOffset = StartOffset(1600)
        ),
        label = "p3y"
    )
    val p4y by liveTransition.animateFloat(
        initialValue = 1.15f, targetValue = -0.15f,
        animationSpec = infiniteRepeatable(
            tween(3100, easing = LinearEasing),
            initialStartOffset = StartOffset(400)
        ),
        label = "p4y"
    )
    val p5y by liveTransition.animateFloat(
        initialValue = 1.15f, targetValue = -0.15f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = LinearEasing),
            initialStartOffset = StartOffset(1200)
        ),
        label = "p5y"
    )
    // Subtle data-node flicker for circuit dots
    val nodeFlicker by liveTransition.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "flicker"
    )

    LaunchedEffect(Unit) {
        // 1. Logo springs in (run progress bar in parallel)
        coroutineScope {
            launch {
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch { progressWidth.animateTo(1f, tween(2400, easing = LinearEasing)) }
        }
        // 2. Circuit lines fade in
        launch { circuitAlpha.animateTo(1f, tween(500)) }
        delay(250)

        // 3. Title text fades + slides up
        coroutineScope {
            launch { textAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
            launch { textOffset.animateTo(0f, tween(700, easing = FastOutSlowInEasing)) }
        }
        delay(200)

        // 4. Subtitle
        subAlpha.animateTo(1f, tween(500))

        // 5. Hold then navigate
        delay(700L)
        onSplashComplete()
    }

    // ── Root ────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {

        // ── Background ambient glow (decorative) ────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center)
                .alpha(glowPulse * 0.18f)
                .blur(80.dp)
                .background(RoyalBlue, CircleShape)
        )
        // ── Live background: dot grid + scan line + rising particles ─
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle dot grid (every 44dp)
            val grid = 44.dp.toPx()
            var gx = grid / 2
            while (gx < w) {
                var gy = grid / 2
                while (gy < h) {
                    drawCircle(
                        color = Color(0xFF0D1B3E),
                        radius = 1.2f,
                        center = androidx.compose.ui.geometry.Offset(gx, gy)
                    )
                    gy += grid
                }
                gx += grid
            }

            // Scanning glow band
            val sy = scanY * h
            val scanGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    EmeraldGreen.copy(alpha = 0.07f),
                    EmeraldGreen.copy(alpha = 0.14f),
                    EmeraldGreen.copy(alpha = 0.07f),
                    Color.Transparent
                ),
                startY = sy - 60f,
                endY   = sy + 60f
            )
            drawRect(
                brush = scanGradient,
                topLeft = androidx.compose.ui.geometry.Offset(0f, sy - 60f),
                size = Size(w, 120f)
            )
            // Bright leading edge
            drawLine(
                color = EmeraldGreen.copy(alpha = 0.30f),
                start = androidx.compose.ui.geometry.Offset(0f, sy),
                end   = androidx.compose.ui.geometry.Offset(w, sy),
                strokeWidth = 1f
            )

            // Rising particles with tails
            data class Particle(val xFraction: Float, val yValue: Float, val color: Color)
            val particles = listOf(
                Particle(0.12f, p1y, EmeraldGreen),
                Particle(0.31f, p2y, NeonCyan),
                Particle(0.54f, p3y, EmeraldGreen),
                Particle(0.73f, p4y, RoyalBlue),
                Particle(0.88f, p5y, NeonCyan),
            )
            particles.forEach { p ->
                val px = p.xFraction * w
                val py = p.yValue * h
                val alpha = when {
                    p.yValue < 0.08f -> ((p.yValue + 0.15f) * 4f).coerceIn(0f, 0.75f)
                    p.yValue > 0.88f -> ((1.15f - p.yValue) * 4f).coerceIn(0f, 0.75f)
                    else -> 0.75f
                }
                // Tail
                drawLine(
                    color = p.color.copy(alpha = alpha * 0.25f),
                    start = androidx.compose.ui.geometry.Offset(px, py + 22f),
                    end   = androidx.compose.ui.geometry.Offset(px, py + 4f),
                    strokeWidth = 1.5f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // Core dot
                drawCircle(p.color.copy(alpha = alpha), radius = 2.8f,
                    center = androidx.compose.ui.geometry.Offset(px, py))
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            // ── Logo Container: 3D Glassmorphism Card + Orbit ─
            Box(contentAlignment = Alignment.Center) {

                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(glowPulse * 0.30f)
                        .blur(24.dp)
                        .background(EmeraldGreen, CircleShape)
                )

                // Orbiting dots around the logo card
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .alpha(logoScale.value)
                ) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val r  = size.width / 2 - 6.dp.toPx()
                    val a  = (orbitAngle * PI / 180.0).toFloat()
                    // Three equidistant orbit dots
                    listOf(
                        Triple(EmeraldGreen,      5.5f, a),
                        Triple(NeonCyan,          4f,   a + (2f * PI / 3).toFloat()),
                        Triple(RoyalBlueLight,    3.5f, a + (4f * PI / 3).toFloat()),
                    ).forEach { (color, radius, angle) ->
                        drawCircle(
                            color  = color.copy(alpha = 0.9f),
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(
                                cx + r * cos(angle),
                                cy + r * sin(angle)
                            )
                        )
                    }
                    // Faint orbit ring
                    drawCircle(
                        color  = EmeraldGreen.copy(alpha = 0.12f),
                        radius = r,
                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                        style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )
                }

                // Glass card backing
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = NavyCard,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF111111).copy(alpha = 0.6f),
                                        Color(0xFF000000).copy(alpha = 0.9f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Custom canvas logo: document + AI circuit + checkmark
                        Canvas(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(logoScale.value)
                        ) {
                            val w = size.width
                            val h = size.height

                            // ── Document body ────────────────
                            val docPath = Path().apply {
                                moveTo(w * 0.20f, h * 0.05f)
                                lineTo(w * 0.65f, h * 0.05f)
                                lineTo(w * 0.85f, h * 0.22f)   // folded corner
                                lineTo(w * 0.85f, h * 0.88f)
                                lineTo(w * 0.15f, h * 0.88f)
                                close()
                            }
                            drawPath(
                                path = docPath,
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF1A1A1A), Color(0xFF000000)),
                                    start = Offset(0f, 0f),
                                    end = Offset(w, h)
                                ),
                                style = Fill
                            )
                            drawPath(
                                path = docPath,
                                color = NavyBorder.copy(alpha = 0.7f),
                                style = Stroke(width = 2f)
                            )

                            // Folded corner triangle
                            val corner = Path().apply {
                                moveTo(w * 0.65f, h * 0.05f)
                                lineTo(w * 0.85f, h * 0.22f)
                                lineTo(w * 0.65f, h * 0.22f)
                                close()
                            }
                            drawPath(corner, color = RoyalBlue.copy(alpha = 0.35f), style = Fill)

                            // ── Document text lines (circuit) ─
                            val lineColor = RoyalBlueLight.copy(alpha = circuitAlpha.value * 0.5f)
                            for (i in 0..2) {
                                val y = h * (0.38f + i * 0.10f)
                                val xEnd = if (i == 2) w * 0.56f else w * 0.70f
                                drawLine(lineColor, Offset(w * 0.25f, y), Offset(xEnd, y), strokeWidth = 2f, cap = StrokeCap.Round)
                            }

                            // ── AI circuit node dots ──────────
                            val dotColor = NeonCyan.copy(alpha = circuitAlpha.value * 0.8f)
                            listOf(
                                Offset(w * 0.25f, h * 0.38f),
                                Offset(w * 0.70f, h * 0.38f),
                                Offset(w * 0.56f, h * 0.58f)
                            ).forEach { pos ->
                                drawCircle(dotColor, radius = 3f, center = pos)
                            }

                            // ── Emerald Checkmark (success) ───
                            val checkPath = Path().apply {
                                moveTo(w * 0.30f, h * 0.56f)
                                lineTo(w * 0.44f, h * 0.72f)
                                lineTo(w * 0.72f, h * 0.40f)
                            }
                            drawPath(
                                path = checkPath,
                                brush = Brush.linearGradient(
                                    listOf(EmeraldGreen, Color(0xFF00FFB8)),
                                    start = Offset(w * 0.30f, h * 0.72f),
                                    end   = Offset(w * 0.72f, h * 0.40f)
                                ),
                                style = Stroke(
                                    width = w * 0.072f,
                                    cap   = StrokeCap.Round,
                                    join  = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // Luminous border ring around card
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    EmeraldGreen.copy(alpha = glowPulse * 0.5f),
                                    RoyalBlue.copy(alpha = glowPulse * 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── App Title ───────────────────────────────────
            Text(
                text = "Invoice Validator",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffset.value.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Automated 3-Way Match",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SilverText,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .alpha(subAlpha.value)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "POWERED BY AI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(subAlpha.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Animated loading progress bar ─────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFF0D1B3E))
                    .alpha(subAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressWidth.value)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(EmeraldGreen, NeonCyan)
                            )
                        )
                )
                // Glowing head of the progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressWidth.value)
                        .fillMaxHeight()
                        .alpha(glowPulse * 0.7f)
                        .blur(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, EmeraldGreen)
                            )
                        )
                )
            }

        } // end Column

        // ── Pulsing FAB-style dot at bottom ─────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple ring
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(ringScale)
                    .alpha((2f - ringScale) * 0.35f)
                    .background(EmeraldGreen, CircleShape)
            )
            // Core dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(EmeraldGreen, CircleShape)
            )
        }
    }
}
