import { describe, expect, it } from 'vitest'
import { readTokenIdentity } from './tokenIdentity'

describe('readTokenIdentity', () => {
  it('reads the signed tenant identity used to select the company workspace', () => {
    const token = jwt({
      sub: 'venue.admin',
      roles: 'ROLE_COMPANY_ADMIN',
      companyId: 17,
    })

    expect(readTokenIdentity(token)).toEqual({
      username: 'venue.admin',
      roles: ['ROLE_COMPANY_ADMIN'],
      companyId: 17,
    })
  })

  it('rejects a malformed token payload', () => {
    expect(readTokenIdentity('not-a-jwt')).toBeNull()
  })
})

function jwt(payload) {
  return `header.${btoa(JSON.stringify(payload))}.signature`
}
