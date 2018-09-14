var App;

(function (App) {


    App.AppController = App._module.controller('App.AppController', AppController);
    AppController.$inject = ['AuthService', 'CredentialService'];
    function AppController(AuthService, CredentialService) {
        var vm = this;

        vm.getUsername = function() {
            return CredentialService.credentials().username || 'User';
        };


    }

}