const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function getCandidateQueue(accessToken, driveId, filters = {}) {
  const parameters = new URLSearchParams({
    page: String(filters.page ?? 0),
    size: String(filters.size ?? 20),
    sort: 'registeredAt',
    direction: 'asc',
  })
  if (filters.status) parameters.set('status', filters.status)
  if (filters.query) parameters.set('query', filters.query)

  const response = await fetch(
    `${queueUrl(driveId)}?${parameters}`,
    { headers: authorizationHeaders(accessToken) },
  )
  return readJson(response, 'Unable to load the candidate queue')
}

export async function getCandidateQueueSummary(accessToken, driveId) {
  const response = await fetch(`${queueUrl(driveId)}/summary`, {
    headers: authorizationHeaders(accessToken),
  })
  return readJson(response, 'Unable to load the queue summary')
}

export async function updateCandidateStatus(accessToken, driveId, reference, status) {
  const response = await fetch(`${queueUrl(driveId)}/${reference}/status`, {
    method: 'PATCH',
    headers: {
      ...authorizationHeaders(accessToken),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ status }),
  })
  return readJson(response, 'Unable to update the candidate status')
}

export async function getCandidateResume(accessToken, driveId, reference) {
  const response = await fetch(`${queueUrl(driveId)}/${reference}/resume`, {
    headers: authorizationHeaders(accessToken),
  })
  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.message ?? 'Unable to download the candidate resume')
  }
  return response.blob()
}

function queueUrl(driveId) {
  return `${API_BASE_URL}/api/hiring-drives/${encodeURIComponent(driveId)}/registrations`
}

function authorizationHeaders(accessToken) {
  return { Authorization: `Bearer ${accessToken}` }
}

async function readJson(response, fallbackMessage) {
  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.message ?? fallbackMessage)
  }
  return response.json()
}
