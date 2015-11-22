var webClient = angular.module('webClient', ['ngRoute']);

webClient.controller('Controller', function ($scope) {

    var webSocket = new WebSocket('ws://localhost:8080/ws');
    webSocket.onmessage = function(message) {
        $scope.$apply(function() {
            var data = JSON.parse(message.data);
            switch(data.type) {
                case "registrationOpened":
                    $scope.gameInProgress = false;
                    break;
                case "registrationClosed":
                    $scope.gameInProgress = true;
                    break;
                case "leaderBoard":
                    $scope.leaderBoard = data.leaderBoard;
                    break;
            }
        });
    };

    $scope.gameInProgress = false;

    $scope.leaderBoard = [];

    $scope.register = function(){
        $scope.formDisabled = true;
        var message = angular.toJson({
            type: 'user',
            user: $scope.user
        });
        $scope.user = {};
        webSocket.send(message);
    };
});

webClient.config(function ($routeProvider) {
        $routeProvider
            .when('/start', {
                templateUrl: 'views/start.html',
                controller: 'Controller'
            })
            .when('/results', {
                templateUrl: 'views/results.html',
                controller: 'Controller'
            })
            .otherwise({
                redirectTo: '/start'
            });
    });