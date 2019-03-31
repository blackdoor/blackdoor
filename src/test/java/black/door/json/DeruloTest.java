package black.door.json;

import black.door.util.DBP;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by nfischer on 5/23/15.
 */
public class DeruloTest {

    Map<String, Object> map;
    Map<String, Object> simpleMap;
    List<Object> simpleList;

    @Before
    public void setup(){
        map = new HashMap<>();

        simpleMap = new HashMap<>();
        simpleMap.put("string", "sample");
        simpleMap.put("int", 5l);
        simpleMap.put("fraction", 5.50d);
        simpleMap.put("sciNote", Double.valueOf("3.7e-4"));
        simpleMap.put("bool", true);
        simpleMap.put("null", JsonNull.NULL);
        simpleMap.put("emptyList", new ArrayList<>());

        simpleList = new ArrayList<>();
        simpleList.add("sample");
        simpleList.add(5l);
        simpleList.add(5.5d);
        simpleList.add(Double.valueOf("9.3e-200"));
        simpleList.add(true);
        simpleList.add(JsonNull.NULL);
        
        map.put("map", simpleMap);
        map.put("array", simpleList);
        map.put("emptyMap", new HashMap<>());
    }

    @Test
    public void testToJSON() throws Exception {
        System.out.println(Derulo.toJSON(2,map));
        System.out.println(Derulo.toJSON(map));
        System.out.println(Derulo.fromJSON("546"));
        System.out.println(Derulo.fromJSON("false"));

    }

    @Test
    public void fromJson(){
        String m2 = Derulo.toJSON(map);
        assertEquals(map, Derulo.fromJSON(m2));
    }

    @Test
    public void testJsonObject(){
        String m2 = Derulo.toJSON(simpleMap);
        Map parsed = new JsonObject(m2);
        //System.out.println(Derulo.toJSON(2, parsed));
        assertEquals(simpleMap, parsed);
    }

    @Test
    public void testJsonArray(){
        String arr = Derulo.toJSON(simpleList);
        List parsed = new JsonArray(arr);
        //System.out.println(Derulo.toJSON(4, parsed));
        assertEquals(simpleList, parsed);
    }

    @Test
    public void testToTokens(){
        DBP.toggleDebug();
        String json = Derulo.toJSON(map);
        System.out.println(Derulo.toTokens(json));
    }

    @Test
    public void other(){
        JsonObject obj = new JsonObject(Derulo.toJSON(map));
        System.out.println("Array " + obj.getArray("array"));
        System.out.println("scientific notation " +obj.getJsonObject("map").getFraction("sciNote"));
        System.out.println(obj.isFieldNull("map"));
        System.out.println(obj.getJsonObject("map").isFieldNull("null"));

        int i = obj.getJsonObject("map").getInteger("int").intValue();
    }
}
