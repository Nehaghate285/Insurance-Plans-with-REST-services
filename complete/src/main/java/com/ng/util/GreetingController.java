//package com.ng.util;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicLong;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpRequest;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.context.request.ServletWebRequest;
//import org.springframework.web.context.request.WebRequest;
//import com.github.fge.jsonschema.core.exceptions.ProcessingException;
//import com.ng.pojo.InsurancePlan;
//
//@RestController
//public class GreetingController {
//
//	
//	 @Autowired
//	 private InsuranceDBConn greetingImpl;
//	
//	private static final String template = "Hello, %s!";
//	private final AtomicLong counter = new AtomicLong();
//	private Map<Long, Map<String, Object>> map = new HashMap<Long, Map<String, Object>>();
//
//	// working
//	@RequestMapping(value = "/{greeting}", method = RequestMethod.GET)
//	public JSONArray greeting() throws ParseException {
//
//		JSONArray jsonArray = new JSONArray();
//		Map<String, String> map=	greetingImpl.findAllStudents();
//		for (String obj : map.values()) {
//			JSONParser jsonParser=new JSONParser();
//			JSONObject jsonObject = (JSONObject) jsonParser.parse(obj);
//			
//			jsonArray.add(jsonObject);
//
//		}
//
//
//		return jsonArray;
//
//	}
//
//	// working
//	@RequestMapping(value = "/greeting/{id}", method = RequestMethod.GET)
//	@ResponseBody
//	public JSONObject getSepcficGreeting(@RequestParam(value = "name", defaultValue = "World") String name,
//			@PathVariable String id, WebRequest webRequest, HttpRequest request, HttpServletResponse response,
//			ServletWebRequest servletWebRequest) throws ParseException {
//		//CacheControl cc = new CacheControl();
//
//		String greeting=greetingImpl.findStudent(id);
//		//Map map=ValidationUtils.splitToMap(greeting, " ", "=");
//		JSONParser jsonParser=new JSONParser();
//		JSONObject jsonObject = (JSONObject) jsonParser.parse(greeting);
//		//jsonObject.putAll(map);
//		return jsonObject;
//		
//		// Long str = (Long) jsonObject.get("lastModified");
//		//
//		// boolean notModified = servletWebRequest.checkNotModified(new
//		// Date().getTime());
//		//
//		// if (notModified) {
//		// JSONObject jsonObject1 = new JSONObject();
//		// jsonObject1.put("value", "not modified");
//		// return jsonObject1;
//		// } else {
//		// // String etag=String.valueOf(jsonObject.hashCode());
//		// // boolean ismodified = webRequest.checkNotModified(etag);
//		// // if ( !ismodified) {
//		// // JSONObject jsonObject1 = new JSONObject();
//		// // jsonObject1.put("value", "not modified");
//		// // return jsonObject1;
//		// //
//		// // }
//		// // else {
//		// // //Forces revalidation of cache. Browser would send an
//		// // If-Not-Match with ETag in the next request.
//		// // response.setHeader("Cache-control", "max-age=0");
//		// // return jsonObject;
//		// // }
//		// return jsonObject;
//		// }
//	}
//
//	// working
//	@RequestMapping(value = "/{greeting}", method = RequestMethod.POST)
//	public String addGreeting(@RequestBody JSONObject jsonObject)
//			throws ProcessingException, IOException, ParseException {
//		if (validateJSON(jsonObject)) {
//			addPostDataToRedis(jsonObject);
//			
//			
//			return "success";
//		} else {
//			return "failed";
//		}
//
//	}
//	// working
//
//	@RequestMapping(value = "/greeting/{id}", method = RequestMethod.PUT)
//	public String updateGreeting(@RequestBody JSONObject jsonObject, @PathVariable Long id) {
//
//		// jsonObject.put("lastModified", new Date().getTime());
//		map.put(id, jsonObject);
//		return "success from put";
//
//	}
//
//	// working
//	@RequestMapping(value = "/greeting/{id}", method = RequestMethod.DELETE)
//	public String deleteGreeting(@PathVariable Long id) {
//		map.remove(id);
//		return "success from delete";
//
//	}
//
//	// not working
//	@RequestMapping(value = "/greeting/{id}", method = RequestMethod.PATCH)
//	public String patchGreeting(@RequestBody JSONObject jsonObject, @PathVariable Long id) {
//		JSONObject jsonObject1 = new JSONObject();
//		jsonObject1.putAll(map.get(id));
//		jsonObject1 = JSONUitility.patch(jsonObject1, jsonObject);
//		map.put(id, jsonObject1);
//		return "success from patch";
//
//	}
//
//	public boolean validateJSON(JSONObject jsonObject) throws ProcessingException, IOException, ParseException {
//		JSONParser parser = new JSONParser();
//
//		Object obj;
//		obj = parser.parse(new FileReader("C:/Users/pranjal.jain/Desktop/schema-1.json"));
//		JSONObject jsonSchema = (JSONObject) obj;
//		File schemaFile = new File("C:/Users/pranjal.jain/Desktop/schema-1.json");
//		File jsonFile = new File("C:/Users/pranjal.jain/Desktop/sample-2.json");
//
//		if (ValidationUtils.isJsonValid(jsonSchema.toJSONString(), jsonObject.toJSONString())) {
//			return true;
//		} else {
//			return false;
//		}
//
//	}
//
//	
//	 public void addPostDataToRedis(JSONObject jsonObject){
//		
//		// setMap.add("greeting"+counter.incrementAndGet(), map1.toString());
//		// hashMap.put("Greeting","greeting"+counter.incrementAndGet(), map1.toString());
//		 InsurancePlan greeting=new InsurancePlan("greeting"+counter.incrementAndGet(),jsonObject.toString());
//		 greetingImpl.saveStudent(greeting);
//		 
//	
//	 }
//	
//
//}