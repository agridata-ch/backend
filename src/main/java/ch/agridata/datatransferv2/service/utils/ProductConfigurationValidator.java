package ch.agridata.datatransferv2.service.utils;

import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Guards the entry points of the data transfer against products whose provider configuration is incomplete. Every technical field is
 * optional on a data product, so a product can be activated before its provider configuration is filled in; the transfer and change
 * detection endpoints must reject such a product with a readable error instead of failing deep inside a flow.
 *
 * @CommentLastReviewed 2026-09-11
 */
public final class ProductConfigurationValidator {

  private ProductConfigurationValidator() {
  }

  /**
   * Asserts that the product carries everything a data transfer needs: a rest client, a flow, and the method and path used to build the
   * provider request. {@code restClientRequestTemplate} is not required because GET based products have no body.
   *
   * @throws IllegalStateException if at least one of those fields is not configured
   */
  public static void requireTransferConfiguration(DataProductProviderConfigurationDto config) {
    var missing = new ArrayList<String>();
    if (config.restClientIdentifierCode() == null) {
      missing.add("restClient");
    }
    if (config.flowCode() == null) {
      missing.add("flowCode");
    }
    if (config.restClientMethodCode() == null) {
      missing.add("restClientMethodCode");
    }
    if (config.restClientPathTemplate() == null) {
      missing.add("restClientPathTemplate");
    }
    throwIfIncomplete(config.id(), missing, "transferred");
  }

  /**
   * Asserts that the product carries what change detection needs: a rest client and a flow. The change detection path template itself is
   * checked by the caller, which treats its absence as "change detection is not supported" rather than as a misconfiguration.
   *
   * @throws IllegalStateException if at least one of those fields is not configured
   */
  public static void requireChangeDetectionConfiguration(DataProductProviderConfigurationDto config) {
    var missing = new ArrayList<String>();
    if (config.restClientIdentifierCode() == null) {
      missing.add("restClient");
    }
    if (config.flowCode() == null) {
      missing.add("flowCode");
    }
    throwIfIncomplete(config.id(), missing, "used for change detection");
  }

  private static void throwIfIncomplete(UUID productId, List<String> missing, String action) {
    if (!missing.isEmpty()) {
      throw new IllegalStateException(
          "Product " + productId + " cannot be " + action + ", the following fields are not configured: " + String.join(", ", missing));
    }
  }
}
