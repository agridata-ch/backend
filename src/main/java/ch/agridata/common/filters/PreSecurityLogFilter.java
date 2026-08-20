package ch.agridata.common.filters;

import static ch.agridata.user.service.ImpersonationHeaderFilter.IMPERSONATION_HEADER;
import static io.quarkiverse.loggingjson.providers.KeyValueStructuredArgument.kv;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import java.util.ArrayList;
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
 * @CommentLastReviewed 2026-01-28
 */

@Slf4j
public class PreSecurityLogFilter {
  private static final String START_TIME_KEY = "request.startTime";
  private static final List<String> URIS_TO_APPLY = List.of("/api/", "/public/api/");
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
        var logArguments = new ArrayList<>(List.of(
            ctx.request().method().name(),
            uriWithQuery,
            kv("operation", "rest.request"),
            kv("method", ctx.request().method().name()),
            kv("uri", uriWithQuery),
            kv("contentType", getContentType(ctx))));

        String impersonationInfo = getImpersonationInfo(ctx);
        if (!impersonationInfo.isEmpty()) {
          logArguments.add(kv("impersonation", impersonationInfo));
        }

        log.info("rest.request {} {}", logArguments.toArray());
      }
      logResponse(ctx);
    }
    ctx.next();
  }

  private void logRequestWithBody(RoutingContext ctx) {
    ctx.request().bodyHandler(buffer -> {
      String bodyString = buffer.toString();
      String uriWithQuery = getUriWithQuery(ctx);

      var logArguments = new ArrayList<>(List.of(
          ctx.request().method().name(),
          uriWithQuery,
          kv("operation", "rest.request"),
          kv("method", ctx.request().method().name()),
          kv("uri", uriWithQuery),
          kv("contentType", getContentType(ctx)),
          kv("body", bodyString)));

      String impersonationInfo = getImpersonationInfo(ctx);
      if (!impersonationInfo.isEmpty()) {
        logArguments.add(kv("impersonating", impersonationInfo));
      }

      log.debug("rest.request {} {}", logArguments.toArray());
    });
  }

  private void logResponse(RoutingContext ctx) {
    ctx.addBodyEndHandler(v -> {
      int status = ctx.response().getStatusCode();
      Long startTime = ctx.get(START_TIME_KEY);
      long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;
      String uriWithQuery = getUriWithQuery(ctx);

      log.info("rest.response {} {} -> {} ({} ms)",
          ctx.request().method().name(),
          uriWithQuery,
          status,
          duration,
          kv("operation", "rest.response"),
          kv("method", ctx.request().method().name()),
          kv("uri", uriWithQuery),
          kv("status", status),
          kv("duration", duration));
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
        .anyMatch(ct::startsWith);
  }

  private String getImpersonationInfo(RoutingContext ctx) {
    var header = ctx.request().getHeader(IMPERSONATION_HEADER);
    if (header == null) {
      return "";
    }
    return header;
  }

}
