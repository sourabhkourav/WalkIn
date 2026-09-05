const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function getHiringDrives(accessToken) {
  const response = await fetch(
    `${API_BASE_URL}/api/hiring-drives?page=0&size=100&sort=startsAt&direction=desc`,
    { headers: authorizationHeaders(accessToken) },
  )

  return readJson(response, 'Unable to load hiring drives')
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
