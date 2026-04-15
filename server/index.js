require('dotenv').config({ path: require('path').resolve(__dirname, '../.env.local') });
const express = require('express');
const cors = require('cors');
const db = require('./db');
const waBridge = require('./whatsapp-bridge');
const { scrapeLink } = require('./link-scraper');
const { checkOllamaAvailable, analyzeContent } = require('./ai-analyzer');

const app = express();
const PORT = parseInt(process.env.PORT || '3099', 10);

app.use(cors());
app.use(express.json());

// --- WhatsApp endpoints ---

app.get('/api/whatsapp/status', (req, res) => {
  res.json(waBridge.getStatus());
});

app.post('/api/whatsapp/start', async (req, res) => {
  try {
    const result = await waBridge.startWhatsApp();
    res.json(result);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.get('/api/whatsapp/chats', async (req, res) => {
  try {
    const chats = await waBridge.getChats();
    res.json({ chats });
  } catch (e) {
    res.status(503).json({ error: e.message });
  }
});

app.get('/api/whatsapp/contacts', async (req, res) => {
  try {
    const contacts = await waBridge.getContacts();
    res.json({ contacts });
  } catch (e) {
    res.status(503).json({ error: e.message });
  }
});

// Scrape messages from a specific chat by JID
app.post('/api/whatsapp/scrape', async (req, res) => {
  try {
    const { jid, limit } = req.body;
    if (!jid) return res.status(400).json({ error: 'jid required' });
    const result = await waBridge.scrapeMessagesFromChat(jid, parseInt(limit || '200'));
    res.json(result);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// --- Links / Messages endpoints ---

app.get('/api/links', async (req, res) => {
  try {
    const filters = {
      contact_id: req.query.contact_id ? parseInt(req.query.contact_id) : null,
      link_type: req.query.link_type || null,
      status: req.query.status || null,
    };
    const links = await db.getLinksForViewing(filters);
    res.json({ links });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.get('/api/links/:id', async (req, res) => {
  try {
    const link = await db.getLinkDetail(parseInt(req.params.id));
    if (!link) return res.status(404).json({ error: 'Link not found' });
    res.json(link);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.get('/api/contacts', async (req, res) => {
  try {
    const contacts = await db.getContacts();
    res.json({ contacts });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.get('/api/messages', async (req, res) => {
  try {
    const messages = req.query.with_links === 'true'
      ? await db.getMessagesWithLinks()
      : await db.getAllMessages();
    res.json({ messages });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// --- Processing pipeline ---

// Process pending links: scrape → AI analyze → update DB
app.post('/api/process/pending', async (req, res) => {
  try {
    const { limit } = req.body;
    const pending = await db.getPendingLinks(parseInt(limit || '5'));
    if (pending.length === 0) return res.json({ processed: 0, message: 'No pending links' });

    let processed = 0;
    const results = [];

    for (const link of pending) {
      try {
        // Step 1: Scrape
        await db.updateLinkStatus(link.id, 'scraping');
        const scraped = await scrapeLink(link.url);
        await db.updateLinkScraped(link.id, scraped);

        // Step 2: AI analyze
        await db.updateLinkStatus(link.id, 'analyzing');
        const aiResult = await analyzeContent({
          url: link.url,
          title: scraped.title,
          description: scraped.description,
          raw_content: scraped.raw_content,
          link_type: link.link_type,
        });
        await db.updateLinkAI(link.id, aiResult);

        processed++;
        results.push({ id: link.id, url: link.url, status: 'completed' });
      } catch (e) {
        await db.updateLinkStatus(link.id, 'error');
        results.push({ id: link.id, url: link.url, status: 'error', error: e.message });
      }
    }

    res.json({ processed, results });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// Reprocess a single link
app.post('/api/links/:id/reprocess', async (req, res) => {
  try {
    const linkId = parseInt(req.params.id);
    const link = await db.getLinkDetail(linkId);
    if (!link) return res.status(404).json({ error: 'Link not found' });

    await db.updateLinkStatus(linkId, 'scraping');
    const scraped = await scrapeLink(link.url);
    await db.updateLinkScraped(linkId, scraped);

    await db.updateLinkStatus(linkId, 'analyzing');
    const aiResult = await analyzeContent({
      url: link.url,
      title: scraped.title,
      description: scraped.description,
      raw_content: scraped.raw_content,
      link_type: link.link_type,
    });
    await db.updateLinkAI(linkId, aiResult);

    res.json({ ok: true, linkId });
  } catch (e) {
    await db.updateLinkStatus(parseInt(req.params.id), 'error').catch(() => {});
    res.status(500).json({ error: e.message });
  }
});

// --- Stats ---
app.get('/api/stats', async (req, res) => {
  try {
    const stats = await db.getStats();
    const ollama = await checkOllamaAvailable();
    res.json({ ...stats, ollama });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// --- Ollama info ---
app.get('/api/ollama/status', async (req, res) => {
  try {
    const ollama = await checkOllamaAvailable();
    res.json(ollama);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

app.listen(PORT, () => {
  console.log(`[SocialMediaRepo] API server running on http://localhost:${PORT}`);
});
