# Personal Accountant

Personal finance kept as real double-entry bookkeeping. The repository holds four
modules; `backend` and `frontend` are the web application, `desktop` is the Swing
data-entry app that exports the sync XML, and `android` is a small companion app.

## Web development

Run the complete local quality suite from this directory:

```sh
./verify.sh
```

Run the backend and frontend in separate terminals:

```sh
(cd backend && ./build_dev.sh)
(cd frontend && ./build_dev.sh)
```

The frontend is served on http://localhost:3001 and the backend on
http://localhost:9091, with the remote debug port on 5006. These are offset by
one from the Trading web project so both stacks can run at the same time.

Plain development mode activates the backend Maven `dev` profile: an in-memory H2
database and the sample export in `backend/src/dev/resources/data/`, which is
synced automatically on every start. It comes from the test fixtures, so the
years are uneven - 2020 is the only one with a complete eight-class schema and is
the active year; the accounting views need that full schema.

## How the frontend reaches the backend

The React app calls a same-origin `/api` prefix; in development `setupProxy.js`
forwards it to the backend. No host or port is baked into the bundle. Swagger UI
is the one exception - it is served by the backend at its own `/api/docs` path and
is linked directly on the backend port.
