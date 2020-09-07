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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to display the requested exam  and send responses to ExamResponseServlet or if no exam
 * is selected it will display a list of available exams.
 *
 * @author Aidan Molloy
 */
@WebServlet("/exam")
public class ExamServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * Gets the exam ID from the httpRequest, displays exam if it exists otherwise displays
   * list of exams.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendRedirect("/");
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());
    response.setContentType("text/html;");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<link href=\"https://fonts.googleapis.com/css2?family=Domine:wght@400;"
        + "700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap\""
        + " rel=\"stylesheet\">");
    out.println("<link rel=\"stylesheet\" href=\"style.css\">");
    out.println("<script src=\"script.js\"></script>");
    out.println("<script src=\"examSubmission.js\"></script>");
    out.println("<title>Take Exam</title>");
    out.println("<style>main {padding: 20px;}</style>");
    out.println("</head>");
    out.println("<body>");
    out.println("<header>");
    out.println("<div class=\"navtop\">");
    out.println("<p><a href=\"index.html\">Homepage</a></p>");
    out.println("<p><a href=\"dashboardServlet\">Dashboard</a></p>");
    out.println("<p id=logInOut></p>");
    out.println("</div>");
    out.println("</header>");
    out.println("<main>");

    String examID = UtilityClass.getParameter(request, "examID", null);
    Entity examEntity = null;
    if (examID != null) {
      // If an exam has been selected
      try {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey("Exam", Long.parseLong(examID));
        examEntity = datastore.get(key);
      } catch (Exception e) {
        out.println("<h3>Selected exam is not available.</h3>");
        logger.atInfo().log("Exam ID does not exist: %s", e);
      }
      if (examEntity != null) {
        // If exam exists, then display the exam and questions
        String name = (String) examEntity.getProperty("name");
        String duration = (String) examEntity.getProperty("duration");
        final String ownerID = (String) examEntity.getProperty("ownerID");
        List<Long> questionsList = null;
        try {
          questionsList = (List<Long>) examEntity.getProperty("questionsList");
        } catch (Exception e) {
          logger.atWarning().log("There was an error getting the questions list: %s", e);
        }
        out.println("<h1>Exam Name: " + name + "</h1>");
        out.println("<h3>Length: " + duration + "</h3>");
        out.println("<h3>Created By: " + ownerID + "</h3>");

        // If there are questions
        if (questionsList != null) {
          int questionNumber = 0;
          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          out.println("<section class=\"form\">");
          out.println("<form action=\"/examResponse\" method=\"POST\">");
          out.println("<input type=\"hidden\" id=\"examID\" name=\"examID\" value=\""+examID+"\">"); 
          for (Long question : questionsList) {
            try {
              questionNumber++;
              Key key = KeyFactory.createKey("Question", question);
              Entity qs = datastore.get(key);
              Long questionID = qs.getKey().getId();
              String questionValue = (String) qs.getProperty("question");
              String type = (String) qs.getProperty("type");

              if (type.equals("MCQ")){
                 List<String> answerList = (List<String>) qs.getProperty("mcqPossibleAnswers");
                 out.println("<output>" + questionNumber + ") "
                     + questionValue + ": </output><br>");
                  for(String answer : answerList){
                    out.println("<input type=\"radio\" id=\"" + answer + "\" name=\""
                        + questionID + "\" value=\"" + answer + "\"onchange=\"setDirty()\">");
                    out.println("<label for=\"" + answer + "\">" + answer +"</label><br><br>");
                    
                  }
              } else{
                out.println("<input type=\"hidden\" id=\"type\" name=\"type\" value=type>");
                out.println("<label for=\"" + questionID + "\">" + questionNumber + ") "
                    + questionValue + ": </label>");
                out.println("<input type=\"text\" id=\"" + questionID + "\" name=\""
                    + questionID + "\" onclick=\"startDictation(this.id)\" onchange=\"setDirty()\"><br><br>");
              }


            } catch (Exception e) {
              out.println("<p>Question was not found</p><br>");
              logger.atWarning().log("Question does not exist: %s", e);
            }
          }
          out.println("<br><input type=\"submit\" value=\"Submit\"" 
              + "onclick=\"setExamSubmitting()\">");
          out.println("</form>");
          out.println("<section>");
        } else {
          out.println("<p>There are no questions associated with this exam.</p>");
        }
        out.println("</main></body>");
        return;
      }
    }

    // If exam is not selected or unavailable display list of available exams
    out.println("<h1>Choose an exam to take.</h1>");
    out.println("<table><tr><th>Name</th><th>Duration</th></tr>");

    try {
      Query query = new Query("Exam");
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);
      for (Entity entity : results.asIterable()) {
        long id = entity.getKey().getId();
        String name = (String) entity.getProperty("name");
        String duration = (String) entity.getProperty("duration");

        out.println("<tr>");
        out.println("<td>" + name + "</td>");
        out.println("<td>" + duration + "</td>");
        out.println("<td><a href=\"/exam?examID=" + id + "\">Take Exam</a></td>");
        out.println("</tr>");
      }
    } catch (Exception e) {
      logger.atSevere().log("Error with Datastore: %s", e);
    }
    out.println("</table>");
    out.println("</body>");
  }
}