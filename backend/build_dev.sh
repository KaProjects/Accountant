#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPT_DIR/../deploy" && pwd)"
USE_PROD_DB=0
DEV_DATA_DIR="${DEV_DATA_DIR:-}"
DEV_FRONTEND_ORIGIN="${DEV_FRONTEND_ORIGIN:-http://localhost:3001}"
DEV_HTTP_PORT="${DEV_HTTP_PORT:-9091}"

# Quarkus defaults the debugger to 5005, which is the port Trading's dev stack already uses, so
# running both at once left the second one without a debugger. The host defaults to loopback and
# is only widened inside the development container, where the published port is itself bound to
# the host's loopback.
DEV_DEBUG_PORT="${DEV_DEBUG_PORT:-5006}"
DEV_DEBUG_HOST="${DEV_DEBUG_HOST:-localhost}"

load_env_file() {
  local env_file="$1"
  local line
  local key
  local value

  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    [[ -n "$line" ]] || continue
    [[ "$line" =~ ^[[:space:]]*# ]] && continue
    [[ "$line" == *=* ]] || continue

    key="${line%%=*}"
    value="${line#*=}"
    key="${key#"${key%%[![:space:]]*}"}"
    key="${key%"${key##*[![:space:]]}"}"

    export "$key=$value"
  done < "$env_file"
}

usage() {
  cat >&2 <<'USAGE'
Usage: build_dev.sh [--db-prod] [--data-dir <path>]

  --db-prod           use the production database instead of the in-memory one
  --data-dir <path>   read the accounting data from <path> instead of the sample
                      bundled in src/dev/resources/data

The data directory can also be set once in backend/.env (which is git-ignored)
as DEV_DATA_DIR=<path>, and --data-dir overrides that.
USAGE
  exit 2
}

require_data_dir() {
  local directory="$1"

  [[ -d "$directory" ]] || {
    printf 'No such data directory: %s\n' "$directory" >&2
    return 1
  }
  [[ -f "$directory/config.xml" ]] || {
    printf 'Not a data export, it has no config.xml: %s\n' "$directory" >&2
    return 1
  }
}

# A path set here outranks application-dev.properties, because MicroProfile Config maps the
# DATA_LOCATION variable onto the data.location property and reads environment variables at a
# higher ordinal than a properties file. The real accounting data lives outside this repository
# and must stay there: src/dev/resources/data is committed in the clear.
if [[ -f "$SCRIPT_DIR/.env" ]]; then
  load_env_file "$SCRIPT_DIR/.env"
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --db-prod)
      USE_PROD_DB=1
      shift
      ;;
    --data-dir)
      [[ $# -ge 2 ]] || usage
      DEV_DATA_DIR="$2"
      shift 2
      ;;
    *)
      usage
      ;;
  esac
done

java_is_25() {
  local version

  version="$("$1" -version 2>&1)" || return 1
  grep -Eq 'version "25(\.|")' <<< "$version"
}

activate_java_25() {
  local candidate

  if [[ -n "${JAVA_HOME:-}" ]] \
      && [[ -x "$JAVA_HOME/bin/java" ]] \
      && java_is_25 "$JAVA_HOME/bin/java"; then
    export PATH="$JAVA_HOME/bin:$PATH"
    return 0
  fi

  if command -v java >/dev/null 2>&1 && java_is_25 "$(command -v java)"; then
    return 0
  fi

  for candidate in \
      /Library/Java/JavaVirtualMachines/graalvm-jdk-25*/Contents/Home \
      /Library/Java/JavaVirtualMachines/jdk-25*.jdk/Contents/Home \
      "$HOME"/Library/Java/JavaVirtualMachines/graalvm-jdk-25*/Contents/Home \
      "$HOME"/Library/Java/JavaVirtualMachines/jdk-25*.jdk/Contents/Home; do
    [[ -x "$candidate/bin/java" ]] || continue
    export JAVA_HOME="$candidate"
    export PATH="$JAVA_HOME/bin:$PATH"
    printf 'Using Java 25 from %s.\n' "$JAVA_HOME"
    return 0
  done

  printf 'Java 25 is required. Set JAVA_HOME to a Java 25 installation.\n' >&2
  return 127
}

require_production_config() {
  local variable

  for variable in DB_KIND JDBC_URL DB_USERNAME DB_PASSWORD DATA_LOCATION; do
    if [[ -z "${!variable:-}" ]]; then
      printf 'Missing required --db-prod configuration: %s\n' "$variable" >&2
      return 1
    fi
  done
}

cd "$SCRIPT_DIR"

apply_data_dir() {
  [[ -n "${DEV_DATA_DIR:-}" ]] || return 0
  require_data_dir "$DEV_DATA_DIR" || exit 1
  export DATA_LOCATION="${DEV_DATA_DIR%/}/"
  printf 'Reading accounting data from %s\n' "$DATA_LOCATION"
}

activate_java_25 || exit $?

if [[ $USE_PROD_DB -eq 1 ]]; then
  # Only --db-prod needs the production environment. Loading it for plain dev
  # would clobber the dev profile: MicroProfile Config maps the DATA_LOCATION
  # and ENVIRONMENT variables onto data.location and environment, and
  # environment variables outrank application-dev.properties.
  [[ -f "$DEPLOY_DIR/.env.prod" ]] || {
    printf 'Missing %s, required by --db-prod.\n' "$DEPLOY_DIR/.env.prod" >&2
    exit 1
  }
  load_env_file "$DEPLOY_DIR/.env.prod"
  apply_data_dir
  require_production_config || exit $?
  printf 'WARNING: Development mode is using the production database.\n'
  exec env \
    FRONTEND_ORIGIN="$DEV_FRONTEND_ORIGIN" \
    HTTP_PORT="$DEV_HTTP_PORT" \
    ./mvnw -Pdev-output clean compile quarkus:dev -Ddebug="$DEV_DEBUG_PORT" -DdebugHost="$DEV_DEBUG_HOST"
fi

apply_data_dir

exec ./mvnw -Pdev,dev-output clean compile quarkus:dev -Ddebug="$DEV_DEBUG_PORT" -DdebugHost="$DEV_DEBUG_HOST"
