package com.ge.predix.solsvc.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.Body;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.entity.timeseries.datapoints.queryrequest.DatapointsQuery;
import com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.DatapointsLatestQuery;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
import com.ge.predix.solsvc.api.WindDataAPI;
import com.ge.predix.solsvc.restclient.impl.RestClient;
import com.ge.predix.solsvc.spi.IServiceManagerService;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesRestConfig;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesWSConfig;
import com.ge.predix.solsvc.timeseries.bootstrap.factories.TimeseriesFactory;

/**
 * 
 * @author predix -
 */
@Component
public class WindDataImpl implements WindDataAPI {

	@Autowired
	private IServiceManagerService serviceManagerService;

	@Autowired
	private TimeseriesRestConfig timeseriesRestConfig;

	@Autowired
	private RestClient restClient;

	@Autowired
	private TimeseriesWSConfig tsInjectionWSConfig;

	@Autowired
	private TimeseriesFactory timeseriesFactory;

	private static Logger log = LoggerFactory.getLogger(WindDataImpl.class);

	/**
	 * -
	 */
	public WindDataImpl() {
		super();
	}

	/**
	 * -
	 */
	@PostConstruct
	public void init() {
		this.serviceManagerService.createRestWebService(this, null);
		List<Header> headers = generateHeaders();
		headers.add(new BasicHeader("Origin", "http://predix.io"));
		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
		createMetrics(headers);
		this.timeseriesFactory.closeConnectionToTimeseriesWebsocket();

		this.serviceManagerService.createRestWebService(this, null);
		List<Header> headersForHealaway = generateHeaders();
		headers.add(new BasicHeader("Origin", "http://predix.io"));
		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
		createHealawayData(headers);
		this.timeseriesFactory.closeConnectionToTimeseriesWebsocket();

	}

	@Override
	public Response greetings() {
		return handleResult("Greetings from CXF Bean Rest Service " + new Date()); //$NON-NLS-1$
	}

	@Override
	public Response greetingsFromHealAway() {
		return handleResult("Greetings from HealAway@GE@Harman. Hope this makes you feel better. Long live.");
	}

	@Override
	public Response getPatientForHealAwayWeight(String id,
			String authorization, String starttime, String taglimit,
			String tagorder) {

		if (id == null) {
			return null;
		}

		List<Header> headers = generateHeaders();

		DatapointsQuery dpQuery = buildDatapointsQueryRequest(id, starttime,
				getInteger(taglimit), tagorder);

		DatapointsResponse response = this.timeseriesFactory
				.queryForDatapoints(this.timeseriesRestConfig.getBaseUrl(),
						dpQuery, headers);

		return handleResult(response);
	}

	@Override
	public Response getYearlyWindDataPoints(String id, String authorization,
			String starttime, String taglimit, String tagorder) {
		if (id == null) {
			return null;
		}

		List<Header> headers = generateHeaders();

		DatapointsQuery dpQuery = buildDatapointsQueryRequest(id, starttime,
				getInteger(taglimit), tagorder);
		DatapointsResponse response = this.timeseriesFactory
				.queryForDatapoints(this.timeseriesRestConfig.getBaseUrl(),
						dpQuery, headers);
		log.debug(response.toString());

		return handleResult(response);
	}

	/**
	 * 
	 * @param s
	 *            -
	 * @return
	 */
	private int getInteger(String s) {
		int inValue = 25;
		try {
			inValue = Integer.parseInt(s);

		} catch (NumberFormatException ex) {
			// s is not an integer
		}
		return inValue;
	}

	@Override
	public Response getLatestWindDataPoints(String id, String authorization) {
		if (id == null) {
			return null;
		}
		List<Header> headers = generateHeaders();

		DatapointsLatestQuery dpQuery = buildLatestDatapointsQueryRequest(id);
		DatapointsResponse response = this.timeseriesFactory
				.queryForLatestDatapoint(
						this.timeseriesRestConfig.getBaseUrl(), dpQuery,
						headers);
		log.debug(response.toString());

		return handleResult(response);
	}

	@SuppressWarnings({ "unqualified-field-access", "nls" })
	private List<Header> generateHeaders() {
		List<Header> headers = this.restClient.getSecureTokenForClientId();
		this.restClient.addZoneToHeaders(headers,
				this.timeseriesRestConfig.getZoneId());
		return headers;
	}

	private DatapointsLatestQuery buildLatestDatapointsQueryRequest(String id) {
		DatapointsLatestQuery datapointsLatestQuery = new DatapointsLatestQuery();

		com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag tag = new com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag();
		tag.setName(id);
		List<com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag> tags = new ArrayList<com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag>();
		tags.add(tag);
		datapointsLatestQuery.setTags(tags);
		return datapointsLatestQuery;
	}


	
	
	
	
	
	/**
	 * 
	 * @param id
	 * @param startDuration
	 * @param tagorder
	 * @return
	 */
	private DatapointsQuery buildDatapointsQueryRequest(String id,
			String startDuration, int taglimit, String tagorder) {
		DatapointsQuery datapointsQuery = new DatapointsQuery();
		List<com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag> tags = new ArrayList<com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag>();
		datapointsQuery.setStart(startDuration);
		//datapointsQuery.setStart("1y-ago"); //$NON-NLS-1$
		String[] tagArray = id.split(","); //$NON-NLS-1$
		List<String> entryTags = Arrays.asList(tagArray);

		for (String entryTag : entryTags) {
			com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag tag = new com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag();
			tag.setName(entryTag);
			tag.setLimit(taglimit);
			tag.setOrder(tagorder);
			tags.add(tag);
		}
		datapointsQuery.setTags(tags);
		return datapointsQuery;
	}

	@SuppressWarnings({ "nls", "unchecked" })
	private void createHealawayData(List<Header> headers) {
		for (int i = 0; i < 5; i++) {
			
			log.info("Creating Data for Healway");
			DatapointsIngestion dpIngestion = new DatapointsIngestion();
			dpIngestion
					.setMessageId(String.valueOf(System.currentTimeMillis()));

			// JSON inputJson = get Json from request

			Body body = new Body();
			log.debug("Creating new body for new tag:" +  "Harman - WeightingScale-1");
			body.setName("PatientId:37");
			List<Object> datapoint1 = new ArrayList<Object>();

			datapoint1.add(generateTimestampsWithinYear(System
					.currentTimeMillis()));
			datapoint1.add(78);
			datapoint1.add(4);

			// Create more data points as shown above

			// Create a Lit object to hold all data points and add to it the
			// data points created above
			List<Object> datapoints = new ArrayList<Object>();
			datapoints.add(datapoint1);

			// Add the List object created above to the body
			body.setDatapoints(datapoints);

			// Create a map to hold the attributes. Add the device id, patient
			// id, device details, doctor details etc here.
			com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
			map.put("devide id", "WS1"); //$NON-NLS-2$
			map.put("patient id", "Patient_1234"); //$NON-NLS-2$
			map.put("Device details",
					"Make: AMD, MinReading: 20, MaxReading: 120");
			map.put("Doctor details", "Name: Hans Verdi, Contact: 784-283-4496");

			// Add the attributes map to the body
			body.setAttributes(map);

			// Create a list to hold multiple bodies
			List<Body> bodies = new ArrayList<Body>();
			bodies.add(body);

			dpIngestion.setBody(bodies);
			this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion,
					headers);

			log.info("ingested from createHealawayData");

			
		}
	}

	@SuppressWarnings({ "nls", "unchecked" })
	private void createMetrics(List<Header> headers) {
		for (int i = 0; i < 10; i++) {
			DatapointsIngestion dpIngestion = new DatapointsIngestion();
			dpIngestion
					.setMessageId(String.valueOf(System.currentTimeMillis()));

			Body body = new Body();
			//body.setName("Compressor-2016:CompressionRatio"); //$NON-NLS-1$
			body.setName("PatientId:37");
			
			List<Object> datapoint1 = new ArrayList<Object>();
			datapoint1.add(generateTimestampsWithinYear(System
					.currentTimeMillis()));
			datapoint1.add(10);
			datapoint1.add(3); // quality

			List<Object> datapoint2 = new ArrayList<Object>();
			datapoint2.add(generateTimestampsWithinYear(System
					.currentTimeMillis()));
			datapoint2.add(9);
			datapoint2.add(1); // quality

			List<Object> datapoint3 = new ArrayList<Object>();
			datapoint3.add(generateTimestampsWithinYear(System
					.currentTimeMillis()));
			datapoint3.add(27);
			datapoint3.add(0); // quality

			List<Object> datapoint4 = new ArrayList<Object>();
			datapoint4.add(generateTimestampsWithinYear(System
					.currentTimeMillis()));
			datapoint4.add(78);
			datapoint4.add(2); // quality

			List<Object> datapoint5 = new ArrayList<Object>();
			datapoint5.add(generateTimestampsWithinYear(System
					.currentTimeMillis()));
			datapoint5.add(2);
			datapoint5.add(3); // quality

			List<Object> datapoint6 = new ArrayList<Object>();
			datapoint6.add(generateTimestampsWithinYear(System
					.currentTimeMillis()));
			datapoint6.add(98);
			datapoint6.add(1); // quality

			List<Object> datapoints = new ArrayList<Object>();
			datapoints.add(datapoint1);
			datapoints.add(datapoint2);
			datapoints.add(datapoint3);
			datapoints.add(datapoint4);
			datapoints.add(datapoint5);
			datapoints.add(datapoint6);

			body.setDatapoints(datapoints);

			com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
			map.put("host", "server1"); //$NON-NLS-2$
			map.put("customer", "Acme"); //$NON-NLS-2$

			body.setAttributes(map);

			List<Body> bodies = new ArrayList<Body>();
			bodies.add(body);

			dpIngestion.setBody(bodies);
			this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion,
					headers);
			log.debug("ingested from Create Metrics");
		}
	}

	@SuppressWarnings("javadoc")
	protected Response handleResult(Object entity) {
		ResponseBuilder responseBuilder = Response.status(Status.OK);
		responseBuilder.type(MediaType.APPLICATION_JSON);
		responseBuilder.entity(entity);
		return responseBuilder.build();
	}

	private Long generateTimestampsWithinYear(Long current) {
		long yearInMMS = Long.valueOf(31536000000L);
		return ThreadLocalRandom.current().nextLong(current - yearInMMS,
				current + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ge.predix.solsvc.api.WindDataAPI#getWindDataTags()
	 */
	@Override
	public Response getWindDataTags() {
		List<Header> headers = generateHeaders();
		CloseableHttpResponse httpResponse = null;
		String entity = null;
		try {
			httpResponse = this.restClient
					.get(this.timeseriesRestConfig.getBaseUrl() + "/v1/tags", headers, this.timeseriesRestConfig.getTimeseriesConnectionTimeout(), this.timeseriesRestConfig.getTimeseriesSocketTimeout()); //$NON-NLS-1$

			if (httpResponse.getEntity() != null) {
				try {
					entity = processHttpResponseEntity(httpResponse.getEntity());
					log.debug("HttpEntity returned from Tags" + httpResponse.getEntity().toString()); //$NON-NLS-1$
				} catch (IOException e) {
					throw new RuntimeException(
							"Error occured calling=" + this.timeseriesRestConfig.getBaseUrl() + "/v1/tags", e); //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		} finally {
			if (httpResponse != null)
				try {
					httpResponse.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}

		return handleResult(entity);
	}

	/**
	 * 
	 * @param entity
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	private String processHttpResponseEntity(org.apache.http.HttpEntity entity)
			throws IOException {
		if (entity == null)
			return null;
		if (entity instanceof GzipDecompressingEntity) {
			return IOUtils.toString(
					((GzipDecompressingEntity) entity).getContent(), "UTF-8");
		}
		return EntityUtils.toString(entity);
	}


	public Response getLatestRPMDataPoints(String id, String authorization) {
		if (id == null) {
			return null;
		}
		List<Header> headers = generateHeaders();

		DatapointsLatestQuery dpQuery = buildLatestDatapointsQueryRequest(id);
		DatapointsResponse response = this.timeseriesFactory
				.queryForLatestDatapoint(
						this.timeseriesRestConfig.getBaseUrl(), dpQuery,
						headers);
		log.debug(response.toString());

		return handleResult(response);
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public Response postPatientToDeviceMapping(String requestBody, String authorization, String contentType){

		List<Header> headers = generateHeaders();

		org.codehaus.jettison.json.JSONObject json = null;
		
		String deviceMACId = "";
		String deviceId = "";
		String patientId = "";
		
		try {
			 json = new org.codehaus.jettison.json.JSONObject(requestBody);
			 
			 deviceMACId = (String) json.get("deveiceMACId");
//			 deviceId = (String) json.get("deviceId");
			 patientId = (String) json.get("patientId");
			 
			 
		} catch (JSONException ex) {
			String errorString = "The input JSON is not correct in postPatientToDeviceMapping Method:" + ex.toString();
			log.error(errorString);
		}

		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));

		
		Body body = new Body();

		body.setName("patientId" + ":" + patientId);

		List<Object> datapoint1 = new ArrayList<Object>();

		datapoint1.add(generateTimestampsWithinYear(System.currentTimeMillis()));
		datapoint1.add(deviceMACId);
		datapoint1.add("10");

		// Create more data points as shown above

		// Create a Lit object to hold all data points and add to it the data
		// points created above
		List<Object> datapoints = new ArrayList<Object>();
		datapoints.add(datapoint1);

		// Add the List object created above to the body
		body.setDatapoints(datapoints);

		// Create a map to hold the attributes. Add the device id, patient id,
		// device details, doctor details etc here.
		com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
		map.put("devide id", "device id is the value coming in request body"); //$NON-NLS-2$
		map.put("patient id", "patient id is value coming in request body"); //$NON-NLS-2$

		// Add the attributes map to the body
		body.setAttributes(map);

		// Create a list to hold multiple bodies
		List<Body> bodies = new ArrayList<Body>();
		bodies.add(body);

		dpIngestion.setBody(bodies);

		this.serviceManagerService.createRestWebService(this, null);
		headers.add(new BasicHeader("Origin", "http://predix.io"));
		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
		this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion,
				headers);

		
		return handleResult("Device MAC Id: " + deviceId + "and " +  ", PatientId: " + patientId + " are mapped");
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	/*{
                "deviceRawData": "",
                "deviceData": "122,82,72",
                "assetId": "",
                "geoLocationLatitude": "",
                "clientId": "",
                "siteId": "",
                "deviceMACId": "21:21:21:21:21:21",
                "timeStamp": 1468914477000,
                "deviceType": "BPMonitor",
                "geoLocationLongitude": "",
                "readingType": 102
}
*/
//	https://healway-timeseries-service.run.aws-usw02-pr.ice.predix.io/services/windservices/postPatientWeightReading 

	public Response postPatientWeightReading(String requestBody, String authorization, String contentType) {

		List<Header> headers = generateHeaders();

		System.out.println("requestBody in postPatientWeightReading: " + requestBody);

		String deviceData = "";
		String deviceMACId = "";
		String deviceId = "";
		String deviceType = "";
		String deviceQuality = "";
		
		try {
			org.codehaus.jettison.json.JSONObject json = new org.codehaus.jettison.json.JSONObject(requestBody);
			System.out.println("json converted to string: " + json.toString());

			deviceData = (String) json.get("deviceData");
			System.out.println("Device Data got in input JSON string: " + deviceData);

			String deviceTimeStamp = json.get("timeStamp").toString();
			log.debug("Device Timestamp got in input JSON string: " + deviceTimeStamp);
		
			deviceMACId = json.getString("deviceMACId");
			
//			deviceId = json.getString("deviceId");
			
			deviceType = json.getString("deviceType");
			
			deviceQuality = "7"; //json.getString(deviceQuality);

		} catch (JSONException ex) {
			String errorString = "The input JSON is not correct in postPatientWeightReading Method:" + ex.toString();
			log.error(errorString);
		}


		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));
		
		Body body = new Body();

		body.setName("37");

		List<Object> datapoint1 = new ArrayList<Object>();

		datapoint1.add(generateTimestampsWithinYear(System.currentTimeMillis()));
		datapoint1.add(deviceData);
		datapoint1.add("7");

		// Create more data points as shown above

		// Create a List object to hold all data points and add to it the data points created above
		List<Object> datapoints = new ArrayList<Object>();
		datapoints.add(datapoint1);

		// Add the List object created above to the body
		body.setDatapoints(datapoints);

		// Create a map to hold the attributes. Add the device id, patient id, device details, doctor details etc here.
		com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
		map.put("devide id", deviceId); //$NON-NLS-2$
		map.put("deviceType", deviceType); //$NON-NLS-2$
		map.put("Device details", "all wil come as part of request Make: AMD, MinReading: 20, MaxReading: 120");
		map.put("deviceQuality", deviceQuality);

		// Add the attributes map to the body
		body.setAttributes(map);

		// Create a list to hold multiple bodies
		List<Body> bodies = new ArrayList<Body>();
		bodies.add(body);

		dpIngestion.setBody(bodies);

		this.serviceManagerService.createRestWebService(this, null);
		headers.add(new BasicHeader("Origin", "http://predix.io"));
		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
		this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion, headers);

		return handleResult("Posted Weight Datapoint: " + bodies);
	}
	
	
	// To get the BP reading
	
//	https://healway-timeseries-service.run.aws-usw02-pr.ice.predix.io/services/windservices/postPatientBPReading 

	@SuppressWarnings("unchecked")
	@Override
	public Response postPatientBPReading(String requestBody, String authorization, String contentType) {

		List<Header> headers = generateHeaders();

		System.out.println("requestBody in postPatientWeightReading: " + requestBody);

		String deviceData = "";
		String deviceMACId = "";
		String deviceId = "";
		String deviceType = "";
		String deviceQuality = "";
		
		try {
			org.codehaus.jettison.json.JSONObject json = new org.codehaus.jettison.json.JSONObject(requestBody);
			System.out.println("json converted to string: " + json.toString());

			deviceData = (String) json.get("deviceData");
			System.out.println("Device Data got in input JSON string: " + deviceData);

			String deviceTimeStamp = json.get("timeStamp").toString();
			log.debug("Device Timestamp got in input JSON string: " + deviceTimeStamp);
		
			deviceMACId = json.getString("deviceMACId");
			
			deviceId = json.getString("deviceId");
			
			deviceType = json.getString("deviceType");
			
			deviceQuality = "7"; //json.getString(deviceQuality);

		} catch (JSONException ex) {
			String errorString = "The input JSON is not correct in postPatientBPReading Method:" + ex.toString();
			log.error(errorString);
		}


		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));
		
		Body body = new Body();

		body.setName(deviceMACId);

		List<Object> datapoint1 = new ArrayList<Object>();

		datapoint1.add(generateTimestampsWithinYear(System.currentTimeMillis()));
		datapoint1.add(deviceData);
		datapoint1.add("7");

		// Create more data points as shown above

		// Create a List object to hold all data points and add to it the data points created above
		List<Object> datapoints = new ArrayList<Object>();
		datapoints.add(datapoint1);

		// Add the List object created above to the body
		body.setDatapoints(datapoints);

		// Create a map to hold the attributes. Add the device id, patient id, device details, doctor details etc here.
		com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
		map.put("devide id", deviceId); //$NON-NLS-2$
		map.put("deviceType", deviceType); //$NON-NLS-2$
		map.put("Device details", "all wil come as part of request Make: AMD, MinReading: 20, MaxReading: 120");
		map.put("deviceQuality", deviceQuality);

		// Add the attributes map to the body
		body.setAttributes(map);

		// Create a list to hold multiple bodies
		List<Body> bodies = new ArrayList<Body>();
		bodies.add(body);

		dpIngestion.setBody(bodies);

		this.serviceManagerService.createRestWebService(this, null);
		headers.add(new BasicHeader("Origin", "http://predix.io"));
		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
		this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion, headers);

		return handleResult("Posted BP Datapoint: " + bodies);

	}





//	To get the PulseOxi reading
	
//	https://healway-timeseries-service.run.aws-usw02-pr.ice.predix.io/services/windservices/postPatientOxiReading 

	@SuppressWarnings("unchecked")
	@Override
	public Response postPatientOxiReading(String requestBody,
			String authorization, String contentType) {

		List<Header> headers = generateHeaders();

		System.out.println("requestBody in postPatientWeightReading: " + requestBody);

		String deviceData = "";
		String deviceMACId = "";
		String deviceId = "";
		String deviceType = "";
		String deviceQuality = "";
		
		try {
			org.codehaus.jettison.json.JSONObject json = new org.codehaus.jettison.json.JSONObject(requestBody);
			System.out.println("json converted to string: " + json.toString());

			deviceData = (String) json.get("deviceData");
			System.out.println("Device Data got in input JSON string: " + deviceData);

			String deviceTimeStamp = json.get("timeStamp").toString();
			log.debug("Device Timestamp got in input JSON string: " + deviceTimeStamp);
		
			deviceMACId = json.getString("deviceMACId");
			
			deviceId = json.getString("deviceId");
			
			deviceType = json.getString("deviceType");
			
			deviceQuality = "7"; //json.getString(deviceQuality);

		} catch (JSONException ex) {
			String errorString = "The input JSON is not correct in postPatientPulseOxiReading Method:" + ex.toString();
			log.error(errorString);
		}


		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));
		
		Body body = new Body();

		body.setName(deviceMACId);

		List<Object> datapoint1 = new ArrayList<Object>();

		datapoint1.add(generateTimestampsWithinYear(System.currentTimeMillis()));
		datapoint1.add(deviceData);
		datapoint1.add("7");

		// Create more data points as shown above

		// Create a List object to hold all data points and add to it the data points created above
		List<Object> datapoints = new ArrayList<Object>();
		datapoints.add(datapoint1);

		// Add the List object created above to the body
		body.setDatapoints(datapoints);

		// Create a map to hold the attributes. Add the device id, patient id, device details, doctor details etc here.
		com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
		map.put("devide id", deviceId); //$NON-NLS-2$
		map.put("deviceType", deviceType); //$NON-NLS-2$
		map.put("Device details", "all wil come as part of request Make: AMD, MinReading: 20, MaxReading: 120");
		map.put("deviceQuality", deviceQuality);

		// Add the attributes map to the body
		body.setAttributes(map);

		// Create a list to hold multiple bodies
		List<Body> bodies = new ArrayList<Body>();
		bodies.add(body);

		dpIngestion.setBody(bodies);

		this.serviceManagerService.createRestWebService(this, null);
		headers.add(new BasicHeader("Origin", "http://predix.io"));
		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
		this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion, headers);

		return handleResult("Posted PulseOxi Datapoint: " + bodies);
	}

	@Override
	public Response getLatestWeightPointForDevice(String deviceId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getLatestWeightPointForPatient(String patientId, String authorization) {
		if (patientId == null) {
			return null;
		}
		List<Header> headers = generateHeaders();

		DatapointsLatestQuery dpQuery = buildLatestDatapointsQueryRequest(patientId);
		log.info("dpQuery === "  + dpQuery);
		DatapointsResponse response = this.timeseriesFactory
				.queryForLatestDatapoint(
						this.timeseriesRestConfig.getBaseUrl(), dpQuery,
						headers);
		log.debug(response.toString());

		return handleResult(response);

	
//		return handleResult("73");
	}

	@Override
	public Response getLatestBPPointForDevice(String deviceId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getLatestBPPointForPatient(String patientId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getLatestOxyPointForDevice(String deviceId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getLatestOxyPointForPatient(String patientId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public Response getLatestWeightDataPoints(String id, String authorization) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Response getPreviousWeightPointForPatient(String patientId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getPreviousBPPointForPatient(String patientId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getPreviousOxyPointForPatient(String patientId, String authorization) {
		// TODO Auto-generated method stub
		return null;
	}


}







































//package com.ge.predix.solsvc.impl;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//
//import javax.annotation.PostConstruct;
//import javax.ws.rs.DefaultValue;
//import javax.ws.rs.HeaderParam;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.ResponseBuilder;
//import javax.ws.rs.core.Response.Status;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.http.Header;
//import org.apache.http.client.entity.GzipDecompressingEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.util.EntityUtils;
//import org.codehaus.jettison.json.JSONException;
//import org.json.simple.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.Body;
//import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
//import com.ge.predix.entity.timeseries.datapoints.queryrequest.DatapointsQuery;
//import com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.DatapointsLatestQuery;
//import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
//import com.ge.predix.solsvc.api.WindDataAPI;
//import com.ge.predix.solsvc.restclient.impl.RestClient;
//import com.ge.predix.solsvc.spi.IServiceManagerService;
//import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesRestConfig;
//import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesWSConfig;
//import com.ge.predix.solsvc.timeseries.bootstrap.factories.TimeseriesFactory;
//
///**
// * 
// * @author predix -
// */
//@Component
//public class WindDataImpl implements WindDataAPI {
//
//	@Autowired
//	private IServiceManagerService serviceManagerService;
//
//	@Autowired
//	private TimeseriesRestConfig timeseriesRestConfig;
//
//	@Autowired
//	private RestClient restClient;
//
//	@Autowired
//	private TimeseriesWSConfig tsInjectionWSConfig;
//
//
//	@Autowired
//	private TimeseriesFactory timeseriesFactory;
//
//
//	private static Logger log = LoggerFactory.getLogger(WindDataImpl.class);
//
//	/**
//	 * -
//	 */
//	public WindDataImpl() {
//		super();
//	}
//
//	/**
//	 * -
//	 */
//	@PostConstruct
//	public void init() {
//		this.serviceManagerService.createRestWebService(this, null);
//		List<Header> headers = generateHeaders();
//		headers.add(new BasicHeader("Origin", "http://predix.io"));
//		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
//		createMetrics(headers);
//		this.timeseriesFactory.closeConnectionToTimeseriesWebsocket();
//		
//		this.serviceManagerService.createRestWebService(this, null);
//		List<Header> headersForHealaway = generateHeaders();
//		headers.add(new BasicHeader("Origin", "http://predix.io"));
//		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
//		createHealawayData(headersForHealaway);
//		this.timeseriesFactory.closeConnectionToTimeseriesWebsocket();
//		
//	}
//
//	@Override
//	public Response greetings() {
//		return handleResult("Greetings from CXF Bean Rest Service " + new Date()); //$NON-NLS-1$
//	}
//	
//	@Override
//	public Response greetingsFromHealAway(){
//		return handleResult("Greetings from HealAway@GE@Harman. Hope this makes you feel better. Long live.");
//	}
//	
//	
//	@Override
//	public Response getPatientForHealAwayWeight(String id, String authorization, String starttime, String taglimit, String tagorder){
//	
//		if(id == null){
//			return null;
//		}
//		
//		
//		List<Header> headers = generateHeaders();
//		
//		DatapointsQuery dpQuery= buildDatapointsQueryRequest(id, starttime, getInteger(taglimit), tagorder);
//		
//		DatapointsResponse response = this.timeseriesFactory.queryForDatapoints(this.timeseriesRestConfig.getBaseUrl(), dpQuery,  headers);
//		
//		return handleResult(response);
//	}
//
//	
//	
//	@Override
//	public Response getYearlyWindDataPoints(String id, String authorization,String starttime,String taglimit,String tagorder) {
//		if (id == null) {
//			return null;
//		}
//		
//		List<Header> headers = generateHeaders();
//
//		DatapointsQuery dpQuery = buildDatapointsQueryRequest(id, starttime,getInteger(taglimit),tagorder);
//		DatapointsResponse response = this.timeseriesFactory
//				.queryForDatapoints(this.timeseriesRestConfig.getBaseUrl(),
//						dpQuery, headers);
//		log.debug(response.toString());
//
//		return handleResult(response);
//	}
//
//	/**
//	 * 
//	 * @param s -
//	 * @return
//	 */
//	private int getInteger(String s) {
//		int inValue = 25;
//		try {
//			inValue = Integer.parseInt(s);
//			
//		} catch (NumberFormatException ex) {
//			// s is not an integer
//		}
//		return inValue;
//	}
//
//	@Override
//	public Response getLatestWindDataPoints(String id, String authorization) {
//		if (id == null) {
//			return null;
//		}
//		List<Header> headers = generateHeaders();
//
//		DatapointsLatestQuery dpQuery = buildLatestDatapointsQueryRequest(id);
//		DatapointsResponse response = this.timeseriesFactory
//				.queryForLatestDatapoint(
//						this.timeseriesRestConfig.getBaseUrl(), dpQuery,
//						headers);
//		log.debug(response.toString());
//
//		return handleResult(response);
//	}
//
//	@SuppressWarnings({ "unqualified-field-access", "nls" })
//	private List<Header> generateHeaders()
//    {
//        List<Header> headers = this.restClient.getSecureTokenForClientId();
//		this.restClient.addZoneToHeaders(headers,
//				this.timeseriesRestConfig.getZoneId());
//        return headers;
//    }
//
//
//	private DatapointsLatestQuery buildLatestDatapointsQueryRequest(String id) {
//		DatapointsLatestQuery datapointsLatestQuery = new DatapointsLatestQuery();
//
//		com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag tag = new com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag();
//		tag.setName(id);
//		List<com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag> tags = new ArrayList<com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.Tag>();
//		tags.add(tag);
//		datapointsLatestQuery.setTags(tags);
//		return datapointsLatestQuery;
//	}
//
//	/**
//	 * 
//	 * @param id
//	 * @param startDuration
//	 * @param tagorder 
//	 * @return
//	 */
//	private DatapointsQuery buildDatapointsQueryRequest(String id,
//			String startDuration, int taglimit, String tagorder) {
//		DatapointsQuery datapointsQuery = new DatapointsQuery();
//		List<com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag> tags = new ArrayList<com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag>();
//		datapointsQuery.setStart(startDuration);
//		//datapointsQuery.setStart("1y-ago"); //$NON-NLS-1$
//		String[] tagArray = id.split(","); //$NON-NLS-1$
//		List<String> entryTags = Arrays.asList(tagArray);
//
//		for (String entryTag : entryTags) {
//			com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag tag = new com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag();
//			tag.setName(entryTag);
//			tag.setLimit(taglimit);
//			tag.setOrder(tagorder); 
//			tags.add(tag);
//		}
//		datapointsQuery.setTags(tags);
//		return datapointsQuery;
//	}
//
//	@SuppressWarnings({"nls","unchecked"})
//	private void createHealawayData(List<Header>  headers){
//		for(int i = 0; i < 5; i++ ){
//			DatapointsIngestion dpIngestion = new DatapointsIngestion();
//			dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));
//			
//			
//			//JSON inputJson = get Json from request
//			
//			
//			Body body = new Body();
//			body.setName("WeighingScale-1");
//			List<Object> datapoint1 = new ArrayList<Object>();
//			
//			datapoint1.add(generateTimestampsWithinYear(System.currentTimeMillis()));
//			datapoint1.add(78);
//			datapoint1.add(4);
//			
//			//Create more data points as shown above
//			
//			//Create a Lit object to hold all data points and add to it the data points created above
//			List<Object> datapoints = new ArrayList<Object>();
//			datapoints.add(datapoint1);
//
//			//Add the List object created above to the body
//			body.setDatapoints(datapoints);
//			
//			//Create a map to hold the attributes. Add the device id, patient id, device details, doctor details etc here.
//			com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
//			map.put("devide id", "WS1"); //$NON-NLS-2$
//			map.put("patient id", "Patient_1234"); //$NON-NLS-2$
//			map.put("Device details", "Make: AMD, MinReading: 20, MaxReading: 120");
//			map.put("Doctor details", "Name: Hans Verdi, Contact: 784-283-4496");
//
//			//Add the attributes map to the body
//			body.setAttributes(map);
//			
//			//Create a list to hold multiple bodies
//			List<Body> bodies = new ArrayList<Body>();
//			bodies.add(body);
//
//			dpIngestion.setBody(bodies);
//			this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion, headers);
//		
//		}
//	}
//	
//	@SuppressWarnings({ "nls", "unchecked" })
//	private void createMetrics(List<Header> headers) {
//		for (int i = 0; i < 10; i++) {
//			DatapointsIngestion dpIngestion = new DatapointsIngestion();
//			dpIngestion
//					.setMessageId(String.valueOf(System.currentTimeMillis()));
//
//			Body body = new Body();
//			body.setName("Compressor-2015:CompressionRatio"); //$NON-NLS-1$
//			List<Object> datapoint1 = new ArrayList<Object>();
//			datapoint1.add(generateTimestampsWithinYear(System
//					.currentTimeMillis()));
//			datapoint1.add(10);
//			datapoint1.add(3); // quality
//
//			List<Object> datapoint2 = new ArrayList<Object>();
//			datapoint2.add(generateTimestampsWithinYear(System
//					.currentTimeMillis()));
//			datapoint2.add(9);
//			datapoint2.add(1); // quality
//
//			List<Object> datapoint3 = new ArrayList<Object>();
//			datapoint3.add(generateTimestampsWithinYear(System
//					.currentTimeMillis()));
//			datapoint3.add(27);
//			datapoint3.add(0); // quality
//
//			List<Object> datapoint4 = new ArrayList<Object>();
//			datapoint4.add(generateTimestampsWithinYear(System
//					.currentTimeMillis()));
//			datapoint4.add(78);
//			datapoint4.add(2); // quality
//
//			List<Object> datapoint5 = new ArrayList<Object>();
//			datapoint5.add(generateTimestampsWithinYear(System
//					.currentTimeMillis()));
//			datapoint5.add(2);
//			datapoint5.add(3); // quality
//
//			List<Object> datapoint6 = new ArrayList<Object>();
//			datapoint6.add(generateTimestampsWithinYear(System
//					.currentTimeMillis()));
//			datapoint6.add(98);
//			datapoint6.add(1); // quality
//
//			List<Object> datapoints = new ArrayList<Object>();
//			datapoints.add(datapoint1);
//			datapoints.add(datapoint2);
//			datapoints.add(datapoint3);
//			datapoints.add(datapoint4);
//			datapoints.add(datapoint5);
//			datapoints.add(datapoint6);
//
//			body.setDatapoints(datapoints);
//
//			com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
//			map.put("host", "server1"); //$NON-NLS-2$
//			map.put("customer", "Acme"); //$NON-NLS-2$
//
//			body.setAttributes(map);
//
//			List<Body> bodies = new ArrayList<Body>();
//			bodies.add(body);
//
//			dpIngestion.setBody(bodies);
//			this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion, headers);
//		}
//	}
//
//	@SuppressWarnings("javadoc")
//	protected Response handleResult(Object entity) {
//		ResponseBuilder responseBuilder = Response.status(Status.OK);
//		responseBuilder.type(MediaType.APPLICATION_JSON);
//		responseBuilder.entity(entity);
//		return responseBuilder.build();
//	}
//
//	private Long generateTimestampsWithinYear(Long current) {
//		long yearInMMS = Long.valueOf(31536000000L);
//		return ThreadLocalRandom.current().nextLong(current - yearInMMS,
//				current + 1);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.ge.predix.solsvc.api.WindDataAPI#getWindDataTags()
//	 */
//	@Override
//	public Response getWindDataTags() {
//		List<Header> headers = generateHeaders();
//		CloseableHttpResponse httpResponse = null;
//		String entity = null;
//		try {
//			httpResponse = this.restClient
//					.get(this.timeseriesRestConfig.getBaseUrl() + "/v1/tags", headers, this.timeseriesRestConfig.getTimeseriesConnectionTimeout(), this.timeseriesRestConfig.getTimeseriesSocketTimeout()); //$NON-NLS-1$
//
//			if (httpResponse.getEntity() != null) {
//				try {
//					entity = processHttpResponseEntity(httpResponse.getEntity());
//					log.debug("HttpEntity returned from Tags" + httpResponse.getEntity().toString()); //$NON-NLS-1$
//				} catch (IOException e) {
//					throw new RuntimeException(
//							"Error occured calling=" + this.timeseriesRestConfig.getBaseUrl() + "/v1/tags", e); //$NON-NLS-1$//$NON-NLS-2$
//				}
//			}
//		} finally {
//			if (httpResponse != null)
//				try {
//					httpResponse.close();
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//		}
//
//		return handleResult(entity);
//	}
//
//	/**
//	 * 
//	 * @param entity
//	 * @return
//	 * @throws IOException
//	 */
//	@SuppressWarnings("nls")
//	private String processHttpResponseEntity(org.apache.http.HttpEntity entity)
//			throws IOException {
//		if (entity == null)
//			return null;
//		if (entity instanceof GzipDecompressingEntity) {
//			return IOUtils.toString(
//					((GzipDecompressingEntity) entity).getContent(), "UTF-8");
//		}
//		return EntityUtils.toString(entity);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public Response postPatientWeightReading(String requestBody, String authorization, String contentType)  {
//		
//		List<Header> headers = generateHeaders();
//				
//		DatapointsIngestion dpIngestion = new DatapointsIngestion();
//		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));
//		System.out.println("requestBody in postPatientWeightReading: " + requestBody);
//		
//		//JSON inputJson = get Json from request
//		
//		String deviceData = "";
//		try{
//			org.codehaus.jettison.json.JSONObject json = new org.codehaus.jettison.json.JSONObject(requestBody);
//			System.out.println("json converted to string: " + json.toString());
//			
//			deviceData = (String) json.get("deviceData");
//			System.out.println("Device Data got in input JSON string: " + deviceData);
//			
////			String deviceData = (String) json.get("deviceData");
////			log.debug("Device Data got in input JSON string: " + deviceData);
////			
////			String deviceData = (String) json.get("deviceData");
////			log.debug("Device Data got in input JSON string: " + deviceData);
//			
//			
//		}catch(JSONException ex){
//			String errorString = "The input JSON is not correct:" + ex.toString();
//			log.error(errorString);
//		}
//		
////		requestBody.getJson
//		
//		Body body = new Body();
//	
//		
//		body.setName("WeighingScale-1");
//		
//		List<Object> datapoint1 = new ArrayList<Object>();
//		
//		datapoint1.add(generateTimestampsWithinYear(System.currentTimeMillis()));
//		datapoint1.add(deviceData);
//		datapoint1.add("7");
//		
//		//Create more data points as shown above
//		
//		//Create a Lit object to hold all data points and add to it the data points created above
//		List<Object> datapoints = new ArrayList<Object>();
//		datapoints.add(datapoint1);
//
//		//Add the List object created above to the body
//		body.setDatapoints(datapoints);
//		
//		//Create a map to hold the attributes. Add the device id, patient id, device details, doctor details etc here.
//		com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
//		map.put("devide id", "device id is the value coming in request body"); //$NON-NLS-2$
//		map.put("patient id", "patient id is value coming in request body"); //$NON-NLS-2$
//		map.put("Device details", "all wil come as part of request Make: AMD, MinReading: 20, MaxReading: 120");
////		map.put("Doctor details", "Name: Hans Verdi, Contact: 784-283-4496");
//
//		//Add the attributes map to the body
//		body.setAttributes(map);
//		
//		
//		//Create a list to hold multiple bodies
//		List<Body> bodies = new ArrayList<Body>();
//		bodies.add(body);
//
//		dpIngestion.setBody(bodies);
//
//		this.serviceManagerService.createRestWebService(this, null);
//		headers.add(new BasicHeader("Origin", "http://predix.io"));
//		this.timeseriesFactory.createConnectionToTimeseriesWebsocket(headers);
//		this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion, headers);
//		
//		return handleResult("Greetings from PostWeightReading@HealAway@GE@Harman. Hope this makes you feel better. Long live.");
//		
//	}
//
//
//
//
//
//
//}
