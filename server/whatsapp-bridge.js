/**
 * WhatsApp Web.js bridge for SocialMediaRepo.
 * Adapted from RidersHub vendor-whatsapp-bridge pattern.
 * Connects to WhatsApp, fetches contacts/chats, scrapes messages from selected contacts.
 */

const fs = require('fs');
const path = require('path');
const db = require('./db');

const WHATSAPP_CLIENT_ID = process.env.WHATSAPP_CLIENT_ID || 'socialmedia-repo';
const WHATSAPP_AUTH_DIR = process.env.WHATSAPP_AUTH_DIR || '.wwebjs_auth';
const PUPPETEER_HEADLESS = (process.env.WHATSAPP_HEADLESS || 'false').toLowerCase() !== 'false';

let qrDataUrl = null;
let isReady = false;
let initializing = false;
let whatsappClient = null;
let booting = false;

function digits10(raw) {
  const d = String(raw || '').replace(/\D/g, '');
  return d.length >= 10 ? d.slice(-10) : d;
}

async function startWhatsApp() {
  try {
    if (booting) return { status: 'already_booting' };
    if (whatsappClient && !isReady) {
      console.log('[wa-bridge] Client exists but not ready, destroying...');
      try {
        await whatsappClient.destroy();
      } catch {}
      whatsappClient = null;
    }
    if (whatsappClient && isReady) {
      return { status: 'already_ready' };
    }
    if (initializing) {
      return { status: 'already_initializing' };
    }
    booting = true;
    initializing = true;
    isReady = false;
    qrDataUrl = null;

  if (!fs.existsSync(WHATSAPP_AUTH_DIR)) fs.mkdirSync(WHATSAPP_AUTH_DIR, { recursive: true });

    const wwjs = require('whatsapp-web.js');
    const qrcode = require('qrcode');

    const chromePath = process.env.PUPPETEER_EXECUTABLE_PATH ||
      (process.platform === 'win32' ? 
        (fs.existsSync('C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe') ? 
          'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe' :
          'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe') : '');

    const puppeteerConfig = {
      headless: PUPPETEER_HEADLESS,
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-dev-shm-usage',
        '--disable-web-security',
        '--disable-features=VizDisplayCompositor'
      ],
    };
    if (chromePath) puppeteerConfig.executablePath = chromePath;

    whatsappClient = new wwjs.Client({
      authStrategy: new wwjs.LocalAuth({ clientId: WHATSAPP_CLIENT_ID, dataPath: WHATSAPP_AUTH_DIR }),
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
      puppeteer: puppeteerConfig,
    });

    whatsappClient.on('qr', async (qr) => {
      try {
        qrDataUrl = await qrcode.toDataURL(qr, { margin: 2, width: 256 });
        console.log('[wa-bridge] QR code generated - scan with WhatsApp Linked Devices');
      } catch (e) {
        console.error('[wa-bridge] QR encode failed', e);
        qrDataUrl = null;
      }
      isReady = false;
      initializing = false;
    });

    whatsappClient.on('ready', () => {
      console.log('[wa-bridge] WhatsApp client ready');
      isReady = true;
      initializing = false;
      qrDataUrl = null;
    });

    whatsappClient.on('auth_failure', (m) => {
      console.error('[wa-bridge] auth_failure', m);
      isReady = false;
      initializing = false;
    });

    whatsappClient.on('disconnected', (r) => {
      console.warn('[wa-bridge] disconnected', r);
      isReady = false;
      initializing = false;
    });

    console.log('[wa-bridge] initializing WhatsApp client (headless=%s)', PUPPETEER_HEADLESS);
    await whatsappClient.initialize();
    return { status: 'initialized' };
  } catch (e) {
    console.error('[wa-bridge] Start error:', e);
    booting = false;
    initializing = false;
    throw e;
  }
}

function getStatus() {
  return {
    ready: isReady,
    initializing,
    qrDataUrl: isReady ? null : qrDataUrl,
  };
}

async function getChats() {
  if (!isReady || !whatsappClient) throw new Error('WhatsApp not connected');
  const chats = await whatsappClient.getChats();
  return chats
    .filter(c => c && c.id && c.id._serialized)
    .map(c => ({
      jid: c.id._serialized,
      name: c.name || c.formattedTitle || c.pushname || c.number || c.id._serialized,
      isGroup: Boolean(c.isGroup),
      unreadCount: c.unreadCount || 0,
    }))
    .slice(0, 500);
}

async function getContacts() {
  if (!isReady || !whatsappClient) throw new Error('WhatsApp not connected');
  const contacts = await whatsappClient.getContacts();
  return contacts
    .filter(c => c && !c.isGroup && !c.isBroadcast && c.id && c.id._serialized)
    .map(c => ({
      jid: c.id._serialized,
      name: c.pushname || c.name || c.shortName || c.number || c.id._serialized,
      phone10: digits10(c.number || ''),
    }))
    .slice(0, 500);
}

/**
 * Fetch messages from a specific chat and store them in DB.
 * Returns count of new messages stored.
 */
async function scrapeMessagesFromChat(jid, limit = 200) {
  if (!isReady || !whatsappClient) throw new Error('WhatsApp not connected');

  const chat = await whatsappClient.getChatById(jid);
  if (!chat) throw new Error('Chat not found for JID: ' + jid);

  const contactName = chat.name || chat.formattedTitle || jid;
  const isGroup = Boolean(chat.isGroup);

  // Upsert contact
  const contactId = await db.upsertContact({
    wa_jid: jid,
    name: contactName,
    phone: digits10(jid.split('@')[0] || ''),
    is_group: isGroup,
  });

  // Fetch messages
  const messages = await chat.fetchMessages({ limit });
  let newCount = 0;

  const URL_REGEX = /https?:\/\/[^\s<>"']+/gi;

  for (const msg of messages) {
    if (!msg.body && !msg.hasMedia) continue;

    const body = msg.body || '';
    const links = body.match(URL_REGEX) || [];
    const hasLinks = links.length > 0;
    const msgType = msg.type || 'text';
    const waMsgId = msg.id?._serialized || String(msg.id || '');
    if (!waMsgId) continue;

    const ts = msg.timestamp ? new Date(msg.timestamp * 1000) : new Date();

    const messageId = await db.insertMessage({
      contact_id: contactId,
      wa_message_id: waMsgId,
      message_body: body,
      message_type: msgType,
      has_links: hasLinks,
      timestamp: ts,
    });

    if (messageId && hasLinks) {
      for (const linkUrl of links) {
        try {
          const domain = new URL(linkUrl).hostname;
          const linkType = detectLinkType(linkUrl);
          await db.insertLink({
            message_id: messageId,
            url: linkUrl,
            domain,
            link_type: linkType,
          });
        } catch (e) {
          // skip invalid URLs
        }
      }
      newCount++;
    }
  }

  return { contactName, totalFetched: messages.length, newWithLinks: newCount };
}

function detectLinkType(url) {
  const u = url.toLowerCase();
  if (u.includes('instagram.com')) return 'instagram';
  if (u.includes('youtube.com') || u.includes('youtu.be')) return 'youtube';
  if (u.includes('twitter.com') || u.includes('x.com')) return 'twitter';
  if (u.includes('facebook.com') || u.includes('fb.watch')) return 'facebook';
  if (u.includes('linkedin.com')) return 'linkedin';
  if (u.includes('tiktok.com')) return 'tiktok';
  if (u.includes('reddit.com')) return 'reddit';
  if (u.includes('pinterest.com')) return 'pinterest';
  if (u.includes('.pdf') || u.includes('docs.google.com') || u.includes('drive.google.com')) return 'document';
  if (u.includes('medium.com')) return 'medium';
  return 'other';
}

module.exports = {
  startWhatsApp,
  getStatus,
  getChats,
  getContacts,
  scrapeMessagesFromChat,
};
