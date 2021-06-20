package tv.mediagenix.transformer.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import tv.mediagenix.transformer.saxon.TransformationException;


@ControllerAdvice
class GlobalControllerExceptionHandler {

    private Log logger = LogFactory.getLog(this.getClass());

    private void log(Exception e) {
        e.printStackTrace();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({TransformationException.class, InvalidRequestException.class})
    @ResponseBody
    public ErrorMessage handleBadRequest(Exception e) {
        log(e);
        return new ErrorMessage(e, 400);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ErrorMessage handleServerError(Exception e) {
        log(e);
        e.printStackTrace();
        return new ErrorMessage(e, 500);
    }
}