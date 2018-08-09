package com.aptus.blackbox;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.models.objects;



public abstract class SourceAuthorization 
{
//	
//	
//	protected String getAuthorization(UrlObject token, List<objects> list, Map<String, String> credentials,String message) {
//		String s="";
//		try {
//			switch(token.getAuthorization()) {
//			case"BASIC":
//				String username="",password="";
//				for(objects x:list) {
//					if(x.getKey().equalsIgnoreCase("username"))
//						username=x.getValue();
//					else if(x.getKey().equalsIgnoreCase("password"))
//						password=x.getValue();
//				}
//				s+=new BASE64Encoder().encode((username+":"+password).getBytes());
//				s = s.replaceAll("\n", "");
//				System.out.println(s.indexOf('\n')+" "+username+" "+password);
//				s+=",";
//				break;
//			case"Auth1":
//				for(objects x:list)
//				{
//					if(x.getKey().indexOf("timestamp")!=-1) {
//						credentials.put(x.getKey(), timestamp());
//					}
//					if(x.getKey().indexOf("signature")!=-1) {
//						
//						credentials.put(x.getKey(),signature(token,credentials,message));
//						System.out.println(credentials.get(x.getKey()).toString());
//					}
//					if(x.getValue().equals("codeValue"))
//						s+= x.getKey() + "=" + encode(credentials.get(x.getKey()))+",";
//					else
//						s+= x.getKey() + "=" + encode(x.getValue())+",";
//				}
//				break;
//			case"Auth2":
//				objects x = list.get(0);
//				s+= encode(credentials.get(x.getKey()))+",";
//				break;
//			}		
//			return s;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	protected String timestamp() {
//		String time = String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
//		return time.substring(0, time.length()-3);
//	}
//	protected String signature(UrlObject object, Map<String, String> values,String message) throws Exception {
//		String consumerSecret=null;
//		Map<String,String> params = new HashMap<String, String>();
//		for(objects obj:object.getSignature()){
//			if(obj.getKey().toLowerCase().indexOf("secret")!=-1) {
//				consumerSecret = obj.getValue().toString();
//			}
//		}
//		String baseSignature = object.getMethod().toString()+"&"+
//		encode(object.getUrl())+"&";
//		String parameter="",value="";
//		if(object.getParams()!=null) {
//			for(objects ob:object.getParams()) {
//				if(ob.getValue().equalsIgnoreCase("codevalue")) {
//					value=values.get(ob.getKey());
//				}
//				else {
//					value = ob.getValue();
//				}
//				params.put(encode(ob.getKey()), encode(value));
//			}
//		}
//		if(object.getResponseBody()!=null) {
//			for(objects ob:object.getResponseBody()) {
//				params.put(encode(ob.getKey()), encode(ob.getValue()));
//			}
//		}
//		for(String key:params.keySet()) {
//			parameter+=key+"="+params.get(key)+"&";
//		}
//		if(parameter.length()!=0) {
//			parameter = parameter.substring(0, parameter.length()-1);
//			parameter = baseSignature+encode(parameter);
//		}
//		
//		consumerSecret=encode(consumerSecret)+"&";
//		if((object.getLabel().toLowerCase().indexOf("token")!=-1)&&(values.containsKey("oauth_token")))
//			consumerSecret+=values.get("oauth_token");
//		System.out.println(message+" "+parameter+"\n"+consumerSecret);
//		return generateSignature(parameter,consumerSecret);
//	}
//	protected String encode(String value) {
//		String encoded = "";
//		try {
//			encoded = URLEncoder.encode(value, "UTF-8");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		String sb = "";
//		char focus;
//		for (int i = 0; i < encoded.length(); i++) {
//			focus = encoded.charAt(i);
//			if (focus == '*') {
//				sb += "%2A";
//			} else if (focus == '+') {
//				sb += "%20";
//			} else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
//					&& encoded.charAt(i + 2) == 'E') {
//				sb += '~';
//				i += 2;
//			} else {
//				sb += focus;
//			}
//		}
//		return sb.toString();
//	}
//
//	protected byte[] calculateRFC2104HMAC(String data, String key,String Algorithm)
//		throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
//	{
//		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), Algorithm);
//		Mac mac = Mac.getInstance(Algorithm);
//		mac.init(signingKey);
//		return mac.doFinal(data.getBytes());
//	}
//	protected String generateSignature(String data,String key) throws Exception {
//		String hmac = new BASE64Encoder().encode(calculateRFC2104HMAC(data,key,"HmacSHA1"));
//		return hmac;
//	}
}
