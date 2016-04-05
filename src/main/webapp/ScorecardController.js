scApp.controller('ScorecardController', ['$scope', '$http', function($scope, $http) {

  $scope.debugData = {
    inDebugMode: false
  };
  $scope.jsonData = {};
  $scope.categories = {};
  $scope.errorData = {
    getJsonDataError: ""
  };
  categoryTypes = Object.freeze([
    "General", "Problems", "Medications", "Allergies", "Procedures", "Immunizations",
    "Laboratory Tests and Results", "Vital Signs", "Patient Information", "Encounters",
    "Miscellaneous"
  ]);
  $scope.totalNumberOfScorecardIssues = 0;
  $scope.totalNumberOfScorecardPoints = 0;

  //this is populated after the JSON is returned
  $scope.chartsData = {};

  //adjust the chart type here and it will be reflected live using $scope.charts.currentChartOption
  var chartFormatBools = {
    isIssuesPerSectionBar: false,
    isPointsPerSectionBar: true, //should probably always be TRUE since we can't display '0' on a pie chart
    isCriticalProblemAreasBar: false, //FALSE seems the clearest since the values are always '1' and not relevant to that number
    isPointsPerIssueBar: false,
    isCategoryGradesBar: false, //false is best for display as the names are longer due to including grade letters and the numbers aren't relevant
    isMultiServiceBar: true //true or false is fine but easy to use for true since will always be only 3 items
  };

  var chartTypeEnum = Object.freeze({
    ISSUES_PER_SECTION: "Issues per Section",
    POINTS_PER_CATEGORY: "Points Per Section",
    MULTI_SERVICE: "Multi-Service",
    CRITICAL_PROBLEM_AREAS: "Critcal Problem Area",
    POINTS_PER_ISSUE: "Positive Points Per Issue",
    CATEGORY_GRADES: "Section Grade"
  });

  $scope.getJsonData = function() {
    $http({
      method: "GET",
        url: "http://localhost:7080/ccda-smart-scorecard/ccdascorecardservice",
        respondType: 'json',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
    }).then(function mySuccess(response) {
      //get and cache the JSON data
      $scope.jsonData = response.data;
      //store the sub-data in a more usable/direct manner
      $scope.categories = $scope.jsonData.results.categoryList;
      $scope.finalGrade = $scope.jsonData.results.finalGrade;
      //populate the charts
      populateChartsData();
    }, function myError(response) {
      $scope.errorData.getJsonDataError = "Error retrieving data from server.";
    });
  };
  //instead of having as an iffy leaving this as an option to call from anywhere in future
  $scope.getJsonData();

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
    //0 is red, maxPoints is green, all other points in between are orange
    if (pointOrGrade === 0 || pointOrGrade === "C") {
      return classPrefix + "danger";
    } else if (pointOrGrade === passingComparator) {
      return classPrefix + "success";
    } else {
      return classPrefix + "warning";
    }
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
      legend: {
        margin: {
          top: 35,
          right: 0,
          bottom: 0,
          left: 0
        }
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
    LABORATORY_TESTS_AND_RESULTS: "Results",
    PATIENT_INFORMATION: "Patient",
    IMMUNIZATIONS: "Immun.",
    MISCELLANEOUS: "Misc."
  });

  var setChartData = function(chartType, isBarChart) {

    console.log("\n" + chartType + " Chart")
    var issueCountIsNotStored = $scope.totalNumberOfScorecardIssues === 0;
    var data = [];
    var categoryDisplayName = "";
    var zeroCount = 0;
    var positivePointCount = 0;
    //loop through all the categories
    // for (var curCategory in $scope.categories) {
    for (var catIndex = 0; catIndex < $scope.categories.length; catIndex++) {
      //only apply logic to categories with data
      var numberOfIssues = $scope.categories[catIndex].categoryRubrics.length;
      var curCategory = $scope.categories[catIndex];
      if (numberOfIssues > 0) {
        //ensure visibility of longer category names
        categoryDisplayName = truncateCategoryName(curCategory.categoryName);
        //prcess data for charts
        switch (chartType) {
          case chartTypeEnum.ISSUES_PER_SECTION:
            data.push(pushChartDataByDisplayType(isBarChart, categoryDisplayName, numberOfIssues, categoryDisplayName, numberOfIssues));
            //store for use in Multi-service chart
            $scope.totalNumberOfScorecardIssues += numberOfIssues;
            break;
          case chartTypeEnum.POINTS_PER_CATEGORY:
            var numberOfPoints = 0;
            //loop through all the rubrics within each category
            //used for a positive graph of points
            for (var curRubricIndex = 0; curRubricIndex < curCategory.categoryRubrics.length; curRubricIndex++) {
              var curRubric = curCategory.categoryRubrics[curRubricIndex];
              numberOfPoints += curRubric.actualPoints;
            }
            data.push(pushChartDataByDisplayType(isBarChart, categoryDisplayName, numberOfPoints, categoryDisplayName, numberOfPoints));
            //store this as a field as it may be useful elsewhere in the display
            $scope.totalNumberOfScorecardPoints += numberOfPoints;
            break;
          case chartTypeEnum.CRITICAL_PROBLEM_AREAS:
            numberOfPoints = -1;
            for (curRubricIndex = 0; curRubricIndex < curCategory.categoryRubrics.length; curRubricIndex++) {
              curRubric = curCategory.categoryRubrics[curRubricIndex];
              numberOfPoints = curRubric.actualPoints;
              if (numberOfPoints === 0) {
                data.push(pushChartDataByDisplayType(isBarChart, categoryDisplayName + " " + (curRubricIndex + 1), 1, categoryDisplayName + " " + curRubricIndex, 1));
                zeroCount++;
              }
            }
            //post rubric loop
            //if we are on the last category and no critical issues were found then we have none
            if (catIndex === ($scope.categories.length - 1) && zeroCount === 0) {
              data.push(pushChartDataByDisplayType(isBarChart, "No critical issues found", 1, "No critical issues found", 0));
            }
            break;
          case chartTypeEnum.POINTS_PER_ISSUE:
            numberOfPoints = 0;
            for (curRubricIndex = 0; curRubricIndex < curCategory.categoryRubrics.length; curRubricIndex++) {
              curRubric = curCategory.categoryRubrics[curRubricIndex];
              numberOfPoints = curRubric.actualPoints;
              if (numberOfPoints > 0) {
                data.push(pushChartDataByDisplayType(isBarChart, categoryDisplayName + " " + curRubricIndex, numberOfPoints, categoryDisplayName + " " + curRubricIndex, numberOfPoints));
                positivePointCount++;
              }
            }
            //post rubric loop
            if (catIndex === ($scope.categories.length - 1) && positivePointCount === 0) {
              data.push(pushChartDataByDisplayType(isBarChart, "No issues with positive points found", 1, "No issues with positive points found", 0));
            }
            break;
          case chartTypeEnum.CATEGORY_GRADES:
            var curGrade = curCategory.categoryGrade;
            data.push(pushChartDataByDisplayType(isBarChart, categoryDisplayName + ": " + curGrade, processGradeForChart(curGrade), categoryDisplayName + ": " + curGrade, processGradeForChart(curGrade)));
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
      return ShortCategoryNamesEnum.IMMUNIZATIONS;
    } else if (curCategoryName === categoryTypes[10]) {
      return ShortCategoryNamesEnum.MISCELLANEOUS;
    }
    return curCategoryName;
  }

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

  var processGradeForChart = function(gradeString) {
    if (gradeString === "A") {
      return 1;
    } else if (gradeString === "B") {
      return 2;
    }
    return 3;
  };

  var setMultiServiceChartData = function(isBarChart) {
    var issues = $scope.jsonData.results.externalIssueCount;
    var CCDA_DISPLAY_NAME = "C-CDA",
      VOCABULARY_DISPLAY_NAME = "Vocabulary",
      SCORECARD_DISPLAY_NAME = "Scorecard";
    if (isBarChart) {
      return [{
        key: chartTypeEnum.MULTI_SERVICE,
        values: [{
          "label": CCDA_DISPLAY_NAME,
          "value": issues.ccdaValidator
        }, {
          "label": VOCABULARY_DISPLAY_NAME,
          "value": issues.vocabularyValidator
        }, {
          "label": SCORECARD_DISPLAY_NAME,
          "value": $scope.totalNumberOfScorecardIssues
        }]
      }];
    }
    return [{
      key: CCDA_DISPLAY_NAME,
      y: issues.ccdaValidator
    }, {
      key: VOCABULARY_DISPLAY_NAME,
      y: issues.vocabularyValidator
    }, {
      key: SCORECARD_DISPLAY_NAME,
      y: $scope.totalNumberOfScorecardIssues
    }];
  };

  //called after JSON scorecard data is returned
  var populateChartsData = function() {
    $scope.chartsData = {
      issuesPerSection: {
        chartTitle: chartTypeEnum.ISSUES_PER_SECTION + " Results",
        chartDescription: "These results identify and chart the total amount of issues found in each section or category. Sections or categories without issues are not displayed in order to amplify focus on the issues at hand.",
        movingForwardText: "It may help to start work on the largest areas of the chart.",
        displayOnLeft: true,
        isBar: chartFormatBools.isIssuesPerSectionBar,
        chartData: setChartData(chartTypeEnum.ISSUES_PER_SECTION, chartFormatBools.isIssuesPerSectionBar),
        chartOption: getChartOption(chartFormatBools.isIssuesPerSectionBar)
      },
      categoryGrades: {
        chartTitle: chartTypeEnum.CATEGORY_GRADES + " Results",
        chartDescription: "These results display the grades for each section tested as whole.",
        movingForwardText: "A section can be graded A, B, or C. It may help to focus on the sections with the lowest grades first. For this reason, the lower the grade, the more space it takes up in the chart.",
        displayOnLeft: false,
        isBar: chartFormatBools.isCategoryGradesBar,
        chartData: setChartData(chartTypeEnum.CATEGORY_GRADES, chartFormatBools.isCategoryGradesBar),
        chartOption: getChartOption(chartFormatBools.isCategoryGradesBar)
      },
      criticalProblemAreas: {
        chartTitle: chartTypeEnum.CRITICAL_PROBLEM_AREAS + " Results",
        chartDescription: "These results identify and chart the issues which scored zero points.",
        movingForwardText: "Fixing these issues first will have the greatest impact on the quality of the document.",
        displayOnLeft: true,
        isBar: chartFormatBools.isCriticalProblemAreasBar,
        chartData: setChartData(chartTypeEnum.CRITICAL_PROBLEM_AREAS, chartFormatBools.isCriticalProblemAreasBar),
        chartOption: getChartOption(chartFormatBools.isCriticalProblemAreasBar)
      },
      pointsPerSection: {
        chartTitle: chartTypeEnum.POINTS_PER_CATEGORY + " Results",
        chartDescription: "These results identify and chart the total amount of points given in each section or category.",
        movingForwardText: "Click the 'View Scoring Criteia' button within each issue to understand exactly why the points were given. For each issue, zero points is considered the lowest grade and the highest amount of points is considered the highest grade. The ultimate goal would be to remove all isues. The next best goal is to attain the highest amount of points possible for each issue.",
        displayOnLeft: false,
        isBar: chartFormatBools.isPointsPerSectionBar,
        chartData: setChartData(chartTypeEnum.POINTS_PER_CATEGORY, chartFormatBools.isPointsPerSectionBar),
        chartOption: getChartOption(chartFormatBools.isPointsPerSectionBar)
      },
      pointsPerIssue: {
        chartTitle: chartTypeEnum.POINTS_PER_ISSUE + " Results",
        chartDescription: "These results identify and chart the amount of points given for each issue which scored higher than a zero.",
        movingForwardText: "Use this chart to understand and locate the amount of minor to moderate issues.",
        displayOnLeft: true,
        isBar: chartFormatBools.isPointsPerIssueBar,
        chartData: setChartData(chartTypeEnum.POINTS_PER_ISSUE, chartFormatBools.isPointsPerIssueBar),
        chartOption: getChartOption(chartFormatBools.isPointsPerIssueBar)
      },
      multiService: {
        chartTitle: chartTypeEnum.MULTI_SERVICE + " Results",
        chartDescription: "These results identify and chart the total amount of issues found by each SITE validation service.",
        movingForwardText: "This perspective is intended to help form a plan of attack for accurately resolving issues in the C-CDA document tested.",
        displayOnLeft: false,
        isBar: chartFormatBools.isMultiServiceBar,
        chartData: setMultiServiceChartData(chartFormatBools.isMultiServiceBar),
        chartOption: getChartOption(chartFormatBools.isMultiServiceBar)
      }
    };
  };

}]);