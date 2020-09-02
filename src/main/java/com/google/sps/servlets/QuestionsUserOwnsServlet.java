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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import freemarker.template.Version;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a checkbox form of all the questions owned by the user
* A user can then select the questions they want to reuse by clicking on the
* checkboxes.
* @author Klaudia Obieglo
*/
@WebServlet("/returnQuestionsUserOwns")
public class QuestionsUserOwnsServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  Configuration cfg;
  //set up the configuration once
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
  @Override
  public void doGet(final HttpServletRequest request,
       final HttpServletResponse response) throws IOException {
    /*Returns all the questions that the user has created */
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "You are not authorised to view this page");
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());
    String ownerID = userService.getCurrentUser().getEmail();

    DatastoreService datastore = null;
    Map data = new HashMap();

    boolean exceptions = false;
    //Find all questions a user owns
    exceptions = findQuestionsUserOwns(ownerID, datastore, data);
    if(exceptions) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to find your questions");
      return;
    }
    //Find all the Exams a user owns
    exceptions = findExamsUserOwns(ownerID, datastore, data);
    if(exceptions) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to find your exams");
      return;
    }
    // run the freemarker template
    try {
      Template template = cfg.getTemplate("QuestionsUsersOwn.ftl");
      PrintWriter out = response.getWriter();
      template.process(data, out);
      logger.atInfo().log("Questions and Tests that the User: %s , owns were found"
        + " and displayed correctly", userService.getCurrentUser());
    } catch (TemplateException e) {
      logger.atWarning().log("There was a problem with processing the template %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to display the questions you own");
      return;
    }
  }
  public boolean findQuestionsUserOwns(String ownerID, DatastoreService datastore, Map data) {
    /* Finds all questions a user owns and saves them in the data map.
    * Arguments :
    * - ownerID - email of the user.
    * - datastore - datastore for retrieving entities
    * - data - map to store the questions user owns.
    */
    try {
      Map<Long,String> questionsMap = new LinkedHashMap<Long,String>();
      datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("Question").setFilter(new FilterPredicate("ownerID",
          FilterOperator.EQUAL, ownerID)).addSort("date", SortDirection.ASCENDING);
      PreparedQuery results = datastore.prepare(query);

      for (Entity entity : results.asIterable()) {
        long questionId = entity.getKey().getId();
        String question = (String) entity.getProperty("question");
        String marks = (String) entity.getProperty("marks");
        String qs = question + " (" + marks+")";
        questionsMap.put(questionId, qs);
        data.put("questions", questionsMap);
      }
      return false;
    } catch (DatastoreFailureException e) {
      logger.atWarning().log("There was an error with retrieving the questions: %s",
          e);
      return true;
    }
  }
  public boolean findExamsUserOwns(String ownerID, DatastoreService datastore, Map data) {
    /* Finds all exams a user owns and saves them in the data map.
    * Arguments :
    * - ownerID - email of the user.
    * - datastore - datastore for retrieving entities
    * - data - map to store the questions user owns.
    */
    try {
      Map<Long,String> testMap = new LinkedHashMap<Long,String>();
      datastore = DatastoreServiceFactory.getDatastoreService();
      Query queryExams = new Query("Exam")
          .setFilter(new FilterPredicate("ownerID", FilterOperator.EQUAL,
          ownerID)).addSort("date", SortDirection.DESCENDING);
      PreparedQuery listExams = datastore.prepare(queryExams);

      for (Entity entity : listExams.asIterable()) {
        long examID = entity.getKey().getId();
        String name = (String) entity.getProperty("name");
        testMap.put(examID, name);
        data.put("tests", testMap);
      }
      return false;
    } catch (DatastoreFailureException e) {
      logger.atWarning().log("There was an error when retrieving the tests: %s",
          e);
      return true;
    }
  }
}