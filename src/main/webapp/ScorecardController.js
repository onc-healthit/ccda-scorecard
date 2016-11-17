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
  $scope.totalNumberOfScorecardOccurrences = 0;
  
  //this is populated after the JSON is returned
  $scope.chartsData = {};
  
  function ChartAndCategoryTracker(chartIndex, categoryIndex) {
  	this.chartIndex = chartIndex;
  	this.categoryIndex = categoryIndex;
  }
  var chartAndCategoryIndexMap = [];
  
  $scope.finalCategoryListByGrade = [];
  
  $scope.igResults = []; $scope.certResults = []; $scope.referenceResults = [];  
  
  $scope.ReferenceInstanceTypeEnum = Object.freeze({  	
		IG_CONFORMANCE: "C-CDA IG Conformance Errors",
		CERTIFICATION_2015: "2015 Certification Feedback"    
  });  
  
  //if the SiteUploadControllers $scope.jsonScorecardData changes, 
  //then the service was called and returned new results,
  //so we process them so it is reflected in the view
  $scope.$watch('jsonScorecardData', function() {
  	$scope.debugLog("$scope.jsonScorecardData was changed");
	  $scope.debugLog($scope.jsonScorecardData);
	  if(!jQuery.isEmptyObject($scope.jsonScorecardData)) {		  
		  $scope.ccdaFileName = $scope.ccdaUploadData.fileName;
		  getAndProcessUploadControllerData();
		  $scope.uploadDisplay.isLoading = false;
		  if($scope.uploadErrorData.validationServiceError) {
		  	$scope.uploadDisplay.isValidationLoading = false;
		  }
		  $scope.resizeWindow(300);
	  }
  }, true);
  
  //if isLoading changes (from false to true)
  //then we reset our local scorecard data  
  $scope.$watch('uploadDisplay.isLoading', function() {
  	$scope.debugLog('$scope.uploadDisplay.isLoading: ');
  	$scope.debugLog($scope.uploadDisplay.isLoading);
	  if($scope.uploadDisplay.isLoading) {
		  resetScorecardData();
	  }
  }, true);
  
  var resetScorecardData = function() {
	  if(!$scope.ngFileUploadError) {
		  $scope.ccdaFileName = "Scoring...";
	  }
	  $scope.totalNumberOfScorecardOccurrences = 0;
	  $scope.chartsData = {};
	  $scope.jsonData = {};
	  $scope.categories = $scope.categoriesClone = {};
	  $scope.errorData.getJsonDataError = "";
	  $scope.errorData.getJsonDataErrorForUser = "";
	  $scope.errorData.saveScorecardError = "";
	  $scope.errorData.saveTryMeFileError = "";
	  chartAndCategoryIndexMap = [];
	  $scope.finalCategoryListByGrade = [];
	  $scope.igResults = []; $scope.certResults = []; $scope.referenceResults = [];
  };

  //adjust the chart type here and it will be reflected live using $scope.charts.currentChartOption
  var chartFormatBools = {
    isIssuesPerSectionWithGradeBar: false //false is best due to length of display names in this case
  };

  var chartTypeEnum = Object.freeze({
    ISSUES_PER_SECTION_WITH_GRADE: "C-CDA Scorecard Chart Overview"
  });
  
  var storeDataAndPopulateResults = function() {
	  //store scorecard sub-data in a more usable/direct manner
	  $scope.categories = $scope.jsonData.results.categoryList;
	  $scope.finalGrade = $scope.jsonData.results.finalGrade;
	  populateTotalNumberOfScorecardOccurrences();
	  //Note: populateCategoryListsByGrade() must be run after populateTotalNumberOfScorecardOccurrences() 
	  //as it uses modified local data
	  populateCategoryListsByGrade();	  
	  populateChartsData();
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
	  console.log("$scope.referenceResults");console.log($scope.referenceResults);
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
	  		//check for schema errors (there will always be a specific message if there are schema errors)
	  		if($scope.jsonData.schemaErrorList.length > 0 || schemaErrors) {
	  			if($scope.jsonData.schemaErrorList.length > 0) {
		  			for(var schemaError in $scope.jsonData.schemaErrorList) {
		  				$scope.errorData.getJsonDataErrorForUser += "<br />" + schemaError; 
		  			}
	  			} else {
	  				$scope.errorData.getJsonDataErrorForUser += "<br />" 
	  				+ "The specific schema errors encountered have not been identified.";
	  			}
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
        //panel-heading categoryPanelHeading
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
      		classPrefix + " heatMapFail" : $scope.calculateCategoryColor(classPrefix, grade);
    };  

  var calculateCategoryIndex = function(chartIndexClicked) {
  	for(var i = 0; i < chartAndCategoryIndexMap.length; i++) {
  		var curPair = chartAndCategoryIndexMap[i];
  		var chartIndexStored = curPair.chartIndex;
  		var categoryIndexStored = curPair.categoryIndex;  		
  		if(chartIndexClicked === chartIndexStored) {
  			$scope.debugLog("chartIndexClicked === chartIndexStored: returning categoryIndexStored: ");
  			$scope.debugLog(categoryIndexStored);
  			return categoryIndexStored;
  		}
  	}
  	$scope.debugLog("Error in calculateCategoryIndex(): Sending user to the first category.");
  	return 0;
  };
  
  var jumpToCategoryViaIndex = function(index, weWait, timeToWaitInMiliseconds) {
    $scope.jumpToElementViaId("catAccordion" + calculateCategoryIndex(index), weWait, timeToWaitInMiliseconds);
  };

  var jumpToCategoryViaKey = function(key, weWait, timeToWaitInMiliseconds) {
    //get string up to and not including the separator
    var elementId = "";
    var SEPARATOR = ':';
    for (var i = 0; i < key.length - 1; i++) {
      var curChar = key[i];
      var nextChar = key[i + 1];
      elementId += curChar;
      if (nextChar === SEPARATOR) {
        break;
      }
    }
    elementId = detruncateCategoryName(elementId);
    $scope.debugLog("elementId extracted: " + elementId);
    //move up one level for a cleaner look
    elementId = document.getElementById(elementId).parentNode.id;
    $scope.debugLog("parentNode: " + elementId);
    $scope.jumpToElementViaId(elementId, weWait, timeToWaitInMiliseconds);
  };
    
    $scope.jumpToCategoryViaName = function(key, weWait, timeToWaitInMiliseconds) {
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
    
  //*************HEAT MAP RELATED****************
    
   
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
        //var issues = curCategory.numberOfIssues;
        var issues = curCategory.numberOfOccurrences;        
        var score = curCategory.categoryNumericalScore;
        var failed = curCategory.failingConformance;
        var sectionData = {
          name: name,
          grade: grade,
          sectionIssueCount: issues,
          score: score,
          failed: failed
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
          failed: false
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
  
  var getCurrentHeatMapSectionData = function(row, columnNumber) {
  	return ($scope.finalCategoryListByGrade[columnNumber - 1])[row];
  };
  
  var heatMapCategoryController = function(row, columnNumber, positiveResult, negativeResult) {
  	if(!isCategoryListByGradeEmpty()) {
    	return positiveResult;
  	}
    return negativeResult ? negativeResult : "UNK.";  	
  }
  
  $scope.getHeatMapCategoryName = function(row, columnNumber) {
		return heatMapCategoryController(row, columnNumber, 
				getCurrentHeatMapSectionData(row, columnNumber).name);
  };
  
  $scope.getHeatMapCategoryGrade = function(row, columnNumber) {
  	var curSection = getCurrentHeatMapSectionData(row, columnNumber);
  	var result = curSection.failed ? '' : curSection.grade + ' ';
  	return heatMapCategoryController(row, columnNumber, result);
  };

  $scope.getHeatMapCategoryIssueCount = function(row, columnNumber) {
		var curSection = getCurrentHeatMapSectionData(row, columnNumber);
		var result = curSection.failed ? "Conformance Errors" : curSection.sectionIssueCount; 
		return heatMapCategoryController(row, columnNumber, result);
  };

  $scope.getGradeClass = function(row, columnNumber, classPrefix) {
		var failedClasses = classPrefix + " heatMapFail";
		var curSection = getCurrentHeatMapSectionData(row, columnNumber); 
    var positiveResult = curSection.failed ? 
    		failedClasses : $scope.calculateCategoryColor(classPrefix, curSection.grade);
    var negativeResult = classPrefix + " unknownGradeColor"; 
		return heatMapCategoryController(row, columnNumber, positiveResult, negativeResult);
  };
    
  //***************CHART RELATED*****************

  $scope.pieChartOptions = {
    chart: {
      type: 'pieChart',
      height: 625,
      margin: {
          top: 0,
          right: 0,
          bottom: 0,
          left: 0
      },      
      x: function(d) {
        return d.key;
      },
      y: function(d) {
        return d.y;
      },
      showLabels: true,
      duration: 550,
      labelThreshold: 0.01,
      labelSunbeamLayout: true,
      pie: {
        dispatch: {
          elementClick: function(e) {
          	$scope.debugLog("e.data.key: " + e.data.key);
          	$scope.debugLog("e.data.y: " + e.data.y);
          	$scope.debugLog("e.index: " + e.index);
            //jump to the related category in the detailed results
            jumpToCategoryViaIndex(e.index);
          }
        }
      },
      legend: {
        margin: {
          top: 3,
          right: 30,
          bottom: -10,
          left: 0
        },
        dispatch: {
          legendClick: function(e) {
          	$scope.debugLog("e.key: " + e.key);
          	$scope.debugLog("e.y: " + e.y);
          	$scope.debugLog("e.disabled: " + e.disabled);
            jumpToCategoryViaKey(e.key);
          }
        },
        //disable/enable pie chart item removal/resize via legend item click
        //Note: This may pose a problem if we are working with dynamic results
        updateState: false
      },
      tooltip: {
        enabled: false
      }
    }
  };

  $scope.barChartOptions = {
    chart: {
      type: 'discreteBarChart',
      height: 450,
      margin: {
        top: 20,
        right: 20,
        bottom: 50,
        left: 55
      },
      x: function(d) {
        return d.label;
      },
      y: function(d) {
        return d.value + (1e-10);
      },
      showValues: true,
      valueFormat: function(d) {
        return d3.format(',.4f')(d);
      },
      duration: 500,
      xAxis: {
        axisLabel: 'X Axis'
      },
      yAxis: {
        axisLabel: 'Y Axis',
        axisLabelDistance: -10
      }
    }
  };

  var getChartOption = function(isBar) {
    return isBar ? $scope.barChartOptions : $scope.pieChartOptions;
  };

  var ShortCategoryNamesEnum = Object.freeze({
    LABORATORY_TESTS_AND_RESULTS: "Lab Results",
    PATIENT_DEMOGRAPHICS: "Patient"
  });

  var setChartData = function(chartType, isBarChart) {
  	$scope.debugLog("\n" + chartType + " Chart set...");
    var data = [];
    var categoryDisplayName = "";
    var chartIndex = 0;
    //loop through all the categories
    for (var catIndex = 0; catIndex < $scope.categories.length; catIndex++) {
      var curCategory = $scope.categories[catIndex];
      var curRubrics = $scope.categories[catIndex].categoryRubrics;
      var numberOfIssues = curRubrics ? curRubrics.length : 0;
      var numberOfFailingIssuesPerSection = calculateNumberOfFailingIssuesPerSection(curCategory);            
      //apply logic to all categories whether they fail or not
      if (numberOfIssues) {
      	//make map for navigation
        chartAndCategoryIndexMap.push(new ChartAndCategoryTracker(chartIndex++, catIndex));
        //ensure visibility of longer category names
        categoryDisplayName = $scope.truncateCategoryName(curCategory.categoryName);
        //process data for charts
        switch (chartType) {
          case chartTypeEnum.ISSUES_PER_SECTION_WITH_GRADE:
            var curGrade = curCategory.categoryGrade;
                var keyAndLabelVal = categoryDisplayName + ": " + curGrade + " (" + numberOfIssues + ")";
                data.push(pushChartDataByDisplayType(isBarChart, keyAndLabelVal, numberOfIssues, keyAndLabelVal, numberOfIssues));
            break;
        }
      }
    }
		$scope.debugLog("chartAndCategoryIndexMap built:");
		$scope.debugLog(chartAndCategoryIndexMap);
    if (isBarChart) {
      return [{
        key: chartType,
        values: data
      }];
    }
    return data;
  };
  
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

  var pushChartDataByDisplayType = function(isBarChart, keyVal, yVal, labelVal, valueVal) {
    if (isBarChart) {
      return {
        "label": labelVal,
        "value": valueVal
      };
    }
    //pie chart
    return {
      key: keyVal,
      y: yVal
    };
  };

  //called after JSON scorecard data is returned
  var populateChartsData = function() {
    $scope.chartsData = {
      issuesPerSectionWithGrade: {
        chartTitle: chartTypeEnum.ISSUES_PER_SECTION_WITH_GRADE,
        chartDescription: "The C-CDA scorecard chart to the left provides a top level view of the data domains, the grade for each domain and the number of rubrics scored.",
        movingForwardText: "Providers and implementers can quickly focus on specific domains which have data quality issues and use the detailed reports below to improve the data quality of the C-CDA.",
        displayOnLeft: true,
        isBar: chartFormatBools.isIssuesPerSectionWithGradeBar,
        chartData: setChartData(chartTypeEnum.ISSUES_PER_SECTION_WITH_GRADE, chartFormatBools.isIssuesPerSectionWithGradeBar),
        chartOption: getChartOption(chartFormatBools.isIssuesPerSectionWithGradeBar)
      }
    };
  };
  
  var calculateNumberOfFailingIssuesPerSection = function(curCategory) {
  	return curCategory.numberOfIssues;
  };
  
  var populateTotalNumberOfScorecardOccurrences = function() {
  	$scope.categoriesClone = angular.copy($scope.categories);
    for (var catIndex = 0; catIndex < $scope.categoriesClone.length; catIndex++) {
    	var curCategory = $scope.categoriesClone[catIndex];
    	var curCategoryNumberOfOccurrences = 0;
      if (curCategory.numberOfIssues > 0) {
      	//get into the rubrics to get the sum of the rubric level numberOfIssues (occurrences)
      	for (var rubricIndex = 0; rubricIndex < curCategory.categoryRubrics.length; rubricIndex++) {
          var curRubric = curCategory.categoryRubrics[rubricIndex];
          if(curRubric.numberOfIssues > 0) {
            $scope.totalNumberOfScorecardOccurrences += curRubric.numberOfIssues;
            curCategoryNumberOfOccurrences += curRubric.numberOfIssues;
          }
        }
      }
    	//add numberOfOccurrences at current category to local data object for access in view
    	curCategory.numberOfOccurrences = curCategoryNumberOfOccurrences;      
    }
  };
  
  $scope.getDropdownStateClasses = function(panelDropdownElementId) {
//	  $scope.debugLog('panelDropdownElementId:');
//	  $scope.debugLog(panelDropdownElementId);
	  var panelElement = document.getElementById(panelDropdownElementId);
	  if(angular.element(panelElement).hasClass('collapsed')) {
	  	$scope.detailedResultsTextPrefix = "Click Here For ";
		  return "glyphicon glyphicon-triangle-right pull-left";
	  }
	  $scope.detailedResultsTextPrefix = '';
	  return "glyphicon glyphicon-triangle-bottom pull-left";	  
  };
    
  $scope.setPassingCertificationColors = function(classPrefix) {
      if($scope.calculatedValidationData.passedCertification) {
          return classPrefix + " passCertColor";
      } else {
          return classPrefix + " darkGrayBackground";
      }
      return classPrefix;
  };
    
  $scope.setFailingCertificationColors = function(classPrefix) {
      if(!$scope.calculatedValidationData.passedCertification) {
          return classPrefix + " failCertColor";
      } else {
          return classPrefix + " darkGrayBackground";
      }
      return classPrefix;
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
  
  var addCertificationResultToJson = function() {  	  	
  	var jsonWithCert = angular.copy($scope.jsonData);
  	jsonWithCert.results.passedCertification = $scope.calculatedValidationData.passedCertification;
  	console.log("jsonWithCert created to be saved:");
  	console.log(jsonWithCert);
  	return jsonWithCert;
  };
  
  $scope.saveScorecard = function() {  	
  	callSaveScorecardService(addCertificationResultToJson());
  };
  
  var callSaveScorecardService = function(jsonWithCert, newLocalUrl) {
  	$scope.debugLog("Entered callSaveScorecardService()");
    var externalUrl = 'http://54.200.51.225:8080/ccda-smart-scorecard/savescorecardservice/';
    var localUrl = 'savescorecardservice/';
    var postedMediaType = "application/json";
    var returnedMediaType = "application/pdf";
    $scope.saveServiceData.isLoading = true;
    $http({    	
      method: "POST",
      url: newLocalUrl ? newLocalUrl : localUrl,
      data: jsonWithCert,
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
    var filename = $scope.TryMeConstants.FILENAME + ".xml";
    $http({
      method: "GET",
      url: localUrl,
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

}]);
