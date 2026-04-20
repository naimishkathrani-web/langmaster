from fastapi import FastAPI, UploadFile, File, HTTPException
import uvicorn
import shutil
import os
import whisper
import asyncio

app = FastAPI(title="LangMaster AI Server")

# Load whisper model globally (Downloads the 'base' model automatically on first run)
print("Loading Whisper model...")
model = whisper.load_model("base")
print("Whisper ready!")

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.get("/health")
def health_check():
    """Simple status check"""
    return {"status": "ok", "service": "LangMaster FastAPI"}

@app.post("/api/transcribe")
async def transcribe_audio(file: UploadFile = File(...)):
    """Accepts an audio file and returns its text transcription"""
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    # Process transcription natively
    result = model.transcribe(file_path)
    return {"status": "success", "file_saved": file.filename, "text": result["text"]}

@app.post("/api/dub-video")
async def dub_video(file: UploadFile = File(...), target_lang: str = "hi"):
    """Accepts an MP4, strips audio, translates, generates TTS, and muxes."""
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    # TODO: Implement ffmpeg separation, Whisper translation, and Edge-TTS dubbing
    return {"status": "success", "message": "Video received for dubbing."}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
