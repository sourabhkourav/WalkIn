import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/authContext'
import LoginForm from './components/LoginForm'
import './App.css'

function Application() {
  const { session, logout } = useAuth()

  if (!session) return <LoginForm />

  return (
    <main className="app-shell">
      <section className="panel dashboard">
        <p className="eyebrow">WalkIn administration</p>
        <h1>Welcome back</h1>
        <p>You are signed in and ready to manage walk-in drives.</p>
        <button type="button" className="secondary-button" onClick={logout}>Sign out</button>
      </section>
    </main>
  )
}

function App() {
  return <AuthProvider><Application /></AuthProvider>
}

export default App
