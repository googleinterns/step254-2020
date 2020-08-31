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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
 * Servlet that gets all exams a user owns and all the students that haven taken
 * one of those exams
 *
 * @author Róisín O'Farrell
 */
@WebServlet("/getExamResponses")
public class GetExamResponsesServlet extends HttpServlet {
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
   * doPost process the information from the exam form response and send it to the datastore.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doGet(final HttpServletRequest request,
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
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    /* create Map with all the tests the user owns and all students who have 
       taken one of those exams*/
    Map data = new HashMap();
    Map<Long,String> testMap = new LinkedHashMap<Long,String>();
    //Map<Long, String> studentMap = new LinkedHashMap<Long,String>();
    List<String> studentList = new ArrayList();
    //int responseNumber = 0;
    /*Look up each Exam to get the question list and look up students who have 
      answered those questions*/
    try {
      // Get all exams from owner using query as Exam does not have a known ID/Name
      Query queryExams = new Query("Exam").setFilter(new FilterPredicate(
          "ownerID", FilterOperator.EQUAL, ownerID)).addSort("date",
          SortDirection.DESCENDING);
      PreparedQuery listExams = datastore.prepare(queryExams);
      for (Entity entity : listExams.asIterable()) {
        long examID = entity.getKey().getId();
        String name = (String) entity.getProperty("name");
        testMap.put(examID,name);
        data.put("tests", testMap);
        List<Long> questionsList = null;
        try {
          questionsList = (List<Long>) entity.getProperty("questionsList");
        } catch (Exception e) {
          logger.atWarning().log("There was an error getting the questions list: %s", e);
        }
        if(questionsList != null){
          Long questionID = questionsList.get(0);
          String responseQuery = Long.toString(questionID);
          /* Get all responses from owner using query as the question response does not 
             have a known ID/Name*/
          Query queryResponses = new Query(responseQuery);
          PreparedQuery questionResponses = datastore.prepare(queryResponses);

          for (Entity responders : questionResponses.asIterable()) {
           if(responders != null){
             String email = (String) responders.getProperty("email");
             studentList.add(email);
           }  
          }
        }
      }
    data.put("students", studentList);
    } catch (DatastoreFailureException e) {
      logger.atWarning().log("There was a problem with retrieving the exams %s",
          e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to find your tests");
      return;
    }
    // run to freemarker template
    try {
      Template template = cfg.getTemplate("GetExamResponsesForm.ftl");
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