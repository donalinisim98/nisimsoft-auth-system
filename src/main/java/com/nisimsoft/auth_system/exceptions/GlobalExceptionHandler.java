package com.nisimsoft.auth_system.exceptions;

import com.nisimsoft.auth_system.exceptions.auth.AuthenticationFailedException;
import com.nisimsoft.auth_system.exceptions.auth.EmailAlreadyExistsException;
import com.nisimsoft.auth_system.responses.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NoHandlerFoundException ex, WebRequest request) {
    ErrorResponse response =
        new ErrorResponse(
            "Ruta no encontrada",
            "La ruta solicitada no existe: " + ex.getRequestURL(),
            getRequestPath(request));
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(TransactionSystemException.class)
  public ResponseEntity<ErrorResponse> handleTransactionException(
      TransactionSystemException ex, WebRequest request) {

    Throwable cause = ex.getRootCause();

    if (cause instanceof ConstraintViolationException constraintEx) {
      String errorMessage =
          constraintEx.getConstraintViolations().stream()
              .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
              .collect(Collectors.joining(", "));

      ErrorResponse response =
          new ErrorResponse(
              "Error de validación al realizar operación", errorMessage, getRequestPath(request));

      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Si no es ConstraintViolationException, responde con 500 genérico
    ErrorResponse response =
        new ErrorResponse(
            "Error en la transacción",
            "No se pudo completar la operación: " + ex.getMessage(),
            getRequestPath(request));

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(AuthenticationFailedException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationFailed(
      AuthenticationFailedException ex, WebRequest request) {

    String path = ((ServletWebRequest) request).getRequest().getRequestURI();
    ErrorResponse response = new ErrorResponse("Error de autenticación", ex.getMessage(), path);

    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, WebRequest request) {
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining(", "));

    ErrorResponse response =
        new ErrorResponse("Error de validación", errorMessage, getRequestPath(request));
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailExists(
      EmailAlreadyExistsException ex, WebRequest request) {
    ErrorResponse response =
        new ErrorResponse("Error de registro", ex.getMessage(), getRequestPath(request));
    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
    ErrorResponse response =
        new ErrorResponse(
            "Error interno",
            "Ocurrió un error inesperado: " + ex.getMessage(),
            getRequestPath(request));
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // Método auxiliar para obtener la ruta de la solicitud
  private String getRequestPath(WebRequest request) {
    return ((ServletWebRequest) request).getRequest().getRequestURI();
  }
}
