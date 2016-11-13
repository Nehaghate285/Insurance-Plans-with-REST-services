package com.ng.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ng.pojo.InsurancePlan;

public class JSONUtility {
	public InsuranceDBConn insuranceDBConn;
	public long count;

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

	// public static boolean keyExists(JSONObject object, String searchedKey,
	// Object value) {
	// boolean exists = object.containsKey(searchedKey);
	// if (!exists) {
	// Iterator<String> keys = object.keySet().iterator();
	// while (keys.hasNext()) {
	// String key = keys.next();
	// if (object.get(key) instanceof Map) {
	// Map<String, Object> map= object.get(key);
	// exists = keyExists(o, searchedKey, value);
	// if(exists)
	// {
	// o.put(searchedKey, value);
	// break;
	// }
	// }
	//
	// }
	//
	// }
	// else{
	// object.put(searchedKey, value);
	// }
	//
	// return exists;
	// }

	public static boolean map(JSONObject oldObject, String searchedKey, Object value) {
		Map<String, Object> mappedone = jsonToMap(oldObject);
		return isKeyExisting(mappedone, searchedKey, value);
	}

	public static boolean isKeyExisting(Map mappedone, String searchedKey, Object value) {

		Iterator<String> keys = mappedone.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();

			if (mappedone.get(key) instanceof Map) {
				isKeyExisting((Map) mappedone.get(key), searchedKey, value);
			}
		}

		return true;
	}

	public static JSONObject patch(JSONObject oldobject, JSONObject tobeUpdated) {

		Iterator<?> keys = tobeUpdated.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object newValue = tobeUpdated.get(key);
			oldobject.put(key, newValue);
			
		}
		return oldobject;
		
		
		
	}

	// new patch
	public static JSONObject patchObj(JSONObject oldJsonObject1, JSONObject newJsonObject) {

		Iterator<?> keys = newJsonObject.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object newValue = newJsonObject.get(key);
			if (keyExists(oldJsonObject1, key, newValue)) {
				oldJsonObject1.put(key, newValue);
			} else {
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
	
	public static String generateEtag(String jsonObject) throws UnsupportedEncodingException, NoSuchAlgorithmException{
		 try {
		        MessageDigest md = MessageDigest.getInstance("MD5");
		        byte[] array = md.digest(jsonObject.getBytes());
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < array.length; ++i) {
		          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		       }
		        return sb.toString();
		    } catch (java.security.NoSuchAlgorithmException e) {
		    }
		    return null;
		
	}

	public String jsonToMapAndStoreInRedis(JSONObject jsonObject, InsuranceDBConn insurancedbConn, long count) {
		// TODO Auto-generated method stub
		this.insuranceDBConn = insurancedbConn;
		this.count = count;
		if (jsonObject != null) {
			return toMap1(jsonObject);
		}
		return null;
	}

	public String toMap1(JSONObject jsonObj) {
		Map<String, Object> simplePropertiesMap = new HashMap<String, Object>();
		/// id generation
		long localCount = count;
		Iterator<String> keysItr = jsonObj.keySet().iterator();
		Set relationship = new HashSet();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = jsonObj.get(key);

			if (value instanceof Map) {
				// id_relationship,Arraylist09alue--key
				// key+uuid value--redis
				if (!checkIfRef((Map) value)) {
					relationship.add(key + "_" + localCount);
				} else {
					relationship.add("ref_" + key + "_" + localCount);
				}
				relationship.add(key + "_" + localCount);
				iterateMap(key + "_" + localCount, (Map) value, localCount);

			} else if (value instanceof ArrayList) {
				List valuesList = new ArrayList();
				Set secondaryRelationship = new HashSet();
				long arrylistCount = 0;
				String mainArrKey = key + "_" + localCount;
				for (Object val : (List) value) {

					if (val instanceof Map) {
						// serviceList_1
						relationship.add(mainArrKey);
						arrylistCount++;
						if (!checkIfRef((Map) val)) {
							secondaryRelationship.add(mainArrKey + "_" + arrylistCount);
						} else {
							secondaryRelationship.add("ref_" + mainArrKey + "_" + arrylistCount);
						} 																// serviceLiset_1_2
						String nextKey = mainArrKey + "_" + arrylistCount;
						iterateMap(nextKey, (Map) val, arrylistCount);
					} else {
						valuesList.add(val);
					}

				}
				if (valuesList.size() > 0) {
					simplePropertiesMap.put(key, valuesList);
				}
				if (secondaryRelationship.size() > 0) {
					InsurancePlan secondaryRela = new InsurancePlan(mainArrKey, secondaryRelationship.toString());
					insuranceDBConn.saveInsuranceInfo(secondaryRela);
				}
			}

			else {
				// id,map---redis
				simplePropertiesMap.put(key, value);

			}

		}
		// Adding simple properties with main plan key in redis
		if (!simplePropertiesMap.isEmpty()) {
			InsurancePlan ip = new InsurancePlan("plan_" + localCount, new JSONObject(simplePropertiesMap).toString());
			insuranceDBConn.saveInsuranceInfo(ip);
		}

		// Storing realtionships with main plan relationship key
		if (relationship.size() > 0) {
			InsurancePlan planRelationship = new InsurancePlan("rel_plan_" + localCount, relationship.toString());
			insuranceDBConn.saveInsuranceInfo(planRelationship);
		}

		return "Plan_" + localCount ;
	}
	
	public boolean checkIfRef(Map map) {
		if (map.containsKey("ref") && null != map.get("ref")) {
			return (boolean) map.get("ref");

		}
		return false;

	}

	private void iterateMap(String mainKey, Map valueMap, long localCount) {

		Map<String, Object> simplePropertiesMap = new HashMap<String, Object>();
		Iterator<String> keysItr = valueMap.keySet().iterator();
		Set relationship = new HashSet();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = valueMap.get(key);

			if (value instanceof Map) {
				// id_relationship,Arraylist value--key
				// key+uuid value--redis
				relationship.add(key + "_" + localCount);
				iterateMap(key, (Map) value, localCount);

			} else if (value instanceof ArrayList) {
				List valuesList = new ArrayList();
				long arrylistCount = localCount;
				for (Object val : (List) value) {
					if (val instanceof Map) {
						iterateMap(key + "_", (Map) val, arrylistCount++);
					} else {
						valuesList.add(val);
					}
				}
				simplePropertiesMap.put(key, valuesList);

			} else {
				// id,map---redis
				simplePropertiesMap.put(key, value);

			}
		}
		// Adding simple properties with main plan key in redis
		if (!simplePropertiesMap.isEmpty()) {
			InsurancePlan ip = new InsurancePlan(mainKey, new JSONObject(simplePropertiesMap).toString());
			insuranceDBConn.saveInsuranceInfo(ip);
		}

		// Storing relationships with main plan relationship key
		if (relationship.size() > 0) {
			InsurancePlan entityRelationship = new InsurancePlan("rel_plan_" + localCount + "_" + mainKey,
					relationship.toString());
			insuranceDBConn.saveInsuranceInfo(entityRelationship);
		}

	}

}
