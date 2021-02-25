package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.List;

import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAXmlSnippet;
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.TemplateIdRepository11;
import org.sitenv.service.ccda.smartscorecard.repositories.inmemory.TemplateIdRepository21;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateIdProcessor {
	
	@Autowired
	TemplateIdRepository21 R21templateIdRepository;
	
	@Autowired
	TemplateIdRepository11 R11templateIdRepository;
	
	public int scoreTemplateId(CCDAII templateId, List<CCDAXmlSnippet> issuesList,String ccdaVersion) {
		int actualPoints = 0;
		CCDAXmlSnippet issue = null;
		if(ccdaVersion.equals(ApplicationConstants.CCDAVersion.R21.getVersion()))
		{
			if (ApplicationUtil.isRootAndExtensionPresent(templateId)) {
				if (R21templateIdRepository.findByTemplateIdAndExtension(templateId.getRootValue(),templateId.getExtValue())) {
					actualPoints++;
				} else {
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(templateId.getLineNumber());
					issue.setXmlString(templateId.getXmlString());
					issuesList.add(issue);
				}
			} else if (ApplicationUtil.isRootValuePresent(templateId)) {
				if (R21templateIdRepository.findByTemplateId(templateId.getRootValue())) {
					actualPoints++;
				} else {
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(templateId.getLineNumber());
					issue.setXmlString(templateId.getXmlString());
					issuesList.add(issue);
				}
			} else {
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(templateId.getLineNumber());
				issue.setXmlString(templateId.getXmlString());
				issuesList.add(issue);
			}
		}else if (ccdaVersion.equals(ApplicationConstants.CCDAVersion.R11.getVersion()))
		{
			if (ApplicationUtil.isRootValuePresent(templateId)) {
				if (R11templateIdRepository.findByTemplateId(templateId.getRootValue())) {
					actualPoints++;
				} else {
					issue = new CCDAXmlSnippet();
					issue.setLineNumber(templateId.getLineNumber());
					issue.setXmlString(templateId.getXmlString());
					issuesList.add(issue);
				}
			} else {
				issue = new CCDAXmlSnippet();
				issue.setLineNumber(templateId.getLineNumber());
				issue.setXmlString(templateId.getXmlString());
				issuesList.add(issue);
			}
		}
		return actualPoints;
	}
}
