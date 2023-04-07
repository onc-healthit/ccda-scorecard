scApp.controller('SiteUploadController', ['$scope', '$http', 'Upload', '$timeout', function($scope, $http, Upload, $timeout) {
	
  function UploadData(fileName) {
    this.fileName = fileName;
  }
  $scope.ccdaUploadData = new UploadData("Unknown File (Upload)");
  
	var tryMeConstants = Object.freeze({
		DEFAULT_GOOD_SCORE: "highScoringSample",
		BAD_SCORE_1: "lowScoringSample",
		HAS_CONF_AND_CERT_ERRORS_1: "sampleWithErrors"
	});
  $scope.tryMeData = {
    	isTryMeActive: false,
      tryMeDocs: [
	      {id: 1, value: "High scoring sample", filename: tryMeConstants.DEFAULT_GOOD_SCORE}, 
        {id: 2, value: "Low scoring sample", filename: tryMeConstants.BAD_SCORE_1},
        {id: 3, value: "Sample with errors", filename: tryMeConstants.HAS_CONF_AND_CERT_ERRORS_1}
      ]
  };

  $scope.userMessageConstant = Object.freeze ({
	GENERIC: "Please try a different file and report the issue to edge-test-tool@googlegroups.com.",
	GENERIC_LATER: "Please try again later or contact edge-test-tool@googlegroups.com for help.",
	GENERIC_COMBINED: "Please try again later, try a different file, or contact edge-test-tool@googlegroups.com for help.",
	UPLOAD_ERROR: "Error uploading <unknownFileName>: ",
	TIMEOUT_ERROR: "The scorecard application has been stopped " +
	"due to the length of time it has been processing the given request. "
  });
  
  $scope.uploadErrorData = {
    serviceTypeError: "No error encountered.",
    uploadError: $scope.userMessageConstant.UPLOAD_ERROR + $scope.userMessageConstant.GENERIC
  };
  
  $scope.timeoutData = {
  	lengthInMilliseconds: 300000, //5 minutes
  	errorMessage: null,
  	timer: null
  };

  $scope.jsonScorecardData = {};
  
  $scope.uploadDisplay = {
  		isLoading: true
  };

  var ServiceTypeEnum = Object.freeze({
    SCORECARD: "C-CDA R2.1 Scorecard Service",
    DEBUG: "Debug Service"
  });

  $scope.isFirefox = typeof InstallTrigger !== 'undefined';
  $scope.isSafari = typeof safari !== 'undefined';
  
  var resetValidationData = function() {	  	  
		$scope.jsonScorecardData = {};
		$scope.ngFileUploadError = null;
		$scope.uploadDisplay.isLoading = true; //set to false by ScorecardController $watch upon collection of new json data		
		$scope.uploadErrorData.uploadError = $scope.userMessageConstant.UPLOAD_ERROR + $scope.userMessageConstant.GENERIC;
		$scope.timeoutData.errorMessage = null;
  };  

  /**
   * Called by score button on SiteUploadForm
   * */
  $scope.uploadCcdaScFileAndCallServices = function(ccdaScFile, callDebug) {
    if($scope.timeoutData.timer) {
    	 //disable timer from last call so it doesn't independently interfere with this call
    	$timeout.cancel($scope.timeoutData.timer);
    }
    resetValidationData();
    $scope.tryMeData.isTryMeActive = false;
    
    $scope.ccdaUploadData.fileName = (!$scope.mainDebug.inDebugMode || $scope.mainDebug.inDebugMode && ccdaScFile) 
    	? ccdaScFile.name
		: "No file selected: In debug mode";
     
     if(callDebug) {
    	 $scope.debugLog("In main debug mode");
       if($scope.mainDebug.useLocalTestDataForServices) {
    	   getLocalJsonResults("data.json", ServiceTypeEnum.SCORECARD);
       } else {
    	   callDebugService(ccdaScFile);
       }
     } else {
       callCcdaScorecardService(ccdaScFile);    	 
     }     
  };

  var callDebugService = function(ccdaScFile) {
    var dataObject = {
      username: 'testName',
      file: ccdaScFile
    };
    uploadFileAndCallServices(ccdaScFile, 'https://angular-file-upload-cors-srv.appspot.com/upload', dataObject);
  };

  var callCcdaScorecardService = function(ccdaScFile, newLocalUrl) {
    var externalUrl = 'http://54.200.51.225:8080/scorecard/ccdascorecardserviceinternal/';
    var localUrl = 'ccdascorecardserviceinternal/';
    if(newLocalUrl) {
    	localUrl = newLocalUrl;
    }
    var dataObject = {
      ccdaFile: ccdaScFile
    };
    uploadFileAndCallServices(ccdaScFile, localUrl, dataObject, ServiceTypeEnum.SCORECARD);
  };
  
  var uploadFileAndCallServices = function(ccdaFile, urlOfServiceToCall, dataObject, serviceType) {
    ccdaFile.upload = Upload.upload({
      url: urlOfServiceToCall,
      data: dataObject
    });
    
    //if we don't already have valid JSON returned, then after the specified time, 
    //we cancel the active scorecard service request and post an error message
    $scope.timeoutData.timer = $timeout(function() {
  			if(jQuery.isEmptyObject($scope.jsonScorecardData)) {
	  			$scope.timeoutData.errorMessage = $scope.userMessageConstant.TIMEOUT_ERROR + $scope.userMessageConstant.GENERIC_COMBINED;      
	    		console.log($scope.timeoutData.errorMessage);
	        $scope.disableAllLoading();
	        ccdaFile.upload.abort(); //cancels the following service request (defined in ccdaFile.upload.then)
  			}
  	}, $scope.timeoutData.lengthInMilliseconds);
  	  	
    ccdaFile.upload.then(function(response) {
      $timeout(function() {
    	  $scope.debugLog("response.data:");$scope.debugLog(response.data);
        if (serviceType !== ServiceTypeEnum.DEBUG) {
          cacheAndProcessReturnedJsonData(response, serviceType);
        }
      });
    }, function(response) {
      if (response.status > 0) {
      	$scope.uploadErrorData.uploadError = 
      		$scope.uploadErrorData.uploadError.replace("unknownFileName", $scope.ccdaUploadData.fileName);
        $scope.ngFileUploadError = 'Status: ' + response.status + ' - ' + "Data: " + response.data;
        console.log("Error uploading file or calling service(s):");
        console.log($scope.uploadErrorData.uploadError);
        console.log($scope.ngFileUploadError);
        $scope.disableAllLoading();
      }
    }, function(evt) {
      ccdaFile.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
    });    
  };

  var cacheAndProcessReturnedJsonData = function(response, serviceType) {
    switch (serviceType) {
      case ServiceTypeEnum.SCORECARD:
        $scope.jsonScorecardData = response.data;
        break;
      default:
        $scope.uploadErrorData.serviceTypeError = 
        	"Error in cacheAndProcessReturnedJsonData(): The ServiceTypeEnum sent does not exist: " + serviceType;
    }
  };
  
  $scope.disableAllLoading = function() {
    $scope.uploadDisplay.isLoading = false;
  }
  
  var getLocalJsonResults = function(localJsonFileLocation, serviceType) {
    $http({
      method: "GET",
      url: localJsonFileLocation
    }).then(function mySuccess(response) {
      cacheAndProcessReturnedJsonData(response, serviceType);
    }, function myError(response) {
    	$scope.debugLog("Error: Cannot retrieve local " + serviceType + " data");
    });
  };
  
  /**
   * Called by try me button
   */
  $scope.tryScorecard = function() {
  	resetValidationData();
  	$scope.tryMeData.isTryMeActive = true;
  	var extension = ".xml";
  	$scope.ccdaUploadData = new UploadData($scope.selectedTryMeDoc.filename + extension);
  	var localFolder = "resources";
  	extension = ".json";
  	getLocalJsonResults(localFolder + "/" + $scope.selectedTryMeDoc.filename + extension, 
  			ServiceTypeEnum.SCORECARD);  	
  };

}]);
