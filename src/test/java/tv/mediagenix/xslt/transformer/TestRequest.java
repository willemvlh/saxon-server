package tv.mediagenix.xslt.transformer;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestRequest {

  private String path = "transform";
  private boolean isPost = true;
  private final List<MultipartBody.Part> parts = new ArrayList<>();

  public TestRequest addXML(String payload) {
    parts.add(MultipartBody.Part.createFormData("xml", payload));
    return this;
  }

  public TestRequest addXSL(String payload) {
    parts.add(MultipartBody.Part.createFormData("xsl", payload));
    return this;
  }

  public TestRequest addParameters(String payload) {
    parts.add(MultipartBody.Part.createFormData("parameters", payload));
    return this;
  }

  public TestRequest addOutput(String payload) {
    parts.add(MultipartBody.Part.createFormData("output", payload));
    return this;
  }

  public TestRequest addPart(MultipartBody.Part part) {
    parts.add(part);
    return this;
  }

  public Response execute() {
    try {
      OkHttpClient client = new OkHttpClient();
      return client.newCall(this.request()).execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Request request() {
    HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(5000).addPathSegment(this.path).build();
    if (!this.isPost) {
      return new Request.Builder().url(url).build();
    }
    MultipartBody.Builder builder = new MultipartBody.Builder();
    if (parts.isEmpty()) {
      parts.add(MultipartBody.Part.createFormData("dummy", "dummy"));
    }
    for (MultipartBody.Part part : parts) {
      builder.addPart(part);
    }
    builder.setType(MultipartBody.FORM);
    return new Request.Builder().url(url).post(builder.build()).build();
  }

  public void setIsGetRequest() {
    this.isPost = false;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
