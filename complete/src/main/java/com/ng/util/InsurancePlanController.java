package com.ng.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.ng.pojo.InsurancePlan;


@RestController
public class InsurancePlanController {

	@Autowired
	private InsuranceDBConn insurancedbConn;

	private final AtomicLong counter = new AtomicLong();
	private Map<String, Map<String, Object>> insuranceMap = new HashMap<String, Map<String, Object>>();

	// Get all the data
	@RequestMapping(value = "/plan", method = RequestMethod.GET)
	public JSONArray getAll() throws ParseException {
		JSONArray jsonArray = new JSONArray();

		String count = insurancedbConn.findInsurancePlan("Count");
		int countInt = Integer.parseInt(count);
		for (int i = 1; i <= countInt; i++) {
			jsonArray.add(getSpecficPlan("plan_" + i));
		}
		return jsonArray;

	}

	// Get details for particular ID
	@RequestMapping(value = "/{urlTypes}/{id}", method = RequestMethod.GET)
	@ResponseBody
	public JSONObject getInsuranceById(@PathVariable String id, HttpServletRequest request,
			HttpServletResponse response,@PathVariable String urlTypes) throws ParseException {
		JSONParser jsonParser = new JSONParser();
		String etagFromRequest = request.getHeader(HttpHeaders.IF_NONE_MATCH);
		String etagFromRedis = insurancedbConn.findInsurancePlan("etag_" +urlTypes+"_" +id);
		
		id = urlTypes + "_" + id;
		
		if (etagFromRequest == null || !etagFromRequest.equals(etagFromRedis)) {
			String simplePropertiesMap = insurancedbConn.findInsurancePlan(id);
			if (null != simplePropertiesMap && !simplePropertiesMap.isEmpty()) {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(simplePropertiesMap);
				String str = insurancedbConn.findPlanRelationShips("rel_" + id);
				str = str.toString().replace("[", "").replace("]", "");
				String[] relationShips = str.split(",");
				for (String rel : relationShips) {
					String nestSimpleProp = insurancedbConn.findInsurancePlan(rel.trim().replaceAll("ref_", ""));
					if (!nestSimpleProp.contains("[")) {
						JSONObject jsonObject1 = (JSONObject) jsonParser.parse(nestSimpleProp);
						rel = rel.replaceAll("ref_", "");
						jsonObject.put(rel.substring(0, rel.indexOf("_")), jsonObject1);
					} else {
						nestSimpleProp = nestSimpleProp.toString().replace("[", "").replace("]", "");
						String[] nestSimplePropArr = nestSimpleProp.split(",");
						JSONArray jsonArray = new JSONArray();
						for (String rel1 : nestSimplePropArr) {
							String rel1value = insurancedbConn.findInsurancePlan(rel1.trim().replaceAll("ref_", ""));
							JSONObject jsonObject1 = (JSONObject) jsonParser.parse(rel1value);
							// jsonObject.put(rel1.toString().substring(0,
							// rel1.toString().indexOf("_")), jsonObject1);
							jsonArray.add(jsonObject1);
						}
						jsonObject.put(rel.toString().substring(0, rel.toString().indexOf("_")), jsonArray);
					}
				}
				// JSONObject jsonObject = new JSONObject(simplePropertiesMap);
				return jsonObject;
			} else {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", "plan does not exists");
				return jsonObject;
			}
		} else {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("error", "No change in object");
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return jsonObject;
		}

	}

	// Post insurance details
	@RequestMapping(value = "/{url}", method = RequestMethod.POST)
	public String addGreeting(@RequestBody JSONObject jsonObject, HttpServletResponse response,
			@PathVariable String url)
			throws ProcessingException, IOException, ParseException, NoSuchAlgorithmException {
		if (url.equalsIgnoreCase("jsonSchema")) {
			addSchemaToRedis();
			return "schema added";
		}

		if (validateJSON(jsonObject)) {
			long tagCount = counter.incrementAndGet();
			
			response.setHeader(HttpHeaders.IF_NONE_MATCH, addPostDataToRedisEtag(jsonObject,tagCount));
			return addPostDataToRedis(jsonObject,tagCount);
		} else {
			return "failed to post.Please check the json schema";
		}
	}
	
	
	public String addPostDataToRedisEtag(JSONObject jsonObject,long tagCount)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {

		String etagValue = JSONUtility.generateEtag(jsonObject.toString());
		InsurancePlan etag = new InsurancePlan("etag_plan_" + tagCount, etagValue);
		insurancedbConn.saveInsuranceInfo(etag);
		return etagValue;
	}


	// update insurance details
	@RequestMapping(value = "/{urlTypes}/{id}", method = RequestMethod.PUT)
	public String updatePlan(@RequestBody JSONObject jsonObject, @PathVariable String id,@PathVariable String urlTypes,
			HttpServletResponse response) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		// insuranceMap.put(id, jsonObject);
		String newID;
		newID = urlTypes + "_" + id;
		
		updatePutToRedis(jsonObject, newID);
		InsurancePlan etag = new InsurancePlan("etag_" + newID, JSONUtility.generateEtag(jsonObject.toString()));
		insurancedbConn.saveInsuranceInfo(etag);

		response.setHeader(HttpHeaders.IF_NONE_MATCH, JSONUtility.generateEtag(jsonObject.toString()));

		return "updated insurance info";

	}

	// delete insurance info
	@RequestMapping(value = "/{urlTypes}/{id}", method = RequestMethod.DELETE)
	public String deletePlan(@PathVariable String id, @PathVariable String urlTypes) {

		id = urlTypes + "_" + id;
		
		String str = insurancedbConn.findPlanRelationShips("rel_" + id);
		str = str.toString().replace("[", "").replace("]", "");
		String[] relationShips = str.split(",");
		for (String rel : relationShips) {
			String nestSimpleProp = insurancedbConn.findInsurancePlan(rel.trim());
			if (null != nestSimpleProp) {
				if (!nestSimpleProp.contains("[")) {
					if (!rel.contains("ref_")) {
						insurancedbConn.deleteInsurancePlan(rel.trim());
					}
				} else {
					nestSimpleProp = nestSimpleProp.toString().replace("[", "").replace("]", "");
					String[] nestSimplePropArr = nestSimpleProp.split(",");

					for (String rel1 : nestSimplePropArr) {
						if (!rel1.contains("ref_")) {
							insurancedbConn.deleteInsurancePlan(rel1.trim());
						}
					}
					insurancedbConn.deleteInsurancePlan(rel.trim());
				}
			}
		}
		insurancedbConn.deleteInsurancePlan("rel_" + id);
		insurancedbConn.deleteInsurancePlan(id);
		return "success from delete for id : " + id;

	}

	// working
	@RequestMapping(value = { "/{urlTypes}/{id}", "/{urlTypes}/{id}/{subId}" }, method = RequestMethod.PATCH)
	public String patchGreeting(@RequestBody JSONObject newjsonObject, @PathVariable String id,
			@PathVariable String urlTypes, @PathVariable Optional<String> subId) throws ParseException {
		id = urlTypes + "_" + id;
		if (subId.isPresent()) {
			id = id + "_" + subId.get();
		}
		JSONParser jsonParser = new JSONParser();
		String content = insurancedbConn.findInsurancePlan(id);
		JSONObject oldJSONObject = (JSONObject) jsonParser.parse(content);
		JSONObject finalJSON = JSONUtility.patch(oldJSONObject, newjsonObject);
		InsurancePlan insurancePlan = new InsurancePlan(id, finalJSON.toString());
		insurancedbConn.saveInsuranceInfo(insurancePlan);

		return "success from patch for " + id;

	}

	// validation for json document
	public boolean validateJSON(JSONObject jsonObject) throws ProcessingException, IOException, ParseException {
		JSONParser parser = new JSONParser();

		JSONParser jsonParser = new JSONParser();
		String jsonSchema = insurancedbConn.findInsurancePlan("jsonSchema");
		JSONObject jsonSchemaObj = (JSONObject) jsonParser.parse(jsonSchema);

		if (ValidationUtils.isJsonValid(jsonSchemaObj.toJSONString(), jsonObject.toJSONString())) {
			return true;
		} else {
			return false;
		}
	}

	public void addSchemaToRedis() throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();

		Object obj;
		obj = parser.parse(new FileReader("D:/files/schema-1.json"));
		JSONObject jsonSchema = (JSONObject) obj;

		InsurancePlan ip = new InsurancePlan("jsonSchema", jsonSchema.toString());
		insurancedbConn.saveInsuranceInfo(ip);

	}

	public JSONObject getSpecficPlan(String id) throws ParseException {
		JSONParser jsonParser = new JSONParser();

		String simplePropertiesMap = insurancedbConn.findInsurancePlan(id);
		if (null != simplePropertiesMap && !simplePropertiesMap.isEmpty()) {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(simplePropertiesMap);
			String str = insurancedbConn.findPlanRelationShips("rel_" + id);
			str = str.toString().replace("[", "").replace("]", "");
			String[] relationShips = str.split(",");
			for (String rel : relationShips) {
				String nestSimpleProp = insurancedbConn.findInsurancePlan(rel.trim().toString());
				if (!nestSimpleProp.contains("[")) {
					JSONObject jsonObject1 = (JSONObject) jsonParser.parse(nestSimpleProp);
					jsonObject.put(rel.toString().substring(0, rel.toString().indexOf("_")), jsonObject1);
				} else {
					nestSimpleProp = nestSimpleProp.toString().replace("[", "").replace("]", "");
					String[] nestSimplePropArr = nestSimpleProp.split(",");
					JSONArray jsonArray = new JSONArray();
					for (String rel1 : nestSimplePropArr) {
						String rel1value = insurancedbConn.findInsurancePlan(rel1.trim().toString());
						JSONObject jsonObject1 = (JSONObject) jsonParser.parse(rel1value);
						// jsonObject.put(rel1.toString().substring(0,
						// rel1.toString().indexOf("_")), jsonObject1);
						jsonArray.add(jsonObject1);
					}
					jsonObject.put(rel.toString().substring(0, rel.toString().indexOf("_")), jsonArray);
				}
			}
			// JSONObject jsonObject = new JSONObject(simplePropertiesMap);
			return jsonObject;
		}

		return null;
	}

	// inject the template as ListOperations
	// can also inject as Value, Set, ZSet, and HashOperations

	public String addPostDataToRedis(JSONObject jsonObject, long tagCount)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		JSONUtility jsonUtility = new JSONUtility();
		int countInt = 0;
		
		if (null != insurancedbConn.findInsurancePlan("Count")) {
			String count = insurancedbConn.findInsurancePlan("Count");
			countInt = Integer.parseInt(count) + 1;
			InsurancePlan entity = new InsurancePlan("Count", String.valueOf(countInt));
			insurancedbConn.saveInsuranceInfo(entity);
		} else {
			InsurancePlan entity = new InsurancePlan("Count", String.valueOf(1));
			insurancedbConn.saveInsuranceInfo(entity);
		}
		return jsonUtility.jsonToMapAndStoreInRedis(jsonObject, insurancedbConn, tagCount);

	}

	public void updatePutToRedis(JSONObject jsonObject, String id) {
		// InsurancePlan insurancePlan = new InsurancePlan(id,
		// jsonObject.toString());
		// insurancedbConn.updateInsurancePlan(insurancePlan);

		JSONUtility jsonUtility = new JSONUtility();
		jsonUtility.jsonToMapAndStoreInRedis(jsonObject, insurancedbConn,
				Long.parseLong(id.substring(id.indexOf("_") + 1, id.length())));

	}

	public void deleteFromRedis(String id) {
		insurancedbConn.deleteInsurancePlan(id);
	}

}
