import { afterEach, describe, expect, it, vi } from 'vitest'
import { getHiringDrives } from './hiringDriveApi'

describe('hiringDriveApi', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('loads recent drives with bearer authentication', async () => {
    const page = { content: [{ driveId: 12, driveName: 'Engineering Drive' }] }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(page),
    }))

    await expect(getHiringDrives('test-token')).resolves.toEqual(page)
    expect(fetch).toHaveBeenCalledWith(
      '/api/hiring-drives?page=0&size=100&sort=startsAt&direction=desc',
      { headers: { Authorization: 'Bearer test-token' } },
    )
  })
})
