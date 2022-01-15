package com.adtsw.jcommons.utils;

import com.adtsw.jcommons.models.EncodingFormat;
import org.junit.Assert;
import org.junit.Test;

public class EncoderUtilTest {

    private final String GZIP_ENCODED_STRING = "H4sIAAAAAAAAACsuKcrMSwcAqbK+ngYAAAA=";
    private final String BASIC_ENCODED_STRING = "string";
    private final String DECODED_STRING = "string";

    @Test
    public void testEncodeGZipSuccess() {
        String encodedString = EncoderUtil.encode(EncodingFormat.GZIP_WITH_BASE64, DECODED_STRING);
        Assert.assertEquals(GZIP_ENCODED_STRING, encodedString);
    }

    @Test
    public void testEncodeStringSuccess() {
        String encodedString = EncoderUtil.encode(EncodingFormat.STRING, DECODED_STRING);
        Assert.assertEquals(BASIC_ENCODED_STRING, encodedString);
    }

    @Test
    public void testDecodeGZipSuccess() {
        String decodedString = EncoderUtil.decode(EncodingFormat.GZIP_WITH_BASE64, GZIP_ENCODED_STRING);
        Assert.assertEquals(DECODED_STRING, decodedString);
    }

    @Test
    public void testDecodeSuccess() {
        String decodedString = EncoderUtil.decode(EncodingFormat.STRING, BASIC_ENCODED_STRING);
        Assert.assertEquals(DECODED_STRING, decodedString);
    }
}
