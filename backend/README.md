# Auto Shorts App Backend

FastAPI backend for the Auto Shorts Android application.

## Features
- Video upload (file and YouTube URL)
- Video processing simulation
- Clip generation
- AI metadata generation (Rocket Share)

## Deploy to Render

1. Connect this repo to Render
2. Create a new Web Service
3. Use Python 3 runtime
4. Build command: `pip install -r requirements.txt`
5. Start command: `uvicorn main:app --host 0.0.0.0 --port $PORT`

## API Endpoints

- `POST /api/upload` - Upload video
- `POST /api/process` - Start processing
- `GET /api/process/status/{job_id}` - Get job status
- `POST /api/process/regenerate` - Regenerate clip
- `POST /api/rocket/generate` - Generate AI metadata
- `POST /api/share/{platform}` - Share video

## Environment Variables

- `OPENAI_API_KEY` - For AI metadata generation (optional)
