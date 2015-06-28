package black.door.json;

import black.door.util.Misc;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Hashes canonicalized JSON objects according to several rules
 * 1. fields of the object and any objects in the object are sorted lexically
 * 2. all non-required white space is removed
 * 3. fractions less than 10^-3 or greater than or equal to 10^7 are put into scientific notation
 * 4. UTF-8 encoding is used to serialize the String representation of the JSON object
 *
 * Created by nfischer on 6/27/2015.
 */
public class JsonHasher {

	private static final int DEFAULT_DIGEST_SIZE = 16;

	public static byte[] hash(Map<String, Object> json){
		try {
			return Arrays.copyOf(hash(json, MessageDigest.getInstance("SHA-256")), DEFAULT_DIGEST_SIZE);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] hash(Map<String, Object> json, MessageDigest digest){
		digest.reset();
		byte[] hash = digest.digest(canonicalize(json).toString().getBytes(StandardCharsets.UTF_8));
		digest.reset();
		return hash;
	}

	protected static CharSequence canonicalize(Map obj){
		List<String> fieldNames = new LinkedList<>();
		Map<String, Object> originalKeys = new HashMap<>();
		for(Object e : obj.keySet()){
			String toString = e.toString();
			originalKeys.put(toString, e);
			fieldNames.add(toString);
		}
		Collections.sort(fieldNames);

		StringBuilder sb = new StringBuilder();
		sb.append('{');

		for(String field:fieldNames){
			sb.append('\"');
			sb.append(field);
			sb.append('\"');
			sb.append(':');
			sb.append(canonicalize(obj.get(originalKeys.get(field))));
			sb.append(',');
		}
		if(sb.lastIndexOf("{") != sb.length() -1)
			sb.setLength(sb.length() -1);
		sb.append('}');
		return sb;
	}

	protected static CharSequence canonicalize(Collection obj){
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(Object e : obj){
			sb.append(canonicalize(e));
			sb.append(',');
		}
		if(sb.lastIndexOf("[") != sb.length() -1)
			sb.setLength(sb.length() -1);
		sb.append(']');
		return sb;
	}

	protected static CharSequence canonicalize(Object obj){
		if(obj instanceof Map)
			return canonicalize((Map) obj);
		if(obj instanceof Collection)
			return canonicalize((Collection) obj);
		if(obj.getClass().isArray()){
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for(int i = 0; i < Array.getLength(obj); i++){
				sb.append(canonicalize(Array.get(obj, i)));
				sb.append(',');
			}
			sb.append(']');
			return sb;
		}
		return Derulo.toJSON(obj);
	}

	private List<String> ignoredFields;
	private Map<String, Object> json;
	private MessageDigest digest;

	public JsonHasher(String json){
		this(new JsonObject(json));
	}

	public JsonHasher(Map<String, Object> json){
		this.json = json;
		ignoredFields = new LinkedList<>();
	}

	public byte[] hash(){
		Map<String, Object> copy = new HashMap<>(json);
		for(String field : ignoredFields){
			copy.remove(field);
		}

		if(digest == null)
			return hash(copy);

		return hash(copy, digest);
	}

	public List<String> getIgnoredFields() {
		return ignoredFields;
	}

	public void setIgnoredFields(List<String> ignoredFields) {
		this.ignoredFields = ignoredFields;
	}

	public JsonHasher addIgnoredField(String fieldName){
		ignoredFields.add(fieldName);
		return this;
	}

	private MessageDigest getDigest() {
		return digest;
	}

	public void setDigest(MessageDigest digest) {
		this.digest = digest;
	}

	public Map<String, Object> getJson() {
		return json;
	}

	public void setJson(Map<String, Object> json) {
		this.json = json;
	}
}
