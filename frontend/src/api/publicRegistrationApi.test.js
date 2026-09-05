import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  getPublicHiringDrive,
  registerForHiringDrive,
} from './publicRegistrationApi'

describe('publicRegistrationApi', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('resolves a public drive using an encoded token', async () => {
    const drive = { companyName: 'Acme', driveName: 'Engineering Walk-in' }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(drive),
    }))

    await expect(getPublicHiringDrive('token/value')).resolves.toEqual(drive)
    expect(fetch).toHaveBeenCalledWith('/api/public/hiring-drives/token%2Fvalue')
  })

  it('submits only populated fields as multipart form data', async () => {
    const confirmation = { registrationReference: 'reference', status: 'WAITING' }
    const resume = new File(['%PDF-resume'], 'resume.pdf', { type: 'application/pdf' })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(confirmation),
    }))

    await registerForHiringDrive('public-token', {
      firstName: '  Asha ',
      lastName: '',
      email: null,
      contactNumber: '9876543210',
      resume,
      notificationChannel: 'SMS',
      notificationDestination: '9876543211',
      advanceNoticeMinutes: 30,
    })

    const [url, options] = fetch.mock.calls[0]
    expect(url).toBe('/api/public/hiring-drives/public-token/registrations')
    expect(options.method).toBe('POST')
    expect(options.headers).toBeUndefined()
    expect(options.body).toBeInstanceOf(FormData)
    expect(options.body.get('firstName')).toBe('Asha')
    expect(options.body.has('lastName')).toBe(false)
    expect(options.body.has('email')).toBe(false)
    expect(options.body.get('resume')).toBe(resume)
    expect(options.body.get('notificationChannel')).toBe('SMS')
    expect(options.body.get('advanceNoticeMinutes')).toBe('30')
  })

  it('returns the backend validation message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: 'Candidate is already registered' }),
    }))

    await expect(registerForHiringDrive('public-token', {}))
      .rejects.toThrow('Candidate is already registered')
  })
})
