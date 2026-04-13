# Room Migration Plan (V1 Baseline)

## Baseline (Version 1)

Version 1 now includes:
- chat/call core tables,
- translation events,
- translation lab sessions,
- learning tracks/modules/progress,
- retention and backup metadata.

## Planned next migrations

### V1 -> V2
- Add learning assessment tables (quiz, attempts, rubric).
- Add certification milestones and deadlines.

### V2 -> V3
- Add group call stream chunks for strict FIFO mapping.
- Add per-participant call translation preference history.

### V3 -> V4
- Add archived media flags and lifecycle timestamps.
- Add low-memory downgrade telemetry events.

## Safety rules
- Explicit SQL migrations only.
- All non-null added columns must have default values.
- Backfill in transaction.
- Maintain schema snapshots for migration tests.
