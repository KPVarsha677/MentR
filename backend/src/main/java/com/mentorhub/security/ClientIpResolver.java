package com.mentorhub.security;

/**
 * ClientIpResolver - determines the real client IP address behind Render's
 * reverse proxy, for the registration rate limiter (see AuthService).
 *
 * WHY NOT JUST TRUST THE X-Forwarded-For HEADER DIRECTLY:
 * A client can put anything it wants in X-Forwarded-For before its request
 * ever reaches Render's proxy — e.g. sending a different fake IP on every
 * request would make each one land in a different rate-limit bucket,
 * defeating the limiter entirely. What the client CANNOT spoof is the real
 * TCP connection that reaches Render's proxy: Render appends that real
 * source IP as the LAST entry of the header before forwarding the request
 * to this app. Reading the last entry (never the first, and never trusting
 * a header with no proxy in front of it) is what makes this safe.
 *
 * This assumes exactly one trusted reverse proxy sits in front of the app
 * (Render's), which matches this app's actual deployment. If a second proxy
 * were ever added in front of Render, this would need to read the
 * second-to-last entry instead.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    /**
     * @param forwardedForHeader the raw "X-Forwarded-For" header value, or null if absent
     * @param remoteAddr         the direct TCP peer address (e.g. HttpServletRequest#getRemoteAddr())
     * @return the best-known real client IP
     */
    public static String resolve(String forwardedForHeader, String remoteAddr) {
        if (forwardedForHeader != null && !forwardedForHeader.isBlank()) {
            String[] hops = forwardedForHeader.split(",");
            String lastHop = hops[hops.length - 1].trim();
            if (!lastHop.isEmpty()) {
                return lastHop;
            }
        }
        return remoteAddr;
    }
}
