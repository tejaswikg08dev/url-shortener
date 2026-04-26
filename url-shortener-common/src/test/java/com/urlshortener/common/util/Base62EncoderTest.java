package com.urlshortener.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Test
    void encode_shouldReturnSevenCharacterString(){
        String result = Base62Encoder.encode(100000000L);
        assertEquals(7, result.length());
    }

    @Test
    void encode_shouldBeReversibleWithDecode(){
        long original = 123456789L;
        String encoded = Base62Encoder.encode(original);
        long decoded = Base62Encoder.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void encode_Zero_shouldReturnPaddedString(){
        String result = Base62Encoder.encode(0);
        assertEquals(7, result.length());
        assertEquals("0000000", result);
    }

    @Test
    void encode_negativeNumber_shouldThrow(){
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.encode(-1));
    }

    @Test
    void encode_differentNumbers_shouldProduceDifferentKeys(){
        String key1 = Base62Encoder.encode(100000000L);
        String key2 = Base62Encoder.encode(100000001L);
        assertNotEquals(key1, key2);
    }

    @Test
    void decode_invalidCharacter_shouldThrow(){
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode("abc!def"));
    }
}