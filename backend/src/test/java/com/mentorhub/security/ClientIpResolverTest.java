package com.mentorhub.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientIpResolverTest {

    @Test
    void noForwardedForHeader_fallsBackToRemoteAddr() {
        assertEquals("10.0.0.5", ClientIpResolver.resolve(null, "10.0.0.5"));
    }

    @Test
    void blankForwardedForHeader_fallsBackToRemoteAddr() {
        assertEquals("10.0.0.5", ClientIpResolver.resolve("   ", "10.0.0.5"));
    }

    @Test
    void singleIpInHeader_isUsed() {
        assertEquals("203.0.113.7", ClientIpResolver.resolve("203.0.113.7", "10.0.0.5"));
    }

    @Test
    void multipleHopsInHeader_usesLastHopNotFirst() {
        // A client can freely set this header to anything before its request
        // reaches Render's proxy — e.g. prepending a fake IP. Render appends
        // the real, unspoofable source IP as the LAST entry, so that's the
        // one that must be trusted, never the first.
        String spoofedThenReal = "1.2.3.4, 203.0.113.9";
        assertEquals("203.0.113.9", ClientIpResolver.resolve(spoofedThenReal, "10.0.0.5"));
    }

    @Test
    void multipleHopsWithExtraWhitespace_isTrimmed() {
        assertEquals("203.0.113.9", ClientIpResolver.resolve("1.2.3.4 ,  203.0.113.9  ", "10.0.0.5"));
    }

    @Test
    void trailingCommaWithNoFinalHop_fallsBackToRemoteAddr() {
        assertEquals("10.0.0.5", ClientIpResolver.resolve("1.2.3.4, ", "10.0.0.5"));
    }
}
