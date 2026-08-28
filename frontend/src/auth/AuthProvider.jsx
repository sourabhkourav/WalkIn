import { useEffect, useMemo, useState } from 'react'
import { login as requestLogin } from '../api/authApi'
import { AuthContext } from './authContext'

const STORAGE_KEY = 'walkin.auth'

function readSession() {
  try {
    const session = JSON.parse(sessionStorage.getItem(STORAGE_KEY))
    if (!session?.accessToken || Date.parse(session.expiresAt) <= Date.now()) {
      sessionStorage.removeItem(STORAGE_KEY)
      return null
    }
    return session
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession)

  useEffect(() => {
    if (!session) return undefined
    const remainingTime = Math.max(0, Date.parse(session.expiresAt) - Date.now())
    const timeoutId = window.setTimeout(() => {
      sessionStorage.removeItem(STORAGE_KEY)
      setSession(null)
    }, remainingTime)
    return () => window.clearTimeout(timeoutId)
  }, [session])

  const value = useMemo(() => ({
    session,
    login: async (credentials) => {
      const nextSession = await requestLogin(credentials)
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(nextSession))
      setSession(nextSession)
    },
    logout: () => {
      sessionStorage.removeItem(STORAGE_KEY)
      setSession(null)
    },
  }), [session])

  return <AuthContext value={value}>{children}</AuthContext>
}
