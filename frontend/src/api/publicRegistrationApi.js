const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function getPublicHiringDrive(registrationToken) {
  const response = await fetch(`${publicDriveUrl(registrationToken)}`)
  return readJson(response, 'This hiring drive is unavailable')
}

export async function registerForHiringDrive(registrationToken, registration) {
  const formData = new FormData()
  appendText(formData, 'firstName', registration.firstName)
  appendText(formData, 'lastName', registration.lastName)
  appendText(formData, 'email', registration.email)
  appendText(formData, 'contactNumber', registration.contactNumber)
  appendText(formData, 'notificationChannel', registration.notificationChannel)
  appendText(formData, 'notificationDestination', registration.notificationDestination)
  appendText(formData, 'advanceNoticeMinutes', registration.advanceNoticeMinutes)
  if (registration.resume) formData.append('resume', registration.resume)

  const response = await fetch(`${publicDriveUrl(registrationToken)}/registrations`, {
    method: 'POST',
    body: formData,
  })
  return readJson(response, 'Unable to complete registration')
}

function publicDriveUrl(registrationToken) {
  return `${API_BASE_URL}/api/public/hiring-drives/${encodeURIComponent(registrationToken)}`
}

function appendText(formData, name, value) {
  if (value !== undefined && value !== null && String(value).trim() !== '') {
    formData.append(name, String(value).trim())
  }
}

async function readJson(response, fallbackMessage) {
  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.message ?? fallbackMessage)
  }
  return response.json()
}
