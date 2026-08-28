function CandidateForm({ candidate = null, isSubmitting = false, onCancel, onSubmit }) {
  const isEditing = candidate !== null

  async function handleSubmit(event) {
    event.preventDefault()
    const form = event.currentTarget
    const formData = new FormData(form)

    const wasSuccessful = await onSubmit({
      firstName: formData.get('firstName').trim(),
      lastName: formData.get('lastName').trim(),
      email: formData.get('email').trim(),
      contactNumber: formData.get('contactNumber').trim(),
    })
    if (wasSuccessful !== false) form.reset()
  }

  return (
    <section className="candidate-form-section" aria-labelledby="registration-heading">
      <h2 id="registration-heading">{isEditing ? 'Edit candidate' : 'Register candidate'}</h2>
      <form className="candidate-form" onSubmit={handleSubmit}>
        <label className="field">
          First name
          <input name="firstName" defaultValue={candidate?.firstName} maxLength="50" required />
        </label>
        <label className="field">
          Last name
          <input name="lastName" defaultValue={candidate?.lastName} maxLength="50" required />
        </label>
        <label className="field">
          Email
          <input name="email" type="email" defaultValue={candidate?.email} maxLength="100" required />
        </label>
        <label className="field">
          Contact number
          <input
            name="contactNumber"
            type="tel"
            defaultValue={candidate?.contactNumber}
            inputMode="tel"
            pattern="\+?[0-9]{10,15}"
            required
          />
        </label>
        <div className="form-actions">
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Saving…' : isEditing ? 'Save changes' : 'Register candidate'}
          </button>
          {onCancel && (
            <button type="button" className="secondary-button" disabled={isSubmitting} onClick={onCancel}>
              Cancel
            </button>
          )}
        </div>
      </form>
    </section>
  )
}

export default CandidateForm
