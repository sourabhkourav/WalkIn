const STATUS_CARDS = [
  ['waiting', 'Waiting', 'Candidates ready for their turn'],
  ['called', 'Called', 'Candidates expected at a round'],
  ['completed', 'Completed', 'Candidates finished at the venue'],
  ['withdrawn', 'Withdrawn', 'Candidates no longer in the queue'],
]

function QueueSummary({ summary }) {
  return (
    <section className="summary-grid" aria-label="Queue summary">
      {STATUS_CARDS.map(([key, label, description]) => (
        <article className={`summary-card summary-${key}`} key={key}>
          <span>{label}</span>
          <strong>{summary[key] ?? 0}</strong>
          <small>{description}</small>
        </article>
      ))}
    </section>
  )
}

export default QueueSummary
