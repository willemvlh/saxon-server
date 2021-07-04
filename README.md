This application exposes a REST API to perform XSLT and XQuery transformations using the Saxon processor.

Start the server by running `java -jar saxon-1.x.jar`.

Transformations can then be invoked by sending an HTTP POST call to the server at the `/transform` or `/query` endpoint, depending
on whether you want to use XSLT or XQuery.
The default port is `5000`, but this can be configured (see below).
This call must contain a `multipart/form-data` encoded body with two items:

* `xml` : a file or string containing the input XML.
* `xsl`: a file or string containing the XSLT or XQuery input.

The `xml` parameter is not mandatory: in the case of XSLT the default named template `xsl:initial-template` will be invoked, in XQuery the query will be evaluated without a context item.

These files can be gzip encoded. In this case, you must set the `Content-Type` of the individual parts as `application/gzip`.
In all other cases, no encoding is assumed.  

The response will contain the serialized result in its body.

Serialization parameters can be specified in the request as well. This is done by adding a form item named `output`, which contains
a list of key-value pairs separated by semi-colons. All serialization parameters supported by Saxon-HE can be used. 
These parameters take precedence over the ones supplied in the stylesheet or query.

In case of an error, the response will contain a JSON object describing the error.
For example:

```
{
    "statusCode": 400,
    "exceptionType": "TransformationException",
    "message": "Compilation error: Content is not allowed in prolog. (line 1, col 1)"
}
```

Following command-line options are available:

* `-c`, `--config`: Location to Saxon configuration XML file
* `-h,--help`: Display help
* `-i, --insecure`: Run with default (insecure) configuration.*
* `-p, --port <arg>`: Port on which the server runs
* `-t, --timeout <arg>`: The maximum time a transformation is allowed to run in milliseconds.
* `-v, --version`: Display Saxon version info


\* This enables external function calls, retrieval of system properties and environment variables and connecting to arbitrary URLs. It also allows the usage of doctype declarations.
This option cannot be set in combination with `--config`. You should not use this when the input is untrusted.


You can build from source by running the Maven command `mvn install`.