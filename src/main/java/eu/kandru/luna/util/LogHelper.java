package eu.kandru.luna.util;

import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class for logging output.
 *
 * @author jko
 */
@UtilityClass
public class LogHelper {

    /**
     * Formats a {@link HttpServletRequest} for logging by extracting the important information.
     *
     * @param request the request.
     * @return this can be logged.
     */
    public String formatRequest(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("request from ")
          .append(request.getRemoteAddr())
          .append(" via ")
          .append(request.getMethod())
          .append(" to ")
          .append(request.getServletPath());
        return sb.toString();
    }
}
