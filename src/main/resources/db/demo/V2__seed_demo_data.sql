-- Demo-only migration. It runs with the "demo" profile after V1 creates the schema.
INSERT INTO author (author_id, first_name, last_name)
SELECT n, 'Autor', 'Demo ' || lpad(n::text, 3, '0')
FROM generate_series(1, 100) AS n;

INSERT INTO category (category_id, name)
SELECT n, 'Kategoria ' || lpad(n::text, 3, '0')
FROM generate_series(1, 100) AS n;

INSERT INTO member (member_id, first_name, last_name, email, archived)
SELECT n, 'Czytelnik', 'Demo ' || lpad(n::text, 3, '0'),
       'czytelnik' || lpad(n::text, 3, '0') || '@example.test', false
FROM generate_series(1, 100) AS n;

-- The first 12 digits are 978000000NNN. Calculate the ISBN-13 check digit.
INSERT INTO book (book_id, isbn, title, publication_year, available, archived, author_id)
SELECT n,
       '978000000' || lpad(n::text, 3, '0') ||
         ((10 - ((38 + 3 * (n / 100) + ((n / 10) % 10) + 3 * (n % 10)) % 10)) % 10)::text,
       'Książka demonstracyjna ' || lpad(n::text, 3, '0'),
       2000 + (n % 26), n <= 80, false, n
FROM generate_series(1, 100) AS n;

INSERT INTO book_category (book_id, category_id)
SELECT n, n
FROM generate_series(1, 100) AS n;

-- The last 20 loans remain active, matching the available flag on their books.
INSERT INTO loan (loan_id, book_book_id, member_member_id, borrowed_at, returned_at)
SELECT n, n, n,
       CURRENT_TIMESTAMP - n * INTERVAL '1 day',
       CASE WHEN n <= 80 THEN CURRENT_TIMESTAMP - n * INTERVAL '1 day' + INTERVAL '12 hours' END
FROM generate_series(1, 100) AS n;

-- Hibernate allocates IDs in groups of 50, so the next free block starts at 101.
SELECT setval('author_seq', 101, false);
SELECT setval('category_seq', 101, false);
SELECT setval('member_seq', 101, false);
SELECT setval('book_seq', 101, false);
SELECT setval('loan_seq', 101, false);
