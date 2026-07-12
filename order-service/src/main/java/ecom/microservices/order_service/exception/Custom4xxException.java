package ecom.microservices.order_service.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public class Custom4xxException extends RuntimeException {
  private final HttpStatusCode statusCode;
  private final HttpHeaders headers;

  public Custom4xxException(HttpStatusCode statusCode, HttpHeaders headers) {
    super("HTTP Error: " + statusCode);
    this.statusCode = statusCode;
    this.headers = headers;
  }

  public HttpStatusCode getStatusCode() {
    return statusCode;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }
}
