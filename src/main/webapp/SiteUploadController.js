scApp.controller('SiteUploadController', ['$scope', '$http', 'Upload', '$timeout', function($scope, $http, Upload, $timeout) {
	
	$scope.TryMeConstants = Object.freeze({
		FILENAME: "170.315_b1_toc_amb_ccd_r21_sample1_v8"
	});
	
  function UploadData(fileName, docTypeSelected) {
    this.fileName = fileName;
    this.docTypeSelected = docTypeSelected;
  }
  $scope.ccdaUploadData = new UploadData("Unknown File (Upload)", "Unknown Document Type");

  $scope.userMessageConstant = Object.freeze ({
	GENERIC: "Please try a different file and report the issue to TestingServices@sitenv.org.",
	GENERIC_LATER: "Please try again later or contact TestingServices@sitenv.org for help.",
	UPLOAD_ERROR: "Error uploading <unknownFileName>: "
  });
  
  $scope.uploadErrorData = {
    getValidationResultsAsJsonError: "was uploaded successfully.",
    serviceTypeError: "No error encountered.",
    uploadError: $scope.userMessageConstant.UPLOAD_ERROR + $scope.userMessageConstant.GENERIC,
    validationServiceError: ""
  };

  $scope.jsonValidationData = $scope.metaResults = $scope.ccdaResults = {};
  $scope.jsonScorecardData = {};
    
  $scope.calculatedValidationData = {
      passedCertification: false,
      certificationResult: ""
  }
  
  $scope.uploadDisplay = {
  		isLoading: true,
  		isValidationLoading: true,
  };
  
  $scope.tryMeData = {
    	isTryMeActive: false	
  };

  var ServiceTypeEnum = Object.freeze({
    CCDA_VALIDATOR: "C-CDA R2.1 Validator Service",
    SCORECARD: "C-CDA R2.1 Scorecard Service",
    DEBUG: "Debug Service"
  });
  
  $scope.validationOptions = [
	{id: 1, value: "Scorecard and Validation results"}, 
	{id: 2, value: "Scorecard results only"}		  
  ];
  //default to run both services since we no longer have a selection option
  $scope.selectedValidationOption = $scope.validationOptions[0];

  $scope.isFirefox = typeof InstallTrigger !== 'undefined';
  $scope.isSafari = typeof safari !== 'undefined';
  
  var resetValidationData = function() {	  	  
		$scope.jsonScorecardData = {};
		$scope.ngFileUploadError = null;
		$scope.uploadDisplay.isLoading = true;
		$scope.uploadDisplay.isValidationLoading = true;
		$scope.uploadErrorData.validationServiceError = "";
		$scope.uploadErrorData.uploadError = $scope.userMessageConstant.UPLOAD_ERROR + $scope.userMessageConstant.GENERIC;
    $scope.calculatedValidationData.certificationResult = "";
    $scope.calculatedValidationData.passedCertification = false;
  };  

  //called by Validate Document button on SiteUploadForm
  $scope.uploadCcdaScFileAndCallServices = function(ccdaScFile, callDebug) {
    $scope.debugLog("$scope.uploadDisplay.isValidationLoading (before load):")
    $scope.debugLog($scope.uploadDisplay.isValidationLoading);
    
    resetValidationData();
    $scope.tryMeData.isTryMeActive = false;
    
	//static for now since we are not using the selector/sending this manually
    $scope.ccdaUploadData.docTypeSelected = "C-CDA_IG_Only";
    $scope.ccdaUploadData.fileName = (!$scope.mainDebug.inDebugMode || $scope.mainDebug.inDebugMode && ccdaScFile) 
    	? ccdaScFile.name
		: "No file selected: In debug mode";

     if(callDebug) {
    	 $scope.debugLog("In main debug mode");
       if($scope.mainDebug.useLocalTestDataForServices) {
    	   getLocalJsonResults("dataValidation.json", ServiceTypeEnum.CCDA_VALIDATOR);
    	   getLocalJsonResults("data.json", ServiceTypeEnum.SCORECARD);
       } else {
    	   callDebugService(ccdaScFile);
       }
     } else if ($scope.selectedValidationOption.id === 1) {
       callCcdaR2ValidatorService(ccdaScFile);    	 
       callCcdaScorecardService(ccdaScFile);
     } else if ($scope.selectedValidationOption.id === 2) {
       $scope.uploadDisplay.isValidationLoading = false;
       callCcdaScorecardService(ccdaScFile);
     } else {
       callCcdaR2ValidatorService(ccdaScFile);    	 
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

  var callCcdaR2ValidatorService = function(ccdaScFile) {
    var externalUrl = 'http://54.200.51.225:8080/referenceccdaservice/';
    var localUrl = 'ccdavalidatorservice/';
    var dataObject = {
      ccdaFile: ccdaScFile,
      referenceFileName: 'test',
      validationObjective: 'NonSpecificCCDAR2',
      debug_mode: true
    };
    uploadFileAndCallServices(ccdaScFile, localUrl, dataObject, ServiceTypeEnum.CCDA_VALIDATOR);
  };

  var callCcdaScorecardService = function(ccdaScFile, newLocalUrl) {
    var externalUrl = 'http://54.200.51.225:8080/ccda-smart-scorecard/ccdascorecardservice2/';
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
    	  $scope.debugLog("response.data:");
    	  $scope.debugLog(response.data);
        if (serviceType !== ServiceTypeEnum.DEBUG) {
          cacheAndProcessReturnedJsonData(response, serviceType);
        }
      });
    }, function(response) {
      if (response.status > 0) {
    	$scope.uploadErrorData.uploadError = $scope.uploadErrorData.uploadError.replace("unknownFileName", $scope.ccdaUploadData.fileName);
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
      case ServiceTypeEnum.CCDA_VALIDATOR:
      	collectAndHandleValidationData(response);
        break;
      case ServiceTypeEnum.SCORECARD:
        $scope.jsonScorecardData = response.data;
        break;
      default:
        $scope.uploadErrorData.serviceTypeError = "Error in cacheAndProcessReturnedJsonData(): The ServiceTypeEnum sent does not exist: " + serviceType;
    }
  };
  
  var collectAndHandleValidationData = function(response) {
      $scope.jsonValidationData = response.data;
      $scope.debugLog("$scope.jsonValidationData:");
      $scope.debugLog($scope.jsonValidationData);
      $scope.metaResults = $scope.jsonValidationData.resultsMetaData;
      $scope.ccdaResults = $scope.jsonValidationData.ccdaValidationResults;
      checkForValidationErrors();
      //only process results if there are results
      if(typeof $scope.metaResults !== 'undefined') {
	      setIssueCounts();
	      setCertificationResult();
	      $scope.uploadDisplay.isValidationLoading = false;
      }
  };
  
  $scope.disableAllLoading = function() {
    $scope.uploadDisplay.isValidationLoading = false;
    $scope.uploadDisplay.isLoading = false;
  }
  
  var checkForValidationErrors = function() {
  	if(typeof $scope.metaResults !== 'undefined') {
    	//invalid results returned due to a defined service error
      if($scope.metaResults.serviceError) {
    		$scope.uploadErrorData.validationServiceError = $scope.metaResults.serviceErrorMessage + 
    			" The file uploaded which encountered the error is " + $scope.ccdaUploadData.fileName + ". " + 
    				$scope.userMessageConstant.GENERIC;
    		console.log("Validation Service Error: " + $scope.uploadErrorData.validationServiceError);    		
    		$scope.uploadDisplay.isValidationLoading = false;
    		$scope.calculatedValidationData.certificationResult = false;
      }
  	} else if(!$scope.ccdaResults) {
    	//invalid results returned due to an undefined service error
  		$scope.uploadErrorData.validationServiceError = "The SITE C-CDA R2.1 Validation web service has failed to return results " +
			"for an unknown reason. Please try a file other than " + $scope.ccdaUploadData.fileName + " and report " +
					"the issue to TestingServices@sitenv.org.";
  		console.log("Validation Service Error: " + $scope.uploadErrorData.validationServiceError);
  		$scope.uploadDisplay.isValidationLoading = false;
  		$scope.calculatedValidationData.certificationResult = false;
    }  	
  }
  
  //TODO: delete this entire method when done with old UI based reference call
  var setIssueCounts = function() {  	
	    var metaData = $scope.metaResults.resultMetaData;

	    //TODO: delete these lines when done with old version of validation results
      /*
    	var mdhtMetaIssues = [metaData[0], metaData[1], metaData[2]];
    	var vocabMetaIssues = [metaData[3], metaData[4], metaData[5]];
    	var referenceMetaIssues = [metaData[6], metaData[7], metaData[8]];
    	$scope.allUsedMetaIssues = [mdhtMetaIssues, vocabMetaIssues, referenceMetaIssues];
    	*/
  
	    var mdhtErrors = metaData[0];
	    var vocabErrors = metaData[3];
	    var referenceErrors = metaData[6];
	    $scope.allUsedMetaIssues = [mdhtErrors, vocabErrors, referenceErrors];
	    //TODO: delete this line when done with old version of validaiton results
	    $scope.allUsedMetaIssuesOldStyle = [[mdhtErrors], [vocabErrors], [referenceErrors]];	    
  };
    
    var setCertificationResult = function() {
        var passesValidation = $scope.metaResults.resultMetaData[0].count === 0;
        var passesVocabulary = $scope.metaResults.resultMetaData[3].count === 0;
        if(passesValidation && passesVocabulary) {
            $scope.calculatedValidationData.passedCertification = true;
            $scope.calculatedValidationData.certificationResult = "Pass";
        } else {
            $scope.calculatedValidationData.passedCertification = false;
            $scope.calculatedValidationData.certificationResult = "Fail";                        
        }
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
  
  $scope.tryScorecard = function() {
  	$scope.tryMeData.isTryMeActive = true;
  	var extension = ".xml";
  	$scope.ccdaUploadData = new UploadData(
  			$scope.TryMeConstants.FILENAME + extension,
		"C-CDA_IG_Only");
  	var localFolder = "resources";
  	extension = ".json";
  	getLocalJsonResults(localFolder + "/" + $scope.TryMeConstants.FILENAME + extension, 
  			ServiceTypeEnum.SCORECARD); 	
  };
  
  $scope.downloadViaAnchor = function(link, name) {
  	var anchorElement = angular.element('<a/>');
	  anchorElement.attr({
      href: link,
      target: '_self',
      download: name
	  })[0].click();
  };
  
  var IssueTypeEnum = Object.freeze({
    MDHT_ERROR: "C-CDA MDHT Conformance Error",
    MDHT_WARNING: "C-CDA MDHT Conformance Warning",
    MDHT_INFO: "C-CDA MDHT Conformance Info",
    VOCAB_ERROR: "ONC 2015 S&CC Vocabulary Validation Conformance Error",
    VOCAB_WARNING: "ONC 2015 S&CC Vocabulary Validation Conformance Warning",
    VOCAB_INFO: "ONC 2015 S&CC Vocabulary Validation Conformance Info",
    REFERENCE_ERROR: "ONC 2015 S&CC Reference C-CDA Validation Error",
    REFERENCE_WARNING: "ONC 2015 S&CC Reference C-CDA Validation Warning",
    REFERENCE_INFO: "ONC 2015 S&CC Reference C-CDA Validation Info"    
  });
  
  var ResultCategoryEnum = Object.freeze({
  	MDHT: "C-CDA MDHT Conformance",
  	VOCAB: "ONC 2015 S&CC Vocabulary Validation Conformance",
  	REFERENCE: "ONC 2015 S&CC Reference C-CDA Validation",
  	UNKNOWN: "Unknown"
  });
  
  var getValidationCategoryViaType = function(curResultOrMetaCategoryType) {
    switch (curResultOrMetaCategoryType) {
      case IssueTypeEnum.MDHT_ERROR:
      case IssueTypeEnum.MDHT_WARNING:
      case IssueTypeEnum.MDHT_INFO:
        return ResultCategoryEnum.MDHT;
      case IssueTypeEnum.VOCAB_ERROR:
      case IssueTypeEnum.VOCAB_WARNING:
      case IssueTypeEnum.VOCAB_INFO:      
      	return ResultCategoryEnum.VOCAB;      
      case IssueTypeEnum.REFERENCE_ERROR:
      case IssueTypeEnum.REFERENCE_WARNING:
      case IssueTypeEnum.REFERENCE_INFO:      	
      	return ResultCategoryEnum.REFERENCE;
      default:
        return ResultCategoryEnum.UNKNOWN;
    }
  };

  var ResultSeverityEnum = Object.freeze({
    ERROR: "Error",
    WARNING: "Warning",
    INFO: "Info",
    UNKNOWN: "Unknown"
  });

  var getValidationResultSeverityViaType = function(curResultType) {
    switch (curResultType) {
      case IssueTypeEnum.MDHT_ERROR:
      case IssueTypeEnum.VOCAB_ERROR:
      case IssueTypeEnum.REFERENCE_ERROR:
        return ResultSeverityEnum.ERROR;
      case IssueTypeEnum.MDHT_WARNING:
      case IssueTypeEnum.VOCAB_WARNING:
      case IssueTypeEnum.REFERENCE_WARNING:
        return ResultSeverityEnum.WARNING;
      case IssueTypeEnum.MDHT_INFO:
      case IssueTypeEnum.VOCAB_INFO:
      case IssueTypeEnum.REFERENCE_INFO:
        return ResultSeverityEnum.INFO;
      default:
        return ResultSeverityEnum.UNKNOWN;
    }
  };
  
  $scope.determineResultsToShow = function(curResult, curMetaCategory) {
    if(getValidationResultSeverityViaType(curResult.type) === ResultSeverityEnum.ERROR
    		&& getValidationCategoryViaType(curResult.type) === getValidationCategoryViaType(curMetaCategory.type)) {
    	return true;
    } else {
    	return false;
    }
  	return true;
  };

  $scope.getValidationResultColorViaType = function(curResult, isBadge) {
    switch (getValidationResultSeverityViaType(curResult.type)) {
      case ResultSeverityEnum.ERROR:
        if (isBadge)
          return "badge btn-danger";
        return "errorColor";
      case ResultSeverityEnum.WARNING:
        if (isBadge)
          return "badge btn-warning";
        return "warningColor";
      case ResultSeverityEnum.INFO:
        if (isBadge)
          return "badge btn-info";
        return "infoColor";
      case ResultSeverityEnum.UNKNOWN:
        if (isBadge)
          return "badge btn-primary";
        return "unknownColor";
    }
  };

}]);
