package tv.mediagenix.transformer.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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
import java.util.Arrays;

@ControllerAdvice
class GlobalControllerExceptionHandler {

    private final Log logger = LogFactory.getLog(this.getClass());

    public GlobalControllerExceptionHandler() {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseBody
    public ErrorMessage handleBadRequest(Exception e) {
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
        StringBuilder sb = new StringBuilder();
        sb
                .append("Transformation exception: ").append(e.getMessage()).append('\n')
                .append("Received following input: ").append('\n');
        try {
            req.getParts().forEach(p -> {
                sb.append(p.getName()).append(": \n");
                try {
                    sb
                            .append(trimFile(Convert.toString(p.getInputStream()))).append("\n\n");
                } catch (IOException err) {
                    sb.append("Could not print file contents: ").append(err.getMessage()).append("\n\n");
                }
            });
            logger.error(sb.toString());
        } catch (IOException | ServletException err) {
            err.printStackTrace();
        }
        return new ErrorMessage(e, 400);
    }

    private String trimFile(String f) {
        StringWriter writer = new StringWriter();
        String[] lines = f.split("\r\n?|\n");
        if (lines.length <= 10) {
            return f;
        }
        Arrays.stream(lines).limit(5).forEach(l -> writer.append(l).append('\n'));
        writer.append(String.format("... (%s lines omitted)\n", lines.length - 10));
        Arrays.stream(lines).skip(lines.length - 5).forEach(l -> writer.append(l).append('\n'));
        return writer.toString();
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ErrorMessage handleUnsupportedMethodException(Exception e) {
        return new ErrorMessage(new InvalidRequestException(e), HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ErrorMessage handleServerError(Exception e) {
        log(e);
        return new ErrorMessage(e, 500);
    }
}