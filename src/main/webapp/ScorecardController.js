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
    "General", "Problems", "Medications", "Allergies", "Procedures", "Immunizations",
    "Laboratory Tests and Results", "Vital Signs", "Patient Information", "Encounters",
    "Miscellaneous"
  ]);

  $scope.ccdaFileName = "Scoring...";
  $scope.totalNumberOfScorecardIssues = 0;
  $scope.totalNumberOfFailingScorecardIssues = 0;
  
  //this is populated after the JSON is returned
  $scope.chartsData = {};
  
  //if the SiteUploadControllers $scope.jsonScorecardData changes, 
  //then the service was called and returned new results,
  //so we process them so it is reflected in the view
  $scope.$watch('jsonScorecardData', function() {
	  console.log($scope.jsonScorecardData);
	  if(!jQuery.isEmptyObject($scope.jsonScorecardData)) {		  
		  $scope.ccdaFileName = $scope.ccdaUploadData.fileName;
		  getAndProcessUploadControllerData();
		  $scope.uploadDisplay.isLoading = false;
	  }
  }, true);
  
  //if isLoading changes (from false to true)
  //then we reset out local scorecard data  
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
	  $scope.totalNumberOfScorecardIssues = 0;
	  $scope.totalNumberOfFailingScorecardIssues = 0;
	  $scope.chartsData = {};
	  $scope.jsonData = {};
	  $scope.categories = {};
	  $scope.errorData.getJsonDataError = "";
	  $scope.errorData.getJsonDataErrorForUser = "";
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
	  populateTotalNumberOfScorecardIssues();
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
	      $scope.errorData.getJsonDataErrorForUser = "The C-CDA R2.1 Scorecard web service has failed to return valid data. Please try a file other than " + $scope.ccdaFileName + " and report the issue to TestingServices@sitenv.org."
		  $scope.uploadDisplay.isLoading = false;
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
	return classPrefix + "info";
  };

  var jumpToCategoryViaIndex = function(index, weWait, timeToWaitInMiliseconds) {
    $scope.jumpToElementViaId("catAccordion" + index, weWait, timeToWaitInMiliseconds);
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

  //***************CHART RELATED*****************

  $scope.pieChartOptions = {
    chart: {
      type: 'pieChart',
      height: 600,
      x: function(d) {
        return d.key;
      },
      y: function(d) {
        return d.y;
      },
      showLabels: true,
      duration: 500,
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
          top: 35,
          right: 0,
          bottom: 0,
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
    PATIENT_INFORMATION: "Patient",
    IMMUNIZATIONS: "Immun.",
    MISCELLANEOUS: "Misc."
  });

  var setChartData = function(chartType, isBarChart) {
    console.log("\n" + chartType + " Chart set...");
    var issueCountIsNotStored = $scope.totalNumberOfScorecardIssues === 0;
    var data = [];
    var categoryDisplayName = "";
    //loop through all the categories
    for (var catIndex = 0; catIndex < $scope.categories.length; catIndex++) {
      //only apply logic to categories with data
      var numberOfIssues = $scope.categories[catIndex].categoryRubrics.length;
      var curCategory = $scope.categories[catIndex];
      if (numberOfIssues > 0) {
        //ensure visibility of longer category names
        categoryDisplayName = truncateCategoryName(curCategory.categoryName);
        //prcess data for charts
        switch (chartType) {
          case chartTypeEnum.ISSUES_PER_SECTION_WITH_GRADE:
            var curGrade = curCategory.categoryGrade;
            var keyAndLabelVal = categoryDisplayName + ": " + curGrade + " (" + numberOfIssues + ")";
            data.push(pushChartDataByDisplayType(isBarChart, keyAndLabelVal, numberOfIssues, keyAndLabelVal, numberOfIssues));
            break;
        }
      }
    }

    if (isBarChart) {
      return [{
        key: chartType,
        values: data
      }];
    }
    return data;
  };

  var truncateCategoryName = function(curCategoryName) {
    if (curCategoryName === categoryTypes[6]) {
      return ShortCategoryNamesEnum.LABORATORY_TESTS_AND_RESULTS;
    } else if (curCategoryName === categoryTypes[8]) {
      return ShortCategoryNamesEnum.PATIENT_INFORMATION;
    } else if (curCategoryName === categoryTypes[5]) {
      // return ShortCategoryNamesEnum.IMMUNIZATIONS;
    } else if (curCategoryName === categoryTypes[10]) {
      return ShortCategoryNamesEnum.MISCELLANEOUS;
    }
    return curCategoryName;
  };

  var detruncateCategoryName = function(curCategoryName) {
    if (curCategoryName === ShortCategoryNamesEnum.LABORATORY_TESTS_AND_RESULTS) {
      return categoryTypes[6];
    } else if (curCategoryName === ShortCategoryNamesEnum.PATIENT_INFORMATION) {
      return categoryTypes[8];
    } else if (curCategoryName === ShortCategoryNamesEnum.IMMUNIZATIONS) {
      return categoryTypes[5];
    } else if (curCategoryName === ShortCategoryNamesEnum.MISCELLANEOUS) {
      return categoryTypes[10];
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
        chartDescription: "The C-CDA scorecard chart to the left provides a top level view of the data domains, the grade for each domain and the number of issues present in the C-CDA document that was scored.",
        movingForwardText: "Providers and implementers can quickly focus on specific domains which have data quality issues and use the detailed reports below to improve the data quality of the C-CDA.",
        displayOnLeft: true,
        isBar: chartFormatBools.isIssuesPerSectionWithGradeBar,
        chartData: setChartData(chartTypeEnum.ISSUES_PER_SECTION_WITH_GRADE, chartFormatBools.isIssuesPerSectionWithGradeBar),
        chartOption: getChartOption(chartFormatBools.isIssuesPerSectionWithGradeBar)
      }
    };
  };

  var populateTotalNumberOfScorecardIssues = function() {
    for (var catIndex = 0; catIndex < $scope.categories.length; catIndex++) {

      var numberOfIssues = $scope.categories[catIndex].categoryRubrics.length;
      var curCategory = $scope.categories[catIndex];
      if (numberOfIssues > 0) {
        //store count for total possible issues
        $scope.totalNumberOfScorecardIssues += numberOfIssues;
      }

      for (var rubricIndex = 0; rubricIndex < curCategory.categoryRubrics.length; rubricIndex++) {
        var curRubric = curCategory.categoryRubrics[rubricIndex];
        //store count for issues which need attention
        if (curRubric.actualPoints !== curRubric.maxPoints) {
          $scope.totalNumberOfFailingScorecardIssues++;
        }
      }

    }
  };

}]);