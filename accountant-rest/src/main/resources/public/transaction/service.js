angular.module('Accountant').service('TransactionService', ['$q', '$resource', function($q,$resource) {
    var service = this;

    service.getTransactions = function() {
        return $resource('/data/all').query();
    }

    service.addTransaction = function(keyV, valueV) {
        console.log("Inserting Transaction: [ " + keyV + " | " + valueV + " ]");
        return $resource('/data/insert', {key: keyV, value: valueV}).save();
    }

}]);
