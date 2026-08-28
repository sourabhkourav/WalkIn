function CandidateList({ candidates, deletingStudentId, onDelete }) {
  if (candidates.length === 0) {
    return <p>No candidates found.</p>
  }

  return (
    <section className="candidate-section" aria-labelledby="candidate-heading">
        <h2 id="candidate-heading">Candidates</h2>

        <ul className="candidate-list">
        {candidates.map((candidate) => (
            <li className="candidate-card" key={candidate.studentId}>
            <strong>
                {candidate.firstName} {candidate.lastName}
            </strong>
            <span>{candidate.email}</span>
            <span>{candidate.contactNumber}</span>
            {onDelete && (
              <button
                type="button"
                className="danger-button"
                disabled={deletingStudentId === candidate.studentId}
                onClick={() => onDelete(candidate)}
              >
                {deletingStudentId === candidate.studentId ? 'Deleting…' : 'Delete'}
              </button>
            )}
            </li>
        ))}
        </ul>
    </section>
    )
}

export default CandidateList
