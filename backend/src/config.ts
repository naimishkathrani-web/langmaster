import { z } from "zod";

const envSchema = z.object({
  PORT: z.coerce.number().default(8080),
  AUTH_TOKEN_SECRET: z.string().min(8).default("dev-secret")
});

export const env = envSchema.parse({
  PORT: process.env.PORT,
  AUTH_TOKEN_SECRET: process.env.AUTH_TOKEN_SECRET
});
