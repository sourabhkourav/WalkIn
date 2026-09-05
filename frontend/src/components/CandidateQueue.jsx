const ACTIONS = {
  WAITING: [
    ['CALLED', 'Call candidate'],
    ['WITHDRAWN', 'Withdraw'],
  ],
  CALLED: [
    ['COMPLETED', 'Complete'],
    ['WAITING', 'Return to waiting'],
    ['WITHDRAWN', 'Withdraw'],
  ],
}

function CandidateQueue({ candidates, busyReference, onDownloadResume, onStatusChange }) {
  if (candidates.length === 0) {
    return (
      <div className="empty-state">
        <strong>No candidates match this view</strong>
        <span>New registrations and matching search results will appear here.</span>
      </div>
    )
  }

  return (
    <div className="queue-table-wrap">
      <table className="queue-table">
        <thead>
          <tr>
            <th scope="col">Candidate</th>
            <th scope="col">Contact</th>
            <th scope="col">Registered</th>
            <th scope="col">Status</th>
            <th scope="col"><span className="sr-only">Actions</span></th>
          </tr>
        </thead>
        <tbody>
          {candidates.map((candidate) => {
            const name = candidateName(candidate)
            const busy = busyReference === candidate.registrationReference
            return (
              <tr key={candidate.registrationReference}>
                <td data-label="Candidate">
                  <strong>{name}</strong>
                  <span className="candidate-reference">
                    Ref {candidate.registrationReference.slice(0, 8)}
                  </span>
                </td>
                <td data-label="Contact">
                  {candidate.email && <span>{candidate.email}</span>}
                  {candidate.contactNumber && <span>{candidate.contactNumber}</span>}
                  {!candidate.email && !candidate.contactNumber && <span>Not requested</span>}
                </td>
                <td data-label="Registered">{formatDateTime(candidate.registeredAt)}</td>
                <td data-label="Status">
                  <span className={`status-badge status-${candidate.status.toLowerCase()}`}>
                    {formatStatus(candidate.status)}
                  </span>
                </td>
                <td className="queue-actions" data-label="Actions">
                  {candidate.resumeAvailable && (
                    <button
                      type="button"
                      className="text-button"
                      disabled={busy}
                      onClick={() => onDownloadResume(candidate)}
                    >
                      Resume
                    </button>
                  )}
                  {(ACTIONS[candidate.status] ?? []).map(([status, label]) => (
                    <button
                      type="button"
                      className={status === 'WITHDRAWN' ? 'text-button danger-text' : 'text-button'}
                      disabled={busy}
                      key={status}
                      onClick={() => onStatusChange(candidate, status)}
                    >
                      {label}
                    </button>
                  ))}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

function candidateName(candidate) {
  return [candidate.firstName, candidate.lastName].filter(Boolean).join(' ') || 'Unnamed candidate'
}

function formatStatus(status) {
  return status.charAt(0) + status.slice(1).toLowerCase()
}

function formatDateTime(value) {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export default CandidateQueue
