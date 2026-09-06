import { useEffect, useState } from 'react'
import {
  createRecruiter,
  getCompanyUsers,
  updateRecruiterStatus,
} from '../api/companyUserApi'

function RecruiterPanel({ accessToken }) {
  const [users, setUsers] = useState([])
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    getCompanyUsers(accessToken)
      .then((page) => setUsers(page.content))
      .catch((requestError) => setError(requestError.message))
  }, [accessToken])

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    try {
      const recruiter = await createRecruiter(accessToken, form)
      setUsers((current) => [...current, recruiter])
      setForm({ username: '', password: '' })
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  async function toggle(recruiter) {
    setError('')
    try {
      const updated = await updateRecruiterStatus(
        accessToken, recruiter.id, !recruiter.enabled,
      )
      setUsers((current) => current.map((user) => user.id === updated.id ? updated : user))
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return (
    <section className="setup-card" aria-labelledby="recruiters-heading">
      <div><p className="eyebrow">Company access</p><h2 id="recruiters-heading">Recruiters</h2></div>
      {error && <p className="error-message" role="alert">{error}</p>}
      <form className="setup-grid" onSubmit={handleSubmit}>
        <label>Username<input required minLength="3" value={form.username}
          onChange={(event) => setForm({ ...form, username: event.target.value })} /></label>
        <label>Temporary password<input required type="password" minLength="12" value={form.password}
          onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
        <button type="submit">Add recruiter</button>
      </form>
      <div className="recruiter-list">
        {users.filter((user) => user.role === 'RECRUITER').map((recruiter) => (
          <div key={recruiter.id}>
            <span><strong>{recruiter.username}</strong> · {recruiter.enabled ? 'Active' : 'Disabled'}</span>
            <button type="button" className="ghost-button" onClick={() => toggle(recruiter)}>
              {recruiter.enabled ? 'Disable' : 'Enable'}
            </button>
          </div>
        ))}
      </div>
    </section>
  )
}

export default RecruiterPanel
