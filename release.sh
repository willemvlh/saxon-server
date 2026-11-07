#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ -n "$(git status --porcelain)" ]]; then
    echo "Error: git working tree is not clean. Commit or stash your changes first." >&2
    exit 1
fi

# The project version is the first <version> element in pom.xml (there is no <parent>)
version=$(grep -m1 -oP '<version>\K[^<]+' pom.xml)
if [[ ! "$version" =~ ^([0-9]+)\.([0-9]+)$ ]]; then
    echo "Error: unexpected version format '$version' (expected MAJOR.MINOR)" >&2
    exit 1
fi
major=${BASH_REMATCH[1]}
minor=${BASH_REMATCH[2]}

tag="v$version"
if git rev-parse -q --verify "refs/tags/$tag" >/dev/null; then
    echo "Error: tag $tag already exists." >&2
    exit 1
fi

echo "Tagging current commit as $tag"
git tag "$tag"
git push origin "$tag"

new_version="$major.$((minor + 1))"
echo "Bumping version to $new_version"
sed -i "0,/<version>$version<\/version>/s//<version>$new_version<\/version>/" pom.xml

git add pom.xml
git commit -m "Bump version to $new_version"
