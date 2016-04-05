var scApp = angular.module('scorecard', ['nvd3']);

//*************** DIRECTIVES ********************
scApp.directive('debug', function() {
  return {
    restrict: 'E',
    templateUrl: 'debug.html'
  };
});

scApp.directive('topLevelResults', function() {
  return {
    restrict: 'E',
    templateUrl: 'topLevelResults.html'
  };
});

scApp.directive('detailedResults', function() {
  return {
    restrict: 'E',
    templateUrl: 'detailedResults.html'
  };
});

scApp.directive('charts', function() {
  return {
    restrict: 'E',
    templateUrl: 'charts.html'
  };
});