package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAEncounter;
import org.sitenv.ccdaparsing.model.CCDAEncounterActivity;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;

public class EncounterScorecard {
	
	public static Category getEncounterCategory(CCDARefModel refModel)
	{
		
		Category encounterCategory = new Category();
		encounterCategory.setCategoryName("Encounter");
		
		List<CCDAScoreCardRubrics> encounterScoreList = new ArrayList<CCDAScoreCardRubrics>();
		encounterScoreList.add(getEncounterSectionScore(refModel.getEncounter()));
		encounterScoreList.add(getEncounterSDLScore(refModel.getEncounter()));
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
	
}
