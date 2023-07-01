## release workflow:

    before release: 
        pom.xml: x.y-snapshot -> x.y
        Initializer.VERSION: x.y-snapshot -> x.y
        Initializer.CONTEXT = PRODUCTION
    after release:
        create tag
        pom.xml: x.y -> x.y+1-snapshot
        Initializer.VERSION: x.y -> x.y+1-snapshot
        Initializer.CONTEXT = DEVEL

## 1.7
Released: 2.7.2023

    Revolut CSV parser, CSOB CSV parser, improvements for Firebase importer, invalidate models on demand, support for generating monthly bulk transactions

## 1.6
Released: 5.12.2022

    implemented procedure groups, revolut statement parser, minor fixes

## 1.5
Released: 23.10.2022

    pdf parser for repeating transactions, updated config to store mappings

## 1.4.1
Released: 22.10.2022

    Accounting/Profit - divide institution and move non-work-related institutions outside of net working profit
    Schema account names to lowercase

## 1.4
Released: 2.5.2021

    Created accounting chart, refactoring of accounting overviews model

## 1.3
Released: 1.2.2021

    Implemented: importing transactions from firebase database, DnD support for various components, 
                 auxiliary all accounts dialog
    Improved: cashflow overview, finance tab
    Fixed: long-term fin. assets expense semantic accounts bug, account overview transaction list ordering, 

## 1.2
Released: 13.10.2018

    Major reorganization of the project - added web, android projects.
    Improved: account overview - added transaction info, add transaction dialog - now can add procedures and resources,
              reworked balance and profit overview. 
    Implemented: many UI/UX enhancements and bug fixes, created cash flow and assets depreciation overview.

## 1.1
Released: 5.1.2018

    Improved: accounts overview, edit procedure, schema account/group deletion, add resource dialog.
    Implemented: transaction filer, year closing procedure.
    Fixed: major bug, many minor UI/UX fixes

# 1.0
Released: 13.11.2017

    Completely reworked backed, frontend and services. 
    Created overviews and editors for schema and accounts. 
    Updated journal, balance and profit tables according to new backend.
    Reworked procedure, transaction managemet.
    Improved dialogs.

Known major bugs:

    Accumulated depreciation accounts are opened wrongly - switched debit/credit 

# 0.0
Released: 25.12.2016

	Implemented backend of accounting schema, semantic accounts, procedures and journal.
	Implemented frontend
	    - dialogs via menu
	    - journal, balance and profit tables and their UI

    

        








