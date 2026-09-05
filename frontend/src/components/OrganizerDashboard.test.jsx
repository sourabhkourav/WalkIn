import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import OrganizerDashboard from './OrganizerDashboard'
import {
  getCandidateQueue,
  getCandidateQueueSummary,
  updateCandidateStatus,
} from '../api/candidateQueueApi'
import { getHiringDrives } from '../api/hiringDriveApi'

vi.mock('../api/hiringDriveApi', () => ({
  getHiringDrives: vi.fn(),
}))

vi.mock('../api/candidateQueueApi', () => ({
  getCandidateQueue: vi.fn(),
  getCandidateQueueSummary: vi.fn(),
  getCandidateResume: vi.fn(),
  updateCandidateStatus: vi.fn(),
}))

const drive = {
  driveId: 12,
  driveName: 'Engineering Walk-in',
  venue: 'Convention Centre',
  status: 'OPEN',
}

const candidate = {
  registrationReference: '6593f459-76b0-44b6-bc37-4147b87c8970',
  firstName: 'Asha',
  lastName: 'Sharma',
  email: 'asha@example.com',
  contactNumber: null,
  notificationDestination: 'private-alerts@example.com',
  resumeAvailable: false,
  status: 'WAITING',
  registeredAt: '2026-09-05T12:00:00Z',
}

const queuePage = {
  content: [candidate],
  page: 0,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
}

const summary = {
  waiting: 1,
  called: 0,
  completed: 0,
  withdrawn: 0,
  total: 1,
}

describe('OrganizerDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getHiringDrives.mockResolvedValue({ content: [drive] })
    getCandidateQueue.mockResolvedValue(queuePage)
    getCandidateQueueSummary.mockResolvedValue(summary)
    updateCandidateStatus.mockResolvedValue({ ...candidate, status: 'CALLED' })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('loads the selected drive, summary, and privacy-safe queue', async () => {
    render(<OrganizerDashboard accessToken="test-token" onLogout={vi.fn()} />)

    expect(await screen.findByText('Asha Sharma')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Candidate queue' })).toBeInTheDocument()
    expect(screen.getAllByText('Engineering Walk-in')).toHaveLength(2)
    expect(screen.queryByText('private-alerts@example.com')).not.toBeInTheDocument()
    expect(getCandidateQueue).toHaveBeenCalledWith('test-token', '12', {
      status: '',
      query: '',
      page: 0,
    })
    expect(getCandidateQueueSummary).toHaveBeenCalledWith('test-token', '12')
  })

  it('submits trimmed search text to the queue API', async () => {
    const user = userEvent.setup()
    render(<OrganizerDashboard accessToken="test-token" onLogout={vi.fn()} />)
    await screen.findByText('Asha Sharma')

    await user.type(screen.getByRole('searchbox'), '  Asha  ')
    await user.click(screen.getByRole('button', { name: 'Search' }))

    await waitFor(() => {
      expect(getCandidateQueue).toHaveBeenLastCalledWith('test-token', '12', {
        status: '',
        query: 'Asha',
        page: 0,
      })
    })
  })

  it('updates a candidate and refreshes queue data', async () => {
    const user = userEvent.setup()
    render(<OrganizerDashboard accessToken="test-token" onLogout={vi.fn()} />)
    await screen.findByText('Asha Sharma')

    await user.click(screen.getByRole('button', { name: 'Call candidate' }))

    expect(updateCandidateStatus).toHaveBeenCalledWith(
      'test-token',
      '12',
      candidate.registrationReference,
      'CALLED',
    )
    expect(await screen.findByText('Asha Sharma moved to Called.')).toBeInTheDocument()
    await waitFor(() => expect(getCandidateQueue).toHaveBeenCalledTimes(2))
    expect(getCandidateQueueSummary).toHaveBeenCalledTimes(2)
  })

  it('signs out only when the operator requests it', async () => {
    const user = userEvent.setup()
    const onLogout = vi.fn()
    render(<OrganizerDashboard accessToken="test-token" onLogout={onLogout} />)

    await user.click(screen.getByRole('button', { name: 'Sign out' }))
    expect(onLogout).toHaveBeenCalledOnce()
  })

  it('requires confirmation before a terminal status change', async () => {
    const user = userEvent.setup()
    vi.spyOn(window, 'confirm').mockReturnValue(false)
    render(<OrganizerDashboard accessToken="test-token" onLogout={vi.fn()} />)
    await screen.findByText('Asha Sharma')

    await user.click(screen.getByRole('button', { name: 'Withdraw' }))

    expect(window.confirm).toHaveBeenCalledWith(
      'Withdrawn Asha Sharma? This status is final.',
    )
    expect(updateCandidateStatus).not.toHaveBeenCalled()
  })
})
