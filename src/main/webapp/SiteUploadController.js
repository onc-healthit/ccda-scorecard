scApp.controller('SiteUploadController', ['$scope', '$http', 'Upload', '$timeout', function($scope, $http, Upload, $timeout) {
	
	$scope.TryMeConstants = Object.freeze({
		FILENAME: "170.315_b1_toc_amb_ccd_r21_sample1_v8"
	});
	
  function UploadData(fileName) {
    this.fileName = fileName;
  }
  $scope.ccdaUploadData = new UploadData("Unknown File (Upload)");

  $scope.userMessageConstant = Object.freeze ({
	GENERIC: "Please try a different file and report the issue to TestingServices@sitenv.org.",
	GENERIC_LATER: "Please try again later or contact TestingServices@sitenv.org for help.",
	UPLOAD_ERROR: "Error uploading <unknownFileName>: "
  });
  
  $scope.uploadErrorData = {
    serviceTypeError: "No error encountered.",
    uploadError: $scope.userMessageConstant.UPLOAD_ERROR + $scope.userMessageConstant.GENERIC
  };

  $scope.jsonScorecardData = {};
  
  $scope.uploadDisplay = {
  		isLoading: true
  };
  
  $scope.tryMeData = {
    	isTryMeActive: false	
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
  };  

  /**
   * Called by score button on SiteUploadForm
   * */
  $scope.uploadCcdaScFileAndCallServices = function(ccdaScFile, callDebug) {  	
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
    var externalUrl = 'http://54.200.51.225:8080/scorecard/ccdascorecardservice2/';
    var localUrl = 'ccdascorecardservice2/';
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
  	$scope.ccdaUploadData = new UploadData($scope.TryMeConstants.FILENAME + extension);
  	var localFolder = "resources";
  	extension = ".json";
  	getLocalJsonResults(localFolder + "/" + $scope.TryMeConstants.FILENAME + extension, 
  			ServiceTypeEnum.SCORECARD);  	
  };

}]);
