scApp.controller('MainController', ['$scope', function($scope) {

	$scope.mainDisplayData = {
		showTopLevelContent : true,
		showScorecard : true
	};
	
	$scope.mainDebug = {
		inDebugMode: false,
		useLocalTestDataForServices: false
	};

}]);