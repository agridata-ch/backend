package ch.agridata.product.dto;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Simple carrier dto for downloading documents.
 *
 * @CommentLastReviewed 2026-07-09
 */

public record DocumentDownloadDto(
    String fileName,
    byte[] content
) {

  @Override
  public boolean equals(Object o) {
    return o instanceof DocumentDownloadDto(String name, byte[] content1)
        && Objects.equals(fileName, name)
        && Arrays.equals(content, content1);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fileName, Arrays.hashCode(content));
  }

  @Override
  public @NonNull String toString() {
    return "DocumentDownloadDto[fileName=%s, contentLength=%d]"
        .formatted(fileName, content == null ? 0 : content.length);
  }
}
