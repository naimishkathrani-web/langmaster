import { LANGUAGES } from './store'

export default function LanguageChips({ selected, onSelect }) {
  return (
    <div className="space-y-1.5">
      {LANGUAGES.reduce((rows, lang, i) => {
        const rowIdx = Math.floor(i / 3)
        if (!rows[rowIdx]) rows[rowIdx] = []
        rows[rowIdx].push(lang)
        return rows
      }, []).map((row, ri) => (
        <div key={ri} className="flex gap-2">
          {row.map(lang => (
            <button key={lang} onClick={() => onSelect(lang)}
              className={`px-3 py-1 rounded border text-sm ${lang === selected ? 'border-accent-blue bg-blue-900/30 text-accent-blue' : 'border-gray-600 text-gray-300 hover:border-gray-400'}`}>
              {lang === selected ? `✓ ${lang}` : lang}
            </button>
          ))}
        </div>
      ))}
    </div>
  )
}
