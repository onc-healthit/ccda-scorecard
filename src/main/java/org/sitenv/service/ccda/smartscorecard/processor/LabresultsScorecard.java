package org.sitenv.service.ccda.smartscorecard.processor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDALabResult;
import org.sitenv.ccdaparsing.model.CCDALabResultObs;
import org.sitenv.ccdaparsing.model.CCDALabResultOrg;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.repositories.LoincRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LabresultsScorecard {
	
	@Autowired
	LoincRepository loincRepository;
	
	public Category getLabResultsCategory(CCDALabResult labResults, CCDALabResult labTests, String birthDate)throws UnsupportedEncodingException
	{
		
		CCDALabResult results =null;
		if(labResults!= null && !ApplicationUtil.isEmpty(labResults.getResultOrg()))
		{
			results = labResults;
			if(labTests!= null && !ApplicationUtil.isEmpty(labTests.getResultOrg()))
			{
				results.getResultOrg().addAll(labTests.getResultOrg());

			}
		}
		
		Category labResultsCategory = new Category();
		labResultsCategory.setCategoryName("Laboratory Tests and Results");
		
		List<CCDAScoreCardRubrics> labResultsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		labResultsScoreList.add(getTimePrecisionScore(results));
		labResultsScoreList.add(getValidDateTimeScore(results,birthDate));
		labResultsScoreList.add(getValidDisplayNameScoreCard(results));
		labResultsScoreList.add(getValidUCUMScore(labResults));
//		labResultsScoreList.add(getValidLoincCodesScore(results));
		
		labResultsCategory.setCategoryRubrics(labResultsScoreList);
		labResultsCategory.setCategoryGrade(calculateSectionGrade(labResultsScoreList));
		
		return labResultsCategory;
		
	}
	
	public String calculateSectionGrade(List<CCDAScoreCardRubrics> rubricsList)
	{
		int actualPoints=0;
		int maxPoints = 0;
		float percentage ;
		for(CCDAScoreCardRubrics rubrics : rubricsList)
		{
			actualPoints = actualPoints + rubrics.getActualPoints();
			maxPoints = maxPoints + rubrics.getMaxPoints();
		}
		
		percentage = (actualPoints * 100)/maxPoints;
		
		if(percentage < 70)
		{
			return "D";
		}else if (percentage >=70 && percentage <80)
		{
			return "C";
		}else if(percentage >=80 && percentage <85)
		{
			return "B-";
		}else if(percentage >=85 && percentage <90)
		{
			return "B+";
		}else if(percentage >=90 && percentage <95)
		{
			return "A-";
		}else if(percentage >=95 && percentage <=100)
		{
			return "A+";
		}else
		{
			return "UNKNOWN GRADE";
		}
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDALabResult labResults)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.LABRESULTS_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		
		if(labResults != null)
		{
			for (CCDALabResultOrg resultOrg : labResults.getResultOrg())
			{
				maxPoints = maxPoints + 2;
				if(resultOrg.getEffTime() != null)
				{
					if(resultOrg.getEffTime().getLow() != null)
					{
						if(ApplicationUtil.validateDayFormat(resultOrg.getEffTime().getLow().getValue()));
						{
							actualPoints++;
						}
					}
					if(resultOrg.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.validateDayFormat(resultOrg.getEffTime().getHigh().getValue()));
						{
							actualPoints++;
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
				{
					for (CCDALabResultObs resultObs : resultOrg.getResultObs() )
					{
						maxPoints++;
						if(resultObs.getMeasurementTime() != null)
						{
							if(ApplicationUtil.validateDayFormat(resultObs.getMeasurementTime().getValue()));
							{
								actualPoints++;
							}
							
						}
					}
				}
			}
		}

		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under Results section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Results are not properly precisioned");
		}
		
		if(maxPoints!=0)
		{
			timePrecisionScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			timePrecisionScore.setActualPoints(0);
		}
		
		timePrecisionScore.setMaxPoints(4);
		return timePrecisionScore;
	}
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDALabResult labResults, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validateTimeScore.setRequirement(ApplicationConstants.LABRESULTS_TIMEDATE_VALID_REQUIREMENT);
		validateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints =0;
		int maxPoints = 0;
		
		if(labResults != null)
		{
			for (CCDALabResultOrg resultOrg : labResults.getResultOrg())
			{
				maxPoints = maxPoints + 2;
				if(resultOrg.getEffTime() != null)
				{
					if(resultOrg.getEffTime().getLow() != null)
					{
						if(resultOrg.getEffTime().getLow() != null)
						{
							if(ApplicationUtil.validateDate(resultOrg.getEffTime().getLow().getValue()) &&
									ApplicationUtil.checkDateRange(birthDate, resultOrg.getEffTime().getLow().getValue(),ApplicationConstants.DAY_FORMAT))
							{
								actualPoints++;
							}
						}
					}
					if(resultOrg.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.validateDate(resultOrg.getEffTime().getHigh().getValue()) &&
								ApplicationUtil.checkDateRange(birthDate, resultOrg.getEffTime().getHigh().getValue(),ApplicationConstants.DAY_FORMAT))
						{
							actualPoints++;
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
				{
					for (CCDALabResultObs resultObs : resultOrg.getResultObs() )
					{
						maxPoints++;
						if(resultObs.getMeasurementTime() != null)
						{
							if(ApplicationUtil.validateDate(resultObs.getMeasurementTime().getValue()) &&
									ApplicationUtil.checkDateRange(birthDate, resultObs.getMeasurementTime().getValue(),ApplicationConstants.DAY_FORMAT))
							{
								actualPoints++;
							}
							
						}
					}
				}
			}
		}

		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateTimeScore.setComment("All the time elememts under Results are valid.");
		}else
		{
			validateTimeScore.setComment("Some effective time elements under Results are not valid or not present within human lifespan");
		}
		
		if(maxPoints!=0)
		{
			validateTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateTimeScore.setActualPoints(0);
		}
		validateTimeScore.setMaxPoints(4);
		return validateTimeScore;
	}
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDALabResult labresults)throws UnsupportedEncodingException
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.LABRESULTS_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());

		int maxPoints = 0;
		int actualPoints = 0;
		if(labresults != null)
		{
			maxPoints++;
			if(labresults.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(labresults.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(labresults.getSectionCode().getCodeSystem()),
												labresults.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(labresults.getResultOrg()))
			{
				for (CCDALabResultOrg resultOrg : labresults.getResultOrg())
				{
					maxPoints++;
					if(ApplicationUtil.validateDisplayName(resultOrg.getOrgCode().getCode(), 
							ApplicationConstants.CODE_SYSTEM_MAP.get(resultOrg.getOrgCode().getCodeSystem()),
							resultOrg.getOrgCode().getDisplayName()))
					{
						actualPoints++;
					}
					
					if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
					{
						for (CCDALabResultObs resultobs : resultOrg.getResultObs())
						{
							maxPoints++;
							if(ApplicationUtil.validateDisplayName(resultobs.getResultCode().getCode(), 
									ApplicationConstants.CODE_SYSTEM_MAP.get(resultobs.getResultCode().getCodeSystem()),
									resultobs.getResultCode().getDisplayName()))
							{
								actualPoints++;
							}
						}
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Results are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under Immunization are not having valid display name");
		}
		
		if(maxPoints!=0)
		{
			validateDisplayNameScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateDisplayNameScore.setActualPoints(0);
		}
		validateDisplayNameScore.setMaxPoints(4);
		return validateDisplayNameScore;
	}
	
	public  CCDAScoreCardRubrics getValidUCUMScore(CCDALabResult labresults)
	{
		CCDAScoreCardRubrics validateUCUMScore = new CCDAScoreCardRubrics();
		validateUCUMScore.setPoints(ApplicationConstants.VALID_UCUM_CODE_POINTS);
		validateUCUMScore.setRequirement(ApplicationConstants.LABRESULTS_UCUM_REQUIREMENT);
		validateUCUMScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.UCUM_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(labresults != null && !ApplicationUtil.isEmpty(labresults.getResultOrg()))
		{
			for(CCDALabResultOrg resultsOrg : labresults.getResultOrg())
			{
				if(!ApplicationUtil.isEmpty(resultsOrg.getResultObs()))
				{
					for(CCDALabResultObs resultsObs : resultsOrg.getResultObs())
					{
						maxPoints++;
						if(resultsObs.getResultCode() != null && resultsObs.getResults() != null)
						{
							if(loincRepository.foundUCUMUnitsForLoincCode(resultsObs.getResultCode().getCode(),resultsObs.getResults().getUnits()))
							{
								actualPoints++;
							}
						}
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateUCUMScore.setComment("All the LOINC codes under Results are having proper UCUM units");
		}else
		{
			validateUCUMScore.setComment("Some LOINC codes under Results doesnt have proper UCUM units");
		}
		
		if(maxPoints!=0)
		{
			validateUCUMScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateUCUMScore.setActualPoints(0);
		}
		validateUCUMScore.setMaxPoints(4);
		return validateUCUMScore;
	}
	
	public CCDAScoreCardRubrics getValidLoincCodesScore(CCDALabResult labresults)
	{
		CCDAScoreCardRubrics validatLoincCodeScore = new CCDAScoreCardRubrics();
		validatLoincCodeScore.setPoints(ApplicationConstants.LABRESULTS_LOINC_CODES_POINTS);
		validatLoincCodeScore.setRequirement(ApplicationConstants.LABRESULTS_LOIN_CODE_REQ);
		validatLoincCodeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.LABRESULT_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(labresults != null && !ApplicationUtil.isEmpty(labresults.getResultOrg()))
		{
			for(CCDALabResultOrg resultOrg : labresults.getResultOrg())
			{
			   if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
			   {
				   for(CCDALabResultObs resultObs : resultOrg.getResultObs())
				   {
					   maxPoints++;
					   if(loincRepository.findByCode(resultObs.getResultCode().getCode()))
					   {
						   actualPoints++;
					   }
				   }
			   }
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validatLoincCodeScore.setComment("All Lab results are expressed with LOINC");
		}else
		{
			validatLoincCodeScore.setComment("Some Lab results are not expressed with LOINC codes");
		}
		
		if(maxPoints!= 0)
		{
			validatLoincCodeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else 
			validatLoincCodeScore.setActualPoints(0);
		
		validatLoincCodeScore.setMaxPoints(4);
		return validatLoincCodeScore;
		
	}
}
