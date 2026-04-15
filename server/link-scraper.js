/**
 * Link scraper - fetches page content from URLs using node-fetch + cheerio.
 * Extracts title, description, OG image, and main text content.
 */

const fetch = require('node-fetch');
const cheerio = require('cheerio');

async function scrapeLink(url) {
  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 15000);

    const res = await fetch(url, {
      signal: controller.signal,
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.5',
      },
      follow: 10,
    });
    clearTimeout(timeout);

    if (!res.ok) {
      return { title: '', description: '', og_image: '', raw_content: `HTTP ${res.status}` };
    }

    const html = await res.text();
    const $ = cheerio.load(html);

    const title = $('meta[property="og:title"]').attr('content') ||
                  $('meta[name="twitter:title"]').attr('content') ||
                  $('title').text() || '';
    const description = $('meta[property="og:description"]').attr('content') ||
                        $('meta[name="twitter:description"]').attr('content') ||
                        $('meta[name="description"]').attr('content') || '';
    const ogImage = $('meta[property="og:image"]').attr('content') ||
                    $('meta[name="twitter:image"]').attr('content') || '';

    // Extract main text content - strip scripts, styles, nav
    $('script, style, nav, header, footer, iframe, noscript').remove();
    const bodyText = $('body').text().replace(/\s+/g, ' ').trim().slice(0, 8000);

    return {
      title: title.trim().slice(0, 500),
      description: description.trim().slice(0, 2000),
      og_image: ogImage.trim().slice(0, 1000),
      raw_content: bodyText.slice(0, 8000),
    };
  } catch (err) {
    return {
      title: '',
      description: '',
      og_image: '',
      raw_content: `Scrape error: ${err.message}`,
    };
  }
}

module.exports = { scrapeLink };
