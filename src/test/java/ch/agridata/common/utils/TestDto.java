package ch.agridata.common.utils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public class TestDto {

  @Size(max = 100)
  @Size(min = 10, max = 90, groups = OnSubmit.class)
  @NotNull(groups = OnCreate.class)
  private String description;

  @Pattern(regexp = "\\d{4}")
  private String code;

  @Min(10L)
  private int quantity;

  @Max(50L)
  private int stock;

  @NotNull(groups = OnSubmit.class)
  private Boolean active;

  @NotNull(groups = OnSubmit.class)
  private Status status;

  @NotEmpty
  private List<NestedDto> items;

  @Valid
  private NestedDto metadata;

  public interface OnCreate {
  }

  public interface OnSubmit {
  }

  public static class NestedDto {
    @NotNull
    private String id;

    @Size(min = 2, max = 10)
    private String name;
  }

  public static enum Status {
    DRAFT,
    PUBLISHED,
    ARCHIVED
  }
}
