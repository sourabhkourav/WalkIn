import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import CandidateList from './CandidateList'

describe('CandidateList', () => {
  it('shows a message when there are no candidates', () => {
    render(<CandidateList candidates={[]} />)

    expect(screen.getByText('No candidates found.')).toBeInTheDocument()
  })

  it('displays candidate details', () => {
    const candidates = [
      {
        studentId: 1,
        firstName: 'Aarav',
        lastName: 'Sharma',
        email: 'aarav@example.com',
        contactNumber: '9876543210',
      },
    ]

    render(<CandidateList candidates={candidates} />)

    expect(screen.getByText('Aarav Sharma')).toBeInTheDocument()
    expect(screen.getByText('aarav@example.com')).toBeInTheDocument()
    expect(screen.getByText('9876543210')).toBeInTheDocument()
  })
})