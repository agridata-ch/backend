package ch.agridata.common.filters;

import static ch.agridata.user.filters.ImpersonationHeaderFilter.IMPERSONATION_HEADER;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

/**
 * Logs incoming requests and outgoing responses with configurable filters. It avoids logging sensitive or large binary payloads while
 * ensuring visibility into API interactions.
 *
 * @CommentLastReviewed 2025-10-15
 */

@Slf4j
public class PreSecurityLogFilter {
  private static final String START_TIME_KEY = "request.startTime";
  private static final List<String> URIS_TO_APPLY = List.of("/api");
  private static final List<String> CONTENT_TYPE_LOG_BLACKLIST = List.of(
      "multipart/form-data",
      "text/event-stream",
      "application/octet-stream",
      "application/zip",
      "application/gzip",
      "application/x-gzip",
      "application/x-tar",
      "application/x-7z-compressed",
      "application/x-rar-compressed",
      "application/pdf",
      "application/msword",
      "application/vnd.",
      "image/",
      "audio/",
      "video/",
      "application/x-java-serialized-object",
      "application/x-protobuf"
  );

  private String getUriWithQuery(RoutingContext ctx) {
    String path = ctx.request().path();
    String query = ctx.request().query();
    return (query != null && !query.isEmpty()) ? path + "?" + query : path;
  }

  @RouteFilter(1500)
  void logRequests(RoutingContext ctx) {
    if (logEnabled(ctx)) {
      long startTime = System.currentTimeMillis();
      ctx.put(START_TIME_KEY, startTime);
      String uriWithQuery = getUriWithQuery(ctx);

      if (log.isEnabledForLevel(Level.DEBUG) && !contentTypeIsBinary(ctx)) {
        logRequestWithBody(ctx);
      } else {
        var logBuilder = log.atInfo()
            .addKeyValue("operation", "rest.request")
            .addKeyValue("method", ctx.request().method())
            .addKeyValue("uri", uriWithQuery)
            .addKeyValue("contentType", getContentType(ctx));

        String impersonationInfo = getImpersonationInfo(ctx);
        if (!impersonationInfo.isEmpty()) {
          logBuilder = logBuilder.addKeyValue("impersonation", impersonationInfo);
        }

        logBuilder.log("REQUEST: {} {} {} {} {}", ctx.request().method(), uriWithQuery,
            getContentType(ctx), ctx.request().query(), impersonationInfo);
      }
      logResponse(ctx);
    }
    ctx.next();
  }

  private void logRequestWithBody(RoutingContext ctx) {
    ctx.request().bodyHandler(buffer -> {
      String bodyString = buffer.toString();
      String uriWithQuery = getUriWithQuery(ctx);

      var logBuilder = log.atDebug()
          .addKeyValue("operation", "rest.request")
          .addKeyValue("method", ctx.request().method())
          .addKeyValue("uri", uriWithQuery)
          .addKeyValue("contentType", getContentType(ctx))
          .addKeyValue("body", bodyString);

      String impersonationInfo = getImpersonationInfo(ctx);
      if (!impersonationInfo.isEmpty()) {
        logBuilder = logBuilder.addKeyValue("impersonating", impersonationInfo);
      }

      logBuilder.log("REQUEST: {} {} {} {}", ctx.request().method(),
          uriWithQuery, getContentType(ctx), impersonationInfo);

      logResponse(ctx);
    });
  }

  private void logResponse(RoutingContext ctx) {
    ctx.addBodyEndHandler(v -> {
      int status = ctx.response().getStatusCode();
      Long startTime = ctx.get(START_TIME_KEY);
      long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;
      String uriWithQuery = getUriWithQuery(ctx);

      log.atInfo()
          .addKeyValue("operation", "rest.response")
          .addKeyValue("method", ctx.request().method())
          .addKeyValue("uri", uriWithQuery)
          .addKeyValue("status", status)
          .addKeyValue("duration", duration)
          .log("RESPONSE: {} {} status: {} duration: {} ms", ctx.request().method(),
              uriWithQuery, status, duration);
    });
  }

  private String getContentType(RoutingContext ctx) {

    return Optional.ofNullable(ctx.request().getHeader(HttpHeaders.CONTENT_TYPE)).orElse("");
  }

  boolean logEnabled(RoutingContext ctx) {
    if (!log.isInfoEnabled()) {
      return false;
    }
    if (!applyFilterForPath(ctx.request().path())) {
      return false;
    }
    if (HttpMethod.OPTIONS.name().equals(ctx.request().method().name())) {
      return log.isTraceEnabled();
    }
    return log.isDebugEnabled() || log.isTraceEnabled() || log.isInfoEnabled();
  }

  private boolean applyFilterForPath(String path) {
    return URIS_TO_APPLY.stream()
        .anyMatch(path::startsWith);
  }

  private boolean contentTypeIsBinary(RoutingContext ctx) {
    String contentType = getContentType(ctx);
    if (StringUtils.isBlank(contentType)) {
      return false;
    }
    String ct = contentType.toLowerCase(Locale.ROOT);
    return CONTENT_TYPE_LOG_BLACKLIST.stream()
        .anyMatch(binaryContentType -> binaryContentType.startsWith(ct));
  }

  private String getImpersonationInfo(RoutingContext ctx) {
    var header = ctx.request().getHeader(IMPERSONATION_HEADER);
    if (header == null) {
      return "";
    }
    return header;
  }

}
