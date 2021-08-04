package io.github.willemvlh.transformer.saxon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Convert {

    public static String toString(InputStream source) throws IOException {
        return new BufferedReader(new InputStreamReader(source))
                .lines().collect(Collectors.joining("\n"));

    }

}
