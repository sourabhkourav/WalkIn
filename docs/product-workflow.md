# Product workflow and implementation order

This document records the agreed venue workflow. It is an internal product and engineering guide;
the main README remains focused on the WalkIn brand.

## Candidate journey

1. A candidate registers for a hiring drive and enters the first-round queue as `WAITING`.
2. A recruiter calls a configurable FIFO batch, such as the next 10, 20, or 30 eligible candidates.
3. Every candidate in that batch receives the same reporting time. Each notification is released
   according to that candidate's advance-notice preference.
4. The round progresses through `CALLED`, `INTERVIEWING`, and `AWAITING_RESULT`.
5. A recruiter must record `SELECTED` or `REJECTED` for each completed interview.
6. A selected candidate enters the next round as `WAITING`. A rejected candidate leaves all active
   queues and receives a result notification.
7. Selection in the final round changes the overall result to `FINAL_SELECTED` and creates a
   congratulations notification.

Each round has an independent queue. Round 1, Round 2, and later rounds can therefore operate at the
same time for candidates who are at different stages of the drive.

## Status model

Per-round statuses are `WAITING`, `CALLED`, `INTERVIEWING`, `AWAITING_RESULT`, `SELECTED`,
`REJECTED`, `MISSED`, and `WITHDRAWN`.

Overall candidate statuses are `ACTIVE`, `FINAL_SELECTED`, `REJECTED`, `WITHDRAWN`, and
`COMPLETED`. Per-round history remains available after the overall journey ends.

Withdrawal applies only to the current hiring drive. It cancels active queue entries and pending
call notifications but does not prevent registration for another drive. The restriction ends when
the drive closes, and a company administrator can restore an eligible candidate while it is active.

## Notification rules

- Advance-notice choices are 0, 5, 10, 15, or 20 minutes.
- Selecting 0 requires explicit confirmation that no advance call notification will be sent.
- Result and final-selection notifications are still created when the advance choice is 0.
- A batch has one reporting time; candidate preferences change notification delivery time, not the
  reporting time.
- The first implementation persists an internal notification outbox and simulates delivery. Real
  email, SMS, or WhatsApp providers will be connected later.

## Implementation order

Work through these slices one at a time:

1. **Completed — Per-round progress foundation:** add a drive-registration-to-round progress
   entity, status transition rules, database constraints, repositories, and tests.
2. **Completed — Automatic first-round entry:** put every new registration into the first
   configured round as `WAITING` in the registration transaction.
3. **Selected-candidate advancement:** put a candidate selected in one round into the next
   configured round, or finalize the candidate when there is no next round.
4. **Batch calling:** call the next configurable FIFO batch for one round with a shared reporting
   time and safe concurrent updates.
5. **Result declaration:** record selected or rejected results, require decisions after interviews,
   and finalize candidates who pass the last round.
6. **Notification outbox:** schedule call and result messages using the candidate's chosen channel
   and advance-notice value, with simulated delivery states.
7. **Candidate status page:** provide a private status link, live journey state, notification
   messages, and withdrawal with confirmation.
8. **Round operations UI:** replace the registration table with independent round tabs or columns,
   batch controls, result actions, counts, and clear responsive layouts.
9. **Concurrency and recovery:** protect parallel recruiter actions with locking/idempotency and add
   missed-candidate, restore, and correction workflows.
10. **End-to-end verification:** cover concurrent rounds, cross-company isolation, batch boundaries,
   final selection, withdrawal, and notification scheduling with PostgreSQL integration tests.

## Immediate next slice

Implement item 3 only. When a recruiter selects a candidate after `AWAITING_RESULT`, locate the next
configured round and create its `WAITING` progress entry. If the selected round is the final round,
mark the registration as finally selected instead. Keep notification delivery and UI work outside
this slice.
