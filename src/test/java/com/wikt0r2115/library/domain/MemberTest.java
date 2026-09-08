package com.wikt0r2115.library.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wikt0r2115.library.TestData.MEMBER_EMAIL;
import static com.wikt0r2115.library.TestData.MEMBER_FIRST_NAME;
import static com.wikt0r2115.library.TestData.MEMBER_LAST_NAME;
import static com.wikt0r2115.library.TestData.padded;
import static com.wikt0r2115.library.TestData.sampleMember;
import static org.junit.jupiter.api.Assertions.*;

public class MemberTest {
    private final List<String> INVALID_EMAIL = new ArrayList<>(List.of(
            "test",
            "test@",
            "test@test"
    ));


    @Test
    void create_validMember(){
        Member member = sampleMember();
        assertAll(
                () -> assertNull(member.getMemberId()),
                () -> assertEquals(MEMBER_FIRST_NAME, member.getFirstName()),
                () -> assertEquals(MEMBER_LAST_NAME, member.getLastName()),
                () -> assertEquals(MEMBER_EMAIL, member.getEmail())
        );
    }

    @Test
    void create_trims_normalize_data(){
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
    void create_whenFirstNameIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Member(null, MEMBER_LAST_NAME, MEMBER_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member("", MEMBER_LAST_NAME, MEMBER_EMAIL));
    }

    @Test
    void create_whenLastNameIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, null, MEMBER_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, "", MEMBER_EMAIL));
    }

    @Test
    void create_whenEmailIsNullBlankOrInvalid_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, INVALID_EMAIL.get(0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, INVALID_EMAIL.get(1)));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, INVALID_EMAIL.get(2)));
    }
}
