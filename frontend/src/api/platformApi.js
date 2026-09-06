const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export function getCompanies(accessToken) {
  return request('/api/companies?page=0&size=100&sort=companyName&direction=asc', accessToken)
}

export function createCompany(accessToken, company) {
  return request('/api/companies', accessToken, {
    method: 'POST',
    body: JSON.stringify(company),
  })
}

export function createCompanyAdmin(accessToken, companyId, user) {
  return request('/api/users', accessToken, {
    method: 'POST',
    body: JSON.stringify({ ...user, role: 'COMPANY_ADMIN', companyId }),
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
    throw new Error(error?.message ?? 'Platform request failed')
  }
  return response.json()
}
