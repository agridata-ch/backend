package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ValidationException;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Handles logo processing for data requests. It supports encoding and managing consumer branding.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestLogoService {


  private static final int MAX_INPUT_FILE_SIZE_BYTES = 100 * 1024;
  private static final int MAX_OUTPUT_FILE_SIZE_BYTES = 500 * 1024;

  private static final int MAX_INPUT_WIDTH = 4096;
  private static final int MAX_INPUT_HEIGHT = 4096;
  private static final int MAX_INPUT_PIXELS = 9_000_000;

  private static final int MAX_OUTPUT_WIDTH = 120;
  private static final int MAX_OUTPUT_HEIGHT = 120;

  private final DataRequestLogoWriter dataRequestLogoWriter;

  @RolesAllowed(CONSUMER_ROLE)
  public void updateDataRequestLogo(UUID requestId, FileUpload logo) {
    if (logo == null || logo.uploadedFile() == null) {
      throw new IllegalArgumentException("No file uploaded.");
    }

    byte[] bytes = readAllBytesBounded(logo);
    BufferedImage decodedImage = readImage(bytes);
    BufferedImage resizedImage = resizeIfNecessary(decodedImage);
    byte[] pngBytes = reEncodeToPng(resizedImage);

    if (pngBytes.length > MAX_OUTPUT_FILE_SIZE_BYTES) {
      throw new ValidationException(
          "Re-encoded image too large (max " + MAX_OUTPUT_FILE_SIZE_BYTES + " bytes, got " + pngBytes.length
              + " bytes. Try a smaller or simpler image (fewer details)");
    }

    dataRequestLogoWriter.store(requestId, pngBytes);
  }

  private static byte[] readAllBytesBounded(FileUpload logo) {

    try (InputStream in = Files.newInputStream(logo.uploadedFile())) {
      var buffer = new byte[8192];
      int read;
      int total = 0;
      try (var out = new ByteArrayOutputStream(8192)) {
        while ((read = in.read(buffer)) != -1) {
          total += read;
          if (total > MAX_INPUT_FILE_SIZE_BYTES) {
            throw new ValidationException("File too large (max " + MAX_INPUT_FILE_SIZE_BYTES + " bytes)");
          }
          out.write(buffer, 0, read);
        }
        if (total == 0) {
          throw new ValidationException("File is empty");
        }
        return out.toByteArray();
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read uploaded file", e);
    }
  }

  private static BufferedImage readImage(byte[] bytes) {
    final String sniffedType = sniffFormatFromMagicBytes(bytes);

    final BufferedImage decodedImage;
    ImageReader reader = null;

    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
         ImageInputStream imageStream = ImageIO.createImageInputStream(byteStream)) {
      if (imageStream == null) {
        throw new ValidationException("Failed to decode input image");
      }

      reader = getReaderFor(sniffedType);
      reader.setInput(imageStream, true, true);

      long width = reader.getWidth(0);
      long height = reader.getHeight(0);

      validateImageDimensions(width, height);

      decodedImage = reader.read(0);
    } catch (IOException e) {
      throw new ValidationException("Failed to decode input image", e);
    } finally {
      if (reader != null) {
        reader.dispose();
      }
    }

    if (decodedImage == null) {
      throw new ValidationException("Input image is invalid");
    } else {
      return decodedImage;
    }
  }

  private static void validateImageDimensions(long width, long height) {
    long pixels;
    try {
      pixels = Math.multiplyExact(width, height);
    } catch (ArithmeticException _) {
      throw new ValidationException("Input image dimensions too large");
    }

    if (width > MAX_INPUT_WIDTH || height > MAX_INPUT_HEIGHT || pixels > MAX_INPUT_PIXELS) {
      throw new ValidationException("Input image dimensions too large");
    }
  }

  private static String sniffFormatFromMagicBytes(byte[] bytes) {
    if (bytes.length >= 8
        && (bytes[0] == (byte) 0x89)
        && (bytes[1] == (byte) 0x50)
        && (bytes[2] == (byte) 0x4E)
        && (bytes[3] == (byte) 0x47)
        && (bytes[4] == (byte) 0x0D)
        && (bytes[5] == (byte) 0x0A)
        && (bytes[6] == (byte) 0x1A)
        && (bytes[7] == (byte) 0x0A)
    ) {
      return "png";
    }

    if (bytes.length >= 3
        && (bytes[0] == (byte) 0xFF)
        && (bytes[1] == (byte) 0xD8)
        && (bytes[2] == (byte) 0xFF)
    ) {
      return "jpeg";
    }

    throw new ValidationException("Unsupported file type");
  }

  private static ImageReader getReaderFor(String sniffedType) {
    var readers = ImageIO.getImageReadersByFormatName(sniffedType);
    if (readers.hasNext()) {
      return readers.next();
    } else {
      throw new IllegalStateException("No ImageIO reader available for type: " + sniffedType);
    }
  }

  private static byte[] reEncodeToPng(BufferedImage src) {
    BufferedImage normalized = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = normalized.createGraphics();
    try {
      g.setComposite(AlphaComposite.Src);
      g.drawImage(src, 0, 0, null);
    } finally {
      g.dispose();
    }

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      boolean ok = ImageIO.write(normalized, "png", out);
      if (!ok) {
        throw new RuntimeException("Failed to write PNG");
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to write PNG", e);
    }
  }

  private static BufferedImage resizeIfNecessary(BufferedImage originalImage) {
    int w = originalImage.getWidth();
    int h = originalImage.getHeight();

    if (w <= MAX_OUTPUT_WIDTH && h <= MAX_OUTPUT_HEIGHT) {
      return originalImage;
    }

    float scale = (w > h) ? (float) MAX_OUTPUT_WIDTH / (float) w : (float) MAX_OUTPUT_HEIGHT / (float) h;

    int outputWidth = Math.max(1, Math.round(scale * w));
    int outputHeight = Math.max(1, Math.round(scale * h));

    BufferedImage resizedImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphicsForResizedImage = resizedImage.createGraphics();

    try {
      graphicsForResizedImage.setComposite(AlphaComposite.Src);

      graphicsForResizedImage.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      graphicsForResizedImage.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphicsForResizedImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      graphicsForResizedImage.drawImage(originalImage, 0, 0, outputWidth, outputHeight, null);
    } finally {
      graphicsForResizedImage.dispose();
    }

    return resizedImage;
  }
}
