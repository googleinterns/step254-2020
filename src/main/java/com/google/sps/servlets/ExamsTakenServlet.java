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
import freemarker.template.Version;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler; 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import com.google.sps.data.AnswerClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import freemarker.cache.*;
 
/**
 * Servlet that processes users responses to get exam response form and 
 * outputs the reponses in a format so that the exam can be marked 
 *
 * @author Róisín O'Farrell
 */
@WebServlet("/examsTaken")
public class ExamsTakenServlet extends HttpServlet {
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
 
  /**
   * doPost process the information from the get exam response form response 
   * uses it to create a marking form.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doPost(final HttpServletRequest request,
       final HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "You are not authorised to view this page");
      return;
    }
    logger.atInfo().log("User=%s is logged in", userService.getCurrentUser());
    String ownerID = userService.getCurrentUser().getEmail();
    String examName =UtilityClass.getParameter(request, "examID", null);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    // Create Map with all the test information needed to mark exam
    Map<String,Object>  data = new HashMap<String, Object>();
    List<AnswerClass> answerList = new ArrayList();

    String questionValue, answer, givenMark, possibleMark;
    questionValue = answer = givenMark = possibleMark = null;

    data.put("exam", examName);
    // Look up each response servlet for exam question list and get needed information
    try {
      // Get all exams from owner using query as Exam does not have a known ID/Name
      Query queryExams = new Query("Exam").setFilter(new FilterPredicate(
          "name", FilterOperator.EQUAL, examName));
      PreparedQuery listExams = datastore.prepare(queryExams);
      
      for (Entity entity : listExams.asIterable()) {
        List<Long> questionsList = null;
        try {
          questionsList = (List<Long>) entity.getProperty("questionsList");
        } catch (Exception e) {
          logger.atWarning().log("There was an error getting the questions list: %s", e);
        }
        if(questionsList != null){
          for(Long questionID: questionsList){
            try{
              // Get questionValue and possibleMarks using Key as Question has a known ID/Name
              Key questionKey = KeyFactory.createKey("Question", questionID);
              Entity qs = datastore.get(questionKey); 
              questionValue = (String) qs.getProperty("question");
              possibleMark = (String) qs.getProperty("marks");
            } catch (Exception e) {
              System.out.println("<h3>Question unavailable.</h3>");
              logger.atInfo().log("Cannot find question: %s", e);
            }
            String responseName = Long.toString(questionID);
            try{
              // Get answer and givenMarks using Key as each question response servlet has a known ID/Name
              Key responseKey = KeyFactory.createKey(responseName, ownerID);
              Entity res = datastore.get(responseKey);
              answer = (String) res.getProperty("answer");
              givenMark = (String) res.getProperty("marks");
              if (givenMark == null){
                givenMark = "Not Marked Yet";
              }
            } catch (Exception e) {
              System.out.println("<h3>Response unavailable.</h3>");
              logger.atInfo().log("Cannot find student response: %s", e);
            }
            //Add Answer object to answerList and add that to Map to be used in freemaker template
            answerList.add(new AnswerClass(responseName, answer, givenMark, questionValue, possibleMark));
            data.put("responses", answerList);
          }
        }
      }
    } catch (DatastoreFailureException e) {
      logger.atWarning().log("There was a problem with retrieving the exams %s",
          e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to find your tests");
      return;
    }
    
    // run to freemarker template
    try {
      Template template = cfg.getTemplate("ViewExam.ftl");
      PrintWriter out = response.getWriter();
      template.process(data, out);
      logger.atInfo().log("Question form was displayed correctly for the User:"
          + "%s", userService.getCurrentUser());
    } catch (TemplateException e) {
      logger.atWarning().log("There was a problem with processing the template %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to display the Question Form");
      return;
    } 
 }
}