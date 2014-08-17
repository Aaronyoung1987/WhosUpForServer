package edu.dartmouth.cs.whosupfor.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.dartmouth.cs.whosupfor.data.EventEntry;
import edu.dartmouth.cs.whosupfor.server.data.EventDatastore;


public class QueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
		ArrayList<EventEntry> eventList = EventDatastore.query();
		req.setAttribute("eventList", eventList);
		
		//getServletContext().getRequestDispatcher("/main.jsp").forward(req, resp);
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doPost(req, resp);
	}
	
}
