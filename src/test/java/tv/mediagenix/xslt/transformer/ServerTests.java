package tv.mediagenix.xslt.transformer;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tv.mediagenix.xslt.transformer.TestHelpers.*;

class ServerTests {
  private org.slf4j.Logger logger = LoggerFactory.getLogger(ServerTests.class);

  @Test
  void transformation() {
    runServer(() -> assertDoesNotThrow(() -> {
      var res = request(WellFormedXml, WellFormedXsl);
      var body = res.body().string();
      assertEquals("hello", body);
      assertEquals(200, res.code());
    }));
  }

  @Test
  void largeFile() {
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
  void gzip() throws IOException {
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
    runServer(() -> assertDoesNotThrow(() -> {
      Response response = client.newCall(request).execute();
      assertEquals(200, response.code());
    }));
  }

  @Test
  void badGzip() throws IOException {
    var byteStream = new ByteArrayOutputStream();
    byteStream.write("some unzipped content".getBytes(StandardCharsets.UTF_8));
    OkHttpClient client = new OkHttpClient();
    MultipartBody.Builder builder = new MultipartBody.Builder();
    builder.setType(MultipartBody.FORM);
    var part = MultipartBody.Part.createFormData("xsl", "xsl.xsl",
        RequestBody.create(byteStream.toByteArray(), MediaType.get("application/gzip")));
    builder.addPart(part);
    var request = new Request.Builder().url("http://localhost:5000/transform").post(builder.build()).build();
    runServer(() -> assertDoesNotThrow(() -> {
      Response response = client.newCall(request).execute();
      assertEquals(400, response.code());
    }));
  }

  @Test
  void query() throws IOException {
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
  void noXML() throws IOException {
    TestRequest req = new TestRequest();
    req.addXSL(WellformedXslWithInitialTemplate);
    Response res = runServer(req::execute);
    assertEquals(200, res.code());
    assertEquals("hello", res.body().string());
  }

  @Test
  void noXSL() {
    TestRequest req = new TestRequest();
    req.addXML("<abc/>");
    Response res = runServer(req::execute);
    assertEquals(400, res.code());
  }

  @Test
  void outputParameters() throws IOException {
    TestRequest req = new TestRequest();
    req.addOutput("method=text;media-type=application/json");
    req.setPath("query");
    req.addXML("{\"a\": \"b\"}", "application/json");
    req.addXSL("xml-to-json(.)");
    var res = runServerDebug(req::execute);
    assertEquals("{\"a\":\"b\"}", res.body().string());
    assertEquals(200, res.code());
    assertEquals("application/json;charset=utf-8", res.header("Content-Type").toLowerCase());
  }

  @Test
  void parameters() throws IOException {
    TestRequest req = new TestRequest();
    req.addParameters("myParam=myValue");
    req.addXML(WellFormedXml);
    req.addXSL(XslWithParameters);
    var res = runServer(req::execute);
    assertEquals(200, res.code());
    assertEquals("myValue", res.body().string());
  }

  @Test
  void files() throws IOException {
    TestRequest req = new TestRequest();
    req.addPart(MultipartBody.Part.createFormData("file", "test.xml",
        RequestBody.create("<abc>test</abc>", MediaType.get("application/xml"))));
    req.addXML(WellFormedXml).addXSL(XslWithFile);
    var res = runServer(req::execute);
    assertEquals(200, res.code());
    assertEquals("test", res.body().string());
  }

  @Test
  void filesDuplicate() {
    TestRequest req = new TestRequest();
    var part1 = MultipartBody.Part.createFormData("file", "duplicate_key", RequestBody.create(new byte[]{1,2,3}));
    var part2 = MultipartBody.Part.createFormData("file", "duplicate_key", RequestBody.create(new byte[]{1,2,3}));
    req.addPart(part1).addPart(part2);
    req.addXML(WellFormedXml).addXSL(WellFormedXsl);
    var res = runServer(req::execute);
    assertEquals(200, res.code());
  }

  @Test
  void filesUnparsedText() throws IOException {
    TestRequest req = new TestRequest();
    req.addPart(MultipartBody.Part.createFormData("file", "test.txt",
        RequestBody.create("test", MediaType.get("application/text"))));
    req.addXML(WellFormedXml).addXSL(XslWithUnparsedTextFn);
    var res = runServer(req::execute);
    assertEquals(200, res.code());
    assertEquals("test", res.body().string());
  }

  @Test
  void filesUnparsedTextWithModifiedBaseUri() throws IOException {
    TestRequest req = new TestRequest();
    req.addPart(MultipartBody.Part.createFormData("file", "test.txt",
        RequestBody.create("test", MediaType.get("application/text"))));
    req.addXML(WellFormedXml).addXSL(XslWithUnparsedTextFn);
    var res = runServer(req::execute, "--debug", "--base-uri", "http://localhost:5000/files/");
    assertEquals(200, res.code());
    assertEquals("test", res.body().string());
  }

  @Test
  void filesJsonDoc() throws IOException {
    TestRequest req = new TestRequest();
    req.addPart(MultipartBody.Part.createFormData("file", "test.json",
        RequestBody.create("{\"key\": \"value\"}", MediaType.get("application/json"))));
    req.addXML(WellFormedXml).addXSL(XslWithJsonDocFn);
    var res = runServerDebug(req::execute);
    logger.info(res.body().string());
    assertEquals(200, res.code());
  }

  @Test
  void filesGzipped() throws IOException {
    var byteStream = new ByteArrayOutputStream();
    GZIPOutputStream out = new GZIPOutputStream(byteStream);
    out.write("<abc>test</abc>".getBytes(StandardCharsets.UTF_8));
    out.close();
    TestRequest req = new TestRequest();
    req.addPart(MultipartBody.Part.createFormData("file", "test.xml",
        RequestBody.create(byteStream.toByteArray(), MediaType.get("application/gzip"))));
    req.addXML(WellFormedXml).addXSL(XslWithFile);
    var res = runServer(req::execute);
    assertEquals(200, res.code());
    assertEquals("test", res.body().string());
  }

  @Test
  void wronglyNamedFile() throws IOException {
    TestRequest req = new TestRequest();
    req.addPart(MultipartBody.Part.createFormData("file", "wrong_name_whatever.xml",
        RequestBody.create("<abc>test</abc>", MediaType.get("application/xml"))));
    req.addXML(WellFormedXml).addXSL(XslWithFile);
    var res = runServer(req::execute, "--insecure");
    logger.info("Response: {}", res.body().string());
    assertEquals(400, res.code());
  }

  @Test
  void invalidParameters() {
    var res = runServer(() -> new TestRequest()
        .addXSL(WellFormedXsl)
        .addParameters("eh?").execute());
    assertEquals(400, res.code());
  }

  @Test
  void notFound() {
    TestRequest req = new TestRequest();
    req.setPath("unknown");
    var res = runServer(req::execute);
    assertEquals(404, res.code());
  }

  @Test
  void getInfo() {
    TestRequest req = new TestRequest();
    req.setPath("info");
    req.setIsGetRequest();
    var res = runServer(req::execute);
    assertEquals(200, res.code());
  }

  @Test
  void getWebPage() throws IOException {
    TestRequest req = new TestRequest();
    req.setPath("/");
    req.setIsGetRequest();
    var res = runServer(req::execute);
    assertEquals(200, res.code());
    assertEquals("text/html", res.header("Content-Type").toLowerCase().split(";")[0]);
    assertEquals("<!DOCTYPE html>", res.body().string().substring(0, 15));
  }

  @Test
  void getWebPageWhenDisabled() {
    TestRequest req = new TestRequest();
    req.setPath("/");
    req.setIsGetRequest();
    var res = runServer(req::execute, "--disable-frontend");
    assertEquals(404, res.code());
  }

  @Test
  void httpRequestInsecure() {
    TestRequest req = new TestRequest();
    req.addXSL(XslWithHttpRequest)
      .addXML("<abc/>");
    var res = runServer(req::execute, "--insecure");
    assertEquals(200, res.code());
  }

  @Test
  void httpRequestSecure() {
    TestRequest req = new TestRequest();
    req.addXSL(XslWithHttpRequest)
      .addXML("<abc/>");
    var res = runServer(req::execute);
    assertEquals(400, res.code());
  }
}
