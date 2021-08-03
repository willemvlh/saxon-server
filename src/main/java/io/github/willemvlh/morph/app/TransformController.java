package io.github.willemvlh.morph.app;

import io.github.willemvlh.morph.saxon.SerializationProps;
import io.github.willemvlh.morph.saxon.actors.ActorType;
import io.github.willemvlh.morph.saxon.actors.SaxonActor;
import io.github.willemvlh.morph.saxon.actors.SaxonActorBuilder;
import net.sf.saxon.s9api.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@RestController
class TransformController {

    private final Processor processor;
    private final ServerOptions options;

    @Autowired
    public TransformController(Processor processor, ServerOptions options) {
        this.processor = processor;
        this.options = options;
    }

    @PostMapping(path = {"/query", "/transform"})
    public ResponseEntity<byte[]> doTransform(
            @RequestPart(name = "xml", required = false) Part xml, //use Part instead of MultipartFile so that we can also send strings
            @RequestPart(name = "xsl", required = false) Part xsl,
            @RequestParam(name = "output", required = false) String output,
            @RequestParam(name = "parameters", required = false) String parameters,
            HttpServletRequest request)
            throws Exception {

        Map<String, String> params = new ParameterParser().parseString(parameters);
        Map<String, String> serParams = new ParameterParser().parseString(output);
        SaxonActorBuilder builder = getBuilder(request.getRequestURI());
        SaxonActor tf = builder
                .setProcessor(processor)
                .setTimeout(options.getTransformationTimeoutMs())
                .setParameters(params)
                .setSerializationProperties(serParams)
                .build();

        Optional<InputStream> xmlStr = Optional.ofNullable(xml).flatMap(this::getInputStream);
        InputStream xslStr = Optional.ofNullable(xsl).flatMap(this::getInputStream)
                .orElseThrow(() -> new InvalidRequestException("No XSL supplied"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SerializationProps props = xmlStr.isPresent()
                ? tf.act(new BufferedInputStream(xmlStr.get()), xslStr, os)
                : tf.act(xslStr, os);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(props.getContentType()));
        return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
    }

    private Optional<InputStream> getInputStream(Part p) {
        try {
            String contentType = p.getContentType();
            if ("application/gzip".equalsIgnoreCase(contentType)) {
                return Optional.of(getZippedStreamFromPart(p.getInputStream()));
            }
            return Optional.ofNullable(p.getInputStream());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private InputStream getZippedStreamFromPart(InputStream input) {
        try {
            GZIPInputStream s = new GZIPInputStream(input);
            ZippedStreamReader r = new ZippedStreamReader();
            return r.unzipStream(s);
        } catch (IOException e) {
            throw new InvalidRequestException(e);
        }
    }

    private SaxonActorBuilder getBuilder(String requestURI) {
        switch (requestURI) {
            case "/query":
                return ActorType.QUERY.getBuilder();
            case "/transform":
                return ActorType.TRANSFORM.getBuilder();
            default:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}

