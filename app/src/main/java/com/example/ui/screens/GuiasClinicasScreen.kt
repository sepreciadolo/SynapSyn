package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedCalculation

data class SectionNavItem(val id: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val emoji: String)
data class ProtocolRowItem(val id: String, val title: String, val desc: String)

@Composable
fun ProtocolDirectorySection(
    title: String,
    protocols: List<ProtocolRowItem>,
    onProtocolSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Seleccione una directiva clínica o pauta de manejo:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            protocols.forEach { p ->
                Card(
                    onClick = { onProtocolSelected(p.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("protocol_item_card_${p.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = p.desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver directiva",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrugBadge(drugName: String, onNavigateToDrug: (String) -> Unit) {
    AssistChip(
        onClick = { onNavigateToDrug(drugName) },
        label = { Text(drugName, fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
        leadingIcon = { Icon(Icons.Default.Medication, contentDescription = "Medicamento", modifier = Modifier.size(14.dp)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = Modifier.padding(2.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuideRelatedDrugs(
    drugs: List<String>,
    onNavigateToDrug: (String) -> Unit
) {
    if (drugs.isEmpty()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Farmacoterapia Relacionada (presiona p/ ver ficha):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                drugs.forEach { drug ->
                    DrugBadge(drugName = drug, onNavigateToDrug = onNavigateToDrug)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabGuiasYEscalas(
    selectedMrsGrade: Int?,
    onMrsGradeSelected: (Int?) -> Unit,
    selectedFastStage: Int?,
    onFastStageSelected: (Int?) -> Unit,
    selectedMgfaClass: Int?,
    onMgfaClassSelected: (Int?) -> Unit,
    recentHistoryContent: @Composable () -> Unit,
    onCopyClicked: (String, String, String) -> Unit,
    onNavigateToDrug: (String) -> Unit
) {
    var selectedSectionId by remember { mutableStateOf<String?>(null) } // null = dashboard
    var activeProtocolId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        if (selectedSectionId == null) {
            // Category Dashboard (Home Catalog screen for guidelines)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Banner inside Protocols
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROTOCOLOS CLÍNICOS BEDSIDE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Guías de Especialidad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Directivas clínicas actualizadas basadas en consensos y evidencia científica para soporte inmediato de decisiones médicas en el hospital.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }

                Text(
                    text = "Áreas Clínicas & Herramientas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
                )

                val categories = listOf(
                    Triple("urgencias", "Urgencias Críticas y de Tallo", Pair("ACV agudo, Muerte cerebral, Vértigo agudo (HINTS+), Estado epiléptico, Coma, Meningitis aguda", Icons.Default.LocalHospital)),
                    Triple("neuroinfecto", "Neuroinfectología Bedside", Pair("Triage de patógenos, Análisis de LCR interactiva y algoritmo de tratamiento empírico de meningitis", Icons.Default.Science)),
                    Triple("vascular", "Neurovascular Avanzado", Pair("Prevención secundaria, TIA minor stroke, HIC, HSA, TVC, Nomograma Heparina e inició DAPT", Icons.Default.Shield)),
                    Triple("inmunologia", "Neuroinmunología & Epilepsia", Pair("Criterios McDonald 2024, NMOSD, Encefalitis autoinmune, Seguridad DMTs, ASM y toxicidad", Icons.Default.AutoAwesome)),
                    Triple("cognicion", "Cognición y Movimiento", Pair("Biomarcadores AD 2024, Monoclonales Antiamiloide, Parkinson avanzado DBS, Toxina botulínica", Icons.Default.Psychology)),
                    Triple("escalas", "Escalas y Examen Rápido", Pair("Rankin mRS, FAST demencia, clases MGFA, reflejos osteotendinosos y dermatomas interactivos", Icons.Default.Layers)),
                    Triple("ordenes", "Generador de Órdenes y Notas", Pair("Estructura de órdenes rápidas de ingreso a urgencias y notas de evolución neuro-UCI", Icons.Default.Assignment))
                )

                categories.forEach { (id, title, meta) ->
                    val (desc, icon) = meta
                    val accentColor = when (id) {
                        "urgencias" -> Color(0xFFE11D48)
                        "neuroinfecto" -> Color(0xFF059669)
                        "vascular" -> Color(0xFF2563EB)
                        "inmunologia" -> Color(0xFF7C3AED)
                        "cognicion" -> Color(0xFFD97706)
                        "escalas" -> Color(0xFF0D9488)
                        else -> Color(0xFF4B5563)
                    }

                    Card(
                        onClick = {
                            selectedSectionId = id
                            activeProtocolId = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clinical_category_card_$id"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor.copy(alpha = 0.1f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Siguiente",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Selected category screen (drill-down view)
            Column(modifier = Modifier.fillMaxSize()) {
                // Header / Breadcrumb navigation row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, top = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (activeProtocolId != null) {
                                    activeProtocolId = null
                                } else {
                                    selectedSectionId = null
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guías",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "  ›  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = when (selectedSectionId) {
                                "urgencias" -> "Urgencias Críticas"
                                "vascular" -> "Neurovascular Avanzado"
                                "neuroinfecto" -> "Neuroinfectología Bedside"
                                "inmunologia" -> "Neuroinmuno & Epilepsia"
                                "cognicion" -> "Cognición, Movimiento & Más"
                                "escalas" -> "Escalas Bedside"
                                "ordenes" -> "Generador de Órdenes"
                                else -> "Detalle"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Main Page Content Pane (Dynamic Fullscreen)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (activeProtocolId != null) {
                        ProtocolDetailView(
                            protocolId = activeProtocolId!!,
                            onBack = { activeProtocolId = null },
                            onNavigateToDrug = onNavigateToDrug
                        )
                    } else {
                        when (selectedSectionId) {
                            "urgencias" -> ProtocolDirectorySection(
                                title = "Urgencias Críticas",
                                protocols = listOf(
                                    ProtocolRowItem("acv_2026", "Código ACV Completo AHA/ASA 2026", "Trombólisis IV, Tenecteplasa, Trombectomía, Reversión de Anticoagulados, LVO e Imágenes."),
                                    ProtocolRowItem("muerte_encefalica", "Muerte Encefálica y Donación", "Requisitos legales (Decreto 2493 de 2004), 7 reflejos de tallo, apnea, periodos de observación y mantenimiento del donante."),
                                    ProtocolRowItem("neuro_otologia_urgente", "Neuro-otología y Vértigo de Urgencias", "Acute Vertigo, HINTS/HINTS+ interactivo, descarte de ACV cerebeloso, disección vertebral e impotencia funcional."),
                                    ProtocolRowItem("estado_epileptico", "Estado Epiléptico: Algoritmo AES", "Líneas de tiempo por minutos, dosis exacta calculada por peso, contraindicaciones."),
                                    ProtocolRowItem("coma_encefalopatia", "Coma y Encefalopatía Aguda", "Evaluación de Glasgow, FOUR, reflejos pupilares, patrón respiratorio, panel metabólico."),
                                    ProtocolRowItem("meningitis_flujo", "Meningitis / Encefalitis Aguda", "Antibióticos inmediatos, Dexametasona, cuándo demorar PL y requerir Neuroimagen previa."),
                                    ProtocolRowItem("crisis_miastenica_sgb", "Crisis Miasténica & SGB Crítico", "Criterios de intubación en UCI (Regla 20/30/40), bulbar, disautonomía, contraindicaciones."),
                                    ProtocolRowItem("vasoactivos_infusiones", "Vasoactivos e Infusiones en Neuro-UCI", "Metas de Nicardipina, Labetalol, Clevidipina, Manitol y Salina Hipertónica 3% con límites de seguridad.")
                                ),
                                onProtocolSelected = { activeProtocolId = it }
                            )
                            "neuroinfecto" -> {
                                NeuroInfectologiaPane(
                                    onNavigateToDrug = onNavigateToDrug,
                                    onCopyClicked = onCopyClicked
                                )
                            }
                            "vascular" -> ProtocolDirectorySection(
                                title = "Neurovascular Avanzado",
                                protocols = listOf(
                                    ProtocolRowItem("calculadoras_vasculares", "🧮 Calculadoras Vasculares Interactivas", "Set avanzado de 16 herramientas bedside (mRS, Barthel, ABCD2, ABCD3-I, CHA2DS2, HAS-BLED, ABC/2, ICH Score, etc)."),
                                    ProtocolRowItem("prevencion_secundaria", "Prevención Secundaria Vascular", "Estudio etiológico mínimo/extendido, colesterol LDL <55, metas PA/DBT y FA."),
                                    ProtocolRowItem("tia_minor_stroke", "TIA / Minor Stroke (Ictus Menor)", "Manejo DAPT (POINT/CHANCE), puntaje ABCD², criterios de alto riesgo vascular."),
                                    ProtocolRowItem("hic_reversion", "Hemorragia Intracerebral Aguda", "Esquemas de reversión por fármaco, metas de PA, ICH scale, indicación quirúrgica."),
                                    ProtocolRowItem("hsa_manejo", "Hemorragia Subaracnoidea (HSA)", "Hunt-Hess, WFNS, Fisher Modificado, dosis de Nimodipino para vasoespasmo, monitoreo uci."),
                                    ProtocolRowItem("neurovasc_especiales", "Condiciones Especiales Vasculares", "Disección arterial, vasculitis primaria del SNC, Trombosis Venosa Cerebral (TVC), FOP, aneurismas."),
                                    ProtocolRowItem("dapt_anticoag_aguda", "Iniciación de DAPT & Heparina IV", "Carga/mantenimiento (Aspirina, Clopidogrel, Ticagrelor) and nomograma de Heparina IV con ajuste.")
                                ),
                                onProtocolSelected = { activeProtocolId = it }
                            )
                            "inmunologia" -> ProtocolDirectorySection(
                                title = "Neuroinmuno & Epilepsia",
                                protocols = listOf(
                                    ProtocolRowItem("mcdonald_2024", "Criterios McDonald 2024 (Esclerosis Múltiple)", "Óptico como quinta localización anatómica, unificación de formas, papel de bandas y biomarcadores."),
                                    ProtocolRowItem("nmosd_mogad", "NMOSD & MOGAD Diferenciales", "AQP4/MOG IgG, fenotipos típicos, banderas rojas contra EM, pautas de inducción."),
                                    ProtocolRowItem("encefalitis_autoinmune", "Encefalitis Autoinmune (Criterios Graus)", "Sospecha biológica, panel de anticuerpos en LCR/Sangre, pauta inmediata de esteroides/IG."),
                                    ProtocolRowItem("terapias_dmt_seguridad", "Seguridad de Terapias Modificadoras", "Tamizaje TB/VIH/Hepatitis, vacunación, PML por JCV, embarazo."),
                                    ProtocolRowItem("primera_crisis", "Primera Crisis Convulsiva no Provocada", "Riesgo de recurrencia, indicaciones para RM/EEG, pauta terapéutica inicial."),
                                    ProtocolRowItem("seleccion_asm", "Selección de Fármacos (ASM)", "Por sexo/embarazo, comorbilidad psíquica, disfunción biliar/renal, interacciones."),
                                    ProtocolRowItem("monitoreo_toxicidad", "Monitoreo de Fármacos & Toxicidad", "Rango terapéutico de Fenitoína/Valproato, riesgo HLA-B*1502/SJS con Carbamazepina."),
                                    ProtocolRowItem("refractariedad_sudep", "Epilepsia Farmacorresistente & SUDEP", "Criterios de refractariedad, mitigación de SUDEP, pre-surgical workup básico.")
                                ),
                                onProtocolSelected = { activeProtocolId = it }
                            )
                            "cognicion" -> ProtocolDirectorySection(
                                title = "Cognición, Movimiento & Más",
                                protocols = listOf(
                                    ProtocolRowItem("diagnostico_cognitivo", "MCI vs Demencia vs Delirium", "Diagnóstico diferencial, evaluación de depresión senil, abordaje sintomático."),
                                    ProtocolRowItem("biomarcadores_alzheimer", "Biomarcadores de Alzheimer (Criteria 2024)", "LCR/Plasma de p-tau181/217, PET amiloide/tau, interpretación biológica."),
                                    ProtocolRowItem("terapias_antiamiloide", "Monoclonales Antiamiloide (Lecanemab)", "Perfil de idoneidad, exclusiones de anticoagulación, monitoreo estricto de edema ARIA-E."),
                                    ProtocolRowItem("demencias_no_alzheimer", "Demencia no Alzheimer", "Deterioro frontotemporal variante conductual, demencia por cuerpos de Lewy (DLB)."),
                                    ProtocolRowItem("parkinson_avanzado", "Parkinson Avanzado & DBS", "Trastornos de impulsos, wearing-off, discinesias, Levodopa Challenge para DBS."),
                                    ProtocolRowItem("toxina_botulinica_avanzada", "Toxina Botulínica Avanzada", "Rangos y diluciones para distonía cervical, espasticidad, blefaroespasmo, migraña."),
                                    ProtocolRowItem("neuro_oftalmo_otologia", "Neuro-Oftalmología & Otología", "Diplopia, pérdida visual aguda, vértigo HINTS, papiledema.")
                                ),
                                onProtocolSelected = { activeProtocolId = it }
                            )
                            "escalas" -> {
                                LegacyBedsideScales(
                                    selectedMrsGrade = selectedMrsGrade,
                                    onMrsGradeSelected = onMrsGradeSelected,
                                    selectedFastStage = selectedFastStage,
                                    onFastStageSelected = onFastStageSelected,
                                    selectedMgfaClass = selectedMgfaClass,
                                    onMgfaClassSelected = onMgfaClassSelected,
                                    recentHistoryContent = recentHistoryContent,
                                    onCopyClicked = onCopyClicked
                                )
                            }
                            "ordenes" -> {
                                OrdersAndNotesGenerator()
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 1: PROTOCOL DIRECTORY (Optimized & Categorized)
// ==========================================
@Composable
fun ProtocolDirectory(onProtocolSelected: (String) -> Unit) {
    val sections = listOf(
        ProtocolCategory(
            title = "A. Vascular & Urgencias Críticas",
            color = Color(0xFFFDE8E8),
            tint = Color(0xFF9B1C1C),
            icon = Icons.Default.LocalHospital,
            protocols = listOf(
                ProtocolItem("acv_2026", "Código ACV Completo AHA/ASA 2026", "Trombólisis IV, Tenecteplasa, Trombectomía, Reversión de Anticoagulados, Sospecha LVO e Imágenes."),
                ProtocolItem("estado_epileptico", "Estado Epiléptico: Algoritmo AES", "Líneas de tiempo por minutos, dosis exacta calculada por peso, recordatorios de glucosa, tiamina, contraindicaciones."),
                ProtocolItem("coma_encefalopatia", "Coma y Encefalopatía Aguda", "Evaluación de Glasgow, FOUR, reflejos pupilares, patrón respiratorio, panel metabólico/infeccioso base."),
                ProtocolItem("meningitis_flujo", "Meningitis / Encefalitis Aguda", "Flujo Bedside: Antibióticos inmediatos, Dexametasona, cuándo demorar PL y requerir Neuroimagen previa."),
                ProtocolItem("crisis_miastenica_sgb", "Crisis Miasténica & SGB Crítico", "Criterios de intubación en UCI (Regla 20/30/40), bulbar, disautonomía, contraindicaciones de fármacos en MG."),
                ProtocolItem("vasoactivos_infusiones", "Vasoactivos e Infusiones en Neuro-UCI", "Metas de Nicardipina, Labetalol, Clevidipina, Manitol y Salina Hipertónica 3% con límites de seguridad.")
            )
        ),
        ProtocolCategory(
            title = "B. Neurovascular de Punta a Punta",
            color = Color(0xFFEBF5FF),
            tint = Color(0xFF1E40AF),
            icon = Icons.Default.Shield,
            protocols = listOf(
                ProtocolItem("prevencion_secundaria", "Prevención Secundaria Vascular", "Estudio etiológico mínimo y extendido, metas de colesterol LDL <55, metas de PA/DBT, manejo de FA."),
                ProtocolItem("tia_minor_stroke", "TIA / Minor Stroke (Ictus Menor)", "Manejo DAPT (POINT/CHANCE), puntaje ABCD², criterios de alto riesgo vascular y alta vs hospitalización."),
                ProtocolItem("hic_reversion", "Hemorragia Intracerebral Aguda", "Esquemas de reversión por fármaco, metas estrictas de PA, ICH scale ampliado, FUNC score, indicación quirúrgica."),
                ProtocolItem("hsa_manejo", "Hemorragia Subaracnoidea (HSA)", "Hunt-Hess, WFNS, Fisher Modificado, dosis de Nimodipino para vasoespasmo, monitoreo uci e hidrocefalia."),
                ProtocolItem("neurovasc_especiales", "Condiciones Especiales Vasculares", "Disección arterial, vasculitis primaria del SNC, Trombosis Venosa Cerebral (TVC), FOP, aneurismas no rotos."),
                ProtocolItem("dapt_anticoag_aguda", "Iniciación de DAPT & Heparina IV", "Carga/mantenimiento (Aspirina, Clopidogrel, Ticagrelor) y nomograma de Heparina IV con ajuste por TTPa.")
            )
        ),
        ProtocolCategory(
            title = "C. Neuroinmunología & Epilepsia de Consulta",
            color = Color(0xFFF3E8FF),
            tint = Color(0xFF6B21A8),
            icon = Icons.Default.AutoAwesome,
            protocols = listOf(
                ProtocolItem("mcdonald_2024", "Criterios McDonald 2024 (Esclerosis Múltiple)", "Óptico como quinta localización anatómica, unificación de formas, papel de bandas oligoclonales y biomarcadores."),
                ProtocolItem("nmosd_mogad", "NMOSD & MOGAD Diferenciales", "AQP4/MOG IgG, fenotipos típicos, banderas rojas contra EM, pautas de inducción y mantenimiento inmunológico."),
                ProtocolItem("encefalitis_autoinmune", "Encefalitis Autoinmune (Criterios Graus)", "Sospecha biológica, panel de anticuerpos en LCR/Sangre, pauta inmediata de esteroides/Inmunoglobulinas."),
                ProtocolItem("terapias_dmt_seguridad", "Seguridad de Terapias Modificadoras", "Tamizaje TB/VIH/Hepatitis, vacunación, estratificación de PML por JCV, embarazo, riesgo linfopenia."),
                ProtocolItem("primera_crisis", "Primera Crisis Convulsiva no Provocada", "Riesgo de recurrencia, indicaciones para RM/EEG, pauta terapéutica inicial y regulaciones cotidianas."),
                ProtocolItem("seleccion_asm", "Selección Inteligente de Fármacos (ASM)", "Por sexo/embarazo, comorbilidad psíquica, disfunción biliar/renal, migraña, interacciones medicamentosas."),
                ProtocolItem("monitoreo_toxicidad", "Monitoreo de Fármacos & Toxicidad", "Rango terapéutico de Fenitoína/Valproato, riesgo HLA-B*1502/SJS con Carbamazepina, hepatotoxicidad, hiponatremia."),
                ProtocolItem("refractariedad_sudep", "Epilepsia Farmacorresistente & SUDEP", "Criterios de refractariedad, mitigación de SUDEP, envío a unidad de cirugía, pre-surgical workup básico.")
            )
        ),
        ProtocolCategory(
            title = "D. Cognición & Movimiento Moderno",
            color = Color(0xFFE6FFFA),
            tint = Color(0xFF0D9488),
            icon = Icons.Default.Psychology,
            protocols = listOf(
                ProtocolItem("diagnostico_cognitivo", "MCI vs Demencia vs Delirium", "Diagnóstico diferencial bedside, evaluación de depresión senil, algoritmos de abordaje sintomático."),
                ProtocolItem("biomarcadores_alzheimer", "Biomarcadores de Alzheimer (Criteria 2024)", "LCR/Plasma de p-tau181/217, PET amiloide/tau, interpretación biológica del Alzheimer de vanguardia."),
                ProtocolItem("terapias_antiamiloide", "Monoclonales Antiamiloide (Lecanemab)", "Perfil de idoneidad, exclusiones de anticoagulación, monitoreo estricto de edema ARIA-E y hemorragia ARIA-H."),
                ProtocolItem("demencias_no_alzheimer", "Demencia no Alzheimer", "Deterioro frontotemporal variante conductual, demencia por cuerpos de Lewy (DLB), parálisis supranuclear progresiva."),
                ProtocolItem("parkinson_avanzado", "Parkinson Avanzado & Terapias DBS", "Trastornos de impulsos, wearing-off, discinesias, criterios del Levodopa Challenge para DBS, parkinsonismos."),
                ProtocolItem("toxina_botulinica_avanzada", "Toxina Botulínica Avanzada", "Rangos terapéutico y diluciones para distonía cervical, espasticidad, blefaroespasmo, sialorrea, migraña."),
                ProtocolItem("neuro_oftalmo_otologia", "Neuro-Oftalmología & Neuro-Otología", "Diplopia (III/IV/VI, pupil sparing), pérdida visual aguda (neuritis, ACG), vértigo HINTS/STANDING, papiledema/IIH.")
            )
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Vanguardia Neurológica 2026",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compendio exhaustivo de guías internacionales actualizadas a 2026. Selecciona cualquier protocolo abajo para visualizar el flujograma interactivo, dosis por peso y tablas de evidencia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        sections.forEach { category ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(category.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(category.icon, contentDescription = null, tint = category.tint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(category.protocols) { proto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProtocolSelected(proto.id) }
                        .testTag("proto_card_${proto.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = proto.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver guía",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = proto.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

data class ProtocolCategory(
    val title: String,
    val color: Color,
    val tint: Color,
    val icon: ImageVector,
    val protocols: List<ProtocolItem>
)

data class ProtocolItem(
    val id: String,
    val name: String,
    val summary: String
)

// ==========================================
// COMPONENT 2: PROTOCOL DETAIL VIEW
// ==========================================
data class TrialIndicator(
    val id: String,
    val title: String,
    val window: String,
    val summary: String,
    val mValue: String,
    val accentColor: Color,
    val barColor: Color,
    val icon: ImageVector
)

data class BpResultMetrics(
    val titleColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val alertText: String
)

@Composable
fun DossierCardItem(label: String, valText: String, icon: ImageVector, accentColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = valText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ProtocolDetailView(
    protocolId: String,
    onBack: () -> Unit,
    onNavigateToDrug: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var patientWeight by remember { mutableStateOf("") }

    // State definitions for Stroke interactive mode
    var selectedLkwHours by remember { mutableStateOf(1.5f) }
    var bpWorkingMode by remember { mutableStateOf("lysis_candidate") }
    var bpSys by remember { mutableStateOf("170") }
    var bpDia by remember { mutableStateOf("100") }
    var selectedTrialDossierId by remember { mutableStateOf<String?>(null) }
    
    // States for candidate study matcher list
    var mAge by remember { mutableStateOf("72") }
    var mNihss by remember { mutableStateOf("14") }
    var mLkw by remember { mutableStateOf("9.0") }
    var mCoreVol by remember { mutableStateOf("18") }
    var mMismatchVol by remember { mutableStateOf("80") }
    var mOcclusion by remember { mutableStateOf("ICA/M1") }

    // Render detailed content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable { onBack() }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Volver al Directorio de Guías", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        val relatedDrugs = remember(protocolId) {
            when (protocolId) {
                "acv_2026" -> listOf("Tenecteplasa (TNK-tPA)", "Alteplasa (rt-PA)", "Ácido Acetilsalicílico (Aspirina)", "Clopidogrel", "Ticagrelor")
                "estado_epileptico" -> listOf("Lorazepam", "Diazepam", "Midazolam", "Levetiracetam", "Fenobarbital", "Valproato / Ácido Valproico / Divalproato")
                "meningitis_flujo" -> listOf("Metilprednisolona (Pulsos)")
                "crisis_miastenica_sgb" -> listOf("Piridostigmina")
                "prevencion_secundaria" -> listOf("Ácido Acetilsalicílico (Aspirina)", "Clopidogrel", "Ticagrelor")
                "tia_minor_stroke" -> listOf("Ácido Acetilsalicílico (Aspirina)", "Clopidogrel")
                "dapt_anticoag_aguda" -> listOf("Ácido Acetilsalicílico (Aspirina)", "Clopidogrel", "Ticagrelor")
                "nmosd_mogad" -> listOf("Metilprednisolona (Pulsos)", "Satralizumab")
                "encefalitis_autoinmune" -> listOf("Metilprednisolona (Pulsos)")
                "terapias_dmt_seguridad" -> listOf("Interferón Beta-1a", "Teriflunomida", "Fingolimod", "Natalizumab", "Ocrelizumab")
                "primera_crisis", "seleccion_asm", "monitoreo_toxicidad" -> listOf("Carbamazepina", "Oxcarbazepina", "Eslicarbazepina acetato", "Fenitoína", "Fosfenitoína", "Lamotrigina", "Lacosamida", "Rufinamida", "Cenobamato", "Topiramato", "Valproato / Ácido Valproico / Divalproato", "Zonisamida", "Levetiracetam", "Brivaracetam", "Fenobarbital", "Clobazam", "Clonazepam", "Diazepam", "Lorazepam", "Midazolam", "Vigabatrina", "Gabapentina", "Pregabalina", "Etosuximida", "Perampanel", "Cannabidiol", "Everolimus")
                "refractariedad_sudep" -> listOf("Clobazam", "Cannabidiol", "Everolimus")
                "terapias_antiamiloide" -> listOf("Lecanemab")
                "diagnostico_cognitivo" -> listOf("Donepezilo", "Memantina")
                "parkinson_avanzado" -> listOf("Levodopa / Carbidopa", "Pramipexol", "Rasagilina", "Entacapona")
                "toxina_botulinica_avanzada" -> listOf("Toxina Botulínica Tipo A")
                else -> emptyList()
            }
        }
        if (relatedDrugs.isNotEmpty()) {
            GuideRelatedDrugs(drugs = relatedDrugs, onNavigateToDrug = onNavigateToDrug)
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (protocolId) {
            "acv_2026" -> {
                GuideHeader("Código ACV Completo", "Guías de Práctica Clínica AHA/ASA 2026 y Consejos Científicos", "AHA/ASA Stroke Standards 2026", "Actualizado Q1 2026 - Con Tenecteplasa")

                // Dossier Detail Popup Dialog
                if (selectedTrialDossierId != null) {
                    val trialId = selectedTrialDossierId!!
                    var acronym = ""
                    var title = ""
                    var windowText = ""
                    var hypothesis = ""
                    var methodology = ""
                    var inclusionThresholds = ""
                    var outcomesText = ""
                    var statPower = ""
                    var recommendation = ""

                    when (trialId) {
                        "dawn" -> {
                            acronym = "Estudio DAWN"
                            title = "DWI or CTP Assessment with Clinical Mismatch in the Triage of Wake-Up and Late Presenting Strokes Undergoing Neurointervention with Trevo"
                            windowText = "6 a 24 horas desde LKW (Last Known Well) / Wake-up Stroke"
                            hypothesis = "Comprobar si la Trombectomía Mecánica (EVT) guiada por discrepancia clínico-radiológica (déficits neurológicos severos comparados con volumen de infarto pequeño en RM o CTP) es superior a la terapia médica convencional."
                            methodology = "Ensayo clínico prospectivo, aleatorizado (1:1), multicéntrico, internacional y abierto con evaluación ciega de desenlaces (PROBE). Se reclutaron 206 pacientes de una meta planificada de 500, detenido tempranamente tras análisis intermedio por beneficio abrumador."
                            inclusionThresholds = "• Oclusión de la Carótida Interna intracraneal, segmento M1 de la ACM o ambos.\n" +
                                                 "• Defecto neurológico grave en relación con el infarto (Discrepancia clínico-radiológica):\n" +
                                                 "  - Grupo A: Edad ≥80 años, NIHSS ≥10, volumen de core isquémico por RAPID <21 mL.\n" +
                                                 "  - Grupo B: Edad <80 años, NIHSS ≥10, volumen de core isquémico por RAPID <31 mL.\n" +
                                                 "  - Grupo C: Edad <80 años, NIHSS ≥20, volumen de core isquémico por RAPID de 31 a <51 mL."
                            outcomesText = "• Independencia funcional a los 90 días (mRS 0-2): 49% en el grupo de Trombectomía vs 13% en el grupo de control médico manual (Diferencia de 36%, p<0.001).\n" +
                                           "• NNT de 2.8! Significa que 1 de cada 2.8 pacientes de ventana extendida tratados logró independencia funcional.\n" +
                                           "• Tasa de hemorragia intracraneal sintomática (sICH) no mostró aumento significativo (6% en EVT vs 3% en control, p=0.33).\n" +
                                           "• Mortalidad a 90 días: 19% en EVT vs 18% en control (p=0.92, no inferior)."
                            statPower = "Poder estadístico masivo para subgrupos de LKW tardío (incluidos wake-up stroke y presentación prolongada). Reducción del riesgo relativo de discapacidad del 73% (Hazard Ratio para tasa de recuperación del 4.8)."
                            recommendation = "Recomendación Clase I (Nivel de Evidencia A) en Guías AHA/ASA 2026 para pacientes que cumplan los estrictos criterios de discrepancia DAWN 6-24h."
                        }
                        "defuse_3" -> {
                            acronym = "Estudio DEFUSE 3"
                            title = "Endovascular Therapy for Ischemic Stroke with Perfusion Imaging Selection"
                            windowText = "6 a 16 horas desde LKW / Presentación Tardía"
                            hypothesis = "Evaluar si la Trombectomía Mecánica (EVT) dentro de las 6-16 horas del inicio en pacientes seleccionados por perfusión volumétrica automatizada de penumbra en TAC/RM (alto volumen en riesgo respecto a core pequeño) mejora outcomes funcionales."
                            methodology = "Ensayo clínico controlado, aleatorizado, multicéntrico, abierto fase III. Detenido tras 182 pacientes incluidos debido a la confirmación de la efectividad del estudio DAWN concomitante."
                            inclusionThresholds = "• Edad de 18 a 90 años con NIHSS inicial ≥6.\n" +
                                                 "• Oclusión proximal de la Carótida Interna o segmento M1 de la ACM por AngioTAC/AngioRM.\n" +
                                                 "• Criterios de perfusión de mismatch volumétrico (RAPID):\n" +
                                                 "  - Volumen del core isquémico inicial <70 mL.\n" +
                                                 "  - Volumen de tejido hipoperfundido (Tmax >6s) ≥15 mL.\n" +
                                                 "  - Ratio de mismatch (volumen total / core) ≥1.8."
                            outcomesText = "• Independencia funcional (mRS 0-2) a los 90 días: 45% en el grupo endovascular vs 17% en el grupo médico convencional (p<0.001).\n" +
                                           "• NNT de 4 para outcomes favorables!\n" +
                                           "• Mortalidad a los 90 días fue significativamente menor en el grupo de EVT: 14% vs 26% en el grupo control (p=0.05).\n" +
                                           "• No hubo diferencias significativas en hemorragia sintomática (sICH 7% vs 4%, p=0.75)."
                            statPower = "Odds Ratio ajustado para cambio en escala mRS fue de 2.77, demostrando una mejora consistente en todos los estadíos neurofuncionales."
                            recommendation = "Recomendación Clase I (Nivel de Evidencia A) en Guías AHA/ASA 2026 para pacientes 6-16h de evolución que cumplan criterios de mismatch de perfusión DEFUSE-3."
                        }
                        "extend" -> {
                            acronym = "Estudio EXTEND"
                            title = "Extending the Time for Thrombolysis in Emergency Neurological Deficits"
                            windowText = "4.5 a 9 horas / Wake-up Stroke"
                            hypothesis = "Probar si el rt-PA intravenoso (Alteplasa 0.9 mg/kg) administrado en la ventana extendida de 4.5 a 9.0 horas o Wake-up Stroke guiados por perfusión (RAPID) se asocia con mejores desenlaces de supervivencia sin discapacidad extrema."
                            methodology = "Ensayo clínico aleatorizado, doble ciego, multicéntrico, controlado con placebo. Detenido con 225 pacientes de los 310 previstos por publicación de otro ensayo."
                            inclusionThresholds = "• Edad ≥18 años.\n" +
                                                 "• LKW entre 4.5 a 9 horas, o despertar con el déficit (LKW a la mitad del sueño, dentro de un rango de 9 horas).\n" +
                                                 "• Criterios de mismatch de perfusión por TAC/RM automatizada:\n" +
                                                 "  - Volumen de core isquémico inicial <70 mL.\n" +
                                                 "  - Volumen de mismatch de penumbra isquémica >10 mL.\n" +
                                                 "  - Ratio de mismatch >1.2."
                            outcomesText = "• Independencia funcional (mRS 0-1 con excelente evolución) a los 90 días: 35.4% con Alteplasa vs 29.5% con placebo (p=0.04).\n" +
                                           "• NNT de aproximadamente 17 para recuperación excelente.\n" +
                                           "• Hemorragia intracraneal sintomática (sICH) fue mayor en el grupo activo: 6.2% vs 0.9% en placebo (p=0.053).\n" +
                                           "• Mortalidad de 90 días idéntica en ambos grupos (~9.0%)."
                            statPower = "Muestra por primera vez que el beneficio de la lisis IV se prolonga en pacientes seleccionados por penumbra viable, incluso duplicando el riesgo de sICH, el balance neto funcional es marcadamente favorable."
                            recommendation = "Recomendación Clase IIa (Nivel de Evidencia B-R) en Guías AHA/ASA 2026 en pacientes con déficit moderado-severo en ventana 4.5-9h SIN oclusión proximal tratable por EVT de primera instancia, seleccionados por mismatch de perfusión."
                        }
                        "wakeup" -> {
                            acronym = "Estudio WAKE-UP"
                            title = "Efficacy and Safety of MRI-Based Thrombolysis in Wake-Up Stroke"
                            windowText = "Despertar con síntomas / Tiempo de inicio desconocido"
                            hypothesis = "Determinar si la trombólisis de infusión intravenosa con Alteplasa mejora el resultado funcional en pacientes que se despiertan con un ictus o tienen un inicio de síntomas inatestiguado, seleccionados por la discrepancia de señal en Resonancia de difusión (DWI) vs FLAIR."
                            methodology = "Ensayo multicéntrico, aleatorizado, doble ciego, controlado con placebo. Incluyó 503 pacientes seleccionados por RM de un plan original de 800, detenido de forma prematura por falta de financiamiento."
                            inclusionThresholds = "• Edad de 18 a 80 años.\n" +
                                                 "• ACV agudo de tiempo de inicio desconocido (despertó con síntomas) dentro de las 4.5 horas del reporte subjetivo.\n" +
                                                 "• Discrepancia DWI/FLAIR en RM de cráneo simple:\n" +
                                                 "  - Lesión evidente e isquémica visible en difusión (DWI).\n" +
                                                 "  - Ausencia de hiperintensidad correspondiente en secuencia FLAIR (indica que el infarto tiene menos de 4.5 horas de instauración, 'infarto joven').\n" +
                                                 "• Exclusión de pacientes candidatos directos planeados a trombectomía mecánica."
                            outcomesText = "• Resultados funcionales excelentes (mRS 0-1) a los 90 días: 53.3% con Alteplasa vs 41.8% con placebo (Odds Ratio ajustado 1.61, p=0.02).\n" +
                                           "• Mismatch radiológico positivo confiere un NNT de 9 pacientes.\n" +
                                           "• Tasa de hemorragia cerebral sintomática (sICH): 2.0% en grupo de lisis vs 0.4% en grupo placebo (p=0.15).\n" +
                                           "• Mortalidad a 90 días fue baja e idéntica en ambos brazos (4.1% vs 1.2%, p=0.07)."
                            statPower = "Estudio fundamental que redefinió el manejo del ictus al despertar, sustituyendo el criterio de 'reloj rígido' por un criterio biológico de 'reloj celular tisular' mediante RM cerebral."
                            recommendation = "Recomendación Clase I (Nivel de Evidencia B-R) en Guías AHA/ASA 2026 para el uso de Alteplasa IV en wake-up stroke si se corrobora de forma estricta la discrepancia DWI/FLAIR en RM de urgencia."
                        }
                        "extend_ia_tnk" -> {
                            acronym = "Estudio EXTEND-IA TNK"
                            title = "Tenecteplase versus Alteplase before Thrombectomy for Ischemic Stroke"
                            windowText = "Ventana Temprana < 4.5 horas previo a Trombectomía"
                            hypothesis = "Probar si la Tenecteplasa intravenosa (TNK) es superior a la Alteplasa estándar en lograr la reperfusión del vaso oclusivo principal antes de que el paciente reciba la recanalización por cateterismo (EVT)."
                            methodology = "Ensayo clínico controlado, aleatorizado (1:1), multicéntrico, de no inferioridad y posterior prueba de superioridad. Incluyó 204 pacientes."
                            inclusionThresholds = "• Ictus isquémico agudo eligible para trombólisis farmacológica IV dentro de las 4.5 horas.\n" +
                                                 "• Oclusión proximal confirmada por AngioTAC de la carótida interna, bifurcación carotídea o segmentos M1/M2 de la ACM.\n" +
                                                 "• Candidatos listos para transferir rápido a sala de angiografía (trombectomía mecánica)."
                            outcomesText = "• Recanalización sustancial (>50%) del vaso o mejoría clínica temprana sustancial antes de la primera pasada de trombectomía:\n" +
                                           "  - 22% en el grupo tratado con Tenecteplasa vs 10% en el grupo de Alteplasa (Superioridad idónea, p=0.002).\n" +
                                           "• Outcomes funcionales consistentes (mRS 0-1) a los 90 días: 65% con TNK vs 52% con Alteplasa (p=0.04).\n" +
                                           "• Tasa de hemorragia intracraneal sintomática (sICH) fue igual en ambos grupos (1.0% vs 1.0%), confirmando excelente perfil de seguridad."
                            statPower = "Determinó que la afinidad por fibrina de la Tenecteplasa (TNK) aumenta sustancialmente las lisis tempranas espontáneas facilitando la reperfusión rápida sin aumentar el riesgo de hemorragias masivas."
                            recommendation = "Recomendación Clase I (Nivel de Evidencia A) en Guías AHA/ASA 2026 pregonando a Tenecteplasa (TNK) 0.25 mg/kg IV en bolo directo como el estándar de oro preferido en pacientes elegibles para trombectomía mecánica rápida."
                        }
                        "direct_mt" -> {
                            acronym = "Estudio DIRECT-MT"
                            title = "Direct Intra-Arterial Thrombectomy in Order to Revascularize AIS Patients with Large Vessel Occlusion"
                            windowText = "Ventana Temprana < 6 horas con Oclusión LVO"
                            hypothesis = "Determinar si la Trombectomía Mecánica endovascular directa sola no es inferior a la terapia combinada estándar (lisis intravenosa con Alteplasa seguida rápido por trombectomía mecánica) en pacientes con oclusión de vasos proximales grandes."
                            methodology = "Ensayo clínico de no inferioridad, controlado, aleatorizado, multicéntrico, de fase III en 41 centros académicos en China. Evaluó 656 pacientes."
                            inclusionThresholds = "• Ictus isquémico agudo con oclusión del segmento de vasos proximales (ACI o M1 ACM) confirmado por AngioTAC.\n" +
                                                 "• Ventana terapéutica de inicio menor de 6 horas desde LKW.\n" +
                                                 "• Candidato formal a terapia de reperfusión IV con Alteplasa."
                            outcomesText = "• Trombectomía directa sola cumplió el límite de no inferioridad estadística para la escala mRS a 90 días (ajustado Odds Ratio 1.07, p de no inferioridad = 0.04).\n" +
                                           "• De forma individual: 36.6% en el grupo de trombectomía directa versus 36.8% en el grupo combinado lograron mRS 0-2 (independencia funcional).\n" +
                                           "• Revascularización de perfusión exitosa al primer intento fue menor en el grupo directo (79.4% vs 84.5% en combinado).\n" +
                                           "• Hemorragia intracraneal sintomática (sICH) no mostró diferencias estadísticas (4.3% en directo vs 6.1% en combinado, p=0.30)."
                            statPower = "Aunque demuestra que la trombectomía sola es una alternativa en centros experimentados, la lisis IV continuada provee un amortiguador sustancial y mejora tasas de reperfusión final. Su relevancia clínica reside en pacientes con contraindación absoluta de lisis."
                            recommendation = "Recomendación Clase I (Nivel de Evidencia A) en Guías AHA/ASA 2026: No postergar la lisis IV con Tenecteplasa en candidatos a trombectomía mecánica. El 'puente farmacológico' sigue siendo el default."
                        }
                    }

                    AlertDialog(
                        onDismissRequest = { selectedTrialDossierId = null },
                        shape = RoundedCornerShape(16.dp),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = acronym,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = windowText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                
                                DossierCardItem("Hipótesis Clínica", hypothesis, Icons.Default.Lightbulb, Color(0xFFE65100))
                                DossierCardItem("Metodología & Diseño", methodology, Icons.Default.MenuBook, Color(0xFF0288D1))
                                DossierCardItem("Criterios de Inclusión & Mismatch", inclusionThresholds, Icons.Default.CheckCircle, Color(0xFF2E7D32))
                                DossierCardItem("Resultados Más Relevantes", outcomesText, Icons.Default.Star, Color(0xFFD32F2F))
                                DossierCardItem("Análisis Estadístico & Poder", statPower, Icons.Default.TrendingUp, Color(0xFF7B1FA2))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Recomendación AHA/ASA 2026:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = recommendation,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = { selectedTrialDossierId = null },
                                modifier = Modifier.testTag("dismiss_dossier_dialog")
                            ) {
                                Text("Entendido", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // 1. Interactive Sliding Timeline
                SectionCard("1. Deslizador Interactivo de Ventanas de Tiempo (Bedside Timeline)") {
                    Text(
                        text = "Deslice el dial para simular el tiempo transcurrido desde el inicio de síntomas o Última vez Visto Normal (LKW) y visualizar la conducta inmediata guiada por evidencia:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Selected value indicator label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiempo Estimado LKW:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    when {
                                        selectedLkwHours <= 4.5f -> Color(0xFFE1F5FE)
                                        selectedLkwHours <= 6.0f -> Color(0xFFFFF3E0)
                                        selectedLkwHours <= 24.0f -> Color(0xFFF3E5F5)
                                        else -> Color(0xFFFFEBEE)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${String.format("%.1f", selectedLkwHours)} Horas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    selectedLkwHours <= 4.5f -> Color(0xFF0288D1)
                                    selectedLkwHours <= 6.0f -> Color(0xFFE65100)
                                    selectedLkwHours <= 24.0f -> Color(0xFF7B1FA2)
                                    else -> Color(0xFFC62828)
                                }
                            )
                        }
                    }

                    Slider(
                        value = selectedLkwHours,
                        onValueChange = { selectedLkwHours = it },
                        valueRange = 0.0f..24.0f,
                        steps = 47, // 0.5h steps
                        modifier = Modifier.fillMaxWidth().testTag("stroke_timeline_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Conduct card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                selectedLkwHours <= 4.5f -> Color(0xFFE3F2FD)
                                selectedLkwHours <= 6.0f -> Color(0xFFFFF3E0)
                                selectedLkwHours <= 24.0f -> Color(0xFFFAF0FD)
                                else -> Color(0xFFFFF5F5)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when {
                                selectedLkwHours <= 4.5f -> Color(0xFF90CAF9)
                                selectedLkwHours <= 6.0f -> Color(0xFFFFCC80)
                                selectedLkwHours <= 24.0f -> Color(0xFFE1BEE7)
                                else -> Color(0xFFFFCDD2)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = when {
                                        selectedLkwHours <= 4.5f -> Color(0xFF1976D2)
                                        selectedLkwHours <= 6.0f -> Color(0xFFF57C00)
                                        selectedLkwHours <= 24.0f -> Color(0xFF8E24AA)
                                        else -> Color(0xFFD32F2F)
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        selectedLkwHours <= 4.5f -> "Ventana Ultra Temprana (Trombólisis Activa)"
                                        selectedLkwHours <= 6.0f -> "Ventana Temprana Directa (Oclusión Proximal)"
                                        selectedLkwHours <= 24.0f -> "Ventana de Perfusión Avanzada / Extendida"
                                        else -> "Ventana Crónica / Fuera de Reperfusión Temprana"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when {
                                    selectedLkwHours <= 4.5f -> "• Terapia Farmacológica: Candidato óptimo de primera línea para Trombólisis Intravenosa activa. Se prefiere Tenecteplasa (TNK) 0.25 mg/kg IV en bolo único rápido (máx 25 mg), especialmente en sospecha de oclusión de vaso grande (LVO), por tasas de recanalización superiores.\n• Terapia Endovascular: Realizar AngioTAC de manera inmediata; si se confirma oclusión proximal (ACI/M1/M2), programar trombectomía mecánica (EVT) paralela al bolo."
                                    selectedLkwHours <= 6.0f -> "• Terapia Farmacológica: Trombólisis intravenosa estándar contraindicada clínicamente. (Salvo protocolo de perfusión EXTEND si es Wake-up stroke).\n• Terapia Endovascular: Candidato directo clase I a Trombectomía Mecánica (EVT) si posee oclusión de gran vaso proximal en AngioTAC. No requiere perfusión automatizada para autorizar el pase a sala de hemodinamia."
                                    selectedLkwHours <= 24.0f -> "• Terapia Endovascular Extendida: Trbrombectomía Mecánica (EVT) es muy efectiva en este rango. Requiere corroboración estricta de viabilidad tisular mediante software automatizado (RAPID CTP / RM) demostrando volumen de core <70mL y penumbra en riesgo.\n• Evidencia de Respaldo: Criterios DAWN (6-24h) basados en discrepancia clínica-infarto o DEFUSE 3 (6-16h) basados en mismatch de perfusión."
                                    else -> "• Conducta Bedside: Fuera de ventana para reperfusión estándar (rt-PA/EVT) salvo protocolos de investigación experimentales.\n• Plan Médico: Ingreso inmediato a Unidad de Ictus / cuidados intermedios. Descartar hemorragia mediante TAC de control a las 24 horas y arrancar esquema rápido de antiagregación y estatinas de alta potencia."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // 2. Interactive BP Assistant
                SectionCard("2. Asistente de Tensión Arterial (Presión Objetivo Aguda)") {
                    Text("Seleccione el escenario clínico e introduzca la presión arterial medida para calcular metas bedside de infusión:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Mode selector pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "lysis_candidate" to "Candidato a lisis (pre-bolo)",
                            "post_lysis" to "Post-Trombólisis (<24h)",
                            "no_reperfusion" to "No reperfusión agudo"
                        ).forEach { (mode, title) ->
                            val isSelected = bpWorkingMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { bpWorkingMode = mode },
                                label = { Text(title) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Input Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = bpSys,
                            onValueChange = { bpSys = it.filter { ch -> ch.isDigit() } },
                            label = { Text("PAS (Sistólica)") },
                            suffix = { Text("mmHg") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bpDia,
                            onValueChange = { bpDia = it.filter { ch -> ch.isDigit() } },
                            label = { Text("PAD (Diastólica)") },
                            suffix = { Text("mmHg") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    val sysVal = bpSys.toIntOrNull() ?: 120
                    val diaVal = bpDia.toIntOrNull() ?: 80

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calculation outputs
                    val bpMetrics = when (bpWorkingMode) {
                        "lysis_candidate" -> {
                            if (sysVal >= 185 || diaVal >= 110) {
                                val text = "⚠️ ALERTA DE EXCLUSIÓN DE LISIS: PA actual es ≥185/110 mmHg. DEBE REDUCIRSE antes de iniciar el bolo de Tenecteplasa/rt-PA.\n\n" +
                                           "► TRATAMIENTO URGENTE:\n" +
                                           "- Labetalol 10 a 20 mg IV lentos de 1-2 min (puede repetirse una vez).\n" +
                                           "- Infusión continua de Nicardipina IV a razón de 5 mg/h (ajustar cada 5-15 min, máx 15 mg/h).\n" +
                                           "- Monitorear PA estrictamente cada 5 min."
                                BpResultMetrics(Color(0xFFC62828), Color(0xFFFFF1F1), Color(0xFFFFCDD2), text)
                            } else {
                                val text = "✓ PRESIÓN COMPATIBLE PARA REPERFUSIÓN (<185/110 mmHg). Puede proceder con infusión/bolo de Tenecteplasa (TNK) de forma segura."
                                BpResultMetrics(Color(0xFF2E7D32), Color(0xFFE8F5E9), Color(0xFFC8E6C9), text)
                            }
                        }
                        "post_lysis" -> {
                            if (sysVal >= 180 || diaVal >= 105) {
                                val text = "⛔ EXCESO DE LÍMITE DE SEGURIDAD POST-LISIS: PA excede 180/105 mmHg. Riesgo masivo de transformación hemorrágica del infarto.\n\n" +
                                           "► TRATAMIENTO INMEDIATO:\n" +
                                           "- Administrar Labetalol 10 mg IV seguido de infusión o Clevidipina/Nicardipina en goteo continuo.\n" +
                                           "- El examen neurológico estrecho debe intensificarse (interrumpir infusión ante cefalea brusca, náuseas o pico NIHSS)."
                                BpResultMetrics(Color(0xFFC62828), Color(0xFFFFF1F1), Color(0xFFFFCDD2), text)
                            } else {
                                val text = "✓ CONTROL SEGURO POST-LISIS: Mantenga PA estrictamente menor de 180/105 mmHg durante las próximas 24 horas."
                                BpResultMetrics(Color(0xFF2E7D32), Color(0xFFE8F5E9), Color(0xFFC8E6C9), text)
                            }
                        }
                        else -> { // no_reperfusion
                            if (sysVal > 220 || diaVal > 120) {
                                val text = "⚠️ HIPERTENSIÓN EXTREMA SINFÓNICA (>220/120 mmHg) en paciente no reperfundido.\n\n" +
                                           "► INDICACIÓN:\n" +
                                           "- Reducir un 15% de forma cuidada en las primeras 24 horas usando goteo de Nitroprusiato, Clevidipina, o Labetalol.\n" +
                                           "- Evitar caídas tensionales abruptas para resguardar perfusión crítica."
                                BpResultMetrics(Color(0xFFEF6C00), Color(0xFFFFF3E0), Color(0xFFFFCC80), text)
                            } else {
                                val text = "✓ HIPERTENSIÓN PERMISIVA AGUDA (PA <220/120 mmHg).\n\n" +
                                           "• Conducta: NO intervenir farmacológicamente la Presión Arterial. Se prefiere hipertensión permisiva moderada para mantener la presión de perfusión cerebral óptima en la penumbra isquémica."
                                BpResultMetrics(Color(0xFF1565C0), Color(0xFFE3F2FD), Color(0xFFBBDEFB), text)
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = bpMetrics.containerColor),
                        border = BorderStroke(1.dp, bpMetrics.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = bpMetrics.alertText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = bpMetrics.titleColor,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 16.sp
                        )
                    }
                }

                // 3. Reversion card
                SectionCard("3. Anticoagulantes & Estrategia de Reversión Rápida") {
                    Text("Esquema inmediato ante sospecha de ACV bajo anticoagulantes:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Dabigatrán (Inhibidor de trombina)", "Revertir con Idarucizumab 5g IV (2 viales de 2.5g administrados de forma consecutiva). Puede iniciarse trombolisis si el tiempo de TT / dTT es normal tras su paso.")
                    BulletText("Inhibidores del Factor Xa (Apixabán, Rivaroxabán)", "Revertir con Andexanet Alfa (bolo + infusión) de acuerdo a última toma. Alternativa: Complejo Protrombínico Concentrado (PCC) de 4 factores a dosis de 50 UI/kg IV.")
                    BulletText("Warfarina / Cumarínicos", "Revertir urgente con PCC de 4 Factores (25-50 UI/kg de acuerdo al INR basal) + Vitamina K 10 mg IV infusión lenta, de preferencia rápida sobre Plasma Fresco Congelado.")
                }

                // 4. Clinical Trials Dossier Explorer
                SectionCard("4. Evidencia de Ensayos Pivotales & Métodos Expandidos") {
                    Text(
                        text = "Selección de estudios históricos y de ventanas de tiempo extendidas. Presione un estudio para ver metodologías detalladas, criterios de inclusión y resultados estadísticos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val trials = listOf(
                        TrialIndicator(
                            id = "dawn",
                            title = "Estudio DAWN",
                            window = "6–24 hrs",
                            summary = "Comprueba superioridad clínica de la Trombectomía Mecánica guiada por discrepancia clínico-radiológica.",
                            mValue = "NNT 2.8",
                            accentColor = Color(0xFF7B1FA2),
                            barColor = Color(0xFF7B1FA2),
                            icon = Icons.Default.Layers
                        ),
                        TrialIndicator(
                            id = "defuse_3",
                            title = "Estudio DEFUSE 3",
                            window = "6–16 hrs",
                            summary = "Trombectomía Mecánica guiada por mismatch automatizado (RAPID) de perfusión avanzada.",
                            mValue = "NNT 4",
                            accentColor = Color(0xFF1976D2),
                            barColor = Color(0xFF0288D1),
                            icon = Icons.Default.Assessment
                        ),
                        TrialIndicator(
                            id = "extend",
                            title = "Estudio EXTEND",
                            window = "4.5–9 hrs",
                            summary = "rt-PA lisis intravenosa en ventana extendida o del despertar, guiada por penumbra viable por TAC.",
                            mValue = "NNT 17",
                            accentColor = Color(0xFF00796B),
                            barColor = Color(0xFF00796B),
                            icon = Icons.Default.TrendingUp
                        ),
                        TrialIndicator(
                            id = "wakeup",
                            title = "Estudio WAKE-UP",
                            window = "Wake-up Stroke",
                            summary = "Trombólisis en inicio desconocido evaluada por discrepancia DWI/FLAIR en resonancia magnética.",
                            mValue = "NNT 9",
                            accentColor = Color(0xFF2E7D32),
                            barColor = Color(0xFF2E7D32),
                            icon = Icons.Default.CheckCircle
                        ),
                        TrialIndicator(
                            id = "extend_ia_tnk",
                            title = "Estudio EXTEND-IA TNK",
                            window = "TNK < 4.5 hrs",
                            summary = "Tenecteplasa 0.25 mg/kg vs Alteplasa 0.9 mg/kg pre-trombectomía en oclusión proximal.",
                            mValue = "TNK Superior",
                            accentColor = Color(0xFFC2185B),
                            barColor = Color(0xFFC2185B),
                            icon = Icons.Default.Star
                        ),
                        TrialIndicator(
                            id = "direct_mt",
                            title = "Estudio DIRECT-MT",
                            window = "EVT Directa M1",
                            summary = "Trombectomía directa sola contra el puente combinado convencional de lisis intravenosa pre-intervención.",
                            mValue = "No Inferior",
                            accentColor = Color(0xFFE65100),
                            barColor = Color(0xFFE65100),
                            icon = Icons.Default.Share
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        trials.forEach { trial ->
                            Card(
                                onClick = { selectedTrialDossierId = trial.id },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trial_card_${trial.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    // Custom Left bar matching the category
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(5.dp)
                                            .background(trial.barColor)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = trial.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .background(trial.accentColor.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                                        .border(BorderStroke(1.dp, trial.accentColor.copy(alpha = 0.25f)), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = trial.window,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = trial.accentColor
                                                    )
                                                }
                                            }
                                            Text(
                                                text = trial.summary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(trial.barColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = trial.mValue,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = trial.barColor
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Ver estudio",
                                                tint = trial.barColor.copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Late Window Candidate Evaluator Matcher Simulator
                SectionCard("5. Simulador de Viabilidad Ventana Extendida (Matcher)") {
                    Text(
                        text = "Introduzca los parámetros del paciente para que el simulador analice la elegibilidad empírica según los criterios estrictos de DAWN y DEFUSE-3:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Fields
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mAge,
                                onValueChange = { mAge = it.filter { c -> c.isDigit() } },
                                label = { Text("Edad (años)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = mNihss,
                                onValueChange = { mNihss = it.filter { c -> c.isDigit() } },
                                label = { Text("NIHSS") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mLkw,
                                onValueChange = { mLkw = it },
                                label = { Text("LKW (horas)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = mCoreVol,
                                onValueChange = { mCoreVol = it.filter { c -> c.isDigit() } },
                                label = { Text("Volumen Core (mL)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mMismatchVol,
                                onValueChange = { mMismatchVol = it.filter { c -> c.isDigit() } },
                                label = { Text("Penumbra Vol (mL)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            // Occlusion Picker
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(4.dp))
                                    .clickable {
                                        mOcclusion = when (mOcclusion) {
                                            "ICA/M1" -> "M2/Other"
                                            "M2/Other" -> "None"
                                            else -> "ICA/M1"
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Text("Oclusión Gran Vaso", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(mOcclusion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Logical Evaluation
                    val age = mAge.toIntOrNull() ?: 70
                    val nihss = mNihss.toIntOrNull() ?: 12
                    val lkw = mLkw.toDoubleOrNull() ?: 8.0
                    val core = mCoreVol.toIntOrNull() ?: 18
                    val mismatch = mMismatchVol.toIntOrNull() ?: 80
                    val isLvo = mOcclusion == "ICA/M1"
                    
                    val passesDawn = lkw >= 6.0 && lkw <= 24.0 && isLvo && nihss >= 10 && (
                        (age >= 80 && core < 21) ||
                        (age < 80 && nihss >= 10 && nihss < 20 && core < 31) ||
                        (age < 80 && nihss >= 20 && core >= 31 && core < 51)
                    )

                    val ratio = (core + mismatch).toDouble() / maxOf(1, core)
                    val passesDefuse3 = lkw >= 6.0 && lkw <= 16.0 && isLvo && nihss >= 6 && age in 18..90 && core < 70 && mismatch >= 15 && ratio >= 1.8

                    Text("Emparejamiento / Diagnóstico Simulador:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // DAWN Candidate Checklist Item
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (passesDawn) Color(0xFFE8F5E9) else Color(0xFFFEEBEE), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, if (passesDawn) Color(0xFF81C784) else Color(0xFFEF9A9A)), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (passesDawn) "✓ Criterios DAWN CUMPLIDOS" else "✗ Excluido de Protocolo DAWN",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (passesDawn) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { selectedTrialDossierId = "dawn" },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("Ver Criterios", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        // DEFUSE 3 Candidate Checklist Item
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (passesDefuse3) Color(0xFFE8F5E9) else Color(0xFFFEEBEE), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, if (passesDefuse3) Color(0xFF81C784) else Color(0xFFEF9A9A)), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (passesDefuse3) "✓ Criterios DEFUSE 3 CUMPLIDOS" else "✗ Excluido de Protocolo DEFUSE 3",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (passesDefuse3) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { selectedTrialDossierId = "defuse_3" },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("Ver Criterios", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            "estado_epileptico" -> {
                GuideHeader("Estado Epiléptico Agudo", "Tratamiento por Fases del Status Epilepticus Convulsivo", "American Epilepsy Society (AES) Guidelines", "Vigente / Actualización 2026")

                SectionCard("Calculadora Inteligente de Dosis por Peso") {
                    Text("Ingresa el peso estimado del paciente para calcular dosis bedside:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = patientWeight,
                        onValueChange = { patientWeight = it },
                        label = { Text("Peso del Paciente (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    val weight = patientWeight.toDoubleOrNull()
                    if (weight != null && weight > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Dosis Bedside Calculadas:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val loraDose = minOf(4.0, weight * 0.1)
                        val diaDose = minOf(10.0, weight * 0.2)
                        val midDose = minOf(10.0, weight * 0.2)
                        val leveDose = minOf(4500.0, weight * 60.0)
                        val valproDose = minOf(3000.0, weight * 40.0)
                        val phenytoinDose = minOf(2000.0, weight * 20.0)

                        CalculatedDoseItem("Fase 1 (Lorazepam)", "${String.format("%.1f", loraDose)} mg IV en bolo lento (Máx 4mg). Repetir en 5 min si continúa.")
                        CalculatedDoseItem("Fase 1 (Diazepam)", "${String.format("%.1f", diaDose)} mg IV en 1-2 min (Máx 10mg).")
                        CalculatedDoseItem("Fase 1 (Midazolam IM)", "${String.format("%.1f", midDose)} mg IM directo si no hay vía accesible (Máx 10mg).")
                        CalculatedDoseItem("Fase 2 (Levetiracetam)", "${String.format("%.0f", leveDose)} mg IV en infusión en 15 minutos (Dosis standard 60 mg/kg, máx 4.5g).")
                        CalculatedDoseItem("Fase 2 (Valproato Sódico)", "${String.format("%.0f", valproDose)} mg IV en infusión en 5-10 minutos (Dosis standard 40 mg/kg, máx 3g). Ajustar por hepatopatía.")
                        CalculatedDoseItem("Fase 2 (Fenitoína)", "${String.format("%.0f", phenytoinDose)} mg IV en Solución Salina a velocidad <50 mg/min (Máx 2g). Requiere monitoreo ECG.")
                    } else {
                        Text("*Ingresa un peso válido arriba para mostrar el desglose de dosis personalizado.*", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                SectionCard("Algoritmo por Minutos (Guía Temporal)") {
                    TimePhaseIndicator("T 0 a 5 min", "Fase Inicial / Estabilización", "ABC, O2 suplementario, control metabólico inmediato. ¡Chequear glucosa capilar! Si glucosa <60, administrar 100 mg de Tiamina IV + un bolo de Glucosa al 50%. Enviar toxinas, electrolitos, pH, niveles de anticonvulsivantes.")
                    TimePhaseIndicator("T 5 a 20 min", "Fase de Status Establecido", "Vía de primera línea: Benzodiacepinas. Lorazepam IV, Diazepam IV o Midazolam IM. Si el estatus cede, iniciar dosis de mantenimiento.")
                    TimePhaseIndicator("T 20 a 40 min", "Fase de Estado Refractario Inicial", "Segunda línea no anestésica: Levetiracetam, Valproato de Sodio o Fenitoína. Elegir según perfil (evitar Valproato en sospecha de mitocondriopatías o insuficiencia hepática grave).")
                    TimePhaseIndicator("T >40 min", "Fase de Estado Refractario Tardío / Anestesia", "Requiere Intubación Orotraqueal y goteo continuo monitorizado por EEG con Propofol (1-2 mg/kg bolo, luego 2-10 mg/kg/h), Midazolam (0.2 mg/kg bolo, luego 0.05-2 mg/kg/h), o Pentobarbital. Metas de supresión de brotes (burst suppression) por EEG de 24 horas.")
                }
            }

            "coma_encefalopatia" -> {
                GuideHeader("Coma & Encefalopatía Aguda", "Protocolo de Evaluación Inicial e Integral Bedside", "CNS Acute Coma Consensus", "Revisado 2026")

                SectionCard("1. Evaluación de Glasgow vs FOUR Score") {
                    BulletText("Glasgow Coma Scale", "Clásica, excelente para trauma, pero deficiente para intubación. Evalúa Ocular (1-4), Verbal (1-5), Motora (1-6).")
                    BulletText("FOUR Score (Full Outline of UnResponsiveness)", "Superior en UCI por evaluar respuestas del tronco. Cuatro dominios (0-4 cada uno): Ojos (Apertura e indicaciones visuales), Motor (Respuestas nociceptivas), Reflejos del Tronco (Pupilas, córnea y tos), Respiración (Ritmo respiratorio en ventilados).")
                }

                SectionCard("2. Examen de Alerta & Respuestas Físicas") {
                    BulletText("Reflejos de Tronco / Pupilas", "Pupilas mióticas puntiformes sugieren afectación pontina o sobredosis de opiáceos. Pupila unilateral dilatada no reactiva (Midiátrica) sugiere hernia uncal inminente (emergencia neuroquirúrgica - compresión de III par).")
                    BulletText("Patrones Respiratorios", "Cheyne-Stokes (afectación hemisférica bilateral o metabólica), Hiperventilación neurógena central (mesencéfalo/protuberancia), Apnéusica (protuberancia inferior), Atáxica (bulbo raquídeo - pre-parada).")
                }

                SectionCard("3. Paquete Diagnóstico Inicial") {
                    BulletText("Laboratorios", "Glucemia, panel de electrolitos con calcio ionizado, gasometría arterial con lactato y osmolaridad, perfil hepático con amonio, creatinina, perfil tiroideo y tamizaje de tóxicos en orina.")
                    BulletText("Imágenes", "TAC de cráneo simple inicial para descartar sangrado agudo, hernia o isquemia de tallo. Considerar RM cerebral si hay sospecha de oclusión de arteria basilar.")
                }
            }

            "meningitis_flujo" -> {
                GuideHeader("Meningitis / Encefalitis Aguda & Neuroinfectología", "Secuencia de Toma de Decisiones, Análisis de LCR y Terapia Dirigida", "IDSA / ESCMID / Consensus Guidelines", "Vigente 2026")

                SectionCard("1. Flujograma de Intervención Rápida") {
                    Text("Ante la sospecha fundamentada de neuroinfección activa:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("¡Corticoide lo Antes Posible!", "Dexametasona 10 mg IV debe colocarse ANTES o inmediatamente en paralelo a la primera dosis de antibiótico. Continuar con 10 mg IV cada 6 horas por un total de 4 días si se confirma Streptococcus pneumoniae. Reduce significativamente la muerte neuronal, sordera persistente y el daño cortical secundario.")
                    BulletText("Tratamiento Antibiótico Empírico", "Ceftriaxona 2 g IV cada 12 h + Vancomicina 15-20 mg/kg IV cada 12 h (meta valle 15-20 mcg/mL) para cobertura de S. pneumoniae y N. meningitidis.")
                    BulletText("Cobertura Especial por Grupo (>50 años / Inmunosupresión)", "Añadir de manera mandatoria Ampicilina 2 g IV cada 4 h para cubrir Listeria monocytogenes.")
                    BulletText("Encefalitis Viral (Sospecha)", "Agregar Aciclovir 10 mg/kg IV cada 8 h ante datos sugestivos de Encefalitis Herpética por VHS-1/2 (por ejemplo: fiebre, alteración conductual, afasia, convulsiones focales y edema confluente en lóbulos temporales).")
                }

                SectionCard("2. ¿Cuándo diferir Punción Lumbar (PL) para realizar un TAC?") {
                    Text("Regla de Seguridad: No realizar PL inmediata si el paciente presenta cualquiera de estas 'Banderas de Alerta de Herniación'. Tomar hemocultivos bilaterales de inmediato, iniciar Dexametasona + terapia antimicrobiana empírica y trasladar a TAC de cráneo:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("1. Papiledema", "Signo clínico patognomónico de hipertensión intracraneal de moderada a grave.")
                    BulletText("2. Déficit Neurológico Focal", "Presencia de monoparesia, hemiparesia o parálisis de nervios oculomotores.")
                    BulletText("3. Alteración Severa del Estado de Alerta", "Disminución del nivel de conciencia, estupor profundo o coma (Glasgow ≤10).")
                    BulletText("4. Crisis Convulsivas Nuevas", "De inicio recente (dentro de la última semana) por posible compromiso cortical focal.")
                    BulletText("5. Inmunocompromiso Severo conocido", "Pacientes portadores de VIH/SIDA con carga viral elevada, quimioterapia activa, trasplante o uso activo de inmunomoduladores.")
                }

                SectionCard("3. Análisis Comparativo e Interpretación del LCR") {
                    Text("Parámetros clásicos de orientación diagnóstica inmediata tras obtención de LCR:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text("• Meningitis Bacteriana Clásica:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    Text("  - Aspecto: Turbio / Purulento.\n  - Leucocitos: Elevados (>1 000 a 10 000/mm³).\n  - Predominio celular: Polimorfonucleares (PMN) >80%.\n  - Proteínas: Muy altas (>100 a 500 mg/dL).\n  - Glucosa LCR/Sérica: Muy baja (<0.4 de la sérica simultánea). Tinción Gram positiva en un 60-90%.", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Meningitis Viral / Séptica común:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                    Text("  - Aspecto: Claro / Incoloro.\n  - Leucocitos: Moderados (50 a 500/mm³).\n  - Predominio celular: Mononucleares / Linfocitos >80%.\n  - Proteínas: Normales o levemente elevadas (50 a 100 mg/dL).\n  - Glucosa LCR/Sérica: Normal (rango >0.6). PCR multiplex confirmatoria.", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Meningitis Tuberculosa (TB) / Fúngica:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD69E2E))
                    Text("  - Aspecto: Claro o xantocrómico, a veces aspecto fibrilar ('velo de novia').\n  - Leucocitos: Incrementados (100 a 500/mm³).\n  - Predominio celular: Mononucleares / Linfocitos.\n  - Proteínas: Extremadamente elevadas (150 a 1000 mg/dL).\n  - Glucosa LCR/Sérica: Marcada hipoglucorráquica profunda (<0.3 de la sérica).", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Encefalitis Herpética Aguda:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                    Text("  - LCR inflamatorio con presencia anormal de eritrocitos/glóbulos rojos (necrosis hemorrágica del lóbulo temporal), pleocitosis linfocitaria, hiperproteinorráquia moderada y glucosa normal. Diagnóstico de elección por PCR HSV-1/2.", style = MaterialTheme.typography.bodySmall)
                }

                SectionCard("4. Tratamiento Dirigido y Duración de la Terapia") {
                    Text("Esquemas óptimos post-aislamiento del patógeno causal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Streptococcus pneumoniae", "Vancomicina IV + Ceftriaxona 2 g IV cada 12 h. Duración: 10–14 días. Mantener Dexametasona 10 mg IV c/6 h por los primeros 4 días.")
                    BulletText("Neisseria meningitidis", "Ceftriaxona 2 g IV cada 12 h (alternativa: Penicilina G sódica 24 millones UI/día fraccionadas). Duración: 7 días.")
                    BulletText("Listeria monocytogenes", "Ampicilina 2 g IV cada 4 h + Gentamicina 3-5 mg/kg/día IV en dosis dividas. Duración: 21 días enteros mínimo para evitar recaídas en tallo.")
                    BulletText("Haemophilus influenzae", "Ceftriaxona 2 g IV cada 12 h o cefotaxima. Duración: 7 días.")
                    BulletText("Virus del Herpes Simple", "Aciclovir 10 mg/kg IV cada 8 h (ajustado estrictamente por función renal / aclaramiento). Duración: 14 a 21 días con PCR de control negativa previo a la suspensión del fármaco.")
                }

                SectionCard("5. Complicaciones Neurocríticas Agudas") {
                    BulletText("Hipertensión intracraneal reactiva", "Tratar activamente mediante elevación de cabecera a 30°, optimización de la ventilación pCO2 o empleo de osmoterapia por pulsos (Bolo de Salina Hipertónica 3% 150-250 cc o Manitol 20%).")
                    BulletText("Crisis convulsivas", "Comunes en encefalitis y meningitis. Profilaxis o tratamiento con Levetiracetam 1000-1500 mg IV cada 12 h, ya que carece de interacciones metabólicas hepáticas y posee excelente penetración en LCR inflamado.")
                    BulletText("Ventriculitis, Abscesos & Tromboflebitis Séptica", "El retraso antibiótico puede llevar a infartos venosos secundarios. Monitorizar mediante AngioRM/AngioTC de fase venosa.")
                }
            }

            "crisis_miastenica_sgb" -> {
                GuideHeader("Crisis Miasténica & SGB Crítico", "Criterios de Vigilancia Estricta e Intubación en UCI", "Pautas de Cuidado Intensivo Neurológicos", "Revisión 2026")

                SectionCard("1. Insuficiencia Respiratoria Bedside (Regla 20/30/40)") {
                    Text("Estratificación de riesgo para ventilación mecánica prioritaria:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Capacidad Vital Forzada (FVC)", "< 20 mL/kg (Punto de inflexión crítico de fatiga pulmonar).")
                    BulletText("Inspiración Máxima (NIF/MIP)", "< -30 cmH2O (Indica debilidad severa de diafragma).")
                    BulletText("Espiración Máxima (MEP)", "< 40 cmH2O (Indica debilidad abdominal severa para toser).")
                }

                SectionCard("2. Criterios Clínicos Adicionales de Intubación") {
                    BulletText("Fatiga Bulbar Intensa", "Disfagia grave, incapacidad para manejar secreciones orales con sialorrea evidente e insuficiencia de la fonación (voz nasal). ¡Alto riesgo de broncoaspiración!")
                    BulletText("Progreso Horario Acelerado", "SGB con debilidad ascendente muy rápida o disfunción autonómica (latilidad de presión arterial, taquiarritmias/bradiarritmias).")
                }

                SectionCard("3. Medicamentos Prohibidos en Miastenia Gravis") {
                    Text("Fármacos con riesgo alto de precipitar crisis miasténicas (Bloqueo muscular):", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9B1C1C), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Antibióticos", "Aminoglucósidos (Amikacina, Gentamicina), Fluoroquinolonas (Ciprofloxacina, Levofloxacina), Macrólidos (Azitromicina, Claritromicina).")
                    BulletText("Cardiovasculares", "Beta-bloqueadores (Propranolol, Metoprolol), Bloqueadores de canales de Calcio, Procainamida, Quinidina.")
                    BulletText("Otros", "Magnesio IV, Anestésicos halogenados, Progesterona, D-penicilamina.")
                }
            }

            "vasoactivos_infusiones" -> {
                GuideHeader("Vasoactivos e Infusiones de Primera Línea en Neuro-UCI", "Protocolo Farmacológico y Metas de Seguridad Crítica", "AHA/ASA & NCS Standards", "Vigente 2026")

                SectionCard("1. Nicardipina IV") {
                    Text("• Indicación neurológica principal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - ACV isquémico candidato a lisis/trombectomía: usar si PA >185/110 mmHg; objetivo <185/110 mmHg antes de IVT y ≤180/105 mmHg durante las primeras 24 h post-IVT.\n" +
                         "  - HIC aguda espontánea leve–moderada con PAS 150–220 mmHg: objetivo operativo PAS ~140 mmHg (rango seguro 130–150 mmHg); evitar descenso sostenido <130 mmHg por riesgo de hipoperfusión.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Preparación estándar:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Bolsa habitual: 25 mg en 250 mL SSN/DAD5W = 0.1 mg/mL; alternativa comercial 20 mg/200 mL = 0.1 mg/mL.\n" +
                         "  - Conversión de flujo: 5 mg/h = 50 mL/h | 10 mg/h = 100 mL/h | 15 mg/h = 150 mL/h.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Dosis de inicio", "5 mg/h IV en bomba de infusión continua.")
                    BulletText("Titulación rápida", "Incrementar +2.5 mg/h cada 5–15 min hasta objetivo de PA. Al alcanzarlo, reducir a 3 mg/h o al 50% de la dosis efectiva, y reajustar según PA.")
                    BulletText("Dosis máxima", "15 mg/h IV.")
                    BulletText("Límites de seguridad", "PA no invasiva o arterial cada 2–5 min durante titulación, luego cada 15 min. Vigilar taquicardia refleja, cefalea, flushing, edema periférico, hipotensión. Evitar en estenosis aórtica crítica, shock cardiogénico o IC descompensada. En ACV de reperfusión: evitar caídas abruptas >15% de PA media en la primera hora.")
                }

                SectionCard("2. Labetalol IV / Bolo") {
                    Text("• Indicación neurológica principal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Reducción rápida de PA peri-trombólisis cuando se desea efecto en minutos y no hay bradicardia/bloqueo de conducción. Menos ideal para infusión fina prolongada.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Preparación estándar:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Vial común de fábrica: 5 mg/mL.\n" +
                         "  - Bolo típico listo: 20 mg = 4 mL IV.\n" +
                         "  - Infusión de goteo: 200 mg en 200 mL SSN/DAD5W = 1 mg/mL.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Dosis de inicio", "Bolo: 10–20 mg IV en 1–2 min. Puede repetirse una vez en ACV peri-IVT si PA sigue >185/110 mmHg. Alternativa por peso: 0.25 mg/kg IV (máx 20 mg). Infusión: 2 mg/min (rango 2–8 mg/min).")
                    BulletText("Titulación por pasos", "Bolo escalonado: 20 mg → 40 mg → 80 mg IV cada 10 min hasta el objetivo. Infusión: ajustar en incrementos de +1–2 mg/min cada 10 min según PA y FC.")
                    BulletText("Dosis máxima", "Bolo acumulado total: 300 mg IV. Infusión máxima: 8 mg/min.")
                    BulletText("Límites de seguridad", "PA y FC a los 5 y 10 min tras cada bolo, luego cada 15 min. Mantener FC estrictamente >55–60 lpm. Contraindicado absoluto en asma/broncoespasmo activo, EPOC severo, bloqueo AV de 2º/3º grado, bradicardia marcada o IC descompensada.")
                }

                SectionCard("3. Clevidipina IV") {
                    Text("• Indicación neurológica principal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Control ultrarrápido y altamente capilar de PA en ACV isquémico peri-reperfusión, HIC aguda y posprocedimiento neuroendovascular. Especialmente útil para vida media muy corta y ajuste minuto a minuto.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Preparación estándar:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Emulsión lipídica lista para uso: 0.5 mg/mL. Presentaciones de 25 mg/50 mL o 50 mg/100 mL. No diluir; usar vía dedicada.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Dosis de inicio", "1–2 mg/h IV = 2–4 mL/h.")
                    BulletText("Titulación rápida", "Duplicar dosis cada 90 s al inicio. Luego incrementos de +1–2 mg/h cada 5–10 min. Respuesta terapéutica habitual: 4–6 mg/h.")
                    BulletText("Dosis máxima", "Protocolo AHA/ASA para ACV: 21 mg/h.")
                    BulletText("Límites de seguridad", "PA continua o cada 1–2 min durante titulación. Contraindicada en alergia a soya o huevo, trastornos severos de lípidos y estenosis aórtica severa. Aporta lípidos: 0.2 g/mL; si uso es >24–48 h, vigilar triglicéridos séricos y pancreatitis.")
                }

                SectionCard("4. Manitol al 20%") {
                    Text("• Indicación neurológica principal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Hipertensión intracraneal aguda (HIC), edema cerebral masivo, o deterioro rápido con sospecha de herniación uncal. Evitar uso profiláctico programado en ACV sin elevación de ICP.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Preparación estándar:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Manitol al 20% = 20 g / 100 mL = 200 mg/mL. Conversión práctica: 1 g = 5 mL. Requiere uso de filtro si hay cristales visibles; calentar suavemente.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Dosis de inicio", "0.25–1 g/kg IV en bolo rápido para 15–30 min.\n- ICP moderada: 0.25–0.5 g/kg.\n- Herniación/deterioro: 1 g/kg.\n- Rescate excepcional: 1.5 g/kg una vez.")
                    BulletText("Titulación / Repetición", "Repetir 0.25–0.5 g/kg cada 4–6 h solo si persiste el aumento documentado de ICP o deterioro clínico. Evitar esquemas fijos 'por reloj' sin monitoreo.")
                    BulletText("Dosis máxima", "Límite por dosis individual: 1 g/kg (rescate único 1.5 g/kg).")
                    BulletText("Límites de seguridad", "Osmolaridad sérica cada 4–6 h. Límite clásico: <320 mOsm/kg. Preferir valor de brecha osmolar (Osm medida - Osm calculada): evitar redosificación si >20–25 mOsm/kg. Solicitar panel de electrolitos, BUN y creatinina cada 4-6 h. Suspender si creatinina sube ≥0.3 mg/dL o ≥50% basal, anuria, edema pulmonar o hipernatremia severa.")
                }

                SectionCard("5. Solución Salina Hipertónica 3%") {
                    Text("• Indicación neurológica principal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Hipertensión intracraneal aguda, edema cerebral difuso, herniación cerebral inminente e hiponatremia sintomática con riesgo de crisis. Preferible sobre el manitol en presencia de hipotensión, hipovolemia, lesión renal aguda o necesidad de sostener la presión de perfusión cerebral.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Preparación estándar:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - SS hipertónica al 3% = NaCl 3 g / 100 mL.\n  - Concentración: Na 513 mEq/L, Cl 513 mEq/L. Osmolaridad aproximada: 1026 mOsm/L.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Dosis de inicio (Bolo)", "Bolo general: 2–3 mL/kg IV en 10–20 min. Herniación inminente: 3–5 mL/kg IV en 10–20 min. Bolo estándar adulto rápido: 150–250 mL IV.")
                    BulletText("Infusión continua", "Inicio: 0.1–0.5 mL/kg/h (rango habitual de goteo: 10–50 mL/h). Titular cada 2–4 h según niveles de sodio, Cl e ICP.")
                    BulletText("Dosis máxima", "Bolo máximo operativo: 5 mL/kg. Infusión máxima operativa: 1 mL/kg/h.")
                    BulletText("Límites de seguridad", "Medición de Na sérico cada 4 h si hay infusión o bolos repetidos. Meta habitual de Na: 145–155 mEq/L (evitar >155–160). Mantener cloro <110–115 mEq/L para evitar acidosis metabólica hiperclorémica. En hiponatremia crónica, limitar corrección a <8–10 mEq/L/24h para descartar desmielinización osmótica pontina.")
                }
            }

            "prevencion_secundaria" -> {
                GuideHeader("Prevención Secundaria ACV", "Estrategia Dirigida Según Mecanismo del Evento", "Guías AHA/ASA de Prevención de Ictus", "Actualización con metas intensivas 2026")

                SectionCard("1. Antiagregación Plaquetaria") {
                    BulletText("Doble Antiagregación (DAPT)", "Uso de Aspirina (75-100mg) + Clopidogrel (75mg) por un corto periodo (21 a 90 días dependiendo del estudio clínico base como POINT/CHANCE) para pacientes con ACV isquémico leve (NIHSS ≤3) o TIA de alto riesgo (ABCD² ≥4). Posterior a este plazo, cambiar a monoterapia.")
                    BulletText("Estatinas e Hipercolesterolemia", "Iniciar estatina de alta intensidad (atorvastatina 80 mg o rosuvastatina 20-40 mg). Meta de Colesterol LDL <55 mg/dL (o reducción del 50% si el basal es muy bajo).")
                }

                SectionCard("2. Anticoagulación en Fibrilación Auricular (FA)") {
                    BulletText("Pauta DOACs", "La anticoagulación con inhibidores directos (Apixabán o Rivaroxabán) es preferible sobre la Warfarina tradicional en pacientes no valvulares. Iniciar según regla modificada del día: día 1 (AIT), día 3 (infarto leve), día 6 (infarto moderado) o día 12 (infarto grande) tras excluir transformación hemorrágica.")
                }

                SectionCard("3. Métricas de Cribado Diagnóstico Mínimo") {
                    BulletText("Estudio Mínimo", "Electrocardiograma (ECG), ecocardiograma transtorácico (ETT), ultrasonido Doppler de arterias carótidas y vertebrales o AngioTAC/AngioRM, perfil lipídico, glucosa HbA1c.")
                    BulletText("Estudio Extendido", "Holter ECG de 24h a 7 días en sospecha de ACV criptogénico, Ecopara buscar FOP, cribado para hipercoagulabilidad si es joven sin causa aparente.")
                }
            }

            "tia_minor_stroke" -> {
                GuideHeader("TIA & Minor Stroke", "Abordaje de Ataque Isquémico Transitorio e Ictus Menor", "Consenso AHA/ASA de Reperfusión y Agudos", "Guía 2026")

                SectionCard("1. Score ABCD² de Estimación de Riesgos") {
                    BulletText("Puntaje ABCD²", "Determina el riesgo de ACV recurrente tempranamente. Rango de 0 a 7 puntos. Un puntaje ≥4 denota alto riesgo y requiere ingreso hospitalario prioritario.")
                    BulletText("Criterios de Alto Riesgo", "ABCD² ≥ 4, síntomas fluctuantes, hallazgos de estenosis carotídea ipsilateral de vaso mediano-grande, o lesión isquémica cerebral en la TAC/RM de difusión.")
                }

                SectionCard("2. Indicación de Doble Antiagregación (DAPT)") {
                    BulletText("Fórmula Clave", "Para pacientes con TIA de alto riesgo (ABCD² ≥4) o Ictus leve (NIHSS ≤3) vistos dentro de las 24 horas del inicio: Dar dosis de carga de clopidogrel 300 mg + aspirina 150-325 mg de inmediato, seguidos de Clopidogrel 75mg/día + Aspirina 75-100mg/día por 21 días (conforme estudio POINT) o hasta 90 días si hay estenosis intracraneal focal severa.")
                }
            }

            "hic_reversion" -> {
                GuideHeader("Hemorragia Intracerebral", "Manejo de Emergencia, Metas y Reversión Activa", "AHA/ASA Hemorrhagic Stroke Guidelines", "Vigente 2026")

                SectionCard("1. Tabla Rápida de Reversión por Fármaco") {
                    BulletText("Antagonistas de la Vit. K (Warfarina)", "Dar de inmediato Complejo Protrombínico Concentrado (PCC) de 4 factores a dosis de 25-50 UI/kg para normalizar el INR con rapidez extrema + Fitomenadiona (Vit K) 10 mg IV infundida en 30 min (para evitar rebote). ¡Evitar Plasma Fresco Congelado (FFP) si hay PCC disponible por retraso terapéutico!")
                    BulletText("Nuevos Anticoagulantes (DOACs)", "Dabigatrán: Idarucizumab 5g IV. Apixabán/Rivaroxabán: Andexanet Alfa o PCC de 4 factores 50 UI/kg.")
                    BulletText("Aspirina / Antiagregantes", "No hay recomendación sistémica de transfundir plaquetas a menos que vaya a cirugía descompresiva inmediata.")
                }

                SectionCard("2. Control de Tensión Arterial & Neurocirugía") {
                    BulletText("Meta de PA", "Bajar rápidamente la Presión Arterial Sistólica (PAS) a una meta entre 130 y 140 mmHg mediante infusión IV continua. Reduce sustancialmente la expansión del hematoma.")
                    BulletText("Criterios Neuroquirúrgicos", "Indicación obligatoria de evacuación quirúrgica del hematoma en hemorragias cerebelosas >3 cm de diámetro que demuestran compresión de tallo encefálico u obstrucción ventrículo-cisternal con hidrocefalia.")
                }
            }

            "hsa_manejo" -> {
                GuideHeader("Hemorragia Subaracnoidea", "Intervenciones Críticas de HSA No Traumática", "HSA Consensus / Neurocritical Care Society", "Revisión 2026")

                SectionCard("1. Clasificación Clínica y Radiológica") {
                    BulletText("Hunt-Hess y WFNS", "Clasificaciones de severidad clínica basados en estado neurológico inicial (Hunt-Hess y Escala de la Federación Mundial de Neurocirugía - WFNS). Grados altos (IV-V) van directamente a UCI.")
                    BulletText("Fisher Modificado", "Clasificación de riesgo de vasoespasmo según cantidad de sangre cisternal y presencia de sangre intraventricular. Fisher Modificado 3 y 4 tienen el riesgo más alto.")
                }

                SectionCard("2. Prevención de Vasoespasmo & Edema") {
                    BulletText("Nimodipino por Horarios", "Administrar 60 mg por vía oral (o sonda nasogástrica) cada 4 horas durante los 21 días post-evento. ¡No saltar dosis! Mejora sustancialmente el pronóstico funcional cerebral, reduciendo el infarto isquémico tardío.")
                    BulletText("Manejo de Complicaciones", "Monitoreo por TAC y clínico diario para descartar Hidrocefalia aguda reflectiva por bloqueo de vellosidades. Tratar hipertensión endocraneana con drenaje ventricular externo (DVE).")
                }
            }

            "neurovasc_especiales" -> {
                GuideHeader("Condiciones Especiales Vasculares", "Temas Clínicos Frecuentes de la Práctica Diaria", "Consenso de Expertos de Stroke", "Guía 2026")

                SectionCard("1. Disección Arterial Cervicocefálica (Carótida/Vertebral)") {
                    BulletText("Sospecha", "Paciente joven con dolor cervical/craneal unilateral severo, seguido de síndrome de Horner ipsilateral y déficit focal isquémico.")
                    BulletText("Tratamiento", "Anticoagulación (Heparina/Warfarina) o Antiagregación por 3 a 6 meses. Ambos presentan eficacia similar en estudios de control (Estudio CADISS).")
                }

                SectionCard("2. Trombosis Venosa Cerebral (TVC)") {
                    BulletText("Morfología", "Cefalea progresiva de días, papiledema, crisis convulsivas o focalización motora bilateral.")
                    BulletText("Terapia", "Anticoagulación sistémica completa (Heparina de Bajo Peso Molecular - HBPM) de entrada, INCLUSO si se observa transformación hemorrágica menor en la TAC, seguido de anticoagulantes orales por 3-12 meses.")
                }

                SectionCard("3. Foramen Oval Patente (FOP) & Aneurismas") {
                    BulletText("FOP en Jóvenes", "En ACV criptogénicos de pacientes de 18-60 años, el cierre percutáneo del FOP tiene un beneficio categórico superior vs tratamiento médico solo (estudios RESPECT, CLOSE, NAVIGATE).")
                    BulletText("Aneurismas Incidentales No Rotos", "Manejo conservador (control estricto de PA, dejar de fumar) si el tamaño es <7mm en circulación anterior, vigilancia anual por AngioRM o AngioTAC. Intervención si es gigante o cambia de forma.")
                }
            }

            "dapt_anticoag_aguda" -> {
                GuideHeader("Iniciación de Doble Antiagregación (DAPT) & Anticoagulación Aguda", "Protocolos Clínicos de Empleo y Reversión Bedside", "AHA/ASA Early Stroke & ACCP Standards", "Vigente 2026")

                SectionCard("1. Regla Basal para DAPT") {
                    BulletText("Población objetivo", "ACV isquémico menor no cardioembólico: NIHSS ≤3; o TIA de alto riesgo: ABCD2 ≥4. Inicio idealmente ≤24 h desde el inicio de los síntomas (razonable hasta 72 h en aterosclerosis sintomática severa según guías AHA/ASA 2026).")
                    BulletText("Esquema preferido", "Aspirina + Clopidogrel por un total de 21 días (conforme estudios POINT y CHANCE), seguidos de monoterapia antiplaquetaria a largo plazo. Prolongar la DAPT a 90 días de forma rutinaria aumenta las tasas de sangrado sistémico sin disminuir el riesgo de recurrencia.")
                    BulletText("Ventana extendida (90 días)", "Considerar exclusivamente en estenosis arterial intracraneal sintomática severa (70–99% de luz) con bajo riesgo hemorrágico (siguiendo lógica del estudio SAMMPRIS).")
                    BulletText("Aviso post-lisis", "No iniciar DAPT aguda o antiagregantes si el paciente recibió trombólisis IV activa hasta culminar las 24 h de ventana segura y realizar la neuroimagen de control (para descartar transformación hemorrágica).")
                    BulletText("Alternativa con Ticagrelor", "Aspirina + Ticagrelor por un total de 30 días en ACV con NIHSS ≤5, TIA de muy alto riesgo o estenosis sustancial ≥50%. Especialmente recomendado en resistencia sospechada o genéticamente confirmada a clopidogrel (CYP2C19 loss-of-function).")
                }

                SectionCard("2. Aspirina") {
                    Text("• Carga inicial en agudo:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Bolo general: 162–325 mg VO (o sonda nasogástrica) dosis única rápida.\n" +
                         "  - En esquema THALES (con Ticagrelor): 300–325 mg día 1.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Dosis de mantenimiento:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - 75–100 mg VO diario (frecuente bedside: 81 mg/día tabletas microentéricas).", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("DAPT con Clopidogrel", "Día 1: Aspirina de carga + Clopidogrel de carga. Días 2 a 21: Aspirina 81 mg diario + Clopidogrel 75 mg diario. Día 22 en adelante: suspender clopidogrel; continuar monoterapia diaria.")
                    BulletText("DAPT con Ticagrelor", "Día 1: Aspirina 300–325 mg + Ticagrelor 180 mg. Días 2 a 30: Aspirina 81 mg diario + Ticagrelor 90 mg cada 12 horas.")
                    BulletText("Límites de seguridad", "Evitar bajo hemorragia intracraneal activa, sangrado sistémico severo, trombocitopenia profunda o alergias cruzadas a salicilatos.")
                }

                SectionCard("3. Clopidogrel") {
                    Text("• Carga inicial en agudo:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Esquema CHANCE: 300 mg VO en dosis única.\n  - Esquema POINT: 600 mg VO en dosis única.\n  - Bedside clínico: 300 mg si existe riesgo de sangrado moderado-alto; 600 mg si se prefiere inhibición plaquetaria ultrarrápida.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Dosis de mantenimiento: 75 mg VO diario.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Límites de seguridad", "Suspender de manera mandatoria 5 a 7 días previos a intervenciones quirúrgicas u operaciones de cráneo electivas debido al riesgo de sangrado. Considerar el cambio a Ticagrelor ante sospecha de mala respuesta o portador del alelo no funcional de CYP2C19.")
                }

                SectionCard("4. Ticagrelor") {
                    Text("• Carga inicial en agudo: 180 mg VO dosis única.", style = MaterialTheme.typography.bodySmall)
                    Text("• Dosis de mantenimiento: 90 mg VO cada 12 horas.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Población donde evaluarlo", "ACV isquémico no cardioemólico con NIHSS ≤5. TIA de alto riesgo (ABCD2 ≥6), estenosis intracraneal sintomática relevante ≥50%, o fracaso previo con clopidogrel.")
                    BulletText("Efectos secundarios a vigilar", "Monitorear disnea de esfuerzo (autolimitada por receptor P2Y12), bradicardia transitoria, pausas sinusales o hiperuricemia sintomática. Evitar en hemorragia intracraneal idiopática previa, cirrosis con insuficiencia hepática o coadministración de inductores potentes del CYP3A.")
                }

                SectionCard("5. Heparina No Fraccionada IV") {
                    Text("• Indicación neurológica aguda selecta:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - No utilizar de forma rutinaria en ACV isquémico no cardioembólico común.\n" +
                         "  - Indicado en: Trombosis Venosa Cerebral (TVC) con o sin sangrado; Disección arterial cervical con embolicidad repetida; Válvula protésica mecánica activa o trombo intracardiaco con alto riesgo de émbolos tras medir riesgo-beneficio hemorrágico.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Preparación estándar: 25 000 UI en 250 mL DAD5W/SSN = 100 UI/mL.", style = MaterialTheme.typography.bodySmall)
                    Text("• Laboratorios de base: Recuento plaquetario, TP, basal de TTPa y creatinina.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Dosificación por peso", "Bolo inicial rápido: 80 UI/kg IV. Infusión de inicio continuo: 18 UI/kg/h. Objetivo terapéutico: TTPa 1.5–2.5 veces de valor control (óptimo anti-Xa 0.3–0.7 UI/mL). Chequear control cada 6 h al arrancar o cambiar; pasar a control cada 24 h tras conseguir dos consecutivos terapéuticos.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📋 Nomograma Clásico de Ajuste por Niveles de TTPa:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• TTPa <35 s (<1.2x): Administrar bolo inmediato de 80 UI/kg IV y subir velocidad infusión en +4 UI/kg/h. Control en 6 h.\n" +
                               "• TTPa 35–45 s (1.2–1.5x): Dar bolo inmediato de 40 UI/kg IV y subir infusión en +2 UI/kg/h. Control en 6 h.\n" +
                               "• TTPa 46–70 s (1.5–2.3x): SIN bolo, mantener infusión idéntica. Control en 6 h (o diario si es 2º consecutivo).\n" +
                               "• TTPa 71–90 s (2.3–3.0x): SIN bolo, reducir flujo de infusión en −2 UI/kg/h. Control en 6 h.\n" +
                               "• TTPa >90 s (>3x): Detener infusión por 1 h. Reiniciar con tasa disminuida en −3 UI/kg/h. Control en 6 h.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BulletText("Límites de seguridad", "Evitar bolos si hay un infarto de gran tamaño por riesgo de transformación. Controlar recuento plaquetario cada 24 h del día 4 al 14 por sospecha de trombocitopenia inducida por heparina (HIT). En caso de sangrado activo grave, suspender infusión de inmediato y revertir: Protamina IV a dosis de 1 mg por cada 100 UI de heparina administrada en las últimas 2-3 horas (máx 50 mg).")
                }
            }

            "mcdonald_2024" -> {
                GuideHeader("Criterios McDonald 2024 (Esclerosis Múltiple)", "Última Actualización Internacional de Diagnóstico", "International Panel on Diagnosis of MS (McDonald revised 2025/2026)", "Vigente 2026")

                SectionCard("1. Premisa Diagnóstica Obligatoria") {
                    Text("• Norma Fundamental:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  - Demostración objetiva de al menos un ataque clínico desmielinizante típico con pruebas clínicas o de imagen consistentes.\n" +
                         "  - Descartar estrictamente diagnósticos diferenciales o explicaciones alternativas del cuadro (p. ej. patología isquémica cerebral difusa o migraña atípica con lesiones de sustancia blanca).", style = MaterialTheme.typography.bodySmall)
                }

                SectionCard("2. Diseminación en Espacio (DIS)") {
                    Text("Debe demostrarse afectación en al menos 2 de las siguientes 5 regiones anatómicas del Sistema Nervioso Central (SNC):", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("1. Region Periventricular", "Presencia de al menos 1 lesión típicamente ovoide adyacente al ventrículo lateral.")
                    BulletText("2. Region Cortical o Yuxtacortical", "Lesiones en corteza cerebral (lesión cortical pura observable en secuencias DIR) o inmediatamente adyacentes a la corteza (yuxtacortical).")
                    BulletText("3. Region Infratentorial", "Presencia de lesiones típicas en tronco cerebral o cerebelo.")
                    BulletText("4. Médula Espinal", "Presencia de lesiones medulares desmielinizantes en cualquier plano (cervical o torácica).")
                    BulletText("5. Nervio Óptico (NUEVA 5ª LOCALIZACIÓN)", "Incorporado formalmente en 2024 de manera oficial. Demostrable por Neuritis óptica clínica típica o alteración subclínica mediante: a) Tomografía de Coherencia Óptica (OCT) mostrando adelgazamiento de la capa de fibras nerviosas de la retina o células ganglionares, b) Potenciales Evocados Visuales (PEV) persistentes con retraso de latencia P100, o c) Resonancia Magnética (RM) orbitaria mostrando hiperintensidad T2 o realce con gadolinio del nervio óptico afectado.")
                }

                SectionCard("3. Diseminación en Tiempo (DIT)") {
                    Text("Debe demostrarse mediante cualquiera de las siguientes 4 vías diagnósticas bedside:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    BulletText("Vía 1: Lesiones Simultáneas", "Presencia de lesiones asintomáticas captantes (Gd+) y no captantes de gadolinio en cualquier resonancia funcional de base.")
                    BulletText("Vía 2: Lesión de Seguimiento", "Comprobación de una nueva lesión T2 o captante en comparación con una resonancia magnética basal previa, independientemente del tiempo transcurrido.")
                    BulletText("Vía 3: Bandas Oligoclonales (BOC)", "La confirmación directa de bandas oligoclonales de IgG en líquido cefalorraquídeo (patrones tipo 2 o tipo 3) o índice de IgG elevado puede sustituir plenamente la diseminación temporal clínica o por imagen, requiriendo solo DIS.")
                    BulletText("Vía 4: Novedad Cadenas Kappa (KFLC)", "Presencia demostrada de cadenas ligeras libres kappa (KFLC) elevadas en LCR mediante índice validado de laboratorio es aceptada en consenso 2024 como biomarcador de diseminación cronológica, equivalente a las BOC.")
                }

                SectionCard("4. Unificación de Variantes Clínicas & RIS") {
                    BulletText("Unificación de Fenotipos", "Se reducen las barreras categóricas rígidas entre el Síndrome Clínico Aislado (CIS), la Esclerosis Múltiple Remitente Recurrente (EMRR) y las formas de Esclerosis Múltiple Progresivas (EMP). Todas se asumen como caras evolutivas de una sola entidad biológica espectral.")
                    BulletText("Síndrome Radiológicamente Aislado (RIS)", "Se formaliza el diagnóstico definitivo de Esclerosis Múltiple en un paciente con RIS (hallazgo incidental asintomático en resonancia que cumple criterios de DIS) siempre y cuando se demuestre la positividad concomitante de BOC o KFLC en el análisis de LCR.")
                }

                SectionCard("5. Biomarcadores de Especificidad para Diferencial") {
                    BulletText("Central Vein Sign (CVS)", "Signo de la Vena Central. Altamente indicativo de lesión inflamatoria venulocéntrica típica de EM. Se requiere que ≥40% de las lesiones evaluadas contengan una vena en su eje central para excluir diagnósticos como microangiopatía vascular crónica.")
                    BulletText("Paramagnetic Rim Lesions (PRL)", "Presencia de lesiones con borde paramagnético en secuencias SWI/QSM de resonancia magnética. Indican inflamación persistente desmielinizante subaguda o crónica de bajo grado y son un fuerte biomarcador específico frente a patologías isquémicas ordinarias.")
                }
            }

            "nmosd_mogad" -> {
                GuideHeader("Espectro NMOSD, MOGAD & Diferenciales", "Consenso de Diagnóstico Diferencial Inmunológico", "IPND 2015 Wingerchuk Criteria & MOGAD Diagnostic Consortium", "Vigente 2026")

                SectionCard("1. Seis Características Clínicas Núcleo (NMOSD)") {
                    Text("Constituyen las manifestaciones clínicas definitorias del espectro NMOSD:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("1. Neuritis Óptica", "Frecuentemente bilateral, severamente dolorosa, destructiva, con mala recuperación visual y pérdida casi completa inicial.")
                    BulletText("2. Mielitis Aguda", "Mielitis transversa longitudinalmente extensa (LETM) típica que abarca ≥3 segmentos vertebrales adyacentes continuos.")
                    BulletText("3. Síndrome del Área Postrema", "Episodios de hipo constante incoercible o vómitos de novo incoercibles, intratables por fármacos, de al menos 48 horas de evolución.")
                    BulletText("4. Síndrome de Tronco Encefálico Agudo", "Presentación con oftalmoplejía intermitente, vestibulopatía, diplopía o parálisis facial periférica unilateral.")
                    BulletText("5. Síndrome Diencefálico Agudo", "Episodios agudos de narcolepsia, letargia profunda o hipotermia central por lesiones típicamente localizadas en el hipotálamo.")
                    BulletText("6. Síndrome Cerebral Agudo", "Crisis hemisféricas acompañadas de lesiones de sustancia blanca subcortical gigante difusa.")
                }

                SectionCard("2. Algoritmo de Diagnóstico de NMOSD (Wingerchuk 2015)") {
                    Text("• Estatus AQP4-IgG Seropositivo:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                    Text("  - Requiere: Al menos una manifestación clínica núcleo + prueba Cell-Based Assay (CBA) positiva para anticuerpos Acuaporina 4 IgG en suero.\n  - Excluir con rigor diagnósticos alternos.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Estatus AQP4-IgG Seronegativo o Desconocido:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    Text("  - Requiere: Al menos dos manifestaciones clínicas núcleo diferentes resultantes de uno o varios ataques, cumpliendo:\n" +
                         "    a) Al menos una de las dos manifestaciones núcleo debe ser Neuritis óptica típica, Mielitis transversa longitudinalmente extensa (LETM) o Síndrome del área postrema.\n" +
                         "    b) Diseminación en espacio demostrada clínicamente o por RM típica.\n" +
                         "    c) Hallazgos de Resonancia que validen las sospechas de seronegatividad (médula espinal con LETM, RM cerebral con patrones específicos, etc.).", style = MaterialTheme.typography.bodySmall)
                }

                SectionCard("3. Banderas Rojas que cuestionan NMOSD") {
                    Text("Suspender presunción de NMOSD y sospechar otra patología (p. ej. EM) ante:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("RM Cerebral Típica", "Presencia de Dedos de Dawson perpendiculares a ventrículos, lesiones ovoideas periventriculares puras.")
                    BulletText("Curso Progresivo", "Deterioro neurológico progresivo persistente en ausencia de recaídas clínicas agudas.")
                    BulletText("Bandas Oligoclonales de LCR", "Presencia de BOC positivas en LCR de forma persistente (raras en NMOSD, <15–20% de prevalencia).")
                }

                SectionCard("4. Criterios para Enfermedad por Anti-MOG (MOGAD)") {
                    Text("Diferenciación crucial frente a EM y NMOSD clásico:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Síndromes Núcleo de MOGAD", "Neuritis óptica puramente anterior bilateral con edema papilar severo y marcado, mielitis transversa que compromete predominantemente el cono medular terminal, encefalomielitis diseminada aguda (ADEM), o encefalitis cortical unilateral con convulsiones características (Fenotipo FLAMES).")
                    BulletText("Criterio de Inmunodiagnóstico", "Requiere positividad analítica estricta de anticuerpos séricos contra IgG anti-MOG por método de Cell-Based Assay (CBA) cuantitativo. Desestimar de entrada pruebas de anticuerpos en LCR exclusivo, anticuerpos IgM / IgA o metodologías de inmunoensayo tipo ELISA debido a elevadas tasas de error y falsos positivos.")
                }
            }

            "encefalitis_autoinmune" -> {
                GuideHeader("Encefalitis Autoinmune (Criterios Graus 2016)", "Protocolo de Sospecha Diagnóstica y Evidencia Autoinmune", "Graus Guidelines for Autoimmune Encephalitis", "Vigente 2026")

                SectionCard("1. Criterios de Encefalitis Autoinmune Posible") {
                    Text("Permiten iniciar el abordaje clínico; sospechar fuertemente ante:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Criterio Clínico A", "Deterioro rápido de la memoria de trabajo reciente, alteración evidente del estado mental o síntomas psiquiátricos de novo (psicosis, catatonia, agitación profunda) en un periodo menor a 3 semanas.")
                    BulletText("Criterio Excluyente B", "Exclusión sistemática de causas metabólicas locales, infecciones activas del LCR o tumores cerebrales evidentes.")
                    BulletText("Criterio Biológico C", "Al menos uno de los siguientes hallazgos complementarios:\n  - Pleocitosis linfocitaria leve en líquido cefalorraquídeo (>5 células blancas/mm³).\n  - Resonancia que demuestre hiperintensidad bilateral T2/FLAIR en lóbulos temporales mediales.\n  - EEG con ondas delta-theta localizadas o descargas epilépticas focales temporales de novo.")
                }

                SectionCard("2. Casos Diagnósticos de Definición por Consenso") {
                    BulletText("Encefalitis Límbica Autoimmune Definitiva", "Diagnóstico inmediato que se establece al cumplir criterios de Encefalitis Posible, conjuntamente con una resonancia cerebral que demuestra de forma patognomónica hiperintensidad bilateral T2/FLAIR confinada estrictamente a lóbulos temporales mediales basales bilaterales, sin requerir esperar por los resultados de anticuerpos.")
                    BulletText("Encefalitis Autoinmune Probable Seronegativa", "Cuadro consistente con encefalitis posible, afectación psiconeurológica multiorgánica rápida y asimétrica, exclusión estricta de neuroinfección y ausencia comprobada de autoanticuerpos en paneles de LCR/sangre.")
                    BulletText("Encefalitis anti-NMDAR Definitiva", "Requiere la demostración de anticuerpos específicos IgG anti-GluN1 en líquido cefalorraquídeo purificado. Presentación clínica habitual en mujeres jóvenes: cuadro prodrómico seudogripal, psicosis severa y catatonia de novo, movimientos coreoatetósicos u orofaciales severos, labilidad autonómica de presión arterial/taquiarritmias e hipoventilación central.")
                }

                SectionCard("3. Perfil de Anticuerpos: Superficie vs Intracelulares") {
                    Text("• Anticuerpos de Superficie Sináptica:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                    Text("  - Ejemplos: anti-NMDAR, anti-LGI1 (causa crisis distónicas faciobraquiales y convulsiones), anti-CASPR2 (neuromiotonía e insomnio), anti-AMPAR, anti-GABAbR.\n  - Características: Excelente pronóstico funcional. La patología es puramente mediada por bloqueo soluble o internalización del receptor; responden de forma sobresaliente a inmunoterapia (Metilprednisolona, IVIG, Plasmaféresis y anticuerpos anti-CD20).", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Anticuerpos Intracelulares u Onconeronales clásicos:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    Text("  - Ejemplos: anti-Hu (causa encefalomielitis), anti-Yo (degeneración cerebelosa), anti-Ri, anti-Ma2, anti-Anfofisina.\n  - Características: Elevada asociación paraneoplásica (>90%). La respuesta efectora es mediada por Linfocitos T citotóxicos destruyendo neuronas de forma irreversible. Pobre respuesta a la inmunoterapia convencional directa, requiere de forma mandatoria la detección y remoción precoz del tumor primario subyacente.", style = MaterialTheme.typography.bodySmall)
                }

                SectionCard("4. Panel Mínimo Bedside ante Sospecha Clínica") {
                    Text("Esquema estricto de diagnóstico estructurado al pie de la cama:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Paso 1: Resonancia de Alta Resolución", "Secuencias FLAIR y coronal de T2 para delimitar focos temporolímbicos orientativos.")
                    BulletText("Paso 2: Punción Lumbar de Exclusión", "Chequeo biológico inicial y envío concomitante de PCR FilmArray multiplex para bacterias, virus y levaduras, de este modo excluimos neuroinfección antes de iniciar inmunosupresión.")
                    BulletText("Paso 3: Electroencefalograma (EEG)", "Detección de estatus epiléptico no convulsivo o patrón específico de 'Extreme Delta Brush' (altamente correlacionado con encefalitis anti-NMDAR).")
                    BulletText("Paso 4: Biomarcador Inmunológico directo", "Envío simultáneo de muestra de suero y LCR para panel de encefalitis autoinmune (CBA en LCR de preferencia por sensibilidad superior).")
                    BulletText("Paso 5: Tamizaje Oncológico Mandatorio", "TAC de tórax, abdomen y pelvis con doble contraste, ultrasonografía pélvica/transvaginal (pesquisa de teratoma ovárico asociado a anti-NMDAR) o PET-CT corporal total.")
                }
            }

            "terapias_dmt_seguridad" -> {
                GuideHeader("Inmunomantenimiento e Inmunoseguridad", "Protocolo Estricto de Seguridad de Terapias Modificadoras", "DMT Safety Board Guidelines", "Vigente 2026")

                SectionCard("1. Protocolos de Tamizaje Previo al Inicio") {
                    BulletText("Enfermedades Infecciosas", "Obligatorio realizar: Ensayo de liberación de interferón gamma (IGRA) o PPD para Tuberculosis, serologías para Hepatitis B (HBsAg, anti-HBc IgM/IgG), Hepatitis C, VIH y virus de la Varicela Zóster (VZV) (si es IgG- VZV negativo, aplazar el inicio para vacunar).")
                    BulletText("Vacunas", "Completar vacunas inactivadas o atenuadas al menos 4 semanas antes de iniciar anticuerpos monoclonales de depleción linfocitaria (Rituximab, Ocrelizumab).")
                }

                SectionCard("2. Alertas de Monitoreo Específico") {
                    BulletText("Natalizumab & JCV", "Riesgo crítico de Leucoencefalopatía Multifocal Progresiva (LMP) inducida por virus JC. Medir estatus del anticuerpo anti-JCV cada 6 meses. Suspender fármaco o prolongar infusiones (intervalo extendido cada 6 semanas) si el índice JCV supera >1.5.")
                    BulletText("Monitoreo de Linfocitos", "Linfopenia persistente <500 céls/µL (Grado 3/4) por Fingolimod o Dimetifumarato obliga a suspensión temporal o definitiva por el riesgo inminente de infecciones oportunistas graves.")
                }
            }

            "primera_crisis" -> {
                GuideHeader("Primera Crisis Convulsiva", "Valoración Inicial de Evento Único no Provocado", "ILAE Epilepsy Treatment Group", "Edición 2026")

                SectionCard("1. Riesgo de Recurrencia y Tratamiento") {
                    BulletText("Tasa General de Recurrencia", "Aproximadamente 30-40% en los siguientes 2 años tras una primera crisis no provocada.")
                    BulletText("Cuándo iniciar Tratamiento (ASM)", "Iniciar medicación de inmediato si el riesgo estimado supera >60% (lo cual equivale a un diagnóstico formal de Epilepsia). Esto sucede si la RM demuestra lesión estructural epileptógena obvia, el EEG evidencia descargas epileptiformes claras, o si se asocia a un déficit motor previo por ACV o trauma craneal.")
                }

                SectionCard("2. Regulaciones Diarias del Paciente") {
                    BulletText("Conducción de Vehículos", "Restricción estricta de conducir vehículos o maquinaria pesada durante un mínimo de 6 meses (o un año dependiendo de regulaciones civiles locales) libre de episodios convulsivos.")
                    BulletText("Higiene del Sueño", "Insistir en evitar privaciones de sueño deliberadas, consumo de bebidas de alta concentración energizantes o alcohol, situaciones de natación no supervisada o deportes extremos en solitario.")
                }
            }

            "seleccion_asm" -> {
                GuideHeader("Selección Inteligente de Fármacos", "Prescripción de Fármacos Anticrisis por Perfil", "Consenso Clínico ILAE", "Vigente 2026")

                SectionCard("1. Restricciones e Indicaciones Específicas") {
                    BulletText("Mujeres de Edad Fértil / Embarazo", "¡Evitar Ácido Valproico! Riesgo de teratogenicidad fetal masiva y retraso del neurodesarrollo. Preferir de entrada Lamotrigina o Levetiracetam (fármacos con mejores perfiles de seguridad obstétrica).")
                    BulletText("Perfil Psiquiátrico / Depresión", "Evitar Levetiracetam debido a su incidencia reportada de exacerbar de novo la labilidad afectiva, agresión e ideación depresiva. Preferir Lamotrigina o Valproato.")
                    BulletText("Obesidad vs Migraña", "Topiramato o Zonisamida actúan de forma favorable induciendo pérdida ponderal y coadyuvando en la profilaxis de migraña asociada. Evitar Valproato.")
                }

                SectionCard("2. Ajustes en Falla Hepática y Renal") {
                    BulletText("Falla Renal", "Requieren reducción proactiva de dosis: Levetiracetam (depura renal puro), Gabapentina, Pregabalina y Lacosamida.")
                    BulletText("Falla Hepática", "Evitar Valproato, Fenolbarbital, Carbamazepina. Fármacos sin apenas paso hepático: Levetiracetam.")
                }
            }

            "monitoreo_toxicidad" -> {
                GuideHeader("Monitoreo & Toxicología", "Rangos Clínicos de Control y Reacciones Adversas", "Epilepsy Therapeutics Practice", "Guía 2026")

                SectionCard("1. Monitoreo de Niveles Plasmáticos") {
                    BulletText("Fenitoína", "Rango normal: 10 y 20 µg/mL. Toxicidad cerebelosa clara por enzimas saturadas (cinética de orden cero) con ataxia, nistagmus y sedación si supera >20 µg/mL.")
                    BulletText("Ácido Valproico", "Rango normal: 50 y 100 µg/mL. Riesgo de toxicidad mitocondrial con hiperamonemia secundaria, manifestada por encefalopatía aguda con niveles normales de transaminasas. Tratar con L-carnitina.")
                }

                SectionCard("2. Farmacogenómica & Alertas SJS") {
                    BulletText("Raza / HLA-B*1502", "Pacientes de linaje asiático (Han) presentan riesgo extremo corregido de sufrir Síndrome de Stevens-Johnson (SJS) potencialmente fatal inducido por Carbamazepina si portan el alelo HLA-B*1502. Realizar cribaje preventivo antes de prescribir.")
                }
            }

            "refractariedad_sudep" -> {
                GuideHeader("Epilepsia Farmacorresistente & SUDEP", "Protocolos de Referencia Oportuna e Intervenciones", "Consenso de Expertos ILAE", "Vigente 2026")

                SectionCard("1. Epilepsia Farmacorresistente") {
                    BulletText("Definición", "Persistencia de crisis epilépticas a pesar del ensayo adecuado y bien tolerado de 2 regímenes de Fármacos Anticrisis (ASMs) elegidos de manera inteligente y dosificados a metas terapéuticas correspondientes.")
                    BulletText("Siguiente Paso", "Obligatorio referir inmediatamente a una Unidad Terciaria de Epilepsia para realización de Video-EEG prolongado de fase diagnóstica, RM con protocolo Hard-Epilepsy de 3 Teslas para mapear posible displasia cortical focal, y valorar cirugía curativa, estimulación vagal (VNS) u opción de dieta cetogénica.")
                }

                SectionCard("2. Qué es el SUDEP y cómo mitigarlo") {
                    BulletText("Definición de SUDEP", "Muerte súbita inesperada y no traumática en pacientes con epilepsia, comúnmente asociada a arritmias de origen central o apnea obstructiva transitoria post-ictal.")
                    BulletText("Estrategias de Mitigación", "Priorizar de manera absoluta el control farmacológico estricto de las crisis nocturnas motoras tónico-clónicas generalizadas (el factor de riesgo modificable más poderoso), el uso de almohadas de aireación lateral, sistemas domésticos de alerta de convulsiones de cama y educación explícita familiar.")
                }
            }

            "diagnostico_cognitivo" -> {
                GuideHeader("MCI vs Demencia vs Delirium", "Diagnóstico Diferencial del Deterioro Cognoscitivo", "Guía de Práctica Clínica en Demencias", "Actualización 2026")

                SectionCard("1. Árbol de Diferenciación Bedside") {
                    BulletText("MCI (Deterioro Cognitivo Leve)", "Queja subjetiva u objetiva de pérdida de memoria, pero preservación plena y demostrada de la independencia funcional de la vida cotidiana del paciente. No cumple criterios de demencia.")
                    BulletText("Demencia (Trastorno Neurocognitivo Mayor)", "Déficit cognitivo persistente y progresivo que interfiere de forma categórica e insalvable con las actividades instrumentales de la vida diaria.")
                    BulletText("Delirium (Síndrome Confusional Agudo)", "Fluctuante en curso clínico (por horas), inicio sumamente agudo (horas a pocos días), con alteración del nivel atencional y de alerta. Generalmente secundario a una noxa sistémica tratable (ITU, desbalance iónico, toxicidad farmacológica).")
                }
            }

            "biomarcadores_alzheimer" -> {
                GuideHeader("Biomarcadores de Alzheimer", "Interpretación Biológica de la Progresión de la Enfermedad", "Criterios Biológicos de Alzheimer de la AA", "Vigente 2026")

                SectionCard("1. Clasificación ATN y Conceptos de Vanguardia") {
                    BulletText("Cambio de Paradigma", "La Asociación de Alzheimer y la NIA definen hoy en día la Enfermedad de Alzheimer como un diagnóstico enteramente biológico (no sindrómico).")
                    BulletText("Muestras Biológicas (LCR o Plasma Avanzado)", "Medir Aβ42/40 ratio (A: Amiloide, disminuido en LCR / positivo en plasma indica agregación vascular de placas), p-tau181 o p-tau217 (T: Ovillos neurofibrilares de Tau, aumentados consistentemente), y NfL (N: Neurofilamento ligero, como biomarcador inespecífico de lesión y daño axonal en fase de progresión activa).")
                }
            }

            "terapias_antiamiloide" -> {
                GuideHeader("Inmunoterapias Antiamiloide", "Monitorización en Terapias con Anticuerpos Monoclonales", "FDA/EMA Safety Protocols for Lecanemab/Donanemab", "Vigente 2026")

                SectionCard("1. Selección del Candidato Ideal") {
                    BulletText("Idoneidad", "Solo indicados en fases tempranas: Deterioro Cognitivo Leve (MCI) por Alzheimer o demencia en estadio muy leve. Deben poseer confirmación categórica previa de placas amiloides por LCR o PET cerebral.")
                    BulletText("Exclusiones Críticas", "Contraindicado en pacientes bajo terapia de anticoagulación crónica con DOACs o Warfarin (riesgo inaceptablemente alto de hemorragia cerebral fatal).")
                }

                SectionCard("2. Vigilancia Activa de ARIA (Habilidades Críticas)") {
                    BulletText("¿Qué es ARIA?", "Efectos adversos directos de las inmunoterapias amiloides caracterizados en resonancias magnéticas.")
                    BulletText("ARIA-E (Vasogenic Edema)", "Edema o efusión leptomeníngea. Comúnmente asintomático, pero puede dar cefalea persistente o confusión. Detener infusión de inmunomantenimiento de inmediato.")
                    BulletText("ARIA-H (Microhemorrhage)", "Microangiopatías o depósitos secundarios de hemosiderosis. Obligatorio realizar resonancias en intervalos fijos a las semanas 2, 4, 12, 24 y ante cualquier síntoma neurológico nuevo.")
                }
            }

            "demencias_no_alzheimer" -> {
                GuideHeader("Demencias No Alzheimer", "Principales Causa de Deterioro de Presentación Distinta", "Consenso Internacional de Síndromes Demenciales", "Guía 2026")

                SectionCard("1. Demencia de Cuerpos de Lewy (DLB)") {
                    BulletText("Tríada Cardinal", "Fluctuaciones cognitivas prominentes, alucinaciones visuales sumamente detalladas y recurrentes, y datos de parkinsonismo de inicio rápido o asimétrico. Altísima sensibilidad de reacciones adversas extrapiramidales inducidas por antipsicóticos típicos.")
                }

                SectionCard("2. FTD (Demencia Lobar Frontotemporal)") {
                    BulletText("Variante Conductual (bvFTD)", "Marcada apatía progresiva o desinhibición conductual obvia, pérdida del juicio social temprano, hiperoralidad y comportamientos ritualistas repetitivos con preservación de la memoria en fases iniciales. Atrofia marcada lobar frontal/temporal bilateral por RM.")
                }
            }

            "parkinson_avanzado" -> {
                GuideHeader("Parkinson Avanzado & Terapias DBS", "Clasificación y Criterios de Selección Quirúrgicas", "MDS Consensus Guidelines on PD Guidelines", "Edición 2026")

                SectionCard("1. Fluctuaciones Motoras Complejas") {
                    BulletText("Wearing-Off", "Pérdida rápida del efecto terapéutico de cada dosis administrada de Levodopa a medida que progresa la enfermedad, reduciendo la ventana de confort.")
                    BulletText("Freezing / Alucinaciones", "Bloqueos intermitentes de la marcha, episodios psicóticos tardíos provocados por receptores dopaminérgicos hipersensibles (abordar preferentemente con Pimavanserina o Clozapina/Quetiapina para no exacerbar la rigidez).")
                }

                SectionCard("2. Criterios de Selección para DBS (Estimulación Profunda)") {
                    Text("Debe evaluarse en pacientes con respuestas altamente demandantes a levodopa que satisfacen:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletText("Prueba de Desafío Levodopa", "Mostrarse responsivo a levodopa con mejoría motora ≥30% en la escala de UPDRS lll tras una dosis controlada.")
                    BulletText("Exclusiones absolutas para DBS", "Presencia de demencia clínica establecida por pruebas neuropsicológicas avanzadas, depresión refractaria activa con ideación suicida, o parkinsonismo atípico obvio.")
                }
            }

            "toxina_botulinica_avanzada" -> {
                GuideHeader("Toxina Botulínica Avanzada", "Rangos Fijos de Dosificación por Patología Bedside", "Estándares Clínicos de Aplicación de Toxina", "Guía 2026")

                SectionCard("1. Guía Rápida de Dosis por Patología (Unidades Botox)") {
                    BulletText("Distonía Cervical", "Dosis entre 100 y 300 Unidades totales. Inyecciones focales en los músculos afectados: Esternocleidomastoideo (rotación contralateral, inyectar dosis bajas de 25-50U por riesgo de disfagia), Esplenio de la cabeza (extensión o ipsilateral), Trapecio, elevador de la escápula.")
                    BulletText("Espasticidad de Miembro Superior", "Rango de 200 a 400 Unidades totales repartidas en los complejos flexores principales: Bíceps braquial (50-100U), Flexor profundo/superficial de dedos (25-50U cada uno), Pronador redondo.")
                    BulletText("Blefaroespasmo / Espasmo Hemifacial", "Dosis bajas de 15 a 50 Unidades totales por hemicara inyectados en el músculo orbicular de los párpados en cuadrantes lateral, medial y ceja.")
                    BulletText("Sialorrea Crítica", "Dosis de 50 a 100 Unidades totales repartidas de forma ecodirigida en las glándulas parótidas (repartir dosis 60%) y submandibulares (repartir dosis 40%).")
                }
            }

            "neuro_oftalmo_otologia" -> {
                GuideHeader("Neuro-Oftalmología & Otología", "Diagnósticos Diferenciales Bedside de Alto Impacto", "Consenso Internacional de Síndromes Críticos", "Guía 2026")

                SectionCard("1. Parálisis de Pares Craneanos & Diplopia") {
                    BulletText("III Par (Motor Ocular Común)", "Ojo desviado 'abajo y afuera'. Sospecha crítica: Si presenta involucro pupilar (pupila midriática refractaria con reflejo abolido), representa una emergencia vital inminente. ¡Frecuentemente secundario a compresión aneurismática de la arteria comunicante posterior (PCoA)! Requiere AngioTAC urgente.")
                }

                SectionCard("2. Pérdida Visual e Isquemia Retinal") {
                    BulletText("Arteritis de Células Gigantes (ACG)", "Cefalea temporal de novo, claudicación mandibular al masticar, pérdida visual monocular súbita e irreversible. Solicitar VSG/PCR elevadas, biopsia temporal y terapia inmediata con Metilprednisolona 1g IV diario por 3-5 días para salvar el ojo contralateral.")
                }

                SectionCard("3. Vértigo Agudo & Algoritmo HINTS") {
                    BulletText("HINTS Examination", "(Head Impulse, Nystagmus, Test of Skew). Un infarto cerebeloso o de fosa posterior puede imitar un vértigo periférico agudo. HINTS detecta origen central con mayor sensibilidad diagnóstica que la RM en las primeras 24 horas.")
                    BulletText("HINTS+ Central", "Incluye: 1. Preservación del Head Impulse (test normal es malo en agudo), 2. Nistagmus bidireccional que cambia de fase con la mirada, 3. Test de Skew con desviación vertical, o 4. Pérdida unilateral auditiva aguda nueva. Enviar a TAC/RM urgente y valorar trombólisis.")
                }
            }

            "muerte_encefalica" -> {
                MuerteEncefalicaProtocolView()
            }

            "neuro_otologia_urgente" -> {
                NeuroOtologiaUrgenteView()
            }

            "calculadoras_vasculares" -> {
                CalculadorasVascularesView(onNavigateToDrug = onNavigateToDrug)
            }

            else -> {
                Text("Error: Protocolo no encontrado.")
            }
        }
    }
}

@Composable
fun GuideHeader(
    title: String,
    subtitle: String,
    source: String,
    updateInfo: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fuente: $source", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f))
                Text(updateInfo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun BulletText(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("• ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}

@Composable
fun CalculatedDoseItem(title: String, doseText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(doseText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TimePhaseIndicator(time: String, title: String, decription: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(time, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(decription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
    }
}


// ==========================================
// COMPONENT 3: ORDERS & NOTES GENERATOR (Stateful & Editable Template Builder)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersAndNotesGenerator() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val syndromes = listOf(
        SyndromeTemplate(
            id = "acv_agudo",
            title = "Código ACV Isquémico Agudo",
            diagnostic = "Ictus Isquémico Agudo (ACV Isquémico Agudo) en Ventana Terapéutica / NIHSS inicial.",
            orders = "1. Dieta: Ayuno absoluto por 24h hasta realizar screening formal de disfagia bedside.\n" +
                     "2. Soluciones: Solución Salina Normal al 0.9% 1000 cc IV para pasar a razón de 80 cc/h (Evitar soluciones hipotónicas o glucosadas).\n" +
                     "3. Medicamentos: \n" +
                     "   - Tenecteplasa (TNK) 0.25 mg/kg IV en bolo único rápido de 5-10 segundos (Máx 25 mg).\n" +
                     "   - Labetalol 10 mg IV en bolo lento si la TA es >185/110 mmHg para habilitar lisis.\n" +
                     "4. Monitoreo: \n" +
                     "   - Control estricto de PA y examen neurológico cada 15 min por 2 horas, cada 30 min por 6 horas y posterior cada hora.\n" +
                     "   - Alerta roja e interrupción de infusión si hay sospecha de transformación hemorrágica cerebral aguda.\n" +
                     "5. Laboratorios & Gabinete: TAC de Cráneo simple y AngioTAC de Vasos de Cuello / Intracraneales de urgencia, BH, QS, Tiempos de Coagulacion (TP/TPT).",
            justification = "Se decide abordaje trombolítico bedside según guías líderes de la AHA/ASA 2026. Paciente se presenta dentro de ventana terapéutica menor de 4.5 horas y no presenta contraindicación absoluta. El uso de Tenecteplasa favorece porcentajes más altos de recanalización que Alteplasa.",
            plan = "Efectuar TAC control a las 24 horas previo a inicio de antiagregantes plaquetarios orales. Evaluar indicación urgente de Trombectomía Mecánica si de forma secundaria el AngioTAC demuestra oclusión proximal (LVO)."
        ),
        SyndromeTemplate(
            id = "status_epileptico",
            title = "Estado Epiléptico Generalizado Convulsivo",
            diagnostic = "Estado Epiléptico Convulsivo Agudo en Fase Establecida / Refractaria.",
            orders = "1. Medidas ABC: Mantener vía aérea permeable con O2 suplementario a flujo alto por cánula. Monitoreo cardiorrespiratorio continuo.\n" +
                     "2. Medicamentos de Emergencia: \n" +
                     "   - Lorazepam 4 mg IV directo a velocidad lenta. Repetir dosis inicial en 5 minutos si persisten crisis generalizadas.\n" +
                     "   - Levetiracetam 3000 mg IV diluidos en 100 cc de SS 0.9% para infundir en 15 minutos (Dosis 60 mg/kg).\n" +
                     "   - Si hipoglucemia capilar inicial detectada (<60 mg/dL): Pasar 100mg Tiamina IV + un frasco de Glucosa al 50% IV de inmediato.\n" +
                     "3. Soluciones: Solución Fisiológica al 0.9% 1000 cc IV a goteo continuo de 100 cc/h.\n" +
                     "4. Estudios: Toxicología de orina rápida, niveles séricos basales de anticonvulsivos convencionales, gasometría arterial con niveles de ácido láctico y pH, química sanguínea completa.",
            justification = "Paciente presenta crisis convulsivas tónico-clónicas generalizadas activas de duración prolongada mayor de 5 minutos, cumpliendo criterio de Estado Epiléptico (Algoritmo AES). Se instaura de inmediato protocolo secuencial para evitar muerte neuronal.",
            plan = "Preparar y trasladar de manera inmediata a la Unidad de Cuidados Intensivos si presenta persistencia clínica transcurridos más de 30-40 minutos (refractariedad) para instaurar intubación e infusión anestésica."
        ),
        SyndromeTemplate(
            id = "meningitis",
            title = "Sospecha de Meningitis / Encefalitis Bacteriana",
            diagnostic = "Síndrome Meníngeo Agudo con Sospecha de Meningitis Bacteriana / Encefalitis Aguda.",
            orders = "1. Medicamentos Primordiales:\n" +
                     "   - Dexametasona 10 mg IV de forma rápida (Debe administrarse inmediatamente antes o simultáneamente con la primera dosis de antibiótico).\n" +
                     "   - Ceftriaxona 2 g IV cada 12 horas.\n" +
                     "   - Vancomicina 1 g IV cada 12 horas (Dosis ajustada por peso a 15-20 mg/kg).\n" +
                     "   - Si paciente posee edad >50 años o sospecha celular: Agregar Ampicilina 2g IV cada 4 horas para cobertura focal de Listeria.\n" +
                     "2. Estudios Bedside: Punción Lumbar de urgencia posterior a corroborar seguridad neurológica (No papiledema, no déficit focal). Solicitar citoquímico de LCR, tinción Gram directa, cultivo y PCR multiplex (Panel filmatray).\n" +
                     "3. Medios: Aislamiento por gotas estricto hasta cumplir un mínimo de 24 horas de terapia antibiótica activa confiable.\n" +
                     "4. Soluciones: Solución Salina al 0.9% 1000 cc IV para pasar en 24 horas.",
            justification = "Se sospecha infección del SNC por cuadro de rigidez de nuca, fiebre alta y alteración de alerta. Se instaura terapia con Dexametasona y antibióticos empíricos de forma rápida según Guías ESCMID/IDSA para abatir mortalidad por S. pneumoniae.",
            plan = "Interpretar de forma urgente parámetros del LCR obtenidos y revisar si amerita ajuste bacteriológico de antibióticos por patógeno aislado."
        ),
        SyndromeTemplate(
            id = "crisis_miastenica",
            title = "Crisis Miasténica / Guillain-Barré Crítico",
            diagnostic = "Falla Respiratoria Inminente Secundaria a Crisis Miasténica / Síndrome de Guillain-Barré.",
            orders = "1. Dieta: Ayuno preventivo estricto (alto riesgo asociado de broncoaspiración por debilidad faríngea).\n" +
                     "2. Monitoreo Respiratorio: \n" +
                     "   - Espirometría portátil o medición de fuerza muscular por terapia respiratoria cada 4 horas (FVC, NIF/MIP, MEP).\n" +
                     "   - Guardar monitoreo estricto de saturación y pulso continuo. Alerta de intubación si la FVC es <20 mL/kg o la NIF < -30 cmH2O.\n" +
                     "3. Tratamiento Primario: \n" +
                     "   - Omitir o diferir uso de Bromuro de Piridostigmina temporalmente (reduce sialorrea y secreciones obstructivas durante intubación).\n" +
                     "   - Iniciar Inmunoglobulina Humana IV (IVIG) a dosis de 0.4 g/kg/día por 5 días, o programar 5 recambios de Plasmaféresis.\n" +
                     "4. Contraindicados: Prohibición absoluta de prescribir Aminoglucósidos, Fluoroquinolonas, Macrólidos o beta-bloqueadores en indicaciones de planta.",
            justification = "Paciente presenta cuadriparesia flácida o parálisis bulbar progresiva de curso acelerado con disfunción de deglución y fatiga respiratoria accesoria que amerita vigilancia intensiva y preparación para terapia de inmunomodulación.",
            plan = "Valoración y manejo en cama de UCI. Fisioterapia pulmonar intensiva."
        ),
        SyndromeTemplate(
            id = "primera_crisis_convulsiva",
            title = "Primera Crisis Convulsiva no Provocada",
            diagnostic = "Primera crisis convulsiva de inicio reciente, tónico-clónica generalizada, de presumible origen no provocado.",
            orders = "1. Reposo y control en cama con barandales de seguridad arriba.\n" +
                     "2. Canalizar y resguardar vía periférica salinizada preventiva.\n" +
                     "3. Medicamento de Planta (si se detecta alto riesgo de recurrencia mediante EEG o RM): Levetiracetam 500 mg vía oral cada 12 horas.\n" +
                     "4. Estudios de Imagen & Gabinete: Programar Electroencefalograma (EEG) ambulatorio con privación de sueño parcial y Resonancia Magnética (RM) con protocolo de epilepsia en imán de 1.5 o 3 Tesla.\n" +
                     "5. Laboratorios: Química sanguínea completa de 12 elementos, examen general de orina, panel iónico con niveles de Calcio y Magnesio séricos.",
            justification = "Paciente acude tras presentar primer evento de descarga de reciente aparición. Se requiere descartar etiología estructural cerebral identificable mediante neuroimagen dedicada y evaluar focos paroxísticos a través del EEG.",
            plan = "Egresar de forma ambulatoria con medidas rigurosas restrictivas de seguridad física: Restricción absoluta para conducción de vehículos por al menos 6 meses, no natación libre de supervisión activa y adecuada higiene de horas de sueño."
        ),
        SyndromeTemplate(
            id = "deterioro_cognitivo",
            title = "Deterioro Cognitivo de Reciente Inicio",
            diagnostic = "Trastorno Neurocognitivo Generalizado a Estudiar formalmente.",
            orders = "1. Programar valoración clínica formal de funciones y batería neuropsicológica detallada bedside (MOCA o MMSE).\n" +
                     "2. Solicitar Panel de Laboratorio Demencial: Niveles de Hormona estimulante de Tiroides (TSH) sérico, cuantificación de Vitamina B12 y Ácido Fólico, Serología VDRL / Sífilis en suero, Química sanguínea completa y electrólitos séricos.\n" +
                     "3. Imagen: Resonancia de Cráneo funcional con énfasis estructural de atrofia coronal en hipocampos.",
            justification = "Estudio integral de queja cognitiva progresiva en paciente mayor. Se requiere descartar causas potencialmente médicas o secundarias reversibles tales como hipotiroidismo primario severo o déficit agudo de Vitamina B12.",
            plan = "Interpretar resultados analíticos y valorar inicio de Inhibidores de la Acetilcolinesterasa (Donepezil o Galantamina) si se confirma diagnóstico etiológico."
        )
    )

    var selectedIndex by remember { mutableStateOf(0) }
    var userDiagnostic by remember { mutableStateOf(syndromes[0].diagnostic) }
    var userOrders by remember { mutableStateOf(syndromes[0].orders) }
    var userJustification by remember { mutableStateOf(syndromes[0].justification) }
    var userPlan by remember { mutableStateOf(syndromes[0].plan) }

    // Trigger update on template select
    LaunchedEffect(selectedIndex) {
        val sel = syndromes[selectedIndex]
        userDiagnostic = sel.diagnostic
        userOrders = sel.orders
        userJustification = sel.justification
        userPlan = sel.plan
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Generador Integrado de Órdenes y Notas Clínicas Bedside",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Edita la información según el estado de tu paciente de manera instantánea y presiona 'Copiar Nota Clínica' para exportar un reporte estructurado libre de errores.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Text("Selecciona el Síndrome Clínico de Sospecha:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            
            // Syndrome Pills Option Row
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                syndromes.forEachIndexed { idx, synd ->
                    val isSelected = selectedIndex == idx
                    Surface(
                        onClick = { selectedIndex = idx },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = synd.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = userDiagnostic,
                onValueChange = { userDiagnostic = it },
                label = { Text("Diagnóstico de Sospecha") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        item {
            OutlinedTextField(
                value = userOrders,
                onValueChange = { userOrders = it },
                label = { Text("Órdenes Clínicas Iniciales (EPEC)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        item {
            OutlinedTextField(
                value = userJustification,
                onValueChange = { userJustification = it },
                label = { Text("Justificación Médica (Evidencia de Guías)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        item {
            OutlinedTextField(
                value = userPlan,
                onValueChange = { userPlan = it },
                label = { Text("Plan de Trabajo Bedside") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Button(
                onClick = {
                    val fullFormattedNote = "=========================================\n" +
                            "💡 SYNAPPSE CLINICAL SYNDROME REPORT\n" +
                            "=========================================\n" +
                            "► DIAGNÓSTICO DE SOSPECHA:\n$userDiagnostic\n\n" +
                            "► ÓRDENES INICIALES:\n$userOrders\n\n" +
                            "► JUSTIFICACIÓN CLÍNICA (EVIdenCIA):\n$userJustification\n\n" +
                            "► PLAN DE TRABAJO BEDSIDE:\n$userPlan\n" +
                            "=========================================\n" +
                            "Generado el ${java.util.Date()} bajo Guías Corrientes."
                    
                    clipboardManager.setText(AnnotatedString(fullFormattedNote))
                    Toast.makeText(context, "Nota Completa copiada al portapapeles con éxito", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_note_generator_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copiar Nota Clínica Estructurada")
            }
        }
    }
}

data class SyndromeTemplate(
    val id: String,
    val title: String,
    val diagnostic: String,
    val orders: String,
    val justification: String,
    val plan: String
)

// ==========================================
// COMPONENT 4: LEGACY BEDSIDE SCALES (mRS, FAST, MGFA, ABCD2, Hunt-Hess, Fisher, Historial)
// ==========================================
@Composable
fun LegacyBedsideScales(
    selectedMrsGrade: Int?,
    onMrsGradeSelected: (Int?) -> Unit,
    selectedFastStage: Int?,
    onFastStageSelected: (Int?) -> Unit,
    selectedMgfaClass: Int?,
    onMgfaClassSelected: (Int?) -> Unit,
    recentHistoryContent: @Composable () -> Unit,
    onCopyClicked: (String, String, String) -> Unit
) {
    var selectedSubCategory by remember { mutableStateOf("mrs") } // "mrs", "fast", "mgfa", "examen", "historial"

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "mrs" to "Rankin (mRS)",
                "fast" to "Reisberg (FAST)",
                "mgfa" to "Clasif. MGFA",
                "examen" to "Examen Físico",
                "historial" to "Historial de Cálculos"
            ).forEach { (id, title) ->
                val isSelected = selectedSubCategory == id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSubCategory = id },
                    label = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubCategory) {
                "mrs" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Escala de Rankin Modificada (mRS)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Medida de resultado funcional global usada clásicamente en ensayos de ictus vascular. Rango de 0 (sintomático nulo) a 6 (difunto).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        val grades = listOf(
                            "Grado 0" to "Sin síntomas en absoluto.",
                            "Grado 1" to "Sin discapacidad significativa a pesar de presentar síntomas leves. Capaz de realizar actividades habituales.",
                            "Grado 2" to "Discapacidad ligera. Incapaz de realizar actividades previas pero maneja sus propios asuntos sin ayuda institucional.",
                            "Grado 3" to "Discapacidad moderada. Requiere ayuda externa moderada para algunas tareas pero camina sin asistencia de otra persona.",
                            "Grado 4" to "Discapacidad moderadamente severa. Incapaz de caminar o atender necesidades físicas elementales sin ayuda directa.",
                            "Grado 5" to "Discapacidad severa. Confinamiento permanente en cama, incontinente, con necesidad constante de enfermería especializada.",
                            "Grado 6" to "Defunción / Muerte."
                        )

                        grades.forEachIndexed { i, (lbl, desc) ->
                            val isSelected = selectedMrsGrade == i
                            Card(
                                onClick = { onMrsGradeSelected(i) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = { onMrsGradeSelected(i) })
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(lbl, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        selectedMrsGrade?.let { idx ->
                            Button(
                                onClick = { onCopyClicked("Rankin Modificado (mRS)", "Grado $idx", grades[idx].second) },
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar Grado mRS")
                            }
                        }
                    }
                }
                "fast" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Estadificación FAST de Reisberg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Functional Assessment Staging (FAST). Escala descriptiva para evaluar progresión y pérdida de funcionalidad en demencia de tipo Alzheimer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        val stages = listOf(
                            "FAST 1" to "Adulto sano. Sin decremento cognoscitivo clínico subjetivo u objetivo.",
                            "FAST 2" to "Olvido subjetivo. Dificultad menor para recordar nombres o localización de objetos domésticos.",
                            "FAST 3" to "Deterioro cognitivo leve. Decremento en el desempeño laboral, desorientación espacial menor en viajes.",
                            "FAST 4" to "Demencia leve / Alzheimer leve. Dificultad para manejar finanzas complejas o planear eventos cotidianos.",
                            "FAST 5" to "Demencia moderada. Requiere asistencia para seleccionar adecuadamente la ropa para el clima reinante.",
                            "FAST 6" to "Demencia moderadamente severa. Requiere ayuda directa para vestirse, bañarse, e incontinencia de esfínteres.",
                            "FAST 7" to "Demencia severa. Pérdida del habla articulada, rigidez muscular, incapacidad para sonreír, sostener la cabeza o deglutir."
                        )

                        stages.forEachIndexed { i, (lbl, desc) ->
                            val isSelected = selectedFastStage == (i + 1)
                            Card(
                                onClick = { onFastStageSelected(i + 1) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = { onFastStageSelected(i + 1) })
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(lbl, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        selectedFastStage?.let { stageIdx ->
                            Button(
                                onClick = { onCopyClicked("Estadío FAST de Reisberg", "FAST $stageIdx", stages[stageIdx - 1].second) },
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar Estadío FAST")
                            }
                        }
                    }
                }
                "mgfa" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Clasificación Clínica de MGFA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Myasthenia Gravis Foundation of America (MGFA). Clasifica la severidad clínica funcional global de los pacientes diagnosticados con Miastenia Gravis.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        val classes = listOf(
                            "Clase I" to "Debilidad en los músculos oculares exclusivamente. Fuerza somática normal.",
                            "Clase II" to "Debilidad generalizada leve en otros músculos corporales somáticos u orofaríngeos.",
                            "Clase III" to "Debilidad generalizada moderada en músculos somáticos orofaríngeos.",
                            "Clase IV" to "Debilidad generalizada severa somática y faríngea (Disfagia importante).",
                            "Clase V" to "Crisis Miasténica. Requerimiento inminente de intubación orotraqueal ventilatoria."
                        )

                        classes.forEachIndexed { i, (lbl, desc) ->
                            val isSelected = selectedMgfaClass == i
                            Card(
                                onClick = { onMgfaClassSelected(i) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = { onMgfaClassSelected(i) })
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(lbl, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        selectedMgfaClass?.let { idx ->
                            Button(
                                onClick = { onCopyClicked("Clasificación Clínica MGFA", classes[idx].first, classes[idx].second) },
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar Clasificación MGFA")
                            }
                        }
                    }
                }
                "examen" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExamenFisicoCard(onCopyClicked = onCopyClicked)
                    }
                }
                "historial" -> {
                    recentHistoryContent()
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 5: SYNAPPSE — NEUROINFECTOLOGÍA
// ==========================================

@Composable
fun CalcCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    scoreBadge: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        if (scoreBadge.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = scoreBadge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun NeuroInfectologiaPane(
    onNavigateToDrug: (String) -> Unit,
    onCopyClicked: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var subTab by remember { mutableStateOf(0) } // 0: Triage & Flujos, 1: Calculadoras, 2: Órdenes & Alertas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Applet Banner Header Cards
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🦠", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("SYNAPPSE — Neuroinfectología", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Triage de entrada, LCR interactivo y scores de decisión crítica.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Sub Segmented Control (Pills)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabsLabels = listOf("🚨 Triage & Rutas", "📊 Calculadoras", "📋 Órdenes & Alertas")
            tabsLabels.forEachIndexed { idx, label ->
                val isSelected = subTab == idx
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { subTab = idx }
                        .testTag("neuro_infecto_subtab_$idx"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        when (subTab) {
            0 -> TriageYFlujosView(onNavigateToDrug)
            1 -> CalculatodasYScoresView(onNavigateToDrug, onCopyClicked)
            2 -> OrdenesYAlertasView(clipboardManager, context, onNavigateToDrug)
        }
    }
}

@Composable
fun TriageYFlujosView(onNavigateToDrug: (String) -> Unit) {
    var selectedSyndrome by remember { mutableStateOf(0) }
    val syndromes = listOf(
        "Meningitis Bacteriana Aguda",
        "Encefalitis viral",
        "Meningitis Tuberculosa",
        "Meningitis Criptocócica"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Paso 1: ¿Cuál es el síndrome clínico dominante?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            syndromes.forEachIndexed { idx, synd ->
                val isSel = selectedSyndrome == idx
                FilterChip(
                    selected = isSel,
                    onClick = { selectedSyndrome = idx },
                    label = { Text(synd, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        when (selectedSyndrome) {
            0 -> TriageMeningitisBacteriana(onNavigateToDrug)
            1 -> TriageEncefalitis(onNavigateToDrug)
            2 -> TriageMeningitisTB(onNavigateToDrug)
            3 -> TriageMeningitisCripto(onNavigateToDrug)
        }
    }
}

@Composable
fun TriageMeningitisBacteriana(onNavigateToDrug: (String) -> Unit) {
    var hasInmuno by remember { mutableStateOf(false) }
    var hasSnc by remember { mutableStateOf(false) }
    var hasSeizure by remember { mutableStateOf(false) }
    var hasPapiledema by remember { mutableStateOf(false) }
    var hasConsciousness by remember { mutableStateOf(false) }
    var hasFocalDeficit by remember { mutableStateOf(false) }

    val anyRisk = hasInmuno || hasSnc || hasSeizure || hasPapiledema || hasConsciousness || hasFocalDeficit
    var empGroup by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("🚨 ALERTA ROJA: NO RETRASAR ANTIBIÓTICO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text(
                    "La terapia empírica y corticoide deben administrarse dentro de la primera hora. La punción lumbar o tomografía no deben retrasar el tratamiento inicial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 11.sp
                )
            }
        }

        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("¿Requiere Tomografía (CT) previa a la PL?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Marque si el paciente presenta algún factor de riesgo:", style = MaterialTheme.typography.labelSmall)

                CalcCheckboxRow("Inmunosupresión severa (SIDA, quimioterapia, trasplante)", hasInmuno, { hasInmuno = it })
                CalcCheckboxRow("Enfermedad conocida de SNC (Masa, glioma, stroke previo)", hasSnc, { hasSnc = it })
                CalcCheckboxRow("Convulsión de reciente aparición (última semana)", hasSeizure, { hasSeizure = it })
                CalcCheckboxRow("Papiledema clínico confirmado", hasPapiledema, { hasPapiledema = it })
                CalcCheckboxRow("Alteración moderada o grave de conciencia (GCS <12)", hasConsciousness, { hasConsciousness = it })
                CalcCheckboxRow("Déficit focal en examen (ej. hemiparesia)", hasFocalDeficit, { hasFocalDeficit = it })

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (anyRisk) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("⚠️ INDICACIÓN DE CT ANTES DE PL:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("Acción: Tomar Hemocultivos -> Iniciar Dexametasona + Terapia Empírica -> CT urgente. Si CT es normal/segura, proceder a PL.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                        }
                    }
                } else {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("✅ SEGURO PARA PL INMEDIATA:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Acción: Realizar punción de inmediato para análisis crítico; iniciar Dexametasona y antibióticos inmediatamente después.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tratamiento Empírico de Elección Adultos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                val groups = listOf("Adulto sano 18-50 años", "Adulto >50 años / Inmunosuprimido", "Post-neurocirugía o trauma penetrante")
                groups.forEachIndexed { i, label ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { empGroup = i }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = empGroup == i, onClick = { empGroup = i })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                when (empGroup) {
                    0 -> {
                        Text("• Ceftriaxona 2 g IV cada 12 h + Vancomicina 15-20 mg/kg IV cada 8-12 h.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("• Dexametasona 10 mg IV cada 6 h por 4 días. Administrar 15 min antes o junto al antibiótico.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        DrugBadge("Metilprednisolona (Pulsos)", onNavigateToDrug)
                    }
                    1 -> {
                        Text("• Ceftriaxona 2 g IV cada 12 h + Vancomicina 15-20 mg/kg cada 8-12 h + Ampicilina 2 g IV cada 4 h (Cubre Listeria).", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("• Dexametasona 10 mg IV cada 6 h por 4 días.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        DrugBadge("Metilprednisolona (Pulsos)", onNavigateToDrug)
                    }
                    2 -> {
                        Text("• Cefepime 2 g IV cada 8 h (o Meropenem 2 g cada 8 h) + Vancomicina (Cubre Pseudomonas y MRSA).", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun TriageEncefalitis(onNavigateToDrug: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🧠 Encefalitis Viral Aguda / Meningoencefalitis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("• Descartar hipoglucemia capilar inmediata.", style = MaterialTheme.typography.bodySmall)
                Text("• Estudios LCR: Células, proteínas, glucosa + PCR VHS-1, VHS-2, VZV y enterovirus.", style = MaterialTheme.typography.bodySmall)
                Text("• MRI con contraste y EEG prioritarios (descarte de descargas focales temporales sugerentes de VHS).", style = MaterialTheme.typography.bodySmall)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("REGLA DE SEGURIDAD VHS:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("Aciclovir IV 10 mg/kg cada 8 h inmediato ante sospecha (ajustar por aclaramiento de creatinina). No demorar por resultados diagnósticos. Si PCR de LCR es negativa pero sospecha persiste (lesión temporal unilateral en MRI), repetir PL en 3-7 días.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Medicamentos de interés en encefalomielitis autoinmune:", style = MaterialTheme.typography.labelSmall)
                DrugBadge("Metilprednisolona (Pulsos)", onNavigateToDrug)
            }
        }
    }
}

@Composable
fun TriageMeningitisTB(onNavigateToDrug: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🏔️ Meningitis Tuberculosa (TBM)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text("• Presentación subaguda/crónica (>5 días de cefalea, fiebre, letargia).", style = MaterialTheme.typography.bodySmall)
                Text("• Diagnóstico: thwaites score y marais criterios (ver pestaña Calculadoras).", style = MaterialTheme.typography.bodySmall)
                Text("• Complicaciones críticas: Hidrocefalia, infartos de ganglios basales (vasculitis), compromiso de pares de la base (III, IV, VI).", style = MaterialTheme.typography.bodySmall)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("CONDUCTA CLÍNICA SUGERIDA:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Iniciar esquema anti-TB (RIPE) de inmediato ante sospecha moderada-alta. Asociar siempre corticoides (Dexametasona o Metilprednisolona) por 6-8 semanas con retiro gradual para reducir secuelas vasculares y mortalidad.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    DrugBadge("Metilprednisolona (Pulsos)", onNavigateToDrug)
                }
            }
        }
    }
}

@Composable
fun TriageMeningitisCripto(onNavigateToDrug: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🍄 Meningitis Criptocócica / Fúngica", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("• Pacientes con VIH (<100 CD4) o inmunosupresión biológica.", style = MaterialTheme.typography.bodySmall)
                Text("• Pruebas clave: Antígeno criptocócico (CrAg) en LCR y sangre, tinta china.", style = MaterialTheme.typography.bodySmall)
                Text("• Medir presión de apertura de forma obligatoria en la punción lumbar inicial.", style = MaterialTheme.typography.bodySmall)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("MANEJO DE HIPERTENSIÓN PIC:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("Punciones lumbares terapéuticas seriadas si la presión de apertura inicial es ≥25 cm H₂O. Retirar líquido hasta presión <20 cm H₂O. No usar acetazolamida ni manitol como terapia principal en criptococo.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                
                Text("REGLA VIH / TARV:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("Postergar terapia antirretroviral (TARV) por 4 a 6 semanas tras iniciar antifúngicos para evitar el síndrome de reconstitución inmune (IRIS) meningitis-asociado.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun CalculatodasYScoresView(
    onNavigateToDrug: (String) -> Unit,
    onCopyClicked: (String, String, String) -> Unit
) {
    var activeCalc by remember { mutableStateOf(0) }
    val calculatorsName = listOf(
        "🧠 LCR Avanzado",
        "⚖️ Thwaites (TBM vs MBA)",
        "📜 Marais TBM Criteria",
        "👶 BMS (Meningitis Peds)",
        "🔬 BM-CASCO (Adultos)",
        "💧 Presión Cripto (PL Drenaje)"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Paso 2: Seleccione la Calculadora o Score Clínico:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            calculatorsName.forEachIndexed { idx, name ->
                val isSel = activeCalc == idx
                FilterChip(
                    selected = isSel,
                    onClick = { activeCalc = idx },
                    label = { Text(name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (activeCalc) {
                    0 -> LabLcrInterpreter(onCopyClicked)
                    1 -> ThwaitesScoreCalculator(onCopyClicked)
                    2 -> MaraisScoreCalculator(onCopyClicked)
                    3 -> BmsPediatricCalculator(onCopyClicked)
                    4 -> BmCascoCalculator(onCopyClicked)
                    5 -> CryptococoPressureCalculator(onCopyClicked)
                }
            }
        }
    }
}

@Composable
fun LabLcrInterpreter(onCopyClicked: (String, String, String) -> Unit) {
    var cellsVal by remember { mutableStateOf("") }
    var pmnVal by remember { mutableStateOf("") }
    var proteinVal by remember { mutableStateOf("") }
    var glucoseCsfVal by remember { mutableStateOf("") }
    var glucoseSeraVal by remember { mutableStateOf("") }
    var lactateVal by remember { mutableStateOf("") }

    val cells = cellsVal.toDoubleOrNull() ?: 0.0
    val pmnPercent = pmnVal.toDoubleOrNull() ?: 0.0
    val protein = proteinVal.toDoubleOrNull() ?: 0.0
    val glucoseCsf = glucoseCsfVal.toDoubleOrNull() ?: 0.0
    val glucoseSerum = glucoseSeraVal.toDoubleOrNull() ?: 0.0
    val lactate = lactateVal.toDoubleOrNull() ?: 0.0

    val absNeutro = (cells * pmnPercent) / 100.0
    val absLympho = (cells * (100.0 - pmnPercent)) / 100.0
    val ratio = if (glucoseSerum > 0.0) glucoseCsf / glucoseSerum else 0.0
    val isLowRatio = ratio > 0.0 && ratio < 0.4

    val profileString = when {
        cells == 0.0 && protein == 0.0 -> "Esperando datos..."
        cells > 100 && pmnPercent >= 75 && (isLowRatio || lactate > 4.0) -> "Sugerente de Meningitis Bacteriana Aguda"
        cells in 10.0..1000.0 && pmnPercent < 50 && ratio >= 0.45 && protein < 100.0 -> "Sugerente de Meningitis Viral / Aéptica"
        cells in 20.0..1000.0 && pmnPercent < 50 && ratio < 0.4 && protein >= 100.0 -> "Sugerente de Meningitis Tuberculosa o Fúngica"
        cells < 15.0 && protein > 100.0 && ratio >= 0.5 -> "Disociación Albúmino-Citológica (SGB, bloqueo LCR)"
        else -> "Perfil inespecífico o mixto, evaluar clínicamente"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Interpretador Clínico de LCR 🧪", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Button(onClick = { cellsVal = "1500"; pmnVal = "90"; proteinVal = "250"; glucoseCsfVal = "20"; glucoseSeraVal = "100"; lactateVal = "6.5" }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                Text("Bacteriano", fontSize = 10.sp)
            }
            Button(onClick = { cellsVal = "125"; pmnVal = "15"; proteinVal = "55"; glucoseCsfVal = "65"; glucoseSeraVal = "100"; lactateVal = "1.8" }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                Text("Viral", fontSize = 10.sp)
            }
            Button(onClick = { cellsVal = "350"; pmnVal = "30"; proteinVal = "220"; glucoseCsfVal = "28"; glucoseSeraVal = "100"; lactateVal = "4.2" }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                Text("Tuberculoso", fontSize = 10.sp)
            }
            Button(onClick = { cellsVal = "3"; pmnVal = "0"; proteinVal = "150"; glucoseCsfVal = "60"; glucoseSeraVal = "100"; lactateVal = "1.5" }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                Text("SGB / Disociación", fontSize = 10.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = cellsVal, onValueChange = { cellsVal = it }, label = { Text("Células/µL", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(48.dp))
            OutlinedTextField(value = pmnVal, onValueChange = { pmnVal = it }, label = { Text("% PMN LCR", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(48.dp))
            OutlinedTextField(value = proteinVal, onValueChange = { proteinVal = it }, label = { Text("Proteínas", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(48.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = glucoseCsfVal, onValueChange = { glucoseCsfVal = it }, label = { Text("Glucosa LCR", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(48.dp))
            OutlinedTextField(value = glucoseSeraVal, onValueChange = { glucoseSeraVal = it }, label = { Text("Glucosa Suero", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(48.dp))
            OutlinedTextField(value = lactateVal, onValueChange = { lactateVal = it }, label = { Text("Lactato LCR", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(48.dp))
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Neutrófilos absolutos:", style = MaterialTheme.typography.bodySmall)
                    Text("${absNeutro.toInt()} cél/µL", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Linfocitos absolutos:", style = MaterialTheme.typography.bodySmall)
                    Text("${absLympho.toInt()} cél/µL", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Relación LCR/Sangre:", style = MaterialTheme.typography.bodySmall)
                    Text(String.format("%.2f", ratio), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (isLowRatio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                }
                Text("Perfil:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                Text(profileString, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = {
                val results = "LCR: Células=$cellsVal/µL, PMN=$pmnVal%, Proteínas=$proteinVal, RatioGlucosa=${String.format("%.2f", ratio)}, Lactato=$lactateVal. Interpretación: $profileString"
                onCopyClicked("Analizador de LCR", "Citológico y Bioquímico", results)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copiar Análisis LCR")
        }
    }
}

@Composable
fun ThwaitesScoreCalculator(onCopyClicked: (String, String, String) -> Unit) {
    var checkAge by remember { mutableStateOf(false) }
    var checkWbcBlood by remember { mutableStateOf(false) }
    var checkDuration by remember { mutableStateOf(false) }
    var checkWbcCsf by remember { mutableStateOf(false) }
    var checkPmnCsf by remember { mutableStateOf(false) }

    val score = (if (checkAge) 2 else 0) +
            (if (checkWbcBlood) 4 else 0) +
            (if (checkDuration) -5 else 0) +
            (if (checkWbcCsf) 3 else 0) +
            (if (checkPmnCsf) 4 else 0)

    val isTbm = score <= 4

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Thwaites Score (Meningitis TB vs Bacteriana) ⚖️", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Ayuda a diferenciar meningitis tuberculosa de bacteriana:", style = MaterialTheme.typography.labelSmall)

        CalcCheckboxRow("Edad ≥ 36 años", checkAge, { checkAge = it }, "+2")
        CalcCheckboxRow("Leucocitos en sangre ≥ 15,000/µL", checkWbcBlood, { checkWbcBlood = it }, "+4")
        CalcCheckboxRow("Duración de síntomas en días ≥ 6 días", checkDuration, { checkDuration = it }, "-5")
        CalcCheckboxRow("Leucocitos totales en LCR ≥ 900/µL", checkWbcCsf, { checkWbcCsf = it }, "+3")
        CalcCheckboxRow("Neutrófilos en LCR ≥ 75%", checkPmnCsf, { checkPmnCsf = it }, "+4")

        Card(colors = CardDefaults.cardColors(containerColor = if (isTbm) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Puntaje Thwaites: $score", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isTbm) "Sugerente de Meningitis Tuberculosa (Score ≤ 4)" else "Sugerente de Meningitis Bacteriana Aguda (Score > 4)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isTbm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text("⚠️ ADVERTENCIA: No usar para descartar TBM si hay VIH, inmunosupresión o tratamiento antibiótico o antifúngico previo.", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
            }
        }

        Button(
            onClick = {
                onCopyClicked("Thwaites Score", "Puntuación de Diagnóstico TBM", "Puntaje Thwaites=$score. Resultado: " + (if (isTbm) "TBM" else "Meningitis Bacteriana"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copiar Reporte Thwaites")
        }
    }
}

@Composable
fun MaraisScoreCalculator(onCopyClicked: (String, String, String) -> Unit) {
    // We can hold values in a clean way
    var s1 by remember { mutableStateOf(false) } // symptoms >5d (+4)
    var s2 by remember { mutableStateOf(false) } // constitutional (+2)
    var s3 by remember { mutableStateOf(false) } // contact/positive (+2)
    var s4 by remember { mutableStateOf(false) } // focal deficit (+1)
    var s5 by remember { mutableStateOf(false) } // cranial nerve (+1)
    var s6 by remember { mutableStateOf(false) } // altered state (+1)

    // LCR
    var l1 by remember { mutableStateOf(false) } // clear appearance (+1)
    var l2 by remember { mutableStateOf(false) } // cells 10-500 (+1)
    var l3 by remember { mutableStateOf(false) } // lym >50% (+1)
    var l4 by remember { mutableStateOf(false) } // protein >100 mg (+1)
    var l5 by remember { mutableStateOf(false) } // glucose CSF/sera <0.5 (+1)

    // Imagen
    var i1 by remember { mutableStateOf(false) } // hidro (+1)
    var i2 by remember { mutableStateOf(false) } // enrich (+2)
    var i3 by remember { mutableStateOf(false) } // tuberculoma (+2)
    var i4 by remember { mutableStateOf(false) } // infarct (+1)
    var i5 by remember { mutableStateOf(false) } // precontraste hyper (+2)

    var directMicroConfirm by remember { mutableStateOf(false) }

    val clinicalY = (if (s1) 4 else 0) + (if (s2) 2 else 0) + (if (s3) 2 else 0) + (if (s4) 1 else 0) + (if (s5) 1 else 0) + (if (s6) 1 else 0)
    val lcrY = (if (l1) 1 else 0) + (if (l2) 1 else 0) + (if (l3) 1 else 0) + (if (l4) 1 else 0) + (if (l5) 1 else 0)
    val imgY = (if (i1) 1 else 0) + (if (i2) 2 else 0) + (if (i3) 2 else 0) + (if (i4) 1 else 0) + (if (i5) 2 else 0)
    
    val totalScore = clinicalY + lcrY + imgY

    val diagnosisText = when {
        directMicroConfirm -> "Meningitis Tuberculosa DEFINITIVA (Confirmada por microbiología, GeneXpert en LCR o Histopatología)."
        imgY > 0 && totalScore >= 12 -> "Meningitis Tuberculosa PROBABLE (Score ≥ 12 con neuroimagen)."
        imgY == 0 && totalScore >= 10 -> "Meningitis Tuberculosa PROBABLE (Score ≥ 10 sin neuroimagen)."
        imgY > 0 && totalScore in 6..11 -> "Meningitis Tuberculosa POSIBLE (Score 6-11 con neuroimagen)."
        imgY == 0 && totalScore in 6..9 -> "Meningitis Tuberculosa POSIBLE (Score 6-9 sin neuroimagen)."
        else -> "Baja probabilidad por criterios Marais (Score < 6)."
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Criterios de Marais para TBM 📜", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        CalcCheckboxRow("Confirmación bacteriológica en LCR (BAAR, NAAT o cultivo +)", directMicroConfirm, { directMicroConfirm = it }, "Definitiva")
        
        if (!directMicroConfirm) {
            Text("1. Aspectos Clínicos (Max 6):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            CalcCheckboxRow("Síntomas consistentes por > 5 días", s1, { s1 = it }, "+4")
            CalcCheckboxRow("Síg. constitucionales (Fiebre, baja peso, sudación)", s2, { s2 = it }, "+2")
            CalcCheckboxRow("Contacto cercano con TB o TB activa sistémica", s3, { s3 = it }, "+2")
            CalcCheckboxRow("Déficit focal neurológico (excl. pares craneales)", s4, { s4 = it }, "+1")
            CalcCheckboxRow("Parálisis de par craneal", s5, { s5 = it }, "+1")
            CalcCheckboxRow("Alteración estado de conciencia (letargia, GCS < 15)", s6, { s6 = it }, "+1")

            Text("2. Parámetros de LCR (Max 4):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            CalcCheckboxRow("Aspecto claro / transparente", l1, { l1 = it }, "+1")
            CalcCheckboxRow("Celularidad en LCR entre 10 y 500 células/µL", l2, { l2 = it }, "+1")
            CalcCheckboxRow("Predominio linfocítico > 50%", l3, { l3 = it }, "+1")
            CalcCheckboxRow("Hiperproteinorraquia > 100 mg/dL", l4, { l4 = it }, "+1")
            CalcCheckboxRow("Relación glucosa LCR/suero < 0.5", l5, { l5 = it }, "+1")

            Text("3. Hallazgos Neuroimagen (Max 6):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            CalcCheckboxRow("Hidrocefalia", i1, { i1 = it }, "+1")
            CalcCheckboxRow("Realce meníngeo basal marcado", i2, { i2 = it }, "+2")
            CalcCheckboxRow("Presencia de tuberculoma", i3, { i3 = it }, "+2")
            CalcCheckboxRow("Infarto cerebral agudo", i4, { i4 = it }, "+1")
            CalcCheckboxRow("Hiperdensidad de la base precontraste en CT", i5, { i5 = it }, "+2")
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Puntaje Total Marais: $totalScore (Clínico=$clinicalY, LCR=$lcrY, Imagen=$imgY)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Clasificación: $diagnosisText", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Button(
            onClick = {
                onCopyClicked("Criterios de Marais", "Consenso Internacional de Meningitis TB", "Puntaje total=$totalScore. Diagnóstico: $diagnosisText")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copiar Criterios Marais")
        }
    }
}

@Composable
fun BmsPediatricCalculator(onCopyClicked: (String, String, String) -> Unit) {
    var gramPos by remember { mutableStateOf(false) }
    var neutro1000 by remember { mutableStateOf(false) }
    var prot80 by remember { mutableStateOf(false) }
    var pmnBlood10000 by remember { mutableStateOf(false) }
    var seizureBy by remember { mutableStateOf(false) }

    val points = (if (gramPos) 1 else 0) + (if (neutro1000) 1 else 0) + (if (prot80) 1 else 0) + (if (pmnBlood10000) 1 else 0) + (if (seizureBy) 1 else 0)
    val isLowRisk = points == 0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Bacterial Meningitis Score (BMS - Pediatría) 👶", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("ALERTA: Solo utilizable en pediatría. No aplicar en menores de 2 meses, inestables o pretratados con antibióticos.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

        CalcCheckboxRow("Tinción de Gram en LCR positiva", gramPos, { gramPos = it })
        CalcCheckboxRow("Neutrófilos absolutos en LCR ≥ 1000/µL", neutro1000, { neutro1000 = it })
        CalcCheckboxRow("Proteínas en LCR ≥ 80 mg/dL", prot80, { prot80 = it })
        CalcCheckboxRow("Neutrófilos absolutos en sangre periférica ≥ 10,000/µL", pmnBlood10000, { pmnBlood10000 = it })
        CalcCheckboxRow("Convulsión antes o durante la consulta", seizureBy, { seizureBy = it })

        Card(colors = CardDefaults.cardColors(containerColor = if (isLowRisk) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Criterios de riesgo presentes: $points", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isLowRisk) "MUY BAJO RIESGO DE MENINGITIS BACTERIANA (Elegible para manejo ambulatorio si clínica apoya)." else "RIESGO DETECTADO. Requiere manejo hospitalario y cobertura antibiótica empírica completa.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isLowRisk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BmCascoCalculator(onCopyClicked: (String, String, String) -> Unit) {
    var c1 by remember { mutableStateOf(false) } // csf cells >50 (+2)
    var c2 by remember { mutableStateOf(false) } // csf protein >80 (+1)
    var c3 by remember { mutableStateOf(false) } // lactado >35 mg/dl/3.9 (+1)
    var c4 by remember { mutableStateOf(false) } // ratio <0.45 (+1)
    var c5 by remember { mutableStateOf(false) } // leucos sangre >10k (+1)

    val valS = (if (c1) 2 else 0) + (if (c2) 1 else 0) + (if (c3) 1 else 0) + (if (c4) 1 else 0) + (if (c5) 1 else 0)
    val checkB = valS >= 3

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("BM-CASCO Score (Adultos) 🔬", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Valora de 0 hasta 6 la sospecha de meningitis bacteriana en adultos:", style = MaterialTheme.typography.labelSmall)

        CalcCheckboxRow("Células totales en LCR > 50/µL", c1, { c1 = it }, "+2")
        CalcCheckboxRow("Hiperproteinorraquia en LCR > 80 mg/dL", c2, { c2 = it }, "+1")
        CalcCheckboxRow("Lactato en LCR > 35 mg/dL (aprox > 3.9 mmol/L)", c3, { c3 = it }, "+1")
        CalcCheckboxRow("Relación glucosa LCR/suero < 45%", c4, { c4 = it }, "+1")
        CalcCheckboxRow("Leucocitos periféricos en sangre > 10,000/µL", c5, { c5 = it }, "+1")

        Card(colors = CardDefaults.cardColors(containerColor = if (checkB) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Puntaje BM-CASCO: $valS / 6", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(
                    text = if (checkB) "Sugerente de Meningitis Bacteriana (Score ≥ 3)" else "Baja sospecha por score (Se requiere correlacionar con clínica)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (checkB) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CryptococoPressureCalculator(onCopyClicked: (String, String, String) -> Unit) {
    var openP by remember { mutableStateOf("") }
    val openVal = openP.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Drenaje de LCR por Presión en Criptococo 💧", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Guía para reducir la hipertensión intracraneal de forma segura:", style = MaterialTheme.typography.labelSmall)

        OutlinedTextField(
            value = openP,
            onValueChange = { openP = it },
            label = { Text("Presión de apertura (cm H₂O)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Recomendación de Drenaje Bedside:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                when {
                    openVal == 0.0 -> {
                        Text("Ingrese el valor de presión inicial arriba.", style = MaterialTheme.typography.bodySmall)
                    }
                    openVal < 20.0 -> {
                        Text("• Presión normal (<20). No requiere punción lumbar terapéutica drenante hoy si no hay síntomas severos.", style = MaterialTheme.typography.bodySmall)
                    }
                    openVal in 20.0..25.0 -> {
                        Text("• Presión límite (20-25). Mantener vigilancia neurológica estrecha. Si hay cefalea refractaria o papiledema, drenar hasta presión de cierre <20 cm H₂O.", style = MaterialTheme.typography.bodySmall)
                    }
                    else -> {
                        Text("• Presión ELEVADA (≥25). ALTA INDICACIÓN DE DRENAJE.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("• Meta: Drenar volumen hasta presión de cierre <20 cm H₂O (o reducción aproximada del 50% si el valor inicial era extremadamente alto).", style = MaterialTheme.typography.bodySmall)
                        Text("• Guía volumen: Habitualmente, retirar de 10 a 20 mL de LCR reduce significativamente la presión (cada 1 mL reduce aprox 1 cm H₂O). Realizar diariamente de ser refractaria.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdenesYAlertasView(
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context,
    onNavigateToDrug: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📋 Panel de Órdenes Iniciales de Triage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                Text(
                    "Estudio de Sangre:\n" +
                    "- Hemograma integral + Leucocitos y plaquetas\n" +
                    "- Creatinina, gases arteriales, lactato arterial si hay sepsis\n" +
                    "- Glucosa capilar y sérica simultáneas (Crítico para relación LCR/Sangre)\n" +
                    "- Hemocultivos ×2 inmediatos\n- VIH, serología VDRL\n\n" +
                    "Estudio de LCR:\n" +
                    "- Presión de apertura, citológico (recuento y % diferencial)\n" +
                    "- Proteínas, Glucosa en LCR, Lactato\n" +
                    "- Tinción de Gram, tinta china, cultivo fúngico, bacteriano y BAAR\n" +
                    "- PCR multiplex para virus prioritarios (VHS-1, 2, enterovirus)",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 15.sp
                )

                Button(
                    onClick = {
                        val fullText = "PANEL ÓRDENES NEUROINFECTOLOGÍA:\nHemograma, Glucosa sérica pareada, Creatinina, Glucosa capilar, Hemocultivos x2, VIH, VDRL.\nLCR: Presión apertura, Citológico diferencial, Proteínas, Glucosa LCR, Lactato, Gram, Cultivos, Tinta China, PCR VHS. Generado por SYNAPPSE."
                        clipboardManager.setText(AnnotatedString(fullText))
                        Toast.makeText(context, "Órdenes copiadas al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Órdenes Bedside", fontSize = 11.sp)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("⚠️ Reglas de Oro en Neuroinfecciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("1. NO demorar antibiótico comunitario por realizar tomografía de cráneo si no hay banderas de herniación.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                Text("2. Administrar Dexametasona antes o conjuntamente con la ceftriaxona en sospecha de neumococo.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                Text("3. En encefalitis, iniciar Aciclovir IV inmediato. Ajustar a velocidad de filtración renal para evitar nefrotoxicidad.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                Text("4. No postergar tratamiento de meningitis tuberculosa por reporte microscópico negativo hoy.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                Text("5. En criptococo, el drenaje lumbar diario o seriado por presión es mandatario; evitar acetazolamida.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            }
        }
    }
}

// ==========================================
// NEW COMPONENT A: MUERTE ENCEFÁLICA &. DONACIÓN
// ==========================================
@Composable
fun MuerteEncefalicaProtocolView() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GuideHeader(
            title = "Muerte Encefálica y Donación de Órganos",
            subtitle = "Diagnóstico Científico-Legal de Tallo Encefálico",
            source = "Decreto 2493 de 2004 (Colombia) & Guías de Consenso",
            updateInfo = "Actualizado 2026"
        )

        SectionCard("1. Prerrequisitos Clínicos Obligatorios") {
            Text(
                text = "Antes de iniciar la exploración del tronco encefálico, deben cumplirse y verificarse de forma absoluta los siguientes parámetros fisiológicos:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            BulletText("Etiología Conocida e Irreversible", "Evidencia por clínica o neuroimagen de lesión destructiva severa compatible con muerte encefálica (Trauma craneal severo, HIC catastrófica, anoxia cerebral refractaria).")
            BulletText("Estabilidad Hemodinámica", "Presión Arterial Sistólica ≥ 100 mmHg (puede requerir soporte de vasopresores o inotrópicos).")
            BulletText("Oxigenación y Temperatura", "Temperatura corporal central > 32°C (en adultos preferible ≥ 36°C) y oxigenación adecuada.")
            BulletText("Ausencia de Tóxicos o Fármacos Depresores", "Excluir intoxicación por psicofármacos, sedantes, alcohol o bloqueantes neuromusculares. Suspender infusiones continuas según vidas medias.")
            BulletText("Electrólitos y Perfil Metabólico", "Ausencia de trastornos electrolíticos, ácido-base o endocrinos extremos compatibles con simulación de coma arreactivo.")
        }

        SectionCard("2. Examen de los 7 Reflejos del Tronco Encefálico") {
            Text(
                "Debe constatarse exhaustiva y bilateralmente por dos médicos idóneos no interdependientes (uno de ellos especialista en ciencias neurológicas) que no formen parte del programa de trasplantes, la persistencia de arreactividad total:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            BulletText("1. Ausencia de Respiración Espontánea", "Confirmado mediante Test de Apnea Positivo hoy (PaCO₂ ≥ 60 mmHg o aumento ≥ 20 mmHg con ventilación ausente).")
            BulletText("2. Pupilas Dilatadas Persistentemente", "Medias o midriáticas bilaterales, generalmente de 4 a 9 mm, absolutamente refractarias.")
            BulletText("3. Reflejo Pupilar / Fotomotor Abolido", "Negativo bilateralmente frente al estímulo de luz intensa dirigida.")
            BulletText("4. Reflejo Corneano Abolido", "Ausente bilateralmente frente al tacto con gasa o algodón en la córnea.")
            BulletText("5. Reflejo Óculo-Vestibular Ausente", "Falta de desviación ocular tras irrigar cada conducto auditivo externo con 50 mL de agua helada (observar durante 1 min, intervalo de 5 min entre oídos).")
            BulletText("6. Reflejo Faríngeo / Nauseoso Ausente", "Ningún movimiento de elevación velopalatina al estimular mecánicamente la faringe posterior.")
            BulletText("7. Reflejo Tusígeno / Traqueal Ausente", "Ausencia de tos protectora ante la succión traqueal forzada profunda con sonda por tubo ventilatorio.")
        }

        SectionCard("3. Periodos de Observación & Pruebas de Certeza") {
            Text(
                "Los tiempos mínimos recomendados de observación en coma clínico arreactivo dependen estrictamente de la edad del paciente:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            BulletText("Menor de 2 Meses de Edad", "Requiere dos exploraciones clínicas detalladas y dos Electroencefalogramas (EEG) separados por al menos 48 horas.")
            BulletText("De 2 Meses a 2 Años de Edad", "Requiere dos exploraciones clínicas detalladas y dos EEG separados por al menos 24 horas (en encefalopatía hipóxico-isquémica).")
            BulletText("Mayores de 2 Años y Adultos", "Se evalúan los reflejos clínicos descritos. El lapso de observación recomendado es de al menos 6 a 12 horas, aunque la ley de donación presunta ratifica 6 horas mínimas tras formalizar diagnóstico de muerte encefálica.")
            BulletText("Abreviación del Lapso por Pruebas de Flujo", "Si se dispone de exámenes de certeza que comprueben la ausencia total de flujo sanguíneo cerebral, se puede certificar la muerte encefálica de inmediato de forma legal y segura. Pruebas avaladas: Angiografía Cerebral, Doppler Transcraneal, Gammagrafía Cerebral Tc99m-HMPAO o Electroencefalografía plana.")
        }

        SectionCard("4. Códigos de Mantenimiento de Donante Orgánico") {
            Text(
                "La preservación de los órganos en el cadáver del donante potencial diagnosticado es fundamental para asegurar el éxito post-trasplante. Metas Bedside obligatorias:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            BulletText("Hemodinámica (Perfusión Órgano)", "Meta: PAS ≥ 90-100 mmHg, PAM ≥ 65-70 mmHg, Gasto Urinario 1 a 3 mL/kg/h. Preferir soporte con Noradrenalina de moderada cuantía si se requiere. Evitar la sobrecarga hídrica extrema para proteger pulmón.")
            BulletText("Sodio Sérico Estricto", "Meta: Na⁺ < 150 mEq/L (crítico para prevenir el edema celular masivo y la degeneración hepática eferente en el receptor).")
            BulletText("Ventilación Protectora", "Meta: PaO₂ ≥ 80-100 mmHg, saturación ≥ 95%. Configurar volumen tildal protector (6-8 mL/kg de peso ideal, no real), PEEP 5 a 8 cm H₂O para mantener abiertos alvéolos.")
            BulletText("Terapia Hormonal Triple (Regla Moño)", "1. Metilprednisolona: 15 mg/kg IV para bloquear la tormenta inflamatoria sistémica.\n2. Insulina IV: Mantener glucemia estrictamente controlada entre 140 y 180 mg/dL.\n3. Levotiroxina (T4): Bolus 20 mcg IV seguidos de 10 mcg/h o infusión de triyodotironina (T3) para revertir el hipotiroidismo central.\n4. Desmopresina (DDAVP): En caso de Diabetes Insípida (Poliuria extrema > 4 mL/kg/h, Na⁺ elevado, densidad urinaria baja < 1005). Dar 1-2 mcg IV.")
            BulletText("Notificación Legal Rápida", "Cualquier potencial donante en sala de choque o UCI debe notificarse inmediatamente a la Red del Nivel Regional por ley de presunción de donación en Colombia.")
        }

        SectionCard("5. CIE-10 & Gestión de Procura (Códigos del Donante)") {
            Text(
                "La codificación correcta en la historia clínica del potencial donante activa los procesos logísticos y de auditoría de la Red Nacional de Trasplantes (Ley 1805 de 2016). Registre obligatoriamente los siguientes códigos:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = " G93.82 ",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Muerte encefálica / cerebral diagnosticada debidamente comprobada. Código principal reglamentario de defunción clínica.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = " Z52.9 ",
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Donante potencial de órganos y tejidos, no especificado. Abre formalmente la bitácora administrativa de procura intrahospitalaria.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = " Z52.8 ",
                            color = MaterialTheme.colorScheme.onTertiary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Donante de otros órganos y tejidos especificados (Donación multiorgánica: Corazón, hígado, pulmones, córneas).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "🔔 REGLA DE TRANSMISIÓN DE ALERTA: Todo paciente con patología cerebral aguda catastrófica irreversible que curse con una escala de coma de Glasgow (GCS) ≤ 3 o escala de FOUR ≤ 4, debe registrarse como alerta activa a la coordinación regional de donación en un lapso inferior a 8 horas desde su sospecha.",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                lineHeight = 13.sp
            )
        }
    }
}

// ==========================================
// NEW COMPONENT B: NEURO-OTOLOGÍA URGENTE
// ==========================================
@Composable
fun NeuroOtologiaUrgenteView() {
    var hitResult by remember { mutableStateOf(0) } // 0: Normal/Sacada ausente (Central), 1: Alterado/Sacada presente (Periférico)
    var nystagmusResult by remember { mutableStateOf(0) } // 0: Bidireccional/Dirección cambiante (Central), 1: Unireccional fijo (Periférico)
    var skewResult by remember { mutableStateOf(0) } // 0: Desviación vertical presente (Central), 1: Ausente (Periférico)
    var hearingResult by remember { mutableStateOf(0) } // 0: Pérdida auditiva nueva unilateral (HINTS+ Central), 1: Audición normal/simétrica (Periférico)
    var canWalk by remember { mutableStateOf(true) } // marcher?

    val isCentral = hitResult == 0 || nystagmusResult == 0 || skewResult == 0 || hearingResult == 0 || !canWalk

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GuideHeader(
            title = "Neuro-Otología y Vértigo de Urgencia",
            subtitle = "Diagnóstico Diferencial de SVA (Síndrome Vestibular Agudo)",
            source = "Algoritmo HINTS / HINTS+ Consenso Internacional",
            updateInfo = "Actualizado 2026"
        )

        SectionCard("1. Triage del Síndrome Vestibular Agudo (SVA)") {
            Text(
                "El SVA se define como vértigo agudo severo de inicio súbito, nistagmus espontáneo o inducido por la mirada, intolerancia al movimiento de la cabeza, nauseas y marcha inestable. ¡Siempre distinguir causa periférica benigna (Neuritis) de infarto cerebeloso o troncoencefálico!",
                style = MaterialTheme.typography.bodySmall
            )
        }

        SectionCard("2. Calculadora Predictiva Interactiva: HINTS & HINTS+") {
            Text(
                "Seleccione los hallazgos exploratorios del paciente para definir el riesgo de origen central (fosa posterior isquémica/infarto o disección):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Head Impulse Test
            Text("A. Head Impulse Test (HIT) - Maniobra Impulso Cefálico", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { hitResult = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (hitResult == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Normal / Sin Sacada (Riesgo CENTRAL)", fontSize = 10.sp, color = if (hitResult==0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { hitResult = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (hitResult == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Anormal / Con Sacada (Riesgo Periférico)", fontSize = 10.sp, color = if (hitResult==1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Nota: En un vértigo por ACV, el arco reflejo vestíbulo-ocular suele estar intacto (HIT normal). ¡Un HIT normal en vértigo agudo es una alarma central masiva!", style = MaterialTheme.typography.labelSmall, fontStyle = FontStyle.Italic, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // 2. Nystagmus
            Text("B. Dirección del Nistagmo", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { nystagmusResult = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (nystagmusResult == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Dirección cambiante (CENTRAL)", fontSize = 10.sp, color = if (nystagmusResult==0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { nystagmusResult = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (nystagmusResult == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Unidireccional fijo (Periférico)", fontSize = 10.sp, color = if (nystagmusResult==1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 3. Test of Skew
            Text("C. Alineación Ocular Vertical - Test of Skew (Oclusión Alternante)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { skewResult = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (skewResult == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Desviación vertical SKEW (CENTRAL)", fontSize = 10.sp, color = if (skewResult==0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { skewResult = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (skewResult == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Alineación vertical normal (Periférico)", fontSize = 10.sp, color = if (skewResult==1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Hearing
            Text("D. Audición Aguda (Test HINTS+)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { hearingResult = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (hearingResult == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Sordera unilateral nueva (HINTS+ CENTRAL)", fontSize = 10.sp, color = if (hearingResult==0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { hearingResult = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (hearingResult == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Audición simétrica normal (Periférico)", fontSize = 10.sp, color = if (hearingResult==1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 5. Gait Ataxia
            Text("E. Capacidad de Caminar e Inestabilidad de la Marcha", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { canWalk = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!canWalk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Incapaz de caminar / Ataxia Grave (CENTRAL)", fontSize = 10.sp, color = if (!canWalk) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { canWalk = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (canWalk) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("Camina solo o con mínimo apoyo", fontSize = 10.sp, color = if (canWalk) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Diagnostic Output
            if (isCentral) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("⚠️ HINTS+ ALARMA DE ORIGEN CENTRAL (ALTO RIESGO DE ACV)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
                        val criteriaList = mutableListOf<String>()
                        if (hitResult == 0) criteriaList.add("impulso cefálico conservado (sin sacada correctora)")
                        if (nystagmusResult == 0) criteriaList.add("nistagmo bidireccional / cambia de fase al mirar a los lados")
                        if (skewResult == 0) criteriaList.add("alineación vertical aberrante (desviación de Skew)")
                        if (hearingResult == 0) criteriaList.add("pérdida aguda de audición unilateral")
                        if (!canWalk) criteriaList.add("incapacidad de deambular por ataxia estática de fosa posterior")
                        Text("Sospecha activa por: ${criteriaList.joinToString(", ")}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Acción recomendada: Hospitalizar. AngioTAC de cráneo y fosa posterior urgente + Resonancia Magnética con protocolo de difusión (DWI). Valorar ventana de trombólisis o trombectomía.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("✅ COMPATIBLE CON CAUSA VESTIBULAR PERIFÉRICA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                        Text("Reflejos HINTS+ sugieren etiología periférica benigna (ej. Neuritis Vestibular Aguda). HIT alterado con sacadas compensatorias rápidas de fijación, nistagmo unidireccional y alineación simétrica sin sordera ni ataxia severa.", style = MaterialTheme.typography.bodySmall)
                        Text("Acción: Manejo sintomático con antihistamínicos vestibulares por periodos cortos (ej. Dimenhidrinato) y terapia rehabilitadora vestíbulo-focal.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        SectionCard("3. Banderas Rojas y Disección Vertebral Cervical") {
            BulletText("Disección de Arteria Vertebral", "Dolor occipital o cervical unilateral agudo, seguido de mareo inespecífico, signos cerebelosos ipsilaterales, Horner (ptosis, miosis) bilateral o isquemia de tallo posterior. ¡Causa cardinal de infarto en jóvenes, frecuentemente gatillado por traumatismos leves, deportes o manipulación quiropráctica!")
            BulletText("ACV Cerebeloso", "La incapacidad de mantenerse en bipedestación o caminar por ataxia es el marcador clínico de descarte periférico más certero. Todo paciente con vértigo agudo que no puede caminar requiere Neuroimagen de inmediato, incluso si los nistagmos simulan perfil periférico.")
        }
    }
}

// ==========================================
// NEW COMPONENT C: CALCULADORAS VASCULARES (16 TOOLS)
// ==========================================
@Composable
fun CalculadorasVascularesView(onNavigateToDrug: (String) -> Unit) {
    var selectedToolId by remember { mutableStateOf("mrs") }
    val toolsList = listOf(
        "mrs" to "mRS (Rankin)",
        "barthel" to "Indice de Barthel",
        "abcd2" to "ABCD² (Riesgo TIA)",
        "abcd3_i" to "ABCD³-I (TIA Adv)",
        "cha2ds2" to "CHA₂DS₂-VASc",
        "has_bled" to "HAS-BLED",
        "same_tt2r2" to "SAMe-TT₂R₂",
        "abc2" to "ABC/2 (Volumen)",
        "ich" to "ICH Score",
        "func" to "FUNC Score",
        "hunt_hess" to "Hunt & Hess",
        "wfns" to "WFNS HSA",
        "fisher" to "Fisher Modificado",
        "phases" to "PHASES (Ruptura)",
        "uiats" to "UIATS (Aneurisma)",
        "cvst" to "CVST (Venosa)"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GuideHeader(
            title = "Panel de Calculadoras Vasculares",
            subtitle = "Soporte Interactivo de Decisiones para Ictus, HIC y HSA",
            source = "Guías de Consenso Científico Multicéntrico",
            updateInfo = "Actualizado 2026"
        )

        // Horizontal scrolling selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            toolsList.forEach { (id, label) ->
                val isSelected = selectedToolId == id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedToolId = id },
                    label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Render selected calculator
        when (selectedToolId) {
            "mrs" -> MrsCalculator()
            "barthel" -> BarthelCalculator()
            "abcd2" -> Abcd2Calculator()
            "abcd3_i" -> Abcd3iCalculator()
            "cha2ds2" -> Cha2ds2Calculator()
            "has_bled" -> HasBledCalculator()
            "same_tt2r2" -> SameTt2r2Calculator()
            "abc2" -> Abc2Calculator()
            "ich" -> IchCalculator()
            "func" -> FuncCalculator()
            "hunt_hess" -> HuntHessCalculator()
            "wfns" -> WfnsCalculator()
            "fisher" -> FisherModificadoCalculator()
            "phases" -> PhasesCalculator()
            "uiats" -> UiatsCalculator()
            "cvst" -> CvstCalculator()
        }
    }
}

// -------------------------------------------------------------
// IMPLEMENTATIONS OF 16 INDIVIDUAL CALCULATOR COMPOSABLES
// -------------------------------------------------------------

@Composable
fun MrsCalculator() {
    var selectedGrade by remember { mutableStateOf(0) }
    val grades = listOf(
        "Grado 0: Sin síntomas" to "Sin síntomas en absoluto, sin secuelas físicas ni cognitivas.",
        "Grado 1: Sin discapacidad significativa" to "Presenta síntomas leves pero es capaz de realizar todas sus tareas y actividades habituales.",
        "Grado 2: Discapacidad leve" to "Incapaz de realizar algunas actividades previas, pero puede velar por sí mismo sin asistencia constante.",
        "Grado 3: Discapacidad moderada" to "Requiere alguna ayuda externa, pero camina sin asistencia de otra persona.",
        "Grado 4: Discapacidad moderadamente severa" to "Incapaz de caminar sin ayuda o atender sus necesidades corporales básicas sin asistencia.",
        "Grado 5: Discapacidad severa" to "Espasticidad, postrado en cama, incontinente, requiere cuidados constantes y enfermería continuada.",
        "Grado 6: Muerte" to "Fallecido."
    )

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f))) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Escala de Rankin Modificada (mRS)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Medida estándar internacional para valorar el grado de discapacidad funcional y resultado neurológico post-ACV.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            grades.forEachIndexed { index, (label, desc) ->
                val isSel = selectedGrade == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGrade = index }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = isSel, onClick = { selectedGrade = index })
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Resultado Seleccionado: mRS $selectedGrade", fontWeight = FontWeight.Bold)
                    val outcome = when (selectedGrade) {
                        in 0..2 -> "Resultado funcional favorable / Independencia funcional de actividades cotidianas."
                        in 3..5 -> "Discapacidad severa. Requiere plan de rehabilitación agresivo y cuidados."
                        else -> "Óbito del paciente."
                    }
                    Text(outcome, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BarthelCalculator() {
    // 10 categories
    var feeding by remember { mutableStateOf(10) } // 0, 5, 10
    var bathing by remember { mutableStateOf(5) } // 0, 5
    var grooming by remember { mutableStateOf(5) } // 0, 5
    var dressing by remember { mutableStateOf(10) } // 0, 5, 10
    var bowels by remember { mutableStateOf(10) } // 0, 5, 10
    var bladder by remember { mutableStateOf(10) } // 0, 5, 10
    var toilet by remember { mutableStateOf(10) } // 0, 5, 10
    var transfers by remember { mutableStateOf(15) } // 0, 5, 10, 15
    var mobility by remember { mutableStateOf(15) } // 0, 5, 10, 15
    var stairs by remember { mutableStateOf(10) } // 0, 5, 10

    val totalScore = feeding + bathing + grooming + dressing + bowels + bladder + toilet + transfers + mobility + stairs

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Índice de Barthel (Independencia Funcional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Mide la independencia física autopercibida de un paciente en actividades básicas de la vida diaria (AVD). Escala total de 0 a 100 puntos.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            // Simplificamos con dropdowns o listas de clicks representadas conceptualmente
            Text("Responda cada uno de los ítems:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

            // Selectores compactos
            ScoreItemSelector("1. Comer / Alimentarse", feeding, listOf(0 to "Incapaz de comer", 5 to "Necesita ayuda de otros", 10 to "Totalmente independiente")) { feeding = it }
            ScoreItemSelector("2. Bañarse", bathing, listOf(0 to "Dependiente", 5 to "Independiente")) { bathing = it }
            ScoreItemSelector("3. Asearse / Arreglarse", grooming, listOf(0 to "Necesita ayuda", 5 to "Independiente para cara/manos/afeitarse")) { grooming = it }
            ScoreItemSelector("4. Vestirse", dressing, listOf(0 to "Dependiente corporal", 5 to "Necesita ayuda parcial", 10 to "Independiente completo")) { dressing = it }
            ScoreItemSelector("5. Deposición (Intestinos)", bowels, listOf(0 to "Incontinente o dependiente", 5 to "Accidente ocasional (semanal)", 10 to "Continente completo")) { bowels = it }
            ScoreItemSelector("6. Micción (Vejiga)", bladder, listOf(0 to "Incontinente", 5 to "Accidente ocasional (24h)", 10 to "Continente o autónomo con sonda")) { bladder = it }
            ScoreItemSelector("7. Uso del Retrete (Inodoro)", toilet, listOf(0 to "Dependiente", 5 to "Necesita ayuda parcial", 10 to "Independiente")) { toilet = it }
            ScoreItemSelector("8. Traslados (Camilla/Silla)", transfers, listOf(0 to "Incapaz / dependiente", 5 to "Gran ayuda (2 pers)", 10 to "Mínima ayuda (1 pers)", 15 to "Totalmente independiente")) { transfers = it }
            ScoreItemSelector("9. Deambulación / Movilidad", mobility, listOf(0 to "Incapaz", 5 to "Autónomo en silla de ruedas", 10 to "Camina con ayuda de 1 pers", 15 to "Independiente (con bastón/muletas)")) { mobility = it }
            ScoreItemSelector("10. Escaleras", stairs, listOf(0 to "Dependiente", 5 to "Necesita ayuda mecánica/humana", 10 to "Totalmente independiente")) { stairs = it }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Puntuación Final de Barthel: $totalScore / 100", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    val interpret = when {
                        totalScore >= 95 -> "Independencia Funcional Total"
                        totalScore in 60..90 -> "Discapacidad / Dependencia LEVE"
                        totalScore in 40..55 -> "Dependencia MODERADA"
                        totalScore in 20..35 -> "Dependencia SEVERA"
                        else -> "Dependencia TOTAL (Paciente encamado estático)"
                    }
                    Text("Interpretación: $interpret", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ScoreItemSelector(label: String, currentValue: Int, options: List<Pair<Int, String>>, onSelected: (Int) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (valScore, textDesc) ->
                val isSel = currentValue == valScore
                FilterChip(
                    selected = isSel,
                    onClick = { onSelected(valScore) },
                    label = { Text("$textDesc ($valScore pts)", fontSize = 9.sp) }
                )
            }
        }
    }
}

@Composable
fun Abcd2Calculator() {
    var ageOver60 by remember { mutableStateOf(false) }
    var bpElevated by remember { mutableStateOf(false) } // PAS>=140, PAD>=90
    var clinicalSimp by remember { mutableStateOf(0) } // 0: Otro, 1: Alt. Lenguaje sin debilidad, 2: Debilidad unilateral
    var duration by remember { mutableStateOf(0) } // 0: <10m, 1: 10-59m, 2: >=60m
    var diabetes by remember { mutableStateOf(false) }

    val score = (if (ageOver60) 1 else 0) +
                (if (bpElevated) 1 else 0) +
                (if (clinicalSimp == 1) 1 else if (clinicalSimp == 2) 2 else 0) +
                (if (duration == 1) 1 else if (duration == 2) 2 else 0) +
                (if (diabetes) 1 else 0)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ABCD² Score (Riesgo Temprano de Ictus post-AIT)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Estima el riesgo de presentar un infarto cerebral agudo dentro de los siguientes 2, 7 y 90 días después de un ataque isquémico transitorio (AIT).", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            Text("Marque los criterios clínicos:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            CalcCheckboxRow("Edad ≥ 60 años (1 pt)", ageOver60, { ageOver60 = it })
            CalcCheckboxRow("Presión Arterial ≥ 140/90 mmHg inicial (1 pt)", bpElevated, { bpElevated = it })
            CalcCheckboxRow("Antecedente conocido de Diabetes Mellitus (1 pt)", diabetes, { diabetes = it })

            Text("Clínica del AIT:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0 to "Ninguno (0)", 1 to "Alt. Lenguaje sin debilidad (1)", 2 to "Debilidad unilateral (2)").forEach { (v, txt) ->
                    FilterChip(selected = clinicalSimp == v, onClick = { clinicalSimp = v }, label = { Text(txt, fontSize = 9.sp) })
                }
            }

            Text("Duración de los síntomas:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0 to "< 10 Minutos (0)", 1 to "10 a 59 Minutos (1)", 2 to "≥ 60 Minutos (2)").forEach { (v, txt) ->
                    FilterChip(selected = duration == v, onClick = { duration = v }, label = { Text(txt, fontSize = 9.sp) })
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Puntaje ABCD² calculado: $score Puntos", fontWeight = FontWeight.Bold)
                    val riskLevel = when {
                        score in 0..3 -> "Bajo Riesgo (Riesgo ACV general 2 días = 1.0%). Adecuado estudio ambulatorio rápido."
                        score in 4..5 -> "Riesgo Moderado (Riesgo ACV general 2 días = 4.1%). Requiere hospitalizar u observación estricta."
                        else -> "Alto Riesgo (Riesgo ACV general 2 días = 8.1%). Cárguelo con doble antiagregación (DAPT) inmediata y hospitalice en Stroke Unit."
                    }
                    Text(riskLevel, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun Abcd3iCalculator() {
    var abcd2Points by remember { mutableStateOf("4") }
    var dualTia by remember { mutableStateOf(false) }
    var imagingPositive by remember { mutableStateOf(false) }
    var carotidStenosis by remember { mutableStateOf(false) }

    val abcd2Val = abcd2Points.toIntOrNull() ?: 0
    val totalScore = abcd2Val +
                (if (dualTia) 2 else 0) +
                (if (imagingPositive) 2 else 0) +
                (if (carotidStenosis) 2 else 0)

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Score ABCD³-I (Algoritmo de TIA Completo con Imagen)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Incopora hallazgos avanzados de neuroimagen por angiografía o RM con difusión (DWI), otorgando mayor sensibilidad ante la predicción de recurrencia.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            Text("Puntuación basal de ABCD² (0 a 7):", style = MaterialTheme.typography.labelSmall)
            OutlinedTextField(
                value = abcd2Points,
                onValueChange = { abcd2Points = it },
                modifier = Modifier.width(120.dp).height(48.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            CalcCheckboxRow("Episodio dual de TIA (recurrente) en la última semana (+2 pts)", dualTia, { dualTia = it })
            CalcCheckboxRow("Imagen por DWI con isquemia aguda en resonancia (+2 pts)", imagingPositive, { imagingPositive = it })
            CalcCheckboxRow("Estenosis mayor de arteria carótida ipsilateral ≥ 50% (+2 pts)", carotidStenosis, { carotidStenosis = it })

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Riesgo Total ABCD³-I: $totalScore / 13", fontWeight = FontWeight.Bold)
                    val recom = when {
                        totalScore >= 8 -> "⚠️ ALTA PREDICTIVIDAD DE RECURRENCIA. Acción: Ingreso inmediato a unidad de ictus y terapia doble antiagregación (DAPT) POINT/CHANCE."
                        totalScore in 4..7 -> "Riesgo Intermedio. Vigilancia monitorizada intrahospitalaria por ≥ 24-48 horas."
                        else -> "Riesgo Bajo. Evaluación etiológica ambulatoria precoz."
                    }
                    Text(recom, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun Cha2ds2Calculator() {
    var c by remember { mutableStateOf(false) } // CHF
    var h by remember { mutableStateOf(false) } // HTN
    var ageGts75 by remember { mutableStateOf(false) } // Age>=75 (+2)
    var d by remember { mutableStateOf(false) } // DM
    var s by remember { mutableStateOf(false) } // Stroke (+2)
    var v by remember { mutableStateOf(false) } // Vascular
    var age65_74 by remember { mutableStateOf(false) } // 65-74 (+1)
    var female by remember { mutableStateOf(false) } // Female (+1)

    val score = (if (c) 1 else 0) +
                (if (h) 1 else 0) +
                (if (ageGts75) 2 else if (age65_74) 1 else 0) +
                (if (d) 1 else 0) +
                (if (s) 2 else 0) +
                (if (v) 1 else 0) +
                (if (female) 1 else 0)

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("CHA₂DS₂-VASc (Riesgo Embólico de FA no Valvular)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Puntuación clínica de cribado para evaluar el riesgo tromboembólico en pacientes con fibrilación auricular (FA).", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            CalcCheckboxRow("Insuficiencia cardíaca congestiva o disfunción VI (1 pt)", c, { c = it })
            CalcCheckboxRow("Hipertensión arterial diagnosticada (1 pt)", h, { h = it })
            CalcCheckboxRow("Edad ≥ 75 años (2 pts)", ageGts75, { ageGts75 = it; if (it) age65_74 = false })
            CalcCheckboxRow("Edad entre 65 y 74 años (1 pt)", age65_74, { age65_74 = it; if (it) ageGts75 = false })
            CalcCheckboxRow("Diabetes Mellitus confirmada (1 pt)", d, { d = it })
            CalcCheckboxRow("Antecedente de ACV, TIA o tromboembolismo previo (2 pts)", s, { s = it })
            CalcCheckboxRow("Enfermedad vascular periférica, IAM previo o placa aórtica (1 pt)", v, { v = it })
            CalcCheckboxRow("Sexo femenino (1 pt)", female, { female = it })

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Resultado del CHA₂DS₂-VASc: $score Puntos", fontWeight = FontWeight.Bold)
                    val rec = when {
                        score >= 2 -> "INDICACIÓN ABSOLUTA DE ANTICOAGULACIÓN ORAL (preferir DOACs sobre Warfarina salvo prótesis mecánica o estenosis mitral severa) para reducción de ictus embola."
                        score == 1 && !female -> "Consideración clínica individualizada. Se sugiere anticoagulación."
                        else -> "Bajo Riesgo embólico. No requiere anticoagular (antiagregación sola no se aconseja por falta de beneficio preventivo neto)."
                    }
                    Text(rec, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HasBledCalculator() {
    var h by remember { mutableStateOf(false) } // Hypertension Systolic >160
    var renal by remember { mutableStateOf(false) } // Renal disease
    var liver by remember { mutableStateOf(false) } // Liver disease
    var stroke by remember { mutableStateOf(false) } // Stroke history
    var bleed by remember { mutableStateOf(false) } // Major bleed history
    var labile by remember { mutableStateOf(false) } // Labile INR
    var elderly by remember { mutableStateOf(false) } // Age >65
    var antipl by remember { mutableStateOf(false) } // NSAIDs/Antiplatelets
    var alcohol by remember { mutableStateOf(false) } // >8 drinks/week

    val score = (if (h) 1 else 0) +
                (if (renal) 1 else 0) +
                (if (liver) 1 else 0) +
                (if (stroke) 1 else 0) +
                (if (bleed) 1 else 0) +
                (if (labile) 1 else 0) +
                (if (elderly) 1 else 0) +
                (if (antipl) 1 else 0) +
                (if (alcohol) 1 else 0)

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Score HAS-BLED (Riesgo de Sangrado Mayor)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Estima el riesgo de hemorragia grave intrahospitalaria o ambulatoria de pacientes anticoagulados por fibrilación auricular.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            CalcCheckboxRow("Hipertensión no controlada (Sistólica > 160 mmHg) (1 pt)", h, { h = it })
            CalcCheckboxRow("Disfunción Renal Crítica (Dialisis o Cr > 2.26) (1 pt)", renal, { renal = it })
            CalcCheckboxRow("Disfunción Hepática Crítica (Cirrosis o LFTs >3x) (1 pt)", liver, { liver = it })
            CalcCheckboxRow("Historia previa de ACV isquémico o hemorrágico (1 pt)", stroke, { stroke = it })
            CalcCheckboxRow("Historia de hemorragia mayor previa o diátesis hemorrágica (1 pt)", bleed, { bleed = it })
            CalcCheckboxRow("INR lábil e inestable (TTR < 60% en tratamiento con warfarina) (1 pt)", labile, { labile = it })
            CalcCheckboxRow("Edad avanzada (mayor de 65 años) (1 pt)", elderly, { elderly = it })
            CalcCheckboxRow("Fármacos concomitantes que aumentan sangrado (Aspirina, AINEs) (1 pt)", antipl, { antipl = it })
            CalcCheckboxRow("Consumo excesivo de alcohol (≥ 8 bebidas por semana) (1 pt)", alcohol, { alcohol = it })

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Puntos HAS-BLED: $score / 9", fontWeight = FontWeight.Bold)
                    val interpret = when {
                        score >= 3 -> "⚠️ RIESGO ELEVADO DE SANGRADO EN ANTICOAGULADOS. Requiere precaución máxima, control estricto de PA (meta PAS < 130), suspender AINEs/antiagregantes redundantes y control mensual. ¡Ojo: No contraindica absolutamente la anticoagulación, exige mitigar factores corregibles!"
                        else -> "Bajo a moderado riesgo de sangrado. Perfil de anticoagulación seguro."
                    }
                    Text(interpret, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SameTt2r2Calculator() {
    var sexFemale by remember { mutableStateOf(false) }
    var ageUnder60 by remember { mutableStateOf(false) }
    var coMorbidities by remember { mutableStateOf(false) } // >= 2: HTN, DM, CAD, CHF, COPD, etc
    var rhythmControl by remember { mutableStateOf(false) }
    var tobacco2Yrs by remember { mutableStateOf(false) } // 2 pts
    var nonWhiteRace by remember { mutableStateOf(false) } // 2 pts

    val score = (if (sexFemale) 1 else 0) +
                (if (ageUnder60) 1 else 0) +
                (if (coMorbidities) 1 else 0) +
                (if (rhythmControl) 1 else 0) +
                (if (tobacco2Yrs) 2 else 0) +
                (if (nonWhiteRace) 2 else 0)

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("SAMe-TT₂R₂: Calidad de Anticoagulación Esperada", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Determina si un paciente con FA logrará un buen control del tiempo en rango terapéutico (TTR ≥ 65%) con antagonistas de la vitamina K (Warfarina).", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            CalcCheckboxRow("Femenina (1 pt)", sexFemale, { sexFemale = it })
            CalcCheckboxRow("Edad < 60 años (1 pt)", ageUnder60, { ageUnder60 = it })
            CalcCheckboxRow("Comorbilidades (≥ 2 patologías crónicas presentes) (1 pt)", coMorbidities, { coMorbidities = it })
            CalcCheckboxRow("Terapia de control de ritmo (ej. Amiodarona/Propafenona) (1 pt)", rhythmControl, { rhythmControl = it })
            CalcCheckboxRow("Consumo activo de tabaco (últimos 2 años) (2 pts)", tobacco2Yrs, { tobacco2Yrs = it })
            CalcCheckboxRow("Raza no caucásica (afrodescendientes, hispanos, etc) (2 pts)", nonWhiteRace, { nonWhiteRace = it })

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Marcador SAMe-TT₂R₂: $score Puntos", fontWeight = FontWeight.Bold)
                    val recom = when {
                        score >= 2 -> "❌ PREFERIR DOAC (Dabigatrán, Apixabán, Rivaroxabán). Predice mal control terapéutico del INR con Warfarina (TTR bajo y oscilante)."
                        else -> "✅ APTO PARA WARFARINA / ACO CLÁSICO. Es altamente probable que mantenga un TTR óptimo ≥ 65% y buena seguridad farmacológica."
                    }
                    Text(recom, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun Abc2Calculator() {
    var dimA by remember { mutableStateOf("") }
    var dimB by remember { mutableStateOf("") }
    var dimC by remember { mutableStateOf("") }

    val valA = dimA.toDoubleOrNull() ?: 0.0
    val valB = dimB.toDoubleOrNull() ?: 0.0
    val valC = dimC.toDoubleOrNull() ?: 0.0
    val volume = (valA * valB * valC) / 2.0

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Volumen de Hemorragia Intracerebral Aguda (Fórmula ABC/2)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Calcula de forma simplificada en centímetros el volumen real del hematoma intraparenquimatoso a partir de los cortes axiales de la tomografía (TAC).", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dimA, onValueChange = { dimA = it },
                    label = { Text("A (cm)", fontSize = 10.sp) }, modifier = Modifier.weight(1f).height(48.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = dimB, onValueChange = { dimB = it },
                    label = { Text("B (cm)", fontSize = 10.sp) }, modifier = Modifier.weight(1f).height(48.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = dimC, onValueChange = { dimC = it },
                    label = { Text("C (cm)", fontSize = 10.sp) }, modifier = Modifier.weight(1f).height(48.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Text("Guía C: Multiplique el número de cortes con sangre visible por el espesor del corte de la TAC. EJ: 6 cortes de 5mm = 3.0 cm.", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontStyle = FontStyle.Italic)

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Volumen Estimado del Hematoma: ${"%.2f".format(volume)} mL / cc", fontWeight = FontWeight.Bold)
                    val surgicalWarning = if (volume >= 30.0) {
                        "⚠️ VOLUMEN CRÍTICO (≥ 30 mL). Riesgo grave de hipertensión endocraneal, edema perilesional y muerte a corto plazo. Alta sospecha de herniación."
                    } else {
                        "Volumen moderado (< 30 mL). Requiere control estricto médico."
                    }
                    Text(surgicalWarning, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun IchCalculator() {
    var gcsVal by remember { mutableStateOf(3) } // 0-2 (0:13-15, 1:5-12, 2:3-4)
    var volGte30 by remember { mutableStateOf(false) } // Volume >= 30 mL (1)
    var ivhPresent by remember { mutableStateOf(false) } // IVH (1)
    var infraOrigin by remember { mutableStateOf(false) } // Infratentorial (1)
    var ageGte80 by remember { mutableStateOf(false) } // Age >= 80 (1)

    val gcsPoints = when {
        gcsVal in 13..15 -> 0
        gcsVal in 5..12 -> 1
        else -> 2
    }
    val score = gcsPoints +
                (if (volGte30) 1 else 0) +
                (if (ivhPresent) 1 else 0) +
                (if (infraOrigin) 1 else 0) +
                (if (ageGte80) 1 else 0)

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ICH Score (Mortalidad de Hemorragia Cerebral)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text("Clasificación de gravedad para estimar de forma precisa el porcentaje de mortalidad a los 30 días posteriores de sufrir una HIC espontánea.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            Text("Puntuación de Glasgow (GCS) actual del paciente:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(15 to "GCS 13-15 (0)", 10 to "GCS 5-12 (1)", 3 to "GCS 3-4 (2)").forEach { (v, txt) ->
                    FilterChip(selected = gcsVal == v, onClick = { gcsVal = v }, label = { Text(txt, fontSize = 9.sp) })
                }
            }

            CalcCheckboxRow("Volumen estimado del hematoma ≥ 30 mL (1 pt)", volGte30, { volGte30 = it })
            CalcCheckboxRow("Hemorragia Intraventricular (IVH) asociada (1 pt)", ivhPresent, { ivhPresent = it })
            CalcCheckboxRow("Estructura de origen infratentorial (cerebelo/tallo) (1 pt)", infraOrigin, { infraOrigin = it })
            CalcCheckboxRow("Paciente de edad avanzada (≥ 80 años de edad) (1 pt)", ageGte80, { ageGte80 = it })

            Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Puntaje ICH Total: $score / 6", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    val mortalityRate = when (score) {
                        0 -> "Mortalidad aproximada a 30 días: 0%"
                        1 -> "Mortalidad aproximada a 30 días: 13%"
                        2 -> "Mortalidad aproximada a 30 días: 26%"
                        3 -> "Mortalidad aproximada a 30 días: 72%"
                        4 -> "Mortalidad aproximada a 30 días: 97%"
                        else -> "Mortalidad extremadamente crítica de 100%"
                    }
                    Text(mortalityRate, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FuncCalculator() {
    var gcsVal by remember { mutableStateOf(15) } // >=13 (4), 9-12 (2), <9 (0)
    var ageVal by remember { mutableStateOf(65) } // <70 (2), 70-79 (1), >=80 (0)
    var location by remember { mutableStateOf(2) } // Lobar (2), Deep/Talamico/Caudado (1), Infratentorial (0)
    var volume by remember { mutableStateOf(20) } // <30 (4), 30-59 (2), >=60 (0)
    var cognitiveImpairment by remember { mutableStateOf(false) } // Pre-ICH cognitive impairment

    val gcsPoints = when {
        gcsVal >= 13 -> 4
        gcsVal in 9..12 -> 2
        else -> 0
    }
    val agePoints = when {
        ageVal < 70 -> 2
        ageVal in 70..79 -> 1
        else -> 0
    }
    val locPoints = location
    val volPoints = when {
        volume < 30 -> 4
        volume in 30..54 -> 2
        else -> 0
    }
    val cogPoints = if (cognitiveImpairment) 0 else 1

    val totalScore = gcsPoints + agePoints + locPoints + volPoints + cogPoints

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("FUNC Score (Independencia Funcional a 90 días en HIC)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Evalúa de manera rigurosa la probabilidad de que un paciente recupere la autonomía psicomotora e independencia funcional tras sufrir una hemorragia intracerebral.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            ScoreItemSelector("Escala de Glasgow (GCS):", gcsVal, listOf(15 to "GCS ≥ 13 (+4)", 10 to "GCS 9-12 (+2)", 3 to "GCS < 9 (+0)")) { gcsVal = it }
            ScoreItemSelector("Edad del paciente:", ageVal, listOf(60 to "< 70 años (+2)", 75 to "70-79 años (+1)", 85 to "≥ 80 años (+0)")) { ageVal = it }
            ScoreItemSelector("Localización del sangrado cerebral:", location, listOf(2 to "Lobar (+2)", 1 to "Subcortical / Profundo (+1)", 0 to "Infratentorial (+0)")) { location = it }
            ScoreItemSelector("Volumen estimado del hematoma:", volume, listOf(20 to "< 30 mL (+4)", 40 to "30-59 mL (+2)", 70 to "≥ 60 mL (+0)")) { volume = it }

            CalcCheckboxRow("Presentaba deterioro cognocitivo previo al Ictus (Demencia)", cognitiveImpairment, { cognitiveImpairment = it })

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("FUNC Score Result: $totalScore / 13", fontWeight = FontWeight.Bold)
                    val rate = when {
                        totalScore >= 11 -> "Independencia a 90 días predicha: ~80-87% (ALTA PROBABILIDAD DE RECUPERACIÓN)."
                        totalScore in 5..10 -> "Independencia a 90 días predicha: ~30-46% (Manejo de soporte agresivo en sala / UCI)."
                        else -> "Independencia a 90 días predicha: < 10% (Mal pronóstico global. Considerar medidas paliativas de confort si la familia consiente)."
                    }
                    Text(rate, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HuntHessCalculator() {
    var grade by remember { mutableStateOf(1) }
    val grades = listOf(
        "Grado 1" to "Asintomático o mínima cefalea, rigidez de nuca leve.",
        "Grado 2" to "Cefalea moderada a severa, rigidez de nuca, sin déficit neurológico focalizado salvo parálisis aislada de pares craneanos.",
        "Grado 3" to "Letargia, confusión moderada o leve déficit focal.",
        "Grado 4" to "Estupor, hemiparesia moderada o severa, rigidez temprana de descerebración.",
        "Grado 5" to "Coma profundo, rigidez de descerebración establecida, paciente moribundo."
    )

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Escala Hunt-Hess (HSA No Traumática Clínica)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Estima la severidad clínica basal de la hemorragia subaracnoidea aneurismática espontánea. A mayor grado, peor es la supervivencia.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            grades.forEachIndexed { idx, (lbl, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { grade = idx + 1 }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = grade == idx + 1, onClick = { grade = idx + 1 })
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(lbl, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Resultado Seleccionado: Hunt-Hess Grado $grade", fontWeight = FontWeight.Bold)
                    val rec = when (grade) {
                        in 1..2 -> "Grado favorable. Programar reparación del aneurisma hoy (coiling o clipaje) de urgencia para prevenir sangrado recurrente catastrófico."
                        3 -> "Grado intermedio. Requiere manejo riguroso en cuidados intensivos."
                        else -> "Grado desfavorable. Alta probabilidad de hidrocefalia o vasoespasmo grave secundario. Ubicar catéter de monitoreo de presión intracraneal (PIC) o drenaje externo."
                    }
                    Text(rec, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun WfnsCalculator() {
    var grade by remember { mutableStateOf(1) }
    val details = listOf(
        "Grado I" to "GCS = 15. Sin déficit motor.",
        "Grado II" to "GCS = 13 a 14. Sin déficit motor en exploración.",
        "Grado III" to "GCS = 13 a 14. Defecto motor focal presente.",
        "Grado IV" to "GCS = 7 a 12. Con o sin defecto motor focal.",
        "Grado V" to "GCS = 3 a 6. Con o sin rigidez o defecto motor."
    )

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Escala WFNS (World Federation of Neurosurgical Societies)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Clasificación estandarizada mundial de la severidad de la hemorragia subaracnoidea basada en el nivel de conciencia (GCS) y el déficit neurológico motor principal.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            details.forEachIndexed { index, (lbl, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { grade = index + 1 }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = grade == index + 1, onClick = { grade = index + 1 })
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(lbl, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("WFNS Clasificación: Grado $grade", fontWeight = FontWeight.Bold)
                    val action = when (grade) {
                        in 1..3 -> "Bajo Grado. Supervivencia esperada alta. Reparación precoz."
                        else -> "Alto Grado (Riesgo crítico de isquemia tardía). Monitorizar vasoespasmo mediante Doppler transcraneal y mantener pauta de Nimodipino estricta."
                    }
                    Text(action, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun FisherModificadoCalculator() {
    var grade by remember { mutableStateOf(1) }
    val categories = listOf(
        "Grado 0 o 1" to "Sin sangre en cisternas subaracnoideas, o escaso sangrado difuso menor sin hemorragia intraventricular.",
        "Grado 2" to "Presencia de sangrado difuso / mínimo en cisternas, ASOCIADO con hemorragia intraventricular bilateral (IVH).",
        "Grado 3" to "Sangrado CIS-TER-NAL masivo y grueso (sangre densa > 1 mm en cisternas) SIN hemorragia intraventricular.",
        "Grado 4" to "Sangrado grueso y persistente en cisternas (masivo) ASOCIADO con hemorragia intraventricular bilateral total (IVH)."
    )

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Escala Fisher Modificada (Riesgo de Vasoespasmo)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Clasificación radiológica de la hemorragia subaracnoidea en tomografía. Predice de forma exacta el riesgo de vasoespasmo sintomático tardío.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            categories.forEachIndexed { index, (lbl, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { grade = index + 1 }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = grade == index + 1, onClick = { grade = index + 1 })
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lbl, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Escala Fisher Calculado: Grado $grade", fontWeight = FontWeight.Bold)
                    val rate = when (grade) {
                        1 -> "Riesgo de vasoespasmo sintomático post-ictus: ~21% (Bajo)"
                        2 -> "Riesgo de vasoespasmo sintomático post-ictus: ~28% (Moderado)"
                        3 -> "Riesgo de vasoespasmo sintomático post-ictus: ~37% (Alto)"
                        else -> "Riesgo de vasoespasmo sintomático post-ictus: ~40% (Crítico / Máximo). Alerta uci permanente."
                    }
                    Text(rate, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PhasesCalculator() {
    var pRegion by remember { mutableStateOf(0) } // 0: Europa, 3: Japón, 4: Finlandia
    var htn by remember { mutableStateOf(false) } // 1 pt
    var ageGt70 by remember { mutableStateOf(false) } // 1 pt
    var sizeChoice by remember { mutableStateOf(0) } // 0: <5, 3: 5-6.9, 6: 7-9.9, 11: 10-19.9, 17: >=20
    var earlierSah by remember { mutableStateOf(false) } // 1 pt
    var location by remember { mutableStateOf(0) } // 0: ICA, 1: MCA, 4: ACA/PCoA/Posterior

    val score = pRegion +
                (if (htn) 1 else 0) +
                (if (ageGt70) 1 else 0) +
                sizeChoice +
                (if (earlierSah) 1 else 0) +
                location

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("PHASES Score (Riesgo Ruptura de Aneurisma no Roto)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Estima de forma precisa el porcentaje de riesgo acumulador de ruptura de un aneurisma intracraneal no roto a los 5 años.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            Text("Población e influencia demográfica:", style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0 to "Otro (0)", 3 to "Japón (3)", 4 to "Finlandia (4)").forEach { (v, txt) ->
                    FilterChip(selected = pRegion == v, onClick = { pRegion = v }, label = { Text(txt, fontSize = 9.sp) })
                }
            }

            CalcCheckboxRow("Historia clínica de Hipertensión Arterial (1 pt)", htn, { htn = it })
            CalcCheckboxRow("Edad de paciente mayor o igual a 70 años (1 pt)", ageGt70, { ageGt70 = it })
            CalcCheckboxRow("Antecedente personal de HSA por otro aneurisma roto (1 pt)", earlierSah, { earlierSah = it })

            Text("Diámetro máximo del aneurisma en imagen:", style = MaterialTheme.typography.labelSmall)
            Column {
                listOf(0 to "< 5.0 mm (0)", 3 to "5.0 a 6.9 mm (3)", 6 to "7.0 a 9.9 mm (6)", 11 to "10.0 a 19.9 mm (11)", 17 to "≥ 20.0 mm (17)").forEach { (v, txt) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { sizeChoice = v }) {
                        RadioButton(selected = sizeChoice == v, onClick = { sizeChoice = v })
                        Text(txt, fontSize = 10.sp)
                    }
                }
            }

            Text("Localización del saco aneurismático:", style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0 to "ICA (0)", 1 to "MCA (1)", 4 to "ACA / PCoA / Post (+4)").forEach { (v, txt) ->
                    FilterChip(selected = location == v, onClick = { location = v }, label = { Text(txt, fontSize = 9.sp) })
                }
            }

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Puntuación PHASES calculated: $score Puntos", fontWeight = FontWeight.Bold)
                    val ruptureRate = when {
                        score in 0..2 -> "Tasa de ruptura predicha a 5 años: < 0.4% (Riesgo muy bajo. Se sugiere seguimiento no invasivo anual)."
                        score in 3..4 -> "Tasa de ruptura a 5 años: ~0.9%. Vigilancia estrecha."
                        score in 5..7 -> "Tasa de ruptura a 5 años: ~2.4%. Considerar terapia intervencionista."
                        score in 8..11 -> "Tasa de ruptura a 5 años: ~6.0-9.8%. Alta indicación de reparación."
                        else -> "⚠️ RIESGO CRÍTICO COPROS. Tasa de ruptura a 5 años: ≥ 17.8%. Programar reparación quirúrgica o endovascular inmediata."
                    }
                    Text(ruptureRate, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UiatsCalculator() {
    var pointsRepair by remember { mutableStateOf(0) }
    var pointsCons by remember { mutableStateOf(0) }

    val recommendation = when {
        pointsRepair > pointsCons + 2 -> "Sugerencia Clínica: REPARAR EL ANEURISMA de forma electiva (Intervención endovascular o clipaje)."
        pointsCons > pointsRepair + 2 -> "Sugerencia Clínica: MANEJO CONSERVADOR y vigilancia armada anual de imagen."
        else -> "Zona gris de decisión médica compartida. Considerar miedos del paciente y metas vitales generales."
    }

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("UIATS (Unruptured Intracranial Aneurysm Treatment Score)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Apoya el debate entre reparar activamente un aneurisma cerebral no roto mediante neurocirugía frente al manejo conservador médico.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            Text("Puntos en favor de INTERVENCIÖN / REPARACIÒN:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0 to "Ninguno", 2 to "+2 Mód", 4 to "+4 Int", 7 to "+7 Int").forEach { (v, txt) ->
                    FilterChip(selected = pointsRepair == v, onClick = { pointsRepair = v }, label = { Text(txt, fontSize = 9.sp) })
                }
            }
            Text("Favorables a intervención: Edad joven (<60), tamaño ≥ 7mm, morfología irregular/lóbulo, crecimiento documentado, historia familiar de HSA.", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Puntos en favor de MANEJO CONSERVADOR:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0 to "Ninguno", 2 to "+2 Mód", 4 to "+4 Int", 7 to "+7 Int").forEach { (v, txt) ->
                    FilterChip(selected = pointsCons == v, onClick = { pointsCons = v }, label = { Text(txt, fontSize = 9.sp) })
                }
            }
            Text("Favorables a conservador: Edad mayor (>70), baja esperanza de vida, comorbilidades incapacitantes, aneurisma cavernoso asintomático.", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("UIATS Balance: Reparación ($pointsRepair pts) vs Conservador ($pointsCons pts)", fontWeight = FontWeight.Bold)
                    Text(recommendation, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CvstCalculator() {
    var genderFemale by remember { mutableStateOf(false) }
    var oralContraceptive by remember { mutableStateOf(false) }
    var pregnancyPostpartum by remember { mutableStateOf(false) }
    var activeCancer by remember { mutableStateOf(false) }
    var localInfection by remember { mutableStateOf(false) } // mastoiditis
    var prothromboticState by remember { mutableStateOf(false) }

    val riskFactorsCount = (if (genderFemale) 1 else 0) +
                           (if (oralContraceptive) 1 else 0) +
                           (if (pregnancyPostpartum) 2 else 0) +
                           (if (activeCancer) 2 else 0) +
                           (if (localInfection) 2 else 0) +
                           (if (prothromboticState) 3 else 0)

    val riskLevel = when {
        riskFactorsCount >= 4 -> "ALTO RIESGO"
        riskFactorsCount in 1..3 -> "Riesgo Moderado"
        else -> "Riesgo Bajo"
    }

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("CVST Risk Module (Trombosis Venosa Cerebral)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Módulo automatizado de triage para calificar el riesgo predictivo de sufrir una trombosis de senos venosos ante cefalea atípica o crisis recidivante.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)

            CalcCheckboxRow("Sexo femenino (1 pt)", genderFemale, { genderFemale = it })
            CalcCheckboxRow("Uso activo de anticonceptivos orales (1 pt)", oralContraceptive, { oralContraceptive = it })
            CalcCheckboxRow("Embarazada o en periodo de puerperio (2 pts)", pregnancyPostpartum, { pregnancyPostpartum = it })
            CalcCheckboxRow("Cáncer sistémico o de SNC activo (2 pts)", activeCancer, { activeCancer = it })
            CalcCheckboxRow("Infección otorrinolaríngea regional activa (Mastoiditis/Sinusitis) (2 pts)", localInfection, { localInfection = it })
            CalcCheckboxRow("Trombofilia conocida (Factor V, Mutación Protrombina, etc) (3 pts)", prothromboticState, { prothromboticState = it })

            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Grado de Riesgo Estimado: $riskLevel ($riskFactorsCount Puntos)", fontWeight = FontWeight.Bold)
                    if (riskFactorsCount >= 4) {
                        Text("Acción sugerida: Solicitar de inmediato AngioTAC Venosa o AngioRM de flujo venoso. Si se confirma, iniciar anticoagulación con Heparina de Bajo Peso Molecular (HBPM) incluso si hay transformación hemorrágica.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Sospecha baja. Evaluar otras causas de cefalea antes de imágenes complejas contrastadas.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}


