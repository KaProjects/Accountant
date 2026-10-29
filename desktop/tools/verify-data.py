#!/usr/bin/env python3
"""
Verifies the integrity of an Accountant data directory.

Checks 1-5 are a port of the backend's /sync/all/validate endpoint (web branch,
SyncServiceImpl.validate), so both agree on what "valid" means. Checks 6-12 are
additions aimed at the account-id and schema migrations.

Unlike the endpoint, this needs no database and reports every problem instead of
stopping at the first one.

    ./verify-data.py <data-dir>                    verify one data set
    ./verify-data.py <data-dir> --diff <other>     also compare against another
    ./verify-data.py <data-dir> --quiet            only the summary

Exit code is 0 when there are no errors (warnings alone do not fail).
"""

import argparse
import collections
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET

XSD_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'schema')

# Mirrors org.kaleta.accountant.common.Constants.Schema
ACCUMULATED_DEP_GROUP = '9'
DEPRECIATION_GROUP = '0'
CONSUMPTION_GROUP = '1'

# Backend column widths (backend/sql/createTables.sql) - advisory only for now
MAX_SEMANTIC_ID = 5
MAX_FULL_ID = 9


class Report:
    def __init__(self):
        self.errors = []
        self.warnings = []

    def error(self, check, message):
        self.errors.append((check, message))

    def warn(self, check, message):
        self.warnings.append((check, message))


class Year:
    def __init__(self, path, name):
        self.name = name
        self.accounts = []          # (schemaId, semanticId, name, metadata)
        self.transactions = []      # (id, date, description, amount, debit, credit)
        self.schema = {}            # 3-digit id -> (name, type)
        self.groups = set()         # 2-digit ids
        self.classes = set()        # 1-digit ids
        self.class_count = 0

        for a in ET.parse(f'{path}/{name}/accounts.xml').getroot().findall('account'):
            self.accounts.append((a.get('schemaId'), a.get('semanticId'), a.get('name'), a.get('metadata') or ''))
        for t in ET.parse(f'{path}/{name}/transactions.xml').getroot().findall('transaction'):
            self.transactions.append((t.get('id'), t.get('date'), t.get('description'),
                                      t.get('amount'), t.get('debit'), t.get('credit')))
        root = ET.parse(f'{path}/schema.xml').getroot()
        for c in root.findall('class'):
            self.class_count += 1
            self.classes.add(c.get('id'))
            for g in c.findall('group'):
                self.groups.add(c.get('id') + g.get('id'))
                for acc in g.findall('account'):
                    self.schema[c.get('id') + g.get('id') + acc.get('id')] = (acc.get('name'), acc.get('type') or '')

    @property
    def account_ids(self):
        return {f'{s}.{m}' for s, m, _, _ in self.accounts}

    def by_type(self):
        out = collections.defaultdict(set)
        for sid, (_, typ) in self.schema.items():
            out[typ].add(sid)
        return out


def load(path):
    config = ET.parse(f'{path}/config.xml').getroot()
    years_el = config.find('years')
    listed = [y.get('name') for y in years_el.findall('year')]
    active = years_el.get('active')
    mappings = [(m.get('substring'), m.get('account')) for m in config.find('mapping').findall('debit')]
    return listed, active, mappings


# ---------------------------------------------------------------- checks 1-5
# ports of SyncServiceImpl.validate

def check_transactions(year, rep):
    """1. transactions: valid date, debit and credit accounts exist"""
    ids = year.account_ids
    for tid, date, _, amount, debit, credit in year.transactions:
        if not (date and len(date) == 4 and date.isdigit()):
            rep.error('1-transactions', f'{year.name} transaction {tid}: invalid date {date!r}')
        else:
            day, month = int(date[:2]), int(date[2:])
            if not (1 <= month <= 12) or not (0 <= day <= 31):
                rep.error('1-transactions', f'{year.name} transaction {tid}: impossible date {date!r}')
        if amount is None or not re.fullmatch(r'-?\d+', amount or ''):
            rep.error('1-transactions', f'{year.name} transaction {tid}: non-integer amount {amount!r}')
        if debit not in ids:
            rep.error('1-transactions', f'{year.name} transaction {tid}: debit account not found: {debit}')
        if credit not in ids:
            rep.error('1-transactions', f'{year.name} transaction {tid}: credit account not found: {credit}')


def check_accounts_have_schema(year, rep):
    """2. accounts: the schema account they hang off exists"""
    for sid, sem, _, _ in year.accounts:
        if sid not in year.schema:
            rep.error('2-accounts', f'{year.name} account {sid}.{sem}: schema not found')


def check_schema(year, rep):
    """3. schema: class and group exist for every account, type is set"""
    if year.class_count != 8:
        rep.error('3-schema', f'{year.name}: expected 8 classes, found {year.class_count}')
    for sid, (name, typ) in sorted(year.schema.items()):
        if not typ:
            rep.error('3-schema', f'{year.name} schema {sid}: should have type assigned')
        if sid[0] not in year.classes:
            rep.error('3-schema', f'{year.name} schema {sid}: could not find schema class')
        if sid[:2] not in year.groups:
            rep.error('3-schema', f'{year.name} schema {sid}: could not find schema group')
        if not name:
            rep.error('3-schema', f'{year.name} schema {sid}: empty name')


def check_closed_year_balances(year, rep):
    """4. inactive years: every account has debit sum == credit sum"""
    debit = collections.Counter()
    credit = collections.Counter()
    for _, _, _, amount, d, c in year.transactions:
        try:
            value = int(amount)
        except (TypeError, ValueError):
            continue
        debit[d] += value
        credit[c] += value
    for acc in sorted(year.account_ids):
        if debit[acc] != credit[acc]:
            rep.error('4-closed-balance',
                      f"{year.name} account {acc}: debit='{debit[acc]}' != credit='{credit[acc]}'")


def check_active_year_balance(year, rep):
    """5. active year: assets == liabilities + revenues - expenses"""
    by_type = year.by_type()
    total = {'A': 0, 'L': 0, 'E': 0, 'R': 0}
    for _, _, _, amount, d, c in year.transactions:
        try:
            value = int(amount)
        except (TypeError, ValueError):
            continue
        for typ, sign in (('A', 1), ('L', -1), ('E', 1), ('R', -1)):
            if d[:3] in by_type[typ]:
                total[typ] += sign * value
            if c[:3] in by_type[typ]:
                total[typ] -= sign * value
    if total['A'] != total['L'] + total['R'] - total['E']:
        rep.error('5-active-balance',
                  f"{year.name}: assets='{total['A']}' != liabilities='{total['L']}'"
                  f" + revenues='{total['R']}' - expenses='{total['E']}'")


# ---------------------------------------------------------------- checks 6-12
# additions

def check_xsd(path, years, rep):
    """6. every file validates against its XSD"""
    if not shutil.which('xmllint'):
        rep.warn('6-xsd', 'xmllint not installed, skipped XSD validation')
        return
    files = [('config.xml', 'config.xsd'), ('schema.xml', 'schema.xsd'), ('procedures.xml', 'procedures.xsd')]
    for y in years:
        for base in ('accounts', 'transactions'):
            files.append((f'{y}/{base}.xml', f'{base}.xsd'))
    for rel, xsd in files:
        full = os.path.join(path, rel)
        if not os.path.exists(full):
            rep.error('6-xsd', f'missing file: {rel}')
            continue
        proc = subprocess.run(['xmllint', '--noout', '--schema', os.path.join(XSD_DIR, xsd), full],
                              capture_output=True, text=True)
        if proc.returncode != 0:
            rep.error('6-xsd', f'{rel} does not validate: {proc.stderr.strip().splitlines()[-1]}')


def check_layout(path, listed, active, rep):
    """7. config years and year folders agree"""
    on_disk = {d for d in os.listdir(path) if d.isdigit() and os.path.isdir(os.path.join(path, d))}
    for y in listed:
        if y not in on_disk:
            rep.error('7-layout', f'year {y} listed in config but has no folder')
    for y in sorted(on_disk - set(listed)):
        rep.warn('7-layout', f'folder {y} exists but is not listed in config')
    if active not in listed:
        rep.error('7-layout', f'active year {active} is not in the config year list')


def check_duplicate_accounts(year, rep):
    """8. no account id defined twice in a year"""
    seen = collections.Counter(f'{s}.{m}' for s, m, _, _ in year.accounts)
    for acc, n in seen.items():
        if n > 1:
            rep.error('8-duplicates', f'{year.name} account {acc}: defined {n} times')


def derived_of(schema_id, semantic_id):
    """The derived account ids the app composes for this account (AccountsService)."""
    group, account = schema_id[1], schema_id[2]
    out = []
    if schema_id.startswith('0') and not schema_id.startswith('09'):
        out.append(('accumulated depreciation', f'0{ACCUMULATED_DEP_GROUP}{group}.{account}-{semantic_id}'))
        out.append(('depreciation', f'5{DEPRECIATION_GROUP}{group}.{account}-{semantic_id}'))
    if schema_id.startswith('1'):
        out.append(('consumption', f'5{CONSUMPTION_GROUP}{group}.{account}-{semantic_id}'))
    return out


def parent_of(schema_id, semantic_id):
    """Inverse of derived_of: the account a mirror was composed from, or None."""
    if '-' not in semantic_id:
        return None
    account, _, parent_semantic = semantic_id.partition('-')
    group = schema_id[2]
    if schema_id[:2] == f'0{ACCUMULATED_DEP_GROUP}' or schema_id[:2] == f'5{DEPRECIATION_GROUP}':
        return f'0{group}{account}.{parent_semantic}'
    if schema_id[:2] == f'5{CONSUMPTION_GROUP}':
        return f'1{group}{account}.{parent_semantic}'
    return None


def check_derived_accounts(year, rep):
    """9. derived accounts line up with their parents"""
    ids = year.account_ids
    expected = {}
    for sid, sem, _, _ in year.accounts:
        for kind, derived in derived_of(sid, sem):
            expected[derived] = (kind, f'{sid}.{sem}')
            if derived not in ids:
                rep.warn('9-derived', f'{year.name} {sid}.{sem}: no {kind} account ({derived})')
    # the serious direction: a mirror whose parent is gone is what a bad migration leaves behind
    derived_prefixes = (f'0{ACCUMULATED_DEP_GROUP}', f'5{DEPRECIATION_GROUP}', f'5{CONSUMPTION_GROUP}')
    for sid, sem, _, _ in year.accounts:
        full = f'{sid}.{sem}'
        if sid[:2] in derived_prefixes and '-' in sem and full not in expected:
            rep.error('9-derived', f'{year.name} {full}: derived account with no parent account')


def check_id_formats(years, rep):
    """10. ids are shaped the way the app and the backend expect"""
    for year in years:
        for sid, sem, _, _ in year.accounts:
            full = f'{sid}.{sem}'
            if not re.fullmatch(r'\d{3}', sid):
                rep.error('10-id-format', f'{year.name} {full}: schema id is not 3 digits')
            if not re.fullmatch(r'\d+(-\d+)*', sem):
                rep.error('10-id-format', f'{year.name} {full}: semantic id {sem!r} is not digits separated by dashes')
            if len(sem) > MAX_SEMANTIC_ID:
                rep.warn('10-id-format', f'{year.name} {full}: semantic id is {len(sem)} chars, backend column holds {MAX_SEMANTIC_ID}')
            if len(full) > MAX_FULL_ID:
                rep.warn('10-id-format', f'{year.name} {full}: full id is {len(full)} chars, backend column holds {MAX_FULL_ID}')


def check_one_meaning_per_id(years, rep):
    """11. an account id means the same thing in every year it appears"""
    names = collections.defaultdict(dict)
    for year in years:
        for sid, sem, name, _ in year.accounts:
            names[f'{sid}.{sem}'][year.name] = name
    for acc, per_year in sorted(names.items()):
        distinct = {normalise(n) for n in per_year.values()}
        if len(distinct) > 1:
            spans = ', '.join(f'{y}:{n}' for y, n in sorted(per_year.items()))
            rep.error('11-one-meaning', f'{acc}: {len(distinct)} different names across years -> {spans}')


def check_mappings(mappings, years, rep, active):
    """12. config.xml import mappings point at accounts that exist in the active year"""
    active_years = [y for y in years if y.name == active] or years[-1:]
    target = active_years[0]
    for substring, account in mappings:
        if account not in target.account_ids:
            rep.warn('12-mappings', f'import mapping {substring!r} -> {account}: no such account in {target.name}')


def check_year_continuity(years, rep):
    """13. each year's closing balance is carried into the next year's opening balance

    The strongest check available: it ties consecutive years together, so money that
    moves between accounts during a migration cannot hide.
    """
    def closing(year):
        bal = collections.Counter()
        for _, _, description, amount, d, c in year.transactions:
            if description == 'closure':
                continue
            try:
                value = int(amount)
            except (TypeError, ValueError):
                continue
            bal[d] += value
            bal[c] -= value
        return bal

    def opening(year):
        bal = collections.Counter()
        for _, _, description, amount, d, c in year.transactions:
            if description != 'initiation':
                continue
            try:
                value = int(amount)
            except (TypeError, ValueError):
                continue
            bal[d] += value
            bal[c] -= value
        return bal

    def skip(acc):
        return acc.startswith(('700', '701', '710'))

    for previous, following in zip(years, years[1:]):
        before, after = closing(previous), opening(following)

        # Where an account's balance reappears under a different id, the two mismatches are
        # one renumbering. Matching on name lets the report say so instead of reporting two
        # unrelated-looking holes.
        moved_to = {}
        names_before = {normalise(n): f'{s}.{m}' for s, m, n, _ in previous.accounts}
        for sid, sem, name, _ in following.accounts:
            key = normalise(name)
            old_id = names_before.get(key)
            new_id = f'{sid}.{sem}'
            if old_id and old_id != new_id and before.get(old_id, 0) == after.get(new_id, 0) != 0:
                moved_to[old_id] = new_id

        for acc in sorted(after):
            if skip(acc) or acc not in previous.account_ids:
                continue
            if before[acc] != after[acc]:
                note = ''
                if acc in moved_to:
                    note = f' -- same name now at {moved_to[acc]}, looks like a renumbering'
                rep.error('13-continuity',
                          f'{acc}: {previous.name} closing balance {before[acc]} '
                          f'!= {following.name} opening balance {after[acc]}{note}')

        # An account that simply vanishes carrying a balance is invisible to the loop above,
        # because it has no opening balance in the following year to iterate over.
        # Only balance-sheet accounts (classes 0-4) carry forward at all: expenses and
        # revenues are closed into profit every year, so a balance they do not carry is normal.
        for acc in sorted(previous.account_ids):
            if skip(acc) or acc[0] not in '01234' or before.get(acc, 0) == 0 or acc in after:
                continue
            if acc in moved_to:
                rep.error('13-continuity',
                          f'{acc}: {previous.name} closing balance {before[acc]} is not carried into '
                          f'{following.name} -- same name now at {moved_to[acc]}, looks like a renumbering')
                continue
            # A fully depreciated asset retired together with its accumulated-depreciation
            # mirror nets to zero, so dropping both is correct bookkeeping, not a lost balance.
            schema_id, _, semantic_id = acc.partition('.')
            retired_with_mirror = False
            for _, mirror in derived_of(schema_id, semantic_id):
                if mirror not in following.account_ids and before.get(acc, 0) + before.get(mirror, 0) == 0:
                    retired_with_mirror = True
            # the same applies seen from the mirror's side
            parent = parent_of(schema_id, semantic_id)
            if parent and parent not in following.account_ids and before.get(acc, 0) + before.get(parent, 0) == 0:
                retired_with_mirror = True
            if retired_with_mirror:
                rep.warn('13-continuity',
                         f'{acc}: {previous.name} balance {before[acc]} dropped in {following.name} together '
                         f'with its paired asset/depreciation account (net book value 0) - looks like a retired asset')
            else:
                rep.error('13-continuity',
                          f'{acc}: {previous.name} closing balance {before[acc]} '
                          f'is not carried into {following.name} -- no successor found')


def normalise(s):
    import unicodedata
    s = unicodedata.normalize('NFKD', s or '')
    s = ''.join(c for c in s if not unicodedata.combining(c))
    return ' '.join(s.lower().split())


# ---------------------------------------------------------------- diff mode

def diff(path_a, path_b, rep):
    """Money and counts must be identical; only ids and names may move."""
    listed_a, _, _ = load(path_a)
    listed_b, _, _ = load(path_b)
    if listed_a != listed_b:
        rep.error('diff', f'year lists differ: {listed_a} vs {listed_b}')
        return
    for name in listed_a:
        ya, yb = Year(path_a, name), Year(path_b, name)
        if len(ya.transactions) != len(yb.transactions):
            rep.error('diff', f'{name}: transaction count {len(ya.transactions)} -> {len(yb.transactions)}')
        if len(ya.accounts) != len(yb.accounts):
            rep.error('diff', f'{name}: account count {len(ya.accounts)} -> {len(yb.accounts)}')
        sa = sum(int(t[3]) for t in ya.transactions if re.fullmatch(r'-?\d+', t[3] or ''))
        sb = sum(int(t[3]) for t in yb.transactions if re.fullmatch(r'-?\d+', t[3] or ''))
        if sa != sb:
            rep.error('diff', f'{name}: total amount {sa} -> {sb}')
        # per schema class, money must be untouched even if ids moved
        for cls in '01234567':
            ta = sum(int(t[3]) for t in ya.transactions if t[4].startswith(cls) or t[5].startswith(cls))
            tb = sum(int(t[3]) for t in yb.transactions if t[4].startswith(cls) or t[5].startswith(cls))
            if ta != tb:
                rep.error('diff', f'{name}: class {cls} total {ta} -> {tb}')
        moved = ya.account_ids ^ yb.account_ids
        if moved:
            print(f'    {name}: {len(moved)} account ids changed')


# ---------------------------------------------------------------- main

def main():
    ap = argparse.ArgumentParser(description='Verify an Accountant data directory.')
    ap.add_argument('data_dir')
    ap.add_argument('--diff', metavar='OTHER_DIR', help='compare totals against another data directory')
    ap.add_argument('--quiet', action='store_true', help='print only the summary')
    ap.add_argument('--max-per-check', type=int, default=10, help='how many messages to print per check')
    args = ap.parse_args()

    path = args.data_dir.rstrip('/')
    rep = Report()
    try:
        listed, active, mappings = load(path)
    except (OSError, ET.ParseError) as e:
        print(f'data    : {path}')
        print(f'\nERRORS\n  [0-config] 1\n      cannot read config.xml: {e}')
        print('\nsummary : 1 errors, 0 warnings')
        return 1
    print(f'data    : {path}')
    print(f'years   : {len(listed)} ({listed[0]}..{listed[-1]}), active {active}')

    check_layout(path, listed, active, rep)
    check_xsd(path, listed, rep)

    years = []
    for name in listed:
        try:
            year = Year(path, name)
        except (OSError, ET.ParseError) as e:
            rep.error('0-unreadable', f'year {name}: cannot read data files: {e}')
            continue
        years.append(year)
        check_transactions(year, rep)
        check_accounts_have_schema(year, rep)
        check_schema(year, rep)
        check_duplicate_accounts(year, rep)
        check_derived_accounts(year, rep)
        if name == active:
            check_active_year_balance(year, rep)
        else:
            check_closed_year_balances(year, rep)


    if years:
        check_id_formats(years, rep)
        check_one_meaning_per_id(years, rep)
        check_year_continuity(years, rep)
        check_mappings(mappings, years, rep, active)

    accounts = sum(len(y.accounts) for y in years)
    transactions = sum(len(y.transactions) for y in years)
    print(f'totals  : {accounts} accounts, {transactions} transactions')

    if args.diff:
        print(f'\ndiff against {args.diff}:')
        diff(path, args.diff.rstrip('/'), rep)

    for label, items in (('ERROR', rep.errors), ('WARN', rep.warnings)):
        if not items or args.quiet:
            continue
        print(f'\n{label}S')
        by_check = collections.defaultdict(list)
        for check, message in items:
            by_check[check].append(message)
        for check in sorted(by_check):
            messages = by_check[check]
            print(f'  [{check}] {len(messages)}')
            for message in messages[:args.max_per_check]:
                print(f'      {message}')
            if len(messages) > args.max_per_check:
                print(f'      ... and {len(messages) - args.max_per_check} more')

    print(f'\nsummary : {len(rep.errors)} errors, {len(rep.warnings)} warnings')
    return 1 if rep.errors else 0


if __name__ == '__main__':
    sys.exit(main())
