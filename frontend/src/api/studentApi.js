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