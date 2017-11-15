
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
    
Known bugs:

    Accumulated depreciation accounts are opened wrongly - switched debit/credit 
        
## 1.1
Released: in progress

    before release: 
        pom.xml: x.y-snapshot -> x.y
        Initializer.VERSION: x.y-snapshot -> x.y
        Initializer.CONTEXT = production