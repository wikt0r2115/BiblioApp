package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import static com.wikt0r2115.library.TestData.INVALID_EMAILS;
import static com.wikt0r2115.library.TestData.MEMBER_EMAIL;
import static com.wikt0r2115.library.TestData.MEMBER_FIRST_NAME;
import static com.wikt0r2115.library.TestData.MEMBER_LAST_NAME;
import static com.wikt0r2115.library.TestData.padded;
import static com.wikt0r2115.library.TestData.sampleMember;
import static org.junit.jupiter.api.Assertions.*;

class MemberTest {
    @Test
    void create_validMember() {
        Member member = sampleMember();
        assertAll(
                () -> assertNull(member.getMemberId()),
                () -> assertEquals(MEMBER_FIRST_NAME, member.getFirstName()),
                () -> assertEquals(MEMBER_LAST_NAME, member.getLastName()),
                () -> assertEquals(MEMBER_EMAIL, member.getEmail())
        );
    }

    @Test
    void create_trims_normalize_data() {
        Member member = new Member(
                padded(MEMBER_FIRST_NAME),
                padded(MEMBER_LAST_NAME),
                padded(MEMBER_EMAIL)
        );

        assertEquals(MEMBER_FIRST_NAME, member.getFirstName());
        assertEquals(MEMBER_LAST_NAME, member.getLastName());
        assertEquals(MEMBER_EMAIL, member.getEmail());
    }

    @Test
    void create_whenFirstNameIsNullOrBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Member(null, MEMBER_LAST_NAME, MEMBER_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member("", MEMBER_LAST_NAME, MEMBER_EMAIL));
    }

    @Test
    void create_whenLastNameIsNullOrBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, null, MEMBER_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, "", MEMBER_EMAIL));
    }

    @Test
    void create_whenEmailIsNullBlankOrInvalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, ""));
        for (String invalidEmail : INVALID_EMAILS) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, invalidEmail));
        }
    }

    @Test
    void updateDetails_whenDataIsValid_replacesAndNormalizesFields() {
        Member member = sampleMember();

        member.updateDetails(padded("Kuba"), padded("Kowal"), padded("kuba@example.com"));

        assertAll(
                () -> assertEquals("Kuba", member.getFirstName()),
                () -> assertEquals("Kowal", member.getLastName()),
                () -> assertEquals("kuba@example.com", member.getEmail())
        );
    }

    @Test
    void updateDetails_whenDataIsInvalid_preservesOriginalFields() {
        Member member = sampleMember();

        assertThrows(IllegalArgumentException.class,
                () -> member.updateDetails("Kuba", "Kowal", "invalid"));

        assertAll(
                () -> assertEquals(MEMBER_FIRST_NAME, member.getFirstName()),
                () -> assertEquals(MEMBER_LAST_NAME, member.getLastName()),
                () -> assertEquals(MEMBER_EMAIL, member.getEmail())
        );
    }

    @Test
    void markArchived_setsArchivedFlag() {
        Member member = sampleMember();
        assertFalse(member.isArchived());

        member.markArchived();

        assertTrue(member.isArchived());
    }

    @Test
    void create_whenFieldExceeds255Characters_throwsIllegalArgumentException() {
        String tooLong = "x".repeat(256);
        assertThrows(IllegalArgumentException.class,
                () -> new Member(tooLong, MEMBER_LAST_NAME, MEMBER_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, tooLong, MEMBER_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, tooLong));
    }
}
