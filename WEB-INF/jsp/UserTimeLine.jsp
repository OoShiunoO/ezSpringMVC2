<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.json.JSONArray, org.json.JSONException, org.json.JSONObject, java.util.Map, java.util.HashMap, java.util.TreeMap, java.util.List, java.util.ArrayList" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to Twitter Duplication</title>
</head>
<body>
<%
String message = (String) request.getAttribute("Message");
String screen_name = (String) request.getAttribute("screen_name");
%>
<h1>Hello~  <%=screen_name%></h1>
<h1>你的UserTimeLine:</h1>

<%
Map<String, Object> tweetAndRetweet_order = new TreeMap<String, Object>();
Map<String, Object> tweetObject = new HashMap<String, Object>();
JSONArray jarray = new JSONArray(message);
	for (int i=0; i<jarray.length(); i++) {
		JSONObject jobject = (JSONObject) jarray.get(i);
		String tweetID = String.valueOf(jobject.getLong("id"));
		tweetObject.put(tweetID, jobject);
		if (jobject.isNull("in_reply_to_status_id")) {
			if (tweetAndRetweet_order.get(tweetID) == null) {
				List<String> list = new ArrayList<String>();
				tweetAndRetweet_order.put(tweetID, list);
			}
		} else {
			String in_reply_to_status_id = String.valueOf(jobject.getLong("in_reply_to_status_id"));
			List<String> list = null;
			if (tweetAndRetweet_order.get(in_reply_to_status_id) == null) {
				list = new ArrayList<String>();
				list.add(tweetID);
			} else {
				list = ((List<String>) tweetAndRetweet_order.get(in_reply_to_status_id));
				list.add(tweetID);
			}
			tweetAndRetweet_order.put(in_reply_to_status_id, list);
		}//end of else
	}//end of for
%>
<%
	for (String tweetID: tweetAndRetweet_order.keySet()) {
		JSONObject jobject = (JSONObject) tweetObject.get(tweetID);
		String createdAt = jobject.getString("created_at");
		String text = jobject.getString("text");
		JSONObject userObject = (JSONObject) jobject.get("user");
		String name = userObject.getString("name");
		String screenName = userObject.getString("screen_name");
%>
<b><%=name%></b>
<span style="color:#A9A9A9">@<%=screenName%> <%=createdAt%></span><br/>
<font size="5"><%=text%></font><br/>
<%
		if (jobject.has("extended_entities")) {
			JSONObject extended_entities = jobject.getJSONObject("extended_entities");
			JSONArray media_array = extended_entities.getJSONArray("media");
			for (int i=0; i<media_array.length(); i++) {
				String media_url = ((JSONObject) media_array.get(i)).getString("media_url");
%>
<img src=<%=media_url%>>
<%
			}
		}
%>


<%
		List<String> list = (List<String>)tweetAndRetweet_order.get(tweetID);
		for (int i=list.size()-1;i>=0;i--) {
			JSONObject jobject2 = (JSONObject) tweetObject.get(list.get(i));
			String createdAt2 = jobject2.getString("created_at");
			String text2 = jobject2.getString("text");
			JSONObject userObject2 = (JSONObject) jobject2.get("user");
			String name2 = userObject2.getString("name");
			String screenName2 = userObject2.getString("screen_name");
%>
此推文的回復
<b><%=name2%></b>
<span style="color:#A9A9A9">@<%=screenName2%> <%=createdAt2%></span><br/>
<%=text2%><br/>
<%
		}
		
%>
<hr/>
<%
	}
%>	

</body>
</html>