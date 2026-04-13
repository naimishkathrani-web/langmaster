import request from "supertest";
import { describe, expect, it } from "vitest";
import { createApp } from "../src/app.js";

describe("backend auth + health", () => {
  it("returns health", async () => {
    const res = await request(createApp()).get("/health");
    expect(res.status).toBe(200);
    expect(res.body.ok).toBe(true);
  });

  it("registers and logs in with pin", async () => {
    const app = createApp();
    const phone = "+919111111111";
    const pin = "1234";

    const register = await request(app).post("/auth/register-pin").send({ phone, pin });
    expect(register.status).toBe(200);
    expect(register.body.ok).toBe(true);

    const login = await request(app).post("/auth/login-pin").send({ phone, pin });
    expect(login.status).toBe(200);
    expect(login.body.ok).toBe(true);
    expect(typeof login.body.token).toBe("string");
  });

  it("translates text", async () => {
    const app = createApp();
    const response = await request(app).post("/translate/text").send({
      sourceLang: "English",
      targetLang: "Hindi",
      text: "Hello"
    });
    expect(response.status).toBe(200);
    expect(response.body.translatedText).toContain("[Hindi]");
  });
});
