#!/usr/bin/env bash
#
# Development loop: watches the sources, compiles on every change, restarts when asked.
#
# Compiling and restarting are deliberately separate. A task usually touches several files
# in a row, and restarting on each save made the window disappear and reappear a handful of
# times for one logical change. So the loop compiles continuously - errors show up within a
# second of a bad save - while the app keeps running until a restart is requested:
#
#     ./dev.sh --restart      apply everything compiled so far, in one restart
#
# The running app is isolated from those compiles: it runs from a snapshot of the classes in
# target/classes-run, so recompiling underneath it cannot leave it with half of one build and
# half of another. The snapshot sits next to target/classes on purpose, because the app finds
# its data directory relative to the classes it was loaded from, and both resolve to
# target/DEVEL-DATA.
#
# Runs against the fakes in src/dev (Maven profile 'dev'), so it needs neither the
# network nor the Firebase service key. Set FIREBASE_MODE=real to talk to the real
# database instead.
#
# The app keeps all of its state in DEVEL-DATA, so a restart is not destructive - the active
# year, accounts and transactions all survive it. That makes restarting a better fit here
# than class hot-swapping.
#
# Usage:  ./dev.sh             - watch and compile, restart on request
#         ./dev.sh --restart   - ask the running loop to restart the app now
#         ./dev.sh --auto      - restart on every change, as it used to
#         ./dev.sh --once      - build and run a single time

set -u

cd "$(dirname "$0")"

RUN_CLASSES=target/classes-run
RESTART_FLAG=target/.dev-restart
LOCK_DIR=target/.dev-lock
PID_FILE=target/.dev-loop.pid
# a file, not a variable: with fswatch the compiles happen in a subshell of their own
PENDING_FILE=target/.dev-pending

JAR_DEPS=$(ls target/*-jar-with-dependencies.jar 2>/dev/null | head -1)
APP_PID=""
WATCH_PID=""
FIREBASE_MODE=${FIREBASE_MODE:-fake}

log() { printf '\033[36m[dev]\033[0m %s\n' "$*"; }
err() { printf '\033[31m[dev]\033[0m %s\n' "$*"; }

# --restart is a second invocation talking to the loop running in another terminal.
if [ "${1:-}" = "--restart" ]; then
    mkdir -p target
    touch "$RESTART_FLAG"
    # the loop writes its pid down, which is the only reliable way to ask "is it running?" -
    # matching the process name would match this very invocation of the script
    if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        log "restart requested"
    else
        log "restart requested - but no dev loop is running to pick it up (start one with ./dev.sh)"
    fi
    exit 0
fi

# The fat jar supplies the dependencies; the class snapshot comes first on the
# classpath so the code being developed always wins over the packaged copy.

# Whether this jar was packaged with the 'dev' profile, which is the only thing that puts
# jackson-databind - the dependency the fakes in src/dev need - into it.
has_dev_deps() {
    unzip -l "$1" 2>/dev/null | grep -q 'com/fasterxml/jackson/databind/ObjectMapper.class'
}

# A jar left behind by build_deploy.sh or by a plain './mvnw package' carries no dev
# dependencies, and its timestamp says nothing about that. Comparing it against pom.xml alone
# let such a jar stay in place whenever pom.xml happened to be the older of the two, and the app
# then died at startup complaining that the dev sources were missing - which they were not. So
# the jar's contents decide here, not only its age.
ensure_deps() {
    if [ -z "$JAR_DEPS" ] || [ pom.xml -nt "$JAR_DEPS" ] || ! has_dev_deps "$JAR_DEPS"; then
        log "packaging (jar missing, outdated, or built without the 'dev' profile) ..."
        ./mvnw -B -q -Pdev package -DskipTests || { err "package failed"; return 1; }
        JAR_DEPS=$(ls target/*-jar-with-dependencies.jar 2>/dev/null | head -1)
    fi
    if [ -z "$JAR_DEPS" ] || ! has_dev_deps "$JAR_DEPS"; then
        err "the packaged jar carries no dev dependencies - the app would fail at startup"
        return 1
    fi
}

# Serialises the watcher's compiles against the restart's snapshot copy.
lock() { while ! mkdir "$LOCK_DIR" 2>/dev/null; do sleep 0.2; done; }
unlock() { rmdir "$LOCK_DIR" 2>/dev/null; }

compile() {
    local output
    if ! output=$(./mvnw -B -q -Pdev compile 2>&1); then
        err "compile failed - the running app is untouched:"
        printf '%s\n' "$output" | grep -E '\.java:|COMPILATION ERROR|symbol:|location:' | head -20
        return 1
    fi
    return 0
}

stop_app() {
    if [ -n "$APP_PID" ] && kill -0 "$APP_PID" 2>/dev/null; then
        kill "$APP_PID" 2>/dev/null
        wait "$APP_PID" 2>/dev/null
    fi
    APP_PID=""
}

start_app() {
    rsync -a --delete target/classes/ "$RUN_CLASSES"/
    java -Dfirebase.mode="$FIREBASE_MODE" ${JAVA_OPTS:-} -cp "$RUN_CLASSES:$JAR_DEPS" org.kaleta.accountant.Initializer &
    APP_PID=$!
    echo 0 > "$PENDING_FILE"
    log "app started (pid $APP_PID, firebase=$FIREBASE_MODE)"
}

compile_only() {
    lock
    if compile; then
        local pending=$(( $(cat "$PENDING_FILE" 2>/dev/null || echo 0) + 1 ))
        echo "$pending" > "$PENDING_FILE"
        log "compiled - $pending change(s) waiting, './dev.sh --restart' to apply them"
    fi
    unlock
}

restart_now() {
    lock
    if compile; then
        stop_app
        start_app
    fi
    unlock
}

# Fingerprint of every source file's modification time.
snapshot() {
    {
        find src/main src/dev -type f \( -name '*.java' -o -name '*.xsd' -o -name '*.json' \) -exec stat -f '%m %N' {} + 2>/dev/null
        stat -f '%m %N' pom.xml 2>/dev/null
    } | sort | shasum | cut -d' ' -f1
}

cleanup() {
    echo
    log "shutting down"
    [ -n "$WATCH_PID" ] && kill "$WATCH_PID" 2>/dev/null
    stop_app
    unlock
    # only if it is still ours: another loop may have taken over since we started
    [ "$(cat "$PID_FILE" 2>/dev/null)" = "$$" ] && rm -f "$PID_FILE"
    exit 0
}
trap cleanup INT TERM

mkdir -p target

# Two loops would mean two app windows and a race for every restart request, and the second
# one is nearly always an accident - one started here, one left running elsewhere.
if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    RUNNING_PID=$(cat "$PID_FILE")
    err "a dev loop is already running (pid $RUNNING_PID) - it would fight this one over the app"
    err "use it instead ('./dev.sh --restart'), or stop it first ('kill $RUNNING_PID')"
    exit 1
fi

ensure_deps || exit 1
rm -f "$RESTART_FLAG"
unlock
echo $$ > "$PID_FILE"

log "initial compile ..."
./mvnw -B -q -Pdev compile || { err "initial compile failed"; exit 1; }
start_app

if [ "${1:-}" = "--once" ]; then
    wait "$APP_PID"
    exit 0
fi

AUTO_RESTART=0
[ "${1:-}" = "--auto" ] && AUTO_RESTART=1

on_change() {
    if [ "$AUTO_RESTART" = "1" ]; then
        log "change detected, compiling and restarting ..."
        restart_now
    else
        log "change detected, compiling ..."
        compile_only
    fi
}

if [ "$AUTO_RESTART" = "1" ]; then
    log "watching src/main and src/dev - every change restarts the app (Ctrl-C to stop)"
else
    log "watching src/main and src/dev - changes are compiled, './dev.sh --restart' applies them (Ctrl-C to stop)"
fi

if command -v fswatch >/dev/null 2>&1; then
    # fswatch compiles in the background; the loop below stays free to watch for restart requests
    fswatch -o src/main src/dev pom.xml | while read -r _; do on_change; done &
    WATCH_PID=$!
    while true; do
        sleep 1
        if [ -f "$RESTART_FLAG" ]; then
            rm -f "$RESTART_FLAG"
            log "restarting ..."
            restart_now
        fi
    done
else
    log "(\`brew install fswatch\` makes compiling instant instead of ~1s polled)"
    LAST=$(snapshot)
    while true; do
        sleep 1
        if [ -f "$RESTART_FLAG" ]; then
            rm -f "$RESTART_FLAG"
            log "restarting ..."
            restart_now
            LAST=$(snapshot)
        fi
        NOW=$(snapshot)
        if [ "$NOW" != "$LAST" ]; then
            on_change
            LAST=$(snapshot)
        fi
    done
fi
