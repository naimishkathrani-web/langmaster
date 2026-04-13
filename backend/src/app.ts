import express from "express";
import cors from "cors";
import helmet from "helmet";
import { z } from "zod";
import crypto from "node:crypto";
import { env } from "./config.js";

const pinStore = new Map<string, string>();

export const createApp = () => {
  const app = express();
  app.use(cors());
  app.use(helmet());
  app.use(express.json());

  app.get("/health", (_req, res) => {
    res.json({ ok: true, service: "langmaster-backend", ts: Date.now() });
  });

  app.post("/auth/register-pin", (req, res) => {
    const payload = z
      .object({
        phone: z.string().min(8),
        pin: z.string().regex(/^[0-9]{4}$/)
      })
      .safeParse(req.body);

    if (!payload.success) {
      return res.status(400).json({ ok: false, error: "Invalid phone or pin" });
    }
    pinStore.set(payload.data.phone, payload.data.pin);
    return res.json({ ok: true });
  });

  app.post("/auth/login-pin", (req, res) => {
    const payload = z
      .object({
        phone: z.string().min(8),
        pin: z.string().regex(/^[0-9]{4}$/)
      })
      .safeParse(req.body);

    if (!payload.success) {
      return res.status(400).json({ ok: false, error: "Invalid phone or pin" });
    }
    const savedPin = pinStore.get(payload.data.phone);
    if (!savedPin) return res.status(404).json({ ok: false, error: "User not registered" });
    if (savedPin !== payload.data.pin) return res.status(401).json({ ok: false, error: "Invalid pin" });

    const token = crypto
      .createHmac("sha256", env.AUTH_TOKEN_SECRET)
      .update(`${payload.data.phone}:${Date.now()}`)
      .digest("hex");
    return res.json({ ok: true, token });
  });

  app.post("/translate/text", (req, res) => {
    const payload = z
      .object({
        sourceLang: z.string().min(2),
        targetLang: z.string().min(2),
        text: z.string().min(1)
      })
      .safeParse(req.body);
    if (!payload.success) {
      return res.status(400).json({ ok: false, error: "Invalid translation request" });
    }

    // placeholder deterministic behavior for local development pipeline
    return res.json({
      ok: true,
      translatedText: `[${payload.data.targetLang}] ${payload.data.text}`
    });
  });

  app.get("/learn/modules/:language", (req, res) => {
    const language = req.params.language || "English";
    const modules = [
      { id: `${language}-m1`, phase: 1, title: `${language} Basics` },
      { id: `${language}-m2`, phase: 2, title: `${language} Conversations` },
      { id: `${language}-m3`, phase: 3, title: `${language} Advanced & Certification` }
    ];
    return res.json({ ok: true, language, modules });
  });

  return app;
};
