package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.persistence.ConsentRequestExportProjection;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.exceptions.ExportCapacityExhaustedException;
import ch.agridata.common.exceptions.ExportLimitExceededException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

/**
 * Exports the active consent requests of a data request as CSV. The format targets Excel in Swiss, German, French and Italian
 * locales: semicolon-separated, UTF-8 with BOM and locale-neutral {@code yyyy-MM-dd HH:mm:ss} timestamps in local Swiss time. UID
 * and BUR are written unescaped, as their fixed formats consist of letters and digits only; any free-text column added later
 * needs quoting and protection against formula injection. The query result and the CSV are fully loaded into memory, so exports
 * above {@value #MAX_ROWS} rows are rejected and at most {@value #MAX_CONCURRENT_EXPORTS} exports run concurrently per instance;
 * further exports are rejected immediately to protect the heap. The permit is held until the CSV is rendered, after which only the
 * much smaller CSV bytes remain in memory. The row limit also keeps the CSV below the 10 MB response limit of the API gateway.
 * Only active data requests can be exported. Drafts are not visible to admins and are therefore reported as not found to them.
 *
 * @CommentLastReviewed 2026-10-13
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ConsentRequestExportService {

  private static final String BOM = "\uFEFF";
  private static final String DELIMITER = ";";
  private static final String LINE_SEPARATOR = "\r\n";
  private static final String HEADER = String.join(DELIMITER, "uid", "bur", "requestDate", "lastChangeDate", "stateCode");
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final int MAX_CONCURRENT_EXPORTS = 2;
  private static final int RETRY_AFTER_SECONDS = 10;
  private static final int MAX_ROWS = 100_000;

  private final ConsentRequestRepository consentRequestRepository;
  private final DataRequestRepository dataRequestRepository;
  private final AgridataSecurityIdentity identity;
  private final Clock clock;
  private final Semaphore exportPermits = new Semaphore(MAX_CONCURRENT_EXPORTS);

  /**
   * CSV export ready to be written to the response, together with the file name to offer for download.
   *
   * @CommentLastReviewed 2026-10-12
   */
  public record CsvExport(String fileName, byte[] content) {

    @Override
    public boolean equals(Object o) {
      return o instanceof CsvExport(String name, byte[] content1)
          && Objects.equals(fileName, name)
          && Arrays.equals(content, content1);
    }

    @Override
    public int hashCode() {
      return Objects.hash(fileName, Arrays.hashCode(content));
    }

    @Override
    public @NonNull String toString() {
      return "CsvExport[fileName=%s, contentLength=%d]".formatted(fileName, content == null ? 0 : content.length);
    }
  }

  @RolesAllowed(CONSUMER_ROLE)
  public CsvExport exportConsentRequestsOfActiveDataRequestOfCurrentConsumerAsCsv(UUID dataRequestId) {
    var dataRequest = dataRequestRepository.findByIdAndDataConsumerUid(dataRequestId, identity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(dataRequestId.toString()));
    return exportAsCsv(dataRequest);
  }

  @RolesAllowed(ADMIN_ROLE)
  public CsvExport exportConsentRequestsOfActiveDataRequestAsCsv(UUID dataRequestId) {
    var dataRequest = dataRequestRepository.findByIdAndStateCodeNotDraft(dataRequestId)
        .orElseThrow(() -> new NotFoundException(dataRequestId.toString()));
    return exportAsCsv(dataRequest);
  }

  private CsvExport exportAsCsv(DataRequestEntity dataRequest) {
    if (dataRequest.getStateCode() != DataRequestStateEnum.ACTIVE) {
      throw new IllegalStateException("Data request with id " + dataRequest.getId() + " is not in status active");
    }
    var startedAt = System.nanoTime();
    var rowCount = consentRequestRepository.countActiveByDataRequestId(dataRequest.getId());
    if (rowCount > MAX_ROWS) {
      throw new ExportLimitExceededException(rowCount, MAX_ROWS);
    }
    if (!exportPermits.tryAcquire()) {
      throw new ExportCapacityExhaustedException(RETRY_AFTER_SECONDS);
    }
    try {
      var consentRequests = consentRequestRepository.findActiveExportProjectionsByDataRequestId(dataRequest.getId());
      var content = toCsv(consentRequests);
      log.info("Exported {} consent requests of data request {} in {} ms", consentRequests.size(), dataRequest.getId(),
          (System.nanoTime() - startedAt) / 1_000_000);
      return new CsvExport(fileName(dataRequest), content);
    } finally {
      exportPermits.release();
    }
  }

  private byte[] toCsv(List<ConsentRequestExportProjection> consentRequests) {
    var output = new ByteArrayOutputStream();
    try (var writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
      writer.write(BOM + HEADER + LINE_SEPARATOR);
      for (var consentRequest : consentRequests) {
        writer.write(toCsvRow(consentRequest));
        writer.write(LINE_SEPARATOR);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return output.toByteArray();
  }

  private String fileName(DataRequestEntity dataRequest) {
    return String.format("consent-requests-%s-%s.csv", dataRequest.getHumanFriendlyId(), LocalDate.now(clock));
  }

  private String toCsvRow(ConsentRequestExportProjection consentRequest) {
    return String.join(DELIMITER,
        consentRequest.dataProducerUid(),
        Objects.toString(consentRequest.dataProducerBur(), ""),
        format(consentRequest.requestDate()),
        format(consentRequest.lastStateChangeDate()),
        consentRequest.stateCode().name()
    );
  }

  private static String format(LocalDateTime dateTime) {
    return dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime);
  }
}
