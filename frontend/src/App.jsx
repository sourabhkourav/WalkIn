import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/authContext'
import LoginForm from './components/LoginForm'
import OrganizerDashboard from './components/OrganizerDashboard'
import PublicRegistrationPage from './components/PublicRegistrationPage'
import PlatformAdminDashboard from './components/PlatformAdminDashboard'
import { readTokenIdentity } from './auth/tokenIdentity'
import { registrationTokenFromPath } from './utils/registrationRoute'
import './App.css'

function Application() {
  const { session, logout } = useAuth()

  if (!session) return <LoginForm />

  const identity = readTokenIdentity(session.accessToken)
  if (!identity) {
    return <main className="login-shell"><section className="login-card">
      <h1>Session unavailable</h1><p>Please sign in again.</p>
      <button type="button" onClick={logout}>Sign out</button>
    </section></main>
  }

  if (identity.roles.includes('ROLE_PLATFORM_ADMIN')) {
    return <PlatformAdminDashboard accessToken={session.accessToken} onLogout={logout} />
  }

  return <OrganizerDashboard accessToken={session.accessToken} identity={identity} onLogout={logout} />
}

function App() {
  const registrationToken = registrationTokenFromPath(window.location.pathname)
  if (registrationToken) {
    return <PublicRegistrationPage registrationToken={registrationToken} />
  }

  return (
    <AuthProvider>
      <Application />
    </AuthProvider>
  )
}

export default App
