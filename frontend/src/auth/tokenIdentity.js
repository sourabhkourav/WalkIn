export function readTokenIdentity(accessToken) {
  try {
    const payloadPart = accessToken.split('.')[1]
    const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(normalized))
    const roles = String(payload.roles ?? '').split(/\s+/).filter(Boolean)
    return {
      username: payload.sub,
      roles,
      companyId: payload.companyId == null ? null : Number(payload.companyId),
    }
  } catch {
    return null
  }
}
