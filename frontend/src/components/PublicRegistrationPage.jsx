import { useEffect, useState } from 'react'
import {
  getPublicHiringDrive,
  registerForHiringDrive,
} from '../api/publicRegistrationApi'

const MAX_RESUME_SIZE = 2 * 1024 * 1024
const INITIAL_DETAILS = {
  firstName: '',
  lastName: '',
  email: '',
  contactNumber: '',
  resume: null,
}

function PublicRegistrationPage({ registrationToken }) {
  const [drive, setDrive] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [step, setStep] = useState(1)
  const [details, setDetails] = useState(INITIAL_DETAILS)
  const [notificationChannel, setNotificationChannel] = useState('SMS')
  const [notificationDestination, setNotificationDestination] = useState('')
  const [advanceNoticeMinutes, setAdvanceNoticeMinutes] = useState(30)
  const [fileError, setFileError] = useState('')
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [confirmation, setConfirmation] = useState(null)

  useEffect(() => {
    let active = true
    getPublicHiringDrive(registrationToken)
      .then((response) => {
        if (active) setDrive(response)
      })
      .catch((error) => {
        if (active) setLoadError(error.message)
      })
      .finally(() => {
        if (active) setIsLoading(false)
      })
    return () => {
      active = false
    }
  }, [registrationToken])

  function updateDetail(event) {
    const { name, value } = event.target
    setDetails((current) => ({ ...current, [name]: value }))
  }

  function updateResume(event) {
    const file = event.target.files[0] ?? null
    setFileError('')
    if (file && file.type !== 'application/pdf') {
      setFileError('Resume must be a PDF file.')
      setDetails((current) => ({ ...current, resume: null }))
      event.target.value = ''
      return
    }
    if (file && file.size > MAX_RESUME_SIZE) {
      setFileError('Resume must be 2 MB or smaller.')
      setDetails((current) => ({ ...current, resume: null }))
      event.target.value = ''
      return
    }
    setDetails((current) => ({ ...current, resume: file }))
  }

  function continueToNotifications(event) {
    event.preventDefault()
    if (fileError) return
    setSubmitError('')
    setStep(2)
    window.scrollTo?.({ top: 0, behavior: 'smooth' })
  }

  async function submitRegistration(event) {
    event.preventDefault()
    setSubmitError('')
    setIsSubmitting(true)
    try {
      const response = await registerForHiringDrive(registrationToken, {
        ...details,
        notificationChannel,
        notificationDestination,
        advanceNoticeMinutes,
      })
      setConfirmation(response)
    } catch (error) {
      setSubmitError(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isLoading) {
    return <PublicPageFrame><div className="public-loading" role="status">Opening registration…</div></PublicPageFrame>
  }

  if (loadError) {
    return (
      <PublicPageFrame>
        <div className="public-result public-error-result" role="alert">
          <span className="result-icon" aria-hidden="true">!</span>
          <h1>Registration unavailable</h1>
          <p>{loadError}</p>
          <p className="result-help">Ask the venue team for a current registration link.</p>
        </div>
      </PublicPageFrame>
    )
  }

  if (confirmation) {
    return (
      <PublicPageFrame>
        <div className="public-result" role="status">
          <span className="result-icon success-icon" aria-hidden="true">✓</span>
          <p className="eyebrow">Registration complete</p>
          <h1>You’re in the queue</h1>
          <p>
            You can step away, but be available at the time shared in your notification.
          </p>
          <div className="reference-card">
            <span>Your reference</span>
            <strong>{confirmation.registrationReference}</strong>
          </div>
          <p className="result-help">Keep this page or save your reference until the drive ends.</p>
        </div>
      </PublicPageFrame>
    )
  }

  return (
    <PublicPageFrame>
      <header className="public-drive-header">
        <div>
          <p className="eyebrow">{drive.companyName}</p>
          <h1>{drive.driveName}</h1>
        </div>
        <dl className="drive-facts">
          <div><dt>Venue</dt><dd>{drive.venue}</dd></div>
          <div><dt>Starts</dt><dd>{formatDateTime(drive.startsAt)}</dd></div>
          <div><dt>Ends</dt><dd>{formatDateTime(drive.endsAt)}</dd></div>
        </dl>
      </header>

      <ol className="registration-progress" aria-label="Registration progress">
        <li className={step === 1 ? 'active' : 'complete'}>
          <span>1</span> Your details
        </li>
        <li className={step === 2 ? 'active' : ''}>
          <span>2</span> Notification
        </li>
      </ol>

      {step === 1 ? (
        <DetailsStep
          details={details}
          fileError={fileError}
          form={drive.registrationForm}
          onChange={updateDetail}
          onContinue={continueToNotifications}
          onResumeChange={updateResume}
        />
      ) : (
        <NotificationStep
          advanceNoticeMinutes={advanceNoticeMinutes}
          channel={notificationChannel}
          destination={notificationDestination}
          error={submitError}
          isSubmitting={isSubmitting}
          onAdvanceNoticeChange={setAdvanceNoticeMinutes}
          onBack={() => setStep(1)}
          onChannelChange={(channel) => {
            setNotificationChannel(channel)
            setNotificationDestination('')
          }}
          onDestinationChange={setNotificationDestination}
          onSubmit={submitRegistration}
        />
      )}
    </PublicPageFrame>
  )
}

function DetailsStep({ details, fileError, form, onChange, onContinue, onResumeChange }) {
  return (
    <section className="registration-card" aria-labelledby="details-heading">
      <p className="step-label">Step 1 of 2</p>
      <h2 id="details-heading">Tell the team who you are</h2>
      <p>Only information requested for this hiring drive is shown here.</p>
      <form className="public-form" onSubmit={onContinue}>
        <ConfiguredField label="First name" name="firstName" requirement={form.firstName} value={details.firstName} onChange={onChange} />
        <ConfiguredField label="Last name" name="lastName" requirement={form.lastName} value={details.lastName} onChange={onChange} />
        <ConfiguredField label="Email" name="email" type="email" requirement={form.email} value={details.email} onChange={onChange} />
        <ConfiguredField label="Contact number" name="contactNumber" type="tel" requirement={form.contactNumber} value={details.contactNumber} onChange={onChange} pattern="[+]?[0-9]{10,15}" />
        {form.resume !== 'HIDDEN' && (
          <label className="field file-field">
            <span>Resume {form.resume === 'OPTIONAL' && <small>Optional</small>}</span>
            <input
              name="resume"
              type="file"
              accept="application/pdf,.pdf"
              required={form.resume === 'REQUIRED' && !details.resume}
              onChange={onResumeChange}
            />
            <small>
              {details.resume ? `${details.resume.name} selected` : 'PDF only, up to 2 MB'}
            </small>
          </label>
        )}
        {fileError && <p className="error-message" role="alert">{fileError}</p>}
        <button type="submit" className="wide-button">Continue</button>
      </form>
    </section>
  )
}

function ConfiguredField({ label, name, requirement, type = 'text', ...inputProps }) {
  if (requirement === 'HIDDEN') return null
  return (
    <label className="field">
      <span>{label} {requirement === 'OPTIONAL' && <small>Optional</small>}</span>
      <input
        name={name}
        type={type}
        required={requirement === 'REQUIRED'}
        maxLength={name === 'contactNumber' ? 15 : name.includes('Name') ? 50 : 100}
        {...inputProps}
      />
    </label>
  )
}

function NotificationStep({
  advanceNoticeMinutes,
  channel,
  destination,
  error,
  isSubmitting,
  onAdvanceNoticeChange,
  onBack,
  onChannelChange,
  onDestinationChange,
  onSubmit,
}) {
  const usesEmail = channel === 'EMAIL'
  return (
    <section className="registration-card" aria-labelledby="notification-heading">
      <p className="step-label">Step 2 of 2</p>
      <h2 id="notification-heading">How should we remind you?</h2>
      <p>This preference is yours. It is kept separate from details requested by the company.</p>
      <form className="public-form" onSubmit={onSubmit}>
        <fieldset className="channel-options">
          <legend>Notification channel</legend>
          {[
            ['SMS', 'SMS'],
            ['WHATSAPP', 'WhatsApp'],
            ['EMAIL', 'Email'],
          ].map(([value, label]) => (
            <label className={channel === value ? 'selected' : ''} key={value}>
              <input
                type="radio"
                name="notificationChannel"
                value={value}
                checked={channel === value}
                onChange={() => onChannelChange(value)}
              />
              {label}
            </label>
          ))}
        </fieldset>
        <label className="field">
          {usesEmail ? 'Notification email' : 'Notification phone number'}
          <input
            type={usesEmail ? 'email' : 'tel'}
            value={destination}
            inputMode={usesEmail ? 'email' : 'tel'}
            pattern={usesEmail ? undefined : '[+]?[0-9]{10,15}'}
            maxLength="100"
            autoComplete={usesEmail ? 'email' : 'tel'}
            onChange={(event) => onDestinationChange(event.target.value)}
            required
          />
        </label>
        <label className="field">
          Notify me this many minutes before my reporting time
          <input
            type="number"
            min="5"
            max="240"
            value={advanceNoticeMinutes}
            onChange={(event) => onAdvanceNoticeChange(Number(event.target.value))}
            required
          />
        </label>
        <aside className="privacy-note">
          Your notification contact is used for venue reminders and is not displayed in the
          organizer candidate queue.
        </aside>
        {error && <p className="error-message" role="alert">{error}</p>}
        <div className="public-form-actions">
          <button type="button" className="ghost-button" disabled={isSubmitting} onClick={onBack}>Back</button>
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Joining queue…' : 'Join candidate queue'}
          </button>
        </div>
      </form>
    </section>
  )
}

function PublicPageFrame({ children }) {
  return (
    <main className="public-registration-shell">
      <nav className="public-brand" aria-label="WalkIn">
        <span className="brand-mark" aria-hidden="true">W</span>
        <strong>WalkIn</strong>
        <span>Venue queue</span>
      </nav>
      <div className="public-registration-content">{children}</div>
    </main>
  )
}

function formatDateTime(value) {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export default PublicRegistrationPage
