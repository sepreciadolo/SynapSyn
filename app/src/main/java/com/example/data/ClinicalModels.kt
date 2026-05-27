package com.example.data

data class ScaleOption(
    val text: String,
    val value: Int
)

data class ScaleDomain(
    val id: String,
    val label: String,
    val description: String = "",
    val options: List<ScaleOption>
)

data class ScaleMetadata(
    val id: String,
    val name: String,
    val acronym: String,
    val category: String,
    val maxScore: Int,
    val description: String,
    val domains: List<ScaleDomain>
)

data class DiagnosticCriteriaSection(
    val title: String,
    val items: List<String>
)

data class DiagnosticCriteria(
    val id: String,
    val name: String,
    val acronym: String,
    val year: String,
    val description: String,
    val sections: List<DiagnosticCriteriaSection>,
    val subclassifications: List<DiagnosticSubclass> = emptyList()
)

data class DiagnosticSubclass(
    val name: String,
    val acronym: String,
    val criteria: String,
    val clinicalPoints: List<String> = emptyList()
)

data class DermatomeReference(
    val level: String,
    val landmark: String,
    val description: String
)

data class ReflexReference(
    val name: String,
    val level: String,
    val nerve: String,
    val response: String,
    val clinicalNotes: String
)

object ClinicalDatabase {
    // 1. Scales definitions and content
    val nihss = ScaleMetadata(
        id = "nihss",
        name = "National Institutes of Health Stroke Scale",
        acronym = "NIHSS",
        category = "Vasco-vascular / ACV",
        maxScore = 42,
        description = "Escala neurológica cuantitativa utilizada para evaluar de manera objetiva la gravedad de un ictus isquémico agudo. Es crítica en decisiones de reperfusión y monitoreo clínico.",
        domains = listOf(
            ScaleDomain("1a", "1a. Nivel de Conciencia", "Estado de alerta general del paciente.", listOf(
                ScaleOption("0 - Alerta / Despierto", 0),
                ScaleOption("1 - Somnoliento / Despertable a mínimo estímulo", 1),
                ScaleOption("2 - Estuporoso / Requiere estímulos repetidos o dolorosos", 2),
                ScaleOption("3 - Comatoso / Flácido, respuesta solo refleja o nula", 3)
            )),
            ScaleDomain("1b", "1b. Preguntas de Orientación", "Preguntar mes actual y edad del paciente.", listOf(
                ScaleOption("0 - Ambos correctas", 0),
                ScaleOption("1 - Una correcta", 1),
                ScaleOption("2 - Ninguna correcta (no responde/afasia)", 2)
            )),
            ScaleDomain("1c", "1c. Órdenes motoras", "Abrir y cerrar ojos, apretar y soltar mano no paretica.", listOf(
                ScaleOption("0 - Ambos correctas", 0),
                ScaleOption("1 - Una correcta", 1),
                ScaleOption("2 - Ninguna correcta", 2)
            )),
            ScaleDomain("2", "2. Mirada Conjugada", "Evaluar solo los movimientos oculares horizontales.", listOf(
                ScaleOption("0 - Normal", 0),
                ScaleOption("1 - Parálisis parcial de la mirada", 1),
                ScaleOption("2 - Desviación forzada de la mirada o parálisis total", 2)
            )),
            ScaleDomain("3", "3. Campos Visuales", "Confrontación visual bilateral (cuadrantopsia, hemianopsia).", listOf(
                ScaleOption("0 - Sin pérdida visual", 0),
                ScaleOption("1 - Hemianopsia parcial / Asimetría leve", 1),
                ScaleOption("2 - Hemianopsia completa contralateral", 2),
                ScaleOption("3 - Hemianopsia bilateral (ceguera cortical)", 3)
            )),
            ScaleDomain("4", "4. Parálisis Facial", "Mostrar dientes, elevar cejas, cerrar ojos fuerte.", listOf(
                ScaleOption("0 - Movimiento normal simétrico", 0),
                ScaleOption("1 - Parálisis menor (aplanamiento del surco nasogeniano)", 1),
                ScaleOption("2 - Parálisis parcial (parálisis de rama inferior de la cara)", 2),
                ScaleOption("3 - Parálisis completa (compromiso superior e inferior)", 3)
            )),
            ScaleDomain("5a", "5a. Motor de Miembro Superior Izquierdo", "Brazo extendido a 90° (sentado) o 45° (acostado) por 10 segundos.", listOf(
                ScaleOption("0 - Sin caída (mantiene los 10 segundos)", 0),
                ScaleOption("1 - Caída leve (cae antes de 10s pero no toca cama)", 1),
                ScaleOption("2 - Algún esfuerzo contra gravedad (cae a la cama)", 2),
                ScaleOption("3 - Sin esfuerzo contra gravedad (brazo cae/flácido)", 3),
                ScaleOption("4 - Sin movimiento voluntario alguno", 4)
            )),
            ScaleDomain("5b", "5b. Motor de Miembro Superior Derecho", "Brazo contralateral (mismo criterio).", listOf(
                ScaleOption("0 - Sin caída (mantiene los 10 segundos)", 0),
                ScaleOption("1 - Caída leve (cae antes de 10s pero no toca cama)", 1),
                ScaleOption("2 - Algún esfuerzo contra gravedad", 2),
                ScaleOption("3 - Sin esfuerzo contra gravedad", 3),
                ScaleOption("4 - Sin movimiento voluntario alguno", 4)
            )),
            ScaleDomain("6a", "6a. Motor de Miembro Inferior Izquierdo", "Pierna extendida a 30° (acostado boca arriba) por 5 segundos.", listOf(
                ScaleOption("0 - Sin caída (mantiene los 5 segundos)", 0),
                ScaleOption("1 - Caída leve (cae antes de 5s sin tocar la cama)", 1),
                ScaleOption("2 - Algún esfuerzo contra gravedad (cae a la cama antes de 5s)", 2),
                ScaleOption("3 - Sin esfuerzo contra gravedad", 3),
                ScaleOption("4 - Sin movimiento voluntario alguno", 4)
            )),
            ScaleDomain("6b", "6b. Motor de Miembro Inferior Derecho", "Pierna contralateral (mismo criterio).", listOf(
                ScaleOption("0 - Sin caída (mantiene los 5 segundos)", 0),
                ScaleOption("1 - Caída leve (cae antes de 5s)", 1),
                ScaleOption("2 - Algún esfuerzo contra gravedad", 2),
                ScaleOption("3 - Sin esfuerzo contra gravedad", 3),
                ScaleOption("4 - Sin movimiento voluntario alguno", 4)
            )),
            ScaleDomain("7", "7. Ataxia Apendicular", "Prueba índice-nariz y talón-rodilla bilateral.", listOf(
                ScaleOption("0 - Ausente / Normal", 0),
                ScaleOption("1 - Presente en un miembro", 1),
                ScaleOption("2 - Presente en dos o más miembros", 2)
            )),
            ScaleDomain("8", "8. Sensibilidad", "Estímulo con aguja en cara, brazos, tronco y piernas.", listOf(
                ScaleOption("0 - Normal", 0),
                ScaleOption("1 - Pérdida leve a moderada (siente menos filoso/hipoestesia)", 1),
                ScaleOption("2 - Pérdida grave o total (no percibe estímulo doloroso/anestesia)", 2)
            )),
            ScaleDomain("9", "9. Lenguaje (Afasia)", "Describir lámina, nombrar objetos, leer lista de frases.", listOf(
                ScaleOption("0 - Normal (sin afasia)", 0),
                ScaleOption("1 - Afasia leve o moderada (fluidez disminuida, parafasias)", 1),
                ScaleOption("2 - Afasia grave / Broca o Wernicke dominantes (comunicación fallida)", 2),
                ScaleOption("3 - Mudo / Afasia global (mutismo total, sin comprensión)", 3)
            )),
            ScaleDomain("10", "10. Disartria", "Articulación de palabras difíciles.", listOf(
                ScaleOption("0 - Normal", 0),
                ScaleOption("1 - Leve a moderada (arrastra palabras, se entiende con esfuerzo)", 1),
                ScaleOption("2 - Grave / Intubado o incomprensible", 2)
            )),
            ScaleDomain("11", "11. Extinción e Inatención", "Estímulo táctil y visual simultáneo bilateral.", listOf(
                ScaleOption("0 - Sin alteraciones (negativa)", 0),
                ScaleOption("1 - Inatención hemi-espacial parcial (sólo un canal: visual, táctil o auditivo)", 1),
                ScaleOption("2 - Inatención profunda / Negligencia total hemicorporal", 2)
            ))
        )
    )

    val alsfrsr = ScaleMetadata(
        id = "alsfrsr",
        name = "Revised ALS Functional Rating Scale",
        acronym = "ALSFRS-R",
        category = "Motoneurona",
        maxScore = 48,
        description = "Método estandarizado de valoración del impacto funcional de la Esclerosis Lateral Amiotrófica (ELA). Evalúa 12 dominios (Bulbar, Motor Fino, Motor Grueso y Respiratorio). Rango de 0 a 48 (donde 48 es función normal).",
        domains = listOf(
            ScaleDomain("speech", "1. Habla", "", listOf(
                ScaleOption("4 - Normal", 4),
                ScaleOption("3 - Pérdida detectable de articulación", 3),
                ScaleOption("2 - Disartria moderada, inteligible con esfuerzo", 2),
                ScaleOption("1 - Disartria grave, inteligibilidad sumamente disminuida", 1),
                ScaleOption("0 - Pérdida completa de lenguaje oral o mudo", 0)
            )),
            ScaleDomain("salivation", "2. Salivación", "", listOf(
                ScaleOption("4 - Normal", 4),
                ScaleOption("3 - Exceso leve pero evidente, no babeo", 3),
                ScaleOption("2 - Sialorrea moderada, babeo ocasional", 2),
                ScaleOption("1 - Babeo marcado frecuente, requiere pañuelo", 1),
                ScaleOption("0 - Babeo constante, requiere aspiración", 0)
            )),
            ScaleDomain("swallowing", "3. Deglución", "", listOf(
                ScaleOption("4 - Hábitos normales", 4),
                ScaleOption("3 - Problemas menores de atragantamiento, dieta normal", 3),
                ScaleOption("2 - Requiere alimentos semisólidos / adaptaciones", 2),
                ScaleOption("1 - Requiere sonda / PEG de soporte complementario", 1),
                ScaleOption("0 - Nutrición exclusiva por sonda / PEG o NPO", 0)
            )),
            ScaleDomain("handwriting", "4. Escritura", "", listOf(
                ScaleOption("4 - Normal", 4),
                ScaleOption("3 - Lenta o desprolija, pero legible", 3),
                ScaleOption("2 - Incapaz de escribir todas las palabras, algunas legibles", 2),
                ScaleOption("1 - Puede sostener bolígrafo pero incapaz de escribir textos", 1),
                ScaleOption("0 - Incapaz de sostener herramienta o nulo", 0)
            )),
            ScaleDomain("cutting", "5. Cortar comida / manejar cubiertos", "", listOf(
                ScaleOption("4 - Normal", 4),
                ScaleOption("3 - Algo lento / torpe pero independiente", 3),
                ScaleOption("2 - Requiere ayuda para cortar carne, pero come solo", 2),
                ScaleOption("1 - Alimento debe ser cortado por otros, requiere asistencia", 1),
                ScaleOption("0 - Debe ser alimentado totalmente", 0)
            )),
            ScaleDomain("dressing", "6. Vestirse e higiene personal", "", listOf(
                ScaleOption("4 - Normal / Independiente", 4),
                ScaleOption("3 - Lento / mayor esfuerzo pero independiente", 3),
                ScaleOption("2 - Requiere ayuda para botones, cierres, o aseo parcial", 2),
                ScaleOption("1 - Requiere asistencia física constante para vestirse", 1),
                ScaleOption("0 - Dependencia total", 0)
            )),
            ScaleDomain("turning", "7. Dar la vuelta en la cama y ajustar sábanas", "", listOf(
                ScaleOption("4 - Normal / Sin dificultad", 4),
                ScaleOption("3 - Algo lento / requiere gran esfuerzo pero independiente", 3),
                ScaleOption("2 - Puede voltearse pero requiere barra u objetos auxiliares", 2),
                ScaleOption("1 - Incapaz de girar de forma independiente, requiere inicio", 1),
                ScaleOption("0 - Totalmente dependiente para cambios de decúbito", 0)
            )),
            ScaleDomain("walking", "8. Marcha (Caminar)", "", listOf(
                ScaleOption("4 - Normal / Sin límites", 4),
                ScaleOption("3 - Dificultad leve, marcha lenta o claudicante", 3),
                ScaleOption("2 - Requiere asistencia mecánica (bastón, andador)", 2),
                ScaleOption("1 - Capaz de dar unos pasos con ayuda física, silla de ruedas habitual", 1),
                ScaleOption("0 - Postrado en cama o exclusivamente en silla sin propulsion", 0)
            )),
            ScaleDomain("climbing", "9. Subir escaleras", "", listOf(
                ScaleOption("4 - Normal", 4),
                ScaleOption("3 - Lento, requiere baranda", 3),
                ScaleOption("2 - Requiere asistencia física significativa o baranda bilateral", 2),
                ScaleOption("1 - Puede subir unos escalones con apoyo sustancial", 1),
                ScaleOption("0 - Incapaz de subir escaleras", 0)
            )),
            ScaleDomain("dyspnea", "10. Disnea (Dificultad Respiratoria)", "", listOf(
                ScaleOption("4 - Sin disnea", 4),
                ScaleOption("3 - Disnea al caminar o con esfuerzo moderado", 3),
                ScaleOption("2 - Disnea en reposo o con esfuerzos del autocuidado (vestirse)", 2),
                ScaleOption("1 - Disnea persistente que limita toda actividad", 1),
                ScaleOption("0 - Disnea extrema o requiere asistencia", 0)
            )),
            ScaleDomain("orthopnea", "11. Ortopnea", "", listOf(
                ScaleOption("4 - Ninguna", 4),
                ScaleOption("3 - Disnea leve acostado, usa 1-2 almohadas normales", 3),
                ScaleOption("2 - Requiere elevar cabecera >30° o usar muchas almohadas", 2),
                ScaleOption("1 - Incapaz de dormir acostado horizontalmente", 1),
                ScaleOption("0 - Exclusivamente duerme sentado / requiere CPAP/BiPAP inmediato", 0)
            )),
            ScaleDomain("respinsufficiency", "12. Insuficiencia respiratoria", "", listOf(
                ScaleOption("4 - Ninguna", 4),
                ScaleOption("3 - Uso intermitente de BiPAP/CPAP durante el sueño", 3),
                ScaleOption("2 - Uso continuo de BiPAP/CPAP por las noches o diurno intermitente", 2),
                ScaleOption("1 - Soporte ventilatorio no invasivo continuo (24h/día)", 1),
                ScaleOption("0 - Ventilación invasiva por traqueostomía obligada", 0)
            ))
        )
    )

    val qmg = ScaleMetadata(
        id = "qmg",
        name = "Quantitative Myasthenia Gravis Score",
        acronym = "QMG",
        category = "Placa Neuromuscular",
        maxScore = 39,
        description = "Prueba cuantitativa protocolizada para Myasthenia Gravis. Consta de 13 dominios clínicos, puntuados de 0 a 3 según el grado de debilidad o fatiga. Puntuación máxima de 39.",
        domains = listOf(
            ScaleDomain("diplopia", "1. Diplopía (segundos sosteniendo mirada lateral)", "Mirar de lado constantemente hasta que aparezca visión doble.", listOf(
                ScaleOption("0 - No diplopía en 60 segundos", 0),
                ScaleOption("1 - Diplopía en 11-60 segundos", 1),
                ScaleOption("2 - Diplopía en 1-10 segundos", 2),
                ScaleOption("3 - Diplopía inmediata (0 segundos)", 3)
            )),
            ScaleDomain("ptosis", "2. Ptosis (segundos sosteniendo mirada superior)", "Fijar mirada arriba de manera sostenida.", listOf(
                ScaleOption("0 - No ptosis en 60 segundos", 0),
                ScaleOption("1 - Ptosis en 11-60 segundos", 1),
                ScaleOption("2 - Ptosis en 1-10 segundos", 2),
                ScaleOption("3 - Ptosis inmediata (0 segundos)", 3)
            )),
            ScaleDomain("eyelids", "3. Cierre Palpebral", "Cierre ocular forzado contra resistencia manual del examinador.", listOf(
                ScaleOption("0 - Normal (cierre completo imposible de vencer)", 0),
                ScaleOption("1 - Debilidad leve (vence con resistencia moderada)", 1),
                ScaleOption("2 - Debilidad moderada (abre fácilmente con mínima resistencia)", 2),
                ScaleOption("3 - Debilidad severa / Incapaz de ocluir los ojos por completo", 3)
            )),
            ScaleDomain("swallow", "4. Deglución", "Deglutir media taza de agua fría (120 ml).", listOf(
                ScaleOption("0 - Normal, rápido y sin síntomas", 0),
                ScaleOption("1 - Tos leve o carraspeo persistente", 1),
                ScaleOption("2 - Demora marcada en deglución, tos severa o regurgitación", 2),
                ScaleOption("3 - Incapacidad de tragar 120 ml de forma segura", 3)
            )),
            ScaleDomain("speech", "5. Disartria / Fonación", "Contar en voz alta del 1 al 50.", listOf(
                ScaleOption("0 - Sin disartria (voz clara al 50)", 0),
                ScaleOption("1 - Disartria leve en números finales (40-50)", 1),
                ScaleOption("2 - Disartria moderada visible de forma precoz (30-40)", 2),
                ScaleOption("3 - Disartria severa inmediata / Voz nasal disartria ininteligible", 3)
            )),
            ScaleDomain("armR", "6. Abducción Brazo Derecho (segundos)", "Mano extendida lateral a 90° sentado.", listOf(
                ScaleOption("0 - Mantiene 240 segundos", 0),
                ScaleOption("1 - Mantiene entre 90-239 segundos", 1),
                ScaleOption("2 - Mantiene entre 10-89 segundos", 2),
                ScaleOption("3 - Cae en menos de 10 segundos", 3)
            )),
            ScaleDomain("armL", "7. Abducción Brazo Izquierdo (segundos)", "Brazo izquierdo en abducción a 90°.", listOf(
                ScaleOption("0 - Mantiene 240 segundos", 0),
                ScaleOption("1 - Mantiene entre 90-239 segundos", 1),
                ScaleOption("2 - Mantiene entre 10-89 segundos", 2),
                ScaleOption("3 - Cae en menos de 10 segundos", 3)
            )),
            ScaleDomain("fvc", "8. Capacidad Vital Forzada (CVF %)", "Espirometría / Capacidad pulmonar.", listOf(
                ScaleOption("0 - >= 80% del valor de referencia", 0),
                ScaleOption("1 - 65% a 79% del valor de referencia", 1),
                ScaleOption("2 - 50% a 64% del valor de referencia", 2),
                ScaleOption("3 - < 50% de la capacidad esperada", 3)
            )),
            ScaleDomain("neck", "9. Flexión Cervical (segundos)", "Elevación cefálica sostenida a 45° acostado boca arriba.", listOf(
                ScaleOption("0 - Mantiene 120 segundos", 0),
                ScaleOption("1 - Mantiene entre 30-119 segundos", 1),
                ScaleOption("2 - Mantiene entre 1-29 segundos", 2),
                ScaleOption("3 - Incapaz de elevar o cae de inmediato", 3)
            )),
            ScaleDomain("gripR", "10. Prensión Manual Derecha", "Fuerza de prensión del puño (dinamómetro).", listOf(
                ScaleOption("0 - Normal o > 45 kg (u hombres) / > 30 kg (mujeres)", 0),
                ScaleOption("1 - Leve: 15-44 kg (hombres) / 10-29 kg (mujeres)", 1),
                ScaleOption("2 - Moderada: 5-14 kg (hombres) / 5-9 kg (mujeres)", 2),
                ScaleOption("3 - Severa: < 5 kg o no genera fuerza", 3)
            )),
            ScaleDomain("gripL", "11. Prensión Manual Izquierda", "Fuerza de prensión puño izquierdo.", listOf(
                ScaleOption("0 - Normal", 0),
                ScaleOption("1 - Leve disminución", 1),
                ScaleOption("2 - Moderada disminución", 2),
                ScaleOption("3 - Severa o nula", 3)
            )),
            ScaleDomain("legR", "12. Elevación de Pierna Derecha (segundos)", "Elevación de miembro inferior a 45° acostado boca arriba.", listOf(
                ScaleOption("0 - Mantiene 100 segundos", 0),
                ScaleOption("1 - Mantiene entre 31-99 segundos", 1),
                ScaleOption("2 - Mantiene entre 1-30 segundos", 2),
                ScaleOption("3 - Cae en menos de 1 segundo", 3)
            )),
            ScaleDomain("legL", "13. Elevación de Pierna Izquierda (segundos)", "Mismo criterio en pierna izquierda.", listOf(
                ScaleOption("0 - Mantiene 100 segundos", 0),
                ScaleOption("1 - Mantiene entre 31-99 segundos", 1),
                ScaleOption("2 - Mantiene entre 1-30 segundos", 2),
                ScaleOption("3 - Cae en menos de 1 segundo", 3)
            ))
        )
    )

    val dragon = ScaleMetadata(
        id = "dragon",
        name = "DRAGON Score for Acute Ischemic Stroke",
        acronym = "DRAGON",
        category = "Vasco-vascular / Pronóstico",
        maxScore = 10,
        description = "Escala predictora de pronóstico clínico a 3 meses en pacientes con ACV isquémico agudo sometidos a fibrinólisis intravenosa. Identifica la probabilidad de desenlace funcional favorable (mRS 0-1) vs. hemorragia sintomática.",
        domains = listOf(
            ScaleDomain("decades", "D: Decades of Age (Edad)", "Edad del paciente en décadas.", listOf(
                ScaleOption("< 65 años (0 puntos)", 0),
                ScaleOption("65 - 80 años (1 punto)", 1),
                ScaleOption("> 80 años (2 puntos)", 2)
            )),
            ScaleDomain("rankin", "R: baseline mRS >1 (Rankin Previo)", "Discapacidad neurológica preexistente antes del evento agudo.", listOf(
                ScaleOption("mRS previo <= 1 (No discapacidad / 0 puntos)", 0),
                ScaleOption("mRS previo > 1 (Discapacidad previa / 1 punto)", 1)
            )),
            ScaleDomain("glucose", "G: Glucose (Glucemia al ingreso)", "Nivel de glicemia en laboratorios de urgencias.", listOf(
                ScaleOption("<= 144 mg/dL / <= 8.0 mmol/L (0 puntos)", 0),
                ScaleOption("> 144 mg/dL / > 8.0 mmol/L (1 punto)", 1)
            )),
            ScaleDomain("onset", "O: Onset-to-treatment time", "Tiempo transcurrido desde inicio de síntomas hasta bolo de trombolisis.", listOf(
                ScaleOption("<= 90 minutos de evolución (0 puntos)", 0),
                ScaleOption("> 90 minutos de evolución (1 punto)", 1)
            )),
            ScaleDomain("nihss", "N: NIHSS al ingreso", "Puntuación de ingreso en la escala NIHSS.", listOf(
                ScaleOption("NIHSS de 0 a 4 (0 puntos)", 0),
                ScaleOption("NIHSS de 5 a 9 (1 punto)", 1),
                ScaleOption("NIHSS de 10 a 15 (2 puntos)", 2),
                ScaleOption("NIHSS > 15 (3 puntos)", 3)
            ))
        )
    )

    // Helper list of all scales
    val allScales = listOf(nihss, alsfrsr, qmg, dragon)

    // 2. Classifications and Criteria
    val goldCoastEla = DiagnosticCriteria(
        id = "gold_coast_als",
        name = "Criterios de Gold Coast para Esclerosis Lateral Amiotrófica (ELA)",
        acronym = "Gold Coast ELA",
        year = "2020",
        description = "Reemplazo unificado y simplificado de los anteriores criterios clínicos de El Escorial y Awaji. Se diseñaron para incrementar la sensibilidad diagnóstica sin perder especificidad en la práctica clínica y ensayos.",
        sections = listOf(
            DiagnosticCriteriaSection(
                "Criterios de Inclusión Obligatorios (Ambos requeridos)",
                listOf(
                    "Evidencia de disfunción progresiva de Motoneurona Superior (MNS) e Inferior (MNI) en al menos UNA región anatómica (Bulbar, Cervical, Torácica, Lumbosacra) OR disfunción aislada de MNI en al menos DOS regiones anatómicas.",
                    "Ausencia de otros procesos patológicos alternativos que puedan explicar razonablemente las manifestaciones de MNS y MNI (documentado mediante estudio de electrodiagnóstico y neuroimagen adecuada)."
                )
            )
        )
    )

    val miopatiasInflamatorias = DiagnosticCriteria(
        id = "miopatias_eular",
        name = "Criterios EULAR/ACR para Miopatías Inflamatorias Idiopáticas",
        acronym = "EULAR/ACR 2017",
        year = "2017",
        description = "Clasificación probabilística basada en variables de debilidad muscular, lesiones cutáneas patognomónicas, laboratorios de enzimas musculares, hallazgos de electromiografía y biopsia muscular, con alto rigor metodológico.",
        sections = listOf(
            DiagnosticCriteriaSection(
                "Presentación y Criterio General",
                listOf(
                    "Debilidad simétrica proximal de miembros superiores e inferiores, usualmente de curso subagudo.",
                    "Elevación notable de enzimas musculares séricas (Creatina Quinasa - CK, Aldolasa, LDH, AST/ALT).",
                    "Estudios complementarios de soporte (patrón miopático irritable en EMG, edema en RMN muscular T2/STIR)."
                )
            )
        ),
        subclassifications = listOf(
            DiagnosticSubclass(
                name = "Dermatomiositis",
                acronym = "DM",
                criteria = "Presencia de debilidad muscular proximal asociada a manifestaciones cutáneas patognomónicas u obligadas.",
                clinicalPoints = listOf(
                    "Erupción o Rash Heliotropo (eritema purpúreo simétrico violáceo bipalpebral, con o sin edema).",
                    "Pápulas de Gottron (pápulas descamativas violáceas sobreelevadas en el dorso de articulaciones metacarpofalángicas o interfalángicas).",
                    "Signo de Gottron (eritema macular liso en superficies de extensión de codos o rodillas).",
                    "Signo del Chal / Signo del V (eritema fotosensible en dorso y tórax anterior)."
                )
            ),
            DiagnosticSubclass(
                name = "Síndrome Antisintetasa",
                acronym = "ASyS",
                criteria = "Entidad caracterizada por la presencia de anticuerpos específicos dirigidos contra las ARNt sintetasas (ej. anti-Jo-1, anti-PL-7, anti-PL-12, anti-EJ, anti-OJ).",
                clinicalPoints = listOf(
                    "Miositis inflamatoria activa (clínica o subclínica).",
                    "Enfermedad Pulmonar Intersticial Difusa (EPID / EPI) de compromiso severo o progresivo.",
                    "Artritis simétrica no erosiva de pequeñas articulaciones.",
                    "Manos de mecánico / Hiperqueratosis subungueal y fisuras laterales dolorosas.",
                    "Fenómeno de Raynaud y fiebre persistente inexplicable."
                )
            ),
            DiagnosticSubclass(
                name = "Miopatía Necrotizante Inmunomediada",
                acronym = "MNIM",
                criteria = "Compromiso muscular muy severo con desproporción clínico-histopatológica caracterizado por necrosis muscular.",
                clinicalPoints = listOf(
                    "Instauración rápida de debilidad muscular grave con valores extremadamente elevados de CK (>10-50 veces normal).",
                    "Biopsia muscular revela necrosis de fibras musculares y regeneración prominente, con escaso o nulo infiltrado inflamatorio primario.",
                    "Fuerte asociación con autoanticuerpos específicos: anti-HMGCR (asociado a exposición previa a estatinas) o anti-SRP."
                )
            ),
            DiagnosticSubclass(
                name = "Miositis por Cuerpos de Inclusión",
                acronym = "MCI",
                criteria = "Enfermedad muscular inflamatoria de curso crónico y lento, refractaria a inmunoterapia clásica, típicamente del adulto mayor.",
                clinicalPoints = listOf(
                    "Edad de inicio típicamente superior a 50 años, con curso clínico insidioso y asimétrico.",
                    "Debilidad de predominio distal en miembros superiores: compromiso selectivo de flexores profundos de los dedos de la mano (dificultad para hacer pinza o fuerza de agarre).",
                    "Debilidad proximal de miembros inferiores con atrofia selectiva marcada del músculo cuádriceps femoral (caídas frecuentes por fallo de rodilla).",
                    "Biopsia muscular revela vacuolas ribeteadas (rimmed vacuoles) en el citoplasma celular y depósitos de amiloide."
                )
            )
        )
    )

    val toastAcv = DiagnosticCriteria(
        id = "toast",
        name = "Clasificación de TOAST para subtipos de ACV Isquémico Agudo",
        acronym = "TOAST",
        year = "1993",
        description = "Esquema internacionalmente aceptado y prioritario en neurología para subclasificar los infartos cerebrales según su etiología presunta. Orienta la terapia de prevención secundaria de forma definitiva.",
        sections = listOf(
            DiagnosticCriteriaSection(
                "Subtipos Etiológicos TOAST",
                listOf(
                    "1. Ateroesclerosis de grandes arterias (Gran Vaso): Estenosis oclusiva mayor a 50% o placa significativa en arteria carótida interna, vertebral o cerebral media ipsilateral. Infarto típicamente cortical o cerebeloso > 1.5 cm.",
                    "2. Cardioembolismo (Cardioembólico): Presencia de fuentes cardíacas de alto riesgo (Fibrilación Auricular, prótesis valvular cardíaca mecánica, estenosis mitral, trombo o mixoma en AI, IAM en los últimos 3 meses) o riesgo medio (foramen oval permeable, prolapso mitral con vegetación).",
                    "3. Oclusión de pequeños vasos arteriales (Lacunar / Enfermedad de pequeño vaso): Síndrome clínico lacunar clásico (hemiparesia motora pura, síndrome sensitivo puro, ataxia-hemiparesia, disartria mano torpe) con lesión única lacunar profunda de diámetro menor a 1.5 cm (15 mm). Sin estenosis ipsilateral >50% ni focos cardíacos embolígenos.",
                    "4. ACV de otra etiología determinada (Otras Causas): Infarto secundario a vasculopatías no ateroescleróticas (disección arterial, displasia fibromuscular, vasculitis del SNC, moyamoya) o estados protrombóticos / hipercoagulabilidad molecular (mutación factor V Leiden, SAF primario).",
                    "5. ACV de etiología indeterminada (Indeterminado): No es posible emitir diagnóstico definitivo por: a) Evaluación incompleta; b) Coexistencia de dos o más causas altamente probables (ej. FA + estenosis carotídea >70% ipsilateral); c) Estudios completos negativos (criptogénico)."
                )
            )
        )
    )

    val epilepsyClassification = DiagnosticCriteria(
        id = "ilae_epilepsy",
        name = "Clasificación Actualizada de Crisis y Epilepsias ILAE 2025/2026",
        acronym = "ILAE 2025",
        year = "2025",
        description = "Marco de clasificación actualizado de la Liga Internacional Contra la Epilepsia (ILAE) (Beniczky et al., 2025). Reemplaza la terminología de 2017 por un enfoque basado en reglas taxonómicas estrictas (Clasificadores vs Descriptores) y restaura el término universal 'Consciencia' (Consciousness).",
        sections = listOf(
            DiagnosticCriteriaSection(
                "1. Clases Principales y Clasificadores (ILAE 2025)",
                listOf(
                    "A. Crisis Focal: Se origina dentro de redes unilaterales limitadas a un hemisferio. Se clasifica según CONSCIENCIA (operacionalizada mediante recuerdo y capacidad de respuesta) en:\n" +
                        "   - 1.1. Crisis focal con preservación de la consciencia (CFCPC)\n" +
                        "   - 1.2. Crisis focal con alteración de la consciencia (CFCAC) [Califica por definición como manifestación observable]\n" +
                        "   - 1.3. Crisis tónico-clónica focal a bilateral (CFTCB) [La rigidez y sacudida son bilaterales, con pérdida de consciencia]",
                    "B. Crisis Generalizada: Origen en redes bilaterales distribuidas rápidamente. Subgrupos principales:\n" +
                        "   - 3.1. Crisis de Ausencia (Elimina término 'no motora' por fenómenos motores discretos): Típica, Atípica, Mioclónica, Mioclonía palpebral con/sin ausencia.\n" +
                        "   - 3.2. Crisis tónico-clónica generalizada (CTCG): Con sacudidas mioclónicas o ausencia al inicio, o término principal autónomo.\n" +
                        "   - 3.3. Otras crisis generalizadas: Mioclónica, Clónica, Tónica, Atónica, Mioclónica-atónica, Espasmo epiléptico generalizado, y MIOCLONUS NEGATIVO GENERALIZADO (MNG) [Admitido como tipo de crisis diferenciado en 2025].",
                    "C. Se desconoce si es focal o generalizada: Casos con información incompleta al inicio, clasificada en:\n" +
                        "   - 2.1. Con preservación de la conciencia (PC)\n" +
                        "   - 2.2. Con alteración de la conciencia (AC)\n" +
                        "   - 2.3. Crisis tónico-clónica bilateral (TCB)",
                    "D. No clasificable: Información insuficiente por completo (ej. sin testigos ni registros de video/EEG)."
                )
            ),
            DiagnosticCriteriaSection(
                "2. Descriptores de Semiología (Básicos y Extendidos)",
                listOf(
                    "• Descriptores Básicos: Se describe dicotómicamente si la crisis es 'Con manifestaciones observables' o 'Sin manifestaciones observables'. (La afasia o el rubor son observables).",
                    "• Descriptores Extendidos (Evoluciónd de Semiología): Se describe la secuencia cronológica de los fenómenos ictales (ej. aura epigástrica -> automatismo gestual -> consciencia alterada). Evita depender solo del signo inicial.",
                    "• Fenómenos Motores Elementales: Acinético, atónico, clónico, distónico, nistagmus, espasmo, parpadeo, desv. cefálica, mioclónico, mioclonus negativo, tónico, versiva.",
                    "• Fenómenos Motores Complejos: Automatismos (distales o proximales, gestuales, de mímica, oroalimentarios, verbales, vocales), comportamiento hipercinético/motor.",
                    "• Fenómenos Sensoriales y Cognitivos: Auditivos, visuales, somatosensoriales, de lenguaje (afasia), confusión, dismnesia, déjà vu/jamais vu, afectivos (ansiedad, enojo, éxtasis, culpa)."
                )
            ),
            DiagnosticCriteriaSection(
                "3. Cambios Clave: Versión 2017 vs Actualización 2025",
                listOf(
                    "1. Eliminación del término 'Inicio' (Onset) de las clases principales: Se reduce redundancia puesto que crisis generalizadas pueden tener inicio focal microscópico.",
                    "2. Regla Taxonómica Estricta: Clasificadores (clases biológicas con impacto terapéutico como consciencia, tipo de crisis, clase principal) vs Descriptores (características clínicas que orientan manejo pero no definen tipo, ej. semiología).",
                    "3. De 'Awareness/Alerta' a 'Consciencia' (Consciousness): Término universalmente aceptado y más traducible. Definido operacionalmente por la capacidad de recordar (awareness) y responder.",
                    "4. De 'Motor/No motor' en Focales a 'Manifestaciones Observables vs No Observables' en versión básica.",
                    "5. Reconocimiento del Mioclonus Negativo Epiléptico Generalizado como tipo de crisis autónomo (antes omitido).",
                    "6. Espasmos epilépticos se consideran tipo de crisis en generalizadas, pero descriptor semiológico en focales/desconocidas."
                )
            )
        )
    )

    val allCriteria = listOf(goldCoastEla, miopatiasInflamatorias, toastAcv, epilepsyClassification)

    // 3. Pharmacological Reference
    data class DrugReference(
        val name: String,
        val acronym: String,
        val indications: String,
        val dosage: String,
        val sideEffects: String,
        val clinicalNotes: String,
        val category: String = "Reperfusión y Antiagregantes"
    )

    val drugs = listOf(
        DrugReference(
            name = "Carbamazepina",
            acronym = "CBZ",
            indications = "Crisis de inicio focal y tónico-clónicas generalizadas primarias. No es de elección en Ausencias o Mioclónicas (puede agravarlas).",
            dosage = "Adultos: Inicio 100–200 mg 1–2 veces/día, incrementos de 100–200 mg cada 3–7 días. Mantenimiento: 800–1200 mg/día en 2–4 dosis fraccionadas. Niños: ~10–20 mg/kg/día.",
            sideEffects = "Diplopía, ataxia, somnolencia, hiponatremia, leucopenia transitoria o persistente, hepatitis, SJS/TEN (riesgo severo con alelo HLA-B*1502).",
            clinicalNotes = "Autoinducción enzimática pronunciada. Potente inductor CYP3A4/1A2/2C9/UGT. Monitorización sérica (TDM) ideal: 4–12 µg/mL. Evitar en gestantes (Categoría D por defectos de tubo neural).",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Oxcarbazepina",
            acronym = "OXC",
            indications = "Monoterapia y adyuvancia en crisis focales con o sin generalización secundaria. No es de elección en Ausencias o Mioclónicas.",
            dosage = "Adultos: Inicio 300 mg BID, incrementando 600 mg/día cada semana hasta mantenimiento de 600–1200 mg BID (máximo 2400 mg/día). Niños: 8–10 mg/kg/día en 2 dosis.",
            sideEffects = "Mareo, diplopía, náuseas, somnolencia, hiponatremia marcada/SIADH (especialmente en ancianos), reacciones cutáneas.",
            clinicalNotes = "Menor inducción metabólica que Carbamazepina. Induce CYP3A4/UGT e inhibe CYP2C19. Ajustar si el aclaramiento de creatinina es <30 mL/min (iniciar al 50%). TDM orientativo: MHD 10–35 µg/mL.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Eslicarbazepina acetato",
            acronym = "ESL",
            indications = "Tratamiento de crisis de inicio focal con o sin generalización secundaria, principalmente en adultos.",
            dosage = "Adultos: Inicio 400 mg QD x 1–2 semanas; avanzar a dosis de mantenimiento usual de 800–1600 mg una vez al día (QD).",
            sideEffects = "Mareo, diplopía, somnolencia, cefalea, hiponatremia, rash cutáneo.",
            clinicalNotes = "Carboxamida de 3ª generación. Inhibe CYP2C19, induce moderadamente CYP3A4. Reducir o ajustar dosis si la depuración de creatinina es <=50 mL/min.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Fenitoína",
            acronym = "PHT",
            indications = "Crisis focales y tónico-clónicas generalizadas. Contraindicada en Ausencias y Mioclónicas (puede empeorarlas).",
            dosage = "Oral: Adultos 100 mg TID; ped 5 mg/kg/día en 2-3 tomas. Mantenimiento: 300–400 mg/día. Carga IV: 15–20 mg/kg en SE, velocidad máxima de infusión 50 mg/min (25 mg/min en ancianos/cardiópatas).",
            sideEffects = "Agudos: Nistagmo, ataxia, letargo, diplopía, disartria. Crónicos: Hiperplasia gingival, hirsutismo, neuropatía, osteopenia, hepatotoxicidad, SJS/TEN.",
            clinicalNotes = "Cinética no lineal y saturable (pequeños cambios de dosis provocan grandes saltos séricos). Unión proteica alta (medir nivel libre en hipoalbuminemia/nefropatía). TDM: total 10–20 µg/mL, libre 1–2 µg/mL.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Fosfenitoína",
            acronym = "fPHT",
            indications = "Tratamiento de crisis convulsivas agudas y estado de mal epiléptico (SE) convulsivo. Profármaco soluble para uso IV/IM.",
            dosage = "Carga: 15–20 mg equivalentes de fenitoína (PE)/kg IV o IM, infundida a velocidad máxima de 150 mg PE/min.",
            sideEffects = "Parestesias transitorias generalizadas, prurito pélvico/facial durante la infusión rápida, hipotensión, arritmias cardíacas leves.",
            clinicalNotes = "Disminuye el riesgo de necrosis tisular local ('síndrome del guantelete morado/purple glove') comparada con fenitoína IV clásica. Mismos rangos séricos terapéuticos tras su conversión.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Lamotrigina",
            acronym = "LTG",
            indications = "Espectro amplio: crisis focales, tónico-clónicas primarias (PGTCS), ausencias, y crisis asociadas al Síndrome de Lennox-Gastaut (LGS). Puede agravar Mioclónicas.",
            dosage = "Sin interactores: 25 mg/día x 2 sem, luego 50 mg/día x 2 sem, luego aumentar 50-100 mg cada 1-2 sem. C/ Valproato (duplica vida media): 25 mg días alternos x 2 sem, luego 25 mg/día x 2 sem. Mantenimiento: 100-200 mg/día (c/ VPA) o 300-500 mg/día (c/ inductores).",
            sideEffects = "Rash cutáneo benigno frecuente, pero riesgo crítico de reacción severa (SJS, TEN, DRESS) si se titula rápido. Diplopía, insomnio, cefalea, meningitis aséptica raras.",
            clinicalNotes = "Metabolismo vía UGT1A4. El Valproato frena su aclaramiento drásticamente. Teratogenicidad relativamente baja en comparación con otros FAC tradicionales. TDM: 3–15 µg/mL.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Lacosamida",
            acronym = "LCM",
            indications = "Crisis de inicio focal con o sin generalización secundaria (monoterapia/adyuvancia) y coadyuvante en PGTCS en mayores de 4 años.",
            dosage = "Monoterapia: 100 mg BID inicial. Coadyuvante: 50 mg BID, incrementos semanales de 50 mg BID hasta mantenimiento habitual de 100–200 mg BID. Carga oral/IV rápida: 200 mg única.",
            sideEffects = "Mareo, diplopía, cefalea, náuseas, prolongación leve del intervalo PR en ECG, síncope raro, DRESS.",
            clinicalNotes = "Favorece selectivamente la inactivación lenta de canales de sodio. Pocas interacciones farmacológicas. Obtener ECG basal en pacientes con cardiopatía de conducción.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Rufinamida",
            acronym = "RUF",
            indications = "Tratamiento coadyuvante de crisis asociadas al Síndrome de Lennox-Gastaut (LGS) y crisis focales refractarias.",
            dosage = "Adultos: Inicio 400–800 mg/día en dos tomas con alimentos, duplicando dosis cada 2 días hasta mantenimiento de 3200 mg/día. Niños: Inicio 10 mg/kg/día.",
            sideEffects = "Somnolencia, náuseas y vómitos, cefalea, cansancio, acortamiento del intervalo QT (contraindicación en síndrome de QT corto familiar).",
            clinicalNotes = "Metabolizada por hidrólisis (no CYP dominante). El valproato aumenta significativamente sus concentraciones en pediatría, obligando a usar dosis de rufinamida menores. TDM: 15–30 µg/mL ordinario.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Cenobamato",
            acronym = "CNB",
            indications = "Tratamiento de crisis de inicio focal no controladas en pacientes adultos (novedoso).",
            dosage = "Inicio obligatorio ultra-lento: 12.5 mg QD x 2 sem, luego 25 mg QD x 2 sem, 50 mg QD x 2 sem, 100 mg QD x 2 sem, 150 mg QD x 2 sem, hasta mantenimiento de 200–400 mg QD.",
            sideEffects = "Somnolencia, fatiga extrema, mareo, acortamiento del intervalo QT, DRESS mortal si la titulación es acelerada.",
            clinicalNotes = "Bloquea corriente persistente de sodio y modula alostéricamente GABA_A. Fuerte potencial de DDI: inhibe CYP2C19 (duplica niveles de fenitoína) y estimula CYP3A4/2B6.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Topiramato",
            acronym = "TPM",
            indications = "Crisis focales, tónico-clónicas generalizadas (PGTCS), Lennox-Gastaut (LGS), mioclonías y profilaxis de migraña.",
            dosage = "Adultos: Inicio 25–50 mg/noche, incrementos semanales de 25–50 mg/día. Mantenimiento usual: 100–400 mg/día divididos en 2 tomas.",
            sideEffects = "Parestesias distales, lentitud psicomotora, disfasia/anomia ('dopamato'), pérdida de peso marcada, nefrolitiasis, acidosis metabólica leve, glaucoma de ángulo cerrado.",
            clinicalNotes = "Acción multimecanística (Na+, GABA_A, AMPA, anhidrasa carbónica débil). Si CrCl es <70 mL/min, reducir dosis un 50%. A dosis >=200 mg/día reduce efectividad de anticonceptivos. TDM: 5–20 µg/mL.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Valproato / Ácido Valproico / Divalproato",
            acronym = "VPA",
            indications = "Espectro amplio (el más versátil): crisis focales, generalizadas convulsivas (PGTCS), mioclónicas, ausencias, Lennox-Gastaut, West de rescate.",
            dosage = "Oral: 10–15 mg/kg/día progresivo, mantenimiento habitual 15–60 mg/kg/día (típicamente 500–2500 mg/día). Carga SE IV: 20–40 mg/kg a velocidad de 3–6 mg/kg/min.",
            sideEffects = "Aumento de peso, temblor fino de manos, alopecia parcial transitoria, trombocitopenia dosis-dependiente, hiperamonemia (incluso con PFH normales), pancreatitis aguda grave, teratogénesis.",
            clinicalNotes = "CONTRAINDICADO EN MUJERES EN EDAD FÉRTIL (salvo ausencia total de alternativas, por alto riesgo teratógeno y del neurodesarrollo). Potente inhibidor enzimático (UGT, CYP2C9). TDM: 50–100 µg/mL.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Zonisamida",
            acronym = "ZNS",
            indications = "Tratamiento coadyuvante de crisis focales en adultos y niños, con utilidad en mioclonías y LGS en segunda línea.",
            dosage = "Inicio 100 mg/día QD, incrementando a 200 mg/día tras 2 semanas, luego saltos de 100 mg cada 2 semanas según tolerancia. Mantenimiento: 100–600 mg/día.",
            sideEffects = "Somnolencia, anorexia marcada con pérdida de peso, nefrolitiasis, acidosis metabólica renal, anhidrosis/oligohidrosis con hipertermia en pediatría, ideación suicida.",
            clinicalNotes = "Sulfonamida de espectro amplio (canales Na+, Ca2+ tipo T, y carbónica). Evitar si existe hipersensibilidad grave a sulfas o insuficiencia renal sustancial. TDM: 10–40 µg/mL.",
            category = "Bloqueadores de Sodio y Espectro Amplio"
        ),
        DrugReference(
            name = "Levetiracetam",
            acronym = "LEV",
            indications = "Primerísima línea en crisis focales, generalizadas tónico-clónicas (PGTCS) y mioclónicas (incluyendo JME).",
            dosage = "Adultos: Inicio 500 mg BID, avanzar 500 mg BID cada 2 semanas hasta mantenimiento habitual de 1000–3000 mg/día en dos dosis. Carga SE IV: 60 mg/kg (máximo 4500 mg).",
            sideEffects = "Somnolencia, mareos, fatiga física y, críticamente, efectos psiquiátricos/comportamentales: irritabilidad, hostilidad, ansiedad, depresión o ideación suicida.",
            clinicalNotes = "Une selectivamente la glicoproteína de vesícula sináptica SV2A. Cero metabolismo hepático (excelente frente a interacciones). Ajuste mandatorio según función renal. TDM: 12–46 µg/mL.",
            category = "Ligandos SV2 y Moduladores"
        ),
        DrugReference(
            name = "Brivaracetam",
            acronym = "BRV",
            indications = "Tratamiento en monoterapia y coadyuvante de crisis de inicio focal en adultos y niños a partir de 1 mes de edad.",
            dosage = "Inicio estándar cómodo: 50 mg BID, ajustable de inmediato a 25 mg BID o 100 mg BID según la eficacia individual. Rango de mantenimiento de 50–200 mg/día.",
            sideEffects = "Somnolencia, mareos, fatiga, náuseas. Menor incidencia de trastornos psiquiátricos/irritabilidad en promedio respecto a levetiracetam.",
            clinicalNotes = "Ligando SV2A de afinidad 20 veces superior al levetiracetam. Metabolismo por hidrólisis y CYP2C19. En insuficiencia hepática, limitar dosis máxima a 150 mg/día.",
            category = "Ligandos SV2 y Moduladores"
        ),
        DrugReference(
            name = "Padsevonil",
            acronym = "PAD",
            indications = "Fármaco investigacional/experimental de vanguardia, enfocado en epilepsia focal farmacorresistente.",
            dosage = "Sin posología clínica aprobada formalmente. Regímenes explorados en ensayos clínicos fase II/III por vía oral BID.",
            sideEffects = "Somnolencia, mareos, cefalea, cansancio generalizado.",
            clinicalNotes = "Diseñado para interactuar con alta afinidad terapéutica simultánea sobre SV2A, SV2B y SV2C. Sin disponibilidad en canales comerciales abiertos en Colombia.",
            category = "Ligandos SV2 y Moduladores"
        ),
        DrugReference(
            name = "Fenobarbital",
            acronym = "PB",
            indications = "Crisis focales, tónico-clónicas primarias, convulsiones neonatales recurrentes y estado de mal epiléptico (SE). Contraindicado en Ausencias.",
            dosage = "Adultos 1–3 mg/kg/día (típicamente 100–150 mg QD HS). Niños: 3–6 mg/kg/día. Carga IV en SE: 15–20 mg/kg a velocidad menor a 100 mg/min.",
            sideEffects = "Sedación persistente severa, embotamiento cognitivo, dependencia física marcada, depresión, excitación psicomotriz paradójica en niños y ancianos, osteopenia acelerada.",
            clinicalNotes = "Barbitúrico clásico. Potente inductor de múltiples vías citocromo/UGT (CYP3A4, 2C9). Vida media extremadamente larga (hasta 120 h). TDM: 15–40 µg/mL.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Primidona",
            acronym = "PRM",
            indications = "Crisis focales y generalizadas tónico-clónicas. Alternativa secundaria en temblor esencial familiar.",
            dosage = "Inicio: 100–125 mg HS, incrementando lentamente cada 3–7 días según respuesta hasta mantenimiento usual de 750–1500 mg/día fraccionado en 3 dosis.",
            sideEffects = "Gran sedación y torpeza motora de inicio, náuseas intensas, ataxia, depresión, reacciones cutáneas de hipersensibilidad.",
            clinicalNotes = "Se metaboliza de forma activa en fenobarbital y PEMA, heredando el perfil inductor potente e interacciones complejas. TDM: Primidona 5–12 µg/mL, Fenobarbital 15–40 µg/mL.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Clobazam",
            acronym = "CLB",
            indications = "Tratamiento coadyuvante sistémico de crisis en Síndrome de Lennox-Gastaut (LGS), y crisis focales refractarias. Útil pero limitado por tolerancia.",
            dosage = "Pacientes >30 kg: Inicio 10 mg/día en 1–2 tomas, escalar semanalmente hasta mantenimiento usual de 20–40 mg/día. Niños <30 kg: Inicio 5 mg/día.",
            sideEffects = "Somnolencia diurna, sedación, sialorrea o babeo marcado en pediatría, irritabilidad paradójica, riesgo de tolerancia y síndrome de abstinencia.",
            clinicalNotes = "Benzodiazepina 1,5 (menor efecto sedante clásico). Su metabolito activo (N-desmetilclobazam) se eleva masivamente en coincidencia con Cannabidiol o Stiripentol. TDM: 30-300 ng/mL.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Clonazepam",
            acronym = "CZP",
            indications = "Crisis mioclónicas, ausencias atípicas, crisis atónicas o tónicas asociadas a síndromes neurológicos diversos.",
            dosage = "Adulto: 0.25–0.5 mg/día inicial, ascenso progresivo hasta mantenimiento de 1–4 mg/día. Niños: 0.01–0.03 mg/kg/día.",
            sideEffects = "Somnolencia profunda, hipotonía muscular notable, hiperactividad paradójica en pediatría, retraso motor, tolerancia progresiva (pierde eficacia con los meses).",
            clinicalNotes = "Moderado modulador positivo de GABA_A. No debe retirarse abruptamente por alto potencial de status epiléptico de rebote. TDM orientativa: 20–70 ng/mL.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Clorazepato dipotásico",
            acronym = "CZP_D",
            indications = "Tratamiento adyuvante histórico o de soporte para crisis de inicio focal y tónico-clónicas. Uso residual en la práctica actual.",
            dosage = "Rango habitual: 15–60 mg/día divididos en 2 o 3 tomas diarias por vía oral.",
            sideEffects = "Somnolencia, torpeza motora o ataxia de tronco, dependencia a benzodiazepinas, amnesia anterógrada transitoria.",
            clinicalNotes = "Profármaco de nordiazepam. Suma efectos sedantes severos si se combina libremente con otros supresores del SNC.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Diazepam",
            acronym = "DZP",
            indications = "Fármaco de rescate agudo para frenar crisis prolongadas, crisis en racimo (clusters) y estado de mal epiléptico. No apto para uso crónico.",
            dosage = "Carga IV: 0.15–0.2 mg/kg bolo directo (dosis habitual adultos 10–20 mg) administrado a velocidad lenta <=5 mg/min. Rectal: 0.2–0.5 mg/kg. Nasal: 5–20 mg.",
            sideEffects = "Sedación post-crisis, depresión respiratoria dosis-dependiente, hipotensión transitoria, riesgo de flebitis endotelial en inyección venosa rápida.",
            clinicalNotes = "Sustrato CYP3A4/2C19 con metabolitos activos de vida media sumamente prolongada. Alta liposubilidad con rápida penetración al SNC y posterior redistribución.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Lorazepam",
            acronym = "LZP",
            indications = "Tratamiento de primera línea hospitalario de elección para estado epiléptico (SE) convulsivo y de rescate de urgencia.",
            dosage = "Estado Epiléptico IV: 0.1 mg/kg administrados a velocidad estándar (típicamente bolo de 4 mg para adultos), reproducible una vez a los 5–10 minutos.",
            sideEffects = "Sedación, depresión respiratoria, hipotensión arterial, acidosis láctica infrecuente si se utiliza el vehículo solvente (propilenglicol) en infusiones continuas.",
            clinicalNotes = "Menos liposoluble que el diazepam (permanece más tiempo fijado en el SNC con menor redistribución rápida). Metabolismo por glucuronidación directa.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Midazolam",
            acronym = "MDZ",
            indications = "Rescate prehospitalario (IM, intranasal o bucal) en crisis complejas prolongadas y componente medular del control del SE refractario en UCI.",
            dosage = "Rescate Extrahospitalario (IM): 10 mg dosis única en adultos (o 0.2 mg/kg en pediatría). En SE refractario: Infusión IV continua de mantenimiento ajustada a ráfaga-supresión en EEG.",
            sideEffects = "Depresión respiratoria acentuada en bolos rápidos, shock circulatorio transitorio, somnolencia post-rescate marcada, amnesia retrógrada temporal.",
            clinicalNotes = "Acción ultra-corta. Elevado metabolismo hepático (CYP3A4) susceptible de mermar o disparar sus concentraciones ante bloqueadores enzimáticos.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Vigabatrina",
            acronym = "VGB",
            indications = "Tratamiento de primera línea prioritario para Espasmos Infantiles (Síndrome de West) y crisis focales refractarias del adulto.",
            dosage = "Espasmos Infantiles: 50 mg/kg/día inicial, buscando objetivo clínico de 100–150 mg/kg/día bajo estrecha vigilancia. Adultos: 1–3 g/día.",
            sideEffects = "Riesgo crítico definitivo: Pérdida permanente del campo visual concéntrico bilateral sin alteración inicial de agudeza central (~30-50% de uso crónico).",
            clinicalNotes = "Inhibidor irreversible de la GABA-transaminasa. Requiere evaluación campimétrica oftalmológica rigurosa y consentimiento informado firmado. TDM no útil.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Tiagabina",
            acronym = "TGB",
            indications = "Tratamiento adyuvante secundario para crisis de inicio focal refractarias. Contraindicada en crisis generalizadas primarias.",
            dosage = "Adulto: Inicio de 4 mg/día orales, duplicando la dosificación cada semana según la tolerancia hasta mantenimiento de 12–56 mg/día en tomas.",
            sideEffects = "Mareo postural, temblor fino periférico, confusión mental, dificultad transitoria del habla, somnolencia leve.",
            clinicalNotes = "Bloquea selectivamente el transportador presináptico GAT-1, preservando GABA en la hendidura sináptica. Puede precipitar estado de mal no convulsivo si se prescribe mal.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Ganaxolona",
            acronym = "GNX",
            indications = "Tratamiento de crisis epilépticas graves asociadas al Síndrome por Deficiencia de CDKL5 (CDD). Uso huérfano.",
            dosage = "Pacientes pediátricos: Titulación escalonada durante 2-3 semanas hasta dosis de mantenimiento máxima de 21 mg/kg TID (hasta 1800 mg/día).",
            sideEffects = "Somnolencia, sedación clínica visible, hipersecreción salival, pirexia, aumento de infecciones superiores de la vía respiratoria.",
            clinicalNotes = "Neuroesteroide derivado de progesterona. Diseñado para modular receptores GABA_A de forma sináptica y extrasináptica de forma potente.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Gabapentina",
            acronym = "GBP",
            indications = "Tratamiento en adyuvancia de crisis focales con o sin generalización. No es útil en generalizadas ni ausencias.",
            dosage = "Adultos: Inicio 300 mg noche, escalar a 300 mg TID el día 3. Mantenimiento habitual amplio: 900–3600 mg/día en 3 tomas.",
            sideEffects = "Somnolencia diurna leve, mareos, edema periférico (retención hídrica), fatiga, aumento secundario de peso corporal.",
            clinicalNotes = "Une selectivamente la subunidad alfa2delta de canales de calcio presinápticos tipo N. Absorción intestinal saturable. Ajustar estrictamente de acuerdo al aclaramiento glomerular.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Pregabalina",
            acronym = "PGB",
            indications = "Terapia coadyuvante en crisis de inicio focal en adultos. No posee indicaciones en crisis generalizadas primarias.",
            dosage = "Adultos: Inicio 150 mg/día divididos en 2 o 3 tomas, titulando semanalmente según respuesta clínica hasta mantenimiento de 150–600 mg/día.",
            sideEffects = "Somnolencia, mareo, visión borrosa o diplopía subjetiva, edema maleolar o periférico marcado, incremento notable de apetito y peso.",
            clinicalNotes = "Mismo mecanismo presináptico (alfa2delta) que Gabapentina pero sin absorción saturable intestinal (cinética lineal). Requiere ajuste por depuración de creatinina.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Felbamato",
            acronym = "FBM",
            indications = "Tratamiento de rescate excepcional en crisis focales refractarias y Síndrome de Lennox-Gastaut (LGS) de difícil manejo.",
            dosage = "Adultos: Inicio de 1200 mg/día divididos, con escalada gradual de 600 mg/semana hasta un mantenimiento terapéutico de 2400–3600 mg/día.",
            sideEffects = "Anemia aplásica mortal (riesgo ~1:3000), falla hepática fulminante o necrosis hepática aguda, vómito, insomnio de conciliación.",
            clinicalNotes = "Bloqueador multimecanístico del receptor NMDA (glicina). Requiere firma de consentimiento riguroso, monitorización hematológica/PFH quincenal. TDM: 30–60 µg/mL.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Acetazolamida",
            acronym = "AZA",
            indications = "Fármaco adyuvante histórico o de rescate temporal para crisis catameniales, focales o ausencias. Eficacia mermada por tolerancia.",
            dosage = "Dosis terapéuticas de soporte: 250–1000 mg diarios divididos en dos o tres dosis por vía oral.",
            sideEffects = "Parestesias peribucales y distales, acidosis metabólica compensable, fatiga física, tinnitus, hipopotasemia moderada, litiasis urinaria recurrente.",
            clinicalNotes = "Inhibidor clásico de la anhidrasa carbónica. Su uso continuado por más de 3 meses suele inducir taquifilaxia (pérdida de la potencia anticonvulsiva).",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Sulthiame",
            acronym = "STM",
            indications = "Especialmente indicado en crisis focales pediátricas benignas de la infancia (epilepsia rolándica / BECTS) y Síndrome de Dravet.",
            dosage = "Niños: 5 mg/kg/día de inicio, buscando consolidar rangos clínicos de mantenimiento de 5–15 mg/kg/día divididos en 2 tomas.",
            sideEffects = "Hiperventilación compensatoria (disnea subjetiva sin ruidos), parestesias distales, anorexia severa de inicio, acidosis metabólica leve.",
            clinicalNotes = "Inhibidor selectivo potente de anhidrasa carbónica en el SNC. No se dispone de distribución comercial formal regular en farmacias comunes de Colombia.",
            category = "GABAérgicos, Anhidrasa y afines"
        ),
        DrugReference(
            name = "Etosuximida",
            acronym = "ETX",
            indications = "Fármaco de primera línea de elección exclusiva para crisis de Ausencia clásica (Aus). Ineficaz y contraindicado para crisis tónico-clónicas.",
            dosage = "Inicio: 250 mg BID orales, elevando progresivamente cada 4–7 días según respuesta hasta mantenimiento usual de 500–1500 mg/día, máximo 2000 mg/día.",
            sideEffects = "Dolor/malestar epigástrico, náuseas y vómitos iniciales, letargia leve, cefalea sorda, reacciones de comportamiento e hiperactividad, citopenias raras.",
            clinicalNotes = "Bloqueador selectivo de corrientes de calcio neuronales de tipo T talámicas de umbral bajo. Valproato eleva sus concentraciones. TDM: 40–100 µg/mL.",
            category = "Dianas Específicas y Síndromes"
        ),
        DrugReference(
            name = "Metsuximida",
            acronym = "MSX",
            indications = "Crisis de ausencias refractarias en pacientes donde la monoterapia con etosuximida o valproato ha fallado.",
            dosage = "Inicio: 300 mg una vez al día, con incrementos semanales de 300 mg/día hasta mantenimiento individualizado de 600–1200 mg/día.",
            sideEffects = "Gastrointestinales severas, sedación profunda, reacciones de lupus-like, discinesias o tic motores.",
            clinicalNotes = "Su uso clínico formal a nivel global es mínimo y altamente residual en neurología contemporánea.",
            category = "Dianas Específicas y Síndromes"
        ),
        DrugReference(
            name = "Perampanel",
            acronym = "PER",
            indications = "Tratamiento coadyuvante selectivo en crisis focales refractarias y de crisis generalizadas tónico-clónicas primarias (PGTCS).",
            dosage = "Monoterapia/Adyuvancia: 2 mg vía oral única al acostarse (nocte) para minimizar sedación, incrementos de 2 mg cada 2 semanas hasta mantenimiento de 4–12 mg/QD.",
            sideEffects = "Mareos pronunciados, inestabilidad para la marcha (caídas en ancianos), hostilidad, arrebatos de agresividad severa o ideación homicida (Boxed Warning en EE.UU.).",
            clinicalNotes = "Antagonista no competitivo del receptor ionotrópico de glutamato de tipo AMPA. Metabolismo altamente inducido si se prescribe con CBZ o Fenitoína.",
            category = "Dianas Específicas y Síndromes"
        ),
        DrugReference(
            name = "Cannabidiol",
            acronym = "CBD",
            indications = "Tratamiento coadyuvante de crisis epilépticas en Síndrome de Lennox-Gastaut (LGS), Síndrome de Dravet (DS) y Esclerosis Tuberosa (TSC).",
            dosage = "Inicio: 2.5 mg/kg por vía oral dos veces al día (5 mg/kg/día total). Tras una semana duplicar a 5 mg/kg BID. Mantenimiento terapéutico objetivo: 10–20 mg/kg/día.",
            sideEffects = "Somnolencia marcada, fatiga física, diarrea, anorexia extrema, elevación marcada de las transaminasas hepáticas (PFH), rash cutáneo por hipersensibilidad.",
            clinicalNotes = "Extracto purificado de Cannabis (libre de THC). Incrementa notablemente las concentraciones plasmáticas de clobazam y valproato, requiriendo descenso de dosis.",
            category = "Dianas Específicas y Síndromes"
        ),
        DrugReference(
            name = "Fenfluramina",
            acronym = "FFA",
            indications = "Tratamiento coadyuvante de crisis asociadas al Síndrome de Dravet (DS) y Síndrome de Lennox-Gastaut (LGS).",
            dosage = "Inicio: 0.1 mg/kg orales dos veces al día (0.2 mg/kg/día total). Titulable semanalmente a objetivo de 0.35 mg/kg BID (máximo absoluto diario 26 mg), o 0.2 mg/kg BID si se combina con stiripentol.",
            sideEffects = "Disminución notable del apetito o anorexia, pérdida rápida de peso, diarrea, riesgo histórico potencial de cardiopatía valvular y de hipertensión arterial pulmonar.",
            clinicalNotes = "Requiere ecocardiograma Doppler basal y de control riguroso regular cada 6 meses, así como monitorización 3 meses tras suspenderlo.",
            category = "Dianas Específicas y Síndromes"
        ),
        DrugReference(
            name = "Everolimus",
            acronym = "EVE",
            indications = "Crisis focales refractarias asociadas al complejo de la Esclerosis Tuberosa (TSC) en mayores de 2 años.",
            dosage = "Dosis inicial: 5 mg/m² por vía oral una vez al día, con ajustes de titulación estrictamente guiados por medición directa de niveles séricos mínimos (valle).",
            sideEffects = "Estomatitis aftosa dolorosa recurrente, incremento de infecciones sistémicas oportunistas, hiperlipidemia severa, neumonitis no infecciosa, trastornos de cicatrización.",
            clinicalNotes = "Inhibidor selectivo de la cinasa mTORC1. Monitorizar estrictamente lípidos séricos, recuentos de glóbulos blancos, transaminasas. TDM objetivo: 5–15 ng/mL en sangre.",
            category = "Dianas Específicas y Síndromes"
        ),
        DrugReference(
            name = "Alteplasa (rt-PA)",
            acronym = "rtPA",
            indications = "Infarto cerebral isquémico agudo de menos de 4.5 horas de evolución desde el inicio de los síntomas.",
            dosage = "Dosis total: 0.9 mg/kg (dosis máxima absoluta 90 mg). Administración: 10% de la dosis total administrada como bolo intravenoso directo en 1 minuto, seguido por el 90% restante en infusión continua endovenosa durante 60 minutos.",
            sideEffects = "Hemorragia intracerebral sintomática (rango de riesgo de 6%), sangrado sistémico activo, angioedema orolingual (especialmente en combinación de IECA).",
            clinicalNotes = "Monitorear estrictamente la presión arterial (mantener TA < 180/105 mmHg). Descartar sangrado activo en TAC craneal previo a su administración. Vigilar escalas de exclusión.",
            category = "Reperfusión y Antiagregantes"
        ),
        DrugReference(
            name = "Tenecteplasa (TNK-tPA)",
            acronym = "TNK",
            indications = "Tratamiento alternativo de reperfusión en ACV isquémico agudo de <4.5h. Indicación preferente en oclusiones de gran vaso candidatos a trombectomía mecánica.",
            dosage = "Dosis estándar: 0.25 mg/kg por vía intravenosa en un bolo único de 5-10 segundos (dosis máxima 25 mg). En escenarios raros se evalúa dosis de 0.4 mg/kg.",
            sideEffects = "Riesgo similar o ligeramente inferior de hemorragia intracerebral comparado con alteplasa, sangrado menor de encías y accesos de punción.",
            clinicalNotes = "Facilidad de administración inmediata por bolo directo, acelerando tiempos 'puerta-aguja'. Contraindicaciones idénticas a la alteplasa.",
            category = "Reperfusión y Antiagregantes"
        ),
        DrugReference(
            name = "Ácido Acetilsalicílico (Aspirina)",
            acronym = "AAS / ASA",
            indications = "Prevención secundaria de ACV isquémico no cardioembólico de origen aterotrombótico o lacunar. Fase aguda del infarto cerebral isquémico dentro de las primeras 24-48 horas de evolución (fuera de ventana de trombólisis o transcurridas 24 horas tras rTPA).",
            dosage = "Fase aguda: 160 mg a 325 mg por vía oral (VO) una vez al día. Prevención secundaria a largo plazo: 81 mg a 100 mg VO al día. En DAPT (terapia antiplaquetaria dual) usualmente se asocia a Clopidogrel por un periodo corto.",
            sideEffects = "Dispepsia, pirosis, gastritis, erosión y sangrado latente de la mucosa gastrointestinal, úlceras pépticas, sangrado de encías, epistaxis, reacciones de hipersensibilidad.",
            clinicalNotes = "Inhibidor irreversible de la ciclooxigenasa-1 (COX-1) bloqueando la síntesis de tromboxano A2. Evitar estrictamente su administración antes de que transcurran 24 horas exactas de la infusión de rTPA. El uso concomitante de un inhibidor de bomba de protones (IBP) disminuye el riesgo de sangrados digestivos.",
            category = "Reperfusión y Antiagregantes"
        ),
        DrugReference(
            name = "Clopidogrel",
            acronym = "CLO",
            indications = "Prevención secundaria en ACV isquémico no cardioembólico (intolerancia a aspirina/alergia) o en terapia antiplaquetaria dual combinada (DAPT) para ACV isquémico menor agudo (NIHSS ≤3) o AIT de alto riesgo (ABCD² ≥4).",
            dosage = "Monoterapia: 75 mg por vía oral una vez al día. Terapia Dual (DAPT): dosis de carga inicial única de 300 mg (o 600 mg antes de intervencionismo), seguido de dosis de mantenimiento de 75 mg al día, típicamente durante 21 días (guía CHANCE) o hasta 90 días (guía POINT).",
            sideEffects = "Hemorragia capilar, equimosis, dolor abdominal difuso, dispepsia, diarrea, erupciones cutáneas. Incidencia extremadamente baja de púrpura trombocitopénica trombótica (PTT) o neutropenia.",
            clinicalNotes = "Profármaco activado hepáticamente por la vía CYP2C19. Inhibe selectiva e irreversiblemente la unión del ADP a su receptor plaquetario P2Y12. Portadores de alelos de pérdida de función en CYP2C19 (metabolizadores lentos) exhiben menor respuesta antiplaquetaria.",
            category = "Reperfusión y Antiagregantes"
        ),
        DrugReference(
            name = "Ticagrelor",
            acronym = "TIC",
            indications = "Prevención secundaria en terapia dual combinada (DAPT) junto a AAS en pacientes con ACV isquémico agudo menor no cardioembólico o AIT de alto riesgo. Opción preferida en portadores de variantes de pérdida de función de CYP2C19 (esquema THALES).",
            dosage = "Esquema DAPT (junto a AAS 75-100 mg): Dosis de carga oral inicial única de 180 mg, seguido de dosis de mantenimiento de 90 mg por vía oral cada 12 horas (dos veces al día) durante 30 días continuos.",
            sideEffects = "Disnea transitoria y no cardiogénica (mediada por acumulación local de adenosina), sangrados mayores o menores, bradiarritmias sinusales ocasionales, elevación transitoria de ácido úrico y creatinina plasmática.",
            clinicalNotes = "Antagonista directo, potente y reversible del receptor plaquetario P2Y12 de ADP. No requiere activación metabólica hepática (evita variaciones génicas CYP2C19). Vigilar cumplimiento terapéutico por su pauta de dosificación doble diaria (BID).",
            category = "Reperfusión y Antiagregantes"
        ),
        DrugReference(
            name = "Piridostigmina",
            acronym = "PYR",
            indications = "Tratamiento de primera línea sintomático para debilidad fluctuante en la Miastenia Gravis.",
            dosage = "Mantenimiento: 30 mg a 60 mg por vía oral administrado cada 4 a 6 horas durante el día según las necesidades fluctuantes individuales.",
            sideEffects = "Efectos colinérgicos muscarínicos marcados: aumento de salivación, cólicos abdominales, diarrea severa, fasciculaciones musculares, diaforesis, lagrimeo.",
            clinicalNotes = "Inhibidor reversible de la acetilcolinesterasa. Dosis excesivas provocan crisis colinérgica (debilidad con fasciculaciones que mimetiza crisis miasténica).",
            category = "Fármacos de ELA, Miastenia y Demencias"
        ),
        DrugReference(
            name = "Metilprednisolona (Pulsos)",
            acronym = "MPS",
            indications = "Brotes o recaídas agudas de Esclerosis Múltiple, neuritis óptica desmielinizante, brotes de vasculitis lúpica del SNC.",
            dosage = "Esquema estándar de pulsos: 1000 mg (1 gramo) intravenoso diluido en solución fisiológica para pasar en 1 a 2 horas, administrado por 3 a 5 días consecutivos.",
            sideEffects = "Hiperglucemia aguda marcada, elevación transitoria de presión arterial, insomnio, sabor metálico oral, psicosis esteroidea, retención de líquidos.",
            clinicalNotes = "No requiere reducción progresiva de dosis si la duración es <5 días. Siempre verificar control glucémico y agregar protector gástrico (IBP).",
            category = "Neuroinmunología Desmielinizante"
        ),
        DrugReference(
            name = "Levodopa / Carbidopa",
            acronym = "L-DOPA",
            indications = "Enfermedad de Parkinson idiopática avanzada o temprana, parkinsonismo posencefalítico y rigidez fluctuante.",
            dosage = "Inicio: Usual 25/100 mg por vía oral tres veces al día (TID). Mantenimiento individualizado fluctuante entre 300 mg y 1200 mg de levodopa al día en dosis fraccionadas.",
            sideEffects = "Náuseas, emesis, hipotensión ortostática, alucinaciones visuales, discinesias motoras de rango pico y fluctuaciones motoras (períodos OFF).",
            clinicalNotes = "Precursor directo de dopamina coadministrado con inhibidor de la descarboxilasa periférica (carbidopa) para impedir degradación periférica. Absorción influenciada por proteínas de la dieta.",
            category = "Parkinson y Trastornos del Movimiento"
        ),
        DrugReference(
            name = "Pramipexol",
            acronym = "PPX",
            indications = "Monoterapia o coadyuvante en la enfermedad de Parkinson e hiperactividad del síndrome de piernas inquietas.",
            dosage = "Inicio: 0.125 mg por vía oral TID, duplicando dosis cada 5-7 días hasta mantenimiento estándar de 1.5 mg a 4.5 mg diarios divididos por vía oral.",
            sideEffects = "Somnolencia diurna súbita ('ataques de sueño'), edemas en miembros inferiores, náuseas, hipotensión, trastornos severos del control de impulsos (hipersexualidad, juego patológico).",
            clinicalNotes = "Agonista dopaminérgico no ergótico altamente selectivo para los receptores D2/D3. Eliminación vía renal intacta, requiriendo estricto ajuste de dosis.",
            category = "Parkinson y Trastornos del Movimiento"
        ),
        DrugReference(
            name = "Rasagilina",
            acronym = "RSG",
            indications = "Monoterapia en enfermedad de Parkinson temprana o adyuvante con levodopa para reducir fluctuaciones motoras y tiempos de fase OFF.",
            dosage = "Monoterapia: 1 mg por vía oral una vez al día (QD). Coadyuvante con levodopa: 0.5 mg a 1 mg una vez al día.",
            sideEffects = "Cefalea sorda, disquinesia aditiva, mareo postural, náuseas, hipotensión ortostática, riesgo de crisis hipertensiva rara.",
            clinicalNotes = "Inhibidor irreversible selectivo de la monoaminooxidasa tipo B (MAO-B). Interacción farmacológica crítica con opioides/psicotrópicos por riesgo de síndrome serotoninérgico.",
            category = "Parkinson y Trastornos del Movimiento"
        ),
        DrugReference(
            name = "Entacapona",
            acronym = "ENT",
            indications = "Coadyuvante de levodopa/carbidopa en pacientes con enfermedad de Parkinson que experimentan fluctuaciones motoras fin de dosis (wearing-off).",
            dosage = "Administración de 200 mg por vía oral con cada toma individual de levodopa, hasta un máximo absoluto de 8 tomas diarias (1600 mg/día).",
            sideEffects = "Incremento de las disquinesias, diarrea severa deshidratante, coloración naranja o marrón de la orina, náuseas.",
            clinicalNotes = "Inhibidor periférico de la catecol-O-metiltransferasa (COMT). Prolonga la ventana plasmática útil de levodopa.",
            category = "Parkinson y Trastornos del Movimiento"
        ),
        DrugReference(
            name = "Toxina Botulínica Tipo A",
            acronym = "BoNT-A",
            indications = "Distonía cervical focal, blefaroespasmo severo, espasticidad refractaria y profilasis de migraña crónica.",
            dosage = "Dosis altamente individualizada guiada por electromiografía / ecografía de forma anatómica. Rangos típicos de 100-400 unidades.",
            sideEffects = "Debilidad muscular local por difusión de dosis, disfagia cervical moderada, dolor localizado en inyección.",
            clinicalNotes = "Inhibe la liberación presináptica de acetilcolina mediante la escisión específica de la proteína SNAP-25.",
            category = "Parkinson y Trastornos del Movimiento"
        ),
        DrugReference(
            name = "Interferón Beta-1a",
            acronym = "IFN-b",
            indications = "Tratamiento modificador de la enfermedad en Esclerosis Múltiple remitente-recurrente (EMRR) o síndrome clínico aislado.",
            dosage = "Avonex: 30 mcg IM semanal. Rebif: 44 mcg subcutánea (SC) tres veces por semana (TIW).",
            sideEffects = "Síndrome pseudogripal tras inyección (atenuable con ibuprofeno), elevación de transaminasas, leucopenia transitoria, depresión.",
            clinicalNotes = "Inmunomodulador clásico. Requiere control hematológico y de PFH cada 6 meses durante el inicio del tratamiento.",
            category = "Neuroinmunología Desmielinizante"
        ),
        DrugReference(
            name = "Teriflunomida",
            acronym = "TFM",
            indications = "Tratamiento modificador de primera línea en Esclerosis Múltiple remitente-recurrente en adultos.",
            dosage = "14 mg por vía oral una vez al día (QD) con o sin alimentos.",
            sideEffects = "Hepatotoxicidad (PFH ↑), alopecia transitoria moderada, diarrea, elevación de presión arterial, neuropatía.",
            clinicalNotes = "Inhibe la proliferación linfocitaria mediante el bloqueo de la enzima de pirimidina DHODH. Contraindicación absoluta de gestación. Requiere lavado por colestiramina si plan de embarazo.",
            category = "Neuroinmunología Desmielinizante"
        ),
        DrugReference(
            name = "Fingolimod",
            acronym = "FGM",
            indications = "Esclerosis Múltiple remitente-recurrente activa que no responde a terapias de primera línea o de progresión rápida.",
            dosage = "0.5 mg por vía oral una vez al día (QD). Exige primera dosis monitorizada con ECG de 6 horas.",
            sideEffects = "Bradicardia sinusal marcada, bloqueos AV transitorios, edema macular bilateral infrecuente, riesgo alto de leucoencefalopatía (PML).",
            clinicalNotes = "Modulador del receptor S1P. Secuestra linfocitos en ganglios linfáticos alejados de la barrera hematoencefálica.",
            category = "Neuroinmunología Desmielinizante"
        ),
        DrugReference(
            name = "Natalizumab",
            acronym = "NTZ",
            indications = "EM remitente-recurrente altamente activa o progresiva con recaídas frecuentes.",
            dosage = "300 mg por infusión intravenosa continua cada 4 semanas.",
            sideEffects = "Riesgo primario de Leucoencefalopatía Multifocal Progresiva (PML) ligada a virus JC, reacciones infusionales.",
            clinicalNotes = "Anticuerpo contra la integrina alfa-4 (VLA-4), bloqueando el tráfico leucocitario en la barrera hematoencefálica. Exige nivel de anticuerpo JCV basal.",
            category = "Neuroinmunología Desmielinizante"
        ),
        DrugReference(
            name = "Ocrelizumab",
            acronym = "OCR",
            indications = "Esclerosis Múltiple remitente-recurrente y Esclerosis Múltiple primaria progresiva (EMPP) en fases tempranas.",
            dosage = "Dosis inicial: 300 mg IV de inicio día 1 y 15. Dosis de mantenimiento: 600 mg IV administrados semestralmente cada 6 meses.",
            sideEffects = "Reacciones transfusionales / infusionales, aumento de riesgo de infecciones de la vía respiratoria y neumonía, riesgo de PML.",
            clinicalNotes = "Anticuerpo anti-CD20 que causa depleción profunda y selectiva de células B. Requiere cribado de hepatitis B (HBV) antes del inicio del tratamiento.",
            category = "Neuroinmunología Desmielinizante"
        ),
        DrugReference(
            name = "Satralizumab",
            acronym = "SAT",
            indications = "Tratamiento modificador específico para el espectro de Neuromielitis Óptica (NMOSD) con anticuerpos anti-AQP4 positivos.",
            dosage = "120 mg por vía subcutánea (SC) en las semanas 0, 2 y 4, seguido por un mantenimiento de 120 mg SC cada 4 semanas.",
            sideEffects = "Reacciones de inyección local, aumento de infecciones respiratorias superiores, neutropenia transitoria, elevación de PFH.",
            clinicalNotes = "Anticuerpo monoclonal dirigido contra el receptor de interleucina-6 (IL-6R). No es útil ni recomendado en esclerosis múltiple clásica.",
            category = "Neuroinmunología Desmielinizante"
        ),
        DrugReference(
            name = "Riluzol",
            acronym = "RLZ",
            indications = "Tratamiento modificador específico diseñado para enlentecer la progresión en Esclerosis Lateral Amiotrófica (ELA).",
            dosage = "50 mg por vía oral dos veces al día (BID), idealmente tomado 1 hora antes o 2 horas después de comidas.",
            sideEffects = "Astenia severa, náuseas, anorexia, dolor abdominal, incremento de transaminasas hepáticas, neutropenia leve.",
            clinicalNotes = "Bloqueador de corrientes de sodio y modulador de la liberación presináptica de glutamato. Exige control estricto de LFTs (pruebas de función hepática) mensual.",
            category = "Fármacos de ELA, Miastenia y Demencias"
        ),
        DrugReference(
            name = "Tofersen",
            acronym = "TFS",
            indications = "Tratamiento modificador en adultos con Esclerosis Lateral Amiotrófica (ELA) con mutación confirmada en el gen SOD1.",
            dosage = "100 mg administrados por inyección intratecal (IT) en dosis de carga el día 1, 15 y 29; seguido de mantenimiento regular de 100 mg cada 28 días.",
            sideEffects = "Meningitis aséptica reactiva, pleocitosis transitoria en LCR, elevación de proteínas en líquido espinal, dolor de espalda.",
            clinicalNotes = "Oligonucleótido antisentido de vanguardia diseñado para degradar el ARNm mutado de la SOD1 hepato-neuronal.",
            category = "Fármacos de ELA, Miastenia y Demencias"
        ),
        DrugReference(
            name = "Donepezilo",
            acronym = "DPZ",
            indications = "Enfermedad de Alzheimer en fases leve, moderada o grave (rango sintomático cortical).",
            dosage = "Dosis inicial: 5 mg por vía oral por las noches (QHS). Subir a 10 mg QD tras 4-6 semanas de excelente tolerancia clínica.",
            sideEffects = "Náuseas potentes, diarrea líquida ocasional, anorexia marcada, insomnio de conciliación, calambres musculares, bradicardia sinusal o bloqueos AV.",
            clinicalNotes = "Inhibidor reversible altamente específico de la acetilcolinesterasa periférica y central. Interacciones con betabloqueantes aditivos.",
            category = "Fármacos de ELA, Miastenia y Demencias"
        ),
        DrugReference(
            name = "Memantina",
            acronym = "MMT",
            indications = "Enfermedad de Alzheimer en etapas moderadamente severas o graves, ya sea sola o asociada a donepezilo.",
            dosage = "Dosis de titulación: Iniciar 5 mg QD, subiendo 5 mg semanales hasta un mantenimiento estable de 10 mg por vía oral dos veces al día (20 mg/día).",
            sideEffects = "Alucinaciones visuales incidentales leves, constipación marcada, mareo postural, somnolencia, hipertensión arterial.",
            clinicalNotes = "Antagonista no competitivo de afinidad moderada para el receptor de glutamato NMDA. Protege de la excitotoxicidad mediada por calcio. Requiere ajuste estricto si CrCl <30 mL/min.",
            category = "Fármacos de ELA, Miastenia y Demencias"
        ),
        DrugReference(
            name = "Brexpiprazol",
            acronym = "BXP",
            indications = "Tratamiento de la agitación del comportamiento severa asociada a la demencia por enfermedad de Alzheimer.",
            dosage = "Titulación: 0.5 mg/día en días 1-7, subir a 1 mg/día en días 8-14, y luego dosis objetivo de 2 mg por vía oral una vez al día (QD) de mantenimiento.",
            sideEffects = "Somnolencia pronunciada, acatisia moderada, aumento del riesgo de eventos vasculares en ancianos (Advertencia de clase).",
            clinicalNotes = "Agonista parcial de los receptores D2 y 5-HT1A, y antagonista del receptor 5-HT2A. Metabolismo regulado por CYP3A4 y CYP2D6.",
            category = "Fármacos de ELA, Miastenia y Demencias"
        ),
        DrugReference(
            name = "Lecanemab",
            acronym = "LCM_A",
            indications = "Tratamiento modificador en fases ultra-tempranas o deterioro cognitivo leve (DCL) por enfermedad de Alzheimer con placas de amiloide en PET/LCR.",
            dosage = "Dosis de mantenimiento: 10 mg/kg administrados en infusión intravenosa líquida cada 2 semanas.",
            sideEffects = "Anomalías de Imagen Relacionadas con el Amiloide: edema cerebral (ARIA-E) y microhemorragias cerebrales (ARIA-H), cefaleas severas.",
            clinicalNotes = "Anticuerpo monoclonal IgG1 humanizado que aclara los protofilamentos solubles de beta-amiloide soluble cerebral. Exige monitorización rigurosa de seguridad con RM cerebral antes de la 5ª, 7ª y 14ª dosis.",
            category = "Fármacos de ELA, Miastenia y Demencias"
        )
    )

    // Miscellaneous scale options defined inline or generated
    val mgfaClasses = listOf(
        "Clase I: Cualquier debilidad muscular ocular; fuerza del resto normal.",
        "Clase II: Debilidad leve de músculos extraoculares (ocular, bulbar o extremidades).",
        "Clase IIa: Principalmente afectación de extremidades, músculos axiales o ambos.",
        "Clase IIb: Principalmente afectación de músculos orofaríngeos, respiratorios o ambos.",
        "Clase III: Debilidad moderada de músculos extraoculares de la placa neuromuscular.",
        "Clase IIIa: Principalmente afectación de extremidades, músculos axiales o ambos.",
        "Clase IIIb: Principalmente afectación de músculos orofaríngeos, respiratorios o ambos.",
        "Clase IV: Debilidad grave de músculos extraoculares o bulbares.",
        "Clase IVa: Principalmente afectación de extremidades, músculos axiales o ambos.",
        "Clase IVb: Principalmente afectación de músculos orofaríngeos, respiratorios o ambos.",
        "Clase V: Definido por necesidad de intubación endotraqueal con o sin soporte ventilatorio."
    )

    val fastStages = listOf(
        "Etapa 1 - Normal: Sin quejas subjetivas de fallos de memoria ni de rendimiento funcional.",
        "Etapa 2 - Olvidos subjetivos: Quejas subjetivas de extraviar objetos, fallos de nombres habituales.",
        "Etapa 3 - Incipiente / Defecto leve: Defecto cognitivo incipiente. Reducción en rendimiento laboral o desorientación en zonas nuevas.",
        "Etapa 4 - Leve: Defecto cognitivo leve. Dificultad para manejar finanzas complejas o planificar eventos sociales estructurados.",
        "Etapa 5 - Moderado: Defecto cognitivo moderado. Requiere asistencia para seleccionar ropa correcta según el clima habitual.",
        "Etapa 6 - Moderadamente Severo: Requiere asistencia para actividades básicas de la vida diaria.",
        "Etapa 6a: Requiere asistencia física para vestirse correctamente.",
        "Etapa 6b: Requiere asistencia para higiene / baño regular.",
        "Etapa 6c: Requiere asistencia directa para ir al cuarto de baño / inodoro.",
        "Etapa 6d: Incontinencia urinaria ocasional o recurrente.",
        "Etapa 6e: Incontinencia fecal ocasional o frecuente.",
        "Etapa 7 - Severo: Pérdida marcada de facultades verbales, motoras e intelectuales básicas.",
        "Etapa 7a: Capacidad verbal restringida a unas 6 palabras inteligibles por jornada.",
        "Etapa 7b: Capacidad verbal restringida a una sola palabra comprensible.",
        "Etapa 7c: Pérdida total de la capacidad para marchar o deambular sin apoyo físico.",
        "Etapa 7d: Pérdida de la capacidad de sentarse erguido sin asistencia lateral.",
        "Etapa 7e: Pérdida de la sonrisa social.",
        "Etapa 7f: Pérdida de la habilidad para sostener su propia cabeza sin soporte."
    )

    val mrsGrades = listOf(
        "Grado 0: Sin síntomas activos residuales de ningún tipo.",
        "Grado 1: Incapacidad sintomática insignificante. Lleva a cabo todas sus actividades previas habituales.",
        "Grado 2: Incapacidad leve. Incapaz de realizar actividades previas pesadas pero completamente autónomo.",
        "Grado 3: Incapacidad moderada. Requiere ayuda externa, pero deambula sin asistencia de forma autónoma.",
        "Grado 4: Incapacidad moderadamente grave. Incapaz de vestirse, asearse o deambular sin asistencia directa.",
        "Grado 5: Incapacidad grave. Postrado en cama por completo, incontinencia urinaria y fecal, requiere cuido permanente.",
        "Grado 6: Éxitus / Fallecido."
    )

    val dermatomes = listOf(
        DermatomeReference("C2", "Protuberancia occipital externa", "Región occipital superior y base del cráneo posterior."),
        DermatomeReference("C3", "Fosa supraclavicular", "Fosa supraclavicular en la línea medioclavicular."),
        DermatomeReference("C4", "Articulación acromioclavicular", "Región superior del hombro, sobre la articulación acromioclavicular."),
        DermatomeReference("C5", "Fosa antecubital lateral", "Borde externo del brazo y cara lateral de la fosa antecubital."),
        DermatomeReference("C6", "Pulgar", "Falange distal del pulgar (primer dedo de la mano)."),
        DermatomeReference("C7", "Dedo medio", "Falange distal del dedo medio (tercer dedo de la mano)."),
        DermatomeReference("C8", "Dedo meñique", "Falange distal del dedo meñique (quinto dedo de la mano) en su borde cubital."),
        DermatomeReference("T1", "Fosa antecubital medial", "Borde interno de la fosa antecubital, proximal al epicóndilo medial."),
        DermatomeReference("T2", "Vértice de la axila", "Región de la axila medial alta."),
        DermatomeReference("T4", "Pecho (pezones)", "Cuarto espacio intercostal en la línea medioclavicular."),
        DermatomeReference("T10", "Ombligo", "Décimo espacio intercostal en la línea medioclavicular (línea umbilical)."),
        DermatomeReference("L1", "Región inguinal", "Punto medio entre el ligamento inguinal (T12) y L2."),
        DermatomeReference("L2", "Cara anterior del muslo", "Punto medio de la cara anterior del muslo."),
        DermatomeReference("L3", "Cóndilo femoral medial", "Zona del cóndilo femoral medial justo por encima de la rodilla anterior."),
        DermatomeReference("L4", "Maléolo medial", "Maléolo medial del tobillo anterior y cara medial del pie."),
        DermatomeReference("L5", "Dorso del pie", "Cara dorsal del pie a nivel de la tercera articulación metatarsofalángica."),
        DermatomeReference("S1", "Borde lateral del talón", "Borde externo del talón y maléolo lateral externo."),
        DermatomeReference("S2", "Fosa poplítea", "Punto medio de la fosa poplítea detrás de la rodilla."),
        DermatomeReference("S3", "Tuberosidad isquiática", "Región de la nalga inferior o pliegue glúteo medio."),
        DermatomeReference("S4-S5", "Región perianal", "Área perianal profunda.")
    )

    val reflexes = listOf(
        ReflexReference(
            name = "Reflejo Bicipital",
            level = "C5 - C6",
            nerve = "Nervio Musculocutáneo",
            response = "Contracción del músculo bíceps y flexión del codo.",
            clinicalNotes = "Paciente con codo semiflexionado y mano en pronación; percutir sobre el tendón del bíceps en la fosa antecubital."
        ),
        ReflexReference(
            name = "Reflejo Estilorradial / Supinador Largo",
            level = "C5 - C6",
            nerve = "Nervio Radial",
            response = "Flexión y supinación leve de la mano y antebrazo.",
            clinicalNotes = "Percutir sobre la apófisis estiloides del radio, aproximadamente 2-5 cm proximal a la muñeca."
        ),
        ReflexReference(
            name = "Reflejo Tricipital",
            level = "C7 - C8",
            nerve = "Nervio Radial",
            response = "Flexión menor o extensión del codo.",
            clinicalNotes = "Sosteniendo el brazo en abducción y antebrazo colgando; percutir directamente el tendón del tríceps sobre el olécranon."
        ),
        ReflexReference(
            name = "Reflejo Patelar / Rotuliano",
            level = "L2 - L4",
            nerve = "Nervio Femoral",
            response = "Extensión de la rodilla por contracción del cuádriceps.",
            clinicalNotes = "Paciente sentado con piernas colgando libremente; percutir el tendón rotuliano entre la rótula y la tuberosidad de la tibia."
        ),
        ReflexReference(
            name = "Reflejo Aquiliano",
            level = "S1 - S2",
            nerve = "Nervio Tibial",
            response = "Flexión plantar de la articulación del tobillo.",
            clinicalNotes = "Paciente de rodillas sobre silla o sentado con pie suspendido en ligera flexión dorsal pasiva; percutir tendón de Aquiles."
        ),
        ReflexReference(
            name = "Reflejo Flexor Plantar (Babinski)",
            level = "L5 - S2",
            nerve = "Nervio Tibial",
            response = "Respuesta normal: Flexión plantar de todos los dedos.",
            clinicalNotes = "Raspado firme del borde externo de la planta del pie. Elevación y abanico del primer dedo (Babinski positivo) indica disfunción suprasegmentaria o del haz piramidal."
        )
    )
}
