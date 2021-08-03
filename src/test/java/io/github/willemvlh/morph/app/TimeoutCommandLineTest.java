package io.github.willemvlh.morph.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockPart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(args = {"--timeout", "200"})
@AutoConfigureMockMvc
class TimeoutCommandLineTest extends TestClass {

    @Test
    void timeout() throws Exception {
        mvc.perform(multipart("/query")
                .part(new MockPart("xsl", "for $x in 1 to 5000 return for $y in 1 to 1000 return $x + $y".getBytes()))
        )
                .andExpect(status().isBadRequest());
    }
}
