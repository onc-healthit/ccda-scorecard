package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.sitenv.ccdaparsing.model.CCDAAllergy;
import org.sitenv.ccdaparsing.model.CCDAAllergyConcern;
import org.sitenv.ccdaparsing.model.CCDAAllergyObs;
import org.sitenv.ccdaparsing.model.CCDAAllergyReaction;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.configuration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class AllergiesScorecard {

	private static final Logger logger = Logger.getLogger(AllergiesScorecard.class);

	@Autowired
	TemplateIdProcessor templateIdProcessor;

	@Autowired
	ReferenceValidatorService referenceValidatorService;
	
	public static final List<String> allergyCodeSystems = new ArrayList<>(Arrays.asList("2.16.840.1.113883.6.96", 
  			"2.16.840.1.113883.6.88",
  			"2.16.840.1.113883.6.209",
  			"2.16.840.1.113883.4.9"));

	@Async()
	public Future<Category> getAllergiesCategory(CCDAAllergy allergies, PatientDetails patientDetails,
			String ccdaVersion, List<SectionRule> sectionRules) {
		long startTime = System.currentTimeMillis();
		logger.info("Allergies Start time:" + startTime);
		Category allergyCategory = new Category();
		
		try {
	
			if (allergies == null || allergies.isSectionNullFlavourWithNI()) {
				return new AsyncResult<Category>(
						new Category(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc(), true));
			}
			
			allergyCategory.setCategoryName(ApplicationConstants.CATEGORIES.ALLERGIES.getCategoryDesc());
	
			List<CCDAScoreCardRubrics> allergyScoreList = new ArrayList<CCDAScoreCardRubrics>();
	
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A1)) {
				allergyScoreList.add(getTimePrecisionScore(allergies, ccdaVersion));
			}
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A2)) {
				allergyScoreList.add(getValidDateTimeScore(allergies, patientDetails, ccdaVersion));
			}
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A3)) {
				allergyScoreList.add(getValidDisplayNameScoreCard(allergies, ccdaVersion));
			}
			// allergyScoreList.add(getApprEffectivetimeScore(allergies,ccdaVersion));
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A4)) {
				allergyScoreList.add(getApprStatusCodeScore(allergies, ccdaVersion));
			}
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A5)) {
				allergyScoreList.add(getNarrativeStructureIdScore(allergies, ccdaVersion));
			}
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A6)) {
				allergyScoreList.add(getTemplateIdScore(allergies, ccdaVersion));
			}
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A7)) {
				allergyScoreList.add(getAllergyObservationScore(allergies, ccdaVersion));
			}
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A8)) {
				allergyScoreList.add(getAuthorEntryScore(allergies, ccdaVersion));
			}
			if (sectionRules == null || ApplicationUtil.isRuleEnabled(sectionRules, ApplicationConstants.RULE_IDS.A9)) {
				allergyScoreList.add(getAllergyCodeScore(allergies, ccdaVersion));
			}
	
			allergyCategory.setCategoryRubrics(allergyScoreList);
			ApplicationUtil.calculateSectionGradeAndIssues(allergyScoreList, allergyCategory);
			ApplicationUtil.calculateNumberOfChecksAndFailedRubrics(allergyScoreList, allergyCategory);
			logger.info("Allergies End time:" + (System.currentTimeMillis() - startTime));
		}catch (Exception e) {
			logger.info("exception while scoring Allergies section:" + e.getMessage());
		}
		return new AsyncResult<Category>(allergyCategory);

	}

	public CCDAScoreCardRubrics getTimePrecisionScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics timePrecisionScore = new CCDAScoreCardRubrics();
		timePrecisionScore.setRule(ApplicationConstants.TIME_PRECISION_REQUIREMENT);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					
					if (allergyConcern.getEffTime() != null && !allergyConcern.getEffTime().isNullFlavour()) 
					{
						maxPoints++;
						numberOfChecks++;
						if (ApplicationUtil.validateYearFormat(allergyConcern.getEffTime())
								|| ApplicationUtil.validateMonthFormat(allergyConcern.getEffTime())
								|| ApplicationUtil.validateDayFormat(allergyConcern.getEffTime())
								|| ApplicationUtil.validateMinuteFormatWithoutPadding(allergyConcern.getEffTime())
								|| ApplicationUtil.validateSecondFormatWithoutPadding(allergyConcern.getEffTime())) {
							actualPoints++;
						} else {
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyConcern.getEffTime().getLineNumber());
							issue.setXmlString(allergyConcern.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}

					if (!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs())) {
						for (CCDAAllergyObs allergyObservation : allergyConcern.getAllergyObs()) 
						{
							if (allergyObservation.getEffTime() != null && !allergyObservation.getEffTime().isNullFlavour()) 
							{
								maxPoints++;
								numberOfChecks++;
								if (ApplicationUtil.validateYearFormat(allergyObservation.getEffTime())
										|| ApplicationUtil.validateMonthFormat(allergyObservation.getEffTime())
										|| ApplicationUtil.validateDayFormat(allergyObservation.getEffTime())
										|| ApplicationUtil.validateMinuteFormatWithoutPadding(allergyObservation.getEffTime())
										|| ApplicationUtil.validateSecondFormatWithoutPadding(allergyObservation.getEffTime())) 
								{
									actualPoints++;
								} 
								else {
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObservation.getEffTime().getLineNumber());
									issue.setXmlString(allergyObservation.getEffTime().getXmlString());
									issuesList.add(issue);
								}
							} 
						}
					}
				}
			} 
		} 
		
		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		timePrecisionScore.setActualPoints(actualPoints);
		timePrecisionScore.setMaxPoints(maxPoints);
		timePrecisionScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		timePrecisionScore.setIssuesList(issuesList);
		timePrecisionScore.setNumberOfIssues(issuesList.size());
		timePrecisionScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			timePrecisionScore.setDescription(ApplicationConstants.TIME_PRECISION_DESCRIPTION);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				timePrecisionScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_CONCERN.getIgReference());
			}
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				timePrecisionScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_CONCERN.getIgReference());
			}
			timePrecisionScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return timePrecisionScore;
	}

	public CCDAScoreCardRubrics getValidDateTimeScore(CCDAAllergy allergies, PatientDetails patientDetails,
			String ccdaVersion) {
		CCDAScoreCardRubrics validateTimeScore = new CCDAScoreCardRubrics();
		validateTimeScore.setRule(ApplicationConstants.TIME_VALID_REQUIREMENT);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					if (allergyConcern.getEffTime() != null
							&& ApplicationUtil.isEffectiveTimePresent(allergyConcern.getEffTime())) {
						maxPoints++;
						numberOfChecks++;
						if (ApplicationUtil.checkDateRange(patientDetails, allergyConcern.getEffTime())) {
							actualPoints++;
						} else {
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyConcern.getEffTime().getLineNumber());
							issue.setXmlString(allergyConcern.getEffTime().getXmlString());
							issuesList.add(issue);
						}
					}

					if (!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs())) {
						for (CCDAAllergyObs allergyObservation : allergyConcern.getAllergyObs()) {
							if (allergyObservation.getEffTime() != null
									&& ApplicationUtil.isEffectiveTimePresent(allergyObservation.getEffTime())) {
								maxPoints++;
								numberOfChecks++;
								if (ApplicationUtil.checkDateRange(patientDetails, allergyObservation.getEffTime())) {
									actualPoints++;
								} else {
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObservation.getEffTime().getLineNumber());
									issue.setXmlString(allergyObservation.getEffTime().getXmlString());
									issuesList.add(issue);
								}
							}
						}
					}
				}
			}
		}

		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		validateTimeScore.setActualPoints(actualPoints);
		validateTimeScore.setMaxPoints(maxPoints);
		validateTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateTimeScore.setIssuesList(issuesList);
		validateTimeScore.setNumberOfIssues(issuesList.size());
		validateTimeScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			validateTimeScore.setDescription(ApplicationConstants.TIME_VALID_DESCRIPTION);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				validateTimeScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_CONCERN.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				validateTimeScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_CONCERN.getIgReference());
			}
			validateTimeScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateTimeScore;
	}

	public CCDAScoreCardRubrics getValidDisplayNameScoreCard(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics validateDisplayNameScore = new CCDAScoreCardRubrics();
		validateDisplayNameScore.setRule(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (allergies.getSectionCode() != null
					&& !ApplicationUtil.isEmpty(allergies.getSectionCode().getDisplayName())
					&& ApplicationUtil.isCodeSystemAvailable(allergies.getSectionCode().getCodeSystem())) {
				maxPoints++;
				numberOfChecks++;
				if (referenceValidatorService.validateDisplayName(allergies.getSectionCode().getCode(),
						allergies.getSectionCode().getCodeSystem(), allergies.getSectionCode().getDisplayName())) {
					actualPoints++;
				} else {
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(allergies.getSectionCode().getLineNumber());
					issue.setXmlString(allergies.getSectionCode().getXmlString());
					issuesList.add(issue);
				}
			}

			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					if (!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs())) {
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs()) {
							if (allergyObs.getAllergyIntoleranceType() != null
									&& !ApplicationUtil.isEmpty(allergyObs.getAllergyIntoleranceType().getDisplayName())
									&& ApplicationUtil.isCodeSystemAvailable(
											allergyObs.getAllergyIntoleranceType().getCodeSystem())) {
								maxPoints++;
								numberOfChecks++;
								if (referenceValidatorService.validateDisplayName(
										allergyObs.getAllergyIntoleranceType().getCode(),
										allergyObs.getAllergyIntoleranceType().getCodeSystem(),
										allergyObs.getAllergyIntoleranceType().getDisplayName())) {
									actualPoints++;
								} else {
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObs.getAllergyIntoleranceType().getLineNumber());
									issue.setXmlString(allergyObs.getAllergyIntoleranceType().getXmlString());
									issuesList.add(issue);
								}
							}

							if (allergyObs.getAllergySubstance() != null
									&& !ApplicationUtil.isEmpty(allergyObs.getAllergySubstance().getDisplayName())
									&& ApplicationUtil
											.isCodeSystemAvailable(allergyObs.getAllergySubstance().getCodeSystem())) {
								maxPoints++;
								numberOfChecks++;
								if (referenceValidatorService.validateDisplayName(
										allergyObs.getAllergySubstance().getCode(),
										allergyObs.getAllergySubstance().getCodeSystem(),
										allergyObs.getAllergySubstance().getDisplayName())) {
									actualPoints++;
								} else {
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObs.getAllergySubstance().getLineNumber());
									issue.setXmlString(allergyObs.getAllergySubstance().getXmlString());
									issuesList.add(issue);
								}
							}
						}
					}
				}
			}
		}
		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}
		validateDisplayNameScore.setActualPoints(actualPoints);
		validateDisplayNameScore.setMaxPoints(maxPoints);
		validateDisplayNameScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateDisplayNameScore.setIssuesList(issuesList);
		validateDisplayNameScore.setNumberOfIssues(issuesList.size());
		validateDisplayNameScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			validateDisplayNameScore.setDescription(ApplicationConstants.CODE_DISPLAYNAME_DESCRIPTION);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				validateDisplayNameScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				validateDisplayNameScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			validateDisplayNameScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateDisplayNameScore;
	}

	public CCDAScoreCardRubrics getApprEffectivetimeScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.ALLERGIES_CONCERN_DATE_ALIGN_REQ);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyAct : allergies.getAllergyConcern()) {
					if (allergyAct.getEffTime() != null
							&& ApplicationUtil.isEffectiveTimePresent(allergyAct.getEffTime())) {
						if (!ApplicationUtil.isEmpty(allergyAct.getAllergyObs())) {
							for (CCDAAllergyObs allergyObs : allergyAct.getAllergyObs()) {
								if (allergyObs.getEffTime() != null
										&& ApplicationUtil.isEffectiveTimePresent(allergyObs.getEffTime())) {
									maxPoints++;
									numberOfChecks++;
									if (ApplicationUtil.checkDateRange(allergyAct.getEffTime().getLow(),
											allergyAct.getEffTime().getHigh(), allergyObs.getEffTime().getLow(),
											allergyObs.getEffTime().getHigh())) {
										actualPoints++;
									} else {
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(allergyObs.getEffTime().getLineNumber());
										issue.setXmlString(allergyObs.getEffTime().getXmlString());
										issuesList.add(issue);
									}
								}
							}
						}
					}
				}
			}
		}

		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		validateApprEffectiveTimeScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.ALLERGIES_CONCERN_DATE_ALIGN_DESC);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				validateApprEffectiveTimeScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_CONCERN.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				validateApprEffectiveTimeScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_CONCERN.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}

	public CCDAScoreCardRubrics getNarrativeStructureIdScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics narrativeTextIdScore = new CCDAScoreCardRubrics();
		narrativeTextIdScore.setRule(ApplicationConstants.NARRATIVE_STRUCTURE_ID_REQ);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyAct : allergies.getAllergyConcern()) {
					maxPoints++;
					numberOfChecks++;
					if (allergyAct.getReferenceText() != null) {
						if (allergies.getReferenceLinks() != null
								&& allergies.getReferenceLinks().contains(allergyAct.getReferenceText().getValue())) {
							actualPoints++;
						} else {
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyAct.getReferenceText().getLineNumber());
							issue.setXmlString(allergyAct.getReferenceText().getXmlString());
							issuesList.add(issue);
						}
					} else {
						issue = new CCDAXmlSnippet();
						issue.setLineNumber(allergyAct.getLineNumber());
						issue.setXmlString(allergyAct.getXmlString());
						issuesList.add(issue);
					}

					if (allergyAct.getAllergyObs() != null) {
						for (CCDAAllergyObs allObs : allergyAct.getAllergyObs()) {
							if (allObs.getReactions() != null) {
								for (CCDAAllergyReaction allReaction : allObs.getReactions()) {
									maxPoints++;
									numberOfChecks++;
									if (allReaction.getReferenceText() != null) {
										if (allergies.getReferenceLinks() != null && allergies.getReferenceLinks()
												.contains(allReaction.getReferenceText().getValue())) {
											actualPoints++;
										} else {
											issue = new CCDAXmlSnippet();
											issue.setLineNumber(allReaction.getReferenceText().getLineNumber());
											issue.setXmlString(allReaction.getReferenceText().getXmlString());
											issuesList.add(issue);
										}
									} else {
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(allReaction.getLineNumber());
										issue.setXmlString(allReaction.getXmlString());
										issuesList.add(issue);
									}
								}
							}
						}
					}
				}
			}
		}
		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		narrativeTextIdScore.setActualPoints(actualPoints);
		narrativeTextIdScore.setMaxPoints(maxPoints);
		narrativeTextIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		narrativeTextIdScore.setIssuesList(issuesList);
		narrativeTextIdScore.setNumberOfIssues(issuesList.size());
		narrativeTextIdScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			narrativeTextIdScore.setDescription(ApplicationConstants.NARRATIVE_STRUCTURE_ID_DESC);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				narrativeTextIdScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				narrativeTextIdScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			narrativeTextIdScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}

		return narrativeTextIdScore;
	}

	public CCDAScoreCardRubrics getApprStatusCodeScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics validateApprEffectiveTimeScore = new CCDAScoreCardRubrics();
		validateApprEffectiveTimeScore.setRule(ApplicationConstants.ALLERGIES_APR_TIME_REQ);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					if (ApplicationUtil.isValidStatusCode(allergyConcern.getStatusCode())) {
						if (allergyConcern.getAllergyObs() != null) {
							for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs()) 
							{
								if(allergyObs.getNegationInd()!=null && !allergyObs.getNegationInd()) 
								{
									maxPoints++;
									numberOfChecks++;
									if (ApplicationUtil.validateStatusCode(allergyObs.getEffTime(),
											allergyConcern.getStatusCode().getCode())) 
									{
										actualPoints++;
									} else {
										issue = new CCDAXmlSnippet();
										issue.setLineNumber(
												allergyObs.getEffTime() != null ? allergyObs.getEffTime().getLineNumber()
														: allergyObs.getLineNumber());
										issue.setXmlString(
												allergyObs.getEffTime() != null ? allergyObs.getEffTime().getXmlString()
														: allergyObs.getXmlString());
										issuesList.add(issue);
									}
								}
							}
						} else {
							maxPoints++;
							numberOfChecks++;
							issue = new CCDAXmlSnippet();
							issue.setLineNumber(allergyConcern.getLineNumber());
							issue.setXmlString(allergyConcern.getXmlString());
							issuesList.add(issue);
						}
					}
				}
			} 
		} 
		
		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		validateApprEffectiveTimeScore.setActualPoints(actualPoints);
		validateApprEffectiveTimeScore.setMaxPoints(maxPoints);
		validateApprEffectiveTimeScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		validateApprEffectiveTimeScore.setIssuesList(issuesList);
		validateApprEffectiveTimeScore.setNumberOfIssues(issuesList.size());
		validateApprEffectiveTimeScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			validateApprEffectiveTimeScore.setDescription(ApplicationConstants.ALLERGIES_APR_TIME_DESC);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				validateApprEffectiveTimeScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				validateApprEffectiveTimeScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			validateApprEffectiveTimeScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return validateApprEffectiveTimeScore;
	}

	public CCDAScoreCardRubrics getTemplateIdScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics templateIdScore = new CCDAScoreCardRubrics();
		templateIdScore.setRule(ApplicationConstants.TEMPLATEID_DESC);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getSectionTemplateId())) {
				for (CCDAII templateId : allergies.getSectionTemplateId()) {
					maxPoints++;
					numberOfChecks++;
					actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
				}
			}
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					if (!ApplicationUtil.isEmpty(allergyConcern.getTemplateId())) {
						for (CCDAII templateId : allergyConcern.getTemplateId()) {
							maxPoints++;
							actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);
						}
					}

					if (!ApplicationUtil.isEmpty(allergyConcern.getAllergyObs())) {
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs()) {
							if (!ApplicationUtil.isEmpty(allergyObs.getTemplateId())) {
								for (CCDAII templateId : allergyObs.getTemplateId()) {
									maxPoints++;
									actualPoints =  actualPoints + templateIdProcessor.scoreTemplateId(templateId, issuesList, ccdaVersion);

								}
							}
						}
					}
				}
			}
		}

		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		templateIdScore.setActualPoints(actualPoints);
		templateIdScore.setMaxPoints(maxPoints);
		templateIdScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		templateIdScore.setIssuesList(issuesList);
		templateIdScore.setNumberOfIssues(issuesList.size());
		templateIdScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			templateIdScore.setDescription(ApplicationConstants.TEMPLATEID_REQ);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				templateIdScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				templateIdScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			templateIdScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}

		return templateIdScore;
	}

	public CCDAScoreCardRubrics getAllergyObservationScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics alleryObservationScore = new CCDAScoreCardRubrics();
		alleryObservationScore.setRule(ApplicationConstants.ALLERGIES_OBSERVATION_REQ);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					if (allergyConcern.getAllergyObs() != null) {
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs()) {
							maxPoints++;
							numberOfChecks++;
							if (allergyObs.getReactions() != null && allergyObs.getReactions().size() > 0) {
								actualPoints++;
							} else {
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(allergyObs.getLineNumber());
								issue.setXmlString(allergyObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
		}
		
		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		alleryObservationScore.setActualPoints(actualPoints);
		alleryObservationScore.setMaxPoints(maxPoints);
		alleryObservationScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		alleryObservationScore.setIssuesList(issuesList);
		alleryObservationScore.setNumberOfIssues(issuesList.size());
		alleryObservationScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			alleryObservationScore.setDescription(ApplicationConstants.ALLERGIES_OBSERVATION_REQ);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				alleryObservationScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				alleryObservationScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			alleryObservationScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return alleryObservationScore;
	}

	public CCDAScoreCardRubrics getAuthorEntryScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics authorEntryScore = new CCDAScoreCardRubrics();
		authorEntryScore.setRule(ApplicationConstants.ALLERGIES_AUTHOR_REQ);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					maxPoints++;
					numberOfChecks++;
					if (allergyConcern.getAuthor() != null && allergyConcern.getAuthor().getTime() != null) {
						actualPoints++;
					} else if (allergyConcern.getAllergyObs() != null && allergyConcern.getAllergyObs().size() > 0) {
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs()) {
							if (allergyObs.getAuthor() != null && allergyObs.getAuthor().getTime() != null) {
								actualPoints++;
								break;
							} else {
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(allergyConcern.getLineNumber());
								issue.setXmlString(allergyConcern.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
		}
		
		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		authorEntryScore.setActualPoints(actualPoints);
		authorEntryScore.setMaxPoints(maxPoints);
		authorEntryScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		authorEntryScore.setIssuesList(issuesList);
		authorEntryScore.setNumberOfIssues(issuesList.size());
		authorEntryScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			authorEntryScore.setDescription(ApplicationConstants.ALLERGIES_AUTHOR_REQ);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				authorEntryScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				authorEntryScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			authorEntryScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return authorEntryScore;
	}

	public CCDAScoreCardRubrics getAllergyCodeScore(CCDAAllergy allergies, String ccdaVersion) {
		CCDAScoreCardRubrics authorEntryScore = new CCDAScoreCardRubrics();
		authorEntryScore.setRule(ApplicationConstants.ALLERGIES_CODE_REQ);

		int maxPoints = 0;
		int actualPoints = 0;
		int numberOfChecks = 0;
		List<CCDAXmlSnippet> issuesList = new ArrayList<CCDAXmlSnippet>();
		CCDAXmlSnippet issue = null;
		if (allergies != null) {
			if (!ApplicationUtil.isEmpty(allergies.getAllergyConcern())) {
				for (CCDAAllergyConcern allergyConcern : allergies.getAllergyConcern()) {
					if (allergyConcern.getAllergyObs() != null) {
						for (CCDAAllergyObs allergyObs : allergyConcern.getAllergyObs()) {
							maxPoints++;
							numberOfChecks++;
							if (allergyObs.getAllergyIntoleranceType() != null) {
								if (allergyCodeSystems
										.contains(allergyObs.getAllergyIntoleranceType().getCodeSystem())) {
									actualPoints++;
								} else {
									issue = new CCDAXmlSnippet();
									issue.setLineNumber(allergyObs.getAllergyIntoleranceType().getLineNumber());
									issue.setXmlString(allergyObs.getAllergyIntoleranceType().getXmlString());
									issuesList.add(issue);
								}
							} else {
								issue = new CCDAXmlSnippet();
								issue.setLineNumber(allergyObs.getLineNumber());
								issue.setXmlString(allergyObs.getXmlString());
								issuesList.add(issue);
							}
						}
					}
				}
			}
		}
		
		if (maxPoints == 0) {
			maxPoints = 1;
			actualPoints = 1;
		}

		authorEntryScore.setActualPoints(actualPoints);
		authorEntryScore.setMaxPoints(maxPoints);
		authorEntryScore.setRubricScore(ApplicationUtil.calculateRubricScore(maxPoints, actualPoints));
		authorEntryScore.setIssuesList(issuesList);
		authorEntryScore.setNumberOfIssues(issuesList.size());
		authorEntryScore.setNumberOfChecks(numberOfChecks);
		if (issuesList.size() > 0) {
			authorEntryScore.setDescription(ApplicationConstants.ALLERGIES_CODE_REQ);
			if (ccdaVersion.equals("") || ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion())) {
				authorEntryScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES.ALLERGY_SECTION.getIgReference());
			} else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion())) {
				authorEntryScore.getIgReferences()
						.add(ApplicationConstants.IG_REFERENCES_R1.ALLERGY_SECTION.getIgReference());
			}
			authorEntryScore.getExampleTaskForceLinks()
					.add(ApplicationConstants.TASKFORCE_LINKS.ALLERGIES.getTaskforceLink());
		}
		return authorEntryScore;
	}
}
