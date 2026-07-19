package com.wikt0r2115.library.domain;

import com.jayway.jsonpath.internal.function.sequence.Last;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MemberTest {
    private final String FIRST_NAME = "Jack";
    private final String LAST_NAME = "Sparrow";
    private final String VALID_EMAIL = "test@test.gmail";
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
                () -> assertEquals(FIRST_NAME, member.getFirstName()),
                () -> assertEquals(LAST_NAME, member.getLastName()),
                () -> assertEquals(VALID_EMAIL, member.getEmail())
        );
    }

    @Test
    void create_trims_normalize_data(){
        Member member = new Member(
                addSpacesForString(FIRST_NAME),
                addSpacesForString(LAST_NAME),
                addSpacesForString(VALID_EMAIL)
        );

        assertEquals(FIRST_NAME, member.getFirstName());
        assertEquals(LAST_NAME, member.getLastName());
        assertEquals(VALID_EMAIL, member.getEmail());
    }

    @Test
    void create_whenFirstNameIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Member(null,LAST_NAME,VALID_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member("", LAST_NAME, VALID_EMAIL));
    }

    @Test
    void create_whenLastNameIsNullOrBlank_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Member(FIRST_NAME, null, VALID_EMAIL));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(FIRST_NAME,"", VALID_EMAIL));
    }

    @Test
    void create_whenEmailIsNullBlankOrInvalid_throwsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class,
                () -> new Member(FIRST_NAME,LAST_NAME, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(FIRST_NAME,LAST_NAME,""));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(FIRST_NAME, LAST_NAME, INVALID_EMAIL.get(0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(FIRST_NAME, LAST_NAME, INVALID_EMAIL.get(1)));
        assertThrows(IllegalArgumentException.class,
                () -> new Member(FIRST_NAME, LAST_NAME, INVALID_EMAIL.get(2)));
    }


    private String addSpacesForString(String string){
        return "     " + string + "        ";
    }


    private Member sampleMember(){
        return new Member(FIRST_NAME,LAST_NAME,VALID_EMAIL);
    }
}
