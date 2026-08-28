import { useState } from 'react'
import { useAuth } from '../auth/authContext'

function LoginForm() {
  const { login } = useAuth()
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    const formData = new FormData(event.currentTarget)

    try {
      await login({ username: formData.get('username'), password: formData.get('password') })
    } catch (loginError) {
      setError(loginError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="app-shell">
      <section className="panel" aria-labelledby="login-heading">
        <p className="eyebrow">WalkIn administration</p>
        <h1 id="login-heading">Sign in</h1>
        <p className="intro">Use your administrator or recruiter account.</p>
        <form className="login-form" onSubmit={handleSubmit}>
          <label className="field">Username<input name="username" autoComplete="username" required autoFocus /></label>
          <label className="field">Password<input name="password" type="password" autoComplete="current-password" required /></label>
          {error && <p className="error-message" role="alert">{error}</p>}
          <button type="submit" disabled={isSubmitting}>{isSubmitting ? 'Signing in…' : 'Sign in'}</button>
        </form>
      </section>
    </main>
  )
}

export default LoginForm
