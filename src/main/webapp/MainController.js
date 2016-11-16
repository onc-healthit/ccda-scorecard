scApp.controller('MainController', ['$scope', '$location', '$anchorScroll', '$timeout', function($scope, $location, $anchorScroll, $timeout) {

	$scope.mainDisplayData = {
		showTopLevelContent : true,
		showScorecard : true
	};
	
	$scope.mainDebug = {
		inDebugMode: false,
		useLocalTestDataForServices: false
	};
	
	$scope.siteUiData = {
		disclaimer: {
			classesForNavbar: "external-page embedded-navbar",
			classesForButton: "external-page embedded"
		}		
	};
	
		$scope.debugLog = function(debugMessage) {
			if(!$scope.mainDebug.inDebugMode) return;
			console.log(debugMessage);
		};
	
    $scope.jumpToElementViaId = function(elementId, weWait, timeToWaitInMiliseconds) {
	    if (weWait) {
	      //this forces the jump in cases such as an outward collapse - 
	      //where the location does not yet exist until it is fully expanded
	      $timeout(function() {
	        console.log("waited " + timeToWaitInMiliseconds);
	        $location.hash(elementId);
	      }, timeToWaitInMiliseconds);
	    }
	    //set the location of the element via id to scroll to
	    $location.hash(elementId);
	    //scroll there
	    $anchorScroll();
    };
    
    $scope.resizeWindow = function(timeToWaitInMiliseconds) { 	  	  
  	  if(!timeToWaitInMiliseconds) {
  		  timeToWaitInMiliseconds = 1;
  	  }
  	  $timeout(function() {
  	  	  window.dispatchEvent(new Event('resize'));
  	  }, timeToWaitInMiliseconds);
    };
    
    $scope.removeElementFocusById = function(elementId) {
    	document.getElementById(elementId).blur();
    };
    
    $scope.removeWhiteSpaceFromString = function(stringWithWhiteSpace) {
    	return stringWithWhiteSpace.replace(/\s+/g, '');
    };

}]);
