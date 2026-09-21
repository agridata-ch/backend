package ch.agridata.common.exceptions;

/**
 * Indicates a faulty {@link ch.agridata.common.persistence.SearchSpec}, e.g. base parameters
 * colliding with the parameter names generated for the search or column filter clause. This is a
 * programming error, not a bad request, and therefore results in a 500 response.
 *
 * @CommentLastReviewed 2026-09-14
 */
public class SearchSpecificationException extends RuntimeException {

  public SearchSpecificationException(String message) {
    super(message);
  }

}
