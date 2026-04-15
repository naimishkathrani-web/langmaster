import React, { useState, useEffect, useCallback } from 'react';
import {
  MessageSquare, Link2, Brain, RefreshCw, Download, Eye,
  ChevronDown, Search, Wifi, WifiOff, Loader2, CheckCircle2,
  AlertCircle, X, ExternalLink, Tag, Calendar, User, FileText
} from 'lucide-react';
import ReactMarkdown from 'react-markdown';

const API = '/api';

// ─── Helpers ──────────────────────────────────────────────
async function apiGet(path) {
  const res = await fetch(`${API}${path}`);
  return res.json();
}
async function apiPost(path, body = {}) {
  const res = await fetch(`${API}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  return res.json();
}

const LINK_TYPE_COLORS = {
  instagram: 'bg-pink-100 text-pink-700',
  youtube: 'bg-red-100 text-red-700',
  twitter: 'bg-sky-100 text-sky-700',
  facebook: 'bg-blue-100 text-blue-700',
  linkedin: 'bg-blue-100 text-blue-800',
  tiktok: 'bg-gray-100 text-gray-700',
  reddit: 'bg-orange-100 text-orange-700',
  document: 'bg-emerald-100 text-emerald-700',
  medium: 'bg-green-100 text-green-700',
  pinterest: 'bg-rose-100 text-rose-700',
  other: 'bg-gray-100 text-gray-600',
  unknown: 'bg-gray-100 text-gray-500',
};

const STATUS_BADGE = {
  pending: 'bg-yellow-100 text-yellow-700',
  scraping: 'bg-blue-100 text-blue-700',
  analyzing: 'bg-purple-100 text-purple-700',
  completed: 'bg-green-100 text-green-700',
  error: 'bg-red-100 text-red-700',
  scraped: 'bg-blue-100 text-blue-700',
};

// ─── Detail Modal ─────────────────────────────────────────
function DetailModal({ link, onClose }) {
  if (!link) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onClose}>
      <div
        className="bg-white rounded-xl shadow-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between rounded-t-xl">
          <h2 className="text-lg font-semibold text-gray-900 truncate pr-4">{link.title || link.url}</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg"><X size={20} /></button>
        </div>
        <div className="px-6 py-5 space-y-5">
          {/* Meta info */}
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div className="flex items-center gap-2 text-gray-600">
              <Link2 size={14} />
              <a href={link.url} target="_blank" rel="noopener" className="text-blue-600 hover:underline truncate">
                {link.url} <ExternalLink size={12} className="inline" />
              </a>
            </div>
            <div className="flex items-center gap-2 text-gray-600">
              <Tag size={14} />
              <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${LINK_TYPE_COLORS[link.link_type] || LINK_TYPE_COLORS.other}`}>
                {link.link_type}
              </span>
            </div>
            <div className="flex items-center gap-2 text-gray-600">
              <User size={14} />
              <span>{link.contact_name}</span>
            </div>
            <div className="flex items-center gap-2 text-gray-600">
              <Calendar size={14} />
              <span>{link.msg_timestamp ? new Date(link.msg_timestamp).toLocaleString() : '—'}</span>
            </div>
          </div>

          {/* OG Image */}
          {link.og_image && (
            <div className="rounded-lg overflow-hidden border">
              <img src={link.og_image} alt="" className="w-full max-h-48 object-cover" onError={e => e.target.style.display='none'} />
            </div>
          )}

          {/* Original message */}
          {link.message_body && (
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="text-xs font-medium text-gray-500 mb-1 flex items-center gap-1">
                <MessageSquare size={12} /> Original WhatsApp Message
              </div>
              <p className="text-sm text-gray-800 whitespace-pre-wrap">{link.message_body}</p>
            </div>
          )}

          {/* AI Summary */}
          {link.ai_summary && (
            <div className="bg-blue-50 rounded-lg p-4">
              <div className="text-xs font-medium text-blue-600 mb-1 flex items-center gap-1">
                <Brain size={12} /> AI Summary
              </div>
              <p className="text-sm text-gray-800">{link.ai_summary}</p>
            </div>
          )}

          {/* AI Detailed Analysis */}
          {link.ai_detailed_analysis && (
            <div className="bg-purple-50 rounded-lg p-4">
              <div className="text-xs font-medium text-purple-600 mb-2 flex items-center gap-1">
                <FileText size={12} /> Detailed AI Analysis
              </div>
              <div className="prose prose-sm max-w-none text-gray-800">
                <ReactMarkdown>{link.ai_detailed_analysis}</ReactMarkdown>
              </div>
            </div>
          )}

          {/* Tags */}
          {link.ai_tags && link.ai_tags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {link.ai_tags.map((tag, i) => (
                <span key={i} className="bg-gray-100 text-gray-700 px-2 py-1 rounded-full text-xs">{tag}</span>
              ))}
            </div>
          )}

          {/* Raw scraped content (collapsible) */}
          {link.raw_content && (
            <details className="text-sm">
              <summary className="text-gray-500 cursor-pointer hover:text-gray-700">Raw scraped content</summary>
              <pre className="mt-2 bg-gray-50 p-3 rounded-lg overflow-x-auto text-xs text-gray-600 whitespace-pre-wrap">
                {link.raw_content.slice(0, 3000)}
              </pre>
            </details>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Main App ─────────────────────────────────────────────
export default function App() {
  const [waStatus, setWaStatus] = useState(null);
  const [chats, setChats] = useState([]);
  const [links, setLinks] = useState([]);
  const [stats, setStats] = useState(null);
  const [selectedLink, setSelectedLink] = useState(null);
  const [loading, setLoading] = useState({});
  const [filterType, setFilterType] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [searchQ, setSearchQ] = useState('');
  const [scrapeLimit, setScrapeLimit] = useState(100);

  const refreshStatus = useCallback(() => apiGet('/whatsapp/status').then(setWaStatus), []);
  const refreshStats = useCallback(() => apiGet('/stats').then(setStats), []);
  const refreshLinks = useCallback(() => {
    const params = new URLSearchParams();
    if (filterType) params.set('link_type', filterType);
    if (filterStatus) params.set('status', filterStatus);
    apiGet(`/links?${params}`).then(d => setLinks(d.links || []));
  }, [filterType, filterStatus]);

  useEffect(() => { refreshStatus(); refreshStats(); refreshLinks(); }, [refreshStatus, refreshStats, refreshLinks]);

  const startWA = async () => {
    setLoading(l => ({ ...l, wa: true }));
    await apiPost('/whatsapp/start');
    // Poll for status
    const poll = setInterval(async () => {
      const s = await apiGet('/whatsapp/status');
      setWaStatus(s);
      if (s.ready) { clearInterval(poll); setLoading(l => ({ ...l, wa: false })); refreshStats(); }
    }, 2000);
  };

  const loadChats = async () => {
    setLoading(l => ({ ...l, chats: true }));
    const d = await apiGet('/whatsapp/chats');
    setChats(d.chats || []);
    setLoading(l => ({ ...l, chats: false }));
  };

  const scrapeChat = async (jid) => {
    setLoading(l => ({ ...l, [jid]: true }));
    const result = await apiPost('/whatsapp/scrape', { jid, limit: scrapeLimit });
    alert(`Scraped "${result.contactName}": ${result.totalFetched} messages, ${result.newWithLinks} with links`);
    setLoading(l => ({ ...l, [jid]: false }));
    refreshLinks();
    refreshStats();
  };

  const processPending = async () => {
    setLoading(l => ({ ...l, process: true }));
    const result = await apiPost('/process/pending', { limit: 5 });
    alert(`Processed ${result.processed} links`);
    setLoading(l => ({ ...l, process: false }));
    refreshLinks();
    refreshStats();
  };

  const reprocessLink = async (id) => {
    setLoading(l => ({ ...l, [`rp-${id}`]: true }));
    await apiPost(`/links/${id}/reprocess`);
    setLoading(l => ({ ...l, [`rp-${id}`]: false }));
    refreshLinks();
  };

  const filteredLinks = links.filter(l => {
    if (!searchQ) return true;
    const q = searchQ.toLowerCase();
    return (l.url || '').toLowerCase().includes(q)
      || (l.title || '').toLowerCase().includes(q)
      || (l.ai_summary || '').toLowerCase().includes(q)
      || (l.contact_name || '').toLowerCase().includes(q)
      || (l.message_body || '').toLowerCase().includes(q);
  });

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="bg-indigo-600 text-white p-2 rounded-lg"><Link2 size={20} /></div>
            <div>
              <h1 className="text-lg font-bold text-gray-900">SocialMedia Repo</h1>
              <p className="text-xs text-gray-500">WhatsApp Link Analyzer & Repository</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            {stats && (
              <div className="hidden sm:flex items-center gap-4 text-xs text-gray-500">
                <span>{stats.contacts} contacts</span>
                <span>{stats.messages} msgs</span>
                <span>{stats.links} links</span>
                <span>{stats.completed} analyzed</span>
              </div>
            )}
            <div className="flex items-center gap-1">
              {waStatus?.ready ? (
                <span className="flex items-center gap-1 text-green-600 text-xs font-medium"><Wifi size={14} /> Connected</span>
              ) : (
                <span className="flex items-center gap-1 text-gray-400 text-xs"><WifiOff size={14} /> Disconnected</span>
              )}
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 space-y-6">
        {/* Step 1: WhatsApp Connection */}
        <section className="bg-white rounded-xl border p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
            <MessageSquare size={16} /> Step 1: WhatsApp Connection
          </h2>
          <div className="flex flex-wrap items-center gap-3">
            {!waStatus?.ready && !loading.wa && (
              <button onClick={startWA} className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 flex items-center gap-2">
                <RefreshCw size={14} /> Connect WhatsApp
              </button>
            )}
            {loading.wa && <span className="flex items-center gap-2 text-sm text-gray-500"><Loader2 className="animate-spin" size={14} /> Waiting for QR scan…</span>}
            {waStatus?.qrDataUrl && (
              <div className="text-center">
                <img src={waStatus.qrDataUrl} alt="QR Code" className="w-40 h-40 mx-auto border rounded-lg" />
                <p className="text-xs text-gray-500 mt-1">Scan with WhatsApp → Linked Devices</p>
              </div>
            )}
            {waStatus?.ready && (
              <>
                <button onClick={loadChats} className="bg-gray-100 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-200 flex items-center gap-2">
                  <Download size={14} /> Load Chats
                </button>
                <label className="text-xs text-gray-500 flex items-center gap-1">
                  Msg limit:
                  <input type="number" value={scrapeLimit} onChange={e => setScrapeLimit(parseInt(e.target.value) || 100)}
                    className="w-16 border rounded px-2 py-1 text-xs" min={10} max={1000} />
                </label>
              </>
            )}
          </div>

          {/* Chat list */}
          {chats.length > 0 && (
            <div className="mt-4 max-h-64 overflow-y-auto border rounded-lg">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 sticky top-0">
                  <tr>
                    <th className="text-left px-3 py-2 text-xs text-gray-500">Chat Name</th>
                    <th className="text-left px-3 py-2 text-xs text-gray-500">Type</th>
                    <th className="text-left px-3 py-2 text-xs text-gray-500">Unread</th>
                    <th className="text-right px-3 py-2 text-xs text-gray-500">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {chats.map(c => (
                    <tr key={c.jid} className="border-t hover:bg-gray-50">
                      <td className="px-3 py-2">{c.name}</td>
                      <td className="px-3 py-2">{c.isGroup ? 'Group' : 'Personal'}</td>
                      <td className="px-3 py-2">{c.unreadCount || 0}</td>
                      <td className="px-3 py-2 text-right">
                        <button onClick={() => scrapeChat(c.jid)} disabled={loading[c.jid]}
                          className="bg-indigo-50 text-indigo-600 px-3 py-1 rounded text-xs font-medium hover:bg-indigo-100 disabled:opacity-50 flex items-center gap-1 ml-auto">
                          {loading[c.jid] ? <Loader2 className="animate-spin" size={12} /> : <Download size={12} />}
                          Scrape
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {/* Step 2: Process Links */}
        <section className="bg-white rounded-xl border p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
            <Brain size={16} /> Step 2: AI Analysis Pipeline
          </h2>
          <div className="flex items-center gap-3">
            <button onClick={processPending} disabled={loading.process}
              className="bg-purple-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-purple-700 disabled:opacity-50 flex items-center gap-2">
              {loading.process ? <Loader2 className="animate-spin" size={14} /> : <Brain size={14} />}
              Process Pending Links (5 at a time)
            </button>
            <span className="text-xs text-gray-500">
              Scrapes web content → sends to Ollama AI → stores summary & analysis
            </span>
          </div>
        </section>

        {/* Step 3: Browse Links */}
        <section className="bg-white rounded-xl border p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
            <Eye size={16} /> Saved Links Repository
          </h2>

          {/* Filters */}
          <div className="flex flex-wrap items-center gap-3 mb-4">
            <div className="relative flex-1 min-w-[200px]">
              <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input type="text" placeholder="Search links, summaries, contacts…"
                value={searchQ} onChange={e => setSearchQ(e.target.value)}
                className="w-full pl-8 pr-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300" />
            </div>
            <select value={filterType} onChange={e => setFilterType(e.target.value)}
              className="border rounded-lg px-3 py-2 text-sm">
              <option value="">All types</option>
              {['instagram', 'youtube', 'twitter', 'facebook', 'linkedin', 'tiktok', 'reddit', 'document', 'medium', 'pinterest', 'other'].map(t => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
            <select value={filterStatus} onChange={e => setFilterStatus(e.target.value)}
              className="border rounded-lg px-3 py-2 text-sm">
              <option value="">All statuses</option>
              {['pending', 'scraped', 'analyzing', 'completed', 'error'].map(s => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
            <button onClick={refreshLinks} className="p-2 hover:bg-gray-100 rounded-lg" title="Refresh">
              <RefreshCw size={16} />
            </button>
          </div>

          {/* Links Table */}
          <div className="overflow-x-auto border rounded-lg">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="text-left px-3 py-2 text-xs text-gray-500">#</th>
                  <th className="text-left px-3 py-2 text-xs text-gray-500">Type</th>
                  <th className="text-left px-3 py-2 text-xs text-gray-500">Title / URL</th>
                  <th className="text-left px-3 py-2 text-xs text-gray-500">Contact</th>
                  <th className="text-left px-3 py-2 text-xs text-gray-500">AI Summary / Description</th>
                  <th className="text-left px-3 py-2 text-xs text-gray-500">Status</th>
                  <th className="text-left px-3 py-2 text-xs text-gray-500">Date</th>
                  <th className="text-right px-3 py-2 text-xs text-gray-500">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredLinks.length === 0 ? (
                  <tr><td colSpan={8} className="px-3 py-8 text-center text-gray-400">
                    No links found. Connect WhatsApp, scrape a chat, then process pending links.
                  </td></tr>
                ) : filteredLinks.map((l, i) => (
                  <tr key={l.id} className="border-t hover:bg-gray-50">
                    <td className="px-3 py-2 text-gray-400">{i + 1}</td>
                    <td className="px-3 py-2">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${LINK_TYPE_COLORS[l.link_type] || LINK_TYPE_COLORS.other}`}>
                        {l.link_type}
                      </span>
                    </td>
                    <td className="px-3 py-2 max-w-[250px]">
                      <div className="font-medium text-gray-900 truncate">{l.title || '—'}</div>
                      <a href={l.url} target="_blank" rel="noopener"
                        className="text-xs text-blue-500 hover:underline truncate block max-w-[230px]">
                        {l.url?.slice(0, 60)}{l.url?.length > 60 ? '…' : ''}
                      </a>
                    </td>
                    <td className="px-3 py-2 text-gray-600 whitespace-nowrap">{l.contact_name}</td>
                    <td className="px-3 py-2 max-w-[300px]">
                      <p className="text-gray-700 text-xs line-clamp-3">
                        {l.ai_summary || l.description || 'Not yet analyzed'}
                      </p>
                    </td>
                    <td className="px-3 py-2">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[l.processing_status] || STATUS_BADGE.other}`}>
                        {l.processing_status}
                      </span>
                    </td>
                    <td className="px-3 py-2 text-gray-500 text-xs whitespace-nowrap">
                      {l.msg_timestamp ? new Date(l.msg_timestamp).toLocaleDateString() : '—'}
                    </td>
                    <td className="px-3 py-2 text-right whitespace-nowrap">
                      <button onClick={() => setSelectedLink(l)}
                        className="text-indigo-600 hover:text-indigo-800 text-xs font-medium mr-2">
                        <Eye size={14} className="inline" /> View
                      </button>
                      {l.processing_status !== 'completed' && (
                        <button onClick={() => reprocessLink(l.id)} disabled={loading[`rp-${l.id}`]}
                          className="text-purple-600 hover:text-purple-800 text-xs font-medium">
                          {loading[`rp-${l.id}`] ? <Loader2 className="animate-spin inline" size={12} /> : <RefreshCw size={12} className="inline" />}
                          {' '}Reprocess
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      {/* Detail Modal */}
      {selectedLink && (
        <DetailModal link={selectedLink} onClose={() => { setSelectedLink(null); refreshLinks(); }} />
      )}
    </div>
  );
}
