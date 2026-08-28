function CandidateList({ candidates }) {
  if (candidates.length === 0) {
    return <p>No candidates found.</p>
  }

  return (
    <ul>
      {candidates.map((candidate) => (
        <li key={candidate.studentId}>
          <strong>
            {candidate.firstName} {candidate.lastName}
          </strong>
          <span>{candidate.email}</span>
        </li>
      ))}
    </ul>
  )
}

export default CandidateList