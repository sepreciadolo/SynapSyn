package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
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

enum class PcSliceLevel {
    THALAMI,
    MIDBRAIN,
    PONS_CEREBELLUM
}

// Diagnostic Atlas Colors from Reference Images
object DiagnosticColors {
    // ACM (Anterior)
    val Caudate = Color(0xFF3B82F6)        // Blue (C)
    val Lentiform = Color(0xFFEF4444)      // Red (L)
    val InternalCapsule = Color(0xFF84CC16)// Green (IC)
    val Insula = Color(0xFF8B5CF6)         // Purple (I)
    val M1 = Color(0xFFF97316)             // Orange
    val M2 = Color(0xFFF59E0B)             // Amber
    val M3 = Color(0xFF06B6D4)             // Teal/Light Blue
    val M4 = Color(0xFFF43F5E)             // Coral/Pinkish-Red
    val M5 = Color(0xFFA855F7)             // Purple/Violet
    val M6 = Color(0xFF22D3EE)             // Cyan/Teal

    // Posterior (pc-ASPECTS)
    val Thalamus = Color(0xFF3B82F6)       // Blue (T)
    val Occipital = Color(0xFF84CC16)      // Green/Lime (OL)
    val Midbrain = Color(0xFFEF4444)       // Red (M)
    val Pons = Color(0xFF3B82F6)           // Blue (P)
    val Cerebellum = Color(0xFFF97316)     // Orange (C)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AspectsVisualizer(
    selectedRegions: Set<String>,
    onRegionToggled: (String) -> Unit,
    selectedPcRegions: Set<String>,
    onPcRegionToggled: (String) -> Unit,
    isPosterior: Boolean,
    onModeChanged: (Boolean) -> Unit,
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
        // Mode Selector (Anterior vs Posterior)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF13131A), shape = RoundedCornerShape(8.dp))
                .border(0.5.dp, Color(0xFF22222E), shape = RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (!isPosterior) Color(0xFF26263F) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onModeChanged(false) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Circulación Anterior (ACM)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (!isPosterior) Color.White else Color(0xFF8C90A6)
                    )
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isPosterior) Color(0xFF26263F) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onModeChanged(true) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Circulación Posterior (pc-ASPECTS)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPosterior) Color.White else Color(0xFF8C90A6)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPosterior) "VISOR pc-ASPECTS (FOSA POSTERIOR)" else "VISOR ACM ASPECTS (ANTERIOR)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C90A6),
                        letterSpacing = 1.2.sp
                    )
                )
                Text(
                    text = if (isPosterior) 
                        "Toque las áreas isquémicas en fosa posterior para restar puntaje (T=1, OL=1, CE=1, M=2, P=2)"
                        else "Toque las áreas con isquemia o hipodensidad TAC de la ACM para restar puntos (M1-M6, subcorticales 1 pt c/u)",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5E6278))
                )
            }
            // State Badge
            val totalIschemicAreas = if (isPosterior) selectedPcRegions.size else selectedRegions.size
            Surface(
                color = if (totalIschemicAreas == 0) Color(0xFF1B3D2F) else Color(0xFF3D1B1B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (totalIschemicAreas == 0) "SANO / NORMAL" else "ISQUEMIA ($totalIschemicAreas)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (totalIschemicAreas == 0) Color(0xFF50CD89) else Color(0xFFF1416C)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Screen layout
        if (!isPosterior) {
            // --- ANTERIOR ACM LAYOUT ---
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth > 560.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BrainSliceMonitor(
                            level = SliceLevel.GANGLIONIC,
                            title = "Corte A: Nivel Ganglionar",
                            selectedRegions = selectedRegions,
                            onRegionToggled = onRegionToggled,
                            modifier = Modifier.weight(1f).padding(8.dp)
                        )
                        BrainSliceMonitor(
                            level = SliceLevel.SUPRAGANGLIONIC,
                            title = "Corte B: Supraganglionar",
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
                            title = "Corte A: Nivel Ganglionar",
                            selectedRegions = selectedRegions,
                            onRegionToggled = onRegionToggled,
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                        BrainSliceMonitor(
                            level = SliceLevel.SUPRAGANGLIONIC,
                            title = "Corte B: Supraganglionar",
                            selectedRegions = selectedRegions,
                            onRegionToggled = onRegionToggled,
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                    }
                }
            }
        } else {
            // --- POSTERIOR FOSA LAYOUT (3 slices: Thalami, Midbrain, Pons/Cerebellum) ---
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth > 720.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PcSliceMonitor(
                            level = PcSliceLevel.THALAMI,
                            title = "1. Nivel Tálamos",
                            selectedRegions = selectedPcRegions,
                            onRegionToggled = onPcRegionToggled,
                            modifier = Modifier.weight(1f).padding(4.dp)
                        )
                        PcSliceMonitor(
                            level = PcSliceLevel.MIDBRAIN,
                            title = "2. Nivel Mesencéfalo",
                            selectedRegions = selectedPcRegions,
                            onRegionToggled = onPcRegionToggled,
                            modifier = Modifier.weight(1f).padding(4.dp)
                        )
                        PcSliceMonitor(
                            level = PcSliceLevel.PONS_CEREBELLUM,
                            title = "3. Nivel Puente/Cerebelo",
                            selectedRegions = selectedPcRegions,
                            onRegionToggled = onPcRegionToggled,
                            modifier = Modifier.weight(1f).padding(4.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PcSliceMonitor(
                            level = PcSliceLevel.THALAMI,
                            title = "1. Nivel Tálamos (T, OL)",
                            selectedRegions = selectedPcRegions,
                            onRegionToggled = onPcRegionToggled,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                        PcSliceMonitor(
                            level = PcSliceLevel.MIDBRAIN,
                            title = "2. Nivel Mesencéfalo (M)",
                            selectedRegions = selectedPcRegions,
                            onRegionToggled = onPcRegionToggled,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                        PcSliceMonitor(
                            level = PcSliceLevel.PONS_CEREBELLUM,
                            title = "3. Nivel Puente y Cerebelo (P, C)",
                            selectedRegions = selectedPcRegions,
                            onRegionToggled = onPcRegionToggled,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Collapsible Clinical Reference Guide Panel
        var showGuide by remember { mutableStateOf(false) }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF13131A)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E1E28), shape = RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGuide = !showGuide }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF1C1C26),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "📖",
                                    style = TextStyle(fontSize = 14.sp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Guía Anatómica de Referencia",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Identificación de áreas corticales y subcorticales",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF8C90A6)
                                )
                            )
                        }
                    }
                    Text(
                        text = if (showGuide) "OCULTAR ▲" else "MOSTRAR ▼",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (showGuide) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF232330)))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isPosterior) {
                        // Anterior (ACM) Details
                        Text(
                            text = "Arteria Cerebral Media (Circulación Anterior)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA5A8BA)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                ReferenceItem("C", "Caudado", "Estructura subcortical (1 pt)", DiagnosticColors.Caudate)
                                ReferenceItem("L", "Lenticular", "Sustancia gris nuclear (1 pt)", DiagnosticColors.Lentiform)
                                ReferenceItem("IC", "Cápsula Interna", "Brazo posterior (1 pt)", DiagnosticColors.InternalCapsule)
                                ReferenceItem("I", "Corteza Insular", "Banda insular lateral (1 pt)", DiagnosticColors.Insula)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ReferenceItem("M1", "ACM Anterior", "Corteza MCA anterior (1 pt)", DiagnosticColors.M1)
                                ReferenceItem("M2", "ACM Lateral", "Corteza MCA lateral (1 pt)", DiagnosticColors.M2)
                                ReferenceItem("M3", "ACM Posterior", "Corteza MCA posterior (1 pt)", DiagnosticColors.M3)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                ReferenceItem("M4", "ACM Anterior Sup.", "Corteza cortical superior (1 pt)", DiagnosticColors.M4)
                                ReferenceItem("M5", "ACM Lateral Sup.", "Corteza cortical lateral (1 pt)", DiagnosticColors.M5)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ReferenceItem("M6", "ACM Posterior Sup.", "Corteza cortical posterior (1 pt)", DiagnosticColors.M6)
                            }
                        }
                    } else {
                        // Posterior (pc-ASPECTS) Details
                        Text(
                            text = "Circulación Posterior (Fosa Posterior)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA5A8BA)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                ReferenceItem("T", "Tálamos", "Bilateral (1 pt por cada lado)", DiagnosticColors.Thalamus)
                                ReferenceItem("OL", "Lóbulos Occipitales", "Corteza visual (1 pt por cada lado)", DiagnosticColors.Occipital)
                                ReferenceItem("CE", "Cerebelo", "Coordinación motora (1 pt por cada lado)", DiagnosticColors.Cerebellum)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ReferenceItem("M", "Mesencéfalo", "Tronco encefálico superior (2 pts)", DiagnosticColors.Midbrain)
                                ReferenceItem("P", "Puente (Pons)", "Protuberancia medial (2 pts)", DiagnosticColors.Pons)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferenceItem(
    tag: String,
    name: String,
    description: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                .border(1.dp, color, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tag,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = Color(0xFF8C90A6)
                )
            )
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

    val infiniteTransition = rememberInfiniteTransition(label = "inf_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val isM1 = selectedRegions.contains("M1")
    val isM2 = selectedRegions.contains("M2")
    val isM3 = selectedRegions.contains("M3")
    val isM4 = selectedRegions.contains("M4")
    val isM5 = selectedRegions.contains("M5")
    val isM6 = selectedRegions.contains("M6")
    val isC = selectedRegions.contains("C")
    val isL = selectedRegions.contains("L")
    val isIc = selectedRegions.contains("IC")
    val isI = selectedRegions.contains("I")

    val m1Color by animateColorAsState(targetValue = if (isM1) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.M1.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "m1")
    val m2Color by animateColorAsState(targetValue = if (isM2) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.M2.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "m2")
    val m3Color by animateColorAsState(targetValue = if (isM3) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.M3.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "m3")
    val m4Color by animateColorAsState(targetValue = if (isM4) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.M4.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "m4")
    val m5Color by animateColorAsState(targetValue = if (isM5) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.M5.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "m5")
    val m6Color by animateColorAsState(targetValue = if (isM6) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.M6.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "m6")
    val cColor by animateColorAsState(targetValue = if (isC) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Caudate.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "c")
    val icColor by animateColorAsState(targetValue = if (isIc) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.InternalCapsule.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "ic")
    val lColor by animateColorAsState(targetValue = if (isL) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Lentiform.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "l")
    val iColor by animateColorAsState(targetValue = if (isI) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Insula.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "i")

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

                // Midline X is shifted slightly to the right so we draw the hemisphere on the left (patient's right hemisphere)
                val cx = w / 2f + rMax * 0.4f
                val cy = h / 2f

                // Draw medical crosshairs background
                drawMedicalGrid(w, h, cx, cy, rMax)

                // 1. Draw Skull boundary
                drawOval(
                    color = Color(0xFFE0E0EB).copy(alpha = 0.5f),
                    topLeft = Offset(cx - rMax * 1.05f, cy - rMax * 1.05f),
                    size = Size(rMax * 2.1f, rMax * 2.1f),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw space inside scalp
                drawOval(
                    color = Color(0xFF161622),
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

                    // M1 Arc (Anterior Cortex)
                    drawArc(
                        color = m1Color,
                        startAngle = 270f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M2 Arc (Lateral Cortex)
                    drawArc(
                        color = m2Color,
                        startAngle = 210f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M3 Arc (Posterior Cortex)
                    drawArc(
                        color = m3Color,
                        startAngle = 150f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // Cortex ribbon inner cutout (radius 0.55 R)
                    val rInner = rMax * 0.55f
                    drawArc(
                        color = Color(0xFF0F0F15),
                        startAngle = 270f,
                        sweepAngle = -180f,
                        useCenter = true,
                        topLeft = Offset(cx - rInner, cy - rInner),
                        size = Size(rInner * 2f, rInner * 2f)
                    )

                    // Insula (I) ribbon (outer lip of inner core)
                    drawArc(
                        color = iColor,
                        startAngle = 220f,
                        sweepAngle = -80f,
                        useCenter = true,
                        topLeft = Offset(cx - rInner, cy - rInner),
                        size = Size(rInner * 2f, rInner * 2f)
                    )

                    // Inner-inner core mask (radius 0.42 R)
                    val rCore = rMax * 0.42f
                    drawArc(
                        color = Color(0xFF0C0C12),
                        startAngle = 270f,
                        sweepAngle = -180f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore, cy - rCore),
                        size = Size(rCore * 2f, rCore * 2f)
                    )

                    // Draw caudate (C) anterior-medially
                    drawArc(
                        color = cColor,
                        startAngle = 270f,
                        sweepAngle = -50f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore * 1.0f, cy - rCore * 1.0f),
                        size = Size(rCore * 2f, rCore * 2f)
                    )

                    // Draw capsula interna (IC)
                    drawArc(
                        color = icColor,
                        startAngle = 220f,
                        sweepAngle = -45f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore * 0.9f, cy - rCore * 0.9f),
                        size = Size(rCore * 1.8f, rCore * 1.8f)
                    )

                    // Draw Lentiform nucleus (L)
                    drawArc(
                        color = lColor,
                        startAngle = 220f,
                        sweepAngle = -75f,
                        useCenter = true,
                        topLeft = Offset(cx - rCore * 0.72f, cy - rCore * 0.72f),
                        size = Size(rCore * 1.44f, rCore * 1.44f)
                    )

                    // Ventricle schematic line
                    drawVentricles(cx, cy, rCore)

                    // Sector outlines to give sharp clinical division
                    drawSectorOutlinesGanglionic(cx, cy, rMax, rInner, rCore)

                    // Highlight Ischemic borders
                    drawIschemicAlertsGanglionic(cx, cy, rMax, rInner, rCore, selectedRegions)

                    // Draw labels
                    drawTextLabelsGanglionic(cx, cy, rMax, rInner, rCore, textMeasurer, selectedRegions)

                } else {
                    // --- SUPRAGANGLIONIC LEVEL ---

                    // M4 Arc
                    drawArc(
                        color = m4Color,
                        startAngle = 270f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M5 Arc
                    drawArc(
                        color = m5Color,
                        startAngle = 210f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // M6 Arc
                    drawArc(
                        color = m6Color,
                        startAngle = 150f,
                        sweepAngle = -60f,
                        useCenter = true,
                        topLeft = Offset(cx - rMax, cy - rMax),
                        size = Size(rMax * 2f, rMax * 2f)
                    )

                    // Inner brain background
                    val rInner = rMax * 0.55f
                    drawOval(
                        color = Color(0xFF14141E),
                        topLeft = Offset(cx - rInner, cy - rInner),
                        size = Size(rInner * 2f, rInner * 2f)
                    )

                    // Center ventricles
                    drawHighVentricles(cx, cy, rInner)

                    // Sector outlines
                    drawSectorOutlinesSupraganglionic(cx, cy, rMax, rInner)

                    // Highlight Ischemic borders
                    drawIschemicAlertsSupraganglionic(cx, cy, rMax, rInner, selectedRegions)

                    // Text labels
                    drawTextLabelsSupraganglionic(cx, cy, rMax, rInner, textMeasurer, selectedRegions)
                }

                // Reference anatomical indicators
                drawAnatomicalIndicators(cx, cy, rMax, textMeasurer)
            }
        }
    }
}

@Composable
fun PcSliceMonitor(
    level: PcSliceLevel,
    title: String,
    selectedRegions: Set<String>,
    onRegionToggled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "inf_glow_pc")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_pc"
    )

    val isTL = selectedRegions.contains("T_L")
    val isTR = selectedRegions.contains("T_R")
    val isOLL = selectedRegions.contains("OL_L")
    val isOLR = selectedRegions.contains("OL_R")
    val isM = selectedRegions.contains("M")
    val isP = selectedRegions.contains("P")
    val isCL = selectedRegions.contains("C_L")
    val isCR = selectedRegions.contains("C_R")

    val tlColor by animateColorAsState(targetValue = if (isTL) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Thalamus.copy(alpha = 0.3f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "tl")
    val trColor by animateColorAsState(targetValue = if (isTR) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Thalamus.copy(alpha = 0.3f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "tr")
    val ollColor by animateColorAsState(targetValue = if (isOLL) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Occipital.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "oll")
    val olrColor by animateColorAsState(targetValue = if (isOLR) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Occipital.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "olr")
    val mColor by animateColorAsState(targetValue = if (isM) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Midbrain.copy(alpha = 0.3f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "m")
    val pColor by animateColorAsState(targetValue = if (isP) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Pons.copy(alpha = 0.3f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "p")
    val clColor by animateColorAsState(targetValue = if (isCL) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Cerebellum.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "cl")
    val crColor by animateColorAsState(targetValue = if (isCR) Color(0xFFE53935).copy(alpha = pulseAlpha) else DiagnosticColors.Cerebellum.copy(alpha = 0.25f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "cr")

    Column(
        modifier = modifier
            .background(Color(0xFF13131A), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E1E28), shape = RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5A8BA)
            ),
            modifier = Modifier.padding(bottom = 6.dp)
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
                            val regionId = getPcRegionAtOffset(offset, width, height, level)
                            if (regionId != null) {
                                onRegionToggled(regionId)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val rMax = minOf(w - 50f, h - 30f) / 2f

                // Central Brain symmetry coordinates
                val cx = w / 2f
                val cy = h / 2f

                // Draw medical crosshairs in background
                drawMedicalGrid(w, h, cx, cy, rMax)

                // Skull boundary (Symmetric background)
                drawOval(
                    color = Color(0xFFDCDCE5).copy(alpha = 0.4f),
                    topLeft = Offset(cx - rMax * 1.05f, cy - rMax * 1.05f),
                    size = Size(rMax * 2.1f, rMax * 2.1f),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // CSF space inside scalp
                drawOval(
                    color = Color(0xFF181826),
                    topLeft = Offset(cx - rMax * 1.02f, cy - rMax * 1.02f),
                    size = Size(rMax * 2.04f, rMax * 2.04f)
                )

                // Midline dividing dashed line
                drawLine(
                    color = Color(0xFF323B54),
                    start = Offset(cx, cy - rMax * 1.05f),
                    end = Offset(cx, cy + rMax * 1.05f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                    strokeWidth = 1.dp.toPx()
                )

                when (level) {
                    PcSliceLevel.THALAMI -> {
                        // --- LEVEL 1: THALAMI & OCCIPITAL LOBE ---

                        // Occipital lobe wedges (Bilateral Posterior)
                        drawArc(
                            color = olrColor,
                            startAngle = 90f,
                            sweepAngle = -90f,
                            useCenter = true,
                            topLeft = Offset(cx - rMax, cy - rMax),
                            size = Size(rMax * 2f, rMax * 2f)
                        )
                        drawArc(
                            color = ollColor,
                            startAngle = 90f,
                            sweepAngle = 90f,
                            useCenter = true,
                            topLeft = Offset(cx - rMax, cy - rMax),
                            size = Size(rMax * 2f, rMax * 2f)
                        )

                        // Inner mask to hollow cortex
                        val rInner = rMax * 0.55f
                        drawOval(
                            color = Color(0xFF101016),
                            topLeft = Offset(cx - rInner, cy - rInner),
                            size = Size(rInner * 2f, rInner * 2f)
                        )

                        // Thalami (Bilateral ovals near center-posterior)
                        val thWidth = rMax * 0.25f
                        val thHeight = rMax * 0.42f
                        
                        // Right Thalamus (T_R) - patient right is on screen left (dx < 0)
                        drawOval(
                            color = trColor,
                            topLeft = Offset(cx - rMax * 0.32f, cy - thHeight / 2f),
                            size = Size(thWidth, thHeight)
                        )
                        // Left Thalamus (T_L) - patient left is on screen right (dx > 0)
                        drawOval(
                            color = tlColor,
                            topLeft = Offset(cx + rMax * 0.07f, cy - thHeight / 2f),
                            size = Size(thWidth, thHeight)
                        )

                        // Draw borders
                        drawOval(
                            color = if (isTR) Color(0xFFF1416C) else Color(0xFF4A4D5E),
                            topLeft = Offset(cx - rMax * 0.32f, cy - thHeight / 2f),
                            size = Size(thWidth, thHeight),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        drawOval(
                            color = if (isTL) Color(0xFFF1416C) else Color(0xFF4A4D5E),
                            topLeft = Offset(cx + rMax * 0.07f, cy - thHeight / 2f),
                            size = Size(thWidth, thHeight),
                            style = Stroke(width = 1.2.dp.toPx())
                        )

                        // Outer sector outlines
                        drawArc(
                            color = Color(0xFF3A3A4C),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(cx - rMax, cy - rMax),
                            size = Size(rMax * 2f, rMax * 2f),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Draw Text
                        val rMid = (rMax + rInner) / 2f
                        drawTextCentroid("OL_D", cx, cy, rMid, 135f, textMeasurer, isOLR)
                        drawTextCentroid("OL_I", cx, cy, rMid, 45f, textMeasurer, isOLL)
                        
                        drawTextCentroid("T_D", cx - rMax * 0.2f, cy, 0f, 0f, textMeasurer, isTR)
                        drawTextCentroid("T_I", cx + rMax * 0.2f, cy, 0f, 0f, textMeasurer, isTL)
                    }

                    PcSliceLevel.MIDBRAIN -> {
                        // --- LEVEL 2: MIDBRAIN ---

                        // Anterior cerebral peduncles (heart/butterfly center)
                        val mPath = Path().apply {
                            moveTo(cx, cy + rMax * 0.3f)
                            cubicTo(cx - rMax * 0.35f, cy + rMax * 0.15f, cx - rMax * 0.3f, cy - rMax * 0.2f, cx - rMax * 0.15f, cy - rMax * 0.3f)
                            quadraticTo(cx, cy - rMax * 0.12f, cx + rMax * 0.15f, cy - rMax * 0.3f)
                            cubicTo(cx + rMax * 0.3f, cy - rMax * 0.2f, cx + rMax * 0.35f, cy + rMax * 0.15f, cx, cy + rMax * 0.3f)
                            close()
                        }

                        // Fill midbrain
                        drawPath(
                            path = mPath,
                            color = mColor
                        )
                        drawPath(
                            path = mPath,
                            color = if (isM) Color(0xFFF1416C) else Color(0xFF5A5C70),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Draw Text
                        drawTextCentroid("M", cx, cy - rMax * 0.05f, 0f, 0f, textMeasurer, isM)
                    }

                    PcSliceLevel.PONS_CEREBELLUM -> {
                        // --- LEVEL 3: PONS & CEREBELLUM ---

                        // Bilateral massive ovals for cerebellar hemispheres (Posterior fossa)
                        drawArc(
                            color = crColor,
                            startAngle = 90f,
                            sweepAngle = -90f,
                            useCenter = true,
                            topLeft = Offset(cx - rMax, cy - rMax),
                            size = Size(rMax * 2f, rMax * 2f)
                        )
                        drawArc(
                            color = clColor,
                            startAngle = 90f,
                            sweepAngle = 90f,
                            useCenter = true,
                            topLeft = Offset(cx - rMax, cy - rMax),
                            size = Size(rMax * 2f, rMax * 2f)
                        )

                        // Anterior space separator
                        val rInner = rMax * 0.5f
                        drawOval(
                            color = Color(0xFF101016),
                            topLeft = Offset(cx - rInner, cy - rInner),
                            size = Size(rInner * 2f, rInner * 2f)
                        )

                        // Protuberancia (Pons) - bulky central anterior round structure
                        val ponsSize = rMax * 0.38f
                        drawCircle(
                            color = pColor,
                            radius = ponsSize,
                            center = Offset(cx, cy - rMax * 0.25f)
                        )
                        drawCircle(
                            color = if (isP) Color(0xFFF1416C) else Color(0xFF4C4F62),
                            radius = ponsSize,
                            center = Offset(cx, cy - rMax * 0.25f),
                            style = Stroke(width = 1.2.dp.toPx())
                        )

                        // Outline cerebellar halves
                        drawArc(
                            color = Color(0xFF333346),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(cx - rMax, cy - rMax),
                            size = Size(rMax * 2f, rMax * 2f),
                            style = Stroke(width = 1.2.dp.toPx())
                        )

                        // Draw Texts
                        val rMid = (rMax + rInner) / 2f
                        drawTextCentroid("CE_D", cx, cy, rMid, 135f, textMeasurer, isCR)
                        drawTextCentroid("CE_I", cx, cy, rMid, 45f, textMeasurer, isCL)
                        
                        drawTextCentroid("P", cx, cy - rMax * 0.25f, 0f, 0f, textMeasurer, isP)
                    }
                }

                // Reference anatomical indicators
                drawAnatomicalIndicators(cx, cy, rMax, textMeasurer)
            }
        }
    }
}

// Outlines
fun DrawScope.drawIschemicAlertsGanglionic(
    cx: Float, cy: Float, rMax: Float, rInner: Float, rCore: Float,
    selected: Set<String>
) {
    val alertColor = Color(0xFFF1416C)
    val alertWidth = 2.dp.toPx()

    if (selected.contains("M1")) {
        drawArc(
            color = alertColor, startAngle = 270f, sweepAngle = -60f, useCenter = true,
            topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
            style = Stroke(width = alertWidth)
        )
    }
    if (selected.contains("M2")) {
        drawArc(
            color = alertColor, startAngle = 210f, sweepAngle = -60f, useCenter = true,
            topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
            style = Stroke(width = alertWidth)
        )
    }
    if (selected.contains("M3")) {
        drawArc(
            color = alertColor, startAngle = 150f, sweepAngle = -60f, useCenter = true,
            topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
            style = Stroke(width = alertWidth)
        )
    }
}

fun DrawScope.drawIschemicAlertsSupraganglionic(
    cx: Float, cy: Float, rMax: Float, rInner: Float,
    selected: Set<String>
) {
    val alertColor = Color(0xFFF1416C)
    val alertWidth = 2.dp.toPx()

    if (selected.contains("M4")) {
        drawArc(
            color = alertColor, startAngle = 270f, sweepAngle = -60f, useCenter = true,
            topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
            style = Stroke(width = alertWidth)
        )
    }
    if (selected.contains("M5")) {
        drawArc(
            color = alertColor, startAngle = 210f, sweepAngle = -60f, useCenter = true,
            topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
            style = Stroke(width = alertWidth)
        )
    }
    if (selected.contains("M6")) {
        drawArc(
            color = alertColor, startAngle = 150f, sweepAngle = -60f, useCenter = true,
            topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
            style = Stroke(width = alertWidth)
        )
    }
}

fun DrawScope.drawMedicalGrid(w: Float, h: Float, cx: Float, cy: Float, rMax: Float) {
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
    drawLine(
        color = Color(0xFF151821),
        start = Offset(cx - rMax * 1.3f, cy),
        end = Offset(cx + rMax * 1.3f, cy),
        strokeWidth = 0.5.dp.toPx()
    )
}

fun DrawScope.drawVentricles(cx: Float, cy: Float, rCore: Float) {
    val path = Path().apply {
        moveTo(cx - 2f, cy - rCore * 0.6f)
        quadraticTo(cx - rCore * 0.15f, cy - rCore * 0.4f, cx - rCore * 0.25f, cy - rCore * 0.1f)
        quadraticTo(cx - rCore * 0.15f, cy - rCore * 0.18f, cx - 2f, cy - rCore * 0.22f)
        close()
        moveTo(cx - 2f, cy + rCore * 0.1f)
        quadraticTo(cx - rCore * 0.12f, cy + rCore * 0.25f, cx - rCore * 0.18f, cy + rCore * 0.65f)
        quadraticTo(cx - rCore * 0.08f, cy + rCore * 0.38f, cx - 2f, cy + rCore * 0.28f)
        close()
    }
    drawPath(path = path, color = Color(0xFF00B0FF).copy(alpha = 0.15f))
    drawPath(path = path, color = Color(0xFF00E5FF).copy(alpha = 0.4f), style = Stroke(width = 1.dp.toPx()))
}

fun DrawScope.drawHighVentricles(cx: Float, cy: Float, rInner: Float) {
    val path = Path().apply {
        moveTo(cx - 5f, cy - rInner * 0.35f)
        quadraticTo(cx - rInner * 0.1f, cy - rInner * 0.1f, cx - 5f, cy + rInner * 0.2f)
    }
    drawPath(path = path, color = Color(0xFF00E5FF).copy(alpha = 0.3f), style = Stroke(width = 1.5.dp.toPx()))
}

fun DrawScope.drawSectorOutlinesGanglionic(cx: Float, cy: Float, rMax: Float, rInner: Float, rCore: Float) {
    val outlineColor = Color(0xFF333344)
    val strokeWidth = 1.dp.toPx()

    drawArc(
        color = outlineColor, startAngle = 270f, sweepAngle = -180f, useCenter = false,
        topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
        style = Stroke(width = strokeWidth)
    )
    drawArc(
        color = outlineColor, startAngle = 270f, sweepAngle = -180f, useCenter = false,
        topLeft = Offset(cx - rInner, cy - rInner), size = Size(rInner * 2f, rInner * 2f),
        style = Stroke(width = strokeWidth)
    )
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

    drawArc(
        color = outlineColor, startAngle = 270f, sweepAngle = -180f, useCenter = false,
        topLeft = Offset(cx - rCore, cy - rCore), size = Size(rCore * 2f, rCore * 2f),
        style = Stroke(width = strokeWidth)
    )
}

fun DrawScope.drawSectorOutlinesSupraganglionic(cx: Float, cy: Float, rMax: Float, rInner: Float) {
    val outlineColor = Color(0xFF333344)
    val strokeWidth = 1.2.dp.toPx()

    drawArc(
        color = outlineColor, startAngle = 270f, sweepAngle = -180f, useCenter = false,
        topLeft = Offset(cx - rMax, cy - rMax), size = Size(rMax * 2f, rMax * 2f),
        style = Stroke(width = strokeWidth)
    )
    drawArc(
        color = outlineColor, startAngle = 270f, sweepAngle = -180f, useCenter = false,
        topLeft = Offset(cx - rInner, cy - rInner), size = Size(rInner * 2f, rInner * 2f),
        style = Stroke(width = strokeWidth)
    )
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
    val rCortexMid = (rMax + rInner) / 2f
    val rDeepMid = (rInner + rCore) / 2f

    drawTextCentroid("M1", cx, cy, rCortexMid, 240f, textMeasurer, selectedRegions.contains("M1"))
    drawTextCentroid("M2", cx, cy, rCortexMid, 180f, textMeasurer, selectedRegions.contains("M2"))
    drawTextCentroid("M3", cx, cy, rCortexMid, 120f, textMeasurer, selectedRegions.contains("M3"))
    drawTextCentroid("I", cx, cy, rDeepMid, 180f, textMeasurer, selectedRegions.contains("I"))
    drawTextCentroid("C", cx, cy, rCore * 0.72f, 245f, textMeasurer, selectedRegions.contains("C"))
    drawTextCentroid("IC", cx, cy, rCore * 0.75f, 195f, textMeasurer, selectedRegions.contains("IC"))
    drawTextCentroid("L", cx, cy, rCore * 0.45f, 170f, textMeasurer, selectedRegions.contains("L"))
}

fun DrawScope.drawTextLabelsSupraganglionic(
    cx: Float, cy: Float, rMax: Float, rInner: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    selectedRegions: Set<String>
) {
    val rCortexMid = (rMax + rInner) / 2f
    drawTextCentroid("M4", cx, cy, rCortexMid, 240f, textMeasurer, selectedRegions.contains("M4"))
    drawTextCentroid("M5", cx, cy, rCortexMid, 180f, textMeasurer, selectedRegions.contains("M5"))
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

    val sizeA = textMeasurer.measure("A (ANT)", indicatorStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = "A (ANT)",
        topLeft = Offset(cx - sizeA.size.width / 2f, cy - rMax * 1.25f),
        style = indicatorStyle,
        size = Size(sizeA.size.width.toFloat(), sizeA.size.height.toFloat())
    )

    val sizeP = textMeasurer.measure("P (POST)", indicatorStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = "P (POST)",
        topLeft = Offset(cx - sizeP.size.width / 2f, cy + rMax * 1.15f),
        style = indicatorStyle,
        size = Size(sizeP.size.width.toFloat(), sizeP.size.height.toFloat())
    )

    val sizeL = textMeasurer.measure("L (LAT)", indicatorStyle)
    drawText(
        textMeasurer = textMeasurer,
        text = "L (LAT)",
        topLeft = Offset(cx - rMax * 1.30f, cy - sizeL.size.height / 2f),
        style = indicatorStyle,
        size = Size(sizeL.size.width.toFloat(), sizeL.size.height.toFloat())
    )
}

fun getRegionAtOffset(offset: Offset, w: Float, h: Float, level: SliceLevel): String? {
    val rMax = minOf(w - 60f, h - 40f) / 2f
    val cx = w / 2f + rMax * 0.4f
    val cy = h / 2f

    val dx = offset.x - cx
    val dy = offset.y - cy

    if (dx > 0) return null

    val dist = sqrt(dx * dx + dy * dy)
    val rNorm = dist / rMax

    if (rNorm > 1.15) return null

    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
    if (angle > 0) {
        angle = angle - 360.0
    }

    if (level == SliceLevel.GANGLIONIC) {
        if (rNorm > 0.55 && rNorm <= 1.15) {
            return when {
                angle in -150.0..-90.0 -> "M1"
                angle in -210.0..-150.0 -> "M2"
                angle in -270.0..-210.0 -> "M3"
                else -> null
            }
        } else if (rNorm <= 0.55) {
            if (rNorm > 0.42 && angle in -220.0..-140.0) {
                return "I"
            }
            return when {
                angle in -270.0..-220.0 -> "C"
                angle in -220.0..-175.0 && rNorm > 0.22 -> "IC"
                else -> "L"
            }
        }
    } else {
        return when {
            angle in -150.0..-90.0 -> "M4"
            angle in -210.0..-150.0 -> "M5"
            angle in -270.0..-210.0 -> "M6"
            else -> null
        }
    }
    return null
}

fun getPcRegionAtOffset(offset: Offset, w: Float, h: Float, level: PcSliceLevel): String? {
    val rMax = minOf(w - 50f, h - 30f) / 2f
    val cx = w / 2f
    val cy = h / 2f

    val dx = offset.x - cx
    val dy = offset.y - cy
    val dist = sqrt(dx * dx + dy * dy)
    val rNorm = dist / rMax

    if (rNorm > 1.15) return null

    when (level) {
        PcSliceLevel.THALAMI -> {
            if (rNorm <= 0.35) {
                return if (dx > 0) "T_L" else "T_R"
            }
            if (dy > 0 && rNorm > 0.35 && rNorm <= 1.0) {
                return if (dx > 0) "OL_L" else "OL_R"
            }
        }
        PcSliceLevel.MIDBRAIN -> {
            if (rNorm <= 0.4) {
                return "M"
            }
        }
        PcSliceLevel.PONS_CEREBELLUM -> {
            if (dy < 0 && rNorm <= 0.4) {
                return "P"
            }
            if (dy > 0 && rNorm > 0.3 && rNorm <= 1.0) {
                return if (dx > 0) "C_L" else "C_R"
            }
        }
    }
    return null
}
