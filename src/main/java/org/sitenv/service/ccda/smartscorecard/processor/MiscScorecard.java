package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sitenv.ccdaparsing.model.CCDAAllergyConcern;
import org.sitenv.ccdaparsing.model.CCDAAllergyObs;
import org.sitenv.ccdaparsing.model.CCDAEncounterActivity;
import org.sitenv.ccdaparsing.model.CCDAEncounterDiagnosis;
import org.sitenv.ccdaparsing.model.CCDAID;
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
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.TemplateIdRepository21;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MiscScorecard {
	
	@Autowired
	TemplateIdRepository21 templateIdRepository;
	
	public Category getMiscCategory(CCDARefModel ccdaModels)
	{
		
		Category miscCategory = new Category();
		miscCategory.setCategoryName(ApplicationConstants.CATEGORIES.MISC.getCategoryDesc());
		
		List<CCDAScoreCardRubrics> miscScoreList = new ArrayList<CCDAScoreCardRubrics>();
		miscScoreList.add(getUniqueIdScore(ccdaModels));
		miscCategory.setCategoryRubrics(miscScoreList);
		ApplicationUtil.calculateSectionGradeAndIssues(miscScoreList,miscCategory);
		
		return miscCategory;
	}
	
	public CCDAScoreCardRubrics getUniqueIdScore(CCDARefModel ccdaModels)
	{
		CCDAScoreCardRubrics uniqeIdScore = new CCDAScoreCardRubrics();
		uniqeIdScore.setRule(ApplicationConstants.UNIQUEID_REQ);
		
		int actualPoints =0;
		int maxPoints = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue= null;
		
		List<CCDAID> duplicates = new ArrayList<CCDAID>();
		Set<CCDAID> set = new HashSet<CCDAID>();
		if(ccdaModels.getIdList()!= null)
		{
			for(CCDAID id : ccdaModels.getIdList())
			{
				if(!set.add(id))
				{
					duplicates.add(id);
				}
			}
			maxPoints = ccdaModels.getIdList().size();
			actualPoints = set.size();
		}
		
		for (CCDAID id : duplicates)
		{
			issue = new CCDAXmlSnippet();
			issue.setLineNumber(id.getLineNumber());
			issue.setXmlString(id.getXmlString());
			issuesList.add(issue);
		}
		
		if(maxPoints==0)
		{
			maxPoints =1;
			actualPoints=1;
		}
		
		uniqeIdScore.setActualPoints(actualPoints);
		uniqeIdScore.setMaxPoints(maxPoints);
		uniqeIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		uniqeIdScore.setIssuesList(issuesList);
		uniqeIdScore.setNumberOfIssues(issuesList.size());
		
		if(issuesList.size() > 0)
		{
			uniqeIdScore.setDescription(ApplicationConstants.UNIQUEID_DESC);
			uniqeIdScore.getIgReferences().add(ApplicationConstants.IG_URL);
			uniqeIdScore.getExampleTaskForceLinks().add(ApplicationConstants.TASKFORCE_URL);
		}
		return uniqeIdScore;
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
					for (CCDAII templateId : ccdaModels.getAllergy().getSectionTemplateId())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				if(!ApplicationUtil.isEmpty(ccdaModels.getAllergy().getAllergyConcern()))
				{
					for(CCDAAllergyConcern allergyConcern : ccdaModels.getAllergy().getAllergyConcern())
					{
						if(!ApplicationUtil.isEmpty(allergyConcern.getTemplateId()))
						{
							for (CCDAII templateId : allergyConcern.getTemplateId())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs()))
						{
							for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs())
							{
								if(!ApplicationUtil.isEmpty(allergyObs.getTemplateId()))
								{
									for (CCDAII templateId : allergyObs.getTemplateId())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getEncounter()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getEncounter().getTemplateId()))
				{
					for (CCDAII templateId : ccdaModels.getEncounter().getTemplateId())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getEncounter().getEncActivities()))
				{
					for(CCDAEncounterActivity encAct : ccdaModels.getEncounter().getEncActivities())
					{
						if(!ApplicationUtil.isEmpty(encAct.getTemplateId()))
						{
							for (CCDAII templateId : encAct.getTemplateId())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(encAct.getIndications()))
						{
							for(CCDAProblemObs probObs :  encAct.getIndications())
							{
								if(!ApplicationUtil.isEmpty(probObs.getTemplateId()))
								{
									for (CCDAII templateId : probObs.getTemplateId())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(encAct.getDiagnoses()))
						{
							for(CCDAEncounterDiagnosis encDiagnosis :  encAct.getDiagnoses())
							{
								if(!ApplicationUtil.isEmpty(encDiagnosis.getTemplateId()))
								{
									for (CCDAII templateId : encDiagnosis.getTemplateId())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(encAct.getSdLocs()))
						{
							for(CCDAServiceDeliveryLoc sdlLoc :  encAct.getSdLocs())
							{
								if(!ApplicationUtil.isEmpty(sdlLoc.getTemplateId()))
								{
									for (CCDAII templateId : sdlLoc.getTemplateId())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			
			if(ccdaModels.getImmunization()!= null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getImmunization().getTemplateIds()))
				{
					for (CCDAII templateId : ccdaModels.getImmunization().getTemplateIds())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getImmunization().getImmActivity()))
				{
					for(CCDAImmunizationActivity immuActivity :  ccdaModels.getImmunization().getImmActivity())
					{
						if(!ApplicationUtil.isEmpty(immuActivity.getTemplateIds()))
						{
							for (CCDAII templateId : immuActivity.getTemplateIds())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if((immuActivity.getConsumable()!=null))
						{
							if(!ApplicationUtil.isEmpty(immuActivity.getConsumable().getTemplateIds()))
							{
								for (CCDAII templateId : immuActivity.getConsumable().getTemplateIds())
								{
									if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
									{
										maxPoints = maxPoints++;
										if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getLabResults()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getLabResults().getResultSectionTempalteIds()))
				{
					for (CCDAII templateId : ccdaModels.getLabResults().getResultSectionTempalteIds())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getLabResults().getResultOrg()))
				{
					for(CCDALabResultOrg resultOrg :  ccdaModels.getLabResults().getResultOrg())
					{
						if(!ApplicationUtil.isEmpty(resultOrg.getTemplateIds()))
						{
							for (CCDAII templateId : resultOrg.getTemplateIds())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
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
						
						if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
						{
							for(CCDALabResultObs labResultObs : resultOrg.getResultObs())
							{
								if(!ApplicationUtil.isEmpty(labResultObs.getTemplateIds()))
								{
									for (CCDAII templateId : labResultObs.getTemplateIds())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getLabTests()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getLabTests().getResultOrg()))
				{
					for(CCDALabResultOrg resultOrg :  ccdaModels.getLabTests().getResultOrg())
					{
						if(!ApplicationUtil.isEmpty(resultOrg.getTemplateIds()))
						{
							for (CCDAII templateId : resultOrg.getTemplateIds())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(resultOrg.getResultObs()))
						{
							for(CCDALabResultObs labResultObs : resultOrg.getResultObs())
							{
								if(!ApplicationUtil.isEmpty(labResultObs.getTemplateIds()))
								{
									for (CCDAII templateId : labResultObs.getTemplateIds())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getMedication()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getMedication().getTemplateIds()))
				{
					for (CCDAII templateId : ccdaModels.getMedication().getTemplateIds())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getMedication().getMedActivities()))
				{
					for(CCDAMedicationActivity medAct : ccdaModels.getMedication().getMedActivities())
					{
						if(!ApplicationUtil.isEmpty(medAct.getTemplateIds()))
						{
							for (CCDAII templateId : medAct.getTemplateIds())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(medAct.getConsumable()!=null)
						{
							if(!ApplicationUtil.isEmpty(medAct.getConsumable().getTemplateIds()))
							{
								for (CCDAII templateId : medAct.getConsumable().getTemplateIds())
								{
									if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
									{
										maxPoints = maxPoints++;
										if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getProblem()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getProblem().getSectionTemplateId()))
				{
					for (CCDAII templateId : ccdaModels.getProblem().getSectionTemplateId())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getProblem().getProblemConcerns()))
				{
					for(CCDAProblemConcern probConcern : ccdaModels.getProblem().getProblemConcerns())
					{
						if(!ApplicationUtil.isEmpty(probConcern.getTemplateId()))
						{
							for (CCDAII templateId : probConcern.getTemplateId())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(probConcern.getProblemObservations()))
						{
							for(CCDAProblemObs probObs : probConcern.getProblemObservations())
							{
								if(!ApplicationUtil.isEmpty(probObs.getTemplateId()))
								{
									for (CCDAII templateId : probObs.getTemplateId())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getProcedure()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getProcedure().getSectionTemplateId()))
				{
					for (CCDAII templateId : ccdaModels.getProcedure().getSectionTemplateId())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getProcedure().getProcActsProcs()))
				{
					for(CCDAProcActProc procAct : ccdaModels.getProcedure().getProcActsProcs())
					{
						if(!ApplicationUtil.isEmpty(procAct.getSectionTemplateId()))
						{
							for (CCDAII templateId : procAct.getSectionTemplateId())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{
									maxPoints = maxPoints++;
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(procAct.getSdLocs()))
						{
							for(CCDAServiceDeliveryLoc sdLoc : procAct.getSdLocs())
							{
								if(!ApplicationUtil.isEmpty(sdLoc.getTemplateId()))
								{
									for (CCDAII templateId : sdLoc.getTemplateId())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{
											maxPoints = maxPoints++;	
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getSmokingStatus()!=null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getSmokingStatus().getSectionTemplateIds()))
				{
					for (CCDAII templateId : ccdaModels.getSmokingStatus().getSectionTemplateIds())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{
							maxPoints = maxPoints++;
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getSmokingStatus().getSmokingStatus()))
				{
					for(CCDASmokingStatus smokingStatus : ccdaModels.getSmokingStatus().getSmokingStatus())
					{
						if(!ApplicationUtil.isEmpty(smokingStatus.getSmokingStatusTemplateIds()))
						{
							for (CCDAII templateId : smokingStatus.getSmokingStatusTemplateIds())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{	
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getSmokingStatus().getTobaccoUse()))
				{
					for(CCDATobaccoUse tobaccoUse : ccdaModels.getSmokingStatus().getTobaccoUse())
					{
						if(!ApplicationUtil.isEmpty(tobaccoUse.getTobaccoUseTemplateIds()))
						{
							for (CCDAII templateId : tobaccoUse.getTobaccoUseTemplateIds())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{	
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
			
			if(ccdaModels.getVitalSigns()!= null)
			{
				if(!ApplicationUtil.isEmpty(ccdaModels.getVitalSigns().getTemplateIds()))
				{
					for (CCDAII templateId : ccdaModels.getVitalSigns().getTemplateIds())
					{
						if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
						{	
							if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
				
				if(!ApplicationUtil.isEmpty(ccdaModels.getVitalSigns().getVitalsOrg()))
				{
					for(CCDAVitalOrg vitalOrg :ccdaModels.getVitalSigns().getVitalsOrg())
					{
						if(!ApplicationUtil.isEmpty(vitalOrg.getTemplateIds()))
						{
							for (CCDAII templateId : vitalOrg.getTemplateIds())
							{
								if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
								{	
									if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
						
						if(!ApplicationUtil.isEmpty(vitalOrg.getVitalObs()))
						{
							for(CCDAVitalObs vitalObs : vitalOrg.getVitalObs())
							{
								if(!ApplicationUtil.isEmpty(vitalObs.getTemplateIds()))
								{
									for (CCDAII templateId : vitalObs.getTemplateIds())
									{
										if(templateId.getRootValue() != null && ApplicationUtil.validTemplateIdFormat(templateId.getRootValue()))
										{	
											if(templateIdRepository.findByTemplateId(templateId.getRootValue()))
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
		}
		
		if(maxPoints==0)
		{
			maxPoints =1;
			actualPoints =1;
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
