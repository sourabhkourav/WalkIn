import { afterEach, describe, expect, it, vi } from 'vitest'
import { createStudent } from './studentApi'

describe('studentApi', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('registers a candidate with bearer authentication', async () => {
    const candidate = {
      firstName: 'Aarav',
      lastName: 'Sharma',
      email: 'aarav@example.com',
      contactNumber: '9876543210',
    }
    const savedCandidate = { studentId: 1, ...candidate }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(savedCandidate),
    }))

    await expect(createStudent('test-token', candidate)).resolves.toEqual(savedCandidate)
    expect(fetch).toHaveBeenCalledWith('/api/students', {
      method: 'POST',
      headers: {
        Authorization: 'Bearer test-token',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(candidate),
    })
  })

  it('uses the backend error message when registration fails', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: 'Email already exists' }),
    }))

    await expect(createStudent('test-token', {})).rejects.toThrow('Email already exists')
  })
})
