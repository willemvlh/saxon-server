package tv.mediagenix.transformer.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockPart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@AutoConfigureMockMvc
class XQueryTests extends TestClass {

    @Test
    void xQuery() throws Exception {
        query("dummy.xml", "abc.xquery")
                .andExpect(status().isOk())
                .andExpect(content().string("abc"));
    }

    @Test
    void xQueryJson() throws Exception {
        query("dummy.xml", "hof.xquery")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("abc"));
    }

    @Test
    void xQueryError() throws Exception {
        queryWithError("dummy.xml", "syntax-error.xquery", null);
    }

    @Test
    void xQueryErrorFunction() throws Exception {
        queryWithError("dummy.xml", "error.xquery", err -> {
            assertEquals(":-(", err.getMessage());
        });
    }

    @Test
    void xQueryNoInput() throws Exception {
        query("hof.xquery")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("abc"));
    }

    @Test
    void jsonOutput() throws Exception {
        mvc.perform(multipart("/query")
                .part(new MockPart("xml", "{\"a\": \"b\"}".getBytes()))
                .part(new MockPart("xsl", "xml-to-json(.)".getBytes()))
                .part(new MockPart("output", "method=text;media-type=application/json".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a").value("b"));
    }

    @Test
    void parameters() throws Exception {
        mvc.perform(multipart("/query")
                .part(new MockPart("parameters", "myParam=someText".getBytes()))
                .part(new MockPart("xsl", "declare variable $myParam external; $myParam".getBytes()))
                .part(new MockPart("output", "method=text".getBytes())))
                .andExpect(status().isOk())
                .andExpect(content().string("someText"));
    }
}
