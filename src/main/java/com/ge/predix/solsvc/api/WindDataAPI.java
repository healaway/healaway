package com.ge.predix.solsvc.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.ws.rs.core.HttpHeaders;;

/**
 * 
 * @author predix -
 */
@Consumes(
{
        "application/json", "application/xml"
})
@Produces(
{
        "application/json", "application/xml"
})
@Path("/windservices")
public interface WindDataAPI
{
	/**
	 * @return -
	 */
	@GET
	@Path("/ping")
	public Response greetings();

	/**
	 * @return -
	 */
	@GET
	@Path("/pingHeal")
	public Response greetingsFromHealAway();


	
	/**
	 *  * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @param starttime
	 *            -
	 * @param tagLimit -
	 * @param tagorder -
	 * @return - Patient Readings
	 */
	@GET
	@Path("/getPatientReadingsForWeight")
	public Response getPatientForHealAwayWeight(@PathParam("id") String id,
			@HeaderParam(value = "Authorization") String authorization,
			@DefaultValue("1y-ago") @QueryParam("starttime") String starttime,
			@DefaultValue("25") @QueryParam("taglimit") String tagLimit,@DefaultValue("desc") @QueryParam("order") String tagorder);


	/**
	 *  * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @param starttime
	 *            -
	 * @param tagLimit -
	 * @param tagorder -
	 * @return 
	 * @return - 
	 */
	@POST
	@Path("/postPatientWeightReading")
	public Response postPatientWeightReading(@RequestBody String requestBody, @RequestHeader("Authorization") String authorization, 
					@RequestHeader("Content-Type") String contentType);
	
	

	/**
	 *  * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @param starttime
	 *            -
	 * @param tagLimit -
	 * @param tagorder -
	 * @return 
	 * @return - 
	 */
//	public Response getYearlyWindDataPoints(@PathParam("id") String id,
//			@HeaderParam(value = "Authorization") String authorization,
//			@DefaultValue("1y-ago") @QueryParam("starttime") String starttime,
//			@DefaultValue("25") @QueryParam("taglimit") String tagLimit,@DefaultValue("desc") @QueryParam("order") String tagorder);

	
	
	@POST
	@Path("/postPatientToDeviceMapping")
	public Response postPatientToDeviceMapping(	@RequestBody String requestBody, 
												@RequestHeader("Authorization") String authorization, 
												@RequestHeader("Content-Type") String contentType);		
	

	
	
	
	
	/**
	 *  * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @param starttime
	 *            -
	 * @param tagLimit -
	 * @param tagorder -
	 * @return 
	 * @return - 
	 */
	@POST
	@Path("/postPatientBPReading")
	public Response postPatientBPReading(@RequestBody String requestBody, @RequestHeader("Authorization") String authorization, 
					@RequestHeader("Content-Type") String contentType);

	
	
	/**
	 *  * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @param starttime
	 *            -
	 * @param tagLimit -
	 * @param tagorder -
	 * @return 
	 * @return - 
	 */
	@POST
	@Path("/postPatientOxiReading")
	public Response postPatientOxiReading(@RequestBody String requestBody, @RequestHeader("Authorization") String authorization, 
					@RequestHeader("Content-Type") String contentType);
	/**
	 *  * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @param starttime
	 *            -
	 * @param tagLimit -
	 * @param tagorder -
	 * @return - Patient Readings
	 */
//	@GET
//	@Path("/getPatientReadingsForBP")
//	public Response getPatientForHealAwayBP(@PathParam("id") String id,
//			@HeaderParam(value = "Authorization") String authorization,
//			@DefaultValue("1y-ago") @QueryParam("starttime") String starttime,
//			@DefaultValue("250") @QueryParam("taglimit") String tagLimit,@DefaultValue("desc") @QueryParam("order") String tagorder);
	
	
	
	
	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @param starttime
	 *            -
	 * @param tagLimit -
	 * @param tagorder -
	 * @return -
	 */
	@GET
	@Path("/yearly_data/sensor_id/{id}")
	public Response getYearlyWindDataPoints(@PathParam("id") String id,
			@HeaderParam(value = "Authorization") String authorization,
			@DefaultValue("1y-ago") @QueryParam("starttime") String starttime,
			@DefaultValue("25") @QueryParam("taglimit") String tagLimit,@DefaultValue("desc") @QueryParam("order") String tagorder);

	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_data/sensor_id/{id}")
	public Response getLatestWindDataPoints(@PathParam("id") String id,
			@HeaderParam(value = "authorization") String authorization);



	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_data/sensor_id/{id}")
	public Response getLatestWeightPointForDevice(@PathParam("id") String deviceId,
			@HeaderParam(value = "authorization") String authorization);


	
	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_data/sensor_id/{id}")
	public Response getPreviousWeightPointForPatient(@PathParam("id") String patientId,
			@HeaderParam(value = "authorization") String authorization);

	
	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_weight_for_patient_with_id/{id}")
	public Response getLatestWeightPointForPatient(@PathParam("id") String patientId,
			@HeaderParam(value = "authorization") String authorization);


	
	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_BP_for_device_with_id/{id}")
	public Response getLatestBPPointForDevice(@PathParam("id") String deviceId,
			@HeaderParam(value = "authorization") String authorization);



	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/previous_BP_reading_for_patient_with_id/{id}")
	public Response getPreviousBPPointForPatient(@PathParam("id") String patientId,
			@HeaderParam(value = "authorization") String authorization);

	
	
	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_BP_reading_for_patient_with_id/{id}")
	public Response getLatestBPPointForPatient(@PathParam("id") String patientId,
			@HeaderParam(value = "authorization") String authorization);

	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_Oxy_reading_for_device_with_id/{id}")
	public Response getLatestOxyPointForDevice(@PathParam("id") String deviceId,
			@HeaderParam(value = "authorization") String authorization);



	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/previous_Oxy_reading_for_patient_with_id/{id}")
	public Response getPreviousOxyPointForPatient(@PathParam("id") String patientId,
			@HeaderParam(value = "authorization") String authorization);

	
	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */
	@GET
	@Path("/latest_data/sensor_id/{id}")
	public Response getLatestOxyPointForPatient(@PathParam("id") String patientId,
			@HeaderParam(value = "authorization") String authorization);


	/**
	 * 
	 * @return List of tags
	 */
	@GET
	@Path("/tags")
	public Response getWindDataTags();

//	Response getLatestWeightDataPoints(String id, String authorization);


	/**
	 * @param id
	 *            -
	 * @param authorization
	 *            -
	 * @return -
	 */



}


































//package com.ge.predix.solsvc.api;
//
//import javax.ws.rs.Consumes;
//import javax.ws.rs.DefaultValue;
//import javax.ws.rs.GET;
//import javax.ws.rs.HeaderParam;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.Response;
//
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//
//import javax.ws.rs.core.HttpHeaders;;
//
///**
// * 
// * @author predix -
// */
//@Consumes(
//{
//        "application/json", "application/xml"
//})
//@Produces(
//{
//        "application/json", "application/xml"
//})
//@Path("/windservices")
//public interface WindDataAPI
//{
//	/**
//	 * @return -
//	 */
//	@GET
//	@Path("/ping")
//	public Response greetings();
//
//	/**
//	 * @return -
//	 */
//	@GET
//	@Path("/pingHeal")
//	public Response greetingsFromHealAway();
//
//
//	
//	/**
//	 *  * @param id
//	 *            -
//	 * @param authorization
//	 *            -
//	 * @param starttime
//	 *            -
//	 * @param tagLimit -
//	 * @param tagorder -
//	 * @return - Patient Readings
//	 */
//	@GET
//	@Path("/getPatientReadingsForWeight")
//	public Response getPatientForHealAwayWeight(@PathParam("id") String id,
//			@HeaderParam(value = "Authorization") String authorization,
//			@DefaultValue("1y-ago") @QueryParam("starttime") String starttime,
//			@DefaultValue("25") @QueryParam("taglimit") String tagLimit,@DefaultValue("desc") @QueryParam("order") String tagorder);
//
//
//	/**
//	 *  * @param id
//	 *            -
//	 * @param authorization
//	 *            -
//	 * @param starttime
//	 *            -
//	 * @param tagLimit -
//	 * @param tagorder -
//	 * @return 
//	 * @return - Patient Readings
//	 */
//	@POST
//	@Path("/postPatientWeightReading")
//	public Response postPatientWeightReading(@RequestBody String requestBody, @RequestHeader("Authorization") String authorization, 
//					@RequestHeader("Content-Type") String contentType);
//
//	
//	
//	
//	/**
//	 *  * @param id
//	 *            -
//	 * @param authorization
//	 *            -
//	 * @param starttime
//	 *            -
//	 * @param tagLimit -
//	 * @param tagorder -
//	 * @return - Patient Readings
//	 */
////	@GET
////	@Path("/getPatientReadingsForBP")
////	public Response getPatientForHealAwayBP(@PathParam("id") String id,
////			@HeaderParam(value = "Authorization") String authorization,
////			@DefaultValue("1y-ago") @QueryParam("starttime") String starttime,
////			@DefaultValue("250") @QueryParam("taglimit") String tagLimit,@DefaultValue("desc") @QueryParam("order") String tagorder);
//	
//	
//	
//	
//	/**
//	 * @param id
//	 *            -
//	 * @param authorization
//	 *            -
//	 * @param starttime
//	 *            -
//	 * @param tagLimit -
//	 * @param tagorder -
//	 * @return -
//	 */
//	@GET
//	@Path("/yearly_data/sensor_id/{id}")
//	public Response getYearlyWindDataPoints(@PathParam("id") String id,
//			@HeaderParam(value = "Authorization") String authorization,
//			@DefaultValue("1y-ago") @QueryParam("starttime") String starttime,
//			@DefaultValue("25") @QueryParam("taglimit") String tagLimit,@DefaultValue("desc") @QueryParam("order") String tagorder);
//
//	/**
//	 * @param id
//	 *            -
//	 * @param authorization
//	 *            -
//	 * @return -
//	 */
//	@GET
//	@Path("/latest_data/sensor_id/{id}")
//	public Response getLatestWindDataPoints(@PathParam("id") String id,
//			@HeaderParam(value = "authorization") String authorization);
//
//
//	/**
//	 * 
//	 * @return List of tags
//	 */
//	@GET
//	@Path("/tags")
//	public Response getWindDataTags();
//
//
//	/**
//	 * @param id
//	 *            -
//	 * @param authorization
//	 *            -
//	 * @return -
//	 */
//
//
//
//}
//
