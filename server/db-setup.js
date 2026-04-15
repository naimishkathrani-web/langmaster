require('dotenv').config({ path: require('path').resolve(__dirname, '../.env.local') });
const { Client } = require('pg');

async function setup() {
  const client = new Client({
    host: process.env.PG_HOST,
    port: parseInt(process.env.PG_PORT || '5432'),
    user: process.env.PG_USER,
    password: process.env.PG_PASSWORD,
    database: 'postgres', // connect to default db first
  });

  try {
    await client.connect();

    // Create database if not exists
    const dbRes = await client.query(
      `SELECT 1 FROM pg_database WHERE datname = $1`,
      [process.env.PG_DB]
    );
    if (dbRes.rows.length === 0) {
      await client.query(`CREATE DATABASE "${process.env.PG_DB}"`);
      console.log(`Database "${process.env.PG_DB}" created.`);
    } else {
      console.log(`Database "${process.env.PG_DB}" already exists.`);
    }
    await client.end();
  } catch (err) {
    console.error('Error creating database:', err.message);
    await client.end().catch(() => {});
    process.exit(1);
  }

  // Connect to the target database and create schema + tables
  const dbClient = new Client({
    host: process.env.PG_HOST,
    port: parseInt(process.env.PG_PORT || '5432'),
    user: process.env.PG_USER,
    password: process.env.PG_PASSWORD,
    database: process.env.PG_DB,
  });

  try {
    await dbClient.connect();

    await dbClient.query(`CREATE SCHEMA IF NOT EXISTS "SocialMedia_dev"`);
    console.log('Schema "SocialMedia_dev" ensured.');

    await dbClient.query(`
      SET search_path TO "SocialMedia_dev", public;
    `);

    // Contacts table - WhatsApp contacts we've scraped from
    await dbClient.query(`
      CREATE TABLE IF NOT EXISTS "SocialMedia_dev".contacts (
        id SERIAL PRIMARY KEY,
        wa_jid TEXT UNIQUE NOT NULL,
        name TEXT,
        phone TEXT,
        is_group BOOLEAN DEFAULT false,
        last_scraped_at TIMESTAMPTZ,
        created_at TIMESTAMPTZ DEFAULT now()
      )
    `);

    // Messages table - raw WhatsApp messages
    await dbClient.query(`
      CREATE TABLE IF NOT EXISTS "SocialMedia_dev".messages (
        id SERIAL PRIMARY KEY,
        contact_id INTEGER REFERENCES "SocialMedia_dev".contacts(id),
        wa_message_id TEXT UNIQUE,
        message_body TEXT,
        message_type TEXT DEFAULT 'text',
        has_links BOOLEAN DEFAULT false,
        timestamp TIMESTAMPTZ,
        scraped_at TIMESTAMPTZ DEFAULT now()
      )
    `);

    // Links table - extracted links from messages
    await dbClient.query(`
      CREATE TABLE IF NOT EXISTS "SocialMedia_dev".links (
        id SERIAL PRIMARY KEY,
        message_id INTEGER REFERENCES "SocialMedia_dev".messages(id),
        url TEXT NOT NULL,
        domain TEXT,
        link_type TEXT DEFAULT 'unknown',
        title TEXT,
        description TEXT,
        og_image TEXT,
        raw_content TEXT,
        ai_summary TEXT,
        ai_detailed_analysis TEXT,
        ai_tags TEXT[],
        processing_status TEXT DEFAULT 'pending',
        processed_at TIMESTAMPTZ,
        created_at TIMESTAMPTZ DEFAULT now()
      )
    `);

    // Indexes
    await dbClient.query(`CREATE INDEX IF NOT EXISTS idx_messages_contact ON "SocialMedia_dev".messages(contact_id)`);
    await dbClient.query(`CREATE INDEX IF NOT EXISTS idx_links_message ON "SocialMedia_dev".links(message_id)`);
    await dbClient.query(`CREATE INDEX IF NOT EXISTS idx_links_status ON "SocialMedia_dev".links(processing_status)`);
    await dbClient.query(`CREATE INDEX IF NOT EXISTS idx_links_type ON "SocialMedia_dev".links(link_type)`);

    console.log('All tables created/verified.');
    await dbClient.end();
  } catch (err) {
    console.error('Error setting up schema/tables:', err.message);
    await dbClient.end().catch(() => {});
    process.exit(1);
  }
}

setup();
