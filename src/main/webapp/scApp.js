var scApp = angular.module('scorecard', [ 'nvd3', 'ngFileUpload' ]);

// *************** DIRECTIVES ********************
// *Scorecard related*//
scApp.directive('scorecard', function() {
	return {
		restrict : 'E',
		templateUrl : 'scorecard.html'
	};
});

scApp.directive('debug', function() {
	return {
		restrict : 'E',
		templateUrl : 'debug.html'
	};
});

scApp.directive('topLevelResults', function() {
	return {
		restrict : 'E',
		templateUrl : 'topLevelResults.html'
	};
});

scApp.directive('charts', function() {
	return {
		restrict : 'E',
		templateUrl : 'charts.html'
	};
});

scApp.directive('heatMap', function() {
    return {
        restrict : 'E',
        templateUrl : 'heatMap.html'
    };
});

scApp.directive('detailedResults', function() {
	return {
		restrict : 'E',
		templateUrl : 'detailedResults.html'
	};
});

scApp.directive('saveScorecardButton', function() {
	return {
		restrict: 'E',
		templateUrl: 'saveScorecardButton.html'
	};
});

scApp.directive('saveTryMeXmlButton', function() {
	return {
		restict: 'E',
		templateUrl: 'saveTryMeXmlButton.html'
	};
});

// *SITE mock related*//
scApp.directive('siteHeader', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteHeader.html'
	};
});

scApp.directive('siteNavbar', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteNavbar.html'
	};
});

scApp.directive('siteTopLevelContent', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteTopLevelContent.html'
	};
});

scApp.directive('siteUploadForm', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteUploadForm.html'
	};
});

scApp.directive('siteResultsModal', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteResultsModal.html'
	};
});

scApp.directive('siteValidationResults', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteValidationResults.html'
	};
});

scApp.directive('siteFooter', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteFooter.html'
	};
});

scApp.directive('siteScoringCriteriaModal', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteScoringCriteriaModal.html'
	};
});

scApp.directive('siteApiInstructionsModal', function() {
	return {
		restrict : 'E',
		templateUrl : 'siteApiInstructionsModal.html'
	};
});

scApp.directive('siteIntroductionModal', function() {
	return {
		restrict: 'E',
		templateUrl: 'siteIntroductionModal.html'
	};
});

scApp.directive('siteDownloadLocalModal', function() {
	return {
		restrict: 'E',
		templateUrl: 'siteDownloadLocalModal.html'
	};
});

scApp.directive('linkDisclaimer', function() {
	return {
		retrict: 'E',
		scope: {
			spanClass: "="
		},
		templateUrl: 'siteExternalLinkDisclaimer.html'
	};
});
