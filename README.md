# LangMaster Android (WIP)

## Current structure

App now follows your requested 3-tab model:
1. **Connect**: chat + voice recording + voice/video calls with translation controls.
2. **AI Translate**: same style conversation UI but with AI agent for text/image/voice/video translation and sharing.
3. **Learn**: AI language-learning modules (beginner -> moderate -> advanced -> certification).

Launch language list:
- English
- Hindi
- Gujarati
- Marathi
- Tamil

## What is implemented now
- Compose app shell with the 3-tab flow (Connect / AI Translate / Learn).
- `LangMasterViewModel` wired to Room repositories for real local persistence.
- Connect tab persists messages and enforces 24-hour "delete for everyone" behavior.
- Listener translation toggle and language preference persisted per member.
- AI Translate tab saves text translation sessions to Room and shows recent history.
- Learn tab seeds phase-based tracks/modules and stores progress updates.

## Build instructions
1. Use JDK 17
2. Open in Android Studio
3. Sync and build debug APK

```bash
./gradlew :app:assembleDebug
```

Expected APK output path:

`app/build/outputs/apk/debug/app-debug.apk`

## Local backend (Node + Socket.IO)
This repo now includes a lightweight backend for auth OTP scaffolding and call signaling.

```bash
cd backend
npm install
npm run dev
```

Backend default URL: `http://localhost:8080`
Copy backend environment template before running:

```bash
cd backend
cp .env.example .env
```

Endpoints:
- `GET /health`
- `POST /auth/register-pin`
- `POST /auth/login-pin`
- `POST /translate/text`
- `GET /learn/modules/:language`

App integration notes:
- `AI Translate` tab service path uses `POST /translate/text`.
- `Learn` tab module sync path uses `GET /learn/modules/:language` when backend mode is enabled.
- AI Translate output can be shared via Android share sheet and WhatsApp package target fallback.

Socket.IO events:
- `join-room`
- `signal`

## Production build tasks checklist
1. Android:
   - Configure `local.properties` with Android SDK path.
   - Install JDK 17 in Android Studio.
   - Build release bundle/APK from **Build > Generate Signed Bundle/APK**.
2. Backend:
   - Set environment variable `PORT` (default 8080).
   - Set strong `AUTH_TOKEN_SECRET`.
   - Harden PIN auth with rate limits + secure persistent storage.
   - Build with `npm run build` and run with `npm run start`.
3. Deployment:
   - Run backend in Docker or VM.
   - Point Android app API base URL to backend host.
   - Enable HTTPS and secure token signing before production rollout.

## Windows automation scripts
- `scripts/setup_windows.ps1` → clone/setup repo under `D:\\Dev\\langmaster`
- `scripts/build_android.ps1` → build debug APK using committed Gradle wrapper
- `scripts/build_backend.ps1` → install/build/start backend service

## CI pipelines
- `.github/workflows/android-build.yml` runs Android build on push/PR.
- `.github/workflows/backend-build.yml` runs backend build + tests on push/PR.

## One-command build helpers
Use the root `Makefile`:

```bash
make android-debug
make backend-build
make backend-test
make compose-up
```

## Handoff: move code to your laptop/GitHub

### Option A (preferred): normal git remote push/pull
1. On your laptop:
   - `git clone https://github.com/naimishkathrani-web/langmaster.git D:\\Dev\\langmaster`
2. In this workspace (or any environment that has repo write access):
   - `git push origin work`
3. On your laptop:
   - `cd D:\\Dev\\langmaster`
   - `git checkout work`
   - `git pull origin work`

### Option B (fallback): git bundle transfer (no direct push required)
1. Create bundle in source environment:
   - `./scripts/export_bundle.sh langmaster-work.bundle work`
2. Copy `langmaster-work.bundle` to your laptop (e.g., `D:\\Dev\\langmaster-work.bundle`).
3. Import on laptop:
   - `powershell -ExecutionPolicy Bypass -File .\\scripts\\import_bundle.ps1 -RepoPath \"D:\\Dev\\langmaster\" -BundlePath \"D:\\Dev\\langmaster-work.bundle\" -Branch \"work\"`

## Pending production items you can choose next
- Harden PIN authentication with account lockouts + secure server persistence.
- End-to-end encryption key lifecycle and backup-safe key restoration.
- Release signing, Play Store config, and secure secrets management.
- Real media pipeline (voice/video translation processing) replacing current placeholders.
