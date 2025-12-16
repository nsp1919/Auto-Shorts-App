"""
Auto Shorts App - FastAPI Backend
Handles video upload, processing, and AI metadata generation.
"""

from fastapi import FastAPI, UploadFile, File, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional, List
import uuid
import asyncio
import os
from datetime import datetime
import random

app = FastAPI(
    title="Auto Shorts API",
    description="Backend API for Auto Shorts Android App",
    version="1.0.0"
)

# CORS for mobile app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory storage (use database in production)
videos = {}
jobs = {}

# ============ Models ============

class YouTubeUploadRequest(BaseModel):
    url: str

class ProcessRequest(BaseModel):
    video_id: str
    duration: int = 60
    quantity: int = 3
    language: str = "en"

class CaptionStyle(BaseModel):
    style: str = "Modern"
    color: str = "#FFFFFF"
    font_size: int = 24
    position: str = "bottom"

class RegenerateRequest(BaseModel):
    job_id: str
    clip_id: str
    caption_style: CaptionStyle

class RocketRequest(BaseModel):
    clip_id: str
    platform: Optional[str] = None

class ShareRequest(BaseModel):
    clip_id: str
    video_url: str
    title: Optional[str] = None
    description: Optional[str] = None
    hashtags: Optional[List[str]] = None

# ============ Endpoints ============

@app.get("/")
async def root():
    return {"message": "Auto Shorts API is running!", "version": "1.0.0"}

@app.get("/health")
async def health():
    return {"status": "healthy"}

# Upload video file
@app.post("/api/upload")
async def upload_video(
    file: Optional[UploadFile] = File(None),
    url: Optional[str] = None
):
    """Upload a video file or YouTube URL."""
    video_id = str(uuid.uuid4())[:8]
    
    if file:
        # Handle file upload
        filename = file.filename or "video.mp4"
        videos[video_id] = {
            "id": video_id,
            "filename": filename,
            "source": "file",
            "uploaded_at": datetime.now().isoformat()
        }
        return {
            "video_id": video_id,
            "filename": filename,
            "message": "Video uploaded successfully",
            "status": "success"
        }
    elif url:
        # Handle YouTube URL (simulation)
        videos[video_id] = {
            "id": video_id,
            "filename": f"youtube_{video_id}.mp4",
            "source": "youtube",
            "url": url,
            "uploaded_at": datetime.now().isoformat()
        }
        return {
            "video_id": video_id,
            "filename": f"youtube_{video_id}.mp4",
            "message": "YouTube video queued for download",
            "status": "success"
        }
    else:
        raise HTTPException(status_code=400, detail="No file or URL provided")

# Upload via JSON body (for YouTube URLs)
@app.post("/api/upload/url")
async def upload_youtube_url(request: YouTubeUploadRequest):
    """Upload video via YouTube URL."""
    video_id = str(uuid.uuid4())[:8]
    videos[video_id] = {
        "id": video_id,
        "filename": f"youtube_{video_id}.mp4",
        "source": "youtube",
        "url": request.url,
        "uploaded_at": datetime.now().isoformat()
    }
    return {
        "video_id": video_id,
        "filename": f"youtube_{video_id}.mp4",
        "message": "YouTube video queued for processing",
        "status": "success"
    }

# Start processing
@app.post("/api/process")
async def process_video(request: ProcessRequest, background_tasks: BackgroundTasks):
    """Start video processing."""
    if request.video_id not in videos:
        raise HTTPException(status_code=404, detail="Video not found")
    
    job_id = str(uuid.uuid4())[:8]
    
    # Create job
    jobs[job_id] = {
        "job_id": job_id,
        "video_id": request.video_id,
        "status": "processing",
        "progress": 0,
        "current_step": "Initializing...",
        "steps": [
            {"name": "Analyzing Video", "status": "pending"},
            {"name": "Extracting Highlights", "status": "pending"},
            {"name": "Generating Captions", "status": "pending"},
            {"name": "Creating Shorts", "status": "pending"},
            {"name": "Finalizing", "status": "pending"}
        ],
        "clips": [],
        "duration": request.duration,
        "quantity": request.quantity,
        "language": request.language,
        "created_at": datetime.now().isoformat()
    }
    
    # Simulate processing in background
    background_tasks.add_task(simulate_processing, job_id, request.quantity)
    
    return {
        "job_id": job_id,
        "message": "Processing started",
        "status": "processing"
    }

async def simulate_processing(job_id: str, quantity: int):
    """Simulate video processing with progress updates."""
    if job_id not in jobs:
        return
    
    steps = ["Analyzing Video", "Extracting Highlights", "Generating Captions", "Creating Shorts", "Finalizing"]
    
    for i, step in enumerate(steps):
        # Update current step
        jobs[job_id]["current_step"] = step
        jobs[job_id]["steps"][i]["status"] = "in_progress"
        
        # Simulate work
        await asyncio.sleep(2)
        
        # Update progress
        progress = int(((i + 1) / len(steps)) * 100)
        jobs[job_id]["progress"] = progress
        jobs[job_id]["steps"][i]["status"] = "completed"
    
    # Generate clips
    clips = []
    for j in range(quantity):
        clip_id = f"clip_{job_id}_{j+1}"
        clips.append({
            "id": clip_id,
            "url": f"https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
            "thumbnail_url": f"https://picsum.photos/seed/{clip_id}/270/480",
            "duration": random.choice([30, 45, 60]),
            "title": f"Viral Short #{j+1}"
        })
    
    jobs[job_id]["clips"] = clips
    jobs[job_id]["status"] = "completed"
    jobs[job_id]["current_step"] = "Complete!"

# Get job status
@app.get("/api/process/status/{job_id}")
async def get_job_status(job_id: str):
    """Get the status of a processing job."""
    if job_id not in jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job = jobs[job_id]
    return {
        "job_id": job["job_id"],
        "status": job["status"],
        "progress": job["progress"],
        "current_step": job["current_step"],
        "steps": job["steps"],
        "clips": job.get("clips", []),
        "estimated_time_remaining": max(0, 100 - job["progress"]) // 10 * 2
    }

# Regenerate clip
@app.post("/api/process/regenerate")
async def regenerate_clip(request: RegenerateRequest):
    """Regenerate a clip with new caption style."""
    if request.job_id not in jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    # Simulate regeneration
    await asyncio.sleep(2)
    
    return {
        "job_id": request.job_id,
        "clip_id": request.clip_id,
        "url": f"https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
        "status": "regenerated"
    }

# Generate rocket metadata
@app.post("/api/rocket/generate")
async def generate_rocket_metadata(request: RocketRequest):
    """Generate AI-powered metadata for sharing."""
    
    # Simulate AI generation
    titles = [
        "🔥 This changed everything!",
        "Wait for it... 😱",
        "POV: You just discovered this",
        "Nobody talks about this enough",
        "This is why you need to see this"
    ]
    
    descriptions = [
        "You won't believe what happens next! Watch until the end 👀",
        "This is the content you've been waiting for ✨",
        "Drop a comment if you agree! 💬",
        "Save this for later, you'll thank me 🙏",
        "Tag someone who needs to see this 👇"
    ]
    
    hashtags = ["fyp", "viral", "trending", "shorts", "reels", 
                "explore", "foryou", "trend", "video", "content"]
    
    return {
        "title": random.choice(titles),
        "description": random.choice(descriptions),
        "hashtags": random.sample(hashtags, 5),
        "suggested_time": "6:00 PM - 9:00 PM"
    }

# Share to platform
@app.post("/api/share/{platform}")
async def share_video(platform: str, request: ShareRequest):
    """Share video to a platform."""
    valid_platforms = ["instagram", "youtube", "tiktok"]
    
    if platform.lower() not in valid_platforms:
        raise HTTPException(status_code=400, detail=f"Invalid platform. Use: {valid_platforms}")
    
    return {
        "success": True,
        "message": f"Video ready to share on {platform.title()}",
        "share_url": f"https://{platform}.com/share?video={request.clip_id}"
    }

# ============ Run Server ============

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run(app, host="0.0.0.0", port=port)
