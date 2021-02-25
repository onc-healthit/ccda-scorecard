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

import org.sitenv.ccdaparsing.model.CCDACode;
import org.sitenv.ccdaparsing.model.CCDADataElement;
import org.sitenv.ccdaparsing.model.CCDAEffTime;
import org.sitenv.ccdaparsing.model.CCDAII;
import org.sitenv.ccdaparsing.model.CCDAProblemObs;
import org.sitenv.ccdaparsing.model.CCDARefModel;
import org.sitenv.service.ccda.smartscorecard.configuration.ApplicationConfiguration;
import org.sitenv.service.ccda.smartscorecard.configuration.ScorecardSection;
import org.sitenv.service.ccda.smartscorecard.configuration.SectionRule;
import org.sitenv.service.ccda.smartscorecard.model.CCDAScoreCardRubrics;
import org.sitenv.service.ccda.smartscorecard.model.Category;
import org.sitenv.service.ccda.smartscorecard.model.PatientDetails;
import org.sitenv.service.ccda.smartscorecard.model.Results;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationConstants.CONCERNACT_STATUS;

public class ApplicationUtil {

	/**
	 * Checks if is null or empty.
	 * 
	 * @param input List<T> : list
	 * @return true, if input is null or Empty
	 */
	public static <T> boolean isEmpty(final List<T> list) {
		return list == null || list.isEmpty();
	}

	/**
	 * Checks if is null or empty.
	 * 
	 * @param input String : str
	 * @return true, if input is null or Zero
	 */
	public static boolean isEmpty(final String str) {
		return str == null || str.trim().length() == 0;
	}

	/**
	 * Checks if is null or empty.
	 * 
	 * @param input Integer : value
	 * @return true, input if is null or Zero
	 */
	public static boolean isEmpty(final Integer value) {
		return value == null || value.intValue() == 0;
	}

	public static boolean isEmpty(final int[] arr) {
		return arr.length == 0;
	}

	/**
	 * Checks if is null or Zero.
	 * 
	 * @param input the value
	 * @return true, if input is null or Zero.
	 */
	public static boolean isEmpty(final Short value) {
		return value == null || value.intValue() == 0;
	}

	/**
	 * Checks if an array of String is empty
	 * 
	 * @param strArr
	 * @return true, if input empty.
	 */
	public static boolean isEmpty(final String[] strArr) {
		return strArr.length == 0;
	}
	
	public static boolean isEmpty(final CCDAEffTime effectiveTime) {
		return effectiveTime == null || effectiveTime.isNullFlavour();
	}

	public static boolean checkLabResultType(String resultType) {
		if (resultType != null) {
			return resultType.equalsIgnoreCase("PQ") || resultType.equalsIgnoreCase("IVL_PQ");
		} else
			return false;
	}

	public static boolean isValueEmpty(final CCDADataElement object) {
		boolean result = true;
		if (object != null) {
			result = isEmpty(object.getValue());
		}

		return result;
	}

	public static boolean isExtensionPresent(List<CCDAII> templateIds) {
		boolean value = false;
		if (templateIds != null) {
			for (CCDAII templateId : templateIds) {
				if (templateId.getExtValue() != null) {
					return true;
				}
			}
		}
		return value;
	}

	public static boolean isExtensionPresent(CCDAII templateId) {
		return templateId != null && templateId.getExtValue() != null;
	}

	public static boolean isRootAndExtensionPresent(CCDAII templateId) {
		return templateId != null && templateId.getExtValue() != null && templateId.getRootValue() != null;
	}

	public static boolean isRootValuePresent(CCDAII templateId) {
		return templateId != null && templateId.getRootValue() != null;
	}

	public static Date convertStringToDate(final String string, String format) throws ParseException {
		Date date = null;
		if (!ApplicationUtil.isEmpty(string)) {
			final DateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
			formatter.setLenient(false);
			date = formatter.parse(string);
		}
		return date;
	}

	public static Timestamp getTsFromString(String timestamp, String format) throws ParseException {

		Date d = convertStringToDate(timestamp, format);
		return new Timestamp(d.getTime());

	}

	public static Timestamp getTsFromString(String date) throws ParseException {

		Date d = convertStringToDate(date, getFormat(date));
		return new Timestamp(d.getTime());

	}

	public static boolean validateDatesForExcelGeneration(String stringFromDate, String stringToDate) {
		String dateFormat = "yyyy-MM-dd";
		try {
			Date fromDate = convertStringToDate(stringFromDate, dateFormat);
			Date toDate = convertStringToDate(stringToDate, dateFormat);
			Date currentDate = new Date();
			if ((fromDate.equals(toDate) || fromDate.before(toDate))
					&& (toDate.equals(currentDate) || toDate.before(currentDate))) {
				return true;
			}
		} catch (ParseException e) {
			return false;
		}
		return false;

	}

	public static boolean validateFromDateForExcelGeneration(String stringFromDate) {
		String dateFormat = "yyyy-MM-dd";
		try {
			Date fromDate = convertStringToDate(stringFromDate, dateFormat);
			Date currentDate = new Date();
			if (fromDate.equals(currentDate) || fromDate.before(currentDate)) {
				return true;
			}
		} catch (ParseException e) {
			return false;
		}
		return false;

	}

	public static java.sql.Date convertStringToSqlDate(String stringDate) throws ParseException {
		String dateFormat = "yyyy-MM-dd";
		Date utilDate = convertStringToDate(stringDate, dateFormat);
		return new java.sql.Date(utilDate.getTime());
	}

	public static boolean validateDate(String date) {
		boolean isValid = true;
		String format;
		try {
			format = getFormat(date);
			convertStringToDate(date, format);
		} catch (ParseException pe) {
			isValid = false;
		}

		return isValid;
	}

	public static boolean validateBirthDate(String birthDate) {
		boolean isValid = true;
		String format;
		Timestamp ts;
		try {
			format = getFormat(birthDate);
			ts = getTsFromString(birthDate, format);
			isValid = ts.before(new Timestamp(new Date().getTime()));
			if (format.equalsIgnoreCase(ApplicationConstants.MINUTE_FORMAT)
					|| format.equalsIgnoreCase(ApplicationConstants.SECOND_FORMAT)) {
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTimeInMillis(ts.getTime());
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int min = calendar.get(Calendar.MINUTE);
				if (hour == 0 && min == 0) {
					isValid = false;
				}
			}
		} catch (ParseException pe) {
			isValid = false;
		}

		return isValid;
	}

	public static boolean validateDateTime(String date) {
		boolean isValid = true;
		Timestamp ts = null;
		try {
			ts = getTsFromString(date, ApplicationConstants.MINUTE_FORMAT);
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(ts.getTime());
			if (cal.get(Calendar.HOUR) == 0) {
				isValid = false;
			}
		} catch (ParseException pe) {
			isValid = false;
		}

		return isValid;
	}

	public static boolean validateDateTimeSecond(String date) {
		boolean isValid = true;
		Timestamp ts = null;
		try {
			ts = getTsFromString(date, ApplicationConstants.SECOND_FORMAT);
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(ts.getTime());
			if (cal.get(Calendar.HOUR) == 0) {
				isValid = false;
			}
		} catch (ParseException pe) {
			isValid = false;
		}

		return isValid;
	}

	/*
	 * public static boolean checkDateRange(String minDate, CCDAEffTime
	 * effectiveTime) { boolean isValid = false; if(effectiveTime.getValuePresent())
	 * { isValid = checkDateRange(minDate, effectiveTime.getValue()); }
	 * 
	 * if(effectiveTime.isSingleAdministrationValuePresent()) { isValid =
	 * checkDateRange(minDate, effectiveTime.getSingleAdministration()); }
	 * 
	 * if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) { isValid
	 * = checkDateRange(minDate, effectiveTime.getLow()) || checkDateRange(minDate,
	 * effectiveTime.getHigh()); } return isValid; }
	 */

	// Date Range check using DOD or current TS as end time
	/*
	 * public static boolean checkDateRange(String minDate, CCDAEffTime
	 * effectiveTime,CCDAEffTime endDate) { boolean isValid = false;
	 * if(effectiveTime.getValuePresent()) { isValid = checkDateRange(minDate,
	 * effectiveTime.getValue(),endDate); }
	 * 
	 * if(effectiveTime.isSingleAdministrationValuePresent()) { isValid =
	 * checkDateRange(minDate, effectiveTime.getSingleAdministration(),endDate); }
	 * 
	 * if(effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) { isValid
	 * = checkDateRange(minDate, effectiveTime.getLow(),endDate) ||
	 * checkDateRange(minDate, effectiveTime.getHigh(),endDate); } return isValid; }
	 */

	// Date Range check using DOD or current TS as end time
	public static boolean checkDateRange(PatientDetails patientDetails, CCDAEffTime effectiveTime) {
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

	// Date Range check using DOD or current TS as end time
	public static boolean checkDateRange(PatientDetails patientDetail, String actualDate) {
		Timestamp ts;
		Timestamp beforeTs;
		Timestamp afterTs;
		boolean isValid = false;
		try {
			isValid = patientDetail.isDodPresent() ? (patientDetail.isDobValid() && patientDetail.isDodValid())
					: patientDetail.isDobValid();
			if (isValid) {
				ts = getTsFromString(actualDate);
				afterTs = addDays(365, getTsFromString(patientDetail.getPatientDob()));
				beforeTs = patientDetail.isDodPresent() ? addDays(90,getTsFromString(patientDetail.getPatientDod()))
						: new Timestamp(new Date().getTime());
				isValid = ts.after(afterTs) && ts.before(beforeTs);
			}
		} catch (ParseException pe) {
			isValid = false;
		} catch (NullPointerException ne) {
			isValid = false;
		}

		return isValid;
	}

	public static Long dayToMiliseconds(int days) {
		Long result = Long.valueOf(days * 24 * 60 * 60 * 1000);
		return result;
	}

	public static Timestamp addDays(int days, Timestamp t1) {
		Long miliseconds = dayToMiliseconds(days);
		return new Timestamp(t1.getTime() + miliseconds);
	}

	// Date Range check using DOD or current TS as end time
	/*
	 * public static boolean checkDateRange(PatientDetails patientDetail,
	 * CCDADataElement actualDate) { Date ts; boolean isValid = false; String
	 * format; try { if(actualDate!=null) { format =
	 * getFormat(actualDate.getValue()); ts = getTsFromString(actualDate.getValue(),
	 * format); format = getFormat(minDate); isValid =
	 * ts.after(getTsFromString(minDate, format)) && ts.before(getDodTs(endDate)); }
	 * }catch(ParseException pe) { isValid = false; }catch(NullPointerException ne)
	 * { isValid = false; }
	 * 
	 * return isValid; }
	 */

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

	public static boolean checkDateRange(String minDate, String actualDate) {
		Timestamp ts;
		boolean isValid = false;
		String format;
		try {
			format = getFormat(actualDate);
			ts = getTsFromString(actualDate, format);
			format = getFormat(minDate);
			isValid = ts.after(getTsFromString(minDate, format)) && ts.before(new Timestamp(new Date().getTime()));
		} catch (ParseException pe) {
			isValid = false;
		} catch (NullPointerException ne) {
			isValid = false;
		}

		return isValid;
	}

	public static boolean isEffectiveTimePresent(CCDAEffTime effectiveTime) {
		return effectiveTime.getValuePresent() || effectiveTime.isSingleAdministrationValuePresent()
				|| effectiveTime.getLowPresent() || effectiveTime.getHighPresent();
	}

	public static String getFormat(String date) {
		String format;
		if (validateYearFormat(date)) {
			format = ApplicationConstants.YEAR_FORMAT;
		} else if (validateMonthFormat(date)) {
			format = ApplicationConstants.MONTH_FORMAT;
		} else if (validateDayFormat(date)) {
			format = ApplicationConstants.DAY_FORMAT;
		} else if (validateMinuteFormat(date)) {
			format = ApplicationConstants.MINUTE_FORMAT;
		} else if (validateSecondFormat(date)) {
			format = ApplicationConstants.SECOND_FORMAT;
		} else {
			format = "";
		}
		return format;
	}

	public static boolean checkDateRange(CCDADataElement minDate, String actualDate, CCDADataElement maxDate) {
		Date ts;
		Date minimumTs;
		Date maximumTs;
		boolean isValid = false;

		try {
			if (minDate != null && actualDate != null) {
				if (maxDate != null) {
					minimumTs = getTsFromString(minDate.getValue(), getFormat(minDate.getValue()));
					maximumTs = getTsFromString(maxDate.getValue(), getFormat(maxDate.getValue()));
					ts = getTsFromString(actualDate, getFormat(actualDate));
					isValid = ts.equals(minimumTs) || ts.after(minimumTs) && ts.equals(maximumTs)
							|| ts.before(maximumTs);
				} else if (maxDate == null) {
					minimumTs = getTsFromString(minDate.getValue(), getFormat(minDate.getValue()));
					ts = getTsFromString(actualDate, getFormat(actualDate));
					isValid = ts.equals(minimumTs) || ts.after(minimumTs);
				}
			}

		} catch (ParseException pe) {
			isValid = false;
		} catch (NullPointerException ne) {
			isValid = false;
		}

		return isValid;
	}

	public static boolean checkDateRange(CCDAEffTime valueEffetiveTime, CCDAEffTime rangeEffectiveTime) {
		boolean isValid = false;
		if (valueEffetiveTime.getValuePresent() && rangeEffectiveTime.getLowPresent()
				&& rangeEffectiveTime.getHighPresent()) {
			isValid = checkDateRange(rangeEffectiveTime.getLow(), valueEffetiveTime.getValue(),
					rangeEffectiveTime.getHigh());
		}

		if (valueEffetiveTime.getValuePresent() && rangeEffectiveTime.getValuePresent()) {
			if (valueEffetiveTime.getValue() != null
					&& valueEffetiveTime.getValue().equals(rangeEffectiveTime.getValue())) {
				isValid = true;
			} else {
				isValid = checkTimestampRange(valueEffetiveTime.getValue(), rangeEffectiveTime.getValue());
			}
		}
		if (valueEffetiveTime.getLowPresent() && valueEffetiveTime.getHighPresent()
				&& rangeEffectiveTime.getLowPresent() && rangeEffectiveTime.getHighPresent()) {
			isValid = checkDateRange(rangeEffectiveTime.getLow(), rangeEffectiveTime.getHigh(),
					valueEffetiveTime.getLow(), valueEffetiveTime.getHigh());
		}
		return isValid;
	}

	public static boolean checkDateRange(CCDADataElement actMinDate, CCDADataElement actMaxDate,
			CCDADataElement obsMinDate, CCDADataElement obsMaxDate) {
		boolean isValid = false;
		Date concernActMinTs;
		Date concernActMaxTs;
		Date observationMinTs;
		Date observationMaxTs;

		try {
			if (actMinDate != null) {
				if (actMaxDate == null) {
					if (obsMinDate != null && obsMaxDate == null) {
						observationMinTs = getTsFromString(obsMinDate.getValue(), getFormat(obsMinDate.getValue()));
						concernActMinTs = getTsFromString(actMinDate.getValue(), getFormat(actMinDate.getValue()));
						isValid = observationMinTs.equals(concernActMinTs) || observationMinTs.after(concernActMinTs);
					} else if (obsMinDate != null && obsMaxDate != null) {
						observationMinTs = getTsFromString(obsMinDate.getValue(), getFormat(obsMinDate.getValue()));
						observationMaxTs = getTsFromString(obsMaxDate.getValue(), getFormat(obsMaxDate.getValue()));
						concernActMinTs = getTsFromString(actMinDate.getValue(), getFormat(actMinDate.getValue()));
						isValid = (observationMinTs.equals(concernActMinTs) || observationMinTs.after(concernActMinTs))
								&& (observationMaxTs.equals(observationMinTs)
										|| observationMaxTs.after(observationMinTs));
					}
				} else if (actMaxDate != null) {
					if (obsMinDate != null && obsMaxDate == null) {
						concernActMinTs = getTsFromString(actMinDate.getValue(), getFormat(actMinDate.getValue()));
						concernActMaxTs = getTsFromString(actMaxDate.getValue(), getFormat(actMaxDate.getValue()));
						observationMinTs = getTsFromString(obsMinDate.getValue(), getFormat(obsMinDate.getValue()));
						isValid = (observationMinTs.equals(concernActMinTs) || observationMinTs.after(concernActMinTs))
								&& (observationMinTs.equals(concernActMaxTs)
										|| observationMinTs.before(concernActMaxTs));
					} else if (obsMinDate != null && obsMaxDate != null) {
						concernActMinTs = getTsFromString(actMinDate.getValue(), getFormat(actMinDate.getValue()));
						concernActMaxTs = getTsFromString(actMaxDate.getValue(), getFormat(actMaxDate.getValue()));
						observationMinTs = getTsFromString(obsMinDate.getValue(), getFormat(obsMinDate.getValue()));
						observationMaxTs = getTsFromString(obsMaxDate.getValue(), getFormat(obsMaxDate.getValue()));
						isValid = (observationMinTs.equals(concernActMinTs) || observationMinTs.after(concernActMinTs))
								&& (observationMaxTs.equals(concernActMaxTs)
										|| observationMaxTs.before(concernActMaxTs))
								&& (observationMaxTs.equals(observationMinTs)
										|| observationMaxTs.after(observationMinTs))
								&& (observationMaxTs.equals(concernActMaxTs)
										|| observationMaxTs.before(concernActMaxTs));
					}
				}
			}

		} catch (ParseException pe) {
			isValid = false;
		} catch (NullPointerException ne) {
			isValid = false;
		}

		return isValid;
	}

	public static boolean checkTimestampRange(String minDate, String actualDate) {
		Timestamp ts;
		boolean isValid = false;
		String format;
		try {
			format = getFormat(actualDate);
			ts = getTsFromString(actualDate, format);
			format = getFormat(minDate);
			isValid = ts.after(getTsFromString(minDate, format));
		} catch (ParseException pe) {
			isValid = false;
		} catch (NullPointerException ne) {
			isValid = false;
		}

		return isValid;
	}

	public static boolean compareTimestamps(Timestamp minTimestamp, Timestamp maxTimestamp) {
		return maxTimestamp.after(minTimestamp);
	}

	public static boolean validateYearFormat(String date) {
		return date != null ? date.matches(ApplicationConstants.YEAR_PATTERN) : false;
	}

	public static boolean validateMonthFormat(String date) {
		return date != null ? date.matches(ApplicationConstants.MONTH_PATTERN) : false;
	}

	public static boolean validateDayFormat(String date) {
		return date != null ? date.matches(ApplicationConstants.DAY_PATTERN) : false;
	}

	public static boolean validateMinuteFormat(String date) {
		return date != null
				? (date.matches(ApplicationConstants.MINUTE_PATTERN_PLUS_OFFSET)
						|| date.matches(ApplicationConstants.MINUTE_PATTERN_MINUS_OFFSET))
				: false;
	}
	
	public static boolean validateMinuteFormatWithoutPadding(String date) {
		boolean formatMatched = false;
		boolean hoursMinuteFormatMatched = false;
		if (date != null) {
			formatMatched = date.matches(ApplicationConstants.MINUTE_PATTERN_PLUS_OFFSET)
					|| date.matches(ApplicationConstants.MINUTE_PATTERN_MINUS_OFFSET);
			hoursMinuteFormatMatched = formatMatched ? !date.substring(8, 12).equalsIgnoreCase("0000"):formatMatched;
		}
		return formatMatched && hoursMinuteFormatMatched;
	}
	
	public static boolean validateSecondFormat(String date) {
		return date != null
				? (date.matches(ApplicationConstants.SECOND_PATTERN_MINUS_OFFSET)
						|| date.matches(ApplicationConstants.SECOND_PATTERN_PLUS_OFFSET))
				: false;
	}
	
	public static boolean validateSecondFormatWithoutPadding(String date) {
		boolean formatMatched = false;
		boolean hoursMinuteFormatMatched = false;
		if (date != null) {
			formatMatched = date.matches(ApplicationConstants.SECOND_PATTERN_MINUS_OFFSET)
					|| date.matches(ApplicationConstants.SECOND_PATTERN_PLUS_OFFSET);
			hoursMinuteFormatMatched = formatMatched ? !date.substring(8, 14).equalsIgnoreCase("000000"):formatMatched;
		}
		return formatMatched && hoursMinuteFormatMatched;
	}
	
	public static boolean validateYearFormat(CCDADataElement date) {
		return date != null
				? (date.getValue() != null ? date.getValue().matches(ApplicationConstants.YEAR_PATTERN) : false)
				: false;
	}

	public static boolean validateMonthFormat(CCDADataElement date) {
		return date != null
				? (date.getValue() != null ? date.getValue().matches(ApplicationConstants.MONTH_PATTERN) : false)
				: false;
	}

	public static boolean validateDayFormat(CCDADataElement date) {
		return date != null
				? (date.getValue() != null ? date.getValue().matches(ApplicationConstants.DAY_PATTERN) : false)
				: false;
	}

	public static boolean validateMinuteFormat(CCDADataElement date) {
		return date != null
				? (date.getValue() != null
						? date.getValue().matches(ApplicationConstants.MINUTE_PATTERN_PLUS_OFFSET)
								|| date.getValue().matches(ApplicationConstants.MINUTE_PATTERN_MINUS_OFFSET)
						: false)
				: false;
	}
	
	public static boolean validateMinuteFormatWithoutPadding(CCDADataElement date) {
		boolean formatMatched = false;
		boolean hoursMinuteFormatMatched = false;
		if (date != null && date.getValue()!=null) {
			formatMatched = date.getValue().matches(ApplicationConstants.MINUTE_PATTERN_PLUS_OFFSET)
					|| date.getValue().matches(ApplicationConstants.MINUTE_PATTERN_MINUS_OFFSET);
			hoursMinuteFormatMatched = formatMatched ? !date.getValue().substring(8, 12).equalsIgnoreCase("0000"):formatMatched;
		}
		return formatMatched && hoursMinuteFormatMatched;
	}

	public static boolean validateSecondFormat(CCDADataElement date) {
		return date != null
				? (date.getValue() != null
						? date.getValue().matches(ApplicationConstants.SECOND_PATTERN_MINUS_OFFSET)
								|| date.getValue().matches(ApplicationConstants.SECOND_PATTERN_PLUS_OFFSET)
						: false)
				: false;
	}
	
	public static boolean validateSecondFormatWithoutPadding(CCDADataElement date) {
		boolean formatMatched = false;
		boolean hoursMinuteFormatMatched = false;
		if (date != null && date.getValue()!=null) {
			formatMatched = date.getValue().matches(ApplicationConstants.SECOND_PATTERN_PLUS_OFFSET)
					|| date.getValue().matches(ApplicationConstants.SECOND_PATTERN_MINUS_OFFSET);
			hoursMinuteFormatMatched = formatMatched ? !date.getValue().substring(8, 14).equalsIgnoreCase("000000"):formatMatched;
		}
		return formatMatched && hoursMinuteFormatMatched;
	}

	public static boolean validateYearFormat(CCDAEffTime effectiveTime) {
		boolean isValid = false;

		if (effectiveTime.getValuePresent()) {
			isValid = validateYearFormat(effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = validateYearFormat(effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) {
			isValid = validateYearFormat(effectiveTime.getLow()) || validateYearFormat(effectiveTime.getHigh());
		}

		return isValid;
	}

	public static boolean validateMonthFormat(CCDAEffTime effectiveTime) {
		boolean isValid = false;

		if (effectiveTime.getValuePresent()) {
			isValid = validateMonthFormat(effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = validateMonthFormat(effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) {
			isValid = validateMonthFormat(effectiveTime.getLow()) || validateMonthFormat(effectiveTime.getHigh());
		}

		return isValid;
	}

	public static boolean validateDayFormat(CCDAEffTime effectiveTime) {
		boolean isValid = false;

		if (effectiveTime.getValuePresent()) {
			isValid = validateDayFormat(effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = validateDayFormat(effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) {
			isValid = validateDayFormat(effectiveTime.getLow()) || validateDayFormat(effectiveTime.getHigh());
		}

		return isValid;
	}

	public static boolean validateMinuteFormat(CCDAEffTime effectiveTime) {
		boolean isValid = false;

		if (effectiveTime.getValuePresent()) {
			isValid = validateMinuteFormat(effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = validateMinuteFormat(effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) {
			isValid = validateMinuteFormat(effectiveTime.getLow()) || validateMinuteFormat(effectiveTime.getHigh());
		}

		return isValid;
	}
	
	public static boolean validateMinuteFormatWithoutPadding(CCDAEffTime effectiveTime) {
		boolean isValid = false;

		if (effectiveTime.getValuePresent()) {
			isValid = validateMinuteFormatWithoutPadding(effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = validateMinuteFormatWithoutPadding(effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) {
			isValid = validateMinuteFormatWithoutPadding(effectiveTime.getLow()) || validateMinuteFormatWithoutPadding(effectiveTime.getHigh());
		}

		return isValid;
	}

	public static boolean validateSecondFormat(CCDAEffTime effectiveTime) {
		boolean isValid = false;

		if (effectiveTime.getValuePresent()) {
			isValid = validateSecondFormat(effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = validateSecondFormat(effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) {
			isValid = validateSecondFormat(effectiveTime.getLow()) || validateSecondFormat(effectiveTime.getHigh());
		}

		return isValid;
	}
	
	public static boolean validateSecondFormatWithoutPadding(CCDAEffTime effectiveTime) {
		boolean isValid = false;

		if (effectiveTime.getValuePresent()) {
			isValid = validateSecondFormatWithoutPadding(effectiveTime.getValue());
		}

		if (effectiveTime.isSingleAdministrationValuePresent()) {
			isValid = validateSecondFormatWithoutPadding(effectiveTime.getSingleAdministration());
		}

		if (effectiveTime.getLowPresent() || effectiveTime.getHighPresent()) {
			isValid = validateSecondFormatWithoutPadding(effectiveTime.getLow()) || validateSecondFormatWithoutPadding(effectiveTime.getHigh());
		}

		return isValid;
	}

	public static boolean isCodeSystemAvailable(String codeSystem) {
		boolean result = false;
		if (codeSystem != null) {
			result = ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem) == null ? false : true;
		}
		return result;
	}

	/*
	 * public static boolean validateDisplayName(String code, String codeSystem,
	 * String displayName ) { boolean result = false;
	 * if(!ApplicationUtil.isEmpty(code) && !ApplicationUtil.isEmpty(codeSystem) &&
	 * !ApplicationUtil.isEmpty(displayName)) { UriComponentsBuilder builder =
	 * UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.
	 * CODE_DISPLAYNAME_VALIDATION_URL) .queryParam("code", code)
	 * .queryParam("codeSystems",
	 * ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem)==null ? "\"\"" :
	 * ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem))
	 * .queryParam("displayName", displayName.toUpperCase()); RestTemplate
	 * restTemplate = new RestTemplate(getClientHttpRequestFactory()); result =
	 * restTemplate.getForObject(builder.build().encode().toUri(), Boolean.class); }
	 * return result; }
	 * 
	 * public static boolean validateCodeForValueset(String code, String valuesetId)
	 * { boolean result = false; if(!ApplicationUtil.isEmpty(code) &&
	 * !ApplicationUtil.isEmpty(valuesetId)) { UriComponentsBuilder builder =
	 * UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.
	 * CODE_VALUSET_VALIDATION_URL) .queryParam("code", code)
	 * .queryParam("valuesetOids", valuesetId);
	 * 
	 * RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	 * result = restTemplate.getForObject(builder.build().encode().toUri(),
	 * Boolean.class); }
	 * 
	 * return result;
	 * 
	 * }
	 * 
	 * public static boolean validateCodeForCodeSystem(String code, String
	 * codeSystem) { boolean result = false; if(!ApplicationUtil.isEmpty(code) &&
	 * !ApplicationUtil.isEmpty(codeSystem)) { UriComponentsBuilder builder =
	 * UriComponentsBuilder.fromHttpUrl(ApplicationConfiguration.
	 * CODE_CODESYSTEM_VALIDATION_URL) .queryParam("code",code)
	 * .queryParam("codeSystems",
	 * ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem)==null ? "\"\"" :
	 * ApplicationConstants.CODE_SYSTEM_MAP.get(codeSystem));
	 * 
	 * RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
	 * result = restTemplate.getForObject(builder.build().encode().toUri(),
	 * Boolean.class); } return result;
	 * 
	 * }
	 * 
	 * private static ClientHttpRequestFactory getClientHttpRequestFactory() { int
	 * timeout = 5000; HttpComponentsClientHttpRequestFactory
	 * clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
	 * clientHttpRequestFactory.setConnectTimeout(timeout); return
	 * clientHttpRequestFactory; }
	 */

	/*
	 * public static boolean validateProblemStatusCode(String problemActStatuscode,
	 * List<CCDAProblemObs> probObservations) {
	 * 
	 * boolean isValid = false; boolean active = false; boolean suspended = false;
	 * for(CCDAProblemObs problemObs : probObservations) {
	 * if(problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants
	 * .PROBLEM_STATUS.ACTIVE.getstatus())) { active = true; }else
	 * if(problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants
	 * .PROBLEM_STATUS.RESOLVED.getstatus()) ||
	 * problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.
	 * PROBLEM_STATUS.INACTIVE.getstatus())||
	 * problemObs.getStatusCode().getCode().equalsIgnoreCase(ApplicationConstants.
	 * PROBLEM_STATUS.COMPLETED.getstatus())) { suspended = true; } }
	 * 
	 * if(active == true && suspended == true) { isValid =
	 * problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
	 * }else if(active == false && suspended == true) { isValid =
	 * problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.COMPLETED.getstatus()
	 * ); }else if (active == true && suspended == false) { isValid =
	 * problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
	 * }
	 * 
	 * 
	 * return isValid;
	 * 
	 * }
	 */

	public static boolean validateProblemStatusCode(String problemActStatuscode,
			List<CCDAProblemObs> probObservations) {

		boolean isValid = false;
		boolean active = false;
		boolean completed = false;

		for (CCDAProblemObs problemObs : probObservations) {
			if (problemObs.getEffTime() != null) {
				if (problemObs.getEffTime().getLowPresent() && !problemObs.getEffTime().getHighPresent()) {
					active = true;
				} else if (problemObs.getEffTime().getLowPresent() && problemObs.getEffTime().getHighPresent()) {
					completed = true;
				}
			} else {
				return false;
			}
		}

		if (active == true && completed == true) {
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
		} else if (active == false && completed == true) {
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.COMPLETED.getstatus())
					|| problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.SUSPENDED.getstatus())
					|| problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.COMPLETED.getstatus());
		} else if (active == true && completed == false) {
			isValid = problemActStatuscode.equalsIgnoreCase(CONCERNACT_STATUS.ACTIVE.getstatus());
		}
		return isValid;
	}

	public static boolean validateStatusCode(CCDAEffTime effectiveTime, String concernStatusCode) {

		boolean isValid = true;

		if (effectiveTime != null) {
			if (concernStatusCode.equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.COMPLETED.getstatus())
					|| concernStatusCode
							.equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.SUSPENDED.getstatus())) {
				isValid = effectiveTime.getHighPresent() && effectiveTime.getLowPresent();
			} else if (concernStatusCode.equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.ACTIVE.getstatus())) {
				isValid = !effectiveTime.getHighPresent() && effectiveTime.getLowPresent();
			} else
				isValid = false;
		} else {
			isValid = false;
		}

		return isValid;

	}
	
	public static boolean isValidStatusCode(CCDACode concernStatusCode) {
		
		return concernStatusCode!=null && !concernStatusCode.getCode().equalsIgnoreCase(ApplicationConstants.CONCERNACT_STATUS.ABORTED.getstatus());
		
	}

	public static int calculateActualPoints(int maxPoints, int actualPoints) {
		float percentage = (actualPoints * 100) / maxPoints;

		if (percentage < 25) {
			return 0;
		} else if (percentage >= 25 && percentage < 50) {
			return 1;
		} else if (percentage >= 50 && percentage < 75) {
			return 2;
		} else if (percentage >= 75 && percentage < 100) {
			return 3;
		} else if (percentage == 100) {
			return 4;
		} else
			return 0;
	}

	public static void calculateSectionGradeAndIssues(List<CCDAScoreCardRubrics> rubricsList, Category category) {
		float actualPoints = 0;
		float maxPoints = 0;
		int percentage;
		int categoryIssues = 0;
		String categoryGrade;

		for (CCDAScoreCardRubrics rubrics : rubricsList) {
			actualPoints = actualPoints + rubrics.getRubricScore();
			maxPoints++;
			if (rubrics.getNumberOfIssues() != 0) {
				categoryIssues = categoryIssues + rubrics.getNumberOfIssues();
			}
		}

		percentage = Math.round((actualPoints * 100) / maxPoints);
		categoryGrade = calculateGradeFromScore(percentage);
		category.setCategoryGrade(categoryGrade);
		category.setCategoryNumericalScore(percentage);
		category.setNumberOfIssues(categoryIssues);
	}

	public static void calculateNumberOfChecksAndFailedRubrics(List<CCDAScoreCardRubrics> rubricsList,
			Category category) {
		int numberOfChecks = 0;
		int numberOfFailedRubrics = 0;

		for (CCDAScoreCardRubrics rubrics : rubricsList) {
			numberOfChecks = numberOfChecks + rubrics.getNumberOfChecks();
			if (rubrics.getNumberOfIssues() > 0) {
				numberOfFailedRubrics++;
			}
		}

		category.setNumberOfChecks(numberOfChecks);
		category.setNumberOfFailedRubrics(numberOfFailedRubrics);
	}

	public static void calculateFinalGradeAndIssues(List<Category> categoryList, Results results) {
		float finalMaxPoints = 0;
		float finalActualPoints = 0;
		String finalGrade = "";
		int numberOfIssues = 0;
		int numberOfChecks = 0;
		int numberOfRules = 0;
		int numberOfFailedRubrics = 0;

		for (Category category : categoryList) {
			if (!(category.isFailingConformance() || category.isNullFlavorNI())) {
				for (CCDAScoreCardRubrics rubrics : category.getCategoryRubrics()) {
					finalMaxPoints++;
					finalActualPoints = finalActualPoints + rubrics.getRubricScore();
				}
				numberOfIssues = numberOfIssues + category.getNumberOfIssues();
				numberOfRules = numberOfRules + category.getCategoryRubrics().size();
				numberOfChecks = numberOfChecks + category.getNumberOfChecks();
				numberOfFailedRubrics = numberOfFailedRubrics + category.getNumberOfFailedRubrics();
			}
		}

		int percentage = Math.round((finalActualPoints * 100) / finalMaxPoints);
		finalGrade = calculateGradeFromScore(percentage, true);
		results.setFinalGrade(finalGrade);
		results.setFinalNumericalGrade(percentage);
		results.setNumberOfIssues(numberOfIssues);
		results.setNumberOfRules(numberOfRules);
		results.setNumberOfFailedRules(numberOfFailedRubrics);
		results.setTotalElementsChecked(numberOfChecks);
	}

	public static String calculateIndustryAverageGrade(int industryAverageScore) {
		return calculateGradeFromScore(industryAverageScore);
	}

	public static String calculateGradeFromScore(int score) {
		return calculateGradeFromScore(score, false);
	}

	public static String calculateGradeFromScore(int score, boolean emptyStringReturnedIfUnknown) {
		if (score < 70) {
			return "D";
		} else if (score >= 70 && score < 80) {
			return "C";
		} else if (score >= 80 && score < 85) {
			return "B-";
		} else if (score >= 85 && score < 90) {
			return "B+";
		} else if (score >= 90 && score < 95) {
			return "A-";
		} else if (score >= 95 && score <= 100) {
			return "A+";
		} else {
			if (emptyStringReturnedIfUnknown) {
				return "";
			}
			return ApplicationConstants.UNKNOWN_GRADE;
		}
	}

	public static float calculateRubricScore(int maxPoints, int actualPoints) {
		if (maxPoints != 0) {
			return (float) actualPoints / (float) maxPoints;
		} else
			return 0;
	}

	public static String checkCcdaVersion(CCDARefModel ccdaModels) {
		String ccdaVersion = "";

		if (ccdaModels != null) {
			if (isExtensionPresent(ccdaModels.getDocTemplateId())) {
				ccdaVersion = ApplicationConstants.CCDAVersion.R21.getVersion();
			} else {
				ccdaVersion = ApplicationConstants.CCDAVersion.R11.getVersion();
			}
		}

		return ccdaVersion;
	}

	public static boolean isRuleEnabled(List<SectionRule> sectionRules, String ruleName) {
		boolean isRuleEnabled = false;
		for (SectionRule sectionRule : sectionRules) {
			if (sectionRule.getRuleName().equalsIgnoreCase(ruleName) && sectionRule.isRuleEnabled()) {
				isRuleEnabled = true;
				break;
			}
		}
		return isRuleEnabled;
	}

	public static boolean isRuleEnabled(List<SectionRule> sectionRules, ApplicationConstants.RULE_IDS ruleId) {
		boolean isRuleEnabled = false;
		for (SectionRule sectionRule : sectionRules) {
			if (sectionRule.getRuleId().equalsIgnoreCase(ruleId.toString()) && sectionRule.isRuleEnabled()) {
				isRuleEnabled = true;
				break;
			}
		}
		return isRuleEnabled;
	}

	public static List<SectionRule> getSectionRules(List<ScorecardSection> sectionList, String sectionName) {
		List<SectionRule> sectionRules = null;
		for (ScorecardSection section : sectionList) {
			if (section.getSectionName().equalsIgnoreCase(sectionName)) {
				sectionRules = section.getSectionRules();
				break;
			}
		}
		return sectionRules;
	}

	public static boolean validTemplateIdFormat(String templateId) {
		return templateId.startsWith(ApplicationConstants.TEMPLATEID_FORMAT);
	}

	public static void debugLog(String debugMessage) {
		if (ApplicationConfiguration.ENV.isProduction())
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
