package com.logistimo.callisto.exception;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
@RestController
public class CustomizedResponseExceptionHandler extends ResponseEntityExceptionHandler{

  private static final Logger logger = Logger.getLogger(CustomizedResponseExceptionHandler.class);

  @ExceptionHandler(CallistoSyntaxErrorException.class)
  public final ResponseEntity<ErrorDetails> handleQuerySyntaxErrorException
      (CallistoSyntaxErrorException ex, WebRequest request) {
    logger.error(ex.getMessage(), ex);
    ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
        request.getDescription(false));
    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(CallistoException.class)
  public final ResponseEntity<ErrorDetails> handleCallistoException
      (CallistoException ex, WebRequest request) {
    logger.error(ex.getMessage(), ex);
    ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
        request.getDescription(false));
    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(DuplicateQueryIdException.class)
  public final ResponseEntity<ErrorDetails> handleDuplicateQueryIdException
      (DuplicateQueryIdException ex, WebRequest request) {
    ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
        request.getDescription(false));
    return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
    logger.error(ex.getMessage(), ex);
    ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
        request.getDescription(false));
    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}