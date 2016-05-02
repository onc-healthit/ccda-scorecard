package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAVitalObs;
import org.sitenv.ccdaparsing.model.CCDAVitalOrg;
import org.sitenv.ccdaparsing.model.CCDAVitalSigns;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.repositories.LoincRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VitalsScorecard {
	
	@Autowired
	LoincRepository loincRepository;
	
	public Category getVitalsCategory(CCDAVitalSigns vitals, String birthDate)
	{
		
		Category vitalsCategory = new Category();
		vitalsCategory.setCategoryName("Vital Signs");
		
		List<CCDAScoreCardRubrics> vitalsScoreList = new ArrayList<CCDAScoreCardRubrics>();
		vitalsScoreList.add(getTimePrecisionScore(vitals));
		vitalsScoreList.add(getValidDateTimeScore(vitals,birthDate));
		vitalsScoreList.add(getValidDisplayNameScoreCard(vitals));
		vitalsScoreList.add(getValidLoincCodesScore(vitals));
		vitalsScoreList.add(getValidUCUMScore(vitals));
		vitalsScoreList.add(getApprEffectivetimeScore(vitals));
		
		vitalsCategory.setCategoryRubrics(vitalsScoreList);
		vitalsCategory.setCategoryGrade(calculateSectionGrade(vitalsScoreList));
		
		return vitalsCategory;
		
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
		
		if(percentage <= 35)
		{
			return "c";
		}else if(percentage >= 35 && percentage <=70)
		{
			return "B";
		}else if(percentage >=70 && percentage <=100)
		{
			return "A";
		}else 
			return "UNKNOWN GRADE";
	}
	
	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.VITALS_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		
		int actualPoints = 0;
		int maxPoints = 0;
		
		if(vitals != null)
		{
			for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
			{
				maxPoints = maxPoints + 2;
				if(vitalOrg.getEffTime() != null)
				{
					if(vitalOrg.getEffTime().getLow() != null)
					{
						if(ApplicationUtil.validateDayFormat(vitalOrg.getEffTime().getLow().getValue()));
						{
							actualPoints++;
						}
					}
					if(vitalOrg.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.validateDayFormat(vitalOrg.getEffTime().getHigh().getValue()));
						{
							actualPoints++;
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
				{
					for (CCDAVitalObs vitalObs : vitalOrg.getVitalObs() )
					{
						maxPoints++;
						if(vitalObs.getMeasurementTime() != null)
						{
							if(ApplicationUtil.validateDayFormat(vitalObs.getMeasurementTime().getValue()));
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
			timePrecisionScore.setComment("All the time elememts under Vitals section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under Vitals are not properly precisioned");
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
	
	
	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAVitalSigns vitals, String birthDate)
	{
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setPoints(ApplicationConstants.VALID_TIME_POINTS);
		validateTimeScore.setRequirement(ApplicationConstants.VITALS_TIMEDATE_VALID_REQUIREMENT);
		validateTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_VALIDATION.getSubcategory());
		
		int actualPoints = 0;
		int maxPoints = 0;
		
		if(vitals != null)
		{
			for (CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
			{
				maxPoints = maxPoints + 2;
				if(vitalOrg.getEffTime() != null)
				{
					if(vitalOrg.getEffTime().getLow() != null)
					{
						if(ApplicationUtil.validateDate(vitalOrg.getEffTime().getLow().getValue()) &&
								ApplicationUtil.checkDateRange(birthDate, vitalOrg.getEffTime().getLow().getValue(),ApplicationConstants.DAY_FORMAT))
						{
							actualPoints++;
						}
					}
					if(vitalOrg.getEffTime().getHigh() != null)
					{
						if(ApplicationUtil.validateDate(vitalOrg.getEffTime().getHigh().getValue()) &&
								ApplicationUtil.checkDateRange(birthDate, vitalOrg.getEffTime().getHigh().getValue(),ApplicationConstants.DAY_FORMAT))
						{
							actualPoints++;
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
				{
					for (CCDAVitalObs vitalObs : vitalOrg.getVitalObs() )
					{
						maxPoints++;
						if(vitalObs.getMeasurementTime() != null)
						{
							if(ApplicationUtil.validateDate(vitalObs.getMeasurementTime().getValue()) &&
									ApplicationUtil.checkDateRange(birthDate, vitalObs.getMeasurementTime().getValue(),ApplicationConstants.DAY_FORMAT))
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
			validateTimeScore.setComment("All the time elememts under Vitals section are valid.");
		}else
		{
			validateTimeScore.setComment("Some effective time elements under Lab Vitals are not valid or not present within human lifespan");
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
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.VITALS_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(vitals != null)
		{
			maxPoints++;
			if(vitals.getSectionCode().getDisplayName()!= null)
			{
				if(ApplicationUtil.validateDisplayName(vitals.getSectionCode().getCode(), 
						ApplicationConstants.CODE_SYSTEM_MAP.get(vitals.getSectionCode().getCodeSystem()),
														vitals.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
			{
				for (CCDAVitalOrg vitalsOrg : vitals.getVitalsOrg())
				{
					maxPoints++;
					if(vitalsOrg.getOrgCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(vitalsOrg.getOrgCode().getCode(), 
								ApplicationConstants.CODE_SYSTEM_MAP.get(vitalsOrg.getOrgCode().getCodeSystem()),
																		vitalsOrg.getOrgCode().getDisplayName()))
						{
							actualPoints++;
						}
					}
					
					if(!ApplicationUtil.isEmpty(vitalsOrg.getVitalObs()))
					{
					
						for(CCDAVitalObs vitalsObs : vitalsOrg.getVitalObs())
						{
							maxPoints++;
							if(vitalsObs.getVsCode() != null)
							{
								if(ApplicationUtil.validateDisplayName(vitalsObs.getVsCode().getCode(), 
										ApplicationConstants.CODE_SYSTEM_MAP.get(vitalsObs.getVsCode().getCodeSystem()),
																	vitalsObs.getVsCode().getDisplayName()))
								{
									actualPoints++;
								}
							}
						}
					}
				}
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Vitals are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under Vitals are not having valid display name");
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
	
	public CCDAScoreCardRubrics getValidUCUMScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validateUCUMScore = new CCDAScoreCardRubrics();
		validateUCUMScore.setPoints(ApplicationConstants.VALID_UCUM_CODE_POINTS);
		validateUCUMScore.setRequirement(ApplicationConstants.VITALS_UCUM_REQUIREMENT);
		validateUCUMScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.UCUM_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(vitals != null && !ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
		{
			for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
			{
				if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
				{
					for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
					{
						maxPoints++;
						if(vitalObs.getVsResult() != null)
						{
							if(loincRepository.foundUCUMUnitsForLoincCode(vitalObs.getVsCode().getCode(),vitalObs.getVsResult().getUnits()))
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
			validateUCUMScore.setComment("All the LOINC codes under vitals are having proper UCUM units");
		}else
		{
			validateUCUMScore.setComment("Some LOINC codes under vitals doesnt have proper UCUM units");
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
	
	public CCDAScoreCardRubrics getValidLoincCodesScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validatLoincCodeScore = new CCDAScoreCardRubrics();
		validatLoincCodeScore.setPoints(ApplicationConstants.VITALS_LOINC_CODES_POINTS);
		validatLoincCodeScore.setRequirement(ApplicationConstants.VITALS_LOIN_CODE_REQ);
		validatLoincCodeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.VITAL_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(vitals != null && !ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
		{
			for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
			{
			   if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
			   {
				   for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
				   {
					   maxPoints++;
					   if(ApplicationUtil.validateCodeForValueset(vitalObs.getVsCode().getCode(), ApplicationConstants.HITSP_VITAL_VALUESET_OID))
					   {
						   actualPoints++;
					   }
				   }
			   }
			}
		}
		
		if(maxPoints!=0 && maxPoints == actualPoints)
		{
			validatLoincCodeScore.setComment("All Vital observation codes are expressed with LOINC");
		}else
		{
			validatLoincCodeScore.setComment("Some Vital obseervation codes are not expressed with LOINC codes");
		}
		
		if(maxPoints!= 0)
		{
			validatLoincCodeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else 
			validatLoincCodeScore.setActualPoints(0);
		
		validatLoincCodeScore.setMaxPoints(4);
		return validatLoincCodeScore;
		
	}
	
	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAVitalSigns vitals)
	{
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setPoints(ApplicationConstants.VITALS_APPR_TIME_POINTS);
		validateApprEffectiveTimeScore.setRequirement(ApplicationConstants.VITALS_ORG_DATE_ALIGN);
		validateApprEffectiveTimeScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_ALIGN.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(vitals != null && !ApplicationUtil.isEmpty(vitals.getVitalsOrg()))
		{
			for(CCDAVitalOrg vitalOrg : vitals.getVitalsOrg())
			{
				if(vitalOrg.getEffTime()!= null && (vitalOrg.getEffTime().getLowPresent()))
				if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
				{
					for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
					{
						maxPoints++;
						if(vitalObs.getMeasurementTime() != null && vitalOrg.getEffTime() != null)
						{
							if(ApplicationUtil.checkDateRange(vitalOrg.getEffTime().getLow().getValue(), vitalObs.getMeasurementTime().getValue(), 
																vitalOrg.getEffTime().getHigh().getValue(), ApplicationConstants.DAY_FORMAT))
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
			validateApprEffectiveTimeScore.setComment("All Vitals observation effective time are aligned with Organizer effective time");
		}else
		{
			validateApprEffectiveTimeScore.setComment("Some Vitals observation effective time are not aligned with Organizer effective time");
		}
		
		if(maxPoints!=0)
		{
			validateApprEffectiveTimeScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateApprEffectiveTimeScore.setActualPoints(0);
		}
		validateApprEffectiveTimeScore.setMaxPoints(4);
		return validateApprEffectiveTimeScore;
	}

}
