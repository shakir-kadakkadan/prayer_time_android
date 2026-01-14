# Prayer Time Audio Hosting

This Firebase hosting project serves the audio files for the Prayer Time Android app.

## Structure

```
htmlHostAudio/
├── public/
│   ├── audio/           # MP3 audio files
│   ├── audio-list.json  # Auto-generated metadata
│   └── index.html       # Landing page redirecting to Play Store
├── firebase.json        # Firebase hosting config
├── .firebaserc          # Firebase project config
└── generate-audio-json.js  # Script to generate metadata
```

## Setup

1. Install Firebase CLI:
```bash
npm install -g firebase-tools
```

2. Login to Firebase:
```bash
firebase login
```

3. Initialize (if needed):
```bash
firebase init hosting
```

## Adding New Audio Files

1. Place new MP3 files in `public/audio/`
2. Generate updated metadata:
```bash
node generate-audio-json.js
```
3. Deploy:
```bash
firebase deploy --only hosting
```

## Deploy

```bash
firebase deploy --only hosting
```

## URLs

- Website: https://prayer-time-shakir.web.app/
- Audio List JSON: https://prayer-time-shakir.web.app/audio-list.json
- Individual Audio: https://prayer-time-shakir.web.app/audio/{filename}.mp3

## Audio List JSON Format

```json
[
  {
    "name": "Audio Name",
    "url": "https://prayer-time-shakir.web.app/audio/filename.mp3",
    "size": 1234567,
    "sizeFormatted": "1.18 MB"
  }
]
```

## Features

- CORS enabled for all audio files
- Long cache control for audio files (1 year)
- Shorter cache for audio-list.json (1 hour)
- Landing page auto-redirects to Play Store after 3 seconds
- Direct link /app redirects to Play Store
