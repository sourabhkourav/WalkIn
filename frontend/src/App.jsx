import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/authContext'
import LoginForm from './components/LoginForm'
import CandidateForm from './components/CandidateForm'
import './App.css'
import CandidateList from './components/CandidateList'
import { useEffect, useState } from 'react'
import { createStudent, deleteStudent, getStudents, updateStudent } from './api/studentApi'

function Application() {
  const { session, logout } = useAuth()
  const [candidates, setCandidates] = useState([])
  const [loadError, setLoadError] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isRegistering, setIsRegistering] = useState(false)
  const [registrationError, setRegistrationError] = useState('')
  const [registrationMessage, setRegistrationMessage] = useState('')
  const [deletingStudentId, setDeletingStudentId] = useState(null)
  const [deleteError, setDeleteError] = useState('')
  const [editingCandidate, setEditingCandidate] = useState(null)
  const [isUpdating, setIsUpdating] = useState(false)

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

  async function handleCandidateDelete(candidate) {
    const confirmed = window.confirm(
      `Delete ${candidate.firstName} ${candidate.lastName}? This action cannot be undone.`,
    )
    if (!confirmed) return

    setDeletingStudentId(candidate.studentId)
    setDeleteError('')

    try {
      await deleteStudent(session.accessToken, candidate.studentId)
      setCandidates((currentCandidates) =>
        currentCandidates.filter((currentCandidate) => currentCandidate.studentId !== candidate.studentId),
      )
    } catch (error) {
      setDeleteError(error.message)
    } finally {
      setDeletingStudentId(null)
    }
  }

  async function handleCandidateUpdate(candidateDetails) {
    setIsUpdating(true)
    setRegistrationError('')
    setRegistrationMessage('')

    try {
      const updatedCandidate = await updateStudent(
        session.accessToken,
        editingCandidate.studentId,
        candidateDetails,
      )
      setCandidates((currentCandidates) =>
        currentCandidates.map((candidate) =>
          candidate.studentId === updatedCandidate.studentId ? updatedCandidate : candidate,
        ),
      )
      setRegistrationMessage(`${updatedCandidate.firstName} ${updatedCandidate.lastName} updated successfully.`)
      setEditingCandidate(null)
      return true
    } catch (error) {
      setRegistrationError(error.message)
      return false
    } finally {
      setIsUpdating(false)
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
        {editingCandidate ? (
          <CandidateForm
            key={editingCandidate.studentId}
            candidate={editingCandidate}
            isSubmitting={isUpdating}
            onCancel={() => setEditingCandidate(null)}
            onSubmit={handleCandidateUpdate}
          />
        ) : (
          <CandidateForm isSubmitting={isRegistering} onSubmit={handleCandidateRegistration} />
        )}
        {deleteError && <p className="error-message" role="alert">{deleteError}</p>}
        {!isLoading && !loadError && (
          <CandidateList
            candidates={candidates}
            deletingStudentId={deletingStudentId}
            onDelete={handleCandidateDelete}
            onEdit={setEditingCandidate}
          />
        )}
        <button type="button" className="secondary-button" onClick={handleLogout}>Sign out</button>
      </section>
    </main>
  )
}

function App() {
  return <AuthProvider><Application /></AuthProvider>
}

export default App
