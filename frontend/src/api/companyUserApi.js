const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export function getCompanyUsers(accessToken) {
  return request('/api/company-users?page=0&size=100', accessToken)
}

export function createRecruiter(accessToken, recruiter) {
  return request('/api/company-users/recruiters', accessToken, {
    method: 'POST', body: JSON.stringify(recruiter),
  })
}

export function updateRecruiterStatus(accessToken, userId, enabled) {
  return request(`/api/company-users/recruiters/${userId}/status`, accessToken, {
    method: 'PATCH', body: JSON.stringify({ enabled }),
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
    throw new Error(error?.message ?? 'Unable to manage company users')
  }
  return response.json()
}
