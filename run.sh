#!/bin/bash
exec java -jar "$(dirname "$0")"/target/saxon-*.jar "$@"
