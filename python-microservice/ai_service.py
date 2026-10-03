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

class DiseaseAnalysis(BaseModel):
    plant_name: str
    is_healthy: bool
    disease_name: str
    overall_confidence: float
    general_description: str
    detected_spots: List[DetectedSpot]
    possible_treatments: List[str]
    prevention_tips: List[str]

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
            "general_description, detected spot descriptions, possible_treatments, and prevention_tips) "
            "MUST be written in clear, natural Sinhala (සිංහල). "
            "Common agricultural or botanical terms can include English names in parentheses if helpful for clarity."
        )
    elif lang.startswith("ta"):
        return (
            "LANGUAGE REQUIREMENT: Respond in Tamil (தமிழ்). "
            "All text values in the response MUST be written in clear and natural Tamil."
        )
    elif lang.startswith("en"):
        return "LANGUAGE REQUIREMENT: Respond in English."
    else:
        return f"LANGUAGE REQUIREMENT: Respond in {language}."


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
    return f"""You are an agronomy assistant for AgroFarm. Answer only using this detection result.
If the farmer asks something not covered, say so and give cautious general advice.
Keep answers practical and short.
{lang_instruction}

DETECTED SCAN:
- Plant: {analysis.plant_name}
- Healthy: {analysis.is_healthy}
- Disease: {analysis.disease_name}
- Model confidence: {analysis.overall_confidence}
- Overview: {analysis.general_description}
Detected spots:
{spots}
Treatments:
{treatments}
Prevention:
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
        Analyze this leaf image for signs of plant disease.
        Identify the disease, describe it, provide treatment steps, and return
        bounding box coordinates [ymin, xmin, ymax, xmax] normalized on a 0-1000 scale
        for each detected spot.

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
                    # forces low-latency basic detection
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

        return DiseaseAnalysis.model_validate_json(interaction.output_text)

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