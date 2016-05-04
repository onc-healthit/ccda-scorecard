package org.sitenv.service.ccda.smartscorecard.util;

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
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.Results;
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
	
	public static Date convertStringToDate(final String string, String format)throws ParseException
	{
		Date date = null;
		if (!ApplicationUtil.isEmpty(string))
		{
			final DateFormat formatter = new SimpleDateFormat(format,
						Locale.ENGLISH);
			formatter.setLenient(false);
			final Date utilDate = formatter.parse(string);
			date = new java.sql.Date(utilDate.getTime());
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
	
	
	public static boolean checkDateRange(String minDate, String actualDate, String format)
	{
		Date date;
		boolean isValid = true;
		
		try
		{
			date = convertStringToDate(actualDate, format);
			isValid =  date.after(convertStringToDate(minDate, ApplicationConstants.DAY_FORMAT)) && date.before(new Date());
		}catch(ParseException pe)
		{
			isValid = false;
		}
		
		return isValid;
	}
	
	public static boolean checkDateRange(String minDate, String actualDate, String maxDate, String format)
	{
		Date date;
		boolean isValid = true;
		
		try
		{
			date = convertStringToDate(actualDate, format);
			if(minDate.equals(maxDate))
			{
				isValid = minDate.equals(actualDate);
			}else
			{
				isValid =  date.after(convertStringToDate(minDate, ApplicationConstants.DAY_FORMAT)) && 
						date.before(convertStringToDate(maxDate, ApplicationConstants.DAY_FORMAT));
			}
		}catch(ParseException pe)
		{
			isValid = false;
		}
		
		return isValid;
	}
	
	
	public static boolean validateDayFormat(String date)
	{
		return date.matches(ApplicationConstants.DAY_PATTERN);
	}
	
	public static boolean validateMinuteFormat(String date)
	{
		return date.matches(ApplicationConstants.MINUTE_PATTERN);
	}
	
	public static boolean validateSecondFormat(String date)
	{
		return date.matches(ApplicationConstants.SECOND_PATTERN);
	}
	
	
	public static boolean validateDisplayName(String code, String codeSystemName, String displayName )
	{
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConstants.CODE_DISPLAYNAME_VALIDATION_URL)
		        .queryParam("code", code==null?"":code)
		        .queryParam("codeSystems", codeSystemName==null?"":codeSystemName)
		        .queryParam("displayName", displayName==null?"":displayName);
		
		RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	    boolean value = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
	    return value;
		
	}
	
	public static boolean validateCodeForValueset(String code, String valusetId)
	{
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ApplicationConstants.CODE_VALUSET_VALIDATION_URL)
		        .queryParam("code", code==null?"":code)
		        .queryParam("valuesetOids", valusetId==null?"":valusetId);
		
		RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	    boolean value = restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class);
	    return value;
		
	}
	
	private static ClientHttpRequestFactory getClientHttpRequestFactory() {
	    int timeout = 5000;
	    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
	      new HttpComponentsClientHttpRequestFactory();
	    clientHttpRequestFactory.setConnectTimeout(timeout);
	    return clientHttpRequestFactory;
	}
	
	public static boolean validateProblemStatusCode(String problemStatuscode, String concernStatusCode)
	{
		
		boolean isValid = true;
		if(concernStatusCode.equalsIgnoreCase(ApplicationConstants.PROBLEMACT_STATUS.ACTIVE.getstatus()))
		{
			isValid = problemStatuscode.equalsIgnoreCase(ApplicationConstants.PROBLEM_STATUS.ACTIVE.getstatus());
		}
		else if(concernStatusCode.equalsIgnoreCase(ApplicationConstants.PROBLEMACT_STATUS.COMPLETED.getstatus()))
		{
			isValid = problemStatuscode.equalsIgnoreCase(ApplicationConstants.PROBLEM_STATUS.RESOLVED.getstatus()) ||
					problemStatuscode.equalsIgnoreCase(ApplicationConstants.PROBLEM_STATUS.INACTIVE.getstatus());
		}
		
	    return isValid;
		
	}
	
	public static boolean validateProblemStatusCode(CCDAEffTime effectiveTime, String concernStatusCode)
	{
		
		boolean isValid = true;
		
		if(effectiveTime != null)
		{
			if(concernStatusCode.equalsIgnoreCase(ApplicationConstants.PROBLEMACT_STATUS.COMPLETED.getstatus()) || 
					concernStatusCode.equalsIgnoreCase(ApplicationConstants.PROBLEMACT_STATUS.SUSPENDED.getstatus()))
			{
				isValid = effectiveTime.getHighPresent();
			}
			else if(concernStatusCode.equalsIgnoreCase(ApplicationConstants.PROBLEMACT_STATUS.ACTIVE.getstatus()))
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
	
	public static Results calculateFinalGrade(List<Category> categoryList, Results results)
	{
		int finalMaxPoints = 0;
		int finalActualPoints = 0;
		String finalGrade = "";
		
		for (Category category : categoryList)
		{
			for(CCDAScoreCardRubrics rubrics : category.getCategoryRubrics())
			{
				finalMaxPoints = finalMaxPoints + rubrics.getMaxPoints();
				finalActualPoints = finalActualPoints + rubrics.getActualPoints();
			}
		}
		
		float percentage = (finalActualPoints * 100)/finalMaxPoints;
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
		results.setFinalNumericalGrade(Math.round(percentage));
		return results;
	}
	
	
}
