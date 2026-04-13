CREATE TABLE users (
  id TEXT PRIMARY KEY NOT NULL,
  phone_e164 TEXT NOT NULL,
  display_name TEXT NOT NULL,
  google_account_email TEXT,
  avatar_uri TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE contacts (
  id TEXT PRIMARY KEY NOT NULL,
  phone_e164 TEXT NOT NULL UNIQUE,
  display_name TEXT NOT NULL,
  avatar_uri TEXT,
  is_app_user INTEGER NOT NULL,
  last_synced_at INTEGER NOT NULL
);

CREATE TABLE conversations (
  id TEXT PRIMARY KEY NOT NULL,
  type TEXT NOT NULL,
  title TEXT,
  avatar_uri TEXT,
  created_by TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  last_message_id TEXT
);

CREATE TABLE messages (
  id TEXT PRIMARY KEY NOT NULL,
  conversation_id TEXT NOT NULL,
  sender_phone_e164 TEXT NOT NULL,
  message_type TEXT NOT NULL,
  body TEXT,
  reply_to_message_id TEXT,
  created_at INTEGER NOT NULL,
  edited_at INTEGER,
  deleted_for_everyone_at INTEGER,
  deleted_for_me INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY(conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at);

CREATE TABLE translation_sessions (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  input_mode TEXT NOT NULL,
  source_lang TEXT NOT NULL,
  target_lang TEXT NOT NULL,
  input_text TEXT,
  output_text TEXT,
  input_audio_path TEXT,
  output_audio_path TEXT,
  created_at INTEGER NOT NULL
);

CREATE TABLE learning_tracks (
  id TEXT PRIMARY KEY NOT NULL,
  language_code TEXT NOT NULL,
  level TEXT NOT NULL,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  certification_hint TEXT
);

CREATE TABLE learning_modules (
  id TEXT PRIMARY KEY NOT NULL,
  track_id TEXT NOT NULL,
  phase_order INTEGER NOT NULL,
  title TEXT NOT NULL,
  goal TEXT NOT NULL,
  content_markdown TEXT NOT NULL,
  FOREIGN KEY(track_id) REFERENCES learning_tracks(id) ON DELETE CASCADE
);

CREATE TABLE learner_progress (
  id TEXT PRIMARY KEY NOT NULL,
  user_id TEXT NOT NULL,
  module_id TEXT NOT NULL,
  status TEXT NOT NULL,
  score_percent INTEGER NOT NULL DEFAULT 0,
  updated_at INTEGER NOT NULL,
  FOREIGN KEY(module_id) REFERENCES learning_modules(id) ON DELETE CASCADE
);
