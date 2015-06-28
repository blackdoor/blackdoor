package black.door.json;

import black.door.util.Misc;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by nfischer on 6/28/2015.
 */
public class JsonHasherTest {

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
	public void testCannon(){
		String cannon = JsonHasher.canonicalize(map).toString();

		JsonObject json = new JsonObject(cannon);

		List<String> mapKeyset = new ArrayList<>(map.keySet());
		Collections.sort(mapKeyset);
		assertTrue(new ArrayList<>(json.keySet()).equals(mapKeyset));

		System.out.println(cannon);
		System.out.println(Derulo.toJSON(2,Derulo.fromJSON(cannon)));
		System.out.println(Misc.bytesToHex(JsonHasher.hash(map)));
	}

	@Test
	public void testHash() throws Exception {
		byte[] hash1 = JsonHasher.hash(map);

		List<String> simpleKeys = new ArrayList<>(simpleMap.keySet());
		Collections.shuffle(simpleKeys);

		JsonObject json = new JsonObject();

		for(String key : simpleKeys){
			json.put(key, simpleMap.get(key));
		}
		map.put("map", json);

		assertArrayEquals(hash1, JsonHasher.hash(map));
	}

	@Test
	public void testInstance(){
		Map<String, Object> original = new HashMap<>(simpleMap);
		JsonHasher hasher = new JsonHasher(simpleMap);
		hasher.setIgnoredFields(Arrays.asList(new String[]{"int", "null"}));

		Map<String, Object> copy = new HashMap<>(simpleMap);
		copy.remove("int");
		copy.remove("null");

		assertArrayEquals(JsonHasher.hash(copy), hasher.hash());
		assertTrue(original.equals(simpleMap));
	}
}