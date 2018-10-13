
# 0.0
Released: 25.12.2016

	Implemented backend of accounting schema, semantic accounts, procedures and journal.
	Implemented frontend
	    - dialogs via menu
	    - journal, balance and profit tables and their UI

# 1.0
Released: 13.11.2017

    Completely reworked backed, frontend and services. 
    Created overviews and editors for schema and accounts. 
    Updated journal, balance and profit tables according to new backend.
    Reworked procedure, transaction managemet.
    Improved dialogs.
    
Known major bugs:

    Accumulated depreciation accounts are opened wrongly - switched debit/credit 
        
## 1.1
Released: 5.1.2018

    Improved: accounts overview, edit procedure, schema account/group deletion, add resource dialog.
    Implemented: transaction filer, year closing procedure.
    Fixed: major bug, many minor UI/UX fixes
        
## 1.2
Released: 13.10.2018

    Major reorganization of the project - added web, android projects.
    Improved: account overview - added transaction info, add transaction dialog - now can add procedures and resources,
              reworked balance and profit overview. 
    Implemented: many UI/UX enhancements and bug fixes, created cash flow and assets depreciation overview.
        
## 1.3
Released: in progress

    before release: 
        pom.xml: x.y-snapshot -> x.y
        Initializer.VERSION: x.y-snapshot -> x.y
        Initializer.CONTEXT = production