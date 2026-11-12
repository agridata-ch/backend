package ch.agridata.auditing.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ActionEnum}. The action is stored by name in {@code audit_log.action_code}, a {@code VARCHAR(50)} column, so a
 * longer name only fails at runtime when the action is first audited.
 *
 * @CommentLastReviewed 2026-11-12
 */

class ActionEnumTest {

  private static final int ACTION_CODE_MAX_LENGTH = 50;

  @Test
  void whenAllActionsChecked_thenEveryNameFitsIntoActionCodeColumn() {
    var tooLong = Arrays.stream(ActionEnum.values())
        .map(ActionEnum::name)
        .filter(name -> name.length() > ACTION_CODE_MAX_LENGTH)
        .toList();

    assertThat(tooLong)
        .as("Action names longer than %d characters do not fit into audit_log.action_code", ACTION_CODE_MAX_LENGTH)
        .isEmpty();
  }
}
