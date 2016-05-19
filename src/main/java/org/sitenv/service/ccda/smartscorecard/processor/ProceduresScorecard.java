package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAProcActProc;
import org.sitenv.ccdaparsing.model.CCDAProcedure;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.stereotype.Service;

@Service
public class ProceduresScorecard {
	
	public Category getProceduresCategory(CCDAProcedure procedures, String birthDate)
	{
		
		Category procedureCategory = new Category();
		procedureCategory.setCategoryName("Procedures");
		
		List<CCDAScoreCardRubrics> procedureScoreList = new ArrayList<CCDAScoreCardRubrics>();
		procedureScoreList.add(getValidDisplayNameScoreCard(procedures));
		
		procedureCategory.setCategoryRubrics(procedureScoreList);
		procedureCategory.setCategoryGrade(ApplicationUtil.calculateSectionGrade(procedureScoreList));
		
		return procedureCategory;
	}
	
	
	
	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAProcedure procedures)
	{
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setPoints(ApplicationConstants.VALID_CODE_DISPLAYNAME_POINTS);
		validateDisplayNameScore.setRequirement(ApplicationConstants.PROCEDURES_CODE_DISPLAYNAME_REQUIREMENT);
		validateDisplayNameScore.setSubCategory(ApplicationConstants.SUBCATEGORIES.CODE_DISPLAYNAME_VALIDATION.getSubcategory());
		
		int maxPoints = 0;
		int actualPoints = 0;
		if(procedures != null)
		{
			maxPoints++;
			if(procedures.getSectionCode()!= null)
			{
				if(ApplicationUtil.validateDisplayName(procedures.getSectionCode().getCode(), 
									ApplicationConstants.CODE_SYSTEM_MAP.get(procedures.getSectionCode().getCodeSystem()),
									procedures.getSectionCode().getDisplayName()))
				{
					actualPoints++;
				}
			}
			
			if(!ApplicationUtil.isEmpty(procedures.getProcActsProcs()))
			{
				for (CCDAProcActProc procAct : procedures.getProcActsProcs())
				{
					maxPoints++;
					if(procAct.getProcCode() != null)
					{
						if(ApplicationUtil.validateDisplayName(procAct.getProcCode().getCode(), 
												ApplicationConstants.CODE_SYSTEM_MAP.get(procAct.getProcCode().getCodeSystem()),
												procAct.getProcCode().getDisplayName()))
						{
							actualPoints++;
						}
					}
				}
			}
		}
		
		if(maxPoints!= 0 && maxPoints == actualPoints)
		{
			validateDisplayNameScore.setComment("All the code elements under Procedures are having valid display name");
		}else
		{
			validateDisplayNameScore.setComment("Some code elements under Procedures are not having valid display name");
		}
		
		if(maxPoints!= 0)
		{
			validateDisplayNameScore.setActualPoints(ApplicationUtil.calculateActualPoints(maxPoints, actualPoints));
		}else
		{
			validateDisplayNameScore.setActualPoints(0);
		}
		
		validateDisplayNameScore.setMaxPoints(4);
		return validateDisplayNameScore;
	}
}
