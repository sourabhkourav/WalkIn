import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CandidateForm from './CandidateForm'

describe('CandidateForm', () => {
  it('submits trimmed candidate details with notification preferences', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn()
    render(<CandidateForm onSubmit={onSubmit} />)

    await user.type(screen.getByLabelText('First name'), '  Aarav  ')
    await user.type(screen.getByLabelText('Last name'), '  Sharma  ')
    await user.type(screen.getByLabelText('Email'), '  aarav@example.com  ')
    await user.type(screen.getByLabelText('Contact number'), '9876543210')
    await user.click(screen.getByRole('button', { name: 'Register candidate' }))

    expect(onSubmit).toHaveBeenCalledWith({
      firstName: 'Aarav',
      lastName: 'Sharma',
      email: 'aarav@example.com',
      contactNumber: '9876543210',
      notificationChannel: 'SMS',
      advanceNoticeMinutes: 30,
    })
  })

  it('disables submission while registration is in progress', () => {
    render(<CandidateForm isSubmitting onSubmit={vi.fn()} />)
    expect(screen.getByRole('button', { name: 'Saving...' })).toBeDisabled()
  })

  it('prefills candidate details for editing', () => {
    const candidate = {
      firstName: 'Aarav',
      lastName: 'Sharma',
      email: 'aarav@example.com',
      contactNumber: '9876543210',
      notificationChannel: 'WHATSAPP',
      advanceNoticeMinutes: 45,
    }
    render(<CandidateForm candidate={candidate} onCancel={vi.fn()} onSubmit={vi.fn()} />)

    expect(screen.getByRole('heading', { name: 'Edit candidate' })).toBeInTheDocument()
    expect(screen.getByLabelText('First name')).toHaveValue('Aarav')
    expect(screen.getByLabelText('Email')).toHaveValue('aarav@example.com')
    expect(screen.getByLabelText('Notification channel')).toHaveValue('WHATSAPP')
    expect(screen.getByLabelText('Advance notice in minutes')).toHaveValue(45)
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument()
  })
})
