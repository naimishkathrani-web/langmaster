# LangMaster V1 — UI + Data Design

## 3-tab product model

### Tab 1: Connect
- WhatsApp-like communication surface:
  - chat
  - voice notes
  - voice calls
  - video calls
- Per-listener translation switch and preferred language.

### Tab 2: AI Translate
- Same conversational UX style as Connect but with AI agent.
- Translation flows:
  - text-to-text
  - image text translation
  - voice translation
  - video with translated voice output
- Share translated output to other apps (including WhatsApp) via Android share intents.

### Tab 3: Learn
- AI teaching assistant for any selected language.
- Module progression:
  - beginner
  - moderate
  - advanced
  - certification readiness

## Launch languages
- English, Hindi, Gujarati, Marathi, Tamil.

## Data model highlights
- `translation_sessions` for AI Translate tab history.
- `learning_tracks`, `learning_modules`, `learner_progress` for Learn tab progression.
- Core conversation tables remain for Connect tab.
