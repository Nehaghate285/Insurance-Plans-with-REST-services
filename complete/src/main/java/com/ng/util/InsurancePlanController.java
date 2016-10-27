package com.ng.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
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
		Map<String, String> map = insurancedbConn.findAllInsurancePlans();
		for (String obj : map.values()) {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(obj);
			jsonArray.add(jsonObject);
		}
		return jsonArray;

	}

	// Get details for particular ID
	@RequestMapping(value = "/plan/{id}", method = RequestMethod.GET)
	@ResponseBody
	public JSONObject getInsuranceById(@PathVariable String id) throws ParseException {
		String insuranceInfo = insurancedbConn.findInsurancePlan(id);
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(insuranceInfo);
		return jsonObject;
	}

	// Post insurance details
	@RequestMapping(value = "/plan", method = RequestMethod.POST)
	public String addGreeting(@RequestBody JSONObject jsonObject)
			throws ProcessingException, IOException, ParseException {
		if (validateJSON(jsonObject)) {
			addPostDataToRedis(jsonObject);
			return "posted insurance details";
		} else {
			return "failed to post.Please check the json schema";
		}
	}
	
	// update insurance details
	@RequestMapping(value = "/plan/{id}", method = RequestMethod.PUT)
	public String updateGreeting(@RequestBody JSONObject jsonObject, @PathVariable String id) {
//		insuranceMap.put(id, jsonObject);
		updatePutToRedis(jsonObject,id);
		return "updated insurance info";

	}

	// delete insurance info
	@RequestMapping(value = "/plan/{id}", method = RequestMethod.DELETE)
	public String deleteGreeting(@PathVariable String id) {
//		insuranceMap.remove(id);
		deleteFromRedis(id);
		return "successfully deleted";
	}

	// not working
	@RequestMapping(value = "/plan/{id}", method = RequestMethod.PATCH)
	public String patchGreeting(@RequestBody JSONObject newjsonObject, @PathVariable String id) throws ParseException {
		String oldObject = insurancedbConn.findInsurancePlan(id);
		JSONParser jsonParser = new JSONParser();
		JSONObject oldJsonObject = (JSONObject) jsonParser.parse(oldObject);
	
//		jsonObject1.putAll(insuranceMap.get(id));
		JSONObject patchedObject = JSONUtility.patchObj(oldJsonObject, newjsonObject);
//		insuranceMap.put(id, jsonObject1);
		updatePutToRedis(patchedObject, id);
		return "success from patch";

	}

	
	//validation for json document 
	public boolean validateJSON(JSONObject jsonObject) throws ProcessingException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		Object obj;
		obj = parser.parse(new FileReader("C:/My Work/Big data Design/projFiles/schema-1.json"));
		JSONObject jsonSchema = (JSONObject) obj;
		
		File schemaFile = new File("C:/My Work/Big data Design/projFiles/schema-1.json");
		File jsonFile = new File("C:/My Work/Big data Design/projFiles/sample-2.json");

		if (ValidationUtils.isJsonValid(jsonSchema.toJSONString(), jsonObject.toJSONString())) {
			return true;
		} else {
			return false;
		}
	}

	// inject the template as ListOperations
	// can also inject as Value, Set, ZSet, and HashOperations

	public void addPostDataToRedis(JSONObject jsonObject) {
		InsurancePlan insurancePlan = new InsurancePlan("insurance_" + counter.incrementAndGet(), 
				jsonObject.toString());
		insurancedbConn.saveInsuranceInfo(insurancePlan);
	}
	
	public void updatePutToRedis(JSONObject jsonObject, String id) {
		InsurancePlan insurancePlan =  new InsurancePlan(id,jsonObject.toString());
		insurancedbConn.updateInsurancePlan(insurancePlan);
	}
	
	public void deleteFromRedis(String id) {
		insurancedbConn.deleteInsurancePlan(id);
	}
	
}
