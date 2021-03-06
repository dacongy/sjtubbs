﻿package com.tyt.bbs.utils;
/**
 * 
 * @author SJTU Tanyeteng
 * email:tank.tyt@gmail.com
 * No Business Use is Allowed
 * 2011-2-14
 */
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Entity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.ToggleButton;

public class Net {
	private static final String TAG = "Net";
	private static Net net;
	private String cookie = null;
	public static Net getInstance() 
	{
		if (net == null) 
		{
			net = new Net();
		}
		return net;
	}


	public String get(String URL) throws Exception 
	{
		String resultString;
		HttpGet sourceaddr= new HttpGet(URL);
		HttpClient client = CustomHttpClient.getHttpClient();
		try {
			HttpResponse httpResponse = client.execute(sourceaddr);
			if (httpResponse.getStatusLine().getStatusCode()==200) 
			{
				resultString = entityToString(httpResponse.getEntity());

			}
			else {
				throw new Exception("can't connect the network");
			}
			return resultString.toString();
		}catch (Exception e) {
			throw e;
		}
	}

	private String entityToString(HttpEntity  entity) throws Exception{
		try {
		String charset= entity.getContentType().getValue();
		Log.v(TAG, charset);
		if(charset.contains("charset")){
			charset = charset.substring(charset.indexOf("charset")+8);
			if(charset.equalsIgnoreCase("utf"))

					return  EntityUtils.toString(entity,"UTF-8");

			else if(charset.equalsIgnoreCase("gbk"))
				return  EntityUtils.toString(entity,"GBK");	
			else if(charset.equalsIgnoreCase("gb2312"))
				return  EntityUtils.toString(entity,"gb2312");	
			else
				return readstream(entity.getContent());	
		}
		else
			return readstream(entity.getContent());	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return readstream(entity.getContent());	
		} 
	}

	public String getWithCookie(String URL) throws Exception 
	{
		String resultString;
		HttpGet httpget= new HttpGet(URL);
		DefaultHttpClient httpClient= CustomHttpClient.getHttpClient();
		if(getCookie()!=null) {
			httpget.addHeader("Cookie",getCookie());
			httpget.addHeader("Connection","keep-alive");	
			//			Log.v("客户端Cookie", "-------"+getCookie()+"---------");  
		}

		try {
			//		
			HttpResponse httpResponse = httpClient.execute(httpget);
			if (httpResponse.getStatusLine().getStatusCode()==200) 
			{
				resultString = entityToString(httpResponse.getEntity());
			}
			else {
				throw new Exception("can't connect the network");
			}
			return resultString.toString();
		}catch (Exception e) {
			throw e;
		}
	}


	public String GMTString() {
		Date date= new Date();
		SimpleDateFormat format1 = new SimpleDateFormat("EEE, d-MMM", Locale.US); //$NON-NLS-1$
		SimpleDateFormat format2 = new SimpleDateFormat(
				" HH:mm:ss 'GMT'", Locale.US); //$NON-NLS-1$
		TimeZone gmtZone = TimeZone.getTimeZone("GMT"); //$NON-NLS-1$
		format1.setTimeZone(gmtZone);
		format2.setTimeZone(gmtZone);
		GregorianCalendar gc = new GregorianCalendar(gmtZone);
		gc.setTimeInMillis(java.lang.System.currentTimeMillis());
		return format1.format(date) +"-"+gc.get(Calendar.YEAR) + format2.format(date);
	}

	public boolean getMsgMarkread(String URL) throws Exception 
	{
		HttpGet httpget= new HttpGet(URL);
		DefaultHttpClient httpClient= CustomHttpClient.getHttpClient();
		if(getCookie()!=null) {
			httpget.addHeader("Cookie",getCookie());
			httpget.addHeader("Connection","keep-alive");
			httpget.addHeader("Cache-Control","max-age=0");
			httpget.addHeader("If-Modified-Since",GMTString());
			httpget.addHeader("Referer",Property.Base_URL+"/bbsnewmail");
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpget);
			if (httpResponse.getStatusLine().getStatusCode()==200) 
			{
				//				Log.v("Mark Read ", readstream(httpResponse.getEntity().getContent()));
				return true;				
			}else {
				throw new Exception("can't connect the network");
			}

		}catch (Exception e) {
			throw e;
		}
	}

	public String get(String URL,List <NameValuePair> params) throws Exception  {

		String resultString;
		StringBuilder path=new StringBuilder(URL);
		path.append('?');

		for(int index=0; index < params.size(); index++){
			NameValuePair temp=params.get(index);
			path.append(temp.getName())
			.append('=')
			.append(temp.getValue())
			.append('&');
		}
		path.deleteCharAt(path.length()-1);
		Log.i("get Path", "*******************************");
		Log.i("get Path", path.toString());
		Log.i("get Path", "*******************************");

		HttpGet sourceaddr= new HttpGet(path.toString());
		HttpClient client= CustomHttpClient.getHttpClient();
		if(getCookie()!=null) {
			sourceaddr.addHeader("Cookie",getCookie());
			sourceaddr.addHeader("Connection","keep-alive");	
			//			Log.v("客户端Cookie", "-------"+getCookie()+"---------");  
		}
		try {
			HttpResponse httpResponse = client.execute(sourceaddr);

			if (httpResponse.getStatusLine().getStatusCode()==200) 
			{
				resultString = entityToString(httpResponse.getEntity());
				Log.i("get resultString", "**********************************");
				Log.i("get resultString", resultString);
				Log.i("get resultString", "**********************************");

			}
			else {
				throw new Exception("can't connect the network");
			}
			return resultString;
		}catch (Exception e) {
			throw e;
		}
	}



	public String loginPost(String URL,List <NameValuePair> params) throws Exception 
	{
		String resultString;
		try {
			HttpPost httpRequest = new HttpPost(URL);
			DefaultHttpClient client= CustomHttpClient.getHttpClient();
			httpRequest.addHeader("Connection","keep-alive");	
			httpRequest.setEntity(new UrlEncodedFormEntity(params, "GB2312"));  
			HttpResponse httpResponse = client.execute(httpRequest);
			if(httpResponse.getStatusLine().getStatusCode() == 200)    
			{   
				resultString = entityToString(httpResponse.getEntity());
				fetchCookie(client);

			}else {
				throw new Exception("can't connect the network");
			}
			return resultString;

		}catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 上传文件
	 * @param URL	
	 * 			上传地址
	 * @param params
	 * 			上传参数
	 * @return
	 * 			响应正文Httprequest
	 * @throws Exception
	 */
	public String postFile(String URL,List <NameValuePair> params) throws Exception 
	{
		//		PostMethod   filePost=new PostMethod();

		String resultString="";
		try {
			HttpPost httpRequest = new HttpPost(URL);
			DefaultHttpClient client= CustomHttpClient.getHttpClient();
			MultipartEntity mpEntity = new MultipartEntity();

			if(getCookie()!=null) {
				httpRequest.addHeader("Cookie",getCookie());
				httpRequest.addHeader("Connection","keep-alive");	
				Log.v("客户端Cookie", "-------"+getCookie()+"---------");  
			}
			for(int index=0; index < params.size(); index++)
			{          
				if(params.get(index).getName().equalsIgnoreCase("up")) {                
					// If the key equals to "image", we use FileBody to transfer the data		
					mpEntity.addPart(params.get(index).getName(), new FileBody(new File(params.get(index).getValue())));           
				} 
				else {  
					// Normal string data                 
					mpEntity.addPart(params.get(index).getName(), new StringBody(params.get(index).getValue()));             
				}  

			}  
			//			Log.v("Post request", EntityUtils.toString(mpEntity));
			httpRequest.setEntity(mpEntity) ; 

			//			httpRequest.setEntity(new UrlEncodedFormEntity(params, "GB2312"));  
			HttpResponse httpResponse = client.execute(httpRequest);
			if(httpResponse.getStatusLine().getStatusCode() == 200)    
			{   
				resultString = entityToString(httpResponse.getEntity());
				Log.v("Post", resultString);
				if(!resultString.contains("ERROR")){
					fetchCookie(client);

				}else{
					throw new Exception("cookie is old,you need a new one.");
				}

			}else {
				throw new Exception("can't connect the network");
			}
			return resultString;
		}catch (Exception e) {
			throw e;
		}
	}


	public String postFile(List <NameValuePair> params,File file) throws Exception 
	{
		String resultString="";
		try {
			HttpPost httpRequest = new HttpPost("https://bbs.sjtu.edu.cn/bbsdoupload");
			DefaultHttpClient client= CustomHttpClient.getHttpClient();
			MultipartEntity mpEntity = new MultipartEntity();

			if(getCookie()!=null) {
				httpRequest.addHeader("Cookie",getCookie());
				httpRequest.addHeader("Connection","keep-alive");	
			}
			for(int index=0; index < params.size(); index++)
			{          
				if(params.get(index).getName().equalsIgnoreCase("up")) {                
					// If the key equals to "image", we use FileBody to transfer the data		
					mpEntity.addPart(params.get(index).getName(),  new FileBody(file));           
				} 
				else {  
					// Normal string data                 
					mpEntity.addPart(params.get(index).getName(), new StringBody(params.get(index).getValue()));             
				}  

			}  
			//			Log.v("Post request", EntityUtils.toString(mpEntity));
			httpRequest.setEntity(mpEntity) ; 
			HttpResponse httpResponse = client.execute(httpRequest);
			if(httpResponse.getStatusLine().getStatusCode() == 200)    
			{   
				resultString = entityToString(httpResponse.getEntity());
				Log.v(TAG, resultString);
				if(!resultString.contains("ERROR")){
					fetchCookie(client);
				}else{
					throw new Exception("cookie is old,you need a new one.");
				}

			}else {
				throw new Exception("can't connect the network");
			}
			return resultString;
		}catch (Exception e) {
			throw e;
		}
	}

	public String fetchCookie(DefaultHttpClient client){
		List<Cookie> cookies = client.getCookieStore().getCookies();  
		if (cookies.isEmpty()) {  
		} else {       
			setCookie("");
			for (int i = 0; i < cookies.size(); i++ ) { 
				setCookie(getCookie()+cookies.get(i).getName()+ "="+ cookies.get(i).getValue());
				if(i<2)
					setCookie(getCookie()+";");
			}
		}
		return getCookie();
	}

	public String post(String URL,String datas) throws Exception{

		String resultString;

		try {
			HttpPost httpRequest = new HttpPost(URL);
			DefaultHttpClient client= CustomHttpClient.getHttpClient();
			if(getCookie()!=null) {
				httpRequest.addHeader("Cookie",getCookie());
				httpRequest.addHeader("Connection","keep-alive");	
				Log.v("客户端Cookie", "-------"+getCookie()+"---------");  
			}
			httpRequest.setEntity(new StringEntity(datas));  
			HttpResponse httpResponse = client.execute(httpRequest);
			if(httpResponse.getStatusLine().getStatusCode() == 200)    
			{   
				resultString = entityToString(httpResponse.getEntity());
				Log.v("Post", resultString);
				if(!resultString.contains("ERROR")){
					fetchCookie(client);
				}else{
					throw new Exception("cookie is old,you need a new one.");
				}

			}else {
				throw new Exception("can't connect the network");
			}
			return resultString;
		}catch (Exception e) {
			throw e;
		}

	}

	public String post(String URL,List <NameValuePair> params) throws Exception 
	{
		String resultString;
		try {
			HttpPost httpRequest = new HttpPost(URL);
			DefaultHttpClient client= CustomHttpClient.getHttpClient();
			if(getCookie()!=null) {
				httpRequest.addHeader("Cookie",getCookie());
				httpRequest.addHeader("Connection","keep-alive");	
				Log.v("客户端Cookie", "-------"+getCookie()+"---------");  
			}

			httpRequest.setEntity(new UrlEncodedFormEntity(params, "GB2312"));  
			HttpResponse httpResponse = client.execute(httpRequest);
			if(httpResponse.getStatusLine().getStatusCode() == 200)    
			{   
				resultString = entityToString(httpResponse.getEntity());
				Log.v("Post", resultString);
				if(!resultString.contains("ERROR")){
					fetchCookie(client);
				}else{
					throw new Exception("cookie is old,you need a new one.");
				}

			}else {
				throw new Exception("can't connect the network");
			}
			return resultString;
		}catch (Exception e) {
			throw e;
		}
	}


	/**
	 * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	 * @param actionUrl
	 * @param params
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public static String post(Context context,String actionUrl, Map<String, String> params, 
			List<File> files) throws IOException { 
		Log.v(TAG,"post");
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--" , LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data"; 
		String CHARSET = "UTF-8";

		URL uri = new URL(actionUrl); 
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection(); 
		conn.setReadTimeout(5 * 1000); // 缓存的最长时间 
		conn.setDoInput(true);// 允许输入 
		conn.setDoOutput(true);// 允许输出 
		conn.setUseCaches(false); // 不允许使用缓存 
		conn.setRequestMethod("GET"); 
		conn.setRequestProperty("Connection", "keep-alive"); 
		conn.setRequestProperty("Charsert", "UTF-8"); 
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY); 

		// 首先组拼文本类型的参数 
		StringBuilder sb = new StringBuilder(); 
		if(params != null){
			for (Map.Entry<String, String> entry : params.entrySet()) { 
				sb.append(PREFIX); 
				sb.append(BOUNDARY); 
				sb.append(LINEND); 
				sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
				sb.append("Content-Type: text/plain; charset=" + CHARSET+LINEND);
				sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
				sb.append(LINEND);
				sb.append(entry.getValue()); 
				sb.append(LINEND); 
			} 
		}


		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream()); 
		outStream.write(sb.toString().getBytes()); 
		// 发送文件数据 
		if(files!=null){
			int i = 0;
			for (File source: files) { 
				StringBuilder sb1 = new StringBuilder(); 
				sb1.append(PREFIX); 
				sb1.append(BOUNDARY); 
				sb1.append(LINEND); 
				sb1.append("Content-Disposition: form-data; name=\"file"+(i++)+"\"; filename=\""+source.getName()+"\""+LINEND);
				sb1.append("Content-Type: application/octet-stream; charset="+CHARSET+LINEND);
				sb1.append(LINEND);
				outStream.write(sb1.toString().getBytes()); 


				if(source!= null){//file
					Log.v(TAG,"file");
					InputStream is = new FileInputStream(source);
					byte[] buffer = new byte[1024]; 
					int len = 0; 
					while ((len = is.read(buffer)) != -1) { 
						outStream.write(buffer, 0, len); 
					}
					is.close(); 
				}	     
				outStream.write(LINEND.getBytes()); 
			} 
		}

		//请求结束标志
		byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes(); 
		outStream.write(end_data); 
		outStream.flush(); 

		//得到响应码 
		int res = conn.getResponseCode(); 
		InputStream in = null;
		StringBuilder sb2 = new StringBuilder(); 
		if (res == 200) {
			in = conn.getInputStream(); 
			int ch; 

			while ((ch = in.read()) != -1) { 
				sb2.append((char) ch); 
			} 
		}
		return in == null ? null : sb2.toString(); 
	}

	public boolean checknetwork(Context context) 
	{
		ConnectivityManager connectivity = (ConnectivityManager) context  
				.getSystemService(Context.CONNECTIVITY_SERVICE);  
		if (connectivity != null) {  
			NetworkInfo info = connectivity.getActiveNetworkInfo();  
			if (info != null) {  
				if (info.getState() == NetworkInfo.State.CONNECTED) {  
					return true;  
				}  
			}  
		}  
		return false;  
	}

		private String readstream(InputStream in) 
		{
			StringBuffer resultString = new StringBuffer() ;
			try {
				BufferedReader inbuff = new BufferedReader(new InputStreamReader(in,"GB2312"));
				String line = "";
				while ((line = inbuff.readLine()) != null){
					resultString.append('\n');
					resultString.append(line);
				}
	
			}catch (Exception e) {
			}
			return resultString.toString();
		}


	public void setCookie(String cookie) {
		this.cookie = cookie;
	}


	public String getCookie() {
		return cookie;
	}


}
