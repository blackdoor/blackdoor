package black.door.json;

import black.door.json.tokens.SyntaxToken;
import black.door.json.tokens.Token;
import black.door.json.tokens.ValueToken;
import black.door.json.tokens.StringToken;

import java.util.*;

/**
 * @author Nathan Fischer &lt;nfischer921@gmail.com&gt;
 * @since 2015-5-28
 *
 * A class to represent a JSON object. Aside from constructor to parse JSON strings this class is just a List with
 * typed retrieval methods for convenience.
 *
 */
public class JsonObject extends LinkedHashMap<String, Object> implements JsonSerializable{

    public JsonObject(){}

    /**
     * Create a new JsonObject from jsonString
     * @param jsonString a JSON string
     * @throws JsonException
     */
    public JsonObject(String jsonString){
        this(Derulo.toTokens(jsonString));
    }

    protected void fromIterator(Iterator<Token> i){
        //i starts inside the open curl
        for(Token token = i.next(); i.hasNext(); token = i.next()){
            if(token == SyntaxToken.CLOSE_CURL)
                return;
            if(!(token instanceof StringToken))
                throw new JsonException("Expected field key string, found " + token.getContent());
            Token colon = i.next();
            if(colon != SyntaxToken.COLON)
                throw new RuntimeException("Expected ':', found " + token.getContent());
            Token value = i.next();
            if(value instanceof ValueToken || value instanceof StringToken)
                this.put(token.getContent(), Derulo.fromJSONToken(value));
            if(value == SyntaxToken.OPEN_CURL) {
                JsonObject obj = new JsonObject();
                obj.fromIterator(i);
                this.put(token.getContent(), obj);
            }
            if(value == SyntaxToken.OPEN_SQUARE) {
                JsonArray arr = new JsonArray();
                arr.fromIterator(i);
                this.put(token.getContent(), arr);
            }
            Token commaOrCurl = i.next();
            if(commaOrCurl == SyntaxToken.CLOSE_CURL)
                break;
            if(commaOrCurl != SyntaxToken.COMMA)
                throw new JsonException("Missing comma or closing curly brace");
        }
    }

    protected JsonObject(List<Token> jsonTokens){
        if(jsonTokens.get(0) != SyntaxToken.OPEN_CURL)
            throw new JsonException("Not a JSON object");
        Iterator<Token> i = jsonTokens.iterator();
        i.next();
        fromIterator(i);
    }

    public boolean isFieldNull(String key){
        Object val = this.get(key);
        if(val == null && this.containsKey(key))
            return true;
        if(JsonNull.NULL.equals(val))
            return true;
        return false;
    }

    public Long getInteger(String key){
        if(getFraction(key) != null)
            return null;
        try {
            return (long) this.get(key);
        }catch (ClassCastException e){
            return null;
        }
    }

    public String getString(String key){
        return getNCast(key, String.class);
    }

    public Map getObject(String index){
        return getNCast(index, Map.class);
    }

    public JsonObject getJsonObject(String key){
        return getNCast(key, JsonObject.class);
    }

    public Boolean getBoolean(String key){
        return getNCast(key, Boolean.class);
    }

    public Double getFraction(String key){
        double candidate;
        try {
            candidate = (double) this.get(key);
        }catch (ClassCastException e){
            return null;
        }

        return candidate - Math.round(candidate) == 0
                ? null
                : candidate;
    }

    public Collection getCollection(String index){
        return getNCast(index, Collection.class);
    }
    public JsonArray getArray(String key){
        return getNCast(key, JsonArray.class);
    }

    private <T extends Object> T getNCast(String key, Class<T> type){
        try {
            return type.cast(this.get(key));
        }catch (ClassCastException e){
            return null;
        }
    }

    public String toString(){
        return toJSONString();
    }

    @Override
    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (Map.Entry e : this.entrySet()) {
            sb.append("\"");
            sb.append(e.getKey().toString());
            sb.append("\":");
            sb.append(Derulo.toJSON(e.getValue()));
            sb.append(",");
        }
        if(sb.lastIndexOf("{") != sb.length() -1)
            sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}
