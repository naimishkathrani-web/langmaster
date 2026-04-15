import { useState } from 'react'

export default function OnboardingScreen({ store }) {
  const [phone, setPhone] = useState('+91')
  const [pin, setPin] = useState('')
  const [confirmPin, setConfirmPin] = useState('')

  return (
    <div className="flex flex-col h-screen bg-surface-dark text-white">
      <header className="bg-primary px-4 py-3 text-lg font-bold shrink-0">LangMaster Setup</header>
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        <p className="font-semibold">Step 1: Register mobile + 4-digit PIN</p>
        <input className="w-full bg-gray-800 border border-gray-600 rounded px-3 py-2 text-white placeholder-gray-400"
          placeholder="Phone number" value={phone} onChange={e => setPhone(e.target.value)} />
        <input className="w-full bg-gray-800 border border-gray-600 rounded px-3 py-2 text-white placeholder-gray-400"
          placeholder="Create 4-digit PIN" value={pin} onChange={e => setPin(e.target.value)} type="password" />
        <input className="w-full bg-gray-800 border border-gray-600 rounded px-3 py-2 text-white placeholder-gray-400"
          placeholder="Confirm PIN" value={confirmPin} onChange={e => setConfirmPin(e.target.value)} type="password" />
        <button className="bg-primary hover:bg-blue-700 rounded px-4 py-2 font-semibold"
          onClick={() => store.registerPin(phone, pin, confirmPin)}>Register PIN</button>
        <p className="font-semibold">Step 2: Login with mobile + PIN</p>
        <button className="bg-primary hover:bg-blue-700 rounded px-4 py-2 font-semibold"
          onClick={() => store.loginWithPin(phone, pin)}>Login &amp; Continue</button>
        {store.authStatus && <p className="text-sm text-gray-300">{store.authStatus}</p>}
        <p className="text-xs text-gray-500">Step 3+: Permissions, contacts sync, Google backup, and profile setup will be added next.</p>
      </div>
    </div>
  )
}
