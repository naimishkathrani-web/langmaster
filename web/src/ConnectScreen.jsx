import { useState } from 'react'
import { LANGUAGES, shouldUseAi } from './store'
import LanguageChips from './LanguageChips'

export default function ConnectScreen({ store }) {
  const [input, setInput] = useState('')
  const [translationEnabled, setTranslationEnabled] = useState(false)
  const [preferredLanguage, setPreferredLanguage] = useState('English')
  const [menuMsgId, setMenuMsgId] = useState(null)

  const aiActive = shouldUseAi(translationEnabled, preferredLanguage, 'Hindi')

  function handleSend() {
    store.sendMessage(input, preferredLanguage)
    setInput('')
  }

  return (
    <div className="p-3 space-y-3">
      <h2 className="font-bold text-base">Chat + Voice Note + Voice Call + Video Call</h2>
      <p className="text-sm text-gray-400">Current: {store.activeConvTitle}</p>

      <div className="flex gap-2 flex-wrap">
        {store.conversations.map(c => (
          <button key={c.id} onClick={() => store.setActiveConvId(c.id)}
            className={`px-3 py-1 rounded border text-sm ${c.id === store.activeConvId ? 'border-accent-blue text-accent-blue' : 'border-gray-600 text-gray-300'}`}>
            {c.title}
          </button>
        ))}
      </div>

      <div className="flex items-center justify-between">
        <span className="font-semibold text-sm">Translation</span>
        <label className="flex items-center gap-2 cursor-pointer">
          <input type="checkbox" checked={translationEnabled}
            onChange={e => { setTranslationEnabled(e.target.checked); store.setListenerPreference?.(e.target.checked, preferredLanguage) }}
            className="w-4 h-4 accent-accent-blue" />
        </label>
      </div>

      {translationEnabled && (
        <div>
          <p className="text-sm text-gray-400 mb-1">Listen in</p>
          <LanguageChips selected={preferredLanguage} onSelect={l => setPreferredLanguage(l)} />
        </div>
      )}

      <div className="flex gap-2">
        <button className="bg-primary hover:bg-blue-700 rounded px-3 py-1.5 text-sm">Voice Call</button>
        <button className="bg-primary hover:bg-blue-700 rounded px-3 py-1.5 text-sm">Video Call</button>
        <button className="bg-primary hover:bg-blue-700 rounded px-3 py-1.5 text-sm">Voice Record</button>
      </div>

      <div className="space-y-2 max-h-64 overflow-y-auto">
        {store.activeMessages.map(msg => (
          <div key={msg.id} className="bg-gray-800 rounded p-3 relative">
            <div className="flex justify-between items-start">
              <span className="font-bold text-sm text-accent-blue">{msg.senderPhoneE164}</span>
              <button onClick={() => setMenuMsgId(menuMsgId === msg.id ? null : msg.id)}
                className="text-gray-400 hover:text-white text-xs">▼</button>
            </div>
            <p className="text-sm mt-1">{msg.body}</p>
            {menuMsgId === msg.id && (
              <div className="absolute right-2 top-8 bg-gray-900 border border-gray-600 rounded shadow-lg z-10">
                <button className="block w-full text-left px-3 py-1.5 text-sm hover:bg-gray-700" onClick={() => setMenuMsgId(null)}>Reply</button>
                <button className="block w-full text-left px-3 py-1.5 text-sm hover:bg-gray-700" onClick={() => { store.forwardMessage(msg, 'conv-group-1'); setMenuMsgId(null) }}>Forward</button>
                <button className="block w-full text-left px-3 py-1.5 text-sm hover:bg-gray-700" onClick={() => { store.deleteForMe(msg.id); setMenuMsgId(null) }}>Delete for me</button>
                <button className="block w-full text-left px-3 py-1.5 text-sm hover:bg-gray-700" onClick={() => { store.deleteForEveryone(msg); setMenuMsgId(null) }}>Delete for everyone (24h)</button>
              </div>
            )}
          </div>
        ))}
      </div>

      <p className={`text-xs ${aiActive ? 'text-blue-300' : 'text-red-300'}`}>
        {aiActive ? 'AI translation active for listener' : 'AI idle (same language or translation off)'}
      </p>

      <div className="flex gap-2">
        <input className="flex-1 bg-gray-800 border border-gray-600 rounded px-3 py-2 text-sm text-white placeholder-gray-400"
          placeholder="Type message" value={input} onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSend()} />
        <button className="bg-primary hover:bg-blue-700 rounded px-4 py-2 text-sm font-semibold" onClick={handleSend}>Send</button>
      </div>
    </div>
  )
}
