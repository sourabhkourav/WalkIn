function CandidateForm({ isSubmitting = false, onSubmit }) {
  function handleSubmit(event) {
    event.preventDefault()
    const form = event.currentTarget
    const formData = new FormData(form)

    onSubmit({
      firstName: formData.get('firstName').trim(),
      lastName: formData.get('lastName').trim(),
      email: formData.get('email').trim(),
      contactNumber: formData.get('contactNumber').trim(),
    })
  }

  return (
    <section className="candidate-form-section" aria-labelledby="registration-heading">
      <h2 id="registration-heading">Register candidate</h2>
      <form className="candidate-form" onSubmit={handleSubmit}>
        <label className="field">
          First name
          <input name="firstName" maxLength="50" required />
        </label>
        <label className="field">
          Last name
          <input name="lastName" maxLength="50" required />
        </label>
        <label className="field">
          Email
          <input name="email" type="email" maxLength="100" required />
        </label>
        <label className="field">
          Contact number
          <input
            name="contactNumber"
            type="tel"
            inputMode="tel"
            pattern="\+?[0-9]{10,15}"
            required
          />
        </label>
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Registering…' : 'Register candidate'}
        </button>
      </form>
    </section>
  )
}

export default CandidateForm
