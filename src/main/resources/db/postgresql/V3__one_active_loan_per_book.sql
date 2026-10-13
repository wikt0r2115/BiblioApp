CREATE UNIQUE INDEX uq_loan_one_active_per_book
    ON loan (book_book_id)
    WHERE returned_at IS NULL;
