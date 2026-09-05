import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  getCandidateQueue,
  getCandidateQueueSummary,
  getCandidateResume,
  updateCandidateStatus,
} from './candidateQueueApi'

describe('candidateQueueApi', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('loads a filtered drive queue with bearer authentication', async () => {
    const page = { content: [], page: 1, totalElements: 0, totalPages: 0 }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(page),
    }))

    await expect(getCandidateQueue('test-token', 12, {
      page: 1,
      status: 'WAITING',
      query: 'Asha Sharma',
    })).resolves.toEqual(page)

    expect(fetch).toHaveBeenCalledWith(
      '/api/hiring-drives/12/registrations?page=1&size=20&sort=registeredAt&direction=asc&status=WAITING&query=Asha+Sharma',
      { headers: { Authorization: 'Bearer test-token' } },
    )
  })

  it('loads summary counts', async () => {
    const summary = { waiting: 4, called: 2, completed: 1, withdrawn: 0, total: 7 }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(summary),
    }))

    await expect(getCandidateQueueSummary('test-token', 12)).resolves.toEqual(summary)
    expect(fetch).toHaveBeenCalledWith('/api/hiring-drives/12/registrations/summary', {
      headers: { Authorization: 'Bearer test-token' },
    })
  })

  it('updates status using JSON and bearer authentication', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ status: 'CALLED' }),
    }))

    await updateCandidateStatus('test-token', 12, 'candidate-reference', 'CALLED')

    expect(fetch).toHaveBeenCalledWith(
      '/api/hiring-drives/12/registrations/candidate-reference/status',
      {
        method: 'PATCH',
        headers: {
          Authorization: 'Bearer test-token',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: 'CALLED' }),
      },
    )
  })

  it('downloads resume content without placing the token in the URL', async () => {
    const resume = new Blob(['resume'], { type: 'application/pdf' })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      blob: vi.fn().mockResolvedValue(resume),
    }))

    await expect(getCandidateResume(
      'secret-token', 12, 'candidate-reference',
    )).resolves.toBe(resume)
    expect(fetch).toHaveBeenCalledWith(
      '/api/hiring-drives/12/registrations/candidate-reference/resume',
      { headers: { Authorization: 'Bearer secret-token' } },
    )
  })

  it('uses the backend error message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: 'Reload and try again' }),
    }))

    await expect(updateCandidateStatus(
      'test-token', 12, 'candidate-reference', 'CALLED',
    )).rejects.toThrow('Reload and try again')
  })
})
