package tv.mediagenix.xslt.transformer;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestRequest {
    private String path = "transform";
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

    public Response execute() {
        try {
            OkHttpClient client = new OkHttpClient();
            return client.newCall(this.request()).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Request request() {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (parts.isEmpty()) {
            parts.add(MultipartBody.Part.createFormData("dummy", "dummy"));
        }
        for (MultipartBody.Part part : parts) {
            builder.addPart(part);
        }
        builder.setType(MultipartBody.FORM);
        HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(5000).addPathSegment(this.path).build();
        return new Request.Builder().url(url).post(builder.build()).build();
    }

    public void setPath(String path) {
        this.path = path;
    }
}
