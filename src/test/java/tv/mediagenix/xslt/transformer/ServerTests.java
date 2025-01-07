package tv.mediagenix.xslt.transformer;

import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static tv.mediagenix.xslt.transformer.TestHelpers.*;

public class ServerTests {
    @Test
    public void transformation() {
        runServer(() -> {
            var res = request(WellFormedXml, WellFormedXsl);
            try {
                var body = res.body().string();
                assertEquals("hello", body);
                assertEquals(200, res.code());
            } catch (IOException e) {
               fail();
            }
        });
    }

    @Test
    public void largeFile(){
        StringBuilder sb = new StringBuilder();
        sb.append("<root>\n");
        sb.append("<child arg=\"value\"></child>\n".repeat(1000000));
        sb.append("</root>\n");
        runServer(() -> {
            var res = request(sb.toString(), WellFormedXsl);
            assertEquals(200, res.code());
        });
    }

    @Test
    public void gzip() throws IOException {
        var byteStream = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(byteStream);
        out.write(WellformedXslWithInitialTemplate.getBytes(StandardCharsets.UTF_8));
        out.close();
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        var part = MultipartBody.Part.createFormData("xsl", "xsl.xsl",
                    RequestBody.create(MediaType.get("application/gzip"), byteStream.toByteArray()));
        builder.addPart(part);
        var request = new Request.Builder().url("http://localhost:5000/transform").post(builder.build()).build();
        runServer(() -> {
            try {
                Response response = client.newCall(request).execute();
                assertEquals(200, response.code());
            } catch (IOException e) {
                fail();
            }
        });
    }
    @Test
    public void badGzip() throws IOException {
        var byteStream = new ByteArrayOutputStream();
        byteStream.write("some unzipped content".getBytes(StandardCharsets.UTF_8));
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        var part = MultipartBody.Part.createFormData("xsl", "xsl.xsl",
                RequestBody.create(MediaType.get("application/gzip"), byteStream.toByteArray()));
        builder.addPart(part);
        var request = new Request.Builder().url("http://localhost:5000/transform").post(builder.build()).build();
        runServer(() -> {
            try {
                Response response = client.newCall(request).execute();
                assertEquals(400, response.code());
            } catch (IOException e) {
                fail();
            }
        });
    }
    @Test
    public void query() throws IOException {
        TestRequest req = new TestRequest();
        req.setPath("query");
        req.addXSL("declare option saxon:output \"omit-xml-declaration=true\";" +
                "let $x := \"abc\"" +
                "return $x");
        Response res = runServer(req::execute);
        assertEquals(200, res.code());
        assertEquals("abc", res.body().string());

    }

    @Test
    public void noXML() throws IOException {
        TestRequest req = new TestRequest();
        req.addXSL(WellformedXslWithInitialTemplate);
        Response res = runServer(req::execute);
        assertEquals(200, res.code());
        assertEquals("hello", res.body().string());
    }

    @Test
    public void noXSL() {
        TestRequest req = new TestRequest();
        req.addXML("<abc/>");
        Response res = runServer(req::execute);
        assertEquals(400, res.code());
    }


    @Test
    public void outputParameters() throws IOException {
        TestRequest req = new TestRequest();
        req.addOutput("method=text;media-type=application/json");
        req.setPath("query");
        req.addXML("{\"a\": \"b\"}");
        req.addXSL("xml-to-json(.)");
        var res = runServer(req::execute);
        assertEquals(200, res.code());
        assertEquals("application/json;charset=utf-8", res.header("Content-Type").toLowerCase());
        assertEquals("{\"a\":\"b\"}", res.body().string());
    }

    @Test
    public void parameters() throws IOException {
        TestRequest req = new TestRequest();
        req.addParameters("myParam=myValue");
        req.addXML(WellFormedXml);
        req.addXSL(XslWithParameters);
        var res = runServer(req::execute);
        assertEquals(200, res.code());
        assertEquals("myValue", res.body().string());
    }

    @Test
    public void invalidParameters() {
        var res = runServer(() ->  new TestRequest()
                .addXSL(WellFormedXsl)
                .addParameters("eh?").execute());
        assertEquals(400, res.code());
    }

    @Test
    public void notFound() {
        TestRequest req = new TestRequest();
        req.setPath("unknown");
        var res = runServer(req::execute);
        assertEquals(404, res.code());
    }
}
