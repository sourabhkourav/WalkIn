const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function getCompanyRounds(accessToken) {
  return request('/api/company-rounds?page=0&size=100', accessToken)
}

export async function createHiringDriveSetup(accessToken, setup) {
  return request('/api/hiring-drive-setups', accessToken, {
    method: 'POST',
    body: JSON.stringify(setup),
  })
}

export async function createCompanyRound(accessToken, round) {
  return request('/api/company-rounds/custom', accessToken, {
    method: 'POST',
    body: JSON.stringify(round),
  })
}

async function request(path, accessToken, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      Authorization: `Bearer ${accessToken}`,
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    },
  })
  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.message ?? 'Unable to configure hiring drive')
  }
  return response.json()
}
