package ch.agridata.testdata;

import static ch.agridata.aws.api.PdfStorageApi.GUARDDUTY_TAG_KEY;

import ch.agridata.aws.api.GuardDutyScanResultEnum;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

/**
 * This class simulates GuardDuty scans for data product pdf-documents.
 *
 * @CommentLastReviewed 2026-07-09
 */

@ApplicationScoped
@IfBuildProfile("local")
@RequiredArgsConstructor
public class GuardDutyScanSimulator {
  private final S3Client s3;

  @ConfigProperty(name = "agridata.product.uploads-bucket-name")
  String bucket;

  private static final String EICAR = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
  private static final byte[] EICAR_BYTES = EICAR.getBytes(StandardCharsets.US_ASCII);
  private static final byte[] UNSUPPORTED_MARKER = "__unsupported__".getBytes(StandardCharsets.US_ASCII);
  private static final byte[] FAILED_MARKER = "__failed__".getBytes(StandardCharsets.US_ASCII);

  @Scheduled(every = "3s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  void scan() {
    for (S3Object object : s3.listObjectsV2(b -> b.bucket(bucket).prefix("data-product/")).contents()) {
      if (!alreadyScanned(object.key())) {
        tag(object.key(), classify(object.key()));
      }
    }
  }

  private boolean alreadyScanned(String key) {
    return s3.getObjectTagging(b -> b.bucket(bucket).key(key)).tagSet().stream().anyMatch(t -> t.key().equals(GUARDDUTY_TAG_KEY));
  }

  private String classify(String key) {
    byte[] content = s3.getObjectAsBytes(b -> b.bucket(bucket).key(key)).asByteArray();
    if (contains(content, UNSUPPORTED_MARKER)) {
      return GuardDutyScanResultEnum.UNSUPPORTED.name();
    }
    if (contains(content, FAILED_MARKER)) {
      return GuardDutyScanResultEnum.FAILED.name();
    }
    if (contains(content, EICAR_BYTES)) {
      return GuardDutyScanResultEnum.THREATS_FOUND.name();
    }
    return GuardDutyScanResultEnum.NO_THREATS_FOUND.name();
  }

  private static boolean contains(byte[] source, byte[] target) {
    if (target.length == 0 || source.length < target.length) {
      return false;
    }

    for (int i = 0; i <= source.length - target.length; i++) {
      int j = 0;
      while (j < target.length && source[i + j] == target[j]) {
        j++;
      }
      if (j == target.length) {
        return true;
      }
    }

    return false;
  }

  private void tag(String key, String status) {
    s3.putObjectTagging(
        b -> b.bucket(bucket).key(key)
            .tagging(Tagging.builder().tagSet(Tag.builder().key(GUARDDUTY_TAG_KEY).value(status).build()).build()));
  }
}
