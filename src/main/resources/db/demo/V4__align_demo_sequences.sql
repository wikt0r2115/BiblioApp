-- Hibernate's pooled allocator uses the database value as the upper bound
-- of a block of 50 IDs. V2's value 101 overlaps the seeded rows 1..100.
-- Keep the next block above both existing rows and any earlier allocation.
SELECT setval('author_seq', GREATEST(
    (SELECT COALESCE(MAX(author_id), 0) + 50 FROM author),
    (SELECT last_value + 50 FROM author_seq)), false);
SELECT setval('category_seq', GREATEST(
    (SELECT COALESCE(MAX(category_id), 0) + 50 FROM category),
    (SELECT last_value + 50 FROM category_seq)), false);
SELECT setval('member_seq', GREATEST(
    (SELECT COALESCE(MAX(member_id), 0) + 50 FROM member),
    (SELECT last_value + 50 FROM member_seq)), false);
SELECT setval('book_seq', GREATEST(
    (SELECT COALESCE(MAX(book_id), 0) + 50 FROM book),
    (SELECT last_value + 50 FROM book_seq)), false);
SELECT setval('loan_seq', GREATEST(
    (SELECT COALESCE(MAX(loan_id), 0) + 50 FROM loan),
    (SELECT last_value + 50 FROM loan_seq)), false);
