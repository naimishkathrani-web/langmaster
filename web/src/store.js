import { useState, useCallback } from 'react'

export const LANGUAGES = ['English', 'Hindi', 'Gujarati', 'Marathi', 'Tamil']
export const LOCAL_PHONE = '+911111111111'
export const CONTACT_PHONE = '+919999999999'
export const gid = () => crypto.randomUUID()

export function shouldUseAi(on, listener, source) {
  if (!on || !listener || !source) return false
  return listener.toLowerCase() !== source.toLowerCase()
}

export function useStore() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [authStatus, setAuthStatus] = useState(null)
  const [pinMap, setPinMap] = useState({})
  const [conversations] = useState([
    { id: 'conv-ravi', type: 'DIRECT', title: 'Ravi' },
    { id: 'conv-group-1', type: 'GROUP', title: 'LangMaster Squad' }
  ])
  const [messages, setMessages] = useState([
    { id: gid(), conversationId: 'conv-ravi', senderPhoneE164: CONTACT_PHONE, body: '[Hindi] Namaste!', createdAt: Date.now() - 60000, deletedForEveryoneAt: null, deletedForMe: false }
  ])
  const [activeConvId, setActiveConvId] = useState('conv-ravi')
  const [translationSessions, setTranslationSessions] = useState([])
  const [learningTracks] = useState([
    { id: 'track-English-beginner', languageCode: 'English', level: 'BEGINNER', title: 'English Foundations', description: 'Start speaking and understanding daily English quickly.' }
  ])
  const [learningModules] = useState([
    { id: 'm1', phaseOrder: 1, title: 'Phase 1: Sounds & Greetings', goal: 'Understand pronunciation and greeting patterns.' },
    { id: 'm2', phaseOrder: 2, title: 'Phase 2: Everyday Conversations', goal: 'Build sentence-level confidence.' },
    { id: 'm3', phaseOrder: 3, title: 'Phase 3: Certification Prep', goal: 'Prepare for formal language assessments.' }
  ])
  const [learnerProgress, setLearnerProgress] = useState([])

  const activeConvTitle = conversations.find(c => c.id === activeConvId)?.title || 'Conversation'
  const activeMessages = messages.filter(m => m.conversationId === activeConvId && !m.deletedForMe && !m.deletedForEveryoneAt)

  const registerPin = useCallback((phone, pin, confirmPin) => {
    if (pin !== confirmPin) { setAuthStatus('PIN mismatch. Please re-enter.'); return }
    if (!/^[0-9]{4}$/.test(pin)) { setAuthStatus('PIN must be exactly 4 digits.'); return }
    setPinMap(p => ({ ...p, [phone]: pin }))
    setAuthStatus('PIN registered. You can log in now.')
  }, [])

  const loginWithPin = useCallback((phone, pin) => {
    const ok = pinMap[phone] === pin
    setIsLoggedIn(ok)
    setAuthStatus(ok ? 'Login successful.' : 'Invalid pin')
  }, [pinMap])

  const sendMessage = useCallback((text, lang) => {
    if (!text.trim()) return
    setMessages(p => [...p, { id: gid(), conversationId: activeConvId, senderPhoneE164: LOCAL_PHONE, body: `[${lang}] ${text}`, createdAt: Date.now(), deletedForEveryoneAt: null, deletedForMe: false }])
  }, [activeConvId])

  const deleteForEveryone = useCallback((msg) => {
    if (msg.createdAt >= Date.now() - 86400000)
      setMessages(p => p.map(m => m.id === msg.id ? { ...m, deletedForEveryoneAt: Date.now() } : m))
  }, [])

  const deleteForMe = useCallback((id) => {
    setMessages(p => p.map(m => m.id === id ? { ...m, deletedForMe: true } : m))
  }, [])

  const forwardMessage = useCallback((msg, targetId) => {
    setMessages(p => [...p, { ...msg, id: gid(), conversationId: targetId, senderPhoneE164: LOCAL_PHONE, createdAt: Date.now() }])
  }, [])

  const saveTranslation = useCallback((source, target, input) => {
    const output = `[${target}] ${input}`
    setTranslationSessions(p => [...p, { id: gid(), sourceLang: source, targetLang: target, inputText: input, outputText: output, createdAt: Date.now() }])
  }, [])

  const markProgress = useCallback((moduleId, score) => {
    setLearnerProgress(p => [...p, { id: gid(), moduleId, status: score >= 75 ? 'COMPLETED' : 'IN_PROGRESS', scorePercent: score, updatedAt: Date.now() }])
  }, [])

  return {
    isLoggedIn, authStatus, conversations, activeConvId, setActiveConvId, activeConvTitle,
    activeMessages, translationSessions, learningTracks, learningModules, learnerProgress,
    registerPin, loginWithPin, sendMessage, deleteForEveryone, deleteForMe, forwardMessage,
    saveTranslation, markProgress
  }
}
