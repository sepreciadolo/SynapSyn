package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SliceLevel {
    GANGLIONIC,
    SUPRAGANGLIONIC
}

@Composable
fun AspectsVisualizer(
    selectedRegions: Set<String>,
    onRegionToggled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D11), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF26262F), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VISOR RADIOLÓGICO ASPECTS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C90A6),
                        letterSpacing = 1.2.sp
                    )
                )
                Text(
                    text = "Toque las zonas con hipodensidad/isquemia aguda para restar puntos",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5E6278))
                )
            }
            // State Badge
            Surface(
                color = if (selectedRegions.isEmpty()) Color(0xFF1B3D2F) else Color(0xFF3D1B1B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (selectedRegions.isEmpty()) "NORMAL" else "ISQUEMIA (${selectedRegions.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selectedRegions.isEmpty()) Color(0xFF50CD89) else Color(0xFFF1416C)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large monitors layout (reponsive column on narrow, row on wide)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth > 560.dp
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BrainSliceMonitor(
                        level = SliceLevel.GANGLIONIC,
                        title = "Slice A: Nivel Ganglionar",
                        selectedRegions = selectedRegions,
                        onRegionToggled = onRegionToggled,
                        modifier = Modifier.weight(1f).padding(8.dp)
                    )
                    BrainSliceMonitor(
                        level = SliceLevel.SUPRAGANGLIONIC,
                        title = "Slice B: Supraganglionar",
                        selectedRegions = selectedRegions,
                        onRegionToggled = onRegionToggled,
                        modifier = Modifier.weight(1f).padding(8.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrainSliceMonitor(
                        level = SliceLevel.GANGLIONIC,
                        title = "Slice A: Nivel Ganglionar",
                        selectedRegions = selectedRegions,
                        onRegionToggled = onRegionToggled,
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                    BrainSliceMonitor(
                        level = SliceLevel.SUPRAGANGLIONIC,
                        title = "Slice B: Supraganglionar",
                        selectedRegions = selectedRegions,
                        onRegionToggled = onRegionToggled,
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BrainSliceMonitor(
    level: SliceLevel,
    title: String,
    selectedRegions: Set<String>,
    onRegionToggled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .background(Color(0xFF13131A), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E1E28), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5A8BA)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .aspectRatio(1.2f)
                .fillMaxWidth()
                .background(Color(0xFF09090C), shape = RoundedCornerShape(8.dp))
                .border(0.5.dp, Color(0xFF1E1E24), shape = RoundedCornerShape(8.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(level, selectedRegions) {
                        detectTapGestures { offset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val regionId = getRegionAtOffset(offset, width, height, level)
                            if (regionId != null) {
                                onRegionToggled(regionId)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val rMax = minOf(w - 60f, h - 40f) / 2f

                // Midline X is on the right of left hemisphere
                val cx = w / 2f + rMax * 0.4f
                val cy = h / 2f

                // Draw medical crosshairs/grid in background
                drawMedicalGrid(w, h, cx, cy, rMax)

                // 1. Draw Skull boundary (Hyperdense bone outline)
                drawOval(
                    color = Color(0xFFE0E0EB).copy(alpha = 0.8f),
                    topLeft = Offset(cx - rMax * 1.05f, cy - rMax * 1.05f),
                    size = Size(rMax * 2.1f, rMax * 2.1f),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw styled cerebrospinal fluid (CSF) dark layer inside scalp border
                drawOval(
                    color = Color(0xFF1C1D2A),
                    topLeft = Offset(cx - rMax * 1.02f, cy - rMax * 1.02f),
                    size = Size(rMax * 2.04f, rMax * 2.04f)
                )

                // Midline dashed reference line
                drawLine(
                    color = Color(0xFF323B54),
                    start = Offset(cx, cy - rMax * 1.05f),
                    end = Offset(cx, cy + rMax * 1.05f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    strokeWidth = 1.dp.toPx()
                )

                if (level == SliceLevel.GANGLIONIC) {
                    // --- GANGLIONIC LEVEL ---
                    // Draw outer cortical sectors (M1, M2, M3)
                    // Normal state: subtle charcoal/teal, Affected: bright alert red
                    val isM1 = selectedRegions.contains("M1")
                    val isM2 = selectedRegions.contains("M2")
                    val isM3 = selectedRegions.contains("M3")

                    // M1 Arc (Anterior Cortex)
                    drawArc(
                        color = if (isM1) Color(0xFFF1416C) else Color(0xFF232334),
                        startAngle = 270f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M2 Arc (Lateral Cortex)
                    drawArc(
                        color = if (isM2) Color(0xFFF1416C) else Color(0xFF1D2925),
                        startAngle = 210f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M3 Arc (Posterior Cortex)
                    drawArc(
                        color = if (isM3) Color(0xFFF1416C) else Color(0xFF232334),
                        startAngle = 150f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // Inner mask (radius 0.55 R) which hollows out the outer ribbon of cortex
                    // and houses/begins deep brain structures
                    val rInner = rMax * 0.55f
                    drawArc(
                        color = Color(0xFF12121A),
                        startAngle = 270f,
                        sweepAngle = -180f,
                        useCenter = true,
                        topLeft = Offset(cx - rInner, cy - rInner),
                        size = Size(rInner * 2f, rInner * 2f)
                    )

                    // Insula (I) ribbon (outer lip of inner core)
                    // Spans from 215 to 145 degrees
                    val isI = selectedRegions.contains("I")
                    drawArc(
                        color = if (isI) Color(0xFFF1416C) else Color(0xFF3A3A4A),
                        startAngle = 220f,
                        sweepAngle = -80f,
                        useCenter = true,
                        topLeft = Offset(cx - rInner, cy - rInner),
                        size = Size(rInner * 2f, rInner * 2f)
                    )

                    // Inner-inner core mask (radius 0.42 R) to finalize the Insula ribbon thickness
                    val rCore = rMax * 0.42f
                    drawArc(
                        color = Color(0xFF0F0F15),
                        startAngle = 270f,
                        sweepAngle = -180f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore, cy - rCore),
                        size = Size(rCore * 2f, rCore * 2f)
                    )

                    // Draw caudate (C) anterior-medially
                    val isC = selectedRegions.contains("C")
                    drawArc(
                        color = if (isC) Color(0xFFF1416C) else Color(0xFF2C2C3C),
                        startAngle = 270f,
                        sweepAngle = -50f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore * 1.0f, cy - rCore * 1.0f),
                        size = Size(rCore * 2f, rCore * 2f)
                    )

                    // Draw capsula interna (IC) middle-medially/posterior-medially
                    val isIc = selectedRegions.contains("IC")
                    drawArc(
                        color = if (isIc) Color(0xFFF1416C) else Color(0xFF313D4A),
                        startAngle = 220f,
                        sweepAngle = -45f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore * 0.9f, cy - rCore * 0.9f),
                        size = Size(rCore * 1.8f, rCore * 1.8f)
                    )

                    // Draw Lentiform nucleus (L) center-laterally
                    val isL = selectedRegions.contains("L")
                    drawArc(
                        color = if (isL) Color(0xFFF1416C) else Color(0xFF223630),
                        startAngle = 220f,
                        sweepAngle = -75f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore * 0.72f, cy - rCore * 0.72f),
                        size = Size(rCore * 1.44f, rCore * 1.44f)
                    )

                    // Ventricle schematic line (lateral ventricles in dark blue/glowing)
                    drawVentricles(cx, cy, rCore)

                    // Sector outlines to give visual structure
                    drawSectorOutlinesGanglionic(cx, cy, rMax, rInner, rCore)

                    // Draw centered TEXT annotations
                    drawTextLabelsGanglionic(cx, cy, rMax, rInner, rCore, textMeasurer, selectedRegions)

                } else {
                    // --- SUPRAGANGLIONIC LEVEL ---
                    // Draw outer cortical sectors (M4, M5, M6)
                    val isM4 = selectedRegions.contains("M4")
                    val isM5 = selectedRegions.contains("M5")
                    val isM6 = selectedRegions.contains("M6")

                    // M4 Arc
                    drawArc(
                        color = if (isM4) Color(0xFFF1416C) else Color(0xFF232334),
                        startAngle = 270f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M5 Arc
                    drawArc(
                        color = if (isM5) Color(0xFFF1416C) else Color(0xFF1D2925),
                        startAngle = 210f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M6 Arc
                    drawArc(
                        color = if (isM6) Color(0xFFF1416C) else Color(0xFF232334),
                        startAngle = 150f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // Inner brain parenchyma background
                    val rInner = rMax * 0.55f
                    drawOval(
                        color = Color(0xFF14141E),
                        topLeft = Offset(cx - rInner, cy - rInner),
                        size = Size(rInner * 2f, rInner * 2f)
                    )

                    // Center reference ventricles at higher sagittal slice
                    drawHighVentricles(cx, cy, rInner)

                    // Sector outlines
                    drawSectorOutlinesSupraganglionic(cx, cy, rMax, rInner)

                    // Text labels
                    drawTextLabelsSupraganglionic(cx, cy, rMax, rInner, textMeasurer, selectedRegions)
                }

                // Reference anatomical indicators: A (Anterior), P (Posterior), L (Lateral)
                drawAnatomicalIndicators(cx, cy, rMax, textMeasurer)
            }
        }
    }
}

fun DrawScope.drawMedicalGrid(w: Float, h: Float, cx: Float, cy: Float, rMax: Float) {
    // Elegant circles and axes on the scanners
    drawCircle(
        color = Color(0xFF151821),
        radius = rMax * 1.2f,
        center = Offset(cx, cy),
        style = Stroke(width = 0.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
    )
    drawCircle(
        color = Color(0xFF1F2231),
        radius = rMax,
        center = Offset(cx, cy),
        style = Stroke(width = 0.5.dp.toPx())
    )
    // Horizontal radar/guideline
    drawLine(
        color = Color(0xFF151821),
        start = Offset(cx - rMax * 1.3f, cy),
        end = Offset(cx + rMax * 1.3f, cy),
        strokeWidth = 0.5.dp.toPx()
    )
}

fun DrawScope.drawVentricles(cx: Float, cy: Float, rCore: Float) {
    // Beautiful stylized dark ventricles represented in neonatal neon blue
    val path = androidx.compose.ui.graphics.Path().apply {
        // Anterior horn
        moveTo(cx - 2f, cy - rCore * 0.6f)
        quadraticTo(cx - rCore * 0.15f, cy - rCore * 0.4f, cx - rCore * 0.25f, cy - rCore * 0.1f)
        quadraticTo(cx - rCore * 0.15f, cy - rCore * 0.18f, cx - 2f, cy - rCore * 0.22f)
        close()
        // Posterior horn
        moveTo(cx - 2f, cy + rCore * 0.1f)
        quadraticTo(cx - rCore * 0.12f, cy + rCore * 0.25f, cx - rCore * 0.18f, cy + rCore * 0.65f)
        quadraticTo(cx - rCore * 0.08f, cy + rCore * 0.38f, cx - 2f, cy + rCore * 0.28f)
        close()
    }
    drawPath(
        path = path,
        color = Color(0xFF00B0FF).copy(alpha = 0.15f)
    )
    drawPath(
        path = path,
        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
        style = Stroke(width = 1.dp.toPx())
    )
}

fun DrawScope.drawHighVentricles(cx: Float, cy: Float, rInner: Float) {
    // Upper level parietal ventricles are small thin lines
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx - 5f, cy - rInner * 0.35f)
        quadraticTo(cx - rInner * 0.1f, cy - rInner * 0.1f, cx - 5f, cy + rInner * 0.2f)
    }
    drawPath(
        path = path,
        color = Color(0xFF00E5FF).copy(alpha = 0.3f),
        style = Stroke(width = 1.5.dp.toPx())
    )
}

fun DrawScope.drawSectorOutlinesGanglionic(cx: Float, cy: Float, rMax: Float, rInner: Float, rCore: Float) {
    val outlineColor = Color(0xFF333344)
    val strokeWidth = 1.dp.toPx()

    // Outer bounding ring arc
    drawArc(
        color = outlineColor,
        startAngle = 270f,
        sweepAngle = -180f,
        useCenter = false,
        topLeft = Offset(cx - rMax, cy - rMax),
        size = Size(rMax * 2f, rMax * 2f),
        style = Stroke(width = strokeWidth)
    )

    // Inner ring arc
    drawArc(
        color = outlineColor,
        startAngle = 270f,
        sweepAngle = -180f,
        useCenter = false,
        topLeft = Offset(cx - rInner, cy - rInner),
        size = Size(rInner * 2f, rInner * 2f),
        style = Stroke(width = strokeWidth)
    )

    // Divider rays (M1, M2, M3 radial limits)
    // Ray 1: Top limit of M1 is midline (vertical straight up, 270 degrees)
    drawLine(outlineColor, Offset(cx, cy - rMax), Offset(cx, cy - rInner), strokeWidth)

    // Ray 2: divider M1 / M2 at 210 degrees
    val angle1Rad = Math.toRadians(210.0)
    drawLine(
        outlineColor,
        Offset(cx + (rInner * cos(angle1Rad)).toFloat(), cy + (rInner * sin(angle1Rad)).toFloat()),
        Offset(cx + (rMax * cos(angle1Rad)).toFloat(), cy + (rMax * sin(angle1Rad)).toFloat()),
        strokeWidth
    )

    // Ray 3: divider M2 / M3 at 150 degrees
    val angle2Rad = Math.toRadians(150.0)
    drawLine(
        outlineColor,
        Offset(cx + (rInner * cos(angle2Rad)).toFloat(), cy + (rInner * sin(angle2Rad)).toFloat()),
        Offset(cx + (rMax * cos(angle2Rad)).toFloat(), cy + (rMax * sin(angle2Rad)).toFloat()),
        strokeWidth
    )

    // Ray 4: Bottom limit of M3 is midline (vertical straight down, 90 degrees)
    drawLine(outlineColor, Offset(cx, cy + rMax), Offset(cx, cy + rInner), strokeWidth)

    // Core divider outlines
    drawArc(
        color = outlineColor,
        startAngle = 270f,
        sweepAngle = -180f,
        useCenter = false,
        topLeft = Offset(cx - rCore, cy - rCore),
        size = Size(rCore * 2f, rCore * 2f),
        style = Stroke(width = strokeWidth)
    )
}

fun DrawScope.drawSectorOutlinesSupraganglionic(cx: Float, cy: Float, rMax: Float, rInner: Float) {
    val outlineColor = Color(0xFF333344)
    val strokeWidth = 1.2.dp.toPx()

    // Outer bounding ring arc
    drawArc(
        color = outlineColor,
        startAngle = 270f,
        sweepAngle = -180f,
        useCenter = false,
        topLeft = Offset(cx - rMax, cy - rMax),
        size = Size(rMax * 2f, rMax * 2f),
        style = Stroke(width = strokeWidth)
    )

    // Inner bounds
    drawArc(
        color = outlineColor,
        startAngle = 270f,
        sweepAngle = -180f,
        useCenter = false,
        topLeft = Offset(cx - rInner, cy - rInner),
        size = Size(rInner * 2f, rInner * 2f),
        style = Stroke(width = strokeWidth)
    )

    // Rays dividing M4, M5, M6
    drawLine(outlineColor, Offset(cx, cy - rMax), Offset(cx, cy - rInner), strokeWidth)

    val angle1Rad = Math.toRadians(210.0)
    drawLine(
        outlineColor,
        Offset(cx + (rInner * cos(angle1Rad)).toFloat(), cy + (rInner * sin(angle1Rad)).toFloat()),
        Offset(cx + (rMax * cos(angle1Rad)).toFloat(), cy + (rMax * sin(angle1Rad)).toFloat()),
        strokeWidth
    )

    val angle2Rad = Math.toRadians(150.0)
    drawLine(
        outlineColor,
        Offset(cx + (rInner * cos(angle2Rad)).toFloat(), cy + (rInner * sin(angle2Rad)).toFloat()),
        Offset(cx + (rMax * cos(angle2Rad)).toFloat(), cy + (rMax * sin(angle2Rad)).toFloat()),
        strokeWidth
    )

    drawLine(outlineColor, Offset(cx, cy + rMax), Offset(cx, cy + rInner), strokeWidth)
}

fun DrawScope.drawTextLabelsGanglionic(
    cx: Float, cy: Float, rMax: Float, rInner: Float, rCore: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    selectedRegions: Set<String>
) {
    // Calculate centroids for text positioning
    val rCortexMid = (rMax + rInner) / 2f
    val rDeepMid = (rInner + rCore) / 2f
    val rInnerCore = rCore * 0.5f

    // M1 (Anterior, angle 240)
    drawTextCentroid("M1", cx, cy, rCortexMid, 240f, textMeasurer, selectedRegions.contains("M1"))
    // M2 (Lateral, angle 180)
    drawTextCentroid("M2", cx, cy, rCortexMid, 180f, textMeasurer, selectedRegions.contains("M2"))
    // M3 (Posterior, angle 120)
    drawTextCentroid("M3", cx, cy, rCortexMid, 120f, textMeasurer, selectedRegions.contains("M3"))

    // I (Insula, angle 180 at outer lip of inner core)
    drawTextCentroid("I", cx, cy, rDeepMid, 180f, textMeasurer, selectedRegions.contains("I"))

    // C (Caudate, anterior inner)
    drawTextCentroid("C", cx, cy, rCore * 0.72f, 245f, textMeasurer, selectedRegions.contains("C"))
    // IC (Internal Capsule, mid-diagonal inner)
    drawTextCentroid("IC", cx, cy, rCore * 0.75f, 195f, textMeasurer, selectedRegions.contains("IC"))
    // L (Lentiform, outer-middle inner)
    drawTextCentroid("L", cx, cy, rCore * 0.45f, 170f, textMeasurer, selectedRegions.contains("L"))
}

fun DrawScope.drawTextLabelsSupraganglionic(
    cx: Float, cy: Float, rMax: Float, rInner: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    selectedRegions: Set<String>
) {
    val rCortexMid = (rMax + rInner) / 2f

    // M4 (Anterior, angle 240)
    drawTextCentroid("M4", cx, cy, rCortexMid, 240f, textMeasurer, selectedRegions.contains("M4"))
    // M5 (Lateral, angle 180)
    drawTextCentroid("M5", cx, cy, rCortexMid, 180f, textMeasurer, selectedRegions.contains("M5"))
    // M6 (Posterior, angle 120)
    drawTextCentroid("M6", cx, cy, rCortexMid, 120f, textMeasurer, selectedRegions.contains("M6"))
}

fun DrawScope.drawTextCentroid(
    label: String, cx: Float, cy: Float, radius: Float, angleDegree: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    isActive: Boolean
) {
    val angleRad = Math.toRadians(angleDegree.toDouble())
    val tx = cx + (radius * cos(angleRad)).toFloat()
    val ty = cy + (radius * sin(angleRad)).toFloat()

    val textStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = if (isActive) Color.White else Color(0xFFF1F1F8)
    )

    val sizeMeasure = textMeasurer.measure(label, textStyle)
    val textWidth = sizeMeasure.size.width
    val textHeight = sizeMeasure.size.height

    // Draw solid dark background for the letter so it stands out crisp and clean
    drawCircle(
        color = if (isActive) Color(0xFFC7153B) else Color(0xFF2E2E3E),
        radius = maxOf(textWidth, textHeight).toFloat() * 0.75f,
        center = Offset(tx, ty)
    )

    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(tx - textWidth / 2f, ty - textHeight / 2f),
        style = textStyle,
        size = Size(textWidth.toFloat(), textHeight.toFloat())
    )
}

fun DrawScope.drawAnatomicalIndicators(cx: Float, cy: Float, rMax: Float, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
    val indicatorStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = Color(0xFF5E6278)
    )

    // A (Anterior)
    val sizeA = textMeasurer.measure("A (ANT)", indicatorStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = "A (ANT)",
        topLeft = Offset(cx - sizeA.size.width / 2f, cy - rMax * 1.25f),
        style = indicatorStyle,
        size = Size(sizeA.size.width.toFloat(), sizeA.size.height.toFloat())
    )

    // P (Posterior)
    val sizeP = textMeasurer.measure("P (POST)", indicatorStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = "P (POST)",
        topLeft = Offset(cx - sizeP.size.width / 2f, cy + rMax * 1.15f),
        style = indicatorStyle,
        size = Size(sizeP.size.width.toFloat(), sizeP.size.height.toFloat())
    )

    // L (Lateral)
    val sizeL = textMeasurer.measure("L (LAT)", indicatorStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = "L (LAT)",
        topLeft = Offset(cx - rMax * 1.30f, cy - sizeL.size.height / 2f),
        style = indicatorStyle,
        size = Size(sizeL.size.width.toFloat(), sizeL.size.height.toFloat())
    )

    // midline guideline medially
    val sizeM = textMeasurer.measure("LÍNEA MEDIA", indicatorStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = "LÍNEA MEDIA",
        topLeft = Offset(cx + 8f, cy - rMax * 1.0f),
        style = indicatorStyle,
        size = Size(sizeM.size.width.toFloat(), sizeM.size.height.toFloat())
    )
}

/**
 * Advanced polar and modular logic to map screen coordinates to exact ASPECTS anatomical territories
 */
fun getRegionAtOffset(offset: Offset, w: Float, h: Float, level: SliceLevel): String? {
    val rMax = minOf(w - 60f, h - 40f) / 2f
    val cx = w / 2f + rMax * 0.4f
    val cy = h / 2f

    val dx = offset.x - cx
    val dy = offset.y - cy

    // Ignore taps to the right of the midline (straight sagittal axis)
    if (dx > 0) return null

    // Radius percentage checks map the hemisphere
    val dist = sqrt(dx * dx + dy * dy)
    val rNorm = dist / rMax

    if (rNorm > 1.15) return null // Out of skull

    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) // in range [-180, 180]
    // Continuous angle sweep: top is -90 degree, going down to -270 degrees
    if (angle > 0) {
        angle = angle - 360.0
    }

    if (level == SliceLevel.GANGLIONIC) {
        // Ganglionic division logic
        if (rNorm > 0.55 && rNorm <= 1.15) {
            // Cortex: M1, M2, M3
            return when {
                angle in -150.0..-90.0 -> "M1"
                angle in -210.0..-150.0 -> "M2"
                angle in -270.0..-210.0 -> "M3"
                else -> null
            }
        } else if (rNorm <= 0.55) {
            // Subcortical deep structure core
            // Insula outer band (radius between 0.42 and 0.55 on lateral aspect)
            if (rNorm > 0.42 && angle in -220.0..-140.0) {
                return "I"
            }
            // Inner nuclear core details (radius < 0.42)
            return when {
                // Caudate (anterior-medial, angle 270 to 220)
                angle in -270.0..-220.0 -> "C"
                // Internal capsule (diagonal divider)
                angle in -220.0..-175.0 && rNorm > 0.22 -> "IC"
                // Lentiform (axial gray core)
                else -> "L"
            }
        }
    } else {
        // Supraganglionic cortical only: M4, M5, M6
        return when {
            angle in -150.0..-90.0 -> "M4"
            angle in -210.0..-150.0 -> "M5"
            angle in -270.0..-210.0 -> "M6"
            else -> null
        }
    }
    return null
}
