// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that creates a question form for the user to fill out.
* A user can add a question to whichever test they want.
* @author Klaudia Obieglo
*/
@WebServlet("/questionForm")
public class QuestionFormServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  @Override
  public void doGet(final HttpServletRequest request,
        final HttpServletResponse response) throws IOException {
    /*Returns all the questions that the user has created */
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "You are not authorised to view this page");
      return;
    }
    logger.atInfo().log("User=%s is logged in", userService.getCurrentUser());
    String ownerID = userService.getCurrentUser().getEmail();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<link href=\"https://fonts.googleapis.com/css2?family=Domine:"
        + "wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,"
        + "600;1,700&display=swap\" rel=\"stylesheet\">");
    out.println("<link rel=\"stylesheet\" href=\"style.css\">");
    out.println("<script src=\"script.js\"></script>");
    out.println("</head>");
    out.println("<body>");
    out.println("<header>");
    out.println("<div class=\"navtop\">");
    out.println("<p><a  href=\"index.html\">Homepage</a></p>");
    out.println("<p><a  href=\"dashboard.html\">Dashboard</a></p>");
    out.println("<p id=logInOut></p>");
    out.println("</div>");
    out.println("</header>");
    out.println("<main>");
    out.println("<body>");
    out.println("<h3>Create a New Question</h3>");
    out.println("<form id=\"createQuestion\" action=\"/createQuestion\""
        + " method=\"POST\">");
    out.println("<label for=\"question\">Enter Question:</label><br>");
    out.println("<textarea name=\"question\" rows=\"4\" cols=\"50\" maxlength=\"200\"required>"
        + "</textarea><br>");
    out.println("<label for=\"marks\">Marks given for Question:</label><br>");
    out.println("<input type=\"number\" id=\"marks\" name=\"marks\" min=\"0\"max=\"1000\" step=\"0.01\"required>");
    out.println("<h3> Select which test you want the questions added to</h1>");
    out.println("<select name=\"testName\">");
    // Find all tests created by this user and display them as a dropdown menu.
    try {
      Query queryExams = new Query("Exam").setFilter(new FilterPredicate(
          "ownerID", FilterOperator.EQUAL, ownerID)).addSort("date",
          SortDirection.DESCENDING);
      PreparedQuery listExams = datastore.prepare(queryExams);
      for (Entity entity : listExams.asIterable()) {
        long examID = entity.getKey().getId();
        System.out.println(examID +"EXAM ID");
        String name = (String) entity.getProperty("name");
        out.println("<option>" + name  + "</option>");
      }
    } catch (Exception e) {
      logger.atWarning().log("There was a problem with retrieving the exams %s",
          e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to find your tests");
      return;
    }

    out.println("</select>");
    out.println("<br/>");
    out.println("<br/>");
    out.println("<button>Submit</button>");
    out.println("</form>");
    out.println("<h3> Click Below to Look Through Previous Questions You asked</h3>");
    out.println("<form id=\"addQuestions\" action=\"/returnQuestionsUserOwns\""
        + " method=\"GET\">");
    out.println("<input type=\"submit\" value=\"Click\">");
    out.println("</form>");
    out.println("</body>");
    logger.atInfo().log("Question form was displayed correctly for the User:"
      + "%s", userService.getCurrentUser());
  }
}