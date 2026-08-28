import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/authContext'
import LoginForm from './components/LoginForm'
import CandidateForm from './components/CandidateForm'
import './App.css'
import CandidateList from './components/CandidateList'
import { useEffect, useState } from 'react'
import { createStudent, getStudents } from './api/studentApi'

function Application() {
  const { session, logout } = useAuth()
  const [candidates, setCandidates] = useState([])
  const [loadError, setLoadError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isRegistering, setIsRegistering] = useState(false)
  const [registrationError, setRegistrationError] = useState('')
  const [registrationMessage, setRegistrationMessage] = useState('')

  useEffect(() => {
    if (!session) return

    let cancelled = false

    getStudents(session.accessToken)
      .then((page) => {
        if (!cancelled) {
          setCandidates(page.content)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setLoadError(error.message)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [session])

  function handleLogout() {
    setCandidates([])
    setLoadError('')
    setIsLoading(true)
    logout()
  }

  async function handleCandidateRegistration(candidate) {
    setIsRegistering(true)
    setRegistrationError('')
    setRegistrationMessage('')

    try {
      const savedCandidate = await createStudent(session.accessToken, candidate)
      setCandidates((currentCandidates) => [savedCandidate, ...currentCandidates])
      setRegistrationMessage(`${savedCandidate.firstName} ${savedCandidate.lastName} registered successfully.`)
      return true
    } catch (error) {
      setRegistrationError(error.message)
      return false
    } finally {
      setIsRegistering(false)
    }
  }

  if (!session) return <LoginForm />

  return (
    <main className="app-shell">
      <section className="panel dashboard">
        {isLoading && <p role="status">Loading candidates…</p>}
        {!isLoading && loadError && (
          <p className="error-message" role="alert">
            {loadError}
          </p>
        )}
        <p className="eyebrow">WalkIn administration</p>
        <h1>Welcome back</h1>
        <p>You are signed in and ready to manage walk-in drives.</p>
        {registrationError && <p className="error-message" role="alert">{registrationError}</p>}
        {registrationMessage && <p className="success-message" role="status">{registrationMessage}</p>}
        <CandidateForm isSubmitting={isRegistering} onSubmit={handleCandidateRegistration} />
        {!isLoading && !loadError && <CandidateList candidates={candidates} />}
        <button type="button" className="secondary-button" onClick={handleLogout}>Sign out</button>
      </section>
    </main>
  )
}

function App() {
  return <AuthProvider><Application /></AuthProvider>
}

export default App
