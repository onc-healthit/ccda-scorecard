scApp.controller('ScorecardController', ['$scope', '$http', '$location', '$anchorScroll', '$timeout', '$sce', '$window', 
                                         function($scope, $http, $location, $anchorScroll, $timeout, $sce, $window) {

  $scope.debugData = {
    inDebugMode: false
  };
  $scope.jsonData = {};
  $scope.categories = {};
  $scope.errorData = {
    getJsonDataError: "",
    getJsonDataErrorForUser: "",
    saveScorecardError: "",
    saveTryMeFileError: ""
  };
  $scope.saveServiceData = {
  	isLoading: false,
  	loadingMessage: "Saving Report..."
  };
  var categoryTypes = Object.freeze([
    "Problems", "Medications", "Allergies", "Procedures", "Immunizations",
    "Laboratory Tests and Results", "Vital Signs", "Patient Demographics", "Encounters",
    "Social History"
  ]);

  $scope.ccdaFileName = "Scoring...";
  
  $scope.finalCategoryListByGrade = [];
  
  $scope.igResults = []; $scope.certResults = []; $scope.referenceResults = [];  
  
  $scope.ReferenceInstanceTypeEnum = Object.freeze({  	
		IG_CONFORMANCE: "C-CDA IG Conformance Errors",
		CERTIFICATION_2015: "2015 Ed. Certification Feedback"    
  });
  
  $scope.ScorecardConstants = Object.freeze({
  	IG_URL: "http://www.hl7.org/implement/standards/product_brief.cfm?product_id=379"
  });
  
  $scope.detailedResultsData = {
  		showDetailedResults: true
  };
  
  //if the SiteUploadControllers $scope.jsonScorecardData changes, 
  //then the service was called (or try me collected local data) and returned new results,
  //so we process them so it is reflected in the view
  $scope.$watch('jsonScorecardData', function() {
  	$scope.debugLog("$scope.jsonScorecardData was changed");$scope.debugLog($scope.jsonScorecardData);
	  if(!jQuery.isEmptyObject($scope.jsonScorecardData)) {  
		  $scope.ccdaFileName = $scope.ccdaUploadData.fileName;
		  getAndProcessUploadControllerData();
		  $scope.uploadDisplay.isLoading = false;
		  $scope.resizeWindow(300);
	  }
  }, true);
  
  //if isLoading changes (from false to true)
  //then we reset our local scorecard data  
  $scope.$watch('uploadDisplay.isLoading', function() {
  	$scope.debugLog('$scope.uploadDisplay.isLoading:');$scope.debugLog($scope.uploadDisplay.isLoading);
	  if($scope.uploadDisplay.isLoading) {
		  resetScorecardData();
	  }
  }, true);
  
  var resetScorecardData = function() {
	  if(!$scope.ngFileUploadError) {
		  $scope.ccdaFileName = "Scoring...";
	  }
	  $scope.jsonData = {};
	  $scope.categories = $scope.categoriesClone = {};
	  $scope.errorData.getJsonDataError = "";
	  $scope.errorData.getJsonDataErrorForUser = "";
	  $scope.errorData.saveScorecardError = "";
	  $scope.errorData.saveTryMeFileError = "";
	  $scope.finalCategoryListByGrade = [];
	  $scope.igResults = []; $scope.certResults = []; $scope.referenceResults = [];
  };
  
  var storeDataAndPopulateResults = function() {
	  //store scorecard sub-data in a more usable/direct manner
	  $scope.categories = $scope.jsonData.results.categoryList;
	  $scope.finalGrade = $scope.jsonData.results.finalGrade;
	  //The clone allows for us to modify the local data for heatmap display purposes (if we want)
	  //while keeping the original json for detailed results
	  $scope.categoriesClone = angular.copy($scope.categories);
	  populateCategoryListsByGrade();
	  
	  //store scorecard2 referenceResults and then restore in a new array
	  //it may seem pointless but it ensures the data we are working with is exactly what we expect -
	  //and verified by the limited types we expect and know how to process
	  $scope.igResults = getReferenceResultViaType($scope.ReferenceInstanceTypeEnum.IG_CONFORMANCE);
	  $scope.certResults = getReferenceResultViaType($scope.ReferenceInstanceTypeEnum.CERTIFICATION_2015);
	  if($scope.igResults) {
	  	$scope.referenceResults.push($scope.igResults);
	  }
	  if($scope.certResults) {
	  	$scope.referenceResults.push($scope.certResults);
	  }	  
	  $scope.debugLog("$scope.referenceResults");$scope.debugLog($scope.referenceResults);
  };
  
  var getReferenceResultViaType = function(referenceInstanceTypeEnum) {
  	var referenceResults = $scope.jsonData.referenceResults;
  	for(var i = 0; i < referenceResults.length; i++) {
  		var refInstance = referenceResults[i];
	  	if(refInstance.type === referenceInstanceTypeEnum) {
	  		return refInstance;
	  	}
	  }
  };
  
  var getAndProcessUploadControllerData = function() {
	  //reference data from parent (SiteUploadController)
	  $scope.jsonData = $scope.jsonScorecardData;
	  
	  //make sure valid data was returned before accessing invalid results
	  if($scope.jsonData.success && $scope.jsonData.results != null && $scope.jsonData.errorMessage == null) {
		  storeDataAndPopulateResults();
	  } else {
		  //the scorecard service could not handle the file sent
	  	if($scope.jsonData.errorMessage != null) {
	  		//apply a specific message
	  		$scope.errorData.getJsonDataErrorForUser = 
        	"The following error was encountered while scoring " +  $scope.ccdaFileName + ": " 
        	+ $scope.jsonData.errorMessage;
	  		//handle schema errors when the list is empty or null (should really never happen)
	  		if($scope.jsonData.schemaErrors && !$scope.jsonData.schemaErrorList) {
	  				$scope.errorData.getJsonDataErrorForUser += 
	  					("\r\n" + "The specific schema errors encountered have not been identified.");
	  		}
	  	} else {
	  		//apply a generic message
        $scope.errorData.getJsonDataErrorForUser = 
        	"The scorecard application is unable to score the C-CDA document. " + 
        	"Please try a file other than " + $scope.ccdaFileName + " or contact TestingServices@sitenv.org for help."	  		
	  	}
	  	//log dev data
      $scope.errorData.getJsonDataError = 
      	"Error thrown from ScorecardController: The C-CDA R2.1 Scorecard web service failed " + 
      	" to return valid data to the controller when posting " + $scope.ccdaFileName;
      console.log('$scope.errorData.getJsonDataError:');
      console.log($scope.errorData.getJsonDataError);
      $scope.disableAllLoading();
	  }
  };

  $scope.ContextEnum = Object.freeze({
    LIST_GROUP_ITEM: "listGroupItem",
    LABEL: "label",
    REQUIREMENT_PANEL: "requirementPanel"
  });

  $scope.updateScoringCriteriaContextClass = function(pointKey, rubric, type) {
    var pointIndex = Number(pointKey);
    var classPrefix = "";

    if (type === $scope.ContextEnum.LIST_GROUP_ITEM) {
      classPrefix = "list-group-item list-group-item-";
    } else if (type === $scope.ContextEnum.LABEL) {
      classPrefix = "label pull-right label-";
    } else if (type === $scope.ContextEnum.REQUIREMENT_PANEL) {
      classPrefix = "panel panel-";
    }

    return $scope.scoringContextSubRoutine(classPrefix, pointIndex, rubric.maxPoints);
  };

  $scope.scoringContextSubRoutine = function(classPrefix, pointOrGrade, passingComparator) {
	if(typeof pointOrGrade !== "undefined") {
	    //0 is red, maxPoints is green, all other points in between are orange
	    if (pointOrGrade === 0 || (pointOrGrade === "C" || pointOrGrade === "D")) {
		  return classPrefix + "danger";
		} else if (pointOrGrade === passingComparator || ~pointOrGrade.toString().indexOf(passingComparator.toString())) {
		  return classPrefix + "success";
		} else {
		  return classPrefix + "warning";
		}
	}
	//this is expected before results are returned from the service.
	//it allows for a generic color prior to the results as well as 
	//protects against running functions against undefined variables
	return classPrefix + "primary";
  };
    
    $scope.calculateCategoryColor = function(classPrefix, grade) {
        if(typeof grade !== "undefined") {
            if(grade === "A+") {
                return classPrefix + " heatMapAPlus";
            } else if(grade === "A-") {
                return classPrefix + " heatMapAMinus";
            } else if(grade === "B+") {
                return classPrefix + " heatMapBPlus";
            } else if(grade === "B-") {
                return classPrefix + " heatMapBMinus";            
            } else if(grade === "C") {
                return classPrefix + " heatMapC";
            } else if(grade === "D") {
                return classPrefix + " heatMapD";          
            }
        }
        return classPrefix + " unknownGradeColor";
    };
    
    $scope.getGradeClassForCategory = function(classPrefix, grade, curCategoryFailingConformance) { 
      return curCategoryFailingConformance ? 
      		classPrefix + " heatMapFailedIGConformance" : $scope.calculateCategoryColor(classPrefix, grade);
    };
    
    $scope.jumpToCategoryViaName = function(key, weWait, timeToWaitInMiliseconds) {
    		$scope.detailedResultsData.showDetailedResults = true;
        elementId = detruncateCategoryName(key);
        elementId = document.getElementById(elementId).parentNode.id;
        $scope.jumpToElementViaId(elementId, weWait, timeToWaitInMiliseconds);
    };
    
    $scope.convertReferenceInstanceTypeToId = function(referenceInstanceTypeFromJson) {
    	return "referenceInstance" + $scope.removeWhiteSpaceFromString(referenceInstanceTypeFromJson);
    };
    
    $scope.jumpToReferenceInstanceTypeViaId = function(referenceInstanceTypeFromJson) {
    	$scope.jumpToElementViaId($scope.convertReferenceInstanceTypeToId(referenceInstanceTypeFromJson));
    };
    
    $scope.getDropdownStateClasses = function(panelDropdownElementId) {
      //$scope.debugLog('panelDropdownElementId:');$scope.debugLog(panelDropdownElementId);
      var panelElement = document.getElementById(panelDropdownElementId);
      if(angular.element(panelElement).hasClass('collapsed')) {
  	    return "glyphicon glyphicon-triangle-right pull-left";
      }
      return "glyphicon glyphicon-triangle-bottom pull-left";
    };
    
    $scope.getDetailedResultsHideShowClasses = function() {
      if($scope.detailedResultsData.showDetailedResults) {
        $scope.detailedResultsTextPrefix = '';
        return "glyphicon glyphicon-triangle-bottom pull-left";
      }
      $scope.detailedResultsTextPrefix = "Click Here For ";
      return "glyphicon glyphicon-triangle-right pull-left";      
    };
    
    $scope.flipDetailedResultsVisibilityAndNavigate = function() {
    	$scope.detailedResultsData.showDetailedResults = !$scope.detailedResultsData.showDetailedResults;
    	var weWait = false;
    	var timeToWaitInMiliseconds = 0;
    	if($scope.detailedResultsData.showDetailedResults) {
    		$scope.jumpToElementViaId('detailedResults', weWait, timeToWaitInMiliseconds);
    	} else {
    		$scope.jumpToElementViaId('root', weWait, timeToWaitInMiliseconds);
    	}
    };
  
    
  //*************HEATMAP RELATED****************

  var ShortCategoryNamesEnum = Object.freeze({
    LABORATORY_TESTS_AND_RESULTS: "Lab Results",
    PATIENT_DEMOGRAPHICS: "Patient"
  });
  
  $scope.truncateCategoryName = function(curCategoryName) {
    if (curCategoryName === categoryTypes[5]) {
      return ShortCategoryNamesEnum.LABORATORY_TESTS_AND_RESULTS;
    } else if (curCategoryName === categoryTypes[7]) {
      return ShortCategoryNamesEnum.PATIENT_DEMOGRAPHICS;
    }
    return curCategoryName;
  };

  var detruncateCategoryName = function(curCategoryName) {
    if (curCategoryName === ShortCategoryNamesEnum.LABORATORY_TESTS_AND_RESULTS) {
      return categoryTypes[5];
    } else if (curCategoryName === ShortCategoryNamesEnum.PATIENT_DEMOGRAPHICS) {
      return categoryTypes[7];
    }
    return curCategoryName;
  };  
    
  /**
   * Allows us to delay the heat-map directive load 
   * (in combination with ng-if) until we have data
   */
  $scope.isFinalCategoryListByGradeInitialized = function() {
  	return $scope.finalCategoryListByGrade.length > 0;
  };
    
  /**
	 * The calculation orders by the numerical grade, which in turn
	 * orders by the letter based grade. The order is not intended
	 * to be related to the amount of occurrences of issues, but
	 * instead, the actual underlying categorical score.
	 */
    var populateCategoryListsByGrade = function() {

      var allCategories = [];
      for (var catIndex = 0; catIndex < $scope.categoriesClone.length; catIndex++) {
        var curCategory = $scope.categoriesClone[catIndex];
        var name = curCategory.categoryName;
        var grade = curCategory.categoryGrade;
        var issues = curCategory.numberOfIssues;        
        var score = curCategory.categoryNumericalScore;
        var failedIGConformance = curCategory.failingConformance;
        /*var hasCertificationFeedback = curCategory.certificationFeedback;*/
        var hasCertificationFeedback = false; //preprogrammed test data until implemented in backend
        var nullFlavorNI = curCategory.nullFlavorNI;
        var sectionData = {
          name: name,
          grade: grade,
          sectionIssueCount: issues,
          score: score,
          failedIGConformance: failedIGConformance,
          hasCertificationFeedback: hasCertificationFeedback,
          nullFlavorNI: nullFlavorNI
        };
        allCategories.push(sectionData);
      };
      

      allCategories.sort(compareNumericalGradesInSectionData);
      
      if(allCategories.length < 12) {
        var emptySectionData = {
          name: "UNK",
          grade: "UNK",
          sectionIssueCount: 0,
          score: 0,
          failedIGConformance: false,
          hasCertificationFeedback: false,
          nullFlavorNI: false          
        };
        if(allCategories.length === 10) {
          allCategories.push(emptySectionData, emptySectionData);
        } else if(allCategories.length === 11) {
          allCategories.push(emptySectionData);
        }
      }

      var categoryListByGradeFirstColumn = allCategories.slice(0, 4);
      var categoryListByGradeSecondColumn = allCategories.slice(4, 8);
      var categoryListByGradeThirdColumn = allCategories.slice(8, allCategories.length);
      $scope.finalCategoryListByGrade = [categoryListByGradeFirstColumn, categoryListByGradeSecondColumn, categoryListByGradeThirdColumn];

    };
  
  var compareNumericalGradesInSectionData = function(a, b) {
    if (a.score > b.score)
      return -1;
    else if (a.score < b.score)
      return 1;
    else 
      return 0;
  };
  
  var isCategoryListByGradeEmpty = function() {
  	for(var i = 0; i < $scope.finalCategoryListByGrade.length; i++) {
  		if($scope.finalCategoryListByGrade[i].length < 1)
  			return true;
  	}
  	return false;
  };
  
  $scope.getCurrentHeatMapSectionData = function(row, columnNumber) {
  	return ($scope.finalCategoryListByGrade[columnNumber - 1])[row];
  };
  
  var heatMapCategoryController = function(row, columnNumber, knownResult, unknownResult) {
  	if(!isCategoryListByGradeEmpty()) {
    	return knownResult;
  	}
    return unknownResult ? unknownResult : "UNK.";  	
  }
  
  $scope.getHeatMapCategoryName = function(row, columnNumber) {
		return heatMapCategoryController(row, columnNumber, 
				$scope.getCurrentHeatMapSectionData(row, columnNumber).name);
  };
  
  $scope.getHeatMapCategoryGrade = function(row, columnNumber) {
  	var curSection = $scope.getCurrentHeatMapSectionData(row, columnNumber);
  	var result = curSection.failedIGConformance || curSection.hasCertificationFeedback || curSection.nullFlavorNI ? '' 
  			: curSection.grade + ' ';
  	return heatMapCategoryController(row, columnNumber, result);
  };

  $scope.getHeatMapCategoryIssueCount = function(row, columnNumber) {
		var curSection = $scope.getCurrentHeatMapSectionData(row, columnNumber);
		var sectionFailLabel = getSectionFailLabel(curSection);
		var result = sectionFailLabel ? sectionFailLabel : curSection.sectionIssueCount;
		return heatMapCategoryController(row, columnNumber, result);
  };

  $scope.getHeatMapClass = function(row, columnNumber, classPrefix) {
		var curSection = $scope.getCurrentHeatMapSectionData(row, columnNumber);
  	var sectionFailClasses = getSectionFailClasses(curSection, classPrefix);
  	$scope.debugLog("sectionFailClasses:");$scope.debugLog(sectionFailClasses);
    var knownResult = sectionFailClasses ? 
    		sectionFailClasses : $scope.calculateCategoryColor(classPrefix, curSection.grade);
    var unknownResult = classPrefix + " unknownGradeColor"; 
		return heatMapCategoryController(row, columnNumber, knownResult, unknownResult);
  };
  
  var SectionFailTypeEnum = Object.freeze({
  	CONFORMANCE_ERRORS: {
  		label: "Conformance Errors",
  		cssClass: " heatMapFailedIGConformance"
  	},
  	CERTIFICATION_FEEDBACK: {
  		label: "Certification Feedback",
  		cssClass: " heatMapHasCertificationFeedback"
  	},
  	EMPTY_SECTION: {
  		label: "Empty Section",
  		cssClass: " heatMapNullFlavorNI"
  	}
  });
  
  $scope.getSectionFailType = function(curSection) {
  	if(curSection.failedIGConformance) {
  		return SectionFailTypeEnum.CONFORMANCE_ERRORS;
  	} else if(curSection.hasCertificationFeedback) {
  		return SectionFailTypeEnum.CERTIFICATION_FEEDBACK;
  	} else if(curSection.nullFlavorNI) {
  		return SectionFailTypeEnum.EMPTY_SECTION;
  	}
  	return null;
  };  
  
  var getSectionFailClasses = function(curSection, classPrefix) {
  	var sectionFailType = $scope.getSectionFailType(curSection);
  	return sectionFailType ? classPrefix + sectionFailType.cssClass : null;
  };  
  var getSectionFailLabel = function(curSection) {
  	var sectionFailType = $scope.getSectionFailType(curSection);
  	return sectionFailType ? sectionFailType.label : null;
  };
  
  
  //***************SAVE REPORT AND SAVE XML RELATED*****************
  
	var downloadViaAnchorWithPureJS = function(filename) {
    //triggers the download via a download tagged anchor element with the binary URL reference
		//Firefox requires a basic JS anchor vs an angular derived one to work so applying this form to all non-IE browsers
    var anchor = document.createElement('a');
    anchor.style = "display: none";  
    anchor.href = $scope.trustedFileUrl;
    anchor.download = filename;
    document.body.appendChild(anchor);
    anchor.click();
    setTimeout(function() {
      document.body.removeChild(anchor);
      window.URL.revokeObjectURL($scope.trustedFileUrl);  
    }, 100);
	};
  
  $scope.saveScorecard = function() {  	
  	callSaveScorecardService();
  };
  
  var callSaveScorecardService = function(newLocalUrl) {
  	$scope.debugLog("Entered callSaveScorecardService()");
    var externalUrl = 'http://54.200.51.225:8080/scorecard/savescorecardservice/';
    var localUrl = 'savescorecardservice/';
    var postedMediaType = "application/json";
    var returnedMediaType = "application/pdf";
    $scope.saveServiceData.isLoading = true;
    $http({    	
      method: "POST",
      url: newLocalUrl ? newLocalUrl : localUrl,
      data: $scope.jsonData,
      headers: {
      	"Content-Type": postedMediaType
      },
      responseType:"arraybuffer"
    }).then(function mySuccess(response) {
    	$scope.saveServiceData.isLoading = false;
    	$scope.errorData.saveScorecardError = "";
    	console.log("Scorecard results saved");
    	var filename = "SITE_C-CDA_Scorecard_" + $scope.ccdaFileName + ".pdf";
    	triggerFileDownload(response.data, returnedMediaType, filename, "saveScorecardButton");
    }, function myError(response) {
    	$scope.saveServiceData.isLoading = false;
    	var genericMessage = "An error was encountered while saving the Scorecard report.";
    	var statusMessage = "Status: " + response.status + " | " + "Message: " + response.statusText;
    	$scope.errorData.saveScorecardError = genericMessage + " " + $scope.userMessageConstant.GENERIC_LATER;
    	console.log(genericMessage + " " + statusMessage);
    });
  };	
	
  $scope.callDownloadTryMeFileService = function() {
  	$scope.debugLog("Entered callDownloadTryMeFileService()");
  	var localUrl = 'downloadtrymefileservice/';
  	//due to Safari limitations, text/plain is set so it can be rendered in-browser and then saved from there
    var mediaType = $scope.isSafari ? "text/plain" : "text/xml";
    var filename = $scope.selectedTryMeDoc.filename + ".xml";
    $http({
      method: "POST",
      url: localUrl,
      params: {filenameWithExtension: filename},           
      headers: {
      	"Content-Type": mediaType
      },
      responseType:"arraybuffer"
    }).then(function mySuccess(response) { 	
    	$scope.errorData.saveTryMeFileError = "";    	
    	triggerFileDownload(response.data, mediaType, filename, "saveTryMeXmlButton");
    	console.log("Try Me XML saved");
    }, function myError(response) {    	
    	var genericMessage = "An error was encountered while downloading the Try Me XML.";
    	var statusMessage = "Status: " + response.status + " | " + "Message: " + response.statusText;
    	$scope.errorData.saveTryMeFileError = genericMessage + " " + $scope.userMessageConstant.GENERIC_LATER;
    	console.log(genericMessage + " " + statusMessage);
    });
  };
	
	var triggerFileDownload = function(responseData, mediaType, filename, downloadButtonElementId) {
		//support IE Blob format vs the standard
    if (responseData != null && navigator.msSaveBlob) {
    	console.log('Downloading ' + mediaType + ' in IE');
      return navigator.msSaveBlob(new Blob([responseData], { type: mediaType }), filename);
		} else {
			console.log('Downloading ' + mediaType + ' in browsers which are not IE');
			var fileUrl = URL.createObjectURL(new Blob([responseData], {type: mediaType}));		
			//allow download of potentially dangerous file type
			$scope.trustedFileUrl = $sce.trustAsResourceUrl(fileUrl);		
			downloadViaAnchorWithPureJS(filename);
		}
	  //clear download button focus
	  document.getElementById(downloadButtonElementId).blur();
	};
	
	
	//*************REFERENCE RESULTS SORTING RELATED****************	
	
	$scope.getIssueTextForReferenceInstance = function(refInstanceType) {
		switch (refInstanceType) {
		case $scope.ReferenceInstanceTypeEnum.IG_CONFORMANCE:
			return "Error"
		case $scope.ReferenceInstanceTypeEnum.CERTIFICATION_2015:
			return "Feedback"
		default:
			return "Issue";
		}
	};
	
	$scope.getIssueTextSingularOrPluralFormForReferenceInstance = function(refInstanceType, refInstanceTotalErrorCount) {
		var singularResult = $scope.getIssueTextForReferenceInstance(refInstanceType);
		if(refInstanceTotalErrorCount !== 1) {
			return singularResult === "Feedback" ? "Results" : singularResult + 's';
		}
		return singularResult === "Feedback" ? "Result" : singularResult;
	};
	
  var IssueTypeEnum = Object.freeze({
    MDHT_ERROR: "C-CDA MDHT Conformance Error",
    VOCAB_ERROR: "ONC 2015 S&CC Vocabulary Validation Conformance Error",
    REFERENCE_ERROR: "ONC 2015 S&CC Reference C-CDA Validation Error"
  });
  
  var ResultCategoryEnum = Object.freeze({
  	MDHT: "C-CDA MDHT Conformance",
  	VOCAB: "ONC 2015 S&CC Vocabulary Validation Conformance",
  	REFERENCE: "ONC 2015 S&CC Reference C-CDA Validation",
  	UNKNOWN: "Unknown"
  });	
  
  var getValidationCategoryViaIssueTypeEnum = function(curIssueTypeEnum) {
    switch (curIssueTypeEnum) {
      case IssueTypeEnum.MDHT_ERROR:
        return ResultCategoryEnum.MDHT;
      case IssueTypeEnum.VOCAB_ERROR:  
      	return ResultCategoryEnum.VOCAB;      
      case IssueTypeEnum.REFERENCE_ERROR:     	
      	return ResultCategoryEnum.REFERENCE;
      default:
        return ResultCategoryEnum.UNKNOWN;
    }
  };

}]);
