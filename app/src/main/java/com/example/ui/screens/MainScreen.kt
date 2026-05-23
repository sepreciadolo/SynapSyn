package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Volatile Room session cache database setup
    val appDb = AppRoomDatabase.getDatabase(context)
    val calculationDao = appDb.savedCalculationDao()
    val calculationRepository = remember { SavedCalculationRepository(calculationDao) }
    val savedCalculations by calculationRepository.allCalculations.collectAsState(initial = emptyList())

    // Prune calculations older than 12 hours on app launch
    LaunchedEffect(Unit) {
        calculationRepository.pruneOldCalculations()
    }

    // Navigation and search states
    var activeTab by remember { mutableStateOf(0) } // 0: Calculadoras, 1: Criterios, 2: Fármacos, 3: Otras Escalas
    var searchQuery by remember { mutableStateOf("") }

    // Selected items for active calculation
    var selectedNihssAnswers by remember { mutableStateOf(mapOf<String, Int>()) }
    var selectedAlsfrsrAnswers by remember { mutableStateOf(mapOf<String, Int>()) }
    var selectedQmgAnswers by remember { mutableStateOf(mapOf<String, Int>()) }
    var selectedDragonAnswers by remember { mutableStateOf(mapOf<String, Int>()) }

    // State definitions for newly added clinical calculators
    // ASPECTS: Ischemic areas map
    var selectedAspectsRegions by remember { mutableStateOf(setOf<String>()) }

    // ICH Score states
    var ichGcsScore by remember { mutableStateOf<Int?>(null) }
    var ichVolumeIsLarge by remember { mutableStateOf<Boolean?>(null) }
    var ichIvhPresent by remember { mutableStateOf<Boolean?>(null) }
    var ichInfratentorial by remember { mutableStateOf<Boolean?>(null) }
    var ichAge80Plus by remember { mutableStateOf<Boolean?>(null) }
    // Computational helper for volume
    var ichAValue by remember { mutableStateOf("") }
    var ichBValue by remember { mutableStateOf("") }
    var ichCValue by remember { mutableStateOf("") }

    // FOUR Score states
    var fourEye by remember { mutableStateOf<Int?>(null) }
    var fourMotor by remember { mutableStateOf<Int?>(null) }
    var fourBrainstem by remember { mutableStateOf<Int?>(null) }
    var fourRespiration by remember { mutableStateOf<Int?>(null) }

    // Kurtzke EDSS states
    var edssPyramidal by remember { mutableStateOf<Int?>(null) }
    var edssCerebellar by remember { mutableStateOf<Int?>(null) }
    var edssBrainstem by remember { mutableStateOf<Int?>(null) }
    var edssSensory by remember { mutableStateOf<Int?>(null) }
    var edssBowelBladder by remember { mutableStateOf<Int?>(null) }
    var edssVisual by remember { mutableStateOf<Int?>(null) }
    var edssCerebral by remember { mutableStateOf<Int?>(null) }
    var edssOther by remember { mutableStateOf<Int?>(null) }
    var edssMarchSelection by remember { mutableStateOf<String?>(null) }

    // Selected options for secondary quick scales
    var selectedMrsGrade by remember { mutableStateOf<Int?>(null) }
    var selectedFastStage by remember { mutableStateOf<Int?>(null) }
    var selectedMgfaClass by remember { mutableStateOf<Int?>(null) }

    // Diagnostic checklist states
    val goldCoastChecklist = remember { mutableStateMapOf<Int, Boolean>() }
    val trombolisisChecklist = remember { mutableStateMapOf<Int, Boolean>() }
    var selectedToastType by remember { mutableStateOf<Int?>(null) }
    var targetDrugFilter by remember { mutableStateOf("") }

    // Search action helper - jumps to exact content
    val onSearchMatchSelected: (String, Int) -> Unit = { id, tabIndex ->
        activeTab = tabIndex
        searchQuery = ""
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NeuroCompendio",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Global Search Box with Acronym matching
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar acrónimo o tema (e.g., NIHSS, ELA, TNK, TOAST)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding()
            ) {
                val tabs = listOf<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>(
                    Pair("Calcular", Icons.Default.Calculate),
                    Pair("Criterios", Icons.Default.FactCheck),
                    Pair("Tratamientos", Icons.Default.Medication),
                    Pair("Rápidas", Icons.Default.Bolt)
                )
                tabs.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = activeTab == index && searchQuery.isEmpty(),
                        onClick = { 
                            activeTab = index 
                            searchQuery = ""
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dropdown style result search overlay if there is a search query
            if (searchQuery.isNotEmpty()) {
                val queryLower = searchQuery.lowercase()
                
                // Perform quick searching across scales, criteria, drugs, and others
                val matchedScales = ClinicalDatabase.allScales.filter { 
                    it.name.lowercase().contains(queryLower) || it.acronym.lowercase().contains(queryLower) || it.category.lowercase().contains(queryLower)
                }
                val matchedCriteria = ClinicalDatabase.allCriteria.filter {
                    it.name.lowercase().contains(queryLower) || it.acronym.lowercase().contains(queryLower) || it.description.lowercase().contains(queryLower)
                }
                val matchedDrugs = ClinicalDatabase.drugs.filter {
                    it.name.lowercase().contains(queryLower) || it.acronym.lowercase().contains(queryLower) || it.indications.lowercase().contains(queryLower)
                }
                val matchedOthers = listOf(
                    Triple("FAST - Demencia", "Geriátria/Cognitiva", 3),
                    Triple("mRS - Rankin Modificado", "Vascular/Funcional", 3),
                    Triple("MGFA - Miastenia Gravis", "Placa Neuromuscular", 3)
                ).filter { it.first.lowercase().contains(queryLower) || it.second.lowercase().contains(queryLower) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Resultados de Búsqueda para \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    if (matchedScales.isEmpty() && matchedCriteria.isEmpty() && matchedDrugs.isEmpty() && matchedOthers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron coincidencias clínicas.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Show Scales
                    if (matchedScales.isNotEmpty()) {
                        item { Text("Calculadoras Clínicas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        items(matchedScales) { scale ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearchMatchSelected(scale.id, 0) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "${scale.acronym} - ${scale.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = scale.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
                            }
                        }
                    }

                    // Show Criteria
                    if (matchedCriteria.isNotEmpty()) {
                        item { Text("Criterios Diagnósticos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        items(matchedCriteria) { criteria ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearchMatchSelected(criteria.id, 1) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "${criteria.acronym} - ${criteria.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = criteria.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
                            }
                        }
                    }

                    // Show Drugs
                    if (matchedDrugs.isNotEmpty()) {
                        item { Text("Fármacos y Tratamientos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        items(matchedDrugs) { drug ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearchMatchSelected(drug.name, 2) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "${drug.name} (${drug.acronym})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "Dosificación: ${drug.dosage}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
                            }
                        }
                    }

                    // Show Others
                    if (matchedOthers.isNotEmpty()) {
                        item { Text("Otras Categorías y Escalas Rápidas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        items(matchedOthers) { other ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearchMatchSelected(other.first, other.third) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = other.first, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "Área: ${other.second}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            } else {
                // Regular tabs content
                when (activeTab) {
                    0 -> TabCalculadoras(
                        selectedNihssAnswers = selectedNihssAnswers,
                        onNihssAnswerChanged = { k, v -> selectedNihssAnswers = selectedNihssAnswers + (k to v) },
                        onResetNihss = { selectedNihssAnswers = emptyMap() },
                        selectedAlsfrsrAnswers = selectedAlsfrsrAnswers,
                        onAlsfrsrAnswerChanged = { k, v -> selectedAlsfrsrAnswers = selectedAlsfrsrAnswers + (k to v) },
                        onResetAlsfrsr = { selectedAlsfrsrAnswers = emptyMap() },
                        selectedQmgAnswers = selectedQmgAnswers,
                        onQmgAnswerChanged = { k, v -> selectedQmgAnswers = selectedQmgAnswers + (k to v) },
                        onResetQmg = { selectedQmgAnswers = emptyMap() },
                        selectedDragonAnswers = selectedDragonAnswers,
                        onDragonAnswerChanged = { k, v -> selectedDragonAnswers = selectedDragonAnswers + (k to v) },
                        onResetDragon = { selectedDragonAnswers = emptyMap() },
                        selectedAspectsRegions = selectedAspectsRegions,
                        onAspectsRegionsChanged = { selectedAspectsRegions = it },
                        onResetAspects = { selectedAspectsRegions = emptySet() },
                        ichGcsScore = ichGcsScore,
                        onIchGcsChanged = { ichGcsScore = it },
                        ichVolumeIsLarge = ichVolumeIsLarge,
                        onIchVolumeChanged = { ichVolumeIsLarge = it },
                        ichIvhPresent = ichIvhPresent,
                        onIchIvhChanged = { ichIvhPresent = it },
                        ichInfratentorial = ichInfratentorial,
                        onIchInfratentorialChanged = { ichInfratentorial = it },
                        ichAge80Plus = ichAge80Plus,
                        onIchAgeChanged = { ichAge80Plus = it },
                        ichAValue = ichAValue,
                        onIchAChanged = { ichAValue = it },
                        ichBValue = ichBValue,
                        onIchBChanged = { ichBValue = it },
                        ichCValue = ichCValue,
                        onIchCChanged = { ichCValue = it },
                        onResetIch = {
                            ichGcsScore = null
                            ichVolumeIsLarge = null
                            ichIvhPresent = null
                            ichInfratentorial = null
                            ichAge80Plus = null
                            ichAValue = ""
                            ichBValue = ""
                            ichCValue = ""
                        },
                        fourEye = fourEye,
                        onFourEyeChanged = { fourEye = it },
                        fourMotor = fourMotor,
                        onFourMotorChanged = { fourMotor = it },
                        fourBrainstem = fourBrainstem,
                        onFourBrainstemChanged = { fourBrainstem = it },
                        fourRespiration = fourRespiration,
                        onFourRespirationChanged = { fourRespiration = it },
                        onResetFour = {
                            fourEye = null
                            fourMotor = null
                            fourBrainstem = null
                            fourRespiration = null
                        },
                        edssPyramidal = edssPyramidal,
                        onEdssPyramidalChanged = { edssPyramidal = it },
                        edssCerebellar = edssCerebellar,
                        onEdssCerebellarChanged = { edssCerebellar = it },
                        edssBrainstem = edssBrainstem,
                        onEdssBrainstemChanged = { edssBrainstem = it },
                        edssSensory = edssSensory,
                        onEdssSensoryChanged = { edssSensory = it },
                        edssBowelBladder = edssBowelBladder,
                        onEdssBowelBladderChanged = { edssBowelBladder = it },
                        edssVisual = edssVisual,
                        onEdssVisualChanged = { edssVisual = it },
                        edssCerebral = edssCerebral,
                        onEdssCerebralChanged = { edssCerebral = it },
                        edssOther = edssOther,
                        onEdssOtherChanged = { edssOther = it },
                        edssMarchSelection = edssMarchSelection,
                        onEdssMarchChanged = { edssMarchSelection = it },
                        onResetEdss = {
                            edssPyramidal = null
                            edssCerebellar = null
                            edssBrainstem = null
                            edssSensory = null
                            edssBowelBladder = null
                            edssVisual = null
                            edssCerebral = null
                            edssOther = null
                            edssMarchSelection = null
                        },
                        onCopyClicked = { label, total, breakdown, conclusion ->
                            val textToCopy = if (label == ClinicalDatabase.nihss.name) {
                                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                val scoreNum = total.substringBefore(" ").filter { it.isDigit() }
                                "NIHSS $scoreNum puntos\n" +
                                    ClinicalDatabase.nihss.domains.joinToString(", ") { domain ->
                                        "${domain.id}: ${selectedNihssAnswers[domain.id] ?: 0}"
                                    } + "\n[Fecha/Hora: $timestamp]"
                            } else {
                                formatSmartCopy(label, total, breakdown, conclusion)
                            }
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "Copiado al portapapeles con éxito", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                calculationRepository.save(
                                    SavedCalculation(
                                        scaleId = label.lowercase().filter { it.isLetter() },
                                        scaleName = label,
                                        scoreText = total,
                                        interpretation = conclusion ?: "",
                                        details = breakdown.joinToString(", ")
                                    )
                                )
                            }
                        }
                    )
                    1 -> TabCriterios(
                        goldCoastChecklist = goldCoastChecklist,
                        trombolisisChecklist = trombolisisChecklist,
                        selectedToastType = selectedToastType,
                        onToastTypeSelected = { selectedToastType = it },
                        onNavigateToDrug = { drugName ->
                            targetDrugFilter = drugName
                            activeTab = 2 // Switch to TabFarmacos
                        },
                        onCopyClicked = { label, status, breakdown, conclusion ->
                            val textToCopy = formatSmartCopy(label, status, breakdown, conclusion)
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "Criterios copiados con éxito", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                calculationRepository.save(
                                    SavedCalculation(
                                        scaleId = "criterios_" + label.lowercase().filter { it.isLetter() },
                                        scaleName = label,
                                        scoreText = status,
                                        interpretation = conclusion ?: "",
                                        details = breakdown.joinToString(", ")
                                    )
                                )
                            }
                        }
                    )
                    2 -> TabFarmacos(
                        filterQuery = targetDrugFilter,
                        onFilterQueryChange = { targetDrugFilter = it },
                        onCopyClicked = { label, dose, details ->
                            val textToCopy = formatSmartCopy(label, dose, listOf(details))
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "Información de fármaco copiada", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                calculationRepository.save(
                                    SavedCalculation(
                                        scaleId = "farmaco_" + label.lowercase().filter { it.isLetter() },
                                        scaleName = label,
                                        scoreText = dose,
                                        interpretation = details,
                                        details = details
                                    )
                                )
                            }
                        }
                    )
                    3 -> TabOtrasEscalas(
                        selectedMrsGrade = selectedMrsGrade,
                        onMrsGradeSelected = { selectedMrsGrade = it },
                        selectedFastStage = selectedFastStage,
                        onFastStageSelected = { selectedFastStage = it },
                        selectedMgfaClass = selectedMgfaClass,
                        onMgfaClassSelected = { selectedMgfaClass = it },
                        recentHistoryContent = {
                            RecentHistorySection(
                                calculations = savedCalculations,
                                onClearClicked = {
                                    scope.launch { calculationRepository.clear() }
                                },
                                onCopyItemClicked = { calc ->
                                    val detailsList = calc.details.split(", ")
                                    val textToCopy = formatSmartCopy(calc.scaleName, calc.scoreText, detailsList, calc.interpretation)
                                    clipboardManager.setText(AnnotatedString(textToCopy))
                                    Toast.makeText(context, "Copiado al portapapeles con éxito", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onCopyClicked = { label, valStr, content ->
                            val textToCopy = formatSmartCopy(label, valStr, listOf(content))
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "Copiado con éxito", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                calculationRepository.save(
                                    SavedCalculation(
                                        scaleId = label.lowercase().filter { it.isLetter() },
                                        scaleName = label,
                                        scoreText = valStr,
                                        interpretation = content,
                                        details = content
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// Global Clean Smart-Copy Formatter conforming to clinical regulations
fun formatSmartCopy(
    label: String,
    resultScore: String,
    clinicalBreakdown: List<String>,
    diagnosticConclusion: String? = null
): String {
    val builder = StringBuilder()
    builder.append("=========================================\n")
    builder.append("REGISTRO EVALUADO: $label\n")
    builder.append("-----------------------------------------\n")
    builder.append("RESULTADO/PUNTAJE: $resultScore\n")
    if (clinicalBreakdown.isNotEmpty()) {
        builder.append("DESGLOSE CLÍNICO:\n")
        clinicalBreakdown.forEach { item ->
            builder.append("- $item\n")
        }
    }
    if (!diagnosticConclusion.isNullOrBlank()) {
        builder.append("\nCONCLUSIÓN DIAGNÓSTICA:\n")
        builder.append(diagnosticConclusion)
        builder.append("\n")
    }
    builder.append("=========================================")
    return builder.toString()
}

// ---------------------- TABS IMPLEMENTATIONS --------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalSelectorBlock(
    id: String,
    title: String,
    options: List<Pair<Int, String>>,
    selectedValue: Int?,
    onSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selector_$id"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { (value, text) ->
                    val isSelected = selectedValue == value
                    Surface(
                        modifier = Modifier
                            .clickable { onSelected(value) }
                            .testTag("${id}_opt_$value"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabCalculadoras(
    selectedNihssAnswers: Map<String, Int>,
    onNihssAnswerChanged: (String, Int) -> Unit,
    onResetNihss: () -> Unit,
    selectedAlsfrsrAnswers: Map<String, Int>,
    onAlsfrsrAnswerChanged: (String, Int) -> Unit,
    onResetAlsfrsr: () -> Unit,
    selectedQmgAnswers: Map<String, Int>,
    onQmgAnswerChanged: (String, Int) -> Unit,
    onResetQmg: () -> Unit,
    selectedDragonAnswers: Map<String, Int>,
    onDragonAnswerChanged: (String, Int) -> Unit,
    onResetDragon: () -> Unit,

    // ASPECTS parameters
    selectedAspectsRegions: Set<String>,
    onAspectsRegionsChanged: (Set<String>) -> Unit,
    onResetAspects: () -> Unit,

    // ICH parameters
    ichGcsScore: Int?,
    onIchGcsChanged: (Int?) -> Unit,
    ichVolumeIsLarge: Boolean?,
    onIchVolumeChanged: (Boolean?) -> Unit,
    ichIvhPresent: Boolean?,
    onIchIvhChanged: (Boolean?) -> Unit,
    ichInfratentorial: Boolean?,
    onIchInfratentorialChanged: (Boolean?) -> Unit,
    ichAge80Plus: Boolean?,
    onIchAgeChanged: (Boolean?) -> Unit,
    ichAValue: String,
    onIchAChanged: (String) -> Unit,
    ichBValue: String,
    onIchBChanged: (String) -> Unit,
    ichCValue: String,
    onIchCChanged: (String) -> Unit,
    onResetIch: () -> Unit,

    // FOUR parameters
    fourEye: Int?,
    onFourEyeChanged: (Int?) -> Unit,
    fourMotor: Int?,
    onFourMotorChanged: (Int?) -> Unit,
    fourBrainstem: Int?,
    onFourBrainstemChanged: (Int?) -> Unit,
    fourRespiration: Int?,
    onFourRespirationChanged: (Int?) -> Unit,
    onResetFour: () -> Unit,

    // EDSS parameters
    edssPyramidal: Int?,
    onEdssPyramidalChanged: (Int?) -> Unit,
    edssCerebellar: Int?,
    onEdssCerebellarChanged: (Int?) -> Unit,
    edssBrainstem: Int?,
    onEdssBrainstemChanged: (Int?) -> Unit,
    edssSensory: Int?,
    onEdssSensoryChanged: (Int?) -> Unit,
    edssBowelBladder: Int?,
    onEdssBowelBladderChanged: (Int?) -> Unit,
    edssVisual: Int?,
    onEdssVisualChanged: (Int?) -> Unit,
    edssCerebral: Int?,
    onEdssCerebralChanged: (Int?) -> Unit,
    edssOther: Int?,
    onEdssOtherChanged: (Int?) -> Unit,
    edssMarchSelection: String?,
    onEdssMarchChanged: (String?) -> Unit,
    onResetEdss: () -> Unit,

    onCopyClicked: (String, String, List<String>, String?) -> Unit
) {
    var activeCalculatorId by remember { mutableStateOf("nihss") }

    // --- STATES FOR NEW CALCULATORS ---
    // LCR
    var lcrLeukos by remember { mutableStateOf("") }
    var lcrPmn by remember { mutableStateOf("") }
    var lcrProteins by remember { mutableStateOf("") }
    var lcrGlucoseLcr by remember { mutableStateOf("") }
    var lcrGlucoseSerum by remember { mutableStateOf("") }
    var lcrErythros by remember { mutableStateOf("") }

    // EGRIS
    var egrisDays by remember { mutableStateOf<Int?>(null) }
    var egrisWeakness by remember { mutableStateOf<Int?>(null) }
    var egrisMrc by remember { mutableStateOf<Int?>(null) }

    // mEGOS
    var megosAge by remember { mutableStateOf<Int?>(null) }
    var megosDiarrhea by remember { mutableStateOf<Int?>(null) }
    var megosMrc by remember { mutableStateOf<Int?>(null) }

    // LEDD
    var leddLevodopa by remember { mutableStateOf("") }
    var leddPramipexol by remember { mutableStateOf("") }
    var leddRopinirol by remember { mutableStateOf("") }
    var leddRotigotine by remember { mutableStateOf("") }
    var leddEntacapone by remember { mutableStateOf(false) }
    var leddRasagilina by remember { mutableStateOf("") }
    var leddSafinamida by remember { mutableStateOf("") }

    // AIMS
    val aimsSelections = remember { mutableStateMapOf<Int, Int>().apply { (1..12).forEach { put(it, 0) } } }

    // MDS-UPDRS
    val updrsSelections = remember { mutableStateMapOf<Int, Int>().apply { (1..3).forEach { put(it, 0) } } }
    var metronomeActive by remember { mutableStateOf(false) }
    var metronomeHz by remember { mutableStateOf(1) }

    // Hoehn y Yahr
    var hoehnYahrSelected by remember { mutableStateOf<Double?>(null) }

    // Toxina
    var toxinaBrand by remember { mutableStateOf("botox") }
    var toxinaDilution by remember { mutableStateOf(2.0) }

    // Hachinski
    val hachinskiSelections = remember { mutableStateMapOf<Int, Boolean>() }

    // CDR
    var cdrMemoria by remember { mutableStateOf<Double?>(null) }
    var cdrOrientacion by remember { mutableStateOf<Double?>(null) }
    var cdrJuicio by remember { mutableStateOf<Double?>(null) }
    var cdrSocial by remember { mutableStateOf<Double?>(null) }
    var cdrHogar by remember { mutableStateOf<Double?>(null) }
    var cdrCuidado by remember { mutableStateOf<Double?>(null) }

    // GDS15
    val gdsSelections = remember { mutableStateMapOf<Int, Boolean>() }

    // 4AT Delirium
    var fouratAlertness by remember { mutableStateOf<Int?>(null) }
    var fouratAmt4 by remember { mutableStateOf<Int?>(null) }
    var fouratAttention by remember { mutableStateOf<Int?>(null) }
    var fouratAcute by remember { mutableStateOf<Int?>(null) }

    // MRC Sum Score
    val mrcLeft = remember { mutableStateMapOf<Int, Int>().apply { (1..6).forEach { put(it, 5) } } }
    val mrcRight = remember { mutableStateMapOf<Int, Int>().apply { (1..6).forEach { put(it, 5) } } }

    // MMT-8 Muscular
    val mmt8Selections = remember { mutableStateMapOf<Int, Int>().apply { (1..8).forEach { put(it, 10) } } }

    // Rabdomiolisis
    var rabdoWeight by remember { mutableStateOf("") }
    var rabdoCk by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Horizontally Scrollable Switch Tab Row for calculators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val calculators = listOf(
                    Pair("nihss", "NIHSS"),
                    Pair("aspects", "ASPECTS"),
                    Pair("ich", "ICH Score"),
                    Pair("four", "FOUR Score"),
                    Pair("edss", "EDSS"),
                    Pair("alsfrsr", "ALSFRS-R"),
                    Pair("qmg", "QMG"),
                    Pair("dragon", "DRAGON"),
                    Pair("lcr", "LCR Liquido"),
                    Pair("egris", "EGRIS (GBS)"),
                    Pair("megos", "mEGOS (GBS)"),
                    Pair("ledd", "Dosis LEDD"),
                    Pair("aims", "AIMS Escala"),
                    Pair("mdsupdrs", "MDS-UPDRS III"),
                    Pair("hoehn_yahr", "Hoehn y Yahr"),
                    Pair("toxina", "Dilución Toxina"),
                    Pair("hachinski", "Hachinski"),
                    Pair("cdr", "CDR Demencia"),
                    Pair("gds15", "GDS-15 Depresión"),
                    Pair("four_delirium", "4AT Delirium"),
                    Pair("mrc_sum", "MRC Sum Score"),
                    Pair("mmt8", "MMT-8 Muscular"),
                    Pair("rabdomiolisis", "Rabdomiólisis")
                )
                calculators.forEach { (id, label) ->
                    Button(
                        onClick = { activeCalculatorId = id },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeCalculatorId == id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeCalculatorId == id) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .testTag("switch_calc_$id")
                    ) {
                        Text(text = label, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeCalculatorId) {
                    "nihss" -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("calculator_intro_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "NIHSS (National Institutes of Health Stroke Scale)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ClinicalDatabase.nihss.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(ClinicalDatabase.nihss.domains) { domain ->
                            ScaleDomainSelectionBlock(
                                id = domain.id,
                                label = domain.label,
                                description = domain.description,
                                options = domain.options,
                                selectedValue = selectedNihssAnswers[domain.id] ?: 0,
                                onOptionSelected = { onNihssAnswerChanged(domain.id, it) }
                            )
                        }
                    }
                    "aspects" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "ASPECTS (Alberta Stroke Program Early CT Score)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Escala de 10 puntos para valorar la isquemia en el territorio de la ACM en la TAC. Cada área afectada resta 1 punto.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            AspectsVisualizer(
                                selectedRegions = selectedAspectsRegions,
                                onRegionToggled = { regId ->
                                    val isSelected = selectedAspectsRegions.contains(regId)
                                    onAspectsRegionsChanged(if (isSelected) selectedAspectsRegions - regId else selectedAspectsRegions + regId)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Mapa Territorial ACM (Toque las áreas isquémicas / infartadas)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // Frontal superior cortical regions rule
                                    Text("Territorios Corticales Superiores:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("M4" to "Ant-Sup", "M5" to "Lat-Sup", "M6" to "Post-Sup").forEach { (regId, name) ->
                                            val isSelected = selectedAspectsRegions.contains(regId)
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        onAspectsRegionsChanged(if (isSelected) selectedAspectsRegions - regId else selectedAspectsRegions + regId)
                                                    }
                                                    .height(55.dp)
                                                    .testTag("aspects_${regId}"),
                                                color = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(regId, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(name, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Frontal inferior cortical regions rule
                                    Text("Territorios Corticales Inferiores:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("M1" to "Ant-Inf", "M2" to "Lat-Inf", "M3" to "Post-Inf").forEach { (regId, name) ->
                                            val isSelected = selectedAspectsRegions.contains(regId)
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        onAspectsRegionsChanged(if (isSelected) selectedAspectsRegions - regId else selectedAspectsRegions + regId)
                                                    }
                                                    .height(55.dp)
                                                    .testTag("aspects_${regId}"),
                                                color = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(regId, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(name, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Deep / Subcortical deep structure maps
                                    Text("Sustancia Gris Subcortical (Profundo):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("C" to "Caudado", "L" to "Lenticular", "IC" to "Cáps.Int", "I" to "Rib.Ins").forEach { (regId, name) ->
                                            val isSelected = selectedAspectsRegions.contains(regId)
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        onAspectsRegionsChanged(if (isSelected) selectedAspectsRegions - regId else selectedAspectsRegions + regId)
                                                    }
                                                    .height(55.dp)
                                                    .testTag("aspects_${regId}"),
                                                color = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(regId, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(name, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, maxLines = 1, color = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "ich" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "ICH Score (Intracerebral Hemorrhage Score)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Clasificación diagnóstica y pronóstica de mortalidad a 30 días para hemorragia intracerebral espontánea.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Computadora integrada del volumen con formula ABC/2 de hematoma
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Calculador del volumen del hematoma (Fórmula A × B × C / 2)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Ingrese diámetros en cm en cortes tomográficos:", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = ichAValue,
                                            onValueChange = { onIchAChanged(it) },
                                            placeholder = { Text("A") },
                                            label = { Text("A (cm)") },
                                            modifier = Modifier.weight(1f).testTag("ich_a"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = ichBValue,
                                            onValueChange = { onIchBChanged(it) },
                                            placeholder = { Text("B") },
                                            label = { Text("B (cm)") },
                                            modifier = Modifier.weight(1f).testTag("ich_b"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = ichCValue,
                                            onValueChange = { onIchCChanged(it) },
                                            placeholder = { Text("C") },
                                            label = { Text("C (cm)") },
                                            modifier = Modifier.weight(1f).testTag("ich_c"),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    val aVal = ichAValue.toDoubleOrNull()
                                    val bVal = ichBValue.toDoubleOrNull()
                                    val cVal = ichCValue.toDoubleOrNull()
                                    val calculatedVolume = if (aVal != null && bVal != null && cVal != null) {
                                        (aVal * bVal * cVal) / 2.0
                                    } else null

                                    if (calculatedVolume != null) {
                                        val isVolLarge = calculatedVolume >= 30.0
                                        val volStr = String.format(java.util.Locale.US, "%.2f", calculatedVolume)
                                        Column {
                                            Text(
                                                text = "Volumen: $volStr cm³ (${if (isVolLarge) ">= 30 cm³ [1 pt]" else "< 30 cm³ [0 pt]"})",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isVolLarge) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { onIchVolumeChanged(isVolLarge) },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Usar este volumen", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    } else {
                                        Text("Complete los diámetros para autocomputar el volumen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }

                        // Clinical questions using our generic block Composable
                        item {
                            ClinicalSelectorBlock(
                                id = "ich_gcs",
                                title = "1. Escala de Coma de Glasgow inicial",
                                options = listOf(2 to "GCS 3 a 4 (+2 pts)", 1 to "GCS 5 a 12 (+1 pt)", 0 to "GCS 13 a 15 (+0 pts)"),
                                selectedValue = ichGcsScore,
                                onSelected = onIchGcsChanged
                            )
                        }
                        item {
                            ClinicalSelectorBlock(
                                id = "ich_volume",
                                title = "2. Volumen del hematoma (intracerebral)",
                                options = listOf(1 to ">= 30 cm³ (+1 pt)", 0 to "< 30 cm³ (+0 pts)"),
                                selectedValue = ichVolumeIsLarge?.let { if (it) 1 else 0 },
                                onSelected = { onIchVolumeChanged(it == 1) }
                            )
                        }
                        item {
                            ClinicalSelectorBlock(
                                id = "ich_ivh",
                                title = "3. Sangre Intraventricular (HVE)",
                                options = listOf(1 to "HVE Presente (+1 pt)", 0 to "HVE Ausente (+0 pts)"),
                                selectedValue = ichIvhPresent?.let { if (it) 1 else 0 },
                                onSelected = { onIchIvhChanged(it == 1) }
                            )
                        }
                        item {
                            ClinicalSelectorBlock(
                                id = "ich_infra",
                                title = "4. Origen cortical infratentorial",
                                options = listOf(1 to "Infratentorial (+1 pt)", 0 to "Supratentorial (+0 pts)"),
                                selectedValue = ichInfratentorial?.let { if (it) 1 else 0 },
                                onSelected = { onIchInfratentorialChanged(it == 1) }
                            )
                        }
                        item {
                            ClinicalSelectorBlock(
                                id = "ich_age",
                                title = "5. Edad del paciente",
                                options = listOf(1 to ">= 80 años (+1 pt)", 0 to "< 80 años (+0 pts)"),
                                selectedValue = ichAge80Plus?.let { if (it) 1 else 0 },
                                onSelected = { onIchAgeChanged(it == 1) }
                            )
                        }
                    }
                    "four" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "FOUR Score (Full Outline of UnResponsiveness)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Escala de elección para coma y pacientes críticos intubados. Valora reflejos de tallo y patrón de respirador.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            ClinicalSelectorBlock(
                                id = "four_eye",
                                title = "Respuesta Ocular (E - Eye):",
                                options = listOf(
                                    4 to "4 - Abre ojos, sigue con la mirada o parpadea",
                                    3 to "3 - Abre ojos, fijación visual ausente",
                                    2 to "2 - Abre ojos ante grito/estímulo auditivo",
                                    1 to "1 - Abre de forma refleja ante estímulo doloroso",
                                    0 to "0 - No responde / ojos cerrados al dolor"
                                ),
                                selectedValue = fourEye,
                                onSelected = onFourEyeChanged
                            )
                        }
                        item {
                            ClinicalSelectorBlock(
                                id = "four_motor",
                                title = "Respuesta Motora (M - Motor):",
                                options = listOf(
                                    4 to "4 - Sigue órdenes con las manos (Signo de victoria/paz)",
                                    3 to "3 - Localiza el estímulo doloroso",
                                    2 to "2 - Flexión anormal (Postura de Decorticación)",
                                    1 to "1 - Extensión refleja (Postura de Descerebración)",
                                    0 to "0 - Flacidez / sin respuesta al dolor o mioclonías"
                                ),
                                selectedValue = fourMotor,
                                onSelected = onFourMotorChanged
                            )
                        }
                        item {
                            ClinicalSelectorBlock(
                                id = "four_stem",
                                title = "Reflejos de Tronco Encefálico (B - Brainstem):",
                                options = listOf(
                                    4 to "4 - Reflejos corneales y pupilares bilaterales",
                                    3 to "3 - Una pupila dilatada y fija (Anisocoria de enclavamiento)",
                                    2 to "2 - Reflejos pupilares O corneales ausentes",
                                    1 to "1 - Reflejos pupilares Y corneales totalmente ausentes",
                                    0 to "0 - Reflejos corneal, pupilar y reflejo tusígeno (tos) ausentes"
                                ),
                                selectedValue = fourBrainstem,
                                onSelected = onFourBrainstemChanged
                            )
                        }
                        item {
                            ClinicalSelectorBlock(
                                id = "four_resp",
                                title = "Patrón Respiratorio (R - Respiration):",
                                options = listOf(
                                    4 to "4 - No intubado, patrón respiratorio normal/regular",
                                    3 to "3 - No intubado, respiración de Cheyne-Stokes",
                                    2 to "2 - No intubado, patrón irregular",
                                    1 to "1 - Intubado, gata y respira sobre el respirador (iniciador)",
                                    0 to "0 - Intubado, dependiente completo del ventilador o apnea"
                                ),
                                selectedValue = fourRespiration,
                                onSelected = onFourRespirationChanged
                            )
                        }
                    }
                    "edss" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "EDSS (Expanded Disability Status Scale) de Kurtzke",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Calificador de discapacidad funcional en Esclerosis Múltiple. Ingrese estados de los 8 Sistemas Funcionales (0 a N) y Deambulación.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Ingrese puntajes de Sistemas Funcionales (SF):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val sfList = listOf(
                                        Triple("edss_pyr", "Piramidal (0-6)", edssPyramidal to onEdssPyramidalChanged),
                                        Triple("edss_cer", "Cerebeloso (0-5)", edssCerebellar to onEdssCerebellarChanged),
                                        Triple("edss_bst", "Tronco Encefálico (0-5)", edssBrainstem to onEdssBrainstemChanged),
                                        Triple("edss_sen", "Sensitivo (0-6)", edssSensory to onEdssSensoryChanged),
                                        Triple("edss_bow", "Vejiga/Intestino (0-6)", edssBowelBladder to onEdssBowelBladderChanged),
                                        Triple("edss_vis", "Visual (0-6)", edssVisual to onEdssVisualChanged),
                                        Triple("edss_cbl", "Mental / Cerebral (0-5)", edssCerebral to onEdssCerebralChanged),
                                        Triple("edss_oth", "Otros (0-1)", edssOther to onEdssOtherChanged)
                                    )

                                    sfList.forEach { (sfId, label, statePair) ->
                                        val (value, setter) = statePair
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                                Text(text = value?.toString() ?: "No asignado", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                val maxRange = if (sfId == "edss_oth") 1 else if (sfId == "edss_pyr" || sfId == "edss_sen" || sfId == "edss_bow" || sfId == "edss_vis") 6 else 5
                                                (0..maxRange).forEach { idx ->
                                                    val isSelected = value == idx
                                                    Surface(
                                                        modifier = Modifier.clickable { setter(idx) },
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                    ) {
                                                        Text(idx.toString(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            ClinicalSelectorBlock(
                                id = "edss_marcha",
                                title = "Capacidad de Marcha / Deambulación:",
                                options = listOf(
                                    0 to "Normal (Sin restricciones, libre >500m)",
                                    1 to "Marcha 300-500m sin ayuda",
                                    2 to "Marcha 200-300m sin ayuda",
                                    3 to "Marcha 100-200m sin ayuda",
                                    4 to "Requiere apoyo unilateral ~100m (bastón/muleta)",
                                    5 to "Requiere dos apoyos / andador ~120m",
                                    6 to "Silla de ruedas (desplazamiento autónomo con ayuda)",
                                    7 to "Silla de ruedas, requiere ayuda transferencias",
                                    8 to "En cama / silla de ruedas dependiente completo",
                                    9 to "Estadío vegetativo vegetante / encamado inmóvil",
                                    10 to "Fallecimiento adjudicable a EM"
                                ),
                                selectedValue = edssMarchSelection?.toIntOrNull(),
                                onSelected = { onEdssMarchChanged(it.toString()) }
                            )
                        }
                    }
                    "alsfrsr" -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("calculator_intro_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "ALSFRS-R (Revised ALS Functional Rating Scale)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ClinicalDatabase.alsfrsr.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(ClinicalDatabase.alsfrsr.domains) { domain ->
                            ScaleDomainSelectionBlock(
                                id = domain.id,
                                label = domain.label,
                                description = domain.description,
                                options = domain.options,
                                selectedValue = selectedAlsfrsrAnswers[domain.id] ?: 4,
                                onOptionSelected = { onAlsfrsrAnswerChanged(domain.id, it) }
                            )
                        }
                    }
                    "qmg" -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("calculator_intro_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "QMG (Quantitative Myasthenia Gravis)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ClinicalDatabase.qmg.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(ClinicalDatabase.qmg.domains) { domain ->
                            ScaleDomainSelectionBlock(
                                id = domain.id,
                                label = domain.label,
                                description = domain.description,
                                options = domain.options,
                                selectedValue = selectedQmgAnswers[domain.id] ?: 0,
                                onOptionSelected = { onQmgAnswerChanged(domain.id, it) }
                            )
                        }
                    }
                    "dragon" -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("calculator_intro_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "DRAGON Score",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ClinicalDatabase.dragon.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(ClinicalDatabase.dragon.domains) { domain ->
                            ScaleDomainSelectionBlock(
                                id = domain.id,
                                label = domain.label,
                                description = domain.description,
                                options = domain.options,
                                selectedValue = selectedDragonAnswers[domain.id] ?: 0,
                                onOptionSelected = { onDragonAnswerChanged(domain.id, it) }
                            )
                        }
                    }
                    "lcr" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("LCR / Análisis Citoquímico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Ingrese los valores de citoquímico de LCR para interpretación automatizada de meningoencefalitis y corrección de pleocitosis.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(value = lcrLeukos, onValueChange = { lcrLeukos = it }, label = { Text("Leucocitos (células/µL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = lcrPmn, onValueChange = { lcrPmn = it }, label = { Text("Porcentaje PMN (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = lcrProteins, onValueChange = { lcrProteins = it }, label = { Text("Proteínas en LCR (mg/dL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = lcrGlucoseLcr, onValueChange = { lcrGlucoseLcr = it }, label = { Text("Glucosa en LCR (mg/dL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = lcrGlucoseSerum, onValueChange = { lcrGlucoseSerum = it }, label = { Text("Glucosa Sérica (mg/dL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = lcrErythros, onValueChange = { lcrErythros = it }, label = { Text("Eritrocitos (para punción traumática, células/µL)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                    "egris" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("EGRIS (GBS Respiratory Insufficiency Score)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Predice la probabilidad de requerir ventilación mecánica durante la primera semana de hospitalización por Guillain-Barré.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("1. Días desde el inicio de debilidad hasta el ingreso:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("> 7 días (0)", "4 - 7 días (1)", "< 4 días (2)").forEachIndexed { idx, txt ->
                                            val isSel = egrisDays == idx
                                            Button(
                                                onClick = { egrisDays = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("2. Debilidad facial y/o bulbar asociada al ingreso:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("Ausente (0)", "Presente (1)").forEachIndexed { idx, txt ->
                                            val isSel = egrisWeakness == idx
                                            Button(
                                                onClick = { egrisWeakness = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("3. MRC Sum Score al ingreso:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("51-60 (0)", "41-50 (1)", "31-40 (2)", "21-30 (3)", "11-20 (4)", "0-10 (5)").forEachIndexed { idx, txt ->
                                            val isSel = egrisMrc == idx
                                            ElevatedButton(
                                                onClick = { egrisMrc = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "megos" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("mEGOS (Modified Erasmus GBS Outcome Score)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Predice la probabilidad de deambular de forma independiente a las 4 semanas, 3 meses y 6 meses.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("1. Edad al ingreso:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("<= 40 años (0)", "41-60 años (1)", "> 60 años (2)").forEachIndexed { idx, txt ->
                                            val isSel = megosAge == idx
                                            Button(
                                                onClick = { megosAge = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("2. Antecedente reciente de Diarrea (<4 semanas):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("No (0)", "Sí (1)").forEachIndexed { idx, txt ->
                                            val isSel = megosDiarrhea == idx
                                            Button(
                                                onClick = { megosDiarrhea = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("3. MRC Sum Score al ingreso o Día 1:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("51-60 (0)", "41-50 (1)", "31-40 (2)", "21-30 (3)", "11-20 (4)", "0-10 (5)").forEachIndexed { idx, txt ->
                                            val isSel = megosMrc == idx
                                            ElevatedButton(
                                                onClick = { megosMrc = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "ledd" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("LEDD (Dosis Diaria Equivalente de Levodopa)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Calculadora de equivalencias de fármacos dopaminérgicos para Enfermedad de Parkinson.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(value = leddLevodopa, onValueChange = { leddLevodopa = it }, label = { Text("Levodopa / Carbidopa dosis diaria (mg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = leddEntacapone, onCheckedChange = { leddEntacapone = it })
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("¿Se co-administra con Entacapona? (Co-inhibidor +33% Levodopa)", style = MaterialTheme.typography.bodySmall)
                                    }
                                    OutlinedTextField(value = leddPramipexol, onValueChange = { leddPramipexol = it }, label = { Text("Pramipexole dosis diaria (mg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = leddRopinirol, onValueChange = { leddRopinirol = it }, label = { Text("Ropinirole dosis diaria (mg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = leddRotigotine, onValueChange = { leddRotigotine = it }, label = { Text("Parches de Rotigotina diaria (mg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = leddRasagilina, onValueChange = { leddRasagilina = it }, label = { Text("Rasagilina dosis diaria (mg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = leddSafinamida, onValueChange = { leddSafinamida = it }, label = { Text("Safinamida dosis diaria (mg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                    "aims" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Escala AIMS (Abnormal Involuntary Movement Scale)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Evaluación sistemática de movimientos involuntarios de discinesia tardía. Califique cada ítem de 0 (Ninguno) a 4 (Severo).", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        val itemsList = listOf(
                            "1. Movimientos orofaciales (músculos de la expresión)",
                            "2. Labios y área perioral (fruncimiento, chupeteo)",
                            "3. Mandíbula (masticación, protrusión)",
                            "4. Lengua (protrusión, movimientos rápidos, 'dardo')",
                            "5. Extremidades superiores (brazos, manos, dedos)",
                            "6. Extremidades inferiores (piernas, pies, flexión de dedos)",
                            "7. Movimientos del tronco (cuello, hombros, balanceo pélvico)",
                            "8. Evaluación global de la severidad del temblor/discinesia",
                            "9. Grado de incapacidad funcional del paciente por movimientos",
                            "10. Grado de conciencia subjetiva del paciente de sus propios movimientos",
                            "11. Problemas dentales asociados o dentaduras flojas",
                            "12. ¿Lleva prótesis dentales actualmente? (0: No, 1: Sí)"
                        )
                        items(itemsList.size) { index ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(itemsList[index], style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (0..4).forEach { score ->
                                            val isSel = aimsSelections[index + 1] == score
                                            Button(
                                                onClick = { aimsSelections[index + 1] = score },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(score.toString(), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "mdsupdrs" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("MDS-UPDRS Parte III (Subset Motor + Metrónomo)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Evaluación bedside simplificada de bradicinesia con metrónomo visual sincronizado para guiar el examen motor.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("METRÓNOMO VISUAL PARA EXAMEN", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { metronomeActive = !metronomeActive }, colors = ButtonDefaults.buttonColors(containerColor = if (metronomeActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)) {
                                            Text(if (metronomeActive) "Detener Metrónomo" else "Iniciar Metrónomo")
                                        }
                                        Button(onClick = { metronomeHz = 1 }, colors = ButtonDefaults.buttonColors(containerColor = if (metronomeHz == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (metronomeHz == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant)) {
                                            Text("1 Hz")
                                        }
                                        Button(onClick = { metronomeHz = 2 }, colors = ButtonDefaults.buttonColors(containerColor = if (metronomeHz == 2) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (metronomeHz == 2) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant)) {
                                            Text("2 Hz")
                                        }
                                    }
                                    if (metronomeActive) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        val infiniteTransition = rememberInfiniteTransition(label = "metronomePulse")
                                        val scale by infiniteTransition.animateFloat(
                                            initialValue = 0.5f,
                                            targetValue = 1.3f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(if (metronomeHz == 1) 1000 else 500, easing = LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "metronomePulseScale"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                                .background(MaterialTheme.colorScheme.onErrorContainer, shape = androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Pulsando ritmo a ${if (metronomeHz == 1) "60" else "120"} BPM", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                                    }
                                }
                            }
                        }
                        val tasks = listOf(
                            "3.4 Golpeteo de dedos (Finger tapping, alternando con máxima amplitud)",
                            "3.5 Movimientos de las manos (abrir/cerrar de forma sucesiva)",
                            "3.6 Pronación-supinación de las manos (brazos extendidos, rápido)"
                        )
                        items(tasks.size) { index ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(tasks[index], style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (0..4).forEach { score ->
                                            val isSel = updrsSelections[index + 1] == score
                                            Button(
                                                onClick = { updrsSelections[index + 1] = score },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(score.toString(), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "hoehn_yahr" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Escala Modificada de Hoehn y Yahr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Estadificación de la gravedad y progresión de los síntomas de la Enfermedad de Parkinson.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        val stages = listOf(
                            Pair(1.0, "Estadío 1.0: Afectación únicamente unilateral. Discapacidad funcional nula o mínima."),
                            Pair(1.5, "Estadío 1.5: Afectación unilateral y axial asociada, sin alteración del equilibrio."),
                            Pair(2.0, "Estadío 2.0: Afectación bilateral o axial, sin alteración de la inestabilidad postural o equilibrio."),
                            Pair(2.5, "Estadío 2.5: Afectación bilateral leve; el paciente puede recuperar el equilibrio en pull-test."),
                            Pair(3.0, "Estadío 3.0: Afectación bilateral leve-moderada; inestabilidad postural pero independiente."),
                            Pair(4.0, "Estadío 4.0: Incapacidad severa; todavía es capaz de caminar o permanecer de pie sin ayuda."),
                            Pair(5.0, "Estadío 5.0: Confinado a silla de ruedas o cama por completo, a menos que reciba asistencia.")
                        )
                        items(stages.size) { index ->
                            val (grade, desc) = stages[index]
                            val isSel = hoehnYahrSelected == grade
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { hoehnYahrSelected = grade },
                                colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Text(desc, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp), fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    "toxina" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Dilución de Toxina Botulínica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Calcula de forma exacta las Unidades efectivas por cada 0.1 mL (10 unidades de jeringa estándar) según la dilución deseada.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("1. Seleccione Presentación / Marca comercial:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(onClick = { toxinaBrand = "botox" }, colors = ButtonDefaults.buttonColors(containerColor = if (toxinaBrand == "botox") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (toxinaBrand == "botox") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant), modifier = Modifier.weight(1f)) {
                                            Text("A (Botox/Xeomin) 100 U")
                                        }
                                        Button(onClick = { toxinaBrand = "dysport" }, colors = ButtonDefaults.buttonColors(containerColor = if (toxinaBrand == "dysport") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (toxinaBrand == "dysport") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant), modifier = Modifier.weight(1f)) {
                                            Text("B (Dysport) 500 U")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("2. Volumen de Diluyente (mL de Solución Salina):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(1.0, 2.0, 2.5, 4.0, 5.0).forEach { vol ->
                                            val isSel = toxinaDilution == vol
                                            ElevatedButton(
                                                onClick = { toxinaDilution = vol },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            ) { Text("${vol} mL") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "hachinski" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Escala Isquémica de Hachinski", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Diferenciación entre demencia de tipo vascular (>=7 ptos) y demencia de tipo degenerativo/Alzheimer (<=4 ptos).", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        val hachinskiItems = listOf(
                            Pair("1. Comienzo o inicio súbito / abrupto de síntomas", 2),
                            Pair("2. Deterioro escalonado de las funciones cognitivas", 1),
                            Pair("3. Curso clínico típicamente fluctuante", 2),
                            Pair("4. Confusión mental de predominio nocturno", 1),
                            Pair("5. Preservación relativa de la personalidad global", 1),
                            Pair("6. Depresión reactiva o humor depresivo", 1),
                            Pair("7. Síntomas somáticos vagos (cefalea, mareos, etc.)", 1),
                            Pair("8. Labilidad emocional o incontinencia de afectos", 1),
                            Pair("9. Historia clínica o antecedente de hipertensión arterial", 1),
                            Pair("10. Historia clínica o antecedente de brotes de ACV / Ictus", 2),
                            Pair("11. Evidencia clínica objetiva de arterioesclerosis asociada", 1),
                            Pair("12. Síntomas neurológicos claramente focalizados", 2),
                            Pair("13. Signos neurológicos focales detectables al examen", 2)
                        )
                        items(hachinskiItems.size) { index ->
                            val (text, score) = hachinskiItems[index]
                            val isChecked = hachinskiSelections[index] == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { hachinskiSelections[index] = !isChecked }
                                    .background(if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = isChecked, onCheckedChange = { hachinskiSelections[index] = it })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text, modifier = Modifier.weight(1f))
                                Text("+$score", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    "cdr" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("CDR (Clinical Dementia Rating)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Esquema de calificación de demencia global basado en 6 áreas cognitivas-funcionales específicas.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        val domains = listOf(
                            Pair("Memoria (Ancla central)", "cdrMemoria"),
                            Pair("Orientación", "cdrOrientacion"),
                            Pair("Juicio y resolución de problemas", "cdrJuicio"),
                            Pair("Asuntos Sociales (asociación comunitaria)", "cdrSocial"),
                            Pair("Hogar y Pasatiempos", "cdrHogar"),
                            Pair("Cuidado Personal", "cdrCuidado")
                        )
                        val choices = listOf(0.0, 0.5, 1.0, 2.0, 3.0)
                        items(domains.size) { index ->
                            val (label, stateVar) = domains[index]
                            val currentVal = when (stateVar) {
                                "cdrMemoria" -> cdrMemoria
                                "cdrOrientacion" -> cdrOrientacion
                                "cdrJuicio" -> cdrJuicio
                                "cdrSocial" -> cdrSocial
                                "cdrHogar" -> cdrHogar
                                else -> cdrCuidado
                            }
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        choices.forEach { choice ->
                                            val isSel = currentVal == choice
                                            Button(
                                                onClick = {
                                                    when (stateVar) {
                                                        "cdrMemoria" -> cdrMemoria = choice
                                                        "cdrOrientacion" -> cdrOrientacion = choice
                                                        "cdrJuicio" -> cdrJuicio = choice
                                                        "cdrSocial" -> cdrSocial = choice
                                                        "cdrHogar" -> cdrHogar = choice
                                                        else -> cdrCuidado = choice
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(choice.toString(), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "gds15" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("GDS-15 (Escala de Depresión Geriátrica)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Herramienta rápida de tamizaje de depresión en adultos mayores. Califique el estado de ánimo (sensación habitual durante la última semana).", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        val gdsQuestions = listOf(
                            "1. ¿Está básicamente satisfecho con su vida?",
                            "2. ¿Ha dejado de lado muchas de sus actividades e intereses?",
                            "3. ¿Siente que su vida está vacía?",
                            "4. ¿Se siente aburrido con frecuencia?",
                            "5. ¿Tiene buen ánimo la mayor parte del tiempo?",
                            "6. ¿Teme que le pase algo malo con frecuencia?",
                            "7. ¿Se siente feliz la mayor parte del tiempo?",
                            "8. ¿Se siente frecuentemente desamparado o indefenso?",
                            "9. ¿Prefiere quedarse en casa en vez de salir?",
                            "10. ¿Siente que tiene más problemas de memoria que los demás?",
                            "11. ¿Cree que vivir en la actualidad es maravilloso?",
                            "12. ¿Piensa que no vale la pena vivir tal y como está actualmente?",
                            "13. ¿Se siente lleno de energía?",
                            "14. ¿Piensa que su situación actual es desesperada?",
                            "15. ¿Cree que la mayoría de la gente está mejor que usted?"
                        )
                        items(gdsQuestions.size) { index ->
                            val isChecked = gdsSelections[index] == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { gdsSelections[index] = !isChecked }
                                    .background(if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = isChecked, onCheckedChange = { gdsSelections[index] = it })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(gdsQuestions[index], modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    "four_delirium" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Escala del Delirium (4AT)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Herramienta rápida de detección clínica de delirium o deterioro cognitivo agudo.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("1. Estado de Alerta (Somnolencia, agitación):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("Normal (0)", "Deteriorado (4)").forEachIndexed { idx, txt ->
                                            val points = if (idx == 0) 0 else 4
                                            val isSel = fouratAlertness == points
                                            Button(
                                                onClick = { fouratAlertness = points },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("2. Test AMT4 (Edad, fecha nac, hospital, año actual):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("0 errores (0)", "1 error (1)", ">=2 err / no responde (2)").forEachIndexed { idx, txt ->
                                            val isSel = fouratAmt4 == idx
                                            Button(
                                                onClick = { fouratAmt4 = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("3. Atención (Meses hacia atrás en orden inverso):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(">= 7 meses (0)", "Completó <7 mes (1)", "No responde/incapaz (2)").forEachIndexed { idx, txt ->
                                            val isSel = fouratAttention == idx
                                            Button(
                                                onClick = { fouratAttention = idx },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center) }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("4. Curso Fluctuante o Inicio Agudo (<2 semanas):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("No (0)", "Sí (4)").forEachIndexed { idx, txt ->
                                            val points = if (idx == 0) 0 else 4
                                            val isSel = fouratAcute == points
                                            Button(
                                                onClick = { fouratAcute = points },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                modifier = Modifier.weight(1f)
                                            ) { Text(txt, style = MaterialTheme.typography.labelSmall) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "mrc_sum" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Escala MRC Sum Score (Fuerza UCI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Evaluación de la fuerza muscular de manera bilateral. Un puntaje total inferior a 48 indica Debilidad Adquirida en UCI (ICU-AW).", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        val muscleGroups = listOf(
                            "1. Abducción del hombro (deltoides)",
                            "2. Flexión del codo (bíceps braquial)",
                            "3. Extensión de muñeca",
                            "4. Flexión de la cadera (íliopsoas)",
                            "5. Extensión de la rodilla (cuádriceps)",
                            "6. Flexión dorsal del pie (tibial anterior)"
                        )
                        items(muscleGroups.size) { index ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(muscleGroups[index], style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("IZQUIERDA (0-5)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                (0..5).forEach { v ->
                                                    val isSel = mrcLeft[index + 1] == v
                                                    Button(
                                                        onClick = { mrcLeft[index + 1] = v },
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) { Text(v.toString(), style = MaterialTheme.typography.labelSmall) }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("DERECHA (0-5)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                (0..5).forEach { v ->
                                                    val isSel = mrcRight[index + 1] == v
                                                    Button(
                                                        onClick = { mrcRight[index + 1] = v },
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) { Text(v.toString(), style = MaterialTheme.typography.labelSmall) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "mmt8" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("MMT-8 (Manual Muscle Testing 8)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Subgrupo estandarizado de 8 músculos para monitoreo de fatiga y debilidad proximal en miopatías / miositis. Escala de 0 a 10 por cada músculo.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        val muscleMmt8 = listOf(
                            "1. Flexores del cuello (músculos profundos)",
                            "2. Abductores del brazo (Deltoides bilateral, peor)",
                            "3. Extensores de muñeca bilateral",
                            "4. Flexores de la cadera (Psoas mayor bilateral)",
                            "5. Extensores de rodilla (Cuádriceps bilateral)",
                            "6. Flexores dorsales del pie (Tibial anterior bilateral)",
                            "7. Extensores de codo (Tríceps bilateral)",
                            "8. Abductores de cadera (Glúteo medio bilateral)"
                        )
                        items(muscleMmt8.size) { index ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(muscleMmt8[index], style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (0..10).forEach { score ->
                                            val isSel = mmt8Selections[index + 1] == score
                                            ElevatedButton(
                                                onClick = { mmt8Selections[index + 1] = score },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(score.toString(), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "rabdomiolisis" -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Fórmula Hidratación en Rabdomiólisis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Cálculo rápido del plan de hidratación agresiva y requerimientos hídricos bedside para prevención de injuria renal aguda (IRA) por mioglobina.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(value = rabdoWeight, onValueChange = { rabdoWeight = it }, label = { Text("Peso del paciente (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = rabdoCk, onValueChange = { rabdoCk = it }, label = { Text("Nivel de Creatina Quinasa CK (UI/L)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Fixed Sticky Result Bar with soft backdrop gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(bottom = 12.dp)
        ) {
            when (activeCalculatorId) {
                "nihss" -> {
                    val currentTotal = ClinicalDatabase.nihss.domains.sumOf { selectedNihssAnswers[it.id] ?: 0 }
                    val scoreCategory = when {
                        currentTotal == 0 -> "Normal"
                        currentTotal in 1..4 -> "Ictus Leve"
                        currentTotal in 5..15 -> "Ictus Moderado"
                        currentTotal in 16..20 -> "Ictus Moderadamente Grave"
                        else -> "Ictus Grave"
                    }
                    val missingItems = ClinicalDatabase.nihss.domains.size - selectedNihssAnswers.size

                    StickyResultBar(
                        scoreText = "NIHSS: $currentTotal puntos",
                        interpretationText = "Clasificación clínica: $scoreCategory",
                        onResetClicked = onResetNihss,
                        missingItemsCount = missingItems,
                        onCopyClicked = {
                            val breakdown = ClinicalDatabase.nihss.domains.map { domain ->
                                val valNum = selectedNihssAnswers[domain.id] ?: 0
                                "${domain.id}. ${domain.label.split(".").getOrNull(1)?.trim() ?: domain.label}: $valNum"
                            }
                            onCopyClicked(
                                ClinicalDatabase.nihss.name,
                                "$currentTotal ($scoreCategory)",
                                breakdown,
                                "Evaluación neurológica cuantitativa en fase de ACV agudo."
                            )
                        }
                    )
                }
                "aspects" -> {
                    val score = 10 - selectedAspectsRegions.size
                    val interpretation = when {
                        score >= 8 -> "Favorable: Buen pronóstico de revascularización y menor riesgo hemorrágico"
                        score in 6..7 -> "Moderado: Evaluar riesgo/beneficio para trombectomía mecánica"
                        else -> "Pobre: Alta extensión isquémica, alto riesgo de transformación hemorrágica"
                    }
                    StickyResultBar(
                        scoreText = "ASPECTS: $score / 10",
                        interpretationText = interpretation,
                        onResetClicked = onResetAspects,
                        missingItemsCount = 0,
                        onCopyClicked = {
                            val breakdown = listOf("Áreas Isquémicas afectadas: " + if (selectedAspectsRegions.isEmpty()) "Ninguna" else selectedAspectsRegions.joinToString(", "))
                            onCopyClicked(
                                "ASPECTS Score",
                                "$score sobre 10 iniciales",
                                breakdown,
                                "Interpretación clínica: $interpretation"
                            )
                        }
                    )
                }
                "ich" -> {
                    val isComplete = ichGcsScore != null && ichVolumeIsLarge != null && ichIvhPresent != null && ichInfratentorial != null && ichAge80Plus != null
                    val missingCount = listOf(ichGcsScore, ichVolumeIsLarge, ichIvhPresent, ichInfratentorial, ichAge80Plus).count { it == null }

                    val currentTotal = (ichGcsScore ?: 0) +
                        (if (ichVolumeIsLarge == true) 1 else 0) +
                        (if (ichIvhPresent == true) 1 else 0) +
                        (if (ichInfratentorial == true) 1 else 0) +
                        (if (ichAge80Plus == true) 1 else 0)

                    val mortality = when (currentTotal) {
                        0 -> "0% de mortalidad estimada a 30 días"
                        1 -> "13% de mortalidad estimada a 30 días"
                        2 -> "26% de mortalidad estimada a 30 días"
                        3 -> "72% de mortalidad estimada a 30 días"
                        4 -> "97% de mortalidad estimada a 30 días"
                        else -> "100% de mortalidad estimada a 30 días"
                    }

                    StickyResultBar(
                        scoreText = "ICH Score: $currentTotal puntos",
                        interpretationText = "Mortalidad: $mortality",
                        onResetClicked = onResetIch,
                        missingItemsCount = missingCount,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Glasglow inicial puntos: $ichGcsScore",
                                "Volumen hematoma >= 30: $ichVolumeIsLarge",
                                "Sangre intraventricular: $ichIvhPresent",
                                "Origen infratentorial: $ichInfratentorial",
                                "Edad >= 80 años: $ichAge80Plus",
                                "Medidas volume manual: A=${ichAValue}cm, B=${ichBValue}cm, C=${ichCValue}cm"
                            )
                            onCopyClicked(
                                "ICH Score",
                                "$currentTotal puntos (Score total)",
                                breakdown,
                                "Mortalidad clínica asociada: $mortality"
                            )
                        }
                    )
                }
                "four" -> {
                    val isComplete = fourEye != null && fourMotor != null && fourBrainstem != null && fourRespiration != null
                    val missingCount = listOf(fourEye, fourMotor, fourBrainstem, fourRespiration).count { it == null }

                    val currentTotal = (fourEye ?: 0) + (fourMotor ?: 0) + (fourBrainstem ?: 0) + (fourRespiration ?: 0)
                    val statusText = when {
                        currentTotal >= 14 -> "Alerta y Funciones Conservadas"
                        currentTotal in 10..13 -> "Letargo / Afectación Moderada"
                        currentTotal in 5..9 -> "Estupor / Afectación Severa"
                        else -> "Coma o Disfunción de Tronco Grave"
                    }

                    StickyResultBar(
                        scoreText = "FOUR Score: $currentTotal / 16",
                        interpretationText = "Clasificación estado crítico: $statusText",
                        onResetClicked = onResetFour,
                        missingItemsCount = missingCount,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Respuesta Ocular (E): $fourEye/4",
                                "Respuesta Motora (M): $fourMotor/4",
                                "Reflejos de Tronco (B): $fourBrainstem/4",
                                "Patrón respirat (R): $fourRespiration/4"
                            )
                            onCopyClicked(
                                "FOUR Score",
                                "$currentTotal de 16 totales",
                                breakdown,
                                "Interpretación neurológica: $statusText."
                            )
                        }
                    )
                }
                "edss" -> {
                    val listStates = listOf(edssPyramidal, edssCerebellar, edssBrainstem, edssSensory, edssBowelBladder, edssVisual, edssCerebral, edssOther, edssMarchSelection)
                    val missingCount = listStates.count { it == null }

                    // Calculation rules helper based on automated Kurtzke mapping
                    val maxFs = listStates.filterIsInstance<Int>().maxOrNull() ?: 0
                    val countFs1 = listStates.filterIsInstance<Int>().count { it == 1 }
                    val countFs2 = listStates.filterIsInstance<Int>().count { it == 2 }
                    val countFs3 = listStates.filterIsInstance<Int>().count { it == 3 }

                    val marchIdx = edssMarchSelection?.toIntOrNull() ?: 0
                    val calculatedEdss = when {
                        marchIdx == 10 -> 10.0
                        marchIdx == 9 -> 9.0
                        marchIdx == 8 -> 8.5
                        marchIdx == 7 -> 7.5
                        marchIdx == 6 -> 7.0
                        marchIdx == 5 -> 6.5
                        marchIdx == 4 -> 6.0
                        marchIdx == 3 -> 5.5
                        marchIdx == 2 -> 5.0
                        marchIdx == 1 -> 4.5
                        else -> {
                            when {
                                maxFs >= 4 -> 4.0
                                maxFs == 3 -> if (countFs3 > 1) 3.5 else 3.0
                                maxFs == 2 -> if (countFs2 > 1) 2.5 else 2.0
                                maxFs == 1 -> if (countFs1 > 1) 1.5 else 1.0
                                else -> 0.0
                            }
                        }
                    }

                    val edssInterp = when {
                        calculatedEdss <= 1.5 -> "Discapacidad no mínima en sistemas funcionales"
                        calculatedEdss in 2.0..3.5 -> "Discapacidad leve a moderada, deambulación conservada"
                        calculatedEdss in 4.0..5.5 -> "Restricciones marcadas, camina distancias autolimitadas"
                        calculatedEdss in 6.0..6.5 -> "Requiere ayuda unilateral o bilateral para marchar"
                        calculatedEdss in 7.0..8.5 -> "Restringido a silla de ruedas o encamado"
                        else -> "Discapacidad total vegetativa o fallecimiento"
                    }

                    StickyResultBar(
                        scoreText = "EDSS Final: $calculatedEdss / 10",
                        interpretationText = "Clasificación Kurtzke: $edssInterp",
                        onResetClicked = onResetEdss,
                        missingItemsCount = missingCount,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Piramidal: $edssPyramidal",
                                "Cerebeloso: $edssCerebellar",
                                "Tronco Encefal: $edssBrainstem",
                                "Sensitivo: $edssSensory",
                                "Esfínteres: $edssBowelBladder",
                                "Visual: $edssVisual",
                                "Mental: $edssCerebral",
                                "Marcha de Kurtzke: $edssMarchSelection"
                            )
                            onCopyClicked(
                                "EDSS (Kurtzke) Score",
                                "$calculatedEdss puntos (0 a 10)",
                                breakdown,
                                "Discapacidad estimada: $edssInterp"
                            )
                        }
                    )
                }
                "alsfrsr" -> {
                    val currentTotal = ClinicalDatabase.alsfrsr.domains.sumOf { selectedAlsfrsrAnswers[it.id] ?: 4 }
                    val percent = (currentTotal * 100.0) / 48.0
                    val percentStr = String.format(java.util.Locale.US, "%.1f", percent)
                    val missingItems = ClinicalDatabase.alsfrsr.domains.size - selectedAlsfrsrAnswers.size

                    StickyResultBar(
                        scoreText = "ALSFRS-R: $currentTotal / 48",
                        interpretationText = "Capacidad funcional conservada: $percentStr%",
                        onResetClicked = onResetAlsfrsr,
                        missingItemsCount = missingItems,
                        onCopyClicked = {
                            val breakdown = ClinicalDatabase.alsfrsr.domains.map { domain ->
                                val valNum = selectedAlsfrsrAnswers[domain.id] ?: 4
                                "${domain.label}: $valNum/4"
                            }
                            onCopyClicked(
                                ClinicalDatabase.alsfrsr.name,
                                "$currentTotal de 48 puntos posibles",
                                breakdown,
                                "Donde 48 es la máxima preservación de capacidad funcional residual."
                            )
                        }
                    )
                }
                "qmg" -> {
                    val currentTotal = ClinicalDatabase.qmg.domains.sumOf { selectedQmgAnswers[it.id] ?: 0 }
                    val qmgInterp = when {
                        currentTotal in 0..4 -> "Afectación Leve"
                        currentTotal in 5..9 -> "Afectación Moderada"
                        currentTotal in 10..14 -> "Afectación Severa"
                        else -> "Afectación Grave"
                    }
                    val missingItems = ClinicalDatabase.qmg.domains.size - selectedQmgAnswers.size

                    StickyResultBar(
                        scoreText = "QMG: $currentTotal / 39",
                        interpretationText = "Clasificación neuromuscular: $qmgInterp",
                        onResetClicked = onResetQmg,
                        missingItemsCount = missingItems,
                        onCopyClicked = {
                            val breakdown = ClinicalDatabase.qmg.domains.map { domain ->
                                val valNum = selectedQmgAnswers[domain.id] ?: 0
                                "${domain.label}: $valNum"
                            }
                            onCopyClicked(
                                ClinicalDatabase.qmg.name,
                                "$currentTotal de 39 puntos posibles",
                                breakdown,
                                "A mayor puntuación médica, mayor debilidad residual de la placa neuromuscular."
                            )
                        }
                    )
                }
                "dragon" -> {
                    val currentTotal = ClinicalDatabase.dragon.domains.sumOf { selectedDragonAnswers[it.id] ?: 0 }
                    val outcomeSummary = when (currentTotal) {
                        in 0..1 -> "Excelente (~95% buen resultado, <2% mort.)"
                        2 -> "Favorable (~80% buen resultado, <5% mort.)"
                        3 -> "Favorable (~75% buen resultado, ~5% mort.)"
                        4 -> "Intermedio (~50% buen resultado, ~10% mort.)"
                        5 -> "Moderado (~35% buen resultado, ~15% mort.)"
                        6 -> "Guardado (~25% buen resultado, ~25% mort.)"
                        7 -> "Pobre (~12% buen resultado, ~40% mort.)"
                        else -> "Muy Pobre (<5% buen resultado, >60% mort.)"
                    }
                    val missingItems = ClinicalDatabase.dragon.domains.size - selectedDragonAnswers.size

                    StickyResultBar(
                        scoreText = "DRAGON: $currentTotal / 10 puntos",
                        interpretationText = "Pronóstico: $outcomeSummary",
                        onResetClicked = onResetDragon,
                        missingItemsCount = missingItems,
                        onCopyClicked = {
                            val breakdown = ClinicalDatabase.dragon.domains.map { domain ->
                                val valNum = selectedDragonAnswers[domain.id] ?: 0
                                "${domain.label}: $valNum"
                            }
                            onCopyClicked(
                                ClinicalDatabase.dragon.name,
                                "$currentTotal de 10 puntos",
                                breakdown,
                                "Estimación de desenlace:\n$outcomeSummary"
                            )
                        }
                    )
                }
                "lcr" -> {
                    val lcrLeukVal = lcrLeukos.toIntOrNull() ?: 0
                    val lcrPmnVal = lcrPmn.toIntOrNull() ?: 0
                    val lcrProteinsVal = lcrProteins.toDoubleOrNull() ?: 0.0
                    val lcrGlucoseLcrVal = lcrGlucoseLcr.toDoubleOrNull() ?: 0.0
                    val lcrGlucoseSerumVal = lcrGlucoseSerum.toDoubleOrNull() ?: 0.0
                    val lcrErythrosVal = lcrErythros.toIntOrNull() ?: 0

                    val correctedLeuko = if (lcrErythrosVal > 0) maxOf(0, lcrLeukVal - (lcrErythrosVal / 700)) else lcrLeukVal
                    val glucRatio = if (lcrGlucoseSerumVal > 0.0) lcrGlucoseLcrVal / lcrGlucoseSerumVal else 0.0
                    val hasDisociacion = lcrProteinsVal > 45.0 && correctedLeuko < 10

                    val diagnosisText = when {
                        correctedLeuko > 5 || lcrLeukVal > 5 -> {
                            when {
                                lcrPmnVal >= 60 && glucRatio < 0.4 -> "Sugerente de Meningitis Bacteriana Aguda"
                                lcrPmnVal < 50 && glucRatio >= 0.5 && lcrProteinsVal < 100.0 -> "Sugerente de Meningitis Viral"
                                lcrPmnVal < 50 && glucRatio < 0.4 && lcrProteinsVal > 100.0 -> "Sugerente de Meningitis Tuberculosa / Fúngica"
                                else -> "Perfil inflamatorio inespecífico de LCR. Correlacionar gram y cultivos."
                            }
                        }
                        hasDisociacion -> "Disociación Albúmino-Citológica detectada (Sugerente de Guillain-Barré)"
                        else -> "Glucosa LCR/Suero: ${String.format("%.2f", glucRatio)}. Citoquímico LCR normal o inespecífico."
                    }

                    StickyResultBar(
                        scoreText = "Leucos corregidos: $correctedLeuko células/µL",
                        interpretationText = diagnosisText,
                        onResetClicked = {
                            lcrLeukos = ""
                            lcrPmn = ""
                            lcrProteins = ""
                            lcrGlucoseLcr = ""
                            lcrGlucoseSerum = ""
                            lcrErythros = ""
                        },
                        missingItemsCount = if (lcrLeukos.isEmpty()) 1 else 0,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Leucocitos ingresados: $lcrLeukVal",
                                "Eritrocitos ingresados: $lcrErythrosVal",
                                "Leucocitos corregidos por punción: $correctedLeuko células/µL",
                                "Porcentaje PMN: $lcrPmnVal%",
                                "Proteínas en LCR: $lcrProteinsVal mg/dL",
                                "Glucosa en LCR: $lcrGlucoseLcrVal",
                                "Glucosa Sérica: $lcrGlucoseSerumVal",
                                "Relación glucosa LCR/Suero: ${String.format("%.2f", glucRatio)}",
                                "Disociación Albúmino-Citológica: ${if(hasDisociacion)"Sí" else "No"}"
                            )
                            onCopyClicked(
                                "LCR / Análisis Citoquímico",
                                "Corrección de Pleocitosis y Perfil probable",
                                breakdown,
                                "Interpretación automatizada:\n$diagnosisText"
                            )
                        }
                    )
                }
                "egris" -> {
                    val isComplete = egrisDays != null && egrisWeakness != null && egrisMrc != null
                    val score = (egrisDays ?: 0) + (egrisWeakness ?: 0) + (egrisMrc ?: 0)
                    val riskLevel = when {
                        score <= 2 -> "Bajo Riesgo (~4% de requerir ventilación mecánica)"
                        score in 3..4 -> "Riesgo Intermedio (~24% de requerir ventilación mecánica)"
                        else -> "Alto Riesgo (~65% de requerir ventilación mecánica)"
                    }

                    StickyResultBar(
                        scoreText = "EGRIS Score: $score puntos",
                        interpretationText = "Riesgo Respiratorio: $riskLevel",
                        onResetClicked = {
                            egrisDays = null
                            egrisWeakness = null
                            egrisMrc = null
                        },
                        missingItemsCount = if (isComplete) 0 else 3 - listOf(egrisDays, egrisWeakness, egrisMrc).count { it != null },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Días de inicio de debilidad a ingreso (0-2): $egrisDays",
                                "Debilidad facial/bulbar al ingreso (0-1): $egrisWeakness",
                                "Rango de MRC Sum Score (0-5): $egrisMrc"
                            )
                            onCopyClicked(
                                "Erasmus GBS Respiratory Insufficiency Score (EGRIS)",
                                "$score puntos (Predicción de ventilación mecánica)",
                                breakdown,
                                "Categoría de riesgo:\n$riskLevel"
                            )
                        }
                    )
                }
                "megos" -> {
                    val isComplete = megosAge != null && megosDiarrhea != null && megosMrc != null
                    val score = (megosAge ?: 0) + (megosDiarrhea ?: 0) + (megosMrc ?: 0)
                    val prognosis = when {
                        score <= 1 -> "Excelente: >95% de deambulación independiente a las 4 semanas-6 meses"
                        score in 2..3 -> "Favorable: ~80% de deambulación independiente a los 6 meses"
                        score in 4..5 -> "Moderado: ~60% de deambulación independiente a los 6 meses"
                        else -> "Reservado: <40% de deambulación independiente a los 6 meses"
                    }

                    StickyResultBar(
                        scoreText = "mEGOS Score: $score puntos",
                        interpretationText = prognosis,
                        onResetClicked = {
                            megosAge = null
                            megosDiarrhea = null
                            megosMrc = null
                        },
                        missingItemsCount = if (isComplete) 0 else 3 - listOf(megosAge, megosDiarrhea, megosMrc).count { it != null },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Edad al ingreso (0-2): $megosAge",
                                "Antec. Diarrea <4 semanas (0-1): $megosDiarrhea",
                                "MRC Sum Score ingreso (0-5): $megosMrc"
                            )
                            onCopyClicked(
                                "Modified Erasmus GBS Outcome Score (mEGOS)",
                                "$score puntos (Deambulación funcional)",
                                breakdown,
                                "Pronóstico de marcha clínica:\n$prognosis"
                            )
                        }
                    )
                }
                "ledd" -> {
                    val lvd = leddLevodopa.toDoubleOrNull() ?: 0.0
                    val totalLvd = if (leddEntacapone) lvd * 1.33 else lvd
                    val prami = (leddPramipexol.toDoubleOrNull() ?: 0.0) * 100.0
                    val ropi = (leddRopinirol.toDoubleOrNull() ?: 0.0) * 20.0
                    val roti = (leddRotigotine.toDoubleOrNull() ?: 0.0) * 30.0
                    val rasa = (leddRasagilina.toDoubleOrNull() ?: 0.0) * 100.0
                    val saf = (leddSafinamida.toDoubleOrNull() ?: 0.0) * 100.0

                    val totalLedValue = totalLvd + prami + ropi + roti + rasa + saf

                    StickyResultBar(
                        scoreText = "Total LEDD: ${String.format("%.1f", totalLedValue)} mg/día",
                        interpretationText = "Equivalencia diaria de dosis de Levodopa en Parkinson.",
                        onResetClicked = {
                            leddLevodopa = ""
                            leddPramipexol = ""
                            leddRopinirol = ""
                            leddRotigotine = ""
                            leddEntacapone = false
                            leddRasagilina = ""
                            leddSafinamida = ""
                        },
                        missingItemsCount = 0,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Levodopa equivalente (con factor Entacapona si aplica): ${String.format("%.1f", totalLvd)} mg",
                                "Pramipexol equiv: ${String.format("%.1f", prami)} mg",
                                "Ropinirol equiv: ${String.format("%.1f", ropi)} mg",
                                "Rotigotina parche equiv: ${String.format("%.1f", roti)} mg",
                                "Rasagilina equiv: ${String.format("%.1f", rasa)} mg",
                                "Safinamida equiv: ${String.format("%.1f", saf)} mg"
                            )
                            onCopyClicked(
                                "Dosis Equivalente Diaria de Levodopa (LEDD)",
                                "${String.format("%.1f", totalLedValue)} mg de Levodopa diaria equivalente",
                                breakdown,
                                "Medicada para optimizar terapia y evitar diskinesias motoras."
                            )
                        }
                    )
                }
                "aims" -> {
                    val total = aimsSelections.values.sum()
                    val positiveItems = aimsSelections.filter { it.key in 1..7 && it.value >= 2 }.size
                    val tardiveStatus = if (positiveItems >= 2 || aimsSelections.values.any { it >= 3 }) "Criterios de severidad para Discinesia Tardía" else "No clasifica formalmente para Discinesia Tardía"

                    StickyResultBar(
                        scoreText = "Severidad Total AIMS: $total puntos",
                        interpretationText = "Estado: $tardiveStatus",
                        onResetClicked = {
                            aimsSelections.clear()
                            (1..12).forEach { aimsSelections[it] = 0 }
                        },
                        missingItemsCount = 12 - aimsSelections.size,
                        onCopyClicked = {
                            val breakdown = aimsSelections.map { (k, v) -> "Ítem $k score: $v" }
                            onCopyClicked(
                                "Abnormal Involuntary Movement Scale (AIMS)",
                                "Total: $total puntos",
                                breakdown,
                                "Evaluación de discinesia tardía. Clasificación: $tardiveStatus"
                            )
                        }
                    )
                }
                "mdsupdrs" -> {
                    val total = updrsSelections.values.sum()

                    StickyResultBar(
                        scoreText = "Puntaje Motor (Tasks 3.4-3.6): $total puntos",
                        interpretationText = "Metrónomo activo: ${if(metronomeActive)"Sí ($metronomeHz Hz)" else "No"}",
                        onResetClicked = {
                            updrsSelections.clear()
                            (1..3).forEach { updrsSelections[it] = 0 }
                            metronomeActive = false
                        },
                        missingItemsCount = 3 - updrsSelections.size,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Finger Tapping score: ${updrsSelections[1]}",
                                "Movimientos rápidos manos score: ${updrsSelections[2]}",
                                "Pronación-supinación manos score: ${updrsSelections[3]}"
                            )
                            onCopyClicked(
                                "MDS-UPDRS Parte III Subset Motor",
                                "$total puntos de 12 posibles",
                                breakdown,
                                "Evaluación simplificada para bradicinesia motora guiada con metrónomo."
                            )
                        }
                    )
                }
                "hoehn_yahr" -> {
                    val grade = hoehnYahrSelected ?: 0.0

                    StickyResultBar(
                        scoreText = "Hoehn & Yahr: Estadío $grade",
                        interpretationText = if (grade == 0.0) "Seleccione el estadío del paciente." else "Estadío clínico seleccionado para enfermedad de Parkinson.",
                        onResetClicked = { hoehnYahrSelected = null },
                        missingItemsCount = if (hoehnYahrSelected == null) 1 else 0,
                        onCopyClicked = {
                            onCopyClicked(
                                "Escala de Hoehn y Yahr Modificada",
                                "Estadío $grade",
                                listOf("Califica la disfuncionalidad motora, afectación simétrica e inestabilidad postural en Parkinson."),
                                "Fase de la enfermedad: Estadío $grade de 5.0"
                            )
                        }
                    )
                }
                "toxina" -> {
                    val baseU = if (toxinaBrand == "botox") 100.0 else 500.0
                    val unitsPerPointOne = baseU / (toxinaDilution * 10.0)

                    StickyResultBar(
                        scoreText = "${String.format("%.1f", unitsPerPointOne)} U / 0.1 mL de solución",
                        interpretationText = "Marca: ${toxinaBrand.uppercase()} ($baseU U) diluido en ${toxinaDilution} mL",
                        onResetClicked = {
                            toxinaBrand = "botox"
                            toxinaDilution = 2.0
                        },
                        missingItemsCount = 0,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Presentación comercial: $toxinaBrand ($baseU UI)",
                                "Volumen de diluyente añadido: $toxinaDilution mL de SF",
                                "Equivalente: 0.1 mL (10 divisiones de jeringa) de volumen inyectable aporta de forma exacta $unitsPerPointOne Unidades de toxina."
                            )
                            onCopyClicked(
                                "Cálculo de Dilución de Toxina Botulínica",
                                "Concentración: $unitsPerPointOne U por 0.1 mL",
                                breakdown,
                                "Recomendaciones: Mantener cadena de frío de 2-8°C y usar jeringa libre de aire."
                            )
                        }
                    )
                }
                "hachinski" -> {
                    val total = hachinskiSelections.filter { it.value }.keys.sumOf { idx ->
                        listOf(2, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 2, 2)[idx]
                    }
                    val diagType = when {
                        total >= 7 -> "Demencia Vascular (puntuación >= 7)"
                        total <= 4 -> "Demencia Primaria Degenerativa (ej. Enfermedad de Alzheimer)"
                        else -> "Perfil mixto u oligovascular (puntuación 5-6)"
                    }

                    StickyResultBar(
                        scoreText = "Hachinski: $total puntos",
                        interpretationText = diagType,
                        onResetClicked = { hachinskiSelections.clear() },
                        missingItemsCount = 0,
                        onCopyClicked = {
                            val breakdown = hachinskiSelections.filter { it.value }.keys.map { index ->
                                listOf(
                                    "Comienzo brusco", "Deterioro escalonado", "Curso fluctuante", "Confusión nocturna",
                                    "Preservación personalidad", "Depresión", "Síntomas somáticos", "Labilidad emocional",
                                    "Antec. Hipertensión", "Antec. ACV/Ictus", "Arteriosclerosis", "Síntomas focales", "Signos focales"
                                )[index] + " (+${listOf(2, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 2, 2)[index]})"
                            }
                            onCopyClicked(
                                "Escala Isquémica de Hachinski",
                                "$total puntos totales",
                                breakdown,
                                "Orientación etiológica:\n$diagType"
                            )
                        }
                    )
                }
                "cdr" -> {
                    val isComplete = cdrMemoria != null && cdrOrientacion != null && cdrJuicio != null && cdrSocial != null && cdrHogar != null && cdrCuidado != null
                    val missingCount = listOf(cdrMemoria, cdrOrientacion, cdrJuicio, cdrSocial, cdrHogar, cdrCuidado).count { it == null }

                    val m = cdrMemoria ?: 0.0
                    val o = cdrOrientacion ?: 0.0
                    val j = cdrJuicio ?: 0.0
                    val s = cdrSocial ?: 0.0
                    val h = cdrHogar ?: 0.0
                    val c = cdrCuidado ?: 0.0

                    val averageSecondary = listOf(o, j, s, h, c).average()
                    val globalCdr = if (m == 0.0 && averageSecondary > 0.0) 0.5 else m

                    val scaleInterp = when (globalCdr) {
                        0.0 -> "Sano. Sin alteración."
                        0.5 -> "Deterioro Cognitivo Leve o Demencia Cuestionable."
                        1.0 -> "Demencia Leve."
                        2.0 -> "Demencia Moderada."
                        else -> "Demencia Severa."
                    }

                    StickyResultBar(
                        scoreText = "Global CDR estimado: $globalCdr",
                        interpretationText = scaleInterp,
                        onResetClicked = {
                            cdrMemoria = null
                            cdrOrientacion = null
                            cdrJuicio = null
                            cdrSocial = null
                            cdrHogar = null
                            cdrCuidado = null
                        },
                        missingItemsCount = missingCount,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Fila de Memoria (Ancla): $m",
                                "Orientación: $o",
                                "Juicio / Problemas: $j",
                                "Asuntos Sociales: $s",
                                "Hogar y Pasatiempos: $h",
                                "Cuidado Personal: $c"
                            )
                            onCopyClicked(
                                "Clinical Dementia Rating (CDR)",
                                "CDR global estimado: $globalCdr",
                                breakdown,
                                "Clasificación cognitiva-funcional:\n$scaleInterp"
                            )
                        }
                    )
                }
                "gds15" -> {
                    val yesQuestions = listOf(1, 2, 3, 5, 7, 8, 9, 11, 13, 14)
                    val checkedKeys = gdsSelections.filter { it.value }.keys
                    // Yes counts:
                    val yesPoints = checkedKeys.count { it in yesQuestions }
                    // No counts on 0, 4, 6, 10, 12: means questions where answering NO adds point (meaning NOT checked)
                    val noPoints = listOf(0, 4, 6, 10, 12).count { it !in checkedKeys }

                    val total = yesPoints + noPoints
                    val depressionLevel = when {
                        total >= 10 -> "Sugerente de Depresión Geriátrica Severa (10+)"
                        total >= 6 -> "Sugerente de Depresión Geriátrica Leve-Moderada (6-9)"
                        else -> "Normal (0-5)"
                    }

                    StickyResultBar(
                        scoreText = "GDS-15 Score: $total puntos",
                        interpretationText = depressionLevel,
                        onResetClicked = { gdsSelections.clear() },
                        missingItemsCount = 0,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Puntos sumados por respuestas afirmativas de riesgo: $yesPoints",
                                "Puntos sumados por respuestas negativas de riesgo: $noPoints",
                                "Completado auto-asistido."
                            )
                            onCopyClicked(
                                "GDS-15 (Escala abreviada de Yesavage)",
                                "$total de 15 puntos",
                                breakdown,
                                "Interpretación clínica para adulto mayor: $depressionLevel"
                            )
                        }
                    )
                }
                "four_delirium" -> {
                    val isComplete = fouratAlertness != null && fouratAmt4 != null && fouratAttention != null && fouratAcute != null
                    val missingCount = listOf(fouratAlertness, fouratAmt4, fouratAttention, fouratAcute).count { it == null }

                    val score = (fouratAlertness ?: 0) + (fouratAmt4 ?: 0) + (fouratAttention ?: 0) + (fouratAcute ?: 0)
                    val resultDelirium = when {
                        score >= 4 -> "Posible DELIRIUM presente / Deterioro cognitivo agudo (score >= 4)"
                        score in 1..3 -> "Deterioro cognitivo leve-moderado (requiere monitoreo continuo)"
                        else -> "Bajo Riesgo de delirium / Normal"
                    }

                    StickyResultBar(
                        scoreText = "4AT Delirium Score: $score puntos",
                        interpretationText = resultDelirium,
                        onResetClicked = {
                            fouratAlertness = null
                            fouratAmt4 = null
                            fouratAttention = null
                            fouratAcute = null
                        },
                        missingItemsCount = missingCount,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Nivel Alerta (0, 4): $fouratAlertness",
                                "Test AMT4 (0-2): $fouratAmt4",
                                "Atención meses atrás (0-2): $fouratAttention",
                                "Inicio agudo / fluctuación (0, 4): $fouratAcute"
                            )
                            onCopyClicked(
                                "Herramienta 4AT para Delirium",
                                "$score puntos totales",
                                breakdown,
                                "Clasificación clínica del estado mental:\n$resultDelirium"
                            )
                        }
                    )
                }
                "mrc_sum" -> {
                    val total = mrcLeft.values.sum() + mrcRight.values.sum()
                    val resultText = if (total < 48) "Debilidad Adquirida en UCI (ICU-AW) presente (<48 ptos)" else "Fuerza conservada bilateral (>=48 ptos)"

                    StickyResultBar(
                        scoreText = "Total MRC Sum Score: $total / 60",
                        interpretationText = resultText,
                        onResetClicked = {
                            mrcLeft.clear()
                            mrcRight.clear()
                            (1..6).forEach { mrcLeft[it] = 5; mrcRight[it] = 5 }
                        },
                        missingItemsCount = 12 - (mrcLeft.size + mrcRight.size),
                        onCopyClicked = {
                            val breakdown = (1..6).map { idx ->
                                "Muscle Group $idx: L[${mrcLeft[idx]}] R[${mrcRight[idx]}]"
                            }
                            onCopyClicked(
                                "MRC Sum Score (Fuerza UCI)",
                                "$total de 60 puntos totales",
                                breakdown,
                                "Interpretación clínica: $resultText"
                            )
                        }
                    )
                }
                "mmt8" -> {
                    val total = mmt8Selections.values.sum()
                    val pct = (total.toDouble() / 80.0) * 100.0

                    StickyResultBar(
                        scoreText = "MMT-8 Total: $total de 80p",
                        interpretationText = "Capacidad proximal muscular del: ${String.format("%.1f", pct)}%",
                        onResetClicked = {
                            mmt8Selections.clear()
                            (1..8).forEach { mmt8Selections[it] = 10 }
                        },
                        missingItemsCount = 8 - mmt8Selections.size,
                        onCopyClicked = {
                            val breakdown = mmt8Selections.map { (k, v) -> "Músculo $k score: $v/10" }
                            onCopyClicked(
                                "Manual Muscle Testing 8 (MMT-8)",
                                "$total sobre 80 puntos máximos (${String.format("%.1f", pct)}%)",
                                breakdown,
                                "Califica fuerza miositis y fatiga proximal neuromuscular."
                            )
                        }
                    )
                }
                "rabdomiolisis" -> {
                    val weight = rabdoWeight.toDoubleOrNull() ?: 70.0
                    val ck = rabdoCk.toDoubleOrNull() ?: 0.0

                    val planText = if (ck >= 5000.0) {
                        "Hidratación agresiva necesaria (10-15 mL/kg/h). Meta diuresis: 200-300 mL/h. Alto riesgo IRA."
                    } else {
                        "Plan normal de mantenimiento (1.5 - 2 mL/kg/h). Meta diuresis: >100-150 mL/h. Riesgo IRA bajo."
                    }

                    StickyResultBar(
                        scoreText = "Plan base: ${(weight * 1.5).toInt()} mL/hora",
                        interpretationText = planText,
                        onResetClicked = {
                            rabdoWeight = ""
                            rabdoCk = ""
                        },
                        missingItemsCount = if (rabdoWeight.isEmpty() || rabdoCk.isEmpty()) 1 else 0,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Peso del paciente: $weight kg",
                                "Nivel total de CK sérica: $ck UI/L",
                                "Tasa hídrica recomendada: ${(weight * 1.5).toInt()} mL/hora"
                            )
                            onCopyClicked(
                                "Fórmula e Hidratación de Rabdomiólisis",
                                "Estado de CK y Recomendaciones",
                                breakdown,
                                "Plan de acción bedside:\n$planText"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StickyResultBar(
    scoreText: String,
    interpretationText: String,
    onResetClicked: () -> Unit,
    missingItemsCount: Int = 0,
    onCopyClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("sticky_result_bar"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scoreText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (missingItemsCount > 0) {
                    Text(
                        text = "Faltan $missingItemsCount ítems por responder",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = interpretationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onResetClicked,
                    modifier = Modifier.testTag("reset_calculator")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restablecer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Button(
                    onClick = onCopyClicked,
                    enabled = missingItemsCount == 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (missingItemsCount == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (missingItemsCount == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("copy_result_bar"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (missingItemsCount > 0) "Faltan" else "Copiar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun CalculatorIntroCard(
    metadata: ScaleMetadata,
    currentScore: String,
    onCopyClipboard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calculator_intro_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${metadata.acronym} - ${metadata.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Total $currentScore",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = metadata.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCopyClipboard,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("smart_copy_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Smart Copy",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Smart Copy (Guardar)", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScaleDomainSelectionBlock(
    id: String,
    label: String,
    description: String,
    options: List<ScaleOption>,
    selectedValue: Int,
    onOptionSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("domain_block_$id"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            
            // FlowRow yields responsive grid buttons without large dropdowns
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option.value == selectedValue
                    val itemTag = "${id}_option_${option.value}"
                    Surface(
                        modifier = Modifier
                            .clickable { onOptionSelected(option.value) }
                            .testTag(itemTag),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabCriterios(
    goldCoastChecklist: Map<Int, Boolean>,
    trombolisisChecklist: Map<Int, Boolean>,
    selectedToastType: Int?,
    onToastTypeSelected: (Int?) -> Unit,
    onNavigateToDrug: (String) -> Unit,
    onCopyClicked: (String, String, List<String>, String?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var activeCriterioId by remember { mutableStateOf("trombolisis") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val criteriaList = listOf(
                Pair("trombolisis", "Trombólisis (ACV)"),
                Pair("gold_coast_als", "Gold Coast (ELA)"),
                Pair("miopatias_eular", "Miopatías (EULAR)"),
                Pair("toast", "TOAST (ACV)"),
                Pair("ilae_epilepsy", "Epílepsia (ILAE)")
            )
            criteriaList.forEach { (id, label) ->
                Button(
                    onClick = { activeCriterioId = id },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeCriterioId == id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeCriterioId == id) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .testTag("switch_criteria_$id")
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            when (activeCriterioId) {
                "trombolisis" -> {
                    val absolutes = listOf(
                        "1. Sospecha clínica / diagnóstica de hemorragia subaracnoidea (HSA)",
                        "2. Sangrado interno activo o diátesis hemorrágica activa",
                        "3. Neurocirugía o trauma craneoencefálico grave en últimos 3 meses",
                        "4. Antecedente personal confirmado de hemorragia intracerebral espontánea",
                        "5. Neoplasia intracraneal, malformación arteriovenosa o aneurisma gigante",
                        "6. PA persistente elevada (Sistólica >185 mmHg o Diastólica >110 mmHg) refractaria"
                    )
                    val relatives = listOf(
                        "7. Síntomas de ACV leves que resuelven espontáneamente de forma rápida",
                        "8. Gestante en periodo de embarazo activo",
                        "9. Convulsiones al inicio del cuadro con déficit neurológico postictal residual",
                        "10. Cirugía mayor o traumatismo grave extracraneal previo en últimos 14 días",
                        "11. Sangrado genitourinario o gastrointestinal activo en últimas 21 días",
                        "12. Infarto de miocardio (IAM) reciente en los últimos 3 meses"
                    )

                    val itemTrombolisisMap = trombolisisChecklist as MutableMap

                    val countsAbsolute = absolutes.indices.count { itemTrombolisisMap[it] == true }
                    val countsRelative = relatives.indices.count { itemTrombolisisMap[100 + it] == true }

                    val statusText: String
                    val statusDesc: String
                    val statusColor: Color
                    val statusBgColor: Color

                    when {
                        countsAbsolute > 0 -> {
                            statusText = "NO CANDIDATO"
                            statusDesc = "CONTRAINDICACIÓN ABSOLUTA detectada. No perfundir Alteplasa / Tenecteplasa bajo peligro de sangrado letal."
                            statusColor = colorScheme.error
                            statusBgColor = colorScheme.errorContainer.copy(alpha = 0.22f)
                        }
                        countsRelative > 0 -> {
                            statusText = "EVALUAR RIESGO/BENEFICIO (PRECAUCIÓN)"
                            statusDesc = "Contraindicación relativa detectada. Se sugiere interconsulta con neurólogo, evaluar extensión y beneficio potencial de reperfusión."
                            statusColor = Color(0xFFE6A23C)
                            statusBgColor = Color(0xFFFDF6EC)
                        }
                        else -> {
                            statusText = "CANDIDATO APTO"
                            statusDesc = "Sin contraindicaciones absolutas ni relativas detectadas. Proceder con protocolo de infusión estándar de urgencias."
                            statusColor = colorScheme.primary
                            statusBgColor = colorScheme.primaryContainer.copy(alpha = 0.35f)
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Checklist de Trombólisis de Acción Rápida",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Validador rápido de contraindicaciones absolutas y relativas para infusión de Alteplasa / Tenecteplasa.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = statusBgColor),
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(statusColor, shape = androidx.compose.foundation.shape.CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ESTADO: $statusText",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = statusDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (countsAbsolute == 0) {
                                        Button(
                                            onClick = { onNavigateToDrug("Alteplasa") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.weight(1f).testTag("go_to_alteplasa")
                                        ) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "Ver dosis", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ver Reperfusión")
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            val absoluteMatches = absolutes.filterIndexed { index, _ -> itemTrombolisisMap[index] == true }
                                            val relativeMatches = relatives.filterIndexed { index, _ -> itemTrombolisisMap[100 + index] == true }
                                            val matchedString = mutableListOf<String>()
                                            matchedString.add("Absolutas detectadas: " + if (absoluteMatches.isEmpty()) "Ninguna" else absoluteMatches.joinToString("; "))
                                            matchedString.add("Relativas detectadas: " + if (relativeMatches.isEmpty()) "Ninguna" else relativeMatches.joinToString("; "))

                                            onCopyClicked(
                                                "Checklist Trombólisis",
                                                statusText,
                                                matchedString,
                                                statusDesc
                                            )
                                        },
                                        modifier = Modifier.weight(1f).testTag("copy_trombolisis")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Smart Copy")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Contraindicaciones Absolutas (Hacer chequeo obligatorio):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    items(absolutes.size) { index ->
                        val text = absolutes[index]
                        val isChecked = itemTrombolisisMap[index] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { itemTrombolisisMap[index] = !isChecked }
                                .background(
                                    if (isChecked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { itemTrombolisisMap[index] = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }

                    item {
                        Text(
                            text = "Contraindicaciones Relativas (Evaluar margen riesgo/beneficio):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6A23C)
                        )
                    }

                    items(relatives.size) { index ->
                        val text = relatives[index]
                        val isChecked = itemTrombolisisMap[100 + index] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { itemTrombolisisMap[100 + index] = !isChecked }
                                .background(
                                    if (isChecked) Color(0xFFFDF6EC).copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { itemTrombolisisMap[100 + index] = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE6A23C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
                "gold_coast_als" -> {
                    val criteria = ClinicalDatabase.goldCoastEla
                    val item0 = goldCoastChecklist[0] == true
                    val item1 = goldCoastChecklist[1] == true

                    val statusText: String
                    val statusDesc: String
                    val statusColor: androidx.compose.ui.graphics.Color
                    val statusBgColor: androidx.compose.ui.graphics.Color

                    if (item0 && item1) {
                        statusText = "COMPATIBLE"
                        statusDesc = "Cumple con ambos criterios obligatorios de Gold Coast 2020 para el diagnóstico de ELA."
                        statusColor = colorScheme.primary
                        statusBgColor = colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else if (item0 && !item1) {
                        statusText = "EVALUACIÓN INCOMPLETA"
                        statusDesc = "Morfología compatible pero requiere exclusión activa y descarte razonable de diagnósticos diferenciales."
                        statusColor = colorScheme.error
                        statusBgColor = colorScheme.errorContainer.copy(alpha = 0.12f)
                    } else if (!item0 && item1) {
                        statusText = "NO COMPATIBLE"
                        statusDesc = "Falta afectación progresiva de motoneurona superior o inferior en el examen clínico."
                        statusColor = colorScheme.error
                        statusBgColor = colorScheme.errorContainer.copy(alpha = 0.22f)
                    } else {
                        statusText = "EVALUACIÓN INCOMPLETA"
                        statusDesc = "Por favor complete el chequeo clínico obligatorio para evaluar compatibilidad de ELA."
                        statusColor = colorScheme.outline
                        statusBgColor = colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${criteria.acronym} (${criteria.year})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = criteria.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        onCopyClicked(
                                            criteria.name,
                                            if (item0 && item1) "COMPATIBLE CON ELA" else "EVALUACIÓN DE ELA INCOMPLETA/NO COMPATIBLE",
                                            listOf(
                                                "MNS y MNI progresivos (Región >=1) o MNI (Regiones >=2): " + if (item0) "PRESENTE" else "AUSENTE",
                                                "Descarte razonable de diagnósticos alternativos: " + if (item1) "HECHO" else "PENDIENTE"
                                            ),
                                            "Criterios de Gold Coast 2020. Diagnóstico: $statusText"
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.End).testTag("copy_gold_coast")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Smart Copy Gold Coast")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = statusBgColor),
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(statusColor, shape = androidx.compose.foundation.shape.CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (statusText == "EVALUACIÓN INCOMPLETA" && statusColor == MaterialTheme.colorScheme.error) MaterialTheme.colorScheme.error else statusColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = statusDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Chequeo de Criterios Obligatorios:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(criteria.sections[0].items.size) { index ->
                        val text = criteria.sections[0].items[index]
                        val isChecked = goldCoastChecklist[index] == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    (goldCoastChecklist as MutableMap)[index] = !isChecked
                                }
                                .background(
                                    if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { (goldCoastChecklist as MutableMap)[index] = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
                "miopatias_eular" -> {
                    val criteria = ClinicalDatabase.miopatiasInflamatorias
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${criteria.acronym} - Miopatías Inflamatorias Idiopáticas",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = criteria.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Formas Clínicas Clínicamente Distinguibles:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(criteria.subclassifications) { subclass ->
                        var isExpanded by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .testTag("subclass_${subclass.acronym}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${subclass.name} (${subclass.acronym})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(onClick = { isExpanded = !isExpanded }) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expandir"
                                        )
                                    }
                                }
                                Text(
                                    text = subclass.criteria,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        Text(text = "Sellos Clínicos Claves:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        subclass.clinicalPoints.forEach { point ->
                                            Text(
                                                text = "• $point",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                onCopyClicked(
                                                    "Criterios de ${subclass.name}",
                                                    "SOSPECHA ALTA / CRITERIO COMPATIBLE",
                                                    subclass.clinicalPoints,
                                                    "Concordante con clasificaciones EULAR/ACR 2017 para miopatías inflamatorias."
                                                )
                                            },
                                            modifier = Modifier.align(Alignment.End).testTag("copy_${subclass.acronym}")
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Saves to Clipboard")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "toast" -> {
                    val criteria = ClinicalDatabase.toastAcv
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = criteria.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = criteria.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Clasificación Etiológica (Seleccione una para Smart Copy):",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    val itemsText = criteria.sections[0].items
                    items(itemsText.size) { index ->
                        val text = itemsText[index]
                        val isSelected = selectedToastType == index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToastTypeSelected(index) }
                                .padding(vertical = 2.dp)
                                .testTag("toast_item_$index"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onToastTypeSelected(index) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                onCopyClicked(
                                                    "Clasificación Etiológica TOAST",
                                                    text.substringBefore(":"),
                                                    listOf(text.substringAfter(":")),
                                                    "Clasificación de subtipo de ACV isquémico agudo."
                                                )
                                            },
                                            modifier = Modifier.align(Alignment.End).testTag("copy_toast_selected")
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copia Diagnóstico TOAST")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "ilae_epilepsy" -> {
                    val criteria = ClinicalDatabase.epilepsyClassification
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = criteria.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = criteria.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    items(criteria.sections) { section ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                section.items.forEach { line ->
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        onCopyClicked(
                                            criteria.name,
                                            section.title,
                                            section.items,
                                            "Compendio ILAE para clasificación oficial."
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Section")
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun TabFarmacos(
    filterQuery: String,
    onFilterQueryChange: (String) -> Unit,
    onCopyClicked: (String, String, String) -> Unit
) {
    val filteredDrugs = if (filterQuery.isBlank()) {
        ClinicalDatabase.drugs
    } else {
        ClinicalDatabase.drugs.filter {
            it.name.contains(filterQuery, ignoreCase = true) || it.acronym.contains(filterQuery, ignoreCase = true)
        }
    }

    val categories = listOf(
        "Bloqueadores de Sodio y Espectro Amplio",
        "Ligandos SV2 y Moduladores",
        "GABAérgicos, Anhidrasa y afines",
        "Dianas Específicas y Síndromes",
        "Inmunomoduladores / Reperfusión"
    )

    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = filterQuery,
            onValueChange = onFilterQueryChange,
            placeholder = { Text("Filtrar medicamentos específicos (e.g., Levetiracetam, CBZ)...") },
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filtrar") },
            trailingIcon = {
                if (filterQuery.isNotEmpty()) {
                    IconButton(onClick = { onFilterQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filterQuery.isNotBlank()) {
                items(filteredDrugs) { drug ->
                    DrugCard(drug = drug, onCopyClicked = onCopyClicked)
                }
            } else {
                categories.forEach { categoryName ->
                    val drugsInCat = filteredDrugs.filter { it.category == categoryName }
                    if (drugsInCat.isNotEmpty()) {
                        val isExpanded = expandedFolders[categoryName] == true
                        item(key = categoryName) {
                            Card(
                                onClick = { expandedFolders[categoryName] = !isExpanded },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("folder_$categoryName"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = "Carpeta",
                                            tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Column {
                                            Text(
                                                text = categoryName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${drugsInCat.size} medicamentos anticrisis",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expandir carpeta",
                                        tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (isExpanded) {
                            items(drugsInCat) { drug ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(140.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        DrugCard(drug = drug, onCopyClicked = onCopyClicked)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DrugCard(
    drug: ClinicalDatabase.DrugReference,
    onCopyClicked: (String, String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("drug_card_${drug.acronym}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = "${drug.name} (${drug.acronym})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = drug.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    )
                }
                IconButton(
                    onClick = {
                        onCopyClicked(
                            "${drug.name} (${drug.acronym})",
                            drug.dosage,
                            "Indicación: ${drug.indications}\nInteracciones/Efectos: ${drug.sideEffects}\nNotas: ${drug.clinicalNotes}"
                        )
                    },
                    modifier = Modifier.testTag("copy_${drug.acronym}")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.outline)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Text(text = "Indicaciones:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(text = drug.indications, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))

            Text(text = "Dosis y Administración:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(text = drug.dosage, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))

            Text(text = "Efectos Adversos Críticos:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(text = drug.sideEffects, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Sello Clínico:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text(text = drug.clinicalNotes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentHistorySection(
    calculations: List<com.example.data.SavedCalculation>,
    onClearClicked: () -> Unit,
    onCopyItemClicked: (com.example.data.SavedCalculation) -> Unit
) {
    if (calculations.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_history_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Historial de cálculos",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Historial Sesión Activa (<12h)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                TextButton(
                    onClick = onClearClicked,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Borrar sesión", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            calculations.forEach { calc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = calc.scaleName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Resultado: ${calc.scoreText}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (calc.interpretation.isNotEmpty()) {
                                Text(
                                    text = calc.interpretation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { onCopyItemClicked(calc) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar de nuevo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabOtrasEscalas(
    selectedMrsGrade: Int?,
    onMrsGradeSelected: (Int?) -> Unit,
    selectedFastStage: Int?,
    onFastStageSelected: (Int?) -> Unit,
    selectedMgfaClass: Int?,
    onMgfaClassSelected: (Int?) -> Unit,
    recentHistoryContent: @Composable () -> Unit,
    onCopyClicked: (String, String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            recentHistoryContent()
        }
        // FAST Functional Assessment Staging Tool
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fast_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FAST (Functional Assessment Staging Tool)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Escala clínica para clasificar la progresión funcional de la Enfermedad de Alzheimer desde normalidad a estadío severo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Seleccione etapa funcional:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Dynamic selections buttons
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClinicalDatabase.fastStages.forEachIndexed { i, stageText ->
                            val numStr = stageText.substringBefore(":").substringBefore("-").trim()
                            val isSelected = selectedFastStage == i
                            Surface(
                                modifier = Modifier
                                    .clickable { onFastStageSelected(i) }
                                    .testTag("fast_option_$i"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = numStr,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    selectedFastStage?.let { stageIdx ->
                        val text = ClinicalDatabase.fastStages[stageIdx]
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onCopyClicked("Escala FAST (Alzheimer)", text.substringBefore(":"), text) },
                            modifier = Modifier.align(Alignment.End).testTag("copy_fast")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Smart Copy FAST")
                        }
                    }
                }
            }
        }

        // mRS Modified Rankin Scale
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mrs_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Escala de Rankin Modificada (mRS)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Escala de uso diario en accidentes cerebrovasculares para medir la discapacidad global del enfermo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Seleccione grado funcional:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClinicalDatabase.mrsGrades.forEachIndexed { i, gradeText ->
                            val numStr = gradeText.substringBefore(":").trim()
                            val isSelected = selectedMrsGrade == i
                            Surface(
                                modifier = Modifier
                                    .clickable { onMrsGradeSelected(i) }
                                    .testTag("mrs_option_$i"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = numStr,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    selectedMrsGrade?.let { gradeIdx ->
                        val text = ClinicalDatabase.mrsGrades[gradeIdx]
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onCopyClicked("Escala de Rankin Modificada (mRS)", text.substringBefore(":"), text) },
                            modifier = Modifier.align(Alignment.End).testTag("copy_mrs")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Smart Copy mRS")
                        }
                    }
                }
            }
        }

        // MGFA Classification
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mgfa_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Clasificación Clínica de MGFA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Clasificación clínica oficial de la Myasthenia Gravis Foundation of America para identificar severidad de la afectación neuromuscular.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Clase de Miastenia:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClinicalDatabase.mgfaClasses.forEachIndexed { i, classText ->
                            val classLabel = classText.substringBefore(":").replace("Clase", "").trim()
                            val isSelected = selectedMgfaClass == i
                            Surface(
                                modifier = Modifier
                                    .clickable { onMgfaClassSelected(i) }
                                    .testTag("mgfa_option_$i"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = classLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    selectedMgfaClass?.let { classIdx ->
                        val text = ClinicalDatabase.mgfaClasses[classIdx]
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onCopyClicked("Clasificación de Miastenia MGFA", text.substringBefore(":"), text) },
                            modifier = Modifier.align(Alignment.End).testTag("copy_mgfa")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Smart Copy MGFA")
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}
