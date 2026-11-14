
CREATE TABLE expense_shares (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    expense_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    share_amount NUMERIC(19, 2) NOT NULL,
    share_percentage NUMERIC(7, 4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
