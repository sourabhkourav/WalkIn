import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/authContext'
import LoginForm from './components/LoginForm'
import OrganizerDashboard from './components/OrganizerDashboard'
import './App.css'

function Application() {
  const { session, logout } = useAuth()

  if (!session) return <LoginForm />

  return <OrganizerDashboard accessToken={session.accessToken} onLogout={logout} />
}

function App() {
  return (
    <AuthProvider>
      <Application />
    </AuthProvider>
  )
}

export default App
