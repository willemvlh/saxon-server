This application exposes an HTTP API to perform XSLT and XQuery transformations using the Saxon processor.

## Installing

There are two ways of installing the application:

* Download the JAR file from the Releases section on GitHub.
* By using Docker: `$ docker pull ghcr.io/willemvlh/saxon-server:latest`

## Running

Start the server by running `java -jar saxon-2.x.jar`.

Following command-line options are available:

* `-c`, `--config`: Location to Saxon configuration XML file
* `-h`, `--help`: Display help
* `-i`, `--insecure`: Run with default (insecure) configuration.
* `-o`, `--output <arg>`: Write console output to the specified file
* `-p`, `--port <arg>`: Port on which the server runs
* `-t`, `--timeout <arg>`: The maximum time a transformation is allowed to run in milliseconds.
* `-v`, `--version`: Display Saxon version info

Transformations can then be invoked by sending an HTTP POST call to the server at the `/transform` or `/query` endpoint,
depending on whether you want to use XSLT or XQuery. The default port is `5000`, but this can be configured (see above).
This call must contain a `multipart/form-data` encoded body with two items:

* `xml` : a file or string containing the XML or JSON document to be transformed.
* `xsl`: a file or string containing the XSLT or XQuery input.

The `xml` parameter is not mandatory: in the case of XSLT the default named template `xsl:initial-template` will be
invoked, in XQuery the query will be evaluated without a context item.

An example call (using cURL) may look as follows:

`$ curl http://localhost:5000/transform -F xml=@input.xml -F xsl=@stylesheet.xsl`

The response body contains the result of the transformation. The character set of the response is the one specified in
the output parameters of the stylesheet, which defaults to UTF-8. The value of the `Content-Type` header can be
controlled by setting the `media-type` output parameter.

## JSON

Next to XML, the application also supports sending JSON as an input format. No additional parameters must be set as JSON
is automatically distinguished from XML. Note that JSON input is automatically transformed to XML using
the `json-to-xml`
function and set as the global context item.

    $ curl http://localhost:5000/query -F xsl=. -F xml="[1,2,3]" -F output="indent=yes"
    <?xml version="1.0" encoding="UTF-8"?>
    <array xmlns="http://www.w3.org/2005/xpath-functions">
        <number>1</number>
        <number>2</number>
        <number>3</number>
    </array>

## Security

By default, Saxon assumes all input is untrusted. This means following functionalities are disabled:

* External function calls
* Retrieval of system properties and environment variables
* Accessing the file system or network

When you do want to allow this, you can either pass the `--insecure` command line parameter, or supply a custom Saxon
configuration file using the `--config` parameter. Note that this alone is not enough to protect against attackers. It
is recommended to place a proxy server in front of this application to take care of IP whitelisting, rate limiting, etc.

The amount of time that a transformation is allowed to run is 10 seconds by default. This can be configured with
the `--timeout` parameter, which takes a number in milliseconds. Use `-1` to disable timeouts.
## Compression

The input can be gzip encoded in order to avoid having to send large files over the network. In this case, you must set
the `Content-Type` of the individual parts as `application/gzip`. In all other cases, no encoding is assumed.

To receive a compressed response, make sure to pass `Accept-Encoding: gzip` in the headers.

## Parameters

It is possible to include parameters to a transformation by including a `parameters` form item in the request.
Parameters must be included in the form `key=value` and are separated using a semicolon. Semicolons can be escaped using
a backslash.

Consider an example stylesheet as follows:

    <?xml version="1.0" encoding="UTF-8"?>
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
        <xsl:output method="text" encoding="UTF-8"/>
        <xsl:param name="myParam"/>
        <xsl:template match="/">
            <xsl:value-of select="$myParam"/>
        </xsl:template>
    </xsl:stylesheet>

The parameter can then be set like this:

    $ curl http://localhost:5000/transform -F xsl=@stylesheet.xsl -F xml=@input.xml -F parameters="myParam=example"
    example

## Serialization

Serialization parameters can be specified in the request as well. This is done by adding a form item named `output`,
which is formed in the same way as normal parameters as described above. All serialization parameters supported by Saxon
can be used. These parameters take precedence over the ones supplied in the stylesheet or query.

For example, to return the result as JSON:

    $ curl http://localhost:5000/query -F xsl="map{'numbers': array{1,2,3}}" -F output="method=json"
    {"numbers":[1,2,3]}

## Error handling

There are different types of errors that can occur:

* Compilation errors (for example, syntactical errors, or an invalid XSLT document)
* Runtime errors (for example, type errors or inaccessible filepaths)
* User-invoked errors using the `xsl:message` element or the `error` function

These errors are all caught and then returned in the reponse. Where possible, the line number and column inside the
stylesheet where the error was encountered is passed along. The response code in this case is always `400`.

```
{
    "statusCode": 400,
    "exceptionType": "TransformationException",
    "message": "Compilation error: Content is not allowed in prolog. (line 1, col 1)"
}
```

Note that `xsl:message` elements without the `terminate=yes` attribute are ignored.

## Performance

Throughput and latency depend on the size of the payload and the complexity of the stylesheet. With small, relatively
simple stylesheets, the application can easily handle hundreds of requests per second.

## Developing

To start developing, you must first clone this repository and then run `mvn clean` (which amongst other things installs
the included Saxon library in your local repository).
