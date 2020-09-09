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
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
  Configuration cfg;

  /**
   * Set up the configuration once.
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    cfg = new Configuration(Configuration.VERSION_2_3_30);
    String path = getServletContext().getRealPath("/WEB-INF/templates/");
    try {
      cfg.setDirectoryForTemplateLoading(new File(path));
    } catch (IOException e) {
      logger.atWarning().log("Could not set directory for template loading: %s", e);
    }
    // Recommended settings for new projects:
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);

  }

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
    if (!userService.isUserLoggedIn()
        || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendRedirect("/");
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());

    Map data = new HashMap();
    String examID = UtilityClass.getParameter(request, "examID", null);
    Entity examEntity = null;
    String examContent = "";
    if (examID != null) {
      // If an exam has been selected
      try {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey("Exam", Long.parseLong(examID));
        examEntity = datastore.get(key);
      } catch (Exception e) {
        examContent += ("<h1>Selected exam is not available.</h1>");
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
        examContent += ("<h1>Exam Name: " + name + "</h1>");
        examContent += ("<h3>Length: " + duration + "</h3>");
        examContent += ("<h3>Created By: " + ownerID + "</h3>");

        // If there are questions
        if (questionsList != null) {
          int questionNumber = 0;
          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          examContent += ("<section class=\"form\">");
          examContent += ("<form action=\"/examResponse\" method=\"POST\">");
          examContent += ("<input type=\"hidden\" name=\"examID\" value=\"" + examID + "\">");
          for (Long question : questionsList) {
            try {
              questionNumber++;
              Key key = KeyFactory.createKey("Question", question);
              Entity qs = datastore.get(key);
              Long questionID = qs.getKey().getId();
              String questionValue = (String) qs.getProperty("question");
              String type = (String) qs.getProperty("type");

              if (type.equals("MCQ")) {
                List<String> answerList = (List<String>) qs.getProperty("mcqPossibleAnswers");
                examContent += ("<output>" + questionNumber + ") "
                    + questionValue + ": </output><br>");
                for (String answer : answerList) {
                  examContent += ("<input type=\"radio\" id=\"" + answer + "\" name=\""
                      + questionID + "\" value=\"" + answer + "\"onchange=\"setDirty()\">");
                  examContent += ("<label for=\"" + answer + "\">" + answer + "</label><br><br>");
                }
              } else {
                examContent += ("<label for=\"" + questionID + "\">" + questionNumber + ") "
                    + questionValue + ": </label>");
                examContent += ("<input type=\"text\" id=\"" + questionID + "\" name=\""
                    + questionID + "\" onclick=\"startDictation(this.id)\" "
                    + "onchange=\"setDirty()\"><br><br>");
              }
            } catch (Exception e) {
              examContent += ("<p>Question was not found</p><br>");
              logger.atWarning().log("Question does not exist: %s", e);
            }
          }
          examContent += ("<br><input type=\"submit\" value=\"Submit\""
              + "onclick=\"setExamSubmitting()\">");
          examContent += ("</form>");
          examContent += ("<section>");
        } else {
          examContent += ("<p>There are no questions associated with this exam.</p>");
        }
      } else {
        // If exam is not selected or unavailable display list of available exams
        try {
          Query query = new Query("Exam");
          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          PreparedQuery results = datastore.prepare(query);
          examContent += ("<h1>Choose an exam to take.</h1><table><tr><th>Name</th> "
            + "<th>Duration</th></tr>");
          for (Entity entity : results.asIterable()) {
            String name = (String) entity.getProperty("name");
            String duration = (String) entity.getProperty("duration");
            long id = entity.getKey().getId();
            examContent += ("<tr><td>" + name + "</td><td>" + duration + "</td>");
            examContent += ("<td><a href=\"/exam?examID=" + id + "\">Take Exam</a></td></tr>");
          }
        } catch (Exception e) {
          logger.atSevere().log("Error with Datastore: %s", e);
        }
        examContent += ("</table>");
      }
    } else {
      // If exam is not selected or unavailable display list of available exams
      try {
        Query query = new Query("Exam");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);
        examContent += ("<h1>Choose an exam to take.</h1><table><tr><th>Name</th> "
            + "<th>Duration</th></tr>");
        for (Entity entity : results.asIterable()) {
          String name = (String) entity.getProperty("name");
          String duration = (String) entity.getProperty("duration");
          long id = entity.getKey().getId();
          examContent += ("<tr><td>" + name + "</td><td>" + duration + "</td>");
          examContent += ("<td><a href=\"/exam?examID=" + id + "\">Take Exam</a></td></tr>");
        }
      } catch (Exception e) {
        logger.atSevere().log("Error with Datastore: %s", e);
      }
      examContent += ("</table>");
    }
    data.put("examContent", examContent);

    // run to freemarker template
    try {
      Template template = cfg.getTemplate("Exam.ftl");
      PrintWriter out = response.getWriter();
      template.process(data, out);
      logger.atInfo().log("Exam page was displayed correctly for the User:"
          + "%s", userService.getCurrentUser());
    } catch (TemplateException e) {
      logger.atWarning().log("There was a problem with processing the template %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to display the Exam Page");
    }
  }
}