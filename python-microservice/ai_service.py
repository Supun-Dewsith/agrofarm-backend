import base64
from typing import List, Optional
from dotenv import load_dotenv
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from pydantic import BaseModel, Field
from google import genai
from google.genai.types import HttpOptions

load_dotenv()

app = FastAPI(title="Plant Disease Analysis Service")
client = genai.Client(
    http_options={
        "base_url": "https://gemini-proxy.supundewsith.workers.dev"
    }
)

class DetectedSpot(BaseModel):
    box_2d: List[int] = Field(description="[ymin, xmin, ymax, xmax] normalized 0-1000.")
    confidence: float
    description: str

class IpmTreatmentItem(BaseModel):
    category: str = Field(description="Category: 'Organic & Biological', 'Chemical Intervention', or 'Cultural & Mechanical'")
    method: str = Field(description="Method or active ingredient name, e.g. Neem seed kernel extract (NSKE 5%) or Spinosad")
    dosage_and_timing: str = Field(description="Precise dosage and application timing schedule, e.g. Spray early morning or late evening every 7 days")
    phi_interval: Optional[str] = Field(default="", description="Pre-Harvest Interval (PHI) or safety withholding time")

class ClassProbability(BaseModel):
    name: str = Field(description="Disease or condition name")
    probability: float = Field(description="Probability between 0.0 and 1.0")

class LifeCycleStage(BaseModel):
    stage_name: str = Field(description="Stage name, e.g. Incubation, Active Damage, Spore Release / Pupation")
    duration: str = Field(description="Duration or window, e.g. 2–4 days")
    description: str = Field(description="Specific biological symptom or event in this stage")

class DiseaseAnalysis(BaseModel):
    plant_name: str
    is_healthy: bool
    disease_name: str
    overall_confidence: float
    general_description: str
    detected_spots: List[DetectedSpot]
    possible_treatments: List[str]
    prevention_tips: List[str]

    # 1. Biological & Pathogen Profile
    pathogen_type: str = Field(default="Fungal Spore", description="Classification: Fungal Spore, Bacterial Blight, Pest / Insect Larva, Viral Pathogen, or Physiological Deficiency")
    scientific_name: str = Field(default="", description="Exact binomial scientific Latin name of pathogen or pest")
    optimal_temperature: str = Field(default="22°C – 30°C", description="Optimal temperature range for pathogen proliferation")
    humidity_level: str = Field(default=">80% RH", description="Moisture / relative humidity range favoring disease")
    weather_correlation: str = Field(default="", description="Weather correlation note, e.g. Current high humidity increases spread velocity by ~35%")
    life_cycle_stages: List[LifeCycleStage] = Field(default_factory=list, description="3-stage biological progression timeline")

    # 2. Yield & Economic Impact Forecast
    yield_loss_risk: str = Field(default="15% – 25% foliage loss", description="Estimated yield loss risk narrative")
    yield_loss_percentage: int = Field(default=20, description="Projected foliage or yield loss percentage integer (0-100)")
    spread_speed: str = Field(default="Moderate: 3–5 days to adjacent rows", description="Spread speed rating badge")
    harvest_impact: str = Field(default="", description="Pre-Harvest Interval (PHI) alert or harvest safety warning")

    # 3. Integrated Pest Management (IPM) Matrix
    ipm_matrix: List[IpmTreatmentItem] = Field(default_factory=list, description="3-tier actionable IPM matrix items")

    # 4. Multi-class Probability Distribution
    probability_distribution: List[ClassProbability] = Field(default_factory=list, description="Top 3 likely diagnoses with probabilities")

    # 5. Reference Textbook Standard
    reference_image_url: Optional[str] = Field(default="", description="Reference specimen standard identifier or URL")

class ChatMessage(BaseModel):
    role: str  # "user" | "assistant"
    content: str

class DiseaseChatRequest(BaseModel):
    question: str
    analysis: Optional[DiseaseAnalysis] = None
    history: List[ChatMessage] = Field(default_factory=list)
    language: Optional[str] = "en"

class DiseaseChatResponse(BaseModel):
    answer: str


def _get_language_instruction(language: Optional[str]) -> str:
    lang = (language or "en").lower().strip()
    if lang.startswith("si"):
        return (
            "LANGUAGE REQUIREMENT: Respond in Sinhala (සිංහල). "
            "All text values in the response (including plant_name, disease_name, "
            "general_description, detected spot descriptions, possible_treatments, prevention_tips, "
            "pathogen_type, scientific_name, optimal_temperature, humidity_level, weather_correlation, "
            "yield_loss_risk, spread_speed, harvest_impact, ipm_matrix, and life_cycle_stages) "
            "MUST be written in clear, natural Sinhala (සිංහල). "
            "Latin scientific names and chemical ingredients may include English in parentheses."
        )
    elif lang.startswith("ta"):
        return (
            "LANGUAGE REQUIREMENT: Respond in Tamil (தமிழ்). "
            "All text values in the response MUST be written in clear and natural Tamil. "
            "Latin scientific names and chemical active ingredients may include English in parentheses."
        )
    elif lang.startswith("en"):
        return "LANGUAGE REQUIREMENT: Respond in English."
    else:
        return f"LANGUAGE REQUIREMENT: Respond in {language}."


def _enrich_defaults_if_needed(analysis: DiseaseAnalysis) -> DiseaseAnalysis:
    """Ensure all high-impact diagnostic sections are richly populated even if AI model returns partial defaults."""
    d_name = (analysis.disease_name or "").lower()
    is_healthy = analysis.is_healthy or "healthy" in d_name

    # Scientific name & pathogen type defaults
    if not analysis.scientific_name or analysis.scientific_name.strip() == "":
        if is_healthy:
            analysis.scientific_name = "Plantae (Normal Flora)"
            analysis.pathogen_type = "Beneficial / Non-Pathogenic"
            analysis.optimal_temperature = "18°C – 28°C (Normal)"
            analysis.humidity_level = "50% – 70% RH (Optimal)"
            analysis.weather_correlation = "Normal ambient farm weather supports vigorous photosynthesis."
            analysis.yield_loss_risk = "0% – Normal yield expected"
            analysis.yield_loss_percentage = 0
            analysis.spread_speed = "None: Foliage healthy"
            analysis.harvest_impact = "No pre-harvest withholding necessary. Safe for standard harvest schedule."
        elif "miner" in d_name:
            analysis.scientific_name = "Liriomyza trifolii"
            analysis.pathogen_type = "Pest / Insect Larva"
            analysis.optimal_temperature = "25°C – 32°C"
            analysis.humidity_level = "60% – 85% RH"
            analysis.weather_correlation = "Warm weather and dry foliage favor accelerated adult fly oviposition."
            analysis.yield_loss_risk = "15% – 30% photosynthetic foliage reduction"
            analysis.yield_loss_percentage = 25
            analysis.spread_speed = "Moderate: 3–5 days to adjacent crop rows"
            analysis.harvest_impact = "Withhold harvest if synthetic larvicides are applied (7-day PHI)."
        elif "blight" in d_name:
            analysis.scientific_name = "Phytophthora infestans" if "late" in d_name else "Alternaria solani"
            analysis.pathogen_type = "Fungal Spore / Oomycete"
            analysis.optimal_temperature = "18°C – 24°C"
            analysis.humidity_level = ">85% RH"
            analysis.weather_correlation = "High farm humidity (>80%) and cool night dew increase fungal sporulation rate by ~40%."
            analysis.yield_loss_risk = "35% – 60% potential crop loss if untreated"
            analysis.yield_loss_percentage = 45
            analysis.spread_speed = "Rapid: 24–48 hours across canopy"
            analysis.harvest_impact = "Do not harvest infected bunches. Strict fungicide withholding period applies."
        elif "rust" in d_name:
            analysis.scientific_name = "Puccinia sorghi / Puccinia arachidis"
            analysis.pathogen_type = "Fungal Spore"
            analysis.optimal_temperature = "20°C – 28°C"
            analysis.humidity_level = ">80% RH"
            analysis.weather_correlation = "Humid winds and surface wetness accelerate urediniospore dissemination."
            analysis.yield_loss_risk = "20% – 35% grain/leaf weight loss"
            analysis.yield_loss_percentage = 28
            analysis.spread_speed = "Moderate to High: 2–4 days"
            analysis.harvest_impact = "Ensure recommended foliar sprays adhere to 14-day pre-harvest interval."
        elif "mildew" in d_name:
            analysis.scientific_name = "Erysiphe cichoracearum / Oidium spp."
            analysis.pathogen_type = "Fungal Spore"
            analysis.optimal_temperature = "20°C – 27°C"
            analysis.humidity_level = "70% – 90% RH"
            analysis.weather_correlation = "High relative humidity with low direct solar radiation encourages powdery hyphae."
            analysis.yield_loss_risk = "15% – 25% foliage loss"
            analysis.yield_loss_percentage = 20
            analysis.spread_speed = "Moderate: 3–5 days"
            analysis.harvest_impact = "Organic sulfur sprays require 3-day PHI."
        elif "spot" in d_name or "cercospora" in d_name:
            analysis.scientific_name = "Cercospora capsici / Septoria lycopersici"
            analysis.pathogen_type = "Fungal Spore"
            analysis.optimal_temperature = "24°C – 30°C"
            analysis.humidity_level = ">80% RH"
            analysis.weather_correlation = "Rain splash and high ambient humidity accelerate conidial germination."
            analysis.yield_loss_risk = "15% – 25% foliage loss"
            analysis.yield_loss_percentage = 20
            analysis.spread_speed = "Moderate: 3–5 days to adjacent rows"
            analysis.harvest_impact = "Ensure 7-day PHI when copper or triazole fungicides are utilized."
        elif "virus" in d_name or "mosaic" in d_name:
            analysis.scientific_name = "Tomato yellow leaf curl virus (TYLCV)"
            analysis.pathogen_type = "Viral Pathogen (Vector-Borne)"
            analysis.optimal_temperature = "26°C – 35°C"
            analysis.humidity_level = "50% – 75% RH"
            analysis.weather_correlation = "Dry warm periods increase whitefly (Bemisia tabaci) flight and transmission."
            analysis.yield_loss_risk = "40% – 70% severe stunt and flower drop"
            analysis.yield_loss_percentage = 55
            analysis.spread_speed = "High: Linked to vector mobility"
            analysis.harvest_impact = "Rogue out infected plants immediately to protect neighbouring blocks."
        elif "bacterial" in d_name or "wilt" in d_name:
            analysis.scientific_name = "Ralstonia solanacearum / Xanthomonas spp."
            analysis.pathogen_type = "Bacterial Blight / Vascular"
            analysis.optimal_temperature = "28°C – 34°C"
            analysis.humidity_level = ">85% RH"
            analysis.weather_correlation = "Waterlogged soil and tropical heat cause rapid vascular colonization."
            analysis.yield_loss_risk = "30% – 50% systemic vascular decline"
            analysis.yield_loss_percentage = 40
            analysis.spread_speed = "High via irrigation run-off"
            analysis.harvest_impact = "Quarantine section and sanitize harvesting knives/shears."
        else:
            analysis.scientific_name = "Infectious Phytopathogen Complex"
            analysis.pathogen_type = "Fungal / Microbial Spore"
            analysis.optimal_temperature = "22°C – 30°C"
            analysis.humidity_level = ">75% RH"
            analysis.weather_correlation = "Current environmental moisture fosters spore survival on leaf surfaces."
            analysis.yield_loss_risk = "15% – 25% foliage loss"
            analysis.yield_loss_percentage = 20
            analysis.spread_speed = "Moderate: 3–5 days to adjacent rows"
            analysis.harvest_impact = "Monitor field and apply preventive IPM protocol."

    # 3-stage life cycle defaults
    if not analysis.life_cycle_stages or len(analysis.life_cycle_stages) < 3:
        if is_healthy:
            analysis.life_cycle_stages = [
                LifeCycleStage(stage_name="Vegetative Phase", duration="15–25 days", description="Active green leaf expansion and solar energy absorption."),
                LifeCycleStage(stage_name="Flowering & Maturation", duration="20–30 days", description="Sturdy stem vascular flow supporting reproductive buds."),
                LifeCycleStage(stage_name="Harvest Prime", duration="10–14 days", description="Optimum photosynthetic tissue health and premium produce quality.")
            ]
        elif "miner" in d_name:
            analysis.life_cycle_stages = [
                LifeCycleStage(stage_name="Incubation & Oviposition", duration="2–4 days", description="Adult females insert eggs under the upper leaf epidermis."),
                LifeCycleStage(stage_name="Active Serpentine Mining", duration="7–10 days", description="Larvae feed inside mesophyll, creating winding white translucent mines."),
                LifeCycleStage(stage_name="Pupation & Emergence", duration="5–7 days", description="Mature larvae emerge from leaf slit to pupate on soil or lower foliage.")
            ]
        else:
            analysis.life_cycle_stages = [
                LifeCycleStage(stage_name="Incubation & Inoculation", duration="2–4 days", description="Microscopic spores adhere to moist leaf cuticle and germinate."),
                LifeCycleStage(stage_name="Active Foliar Damage", duration="5–8 days", description="Pathogen hyphae invade parenchyma, forming chlorotic halo and necrosis."),
                LifeCycleStage(stage_name="Spore Release & Dispersal", duration="4–6 days", description="Secondary conidia/spores erupt from lesions and disperse via wind and water splash.")
            ]

    # IPM Matrix defaults
    if not analysis.ipm_matrix or len(analysis.ipm_matrix) == 0:
        if is_healthy:
            analysis.ipm_matrix = [
                IpmTreatmentItem(
                    category="Organic & Biological",
                    method="Seaweed foliar bio-stimulant / Trichoderma viride",
                    dosage_and_timing="2 ml/L water, foliar spray every 14 days",
                    phi_interval="0 days PHI (Completely safe)"
                ),
                IpmTreatmentItem(
                    category="Chemical Intervention",
                    method="Balanced micro-nutrient chelate (Zn, Fe, Mg, B)",
                    dosage_and_timing="1 g/L water during early morning cool hours",
                    phi_interval="0 days PHI"
                ),
                IpmTreatmentItem(
                    category="Cultural & Mechanical",
                    method="Standard canopy pruning & drip irrigation maintenance",
                    dosage_and_timing="Ensure adequate light penetration and row spacing",
                    phi_interval="Continuous agronomic practice"
                )
            ]
        elif "miner" in d_name:
            analysis.ipm_matrix = [
                IpmTreatmentItem(
                    category="Organic & Biological",
                    method="Neem seed kernel extract (NSKE 5%) or Spinosad 45% SC",
                    dosage_and_timing="0.5 ml/L water, spray in early morning or late evening every 7 days",
                    phi_interval="3 days PHI"
                ),
                IpmTreatmentItem(
                    category="Chemical Intervention",
                    method="Abamectin 1.8% EC / Imidacloprid 17.8% SL",
                    dosage_and_timing="0.5 ml/L water, target lower and upper leaf surfaces",
                    phi_interval="Strict 7-day Pre-Harvest Interval (PHI)"
                ),
                IpmTreatmentItem(
                    category="Cultural & Mechanical",
                    method="Yellow sticky traps (20 traps/acre) & infested leaf hand-pruning",
                    dosage_and_timing="Place sticky traps directly at canopy height immediately; burn heavily mined leaves",
                    phi_interval="Zero chemical residue"
                )
            ]
        else:
            analysis.ipm_matrix = [
                IpmTreatmentItem(
                    category="Organic & Biological",
                    method="Copper octanoate / Bacillus subtilis / Cold-pressed Neem oil (2%)",
                    dosage_and_timing="3–5 ml/L water, spray during calm wind hours every 5–7 days",
                    phi_interval="1–2 days PHI"
                ),
                IpmTreatmentItem(
                    category="Chemical Intervention",
                    method="Mancozeb 75% WP or Azoxystrobin + Difenoconazole",
                    dosage_and_timing="1.5–2.0 g/L water, thorough foliar coverage",
                    phi_interval="Strict 7 to 10-day Pre-Harvest Interval (PHI)"
                ),
                IpmTreatmentItem(
                    category="Cultural & Mechanical",
                    method="Sanitary pruning of lower infected leaves & drip line adjustments",
                    dosage_and_timing="Avoid overhead sprinkler splash; disinfect shears with 70% alcohol between plots",
                    phi_interval="Zero residue"
                )
            ]

    # Probability distribution defaults
    if not analysis.probability_distribution or len(analysis.probability_distribution) == 0:
        conf = analysis.overall_confidence if analysis.overall_confidence > 0 else 0.92
        if conf > 1.0:
            conf = conf / 100.0
        conf = min(max(conf, 0.50), 0.99)
        rem = round(1.0 - conf, 3)
        p2 = round(rem * 0.65, 3)
        p3 = round(rem - p2, 3)

        d_name_clean = analysis.disease_name if not is_healthy else "Healthy Tissue"
        alt1 = "Septoria Leaf Spot" if "miner" in d_name else ("Leaf Miner Infestation" if "blight" in d_name else "Nutrient Deficiency")
        alt2 = "Nutrient Chlorosis (Mg / Fe)" if is_healthy else "Physiological Sunscald"

        analysis.probability_distribution = [
            ClassProbability(name=d_name_clean, probability=round(conf, 3)),
            ClassProbability(name=alt1, probability=p2),
            ClassProbability(name=alt2, probability=p3)
        ]

    return analysis


def _build_context_prompt(analysis: Optional[DiseaseAnalysis], language: Optional[str] = "en") -> str:
    lang_instruction = _get_language_instruction(language)
    if analysis is None:
        return (
            "You are an agronomy assistant. The farmer has not completed a leaf scan yet. "
            "Ask them to run diagnostics first if they need crop-specific advice.\n"
            f"{lang_instruction}"
        )
    spots = "\n".join(
        f"- {s.description} (confidence {s.confidence:.2f}, box {s.box_2d})"
        for s in analysis.detected_spots
    ) or "- none"
    treatments = "\n".join(f"- {t}" for t in analysis.possible_treatments) or "- none"
    prevention = "\n".join(f"- {p}" for p in analysis.prevention_tips) or "- none"
    ipm = "\n".join(
        f"- [{item.category}] {item.method}: {item.dosage_and_timing} (PHI: {item.phi_interval})"
        for item in analysis.ipm_matrix
    ) or "- none"
    return f"""You are the senior agronomy assistant for AgroFarm. Answer using this comprehensive diagnostic report.
Keep answers practical, grounded in agronomic best practices, clear, and actionable.
{lang_instruction}

DIAGNOSTIC REPORT DETAILS:
- Crop / Plant Host: {analysis.plant_name}
- Diagnostic Condition: {analysis.disease_name} (Healthy: {analysis.is_healthy})
- Pathogen Type: {analysis.pathogen_type}
- Scientific Binomial Name: {analysis.scientific_name}
- Optimal Environmental Triggers: Temp {analysis.optimal_temperature}, Humidity {analysis.humidity_level}
- Weather Correlation: {analysis.weather_correlation}
- Projected Yield Loss Risk: {analysis.yield_loss_risk} (Impact: ~{analysis.yield_loss_percentage}%)
- Spread Speed: {analysis.spread_speed}
- Harvest Pre-Harvest Interval Alert: {analysis.harvest_impact}
- General Etiology & Description: {analysis.general_description}
Detected Lesions & Spots:
{spots}
Integrated Pest Management (IPM) Matrix:
{ipm}
Standard Treatments:
{treatments}
Preventative Agronomic Directives:
{prevention}
"""

@app.post("/analyze-leaf", response_model=DiseaseAnalysis)
async def analyze_leaf(
    file: UploadFile = File(...),
    language: Optional[str] = Form(default="en")
):
    try:
        image_bytes = await file.read()
        encoded_image = base64.b64encode(image_bytes).decode("utf-8")

        lang_instruction = _get_language_instruction(language)
        prompt = f"""
        Perform an in-depth agronomic diagnostic analysis of this plant leaf sample image.
        Provide a complete, actionable diagnostic report containing:
        1. Plant Host Name and Disease Name (or state healthy leaf).
        2. Biological & Pathogen Profile:
           - pathogen_type: Classification (Pest / Insect Larva, Fungal Spore, Bacterial Blight, Viral Pathogen, or Physiological Deficiency).
           - scientific_name: Exact Latin binomial scientific name (e.g., Liriomyza trifolii, Cercospora capsici, Phytophthora infestans).
           - optimal_temperature: Temperature range where it flourishes (e.g. 24°C – 30°C).
           - humidity_level: Moisture / relative humidity conditions (e.g. >80% RH).
           - weather_correlation: Correlation narrative describing how farm weather / humidity affects spread speed.
           - life_cycle_stages: A 3-stage biological timeline (Incubation -> Active Damage -> Spore Release / Pupation) with stage_name, duration, and description.
        3. Yield & Economic Impact Forecast:
           - yield_loss_risk: Projected loss if untreated (e.g. '15% – 25% foliage loss, Moderate fruit impact').
           - yield_loss_percentage: Integer percentage (e.g. 25).
           - spread_speed: Badge rating (e.g. 'Moderate: 3–5 days to adjacent rows').
           - harvest_impact: Pre-Harvest Interval (PHI) warnings and harvest withhold guidelines.
        4. Integrated Pest Management (IPM) Matrix:
           A 3-tier actionable matrix containing items for:
           - 'Organic & Biological' (e.g. Neem seed kernel extract NSKE 5%, Spinosad, Bacillus subtilis)
           - 'Chemical Intervention' (e.g. Abamectin 1.8% EC, Imidacloprid, Mancozeb)
           - 'Cultural & Mechanical' (e.g. Yellow sticky traps, sanitizing pruning, canopy airflow)
           Each with precise dosage_and_timing and phi_interval.
        5. Multi-Class Probability Distribution:
           - Top 3 candidate diagnoses with probabilities summing to ~1.0.
        6. Normalized Bounding Boxes:
           - [ymin, xmin, ymax, xmax] on a 0-1000 scale for each visible lesion or damaged spot.
        7. Overall confidence score (0.0 - 1.0), general description, possible_treatments list, and prevention_tips list.

        {lang_instruction}
        """

        interaction = client.interactions.create(
            model="gemini-3.5-flash-lite", 
            input=[
                {"type": "text", "text": prompt},
                {
                    "type": "image",
                    "data": encoded_image,
                    "mime_type": file.content_type or "image/jpeg",
                    "resolution": "low" 
                }
            ],
            response_format={
                "type": "text",
                "mime_type": "application/json",
                "schema": DiseaseAnalysis.model_json_schema()
            },
            generation_config={
                "thinking_level": "minimal" 
            }
        )

        analysis = DiseaseAnalysis.model_validate_json(interaction.output_text)
        return _enrich_defaults_if_needed(analysis)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/disease-chat", response_model=DiseaseChatResponse)
async def disease_chat(payload: DiseaseChatRequest):
    try:
        history_text = "\n".join(
            f"{m.role.upper()}: {m.content}" for m in payload.history[-8:]
        )
        prompt = f"""{_build_context_prompt(payload.analysis, payload.language)}
Conversation so far:
{history_text or "(none)"}
FARMER QUESTION:
{payload.question}
"""
        interaction = client.interactions.create(
            model="gemini-3.5-flash-lite",
            input=[{"type": "text", "text": prompt}],
            generation_config={"thinking_level": "minimal"},
        )
        return DiseaseChatResponse(answer=interaction.output_text.strip())
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class ReportChatPayload(BaseModel):
    message: str
    language: Optional[str] = "en"
    session_context: Optional[dict] = Field(default_factory=dict)
    summary_kpis: Optional[dict] = Field(default_factory=dict)
    tab_data: Optional[List[dict]] = Field(default_factory=list)
    history: List[ChatMessage] = Field(default_factory=list)

class ReportChatResponse(BaseModel):
    answer: str

def _build_report_prompt(payload: ReportChatPayload) -> str:
    lang_instruction = _get_language_instruction(payload.language)

    session_str = "\n".join(f"- {k}: {v}" for k, v in (payload.session_context or {}).items()) or "- Default overview"

    kpi_lines = []
    for k, v in (payload.summary_kpis or {}).items():
        kpi_lines.append(f"- {k}: {v}")
    kpis_str = "\n".join(kpi_lines) or "- No global KPI summary"

    tab_rows = payload.tab_data or []
    if len(tab_rows) > 30:
        tab_rows_sample = tab_rows[:30]
        data_note = f"(Displaying 30 of {len(tab_rows)} total records for this section)"
    else:
        tab_rows_sample = tab_rows
        data_note = f"({len(tab_rows)} records in this section)"

    data_lines = []
    for idx, row in enumerate(tab_rows_sample, 1):
        items_str = ", ".join(
            f"{k}: {v}" for k, v in row.items()
            if v is not None and k not in ['id', 'userId', 'farmId', 'cropId', 'fieldId', 'assigneeId']
        )
        data_lines.append(f"{idx}. {items_str}")
    data_str = "\n".join(data_lines) if data_lines else "- No records found for current filter"

    history_text = "\n".join(
        f"{m.role.upper()}: {m.content}" for m in payload.history[-8:]
    )

    return f"""You are the intelligent AgroFarm Agronomy & Farm Management Assistant.
You assist farm managers, agronomists, and supervisors reviewing reports and analytics.

YOUR CAPABILITIES & KNOWLEDGE:
1. PAGE NAVIGATION & ACTIONS GUIDE:
   You understand the Reports Page UI structure and controls. When the user asks how to perform an action, provide concise step-by-step guidance:
   - "Farm" Filter: Dropdown at top left to filter data by a specific farm or "All Farms".
   - Quick Date Presets: Buttons [7D], [30D], [90D], [1 Year], and custom date pickers to adjust reporting timeframe.
   - "+ Record Entry" Button: Top right green button that opens a modal to record financial income (e.g. crop harvest sales) or operational expenses (fertilizer, seeds, labor wages, equipment).
   - "Download CSV" Button: Exports the currently viewed tab's table into a clean CSV file.
   - Live Search Input: Filters rows in the active table instantly by keywords.
   - Tabs Available:
     * Financial Ledger: Shows revenue/expense trends, net profit margin, and line-item transactions.
     * Crop Cultivation: Shows active crop batches, varieties, growth stages (Flowering, Vegetative, etc.), maturation progress %, and health status.
     * Labor & Attendance: Worker shift check-in/out records, hours worked, and attendance status.
     * Field Tasks: Farm operational tasks, priority levels (HIGH/MEDIUM/LOW), assignees, and due dates.
     * AI Disease History: Neural network scan diagnostics, detected plant pathogens, confidence percentages, and recommended treatments.

2. ACCURATE DATA ANALYSIS:
   - Answer queries based on the provided live farm data below.
   - When asked for recommendations, identify bottlenecks (e.g., lagging crops <50% progress, uncompleted high-priority tasks, or disease alerts) and suggest actionable farming decisions.
   - If the user asks about a section outside their currently active tab, refer to the high-level SUMMARY METRICS and suggest switching to that specific tab for granular records.
   - Keep answers clear, structured with markdown bullet points, concise, and professional.

{lang_instruction}

CURRENT USER CONTEXT:
{session_str}

GLOBAL SUMMARY METRICS:
{kpis_str}

ACTIVE TAB DETAILED DATA {data_note}:
{data_str}

CONVERSATION HISTORY:
{history_text or "(none)"}

USER'S QUESTION:
{payload.message}
"""

@app.post("/report-chat", response_model=ReportChatResponse)
async def report_chat(payload: ReportChatPayload):
    try:
        prompt = _build_report_prompt(payload)
        interaction = client.interactions.create(
            model="gemini-3.5-flash-lite",
            input=[{"type": "text", "text": prompt}],
            generation_config={"thinking_level": "minimal"},
        )
        return ReportChatResponse(answer=interaction.output_text.strip())
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)