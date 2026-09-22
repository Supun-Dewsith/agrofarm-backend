FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000

# Adjust command based on your framework (FastAPI/Uvicorn, Flask, etc.)
CMD ["python", "ai_service.py"]

#need to create low privilage user othervise service wil run root privilage