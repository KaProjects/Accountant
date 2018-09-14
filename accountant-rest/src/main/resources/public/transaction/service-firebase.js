angular.module('Accountant').service('TransactionService', ['$q', '$resource', function($q,$resource) {
    var service = this;

    var database = firebase.database();

    service.getTransactions = function() {
        console.log();

        return database.ref("transactions/").once('value');
    }

    service.addTransaction = function(keyV, valueV) {
//        console.log("Inserting Transaction: [ " + keyV + " | " + valueV + " ]");
//        return $resource('/data/insert', {key: keyV, value: valueV}).save();

           database.ref('users/').set({
                        username: "name",
                        email: "ema"
                      });


    }

}]);



