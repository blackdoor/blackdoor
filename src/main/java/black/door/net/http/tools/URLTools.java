package black.door.net.http.tools;

import black.door.util.DBP;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nfischer on 6/28/2015.
 */
public abstract class URLTools {

	private static Pattern theRegex = Pattern.compile("((?<field>[\\w\\-\\.~%!$'\\(\\)*+,/?:@]+)=?(?<value>[\\w\\-\\.~%!$'\\(\\)*+,/?:@]*))");

	public static Map<String, String> parseQueries(URL url){
		try {
			return parseQueries(url.getQuery());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static Map<String, String> parseQueries(String query) throws MalformedURLException {
		Map<String, String> queries = new HashMap<>();

		Pattern pattern = getRegex();
		Matcher matcher = pattern.matcher(query);

		try {
			while (matcher.find()) {
				String field = matcher.group("field");
				String value = matcher.group("value");
				queries.put(field, value);
			}
		}catch (IllegalArgumentException e){
			DBP.printException(e);
			throw new MalformedURLException();
		}
		return queries;
	}

	public static Map<String, String> parseRef(URL url){
		try {
			return parseRef(url.getRef());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public static Map<String, String> parseRef(String ref) throws MalformedURLException {
		Pattern pattern = getRegex();
		Matcher matcher = pattern.matcher(ref);
		DBP.printdebugln(matcher);
		Map<String, String> queries = new HashMap<>();
		try{
			while (matcher.find()) {
				DBP.printdebugln(matcher.group(3));
				String field = matcher.group("field");
				if(matcher.group(3).isEmpty()){
					queries.put("id", field);
				}else {
					String value = matcher.group("value");
					queries.put(field, value);
				}

			}
		}catch (IllegalArgumentException e){
			DBP.printException(e);
			throw new MalformedURLException();
		}
		return queries;
	}

	private static Pattern getRegex(){
		return theRegex;
	}

}
