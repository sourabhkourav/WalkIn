function CandidateList({ candidates, deletingStudentId, onDelete, onEdit }) {
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
            {onEdit && (
              <button type="button" className="edit-button" onClick={() => onEdit(candidate)}>
                Edit
              </button>
            )}
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
