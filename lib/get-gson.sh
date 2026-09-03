#!/usr/bin/env bash
#
# Fetch the one dependency the examples need.
#
#   ./lib/get-gson.sh
#
# Java has no JSON parser in its standard library, which is why these examples
# need Gson while the examples for every other language need nothing. One jar,
# no build tool, and lib/ is gitignored so the jar never lands in the history.
#
# In a real project you would declare the dependency in Maven or Gradle instead:
#
#   <dependency>
#     <groupId>com.google.code.gson</groupId>
#     <artifactId>gson</artifactId>
#     <version>2.11.0</version>
#   </dependency>

set -euo pipefail

VERSION="${GSON_VERSION:-2.11.0}"
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET="$DIR/gson.jar"
URL="https://repo1.maven.org/maven2/com/google/code/gson/gson/$VERSION/gson-$VERSION.jar"

if [ -f "$TARGET" ]; then
	echo "get-gson: $TARGET is already here, delete it to fetch again"
	exit 0
fi

echo "get-gson: fetching gson $VERSION"
curl -fsSL "$URL" -o "$TARGET"

echo "get-gson: saved to $TARGET"
echo "get-gson: run an example with  java -cp lib/gson.jar examples/v3/account-info.java"
