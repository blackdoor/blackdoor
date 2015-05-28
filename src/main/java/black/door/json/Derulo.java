package black.door.json;

import black.door.json.tokens.StringToken;
import black.door.json.tokens.SyntaxToken;
import black.door.json.tokens.Token;
import black.door.json.tokens.ValueToken;
import black.door.util.Misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.StringCharacterIterator;
import java.util.*;

/**
 * @author Nathan Fischer <nfischer921@gmail.com>nfischer921@gmail.com</nfischer921@gmail.com>
 * @since 2015-5-28
 *
 * Derulo is the main class to build and parse JSON strings. It can be used with JsonArray and JsonObject, but any
 * Collection or Map (respectively) can work in their place.
 *
 * A simple use of Derulo would be as follows:
 *  Map<String, Object> simpleMap = new HashMap<>();
 *  simpleMap.put("string", "sample");
 *  simpleMap.put("int", 5);
 *  String jsonString = Derulo.toJSON(simpleMap);
 *
 *  JsonObject jsonObject = new JsonObject(jsonString);
 *  int i = jsonObject.getInteger("int").intValue();
 *
 */
public class Derulo {

    protected static Object fromJSONToken(Token token){
        if(token instanceof StringToken)
            return token.getContent();
        if(token instanceof ValueToken){
            String content = token.getContent();
            switch (content){
                case "null":
                    return JsonNull.NULL;
                case "true":
                    return Boolean.TRUE;
                case "false":
                    return Boolean.FALSE;
                default:
                    Double number = Double.valueOf(content);
                    if(Math.round(number) == number)
                        return number.longValue();
                    return number;
            }
        }
        throw new JsonException("Expected json value, found " + token.getContent() + "instead.");
    }

    protected static Object fromJSON(List<Token> jsonTokens){
        Token token0 = jsonTokens.get(0);
        if(token0 == SyntaxToken.OPEN_CURL)
            return new JsonObject(jsonTokens);
        if(token0 == SyntaxToken.OPEN_SQUARE)
            return new JsonArray(jsonTokens);
        if(jsonTokens.size() == 1){
            return fromJSONToken(token0);
        }
        throw new JsonException("Invalid JSON");
    }

    protected static Object fromIterator(Iterator<Token> i){
        Token value = i.next();
        if(value instanceof ValueToken || value instanceof StringToken)
            return Derulo.fromJSONToken(value);
        if(value == SyntaxToken.OPEN_CURL) {
            JsonObject obj = new JsonObject();
            obj.fromIterator(i);
            return obj;
        }
        if(value == SyntaxToken.OPEN_SQUARE) {
            JsonArray arr = new JsonArray();
            arr.fromIterator(i);
            return arr;
        }
        throw new JsonException();
    }

    /**
     * Get a java object from a JSON value
     * @param jsonString a JSON value in string form
     * @return a Number, Boolean, JsonObject, JsonArray or JsonNull
     * @throws JsonException
     */
    public static Object fromJSON(String jsonString){
        List<Token> tokens = Derulo.toTokens(jsonString);
        return fromJSON(tokens);
    }

    protected static List<Token> toTokens(String jsonString){
        List<Token> ret = new ArrayList<>();
        char c = jsonString.charAt(0);
        for(StringCharacterIterator i = new StringCharacterIterator(jsonString); c != StringCharacterIterator.DONE; c = i.next()){

            if(c == Misc.NULL || Character.isWhitespace(c))
                continue;

            switch (c) {
                case '{':
                    ret.add(SyntaxToken.OPEN_CURL);
                    break;
                case '}':
                    ret.add(SyntaxToken.CLOSE_CURL);
                    break;
                case '[':
                    ret.add(SyntaxToken.OPEN_SQUARE);
                    break;
                case ']':
                    ret.add(SyntaxToken.CLOSE_SQUARE);
                    break;
                case ':':
                    ret.add(SyntaxToken.COLON);
                    break;
                case ',':
                    ret.add(SyntaxToken.COMMA);
                    break;
                case ' ':
                case '\n':
                case '\t':
                    break;
                case '"':
                    StringToken st = new StringToken();
                    StringBuilder builder = new StringBuilder();
                    for(c = i.next();c != '"'; c = i.next()){
                        if(c == StringCharacterIterator.DONE)
                            throw new RuntimeException();
                        if(c == '\\')
                            i.next();
                        builder.append(c);
                    }
                    st.setContent(builder.toString());
                    ret.add(st);
                    break;
                default:
                    //token is a value
                    StringBuilder sb = new StringBuilder();
                    for(; !Character.isWhitespace(c) && c != ',' && c != '}' && c != ']'; c = i.next()) {
                        if (c == StringCharacterIterator.DONE)
                            throw new RuntimeException();
                        sb.append(c);
                    }
                    i.previous();
                    ValueToken t = new ValueToken();
                    t.setContent(sb.toString());
                    ret.add(t);
            }

        }
        return ret;
    }

    /**
     * Invokes object.toJSONString()
     * @param object a JsonSerializable object
     * @return the JSON string representation of object
     */
    public static String toJSON(JsonSerializable object) {
        return object.toJSONString();
    }

    /**
     * A convenience method for toJSON(int, Object) for creating JSON strings with no whitespace. Equivalent to calling
     * toJSON(0, object)
     * @param object
     * @return the JSON string representation of object
     */
    public static String toJSON(Object object) {
        return toJSON(0, object);
    }

    /**
     * Serializes object as a JSON string. Map objects are serialized as JSON objects, List objects are serialized as
     * JSON arrays. Numbers, Booleans, and null are all serialized to their respective JSON types. Other classes will
     * be serialised using object.toJSONString if they have it, otherwise they will be serialized using
     * String.valueOf(object).
     * @param indent the number of spaces to indent the output between elements
     * @param object the object to serialize
     * @return the JSON string representation of object
     */
    public static String toJSON(int indent, Object object) {
        return toJSON(indent, 1, object, true).toString();
    }

    private static CharSequence toJSON(int indent, int indentLevel, Object object, boolean field) {
        try {
            Method toJsonString = object.getClass().getMethod("toJSONString");
            toJsonString.setAccessible(true);
            return toJsonString.invoke(object).toString();
        } catch (NoSuchMethodException e) {
            //none
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        if (object instanceof Map) {
            if(!field)
                sb.append(getIndents(indent * indentLevel));
            sb.append("{");
            if(indent > 0)
                sb.append('\n');

            Map<Object, Object> map = (Map) object;
            for (Map.Entry e : map.entrySet()) {
                sb.append(getIndents(indent * indentLevel));
                sb.append("\"");
                sb.append(e.getKey().toString());
                sb.append("\":");
                if(indent > 0)
                    sb.append(' ');
                sb.append(toJSON(indent, indentLevel + 1, e.getValue(), true));
                sb.append(",");
                if(indent > 0)
                    sb.append('\n');
            }

            sb.setLength(sb.length() - 1);
            if(indent > 0) {
                sb.setLength(sb.length() - 1);
                sb.append('\n');
            }
            sb.append(getIndents(indent * (indentLevel -1)) + "}");

        } else if (object instanceof Collection) {
            Collection collection = (Collection) object;
            if(!field)
                sb.append(getIndents(indent * indentLevel));
            sb.append("[");
            if(indent > 0)
                sb.append('\n');
            for (Object item : collection) {
                sb.append(toJSON(indent, indentLevel, item, false));
                sb.append(",");
                if(indent > 0)
                    sb.append('\n');
            }
            sb.setLength(sb.length() - 1);
            if(indent > 0) {
                sb.setLength(sb.length() - 1);
                sb.append('\n');
            }
            sb.append(getIndents(indent * (indentLevel -1)) + "]");

        } else
        if(object instanceof CharSequence) {
            if(!field)
                sb.append(getIndents(indent * indentLevel));
            sb.append("\"");
            sb.append(object.toString());
            sb.append("\"");
        }else{
            if(!field)
                sb.append(getIndents(indent * indentLevel));
            sb.append(String.valueOf(object));
        }

        return sb;
    }

    private static CharSequence getIndents(int i) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < i; x++) {
            sb.append(" ");
        }
        return sb;
    }

}
