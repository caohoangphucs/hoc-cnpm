package vn.id.caohoangphuc.cnpm.service;

public class ServiceException extends RuntimeException {
  private final int status;
  private final String code;

  /** Creates a service failure with the HTTP status and stable API error code. */
  public ServiceException(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  /** Returns the HTTP status used by the API error response. */
  public int getStatus() {
    return status;
  }

  /** Returns the stable machine-readable error code. */
  public String getCode() {
    return code;
  }

  /** Creates a not-found error for a named resource. */
  public static ServiceException notFound(String resource) {
    return new ServiceException(404, "NOT_FOUND", resource + " not found");
  }

  /** Creates a conflict error when current resource state blocks an operation. */
  public static ServiceException conflict(String message) {
    return new ServiceException(409, "CONFLICT", message);
  }

  /** Creates a validation error for invalid request data. */
  public static ServiceException invalid(String message) {
    return new ServiceException(422, "INVALID", message);
  }
}
