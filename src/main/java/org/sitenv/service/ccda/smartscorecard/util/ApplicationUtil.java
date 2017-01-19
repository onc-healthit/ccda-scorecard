package org.sitenv.service.ccda.smartscorecard.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAEffTime;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants.CONCERNACT_STATUS;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;



public class ApplicationUtil {
	

	/**
	 * Checks if is null or empty.
	 * 
	 * @param input
	 *            List<T> :  list
	 * @return true, if input is null or Empty
	 */
	public static <T> boolean isEmpty(final List<T> list)
	{
		return list == null || list.isEmpty();
	}

	/**
	 * Checks if is null or empty.
	 * 
	 * @param input
	 *            String :  str
	 * @return true, if input is null or Zero
	 */
	public static boolean isEmpty(final String str)
	{
		return str == null || str.trim().length() == 0;
	}
	/**
	 * Checks if is null or empty.
	 * 
	 * @param input
	 *            Integer :  value
	 * @return true, input if is null or Zero
	 */
	public static boolean isEmpty(final Integer value)
	{
		return value == null || value.intValue() == 0;
	}
	
	public static boolean isEmpty(final int[] arr)
	{
		return arr.length == 0;
	}
		
	/**
	 * Checks if is null or Zero.
	 * 
	 * @param input
	 *            the value
	 * @return true, if input is null or Zero.
	 */
	public static boolean isEmpty(final Short value)
	{
		return value == null || value.intValue() == 0;
	}
	
	
	/**
	 * Checks if an array of String is empty
	 * @param strArr
	 * @return true, if input empty.
	 */
	public static boolean isEmpty(final String[] strArr)
	{
		return strArr.length == 0;
	}
	
	public static boolean isValueEmpty(final CCDADataElement object)
	{
		boolean result = true;
		if(object != null)
		{
			result = isEmpty(object.getValue());
		}
		
		return result;
	}
	
	public static boolean isExtensionPresent(List<CCDAII> templateIds)
	{
		boolean value = false;
		if(templateIds!= null)
		{
			for (CCDAII templateId : templateIds)
			{
				if(templateId.getExtValue()!= null)
				{
					return true;
				}
			}
		}
		return value;
	}
	
	public static Date convertStringToDate(final String string, String format)throws ParseException
	{
		Date date = null;
		if (!ApplicationUtil.isEmpty(string))
		{
			final DateFormat formatter = new SimpleDateFormat(format,
						Locale.ENGLISH);
			formatter.setLenient(false);
			date = formatter.parse(string);
		}
		return date;
	}
	
	
	public static Timestamp getTsFromString(String timestamp, String format)throws ParseException {
		
		Date d  = convertStringToDate(timestamp,format);
			return  new Timestamp(d.getTime());
			
	}
	
	public static boolean validateDate(String date)
	{
		boolean isValid = true;
		try{
			convertStringToDate(date, ApplicationConstants.DAY_FORMAT);
		}catch(ParseException pe){
			isValid = false;
		}
		
		return isValid;
	}
	
	public static boolean validateDateTime(String date)
	{
		boolean isValid = true;
		Timestamp ts = null;
		try{
			ts = getTsFromString(date, ApplicationConstants.MINUTE_FORMAT);
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(ts.getTime());
			if(cal.get(Calendar.HOUR) == 0)
			{
				isValid = false;
			}
		}catch(ParseException pe){
			isValid = false;
		}
		
		return isValid;
	}
	
	public static boolean validateDateTimeSecond(String date)
	{
		boolean isValid = true;
		Timestamp ts = null;
		try{
			ts = getTsFromString(date, ApplicationConstants.SECOND_FORMAT);
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(ts.getTime());
			if(cal.get(Calendar.HOUR) == 0)
			{
				isValid = false;
			}
		}catch(ParseException pe){
			isValid = false;
		}
		
		return isValid;
	}
	
	
	public static boolean checkDateRange(String minDate, String actualDate)
	{
		Date date;
		boolean isValid = true;
		String format;
		try
		{
			format = getFormat(actualDate);
			date = convertStringToDate(actualDate, format);
			format = getFormat(minDate);
			isValid =  date.after(convertStringToDate(minDate, format)) && date.before(new Date());
		}catch(ParseException pe)
		{
			isValid = false;
		}catch(NullPointerException ne)
		{
			isValid = false;
		}
		
		return isValid;
	}
	
	public static String getFormat(String date)
	{
		String format;
		if(validateYearFormat(date))
		{
			format = ApplicationConstants.YEAR_FORMAT;
		}
		else if(validateMonthFormat(date))
		{
			format = ApplicationConstants.MONTH_FORMAT;
		}else if (validateDayFormat(date) )
		{
			format = ApplicationConstants.DAY_FORMAT;
		}else if (validateMinuteFormat(date))
		{
			format = ApplicationConstants.MINUTE_FORMAT;
		}else if (validateSecondFormat(date))
		{
			format = ApplicationConstants.SECOND_FORMAT;
		}else 
		{
			format = "";
		}
		return format;
	}
	
	public static boolean checkDateRange(CCDADataElement minDate,  String actualDate, CCDADataElement maxDate)
	{
		Date date;
		Date minimumDate;
		Date maximumDate;
		boolean isValid = false;
		
		try
		{
			if(minDate!= null && actualDate != null)
			{
				if(maxDate != null)
				{
					minimumDate = convertStringToDate(minDate.getValue(), getFormat(minDate.getValue()));
					maximumDate = convertStringToDate(maxDate.getValue(), getFormat(maxDate.getValue()));
					date = convertStringToDate(actualDate.substring(0, 8), ApplicationConstants.DAY_FORMAT);
					isValid = date.equals(minimumDate)||date.after(minimumDate) &&
							  date.equals(maximumDate)|| date.before(maximumDate);
				}else if(maxDate==null)
				{
					minimumDate = convertStringToDate(minDate.getValue(), getFormat(minDate.getValue()));
					date = convertStringToDate(actualDate, getFormat(actualDate));
					isValid = date.equals(minimumDate)||date.after(minimumDate);				}
			}
			
		}catch(ParseException pe)
		{
			isValid = false;
		}catch(NullPointerException ne)
		{
			isValid = false;
		}
		
		return isValid;
	}
	
	
	public static boolean checkDateRange(CCDADataElement actMinDate, CCDADataElement actMaxDate, CCDADataElement obsMinDate, CCDADataElement obsMaxDate)
	{
		boolean isValid = false;
		Date concernActMinDate;
		Date concernActMaxDate;
		Date observationMinDate;
		Date observationMaxDate;
		
		try
		{
			if(actMinDate!= null)
			{
				if(actMaxDate == null)
				{
					if(obsMinDate != null && obsMaxDate==null)
					{
						observationMinDate = convertStringToDate(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						concernActMinDate = convertStringToDate(actMinDate.getValue(), getFormat(actMinDate.getValue()));
						isValid = observationMinDate.equals(concernActMinDate)||observationMinDate.after(concernActMinDate);
					}else if(obsMinDate != null && obsMaxDate!=null)
					{
						observationMinDate = convertStringToDate(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						observationMaxDate = convertStringToDate(obsMaxDate.getValue(),getFormat(obsMaxDate.getValue()));
						concernActMinDate = convertStringToDate(actMinDate.getValue(),getFormat(actMinDate.getValue()));
						isValid = (observationMinDate.equals(concernActMinDate) || observationMinDate.after(concernActMinDate))&&
								  (observationMaxDate.equals(observationMinDate) || observationMaxDate.after(observationMinDate));
					}
				}else if(actMaxDate!=null)
				{
					if(obsMinDate != null && obsMaxDate==null)
					{
						concernActMinDate = convertStringToDate(actMinDate.getValue(),getFormat(actMinDate.getValue()));
						concernActMaxDate = convertStringToDate(actMaxDate.getValue(),getFormat(actMaxDate.getValue()));
						observationMinDate = convertStringToDate(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						isValid = (observationMinDate.equals(concernActMinDate) || observationMinDate.after(concernActMinDate))&&
									(observationMinDate.equals(concernActMaxDate) || observationMinDate.before(concernActMaxDate));
					}else if(obsMinDate != null && obsMaxDate!=null)
					{
						concernActMinDate = convertStringToDate(actMinDate.getValue(),getFormat(actMinDate.getValue()));
						concernActMaxDate = convertStringToDate(actMaxDate.getValue(),getFormat(actMaxDate.getValue()));
						observationMinDate = convertStringToDate(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						observationMaxDate = convertStringToDate(obsMaxDate.getValue(),getFormat(obsMaxDate.getValue()));
						isValid = (observationMinDate.equals(concernActMinDate)|| observationMinDate.after(concernActMinDate))&&
								  (observationMaxDate.equals(concernActMaxDate) || observationMaxDate.before(concernActMaxDate))&&
								  (observationMaxDate.equals(observationMinDate) || observationMaxDate.after(observationMinDate))&& 
								  (observationMaxDate.equals(concernActMaxDate) || observationMaxDate.before(concernActMaxDate));
					}
				}
			}
			
		}catch(ParseException pe)
		{
			isValid = false;
		}catch(NullPointerException ne)
		{
			isValid = false;
		}
		
		return isValid;
	}
	
	
	public static boolean validateYearFormat(String date)
	{
		return date!=null ? date.matches(ApplicationConstants.YEAR_PATTERN) : false;
	}
	
	public static boolean validateMonthFormat(String date)
	{
		return date!=null ? date.matches(ApplicationConstants.MONTH_PATTERN) : false;
	}
	
	public static boolean validateDayFormat(String date)
	{
		return date!=null ? date.matches(ApplicationConstants.DAY_PATTERN) : false;
	}
	
	public static boolean validateMinuteFormat(String date)
	{
		return date!=null ? date.matches(ApplicationConstants.MINUTE_PATTERN):false;
	}
	
	public static boolean validateSecondFormat(String date)
	{
		return date!=null ? date.matches(ApplicationConstants.SECOND_PATTERN) : false;
	}
	
	
	public static boolean validateDisplayName(String code, String codeSystem, String displayName )
	{
		boolean result = false;
		if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(codeSystem) && !ApplicationUtil.isEmpty(displayName))
		{
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConstants.CODE_DISPLAYNAME_VALIDATION_URL)
					.queryParam("code", code)
					.queryParam("codeSystems", ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem)==null ? "\"\"" : ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem))
					.queryParam("displayName", displayName.toUpperCase());
			RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
			result = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
		}
		return result;
	}
	
	public static boolean validateCodeForValueset(String code, String valuesetId)
	{
		boolean result = false;
		if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(valuesetId))
		{
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConstants.CODE_VALUSET_VALIDATION_URL)
					.queryParam("code", code)
					.queryParam("valuesetOids", valuesetId);
		
			RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
			result = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
		}
		
		return result;
		
	}
	
	public static boolean validateCodeForCodeSystem(String code, String codeSystem)
	{
		boolean result = false;
		if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(codeSystem))
		{
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConstants.CODE_CODESYSTEM_VALIDATION_URL)
			        .queryParam("code",code)
			        .queryParam("codeSystems", ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem)==null ? "\"\"" : ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem));
			
			RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
		    result = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
		}
		return result;
		
	}
	
	private static ClientHttpRequestFactory getClientHttpRequestFactory() {
	    int timeout = 5000;
	    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
	      new HttpComponentsClientHttpRequestFactory();
	    clientHttpRequestFactory.setConnectTimeout(timeout);
	    return clientHttpRequestFactory;
	}
	
	/*public static boolean validateProblemStatusCode(String problemActStatuscode, List<CCDAProblemObs> probObservations)
	{
		
		boolean isValid = false;
		boolean active = false;
		boolean suspended = false;
		for(CCDAProblemObs problemObs : probObservations)
		{
			if(problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.PROBLEM_STATUS.ACTIVE.getstatus()))
			{
				 active = true;
			}else if(problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.PROBLEM_STATUS.RESOLVED.getstatus()) ||
					problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.PROBLEM_STATUS.INACTIVE.getstatus())||
					problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.PROBLEM_STATUS.COMPLETED.getstatus()))
			{
				suspended = true;
			}
		}
		
		if(active == true && suspended == true)
		{
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
		}else if(active == false && suspended == true)
		{
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.COMPLETED.getstatus());
		}else if (active == true && suspended == false)
		{
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
		}
			
		
	    return isValid;
		
	}*/
	
	public static boolean validateProblemStatusCode(String problemActStatuscode, List<CCDAProblemObs> probObservations)

	{

		boolean isValid = false;
		boolean active = false;
		boolean completed = false;

		for(CCDAProblemObs problemObs : probObservations)
		{
			if(problemObs.getEffTime()!= null)
			{
				if(problemObs.getEffTime().getLowPresent() && !problemObs.getEffTime().getHighPresent())
				{
					active = true;
				}
				else if(problemObs.getEffTime().getLowPresent() && problemObs.getEffTime().getHighPresent())
				{
					completed = true;
				}
			}else
			{
				return false;
			}
		}

		if(active == true && completed == true)
		{
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
		}
		else if(active == false && completed == true)
		{
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.COMPLETED.getstatus()) ||
			problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.SUSPENDED.getstatus()) ||
			problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.COMPLETED.getstatus());
		}
		else if (active == true && completed == false)
		{
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
		}
		return isValid;
	}
	
	public static boolean validateProblemStatusCode(CCDAEffTime effectiveTime, String concernStatusCode)
	{
		
		boolean isValid = true;
		
		if(effectiveTime != null)
		{
			if(concernStatusCode.equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.COMPLETED.getstatus()) || 
					concernStatusCode.equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.SUSPENDED.getstatus()))
			{
				isValid = effectiveTime.getHighPresent();
			}
			else if(concernStatusCode.equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.ACTIVE.getstatus()))
			{
				isValid = !effectiveTime.getHighPresent();
			}else
				isValid = false;
		}else 
		{
			isValid = false;
		}
		
	    return isValid;
		
	}
	
	public static int calculateActualPoints(int maxPoints, int actualPoints)
	{
		float percentage = (actualPoints * 100)/maxPoints;
		
		if(percentage < 25)
		{
			return 0;
		}else if (percentage >=25 && percentage <50)
		{
			return 1;
		}else if(percentage >=50 && percentage <75)
		{
			return 2;
		}else if(percentage >=75 && percentage <100)
		{
			return 3;
		}else if(percentage ==100)
		{
			return 4;
		}else
			return 0;
	}
	
	public static void calculateSectionGradeAndIssues(List<CCDAScoreCardRubrics> rubricsList, Category category)
	{
		float actualPoints =0;
		float maxPoints = 0;
		int percentage ;
		int categoryIssues=0;
		String categoryGrade;
		
		for(CCDAScoreCardRubrics rubrics : rubricsList)
		{
			actualPoints = actualPoints + rubrics.getRubricScore();
			maxPoints++;
			if(rubrics.getNumberOfIssues()!=0)
			{
				categoryIssues = categoryIssues + rubrics.getNumberOfIssues();
			}
		}
		
		percentage = Math.round((actualPoints * 100)/maxPoints);
		if(percentage < 70)
		{
			categoryGrade = "D";
		}else if (percentage >=70 && percentage <80)
		{
			categoryGrade = "C";
		}else if(percentage >=80 && percentage <85)
		{
			categoryGrade=  "B-";
		}else if(percentage >=85 && percentage <90)
		{
			categoryGrade = "B+";
		}else if(percentage >=90 && percentage <95)
		{
			categoryGrade =  "A-";
		}else if(percentage >=95 && percentage <=100)
		{
			categoryGrade = "A+";
		}else
		{
			categoryGrade =  "UNKNOWN GRADE";
		}
		category.setCategoryGrade(categoryGrade);
		category.setCategoryNumericalScore(percentage);
		category.setNumberOfIssues(categoryIssues);
	}
	
	public static void calculateFinalGradeAndIssues(List<Category> categoryList, Results results)
	{
		float finalMaxPoints = 0;
		float finalActualPoints = 0;
		String finalGrade = "";
		int numberOfIssues= 0;
		
		for (Category category : categoryList)
		{
			if(!(category.isFailingConformance() || category.isNullFlavorNI()))
			{
				for(CCDAScoreCardRubrics rubrics : category.getCategoryRubrics())
				{
					if(!rubrics.getRule().equalsIgnoreCase(ApplicationConstants.CODE_DISPLAYNAME_REQUIREMENT))
					{
						finalMaxPoints++;
						finalActualPoints = finalActualPoints + rubrics.getRubricScore();
					}
				}
				numberOfIssues = numberOfIssues + category.getNumberOfIssues();
			}
		}
		
		int percentage = Math.round((finalActualPoints * 100)/finalMaxPoints);
		if(percentage < 70)
		{
			finalGrade =  "D";
		}else if (percentage >=70 && percentage <80)
		{
			finalGrade = "C";
		}else if(percentage >=80 && percentage <85)
		{
			finalGrade = "B-";
		}else if(percentage >=85 && percentage <90)
		{
			finalGrade = "B+";
		}else if(percentage >=90 && percentage <95)
		{
			finalGrade = "A-";
		}else if(percentage >=95 && percentage <=100)
		{
			finalGrade = "A+";
		}
		
		results.setFinalGrade(finalGrade);
		results.setFinalNumericalGrade(percentage);
		results.setNumberOfIssues(numberOfIssues);
	}
	
	public static String calculateIndustryAverageGrade(int industryAverageScore)
	{
		
		if(industryAverageScore < 70)
		{
			return "D";
		}else if (industryAverageScore >=70 && industryAverageScore <80)
		{
			return "C";
		}else if(industryAverageScore >=80 && industryAverageScore <85)
		{
			return  "B-";
		}else if(industryAverageScore >=85 && industryAverageScore <90)
		{
			return "B+";
		}else if(industryAverageScore >=90 && industryAverageScore <95)
		{
			return  "A-";
		}else if(industryAverageScore >=95 && industryAverageScore <=100)
		{
			return "A+";
		}else
		{
			return  "UNKNOWN GRADE";
		}
		
	}
	
	public static float calculateRubricScore(int maxPoints, int actualPoints)
	{
		if(maxPoints!=0)
		{
			return (float)actualPoints/(float)maxPoints;
		}else 
			return 0;
	}
	
	public static String checkDocType(CCDARefModel ccdaModels)
	{
		String docType = "";
		
		if(ccdaModels.getAllergy()!=null && !ccdaModels.getAllergy().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getAllergy().getSectionTemplateId()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
				
		}
		else if(ccdaModels.getEncounter()!=null && !ccdaModels.getAllergy().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getEncounter().getTemplateId()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
				
		}
		else if(ccdaModels.getAllergy()!=null && !ccdaModels.getAllergy().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getMedication().getTemplateIds()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
				
		}
		else if(ccdaModels.getAllergy()!=null && !ccdaModels.getAllergy().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getImmunization().getTemplateIds()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getVitalSigns()!=null && !ccdaModels.getAllergy().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getVitalSigns().getTemplateIds()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getSmokingStatus()!=null && !ccdaModels.getAllergy().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getSmokingStatus().getSectionTemplateIds()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		
		return docType;
	}
	
	public static void debugLog(String debugMessage) {
		if (!ApplicationConstants.IN_DEVELOPMENT_MODE)
			return;
		System.out.println(System.lineSeparator() + "Debug Log:");
		System.out.println(debugMessage + System.lineSeparator());
	}
	
	public static void debugLog(String debugTopic, String debugMessage) {
		debugLog("For " + debugTopic + ":" + System.lineSeparator() + debugMessage);
	}
	
	public static String convertStackTraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
}
