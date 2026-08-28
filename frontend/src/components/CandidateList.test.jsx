import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CandidateList from './CandidateList'

const candidate = {
  studentId: 1,
  firstName: 'Aarav',
  lastName: 'Sharma',
  email: 'aarav@example.com',
  contactNumber: '9876543210',
  notificationChannel: 'SMS',
  advanceNoticeMinutes: 30,
}

describe('CandidateList', () => {
  it('shows a message when there are no candidates', () => {
    render(<CandidateList candidates={[]} />)
    expect(screen.getByText('No candidates found.')).toBeInTheDocument()
  })

  it('displays candidate details', () => {
    render(<CandidateList candidates={[candidate]} />)
    expect(screen.getByText('Aarav Sharma')).toBeInTheDocument()
    expect(screen.getByText('aarav@example.com')).toBeInTheDocument()
    expect(screen.getByText('9876543210')).toBeInTheDocument()
    expect(screen.getByText('SMS · 30 minutes notice')).toBeInTheDocument()
  })

  it('requests deletion for the selected candidate', async () => {
    const user = userEvent.setup()
    const onDelete = vi.fn()
    render(<CandidateList candidates={[candidate]} onDelete={onDelete} />)
    await user.click(screen.getByRole('button', { name: 'Delete' }))
    expect(onDelete).toHaveBeenCalledWith(candidate)
  })

  it('requests editing for the selected candidate', async () => {
    const user = userEvent.setup()
    const onEdit = vi.fn()
    render(<CandidateList candidates={[candidate]} onEdit={onEdit} />)
    await user.click(screen.getByRole('button', { name: 'Edit' }))
    expect(onEdit).toHaveBeenCalledWith(candidate)
  })
})
