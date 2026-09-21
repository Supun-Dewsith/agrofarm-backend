import base64
from typing import List, Optional
from dotenv import load_dotenv
from fastapi import FastAPI, UploadFile, File, HTTPException
from pydantic import BaseModel, Field
from google import genai

load_dotenv()

app = FastAPI(title="Plant Disease Analysis Service")
client = genai.Client()

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

class DiseaseChatResponse(BaseModel):
    answer: str


def _build_context_prompt(analysis: Optional[DiseaseAnalysis]) -> str:
    if analysis is None:
        return (
            "You are an agronomy assistant. The farmer has not completed a leaf scan yet. "
            "Ask them to run diagnostics first if they need crop-specific advice."
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
async def analyze_leaf(file: UploadFile = File(...)):
    try:
        image_bytes = await file.read()
        encoded_image = base64.b64encode(image_bytes).decode("utf-8")

        prompt = """
        Analyze this leaf image for signs of plant disease.
        Identify the disease, describe it, provide treatment steps, and return
        bounding box coordinates [ymin, xmin, ymax, xmax] normalized on a 0-1000 scale
        for each detected spot.
        """

        interaction = client.interactions.create(
            model="gemini-3.6-flash",
            input=[
                {"type": "text", "text": prompt},
                {
                    "type": "image",
                    "data": encoded_image,
                    "mime_type": file.content_type or "image/jpeg"
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
        prompt = f"""{_build_context_prompt(payload.analysis)}
Conversation so far:
{history_text or "(none)"}
FARMER QUESTION:
{payload.question}
"""
        interaction = client.interactions.create(
            model="gemini-3.6-flash",
            input=[{"type": "text", "text": prompt}],
            generation_config={"thinking_level": "minimal"},
        )
        return DiseaseChatResponse(answer=interaction.output_text.strip())
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)