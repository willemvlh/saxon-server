package tv.mediagenix.transformer.app;
import org.hamcrest.collection.ArrayMatching;
import org.hamcrest.collection.IsArrayContainingInAnyOrder;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.XpathResultMatchers;
import org.springframework.util.StreamUtils;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc

public class SecurityTests extends TestClass {

    @Test
    public void resultDocument() throws Exception {
        transformWithError("dummy.xml", "test-result-document.xsl", null);
    }

    @Test
    public void resolveUrl() throws Exception {
        transform("dummy.xml", "test-uri-resolver.xsl")
                .andExpect(jsonPath("$.fn:json-doc").value("err:FOUT1170"))
                .andExpect(jsonPath("$.fn:doc").value("err:FODC0002"))
                .andExpect(jsonPath("$.fn:unparsed-text").value("err:FOUT1170"));
    }

    @Test
    public void docType() throws  Exception {
        transformWithError("dummy.xml", "test-dtd.xsl", null);
    }

    @Test
    public void documentFn() throws  Exception {
        transformWithError("dummy.xml", "test-document-fn", null);
    }

    @Test
    public void serverHeader() throws Exception{
        transformDummy("test-1.xsl").andExpect(header().doesNotExist("Server"));
    }

    @Test
    public void wrongURL() throws Exception {
        mvc.perform(MockMvcRequestBuilders.request("GET", new URI("bla"))).andExpect(status().isNotFound());
    }

    @Test
    public void systemProperties() throws Exception{
        transform("dummy.xml", "test-system-properties.xsl")
                .andExpect(xpath("//environment-variables/var").nodeCount(0))
                .andExpect(xpath("//system-properties/prop[starts-with(@name, 'xsl:') => not()]").nodeCount(0)

        );
    }
}
