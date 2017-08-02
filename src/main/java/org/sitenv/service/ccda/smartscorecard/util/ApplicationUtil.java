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
import org.sitenv.service.ccda.smartscorecard.cofiguration.ApplicationConfiguration;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
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
	
	public static boolean checkLabResultType(String resultType)
	{
		if(resultType!= null)
		{
			return resultType.equalsIgnoreCase("PQ") || resultType.equalsIgnoreCase("IVL_PQ") ;
		}else 
			return false;
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
	
	public static boolean isRootAndExtensionPresent(CCDAII templateId)
	{
		return templateId!=null && templateId.getExtValue()!=null && templateId.getRootValue()!=null;
	}
	
	public static boolean isRootValuePresent(CCDAII templateId)
	{
		return templateId!=null && templateId.getRootValue()!=null;
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
	
	public static Timestamp getTsFromString(String date)throws ParseException {
		
		Date d  = convertStringToDate(date,getFormat(date));
			return  new Timestamp(d.getTime());
			
	}
	
	public static boolean validateDate(String date)
	{
		boolean isValid = true;
		String format;
		try{
			format = getFormat(date);
			convertStringToDate(date, format);
		}catch(ParseException pe){
			isValid = false;
		}
		
		return isValid;
	}
	
	public static boolean validateBirthDate(String birthDate)
	{
		boolean isValid = true;
		String format;
		Timestamp ts;
		try{
			format = getFormat(birthDate);
			ts = getTsFromString(birthDate, format);
			isValid = ts.before(new Timestamp(new Date().getTime()));
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
	
	
	/*public static boolean checkDateRange(String minDate, CCDAEffTime effectiveTime)
	{
		boolean isValid = false;
		if(effectiveTime.getValuePresent())
		{
			isValid = checkDateRange(minDate, effectiveTime.getValue());
		}
		
		if(effectiveTime.isSingleAdministrationValuePresent())
		{
			isValid = checkDateRange(minDate, effectiveTime.getSingleAdministration());
		}
		
		if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent())
		{
			isValid = checkDateRange(minDate, effectiveTime.getLow()) || checkDateRange(minDate, effectiveTime.getHigh());
		}
		return isValid;
	}*/
	
	//Date Range check using DOD or current TS as end time
	/*public static boolean checkDateRange(String minDate, CCDAEffTime effectiveTime,CCDAEffTime endDate)
	{
		boolean isValid = false;
		if(effectiveTime.getValuePresent())
		{
			isValid = checkDateRange(minDate, effectiveTime.getValue(),endDate);
		}
		
		if(effectiveTime.isSingleAdministrationValuePresent())
		{
			isValid = checkDateRange(minDate, effectiveTime.getSingleAdministration(),endDate);
		}
		
		if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent())
		{
			isValid = checkDateRange(minDate, effectiveTime.getLow(),endDate) || checkDateRange(minDate, effectiveTime.getHigh(),endDate);
		}
		return isValid;
	}*/
	
	//Date Range check using DOD or current TS as end time
	public static boolean checkDateRange(PatientDetails patientDetails,CCDAEffTime effectiveTime) {
		boolean isValid = false;
		if (effectiveTime.getValuePresent()) {
			isValid = checkDateRange(patientDetails, effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = checkDateRange(patientDetails, effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent()) {
			isValid = checkDateRange(patientDetails, effectiveTime.getLow().getValue());
		}
		if (effectiveTime.getHighPresent()) {
			isValid = checkDateRange(patientDetails, effectiveTime.getHigh().getValue());
		}
		return isValid;
	}
	
	
	//Date Range check using DOD or current TS as end time
	public static boolean checkDateRange(PatientDetails patientDetail, String actualDate)
	{
		Timestamp ts;
		Timestamp beforeTs;
		Timestamp afterTs;
		boolean isValid = false;
		try
		{
			isValid = patientDetail.isDodPresent()?(patientDetail.isDobValid() && patientDetail.isDodValid()):patientDetail.isDobValid();
			if(isValid)
			{
				ts = getTsFromString(actualDate);
				afterTs = getTsFromString(patientDetail.getPatientDob());
				beforeTs = patientDetail.isDodPresent()?getTsFromString(patientDetail.getPatientDod()):new Timestamp(new Date().getTime());
				isValid = ts.after(afterTs) && 
						ts.before(beforeTs);
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
	
	//Date Range check using DOD or current TS as end time
	/*public static boolean checkDateRange(PatientDetails patientDetail, CCDADataElement actualDate)
	{
		Date ts;
		boolean isValid = false;
		String format;
		try
		{
			if(actualDate!=null)
			{
				format = getFormat(actualDate.getValue());
				ts = getTsFromString(actualDate.getValue(), format);
				format = getFormat(minDate);
				isValid =  ts.after(getTsFromString(minDate, format)) && ts.before(getDodTs(endDate));
			}
		}catch(ParseException pe)
		{
			isValid = false;
		}catch(NullPointerException ne)
		{
			isValid = false;
		}
		
		return isValid;
	}*/
	
	public static boolean isDodValid(String birthDate, String dod) {
		Timestamp ts = null;
		String format;
		boolean isValid = false;

		try {
			format = getFormat(dod);
			ts = getTsFromString(dod, format);
			isValid = ts.after(getTsFromString(birthDate, format)) && ts.before(new Timestamp(new Date().getTime()));
		} catch (ParseException pe) {
			isValid = false;
		} catch (NullPointerException ne) {
			isValid = false;
		}
		return isValid;
	}
	
	
	
	public static boolean checkDateRange(String minDate, String actualDate)
	{
		Timestamp ts;
		boolean isValid = false;
		String format;
		try
		{
			format = getFormat(actualDate);
			ts = getTsFromString(actualDate, format);
			format = getFormat(minDate);
			isValid =  ts.after(getTsFromString(minDate, format)) && ts.before(new Timestamp(new Date().getTime()));
		}catch(ParseException pe)
		{
			isValid = false;
		}catch(NullPointerException ne)
		{
			isValid = false;
		}
		
		return isValid;
	}
	
	
	public static boolean isEffectiveTimePresent(CCDAEffTime effectiveTime)
	{
		return effectiveTime.getValuePresent() || effectiveTime.isSingleAdministrationValuePresent() || effectiveTime.getLowPresent() || effectiveTime.getHighPresent();
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
		Date ts;
		Date minimumTs;
		Date maximumTs;
		boolean isValid = false;
		
		try
		{
			if(minDate!= null && actualDate != null)
			{
				if(maxDate != null)
				{
					minimumTs = getTsFromString(minDate.getValue(), getFormat(minDate.getValue()));
					maximumTs = getTsFromString(maxDate.getValue(), getFormat(maxDate.getValue()));
					ts = getTsFromString(actualDate, getFormat(actualDate));
					isValid = ts.equals(minimumTs)||ts.after(minimumTs) &&
							  ts.equals(maximumTs)|| ts.before(maximumTs);
				}else if(maxDate==null)
				{
					minimumTs = getTsFromString(minDate.getValue(), getFormat(minDate.getValue()));
					ts = getTsFromString(actualDate, getFormat(actualDate));
					isValid = ts.equals(minimumTs)||ts.after(minimumTs);				
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
	
	public static boolean checkDateRange(CCDAEffTime valueEffetiveTime,CCDAEffTime rangeEffectiveTime)
	{
		boolean isValid = false;
		if(valueEffetiveTime.getValuePresent() && rangeEffectiveTime.getLowPresent() && rangeEffectiveTime.getHighPresent())
		{
			isValid = checkDateRange(rangeEffectiveTime.getLow(), valueEffetiveTime.getValue(), rangeEffectiveTime.getHigh());
		}
		
		if(valueEffetiveTime.getValuePresent() && rangeEffectiveTime.getValuePresent())
		{
			if(valueEffetiveTime.getValue()!=null && valueEffetiveTime.getValue().equals(rangeEffectiveTime.getValue()))
			{
				isValid = true;
			}else
			{
				isValid = checkTimestampRange(valueEffetiveTime.getValue(), rangeEffectiveTime.getValue());
			}
		}
		if(valueEffetiveTime.getLowPresent() && valueEffetiveTime.getHighPresent() && rangeEffectiveTime.getLowPresent() && rangeEffectiveTime.getHighPresent())
		{
			isValid = checkDateRange(rangeEffectiveTime.getLow(),rangeEffectiveTime.getHigh(),valueEffetiveTime.getLow(),valueEffetiveTime.getHigh());
		}
		return isValid;
	}
	
	
	public static boolean checkDateRange(CCDADataElement actMinDate, CCDADataElement actMaxDate, CCDADataElement obsMinDate, CCDADataElement obsMaxDate)
	{
		boolean isValid = false;
		Date concernActMinTs;
		Date concernActMaxTs;
		Date observationMinTs;
		Date observationMaxTs;
		
		try
		{
			if(actMinDate!= null)
			{
				if(actMaxDate == null)
				{
					if(obsMinDate != null && obsMaxDate==null)
					{
						observationMinTs = getTsFromString(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						concernActMinTs = getTsFromString(actMinDate.getValue(), getFormat(actMinDate.getValue()));
						isValid = observationMinTs.equals(concernActMinTs)||observationMinTs.after(concernActMinTs);
					}else if(obsMinDate != null && obsMaxDate!=null)
					{
						observationMinTs = getTsFromString(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						observationMaxTs = getTsFromString(obsMaxDate.getValue(),getFormat(obsMaxDate.getValue()));
						concernActMinTs = getTsFromString(actMinDate.getValue(),getFormat(actMinDate.getValue()));
						isValid = (observationMinTs.equals(concernActMinTs) || observationMinTs.after(concernActMinTs))&&
								  (observationMaxTs.equals(observationMinTs) || observationMaxTs.after(observationMinTs));
					}
				}else if(actMaxDate!=null)
				{
					if(obsMinDate != null && obsMaxDate==null)
					{
						concernActMinTs = getTsFromString(actMinDate.getValue(),getFormat(actMinDate.getValue()));
						concernActMaxTs = getTsFromString(actMaxDate.getValue(),getFormat(actMaxDate.getValue()));
						observationMinTs = getTsFromString(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						isValid = (observationMinTs.equals(concernActMinTs) || observationMinTs.after(concernActMinTs))&&
									(observationMinTs.equals(concernActMaxTs) || observationMinTs.before(concernActMaxTs));
					}else if(obsMinDate != null && obsMaxDate!=null)
					{
						concernActMinTs = getTsFromString(actMinDate.getValue(),getFormat(actMinDate.getValue()));
						concernActMaxTs = getTsFromString(actMaxDate.getValue(),getFormat(actMaxDate.getValue()));
						observationMinTs = getTsFromString(obsMinDate.getValue(),getFormat(obsMinDate.getValue()));
						observationMaxTs = getTsFromString(obsMaxDate.getValue(),getFormat(obsMaxDate.getValue()));
						isValid = (observationMinTs.equals(concernActMinTs)|| observationMinTs.after(concernActMinTs))&&
								  (observationMaxTs.equals(concernActMaxTs) || observationMaxTs.before(concernActMaxTs))&&
								  (observationMaxTs.equals(observationMinTs) || observationMaxTs.after(observationMinTs))&& 
								  (observationMaxTs.equals(concernActMaxTs) || observationMaxTs.before(concernActMaxTs));
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
	
	public static boolean checkTimestampRange(String minDate, String actualDate)
	{
		Timestamp ts;
		boolean isValid = false;
		String format;
		try
		{
			format = getFormat(actualDate);
			ts = getTsFromString(actualDate, format);
			format = getFormat(minDate);
			isValid =  ts.after(getTsFromString(minDate, format));
		}catch(ParseException pe)
		{
			isValid = false;
		}catch(NullPointerException ne)
		{
			isValid = false;
		}
		
		return isValid;
	}
	
	public static boolean compareTimestamps(Timestamp minTimestamp, Timestamp maxTimestamp)
	{
		return maxTimestamp.after(minTimestamp);
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
		return date!=null ? (date.matches(ApplicationConstants.MINUTE_PATTERN_PLUS_OFFSET) || date.matches(ApplicationConstants.MINUTE_PATTERN_MINUS_OFFSET)):false;
	}
	
	public static boolean validateSecondFormat(String date)
	{
		return date!=null ? (date.matches(ApplicationConstants.SECOND_PATTERN_MINUS_OFFSET) || date.matches(ApplicationConstants.SECOND_PATTERN_PLUS_OFFSET)) : false;
	}
	
	public static boolean validateYearFormat(CCDADataElement date)
	{
		return date!=null ? (date.getValue()!=null ? date.getValue().matches(ApplicationConstants.YEAR_PATTERN):false):false;
	}
	
	public static boolean validateMonthFormat(CCDADataElement date)
	{
		return date!=null ? (date.getValue()!=null ? date.getValue().matches(ApplicationConstants.MONTH_PATTERN):false):false;
	}
	
	public static boolean validateDayFormat(CCDADataElement date)
	{
		return date!=null ? (date.getValue()!=null ? date.getValue().matches(ApplicationConstants.DAY_PATTERN):false):false;
	}
	
	public static boolean validateMinuteFormat(CCDADataElement date)
	{
		return date!=null ? (date.getValue()!=null ? 
				date.getValue().matches(ApplicationConstants.MINUTE_PATTERN_PLUS_OFFSET) 
				|| date.getValue().matches(ApplicationConstants.MINUTE_PATTERN_MINUS_OFFSET):false):false;
	}
	
	public static boolean validateSecondFormat(CCDADataElement date)
	{
		return date!=null ? (date.getValue()!=null ? 
				date.getValue().matches(ApplicationConstants.SECOND_PATTERN_MINUS_OFFSET) 
				|| date.getValue().matches(ApplicationConstants.SECOND_PATTERN_PLUS_OFFSET):false):false;
	}
	
	public static boolean validateYearFormat(CCDAEffTime effectiveTime)
	{
		boolean isValid = false;
		
		if(effectiveTime.getValuePresent())
		{
			isValid = validateYearFormat(effectiveTime.getValue());
		}
		
		if(effectiveTime.isSingleAdministrationValuePresent())
		{
			isValid = validateYearFormat(effectiveTime.getSingleAdministration());
		}
		
		if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent())
		{
			isValid = validateYearFormat(effectiveTime.getLow()) || validateYearFormat(effectiveTime.getHigh());
		}
		
		return isValid;
	}
	
	public static boolean validateMonthFormat(CCDAEffTime effectiveTime)
	{
		boolean isValid = false;
		
		if(effectiveTime.getValuePresent())
		{
			isValid = validateMonthFormat(effectiveTime.getValue());
		}
		
		if(effectiveTime.isSingleAdministrationValuePresent())
		{
			isValid = validateMonthFormat(effectiveTime.getSingleAdministration());
		}
		
		if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent())
		{
			isValid = validateMonthFormat(effectiveTime.getLow()) || validateMonthFormat(effectiveTime.getHigh());
		}
		
		return isValid;
	}
	
	public static boolean validateDayFormat(CCDAEffTime effectiveTime)
	{
		boolean isValid = false;
		
		if(effectiveTime.getValuePresent())
		{
			isValid = validateDayFormat(effectiveTime.getValue());
		}
		
		if(effectiveTime.isSingleAdministrationValuePresent())
		{
			isValid = validateDayFormat(effectiveTime.getSingleAdministration());
		}
		
		if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent())
		{
			isValid = validateDayFormat(effectiveTime.getLow()) || validateDayFormat(effectiveTime.getHigh());
		}
		
		return isValid;
	}
	
	public static boolean validateMinuteFormat(CCDAEffTime effectiveTime)
	{
		boolean isValid = false;
		
		if(effectiveTime.getValuePresent())
		{
			isValid = validateMinuteFormat(effectiveTime.getValue());
		}
		
		if(effectiveTime.isSingleAdministrationValuePresent())
		{
			isValid = validateMinuteFormat(effectiveTime.getSingleAdministration());
		}
		
		if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent())
		{
			isValid = validateMinuteFormat(effectiveTime.getLow()) || validateMinuteFormat(effectiveTime.getHigh());
		}
		
		return isValid;
	}
	
	public static boolean validateSecondFormat(CCDAEffTime effectiveTime)
	{
		boolean isValid = false;
		
		if(effectiveTime.getValuePresent())
		{
			isValid = validateSecondFormat(effectiveTime.getValue());
		}
		
		if(effectiveTime.isSingleAdministrationValuePresent())
		{
			isValid = validateSecondFormat(effectiveTime.getSingleAdministration());
		}
		
		if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent())
		{
			isValid = validateSecondFormat(effectiveTime.getLow()) || validateSecondFormat(effectiveTime.getHigh());
		}
		
		return isValid;
	}
	
	public static boolean isCodeSystemAvailable(String codeSystem)
	{
		boolean result = false;
		if(codeSystem!= null)
		{
			result = ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem)==null ? false : true;
		}
		return result;
	}
	
	public static boolean validateDisplayName(String code, String codeSystem, String displayName )
	{
		boolean result = false;
		if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(codeSystem) && !ApplicationUtil.isEmpty(displayName))
		{
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.CODE_DISPLAYNAME_VALIDATION_URL)
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
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.CODE_VALUSET_VALIDATION_URL)
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
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.CODE_CODESYSTEM_VALIDATION_URL)
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
					finalMaxPoints++;
					finalActualPoints = finalActualPoints + rubrics.getRubricScore();
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
		else if(ccdaModels.getEncounter()!=null && !ccdaModels.getEncounter().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getEncounter().getTemplateId()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getMedication()!=null && !ccdaModels.getMedication().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getMedication().getTemplateIds()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getImmunization()!=null && !ccdaModels.getImmunization().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getImmunization().getTemplateIds()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getProblem()!=null && !ccdaModels.getProblem().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getProblem().getSectionTemplateId()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getProcedure()!=null && !ccdaModels.getProcedure().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getProcedure().getSectionTemplateId()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getVitalSigns()!=null && !ccdaModels.getVitalSigns().isSectionNullFlavourWithNI())
		{
			if(isExtensionPresent(ccdaModels.getVitalSigns().getTemplateIds()))
			{
				docType = "R2.1";
			}else
			{
				docType = "R1.1";
			}
		}
		else if(ccdaModels.getSmokingStatus()!=null && !ccdaModels.getSmokingStatus().isSectionNullFlavourWithNI())
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
	
	public static boolean validTemplateIdFormat(String templateId)
	{
		return templateId.startsWith(ApplicationConstants.TEMPLATEID_FORMAT);
	}
	
	public static void debugLog(String debugMessage) {
		if (!ApplicationConfiguration.IN_DEVELOPMENT_MODE)
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
