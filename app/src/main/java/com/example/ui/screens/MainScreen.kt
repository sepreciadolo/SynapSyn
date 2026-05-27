package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape

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

    // Navigation and search states
    var activeTab by remember { mutableStateOf(0) } // 0: Calculadoras, 1: Criterios, 2: Fármacos, 3: Otras Escalas
    var searchQuery by remember { mutableStateOf("") }
    var activeCalculatorId by remember { mutableStateOf<String?>(null) }
    var activeCriterioId by remember { mutableStateOf<String?>(null) }

    // Selected items for active calculation
    var selectedNihssAnswers by remember { mutableStateOf(mapOf<String, Int>()) }
    var usePostNihss by remember { mutableStateOf(false) }
    var selectedAlsfrsrAnswers by remember { mutableStateOf(mapOf<String, Int>()) }
    var selectedQmgAnswers by remember { mutableStateOf(mapOf<String, Int>()) }
    var selectedDragonAnswers by remember { mutableStateOf(mapOf<String, Int>()) }

    // State definitions for newly added clinical calculators
    // ASPECTS: Ischemic areas map
    var selectedAspectsRegions by remember { mutableStateOf(setOf<String>()) }
    var selectedPcAspectsRegions by remember { mutableStateOf(setOf<String>()) }
    var aspectsIsPosterior by remember { mutableStateOf(false) }

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
        if (tabIndex == 1) {
            activeCalculatorId = id
        } else if (tabIndex == 2) {
            activeCriterioId = id
        }
    }

    Scaffold(
        topBar = {
            if (activeCalculatorId == null && activeCriterioId == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (darkTheme) Color.Black else MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SynAppSe",
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

                    if (activeTab == 0) {
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
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = if (darkTheme) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                tonalElevation = 0.dp
            ) {
                val navItems = remember {
                    listOf(
                        NavItem(label = "Calcular", icon = Icons.Default.Calculate, logicalIndex = 1),
                        NavItem(label = "Criterios", icon = Icons.Default.FactCheck, logicalIndex = 2),
                        NavItem(label = "Inicio", icon = Icons.Default.Home, logicalIndex = 0, isCenter = true),
                        NavItem(label = "Fármacos", icon = Icons.Default.Medication, logicalIndex = 3),
                        NavItem(label = "Exploración", icon = Icons.Default.AccessibilityNew, logicalIndex = 4)
                    )
                }
                navItems.forEach { item ->
                    val isSelected = activeTab == item.logicalIndex && searchQuery.isEmpty()
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { 
                            activeTab = item.logicalIndex 
                            searchQuery = ""
                        },
                        icon = {
                            if (item.isCenter) {
                                val centerBg = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    if (darkTheme) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                }
                                val centerTint = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(23.dp))
                                        .background(centerBg)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = centerTint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(item.icon, contentDescription = item.label)
                            }
                        },
                        label = if (item.isCenter) null else {
                            {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        },
                        colors = if (item.isCenter) {
                            NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        } else {
                            NavigationBarItemDefaults.colors()
                        },
                        alwaysShowLabel = !item.isCenter,
                        modifier = Modifier.testTag("nav_tab_${item.logicalIndex}")
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
                    Triple("FAST - Demencia", "Geriátria/Cognitiva", 4),
                    Triple("mRS - Rankin Modificado", "Vascular/Funcional", 4),
                    Triple("MGFA - Miastenia Gravis", "Placa Neuromuscular", 4)
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
                                    .clickable { onSearchMatchSelected(scale.id, 1) },
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
                                    .clickable { onSearchMatchSelected(criteria.id, 2) },
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
                                    .clickable { onSearchMatchSelected(drug.name, 3) },
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
                    0 -> TabInicio(
                        onNavigateToTab = { index -> activeTab = index },
                        onNavigateToDrug = { drugName ->
                            targetDrugFilter = drugName
                            activeTab = 3 // Switch to TabFarmacos
                        },
                        savedCalculations = savedCalculations,
                        onClearCalculations = { scope.launch { calculationRepository.clear() } },
                        onCopyCalculation = { calc ->
                            val detailsList = calc.details.split(", ")
                            val textToCopy = formatSmartCopy(calc.scaleName, calc.scoreText, detailsList, calc.interpretation)
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "Copiado al portapapeles con éxito", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> TabCalculadoras(
                        selectedNihssAnswers = selectedNihssAnswers,
                        onNihssAnswerChanged = { k, v -> selectedNihssAnswers = selectedNihssAnswers + (k to v) },
                        onResetNihss = { 
                            selectedNihssAnswers = emptyMap()
                            usePostNihss = false
                        },
                        usePostNihss = usePostNihss,
                        onUsePostNihssChanged = { usePostNihss = it },
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
                        onResetAspects = { 
                            selectedAspectsRegions = emptySet()
                            selectedPcAspectsRegions = emptySet()
                        },
                        selectedPcAspectsRegions = selectedPcAspectsRegions,
                        onPcAspectsRegionsChanged = { selectedPcAspectsRegions = it },
                        aspectsIsPosterior = aspectsIsPosterior,
                        onAspectsIsPosteriorChanged = { aspectsIsPosterior = it },
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
                                val currentTotal = ClinicalDatabase.nihss.domains.sumOf { selectedNihssAnswers[it.id] ?: 0 }
                                "NIHSS " + ClinicalDatabase.nihss.domains.joinToString(" ") { domain ->
                                    "${domain.id}:${selectedNihssAnswers[domain.id] ?: 0}"
                                } + " (Total: $currentTotal)"
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
                        },
                        activeCalculatorId = activeCalculatorId,
                        onActiveCalculatorIdChanged = { activeCalculatorId = it }
                    )
                    2 -> TabCriterios(
                        goldCoastChecklist = goldCoastChecklist,
                        trombolisisChecklist = trombolisisChecklist,
                        selectedToastType = selectedToastType,
                        onToastTypeSelected = { selectedToastType = it },
                        onNavigateToDrug = { drugName ->
                            targetDrugFilter = drugName
                            activeTab = 3 // Switch to TabFarmacos (index 3 now)
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
                        },
                        activeCriterioId = activeCriterioId,
                        onActiveCriterioIdChanged = { activeCriterioId = it }
                    )
                    3 -> TabFarmacos(
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
                    4 -> TabGuiasYEscalas(
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
                        },
                        onNavigateToDrug = { drugName ->
                            targetDrugFilter = drugName
                            activeTab = 3
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
    builder.append("REGISTRO EVALUADO: $label\n")
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
    return builder.toString().trim()
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
    usePostNihss: Boolean,
    onUsePostNihssChanged: (Boolean) -> Unit,
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
    selectedPcAspectsRegions: Set<String>,
    onPcAspectsRegionsChanged: (Set<String>) -> Unit,
    aspectsIsPosterior: Boolean,
    onAspectsIsPosteriorChanged: (Boolean) -> Unit,

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

    onCopyClicked: (String, String, List<String>, String?) -> Unit,
    activeCalculatorId: String?,
    onActiveCalculatorIdChanged: (String?) -> Unit
) {

    var isNihssCalculated by remember { mutableStateOf(false) }
    var nihssCalculatedScore by remember { mutableStateOf(0) }
    var nihssCalculatedSummary by remember { mutableStateOf("") }

    LaunchedEffect(selectedNihssAnswers) {
        if (selectedNihssAnswers.isEmpty()) {
            isNihssCalculated = false
            nihssCalculatedScore = 0
            nihssCalculatedSummary = ""
        }
    }

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

    // --- NUEVOS ESTADOS PARA CALCULADORAS DE EPILEPSIA ---
    var selectS by remember { mutableStateOf(0) }
    var selectEtiology by remember { mutableStateOf(0) }
    var selectL by remember { mutableStateOf(0) }
    var selectE2 by remember { mutableStateOf(0) }
    var selectC by remember { mutableStateOf(0) }

    var selectAsysSex by remember { mutableStateOf(0) }
    var selectAsysCortical by remember { mutableStateOf(0) }
    var selectAsysAthero by remember { mutableStateOf(0) }
    var selectAsysType by remember { mutableStateOf(0) }
    var selectAsysTime by remember { mutableStateOf(0) }
    var selectAsysMonths by remember { mutableStateOf(12) }

    var caveCortical by remember { mutableStateOf(false) }
    var caveAgeUnder65 by remember { mutableStateOf(false) }
    var caveVolumeUnder10 by remember { mutableStateOf(false) }
    var caveEarlySeizure by remember { mutableStateOf(false) }

    var cave2Cortical by remember { mutableStateOf(false) }
    var cave2AgeUnder65 by remember { mutableStateOf(false) }
    var cave2VolumeUnder10 by remember { mutableStateOf(false) }
    var cave2EarlySeizure by remember { mutableStateOf(false) }
    var cave2Severity by remember { mutableStateOf(false) }

    var laneLobar by remember { mutableStateOf(false) }
    var laneAgeUnder60 by remember { mutableStateOf(false) }
    var laneNihss10Plus by remember { mutableStateOf(false) }
    var laneEarlySeizure by remember { mutableStateOf(false) }

    var riseRebleed by remember { mutableStateOf(false) }
    var riseIch by remember { mutableStateOf(false) }
    var riseSeizure by remember { mutableStateOf(false) }
    var riseCoiling by remember { mutableStateOf(false) }

    var dias3Hemorrhage by remember { mutableStateOf(false) }
    var dias3Seizure by remember { mutableStateOf(false) }
    var dias3Sinus by remember { mutableStateOf(false) }

    // --- ESTADO PARA GDS REISBERG ---
    var calcGdsStage by remember { mutableStateOf(1) }

    // --- NUEVOS ESTADOS PARA NEUROINFECTO ---
    var thwaitesAge by remember { mutableStateOf(0) }
    var thwaitesWbc by remember { mutableStateOf(0) }
    var thwaitesHistory by remember { mutableStateOf(0) }
    var thwaitesCsfWbc by remember { mutableStateOf(0) }
    var thwaitesCsfNeutro by remember { mutableStateOf(0) }

    var selectedPathology by remember { mutableStateOf("todas") }

    val calculatorPathologyMap = mapOf(
        "nihss" to "stroke",
        "aspects" to "stroke",
        "ich" to "stroke",
        "four" to "stroke",
        "dragon" to "stroke",
        "rabdomiolisis" to "stroke",
        
        "select" to "epilepsia",
        "select_asys" to "epilepsia",
        "cave" to "epilepsia",
        "cave2" to "epilepsia",
        "lane" to "epilepsia",
        "rise" to "epilepsia",
        "dias3" to "epilepsia",
        
        "edss" to "desmielinizante",
        
        "alsfrsr" to "union_neuromuscular",
        "qmg" to "union_neuromuscular",
        "mrc_sum" to "union_neuromuscular",
        "mmt8" to "union_neuromuscular",
        "egris" to "union_neuromuscular",
        "megos" to "union_neuromuscular",
        
        "ledd" to "movimientos_anormales",
        "aims" to "movimientos_anormales",
        "mdsupdrs" to "movimientos_anormales",
        "hoehn_yahr" to "movimientos_anormales",
        "toxina" to "movimientos_anormales",
        
        "thwaites" to "neuroinfecto",
        "lcr" to "neuroinfecto",
        
        "hachinski" to "otros",
        "cdr" to "otros",
        "gds_reisberg" to "otros",
        "gds15" to "otros",
        "four_delirium" to "otros"
    )

    val allCalculators = listOf(
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
        Pair("gds_reisberg", "GDS Reisberg (Alzheimer)"),
        Pair("gds15", "GDS-15 Depresión"),
        Pair("four_delirium", "4AT Delirium"),
        Pair("mrc_sum", "MRC Sum Score"),
        Pair("mmt8", "MMT-8 Muscular"),
        Pair("rabdomiolisis", "Rabdomiólisis"),
        Pair("select", "SeLECT (Epilepsia)"),
        Pair("select_asys", "SeLECT-ASyS (Epilepsia)"),
        Pair("cave", "CAVE (Epilepsia)"),
        Pair("cave2", "CAVE2 (Epilepsia)"),
        Pair("lane", "LANE (Epilepsia)"),
        Pair("rise", "RISE (Epilepsia)"),
        Pair("dias3", "DIAS3 (Epilepsia)"),
        Pair("thwaites", "Thwaites (TBM)")
    )

    val filteredCalculators = allCalculators.filter { (id, _) ->
        selectedPathology == "todas" || calculatorPathologyMap[id] == selectedPathology
    }

    var searchQueryCalculadoras by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (activeCalculatorId == null) {
                if (selectedPathology == "todas") {
                    // Search entry inside the Directory catálogo
                    OutlinedTextField(
                        value = searchQueryCalculadoras,
                        onValueChange = { searchQueryCalculadoras = it },
                        placeholder = { Text("Buscar escala neurológica (e.g. NIHSS, EDSS)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        trailingIcon = {
                            if (searchQueryCalculadoras.isNotEmpty()) {
                                IconButton(onClick = { searchQueryCalculadoras = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("search_calculadora_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )
                }

                if (searchQueryCalculadoras.isNotEmpty()) {
                    // Match Results View
                    val results = allCalculators.filter {
                        it.second.contains(searchQueryCalculadoras, ignoreCase = true) ||
                        it.first.contains(searchQueryCalculadoras, ignoreCase = true)
                    }

                    if (results.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "No encontrado",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No se encontraron calculadoras médicas para \"$searchQueryCalculadoras\"",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "Resultados de búsqueda",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(results) { (id, label) ->
                                val category = calculatorPathologyMap[id] ?: "otros"
                                val categoryLabel = when (category) {
                                    "stroke" -> "Stroke / Ictus / Vascular"
                                    "epilepsia" -> "Epilepsia / Crisis"
                                    "desmielinizante" -> "Desmielinizante"
                                    "union_neuromuscular" -> "Unión Neuromuscular"
                                    "movimientos_anormales" -> "Trastornos del Movimiento"
                                    "neuroinfecto" -> "Neuroinfección / LCR"
                                    else -> "Otros"
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            searchQueryCalculadoras = ""
                                            onActiveCalculatorIdChanged(id) 
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = label, 
                                                style = MaterialTheme.typography.titleMedium, 
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Área: $categoryLabel", 
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Abrir",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedPathology == "todas") {
                    // MAIN DIRECTORIES LAYOUT
                    val directoryCategories = listOf(
                        Triple("stroke", "Ictus, Vascular & Triage", "NIHSS, ASPECTS, ICH, FOUR, DRAGON, Rabdomiólisis"),
                        Triple("epilepsia", "Epilepsia & Crisis Agudas", "SeLECT, SeLECT-ASyS, CAVE, CAVE2, LANE, RISE, DIAS3"),
                        Triple("desmielinizante", "Neuroinmunología / EDSS", "Línea temporal y nivel de discapacidad (EDSS)"),
                        Triple("union_neuromuscular", "Unión Neuromuscular & Miopatías", "ALSFRS-R, QMG, escalas de debilidad segmentaria mEGOS y EGRIS"),
                        Triple("movimientos_anormales", "Trastornos del Movimiento", "Cálculo de dopamina LEDD, AIMS, MDS-UPDRS, H&Y"),
                        Triple("neuroinfecto", "Neuroinfección & Lab LCR", "Indexación Thwaites y análisis de LCR"),
                        Triple("otros", "Cognitivo, Delirium y Depresión", "Criterios Hachinski, CDR Clínico, GDS-15, Triage 4AT")
                    )

                    val calculatorCounts = remember {
                        calculatorPathologyMap.values.groupingBy { it }.eachCount()
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Categorías de Calculadoras",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(directoryCategories) { (id, title, details) ->
                            val icon = when (id) {
                                "stroke" -> Icons.Default.LocalHospital
                                "epilepsia" -> Icons.Default.Bolt
                                "desmielinizante" -> Icons.Default.AutoAwesome
                                "union_neuromuscular" -> Icons.Default.Accessibility
                                "movimientos_anormales" -> Icons.Default.DirectionsRun
                                "neuroinfecto" -> Icons.Default.Biotech
                                else -> Icons.Default.Psychology
                            }
                            val count = calculatorCounts[id] ?: 0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPathology = id },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(100.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Text(
                                                    text = "$count",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = details,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Abrir categoría",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        // Ver todas las escalas card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPathology = "todas_escalas" },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.List,
                                                contentDescription = "Todas las escalas",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Todas las Calculadoras (${allCalculators.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Índice alfabético completo de todas las calculadoras integradas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Ver todas",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SUBDIRECTORY VIEW
                    val subPathologyList = if (selectedPathology == "todas_escalas") {
                        allCalculators
                    } else {
                        allCalculators.filter { calculatorPathologyMap[it.first] == selectedPathology }
                    }
                    val categoryLabel = when (selectedPathology) {
                        "stroke" -> "Ictus, Vascular & Triage"
                        "epilepsia" -> "Epilepsia & Crisis Agudas"
                        "desmielinizante" -> "Neuroinmunología / EDSS"
                        "union_neuromuscular" -> "Unión Neuromuscular"
                        "movimientos_anormales" -> "Trastornos del Movimiento"
                        "neuroinfecto" -> "Neuroinfección / LCR"
                        "todas_escalas" -> "Todas las Calculadoras"
                        else -> "Otros / Cognitivos"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedPathology = "todas" }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Volver al catálogo"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(subPathologyList) { (id, label) ->
                            val descStr = when (id) {
                                "nihss" -> "Escala neurológica cuantitativa para evaluar la gravedad de un ictus isquémico agudo."
                                "aspects" -> "Sistema radiológico semicuantitativo de 10 puntos para evaluar cambios isquémicos tempranos."
                                "ich" -> "Modelo de estratificación pronóstica clínica a los 30 días para hemorragia intracerebral."
                                "four" -> "Evaluación clínica detallada y precisa del coma, reflejos de tronco y respuesta motora."
                                "edss" -> "Escala de Kurtzke para cuantificar la discapacidad funcional en Esclerosis Múltiple."
                                "alsfrsr" -> "Escala de valoración funcional de pacientes diagnosticados de Esclerosis Lateral Amiotrófica."
                                "qmg" -> "Valoración cuantitativa de gravedad de la Miastenia Gravis basado en fuerza muscular."
                                "dragon" -> "Puntaje pronóstico de recanalización y hemorragia sintomática en terapia de reperfusión."
                                "lcr" -> "Herramienta diagnóstica para correlación clínica acelerada de parámetros químicos de LCR."
                                "egris" -> "Score predictivo de insuficiencia respiratoria temprana a la semana del Guillain-Barré."
                                "megos" -> "Score modificado de mEGOS para predecir capacidad de marcha a las semanas del GBS."
                                "ledd" -> "Dosis Equivalente Diaria de Levodopa (LEDD) para control de antiparkinsonianos."
                                "aims" -> "Escala de Movimientos Involuntarios Anormales para evaluar discinesia tardía."
                                "mdsupdrs" -> "MDS-UPDRS sección motora rápida y metrónomo de marcha calibrado."
                                "hoehn_yahr" -> "Clasificación de la progresión y grado de discapacidad clínica en Parkinson."
                                "toxina" -> "Calculadora de dilución volumétrica de Toxina Botulínica por dosis terapéutica."
                                "hachinski" -> "Puntaje de isquemia para diferenciar demencia vascular de degenerativa."
                                "cdr" -> "Clasificación de Demencia Clínica para estadiaje cognitivo general."
                                "gds_reisberg" -> "Escala de Deterioro Global (GDS) de Reisberg para estadificación clínica del Alzheimer y demencias."
                                "gds15" -> "Escala de depresión geriátrica abreviada de Yesavage de 15 afirmaciones."
                                "four_delirium" -> "Triage rápido de evaluación de Delirium agudo y atención sostenida."
                                "mrc_sum" -> "Suma de balance muscular segmental MRC para cuadriparesia y debilidad en UCI."
                                "mmt8" -> "Test Manual Muscular de 8 grupos musculares principales para miopatías clínicas."
                                "rabdomiolisis" -> "Cálculo de riesgo de fallo renal por CK, mioglobina y equilibrio ácido-base."
                                "select" -> "SeLECT Score para predecir el riesgo de crisis epiléptica tardía post-ictus vascular."
                                "select_asys" -> "Score predictivo adaptado para crisis tempranas asociadas a estado epiléptico sintomático."
                                "cave" -> "Score predictivo para crisis epilépticas tardías tras una hemorragia intracerebral espontánea."
                                "cave2" -> "CAVE2 Score con parámetros específicos para riesgo epileptógeno a largo plazo."
                                "lane" -> "Score predictivo de crisis epilépticas tempranas y tardías en pacientes con ictus lober."
                                "rise" -> "Score de riesgo de crisis recidivante tras hemorragia subaracnoidea tratada."
                                "dias3" -> "DIAS3 Score predictivo de crisis epilépticas en trombosis de senos venosos cerebrales."
                                "thwaites" -> "Índice diagnóstico de Thwaites para diferenciar meningitis tuberculosa de bacteriana."
                                else -> "Calculadora clínica neurológica adaptada para soporte clínico rápido."
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onActiveCalculatorIdChanged(id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = label, 
                                            style = MaterialTheme.typography.titleMedium, 
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = descStr, 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Abrir calculadora",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Focus header for active calculator inside focused screen
                val activeCalcLabel = allCalculators.find { it.first == activeCalculatorId }?.second ?: ""
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onActiveCalculatorIdChanged(null) },
                        modifier = Modifier.testTag("back_to_catalog")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver al catálogo"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeCalcLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Formulario de Evaluación de Paciente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (activeCalculatorId) {
                    "nihss" -> {
                        item {
                            CollapsibleIntroCard(
                                title = "NIHSS (National Institutes of Health Stroke Scale)",
                                description = ClinicalDatabase.nihss.description
                            )
                        }
                        
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("toggle_post_nihss_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Extensión POST-NIHSS (NIHSS+)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "Monitorear fosa posterior (ataxia y signos bulbares)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = usePostNihss,
                                            onCheckedChange = { onUsePostNihssChanged(it) },
                                            modifier = Modifier.testTag("toggle_post_nihss")
                                        )
                                    }
                                    if (usePostNihss) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Añade evaluación de ataxia de tronco/marcha, disfagia y tos anormal (+12 pts máx). Útil en ictus de fosa posterior con síntomas leves (<10 NIHSS) para guiar trombectomía o trombólisis.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
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
                                onOptionSelected = { 
                                    onNihssAnswerChanged(domain.id, it)
                                    isNihssCalculated = false
                                }
                            )
                        }

                        if (usePostNihss) {
                            item {
                                Text(
                                    text = "ÍTEMS ADICIONALES POST-NIHSS / NIHSS+",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                )
                            }
                            item {
                                ScaleDomainSelectionBlock(
                                    id = "post_ataxia",
                                    label = "Ext.1 Ataxia Troncal o de la Marcha",
                                    description = "Evaluación de la estabilidad del tronco al sentarse o equilibrio general de la marcha sin ayuda.",
                                    options = listOf(
                                        com.example.data.ScaleOption("Ausente (0 pt)", 0),
                                        com.example.data.ScaleOption("Presente (3 pts)", 1)
                                    ),
                                    selectedValue = selectedNihssAnswers["post_ataxia"] ?: 0,
                                    onOptionSelected = {
                                        onNihssAnswerChanged("post_ataxia", it)
                                        isNihssCalculated = false
                                    }
                                )
                            }
                            item {
                                ScaleDomainSelectionBlock(
                                    id = "post_dysphagia",
                                    label = "Ext.2 Disfagia o Alteración de Deglución",
                                    description = "Evaluación bedside de reflejos de deglución; parálisis o paresia velofaríngea evidente, babeo.",
                                    options = listOf(
                                        com.example.data.ScaleOption("Ausente (0 pt)", 0),
                                        com.example.data.ScaleOption("Presente (4 pts)", 1)
                                    ),
                                    selectedValue = selectedNihssAnswers["post_dysphagia"] ?: 0,
                                    onOptionSelected = {
                                        onNihssAnswerChanged("post_dysphagia", it)
                                        isNihssCalculated = false
                                    }
                                )
                            }
                            item {
                                ScaleDomainSelectionBlock(
                                    id = "post_cough",
                                    label = "Ext.3 Tos Anormal (Carraspeo Ineficaz)",
                                    description = "Incapacidad para producir tos voluntaria efectiva y coordinada, o carraspeo ausente.",
                                    options = listOf(
                                        com.example.data.ScaleOption("Ausente / Normal (0 pt)", 0),
                                        com.example.data.ScaleOption("Presente (5 pts)", 1)
                                    ),
                                    selectedValue = selectedNihssAnswers["post_cough"] ?: 0,
                                    onOptionSelected = {
                                        onNihssAnswerChanged("post_cough", it)
                                        isNihssCalculated = false
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nihss_calc_result_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = {
                                            val standardTotal = ClinicalDatabase.nihss.domains.sumOf { selectedNihssAnswers[it.id] ?: 0 }
                                            val postAtaxia = if (selectedNihssAnswers["post_ataxia"] == 1) 3 else 0
                                            val postDysphagia = if (selectedNihssAnswers["post_dysphagia"] == 1) 4 else 0
                                            val postCough = if (selectedNihssAnswers["post_cough"] == 1) 5 else 0
                                            val extraPoints = postAtaxia + postDysphagia + postCough
                                            val currentTotal = if (usePostNihss) standardTotal + extraPoints else standardTotal
                                            nihssCalculatedScore = currentTotal
                                            val basicSummary = ClinicalDatabase.nihss.domains.joinToString(" ") { d ->
                                                "${d.id}:${selectedNihssAnswers[d.id] ?: 0}"
                                            }
                                            nihssCalculatedSummary = if (usePostNihss) {
                                                "NIHSS $basicSummary | POST-NIHSS Ext: Ataxia:${postAtaxia} Dysphagia:${postDysphagia} Cough:${postCough} (Total: $currentTotal/54)"
                                            } else {
                                                "NIHSS $basicSummary (Total: $standardTotal/42)"
                                            }
                                            isNihssCalculated = true
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("nihss_btn_calculate"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Calculate,
                                            contentDescription = "Calcular"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (usePostNihss) "Calcular POST-NIHSS" else "Calcular Puntaje Total", fontWeight = FontWeight.Bold)
                                    }

                                    if (isNihssCalculated) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "RESULTADO DE LA EVALUACIÓN",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "Puntaje Total: $nihssCalculatedScore puntos",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        val scoreCategory = when {
                                            nihssCalculatedScore == 0 -> "Normal"
                                            nihssCalculatedScore in 1..4 -> "Ictus Leve"
                                            nihssCalculatedScore in 5..15 -> "Ictus Moderado"
                                            nihssCalculatedScore in 16..20 -> "Ictus Moderadamente Grave"
                                            else -> "Ictus Grave"
                                        }
                                        
                                        Text(
                                            text = "Clasificación clínica: $scoreCategory",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = nihssCalculatedSummary,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        val localClipboard = LocalClipboardManager.current
                                        val localContext = LocalContext.current
                                        Button(
                                            onClick = {
                                                val standardTotal = ClinicalDatabase.nihss.domains.sumOf { selectedNihssAnswers[it.id] ?: 0 }
                                                val scoreCategory = when {
                                                    standardTotal == 0 -> "Normal"
                                                    standardTotal in 1..4 -> "Ictus Leve"
                                                    standardTotal in 5..15 -> "Ictus Moderado"
                                                    standardTotal in 16..20 -> "Ictus Moderadamente Grave"
                                                    else -> "Ictus Grave"
                                                }
                                                val breakdown = ClinicalDatabase.nihss.domains.map { domain ->
                                                    val valNum = selectedNihssAnswers[domain.id] ?: 0
                                                    "${domain.id}. ${domain.label.split(".").getOrNull(1)?.trim() ?: domain.label}: $valNum"
                                                }.toMutableList()
                                                if (usePostNihss) {
                                                    breakdown.add("POST-NIHSS Ataxia de la Marcha/Tronco: ${if (selectedNihssAnswers["post_ataxia"] == 1) "Presente (3 pts)" else "Ausente (0 pts)"}")
                                                     breakdown.add("POST-NIHSS Disfagia: ${if (selectedNihssAnswers["post_dysphagia"] == 1) "Presente (4 pts)" else "Ausente (0 pts)"}")
                                                     breakdown.add("POST-NIHSS Tos Anormal: ${if (selectedNihssAnswers["post_cough"] == 1) "Presente (5 pts)" else "Ausente (0 pts)"}")
                                                }
                                                onCopyClicked(
                                                    if (usePostNihss) "POST-NIHSS (NIHSS+ Posterior)" else ClinicalDatabase.nihss.name,
                                                     if (usePostNihss) "$nihssCalculatedScore (NIHSS Base: $standardTotal)" else "$nihssCalculatedScore ($scoreCategory)",
                                                    breakdown,
                                                    if (usePostNihss) "Compendio fosa posterior: Ataxia y bulbares agregados al NIHSS agudo." else "Evaluación neurológica cuantitativa en fase de ACV agudo."
                                                )
                                                // Toast.makeText(localContext, "Resumen copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("nihss_btn_copy_summary"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copiar Resumen"
                                             )
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Text("Copiar y Guardar Resumen", fontWeight = FontWeight.Bold)
                                         }
                                     }
                                 }
                             }
                         }
                     }
                    "aspects" -> {
                        item {
                            CollapsibleIntroCard(
                                title = "ASPECTS (Alberta Stroke Program Early CT Score)",
                                description = "Escala de 10 puntos para valorar la isquemia en el territorio de la ACM en la TAC. Cada área afectada resta 1 punto."
                            )
                        }

                        item {
                            AspectsVisualizer(
                                selectedRegions = selectedAspectsRegions,
                                onRegionToggled = { regId ->
                                    val isSelected = selectedAspectsRegions.contains(regId)
                                    onAspectsRegionsChanged(if (isSelected) selectedAspectsRegions - regId else selectedAspectsRegions + regId)
                                },
                                selectedPcRegions = selectedPcAspectsRegions,
                                onPcRegionToggled = { regId ->
                                    val isSelected = selectedPcAspectsRegions.contains(regId)
                                    onPcAspectsRegionsChanged(if (isSelected) selectedPcAspectsRegions - regId else selectedPcAspectsRegions + regId)
                                },
                                isPosterior = aspectsIsPosterior,
                                onModeChanged = { onAspectsIsPosteriorChanged(it) },
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
                                    if (!aspectsIsPosterior) {
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
                                    } else {
                                        Text(
                                            text = "Mapa Territorial pc-ASPECTS (Toque las áreas isquémicas / infartadas)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        // Thalamus + Occipital regions
                                        Text("Tálamos y Lóbulo Occipital (Cada uno resta 1 punto):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("T_R" to "Tál. Der", "T_L" to "Tál. Izq", "OL_R" to "Occip. Der", "OL_L" to "Occip. Izq").forEach { (regId, name) ->
                                                val isSelected = selectedPcAspectsRegions.contains(regId)
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            onPcAspectsRegionsChanged(if (isSelected) selectedPcAspectsRegions - regId else selectedPcAspectsRegions + regId)
                                                        }
                                                        .height(55.dp)
                                                        .testTag("pcaspects_${regId}"),
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

                                        // Brainstem regions
                                        Text("Tronco Cerebral (Fosa Posterior - Cada uno resta 2 puntos):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("M" to "Mesencéfalo (-2)", "P" to "Puente / Protub (-2)").forEach { (regId, name) ->
                                                val isSelected = selectedPcAspectsRegions.contains(regId)
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            onPcAspectsRegionsChanged(if (isSelected) selectedPcAspectsRegions - regId else selectedPcAspectsRegions + regId)
                                                        }
                                                        .height(55.dp)
                                                        .testTag("pcaspects_${regId}"),
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

                                        // Cerebellar hemispheres
                                        Text("Hemisferio Cerebeloso (Cada uno resta 1 punto):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("C_R" to "Cerebelo Der", "C_L" to "Cerebelo Izq").forEach { (regId, name) ->
                                                val isSelected = selectedPcAspectsRegions.contains(regId)
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            onPcAspectsRegionsChanged(if (isSelected) selectedPcAspectsRegions - regId else selectedPcAspectsRegions + regId)
                                                        }
                                                        .height(55.dp)
                                                        .testTag("pcaspects_${regId}"),
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
                            CollapsibleIntroCard(
                                title = "ALSFRS-R (Revised ALS Functional Rating Scale)",
                                description = ClinicalDatabase.alsfrsr.description
                            )
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
                            CollapsibleIntroCard(
                                title = "QMG (Quantitative Myasthenia Gravis)",
                                description = ClinicalDatabase.qmg.description
                            )
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
                            CollapsibleIntroCard(
                                title = "DRAGON Score",
                                description = ClinicalDatabase.dragon.description
                            )
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
                    "gds_reisberg" -> {
                        item {
                            CollapsibleIntroCard(
                                title = "GDS de Reisberg (Global Deterioration Scale)",
                                description = "Escala de Deterioro Global utilizada para clasificar y estadificar clínicamente la progresión del deterioro cognoscitivo en base a 7 estadios funcionales del Alzheimer y otras demencias degenerativas primarias."
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Seleccione el Estadio Clínico (1 al 7):",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // 1 to 7 selector
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        (1..7).forEach { num ->
                                            val isSelected = calcGdsStage == num
                                            val isDementia = num >= 4
                                            val containerColor = if (isSelected) {
                                                if (isDementia) Color(0xFFE28743) else MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            }
                                            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(containerColor)
                                                    .clickable { calcGdsStage = num }
                                                    .testTag("gds_calc_stage_$num"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = num.toString(),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = contentColor
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Display selected stage details inside
                                    val stagesList = mapOf(
                                        1 to Triple(
                                            "Sin deterioro cognitivo",
                                            "Normalidad clínica. No hay quejas subjetivas de memoria durante la entrevista clínica y no se observan fallos objetivos en la evaluación de la vida laboral o cotidiana.",
                                            "Fase Pre-demencia"
                                        ),
                                        2 to Triple(
                                            "Deterioro cognitivo muy leve (Olvidos)",
                                            "Olvidos discretos que el paciente queja subjetivamente (por ejemplo, dónde deja objetos, olvido de nombres comunes). No hay déficits objetivos en el trabajo ni alteración funcional.",
                                            "Fase Pre-demencia (Olvidos seniles normales)"
                                        ),
                                        3 to Triple(
                                            "Deterioro cognitivo leve (DCL / MCI)",
                                            "Primeros déficits de significación clínica. Desorientación al viajar a lugares nuevos; dificultad visible de concentración y denominación para encontrar palabras; pierde objetos de valor; rendimiento laboral disminuido notablemente.",
                                            "Fase Pre-demencia Límite (Deterioro Cognitivo Leve)"
                                        ),
                                        4 to Triple(
                                            "Deterioro cognitivo moderado (Demencia Leve)",
                                            "Fase borderline de demencia institucional o diagnóstica. Conocimiento mermado de acontecimientos de actualidad, fechas o historia personal. Deficiente capacidad de cálculo serial (7 en 7). Incapacidad para manejar presupuestos complejos o viajar solo. Sabe orientarse y reconoce familiares directos.",
                                            "Fase de Demencia (Leve)"
                                        ),
                                        5 to Triple(
                                            "Deterioro cognitivo moderadamente grave (Demencia Moderada)",
                                            "EL PACIENTE REQUIERE AYUDA PARA LA SUPERVIVENCIA AUTÓNOMA. No puede vivir solo de forma independiente. Olvida su propia dirección, el teléfono de años o el nombre de su escuela. Desorientado frecuentemente en tiempo (fecha, estación). Preserva el conocimiento de su nombre, de su cónyuge e hijos. Sabe alimentarse solo e ir al baño autónomamente pero requiere ayuda para vestirse adecuadamente.",
                                            "Fase de Demencia (Moderada - Pérdida de Autonomía)"
                                        ),
                                        6 to Triple(
                                            "Deterioro cognitivo grave (Demencia Grave)",
                                            "Pérdida casi por completo de la memoria reciente y de la historia vital de soporte. Olvida frecuentemente el nombre de su esposo/a. Incontinencia de esfínteres (urinaria e intestinal) recurrente. Necesita ayuda extrema para vestirse, bañarse, asearse e ir al baño de forma regular. Alucinaciones, arrebatos de agitación o apatía extrema.",
                                            "Fase de Demencia (Grave - Dependencia Extrema)"
                                        ),
                                        7 to Triple(
                                            "Deterioro cognitivo muy grave (Demencia Muy Grave)",
                                            "Fase terminal del Alzheimer. Pérdida total de capacidades de lenguaje hablado (solo puede emitir gruñidos o palabras residuales incoherentes). Inhabilidad para caminar de forma autónoma, pérdida progresiva para sostenerse sentado, sonreír o sostener la cabeza. Rigidez muscular severa y disfagia neurológica.",
                                            "Fase de Demencia (Terminal / Muy Grave)"
                                        )
                                    )

                                    val (gTitle, gDesc, gPhase) = stagesList[calcGdsStage] ?: Triple("", "", "")
                                    val isDementiaMode = calcGdsStage >= 4

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isDementiaMode) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isDementiaMode) Color(0xFFD97706) else MaterialTheme.colorScheme.primary)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "STAGE $calcGdsStage",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = gPhase,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDementiaMode) Color(0xFFB45309) else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = gTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = gDesc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Black.copy(alpha = 0.8f)
                                            )

                                            if (calcGdsStage >= 5) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "⚠️ ADVERTENCIA: El paciente requiere asistencia formal de cuidador permanente para la supervivencia del día a día.",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF991B1B)
                                                )
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
                    "select" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "SeLECT Score (Riesgo Epilepsia post-ACV)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Estima el riesgo de epilepsia tardía (crisis > 7 días) tras un ACV isquémico agudo con base en criterios clínicos y tomográficos.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_nihss",
                                label = "S: Gravedad del ACV (NIHSS al ingreso)",
                                description = "Severity of stroke on admission",
                                options = listOf(
                                    ScaleOption("NIHSS <= 4 (0 pt)", 0),
                                    ScaleOption("NIHSS 5-11 (1 pt)", 1),
                                    ScaleOption("NIHSS >= 12 (2 pt)", 2)
                                ),
                                selectedValue = selectS,
                                onOptionSelected = { selectS = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_laa",
                                label = "e: Etiología (Aterosclerosis de gran vaso - LAA)",
                                description = "Etiology (Large-Artery Atherosclerosis)",
                                options = listOf(
                                    ScaleOption("No (0 pt)", 0),
                                    ScaleOption("Sí (1 pt)", 1)
                                ),
                                selectedValue = selectEtiology,
                                onOptionSelected = { selectEtiology = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_early_seizure",
                                label = "L: Crisis aguda temprana (<= 7 días tras el ACV)",
                                description = "eLementary (Early seizure within 7 days)",
                                options = listOf(
                                    ScaleOption("No (0 pt)", 0),
                                    ScaleOption("Sí (3 pt)", 3)
                                ),
                                selectedValue = selectL,
                                onOptionSelected = { selectL = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_cortical",
                                label = "E: Extensión del compromiso cortical",
                                description = "Extent of cortical involvement",
                                options = listOf(
                                    ScaleOption("No (0 pt)", 0),
                                    ScaleOption("Sí (2 pt)", 2)
                                ),
                                selectedValue = selectE2,
                                onOptionSelected = { selectE2 = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_mca",
                                label = "C: Compromiso de territorio cortical de la ACM",
                                description = "Cortical Middle Cerebral Artery territory involvement",
                                options = listOf(
                                    ScaleOption("No (0 pt)", 0),
                                    ScaleOption("Sí (1 pt)", 1)
                                ),
                                selectedValue = selectC,
                                onOptionSelected = { selectC = it }
                            )
                        }
                    }
                    "select_asys" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "SeLECT-ASyS Score (Predecir crisis tardías)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Optimizado para pacientes con crisis sintomática aguda inicial (<= 7 días) tras un ACV. Desplace el control deslizante para ajustar el intervalo temporal de predicción.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_asys_sex",
                                label = "Sexo biológico",
                                description = "Biological sex of the patient",
                                options = listOf(
                                    ScaleOption("Femenino (0 pt)", 0),
                                    ScaleOption("Masculino (1 pt)", 1)
                                ),
                                selectedValue = selectAsysSex,
                                onOptionSelected = { selectAsysSex = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_asys_cortical",
                                label = "Compromiso cortical",
                                description = "Cortical involvement",
                                options = listOf(
                                    ScaleOption("No (0 pt)", 0),
                                    ScaleOption("Sí (1 pt)", 1)
                                ),
                                selectedValue = selectAsysCortical,
                                onOptionSelected = { selectAsysCortical = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_asys_athero",
                                label = "Aterosclerosis de gran vaso",
                                description = "Large-artery atherosclerosis (LAA)",
                                options = listOf(
                                    ScaleOption("No (0 pt)", 0),
                                    ScaleOption("Sí (1 pt)", 1)
                                ),
                                selectedValue = selectAsysAthero,
                                onOptionSelected = { selectAsysAthero = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_asys_type",
                                label = "Tipo de crisis sintomática aguda inicial",
                                description = "Type of acute symptomatic seizure",
                                options = listOf(
                                    ScaleOption("Focal, otra o desconocida (0 pt)", 0),
                                    ScaleOption("Bilateral Tónico-Clónica (1 pt)", 1),
                                    ScaleOption("Estatus Epiléptico (2 pt)", 2)
                                ),
                                selectedValue = selectAsysType,
                                onOptionSelected = { selectAsysType = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "select_asys_time",
                                label = "Momento de aparición de la crisis",
                                description = "Timing of acute symptomatic seizure",
                                options = listOf(
                                    ScaleOption("Día 0 (0 pt)", 0),
                                    ScaleOption("Día >=1 o Desconocido (1 pt)", 1)
                                ),
                                selectedValue = selectAsysTime,
                                onOptionSelected = { selectAsysTime = it }
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Horizonte de la Predicción: $selectAsysMonths Meses",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Slider(
                                        value = selectAsysMonths.toFloat(),
                                        onValueChange = { selectAsysMonths = it.toInt() },
                                        valueRange = 1f..60f,
                                        steps = 59,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("1 mes", style = MaterialTheme.typography.labelSmall)
                                        Text("12 m (1 año)", style = MaterialTheme.typography.labelSmall)
                                        Text("36 m (3 años)", style = MaterialTheme.typography.labelSmall)
                                        Text("60 m (5 años)", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                    "cave" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "CAVE Score (Hic)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Predice el riesgo de crisis convulsivas tardías a los 2 años de una hemorragia intracerebral (HIC) lobar o profunda.",
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
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("C: Compromiso Cortical", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Cortical involvement", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = caveCortical, onCheckedChange = { caveCortical = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("A: Edad < 65 años", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Age under 65 years", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = caveAgeUnder65, onCheckedChange = { caveAgeUnder65 = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("V: Volumen del hematoma < 10 mL", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Volume of hematoma < 10 mL", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = caveVolumeUnder10, onCheckedChange = { caveVolumeUnder10 = it })
                                        }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("E: Crisis clínica temprana (<= 7 días)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Early seizures", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = caveEarlySeizure, onCheckedChange = { caveEarlySeizure = it })
                                    }
                                }
                            }
                        }
                    }
                    "cave2" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "CAVE2 Score (Hic generalizada)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Estratificación de riesgo ampliada incorporando la gravedad neurológica del paciente para mejorar la solidez del pronóstico.",
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
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Compromiso Cortical", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Cortical involvement", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = cave2Cortical, onCheckedChange = { cave2Cortical = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Edad < 65 años", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Age under 65", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = cave2AgeUnder65, onCheckedChange = { cave2AgeUnder65 = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Volumen del hematoma < 10 mL", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Volume of hematoma < 10 mL", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = cave2VolumeUnder10, onCheckedChange = { cave2VolumeUnder10 = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Crisis clínica temprana (<= 7 días)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Early seizures", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = cave2EarlySeizure, onCheckedChange = { cave2EarlySeizure = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Gravedad neurológica severa", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("NIHSS >= 12 o GCS < 9 al ingreso", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = cave2Severity, onCheckedChange = { cave2Severity = it })
                                    }
                                }
                            }
                        }
                    }
                    "lane" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "LANE Score (Hic tardía)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Identifica de manera oportuna el riesgo de convulsiones tardías a los 2 años tras HIC mediante cribado bedside rápido.",
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
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("L: Localización Lobar", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Lobar intracerebral hemorrhage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = laneLobar, onCheckedChange = { laneLobar = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("A: Edad < 60 años", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Age under 60 years", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = laneAgeUnder60, onCheckedChange = { laneAgeUnder60 = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("N: Gravedad clínica inicial (NIHSS >= 10)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Admission NIHSS score >= 10", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = laneNihss10Plus, onCheckedChange = { laneNihss10Plus = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("E: Crisis clínica temprana (<= 7 días tras HIC)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Early seizure onset", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = laneEarlySeizure, onCheckedChange = { laneEarlySeizure = it })
                                    }
                                }
                            }
                        }
                    }
                    "rise" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "RISE Score (Epilepsia post-Hsa)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Delinea el riesgo individualizado de desarrollo de epilepsia tardía tras una hemorragia subaracnoidea aneurismática (HSA) aguda.",
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
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("R: Resangrado agudo (< 24 horas)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Acute aneurysm rebleeding", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = riseRebleed, onCheckedChange = { riseRebleed = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("I: Presencia de Hemorragia Intracerebral", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Presence of intracerebral hematoma", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = riseIch, onCheckedChange = { riseIch = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("S: Crisis convulsiva al inicio", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Onset symptomatic seizure", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = riseSeizure, onCheckedChange = { riseSeizure = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("E: Tratamiento endovascular (Coiling)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Endovascular coiling procedure", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = riseCoiling, onCheckedChange = { riseCoiling = it })
                                    }
                                }
                            }
                        }
                    }
                    "dias3" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "DIAS3 Score (Epilepsia post-Tvc)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Herramienta específica para predecir el desarrollo de crisis tardías tras trombosis venosa cerebral (TVC).",
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
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Foco Hemorrágico Basal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Baseline hemorrhagic infarct", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = dias3Hemorrhage, onCheckedChange = { dias3Hemorrhage = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Crisis Convulsiva Inicial / Debut", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Seizure at presentation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = dias3Seizure, onCheckedChange = { dias3Seizure = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Trombosis de Seno Sagital Superior", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Superior sagittal sinus thrombosis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = dias3Sinus, onCheckedChange = { dias3Sinus = it })
                                    }
                                }
                            }
                        }
                    }
                    "thwaites" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Índice de Triage de DIAGNÓSTICO Thwaites",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Índice predictivo para discriminar meningitis tuberculosa (MTB) de meningitis bacteriana pyógena común, guiando la terapia antimicrobiana urgente.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "thwaites_age",
                                label = "Edad del paciente",
                                description = "Patient age",
                                options = listOf(
                                    ScaleOption("Menor de 36 años (0 pt)", 0),
                                    ScaleOption("36 años o mayor (+2 pt)", 2)
                                ),
                                selectedValue = thwaitesAge,
                                onOptionSelected = { thwaitesAge = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "thwaites_wbc",
                                label = "Leucocitos séricos totales",
                                description = "Blood white cell count (WBC)",
                                options = listOf(
                                    ScaleOption("< 10.0 x 10^9/L (0 pt)", 0),
                                    ScaleOption(">= 10.0 x 10^9/L (+4 pt)", 4)
                                ),
                                selectedValue = thwaitesWbc,
                                onOptionSelected = { thwaitesWbc = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "thwaites_history",
                                label = "Duración de los síntomas",
                                description = "Duration of illness symptoms",
                                options = listOf(
                                    ScaleOption("Menos de 6 días (0 pt)", 0),
                                    ScaleOption("6 días o más (-5 pt)", -5)
                                ),
                                selectedValue = thwaitesHistory,
                                onOptionSelected = { thwaitesHistory = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "thwaites_csfwbc",
                                label = "Leucocitos totales en LCR",
                                description = "CSF total white cell count",
                                options = listOf(
                                    ScaleOption("< 1000 x 10^6/L (0 pt)", 0),
                                    ScaleOption(">= 1000 x 10^6/L (+3 pt)", 3)
                                ),
                                selectedValue = thwaitesCsfWbc,
                                onOptionSelected = { thwaitesCsfWbc = it }
                            )
                        }
                        item {
                            ScaleDomainSelectionBlock(
                                id = "thwaites_neutro",
                                label = "Porcentaje de neutrófilos en LCR",
                                description = "CSF percentage of neutrophils",
                                options = listOf(
                                    ScaleOption("< 75% (0 pt)", 0),
                                    ScaleOption(">= 75% (+3 pt)", 3)
                                ),
                                selectedValue = thwaitesCsfNeutro,
                                onOptionSelected = { thwaitesCsfNeutro = it }
                            )
                        }
                    }
                }
            }
        }
    }

        // Bottom Fixed Sticky Result Bar with soft backdrop gradient
        if (activeCalculatorId != null) {
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
                    val standardTotal = ClinicalDatabase.nihss.domains.sumOf { selectedNihssAnswers[it.id] ?: 0 }
                    val postAtaxia = if (selectedNihssAnswers["post_ataxia"] == 1) 3 else 0
                    val postDysphagia = if (selectedNihssAnswers["post_dysphagia"] == 1) 4 else 0
                    val postCough = if (selectedNihssAnswers["post_cough"] == 1) 5 else 0
                    val extraPoints = postAtaxia + postDysphagia + postCough
                    val currentTotal = if (usePostNihss) standardTotal + extraPoints else standardTotal

                    val scoreCategory = when {
                        standardTotal == 0 -> "Normal"
                        standardTotal in 1..4 -> "Ictus Leve"
                        standardTotal in 5..15 -> "Ictus Moderado"
                        standardTotal in 16..20 -> "Ictus Moderadamente Grave"
                        else -> "Ictus Grave"
                    }
                    val missingItems = (ClinicalDatabase.nihss.domains.size - selectedNihssAnswers.filter { !it.key.startsWith("post_") }.size) +
                        (if (usePostNihss) 3 - selectedNihssAnswers.filter { it.key.startsWith("post_") }.size else 0)

                    StickyResultBar(
                        scoreText = if (usePostNihss) "POST-NIHSS: $currentTotal pts (Base: $standardTotal)" else "NIHSS: $currentTotal pts",
                        interpretationText = if (usePostNihss) {
                            "Base: $scoreCategory. Posterior Ext: +$extraPoints pts."
                        } else {
                            "Clasificación clínica: $scoreCategory"
                        },
                        onResetClicked = onResetNihss,
                        missingItemsCount = maxOf(0, missingItems),
                        onCopyClicked = {
                            val breakdown = ClinicalDatabase.nihss.domains.map { domain ->
                                val valNum = selectedNihssAnswers[domain.id] ?: 0
                                "${domain.id}. ${domain.label.split(".").getOrNull(1)?.trim() ?: domain.label}: $valNum"
                            }.toMutableList()
                            
                            if (usePostNihss) {
                                breakdown.add("POST-NIHSS Ataxia de la Marcha/Tronco: ${if (selectedNihssAnswers["post_ataxia"] == 1) "Presente (3 pts)" else "Ausente (0 pts)"}")
                                breakdown.add("POST-NIHSS Disfagia: ${if (selectedNihssAnswers["post_dysphagia"] == 1) "Presente (4 pts)" else "Ausente (0 pts)"}")
                                breakdown.add("POST-NIHSS Tos Anormal: ${if (selectedNihssAnswers["post_cough"] == 1) "Presente (5 pts)" else "Ausente (0 pts)"}")
                            }

                            onCopyClicked(
                                if (usePostNihss) "POST-NIHSS (NIHSS+ Posterior)" else ClinicalDatabase.nihss.name,
                                if (usePostNihss) "$currentTotal (NIHSS Base: $standardTotal)" else "$currentTotal ($scoreCategory)",
                                breakdown,
                                if (usePostNihss) {
                                    "Compendio fosa posterior: Ataxia y bulbares agregados al NIHSS agudo."
                                } else {
                                    "Evaluación neurológica cuantitativa en fase de ACV agudo."
                                }
                            )
                        }
                    )
                }
                "aspects" -> {
                    val score = if (aspectsIsPosterior) {
                        val pcDeducted = selectedPcAspectsRegions.sumOf { reg -> if (reg == "M" || reg == "P") 2 else 1 }
                        maxOf(0, 10 - pcDeducted)
                    } else {
                        10 - selectedAspectsRegions.size
                    }
                    
                    val interpretation = if (aspectsIsPosterior) {
                        when {
                            score >= 8 -> "Favorable: Mayor probabilidad de independencia funcional, menor mortalidad"
                            score in 6..7 -> "Moderado: Foco isquémico posterior, evaluar endovascular"
                            else -> "Pobre: Alta extensión de isquemia de fosa posterior, alta morbimortalidad"
                        }
                    } else {
                        when {
                            score >= 8 -> "Favorable: Buen pronóstico de revascularización y menor riesgo hemorrágico"
                            score in 6..7 -> "Moderado: Evaluar riesgo/beneficio para trombectomía mecánica"
                            else -> "Pobre: Alta extensión isquémica, alto riesgo de transformación hemorrágica"
                        }
                    }
                    
                    val scoreTextLabel = if (aspectsIsPosterior) "pc-ASPECTS" else "ASPECTS"
                    
                    StickyResultBar(
                        scoreText = "$scoreTextLabel: $score / 10",
                        interpretationText = interpretation,
                        onResetClicked = onResetAspects,
                        missingItemsCount = 0,
                        onCopyClicked = {
                            val affectedStr = if (aspectsIsPosterior) {
                                if (selectedPcAspectsRegions.isEmpty()) "Ninguna" else selectedPcAspectsRegions.joinToString(", ")
                            } else {
                                if (selectedAspectsRegions.isEmpty()) "Ninguna" else selectedAspectsRegions.joinToString(", ")
                            }
                            val breakdown = listOf("Áreas Isquémicas afectadas: $affectedStr")
                            onCopyClicked(
                                "$scoreTextLabel Score",
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
                "gds_reisberg" -> {
                    val isDementiaMode = calcGdsStage >= 4
                    val statusText = if (isDementiaMode) "Estadio GDS $calcGdsStage (Demencia progresiva)" else "Estadio GDS $calcGdsStage (Fase Pre-demencia)"
                    val statusInterp = when (calcGdsStage) {
                        1 -> "Sano. Sin deterioro cognitivo."
                        2 -> "Olvidos seniles normales. Deterioro muy leve."
                        3 -> "Deterioro Cognitivo Leve (DCL)."
                        4 -> "Demencia Leve. Deterioro moderado."
                        5 -> "Demencia Moderada. REQUIERE ASISTENCIA permanente."
                        6 -> "Demencia Grave. Pérdida de autonomía extrema."
                        else -> "Demencia Muy Grave. Fase Terminal."
                    }
                    val gdsDescriptions = mapOf(
                        1 to "Normalidad clínica. No hay quejas subjetivas de memoria durante la entrevista.",
                        2 to "Olvidos cotidianos subjetivos. Sin compromiso social ni laboral.",
                        3 to "Dificultad de concentración/denominación; desorientación espacial en sitios nuevos.",
                        4 to "Dificultad de manejo financiero; pérdida de hechos recientes. Ayuda instrumental.",
                        5 to "Requiere asistencia constante. No puede elegir vestuario. Olvidos personales cardinales.",
                        6 to "Olvida nombre del cónyuge. Incontinencia. Requiere asistencia en vestuario y baño.",
                        7 to "Pérdida de capacidad verbal. Inmovilidad física. Rigidez y disfagia."
                    )

                    StickyResultBar(
                        scoreText = statusText,
                        interpretationText = statusInterp,
                        onResetClicked = { calcGdsStage = 1 },
                        missingItemsCount = 0,
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Estadio GDS actual: Estadio $calcGdsStage - $statusInterp",
                                "Hallazgos clínicos típicos: " + (gdsDescriptions[calcGdsStage] ?: ""),
                                "Soporte de Autonomía de Vida: " + (if (calcGdsStage >= 5) "REQUERIMIENTO OBLIGATORIO DE CUIDADOR PERMANENTE" else "Preserva supervivencia autónoma básica")
                            )
                            onCopyClicked(
                                "Estadificación GDS de Reisberg (Alzheimer)",
                                "Estadio GDS Clínico: GDS $calcGdsStage",
                                breakdown,
                                "Clasificación cognitiva-funcional:\n$statusInterp"
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
                "select" -> {
                    val total = selectS + selectEtiology + selectL + selectE2 + selectC
                    val risk1y = when (total) {
                        0 -> 0.7; 1 -> 1.8; 2 -> 4.0; 3 -> 8.0; 4 -> 15.1; 5 -> 25.4; 6 -> 38.3; 7 -> 52.3; 8 -> 65.5; else -> 76.4
                    }
                    val risk5y = when (total) {
                        0 -> 1.9; 1 -> 5.3; 2 -> 11.5; 3 -> 22.0; 4 -> 37.6; 5 -> 54.8; 6 -> 70.8; 7 -> 82.5; 8 -> 90.5; else -> 95.3
                    }

                    StickyResultBar(
                        scoreText = "Puntaje SeLECT: $total / 9 puntos",
                        interpretationText = "Riesgo de epilepsia a 1 año: $risk1y% | Riesgo a 5 años: $risk5y%",
                        onResetClicked = {
                            selectS = 0
                            selectEtiology = 0
                            selectL = 0
                            selectE2 = 0
                            selectC = 0
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "NIHSS: $selectS pt",
                                "LAA: ${if (selectEtiology == 1) "Sí(+1)" else "No(0)"}",
                                "Early Seizure: ${if (selectL == 3) "Sí(+3)" else "No(0)"}",
                                "Cortical: ${if (selectE2 == 2) "Sí(+2)" else "No(0)"}",
                                "ACM: ${if (selectC == 1) "Sí(+1)" else "No(0)"}"
                            )
                            onCopyClicked(
                                "SeLECT Score",
                                "$total / 9 puntos",
                                breakdown,
                                "Predicción de riesgo de epilepsia tardía:\n- A 1 año: $risk1y%\n- A 5 años: $risk5y%"
                            )
                        }
                    )
                }
                "select_asys" -> {
                    val total = selectAsysSex + selectAsysCortical + selectAsysAthero + selectAsysType + selectAsysTime

                    fun interpRisk(score: Int, m: Int): Double {
                        val r12 = when (score) {
                            0 -> 3.5; 1 -> 6.8; 2 -> 11.4; 3 -> 19.3; 4 -> 29.8; 5 -> 43.1; else -> 58.5
                        }
                        val r24 = when (score) {
                            0 -> 6.5; 1 -> 9.5; 2 -> 14.8; 3 -> 24.5; 4 -> 36.5; 5 -> 50.8; else -> 66.2
                        }
                        val r60 = when (score) {
                            0 -> 11.2; 1 -> 16.0; 2 -> 24.3; 3 -> 35.5; 4 -> 49.2; 5 -> 63.8; else -> 77.5
                        }
                        return if (m <= 12) {
                            (m / 12.0) * r12
                        } else if (m <= 24) {
                            r12 + ((m - 12) / 12.0) * (r24 - r12)
                        } else {
                            r24 + ((m - 24) / 36.0) * (r60 - r24)
                        }
                    }

                    val computedRisk = interpRisk(total, selectAsysMonths)
                    val formattedRisk = String.format("%.1f", computedRisk)

                    StickyResultBar(
                        scoreText = "Puntaje SeLECT-ASyS: $total / 6 puntos",
                        interpretationText = "Riesgo de epilepsia tardía estimado a los $selectAsysMonths meses: $formattedRisk%",
                        onResetClicked = {
                            selectAsysSex = 0
                            selectAsysCortical = 0
                            selectAsysAthero = 0
                            selectAsysType = 0
                            selectAsysTime = 0
                            selectAsysMonths = 12
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Sexo Masc: ${if (selectAsysSex == 1) "Sí(+1)" else "No(0)"}",
                                "Cortical: ${if (selectAsysCortical == 1) "Sí(+1)" else "No(0)"}",
                                "LAA: ${if (selectAsysAthero == 1) "Sí(+1)" else "No(0)"}",
                                "Crisis: $selectAsysType pt",
                                "Día 1+: ${if (selectAsysTime == 1) "Sí(+1)" else "No(0)"}"
                            )
                            onCopyClicked(
                                "SeLECT-ASyS Score",
                                "$total / 6 puntos",
                                breakdown,
                                "Riesgo de epilepsia tardía estimado a los $selectAsysMonths meses: $formattedRisk%"
                            )
                        }
                    )
                }
                "cave" -> {
                    val score = (if (caveCortical) 1 else 0) +
                                (if (caveAgeUnder65) 1 else 0) +
                                (if (caveVolumeUnder10) 1 else 0) +
                                (if (caveEarlySeizure) 1 else 0)
                    val risk = when (score) {
                        0 -> 0.6; 1 -> 3.6; 2 -> 9.8; 3 -> 16.2; else -> 31.0
                    }
                    StickyResultBar(
                        scoreText = "Puntaje CAVE: $score / 4 puntos",
                        interpretationText = "Riesgo de epilepsia post-HIC estimado a los 2 años: $risk%",
                        onResetClicked = {
                            caveCortical = false
                            caveAgeUnder65 = false
                            caveVolumeUnder10 = false
                            caveEarlySeizure = false
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Cortical: ${if (caveCortical) "Sí(+1)" else "No"}",
                                "Edad <65: ${if (caveAgeUnder65) "Sí(+1)" else "No"}",
                                "Volumen <10mL: ${if (caveVolumeUnder10) "Sí(+1)" else "No"}",
                                "Crisis: ${if (caveEarlySeizure) "Sí(+1)" else "No"}"
                            )
                            onCopyClicked(
                                "CAVE Score",
                                "$score / 4 puntos",
                                breakdown,
                                "Riesgo de epilepsia post-HIC a los 2 años: $risk%"
                            )
                        }
                    )
                }
                "cave2" -> {
                    val score = (if (cave2Cortical) 1 else 0) +
                                (if (cave2AgeUnder65) 1 else 0) +
                                (if (cave2VolumeUnder10) 1 else 0) +
                                (if (cave2EarlySeizure) 1 else 0) +
                                (if (cave2Severity) 1 else 0)
                    val risk = when (score) {
                        0 -> 0.5; 1 -> 2.0; 2 -> 7.0; 3 -> 15.0; 4 -> 28.0; else -> 45.0
                    }
                    StickyResultBar(
                        scoreText = "Puntaje CAVE2: $score / 5 puntos",
                        interpretationText = "Riesgo de epilepsia post-HIC (CAVE2) a los 2 años: $risk%",
                        onResetClicked = {
                            cave2Cortical = false
                            cave2AgeUnder65 = false
                            cave2VolumeUnder10 = false
                            cave2EarlySeizure = false
                            cave2Severity = false
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Cortical: ${if (cave2Cortical) "Sí(+1)" else "No"}",
                                "Edad <65: ${if (cave2AgeUnder65) "Sí(+1)" else "No"}",
                                "Volumen <10mL: ${if (cave2VolumeUnder10) "Sí(+1)" else "No"}",
                                "Crisis: ${if (cave2EarlySeizure) "Sí(+1)" else "No"}",
                                "Neurológica: ${if (cave2Severity) "Sí(+1)" else "No"}"
                            )
                            onCopyClicked(
                                "CAVE2 Score",
                                "$score / 5 puntos",
                                breakdown,
                                "Riesgo de epilepsia post-HIC (CAVE2) a los 2 años: $risk%"
                            )
                        }
                    )
                }
                "lane" -> {
                    val score = (if (laneLobar) 1 else 0) +
                                (if (laneAgeUnder60) 1 else 0) +
                                (if (laneNihss10Plus) 1 else 0) +
                                (if (laneEarlySeizure) 1 else 0)
                    val risk = when (score) {
                        0 -> 1.5; 1 -> 4.0; 2 -> 12.0; 3 -> 25.0; else -> 45.0
                    }
                    StickyResultBar(
                        scoreText = "Puntaje LANE: $score / 4 puntos",
                        interpretationText = "Riesgo de epilepsia post-HIC estimado: $risk%",
                        onResetClicked = {
                            laneLobar = false
                            laneAgeUnder60 = false
                            laneNihss10Plus = false
                            laneEarlySeizure = false
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Lobar: ${if (laneLobar) "Sí(+1)" else "No"}",
                                "Edad <60: ${if (laneAgeUnder60) "Sí(+1)" else "No"}",
                                "NIHSS >=10: ${if (laneNihss10Plus) "Sí(+1)" else "No"}",
                                "Crisis: ${if (laneEarlySeizure) "Sí(+1)" else "No"}"
                            )
                            onCopyClicked(
                                "LANE Score",
                                "$score / 4 puntos",
                                breakdown,
                                "Riesgo de epilepsia post-HIC estimado: $risk%"
                            )
                        }
                    )
                }
                "rise" -> {
                    val score = (if (riseRebleed) 1 else 0) +
                                (if (riseIch) 1 else 0) +
                                (if (riseSeizure) 1 else 0) +
                                (if (riseCoiling) 1 else 0)
                    val risk = when (score) {
                        0 -> 2.0; 1 -> 6.0; 2 -> 13.0; 3 -> 25.0; else -> 42.0
                    }
                    StickyResultBar(
                        scoreText = "Puntaje RISE: $score / 4 puntos",
                        interpretationText = "Riesgo de epilepsia post-HSA estimado: $risk%",
                        onResetClicked = {
                            riseRebleed = false
                            riseIch = false
                            riseSeizure = false
                            riseCoiling = false
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Rebleed: ${if (riseRebleed) "Sí(+1)" else "No"}",
                                "ICH: ${if (riseIch) "Sí(+1)" else "No"}",
                                "Seizure: ${if (riseSeizure) "Sí(+1)" else "No"}",
                                "Coiling: ${if (riseCoiling) "Sí(+1)" else "No"}"
                            )
                            onCopyClicked(
                                "RISE Score",
                                "$score / 4 puntos",
                                breakdown,
                                "Riesgo de epilepsia post-HSA estimado: $risk%"
                            )
                        }
                    )
                }
                "dias3" -> {
                    val score = (if (dias3Hemorrhage) 1 else 0) +
                                (if (dias3Seizure) 1 else 0) +
                                (if (dias3Sinus) 1 else 0)
                    val risk = when (score) {
                        0 -> 1.5; 1 -> 6.0; 2 -> 18.0; else -> 40.0
                    }
                    StickyResultBar(
                        scoreText = "Puntaje DIAS3: $score / 3 puntos",
                        interpretationText = "Riesgo de epilepsia post-TVC a 1 año: $risk%",
                        onResetClicked = {
                            dias3Hemorrhage = false
                            dias3Seizure = false
                            dias3Sinus = false
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Hemorragia: ${if (dias3Hemorrhage) "Sí(+1)" else "No"}",
                                "Crisis debut: ${if (dias3Seizure) "Sí(+1)" else "No"}",
                                "Seno sagital: ${if (dias3Sinus) "Sí(+1)" else "No"}"
                            )
                            onCopyClicked(
                                "DIAS3 Score",
                                "$score / 3 puntos",
                                breakdown,
                                "Riesgo de epilepsia post-TVC a 1 año: $risk%"
                            )
                        }
                    )
                }
                "thwaites" -> {
                    val total = thwaitesAge + thwaitesWbc + thwaitesHistory + thwaitesCsfWbc + thwaitesCsfNeutro
                    val interpretation = if (total <= 4) {
                        "Compatible con MENINGITIS TUBERCULOSA (MTB) (Sensibilidad >95%)"
                    } else {
                        "Compatible con MENINGITIS BACTERIANA"
                    }

                    StickyResultBar(
                        scoreText = "Puntaje de Thwaites: $total puntos",
                        interpretationText = interpretation,
                        onResetClicked = {
                            thwaitesAge = 0
                            thwaitesWbc = 0
                            thwaitesHistory = 0
                            thwaitesCsfWbc = 0
                            thwaitesCsfNeutro = 0
                        },
                        onCopyClicked = {
                            val breakdown = listOf(
                                "Edad >=36: ${if (thwaitesAge == 2) "Sí" else "No"}",
                                "Leucos Sangre >=10: ${if (thwaitesWbc == 4) "Sí" else "No"}",
                                "Duración >=6d: ${if (thwaitesHistory == -5) "Sí" else "No"}",
                                "Leucos LCR >=1000: ${if (thwaitesCsfWbc == 3) "Sí" else "No"}",
                                "Neutrófilos LCR >=75%: ${if (thwaitesCsfNeutro == 3) "Sí" else "No"}"
                            )
                            onCopyClicked(
                                "Índice Triage Thwaites (Meningitis)",
                                "$total puntos",
                                breakdown,
                                interpretation
                            )
                        }
                    )
                }
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
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("sticky_result_bar"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scoreText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (missingItemsCount > 0) {
                    Text(
                        text = "Faltan $missingItemsCount ítems por responder",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = interpretationText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onResetClicked,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reset_calculator")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restablecer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Button(
                    onClick = onCopyClicked,
                    enabled = missingItemsCount == 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (missingItemsCount == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (missingItemsCount == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("copy_result_bar"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (missingItemsCount > 0) "Faltan" else "Copiar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun CollapsibleIntroCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("collapsible_intro_${title.filter { it.isLetter() }}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Ocultar detalles" else "Mostrar detalles",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
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

fun getDomainIcon(id: String, label: String): androidx.compose.ui.graphics.vector.ImageVector {
    val term = (id + " " + label).lowercase()
    return when {
        term.contains("edad") || term.contains("age") || term.contains("decades") || term.contains("año") -> Icons.Default.EventNote
        term.contains("mrs") || term.contains("rankin") || term.contains("motor") || term.contains("debilidad") || term.contains("marcha") || term.contains("fisioter") || term.contains("deambula") || term.contains("fuerza") || term.contains("flex") || term.contains("pierna") -> Icons.Default.DirectionsWalk
        term.contains("gluc") || term.contains("glic") -> Icons.Default.WaterDrop
        term.contains("time") || term.contains("onset") || term.contains("tiempo") || term.contains("hora") || term.contains("durac") || term.contains("dia") || term.contains("mes") -> Icons.Default.AccessTime
        term.contains("nihss") || term.contains("puntuacion") || term.contains("score") || term.contains("puntaje") -> Icons.Default.Leaderboard
        term.contains("conciencia") || term.contains("alerta") || term.contains("orient") || term.contains("mental") || term.contains("cogni") || term.contains("memoria") || term.contains("juicio") || term.contains("gcs") || term.contains("four") -> Icons.Default.Psychology
        term.contains("temp") || term.contains("fiebre") || term.contains("calor") -> Icons.Default.Thermostat
        term.contains("wbc") || term.contains("leuc") || term.contains("lcr") || term.contains("pmn") || term.contains("neutro") || term.contains("prote") || term.contains("celul") || term.contains("liquido") || term.contains("biotech") || term.contains("labora") -> Icons.Default.Biotech
        term.contains("preb") || term.contains("cardio") || term.contains("bp") || term.contains("pulso") || term.contains("coraz") -> Icons.Default.MonitorHeart
        term.contains("cefalea") || term.contains("dolor") || term.contains("vomito") || term.contains("nause") -> Icons.Default.Sick
        term.contains("infarto") || term.contains("hemor") || term.contains("tromb") || term.contains("ictus") || term.contains("isqu") -> Icons.Default.Warning
        term.contains("veji") || term.contains("intest") || term.contains("orina") || term.contains("digest") || term.contains("esfin") -> Icons.Default.Shower
        term.contains("vis") || term.contains("ojo") || term.contains("pupil") || term.contains("vista") || term.contains("diplop") || term.contains("mirad") -> Icons.Default.Visibility
        term.contains("habla") || term.contains("lengu") || term.contains("disar") || term.contains("afas") || term.contains("voz") -> Icons.Default.RecordVoiceOver
        term.contains("sensi") || term.contains("tact") || term.contains("dermo") || term.contains("entume") -> Icons.Default.Sensors
        term.contains("refle") || term.contains("babins") || term.contains("clono") -> Icons.Default.Bolt
        else -> Icons.Default.Assignment
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = getDomainIcon(id, label),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
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

data class CriterioVisualAttrs(
    val domainText: String,
    val domainBg: Color,
    val domainColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@Composable
fun TabCriterios(
    goldCoastChecklist: Map<Int, Boolean>,
    trombolisisChecklist: Map<Int, Boolean>,
    selectedToastType: Int?,
    onToastTypeSelected: (Int?) -> Unit,
    onNavigateToDrug: (String) -> Unit,
    onCopyClicked: (String, String, List<String>, String?) -> Unit,
    activeCriterioId: String?,
    onActiveCriterioIdChanged: (String?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var searchQueryCriterios by remember { mutableStateOf("") }
    var selectedElaTab by remember { mutableStateOf(0) }
    val mnsChecklist = remember { mutableStateMapOf<Int, Boolean>() }
    val mniChecklist = remember { mutableStateMapOf<Int, Boolean>() }
    val suspicionChecklist = remember { mutableStateMapOf<Int, Boolean>() }
    val examsChecklist = remember { mutableStateMapOf<Int, Boolean>() }

    // Alzheimer State Variables
    var selectedAlzheimerTab by remember { mutableStateOf(0) }
    val alzheimerChecklistMap = remember { mutableStateMapOf<Int, Boolean>() }
    var selectedGdsStage by remember { mutableStateOf(1) }

    val allCriteria = remember {
        listOf(
            Triple("trombolisis", "Trombólisis (ACV)", "Checklist de contraindicaciones absolutas y relativas para infusión de Alteplasa / Tenecteplasa."),
            Triple("gold_coast_als", "Gold Coast (ELA)", "Criterios simplificados de Gold Coast 2020 para el diagnóstico de Esclerosis Lateral Amiotrófica."),
            Triple("alzheimer_ea", "Alzheimer - TNM debido a EA", "Directrices clínicas, sospecha, estudios de extensión, diagnósticos diferenciales y estadificación de la progresión funcional mediante la escala de Deterioro Global (GDS)."),
            Triple("miopatias_eular", "Miopatías (EULAR/ACR)", "Clasificaciones de Miopatías Inflamatorias Idiopáticas EULAR/ACR 2017."),
            Triple("toast", "TOAST (ACV)", "Clasificación etiológica del subtipo de ACV isquémico agudo."),
            Triple("ilae_epilepsy", "Epilepsia (ILAE)", "Clasificación operacional oficial de crisis epilépticas y tipos de epilepsias.")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (activeCriterioId == null) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Criterios Clínicos & Diagnósticos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Valide criterios de exclusión, clasificaciones oficiales y checklists de soporte médico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search input inside the Directory
            OutlinedTextField(
                value = searchQueryCriterios,
                onValueChange = { searchQueryCriterios = it },
                placeholder = { Text("Buscar criterio (e.g. Trombólisis, TOAST, ELA)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (searchQueryCriterios.isNotEmpty()) {
                        IconButton(onClick = { searchQueryCriterios = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("search_criterios_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filteredCriteria = allCriteria.filter {
                it.second.contains(searchQueryCriterios, ignoreCase = true) ||
                it.third.contains(searchQueryCriterios, ignoreCase = true)
            }

            if (filteredCriteria.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sin resultados",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron criterios médicos con:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "\"$searchQueryCriterios\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredCriteria.size) { index ->
                        val (id, title, desc) = filteredCriteria[index]
                        val attrs = when (id) {
                            "trombolisis" -> CriterioVisualAttrs("ACV / ICTUS", Color(0xFFFDE8E8), Color(0xFF9B1C1C), Icons.Default.LocalHospital, Color(0xFFEF4444))
                            "gold_coast_als" -> CriterioVisualAttrs("MIO/ELA/DEM", Color(0xFFF3E8FF), Color(0xFF6B21A8), Icons.Default.Accessibility, Color(0xFF8B5CF6))
                            "alzheimer_ea" -> CriterioVisualAttrs("COGNICION/DEM", Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.Psychology, Color(0xFF0284C7))
                            "miopatias_eular" -> CriterioVisualAttrs("MIO/ELA/DEM", Color(0xFFE6FFFA), Color(0xFF0D9488), Icons.Default.FactCheck, Color(0xFF00897B))
                            "toast" -> CriterioVisualAttrs("ACV / ICTUS", Color(0xFFEBF5FF), Color(0xFF1E40AF), Icons.Default.ListAlt, Color(0xFF3B82F6))
                            else -> CriterioVisualAttrs("EPILEPSIA", Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.Bolt, Color(0xFFFFB300))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onActiveCriterioIdChanged(id) }
                                .testTag("criteria_card_$id"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(attrs.accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = attrs.icon,
                                        contentDescription = title,
                                        tint = attrs.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(attrs.domainBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = attrs.domainText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = attrs.domainColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Ver Criterio",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onActiveCriterioIdChanged(null) },
                    modifier = Modifier.testTag("criteria_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver al catálogo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Volver al catálogo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onActiveCriterioIdChanged(null) }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
                            statusDesc = "Morfología compatible pero requiere exclusión activa y descarte de diagnósticos diferenciales."
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

                        // Top Title Header Card
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
                                        text = "Directrices multidisciplinarias y recomendaciones para el diagnóstico, soporte clínico y seguimiento del paciente adulto con sospecha o confirmación de ELA.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Horizontal Tab Pill Row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val tabs = listOf(
                                    Triple(0, "Criterios Gold Coast", Icons.Default.FactCheck),
                                    Triple(1, "Sospecha y CUPS", Icons.Default.List),
                                    Triple(2, "Diferenciales y Educación", Icons.Default.Warning),
                                    Triple(3, "Fenotipos, Proyecciones y Costos", Icons.Default.Timeline)
                                )
                                tabs.forEach { (index, title, icon) ->
                                    val isSelected = selectedElaTab == index
                                    val containerBgColor = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    val contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                                    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.5f)

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(containerBgColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                            .clickable { selectedElaTab = index }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("ela_tab_$index")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = contentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                                ),
                                                color = contentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // render sections based on selected ELA Tab
                        if (selectedElaTab == 0) {
                            // TAB 0: core gold coast and checklists
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
                                                text = "DIAGNÓSTICO: $statusText",
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
                                        Spacer(modifier = Modifier.height(10.dp))
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
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Copia Inteligente")
                                        }
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
                                        .border(
                                            1.dp,
                                            if (isChecked) colorScheme.primary.copy(alpha = 0.5f) else colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
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

                            // Sub-checks for MNS and MNI diagnostic assistance
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Soporte Clínico: Identificación MNS y MNI",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Para calificar la región corporal afectada en el criterio clínico progresivo:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // MNS
                                        Text(
                                            text = "Disfunción de Motoneurona Superior (MNS) - Requiere ≥1:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.secondary
                                        )
                                        
                                        val mnsPoints = listOf(
                                            "Aumento de reflejos musculotendinosos (RMT), incluida la presencia de un reflejo en un músculo clínicamente débil y atrofiado, o diseminación muscular.",
                                            "Presencia de reflejos patológicos (signo de Hoffmann, signo de Babinski, aductores cruzados o reflejo del hocico).",
                                            "Aumento del tono dependiente de la velocidad (espasticidad).",
                                            "Movimiento voluntario lento y mal coordinado (no atribuible a debilidad de MNI ni a características parkinsonianas)."
                                        )
                                        mnsPoints.forEachIndexed { i, pt ->
                                            val chk = mnsChecklist[i] == true
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { mnsChecklist[i] = !chk }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = chk,
                                                    onCheckedChange = { mnsChecklist[i] = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.secondary)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = pt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // MNI
                                        Text(
                                            text = "Disfunción de Motoneurona Inferior (MNI):",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.secondary
                                        )
                                        
                                        val mniPoints = listOf(
                                            "Evidencias de examen clínico: debilidad muscular y atrofia muscular.",
                                            "EMG: Anomalías que incluyen cambio neurológico crónico (MUAPs gigantes de mayor duración, amplitud aumentada con polifasia e inestabilidad como apoyo).",
                                            "EMG: Evidencia de denervación en curso (potenciales de fibrilación, ondas agudas positivas, o potenciales de fasciculación)."
                                        )
                                        mniPoints.forEachIndexed { i, pt ->
                                            val chk = mniChecklist[i] == true
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { mniChecklist[i] = !chk }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = chk,
                                                    onCheckedChange = { mniChecklist[i] = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.secondary)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = pt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                            }
                                        }

                                        // Clinical analysis info
                                        Spacer(modifier = Modifier.height(14.dp))
                                        val countMns = mnsChecklist.values.count { it }
                                        val countMni = mniChecklist.values.count { it }
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (countMns >= 1 && countMni >= 1) Icons.Default.CheckCircle else Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = if (countMns >= 1 && countMni >= 1) colorScheme.primary else colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = when {
                                                        countMns >= 1 && countMni >= 2 -> "Apoyo Clínico: Hallazgos compatibles con afectación mixta superior (MNS) e inferior (MNI) en ≥1 región. Verifique compromiso de progresión."
                                                        countMns >= 1 && countMni >= 1 -> "Apoyo Clínico: Identificada afectación de MNS e MNI en al menos una región anatómica. Cumple requerimiento regional."
                                                        countMni >= 2 -> "Apoyo Clínico: Identificada afectación de MNI únicamente en ≥2 regiones anatómicas. Alternativa cumplida."
                                                        else -> "Apoyo Clínico: Evaluando aportes regionales (Signos MNS: $countMns/1, MNI: $countMni/1 en una región o MNI: $countMni/2 en regiones)."
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (selectedElaTab == 1) {
                            // TAB 1: suspicion and complementary studies (CUPS)
                            
                            // Clinical suspicion criteria
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Perfil de Sospecha de ELA (Criterios de Entrada)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Paciente adulto con síntomas de debilidad progresiva asociada a:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        val suspicionPoints = listOf(
                                            "Debilidad muscular progresiva y atrofia con impacto funcional, dada por disminución de destreza motora, caídas o tropiezos, calambres y pérdida de peso progresiva.",
                                            "Disfagia (deglución alterada) o disartria (habla dificultosa), típicos de la presentación bulbar de ELA.",
                                            "Problemas respiratorios (disnea ante esfuerzo regular, función reducida, somnolencia diurna excesiva, fatiga muscular intensa, cefalea temprano por la mañana u ortopnea).",
                                            "Signos de MNS (debilidad, espasticidad, hiperreflexia, clonus, reflejos patológicos) o signos de MNI (debilidad, atrofia, fasciculaciones, hipo/arreflexia) combinados no atribuibles a otras causas en los segmentos bulbar, cervical, torácico o lumbar."
                                        )
                                        
                                        suspicionPoints.forEachIndexed { i, pt ->
                                            val chk = suspicionChecklist[i] == true
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { suspicionChecklist[i] = !chk }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = chk,
                                                    onCheckedChange = { suspicionChecklist[i] = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = pt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            // Complementary exams with CUPS codes
                            item {
                                Text(
                                    text = "Estudios Complementarios para el Diagnóstico (HUN)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            
                            item {
                                Text(
                                    text = "El diagnóstico es esencialmente clínico. Los exámenes complementarios buscan obligatoriamente excluir otras causas. Marque para armar y copiar ordenamiento:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            val examsList = listOf(
                                Pair("Resonancia magnética simple de cerebro", "CUPS: 883101 - Excluye patologías cerebrales estructurales simuladoras."),
                                Pair("Resonancia magnética simple de columna cervical", "CUPS: 883210 - Excluye mielopatías compresivas."),
                                Pair("Resonancia magnética simple de columna dorsal", "CUPS: 883220 - Excluye comprensiones o mielopatías dorsales."),
                                Pair("Serología: VIH 1 y 2 anticuerpos", "CUPS: 906249 - Descarte de procesos retrovirales asociados."),
                                Pair("Serología: Prueba no treponémica (Sífilis)", "CUPS: 906915 - Exclusión rutinaria obligatoria."),
                                Pair("Serología: HTLV-I y II anticuerpos totales", "CUPS: 906232 - Descarte de paraparesia espástica tropical."),
                                Pair("Nivel sérico de Cianocobalamina (Vit. B12)", "CUPS: 903703 - Descarte de degeneración combinada subaguda."),
                                Pair("Inmunofijación semiautomatizada en suero", "CUPS: 906824 - Estudio de paraproteinemias asociadas."),
                                Pair("Inmunofijación semiautomatizada en orina", "CUPS: 906825 - Descarte de cadenas ligeras urinarias."),
                                Pair("Electromiografía en cada extremidad", "CUPS: 930860 - Valida extensión periférica y de denervación."),
                                Pair("Neuroconducciones por cada nervio de 4 extremidades", "CUPS: 891509 - Protocolo completo para enfermedad de neurona motora."),
                                Pair("Electromiografía de cara (lengua)", "CUPS: 930820 - Útil para evaluación clínica de segmento bulbar."),
                                Pair("Electromiografía de músculos paraespinales", "CUPS: 930806 - Útil para evaluación de segmento torácico paraespinal."),
                                Pair("Punción lumbar (LCR estudio citoquímico)", "CUPS: 033101 - Considerar según sospecha del clínico."),
                                Pair("Electromiografía laríngea con aguja guiada", "CUPS: 930810 / Ecografía: 881601 - Para sospecha bulbar selectiva (disfagia/disfonía).")
                            )
                            
                            items(examsList.size) { index ->
                                val (name, code) = examsList[index]
                                val isChecked = examsChecklist[index] == true
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { examsChecklist[index] = !isChecked }
                                        .background(
                                            if (isChecked) colorScheme.primaryContainer.copy(alpha = 0.25f)
                                            else colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isChecked) colorScheme.primary.copy(alpha = 0.5f) else colorScheme.outlineVariant.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { examsChecklist[index] = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(code, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            
                            // Send copies / actions
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val selectedExams = examsList.filterIndexed { idx, _ -> examsChecklist[idx] == true }
                                        val formatted = selectedExams.joinToString("\n") { "• ${it.first} (${it.second})" }
                                        onCopyClicked(
                                            "Plan de Exámenes - Sospecha ELA (HUN)",
                                            "ORDEN COMPLEMENTARIA DE DESCARTE DE ELA",
                                            selectedExams.map { "${it.first} -> ${it.second}" },
                                            "Lista de exámenes complementarios de exclusión de diagnósticos diferenciales."
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("copy_cups_orders")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy CUPS", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Copiar Exámenes de Exclusión (${examsChecklist.values.count { it }})")
                                }
                            }
                        } else if (selectedElaTab == 2) {
                            // TAB 2: differentials, confirmation and education
                            
                            // Condition Simulators Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = "Simuladores", tint = Color(0xFFE6A23C))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Diagnósticos Diferenciales Frecuentes",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Aproximadamente del 8% al 10% de los pacientes referidos con sospecha de ELA albergan otra condición médica. Considerar diagnósticos frecuentes como:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        val diffs = listOf(
                                            "Mielopatía compresiva o no compresiva a nivel cervical o dorsal.",
                                            "Lesión cerebral estructural (ej. patología vascular, neoplasia, malformación, focos de infección).",
                                            "Infección por virus HTLV 1 y 2 (Paraparesia Espástica Tropical).",
                                            "Paraparesia espástica hereditaria progresiva."
                                        )
                                        diffs.forEach { d ->
                                            Text(
                                                text = "• $d",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Professional Confirmation Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = "Soporte", tint = colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Confirmación Diagnóstica Experta",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Se recomienda enfáticamente que un médico especialista en neurología o en medicina física y rehabilitación (fisiatra) con experiencia certificada realice y confirme el diagnóstico de ELA (NE: Consenso de expertos AAN).",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Estrategias Empáticas: Se prescribe considerar detalladamente estrategias empáticas y oportunas para comunicar de forma compasiva e integral el diagnóstico de ELA a pacientes y familiares.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }

                            // Patient & Family Education Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Educación Dirigida al Paciente con ELA",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val edus = listOf(
                                            "Información sobre la Condición: Educar al paciente y su familia sobre la patogenia, el carácter progresivo e irreversible de la enfermedad.",
                                            "Expectativas de Tratamiento: Abordar tempranamente las expectativas sobre tratamientos sintomáticos y cuidados paliativos.",
                                            "Planificación Familiar: Asesorar oportunamente sobre el deseo de gestación en mujeres o deseos de paternidad en varones fértiles.",
                                            "Recursos Disponibles: Guiar sobre opciones terapéuticas, participación en estudios de investigación médica aprobados, y agrupaciones de apoyo."
                                        )
                                        edus.forEach { pt ->
                                            Text(
                                                text = "• $pt",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // TAB 3: Phenotypes, projections, costs
                            
                            // Forms of presentation
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Presentación Clínica y Variabilidad Fenotípica",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        val forms = listOf(
                                            Pair("Inicio Espinal (~70% casos)", "Se caracteriza por debilidad flácida o espástica de las extremidades y consecuentes problemas notables de la movilidad física primaria."),
                                            Pair("Inicio Bulbar (~25% casos)", "Se caracteriza por compromiso selectivo de músculos orofaríngeos, afectando severamente la deglución (disfagia), la articulación lingual y el habla (disartria precoz)."),
                                            Pair("Deterioro Cognitivo y No Motor", "La ELA es una enfermedad sistémica. Cursa con deterioro cognitivo general y alteraciones de funciones ejecutivas en el 50% de los casos, y cambios comportamentales por demencia frontotemporal en el 15% (formas familiares)."),
                                            Pair("Pronóstico y Mortalidad", "Independientemente del sitio clínico de inicio de síntomas, la mortalidad obedece a compromiso respiratorio progresivo. Hasta el 50% fallece dentro de los 3 años del inicio clínico.")
                                        )
                                        forms.forEach { (title, desc) ->
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colorScheme.secondary)
                                                Text(desc, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Posee alrededor de 10 fenotipos clínicos que abarcan desde la típica forma bulbar progresiva hasta el síndrome de piernas colgantes (flail arm/leg), de acuerdo con perfiles variables de afectación neuronal cortical/periférica.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }

                            // Epidemiological Projections Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Proyecciones de Incremento Clínico Mundial (2015 - 2040)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "En virtud del aumento progresivo en la esperanza de vida (principalmente en países industrializados), se prevé matemáticamente un incremento global del 69% en los casos totales de ELA, elevando la cifra mundial de 222,801 diagnósticos clínicos en 2015 a un acumulado proyectado de 376,674 para el 2040.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Costs Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Gestión de Costos en Salud Asociados al Cuidado",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val costs = listOf(
                                            "Rango de Costos Anuales: Los estudios estiman de 9,741 € (Grecia) a 114,605 € (Australia) de costo al año de asistencia por paciente (aprox. $41,438,332 a $487,531,064 pesos colombianos).",
                                            "Severidad Clínica: Los costos totales ascienden con la progresión de la severidad e incapacidad funcional progresiva del paciente.",
                                            "Costes Máximos: Los picos de demanda económica máxima se concentran en la etapa de diagnóstico diferencial clínico, y en los momentos cercanos al fallecimiento.",
                                            "Costos Directos vs Indirectos: Estudios clínicos demuestran que los costos directos de asistencia médica superan notablemente los costes indirectos de pérdida productiva laboral.",
                                            "Ventaja del Reduccionismo Interdisciplinar: Pacientes tratados de forma coordinada en grupos interdisciplinarios reducen en promedio unos 65 € mensuales ($276,000 COP) respecto al equipo de cuidado general, elevando drásticamente el estándar de calidad de vida."
                                        )
                                        costs.forEach { pt ->
                                            Text(
                                                text = "• $pt",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "alzheimer_ea" -> {
                        // Header Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().testTag("alzheimer_header_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Trastorno Neurocognitivo Mayor debido a Alzheimer",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Estándar clínico institucional del Hospital Universitario Nacional de Colombia (IN-EC-33, 2024). Pautas basadas en evidencia para tamización, diagnóstico y estadificación de GDS.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Horizontal Tab selection Row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val tabs = listOf(
                                    Triple(0, "Sospecha y VGI", Icons.Default.Search),
                                    Triple(1, "Estudios y Consenso", Icons.Default.FactCheck),
                                    Triple(2, "Diferenciales y Evitar", Icons.Default.Warning),
                                    Triple(3, "Estadificación GDS", Icons.Default.ShowChart)
                                )
                                tabs.forEach { (idx, title, icon) ->
                                    val isSelected = selectedAlzheimerTab == idx
                                    val containerBg = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    val contentClr = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                                    val borderClr = if (isSelected) colorScheme.primary else colorScheme.outlineVariant.copy(alpha = 0.5f)

                                     Box(
                                         modifier = Modifier
                                             .clip(RoundedCornerShape(20.dp))
                                             .background(containerBg)
                                             .border(1.dp, borderClr, RoundedCornerShape(20.dp))
                                             .clickable { selectedAlzheimerTab = idx }
                                             .padding(horizontal = 14.dp, vertical = 8.dp)
                                             .testTag("alzheimer_tab_$idx")
                                     ) {
                                         Row(verticalAlignment = Alignment.CenterVertically) {
                                             Icon(
                                                 imageVector = icon,
                                                 contentDescription = title,
                                                 tint = contentClr,
                                                 modifier = Modifier.size(16.dp)
                                             )
                                             Spacer(modifier = Modifier.width(6.dp))
                                             Text(
                                                 text = title,
                                                 style = MaterialTheme.typography.labelMedium.copy(
                                                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                                 ),
                                                 color = contentClr
                                             )
                                         }
                                     }
                                 }
                             }
                         }

                         when (selectedAlzheimerTab) {
                             0 -> {
                                 // Tab 0: Sospecha y Cribado Inicial (VGI)
                                 item {
                                     Card(
                                         colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Column(modifier = Modifier.padding(16.dp)) {
                                             Text(
                                                 text = "A. Sospecha Clínica de TNM debido a EA",
                                                 style = MaterialTheme.typography.titleMedium,
                                                 fontWeight = FontWeight.Bold,
                                                 color = colorScheme.primary
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "Debe sospecharse activamente TNM debido a enfermedad de Alzheimer en todo adulto que curse con cambios cognitivos (principales marcadores) y/o comportamentales:",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )
                                             Spacer(modifier = Modifier.height(10.dp))

                                             val symptoms = listOf(
                                                 "Pérdida de memoria episódica notable (repetición frecuente de preguntas o de historias, distorsiones del recuerdo)",
                                                 "Dificultades persistentes para encontrar palabras o sustituciones inadecuadas de palabras",
                                                 "Desorientación temporoespacial reciente progresiva",
                                                 "Dificultades visibles de planificación o ejecución (manejo de tareas complejas)",
                                                 "Cambios comportamentales y afectivos marcados (apatía, depresión, ansiedad, alucinaciones, irritabilidad, delirios)"
                                             )
                                             symptoms.forEachIndexed { idx, sym ->
                                                 val isChecked = alzheimerChecklistMap[idx] == true
                                                 Row(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .clickable { alzheimerChecklistMap[idx] = !isChecked }
                                                         .padding(vertical = 4.dp),
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Checkbox(
                                                         checked = isChecked,
                                                         onCheckedChange = { alzheimerChecklistMap[idx] = it },
                                                         colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                                                     )
                                                     Spacer(modifier = Modifier.width(8.dp))
                                                     Text(sym, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                 }
                                             }

                                             Spacer(modifier = Modifier.height(8.dp))
                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .background(colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                     .border(1.dp, colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                     .padding(10.dp)
                                             ) {
                                                 Text(
                                                     text = "💡 Recomendación de Expertos: Si bien los cambios comportamentales y emocionales no son los marcadores cardinales iniciales de la EA, se debe realizar obligatoriamente tamización sistemática de los mismos por su prevalencia en estadios avanzados.",
                                                     style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                                     color = colorScheme.onSecondaryContainer
                                                 )
                                             }
                                         }
                                     }
                                 }

                                 item {
                                     Card(
                                         colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Column(modifier = Modifier.padding(16.dp)) {
                                             Text(
                                                 text = "B. Valoración Médica Inicial e Interdisciplinaria",
                                                 style = MaterialTheme.typography.titleMedium,
                                                 fontWeight = FontWeight.Bold,
                                                 color = colorScheme.primary
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "Realice un abordaje integral estructurado mediante valoración geriátrica y remisiones indicadas:",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )
                                             Spacer(modifier = Modifier.height(10.dp))

                                             val vgis = listOf(
                                                 "Historia clínica completa y detallado examen físico general.",
                                                 "Valoración Geriátrica Integral (VGI) con especial énfasis en funcionalidad básica (Escala de Barthel) e instrumental (Escala de Lawton y Brody).",
                                                 "Pruebas sistemáticas de cribado cognitivo (Examen de Estado Minimental - MMSE o Evaluación Cognitiva de Montreal - MoCA).",
                                                 "Tamizaje de riesgo nutricional mediante Mini Nutritional Assessment (MNA). Nota: Debe responderse por familiares o cuidadores para evitar juicio alterado. Remita a soporte nutricional (ECBE) si es de riesgo.",
                                                 "Evaluación ocular rutinaria por Oftalmología / Optometría (Código CUPS: 890207).",
                                                 "Evaluación de salud oral obligatoria por Odontología (Código CUPS: 890403).",
                                                 "Examen neurológico y valoración funcional cognitiva completa por Neuropsicología.",
                                                 "Evaluación detallada de condiciones sociales de soporte por Trabajo Social."
                                             )
                                             vgis.forEachIndexed { i0, vgi ->
                                                 val idx = 10 + i0
                                                 val isChecked = alzheimerChecklistMap[idx] == true
                                                 Row(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .clickable { alzheimerChecklistMap[idx] = !isChecked }
                                                         .padding(vertical = 4.dp),
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Checkbox(
                                                         checked = isChecked,
                                                         onCheckedChange = { alzheimerChecklistMap[idx] = it },
                                                         colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                                                     )
                                                     Spacer(modifier = Modifier.width(8.dp))
                                                     Text(vgi, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                 }
                                             }

                                             // Program COGNITION alert box
                                             AnimatedVisibility(visible = alzheimerChecklistMap[12] == true) { // if MMSE/MoCA screen is checked
                                                 Box(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .padding(top = 10.dp)
                                                         .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                                                         .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                         .padding(10.dp)
                                                 ) {
                                                     Column {
                                                         Text(
                                                             text = "🔔 PROTOCOLO COGNITION SE ACTIVA",
                                                             style = MaterialTheme.typography.labelSmall,
                                                             fontWeight = FontWeight.Bold,
                                                             color = Color(0xFF0369A1)
                                                         )
                                                         Text(
                                                             text = "En caso de tamizaje cognitivo alterado (MMSE/MoCA positivo), remita prioritariamente a la enfermera o jefe del programa COGNITION. Busca fortalecer la respuesta en salud a pacientes con deterioro cognitivo leve o TNC mayor.",
                                                             style = MaterialTheme.typography.bodySmall,
                                                             color = Color(0xFF075985)
                                                         )
                                                     }
                                                 }
                                             }

                                             Spacer(modifier = Modifier.height(10.dp))
                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                     .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                     .padding(10.dp)
                                             ) {
                                                 Text(
                                                     text = "❤️ Lenguaje y Empatía: Use frases cortas, lenguaje sencillo y no verbal claro. Considere siempre el enfoque diferencial y explore las Voluntades Anticipadas del paciente desde la valoración inicial.",
                                                     style = MaterialTheme.typography.bodySmall,
                                                     color = colorScheme.onSurfaceVariant
                                                 )
                                             }
                                         }
                                     }
                                 }
                             }
                             1 -> {
                                 // Tab 1: Estudios de Extensión y Diagnóstico de Consenso
                                 item {
                                     Card(
                                         colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Column(modifier = Modifier.padding(16.dp)) {
                                             Text(
                                                 text = "C. Estudios de Extensión Sugeridos",
                                                 style = MaterialTheme.typography.titleMedium,
                                                 fontWeight = FontWeight.Bold,
                                                 color = colorScheme.primary
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "Estudios diagnósticos complementarios obligatorios para descarte razonable de causas reversibles o alternativas (Marque para ordenar):",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )
                                             Spacer(modifier = Modifier.height(10.dp))

                                             val extensionExams = listOf(
                                                 Pair("Hemograma completo", "CUPS: 902210 - Valoración rutinaria, anemia o procesos hematológicos."),
                                                 Pair("Función Renal (Creatinina o BUN)", "CUPS: 903825 / 903856 / 903605 - Exclusión de uremia y ajuste renal de fármacos."),
                                                 Pair("Hormona Estimulante de Tiroides (TSH)", "CUPS: 904902 - Exclusión obligatoria de hipotiroidismo sistémico."),
                                                 Pair("Nivel Sérico de Vitamina B12", "CUPS: 903703 - Descarte de demencia reversible por déficit vitamínico."),
                                                 Pair("Nivel Sérico de Ácido Fólico (Folatos)", "CUPS: 903105 - Soporte nutricional y metabólico."),
                                                 Pair("Serología para Sífilis (Prueba no treponémica)", "CUPS: 906915 - Descarte de neurosífilis clásica."),
                                                 Pair("Estudio de Imagen Cerebral (TC o Resonancia)", "CUPS: 879113 / 883101 - Exclusión de causas estructurales o hidrocefalia."),
                                                 Pair("Pruebas Neuropsicológicas detalladas", "CUPS: 940700 - Caracterización formal de dominios cognitivos afectados.")
                                             )

                                             extensionExams.forEachIndexed { i0, exam ->
                                                 val idx = 30 + i0
                                                 val isChecked = alzheimerChecklistMap[idx] == true
                                                 Row(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .clickable { alzheimerChecklistMap[idx] = !isChecked }
                                                         .background(
                                                             if (isChecked) colorScheme.primaryContainer.copy(alpha = 0.15f)
                                                             else colorScheme.surface,
                                                             shape = RoundedCornerShape(8.dp)
                                                         )
                                                         .border(
                                                             1.dp,
                                                             if (isChecked) colorScheme.primary.copy(alpha = 0.4f) else colorScheme.outlineVariant.copy(alpha = 0.15f),
                                                             RoundedCornerShape(8.dp)
                                                         )
                                                         .padding(8.dp),
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Checkbox(
                                                         checked = isChecked,
                                                         onCheckedChange = { alzheimerChecklistMap[idx] = it }
                                                     )
                                                     Spacer(modifier = Modifier.width(8.dp))
                                                     Column {
                                                         Text(exam.first, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                         Text(exam.second, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                                     }
                                                 }
                                                 Spacer(modifier = Modifier.height(4.dp))
                                             }

                                             Spacer(modifier = Modifier.height(10.dp))
                                             Button(
                                                 onClick = {
                                                     val markedList = extensionExams.filterIndexed { index, _ -> alzheimerChecklistMap[30 + index] == true }
                                                     onCopyClicked(
                                                         "Ordenes Médicas de Extensión - Alzheimer",
                                                         "EXTENSIÓN DE LABORATORIOS Y NEUROIMAGEN",
                                                         markedList.map { "${it.first} (${it.second})" },
                                                         "Exámenes complementarios indispensables en sospecha de TNM debido a EA."
                                                     )
                                                 },
                                                 modifier = Modifier.fillMaxWidth().testTag("copy_alzheimer_exams")
                                             ) {
                                                 Icon(Icons.Default.ContentCopy, contentDescription = "Copiar órdenes", modifier = Modifier.size(16.dp))
                                                 Spacer(modifier = Modifier.width(8.dp))
                                                 Text("Copiar Exámenes Marcados (${extensionExams.indices.count { alzheimerChecklistMap[30 + it] == true }})")
                                             }
                                         }
                                     }
                                 }

                                 item {
                                     Card(
                                         colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Column(modifier = Modifier.padding(16.dp)) {
                                             Text(
                                                 text = "D. Diagnóstico de Consenso y Grados",
                                                 style = MaterialTheme.typography.titleMedium,
                                                 fontWeight = FontWeight.Bold,
                                                 color = colorScheme.primary
                                             )
                                             Spacer(modifier = Modifier.height(6.dp))
                                             Text(
                                                 text = "• Consenso Multidisciplinario: El diagnóstico formal se realiza en consenso de expertos por Geriatría, Neuropsicología, Neurología y Psiquiatría para definir diagnósticos basándose en criterios estandarizados internacionales.",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurface
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "• Rol del Servicio de Enfermería: Responsable de aplicar la tamización mediante escala de Pfeiffer y dirigir los aspectos logísticos y administrativos de la mesa del consenso experto.",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "• Casos especiales: Ante parkinsonismo, manifestaciones neuropsiquiátricas de inicio temprano o refractariedad médica, solicitar valoración formal por neuropsicología para delimitar perfiles.",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )

                                             Spacer(modifier = Modifier.height(12.dp))
                                             Text(
                                                 text = "Criterios de Probabilidad en Vida (La confirmación definitiva es post-mortem histopatológica):",
                                                 style = MaterialTheme.typography.labelSmall,
                                                 fontWeight = FontWeight.Bold,
                                                 color = colorScheme.secondary
                                             )
                                             Spacer(modifier = Modifier.height(6.dp))

                                             val classes = listOf(
                                                 Pair("Diagnóstico PROBABLE", "Presencia de síntomas cognitivos y conductuales marcados sin otra causa sistémica u orgánica cerebral explicativa."),
                                                 Pair("Diagnóstico POSIBLE", "Presencia de síntomas, con afectación confirmada en uno o más dominios, déficit detectado mediante pruebas de cribado e imagen estructural que descarte otras enfermedades cerebrales.")
                                             )

                                             classes.forEach { classif ->
                                                 Column(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                         .padding(10.dp)
                                                 ) {
                                                     Text(classif.first, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                                                     Text(classif.second, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                                 }
                                                 Spacer(modifier = Modifier.height(6.dp))
                                             }
                                         }
                                     }
                                 }
                             }
                             2 -> {
                                 // Tab 2: Diagnósticos Diferenciales (Medicamentos Inapropiados)
                                 item {
                                     Card(
                                         colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Column(modifier = Modifier.padding(16.dp)) {
                                             Text(
                                                 text = "E. Fármacos Potencialmente Inapropiados (FPI)",
                                                 style = MaterialTheme.typography.titleMedium,
                                                 fontWeight = FontWeight.Bold,
                                                 color = Color(0xFFC05621) // Warning style red-orange
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "En todo paciente con sospecha o diagnóstico de EA, es prioritario revisar la pertinencia y suspender/evitar el uso de fármacos que empeoren el rendimiento cognitivo o induzcan delirium:",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )
                                             Spacer(modifier = Modifier.height(10.dp))

                                             val inappropriateDrugs = listOf(
                                                 Triple("Antihistamínicos de primera generación", "Ej: Difenhidramina, Clorfeniramina.", "Efecto anticolinérgico central severo, aumentan confusión y riesgo de caídas."),
                                                 Triple("Antidepresivos con actividad anticolinérgica", "Ej: Amitriptilina, Imipramina (Tricíclicos).", "Altamente perjudiciales para la memoria, retención urinaria, sequedad y sedación."),
                                                 Triple("Antipsicóticos de 1a y 2a generación", "Ej: Haloperidol, risperidona.", "Riesgo incrementado de eventos cerebrovasculares, delirium refractario o parkinsonismo inducido."),
                                                 Triple("Benzodiacepinas", "Ej: Alprazolam, Clonazepam, Lorazepam.", "Empeoramiento de la memoria episódica, caídas frecuentes y confusión psicomotora."),
                                                 Triple("Agonistas Dopaminérgicos", "Ej: Pramipexol, Rotigotina.", "Fomentan síntomas de psicosis, alucinaciones, delirio paranoide y agitación.")
                                             )

                                             inappropriateDrugs.forEachIndexed { i0, drug ->
                                                 val idx = 50 + i0
                                                 val isChecked = alzheimerChecklistMap[idx] == true
                                                 Row(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .clickable { alzheimerChecklistMap[idx] = !isChecked }
                                                         .background(
                                                             if (isChecked) Color(0xFFFFFAF0) // Warm orange-ish
                                                             else colorScheme.surface,
                                                             shape = RoundedCornerShape(8.dp)
                                                         )
                                                         .border(
                                                             1.dp,
                                                             if (isChecked) Color(0xFFDD6B20).copy(alpha = 0.5f) else colorScheme.outlineVariant.copy(alpha = 0.15f),
                                                             RoundedCornerShape(8.dp)
                                                         )
                                                         .padding(10.dp),
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Checkbox(
                                                         checked = isChecked,
                                                         onCheckedChange = { alzheimerChecklistMap[idx] = it },
                                                         colors = CheckboxDefaults.colors(checkedColor = Color(0xFFDD6B20))
                                                     )
                                                     Spacer(modifier = Modifier.width(8.dp))
                                                     Column {
                                                         Row(verticalAlignment = Alignment.CenterVertically) {
                                                             Text(drug.first, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF9C4221))
                                                             Spacer(modifier = Modifier.width(4.dp))
                                                             Box(
                                                                 modifier = Modifier
                                                                     .clip(RoundedCornerShape(4.dp))
                                                                     .background(Color(0xFFFED7D7))
                                                                     .padding(horizontal = 4.dp, vertical = 2.dp)
                                                             ) {
                                                                 Text("EVITAR", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = Color(0xFF9B1C1C))
                                                             }
                                                         }
                                                         Text(drug.second, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface)
                                                         Text(drug.third, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                                     }
                                                 }
                                                 Spacer(modifier = Modifier.height(4.dp))
                                             }
                                         }
                                     }
                                 }

                                 item {
                                     Card(
                                         colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Column(modifier = Modifier.padding(16.dp)) {
                                             Text(
                                                 text = "Diagnósticos Diferenciales Sistémicos",
                                                 style = MaterialTheme.typography.titleMedium,
                                                 fontWeight = FontWeight.Bold,
                                                 color = colorScheme.primary
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "Considerar descarte activo de procesos que emulen fallas mnésicas antes de emitir juicio clínico final:",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )
                                             Spacer(modifier = Modifier.height(8.dp))

                                             val diffsList = listOf(
                                                 "Infecciones del sistema nervioso central (meningoencefalitis crónicas, virus lentos, priones).",
                                                 "Trauma Craneoencefálico (Hemorragia subdural crónica, demencia post-trauma).",
                                                 "Ataque Cerebrovascular (Demencia vascular pura o mixta por microangiopatía focal).",
                                                 "Abuso de sustancias crónicas (Ej. Etanol, solventes) o patología psiquiátrica mayor.",
                                                 "Delirium agudo (descartar infecciones urinarias, hipoxia, desequilibrios metabólicos/electrolíticos).",
                                                 "Trastorno Neurocognitivo Mayor debido a otras etiologías específicas (Demencia Frontotemporal, Demencia con Cuerpos de Lewy, demencias secundarias)."
                                             )

                                             diffsList.forEach { diff ->
                                                 Row(
                                                     verticalAlignment = Alignment.Top,
                                                     modifier = Modifier.padding(vertical = 4.dp)
                                                 ) {
                                                     Icon(Icons.Default.Warning, contentDescription = null, tint = colorScheme.outline, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                                     Spacer(modifier = Modifier.width(6.dp))
                                                     Text(text = diff, style = MaterialTheme.typography.bodySmall)
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                             3 -> {
                                 // Tab 3: Estadificación de la Condición mediante Escala GDS
                                 item {
                                     Card(
                                         colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Column(modifier = Modifier.padding(16.dp)) {
                                             Text(
                                                 text = "F. Escala de Deterioro Global (GDS - Reisberg)",
                                                 style = MaterialTheme.typography.titleMedium,
                                                 fontWeight = FontWeight.Bold,
                                                 color = colorScheme.primary
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "Estadificación clínica de la progresión del deterioro cognoscitivo en base a 7 estadios funcionales y conductuales del Alzheimer. Toque un estadio para evaluarlo:",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = colorScheme.onSurfaceVariant
                                             )
                                             Spacer(modifier = Modifier.height(12.dp))

                                             // Stage horizontal row grid
                                             Row(
                                                 modifier = Modifier.fillMaxWidth(),
                                                 horizontalArrangement = Arrangement.SpaceBetween
                                             ) {
                                                 (1..7).forEach { num ->
                                                     val isSelected = selectedGdsStage == num
                                                     val isDementia = num >= 4
                                                     val btnColor = if (isSelected) {
                                                         if (isDementia) Color(0xFFDD6B20) else colorScheme.primary
                                                     } else {
                                                         colorScheme.surfaceVariant
                                                     }
                                                     val textClr = if (isSelected) Color.White else colorScheme.onSurfaceVariant

                                                     Box(
                                                         modifier = Modifier
                                                             .size(38.dp)
                                                             .clip(RoundedCornerShape(8.dp))
                                                             .background(btnColor)
                                                             .clickable { selectedGdsStage = num }
                                                             .testTag("gds_stage_button_$num"),
                                                         contentAlignment = Alignment.Center
                                                     ) {
                                                         Text(
                                                             text = num.toString(),
                                                             style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                             color = textClr
                                                         )
                                                     }
                                                 }
                                             }

                                             Spacer(modifier = Modifier.height(14.dp))

                                             // GDS Stage metadata description
                                             val gdsDescriptions = mapOf(
                                                 1 to Triple("Sin deterioro cognitivo", "Normalidad. Ausencia de quejas de memoria del paciente o de déficits en la entrevista clínica formal.", "Fase Pre-demencia"),
                                                 2 to Triple("Deterioro cognitivo muy leve / Olvidos", "Olvida dónde se colocaron objetos familiares, olvida nombres de conocidos. No hay déficits objetivos en trabajo o relaciones sociales.", "Fase Pre-demencia (Olvidos seniles normales)"),
                                                 3 to Triple("Deterioro cognitivo leve (DCL / MCI)", "Primeros déficits claros. Desorientación al viajar a sitios no familiares, dificultad notable de denominación o encuentro de palabras, baja retención de lectura, pierde objetos de valor, fallos de concentración.", "Fase Pre-demencia limite (Deterioro Cognitivo Leve)"),
                                                 4 to Triple("Deterioro cognitivo moderado (Demencia Leve)", "Disminución notable del conocimiento de eventos actuales y recientes. Fallos en concentración (cálculo serial de 7s). Dificultad extrema en planificar finanzas, contabilidad o viajes autónomos. Se conservan bien de orientación temporal.", "Deterioro Moderado (Demencia Leve)"),
                                                 5 to Triple("Deterioro cognitivo moderadamente grave", "El paciente requiere ayuda obligatoria de terceros para sobrevivir (Déficit funcional patente). Incapaz de recordar direcciones frecuentes, teléfonos habituales o nombres de familiares íntimos. Confusión temporoespacial constante. Requiere ayuda para vestir de forma apropiada.", "Deterioro Moderadamente Grave (Demencia Moderada)"),
                                                 6 to Triple("Deterioro cognitivo grave", "Olvida el nombre de su cónyuge. Desconocimiento de eventos vitales recientes. Requiere ayuda directa para higiene, ducha, vestir adecuadamente o ir al baño (Incontinencia ocasional). Trastornos del sueño y disrupción conductual severa (ansiedad, agitación, delirios de sospecha).", "Deterioro Grave (Demencia Moderadamente Severa)"),
                                                 7 to Triple("Deterioro cognitivo muy grave", "Pérdida total del lenguaje expresivo (emisión exclusiva de ruidos, gritos o fragmentos menores). Pérdida de movilización autónoma, marcha, alimentación, sedestación. Incontinencia total de esfínteres (vesical y fecal). Requiere asistencia absoluta en todas las actividades.", "Deterioro Muy Grave (Demencia Severa)")
                                             )

                                             val (gdsTitle, gdsDetail, gdsCategory) = gdsDescriptions[selectedGdsStage] ?: Triple("", "", "")
                                             val isDementiaMode = selectedGdsStage >= 4

                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .background(
                                                         if (isDementiaMode) Color(0xFFFFF5F5) else Color(0xFFF0FDF4),
                                                         RoundedCornerShape(8.dp)
                                                     )
                                                     .border(
                                                         1.dp,
                                                         if (isDementiaMode) Color(0xFFFEB2B2) else Color(0xFFBBF7D0),
                                                         RoundedCornerShape(8.dp)
                                                     )
                                                     .padding(12.dp)
                                             ) {
                                                 Column {
                                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                                         Box(
                                                             modifier = Modifier
                                                                 .clip(RoundedCornerShape(4.dp))
                                                                 .background(if (isDementiaMode) Color(0xFFFEB2B2) else Color(0xFFBBF7D0))
                                                                 .padding(horizontal = 6.dp, vertical = 2.dp)
                                                         ) {
                                                             Text(
                                                                 text = if (isDementiaMode) "DEMENCIA ACTIVA" else "PRE-DEMENCIA (CONSERVADO)",
                                                                 style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                                 color = if (isDementiaMode) Color(0xFF9B1C1C) else Color(0xFF15803D)
                                                             )
                                                         }
                                                         Spacer(modifier = Modifier.width(8.dp))
                                                         Text(
                                                             text = "Estadio GDS $selectedGdsStage",
                                                             style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                             color = Color.Black
                                                         )
                                                     }
                                                     Spacer(modifier = Modifier.height(6.dp))
                                                     Text(text = gdsTitle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                                     Spacer(modifier = Modifier.height(4.dp))
                                                     Text(text = gdsDetail, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                                 }
                                             }

                                             // Critical prompt-based warning for GDS >= 5
                                             if (selectedGdsStage >= 5) {
                                                 Spacer(modifier = Modifier.height(10.dp))
                                                 Box(
                                                     modifier = Modifier
                                                         .fillMaxWidth()
                                                         .background(Color(0xFFFFF2E6), RoundedCornerShape(8.dp))
                                                         .border(2.dp, Color(0xFFDD6B20), RoundedCornerShape(8.dp))
                                                         .padding(12.dp)
                                                 ) {
                                                     Row(verticalAlignment = Alignment.Top) {
                                                         Icon(Icons.Default.Warning, contentDescription = "Alerta Funcional", tint = Color(0xFFDD6B20), modifier = Modifier.size(22.dp))
                                                         Spacer(modifier = Modifier.width(10.dp))
                                                         Column {
                                                             Text(
                                                                 text = "⚠️ REQUERIMIENTO SOBERANO DE APOYO",
                                                                 style = MaterialTheme.typography.labelSmall,
                                                                 fontWeight = FontWeight.Bold,
                                                                 color = Color(0xFFC05621)
                                                             )
                                                             Text(
                                                                 text = "La escala GDS indica que a partir del Estadio 5 la persona REQUIERE APOYO CONTINUO para la realización de sus actividades instrumentales y básicas de la vida diaria. Asegure el reclutamiento del cuidador principal y solicite interconsulta prioritaria a Trabajo Social.",
                                                                 style = MaterialTheme.typography.bodySmall,
                                                                 color = Color(0xFF7B341E)
                                                             )
                                                         }
                                                     }
                                                 }
                                             }

                                             Spacer(modifier = Modifier.height(14.dp))
                                             Card(
                                                 colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                                 modifier = Modifier.fillMaxWidth()
                                             ) {
                                                 Column(modifier = Modifier.padding(12.dp)) {
                                                     Text(
                                                         text = "Voluntades Anticipadas y Enfoque Diferencial",
                                                         style = MaterialTheme.typography.labelMedium,
                                                         fontWeight = FontWeight.Bold,
                                                         color = colorScheme.primary
                                                     )
                                                     Spacer(modifier = Modifier.height(4.dp))
                                                     Text(
                                                         text = "Recuerde interrogar y documentar formalmente las voluntades anticipadas del paciente desde las fases precoces (GDS 1-3). Aplique ajustes diferenciales por nivel formativo, idioma u origen geográfico en las pruebas cognitivas para minimizar errores de falso positivo.",
                                                         style = MaterialTheme.typography.bodySmall,
                                                         color = colorScheme.onSurfaceVariant
                                                     )
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }

                         // SMART COPY ACTION FOR ALZHEIMER
                         item {
                             Spacer(modifier = Modifier.height(4.dp))
                             Button(
                                 onClick = {
                                     val statusSummary = when (selectedAlzheimerTab) {
                                         0 -> {
                                             val countsSymptom = (0..4).count { alzheimerChecklistMap[it] == true }
                                             val countsVgi = (10..17).count { alzheimerChecklistMap[it] == true }
                                             "Cribado Hecho: $countsSymptom síntomas sospechosos, $countsVgi items de VGI realizados."
                                         }
                                         1 -> "Estudios y Consenso: Exámenes de descarte planificados y rol de Pfizer coordinado."
                                         2 -> "Seguridad Farmacológica: Medicinas inapropiadas auditadas e infecciones/delirium revisados."
                                         else -> "Estadificación GDS actual: Estadio GDS $selectedGdsStage (${if (selectedGdsStage >= 4) "Diferencial de Demencia" else "Fase Pre-demencia"})."
                                     }
                                     onCopyClicked(
                                         "Directrices Alzheimer TNM-EA (Hospital Universitario Nacional)",
                                         "ESTADO CLÍNICO DE ALZHEIMER: GDS $selectedGdsStage",
                                         listOf(
                                             statusSummary,
                                             "Estadio GDS actual: $selectedGdsStage",
                                             "Soporte de Cuidado: " + if (selectedGdsStage >= 5) "REQUIERE APOYO OBLIGATORIO DE TERCEROS" else "Preserva supervivencia autónoma básica"
                                         ),
                                         "Conforme código IN-EC-33 del Hospital Universitario Nacional de Colombia."
                                     )
                                 },
                                 modifier = Modifier.fillMaxWidth().testTag("copy_alzheimer_guidelines")
                             ) {
                                 Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Directrices")
                                 Spacer(modifier = Modifier.width(8.dp))
                                 Text("Copiar Pautas y Estado")
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
}

data class NavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val logicalIndex: Int,
    val isCenter: Boolean = false
)

data class ClinicalDomain(
    val id: String,
    val name: String,
    val acronym: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val categories: List<String>,
    val description: String
)

@Composable
fun ClinicalDomainChip(
    domain: ClinicalDomain,
    isSelected: Boolean,
    onClick: () -> Unit,
    drugCount: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "domain_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val borderWidth by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_width"
    )

    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .height(50.dp)
            .clickable(onClick = onClick)
            .testTag("domain_chip_${domain.id}"),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                domain.primaryColor.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = borderWidth.dp,
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        domain.primaryColor.copy(alpha = glowAlpha),
                        domain.secondaryColor.copy(alpha = glowAlpha),
                        domain.primaryColor.copy(alpha = glowAlpha)
                    )
                )
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) domain.primaryColor.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = domain.icon,
                    contentDescription = domain.name,
                    tint = if (isSelected) domain.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = domain.acronym,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isSelected) domain.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(domain.secondaryColor)
                        )
                    }
                }
                Text(
                    text = domain.description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) domain.primaryColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = drugCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp
                    ),
                    color = if (isSelected) domain.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TabFarmacos(
    filterQuery: String,
    onFilterQueryChange: (String) -> Unit,
    onCopyClicked: (String, String, String) -> Unit
) {
    var selectedDomainId by remember { mutableStateOf<String?>(null) }
    var selectedDrugForDetail by remember { mutableStateOf<com.example.data.ClinicalDatabase.DrugReference?>(null) }

    LaunchedEffect(filterQuery) {
        if (filterQuery.isNotBlank()) {
            val matchingDrug = com.example.data.ClinicalDatabase.drugs.find {
                it.name.contains(filterQuery, ignoreCase = true) || 
                it.acronym.equals(filterQuery, ignoreCase = true) ||
                filterQuery.contains(it.name, ignoreCase = true)
            }
            if (matchingDrug != null) {
                selectedDrugForDetail = matchingDrug
            }
        }
    }

    val domains = remember {
        listOf(
            ClinicalDomain(
                id = "epilepsia",
                name = "Epilepsia",
                acronym = "EPILEPSIA",
                icon = Icons.Default.Bolt,
                primaryColor = Color(0xFFFFB300), // Electric Amber
                secondaryColor = Color(0xFFFFE082), // Amber Light
                categories = listOf(
                    "Bloqueadores de Sodio y Espectro Amplio",
                    "Ligandos SV2 y Moduladores",
                    "GABAérgicos, Anhidrasa y afines",
                    "Dianas Específicas y Síndromes"
                ),
                description = "Anticrisis y descargas neuronales"
            ),
            ClinicalDomain(
                id = "acv",
                name = "ACV / Ictus",
                acronym = "ACV / ICTUS",
                icon = Icons.Default.LocalHospital,
                primaryColor = Color(0xFFEF4444), // Crimson Red
                secondaryColor = Color(0xFFFCA5A5), // Red Soft Outline
                categories = listOf("Reperfusión y Antiagregantes"),
                description = "Manejo agudo y prevención secundaria"
            ),
            ClinicalDomain(
                id = "neuroinmuno",
                name = "Neuroinmunología",
                acronym = "NEUROINMUNO",
                icon = Icons.Default.Favorite,
                primaryColor = Color(0xFF00897B), // Mint Teal
                secondaryColor = Color(0xFFB2DFDB), // Teal light
                categories = listOf("Neuroinmunología Desmielinizante"),
                description = "Esclerosis Múltiple y brotes"
            ),
            ClinicalDomain(
                id = "parkinson",
                name = "Trastornos Motores",
                acronym = "PARKINSON",
                icon = Icons.Default.Refresh,
                primaryColor = Color(0xFF3B82F6), // Motion Blue
                secondaryColor = Color(0xFFBFDBFE), // Soft Blue
                categories = listOf("Parkinson y Trastornos del Movimiento"),
                description = "Parkinson, temblor y distonía"
            ),
            ClinicalDomain(
                id = "ela_demencia",
                name = "Degenerativas / Unión",
                acronym = "MIO/ELA/DEM",
                icon = Icons.Default.Home,
                primaryColor = Color(0xFF8B5CF6), // Purple Violet
                secondaryColor = Color(0xFFDDD6FE), // Soft Lavender
                categories = listOf("Fármacos de ELA, Miastenia y Demencias"),
                description = "Demencia, ELA y Miastenia Gravis"
            )
        )
    }

    val activeDomain = domains.find { it.id == selectedDomainId }

    val drugCounts = remember(com.example.data.ClinicalDatabase.drugs) {
        domains.associate { domain ->
            domain.id to com.example.data.ClinicalDatabase.drugs.count { domain.categories.contains(it.category) }
        }
    }

    val filteredDrugs = remember(selectedDomainId, filterQuery) {
        com.example.data.ClinicalDatabase.drugs.filter { drug ->
            val matchesDomain = if (activeDomain != null) {
                activeDomain.categories.contains(drug.category)
            } else {
                true // Show all when searching globally
            }
            val matchesQuery = if (filterQuery.isBlank()) {
                true
            } else {
                drug.name.contains(filterQuery, ignoreCase = true) || drug.acronym.contains(filterQuery, ignoreCase = true)
            }
            matchesDomain && matchesQuery
        }
    }

    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    // Auto-expand single folder/category configurations
    LaunchedEffect(selectedDomainId) {
        expandedFolders.clear()
        activeDomain?.categories?.let { categories ->
            if (categories.size <= 2) {
                categories.forEach { expandedFolders[it] = true }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Main Catalog Search Input (Shown at top for all)
        OutlinedTextField(
            value = filterQuery,
            onValueChange = onFilterQueryChange,
            placeholder = { Text("Buscar fármaco o acrónimo (AAS, CBZ, L-DOPA)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = {
                if (filterQuery.isNotEmpty()) {
                    IconButton(onClick = { onFilterQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                // --- GLOBAL SEARCH RESULT RESULTS ---
                item {
                    Text(
                        text = "Resultados de Búsqueda (${filteredDrugs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(filteredDrugs) { drug ->
                    CompactDrugRow(
                        drug = drug,
                        onClick = { selectedDrugForDetail = drug }
                    )
                }

                if (filteredDrugs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Sin coincidencias. Intenta buscando parte del nombre o siglas.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (selectedDomainId == null) {
                // --- SPECIALTY GRID DIRECTORIES ---
                item {
                    Text(
                        text = "Especialidades Clínicas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Chunk and display in 2 columns
                val chunked = domains.chunked(2)
                chunked.forEach { rowDomains ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowDomains.forEach { d ->
                                Card(
                                    onClick = { selectedDomainId = d.id },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .testTag("domain_dir_${d.id}"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(d.primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = d.icon,
                                                    contentDescription = d.name,
                                                    tint = d.primaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ) {
                                                Text(
                                                    text = "${drugCounts[d.id] ?: 0}",
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = d.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = d.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowDomains.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // General "Ver Todo" link
                item {
                    Card(
                        onClick = { selectedDomainId = "todos_general" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("domain_dir_all"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = "Ver Todos los Medicamentos",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Directorio clínico completo sin filtros de área",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                // --- DIRECTORY SUBFOLDERS IN DRILL-DOWN VIEW ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedDomainId = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            val activeName = if (selectedDomainId == "todos_general") "Catálogo Completo" else activeDomain?.name ?: ""
                            Text(
                                text = activeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Farmacopea > Selección",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                val categoriesList = if (selectedDomainId == "todos_general") {
                    listOf(
                        "Bloqueadores de Sodio y Espectro Amplio",
                        "Ligandos SV2 y Moduladores",
                        "GABAérgicos, Anhidrasa y afines",
                        "Dianas Específicas y Síndromes",
                        "Reperfusión y Antiagregantes",
                        "Parkinson y Trastornos del Movimiento",
                        "Neuroinmunología Desmielinizante",
                        "Fármacos de ELA, Miastenia y Demencias"
                    )
                } else {
                    activeDomain?.categories ?: emptyList()
                }

                categoriesList.forEach { categoryName ->
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
                                    containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = "Carpeta",
                                            tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Column {
                                            Text(
                                                text = categoryName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${drugsInCat.size} fármacos registrados",
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
                                        .padding(start = 12.dp, top = 2.dp, bottom = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(48.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        CompactDrugRow(
                                            drug = drug,
                                            onClick = { selectedDrugForDetail = drug }
                                        )
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

    // --- SLEEK DRUG DETAIL POPUP DIALOG ---
    if (selectedDrugForDetail != null) {
        val drug = selectedDrugForDetail!!
        AlertDialog(
            onDismissRequest = { selectedDrugForDetail = null },
            title = {
                Column {
                    Text(
                        text = "${drug.name} (${drug.acronym})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = drug.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Indicaciones
                    Column {
                        Text(
                            text = "⚡ Indicaciones y Espectro Clínico:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = drug.indications,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Dosis
                    Column {
                        Text(
                            text = "💊 Posología y Administración:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = drug.dosage,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Efectos adversos
                    Column {
                        Text(
                            text = "🚨 Efectos Adversos Críticos:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = drug.sideEffects,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Sello clínico
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Sello Clínico & Farmacología Bedside:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = drug.clinicalNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCopyClicked(
                            "${drug.name} (${drug.acronym})",
                            drug.dosage,
                            "Indicación: ${drug.indications}\nInteracciones/Efectos: ${drug.sideEffects}\nNotas: ${drug.clinicalNotes}"
                        )
                        selectedDrugForDetail = null
                    },
                    modifier = Modifier.fillMaxWidth().testTag("copy_from_dialog_${drug.acronym}")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar Reporte Clínico")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedDrugForDetail = null }
                ) {
                    Text("Cerrar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun CompactDrugRow(
    drug: com.example.data.ClinicalDatabase.DrugReference,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("drug_row_${drug.acronym}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = drug.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = drug.acronym,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
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
                        text = "Historial Clínico de Cálculos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                TextButton(
                    onClick = onClearClicked,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Limpiar historial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
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
    var selectedSubTab by remember { mutableStateOf("all") } // "all", "acv", "funcional", "historial"

    // Local States for ABCD²
    var abcdAge by remember { mutableStateOf<Int?>(null) }
    var abcdBp by remember { mutableStateOf<Int?>(null) }
    var abcdClinical by remember { mutableStateOf<Int?>(null) }
    var abcdDuration by remember { mutableStateOf<Int?>(null) }
    var abcdDiabetes by remember { mutableStateOf<Int?>(null) }

    // Local States for Fisher / Hunt & Hess
    var selectedFisherGrade by remember { mutableStateOf<Int?>(null) }
    var selectedHuntHessGrade by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Scrollable Category Filter Chips Row
        item {
            val categories = listOf(
                Triple("all", "Todas", Icons.Default.Bolt),
                Triple("acv", "ACV / Vascular", Icons.Default.LocalHospital),
                Triple("funcional", "Funcionales", Icons.Default.Accessibility),
                Triple("examen", "Examen Físico", Icons.Default.AccessibilityNew),
                Triple("historial", "Historial", Icons.Default.History)
            )
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (id, label, icon) ->
                    val isSelected = selectedSubTab == id
                    Surface(
                        modifier = Modifier
                            .clickable { selectedSubTab = id }
                            .testTag("subtab_$id"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Show History content
        if (selectedSubTab == "all" || selectedSubTab == "historial") {
            item {
                recentHistoryContent()
            }
        }

        // --- ACV / VASCULAR CATEGORY SCALES ---
        if (selectedSubTab == "all" || selectedSubTab == "acv") {
            // Header for ACV / Vascular Scales
            if (selectedSubTab == "all") {
                item {
                    Text(
                        text = "Ictus, Pronósticos y Escalas Vasculares",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Card 1: ABCD2 Score (TIA risk)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("abcd2_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Score ABCD² (Riesgo Ictus pos-AIT)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Predicción de riesgo de ACV isquémico a los 2, 7 y 90 días después de un ataque isquémico transitorio (AIT). Desempeño bedside de alta utilidad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Age
                        Text("A - Edad (Age):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("0: < 60 años" to 0, "1: ≥ 60 años" to 1).forEach { (lbl, v) ->
                                val selected = abcdAge == v
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { abcdAge = v },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFFEF4444).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // BP Selection
                        Text("B - Presión Arterial (Blood Pressure):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("0: Normal (<140/90)" to 0, "1: TA ≥ 140/90" to 1).forEach { (lbl, v) ->
                                val selected = abcdBp == v
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { abcdBp = v },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFFEF4444).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Clinical features Selection
                        Text("C - Características Clínicas (Clinical features):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf(
                                "0: Otros síntomas cognitivos o sensitivos (0 pts)" to 0,
                                "1: Alteración del habla aislada sin debilidad (1 pt)" to 1,
                                "2: Debilidad motora unilateral (2 pts)" to 2
                            ).forEach { (lbl, v) ->
                                val selected = abcdClinical == v
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { abcdClinical = v },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFFEF4444).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Duration Selection
                        Text("D - Duración de los Síntomas (Duration):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("0: <10 min" to 0, "1: 10-59 min" to 1, "2: ≥60 min" to 2).forEach { (lbl, v) ->
                                val selected = abcdDuration == v
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { abcdDuration = v },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFFEF4444).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Diabetes Selection
                        Text("D - Diabetes (DM):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("0: No" to 0, "1: Sí" to 1).forEach { (lbl, v) ->
                                val selected = abcdDiabetes == v
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { abcdDiabetes = v },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFFEF4444).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // Compiled Results
                        val abcdCompleted = abcdAge != null && abcdBp != null && abcdClinical != null && abcdDuration != null && abcdDiabetes != null
                        val abcdTotal = (if (abcdAge == 1) 1 else 0) +
                                        (if (abcdBp == 1) 1 else 0) +
                                        (abcdClinical ?: 0) +
                                        (abcdDuration ?: 0) +
                                        (if (abcdDiabetes == 1) 1 else 0)

                        val interpretation = when {
                            abcdTotal <= 3 -> "Riesgo Bajo (Aproximadamente 1.0% de riesgo de ACV a los 2 días)"
                            abcdTotal <= 5 -> "Riesgo Moderado (Aproximadamente 4.1% de riesgo de ACV a los 2 días)"
                            else -> "Riesgo Alto (Aproximadamente 8.1% de riesgo de ACV a los 2 días)"
                        }
                        val detailInterpretation = when {
                            abcdTotal <= 3 -> "Reevaluación ambulatoria rápida. Tratamiento preventivo estándar."
                            abcdTotal <= 5 -> "Considerar hospitalización/observación clínica. Evaluar indicación urgente de terapia dual (DAPT)."
                            else -> "Urgencia extrema. Requiere ingreso hospitalario inmediato, monitoreo y estudio vascular inmediato."
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (abcdCompleted) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (abcdCompleted) Color(0xFFEF4444).copy(alpha = 0.3f) else Color.Transparent)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Resultado Score ABCD²: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (abcdCompleted) "$abcdTotal / 7 puntos" else "Seleccione todos los ítems",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (abcdCompleted) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (abcdCompleted) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = interpretation, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "Conducta: $detailInterpretation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val items = listOf(
                                                "Edad >=60: ${if (abcdAge == 1) "Sí (1 pt)" else "No (0 pts)"}",
                                                "TA >=140/90: ${if (abcdBp == 1) "Sí (1 pt)" else "No (0 pts)"}",
                                                "Clínica: ${if (abcdClinical == 2) "Debilidad unilateral (2 pts)" else if (abcdClinical == 1) "Alteración habla (1 pt)" else "Otro (0 pts)"}",
                                                "Duración: ${if (abcdDuration == 2) ">=60 min (2 pts)" else if (abcdDuration == 1) "10-59 min (1 pt)" else "<10 min (0 pts)"}",
                                                "Diabetes: ${if (abcdDiabetes == 1) "Sí (1 pt)" else "No (0 pts)"}"
                                            )
                                            onCopyClicked("Score ABCD² (Riesgo AIT)", "$abcdTotal/7 pts", "Interpretación: $interpretation. Detalle:\n- " + items.joinToString("\n- "))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        modifier = Modifier.align(Alignment.End).testTag("copy_abcd")
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copiar Resultado")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Rankin Modificada (mRS)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mrs_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                modifier = Modifier.testTag("mrs_title"),
                                text = "Escala de Rankin Modificada (mRS)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Escala de uso diario y mandatorio en accidentes cerebrovasculares para medir de forma estandarizada el grado de incapacidad física residual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Seleccione el grado residual descriptivo:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        androidx.compose.foundation.layout.FlowRow(
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
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Smart Copy mRS")
                            }
                        }
                    }
                }
            }

            // Card 3: Escala Fisher Modificada (SAH risk)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fisher_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escala de Fisher Modificada (HSA en TAC)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gradación objetiva de sangrado en TAC para calcular el riesgo predictivo exacto de vasoespasmo cerebral secundario duradero.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Seleccione hallazgo tomográfico principal:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val fisherGradesList = listOf(
                                "Grado 1: Hemorragia subaracnoidea nula o mínima, sin HIV" to "Riesgo estimado de vasoespasmo: BAJO (~8%)",
                                "Grado 2: Hemorragia subaracnoidea nula o mínima con hemorragia intraventricular (HIV)" to "Riesgo estimado de vasoespasmo: MODERADO (~23%)",
                                "Grado 3: Sangrado subaracnoideo difuso, grueso o denso, sin HIV" to "Riesgo estimado de vasoespasmo: ALTO (~33%)",
                                "Grado 4: Sangrado subaracnoideo difuso, grueso o denso con HIV" to "Riesgo estimado de vasoespasmo: EXTREMO (~40%)"
                            )
                            fisherGradesList.forEachIndexed { idx, (itemText, riskText) ->
                                val isSelected = selectedFisherGrade == idx
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedFisherGrade = idx }
                                        .testTag("fisher_option_$idx"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFFFF9800).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFF9800) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = itemText,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                            color = if (isSelected) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "⚠️ $riskText",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        selectedFisherGrade?.let { idx ->
                            val textToCopy = when (idx) {
                                0 -> "Grado 1: Sangrado nulo o mínimo, sin HIV (Vasoespasmo ~8%)"
                                1 -> "Grado 2: Sangrado nulo o mínimo con HIV (Vasoespasmo ~23%)"
                                2 -> "Grado 3: Sangrado grueso sordo sin HIV (Vasoespasmo ~33%)"
                                else -> "Grado 4: Sangrado grueso sordo con HIV (Vasoespasmo ~40%)"
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onCopyClicked("Escala Fisher Modificada (TAC)", "Grado ${idx + 1}", textToCopy) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                modifier = Modifier.align(Alignment.End).testTag("copy_fisher")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copiar Resultado")
                            }
                        }
                    }
                }
            }

            // Card 4: Hunt y Hess
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hunthess_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escala Hunt y Hess (Fisiopatología HSA)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clasificación clínica inmediata del enfermo con HSA aneurismática para estimar el riesgo de morbimortalidad general a los 30 días.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Seleccione estatus neurológico y clínico:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val huntHessGrades = listOf(
                                "Grado I: Asintomático o con cefalea mínima y rigidez de nuca leve" to "Mortalidad aproximada: ~1.5%",
                                "Grado II: Cefalea moderada a severa, rigidez de nuca, sin déficit (excepto pares craneales)" to "Mortalidad aproximada: ~3.0%",
                                "Grado III: Somnolencia fluctuante, confusión sutil o déficit focal leve" to "Mortalidad aproximada: ~5.0%",
                                "Grado IV: Estupor profundo, hemiparesia moderada a severa, rigidez temprana" to "Mortalidad aproximada: ~8.0%",
                                "Grado V: Coma flácido profundo, apariencia moribunda franca" to "Mortalidad aproximada: ~10.0%"
                            )
                            huntHessGrades.forEachIndexed { idx, (itemText, mortalityText) ->
                                val isSelected = selectedHuntHessGrade == idx
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedHuntHessGrade = idx }
                                        .testTag("hunthess_option_$idx"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF2196F3).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF2196F3) else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = itemText,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                            color = if (isSelected) Color(0xFF0D47A1) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "⌛ $mortalityText",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF0D47A1)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        selectedHuntHessGrade?.let { idx ->
                            val textToCopy = when (idx) {
                                0 -> "Grado I: Sordera o cefalea leve, rigidez leve (Mortalidad ~1.5%)"
                                1 -> "Grado II: Cefalea moderada, rigidez de nuca, sin déficit motor (Mortalidad ~3.0%)"
                                2 -> "Grado III: Somnolencia/confusión leve, déficit motor focal leve (Mortalidad ~5%)"
                                3 -> "Grado IV: Estupor franco, hemiparesia severa, rigidez precoz (Mortalidad ~8%)"
                                else -> "Grado V: Coma flácido, descerebración profunda, moribundo (Mortalidad ~10%)"
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onCopyClicked("Escala Hunt y Hess (Clínica HSA)", "Grado ${idx + 1}", textToCopy) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                modifier = Modifier.align(Alignment.End).testTag("copy_hunthess")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copiar Resultado")
                            }
                        }
                    }
                }
            }
        }

        // --- FUNCIONALES CATEGORY SCALES ---
        if (selectedSubTab == "all" || selectedSubTab == "funcional") {
            // Header for Functional assessment
            if (selectedSubTab == "all") {
                item {
                    Text(
                        text = "Valoración Funcional y Declinación",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // Card 5: FAST
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fast_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Accessibility, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "FAST (Functional Assessment Staging Tool)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Escala clínica para clasificar de manera precisa la progresión funcional de la Enfermedad de Alzheimer desde normalidad a estadío severo terminal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Seleccione etapa funcional actual:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        androidx.compose.foundation.layout.FlowRow(
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
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Smart Copy FAST")
                            }
                        }
                    }
                }
            }

            // Card 6: MGFA Classification
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mgfa_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Accessibility, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "Clasificación Clínica de MGFA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Clasificación clínica oficial de la Myasthenia Gravis Foundation of America para tipificar la severidad de la afectación neuromuscular.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Seleccione clase clínica diagnosticada:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        androidx.compose.foundation.layout.FlowRow(
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
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Smart Copy MGFA")
                            }
                        }
                    }
                }
            }
        }

        if (selectedSubTab == "all" || selectedSubTab == "examen") {
            if (selectedSubTab == "all") {
                item {
                    Text(
                        text = "Examen Físico Neurológico",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            item {
                ExamenFisicoCard(onCopyClicked = onCopyClicked)
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun ExamenFisicoCard(
    onCopyClicked: (String, String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("todos") } // "todos", "pares", "fuerza_reflejos", "dermatomas", "signos"

    // Sub-states for Cranial Nerves
    val cranialNervesList = remember {
        listOf(
            CranialNerveInfo("I", "Olfatorio", "Sensitivo", "Percepción de olores comunes unilateralmente (café, jabón) obstruyendo la otra fosa nasal.", "Anosmia / Hiposmia. Descartar causas locales / obstructivas nasales frente a lesión traumática de la lámina cribosa."),
            CranialNerveInfo("II", "Óptico", "Sensitivo", "Agudeza visual (tabla de Snellen bedside), campos visuales por confrontación periférica y fondo de ojo.", "Hemianopsias, cuadrantanopsias, escotomas, borramiento de bordes de papila (papiledema por hipertensión endocraneana)."),
            CranialNerveInfo("III", "Motor Ocular Común", "Motor / Parasimpático", "Reflejos pupilares (fotomotor directo, consensual y acomodación). Elevación de párpados y movimientos oculares superior, inferior e interno.", "Ptosis palpebral, pupila dilatada arreactiva (midriasis directa), estrabismo divergente. Descartar herniación uncal."),
            CranialNerveInfo("IV", "Patético / Troclear", "Motor", "Movimiento ocular hacia abajo y adentro (mirada patética).", "Dificultad para descender escaleras o leer. Diplopía vertical que mejora inclinando la cabeza al lado opuesto."),
            CranialNerveInfo("V", "Trigémino", "Mixto", "Sensibilidad facial en sus 3 ramas (V1 oftálmica, V2 maxilar, V3 mandibular), fuerza de masticación muscular y reflejo corneal primario.", "Hipoestesia facial unilateral, debilidad de músculos masticatorios (desviación de mandíbula hacia lado de la lesión), neuralgia."),
            CranialNerveInfo("VI", "Motor Ocular Externo", "Motor", "Abducción ocular en plano horizontal (mirada hacia afuera).", "Estrabismo convergente unilateral. Incapacidad para abducir el ojo ipsilateral, diplopía de mirada horizontal."),
            CranialNerveInfo("VII", "Facial", "Mixto", "Simetría de mímica facial: arrugar frente, cerrar ojos con fuerza, silbar, sonreír. Gusto en los 2/3 anteriores de la lengua.", "Parálisis facial periférica (compromete frente y mitad inferior, ej. Bell) frente a parálisis central (conserva frente ipsilateral)."),
            CranialNerveInfo("VIII", "Vestibulococlear", "Sensitivo", "Agudeza auditiva (prueba de frote de dedos, susurro), lateralización de Weber y conducción de Rinne. Estabilización nistágmica ocular.", "Hipoacusia de conducción o neurosensorial, vértigo, nistagmus patológico, inestabilidad en la marcha vestibular."),
            CranialNerveInfo("IX", "Glosofaríngeo", "Mixto", "Gusto en el 1/3 posterior de la lengua, elevación simétrica del velo del paladar, reflejo nauseoso y deglución conjunta con el X par.", "Disfagia, ausencia unilateral del reflejo faríngeo, desviación de la úvula hacia el lado sano (signo de la cortina de Vernet)."),
            CranialNerveInfo("X", "Vago / Neumogástrico", "Mixto", "Calidad de la voz (ronquera, disfonía), reflejo de deglución de saliva y simetría de pilares palatinos posteriores.", "Disfonía persistente, disfagia marcada, desviación de la úvula contraria a la lesión. Disfunción autonómica parasimpática."),
            CranialNerveInfo("XI", "Espinal / Accesorio", "Motor", "Fuerza para girar la cabeza contra resistencia lateral (músculo Esternocleidomastoideo) y elevación de hombros (músculo Trapecio).", "Incapacidad para encoger hombros o rotar la cabeza hacia el lado contralateral de la lesión nerviosa central o periférica."),
            CranialNerveInfo("XII", "Hipogloso", "Motor", "Inspección de la lengua en reposo (fasciculaciones, atrofia) y protrusión activa (desviación lingual anterior).", "Desviación de la lengua hacia el lado lesionado al protruirse (por debilidad del músculo geniogloso unilateral), disartria lingual.")
        )
    }

    // Interactive selections for cranial nerves: true = Normal, false = Alterado
    val nerveSelections = remember { mutableStateMapOf<String, Boolean>().apply {
        cranialNervesList.forEach { put(it.number, true) }
    } }
    // User custom clinical findings and abnormalities per nerve
    val nerveAnnotations = remember { mutableStateMapOf<String, String>() }

    // Clicked details state for muscle strength
    var selectedMrcGrade by remember { mutableStateOf<Int?>(null) }
    var selectedReflexGrade by remember { mutableStateOf<Int?>(null) }
    var selectedAshworthGrade by remember { mutableStateOf<Int?>(null) }

    // Special Signs expansion cards
    var expandedBabinski by remember { mutableStateOf(false) }
    var expandedMeningeals by remember { mutableStateOf(false) }
    var expandedJendrassik by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // TOP HEADER INTRO
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Estación de Examen Físico Clínico",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pares craneales reactivos, fuerza segmentaria, reflejos rápidos y dermatomas bedside.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Search filter for physical exam
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar nervio, dermatoma, reflejo, signo clínico...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("examen_search_bar"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        // CATEGORY SEGMENTED CHIPS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "todos" to "Ver Todo",
                "pares" to "Pares Craneales (I-XII)",
                "fuerza_reflejos" to "Escalas de Fuerza & Reflejo",
                "dermatomas" to "Dermatomas",
                "signos" to "Reflejos/Signos Patológicos"
            ).forEach { (id, label) ->
                val isSelected = selectedCategory == id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = id },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("exam_cat_chip_$id")
                )
            }
        }

        // ==========================================
        // 1. DYNAMIC SUMMARY GENERATOR FOR CLINICAL HISTORY
        // ==========================================
        if (selectedCategory == "todos" || selectedCategory == "pares") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "📝 Bitácora del Examen Bedside (Pares Craneales)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Configura el estado de los pares craneales abajo en la cuadrícula [I a XII]. Presiona este botón para copiar el reporte automático instantáneamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    val abnormalNerves = cranialNervesList.filter { !nerveSelections[it.number]!! }
                    val summaryText = buildString {
                        append("EXAMEN DE PARES CRANEALES TRIAJADO:\n")
                        if (abnormalNerves.isEmpty()) {
                            append("- Pares Craneales (I al XII) explorados: Clínicamente Íntegros y simétricos unilateral y bilateralmente, reflejos pupilares conservados.")
                        } else {
                            append("- Pares Craneales normales: ")
                            append(cranialNervesList.filter { nerveSelections[it.number]!! }.joinToString(", ") { "Par ${it.number} (${it.name})" })
                            append("\n")
                            append("- ALTERACIONES DOCUMENTADAS:\n")
                            abnormalNerves.forEach { nerve ->
                                val annot = nerveAnnotations[nerve.number] ?: ""
                                append("  • Par ${nerve.number} (${nerve.name}): ALTERADO. ")
                                if (annot.isNotBlank()) append("Observación: $annot. ")
                                append("Signo clínico concordante: ${nerve.pathology}\n")
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = {
                            onCopyClicked(
                                "REPORTE EXPLORACIÓN NERVIO",
                                "Vía Bedside",
                                summaryText
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("copy_cranial_summary_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar Reporte al Portapapeles", fontSize = 12.sp)
                    }
                }
            }
        }

        // ==========================================
        // 2. PARES CRANEALES CARD LIST
        // ==========================================
        if (selectedCategory == "todos" || selectedCategory == "pares") {
            val filteredNerves = remember(searchQuery) {
                cranialNervesList.filter {
                    it.number.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.type.contains(searchQuery, ignoreCase = true) ||
                    it.howToTest.contains(searchQuery, ignoreCase = true) ||
                    it.pathology.contains(searchQuery, ignoreCase = true)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "I al XII Par Craneal: Exploración Sistematizada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Guía anatómica de chequeo bedside y técnicas de exploración rápida.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredNerves.forEach { nerve ->
                            val isNormal = nerveSelections[nerve.number] ?: true
                            var showDetail by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isNormal) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isNormal) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f).clickable { showDetail = !showDetail }
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isNormal) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = nerve.number,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = if (isNormal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Par ${nerve.number}: ${nerve.name}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = nerve.type,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // TOGGLE CHECKBOX [Normal / Alterado]
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Normal State Chip
                                            Text(
                                                text = if (isNormal) "NORMAL" else "ALTERADO",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isNormal) Color(0xFF10B981) else Color(0xFFEF4444),
                                                modifier = Modifier
                                                    .background(
                                                        color = if (isNormal) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        nerveSelections[nerve.number] = !isNormal
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    // DETAILS
                                    if (showDetail || searchQuery.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // How to examine
                                        Text(
                                            text = "Técnica de exploración bedside:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = nerve.howToTest,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 14.sp,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        // Pathological finding
                                        Text(
                                            text = "Hallazgos patológicos o lesión típica:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = nerve.pathology,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 14.sp
                                        )

                                        // Editable Annotation box if abnormal
                                        if (!isNormal) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val currentAnnot = nerveAnnotations[nerve.number] ?: ""
                                            OutlinedTextField(
                                                value = currentAnnot,
                                                onValueChange = { nerveAnnotations[nerve.number] = it },
                                                label = { Text("Especifique el déficit (" + nerve.number + ")", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredNerves.isEmpty()) {
                            Text(
                                text = "Ningún par craneal coincide con la búsqueda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. FORCE & REFLEX SCALES BEDSIDE INTERACTIVE INTERPRETER
        // ==========================================
        if (selectedCategory == "todos" || selectedCategory == "fuerza_reflejos") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Escalas Clínicas de Motor y Reflejos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Evalúe de forma rápida y objetiva la fuerza motriz segmentaria y las respuestas de reflejos somáticos. Seleccione un nivel para interpretar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // FORCE SECTION
                    Text(
                        text = "1. Escala de Fuerza Muscular (Clasificación MRC)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (0..5).forEach { mrc ->
                            val isSelected = selectedMrcGrade == mrc
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMrcGrade = if (isSelected) null else mrc },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mrc.toString(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Interpret selection MRC
                    if (selectedMrcGrade != null) {
                        val mrcText = when (selectedMrcGrade) {
                            5 -> "Grado 5 (FUERZA NORMAL): El paciente puede sostener la posición de la articulación contra una presión manual fuerte y completa ejercida por el examinador."
                            4 -> "Grado 4 (FUERZA REDUCIDA CONTRA RESISTENCIA): El músculo puede realizar contracciones contra la gravedad y tolerar una resistencia leve a moderada del examinador."
                            3 -> "Grado 3 (MOVIMIENTO SÓLO CONTRA GRAVEDAD): El paciente logra mover la articulación en todo su arco contra la gravedad, pero se desploma ante cualquier mínima resistencia manual."
                            2 -> "Grado 2 (MOVIMIENTO ACTIVADO ELIMINANDO GRAVEDAD): El músculo es incapaz de vencer la gravedad corporal, pero logra desplazar la articulación si se le apoya en una mesa horizontal para eliminarla."
                            1 -> "Grado 1 (CONTRACCIÓN VISIBLE/PALPABLE): No hay movimiento de la extremidad. Al palpar el tendón o el vientre muscular se detecta un ligero esbozo o contracción fibrilar."
                            else -> "Grado 0 (PARÁLISIS COMPLETA): No se visualiza ni palpa ningún tipo de vestigio de contracción voluntaria muscular."
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Interpretación MRC:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = mrcText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // REFLEXES SECTION
                    Text(
                        text = "2. Graduación de Reflejos Osteotendinosos (NINDS / Mayo)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("0", "1+", "2+", "3+", "4+").forEachIndexed { index, reflex ->
                            val isSelected = selectedReflexGrade == index
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedReflexGrade = if (isSelected) null else index },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reflex,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Interpret selection Reflex
                    if (selectedReflexGrade != null) {
                        val refText = when (selectedReflexGrade) {
                            0 -> "0 (ARREFLEXIA): Respuesta ausente. Se debe descartar shock espinal o lesión de segunda motoneurona (nervio periférico o raíz motora). *Realizar Maniobra de Jendrassik para confirmar*."
                            1 -> "1+ (HIPORREFLEXIA): Respuesta disminuida o que requiere facilitación. Compatible con neuropatías periféricas o radiculopatías lumbares/cervicales."
                            2 -> "2+ (NORMORREFLEXIA): Respuesta simétrica y normal esperada en el control de reflejos tendinosos."
                            3 -> "3+ (HIPERREFLEXIA SUTIL / EXALTADO): Respuesta más viva de lo habitual. No siempre indica patología, pero obliga a buscar asimetrías."
                            else -> "4+ (HIPERREFLEXIA SEVERA CON CLONO): Hiperactividad extrema, contracciones repetidas involuntarias (clono). Indica con certeza lesión de la Vía Piramidal / Primera Motoneurona (Cerebral o Médula espinal)."
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Interpretación del Reflejo:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = refText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))

                    // EDIT ESCALA DE ASHWORTH MODIFICADA (TONO Y ESPASTICIDAD)
                    Text(
                        text = "3. Escala de Ashworth Modificada (Tono y Espasticidad)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = "Estandariza la valoración clínica de la espasticidad mediante la movilización pasiva del tono muscular. Seleccione un grado:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val ashworthGrades = listOf("0", "1", "1+", "2", "3", "4")
                        ashworthGrades.forEachIndexed { index, grade ->
                            val isSelected = selectedAshworthGrade == index
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAshworthGrade = if (isSelected) null else index },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = grade,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Interpret selection Ashworth
                    if (selectedAshworthGrade != null) {
                        val ashText = when (selectedAshworthGrade) {
                            0 -> "Grado 0: Tono muscular normal. Sin incremento en la resistencia a la movilización pasiva."
                            1 -> "Grado 1: Ligero incremento del tono. Manifestado por un enganche y liberación (\"catch and release\") o por una resistencia mínima al final del arco de movimiento en flexión o extensión."
                            2 -> "Grado 1+: Ligero incremento del tono. Manifestado por un enganche (\"catch\") seguido de resistencia mínima durante el resto (menos de la mitad) del arco de movimiento pasivo."
                            3 -> "Grado 2: Incremento moderado del tono muscular a lo largo de la mayor parte del rango de movimiento, pero la articulación se desplaza con facilidad."
                            4 -> "Grado 3: Incremento considerable del tono muscular. La movilización pasiva es difícil y requiere esfuerzo del clínico."
                            else -> "Grado 4: Rigidez extrema constante. El miembro afectado se encuentra rígido y bloqueado en flexión o extensión, imposibilitando la movilización pasiva."
                        }
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Interpretación de Espasticidad (Ashworth):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = ashText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }

                    val motorSummaryText = buildString {
                        append("REPORTE MOTOR, TONO Y REFLEJOS BEDSIDE:\n")
                        selectedMrcGrade?.let { mrc ->
                            val mrcText = when (mrc) {
                                5 -> "M5/5 (Fuerza normal contra resistencia manual fuerte completa)"
                                4 -> "M4/5 (Fuerza reducida contra resistencia leve a moderada)"
                                3 -> "M3/5 (Movimiento activo contra gravedad, colapso ante resistencia)"
                                2 -> "M2/5 (Movimiento activo eliminando la fuerza de gravedad)"
                                1 -> "M1/5 (Esbozo de contracción muscular visible o palpable sin desplazamiento)"
                                else -> "M0/5 (Parálisis completa unilateral o bilateral, sin tono muscular)"
                            }
                            append("- Fuerza Muscular (MRC): $mrcText\n")
                        } ?: append("- Fuerza Muscular (MRC): No evaluada activamente\n")

                        selectedReflexGrade?.let { reflexIdx ->
                            val reflexDesc = when (reflexIdx) {
                                0 -> "0 (Arreflexia total. Sugiere shock espinal o compromiso de motoneurona distal)"
                                1 -> "1+ (Hiporreflexia. Respuesta disminuida o palpable con maniobra de Jendrassik)"
                                2 -> "2+ (Normorreflexia. Respuesta articular simétrica y normal fisiológica)"
                                3 -> "3+ (Hiperreflexia leve/exaltada viva sin clono)"
                                else -> "4+ (Hiperreflexia severa patológica con clono o contracciones repetitivas)"
                            }
                            append("- Reflejos Osteotendinosos: $reflexDesc\n")
                        } ?: append("- Reflejos Osteotendinosos: No evaluados activamente\n")

                        selectedAshworthGrade?.let { ashworthIdx ->
                            val ashworthDesc = when (ashworthIdx) {
                                0 -> "Grado 0 (Tono normal, sin aumento en la resistencia pasiva)"
                                1 -> "Grado 1 (Ligero aumento de tono, catch and release al final del arco)"
                                2 -> "Grado 1+ (Ligero aumento de tono, catch seguido de resistencia en la primera mitad del arco)"
                                3 -> "Grado 2 (Aumento moderado del tono, movilización pasiva fácil)"
                                4 -> "Grado 3 (Aumento considerable de tono, movilización pasiva dificultosa)"
                                else -> "Grado 4 (Rigidez extrema constante en flexión o extensión, miembro bloqueado)"
                            }
                            append("- Tono y Espasticidad (Ashworth): $ashworthDesc\n")
                        } ?: append("- Tono y Espasticidad (Ashworth): No evaluado activamente\n")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onCopyClicked(
                                    "REPORTE MOTOR Y TONO",
                                    "Evaluación de Fuerza",
                                    motorSummaryText
                                )
                            },
                            enabled = selectedMrcGrade != null || selectedReflexGrade != null || selectedAshworthGrade != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f).testTag("copy_motor_report")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Reporte Motor & Tono")
                        }

                        IconButton(
                            onClick = {
                                selectedMrcGrade = null
                                selectedReflexGrade = null
                                selectedAshworthGrade = null
                            },
                            enabled = selectedMrcGrade != null || selectedReflexGrade != null || selectedAshworthGrade != null
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar selecciones", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. DERMATOMES CARD
        // ==========================================
        if (selectedCategory == "todos" || selectedCategory == "dermatomas") {
            val filteredDermatomes = remember(searchQuery) {
                com.example.data.ClinicalDatabase.dermatomes.filter {
                    it.level.contains(searchQuery, ignoreCase = true) ||
                    it.landmark.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
                }
            }

            var expandedDermatomes by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("examen_dermatomas_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (expandedDermatomes || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDermatomes = !expandedDermatomes },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessibilityNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Dermatomas Clínicos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Puntos de sensibilidad de referencia y raíces espinales",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (expandedDermatomes || searchQuery.isNotEmpty()) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir dermatomas",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (expandedDermatomes || searchQuery.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredDermatomes.forEach { d ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        color = when {
                                                            d.level.startsWith("C") -> Color(0xFFE0F2FE)
                                                            d.level.startsWith("T") -> Color(0xFFFEE2E2)
                                                            d.level.startsWith("L") -> Color(0xFFFEF3C7)
                                                            else -> Color(0xFFEDE9FE)
                                                        },
                                                        shape = RoundedCornerShape(20.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = d.level,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        d.level.startsWith("C") -> Color(0xFF0369A1)
                                                        d.level.startsWith("T") -> Color(0xFFB91C1C)
                                                        d.level.startsWith("L") -> Color(0xFFB45309)
                                                        else -> Color(0xFF6D28D9)
                                                    }
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = d.landmark,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = d.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                onCopyClicked(
                                                    "Dermatoma ${d.level}",
                                                    d.landmark,
                                                    d.description
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copiar",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (filteredDermatomes.isEmpty()) {
                                Text(
                                    text = "Ningún dermatoma coincide con la búsqueda.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. REFLEXES CARD (ROT)
        // ==========================================
        if (selectedCategory == "todos" || selectedCategory == "dermatomas") { /* Slipped under 'dermatomas' or own category */
            val filteredReflexes = remember(searchQuery) {
                com.example.data.ClinicalDatabase.reflexes.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.level.contains(searchQuery, ignoreCase = true) ||
                    it.nerve.contains(searchQuery, ignoreCase = true) ||
                    it.response.contains(searchQuery, ignoreCase = true) ||
                    it.clinicalNotes.contains(searchQuery, ignoreCase = true)
                }
            }

            var expandedReflexes by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("examen_reflejos_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (expandedReflexes || searchQuery.isNotEmpty()) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedReflexes = !expandedReflexes },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Reflejos Osteotendinosos (Arco Reflejo)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Evaluación segmentaria de integridad espinal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (expandedReflexes || searchQuery.isNotEmpty()) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir reflejos",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (expandedReflexes || searchQuery.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredReflexes.forEach { r ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = r.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${r.level} (${r.nerve})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    onCopyClicked(
                                                        r.name,
                                                        "${r.level} - ${r.nerve}",
                                                        "Respuesta: ${r.response}\nTécnica: ${r.clinicalNotes}"
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copiar",
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Respuesta esperada:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = r.response,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "Metodología diagnóstica:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = r.clinicalNotes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (filteredReflexes.isEmpty()) {
                                Text(
                                    text = "Ningún reflejo coincide con la búsqueda.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. SPECIAL REFLEXES AND MENINGEAL SIGNS
        // ==========================================
        if (selectedCategory == "todos" || selectedCategory == "signos") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Signos Clínicos Especiales (Meninge & Piramidal)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Maniobras de gran impacto en urgencias neurológicas para identificar hipertensión cerebral, irritación de meninges, o lesión piramidal superior.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // BABINSKI REFLEX
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { expandedBabinski = !expandedBabinski },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🦶 Reflejo Plantar Extensor (Babinski)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (expandedBabinski) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (expandedBabinski) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cómo realizar la maniobra:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Deslice un objeto romo (punta roma, mango de martillo) firmemente por el borde lateral externo de la planta del pie, comenzando desde el talón, subiendo curviándose hacia los metatarsianos en forma de 'C' interna.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Interpretación Clínica:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "• Positivo (Anormal): Extensión lenta del primer dedo (hallux) acompañada de apertura en abanico de los dedos restantes. Significa lesión de la Primera Motoneurona / Vía Corticoespinal en cualquier nivel (cerebro o médula).\n• Negativo (Normal): Flexión plantar de los dedos del pie o nula respuesta.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // MENINGEAL IRRITATION SIGNS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { expandedMeningeals = !expandedMeningeals },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🧠 Tríada de Irritación Meníngea (Signos)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (expandedMeningeals) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (expandedMeningeals) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Rigidez de Nuca:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Resistencia dolorosa ante la flexión pasiva anterior del cuello del paciente hacia el tórax.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Text(
                                    text = "2. Signo de Kernig:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Flexionar la cadera del paciente a 90 grados en posición decúbito supino. Al intentar extender la rodilla, se genera una limitación refleja muy dolorosa acompañada de espasmo del bíceps femoral.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Text(
                                    text = "3. Signo de Brudzinski:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Al flexionar pasivamente el cuello del paciente, se produce simultáneamente una flexión refleja espontánea e involuntaria de ambas rodillas y caderas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // JENDRASSIK MANEUVER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { expandedJendrassik = !expandedJendrassik },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🔨 Maniobra Clínicas de Facilitación (Jendrassik)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (expandedJendrassik) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (expandedJendrassik) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Metodología de ejecución y fin fisiológico:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Si un reflejo osteotendinoso (ej. rotuliano) parece ausente, pídale al paciente que entrelace fuertemente los dedos de ambas manos en forma de gancho ante su tórax y tire vigorosamente hacia afuera justo en el momento en que se golpea el tendón. Esto inhibe temporalmente la influencia inhibitoria cortical superior del arco reflejo facilitando la contracción periférica mediada por motoneuronas del asta anterior (Filtro Gamma).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CranialNerveInfo(
    val number: String,
    val name: String,
    val type: String,
    val howToTest: String,
    val pathology: String
)

