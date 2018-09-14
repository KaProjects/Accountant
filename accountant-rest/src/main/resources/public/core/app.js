var rootModule = angular.module('Tester', ['ngResource']);
//rootModule.factory('AccountantHelper', function() {});

rootModule.service('RestService', function($resource) {
    var service = this;

    service.getAllRecords = function() {
        return $resource('/data/all').query();
    };

    service.insertRecord = function(keyV, valueV) {
        console.log("Inserting: [ " + keyV + " | " + valueV + " ]");
        return $resource('/data/insert', {key: keyV, value: valueV}).save();
    }

    service.deleteRecord = function(idV) {
        console.log("Deleting: [ " + idV + " ]");
        return $resource('/data/delete', {id: idV}).remove();
    }



});

rootModule.controller('MainCtrl', function(RestService) {
    var main = this;

    main.prop = "AngularJS's running...";

    main.records = RestService.getAllRecords();

    main.key = "";
    main.value = "";

    main.insertRecord = function() {
        RestService.insertRecord(main.key, main.value).$promise.then(function() {
            main.key = "";
            main.value = "";
            main.records = RestService.getAllRecords();
        });
    };

    main.id = "";

    main.deleteRecord = function($scope) {
        RestService.deleteRecord(main.id).$promise.then(function() {
            main.id = "";
            main.records = RestService.getAllRecords();
     });
    }







});

rootModule.directive('record', function() {
    return {
        template: '<div><div class="cell">{{record.id}}</div><div class="cell">{{record.key}}</div><div class="cell">{{record.value}}</div></div>'
    }
});