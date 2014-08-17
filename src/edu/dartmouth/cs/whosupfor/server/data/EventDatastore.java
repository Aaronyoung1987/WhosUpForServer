package edu.dartmouth.cs.whosupfor.server.data;

import java.util.ArrayList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

import edu.dartmouth.cs.whosupfor.data.EventEntry;
import edu.dartmouth.cs.whosupfor.util.Globals;

public class EventDatastore {
	private static final DatastoreService mDatastore = DatastoreServiceFactory
			.getDatastoreService();

	private static Key getParentKey() {
		return KeyFactory.createKey(DataGlobals.ENTITY_KIND_EVENT_PARENT,
				DataGlobals.ENTITY_EVENT_PARENT_KEY);
	}

	private static void createParentEntity() {
		Entity entity = new Entity(getParentKey());

		mDatastore.put(entity);
	}

	public static boolean add(EventEntry event) {
		Key parentKey = getParentKey();
		try {
			mDatastore.get(parentKey);
		} catch (Exception ex) {
			createParentEntity();
		}

		Entity entity = new Entity(DataGlobals.ENTITY_KIND_EVENT,
				event.getEventId(), parentKey);

		setEntityFromEventEntry(entity, event);

		mDatastore.put(entity);

		return true;
	}

	public static boolean update(EventEntry event) {
		Entity result = null;
		try {
			result = mDatastore.get(KeyFactory.createKey(getParentKey(),
					DataGlobals.ENTITY_KIND_EVENT, event.getEventId()));

			setEntityFromEventEntry(result, event);

			mDatastore.put(result);
		} catch (Exception ex) {
		}
		return false;
	}

	public static boolean addAttendee(String eventId, String attendee) {
		try {
			Entity result = getEntityById(eventId);
			EventEntry event = getEventEntryFromEntity(result);
			if (event != null && event.getAttendees() != null) {
				if (!event.getAttendees().contains(attendee)) {
					event.addAttendee(attendee);
					// add(event);
					setEntityFromEventEntry(result, event);
					mDatastore.put(result);
					return true;
				}
			}
		} catch (Exception ex) {
		}
		return false;
	}

	public static boolean deleteAttendee(String eventId, String attendee) {
		try {
			Entity result = getEntityById(eventId);
			EventEntry event = getEventEntryFromEntity(result);
			if (event != null) {
				if (event.getAttendees().remove(attendee)) {
					setEntityFromEventEntry(result, event);
					mDatastore.put(result);
					return true;
				}
			}
		} catch (Exception ex) {
		}
		return false;
	}

	public static boolean delete(String id) {
		// query
		Filter filter = new FilterPredicate(Globals.KEY_EVENT_ID,
				FilterOperator.EQUAL, id);

		Query query = new Query(Globals.KEY_EVENT_ID);
		query.setFilter(filter);

		PreparedQuery pq = mDatastore.prepare(query);

		Entity result = pq.asSingleEntity();
		boolean ret = false;
		if (result != null) {
			// delete
			mDatastore.delete(result.getKey());
			ret = true;
		}

		return ret;
	}

	public static Entity getEntityById(String id) {
		Entity result = null;
		try {
			result = mDatastore.get(KeyFactory.createKey(getParentKey(),
					DataGlobals.ENTITY_KIND_EVENT, id));
		} catch (Exception ex) {
		}

		return result;
	}

	public static EventEntry getEventById(String id) {
		EventEntry result = null;
		try {
			result = getEventEntryFromEntity(getEntityById(id));
		} catch (Exception ex) {
		}

		return result;
	}

	public static ArrayList<EventEntry> query() {
		ArrayList<EventEntry> resultList = new ArrayList<EventEntry>();

		Query query = new Query(DataGlobals.ENTITY_KIND_EVENT);
		query.setFilter(null);
		query.setAncestor(getParentKey());
		// query.addSort(Globals.KEY_EVENT_TIME_STAMP, SortDirection.ASCENDING);
		PreparedQuery pq = mDatastore.prepare(query);

		for (Entity entity : pq.asIterable()) {
			EventEntry event = getEventEntryFromEntity(entity);

			resultList.add(event);
		}
		return resultList;
	}

	private static void setEntityFromEventEntry(Entity entity, EventEntry event) {
		if (entity == null) {
			return;
		}

		// entity.setProperty(Globals.KEY_EVENT_ROWID, event.getEventId());
		entity.setProperty(Globals.KEY_EVENT_ID, event.getEventId());
		entity.setProperty(Globals.KEY_EVENT_EMAIL, event.getEmail());
		entity.setProperty(Globals.KEY_EVENT_TITLE, event.getEventTitle());
		entity.setProperty(Globals.KEY_EVENT_TYPE, event.getEventType());
		entity.setProperty(Globals.KEY_EVENT_LOCATION, event.getLocation());
		entity.setProperty(Globals.KEY_EVENT_TIME_STAMP, event.getTimeStamp());
		entity.setProperty(Globals.KEY_EVENT_START_DATE_TIME,
				event.getStartDateTimeInMillis());
		entity.setProperty(Globals.KEY_EVENT_END_DATE_TIME,
				event.getEndDateTimeInMillis());
		entity.setProperty(Globals.KEY_EVENT_DETAIL, event.getDetail());
		entity.setProperty(Globals.KEY_EVENT_ATTENDEES, event.getAttendees());
		entity.setProperty(Globals.KEY_EVENT_CIRCLE, event.getCircle());
	}

	@SuppressWarnings("unchecked")
	private static EventEntry getEventEntryFromEntity(Entity entity) {
		if (entity == null) {
			return null;
		}

		EventEntry event = new EventEntry();
		event.setEventId((String) entity.getProperty(Globals.KEY_EVENT_ID));
		event.setEmail((String) entity.getProperty(Globals.KEY_EVENT_EMAIL));
		event.setEventTitle((String) entity
				.getProperty(Globals.KEY_EVENT_TITLE));
		event.setEventType((int) (long) entity
				.getProperty(Globals.KEY_EVENT_TYPE));
		event.setLocation((String) entity
				.getProperty(Globals.KEY_EVENT_LOCATION));
		event.setTimeStamp((long) entity
				.getProperty(Globals.KEY_EVENT_TIME_STAMP));
		event.setStartDateTime((long) entity
				.getProperty(Globals.KEY_EVENT_START_DATE_TIME));
		event.setEndDateTime((long) entity
				.getProperty(Globals.KEY_EVENT_END_DATE_TIME));
		event.setDetail((String) entity.getProperty(Globals.KEY_EVENT_DETAIL));
		// Object attendeesProperty = entity
		// .getProperty(Globals.KEY_EVENT_ATTENDEES);
		// if (attendeesProperty instanceof ArrayList<?>) {
		// for (int i = 1; i < ((ArrayList<?>) attendeesProperty).size(); i++) {
		// event.addAttendee((String) ((ArrayList<?>) attendeesProperty)
		// .get(i));
		// }
		// }

		ArrayList<String> attendeesProperty = new ArrayList<String>();
		attendeesProperty = (ArrayList<String>) (entity
				.getProperty(Globals.KEY_EVENT_ATTENDEES));
		if (attendeesProperty != null) {
			event.setAttendees(attendeesProperty);
		}
		event.setCircle((int) (long) entity
				.getProperty(Globals.KEY_EVENT_CIRCLE));
		return event;
	}

}
