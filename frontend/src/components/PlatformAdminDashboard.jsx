import { useEffect, useState } from 'react'
import { createCompany, createCompanyAdmin, getCompanies } from '../api/platformApi'

const EMPTY_COMPANY = { companyName: '', email: '', contactNumber: '', jobDescription: '' }
const EMPTY_ADMIN = { companyId: '', username: '', password: '' }

function PlatformAdminDashboard({ accessToken, onLogout }) {
  const [companies, setCompanies] = useState([])
  const [companyForm, setCompanyForm] = useState(EMPTY_COMPANY)
  const [adminForm, setAdminForm] = useState(EMPTY_ADMIN)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    getCompanies(accessToken)
      .then((page) => setCompanies(page.content))
      .catch((requestError) => setError(requestError.message))
  }, [accessToken])

  async function handleCompanySubmit(event) {
    event.preventDefault()
    setError('')
    try {
      const company = await createCompany(accessToken, companyForm)
      setCompanies((current) => [...current, company])
      setCompanyForm(EMPTY_COMPANY)
      setMessage(`${company.companyName} was added.`)
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  async function handleAdminSubmit(event) {
    event.preventDefault()
    setError('')
    try {
      const admin = await createCompanyAdmin(
        accessToken, Number(adminForm.companyId), adminForm,
      )
      setAdminForm(EMPTY_ADMIN)
      setMessage(`${admin.username} can now administer ${admin.companyName}.`)
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return (
    <main className="operations-shell">
      <header className="topbar">
        <span className="brand"><span className="brand-mark">W</span>WalkIn</span>
        <button type="button" className="ghost-button" onClick={onLogout}>Sign out</button>
      </header>
      <div className="operations-content">
        <section className="dashboard-heading">
          <div><p className="eyebrow">Platform administration</p><h1>Companies</h1>
            <p>Manage hiring-company tenants and their first administrator.</p></div>
        </section>
        {error && <p className="error-message" role="alert">{error}</p>}
        {message && <p className="success-message" role="status">{message}</p>}
        <div className="admin-grid">
          <form className="setup-card" onSubmit={handleCompanySubmit}>
            <h2>Add hiring company</h2>
            {Object.keys(EMPTY_COMPANY).map((field) => (
              <label key={field}>{label(field)}
                {field === 'jobDescription' ? (
                  <textarea required value={companyForm[field]}
                    onChange={(event) => setCompanyForm({ ...companyForm, [field]: event.target.value })} />
                ) : (
                  <input required type={field === 'email' ? 'email' : 'text'} value={companyForm[field]}
                    onChange={(event) => setCompanyForm({ ...companyForm, [field]: event.target.value })} />
                )}
              </label>
            ))}
            <button type="submit">Add company</button>
          </form>
          <form className="setup-card" onSubmit={handleAdminSubmit}>
            <h2>Create company administrator</h2>
            <label>Company<select required value={adminForm.companyId}
              onChange={(event) => setAdminForm({ ...adminForm, companyId: event.target.value })}>
              <option value="">Select company</option>
              {companies.map((company) => <option key={company.companyId} value={company.companyId}>{company.companyName}</option>)}
            </select></label>
            <label>Username<input required minLength="3" value={adminForm.username}
              onChange={(event) => setAdminForm({ ...adminForm, username: event.target.value })} /></label>
            <label>Temporary password<input required type="password" minLength="12" value={adminForm.password}
              onChange={(event) => setAdminForm({ ...adminForm, password: event.target.value })} /></label>
            <button type="submit">Create administrator</button>
          </form>
        </div>
      </div>
    </main>
  )
}

function label(value) {
  return value.replace(/([A-Z])/g, ' $1').replace(/^./, (character) => character.toUpperCase())
}

export default PlatformAdminDashboard
