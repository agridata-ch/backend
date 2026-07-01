package ch.agridata.product.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ValidationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Verifies that an upload really is a PDF.
 *
 * <p>Two independent gates:
 * <ol>
 *   <li>the declared multipart Content-Type must be {@code application/pdf}; and
 *   <li>the actual bytes must begin with the {@code %PDF-} magic prefix.
 * </ol>
 *
 * <p>This is a cheap hygiene gate, not the malware boundary - GuardDuty is. Its purpose is to
 * reject an upload that declares {@code application/pdf} but carries different bytes (or vice
 * versa) before it costs an S3 put and a scan cycle. For a PDF-only path a signature check is
 * sufficient; a content-detection library (Tika) would only re-check the same {@code %PDF-}
 * signature. If AgriData later accepts multiple formats - where magic bytes get format-specific
 * and containers overlap (a {@code .docx} is a ZIP) - reach for Tika then.
 *
 * <p>Detection reads only the first bytes from the temp file on disk, so nothing is buffered in
 * memory and no content is logged.
 *
 * @CommentLastReviewed 2026-07-10
 */

@ApplicationScoped
public class PdfValidator {

  private static final String PDF = "application/pdf";
  private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F', '-'};

  public void validate(String declaredContentType, Path file) {
    if (declaredContentType == null
        || !PDF.equalsIgnoreCase(stripParameters(declaredContentType))) {
      throw new ValidationException(
          "Declared Content-Type must be application/pdf");
    }
    if (!hasPdfMagic(file)) {
      throw new ValidationException(
          "File content is not a valid PDF (magic bytes mismatch)");
    }
  }

  private boolean hasPdfMagic(Path file) {
    try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
      byte[] head = in.readNBytes(PDF_MAGIC.length);
      if (head.length < PDF_MAGIC.length) {
        return false;
      }
      for (int i = 0; i < PDF_MAGIC.length; i++) {
        if (head[i] != PDF_MAGIC[i]) {
          return false;
        }
      }
      return true;
    } catch (IOException _) {
      throw new ValidationException("Could not read uploaded file for validation");
    }
  }

  private static String stripParameters(String contentType) {
    int semi = contentType.indexOf(';');
    return (semi >= 0 ? contentType.substring(0, semi) : contentType).trim();
  }
}
