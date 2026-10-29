#!/usr/bin/env bash
#
# Checks that the native binary answers the way the JVM build does.
#
# A native image keeps only what it was told to keep, and resolves at build time whatever is done
# during static initialisation, so it can fail in ways no JVM test sees: a response type missing
# its reflection registration serialises as an empty object, a filter that reads configuration at
# initialisation bakes in the build machine's value or fails the build. This builds the native
# binary in a container, runs it against MariaDB loaded with the same fixtures the tests use, and
# compares every endpoint pinned by ResponseSnapshotTest with its recorded snapshot, then checks
# that errors are still answered as problem details.
#
# Needs Docker and python3. Everything it starts is removed when it ends. The session it uses is
# signed with a secret made up for the run, which the binary is given and nothing else knows.
#
#   ./native_smoke.sh               build the binary, then check it
#   ./native_smoke.sh --skip-build  check the binary already in target/

set -euo pipefail

BACKEND_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUN_ID="accountant-native-smoke-$$"
WORK_DIR="$(mktemp -d)"
HOST_PORT="${NATIVE_SMOKE_PORT:-18080}"
RUNNER_IMAGE="registry.access.redhat.com/ubi9/ubi-minimal:9.6"

cleanup() {
  docker rm -f "$RUN_ID-app" "$RUN_ID-db" >/dev/null 2>&1 || true
  docker network rm "$RUN_ID" >/dev/null 2>&1 || true
  rm -rf "$WORK_DIR"
}
trap cleanup EXIT

cd "$BACKEND_DIR"

if [[ "${1:-}" != "--skip-build" ]]; then
  ./mvnw -B -q package -Pnative -DskipTests -Dquarkus.native.container-build=true
fi

RUNNER="$(ls target/*-runner 2>/dev/null | head -1)"
if [[ -z "$RUNNER" ]]; then
  printf 'No native binary in target/ - run without --skip-build.\n' >&2
  exit 1
fi

python3 -c 'import secrets; print(secrets.token_hex(24))' > "$WORK_DIR/secret"
python3 -c 'import secrets; print(secrets.token_hex(12))' > "$WORK_DIR/dbpw"
cp sql/createTables.sql "$WORK_DIR/1-tables.sql"
cp src/test/resources/createTestDb.sql "$WORK_DIR/2-fixtures.sql"

docker network create "$RUN_ID" >/dev/null
docker run -d --name "$RUN_ID-db" --network "$RUN_ID" \
  -e MARIADB_ROOT_PASSWORD="$(cat "$WORK_DIR/dbpw")" -e MARIADB_DATABASE=accountant \
  -v "$WORK_DIR/1-tables.sql:/docker-entrypoint-initdb.d/1-tables.sql:ro" \
  -v "$WORK_DIR/2-fixtures.sql:/docker-entrypoint-initdb.d/2-fixtures.sql:ro" \
  mariadb:11 >/dev/null

printf 'Waiting for the database'
for _ in $(seq 1 60); do
  if docker exec "$RUN_ID-db" mariadb -uroot -p"$(cat "$WORK_DIR/dbpw")" accountant \
      -e 'SELECT COUNT(*) FROM Transaction' >/dev/null 2>&1; then
    break
  fi
  printf '.'
  sleep 2
done
printf '\n'

docker run -d --name "$RUN_ID-app" --network "$RUN_ID" -p "$HOST_PORT:8080" \
  -v "$BACKEND_DIR/$RUNNER:/app/runner:ro" --entrypoint /app/runner \
  -e JDBC_URL="jdbc:mariadb://$RUN_ID-db:3306/accountant" -e DB_KIND=mariadb \
  -e DB_USERNAME=root -e DB_PASSWORD="$(cat "$WORK_DIR/dbpw")" \
  -e HTTP_PORT=8080 -e FRONTEND_ORIGIN=http://smoke.test -e DATA_LOCATION=/tmp/ \
  -e AUTH_TOKEN_SECRET="$(cat "$WORK_DIR/secret")" \
  "$RUNNER_IMAGE" -Dquarkus.http.host=0.0.0.0 >/dev/null

for _ in $(seq 1 30); do
  curl -s -o /dev/null "http://localhost:$HOST_PORT/schema/2023" && break
  sleep 1
done

SECRET_FILE="$WORK_DIR/secret" BASE_URL="http://localhost:$HOST_PORT" python3 - <<'PYTHON'
import base64, hashlib, hmac, json, os, re, sys, time, urllib.error, urllib.request

base = os.environ["BASE_URL"]
secret = open(os.environ["SECRET_FILE"]).read().strip()

def b64(raw):
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode()

now = int(time.time())
token = b64(json.dumps({"alg": "HS256", "typ": "JWT"}).encode()) + "." + b64(
    json.dumps({"iss": "accountant", "upn": "native-smoke", "iat": now, "exp": now + 600}).encode())
token += "." + b64(hmac.new(secret.encode(), token.encode(), hashlib.sha256).digest())

def get(path, session=True):
    headers = {"Cookie": "accountant_session=" + token} if session else {}
    try:
        response = urllib.request.urlopen(urllib.request.Request(base + path, headers=headers))
        return response.status, response.headers.get("Content-Type", ""), response.read().decode()
    except urllib.error.HTTPError as error:
        return error.code, error.headers.get("Content-Type", ""), error.read().decode()

failures = []
paths = re.findall(r'^\s*"(/[^"]+)",', open("src/test/java/org/kaleta/rest/ResponseSnapshotTest.java").read(), re.M)
for path in paths:
    status, _, body = get(path)
    snapshot = json.load(open("src/test/resources/snapshots/" + path[1:].replace("/", "_") + ".json"))
    if status != 200 or json.loads(body) != snapshot:
        failures.append(f"{path}: {status} {body[:200]}")
print(f"{len(paths) - len(failures)}/{len(paths)} endpoints answer as the JVM build does")

def expect_problem(path, status, session=True):
    actual, content_type, body = get(path, session)
    problem = json.loads(body) if body.startswith("{") else {}
    if actual != status or "application/problem+json" not in content_type or problem.get("status") != status:
        failures.append(f"{path}: expected a {status} problem, got {actual} {content_type} {body[:200]}")

expect_problem("/schema/2014", 404)
expect_problem("/schema/20x4", 400)
expect_problem("/nothing/here", 404)
expect_problem("/schema/2023", 401, session=False)

for failure in failures:
    print("FAILED " + failure)
sys.exit(1 if failures else 0)
PYTHON
