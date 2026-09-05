import { useEffect, useMemo, useState } from 'react'
import {
  getCandidateQueue,
  getCandidateQueueSummary,
  getCandidateResume,
  updateCandidateStatus,
} from '../api/candidateQueueApi'
import { getHiringDrives } from '../api/hiringDriveApi'
import CandidateQueue from './CandidateQueue'
import QueueSummary from './QueueSummary'

const EMPTY_PAGE = {
  content: [],
  page: 0,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
}

const EMPTY_SUMMARY = {
  waiting: 0,
  called: 0,
  completed: 0,
  withdrawn: 0,
  total: 0,
}

function OrganizerDashboard({ accessToken, onLogout }) {
  const [drives, setDrives] = useState([])
  const [selectedDriveId, setSelectedDriveId] = useState('')
  const [isLoadingDrives, setIsLoadingDrives] = useState(true)
  const [driveError, setDriveError] = useState('')
  const [queuePage, setQueuePage] = useState(EMPTY_PAGE)
  const [summary, setSummary] = useState(EMPTY_SUMMARY)
  const [statusFilter, setStatusFilter] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [query, setQuery] = useState('')
  const [page, setPage] = useState(0)
  const [refreshVersion, setRefreshVersion] = useState(0)
  const [isLoadingQueue, setIsLoadingQueue] = useState(true)
  const [queueError, setQueueError] = useState('')
  const [queueMessage, setQueueMessage] = useState('')
  const [busyReference, setBusyReference] = useState(null)

  useEffect(() => {
    let active = true
    getHiringDrives(accessToken)
      .then((response) => {
        if (!active) return
        setDrives(response.content)
        setSelectedDriveId((current) => current || String(response.content[0]?.driveId ?? ''))
      })
      .catch((error) => {
        if (active) setDriveError(error.message)
      })
      .finally(() => {
        if (active) setIsLoadingDrives(false)
      })

    return () => {
      active = false
    }
  }, [accessToken])

  useEffect(() => {
    if (!selectedDriveId) {
      return undefined
    }

    let active = true
    Promise.all([
      getCandidateQueue(accessToken, selectedDriveId, {
        status: statusFilter,
        query,
        page,
      }),
      getCandidateQueueSummary(accessToken, selectedDriveId),
    ])
      .then(([nextPage, nextSummary]) => {
        if (!active) return
        setQueuePage(nextPage)
        setSummary(nextSummary)
      })
      .catch((error) => {
        if (active) setQueueError(error.message)
      })
      .finally(() => {
        if (active) setIsLoadingQueue(false)
      })

    return () => {
      active = false
    }
  }, [accessToken, page, query, refreshVersion, selectedDriveId, statusFilter])

  const selectedDrive = useMemo(
    () => drives.find((drive) => String(drive.driveId) === selectedDriveId),
    [drives, selectedDriveId],
  )

  function handleDriveChange(event) {
    setIsLoadingQueue(true)
    setQueueError('')
    setQueuePage(EMPTY_PAGE)
    setSummary(EMPTY_SUMMARY)
    setSelectedDriveId(event.target.value)
    setPage(0)
    setStatusFilter('')
    setSearchInput('')
    setQuery('')
    setQueueMessage('')
  }

  function handleSearch(event) {
    event.preventDefault()
    setIsLoadingQueue(true)
    setQueueError('')
    setPage(0)
    setQuery(searchInput.trim())
  }

  async function handleStatusChange(candidate, targetStatus) {
    if (['COMPLETED', 'WITHDRAWN'].includes(targetStatus)) {
      const confirmed = window.confirm(
        `${formatStatus(targetStatus)} ${candidateName(candidate)}? This status is final.`,
      )
      if (!confirmed) return
    }

    setBusyReference(candidate.registrationReference)
    setQueueError('')
    setQueueMessage('')
    try {
      await updateCandidateStatus(
        accessToken,
        selectedDriveId,
        candidate.registrationReference,
        targetStatus,
      )
      setQueueMessage(`${candidateName(candidate)} moved to ${formatStatus(targetStatus)}.`)
      setIsLoadingQueue(true)
      setRefreshVersion((version) => version + 1)
    } catch (error) {
      setQueueError(error.message)
    } finally {
      setBusyReference(null)
    }
  }

  async function handleResumeDownload(candidate) {
    setBusyReference(candidate.registrationReference)
    setQueueError('')
    try {
      const resume = await getCandidateResume(
        accessToken,
        selectedDriveId,
        candidate.registrationReference,
      )
      const objectUrl = URL.createObjectURL(resume)
      const link = document.createElement('a')
      link.href = objectUrl
      link.download = `candidate-${candidate.registrationReference}.pdf`
      link.click()
      URL.revokeObjectURL(objectUrl)
    } catch (error) {
      setQueueError(error.message)
    } finally {
      setBusyReference(null)
    }
  }

  return (
    <main className="operations-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="WalkIn operations home">
          <span className="brand-mark" aria-hidden="true">W</span>
          <span>WalkIn</span>
        </a>
        <div className="topbar-actions">
          <span className="secure-label">Secure organizer session</span>
          <button type="button" className="ghost-button" onClick={onLogout}>Sign out</button>
        </div>
      </header>

      <div className="operations-content" id="top">
        <section className="dashboard-heading">
          <div>
            <p className="eyebrow">Venue operations</p>
            <h1>Candidate queue</h1>
            <p>Keep candidates moving while giving them the freedom to step away between rounds.</p>
          </div>
          <label className="drive-picker">
            Hiring drive
            <select
              value={selectedDriveId}
              onChange={handleDriveChange}
              disabled={isLoadingDrives || drives.length === 0}
            >
              {drives.length === 0 && <option value="">No hiring drives</option>}
              {drives.map((drive) => (
                <option value={drive.driveId} key={drive.driveId}>
                  {drive.driveName}
                </option>
              ))}
            </select>
          </label>
        </section>

        {driveError && <ErrorNotice message={driveError} />}
        {selectedDrive && (
          <section className="drive-context" aria-label="Selected hiring drive">
            <div>
              <strong>{selectedDrive.driveName}</strong>
              <span>{selectedDrive.venue}</span>
            </div>
            <span className={`drive-status drive-status-${selectedDrive.status.toLowerCase()}`}>
              {formatStatus(selectedDrive.status)}
            </span>
          </section>
        )}

        {selectedDriveId && (
          <>
            <QueueSummary summary={summary} />

            <section className="queue-panel" aria-labelledby="queue-heading">
              <div className="queue-panel-heading">
                <div>
                  <p className="eyebrow">Today at the venue</p>
                  <h2 id="queue-heading">Registrations</h2>
                </div>
                <button
                  type="button"
                  className="ghost-button refresh-button"
                  disabled={isLoadingQueue}
                  onClick={() => {
                    setIsLoadingQueue(true)
                    setQueueError('')
                    setRefreshVersion((version) => version + 1)
                  }}
                >
                  Refresh
                </button>
              </div>

              <div className="queue-toolbar">
                <form className="search-form" role="search" onSubmit={handleSearch}>
                  <label className="sr-only" htmlFor="candidate-search">Search candidates</label>
                  <input
                    id="candidate-search"
                    type="search"
                    value={searchInput}
                    maxLength="100"
                    placeholder="Search name, email, or phone"
                    onChange={(event) => setSearchInput(event.target.value)}
                  />
                  <button type="submit">Search</button>
                </form>
                <label className="status-filter">
                  <span>Status</span>
                  <select
                    value={statusFilter}
                    onChange={(event) => {
                      setIsLoadingQueue(true)
                      setQueueError('')
                      setStatusFilter(event.target.value)
                      setPage(0)
                    }}
                  >
                    <option value="">All statuses</option>
                    <option value="WAITING">Waiting</option>
                    <option value="CALLED">Called</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="WITHDRAWN">Withdrawn</option>
                  </select>
                </label>
              </div>

              {queueError && <ErrorNotice message={queueError} />}
              {queueMessage && <p className="success-message" role="status">{queueMessage}</p>}
              {isLoadingQueue ? (
                <div className="loading-state" role="status">Loading candidate queue…</div>
              ) : (
                <CandidateQueue
                  candidates={queuePage.content}
                  busyReference={busyReference}
                  onDownloadResume={handleResumeDownload}
                  onStatusChange={handleStatusChange}
                />
              )}

              <footer className="pagination">
                <span>
                  {queuePage.totalElements} candidate{queuePage.totalElements === 1 ? '' : 's'}
                </span>
                <div>
                  <button
                    type="button"
                    className="ghost-button"
                    disabled={queuePage.first || isLoadingQueue}
                    onClick={() => {
                      setIsLoadingQueue(true)
                      setQueueError('')
                      setPage((current) => Math.max(0, current - 1))
                    }}
                  >
                    Previous
                  </button>
                  <span>Page {queuePage.totalPages === 0 ? 0 : queuePage.page + 1} of {queuePage.totalPages}</span>
                  <button
                    type="button"
                    className="ghost-button"
                    disabled={queuePage.last || isLoadingQueue}
                    onClick={() => {
                      setIsLoadingQueue(true)
                      setQueueError('')
                      setPage((current) => current + 1)
                    }}
                  >
                    Next
                  </button>
                </div>
              </footer>
            </section>
          </>
        )}

        {!isLoadingDrives && !driveError && drives.length === 0 && (
          <section className="no-drive-state">
            <strong>No hiring drives yet</strong>
            <p>Create a hiring drive through the API before opening the venue queue.</p>
          </section>
        )}
      </div>
    </main>
  )
}

function ErrorNotice({ message }) {
  return <p className="error-message" role="alert">{message}</p>
}

function candidateName(candidate) {
  return [candidate.firstName, candidate.lastName].filter(Boolean).join(' ') || 'Candidate'
}

function formatStatus(status) {
  return status.charAt(0) + status.slice(1).toLowerCase()
}

export default OrganizerDashboard
