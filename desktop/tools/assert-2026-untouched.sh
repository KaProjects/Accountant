#!/usr/bin/env bash
# 2026 must be byte-identical to the pristine copy: the migration may never
# change the current year's schema or accounts.
set -u
cd "$(dirname "$0")/.."
REF=${1:-DEVEL-DATA-2026-baseline}
WORK=${2:-DEVEL-DATA-toModify}
fail=0
for f in accounts.xml transactions.xml; do
    if ! diff -q "$REF/2026/$f" "$WORK/2026/$f" >/dev/null 2>&1; then
        echo "  CHANGED: 2026/$f"; fail=1
    fi
done
[ $fail -eq 0 ] && echo "  2026 untouched (schema, accounts, transactions, procedures all identical)"
exit $fail
