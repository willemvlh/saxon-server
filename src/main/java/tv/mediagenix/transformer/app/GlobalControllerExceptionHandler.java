package tv.mediagenix.transformer.app;

import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import tv.mediagenix.transformer.saxon.TransformationException;

@ControllerAdvice
class GlobalControllerExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({TransformationException.class, InvalidRequestException.class})
    @ResponseBody
    ErrorMessage handleConflict(Exception e) {
        LogFactory.getLog(this.getClass()).error(e.getMessage());
        return new ErrorMessage(e, 400);
    }
}
