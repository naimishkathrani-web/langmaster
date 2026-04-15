import { useState } from 'react'
import LanguageChips from './LanguageChips'

export default function AgentTranslateScreen({ store }) {
  const [source, setSource] = useState('English')
  const [target, setTarget] = useState('Hindi')
  const [inputText, setInputText] = useState('')

  function handleTranslate() {
    if (!inputText.trim()) return
    store.saveTranslation(source, target, inputText)
    setInputText('')
  }

  function shareText(text, pkg) {
    if (!text) return
    const url = pkg ? `https://wa.me/?text=${encodeURIComponent(text)}` : null
    if (url) window.open(url, '_blank')
    else navigator.clipboard?.writeText(text)
  }

  return (
    <div className="p-4 space-y-3">
      <h2 className="font-bold text-base">AI Agent Translation Studio</h2>
      <p className="text-sm text-gray-400">Same UX style as Connect tab, but talking to AI agent instead of contacts.</p>
      <p className="text-sm text-gray-400">Source language</p>
      <LanguageChips selected={source} onSelect={setSource} />
      <p className="text-sm text-gray-400">Target language</p>
      <LanguageChips selected={target} onSelect={setTarget} />
      <div className="flex gap-2">
        <button className="bg-primary hover:bg-blue-700 rounded px-3 py-1.5 text-sm">Translate Image</button>
        <button className="bg-primary hover:bg-blue-700 rounded px-3 py-1.5 text-sm">Translate Voice</button>
        <button className="bg-primary hover:bg-blue-700 rounded px-3 py-1.5 text-sm">Translate Video</button>
      </div>
      <div className="flex gap-2">
        <button className="bg-gray-700 hover:bg-gray-600 rounded px-3 py-1.5 text-sm"
          onClick={() => shareText(store.translationSessions[0]?.outputText)}>Share to App</button>
        <button className="bg-green-700 hover:bg-green-600 rounded px-3 py-1.5 text-sm"
          onClick={() => shareText(store.translationSessions[0]?.outputText, 'whatsapp')}>Share to WhatsApp</button>
      </div>
      <input className="w-full bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm text-white placeholder-gray-400"
        placeholder="Type text to translate" value={inputText} onChange={e => setInputText(e.target.value)}
        onKeyDown={e => e.key === 'Enter' && handleTranslate()} />
      <button className="bg-primary hover:bg-blue-700 rounded px-4 py-2 text-sm font-semibold" onClick={handleTranslate}>Ask AI Agent</button>
      <p className="text-sm text-gray-400">Recent AI translations</p>
      <div className="space-y-2 max-h-48 overflow-y-auto">
        {store.translationSessions.slice(-5).reverse().map(s => (
          <div key={s.id} className="bg-gray-800 rounded p-3">
            <p className="font-semibold text-sm">{s.sourceLang} → {s.targetLang}</p>
            <p className="text-sm">In: {s.inputText}</p>
            <p className="text-sm">Out: {s.outputText}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
