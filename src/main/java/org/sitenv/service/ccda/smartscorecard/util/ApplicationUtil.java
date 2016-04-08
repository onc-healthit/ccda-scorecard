package org.sitenv.service.ccda.smartscorecard.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.sitenv.ccdaparsing.model.CCDADataElement;



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
			final Date utilDate = formatter.parse(string);
			date = new java.sql.Date(utilDate.getTime());
		}
		return date;
	}
	
	
	public static Timestamp getTsFromString(String timestamp, String format)throws ParseException {
		
		Date d  = convertStringToDate(timestamp,format);
			return  new Timestamp(d.getTime());
			
	}
	
}
