import { describe, expect, it } from 'vitest'
import { registrationTokenFromPath } from './utils/registrationRoute'

describe('registrationTokenFromPath', () => {
  it('extracts and decodes a token only from a registration route', () => {
    expect(registrationTokenFromPath('/register/public%2Dtoken')).toBe('public-token')
    expect(registrationTokenFromPath('/register/public-token/')).toBe('public-token')
    expect(registrationTokenFromPath('/')).toBeNull()
    expect(registrationTokenFromPath('/register/')).toBeNull()
    expect(registrationTokenFromPath('/register/token/extra')).toBeNull()
  })
})
