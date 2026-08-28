const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function login(credentials) {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.message ?? 'Unable to sign in. Please try again.')
  }

  return response.json()
}
