#!/usr/bin/env bash
set -euo pipefail

# Builds the three local projects and gathers their newest distributable jars.
# Usage:
#   ./collect-latest-builds.sh             # test, build, publish libs, collect
#   ./collect-latest-builds.sh --no-build  # only collect existing newest jars
#   ./collect-latest-builds.sh /some/path  # use a different output directory

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
output_dir="$script_dir/release-mods"
build_projects=true

for argument in "$@"; do
    case "$argument" in
        --no-build) build_projects=false ;;
        --help|-h)
            sed -n '3,9p' "$0"
            exit 0
            ;;
        --*)
            echo "Unknown option: $argument" >&2
            exit 2
            ;;
        *) output_dir="$argument" ;;
    esac
done

run_gradle() {
    local project_dir="$1"
    shift
    echo "==> Building ${project_dir#"$script_dir/"}"
    (cd "$project_dir" && ./gradlew --no-configuration-cache "$@")
}

if "$build_projects"; then
    # Publish API libraries first so Chemworks always compiles against the
    # exact Tetra and mutil jars included in this handoff.
    run_gradle "$script_dir/tetra stuff/mutil" clean test build publishToMavenLocal
    run_gradle "$script_dir/tetra stuff/tetra" clean test build publishToMavenLocal
    run_gradle "$script_dir/chemworks mod" clean test build
fi

newest_binary_jar() {
    local libs_dir="$1"
    local newest=""
    local candidate
    while IFS= read -r -d '' candidate; do
        case "$candidate" in
            *-sources.jar|*-javadoc.jar|*-dev.jar|*-slim.jar) continue ;;
        esac
        if [[ -z "$newest" || "$candidate" -nt "$newest" ]]; then
            newest="$candidate"
        fi
    done < <(find "$libs_dir" -maxdepth 1 -type f -name '*.jar' -print0)
    if [[ -z "$newest" ]]; then
        echo "No distributable jar found in $libs_dir" >&2
        return 1
    fi
    printf '%s\n' "$newest"
}

mkdir -p "$output_dir"

mutil_jar="$(newest_binary_jar "$script_dir/tetra stuff/mutil/build/libs")"
tetra_jar="$(newest_binary_jar "$script_dir/tetra stuff/tetra/build/libs")"
chemworks_jar="$(newest_binary_jar "$script_dir/chemworks mod/build/libs")"

# Remove only jars previously generated in this dedicated handoff directory.
find "$output_dir" -maxdepth 1 -type f -name '*.jar' -delete
cp "$mutil_jar" "$tetra_jar" "$chemworks_jar" "$output_dir/"

(
    cd "$output_dir"
    shasum -a 256 ./*.jar > SHA256SUMS
)

echo
echo "Latest developer test bundle: $output_dir"
find "$output_dir" -maxdepth 1 -type f \( -name '*.jar' -o -name 'SHA256SUMS' \) -print | sort
