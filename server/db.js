require('dotenv').config({ path: require('path').resolve(__dirname, '../.env.local') });
const { Pool } = require('pg');

const pool = new Pool({
  host: process.env.PG_HOST,
  port: parseInt(process.env.PG_PORT || '5432'),
  user: process.env.PG_USER,
  password: process.env.PG_PASSWORD,
  database: process.env.PG_DB,
});

const SCHEMA = '"SocialMedia_dev"';

async function query(sql, params) {
  return pool.query(sql, params);
}

// --- Contacts ---
async function upsertContact({ wa_jid, name, phone, is_group }) {
  const res = await query(
    `INSERT INTO ${SCHEMA}.contacts (wa_jid, name, phone, is_group, last_scraped_at)
     VALUES ($1, $2, $3, $4, now())
     ON CONFLICT (wa_jid) DO UPDATE SET name = EXCLUDED.name, phone = EXCLUDED.phone, is_group = EXCLUDED.is_group, last_scraped_at = now()
     RETURNING id`,
    [wa_jid, name, phone, is_group]
  );
  return res.rows[0]?.id;
}

async function getContacts() {
  const res = await query(`SELECT * FROM ${SCHEMA}.contacts ORDER BY name`);
  return res.rows;
}

// --- Messages ---
async function insertMessage({ contact_id, wa_message_id, message_body, message_type, has_links, timestamp }) {
  const res = await query(
    `INSERT INTO ${SCHEMA}.messages (contact_id, wa_message_id, message_body, message_type, has_links, timestamp)
     VALUES ($1, $2, $3, $4, $5, $6)
     ON CONFLICT (wa_message_id) DO UPDATE SET message_body = EXCLUDED.message_body, has_links = EXCLUDED.has_links
     RETURNING id`,
    [contact_id, wa_message_id, message_body, message_type, has_links, timestamp]
  );
  return res.rows[0]?.id;
}

async function getMessagesWithLinks(contactId) {
  const res = await query(
    `SELECT m.*, c.name as contact_name, c.wa_jid
     FROM ${SCHEMA}.messages m
     JOIN ${SCHEMA}.contacts c ON c.id = m.contact_id
     WHERE m.has_links = true
     ORDER BY m.timestamp DESC`,
  );
  return res.rows;
}

async function getAllMessages() {
  const res = await query(
    `SELECT m.*, c.name as contact_name, c.wa_jid
     FROM ${SCHEMA}.messages m
     JOIN ${SCHEMA}.contacts c ON c.id = m.contact_id
     ORDER BY m.timestamp DESC`,
  );
  return res.rows;
}

// --- Links ---
async function insertLink({ message_id, url, domain, link_type }) {
  const res = await query(
    `INSERT INTO ${SCHEMA}.links (message_id, url, domain, link_type)
     VALUES ($1, $2, $3, $4)
     ON CONFLICT DO NOTHING
     RETURNING id`,
    [message_id, url, domain, link_type]
  );
  return res.rows[0]?.id;
}

async function updateLinkScraped(id, { title, description, og_image, raw_content }) {
  await query(
    `UPDATE ${SCHEMA}.links SET title = $2, description = $3, og_image = $4, raw_content = $5, processing_status = 'scraped' WHERE id = $1`,
    [id, title, description, og_image, raw_content]
  );
}

async function updateLinkAI(id, { ai_summary, ai_detailed_analysis, ai_tags }) {
  await query(
    `UPDATE ${SCHEMA}.links SET ai_summary = $2, ai_detailed_analysis = $3, ai_tags = $4, processing_status = 'completed', processed_at = now() WHERE id = $1`,
    [id, ai_summary, ai_detailed_analysis, ai_tags]
  );
}

async function updateLinkStatus(id, status) {
  await query(
    `UPDATE ${SCHEMA}.links SET processing_status = $2 WHERE id = $1`,
    [id, status]
  );
}

async function getLinksForViewing(filters = {}) {
  let sql = `
    SELECT l.*,
           m.message_body, m.timestamp as msg_timestamp, m.wa_message_id,
           c.name as contact_name, c.wa_jid
    FROM ${SCHEMA}.links l
    JOIN ${SCHEMA}.messages m ON m.id = l.message_id
    JOIN ${SCHEMA}.contacts c ON c.id = m.contact_id
  `;
  const conditions = [];
  const params = [];
  let idx = 1;

  if (filters.contact_id) {
    conditions.push(`m.contact_id = $${idx++}`);
    params.push(filters.contact_id);
  }
  if (filters.link_type) {
    conditions.push(`l.link_type = $${idx++}`);
    params.push(filters.link_type);
  }
  if (filters.status) {
    conditions.push(`l.processing_status = $${idx++}`);
    params.push(filters.status);
  }
  if (conditions.length > 0) {
    sql += ' WHERE ' + conditions.join(' AND ');
  }
  sql += ' ORDER BY m.timestamp DESC';

  const res = await query(sql, params);
  return res.rows;
}

async function getLinkDetail(linkId) {
  const res = await query(
    `SELECT l.*,
            m.message_body, m.timestamp as msg_timestamp, m.wa_message_id,
            c.name as contact_name, c.wa_jid
     FROM ${SCHEMA}.links l
     JOIN ${SCHEMA}.messages m ON m.id = l.message_id
     JOIN ${SCHEMA}.contacts c ON c.id = m.contact_id
     WHERE l.id = $1`,
    [linkId]
  );
  return res.rows[0];
}

async function getPendingLinks(limit = 10) {
  const res = await query(
    `SELECT l.* FROM ${SCHEMA}.links l WHERE l.processing_status = 'pending' ORDER BY l.created_at ASC LIMIT $1`,
    [limit]
  );
  return res.rows;
}

async function getStats() {
  const [contacts, messages, links, completed] = await Promise.all([
    query(`SELECT count(*) as cnt FROM ${SCHEMA}.contacts`),
    query(`SELECT count(*) as cnt FROM ${SCHEMA}.messages`),
    query(`SELECT count(*) as cnt FROM ${SCHEMA}.links`),
    query(`SELECT count(*) as cnt FROM ${SCHEMA}.links WHERE processing_status = 'completed'`),
  ]);
  return {
    contacts: parseInt(contacts.rows[0].cnt),
    messages: parseInt(messages.rows[0].cnt),
    links: parseInt(links.rows[0].cnt),
    completed: parseInt(completed.rows[0].cnt),
  };
}

module.exports = {
  pool, query,
  upsertContact, getContacts,
  insertMessage, getMessagesWithLinks, getAllMessages,
  insertLink, updateLinkScraped, updateLinkAI, updateLinkStatus,
  getLinksForViewing, getLinkDetail, getPendingLinks, getStats,
};
