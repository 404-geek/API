package com.aptus.blackbox.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.JsonProp;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;

@RestController
public class SourceController {
	
	private String mongoUrl;
	
	public SourceController(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}

	@Autowired
	private Credentials credentials;

	private String appname, refresh;
	private UrlObject accessCode, accessToken, requestToken, refreshToken, validateCredentials;
	private List<UrlObject> endPoints;
	private Map<String, String> values;

	@RequestMapping(method = RequestMethod.GET, value = "/authsource/{app}")
	private ResponseEntity<String> source(@PathVariable String app) {
		ResponseEntity<String> ret = null;
		try {
			values = new HashMap<String, String>();
			app = "source/" + app.toUpperCase();
			JsonProp obj = credentials.getSrcObj();
			System.out.println(obj.getName());
			appname = obj.getName();
			values.put("appname", appname);
			refresh = obj.getRefresh();
			accessCode = obj.getAccessCode();
			System.out.println("source : " + accessCode.getUrl());
			accessToken = obj.getAccessToken();
			System.out.println("source : " + accessToken.getUrl());
			requestToken = obj.getRequestToken();
			System.out.println("source : " + requestToken.getUrl());
			refreshToken = obj.getRefreshToken();
			System.out.println("source : " + refreshToken.getUrl());
			validateCredentials = obj.getValidateCredentials();
			System.out.println("source : " + validateCredentials.getUrl());
			endPoints = obj.getEndPoints();
			System.out.println(endPoints.toString());
			if (obj.getSteps().compareTo("TWO") == 0) {
				ret = code(accessCode);
			} else if (obj.getSteps().compareTo("THREE") == 0) {
				ret = Utilities.token(requestToken,values);
				saveValues(ret);
				ret = code(accessCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.source");
		}
		return ret;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/data")
	private ResponseEntity<String> getdata()
	{
		ResponseEntity<String> ret = null;
		//System.out.println(srcname+" "+destname);
		try
		{
			String body="",b1="";
			for(Map.Entry<String,String> mp:credentials.getSrcToken().entrySet()) {
				b1+="{\"key\":\""+String.valueOf(mp.getKey())+"\",\"value\":\""+String.valueOf(mp.getValue())+"\"},";
			}				
			System.out.println(b1);
			if(!b1.isEmpty())
				b1=b1.substring(0,b1.length()-1);
			
			
			if(credentials.isUserExist())
			{
				body= "{\n" + 
						"	\"$set\":{" + 
						"	\"sourceId\":[{\"sourceName\":\""+credentials.getSrcName()+"\",\n" + 
						"				\"credentials\":["+b1+"]\n" + 
						"		}]\n" + 
						"	}\n" + 
						"}";
				if((!credentials.isUsrDestExist())||(!credentials.isUsrSrcExist()))
				postpatchMetaData("{\"$push\":{\"srcdestId\":{\"srcId\":\""+credentials.getSrcName().toLowerCase()+"\",\"destId\":\""+credentials.getDestName().toLowerCase()+"\"}}" + 
						"}","user","PATCH");
				postpatchMetaData(body,"source","PATCHset");
				body= "{\n" + 
						"	\"$set\":{" + 
						"	\"destinationId\":[{\"destinationName\":\"mongodb\",\n" + 
						"				\"credentials\":["+b1+"]\n" + 
						"		}]\n" + 
						"	}\n" + 
						"}";
				postpatchMetaData(body,"destination","PATCHset");
				
			}
			else {
				//userCredentials
				postpatchMetaData("{\"_id\":\""+credentials.getUserId()+"\",\"srcdestId\":[{\"srcId\":\""+credentials.getSrcName().toLowerCase()+"\",\"destId\":\"mongodb\"}]" + 
						"}","user","POST");
				
				//sourceCredentials
				body= "{\n" + 
						"	\"_id\" : \""+credentials.getUserId()+"\",\n" + 
						"	\"sourceId\":[{\"sourceName\":\""+credentials.getSrcName()+"\",\n" + 
						"				\"credentials\":["+b1+"]\n" + 
						"		}\n" + 
						"	]\n" + 
						"}";
				 
				postpatchMetaData(body,"source","POST");
				//destCredentials
				
				body= "{\n" + 
						"	\"_id\" : \""+credentials.getUserId()+"\",\n" + 
						"	\"destinationId\":[{\"destinationName\":\""+credentials.getDestName()+"\",\n" + 
						"				\"credentials\":["+b1+"]\n" + 
						"		}\n" + 
						"	]\n" + 
						"}";
				
				postpatchMetaData(body,"destination","POST");
			}
			ret = data(credentials.getSrcName());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	
    private ResponseEntity<String> data(String appname) {
        ResponseEntity<String> ret = null;
        try {
        	//credentials.getSrcToken().forEach((ob,ob1)->System.out.println(ob+" : "+ob1));
			if (((values!=null)&&(!values.isEmpty())) && (values.get("appname").equals(appname.toUpperCase()))) {
                if (refresh.equals("YES")) {
                    ret = Utilities.token(validateCredentials,values);
                    ret = Utilities.token(refreshToken,values);
                    if (!ret.getStatusCode().is2xxSuccessful()) {
                        ret = source(appname);
                        ret = validateData(validateCredentials, endPoints.get(0));
                    } else {
                        saveValues(ret);
                        ret = validateData(validateCredentials, endPoints.get(0));
                    }
                } else {
                    ret = Utilities.token(validateCredentials,values);
                    if (!ret.getStatusCode().is2xxSuccessful()) {
                        ret = source(appname);
                        ret = validateData(validateCredentials, endPoints.get(0));
                    } else {
                        ret = Utilities.token(endPoints.get(0),values);
                        return ret;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e + "home.data");
        }
        return ret;
    }

    private ResponseEntity<String> validateData(UrlObject valid, UrlObject end) {
        ResponseEntity<String> ret = null;
        try {
            ret = Utilities.token(valid,values);
            if (!ret.getStatusCode().is2xxSuccessful()) {
                System.out.print("contact support....\n");
            } else {
                ret = Utilities.token(end,values);
                return ret;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("source.validatedata");
        }
        return ret;
    }

    private void postpatchMetaData(String body,String type,String method)
	   {
	       try {
	           ResponseEntity<String> out = null;
	           String url="";
	           String appname = "";
	           RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
	           HttpMethod met=null;
	           System.out.println(body);
	           String filter="";
	           if(method.equalsIgnoreCase("POST")) {
	               url ="http://"+mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials";
	               met = HttpMethod.POST;
	           }                
	           else if(method.equalsIgnoreCase("PATCH")) {
	               url ="http://"+mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials/"+credentials.getUserId().toLowerCase();
	               met = HttpMethod.PATCH;
	           }
	           else if(method.equalsIgnoreCase("PATCHset")) {
	        	   if(type.equalsIgnoreCase("source")) {
	        		   appname = credentials.getSrcName();
	        	   }
	        	   else {
	        		   appname = credentials.getDestName();
	        	   }
	        	   url ="http://"+mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials/"+credentials.getUserId().toLowerCase();
	        	   met = HttpMethod.PATCH;
	           }
	           System.out.println(met);
	           HttpHeaders headers =new HttpHeaders();
	           headers.add("Content-Type","application/json") ;
	           headers.add("Cache-Control", "no-cache");
	           HttpEntity<?> httpEntity = new HttpEntity<Object>(body,headers);
	           out = restTemplate.exchange(url, met, httpEntity, String.class,filter);
	           if (out.getStatusCode().is2xxSuccessful()) {
	               credentials.setUserExist(true);
	            }
	       }
	       catch(Exception e) {
	           e.printStackTrace();
	           System.out.println("source.patch");
	       }
	       return;    
	   }
	private ResponseEntity<String> code(UrlObject object) {
		ResponseEntity<String> redirect = null;
		HttpHeaders headers;
		try {
			String url = Utilities.buildUrl(object, values);

			System.out.println(object.getLabel() + " = " + url);

			headers = Utilities.buildHeader(object, values);
			headers.setLocation(URI.create(url));
			HttpEntity<?> httpEntity;
			if (!object.getResponseString().isEmpty()) {
				String body = object.getResponseString();
				redirect = new ResponseEntity<String>(body,headers, HttpStatus.MOVED_PERMANENTLY);
			}else {
				redirect = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
			}			
			System.out.println("redirect =" + redirect);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.code");
		}

		return redirect;
	}

	@RequestMapping(value = "/oauth2/s2")
	@ResponseStatus(value = HttpStatus.OK)
	private ResponseEntity<String> handlefooo(@RequestParam Map<String, String> parameters) {
		values.putAll(parameters);
		System.out.println("token : " + values.keySet() + ":" + values.values());
		System.out.println("parameters : " + parameters.keySet() + ":" + parameters.values());
		ResponseEntity<String> out = null;
		try {
			String url = Utilities.buildUrl(accessToken, values);
			System.out.println(accessToken.getLabel() + " = " + url);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = Utilities.buildHeader(accessToken, values);
			HttpEntity<?> httpEntity;
			if (!accessToken.getResponseString().isEmpty()) {
				httpEntity = new HttpEntity<Object>(accessToken.getResponseString(), headers);
			} else if (!accessToken.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> body = Utilities.buildBody(accessToken, values);
				httpEntity = new HttpEntity<Object>(body, headers);
			} else {
				httpEntity = new HttpEntity<Object>(headers);
			}
			HttpMethod method = (accessToken.getMethod() == "GET") ? HttpMethod.GET : HttpMethod.POST;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			 url =UrlEscapers.urlFragmentEscaper().escape(url);
			out = restTemplate.exchange(URI.create(url), method, httpEntity, String.class);
			saveValues(out);
			credentials.setSrcToken(values);
			out = Utilities.token(validateCredentials,values);
			System.out.println(out.getBody());
			if (!out.getStatusCode().is2xxSuccessful()) {
				System.out.println("invalid access token");
			} else {
				//tick
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.s2");
		}
		return out;
	}

	private void saveValues(ResponseEntity<String> out) {
		if (out.getBody() != null) {
			try {
				values.putAll(new Gson().fromJson(out.getBody(), values.getClass()));
			} catch (Exception e) {
				for (String s : out.getBody().toString().split("&")) {
					System.out.println(s);
					values.put(s.split("=")[0], s.split("=")[1]);
				}
			}
			System.out.println("token : " + values.keySet() + ":" + values.values());
		}
	}
}