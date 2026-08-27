import base64
import hashlib
from typing import List
from dotenv import load_dotenv
from fastapi import FastAPI, UploadFile, File, HTTPException
from pydantic import BaseModel, Field
from google import genai

load_dotenv()

app = FastAPI(title="Plant Disease Analysis Service")
client = genai.Client()

# Schema definitions
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

@app.post("/analyze-leaf", response_model=DiseaseAnalysis)
async def analyze_leaf(file: UploadFile = File(...)):
    try:
        # 1. Read uploaded image bytes
        image_bytes = await file.read()
        encoded_image = base64.b64encode(image_bytes).decode("utf-8")


        prompt = """
        Analyze this leaf image for signs of plant disease.
        Identify the disease, describe it, provide treatment steps, and return 
        bounding box coordinates [ymin, xmin, ymax, xmax] normalized on a 0-1000 scale 
        for each detected spot.
        """

        # 2. Call Gemini Interactions API
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

        # 3. Parse and return structured output
        return DiseaseAnalysis.model_validate_json(interaction.output_text)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)