-- 1. PARTICIPANTS CONSTRAINTS

ALTER TABLE participants
    ADD CONSTRAINT uq_participants_group_id_id
        UNIQUE (group_id, id);

ALTER TABLE participants
    ADD CONSTRAINT fk_participants_bill_groups
        FOREIGN KEY (group_id)
            REFERENCES bill_groups(id)
            ON DELETE CASCADE;


-- 2. EXPENSES CONSTRAINTS

ALTER TABLE expenses
    ADD CONSTRAINT uq_expenses_group_id_id
        UNIQUE (group_id, id);

ALTER TABLE expenses
    ADD CONSTRAINT fk_expenses_bill_groups
        FOREIGN KEY (group_id)
            REFERENCES bill_groups(id)
            ON DELETE CASCADE;

ALTER TABLE expenses
    ADD CONSTRAINT fk_expenses_participants
        FOREIGN KEY (group_id, payer_id)
            REFERENCES participants(group_id, id)
            ON DELETE RESTRICT;

ALTER TABLE expenses
    ADD CONSTRAINT chk_expenses_amount
        CHECK (amount > 0);

ALTER TABLE expenses
    ADD CONSTRAINT chk_expenses_split_type
        CHECK (split_type IN ('EQUAL', 'EXACT', 'PERCENTAGE'));


-- 3. EXPENSE SHARES CONSTRAINTS

ALTER TABLE expense_shares
    ADD CONSTRAINT fk_expense_shares_expenses
        FOREIGN KEY (group_id, expense_id)
            REFERENCES expenses(group_id, id)
            ON DELETE CASCADE;

ALTER TABLE expense_shares
    ADD CONSTRAINT fk_expense_shares_participants
        FOREIGN KEY (group_id, participant_id)
            REFERENCES participants(group_id, id)
            ON DELETE RESTRICT;

ALTER TABLE expense_shares
    ADD CONSTRAINT uq_expense_shares_expense_participant
        UNIQUE (expense_id, participant_id);

ALTER TABLE expense_shares
    ADD CONSTRAINT chk_expense_shares_amount
        CHECK (share_amount >= 0);

ALTER TABLE expense_shares
    ADD CONSTRAINT chk_expense_shares_percentage
        CHECK (
            share_percentage IS NULL
                OR share_percentage BETWEEN 0 AND 100
            );

-- 4. INDEXES

CREATE INDEX idx_participants_group_id
    ON participants(group_id);

CREATE INDEX idx_expenses_group_id
    ON expenses(group_id);

CREATE INDEX idx_expenses_payer_id
    ON expenses(payer_id);

CREATE INDEX idx_expenses_expense_date
    ON expenses(expense_date);

CREATE INDEX idx_expense_shares_group_id
    ON expense_shares(group_id);

CREATE INDEX idx_expense_shares_expense_id
    ON expense_shares(expense_id);

CREATE INDEX idx_expense_shares_participant_id
    ON expense_shares(participant_id);
