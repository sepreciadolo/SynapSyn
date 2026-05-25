package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedCalculation

@Composable
fun TabInicio(
    onNavigateToTab: (Int) -> Unit,
    onNavigateToDrug: (String) -> Unit,
    savedCalculations: List<SavedCalculation>,
    onClearCalculations: () -> Unit,
    onCopyCalculation: (SavedCalculation) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. GREETING CARD / HERO BANNER (Bold visual styling with gradient atmosphere)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(24.dp)
                    .testTag("home_hero_banner")
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Servicio Médico",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "SYNAPPSE COMPENDIO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "Asistencia Clínica Bedside",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    Text(
                        text = "Calculadoras de escala, criterios diagnósticos rápidos de neurología, examen físico y compendio de neurofármacos avanzados.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }

        // 2. SHORTCUTS NAVIGATION GRID
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
                        description = "NIHSS, ALSFRS-R, QMG, DRAGON, Aspects, ICH, FOUR, EDSS.",
                        icon = Icons.Default.Calculate,
                        modifier = Modifier.weight(1f).testTag("shortcut_calc"),
                        onClick = { onNavigateToTab(1) } // Tab index 1 is now Calcular
                    )
                    HomeShortcutCard(
                        title = "Criterios",
                        description = "Certificar Gold Coast, EULAR, TOAST e ILAE.",
                        icon = Icons.Default.FactCheck,
                        modifier = Modifier.weight(1f).testTag("shortcut_crit"),
                        onClick = { onNavigateToTab(2) } // Tab index 2 is now Criterios
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeShortcutCard(
                        title = "Farmacología",
                        description = "Directorio clínico, dosis, interacciones y advertencias.",
                        icon = Icons.Default.Medication,
                        modifier = Modifier.weight(1f).testTag("shortcut_drugs"),
                        onClick = { onNavigateToTab(3) } // Tab index 3 is now Tratamientos
                    )
                    HomeShortcutCard(
                        title = "Exploración",
                        description = "Dermatomas, reflejos, mRS, FAST y clases de MGFA.",
                        icon = Icons.Default.AccessibilityNew,
                        modifier = Modifier.weight(1f).testTag("shortcut_quick"),
                        onClick = { onNavigateToTab(4) } // Tab index 4 is now Rápidas/Exploración
                    )
                }
            }
        }

        // 3. FEATURED DRUGS spotlight (Fármacos de alta búsqueda)
        item {
            Text(
                text = "Fármacos Destacados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            val spotlightDrugs = listOf(
                Pair("L-DOPA", "Levodopa / Carbidopa"),
                Pair("OCR", "Ocrelizumab"),
                Pair("RLZ", "Riluzol"),
                Pair("DPZ", "Donepezilo"),
                Pair("LCM_A", "Lecanemab"),
                Pair("FGM", "Fingolimod"),
                Pair("LEV", "Levetiracetam")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spotlightDrugs.forEach { (acronym, name) ->
                    AssistChip(
                        onClick = { onNavigateToDrug(name) },
                        label = { Text(text = "$acronym - $name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier
                            .testTag("spotlight_$acronym")
                            .minimumInteractiveComponentSize()
                    )
                }
            }
        }

        // 4. HISTORIAL DE CÁLCULO BED-SIDE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Turno (Últimas 12h)",
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
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (savedCalculations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryToggleOff,
                            contentDescription = "Historial Vacío",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Sin registros clínicos recientes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Las puntuaciones calculadas e historiales clínicos de las últimas 12 horas aparecerán aquí para copia rápida antes de subirlos a la historia clínica del paciente.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            items(savedCalculations) { calculation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_item_${calculation.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        calculation.scaleId.contains("criterios") -> Icons.Default.FactCheck
                                        calculation.scaleId.contains("farmaco") -> Icons.Default.Medication
                                        else -> Icons.Default.Calculate
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = calculation.scaleName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Resultado: ${calculation.scoreText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (calculation.interpretation.isNotEmpty()) {
                                Text(
                                    text = "Diagnóstico/Intervención: ${calculation.interpretation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { onCopyCalculation(calculation) },
                            modifier = Modifier.testTag("copy_history_btn_${calculation.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar registro",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
