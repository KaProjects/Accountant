angular.module('Accountant').controller('TransactionWizardCtrl', ['$scope', '$timeout', '$uibModal', '$rootScope','TransactionService',
function ($scope, $timeout, $uibModal, $rootScope, TransactionService) {
    var ctrl = this;

    ctrl.ready = true;
    ctrl.key = "";
    ctrl.value = "";
    ctrl.done = false;



    var finish = function(){
        $rootScope.$emit('wizard.done', 'done');
        return true;
    };

    var cancel = function(){

    };

}]);