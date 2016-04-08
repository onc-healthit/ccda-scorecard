package org.sitenv.service.ccda.smartscorecard.processor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAEncounter;
import org.sitenv.ccdaparsing.model.CCDAEncounterActivity;
import org.sitenv.ccdaparsing.model.CCDAEncounterDiagnosis;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class EncounterScorecard {
	
	public static Category getEncounterCategory(CCDAEncounter encounter)
	{
		
		Category encounterCategory = new Category();
		encounterCategory.setCategoryName("Encounter");
		
		List<CCDAScoreCardRubrics> encounterScoreList = new ArrayList<CCDAScoreCardRubrics>();
		encounterScoreList.add(getEncounterSectionScore(encounter));
		encounterScoreList.add(getEncounterSDLScore(encounter));
		encounterScoreList.add(getTimePrecisionScore(encounter));
		encounterScoreList.add(getTranslationsScore(encounter));
		encounterCategory.setCategoryRubrics(encounterScoreList);
		
		encounterCategory.setCategoryGrade("B");
		
		return encounterCategory;
		
	}
	
	
	public static CCDAScoreCardRubrics getEncounterSectionScore(CCDAEncounter encounter)
	{
		CCDAScoreCardRubrics encounterSectionScore = new CCDAScoreCardRubrics();
		encounterSectionScore.setPoints(ApplicationConstants.ENCOUNTER_SECTION_POINTS);
		encounterSectionScore.setRequirement(ApplicationConstants.ENCOUNTER_SECTION_REQUIREMENT);
		encounterSectionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.SECTION.getSubcategory());
		encounterSectionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints());
		int actualPoints = 0;
		if(encounter !=null)
		{
			actualPoints++;
			
		}
		
		if(actualPoints == ApplicationConstants.SUBCATEGORIES.SECTION.getMaxPoints())
		{
			encounterSectionScore.setComment("Encounter Section present in CCDA document");
		}else
			encounterSectionScore.setComment("Encounter Section is missing in CCDA document");
		
		encounterSectionScore.setActualPoints(actualPoints);
		
		return encounterSectionScore;
	}
	
	public static CCDAScoreCardRubrics getEncounterSDLScore(CCDAEncounter encounter)
	{
		CCDAScoreCardRubrics encounterSDLScore = new CCDAScoreCardRubrics();
		encounterSDLScore.setPoints(ApplicationConstants.ENCOUNTER_SDL_POINTS);
		encounterSDLScore.setRequirement(ApplicationConstants.ENCOUNTER_SDL_REQUIREMENT);
		encounterSDLScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.ENCOUNTER_SDL.getSubcategory());
		encounterSDLScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.ENCOUNTER_SDL.getMaxPoints());
		int actualPoints = 0;
		if(encounter!= null)
		{
			for (CCDAEncounterActivity encounterActivity : encounter.getEncActivities())
			{
				if(encounterActivity.getSdLocs().size() > 0)
				{
					actualPoints++;
				}
			}
		}
		
		int sdlPercentage = (actualPoints/encounter.getEncActivities().size())*100;
		
		if(sdlPercentage == 0)
		{
			encounterSDLScore.setActualPoints(0);
			encounterSDLScore.setComment(ApplicationConstants.ENCOUNTER_SDL_POINTS.get(0));
			encounterSDLScore.setActualPoints(0);
		}else if(sdlPercentage > 0 && sdlPercentage <25)
		{
			encounterSDLScore.setActualPoints(1);
			encounterSDLScore.setComment(ApplicationConstants.ENCOUNTER_SDL_POINTS.get(1));
			encounterSDLScore.setActualPoints(1);
		}else if(sdlPercentage > 25 && sdlPercentage <50)
		{
			encounterSDLScore.setActualPoints(2);
			encounterSDLScore.setComment(ApplicationConstants.ENCOUNTER_SDL_POINTS.get(2));
			encounterSDLScore.setActualPoints(2);
		}else if (sdlPercentage > 50 && sdlPercentage < 75)
		{
			encounterSDLScore.setActualPoints(3);
			encounterSDLScore.setComment(ApplicationConstants.ENCOUNTER_SDL_POINTS.get(3));
			encounterSDLScore.setActualPoints(3);
		}else if (sdlPercentage > 75 && sdlPercentage < 100)
		{
			encounterSDLScore.setActualPoints(4);
			encounterSDLScore.setComment(ApplicationConstants.ENCOUNTER_SDL_POINTS.get(4));
			encounterSDLScore.setActualPoints(4);
		}else if(sdlPercentage == 100)
		{
			encounterSDLScore.setActualPoints(5);
			encounterSDLScore.setComment(ApplicationConstants.ENCOUNTER_SDL_POINTS.get(5));
			encounterSDLScore.setActualPoints(5);
		}
			
		return encounterSDLScore;
	}
	
	public static CCDAScoreCardRubrics getTimePrecisionScore(CCDAEncounter encounter)
	{
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setPoints(ApplicationConstants.ENCOUNTER_TIME_PRECISION_POINTS);
		timePrecisionScore.setRequirement(ApplicationConstants.ENCOUNTER_TIME_PRECISION_REQUIREMENT);
		timePrecisionScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getSubcategory());
		timePrecisionScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints());
		
		int actualPoints =1;
		try
		{
			if(encounter != null)
			{
				for (CCDAEncounterActivity encounterActivity : encounter.getEncActivities())
				{
					if(encounterActivity.getEffectiveTime() != null)
					{
						if(encounterActivity.getEffectiveTime().getValue() != null)
						{
							ApplicationUtil.getTsFromString(encounterActivity.getEffectiveTime().getValue(), ApplicationConstants.MINUTE_FORMAT);
						}
						
					}
					
					if(!ApplicationUtil.isEmpty(encounterActivity.getIndications()))
					{
						for (CCDAProblemObs problemObs : encounterActivity.getIndications() )
						{
							if(problemObs.getEffTime() != null)
							{
								if(problemObs.getEffTime().getHigh() != null)
								{
									ApplicationUtil.convertStringToDate(problemObs.getEffTime().getHigh().getValue(), ApplicationConstants.DAY_FORMAT);
								}
								if(problemObs.getEffTime().getLow() != null)
								{
									ApplicationUtil.convertStringToDate(problemObs.getEffTime().getLow().getValue(), ApplicationConstants.DAY_FORMAT);
								}
							}
						}
					}
					
					if(!ApplicationUtil.isEmpty(encounterActivity.getDiagnoses()))
					{
						for (CCDAEncounterDiagnosis encounterDiagnosis : encounterActivity.getDiagnoses())
						{
							if(!ApplicationUtil.isEmpty(encounterDiagnosis.getProblemObs()))
							{
								for (CCDAProblemObs problemObs : encounterDiagnosis.getProblemObs() )
								{
									if(problemObs.getEffTime() != null)
									{
										if(problemObs.getEffTime().getHigh() != null)
										{
											ApplicationUtil.convertStringToDate(problemObs.getEffTime().getHigh().getValue(), ApplicationConstants.DAY_FORMAT);
										}
										if(problemObs.getEffTime().getLow() != null)
										{
											ApplicationUtil.convertStringToDate(problemObs.getEffTime().getLow().getValue(), ApplicationConstants.DAY_FORMAT);
										}
									}
								}
							}
						}
					}
				}
			}

		}catch(ParseException e)
		{
			actualPoints =0;
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			timePrecisionScore.setComment("All the time elememts under encounter section has proper precision");
		}else
		{
			timePrecisionScore.setComment("Some effective time elements under encounter are not properly precisioned");
		}
		
		timePrecisionScore.setActualPoints(actualPoints);
		return timePrecisionScore;
	}
	
	
	public static CCDAScoreCardRubrics getTranslationsScore(CCDAEncounter encounter)
	{
		CCDAScoreCardRubrics translationsScore = new CCDAScoreCardRubrics();
		translationsScore.setPoints(ApplicationConstants.ENCOUNTER_TRANSLATIONS_POINTS);
		translationsScore.setRequirement(ApplicationConstants.ENCOUNTER_TRANSLATIONS_REQUIREMENT);
		translationsScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getSubcategory());
		translationsScore.setMaxPoints(ApplicationConstants.SUBCATEGORIES.TRANSLATIONS.getMaxPoints());
		
		int actualPoints =1;
		
		if(encounter != null)
		{
			for (CCDAEncounterActivity encounterActivity : encounter.getEncActivities())
			{
				if(!ApplicationUtil.isEmpty(encounterActivity.getIndications()))
				{
					for (CCDAProblemObs problemObs : encounterActivity.getIndications() )
					{
						if(!ApplicationUtil.isEmpty(problemObs.getTranslationProblemType()))
						{
						   if(problemObs.getProblemCode() == null)
						   {
							   actualPoints = 0;
						   }
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(encounterActivity.getDiagnoses()))
				{
					for (CCDAEncounterDiagnosis encounterDiagnosis : encounterActivity.getDiagnoses())
					{
						if(!ApplicationUtil.isEmpty(encounterDiagnosis.getProblemObs()))
						{
							for (CCDAProblemObs problemObs : encounterDiagnosis.getProblemObs() )
							{
								if(!ApplicationUtil.isEmpty(problemObs.getTranslationProblemType()))
								{
								   if(problemObs.getProblemCode() == null)
								   {
									   actualPoints = 0;
								   }
								}
							}
						}
					}
				}
			}
		}
		
		if(ApplicationConstants.SUBCATEGORIES.TIME_PRECISION.getMaxPoints() == actualPoints)
		{
			translationsScore.setComment("All the teanslations under encounter section has root element");
		}else
		{
			translationsScore.setComment("Some translation elements under encounter is having root element as nullfalvour");
		}
		
		translationsScore.setActualPoints(actualPoints);
		return translationsScore;
	}
	
}
