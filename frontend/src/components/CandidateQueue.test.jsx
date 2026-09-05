import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CandidateQueue from './CandidateQueue'

const candidate = {
  registrationReference: '6593f459-76b0-44b6-bc37-4147b87c8970',
  firstName: 'Asha',
  lastName: 'Sharma',
  email: 'asha@example.com',
  contactNumber: null,
  resumeAvailable: true,
  status: 'WAITING',
  registeredAt: '2026-09-05T12:00:00Z',
}

describe('CandidateQueue', () => {
  it('shows a useful empty state', () => {
    render(<CandidateQueue candidates={[]} />)
    expect(screen.getByText('No candidates match this view')).toBeInTheDocument()
  })

  it('renders safe candidate details and available actions', () => {
    render(
      <CandidateQueue
        candidates={[candidate]}
        onDownloadResume={vi.fn()}
        onStatusChange={vi.fn()}
      />,
    )

    expect(screen.getByText('Asha Sharma')).toBeInTheDocument()
    expect(screen.getByText('asha@example.com')).toBeInTheDocument()
    expect(screen.getByText('Waiting')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Resume' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Call candidate' })).toBeInTheDocument()
  })

  it('requests an allowed status transition', async () => {
    const user = userEvent.setup()
    const onStatusChange = vi.fn()
    render(
      <CandidateQueue
        candidates={[candidate]}
        onDownloadResume={vi.fn()}
        onStatusChange={onStatusChange}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Call candidate' }))
    expect(onStatusChange).toHaveBeenCalledWith(candidate, 'CALLED')
  })
})
