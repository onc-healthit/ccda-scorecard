scApp.controller('SiteUploadController', ['$scope', '$http', 'Upload', '$timeout', function($scope, $http, Upload, $timeout) {
	
  function UploadData(fileName, docTypeSelected) {
    this.fileName = fileName;
    this.docTypeSelected = docTypeSelected;
  }
  $scope.ccdaUploadData = new UploadData("Unknown File (Upload)", "Unknown Document Type");

  $scope.uploadErrorData = {
    getValidationResultsAsJsonError: "was uploaded successfully.",
    serviceTypeError: "No error encountered.",
    uploadError: "Error uploading <unknownFileName>: Please try a different file and report the issue to TestingServices@sitenv.org."
  };

  $scope.jsonValidationData = $scope.metaResults = $scope.ccdaResults = {};
  $scope.mdhtMetaIssues = $scope.vocabMetaIssues = {};
  $scope.jsonScorecardData = {};
  
  $scope.uploadDisplay = {
	isLoading: true
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

  //called by Validate Document button on SiteUploadForm
  $scope.uploadCcdaScFileAndCallServices = function(ccdaScFile, callDebug) {
	//reset data so old data is not seen for any reason	  
	$scope.jsonScorecardData = {};
	$scope.ngFileUploadError = null;
	$scope.uploadDisplay.isLoading = true;
	//static for now since we are not using the selector/sending this manually
    $scope.ccdaUploadData.docTypeSelected = "C-CDA_IG_Only";
    $scope.ccdaUploadData.fileName = ccdaScFile.name;

     if(callDebug) {
       callDebugService(ccdaScFile);
     } else if ($scope.selectedValidationOption.id == 1) {
       callCcdaR2ValidatorService(ccdaScFile);    	 
       callCcdaScorecardService(ccdaScFile);
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

  var callCcdaR2ValidatorService = function(ccdaScFile) {
    var externalUrl = 'http://54.200.51.225:8080/referenceccdaservice/';
    var localUrl = '../referenceccdaservice/';
    var dataObject = {
      ccdaFile: ccdaScFile,
      referenceFileName: 'test',
      validationObjective: 'NonSpecificCCDAR2',
      debug_mode: true
    };
    uploadFileAndCallServices(ccdaScFile, localUrl, dataObject, ServiceTypeEnum.CCDA_VALIDATOR);
  };

  var callCcdaScorecardService = function(ccdaScFile) {
    var externalUrl = 'http://54.200.51.225:8080/ccda-smart-scorecard/ccdascorecardservice/';
    var localUrl = 'ccdascorecardservice/';
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
    	  console.log("response.data" + response.data);
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
      }
    }, function(evt) {
      ccdaFile.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
    });
  };

  var cacheAndProcessReturnedJsonData = function(response, serviceType) {
    switch (serviceType) {
      case ServiceTypeEnum.CCDA_VALIDATOR:
        $scope.jsonValidationData = response.data;        
        console.log("$scope.jsonValidationData:\n");
        console.log($scope.jsonValidationData);
        $scope.metaResults = $scope.jsonValidationData.resultsMetaData;
        $scope.ccdaResults = $scope.jsonValidationData.ccdaValidationResults;
        setIssueCounts();
        break;
      case ServiceTypeEnum.SCORECARD:
        $scope.jsonScorecardData = response.data;       
        break;
      default:
        $scope.uploadErrorData.serviceTypeError = "Error in cacheAndProcessReturnedJsonData(): The ServiceTypeEnum sent does not exist: " + serviceType;
    }
  };

  var setIssueCounts = function() {
    var metaData = $scope.metaResults.resultMetaData;
    mdhtMetaIssues = [metaData[0], metaData[1], metaData[2]];
    vocabMetaIssues = [metaData[3], metaData[4], metaData[5]];
    $scope.allUsedMetaIssues = [mdhtMetaIssues, vocabMetaIssues];
  };
  
  //for debugging purposes only with a local json file
  $scope.getValidationResultsAsJson = function() {
    $http({
      method: "GET",
      url: "dataValidation.json"
    }).then(function mySuccess(response) {
      cacheAndProcessReturnedJsonData(response);
      setIssueCounts();
    }, function myError(response) {
      $scope.uploadErrorData.getValidationResultsAsJsonError = "Upload Controller Error: Cannot retrieve validation data from server.";
    });
  };
  // $scope.getValidationResultsAsJson();  

  var IssueTypeEnum = Object.freeze({
    MDHT_ERROR: "C-CDA MDHT Conformance Error",
    MDHT_WARNING: "C-CDA MDHT Conformance Warning",
    MDHT_INFO: "C-CDA MDHT Conformance Info",
    VOCAB_ERROR: "ONC 2015 S&CC Vocabulary Validation Conformance Error",
    VOCAB_WARNING: "ONC 2015 S&CC Vocabulary Validation Conformance Warning",
    VOCAB_INFO: "ONC 2015 S&CC Vocabulary Validation Conformance Info"
  });

  var ResultTypeEnum = Object.freeze({
    ERROR: "Error",
    WARNING: "Warning",
    INFO: "Info",
    UNKNOWN: "Unknown"
  });

  var getValidationResultType = function(curResultType) {
    switch (curResultType) {
      case IssueTypeEnum.MDHT_ERROR:
      case IssueTypeEnum.VOCAB_ERROR:
        return ResultTypeEnum.ERROR;
      case IssueTypeEnum.MDHT_WARNING:
      case IssueTypeEnum.VOCAB_WARNING:
        return ResultTypeEnum.WARNING;
      case IssueTypeEnum.MDHT_INFO:
      case IssueTypeEnum.VOCAB_INFO:
        return ResultTypeEnum.INFO;
      default:
        return ResultTypeEnum.UNKNOWN;
    }
  };

  $scope.getValidationResultColorViaType = function(curResult, isBadge) {
    switch (getValidationResultType(curResult.type)) {
      case ResultTypeEnum.ERROR:
        if (isBadge)
          return "badge btn-danger";
        return "errorColor";
      case ResultTypeEnum.WARNING:
        if (isBadge)
          return "badge btn-warning";
        return "warningColor";
      case ResultTypeEnum.INFO:
        if (isBadge)
          return "badge btn-info";
        return "infoColor";
      case ResultTypeEnum.UNKNOWN:
        if (isBadge)
          return "badge btn-primary";
        return "unknownColor";
    }
  };

}]);