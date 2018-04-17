package com.aptus.blackbox;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aptus.blackbox.DomainObjects.UrlObject;
import com.aptus.blackbox.DomainObjects.objects;

import sun.misc.BASE64Encoder;

public abstract class Authorization {
	
	protected String getAuthorization() {
		
	}
	
	protected String timestamp() {
		String time = String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
		return time.substring(0, time.length()-3);
	}
	protected String signature(UrlObject object,Map<String,String> params, Map<String, String> values,String message) throws Exception {
		String consumerSecret=null;
		for(objects obj:object.getSignature()){
			if(obj.getKey().toLowerCase().indexOf("secret")!=-1) {
				consumerSecret = obj.getValue().toString();
			}
		}
		String baseSignature = object.getMethod().toString()+"&"+
		encode(object.getUrl())+"&";
		String parameter="",value="";
		if(object.getParams()!=null) {
			for(objects ob:object.getParams()) {
				if(ob.getValue().equalsIgnoreCase("codevalue")) {
					value=values.get(ob.getKey());
				}
				else {
					value = ob.getValue();
				}
				params.put(encode(ob.getKey()), encode(value));
			}
		}
		if(object.getResponseBody()!=null) {
			for(objects ob:object.getResponseBody()) {
				params.put(encode(ob.getKey()), encode(ob.getValue()));
			}
		}
		for(String key:params.keySet()) {
			parameter+=key+"="+params.get(key)+"&";
		}
		parameter = parameter.substring(0, parameter.length()-1);
		parameter = baseSignature+encode(parameter);
		consumerSecret=encode(consumerSecret)+"&";
		if((object.getLabel().toLowerCase().indexOf("token")!=-1)&&(values.containsKey("oauth_token")))
			consumerSecret+=values.get("oauth_token");
		System.out.println(message+" "+parameter+"\n"+consumerSecret);
		return generateSignature(parameter,consumerSecret);
	}
	protected String encode(String value) {
		String encoded = "";
		try {
			encoded = URLEncoder.encode(value, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String sb = "";
		char focus;
		for (int i = 0; i < encoded.length(); i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				sb += "%2A";
			} else if (focus == '+') {
				sb += "%20";
			} else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
					&& encoded.charAt(i + 2) == 'E') {
				sb += '~';
				i += 2;
			} else {
				sb += focus;
			}
		}
		return sb.toString();
	}

	protected byte[] calculateRFC2104HMAC(String data, String key,String Algorithm)
		throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
	{
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), Algorithm);
		Mac mac = Mac.getInstance(Algorithm);
		mac.init(signingKey);
		return mac.doFinal(data.getBytes());
	}
	protected String generateSignature(String data,String key) throws Exception {
		String hmac = new BASE64Encoder().encode(calculateRFC2104HMAC(data,key,"HmacSHA1"));
		return hmac;
	}
}
