package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedCalculation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TabInicio(
    onNavigateToTab: (Int) -> Unit,
    onNavigateToDrug: (String) -> Unit,
    savedCalculations: List<SavedCalculation>,
    onClearCalculations: () -> Unit,
    onCopyCalculation: (SavedCalculation) -> Unit
) {
    var showManualDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "workstation_telemetry")
    val telemetryPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_telemetry"
    )

    val criticalGuidePulse by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = "critical_pulse"
    )

    if (showManualDialog) {
        AppManualDialog(onDismiss = { showManualDialog = false })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // 1. WORKSTATION SUB-HEADER STATUS BAR (TEAL ALIVE MONITOR AND STATE PILLS)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Digital active heartbeat status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer(alpha = telemetryPulse)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary) // Mint green active dot
                    )
                    Text(
                        text = "ESTACIÓN BEDSIDE CRÍTICA • ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    )
                }

                // Data Count pill
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${savedCalculations.size} Registros",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. HERO BANNER: CLINICAL WORKSTATION STATS & REFINEMENT
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                            )
                        )
                    )
                    .testTag("home_hero_banner")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = "Servicio Médico",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "SYNAPPSE PLATFORM",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 2.sp,
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Text(
                                    text = "Asistente Neurológico Bedside",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        FilledTonalIconButton(
                            onClick = { showManualDialog = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(38.dp).testTag("button_open_manual_hero")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Ver manual de capacidades",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "Calculadoras de escala calibradas, criterios diagnósticos interactivos, compendio farmacológico y guías bedside de alta fidelidad.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 15.sp
                        )
                    )

                    // Counters Grid of clinical assets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            Triple("8", "Cálculos", Icons.Default.Calculate),
                            Triple("7", "Criterios", Icons.Default.FactCheck),
                            Triple("23", "Fármacos", Icons.Default.Medication),
                            Triple("7", "Guías UCI", Icons.Default.Bolt)
                        ).forEach { (count, label, icon) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "$count $label",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2.5. INTERACTIVE MANUAL CTA CARD FOR GUEST/ONBOARDING
        item {
            Card(
                onClick = { showManualDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shortcut_manual_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f)
                ),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Manual de la Aplicación",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Manual de Capacidades Clínicas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Presiona aquí para explorar el compendio de herramientas, criterios indexados, guías clínicas e índices avanzados para cuidados bedside.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Abrir Manual",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. SECCIONES PRINCIPALES SECTION HEADER & SHORTCUTS GRID
        item {
            Text(
                text = "Secciones Principales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeShortcutCard(
                        title = "Calculadoras",
                        description = "NIHSS, ALSFRS-R, QMG, ASPECTS, ICH, FOUR, EDSS.",
                        accentColor = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.Calculate,
                        modifier = Modifier.weight(1f).testTag("shortcut_calc"),
                        onClick = { onNavigateToTab(1) }
                    )
                    HomeShortcutCard(
                        title = "Criterios",
                        description = "Certificar Gold Coast, EULAR, TOAST e ILAE.",
                        accentColor = Color(0xFF7C3AED), // Violet
                        icon = Icons.Default.FactCheck,
                        modifier = Modifier.weight(1f).testTag("shortcut_crit"),
                        onClick = { onNavigateToTab(2) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeShortcutCard(
                        title = "Farmacología",
                        description = "Directorio de dosis, interacciones y toxicidad.",
                        accentColor = Color(0xFF0F766E), // Teal
                        icon = Icons.Default.Medication,
                        modifier = Modifier.weight(1f).testTag("shortcut_drugs"),
                        onClick = { onNavigateToTab(3) }
                    )
                    HomeShortcutCard(
                        title = "Exploración",
                        description = "Dermatomas, reflejos, mRS, FAST y MGFA.",
                        accentColor = Color(0xFFD97706), // Amber
                        icon = Icons.Default.AccessibilityNew,
                        modifier = Modifier.weight(1f).testTag("shortcut_quick"),
                        onClick = { onNavigateToTab(4) }
                    )
                }
            }
        }

        // 4. POWER SHORTCUT: PROTOCOLOS & GUÍAS CLÍNICAS BEDSIDE
        item {
            Card(
                onClick = { onNavigateToTab(4) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shortcut_protocols_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = criticalGuidePulse * 0.4f)
                ),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error.copy(alpha = criticalGuidePulse + 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Guías Clínicas",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PROTOCOLOS CLÍNICOS CRÍTICOS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                            Text(
                                text = "Nivel UCI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = "Guías de Especialidad Bedside",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Acesso inmediato a Código ACV, Muerte Encefálica legal, Meningitis, HINTS+ y Vasoactivos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver guías",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 5. SPOTLIGHT DRUGS CONVERTED TO BEAUTIFUL CLINICAL PRESCRIPTION CARDS
        item {
            Text(
                text = "Fármacos de Alta Búsqueda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            val spotlightDrugs = listOf(
                Triple("L-DOPA", "Levodopa / Carbidopa", Color(0xFF7C3AED)), // Violet
                Triple("OCR", "Ocrelizumab", Color(0xFF2563EB)), // Blue
                Triple("TNK", "Tenecteplasa", Color(0xFFDC2626)), // Red
                Triple("LCM", "Lecanemab", Color(0xFFD97706)), // Amber
                Triple("FGM", "Fingolimod", Color(0xFF0D9488)), // Teal
                Triple("LEV", "Levetiracetam", Color(0xFF3B82F6)) // Light Blue
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                spotlightDrugs.forEach { (acronym, name, themeColor) ->
                    Card(
                        onClick = { onNavigateToDrug(name) },
                        modifier = Modifier
                            .width(150.dp)
                            .testTag("spotlight_$acronym"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Circular tag box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = themeColor.copy(alpha = 0.12f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Medication,
                                            contentDescription = null,
                                            tint = themeColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = acronym,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Category pill inside drug
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = when (acronym) {
                                        "L-DOPA" -> "Parkinson"
                                        "OCR" -> "Biológico"
                                        "TNK" -> "Fibrinolítico"
                                        "LCM" -> "Alzheimer"
                                        "FGM" -> "S1P Modulador"
                                        else -> "Antiepiléptico"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. HISTORIAL DE CÁLCULO BED-SIDE
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial Clínico Reciente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (savedCalculations.isNotEmpty()) {
                    TextButton(
                        onClick = onClearCalculations,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("clear_history_btn_home")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Limpiar Todo", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        if (savedCalculations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HistoryToggleOff,
                                    contentDescription = "Historial Vacío",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Text(
                            text = "Sin registros clínicos recientes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Los cálculos de puntuaciones de escalas se guardarán de forma permanente en la base de datos cifrada local para copia rápida o consulta bedside.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        } else {
            items(savedCalculations) { calculation ->
                val calcThemeColor = when {
                    calculation.scaleId.contains("criterios") -> Color(0xFF7C3AED) // Violet
                    calculation.scaleId.contains("farmaco") -> Color(0xFF0F766E) // Teal
                    calculation.scaleId.contains("nihss") -> Color(0xFFDC2626) // Red
                    calculation.scaleId.contains("four") -> Color(0xFF2563EB) // Blue
                    else -> MaterialTheme.colorScheme.primary
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_item_${calculation.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Colored vertical status tab
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(46.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(calcThemeColor)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            calculation.scaleId.contains("criterios") -> Icons.Default.FactCheck
                                            calculation.scaleId.contains("farmaco") -> Icons.Default.Medication
                                            else -> Icons.Default.Calculate
                                        },
                                        contentDescription = null,
                                        tint = calcThemeColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = calculation.scaleName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                // Formatted human timestamp
                                Text(
                                    text = formatTimestamp(calculation.timestamp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Resultado: ${calculation.scoreText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = calcThemeColor
                            )
                            if (calculation.interpretation.isNotEmpty()) {
                                Text(
                                    text = calculation.interpretation,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { onCopyCalculation(calculation) },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("copy_history_btn_${calculation.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar cálculo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeShortcutCard(
    title: String,
    description: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(138.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(date)
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun AppManualDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().testTag("close_manual_button")
            ) {
                Text("Entendido • Volver al Trabajo")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(
                        text = "Manual de Operación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Capacidades de SynAppSe",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Bienvenido a SynAppSe. Esta guía interactiva le proporciona un compendio rápido de todas las capacidades clínicas disponibles en su estación de trabajo bedside.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                    
                    ManualFeatureSection(
                        categoryTitle = "🔬 1. CALCULADORAS CLÍNICAS",
                        features = listOf(
                            "NIHSS" to "Puntuación de 0 a 42 puntos para determinar el déficit del paciente en eventos coronarios/ACV.",
                            "ALSFRS-R (ELA)" to "Escala calificada de Amyotrophic Lateral Sclerosis para evaluar progresión funcional de la motoneurona.",
                            "QMG" to "Quantitative Myasthenia Gravis Score para calificar severidad de fatiga muscular.",
                            "ASPECTS & PC-ASPECTS" to "Mapeo topológico interactivo anterior y posterior en TAC simple para calcular extensión de infarto isquémico.",
                            "FOUR Score" to "Alternativa avanzada a GCS, califica respuesta ocular, motora, de tallo y patrón respiratorio en pacientes intubados.",
                            "Kurtzke EDSS" to "Escala del Estado de Discapacidad de Esclerosis Múltiple mapeada por sistemas funcionales.",
                            "ICH Score" to "Calcula mortalidad por hemorragia intracerebral de 0 a 6 puntos, con estimación volumétrica integrada (AxBxC) / 2."
                        )
                    )

                    ManualFeatureSection(
                        categoryTitle = "📋 2. CRITERIOS DIAGNÓSTICOS",
                        features = listOf(
                            "Guías Gold Coast" to "Censo interactivo para certificar Esclerosis Lateral Amiotrófica (ELA).",
                            "Consenso EULAR/ACR" to "Lista de validación combinada de criterios diagnósticos de Miastenia Gravis.",
                            "Clasificación TOAST" to "Mapeo ágil de los 5 subtipos etiológicos de ACV isquémico.",
                            "Criterios ILAE 2014" to "Algoritmo de clasificación clínica de crisis epilépticas y epilepsia."
                        )
                    )

                    ManualFeatureSection(
                        categoryTitle = "💊 3. FARMACORREGULACIÓN & DOSIS",
                        features = listOf(
                            "Dosificación Crítica" to "Compendio inmediato de Inotrópicos, Anticomiciales, Trombolíticos (Alteplasa/TNK por peso) y Sedoanalgesia.",
                            "Ajuste Renal & SNC" to "Calculadora de depuración (Cockcroft-Gault) con esquemas preventivos de dosis ajustadas al SNC para medicamentos que cruzan la BHE (Aciclovir, Ceftriaxona, Ampicilina, Meropenem y Cefepima) para prevenir fallos renales o neurotoxicidad (encefalopatía por cefepima)."
                        )
                    )

                    ManualFeatureSection(
                        categoryTitle = "🧭 4. EXPLORACIÓN RÁPIDA BEDSIDE",
                        features = listOf(
                            "Dermatomas Interactivos" to "Mapeo sensitivo táctil del cuerpo por niveles segmentarios.",
                            "Reflejos ROT" to "Guía de interpretación y escala clínica profunda de hiper/arreflexia (0 a 4+).",
                            "Escalas de Screening" to "Puntuaciones mRS (Rankin), escala FAST (ACV rápido) y clasificación higiénica MGFA."
                        )
                    )

                    ManualFeatureSection(
                        categoryTitle = "🦠 5. NEUROINFECTOLOGÍA AVANZADA",
                        features = listOf(
                            "Triage de Meningoencefalitis" to "Predicción interactiva de patógenos comunes, análisis de laboratorio/bioquímica del LCR.",
                            "Índices de Lab" to "Alineación de criterios diagnósticos de Thwaites y Marais (Meningitis Tuberculosa vs Bacteriana), BMS pediátrico y BM-CASCO en adultos.",
                            "Presión Cripto & Drenaje" to "Evaluador de presión de apertura y volumen óptimo de PL de goteo terapéutico.",
                            "Índices de Barrera BHE" to "Cálculo en paralelo de Cociente de Albúmina (Q-Alb) para probar permeabilidad de la Barrera Hematoencefálica e Índice de IgG para constatar la síntesis intratecal activa de anticuerpos (Esclerosis Múltiple, lúes o neuroinflamación crónica)."
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Validado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Calibrado de acuerdo con guías clínicas de la AHA/ASA, AAN y consensos internacionales vigentes (2026).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("app_manual_dialog")
    )
}

@Composable
fun ManualFeatureSection(categoryTitle: String, features: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = categoryTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                features.forEach { (name, desc) ->
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                    }
                    if (name != features.last().first) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
