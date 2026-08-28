import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CandidateForm from './CandidateForm'

describe('CandidateForm', () => {
  it('submits trimmed candidate details', async () => {
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
    })
  })

  it('disables submission while registration is in progress', () => {
    render(<CandidateForm isSubmitting onSubmit={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Registering…' })).toBeDisabled()
  })
})
