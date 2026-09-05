export function registrationTokenFromPath(pathname) {
  const match = pathname.match(/^\/register\/([^/]+)\/?$/)
  if (!match) return null
  try {
    return decodeURIComponent(match[1])
  } catch {
    return null
  }
}
