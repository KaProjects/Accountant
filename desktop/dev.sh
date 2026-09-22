#!/usr/bin/env bash
#
# Development loop: watches the sources and restarts the app on every change.
#
# Runs against the fakes in src/dev (Maven profile 'dev'), so it needs neither the
# network nor the Firebase service key. Set FIREBASE_MODE=real to talk to the real
# database instead.
#
# The app keeps all of its state in DEVEL-DATA next to the classes, so a restart
# is not destructive - the active year, accounts and transactions all survive it.
# That makes restart-on-change a better fit here than class hot-swapping.
#
# Usage:  ./dev.sh          - watch and restart
#         ./dev.sh --once   - build and run a single time

set -u

cd "$(dirname "$0")"

JAR_DEPS=$(ls target/*-jar-with-dependencies.jar 2>/dev/null | head -1)
APP_PID=""
FIREBASE_MODE=${FIREBASE_MODE:-fake}

log() { printf '\033[36m[dev]\033[0m %s\n' "$*"; }
err() { printf '\033[31m[dev]\033[0m %s\n' "$*"; }

# The fat jar supplies the dependencies; target/classes comes first on the
# classpath so freshly compiled code always wins over the packaged copy.
ensure_deps() {
    if [ -z "$JAR_DEPS" ] || [ pom.xml -nt "$JAR_DEPS" ]; then
        log "packaging (dependencies changed or jar missing) ..."
        ./mvnw -B -q -Pdev package -DskipTests || { err "package failed"; return 1; }
        JAR_DEPS=$(ls target/*-jar-with-dependencies.jar 2>/dev/null | head -1)
    fi
    [ -n "$JAR_DEPS" ]
}

stop_app() {
    if [ -n "$APP_PID" ] && kill -0 "$APP_PID" 2>/dev/null; then
        kill "$APP_PID" 2>/dev/null
        wait "$APP_PID" 2>/dev/null
    fi
    APP_PID=""
}

start_app() {
    java -Dfirebase.mode="$FIREBASE_MODE" ${JAVA_OPTS:-} -cp "target/classes:$JAR_DEPS" org.kaleta.accountant.Initializer &
    APP_PID=$!
    log "app started (pid $APP_PID, firebase=$FIREBASE_MODE)"
}

rebuild_and_restart() {
    log "change detected, compiling ..."
    local output
    if ! output=$(./mvnw -B -q -Pdev compile 2>&1); then
        err "compile failed - keeping the running app alive:"
        printf '%s\n' "$output" | grep -E '\.java:|COMPILATION ERROR|symbol:|location:' | head -20
        return 1
    fi
    stop_app
    start_app
}

# Fingerprint of every source file's modification time.
snapshot() {
    {
        find src/main src/dev -type f \( -name '*.java' -o -name '*.xsd' -o -name '*.json' \) -exec stat -f '%m %N' {} + 2>/dev/null
        stat -f '%m %N' pom.xml 2>/dev/null
    } | sort | shasum | cut -d' ' -f1
}

trap 'echo; log "shutting down"; stop_app; exit 0' INT TERM

ensure_deps || exit 1

log "initial compile ..."
./mvnw -B -q -Pdev compile || { err "initial compile failed"; exit 1; }
start_app

if [ "${1:-}" = "--once" ]; then
    wait "$APP_PID"
    exit 0
fi

if command -v fswatch >/dev/null 2>&1; then
    log "watching src/main and src/dev with fswatch - edit a file, the app restarts (Ctrl-C to stop)"
    fswatch -o src/main src/dev pom.xml | while read -r _; do
        rebuild_and_restart
    done
else
    log "watching src/main and src/dev by polling - edit a file, the app restarts (Ctrl-C to stop)"
    log "(\`brew install fswatch\` makes this instant instead of ~1s polled)"
    LAST=$(snapshot)
    while true; do
        sleep 1
        NOW=$(snapshot)
        if [ "$NOW" != "$LAST" ]; then
            LAST=$NOW
            rebuild_and_restart
            LAST=$(snapshot)
        fi
    done
fi
