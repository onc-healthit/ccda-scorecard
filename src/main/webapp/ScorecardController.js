scApp.controller('ScorecardController', ['$scope', '$http', '$location', '$anchorScroll', '$timeout', function($scope, $http, $location, $anchorScroll, $timeout) {

  $scope.debugData = {
    inDebugMode: false
  };
  $scope.jsonData = {};
  $scope.categories = {};
  $scope.errorData = {
    getJsonDataError: "",
    getJsonDataErrorForUser: ""
  };
  categoryTypes = Object.freeze([
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
  
  $scope.categoryListByGradeFirstColumn = $scope.categoryListByGradeSecondColumn = $scope.categoryListByGradeThirdColumn = [];
  
  //if the SiteUploadControllers $scope.jsonScorecardData changes, 
  //then the service was called and returned new results,
  //so we process them so it is reflected in the view
  $scope.$watch('jsonScorecardData', function() {
  	console.log("$scope.jsonScorecardData was changed");
	  console.log($scope.jsonScorecardData);
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
	  console.log('$scope.uploadDisplay.isLoading: ');
	  console.log($scope.uploadDisplay.isLoading);
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
	  chartAndCategoryIndexMap = [];
	  $scope.categoryListByGradeFirstColumn = $scope.categoryListByGradeSecondColumn = $scope.categoryListByGradeThirdColumn = [];
  };

  //adjust the chart type here and it will be reflected live using $scope.charts.currentChartOption
  var chartFormatBools = {
    isIssuesPerSectionWithGradeBar: false //false is best due to length of display names in this case
  };

  var chartTypeEnum = Object.freeze({
    ISSUES_PER_SECTION_WITH_GRADE: "C-CDA Scorecard Chart Overview"
  });
  
  var storeDataAndPopulateResults = function() {
	  //store the sub-data in a more usable/direct manner
	  $scope.categories = $scope.jsonData.results.categoryList;
	  $scope.finalGrade = $scope.jsonData.results.finalGrade;
	  populateTotalNumberOfScorecardOccurrences();
	  //Note: populateCategoryListsByGrade() must be run after populateTotalNumberOfScorecardOccurrences() 
	  //as it uses modified local data
	  populateCategoryListsByGrade();	  
	  populateChartsData(); 
  };  
  
  var getAndProcessUploadControllerData = function() {
	  //reference data from parent (SiteUploadController)
	  $scope.jsonData = $scope.jsonScorecardData;
	  
	  //make sure valid data was returned before accessing invalid results
	  if($scope.jsonData.success && $scope.jsonData.results != null) {
		  storeDataAndPopulateResults();
	  } else {
		  //the scorecard service could not handle the file sent
	      $scope.errorData.getJsonDataError = "Error thrown from ScorecardController: The C-CDA R2.1 Scorecard web service failed to return valid data to the controller when posting " + $scope.ccdaFileName;
	      console.log('$scope.errorData.getJsonDataError:');
	      console.log($scope.errorData.getJsonDataError);
        $scope.errorData.getJsonDataErrorForUser = "The scorecard application is unable to score the C-CDA document. Please try a file other than " + $scope.ccdaFileName + " or contact TestingServices@sitenv.org for help."
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

  var calculateCategoryIndex = function(chartIndexClicked) {
  	for(var i = 0; i < chartAndCategoryIndexMap.length; i++) {
  		var curPair = chartAndCategoryIndexMap[i];
  		var chartIndexStored = curPair.chartIndex;
  		var categoryIndexStored = curPair.categoryIndex;  		
  		if(chartIndexClicked === chartIndexStored) {
    		console.log("chartIndexClicked === chartIndexStored: returning categoryIndexStored: ");
    		console.log(categoryIndexStored);
  			return categoryIndexStored;
  		}
  	}
  	console.log("Error in calculateCategoryIndex(): Sending user to the first category.");
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
    console.log("elementId extracted: " + elementId);
    //move up one level for a cleaner look
    elementId = document.getElementById(elementId).parentNode.id;
    console.log("parentNode: " + elementId);
    $scope.jumpToElementViaId(elementId, weWait, timeToWaitInMiliseconds);
  };
    
    $scope.jumpToCategoryViaName = function(key, weWait, timeToWaitInMiliseconds) {
        elementId = detruncateCategoryName(key);
        elementId = document.getElementById(elementId).parentNode.id;
        $scope.jumpToElementViaId(elementId, weWait, timeToWaitInMiliseconds);
    };
    
  //*************HEAT MAP RELATED****************    

    var populateCategoryListsByGrade = function() {

      var allCategories = [];
      for (var catIndex = 0; catIndex < $scope.categoriesClone.length; catIndex++) {
        var curCategory = $scope.categoriesClone[catIndex];
        var name = curCategory.categoryName;
        var grade = curCategory.categoryGrade;
        //var issues = curCategory.numberOfIssues;
        var issues = curCategory.numberOfOccurrences;        
        var score = curCategory.categoryNumericalScore;
        var sectionData = {
          name: name,
          grade: grade,
          sectionIssueCount: issues,
          score: score
        };
        allCategories.push(sectionData);
      };
      
      if(allCategories.length < 12) {
        var emptySectionData = {
          name: "UNK",
          grade: "UNK",
          sectionIssueCount: 0,
          score: 0
        };
        if(allCategories.length === 10) {
          allCategories.push(emptySectionData, emptySectionData);
        } else if(allCategories.length === 11) {
          allCategories.push(emptySectionData);
        }
      }

      allCategories.sort(compareNumericalGradesInSectionData);

      $scope.categoryListByGradeFirstColumn = allCategories.slice(0, 4);
      $scope.categoryListByGradeSecondColumn = allCategories.slice(4, 8);
      $scope.categoryListByGradeThirdColumn = allCategories.slice(8, allCategories.length);

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
  	if($scope.categoryListByGradeFirstColumn.length < 1 
  			|| $scope.categoryListByGradeSecondColumn.length < 1 
  			|| $scope.categoryListByGradeThirdColumn.length < 1) {
  		return true;
  	}
  	return false;
  };
  
  $scope.getHeatMapCategoryName = function(row, columnNumber) {
  	if(!isCategoryListByGradeEmpty()) {
	    switch (columnNumber) {
	      case 1:
	        return $scope.categoryListByGradeFirstColumn[row].name;
	      case 2:
	        return $scope.categoryListByGradeSecondColumn[row].name;
	      case 3:
	        return $scope.categoryListByGradeThirdColumn[row].name;
	    }
  	}
    return "UNK.";
  };

  $scope.getHeatMapCategoryGrade = function(row, columnNumber) {
  	if(!isCategoryListByGradeEmpty()) {
	    switch (columnNumber) {
	      case 1:
	        return $scope.categoryListByGradeFirstColumn[row].grade;
	      case 2:
	        return $scope.categoryListByGradeSecondColumn[row].grade;
	      case 3:
	        return $scope.categoryListByGradeThirdColumn[row].grade;
	    }
  	}
    return "UNK.";
  };

  $scope.getHeatMapCategoryIssueCount = function(row, columnNumber) {
  	if(!isCategoryListByGradeEmpty()) {
	    switch (columnNumber) {
	      case 1:
	        return $scope.categoryListByGradeFirstColumn[row].sectionIssueCount;
	      case 2:
	        return $scope.categoryListByGradeSecondColumn[row].sectionIssueCount;
	      case 3:
	        return $scope.categoryListByGradeThirdColumn[row].sectionIssueCount;
	    }
  	}
    return "UNK.";
  };

  $scope.getGradeClass = function(row, columnNumber, classPrefix) {
  	if(!isCategoryListByGradeEmpty()) {
	    switch (columnNumber) {
	      case 1:
	        return $scope.calculateCategoryColor(classPrefix, $scope.categoryListByGradeFirstColumn[row].grade);
	      case 2:
	        return $scope.calculateCategoryColor(classPrefix, $scope.categoryListByGradeSecondColumn[row].grade);
	      case 3:
	        return $scope.calculateCategoryColor(classPrefix, $scope.categoryListByGradeThirdColumn[row].grade);
	    }
  	}
    return classPrefix + " unknownGradeColor";
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
            console.log("e.data.key: " + e.data.key);
            console.log("e.data.y: " + e.data.y);
            console.log("e.index: " + e.index);
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
            console.log("e.key: " + e.key);
            console.log("e.y: " + e.y);
            console.log("e.disabled: " + e.disabled);
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
    console.log("\n" + chartType + " Chart set...");
    var data = [];
    var categoryDisplayName = "";
    var chartIndex = 0;
    //loop through all the categories
    for (var catIndex = 0; catIndex < $scope.categories.length; catIndex++) {
      var curCategory = $scope.categories[catIndex];
      var numberOfIssues = $scope.categories[catIndex].categoryRubrics.length;
      var numberOfFailingIssuesPerSection = calculateNumberOfFailingIssuesPerSection(curCategory);            
      //apply logic to all categories whether they fail or not
      if (numberOfIssues) {
      	//make map for navigation
        chartAndCategoryIndexMap.push(new ChartAndCategoryTracker(chartIndex++, catIndex));
        //ensure visibility of longer category names
        categoryDisplayName = truncateCategoryName(curCategory.categoryName);
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
		console.log("chartAndCategoryIndexMap built:");
		console.log(chartAndCategoryIndexMap);
    if (isBarChart) {
      return [{
        key: chartType,
        values: data
      }];
    }
    return data;
  };

  var truncateCategoryName = function(curCategoryName) {
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
	  //bypassing for now as going for a clean no icon look...
	  return "";
	  console.log('panelDropdownElementId:');
	  console.log(panelDropdownElementId);	  
	  var panelElement = document.getElementById(panelDropdownElementId);
	  if(angular.element(panelElement).hasClass('collapsed')) {
		  return "glyphicon glyphicon-triangle-right pull-left";
	  }
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

}]);