package black.door.json;

import black.door.json.tokens.StringToken;
import black.door.json.tokens.SyntaxToken;
import black.door.json.tokens.Token;
import black.door.json.tokens.ValueToken;

import java.util.*;

/**
 * @author Nathan Fischer &lt;nfischer921@gmail.com&gt;
 * @since 2015-5-28
 *
 * A class to represent a JSON array. Aside from constructor to parse JSON strings this class is just a List with
 * typed retrieval methods for convenience.
 *
 */
public class JsonArray extends ArrayList<Object> implements JsonSerializable{

    public JsonArray(){}

    /**
     * Construct a JsonArray from given jsonString
     * @param jsonString
     * @throws JsonException
     */
    public JsonArray(String jsonString){
        this(Derulo.toTokens(jsonString));
    }

    protected JsonArray(List<Token> jsonTokens){
        Iterator<Token> i = jsonTokens.iterator();
        if(i.next() != SyntaxToken.OPEN_SQUARE)
            throw new JsonException("Not a JSON array");
        fromIterator(i);
    }

    protected void fromIterator(Iterator<Token> i){
        for(Token next = i.next(); i.hasNext(); next = i.next()){
            if(next == SyntaxToken.CLOSE_SQUARE)
                return;
            if(next instanceof ValueToken || next instanceof StringToken)
                this.add(Derulo.fromJSONToken(next));
            if(next == SyntaxToken.OPEN_CURL) {
                JsonObject obj = new JsonObject();
                obj.fromIterator(i);
                this.add(obj);
            }
            if(next == SyntaxToken.OPEN_SQUARE) {
                JsonArray arr = new JsonArray();
                arr.fromIterator(i);
                this.add(arr);
            }
            Token commaOrBracket = i.next();
            if(commaOrBracket == SyntaxToken.CLOSE_SQUARE)
                break;
            if(commaOrBracket != SyntaxToken.COMMA)
                throw new JsonException("Expected comma or closing square bracket. Found " + commaOrBracket.getContent() + " instead.");
        }
    }

    public boolean isFieldNull(int key){
        Object val = this.get(key);
        if(val == null)
            return false;
        if(JsonNull.NULL.equals(val))
            return true;
        return false;
    }

    public Long getInteger(int key){
        if(getFraction(key) != null)
            return null;
        try {
            return (long) this.get(key);
        }catch (ClassCastException e){
            return null;
        }
    }

    public String getString(int index){
        return getNCast(index, String.class);
    }

    public Map getObject(int index){
        return getNCast(index, Map.class);
    }

    public JsonObject getJsonObject(int index){
        return getNCast(index, JsonObject.class);
    }

    public Boolean getBoolean(int index){
        return getNCast(index, Boolean.class);
    }

    public Double getFraction(int index){
        double candidate;
        try {
            candidate = (double) this.get(index);
        }catch (ClassCastException e){
            return null;
        }

        return candidate - Math.round(candidate) == 0
                ? null
                : candidate;
    }

    public Collection getCollection(int index){
        return getNCast(index, Collection.class);
    }

    public JsonArray getArray(int index){
        return getNCast(index, JsonArray.class);
    }

    private <T extends Object> T getNCast(int key, Class<T> type){
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
        sb.append("[");
        for (Object item : this) {
            sb.append(Derulo.toJSON(item));
            sb.append(",");
        }
        if(sb.lastIndexOf("[") != sb.length() -1)
            sb.setLength(sb.length() -1);
        sb.append("]");
        return sb.toString();
    }
}
