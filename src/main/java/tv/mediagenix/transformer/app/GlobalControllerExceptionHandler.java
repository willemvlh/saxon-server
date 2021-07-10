package tv.mediagenix.transformer.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import tv.mediagenix.transformer.saxon.Convert;
import tv.mediagenix.transformer.saxon.TransformationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


@ControllerAdvice
class GlobalControllerExceptionHandler {

    private final Log logger = LogFactory.getLog(this.getClass());

    public GlobalControllerExceptionHandler() {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseBody
    public ErrorMessage handleBadRequest(Exception e) {
        log(e);
        return new ErrorMessage(e, 400);
    }

    private void log(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.error(sw.toString());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TransformationException.class)
    @ResponseBody
    public ErrorMessage handleBadRequest(HttpServletRequest req, TransformationException e) {
        //TODO make it prettier
        try {
            req.getParts().forEach(p -> {
                logger.error(p.getName());
                try {
                    logger.error(Convert.toString(p.getInputStream()));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
        } catch (IOException | ServletException ioException) {
            ioException.printStackTrace();
        }
        return new ErrorMessage(e, 400);
    }

    private String trimFile(String f) {
        StringWriter writer = new StringWriter();
        writer.append("Input: \n");
        String[] lines = f.split("\r\n?|\n");
        if (lines.length < 10) {
            return f;
        }
        for (int i = 0; i < 5; i++) {
            writer.append(lines[i]).append("\n");
        }
        writer.append("...\n");
        for (int i = lines.length - 5; i < lines.length; i++) {
            writer.append(lines[i]).append("\n");
        }
        return writer.toString();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ErrorMessage handleServerError(Exception e) {
        log(e);
        return new ErrorMessage(e, 500);
    }
}