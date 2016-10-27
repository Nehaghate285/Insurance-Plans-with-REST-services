package com.ng.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtility {
	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
		Map<String, Object> retMap = new HashMap<String, Object>();

		if (json != null) {
			retMap = toMap(json);
		}
		return retMap;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keySet().iterator();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

//	public static boolean keyExists(JSONObject object, String searchedKey, Object value) {
//		boolean exists = object.containsKey(searchedKey);
//		if (!exists) {
//			Iterator<String> keys = object.keySet().iterator();
//			while (keys.hasNext()) {
//				String key =  keys.next();
//				if (object.get(key) instanceof Map) {
//					Map<String, Object> map= object.get(key);
//					exists = keyExists(o, searchedKey, value);
//					if(exists)
//					{
//						o.put(searchedKey, value);
//						break;
//					}
//				} 
//				
//			}
//
//		} 
//		else{
//			object.put(searchedKey, value);
//		}
//		
//		return exists;
//	}
	
	
	public static boolean map(JSONObject oldObject, String searchedKey, Object value){
		Map<String, Object> mappedone = jsonToMap(oldObject);
		return isKeyExisting(mappedone, searchedKey, value);
	}
	
	
	public static boolean isKeyExisting(Map mappedone , String searchedKey, Object value) {
		
		Iterator<String> keys = mappedone.keySet().iterator();
		while (keys.hasNext()) {
			String key =  keys.next();
			
			if (mappedone.get(key) instanceof Map) {	
				isKeyExisting((Map)mappedone.get(key), searchedKey, value);
			}
		}
		
		
		return true;
	}

	public static JSONObject patch(JSONObject oldobject, JSONObject tobeUpdated) {

		Iterator<?> keys = tobeUpdated.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object newValue = tobeUpdated.get(key);
			if(map(oldobject, key, newValue)){
				
			}
//			if (!keyExists(oldobject, key, newValue)) {
//				oldobject.putIfAbsent(key, newValue);
//			}
		}
		return oldobject;
	}
	
	
	//new patch
	public static JSONObject patchObj(JSONObject oldJsonObject1, JSONObject newJsonObject) {

		Iterator<?> keys = newJsonObject.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object newValue = newJsonObject.get(key);
			if (keyExists(oldJsonObject1, key, newValue)) {
				oldJsonObject1.put(key, newValue);
			}
			else{
				newJsonObject.putIfAbsent(key, newValue);
			}

		}
		return oldJsonObject1;
	}
	
	public static boolean keyExists(JSONObject object, String searchedKey, Object value) {
		boolean exists = object.containsKey(searchedKey);
		if (!exists) {
			Iterator<String> keys = object.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				if (object.get(key) instanceof Map) {
					JSONObject map = (JSONObject) object.get(key);
					exists = keyExists(map, searchedKey, value);

				}

			}

		}
		return exists;
	}

}
