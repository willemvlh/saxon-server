package io.github.willemvlh.transformer.saxon.json;

public enum JsonTransformationSetting {
    XMLDOCUMENT {
        @Override
        public JsonTransformer getTransformer() {
            return new JsonToXmlTransformer();
        }
    },
    XDMVALUE {
        @Override
        public JsonTransformer getTransformer() {
            return new JsonToXdmTransformer();
        }
    };

    abstract public JsonTransformer getTransformer();
}
