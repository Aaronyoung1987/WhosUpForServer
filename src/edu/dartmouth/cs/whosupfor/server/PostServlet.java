package edu.dartmouth.cs.whosupfor.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import edu.dartmouth.cs.whosupfor.data.EventEntry;
import edu.dartmouth.cs.whosupfor.data.UserEntry;
import edu.dartmouth.cs.whosupfor.server.data.DataGlobals;
import edu.dartmouth.cs.whosupfor.server.data.EventDatastore;
import edu.dartmouth.cs.whosupfor.server.data.UserDatastore;
import edu.dartmouth.cs.whosupfor.util.Globals;
import com.google.common.io.BaseEncoding;

public class PostServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String ENTITY_KIND_KEY = "EntityKind";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		String postText = req.getParameter(DataGlobals.POST_KEY_POST_TEXT);
		String taskType = req.getParameter(DataGlobals.POST_KEY_TASK_TYPE);
		System.out.println(postText);
		System.out.println(taskType);

		if (taskType.equals(DataGlobals.TASK_TYPE_CREATE_NEW_EVENT)) {
			createNewEvent(postText);
			resp.sendRedirect("/send_event_update_msg.do");
		}

		if (taskType.equals(DataGlobals.TASK_TYPE_CREATE_NEW_USER)) {
			System.out.println("create new user");
			createNewUser(postText);
			System.out.println("new user created");
			resp.sendRedirect("/send_user_update_msg.do");
		}

		if (taskType.equals(DataGlobals.TASK_TYPE_REPLY_GOING)) {
			String eventId = req.getParameter("event_id");
			String attendee = req.getParameter("user_email");
			if (postText.equals("0")) {
				System.out.println("add");
				boolean result = EventDatastore.addAttendee(eventId, attendee);
				if (result) {
					resp.sendRedirect("/send_event_update_msg.do");
				}
			}
			if (postText.equals("1")) {
				System.out.println("delete");
				boolean result = EventDatastore.deleteAttendee(eventId,
						attendee);

				if (result) {
					resp.sendRedirect("/send_event_update_msg.do");
				}
			}

		}

		if (taskType.equals(DataGlobals.TASK_TYPE_GET_EVENTS)) {
			resp.sendRedirect("/get_event_history.do");
		}

		if (taskType.equals(DataGlobals.TASK_TYPE_GET_USERS)) {
			resp.sendRedirect("/get_user_history.do");
		}

	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doPost(req, resp);
	}

	public void createNewEvent(String postText) {
		try {
			JSONArray jsonArray = new JSONArray(postText);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.optString(Globals.KEY_EVENT_ID) != null) {
					EventEntry event = jsonToEventEntry(jsonObject);
					EventDatastore.add(event);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void createNewUser(String postText) {
		try {
			System.out.println("try new user created");
			JSONArray jsonArray = new JSONArray(postText);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				System.out.println("jsonobj: " + jsonObject);
				if (jsonObject.optString(Globals.KEY_USER_EMAIL) != null) {
					System.out.println("add user");
					UserEntry user = jsonToUserEntry(jsonObject);
					UserDatastore.add(user);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static EventEntry jsonToEventEntry(JSONObject eventJSON) {
		EventEntry event = new EventEntry();

		if (eventJSON != null) {
			event.setEventId(eventJSON.optString(Globals.KEY_EVENT_ID));
			event.setEmail(eventJSON.optString(Globals.KEY_EVENT_EMAIL));
			event.setEventType(eventJSON.optInt(Globals.KEY_EVENT_TYPE));
			event.setEventTitle(eventJSON.optString(Globals.KEY_EVENT_TITLE));
			event.setLocation(eventJSON.optString(Globals.KEY_EVENT_LOCATION));
			event.setDetail(eventJSON.optString(Globals.KEY_EVENT_DETAIL));
			event.setCircle(eventJSON.optInt(Globals.KEY_EVENT_CIRCLE));
			event.setTimeStamp(eventJSON.optLong(Globals.KEY_EVENT_TIME_STAMP));
			event.setStartDateTime(eventJSON
					.optLong(Globals.KEY_EVENT_START_DATE_TIME));
			event.setEndDateTime(eventJSON
					.optLong(Globals.KEY_EVENT_END_DATE_TIME));
			JSONArray attendees = eventJSON
					.optJSONArray(Globals.KEY_EVENT_ATTENDEES);
			for (int i = 0; i < attendees.length(); i++) {
				String attendee = attendees.optString(i, null);
				if (attendee != null) {
					event.addAttendee(attendee);
				}
			}
		}
		return event;
	}

	public static UserEntry jsonToUserEntry(JSONObject userJSON) {
		UserEntry user = new UserEntry();

		if (userJSON != null) {
			user.setID(userJSON.optLong(Globals.KEY_USER_ROWID));
			user.setEmail(userJSON.optString(Globals.KEY_USER_EMAIL));
			user.setFirstName(userJSON.optString(Globals.KEY_USER_FIRST_NAME));
			user.setLastName(userJSON.optString(Globals.KEY_USER_LAST_NAME));
			user.setBio(userJSON.optString(Globals.KEY_USER_BIO));
			user.setGender(userJSON.optInt(Globals.KEY_USER_GENDER));
			user.setClassYear(userJSON.optInt(Globals.KEY_USER_CLASS_YEAR));
			user.setMajor(userJSON.optString(Globals.KEY_USER_MAJOR));
			// user.setProfilePhoto((String) userJSON
			// .optString(Globals.KEY_USER_PROFILE_PHOTO));
			// user.setPassword(userJSON.optString(Globals.KEY_USER_PASSWORD));
			byte[] profilePhoto = BaseEncoding.base64Url()
					.decode((String) userJSON
							.optString(Globals.KEY_USER_PROFILE_PHOTO));
			System.out.println(profilePhoto.toString());
			user.setProfilePhoto(profilePhoto);
		}
		return user;
	}

}
