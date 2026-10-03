package vn.id.caohoangphuc.cnpm.service;

public class ServiceException extends RuntimeException {
  private final int status;
  private final String code;

  public ServiceException(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public int getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public static ServiceException notFound(String resource) {
    return new ServiceException(404, "NOT_FOUND", resource + " not found");
  }

  public static ServiceException conflict(String message) {
    return new ServiceException(409, "CONFLICT", message);
  }

  public static ServiceException invalid(String message) {
    return new ServiceException(422, "INVALID", message);
  }
}
