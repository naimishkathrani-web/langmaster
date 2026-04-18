import sqlite3 from 'sqlite3';
import { open, Database } from 'sqlite';
import path from 'path';

let dbInstance: Database | null = null;

export async function getDb(): Promise<Database> {
  if (dbInstance) return dbInstance;

  dbInstance = await open({
    filename: path.join(process.cwd(), 'langmaster.sqlite'),
    driver: sqlite3.Database
  });

  await dbInstance.exec(`
    CREATE TABLE IF NOT EXISTS users (
      phone TEXT PRIMARY KEY,
      pin TEXT NOT NULL,
      failed_attempts INTEGER DEFAULT 0,
      locked_until INTEGER DEFAULT 0
    )
  `);

  return dbInstance;
}
