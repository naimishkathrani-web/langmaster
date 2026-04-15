import { useState } from 'react'
import { useStore, LANGUAGES, shouldUseAi, LOCAL_PHONE } from './store'
import OnboardingScreen from './OnboardingScreen'
import ConnectScreen from './ConnectScreen'
import AgentTranslateScreen from './AgentTranslateScreen'
import LearningScreen from './LearningScreen'

const TABS = [
  { key: 'CONNECT', label: 'Connect' },
  { key: 'AGENT', label: 'AI Translate' },
  { key: 'LEARN', label: 'Learn' }
]

export default function App() {
  const store = useStore()
  const [tab, setTab] = useState('CONNECT')

  if (!store.isLoggedIn) return <OnboardingScreen store={store} />

  return (
    <div className="flex flex-col h-screen bg-surface-dark text-white">
      <header className="bg-primary px-4 py-3 text-lg font-bold shrink-0">LangMaster</header>
      <main className="flex-1 overflow-y-auto">
        {tab === 'CONNECT' && <ConnectScreen store={store} />}
        {tab === 'AGENT' && <AgentTranslateScreen store={store} />}
        {tab === 'LEARN' && <LearningScreen store={store} />}
      </main>
      <nav className="bg-surface-dark border-t border-gray-700 px-4 py-3 flex justify-around shrink-0">
        {TABS.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`text-sm font-medium ${tab === t.key ? 'text-accent-blue font-bold' : 'text-gray-400'}`}>
            {t.label}
          </button>
        ))}
      </nav>
    </div>
  )
}
