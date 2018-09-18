package com.aptus.blackbox.controller;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;

import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.dataServices.SourceDestinationList;
import com.aptus.blackbox.dataServices.SrcDestListService;
import com.aptus.blackbox.dataServices.UserConnectorService;
import com.aptus.blackbox.dataServices.UserService;
import com.aptus.blackbox.dataServices.WebSocketService;
import com.aptus.blackbox.datamodels.Destinations;
import com.aptus.blackbox.datamodels.Sources;
import com.aptus.blackbox.datamodels.UserConnectors;
import com.aptus.blackbox.datamodels.UserInfo;
import com.aptus.blackbox.datamodels.VerificationToken;
import com.aptus.blackbox.event.OnRegistrationCompleteEvent;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.models.ResponseObject;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.security.ExceptionHandling;
import com.aptus.blackbox.utils.Constants;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/*
 * "pagination":[
					{"key":"info.page","to-add":"page","value":"inc"},
 					{"key":"info.page","to-add":"","value":"url"}, 
					{"key":"info.page","to-add":"page","value":"append"}, 
					{"key":"info.page","to-add":"page","value":"self-inc"}, 
 					{"key":"info.page","to-add":"page","value":"self-inc"} 
				]
 */

@Controller
public class home extends RESTFetch {

	@Autowired
	private SrcDestListService srcdestlistService;
	@Autowired
	private Credentials credentials;
	@Autowired
	private ApplicationCredentials applicationCredentials;

	@Autowired
	private UserService userService;
	@Autowired
	private UserConnectorService userConnectorService;
	@Autowired
	private SourceDestinationList sourceDestinationService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private WebSocketService socketService;
	@Autowired
	private Environment env;

	private SimpMessagingTemplate template;
	final Logger logger = LogManager.getLogger(home.class.getPackage());

	@Autowired
	public home(SimpMessagingTemplate template) {

		System.out.println("SimpleMessaging Constructer called");

		/* Initialize messaging template */
		this.template = template;
	}

	// public void fun() {
	// Thread th = new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// while (true) {
	// System.out.println("hello");
	// template.convertAndSend("/chat",
	// "{\"code\":\"200\",\"message\":\"Statistics data
	// updated\",\"data\":{\"DatasourcesCreated\":7,\"FilesDownloaded\":0,\"RowsFetced\":1631,\"DatasourcesScheduled\":2}}");
	// System.out.println("hello");
	// try {
	// Thread.sleep(10000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// });
	// th.start();
	// }

	@RequestMapping(value = "/login")
	private ResponseEntity<String> login(@RequestParam("email") String email, @RequestParam("password") String password,
			HttpSession session) {
		HttpHeaders headers = new HttpHeaders();

		logger.info("INFO MSG");
		logger.debug("debug MSG");
		logger.error("error MSG");
		// ThreadContext.clearAll();
		logger.warn("warn MSG");
		logger.trace("trac  msg");

		System.out.println("/login session" + session.getId());
		JsonObject response = new JsonObject();

		try {
			System.out.println(InetAddress.getLocalHost().getHostName());
			System.out.println(InetAddress.getLocalHost().getHostAddress());
			if (userService.userExist(email) == null) {
				response = new ResponseObject().Response(Constants.USER_NOT_FOUND_CODE, Constants.USER_NOT_FOUND_MSG,
						email);
			}

			else if (!userService.userValid(email, password)) {
				response = new ResponseObject().Response(Constants.INVALID_CREDENTIALS_CODE,
						Constants.INVALID_CREDENTIALS_MSG, email);
			}

			else {

				credentials.setUserId(email);
				applicationCredentials.setSessionId(email, session.getId());

				getConnectionIds(session);
				response = new ResponseObject().Response(Constants.SUCCESS_CODE, Constants.SUCCESS_MSG, email);
				//
				// ThreadContext.clearAll();
				// ThreadContext.put("id", "192.168.21.9");
				// logger.info("User success login");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response.toString());
	}

	@RequestMapping(value = "/activeUsers")
	private ResponseEntity<String> getActiveUsers() {

		ThreadContext.put("id", "poupopuop");
		logger.error("Helllo worls");
		JsonObject jobj;
		try {
			jobj = new JsonObject();
			System.out.println("Users Currently Active");
			applicationCredentials.getSessionId().forEach((k, v) -> {
				System.out.println(k + ":" + v);
				jobj.addProperty(k, v);
			});
			return ResponseEntity.status(HttpStatus.OK).headers(null).body(jobj.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/*
	 * Calls upon use clicking registration confirmation link Input :: Verification
	 * token
	 */

	@RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
	public String confirmRegistration(WebRequest request, @RequestParam("token") String token) {

		Locale locale = request.getLocale();

		VerificationToken verificationToken = userService.getVerificationToken(token);
		if (verificationToken == null) {
			// String message = messages.getMessage("auth.message.invalidToken", null,
			// locale);
			return "redirect:/badUser.html?lang=" + locale.getLanguage() + "&msg=invalidtoken";
		} else if (!verificationToken.isValid()) {
			return "redirect:/badUser.html?lang=" + locale.getLanguage() + "&msg=tokenalreadyused";
		}

		String userId = verificationToken.getEmail();
		Calendar cal = Calendar.getInstance();

		if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			// String messageValue = messages.getMessage("auth.message.expired", null,
			// locale);
			return "redirect:/badUser.html?lang=" + locale.getLanguage() + "&msg=tokenexpired";
		}

		/* Register Confirmed User */
		userService.registerUser(token, userId);

		return "redirect:/index.html?lang=" + request.getLocale().getLanguage();

	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	private ResponseEntity<String> signup(WebRequest request, @RequestBody String data)// @RequestBody UserInfo user)
	{

		HttpHeaders headers = new HttpHeaders();
		JsonObject response = new JsonObject();

		try {
			/* Parse JsonResponse string to UserInfo type */
			UserInfo user = new Gson().fromJson(data, UserInfo.class);
			System.out.println(InetAddress.getLocalHost().getHostName());
			System.out.println(InetAddress.getLocalHost().getHostAddress());

			UserInfo userInfo = userService.userExist(user.getEmail());

			/* Checks userEmail already registered */
			if (userInfo != null) {

				if (userInfo.isEnabled()) {
					response = new ResponseObject().Response(Constants.USER_EXIST_CODE, Constants.USER_EXIST_MSG,
							user.getEmail());
				} else {
					response = new ResponseObject().Response(Constants.EMAIL_NOT_VERIFIED_CODE,
							Constants.EMAIL_NOT_VERIFIED_MSG, user.getEmail());
				}

			} else {

				/* Insert User record to UserInfo Collection */
				System.out.println("New User Email Registration" + user.getEmail());
				userService.createUser(user);
				response = new ResponseObject().Response(Constants.SUCCESS_CODE, Constants.SUCCESS_MSG,
						user.getEmail());

				/* Publish event for sending confirmation mail */

				String protocol = env.getProperty("uapi.mail.protocol");
				String hostname = InetAddress.getLocalHost().getHostName();
				String port = env.getProperty("server.port");
				Locale locale = request.getLocale();

				String appUrl = protocol + "://" + hostname + ":" + port; // request.getContextPath();

				OnRegistrationCompleteEvent event = new OnRegistrationCompleteEvent(user, locale, appUrl);
				applicationEventPublisher.publishEvent(event);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response.toString());
	}

	@RequestMapping(value = "/update")
	private ResponseEntity<String> update(@RequestBody UserInfo user) {
		HttpHeaders headers = new HttpHeaders();
		JsonObject response = null;
		if (userService.userExist(user.getEmail()) != null) {
			System.out.println("User ID Exists " + user.getEmail());
			response = new ResponseObject().Response(Constants.USER_EXIST_CODE, Constants.USER_EXIST_MSG,
					user.getEmail());
		} else {

			System.out.println("User ID Not Exists " + user.getEmail());
			// userService.updateUser(user);
			response = new ResponseObject().Response(Constants.SUCCESS_CODE, Constants.SUCCESS_MSG, user.getEmail());
		}
		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response.toString());
	}

	/*
	 * Input:user_id Takes user_id as input, checks if user already exists and
	 * stores true/false accordingly in credentials. Return type: void
	 */
	/*
	 * private ResponseEntity<String> existUser(String userId, String type) {
	 * ResponseEntity<String> out = null; try { boolean ret = false;
	 * credentials.setUserId(userId);
	 * 
	 * System.out.println("LOGGGGGIIINNN"); ThreadContext.put("id",
	 * "192.168.21.9loginn"); logger.info("asfdsadasfdf");
	 * 
	 * RestTemplate restTemplate = new RestTemplate(); //
	 * restTemplate.getMessageConverters().add(0, new //
	 * StringHttpMessageConverter(Charset.forName("UTF-8"))); String filter =
	 * "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}"; String url;
	 * url = config.getMongoUrl() + "/credentials/" + type + "?filter=" + filter;
	 * System.out.println(url); URI uri =
	 * UriComponentsBuilder.fromUriString(url).build().encode().toUri(); HttpHeaders
	 * headers = new HttpHeaders(); //
	 * headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ="); //
	 * headers.add("Cache-Control", "no-cache"); // headers.add("Authorization",
	 * "Basic YTph"); // headers.add("access-control-allow-origin",
	 * config.getRootUrl()); // headers.add("access-control-allow-credentials",
	 * "true"); HttpEntity<?> httpEntity = new HttpEntity<Object>(headers); out =
	 * restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
	 * System.out.println("inside existuser");
	 * 
	 * JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
	 * JsonObject jobj = jelem.getAsJsonObject(); ret =
	 * jobj.get("_returned").getAsInt() == 0 ? false : true; if
	 * (type.equalsIgnoreCase("usercredentials")) credentials.setUserExist(ret);
	 * 
	 * // System.out.println(ret); JsonObject respBody = new JsonObject(); if (ret)
	 * { respBody.addProperty("code", "200"); respBody.addProperty("message",
	 * "User found"); return
	 * ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); } else {
	 * respBody.addProperty("code", "404"); respBody.addProperty("message",
	 * "User not found"); return
	 * ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); } }
	 * 
	 * catch (HttpStatusCodeException e) {
	 * 
	 * System.out.println("Inside exituser catch"); e.getStatusCode();
	 * 
	 * ExceptionHandling exceptionhandling = new ExceptionHandling(); out =
	 * exceptionhandling.clientException(e); // System.out.println(out.getBody());
	 * // System.out.println(out.getStatusCode().toString()); return out;//
	 * ResponseEntity.status(HttpStatus.OK).body(null);
	 * 
	 * }
	 * 
	 * }
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/getsrcdest")
	private ResponseEntity<String> getSrcDest() {
		// System.out.println("/getsrcdest session" + session.getId());
		ResponseEntity<String> s = null;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		try {
			// if (Utilities.isSessionValid(session, applicationCredentials,
			// credentials.getUserId())) {
			// String response = srcdestlistService.getSrcDestList();
			List<String> categories = sourceDestinationService.getCategories("refIndType");
			List<Sources> sources = sourceDestinationService.getSourceList();
			List<Destinations> destinations = sourceDestinationService.getDestinationList();

			JsonObject respBody = new JsonObject();
			Gson gson = new Gson();

			respBody.add("sources", gson.toJsonTree(sources));
			respBody.add("destinations", gson.toJsonTree(destinations));
			respBody.add("categories", gson.toJsonTree(categories));

			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			// } else {
			// // session.invalidate();
			// System.out.println("Session expired!");
			// JsonObject respBody = new JsonObject();
			// respBody.addProperty("message", "Sorry! Your session has expired");
			// respBody.addProperty("status", "33");
			// return
			// ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			// }
		} catch (HttpStatusCodeException e) {

			System.out.println("Inside getsrcdest catch");
			ResponseEntity<String> out = null;
			e.getStatusCode();

			ExceptionHandling exceptionhandling = new ExceptionHandling();
			out = exceptionhandling.clientException(e);

			// System.out.println(out.getStatusCode().toString());
			return out; // ThreadContext.clearAll();
			// logger.warn("warn MSG");
			// ResponseEntity.status(HttpStatus.OK).body(null);

		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	@RequestMapping(value = "/filterendpoints")
	private ResponseEntity<String> filterendpoints(HttpSession session) {

		System.out.println("INSIDE /filterendpoints");
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Cache-Control", "no-cache");
		// headers.add("access-control-allow-origin", config.getRootUrl());
		// headers.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(session, applicationCredentials, credentials.getUserId())) {

				JsonObject jobj = new JsonObject();
				JsonArray catagory = new JsonArray();
				JsonObject temp = new JsonObject();
				JsonArray endpoints = new JsonArray();
				boolean flag1 = false;
				for (UrlObject obj : credentials.getSrcObj().getDataEndPoints()) {
					if (obj.getCatagory().equalsIgnoreCase("others")) {
						endpoints.add(obj.getLabel());
						flag1 = true;
					}
				}
				for (UrlObject obj : credentials.getSrcObj().getInfoEndpoints()) {
					endpoints.add(obj.getLabel());
					flag1 = true;
				}
				if (flag1 == true) {
					temp.add("value", endpoints);
					temp.addProperty("name", "others");
					temp.addProperty("key", "Others");
					catagory.add(temp);
				}
				List<String> list;
				Gson gson = new Gson();
				for (UrlObject obj : credentials.getSrcObj().getImplicitEndpoints()) {
					temp = new JsonObject();
					ResponseEntity<String> data = Utilities.token(obj, credentials.getSrcToken(), "filteredEndpoints");
					list = new ArrayList<String>();
					list = Utilities.checkByPath(obj.getData().split("::"), 0,
							new Gson().fromJson(data.getBody(), JsonElement.class), list);
					temp.add("value", gson.fromJson(gson.toJson(list), JsonArray.class));
					temp.addProperty("name", obj.getCatagory());
					temp.addProperty("key", obj.getLabel());
					catagory.add(temp);
				}
				jobj.add("endpoints", catagory);
				jobj.addProperty("status", "200");
				System.out.println(jobj.toString());
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobj.toString());
			} else {
				session.invalidate();
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	/*
	 * @RequestMapping(value = "/getconnectionidsOld") private
	 * ResponseEntity<String> getConnectionIdsOld(HttpSession session) {
	 * System.out.println(applicationCredentials.getSessionId(credentials.getUserId(
	 * )) + "INSIDE /getconnectionids:" + session.getId()); String dataSource =
	 * null; HttpHeaders headers = new HttpHeaders(); //
	 * headers.add("Cache-Control", "no-cache"); //
	 * headers.add("access-control-allow-origin", config.getRootUrl()); //
	 * headers.add("access-control-allow-credentials", "true"); //
	 * headers.add("Authorization", "Basic YTph"); try {
	 * 
	 * if (Utilities.isSessionValid(session, applicationCredentials,
	 * credentials.getUserId())) { ResponseEntity<String> out = null; RestTemplate
	 * restTemplate = new RestTemplate(); //
	 * restTemplate.getMessageConverters().add(0, new //
	 * StringHttpMessageConverter(Charset.forName("UTF-8"))); String url =
	 * config.getMongoUrl() + "/credentials/userCredentials/" +
	 * credentials.getUserId(); URI uri =
	 * UriComponentsBuilder.fromUriString(url).build().encode().toUri(); HttpHeaders
	 * header = new HttpHeaders(); //
	 * headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
	 * header.add("Cache-Control", "no-cache"); HttpEntity<?> httpEntity = new
	 * HttpEntity<Object>(header); out = restTemplate.exchange(uri, HttpMethod.GET,
	 * httpEntity, String.class);
	 * 
	 * Gson gson = new Gson(); JsonObject respBody = new JsonObject();
	 * respBody.addProperty("status", "200"); respBody.add("data",
	 * gson.fromJson(out.getBody(), JsonElement.class));
	 * 
	 * ConnObj conObj = new ConnObj(); JsonElement data =
	 * gson.fromJson(out.getBody(), JsonElement.class); JsonArray srcdestId =
	 * data.getAsJsonObject().get("srcdestId").getAsJsonArray(); for (JsonElement
	 * ele : srcdestId) { conObj = gson.fromJson(ele, ConnObj.class);
	 * credentials.setConnectionIds(conObj.getConnectionId(), conObj); }
	 * 
	 * url = config.getMongoUrl() + "/copy_credentials/SrcDstlist/srcdestlist"; uri
	 * = UriComponentsBuilder.fromUriString(url).build().encode().toUri(); out =
	 * restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
	 * respBody.add("images", gson.fromJson(out.getBody(), JsonElement.class));
	 * 
	 * return
	 * ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString(
	 * )); } else { session.invalidate(); System.out.println("Session expired!");
	 * JsonObject respBody = new JsonObject(); respBody.addProperty("message",
	 * "Sorry! Your session has expired"); respBody.addProperty("status", "33");
	 * return
	 * ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody
	 * .toString()); } } catch (HttpClientErrorException e) {
	 * System.out.println(e.getMessage()); if (e.getMessage().startsWith("4")) {
	 * JsonObject respBody = new JsonObject(); respBody.addProperty("data", "null");
	 * respBody.addProperty("status", "200"); return
	 * ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString(
	 * )); } } catch (Exception e) { e.printStackTrace(); } return
	 * ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null); }
	 */
	@RequestMapping(value = "/getconnectionids")
	private ResponseEntity<String> getConnectionIds(HttpSession session) {
		String dataSource = null;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Cache-Control", "no-cache");
		// headers.add("access-control-allow-origin", config.getRootUrl());
		// headers.add("access-control-allow-credentials", "true");
		// headers.add("Authorization", "Basic YTph");
		try {

			if (Utilities.isSessionValid(session, applicationCredentials, credentials.getUserId())) {
				ResponseEntity<String> out = null;

				// restTemplate.getMessageConverters().add(0, new
				// StringHttpMessageConverter(Charset.forName("UTF-8")));

				UserConnectors connObj = userConnectorService.getConnectorObjects(credentials.getUserId());

				for (ConnObj ele : connObj.getConnectorObjs())
					credentials.setConnectionIds(ele.getConnectionId(), ele);

				JsonObject respBody = new JsonObject();
				respBody.addProperty("status", "200");
				respBody.add("data",
						new Gson().fromJson(new Gson().toJson(connObj, UserConnectors.class), JsonElement.class));
				respBody.add("images", new Gson().fromJson(srcdestlistService.getSrcDestList(), JsonElement.class));

				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			} else {
				// session.invalidate();
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (HttpClientErrorException e) {
			System.out.println(e.getMessage());
			if (e.getMessage().startsWith("4")) {
				JsonObject respBody = new JsonObject();
				respBody.addProperty("data", "null");
				respBody.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

}