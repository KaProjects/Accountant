angular.module('Accountant').controller('TransactionCtrl', ['TransactionService', '$filter','$uibModal', '$rootScope',
function (TransactionService, $filter, $uibModal, $rootScope) {
    var ctrl = this;

    ctrl.transactions = [];
    ctrl.allTransactions = [];

    ctrl.loading = false;

    var matchesFilter = function (tr, filter) {
        var match = true;
        if (filter.id === 'id') {
            match = tr.id === parseInt(filter.value);
        } else if (filter.id === 'key') {
            match = tr.key.match(filter.value) !== null;
        } else if (filter.id === 'value') {
            match = tr.value.match(filter.value) !== null;
        }
        return match;
    };

    var matchesFilters = function (tr, filters) {
        var matches = true;
        filters.forEach(function(filter) {
            if (!matchesFilter(tr, filter)) {
                matches = false;
                return false;
            }
        });
        return matches;
    };

    var applyFilters = function (filters) {
        ctrl.transactions = [];
        if (filters && filters.length > 0) {
            ctrl.allTransactions.forEach(function (tr) {
                if (matchesFilters(tr, filters)) {
                    ctrl.transactions.push(tr);
                }
            });
        } else {
            ctrl.transactions = ctrl.allTransactions;
        }
    };

    var filterChange = function (filters) {
        applyFilters(filters);
        ctrl.filterConfig.resultsCount = ctrl.transactions.length;
    };

    var loadAllTransactions = function() {
        ctrl.loading = true;
        TransactionService.getTransactions().$promise.then(function(trs) {
            ctrl.allTransactions = trs;
            filterChange(ctrl.filterConfig.appliedFilters);
            ctrl.loading = false;
        }, function(reason) {
            console.log('Getting All Transactions Failed: ' + reason);
        });
    };

    var addTransactionAction = function(){
              var wizardDoneListener,
                  modalInstance = $uibModal.open({
                    animation: true,
                    backdrop: 'static',
                    templateUrl: 'transaction/wizard/container.html',
                    controller: 'TransactionWizardCtrl as wctrl',
                    size: 'lg'
                  });

              var closeWizard = function (e, reason) {
                modalInstance.dismiss(reason);
                wizardDoneListener();
              };

              modalInstance.result.then(function () { }, function () { });

              wizardDoneListener = $rootScope.$on('wizard.done', closeWizard);

//        ctrl.loading = true;
//        TransactionService.addTransaction("k","v").$promise.then(function() {
//                loadAllTransactions();
//                ctrl.loading = false;
//            }, function(reason) {
//                console.log('Inserting Transaction Failed: ' + reason);
//            });
    };

    ctrl.config = {
        selectionMatchProp: "id",
        itemsAvailable: true,
        showCheckboxes: false
    };

    ctrl.pageConfig = {
        pageNumber: 1,
        pageSize: 50,
        pageSizeIncrements: [50,100,150,200]
    };

    ctrl.columns = [
        { header: "Id", itemField: "id"},
        { header: "Key", itemField: "key"},
        { header: "Value", itemField: "value"}
    ];

    ctrl.emptyStateConfig = {
        icon: 'pficon-warning-triangle-o',
        title: 'No Transactions Available',
    };

    ctrl.emptyStateAction = [
        {
        name: 'Add Transaction',
        title: 'Perform an action',
        actionFn: addTransactionAction,
        type: 'main'
        }
    ];

    var performAction = function(){
        console.log("TODO an action"); // TODO
    };

    ctrl.actionButtons = [];

    ctrl.menuActions = [
        {
            name: 'Action',
            title: 'Perform an action',
            actionFn: performAction
        },
        {
            name: 'Another Action',
            title: 'Do something else',
            actionFn: performAction
        }
    ];

    ctrl.toolbarActionsConfig = {
        primaryActions: [
        {
            name: 'Add Transaction',
            title: 'Opens Add Transaction Dialog',
            actionFn: addTransactionAction
        }
        ],
        moreActions: [],
        actionsInclude: true
    };

    ctrl.filterConfig = {
        fields: [
        {
            id: 'id',
            title:  'Id',
            placeholder: 'Filter by Id...',
            filterType: 'text'
        },
        {
            id: 'key',
            title:  'Key',
            placeholder: 'Filter by Key...',
            filterType: 'text'
        },
        {
            id: 'value',
            title:  'Value',
            placeholder: 'Filter by Value...',
            filterType: 'text'
        }
        ],
        resultsCount: ctrl.transactions.length,
        totalCount: ctrl.allTransactions.length,
        appliedFilters: [],
        onFilterChange: filterChange
    };

    ctrl.toolbarConfig = {
        filterConfig: ctrl.filterConfig,
        actionsConfig: ctrl.toolbarActionsConfig,
        isTableView: true
    };



    (function init() {
       loadAllTransactions();
    })();
}]);
