package de.adorsys.psd2.validator.certificate.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import java.util.Arrays;

public class URLEncodingUtil {
    public static boolean isURLEncoded(final byte[] encodedCert) {
        try {
            final byte[] encodedCertModified = new String(encodedCert).replaceAll("\\+", "%2B").getBytes();
            byte[] decoded = URLCodec.decodeUrl(encodedCertModified);

            if (Arrays.equals(decoded, encodedCert)) {
                return false;
            }
        } catch (DecoderException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static byte[] encode(final byte[] cert) {
        return URLCodec.encodeUrl(null, cert);
    }

    public static byte[] decode(final byte[] encodedCert) {
        try {
            return URLCodec.decodeUrl(encodedCert);
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return null;
    }

}
