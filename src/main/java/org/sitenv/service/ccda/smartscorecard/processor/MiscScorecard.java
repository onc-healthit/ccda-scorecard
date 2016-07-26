package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAAllergyConcern;
import org.sitenv.ccdaparsing.model.CCDAAllergyObs;
import org.sitenv.ccdaparsing.model.CCDAEncounterActivity;
import org.sitenv.ccdaparsing.model.CCDAEncounterDiagnosis;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAImmunizationActivity;
import org.sitenv.ccdaparsing.model.CCDALabResultObs;
import org.sitenv.ccdaparsing.model.CCDALabResultOrg;
import org.sitenv.ccdaparsing.model.CCDAMedicationActivity;
import org.sitenv.ccdaparsing.model.CCDAProblemConcern;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDAProcActProc;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.ccdaparsing.model.CCDAServiceDeliveryLoc;
import org.sitenv.ccdaparsing.model.CCDASmokingStatus;
import org.sitenv.ccdaparsing.model.CCDATobaccoUse;
import org.sitenv.ccdaparsing.model.CCDAVitalObs;
import org.sitenv.ccdaparsing.model.CCDAVitalOrg;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.TemplateIdRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MiscScorecard {
	
	@Autowired
	TemplateIdRepository templateIdRepository;
	
	public Category getMiscCategory(CCDARefModel ccdaModels)
	{
		
		Category miscCategory = new Category();
		miscCategory.setCategoryName(ApplicationConstants.CATEGORIES.MISC.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> miscScoreList = new ArrayList<CCDAScoreCardRubrics>();
		miscScoreList.add(getTemplateIdScore(ccdaModels));
		miscCategory.setCategoryRubrics(miscScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(miscScoreList,miscCategory);
		
		return miscCategory;
	}
	
	
	public CCDAScoreCardRubrics getTemplateIdScore(CCDARefModel ccdaModels)
	{
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_REQ);
		
		int maxPoints = 0;
		int actualPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		
		if(ccdaModels != null)
		{
			if(ccdaModels.getAllergy()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getAllergy().getSectionTemplateId()))
				{
					maxPoints = maxPoints + ccdaModels.getAllergy().getSectionTemplateId().size();
					for (CCDAII templateId : ccdaModels.getAllergy().getSectionTemplateId())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				if(!ApplicationUtil.isEmpty(ccdaModels.getAllergy().getAllergyConcern()))
				{
					for(CCDAAllergyConcern allergyConcern : ccdaModels.getAllergy().getAllergyConcern())
					{
						if(!ApplicationUtil.isEmpty(allergyConcern.getTemplateId()))
						{
							maxPoints = maxPoints + allergyConcern.getTemplateId().size();
							for (CCDAII templateId : allergyConcern.getTemplateId())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
						{
							for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs())
							{
								if(!ApplicationUtil.isEmpty(allergyObs.getTemplateId()))
								{
									maxPoints = maxPoints + allergyObs.getTemplateId().size();
									for (CCDAII templateId : allergyObs.getTemplateId())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getEncounter()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getEncounter().getTemplateId()))
				{
					maxPoints = maxPoints + ccdaModels.getEncounter().getTemplateId().size();
					for (CCDAII templateId : ccdaModels.getEncounter().getTemplateId())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getEncounter().getEncActivities()))
				{
					for(CCDAEncounterActivity encAct : ccdaModels.getEncounter().getEncActivities())
					{
						if(!ApplicationUtil.isEmpty(encAct.getTemplateId()))
						{
							maxPoints = maxPoints + encAct.getTemplateId().size();
							for (CCDAII templateId : encAct.getTemplateId())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(encAct.getIndications()))
						{
							for(CCDAProblemObs probObs :  encAct.getIndications())
							{
								if(!ApplicationUtil.isEmpty(probObs.getTemplateId()))
								{
									maxPoints = maxPoints + probObs.getTemplateId().size();
									for (CCDAII templateId : probObs.getTemplateId())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(encAct.getDiagnoses()))
						{
							for(CCDAEncounterDiagnosis encDiagnosis :  encAct.getDiagnoses())
							{
								if(!ApplicationUtil.isEmpty(encDiagnosis.getTemplateId()))
								{
									maxPoints = maxPoints + encDiagnosis.getTemplateId().size();
									for (CCDAII templateId : encDiagnosis.getTemplateId())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(encAct.getSdLocs()))
						{
							for(CCDAServiceDeliveryLoc sdlLoc :  encAct.getSdLocs())
							{
								if(!ApplicationUtil.isEmpty(sdlLoc.getTemplateId()))
								{
									maxPoints = maxPoints + sdlLoc.getTemplateId().size();
									for (CCDAII templateId : sdlLoc.getTemplateId())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
					}
				}
			}
			
			
			if(ccdaModels.getImmunization()!= null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getImmunization().getTemplateIds()))
				{
					maxPoints = maxPoints + ccdaModels.getImmunization().getTemplateIds().size();
					for (CCDAII templateId : ccdaModels.getImmunization().getTemplateIds())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getImmunization().getImmActivity()))
				{
					for(CCDAImmunizationActivity immuActivity :  ccdaModels.getImmunization().getImmActivity())
					{
						if(!ApplicationUtil.isEmpty(immuActivity.getTemplateIds()))
						{
							maxPoints = maxPoints + immuActivity.getTemplateIds().size();
							for (CCDAII templateId : immuActivity.getTemplateIds())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if((immuActivity.getConsumable()!=null))
						{
							if(!ApplicationUtil.isEmpty(immuActivity.getConsumable().getTemplateIds()))
							{
								maxPoints = maxPoints + immuActivity.getConsumable().getTemplateIds().size();
								for (CCDAII templateId : immuActivity.getConsumable().getTemplateIds())
								{
									if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
									{
										actualPoints++;
									}
									else
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(templateId.getLineNumber());
										issue.setXmlString(templateId.getXmlString());
										issuesList.add(issue);
									}
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getLabResults()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getLabResults().getResultSectionTempalteIds()))
				{
					maxPoints = maxPoints + ccdaModels.getLabResults().getResultSectionTempalteIds().size();
					for (CCDAII templateId : ccdaModels.getLabResults().getResultSectionTempalteIds())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getLabResults().getResultOrg()))
				{
					for(CCDALabResultOrg resultOrg :  ccdaModels.getLabResults().getResultOrg())
					{
						if(!ApplicationUtil.isEmpty(resultOrg.getTemplateIds()))
						{
							maxPoints = maxPoints + resultOrg.getTemplateIds().size();
							for (CCDAII templateId : resultOrg.getTemplateIds())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
						{
							for(CCDALabResultObs labResultObs : resultOrg.getResultObs())
							{
								if(!ApplicationUtil.isEmpty(labResultObs.getTemplateIds()))
								{
									maxPoints = maxPoints + labResultObs.getTemplateIds().size();
									for (CCDAII templateId : labResultObs.getTemplateIds())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getLabTests()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getLabTests().getResultOrg()))
				{
					for(CCDALabResultOrg resultOrg :  ccdaModels.getLabTests().getResultOrg())
					{
						if(!ApplicationUtil.isEmpty(resultOrg.getTemplateIds()))
						{
							maxPoints = maxPoints + resultOrg.getTemplateIds().size();
							for (CCDAII templateId : resultOrg.getTemplateIds())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
						{
							for(CCDALabResultObs labResultObs : resultOrg.getResultObs())
							{
								if(!ApplicationUtil.isEmpty(labResultObs.getTemplateIds()))
								{
									maxPoints = maxPoints + labResultObs.getTemplateIds().size();
									for (CCDAII templateId : labResultObs.getTemplateIds())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getMedication()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getMedication().getTemplateIds()))
				{
					maxPoints = maxPoints + ccdaModels.getMedication().getTemplateIds().size();
					for (CCDAII templateId : ccdaModels.getMedication().getTemplateIds())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getMedication().getMedActivities()))
				{
					for(CCDAMedicationActivity medAct : ccdaModels.getMedication().getMedActivities())
					{
						if(!ApplicationUtil.isEmpty(medAct.getTemplateIds()))
						{
							maxPoints = maxPoints + medAct.getTemplateIds().size();
							for (CCDAII templateId : medAct.getTemplateIds())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(medAct.getConsumable()!=null)
						{
							if(!ApplicationUtil.isEmpty(medAct.getConsumable().getTemplateIds()))
							{
								maxPoints = maxPoints + medAct.getConsumable().getTemplateIds().size();
								for (CCDAII templateId : medAct.getConsumable().getTemplateIds())
								{
									if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
									{
										actualPoints++;
									}
									else
									{
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(templateId.getLineNumber());
										issue.setXmlString(templateId.getXmlString());
										issuesList.add(issue);
									}
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getProblem()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getProblem().getSectionTemplateId()))
				{
					maxPoints = maxPoints + ccdaModels.getProblem().getSectionTemplateId().size();
					for (CCDAII templateId : ccdaModels.getProblem().getSectionTemplateId())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getProblem().getProblemConcerns()))
				{
					for(CCDAProblemConcern probConcern : ccdaModels.getProblem().getProblemConcerns())
					{
						if(!ApplicationUtil.isEmpty(probConcern.getTemplateId()))
						{
							maxPoints = maxPoints + probConcern.getTemplateId().size();
							for (CCDAII templateId : probConcern.getTemplateId())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(probConcern.getProblemObservations()))
						{
							for(CCDAProblemObs probObs : probConcern.getProblemObservations())
							{
								if(!ApplicationUtil.isEmpty(probObs.getTemplateId()))
								{
									maxPoints = maxPoints + probObs.getTemplateId().size();
									for (CCDAII templateId : probObs.getTemplateId())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getProcedure()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getProcedure().getSectionTemplateId()))
				{
					maxPoints = maxPoints + ccdaModels.getProcedure().getSectionTemplateId().size();
					for (CCDAII templateId : ccdaModels.getProcedure().getSectionTemplateId())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getProcedure().getProcActsProcs()))
				{
					for(CCDAProcActProc procAct : ccdaModels.getProcedure().getProcActsProcs())
					{
						if(!ApplicationUtil.isEmpty(procAct.getSectionTemplateId()))
						{
							maxPoints = maxPoints + procAct.getSectionTemplateId().size();
							for (CCDAII templateId : procAct.getSectionTemplateId())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(procAct.getSdLocs()))
						{
							for(CCDAServiceDeliveryLoc sdLoc : procAct.getSdLocs())
							{
								if(!ApplicationUtil.isEmpty(sdLoc.getTemplateId()))
								{
									maxPoints = maxPoints + sdLoc.getTemplateId().size();
									for (CCDAII templateId : sdLoc.getTemplateId())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getSmokingStatus()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getSmokingStatus().getSectionTemplateIds()))
				{
					maxPoints = maxPoints + ccdaModels.getSmokingStatus().getSectionTemplateIds().size();
					for (CCDAII templateId : ccdaModels.getSmokingStatus().getSectionTemplateIds())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getSmokingStatus().getSmokingStatus()))
				{
					for(CCDASmokingStatus smokingStatus : ccdaModels.getSmokingStatus().getSmokingStatus())
					{
						if(!ApplicationUtil.isEmpty(smokingStatus.getSmokingStatusTemplateIds()))
						{
							maxPoints = maxPoints + smokingStatus.getSmokingStatusTemplateIds().size();
							for (CCDAII templateId : smokingStatus.getSmokingStatusTemplateIds())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getSmokingStatus().getTobaccoUse()))
				{
					for(CCDATobaccoUse tobaccoUse : ccdaModels.getSmokingStatus().getTobaccoUse())
					{
						if(!ApplicationUtil.isEmpty(tobaccoUse.getTobaccoUseTemplateIds()))
						{
							maxPoints = maxPoints + tobaccoUse.getTobaccoUseTemplateIds().size();
							for (CCDAII templateId : tobaccoUse.getTobaccoUseTemplateIds())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
					}
				}
			}
			
			if(ccdaModels.getVitalSigns()!= null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getVitalSigns().getTemplateIds()))
				{
					maxPoints = maxPoints + ccdaModels.getVitalSigns().getTemplateIds().size();
					for (CCDAII templateId : ccdaModels.getVitalSigns().getTemplateIds())
					{
						if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
						{
							actualPoints++;
						}
						else
						{
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(templateId.getLineNumber());
							issue.setXmlString(templateId.getXmlString());
							issuesList.add(issue);
						}
					}
				}
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getVitalSigns().getVitalsOrg()))
				{
					for(CCDAVitalOrg vitalOrg :ccdaModels.getVitalSigns().getVitalsOrg())
					{
						if(!ApplicationUtil.isEmpty(vitalOrg.getTemplateIds()))
						{
							maxPoints = maxPoints + vitalOrg.getTemplateIds().size();
							for (CCDAII templateId : vitalOrg.getTemplateIds())
							{
								if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
								{
									actualPoints++;
								}
								else
								{
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(templateId.getLineNumber());
									issue.setXmlString(templateId.getXmlString());
									issuesList.add(issue);
								}
							}
						}
						
						if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
						{
							for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
							{
								if(!ApplicationUtil.isEmpty(vitalObs.getTemplateIds()))
								{
									maxPoints = maxPoints + vitalObs.getTemplateIds().size();
									for (CCDAII templateId : vitalObs.getTemplateIds())
									{
										if(templateId.getRootValue() != null && templateIdRepository.findByTemplateId(templateId.getRootValue()))
										{
											actualPoints++;
										}
										else
										{
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(templateId.getLineNumber());
											issue.setXmlString(templateId.getXmlString());
											issuesList.add(issue);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		if(maxPoints==0)
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber("All sections are empty");
			issue.setXmlString("All sections are empty");
			issuesList.add(issue);
		}
		
		templateIdScore.setActualPoints(actualPoints);
		templateIdScore.setMaxPoints(maxPoints);
		templateIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		templateIdScore.setIssuesList(issuesList);
		templateIdScore.setNumberOfIssues(issuesList.size());
		if(issuesList.size() > 0)
		{
			templateIdScore.setDescription(ApplicationConstants.TEMPLATEID_DESC);
			templateIdScore.getIgReferences().add(ApplicationConstants.IG_URL);
			templateIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		
		return templateIdScore;
	}

}
