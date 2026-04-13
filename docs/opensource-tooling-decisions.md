# LangMaster Open-Source Tooling Decisions (V1)

## 1) Authentication/Identity (phone + OTP)
- **Primary OSS stack recommendation:** Keycloak (community edition) + custom SMS OTP bridge.
- Rationale: open source IAM, extensible flows, supports account linking and federated identity.
- Note: SMS transport itself usually requires a telecom gateway/provider account.

## 2) Voice/Video calls
- **Tool:** WebRTC (open source standard stack).
- Rationale: best OSS ecosystem for low-latency peer media, codec support, group call architecture options.

## 3) On-device AI runtime
- **Tooling mix:**
  - `whisper.cpp` for speech-to-text,
  - `llama.cpp` style runtime for Gemma/Qwen GGUF variants,
  - light TTS runtime via ONNX-compatible local models.
- Rationale: strongest open-source on-device ecosystem with quantization support for low-memory tiers.

## Delivery Sequence
1. **Option A first:** chat + voice note translation and local retention.
2. **Option B next:** voice/video calling + live translation toggles.
