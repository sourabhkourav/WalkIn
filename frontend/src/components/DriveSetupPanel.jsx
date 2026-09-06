import { useEffect, useState } from 'react'
import QRCode from 'qrcode'
import {
  createCompanyRound,
  createHiringDriveSetup,
  getCompanyRounds,
} from '../api/driveSetupApi'

const DEFAULT_FIELDS = {
  firstName: 'REQUIRED',
  lastName: 'REQUIRED',
  email: 'REQUIRED',
  contactNumber: 'REQUIRED',
  resume: 'OPTIONAL',
}

function DriveSetupPanel({ accessToken, companyId, onCreated }) {
  const [rounds, setRounds] = useState([])
  const [selectedRounds, setSelectedRounds] = useState([])
  const [details, setDetails] = useState({
    driveName: '', venue: '', startsAt: '', endsAt: '', openRegistration: true,
  })
  const [fields, setFields] = useState(DEFAULT_FIELDS)
  const [result, setResult] = useState(null)
  const [qrCode, setQrCode] = useState('')
  const [error, setError] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [newRound, setNewRound] = useState({ roundName: '', description: '' })

  useEffect(() => {
    getCompanyRounds(accessToken)
      .then((page) => setRounds(page.content))
      .catch((requestError) => setError(requestError.message))
  }, [accessToken])

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSaving(true)
    try {
      const setup = await createHiringDriveSetup(accessToken, {
        companyId,
        driveName: details.driveName,
        venue: details.venue,
        startsAt: new Date(details.startsAt).toISOString(),
        endsAt: new Date(details.endsAt).toISOString(),
        registrationForm: fields,
        companyRoundIds: selectedRounds,
        openRegistration: details.openRegistration,
      })
      const registrationUrl = `${window.location.origin}/register/${encodeURIComponent(setup.registrationToken)}`
      setQrCode(await QRCode.toDataURL(registrationUrl, { width: 280, margin: 2 }))
      setResult({ ...setup, registrationUrl })
      onCreated?.(setup.drive)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setIsSaving(false)
    }
  }

  function toggleRound(roundId) {
    setSelectedRounds((current) => current.includes(roundId)
      ? current.filter((id) => id !== roundId)
      : [...current, roundId])
  }

  async function handleRoundCreate(event) {
    event.preventDefault()
    setError('')
    try {
      const round = await createCompanyRound(accessToken, { companyId, ...newRound })
      setRounds((current) => [...current, round])
      setSelectedRounds((current) => [...current, round.companyRoundId])
      setNewRound({ roundName: '', description: '' })
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  if (result) {
    return (
      <section className="setup-card setup-result" aria-labelledby="drive-ready-heading">
        <div><p className="eyebrow">Registration ready</p>
          <h2 id="drive-ready-heading">{result.drive.driveName}</h2></div>
        {qrCode && <img src={qrCode} alt={`QR code for ${result.drive.driveName} registration`} />}
        <label>Public registration link<input readOnly value={result.registrationUrl} /></label>
        <div className="setup-actions">
          <button type="button" onClick={() => navigator.clipboard.writeText(result.registrationUrl)}>Copy link</button>
          <a className="button-link" href={qrCode} download={`walkin-drive-${result.drive.driveId}-qr.png`}>Download QR</a>
          <button type="button" className="ghost-button" onClick={() => setResult(null)}>Create another</button>
        </div>
        <p className="privacy-note">The raw registration token is shown only in this result. Store or share the generated link securely.</p>
      </section>
    )
  }

  return (
    <form className="setup-card" onSubmit={handleSubmit}>
      <div><p className="eyebrow">Company workspace</p><h2>Create a hiring drive</h2>
        <p>Configure the candidate experience before opening registration.</p></div>
      {error && <p className="error-message" role="alert">{error}</p>}
      <div className="setup-grid">
        <label>Drive name<input required maxLength="100" value={details.driveName}
          onChange={(event) => setDetails({ ...details, driveName: event.target.value })} /></label>
        <label>Venue<input required maxLength="200" value={details.venue}
          onChange={(event) => setDetails({ ...details, venue: event.target.value })} /></label>
        <label>Starts at<input required type="datetime-local" value={details.startsAt}
          onChange={(event) => setDetails({ ...details, startsAt: event.target.value })} /></label>
        <label>Ends at<input required type="datetime-local" value={details.endsAt}
          onChange={(event) => setDetails({ ...details, endsAt: event.target.value })} /></label>
      </div>
      <fieldset><legend>Candidate registration fields</legend>
        <div className="setup-grid">{Object.entries(fields).map(([field, value]) => (
          <label key={field}>{formatField(field)}<select value={value}
            onChange={(event) => setFields({ ...fields, [field]: event.target.value })}>
            {field !== 'firstName' && <option value="HIDDEN">Hidden</option>}
            <option value="OPTIONAL">Optional</option><option value="REQUIRED">Required</option>
          </select></label>
        ))}</div>
      </fieldset>
      <fieldset><legend>Interview rounds, in order</legend>
        {rounds.length === 0 ? <p>No company rounds are configured yet.</p> : rounds.map((round) => (
          <label className="round-option" key={round.companyRoundId}>
            <input type="checkbox" checked={selectedRounds.includes(round.companyRoundId)}
              onChange={() => toggleRound(round.companyRoundId)} />
            <span>{selectedRounds.indexOf(round.companyRoundId) + 1 || '–'}</span>
            {round.interviewRound.roundName}
          </label>
        ))}
      </fieldset>
      <section className="inline-round-form" aria-labelledby="custom-round-heading">
        <h3 id="custom-round-heading">Add a round on the go</h3>
        <div className="setup-grid">
          <label>Round name<input maxLength="100"
            value={newRound.roundName}
            onChange={(event) => setNewRound({ ...newRound, roundName: event.target.value })} /></label>
          <label>Description<input maxLength="2000"
            value={newRound.description}
            onChange={(event) => setNewRound({ ...newRound, description: event.target.value })} /></label>
        </div>
        <button type="button" onClick={handleRoundCreate}
          disabled={!newRound.roundName.trim() || !newRound.description.trim()}>Add and select round</button>
      </section>
      <label className="round-option"><input type="checkbox" checked={details.openRegistration}
        onChange={(event) => setDetails({ ...details, openRegistration: event.target.checked })} />
        Open public registration immediately</label>
      <button type="submit" disabled={isSaving || selectedRounds.length === 0}>
        {isSaving ? 'Creating…' : 'Create drive and registration link'}
      </button>
    </form>
  )
}

function formatField(field) {
  return field.replace(/([A-Z])/g, ' $1').replace(/^./, (character) => character.toUpperCase())
}

export default DriveSetupPanel
