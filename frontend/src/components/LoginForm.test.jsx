import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from '../App'

describe('LoginForm', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('stores a successful session and shows the authenticated view', async () => {
    const user = userEvent.setup()
    const session = {
      accessToken: 'test-token',
      tokenType: 'Bearer',
      expiresAt: new Date(Date.now() + 60_000).toISOString(),
    }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(session),
    }))

    render(<App />)
    await user.type(screen.getByLabelText('Username'), 'walkin-admin')
    await user.type(screen.getByLabelText('Password'), 'secret-password')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('heading', { name: 'Welcome back' })).toBeInTheDocument()
    expect(JSON.parse(sessionStorage.getItem('walkin.auth'))).toEqual(session)
    expect(fetch).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({ method: 'POST' }))
  })

  it('shows the API error when credentials are rejected', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: 'Invalid username or password' }),
    }))

    render(<App />)
    await user.type(screen.getByLabelText('Username'), 'unknown')
    await user.type(screen.getByLabelText('Password'), 'wrong-password')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid username or password')
    expect(sessionStorage.getItem('walkin.auth')).toBeNull()
  })
})
