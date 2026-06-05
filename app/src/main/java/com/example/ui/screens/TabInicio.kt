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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedCalculation
import com.example.data.UserFavorite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TabInicio(
    darkTheme: Boolean = false,
    onNavigateToTab: (Int) -> Unit,
    onNavigateToDrug: (String) -> Unit,
    savedCalculations: List<SavedCalculation>,
    onClearCalculations: () -> Unit,
    onCopyCalculation: (SavedCalculation) -> Unit,
    favorites: List<UserFavorite> = emptyList(),
    onToggleFavorite: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToCalculator: (String) -> Unit = {},
    onNavigateToCriterio: (String) -> Unit = {}
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
        AppManualDialog(
            onDismiss = { showManualDialog = false },
            onNavigateToTab = onNavigateToTab
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // 1. SYSTEM STATUS SUB-HEADER (COMPACT & OVERLAP-SAFE)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer { alpha = telemetryPulse }
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Text(
                            text = "SISTEMA ACTIVO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
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
                            colors = if (darkTheme) {
                                listOf(
                                    Color(0xFF261847), // Deep dynamic clinic violet-indigo
                                    Color(0xFF130A24)  // Midnight dark plum
                                )
                            } else {
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                                )
                            }
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
                            Column {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        ) {
                                            append("Syn")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Normal,
                                                color = Color(0xFF2DD4BF) // Bright teal/cyan for neon highlight
                                            )
                                        ) {
                                            append("App")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        ) {
                                            append("Se")
                                        }
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.sp
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
                        text = "Calculadoras de escala calibradas, protocolos clínicos interactivos, compendio farmacológico y guías bedside de alta fidelidad.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }



        // 3. SECCIONES PRINCIPALES
        item {
            Text(
                text = "Secciones de Trabajo Bedside",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeShortcutRowCard(
                    title = "Calculadoras de Escalas",
                    description = "Evaluación cuantitativa calibrada con interpretador inmediato integrado.",
                    accentColor = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Calculate,
                    tags = listOf("NIHSS", "FOUR", "ASPECTS", "ICH Volumétrica", "EDSS", "ALSFRS-R"),
                    modifier = Modifier.testTag("shortcut_calc"),
                    onClick = { onNavigateToTab(1) }
                )

                HomeShortcutRowCard(
                    title = "Protocolos & Consenso",
                    description = "Criterios diagnósticos, clasificaciones de ACV y guías de certificación.",
                    accentColor = Color(0xFF7C3AED), // Violet
                    icon = Icons.Default.FactCheck,
                    tags = listOf("Gold Coast", "EULAR/ACR", "TOAST", "ILAE 2014"),
                    modifier = Modifier.testTag("shortcut_crit"),
                    onClick = { onNavigateToTab(2) }
                )

                HomeShortcutRowCard(
                    title = "Farmacología Bedside",
                    description = "Calculadora de infusión, por peso, esquemas renales y prevención SNC.",
                    accentColor = Color(0xFF0F766E), // Teal
                    icon = Icons.Default.Medication,
                    tags = listOf("Dosis Críticas", "Fibrinolíticos", "Cockcroft-Gault", "Ajuste BHE"),
                    modifier = Modifier.testTag("shortcut_drugs"),
                    onClick = { onNavigateToTab(3) }
                )

                HomeShortcutRowCard(
                    title = "Exploración & Guías UCI",
                    description = "Pruebas segmentarias de sensibilidad, reflejos y guías neurocríticas rápidas.",
                    accentColor = Color(0xFFD97706), // Amber
                    icon = Icons.Default.AccessibilityNew,
                    tags = listOf("Dermatomas", "Reflejos ROT", "mRS", "FAST", "Meningitis", "Drenaje LCR"),
                    modifier = Modifier.testTag("shortcut_quick"),
                    onClick = { onNavigateToTab(4) }
                )
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
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
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
                                    .graphicsLayer { alpha = criticalGuidePulse } // Smooth animation on the drawing layer -> 0% recompositions
                                    .size(6.dp)
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
                            text = "Acceso inmediato a Código ACV, Muerte Encefálica legal, Meningitis, HINTS+ y Vasoactivos.",
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

        // 5.5 MIS FAVORITOS BEDSIDE
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favoritos Bedside",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (favorites.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_favorites_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Acceso Rápido Personalizado",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Pulsa el ícono de estrella (⭐) en la parte superior derecha de cualquier calculadora o protocolo para tener acceso inmediato desde aquí.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        } else {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    favorites.forEach { fav ->
                        val themeColor = if (fav.type == "calculator") MaterialTheme.colorScheme.primary else Color(0xFF7C3AED)
                        val iconVector = if (fav.type == "calculator") Icons.Default.Calculate else Icons.Default.FactCheck
                        
                        Card(
                            onClick = {
                                if (fav.type == "calculator") {
                                    onNavigateToCalculator(fav.featureId)
                                } else {
                                    onNavigateToCriterio(fav.featureId)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("favorite_item_${fav.featureId}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = themeColor.copy(alpha = 0.1f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = themeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fav.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (fav.type == "calculator") "Calculadora Médica" else "Protocolo y Criterios",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { onToggleFavorite(fav.featureId, fav.name, fav.type) },
                                    modifier = Modifier.size(32.dp).testTag("delete_favorite_${fav.featureId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Quitar favorito",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
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
                    calculation.scaleId.contains("criterios") || calculation.scaleId.contains("protocolos") -> Color(0xFF7C3AED) // Violet
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
                                            calculation.scaleId.contains("criterios") || calculation.scaleId.contains("protocolos") -> Icons.Default.FactCheck
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
fun HomeShortcutRowCard(
    title: String,
    description: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tags: List<String>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentColor.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
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

data class ManualItemData(
    val name: String,
    val description: String,
    val targetTab: Int,
    val tag: String
)

data class ManualCategoryData(
    val title: String,
    val items: List<ManualItemData>
)

val manualData = listOf(
    ManualCategoryData(
        title = "🔬 1. CALCULADORAS CLÍNICAS",
        items = listOf(
            ManualItemData("NIHSS", "Puntuación de déficit de 0 a 42 puntos en eventos coronarios/ACV.", 1, "Cálculo"),
            ManualItemData("FOUR Score", "Alternativa avanzada a GCS: ocular, motor, tallo y patrón respiratorio.", 1, "Cálculo"),
            ManualItemData("ASPECTS / PC-ASPECTS", "Mapeo topológico interactivo en TAC simple para arteria cerebral anterior y posterior.", 1, "Cálculo"),
            ManualItemData("ICH Score / Volumetría", "Hemorragia intracerebral con cálculo (AxBxC)/2 para estimación de masa hemática.", 1, "Cálculo"),
            ManualItemData("ALSFRS-R", "Escala calificada de Amyotrophic Lateral Sclerosis para progresión funcional.", 1, "Cálculo"),
            ManualItemData("QMG", "Quantitative Myasthenia Gravis Score para fatiga muscular.", 1, "Cálculo"),
            ManualItemData("Kurtzke EDSS", "Escala del estado de discapacidad de Esclerosis Múltiple mapeada por sistemas.", 1, "Cálculo")
        )
    ),
    ManualCategoryData(
        title = "📋 2. PROTOCOLOS & CRITERIOS DE CONSENSO",
        items = listOf(
            ManualItemData("Criterios Gold Coast", "Criterios simplificados para la certificación de Esclerosis Lateral Amiotrófica (ELA).", 2, "Protocolo"),
            ManualItemData("Consenso EULAR/ACR", "Criterios de validación integrados para Miastenia Gravis.", 2, "Protocolo"),
            ManualItemData("Etiología & Fenotipos (TOAST & ASCOD)", "Criterios etiológicos TOAST, fenotipificación interactiva ASCOD y escala de placa carotídea Plaque-RADS.", 4, "Exploración"),
            ManualItemData("Criterios ILAE 2014", "Algoritmo de clasificación clínica de crisis epilépticas y epilepsia.", 2, "Protocolo")
        )
    ),
    ManualCategoryData(
        title = "💊 3. FARMACOLOGÍA & DOSIS BEDSIDE",
        items = listOf(
            ManualItemData("Dosificación Crítica", "Directorio inmediato de inotrópicos, anticomiciales, trombolíticos por peso y sedación.", 3, "Fármaco"),
            ManualItemData("Depuración Renal", "Calculadora Cockcroft-Gault para tasas de filtrado clínico.", 3, "Fármaco"),
            ManualItemData("Ajuste de Barrera BHE", "Dosificación modificatoria para medicamentos de SNC para prevenir neurotoxicidad.", 3, "Fármaco")
        )
    ),
    ManualCategoryData(
        title = "🧭 4. EXPLORACIÓN RÁPIDA BEDSIDE",
        items = listOf(
            ManualItemData("Dermatomas Interactivos", "Mapeo sensitivo táctil del paciente por niveles segmentarios corporales.", 4, "Exploración"),
            ManualItemData("Reflejos ROT", "Guía segmentaria de exploración de reflejos osteotendinosos y su gradación.", 4, "Exploración"),
            ManualItemData("Escalas de Screening", "Puntuaciones funcionales mRS (Rankin), escala FAST en urgencias y clasificación MGFA.", 4, "Exploración")
        )
    ),
    ManualCategoryData(
        title = "🦠 5. NEUROINFECTOLOGÍA UCI",
        items = listOf(
            ManualItemData("Triage Meningoencefalitis", "Diagnóstico interactivo de patógenos comunes y perfil citoquímico del LCR.", 4, "Neuroinfectología"),
            ManualItemData("Índices de Diagnóstico", "Thwaites, Marais, BMS pediátrico y BM-CASCO en adultos para meningitis.", 4, "Neuroinfectología"),
            ManualItemData("Presión de Apertura LCR", "Monitoreo seguro ante hipertensión endocraneana y punción de descarga.", 4, "Neuroinfectología"),
            ManualItemData("Barrera BHE (QC-Alb/IgG)", "Cociente de Albúmina e Índice de IgG para síntesis intratecal activa.", 4, "Neuroinfectología")
        )
    )
)

@Composable
fun AppManualDialog(
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            manualData
        } else {
            manualData.map { category ->
                category.copy(
                    items = category.items.filter { item ->
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.description.contains(searchQuery, ignoreCase = true) ||
                        item.tag.contains(searchQuery, ignoreCase = true)
                    }
                )
            }.filter { it.items.isNotEmpty() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().testTag("close_manual_button")
            ) {
                Text("Cerrar Manual")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Capacidades Clínicas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Buscador general de herramientas y guías",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("manual_search_field"),
                    placeholder = { Text("Buscar capacidad clínica...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                ) {
                    if (filteredCategories.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Sin resultados para \"$searchQuery\"",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Intente otra palabra clave o explore las categorías.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            filteredCategories.forEach { category ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    category.items.forEach { item ->
                                        InteractiveManualItemRow(
                                            item = item,
                                            onClick = {
                                                onNavigateToTab(item.targetTab)
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
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
                                    text = "Calibrado de acuerdo con consensos médicos de la AHA, AAN y guías vigentes.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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
            .padding(20.dp)
            .testTag("app_manual_dialog")
    )
}

@Composable
fun InteractiveManualItemRow(
    item: ManualItemData,
    onClick: () -> Unit
) {
    val accentColor = when (item.targetTab) {
        1 -> MaterialTheme.colorScheme.primary
        2 -> Color(0xFF7C3AED)
        3 -> Color(0xFF0F766E)
        else -> Color(0xFFD97706)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manual_item_launch_${item.name.lowercase().filter { it.isLetter() }}"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.1f),
                            border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = item.tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.08f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Lanzar",
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
