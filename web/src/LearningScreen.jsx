import { useState } from 'react'
import LanguageChips from './LanguageChips'

export default function LearningScreen({ store }) {
  const [selectedLanguage, setSelectedLanguage] = useState('English')

  return (
    <div className="p-4 space-y-3">
      <h2 className="font-bold text-base">AI Language Learning</h2>
      <p className="text-sm text-gray-400">Guided modules to become expert and certification-ready.</p>
      <p className="text-sm text-gray-400">Choose language</p>
      <LanguageChips selected={selectedLanguage} onSelect={setSelectedLanguage} />
      {store.learningTracks.map(track => (
        <p key={track.id} className="text-sm">• {track.title}: {track.description}</p>
      ))}
      {store.learningModules.map(mod => (
        <div key={mod.id} className="bg-gray-800 rounded p-3 space-y-1">
          <p className="font-semibold text-sm">{mod.title}</p>
          <p className="text-sm text-gray-300">{mod.goal}</p>
          <button className="bg-primary hover:bg-blue-700 rounded px-3 py-1 text-sm"
            onClick={() => store.markProgress(mod.id, 80)}>Mark progress</button>
        </div>
      ))}
    </div>
  )
}
