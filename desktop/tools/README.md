# tools

Checks for an Accountant data directory - a folder holding `config.xml`, `schema.xml`,
`procedures.xml` and one sub-folder per year with `accounts.xml` and `transactions.xml`.

## Verifying data

This is the one to run:

```bash
./tools/verify-data.py <data-dir>
```

It reports every problem it finds rather than stopping at the first, and exits non-zero when
there is at least one error. Warnings alone do not fail it.

```bash
./tools/verify-data.py <data-dir> --diff <other-dir>   # also compare against another data set
./tools/verify-data.py <data-dir> --quiet              # summary only
```

Checks 1-5 are a port of the backend's `/sync/all/validate` endpoint (`SyncServiceImpl.validate`
on the web branch), so both agree on what "valid" means; unlike the endpoint this needs no
database. Checks 6-13 go further and cover what the endpoint cannot see: schema accounts
referenced by an account that does not exist in that year, ids that change meaning between years,
per-account balances in closed years, derived accounts that no longer match their parent, and
`config.xml` import mappings pointing at ids that are gone.

The XSDs it validates against come from `src/main/resources/schema`, so the verifier and the app
cannot drift apart.

## Comparing a year against a reference copy

```bash
./tools/assert-2026-untouched.sh [reference-dir] [working-dir]
```

Fails if `accounts.xml` or `transactions.xml` of the current year differ between the two data
sets. It exists for changes that are supposed to leave the current year alone - a rewrite of the
historical years must not touch the year still being booked into.

## migrations/

One-off scripts that rewrote the 2015-2026 data onto a single shared schema. **Not committed**:
they hardcode real account names, institutions and amounts as their migration tables, which is
this repository's owner's personal financial data. `.gitignore` excludes the whole folder, and it
has never been committed - keep it that way.

They are kept only as a record of what was done. The migration they performed is finished, they
are not idempotent, and running one against current data would corrupt it.
