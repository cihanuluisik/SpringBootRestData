package bookmarks.exception;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(value = { ApiBaseException.class })
    protected ResponseEntity<Object>  handleApiErrors(Exception ex) {
        ApiError apiError = null;
        try {
            apiError = new ApiError(ex.getClass().getDeclaredAnnotationsByType(ResponseStatus.class)[0].value(), ex.getLocalizedMessage(), "api error occurred");
        } catch (Exception e) {
            apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), "uncategorized error occurred");
        }

        return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
    }

}
