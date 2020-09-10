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
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import java.util.List;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to update Student exam responses to include marks given by exam owner, overwrites current marks 
 * linked with users email and question on the datastore.
 *
 * @author Róisín O'Farrell
 */
@WebServlet("/updateExamResponse")
public class UpdateExamResponseServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * doPost Get passed parameters for users given marks and save to datastore.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
      "You are not authorised to view this page");
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());

    String examName;
    String studentEmail;
    String givenMark;
    examName= studentEmail = givenMark = null;

    try {
      examName = request.getParameter("examName");
      studentEmail = request.getParameter("studentEmail");
    } catch (Exception e) {
      logger.atSevere().log("One or more null parameters in try/catch");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    // Look up each response servlet for exam question list and overwrite entity corresponding with studentEmail
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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
            String responseName = Long.toString(questionID);
            try{
              // Get passed value for givenMark from questionID
              givenMark = request.getParameter(responseName);
              // Get current values for answer, email and givenMark
              Key responseKey = KeyFactory.createKey(responseName, studentEmail);
              Entity res = datastore.get(responseKey);
              String answer = (String) res.getProperty("answer");
              String email = (String) res.getProperty("email");
              // Overwrite current values for answer, email and givenMark
              Entity updateResponse = new Entity (responseName, studentEmail);
              updateResponse.setProperty("marks", givenMark);
              updateResponse.setProperty("email", email);
              updateResponse.setProperty("answer", answer);
              datastore.put(updateResponse);
            } catch (Exception e) {
              System.out.println("<h3>Response unavailable.</h3>");
              logger.atInfo().log("Cannot set student mark: %s", e);
            }
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
    response.sendRedirect("dashboardServlet");
  }
}
