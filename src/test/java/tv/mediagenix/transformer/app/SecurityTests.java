package tv.mediagenix.transformer.app;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests extends TestClass {

    @Test
    void resultDocument() throws Exception {
        transformWithError("dummy.xml", "test-result-document.xsl", null);
    }

    @Test
    void resolveUrl() throws Exception {
        transform("dummy.xml", "test-uri-resolver.xsl")
                .andExpect(jsonPath("$.fn:json-doc").value("err:FOUT1170"))
                .andExpect(jsonPath("$.fn:doc").value("err:FODC0002"))
                .andExpect(jsonPath("$.fn:unparsed-text").value("err:FOUT1170"));
    }

    @Test
    void docType() throws Exception {
        transformWithError("dummy.xml", "test-dtd.xsl", null);
    }

    @Test
    void documentFn() throws Exception {
        transformWithError("dummy.xml", "test-document-fn", null);
    }

    @Test
    void serverHeader() throws Exception {
        transformDummy("test-1.xsl").andExpect(header().doesNotExist("Server"));
    }

    @Test
    void wrongURL() throws Exception {
        mvc.perform(MockMvcRequestBuilders.request("GET", new URI("bla"))).andExpect(status().isNotFound());
    }

    @Test
    void systemProperties() throws Exception {
        transform("dummy.xml", "test-system-properties.xsl")
                .andExpect(xpath("//environment-variables/var").nodeCount(0))
                .andExpect(xpath("//system-properties/prop[matches(@name, 'xsl:|file:|archive:|bin:') => not()]").nodeCount(0)

                );
    }
}
