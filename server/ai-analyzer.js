/**
 * AI Analyzer - uses Ollama (local) to generate summaries and detailed analysis
 * of scraped link content.
 */

const fetch = require('node-fetch');

const OLLAMA_BASE_URL = process.env.OLLAMA_BASE_URL || 'http://127.0.0.1:11434';
const OLLAMA_MODEL = process.env.OLLAMA_MODEL || 'gemma3:4b';

async function checkOllamaAvailable() {
  try {
    const res = await fetch(`${OLLAMA_BASE_URL}/api/tags`);
    if (!res.ok) return { available: false, models: [] };
    const data = await res.json();
    const models = (data.models || []).map(m => m.name);
    return { available: true, models };
  } catch {
    return { available: false, models: [] };
  }
}

async function ollamaGenerate(prompt) {
  const res = await fetch(`${OLLAMA_BASE_URL}/api/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model: OLLAMA_MODEL,
      prompt,
      stream: false,
      options: { temperature: 0.3, num_predict: 1024 },
    }),
  });

  if (!res.ok) {
    const errText = await res.text().catch(() => '');
    throw new Error(`Ollama error ${res.status}: ${errText}`);
  }

  const data = await res.json();
  return data.response || '';
}

/**
 * Generate a short summary (for table view) and detailed analysis (for detail modal).
 */
async function analyzeContent({ url, title, description, raw_content, link_type }) {
  const contextBlock = [
    `URL: ${url}`,
    `Type: ${link_type}`,
    title ? `Title: ${title}` : '',
    description ? `Description: ${description}` : '',
    raw_content ? `Page Content (excerpt):\n${raw_content.slice(0, 4000)}` : '',
  ].filter(Boolean).join('\n');

  // Short summary
  const summaryPrompt = `You are a content analyst. Based on the following web page information, write a concise 2-3 sentence summary explaining what this content is about, its key message or purpose, and why someone might find it useful.

${contextBlock}

Summary:`;

  // Detailed analysis
  const detailPrompt = `You are a content analyst. Based on the following web page information, provide a detailed, well-structured analysis. Include:

1. **What is this?** — Identify the content type (reel, video, article, document, etc.)
2. **Main Topic** — What subject or theme does it cover?
3. **Key Takeaways** — 3-5 bullet points of the most important information
4. **Purpose & Intent** — Why was this created? What action or understanding does it promote?
5. **Target Audience** — Who is this meant for?
6. **Quality Assessment** — Is this credible, useful, well-made?
7. **Relevance Tags** — 5-8 keyword tags for categorization

Format the response in clean Markdown.

${contextBlock}

Detailed Analysis:`;

  let summary = '';
  let detailedAnalysis = '';
  let tags = [];

  try {
    summary = await ollamaGenerate(summaryPrompt);
  } catch (e) {
    summary = `AI summary unavailable: ${e.message}`;
  }

  try {
    detailedAnalysis = await ollamaGenerate(detailPrompt);
    // Extract tags from the analysis
    const tagMatch = detailedAnalysis.match(/\*\*Relevance Tags\*\*[^[]*\[([^\]]+)\]/);
    if (tagMatch) {
      tags = tagMatch[1].split(/[,|]/).map(t => t.trim().replace(/^`|`$/g, '')).filter(Boolean);
    }
  } catch (e) {
    detailedAnalysis = `AI detailed analysis unavailable: ${e.message}`;
  }

  return { ai_summary: summary.trim(), ai_detailed_analysis: detailedAnalysis.trim(), ai_tags: tags };
}

module.exports = { checkOllamaAvailable, analyzeContent };
