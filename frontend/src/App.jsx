import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/authContext'
import LoginForm from './components/LoginForm'
import OrganizerDashboard from './components/OrganizerDashboard'
import PublicRegistrationPage from './components/PublicRegistrationPage'
import { registrationTokenFromPath } from './utils/registrationRoute'
import './App.css'

function Application() {
  const { session, logout } = useAuth()

  if (!session) return <LoginForm />

  return <OrganizerDashboard accessToken={session.accessToken} onLogout={logout} />
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
