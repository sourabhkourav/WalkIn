const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function getStudents(accessToken) {
  const response = await fetch(
    `${API_BASE_URL}/api/students?page=0&size=20`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  )

  if (!response.ok) {
    throw new Error('Unable to load candidates')
  }

  return response.json()
}

export async function createStudent(accessToken, candidate) {
  const response = await fetch(`${API_BASE_URL}/api/students`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(candidate),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.message ?? 'Unable to register candidate')
  }

  return response.json()
}
