package io.github.willemvlh.transformer.saxon;

import io.github.willemvlh.transformer.saxon.config.SaxonDefaultConfigurationFactory;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SaxonConfigurationFactoryTest {

    @Test
    void enterprise() {
        Configuration configuration = new SaxonDefaultConfigurationFactory().newConfiguration();
        Processor proc = new Processor(configuration);
        assertThrows(SaxonApiException.class, () -> proc.newXQueryCompiler().compile("saxon:timestamp()"));
    }
}
