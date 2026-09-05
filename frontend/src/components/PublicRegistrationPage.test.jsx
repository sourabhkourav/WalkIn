import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PublicRegistrationPage from './PublicRegistrationPage'
import {
  getPublicHiringDrive,
  registerForHiringDrive,
} from '../api/publicRegistrationApi'

vi.mock('../api/publicRegistrationApi', () => ({
  getPublicHiringDrive: vi.fn(),
  registerForHiringDrive: vi.fn(),
}))

const drive = {
  companyName: 'Acme',
  driveName: 'Engineering Walk-in',
  venue: 'Convention Centre',
  startsAt: '2026-09-06T09:00:00Z',
  endsAt: '2026-09-06T17:00:00Z',
  registrationForm: {
    firstName: 'REQUIRED',
    lastName: 'OPTIONAL',
    email: 'HIDDEN',
    contactNumber: 'REQUIRED',
    resume: 'OPTIONAL',
  },
}

describe('PublicRegistrationPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(window, 'scrollTo').mockImplementation(() => {})
    getPublicHiringDrive.mockResolvedValue(drive)
    registerForHiringDrive.mockResolvedValue({
      registrationReference: '6593f459-76b0-44b6-bc37-4147b87c8970',
      status: 'WAITING',
      registeredAt: '2026-09-05T12:00:00Z',
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders only fields configured for the hiring drive', async () => {
    render(<PublicRegistrationPage registrationToken="public-token" />)

    expect(await screen.findByRole('heading', { name: 'Engineering Walk-in' }))
      .toBeInTheDocument()
    expect(screen.getByLabelText(/First name/)).toBeRequired()
    expect(screen.getByLabelText(/Last name/)).not.toBeRequired()
    expect(screen.queryByLabelText(/^Email/)).not.toBeInTheDocument()
    expect(screen.getByLabelText(/Contact number/)).toBeRequired()
    expect(screen.getByLabelText(/Resume/)).not.toBeRequired()
  })

  it('collects notification preferences separately and joins the queue', async () => {
    const user = userEvent.setup()
    render(<PublicRegistrationPage registrationToken="public-token" />)
    await screen.findByRole('heading', { name: 'Engineering Walk-in' })

    await user.type(screen.getByLabelText(/First name/), 'Asha')
    await user.type(screen.getByLabelText(/Last name/), 'Sharma')
    await user.type(screen.getByLabelText(/Contact number/), '9876543210')
    await user.click(screen.getByRole('button', { name: 'Continue' }))

    expect(screen.getByRole('heading', { name: 'How should we remind you?' }))
      .toBeInTheDocument()
    await user.click(screen.getByLabelText('Email'))
    await user.type(screen.getByLabelText('Notification email'), 'alerts@example.com')
    const noticeInput = screen.getByLabelText(/Notify me this many minutes/)
    await user.clear(noticeInput)
    await user.type(noticeInput, '45')
    await user.click(screen.getByRole('button', { name: 'Join candidate queue' }))

    expect(registerForHiringDrive).toHaveBeenCalledWith('public-token', {
      firstName: 'Asha',
      lastName: 'Sharma',
      email: '',
      contactNumber: '9876543210',
      resume: null,
      notificationChannel: 'EMAIL',
      notificationDestination: 'alerts@example.com',
      advanceNoticeMinutes: 45,
    })
    expect(await screen.findByRole('heading', { name: 'You’re in the queue' }))
      .toBeInTheDocument()
    expect(screen.getByText('6593f459-76b0-44b6-bc37-4147b87c8970'))
      .toBeInTheDocument()
  })

  it('shows an unavailable state for an invalid or expired link', async () => {
    getPublicHiringDrive.mockRejectedValue(new Error('Hiring drive is unavailable'))

    render(<PublicRegistrationPage registrationToken="expired-token" />)

    expect(await screen.findByRole('heading', { name: 'Registration unavailable' }))
      .toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('Hiring drive is unavailable')
  })

  it('keeps candidate input when backend submission fails', async () => {
    const user = userEvent.setup()
    registerForHiringDrive.mockRejectedValue(new Error('Candidate is already registered'))
    render(<PublicRegistrationPage registrationToken="public-token" />)
    await screen.findByRole('heading', { name: 'Engineering Walk-in' })

    await user.type(screen.getByLabelText(/First name/), 'Asha')
    await user.type(screen.getByLabelText(/Contact number/), '9876543210')
    await user.click(screen.getByRole('button', { name: 'Continue' }))
    await user.type(screen.getByLabelText(/Notification phone number/), '9876543211')
    await user.click(screen.getByRole('button', { name: 'Join candidate queue' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Candidate is already registered',
    )
    expect(screen.getByLabelText(/Notification phone number/)).toHaveValue('9876543211')
  })
})
