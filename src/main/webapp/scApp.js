var scApp = angular.module('scorecard', [
	'ngFileUpload', 
	'angulartics', 
	'angulartics.google.analytics',
	'nvd3'
]);

// *************** DIRECTIVES ********************
// *Scorecard related*//
scApp.directive('scorecard', function() {
	return {
		restrict : 'E',
		templateUrl : 'scorecard.html?version=R2.3'
	};
});

scApp.directive('debug', function() {
	return {
		restrict : 'E',
		templateUrl : 'debug.html?version=R2.3'
	};
});

scApp.directive('topLevelResults', function() {
	return {
		restrict : 'E',
		templateUrl : 'topLevelResults.html?version=R2.3'
	};
});

scApp.directive('summary', function() {
	return {
		restrict : 'E',
		templateUrl : 'summary.html?version=R2.3'
	};
});

scApp.directive('heatMap', function() {
    return {
        restrict : 'E',
        templateUrl : 'heatMap.html?version=R2.3'
    };
});

scApp.directive('detailedResults', function() {
	return {
		restrict : 'E',
		templateUrl : 'detailedResults.html?version=R2.3'
	};
});

scApp.directive('scorecardTwoResults', function() {
	return {
		restrict : 'E',
		templateUrl : 'scorecardTwoResults.html?version=R2.3'
	};
});

scApp.directive('compare', function() {
    return {
    	restrict: 'E',
    	templateUrl: 'compare.html?version=R2.3'
    };
});

scApp.directive('saveScorecardButton', function() {
	return {
		restrict: 'E',
		templateUrl: 'saveScorecardButton.html?version=R2.3'
	};
});

scApp.directive('saveTryMeXmlButton', function() {
	return {
		restict: 'E',
		templateUrl: 'saveTryMeXmlButton.html?version=R2.3'
	};
});

scApp.directive('markdownSrc', function($http) {
	var converter = new showdown.Converter();
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {
			$http.get(attrs.markdownSrc).then(function(data) {
				element.html(converter.makeHtml(data.data));
			});
		}
	};
});

// *SITE mock related*//
scApp.directive('siteHeader', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteHeader.html?version=R2.3'
	};
});

scApp.directive('siteNavbar', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteNavbar.html?version=R2.3'
	};
});

scApp.directive('siteTopLevelContent', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteTopLevelContent.html?version=R2.3'
	};
});

scApp.directive('siteUploadForm', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteUploadForm.html?version=R2.3'
	};
});

scApp.directive('siteResultsModal', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteResultsModal.html?version=R2.3'
	};
});

scApp.directive('siteFooter', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteFooter.html?version=R2.3'
	};
});

scApp.directive('siteScoringCriteriaModal', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteScoringCriteriaModal.html?version=R2.3'
	};
});

scApp.directive('siteApiInstructionsModal', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteApiInstructionsModal.html?version=R2.3'
	};
});

scApp.directive('siteIntroductionModal', function() {
	return {
		restrict: 'E',
		templateUrl: 'siteIntroductionModal.html?version=R2.3'
	};
});

scApp.directive('siteDownloadLocalModal', function() {
	return {
		restrict: 'E',
		templateUrl: 'siteDownloadLocalModal.html?version=R2.3'
	};
});

scApp.directive('linkDisclaimer', function() {
	return {
		retrict: 'E',
		scope: {
			spanClass: "="
		},
		templateUrl: 'siteExternalLinkDisclaimer.html?version=R2.3'
	};
});

//*************** FILTERS ********************
scApp.filter("trust", ['$sce', function($sce) {
  return function(htmlCode){
    return $sce.trustAsHtml(htmlCode);
  }
}]);

//*************** ENVIRONMENT ********************
var env = {};
if (window) {
	Object.assign(env, window.__env);
}
scApp.constant('__env', env);
