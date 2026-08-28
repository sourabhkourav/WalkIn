import { afterEach, describe, expect, it, vi } from 'vitest'
import { createStudent, deleteStudent } from './studentApi'

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

  it('deletes a candidate with bearer authentication', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true }))

    await expect(deleteStudent('test-token', 42)).resolves.toBeUndefined()
    expect(fetch).toHaveBeenCalledWith('/api/students/42', {
      method: 'DELETE',
      headers: {
        Authorization: 'Bearer test-token',
      },
    })
  })

  it('uses the backend error message when deletion fails', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: 'Candidate has related applications' }),
    }))

    await expect(deleteStudent('test-token', 42)).rejects.toThrow('Candidate has related applications')
  })
})
