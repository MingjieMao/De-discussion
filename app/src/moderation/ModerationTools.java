package moderation;


import dao.PostDAO;
import dao.UserDAO;
import dao.model.Message;
import dao.model.Report;


import java.util.*;


public class ModerationTools {
	private static final Map<UUID, Map<UUID, Report>> reports = new HashMap<>();


	public static boolean addReport(UUID message, UUID user, long timestamp) {
		if (findMessage(message) == null) return false;
		if (UserDAO.getInstance().getByUUID(user) == null) return false;


		reports.putIfAbsent(message, new HashMap<>());
		Map<UUID, Report> byUser = reports.get(message);
		if (byUser.containsKey(user)) return false;


		byUser.put(user, new Report(message, user, timestamp));
		return true;
	}


	public static boolean removeReport(UUID message, UUID user, long timestamp) {
		if (findMessage(message) == null) return false;
		if (UserDAO.getInstance().getByUUID(user) == null) return false;


		Map<UUID, Report> byUser = reports.get(message);
		if (byUser == null || !byUser.containsKey(user)) return false;


		byUser.remove(user);
		return true;
	}


	public static boolean hasReported(UUID message, UUID user) {
		Map<UUID, Report> byUser = reports.get(message);
		return byUser != null && byUser.containsKey(user);
	}


	public static boolean setHidden(UUID message, UUID user, boolean hidden) {
		// TODO: task 2
		return false;
	}


	public static Iterator<Message> getReportedMessages(String strategy, int amount) {
		// TODO: task 4
		return null;
	}


	private static Message findMessage(UUID messageId) {
		Iterator<Message> it = PostDAO.getInstance().getAllMessages();
		while (it.hasNext()) {
			Message m = it.next();
			if (m.id().equals(messageId)) return m;
		}
		return null;
	}
}
