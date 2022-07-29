package hr.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkerUtil {
	public static String markManyToOne(String data, int beginLength, int endLength) {
		String markingResult = data;
		int length = data.length();
		int end = length - endLength;
		if (beginLength > 0 && end < length) {
			markingResult = data.substring(0, beginLength) + "-" + data.substring(end, length);
		}
		return markingResult;
	}
	
	public static String markManyToMany(String data, int beginLength, int endLength, String replacingChar) {
		StringBuilder sbMarkingResult = new StringBuilder();
		int length = data.length();
		int end = length - endLength;
		if (beginLength >= 0 && end <= length) {
			sbMarkingResult.append(data.substring(0, beginLength));
			for(int i = 0; i < end - beginLength; i++) {
				sbMarkingResult.append(replacingChar);
			}
			sbMarkingResult.append(data.substring(end, length));
		} else {
			sbMarkingResult.append(data);
		}
		return sbMarkingResult.toString();
	}
	
	public static String markHRCardInJson(String data) {
		StringBuilder sbMarkingResult = new StringBuilder();
		String regex = "(\"9[0-9]{15,19}\")|(\"8[0-9]{15,19}\")|(\"4[0-9]{15,19}\")";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(data);
		int startIndex = 0;
		while(matcher.find()) {
			String matcherText = matcher.group();
			int beginIndexMatch = matcher.start();
			int endIndexMatch = matcher.end();
			String clearSubUnMark = data.substring(startIndex, beginIndexMatch);
			sbMarkingResult.append(clearSubUnMark);
			startIndex = endIndexMatch;
			String markMatchText = markManyToMany(matcherText, 7, 5, "*");//6-4
			sbMarkingResult.append(markMatchText);
		}
		if (startIndex == 0) {
			sbMarkingResult.append(data);
		} else {
			sbMarkingResult.append(data.substring(startIndex, data.length()));
		}
		return sbMarkingResult.toString();
	}
	
	public static String markDigitOTPInJson(String data) {
		StringBuilder sbMarkingResult = new StringBuilder();
		String regex = "\"[0-9]{6}\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(data);
		int startIndex = 0;
		while(matcher.find()) {
			String matcherText = matcher.group();
			int beginIndexMatch = matcher.start();
			int endIndexMatch = matcher.end();
			String clearSubUnMark = data.substring(startIndex, beginIndexMatch);
			sbMarkingResult.append(clearSubUnMark);
			startIndex = endIndexMatch;
			String markMatchText = markManyToMany(matcherText, 1, 1, "^");//6
			sbMarkingResult.append(markMatchText);
		}
		if (startIndex == 0) {
			sbMarkingResult.append(data);
		} else {
			sbMarkingResult.append(data.substring(startIndex, data.length()));
		}
		return sbMarkingResult.toString();
	}
	
	public static String markForRequestBody(String data) {
		return markHRCardInJson(markDigitOTPInJson(data));
	}
	
	/*public static void main(String[] args) throws Exception {
		String l = "{\"aaa\":\"dddd\"}\"9704341234567887\"sfsfsfsd\"4221341234567890\"xtddtvldhpbn";
		String h = markHRCardInJson(l);
		String hl = markDigitOTPInJson("\"123456\"");
		System.out.println(h);
		System.out.println(hl);
	}*/
}
