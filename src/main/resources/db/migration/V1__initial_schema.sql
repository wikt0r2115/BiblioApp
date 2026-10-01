CREATE SEQUENCE author_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE book_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE category_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE loan_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE member_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE author (
    author_id BIGINT NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_author PRIMARY KEY (author_id)
);

CREATE TABLE category (
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (category_id),
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE member (
    member_id BIGINT NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    archived BOOLEAN NOT NULL,
    CONSTRAINT pk_member PRIMARY KEY (member_id)
);

CREATE TABLE book (
    book_id BIGINT NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    publication_year INTEGER,
    available BOOLEAN,
    archived BOOLEAN NOT NULL,
    author_id BIGINT NOT NULL,
    CONSTRAINT pk_book PRIMARY KEY (book_id),
    CONSTRAINT uq_book_isbn UNIQUE (isbn),
    CONSTRAINT ck_book_publication_year CHECK (publication_year >= 1450),
    CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (author_id)
);

CREATE TABLE book_category (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    CONSTRAINT pk_book_category PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_book_category_book FOREIGN KEY (book_id) REFERENCES book (book_id),
    CONSTRAINT fk_book_category_category FOREIGN KEY (category_id) REFERENCES category (category_id)
);

CREATE TABLE loan (
    loan_id BIGINT NOT NULL,
    book_book_id BIGINT NOT NULL,
    member_member_id BIGINT NOT NULL,
    borrowed_at TIMESTAMP(6),
    returned_at TIMESTAMP(6),
    CONSTRAINT pk_loan PRIMARY KEY (loan_id),
    CONSTRAINT fk_loan_book FOREIGN KEY (book_book_id) REFERENCES book (book_id),
    CONSTRAINT fk_loan_member FOREIGN KEY (member_member_id) REFERENCES member (member_id)
);
