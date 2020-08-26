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
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that saves selected questions to the test.
* @author Klaudia Obieglo
*/
@WebServlet("/saveQuestionsFromBank")
public class SaveQuestionsFromBankServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  @Override
  public void doPost(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    /*Saves the selected questions from the question bank to the test
    * that the user wants the questions saved to
    */
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "You are not authorised to view this page");
      return;
    }
    logger.atInfo().log("user=%s is logged in", userService.getCurrentUser());
    String ownerID = userService.getCurrentUser().getEmail();

    String testName = UtilityClass.getParameter(request, "testName", "");
    String[] questionsList = request.getParameterValues("question");
    
    if (testName == null || questionsList == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      logger.atWarning().log("One or more null parameters, testName:%s, questionsList:%s",
          testName,questionsList);
      return;
    }
    try {
      for (int i = 0; i < questionsList.length; i++) {
        long qsID = Long.valueOf(questionsList[i].replaceAll(",", "").toString());
        boolean success = addQuestionToExamList(qsID, ownerID, testName);
        if(success) {
          response.getWriter().println("Successfully added Question " +
              qsID + " to the test " + testName);
        } else {
          response.getWriter().println("Could not add Question " + qsID +
              " to the test " + testName);
        }
      }
      response.sendRedirect("/questionForm");
    } catch (Exception e) {
      logger.atSevere().log("There was an error with saving the questions"
        + " : %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to save your questions to the test");
      return;
    }

  }
  private boolean addQuestionToExamList(final long questionEntityKey,
      final String ownerID, final String testName) {
    /*Function that adds the question id to the list of questions to the latest
    *  test created by the user
    * Arguments
    * -QuestionEntityKey - id of the question Entity we are adding to the list
    * -ownerID - email of the person who is adding this question to their test
    * Returns true if questions were added successfully and false if there was
    * an exception
    */
    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity latestExam = getExam(ownerID, testName);

      if (latestExam.getProperty("questionsList") == null) {
        List<Long> questionList = new ArrayList<>();
        questionList.add(questionEntityKey);
        latestExam.setProperty("questionsList", questionList);
      } else {
        List<Long> questionList =
            (List<Long>) latestExam.getProperty("questionsList");
        questionList.add(questionEntityKey);
        latestExam.setProperty("questionsList", questionList);
      }
      datastore.put(latestExam);
      logger.atInfo().log("Added question: %s to test: %s", questionEntityKey,
          latestExam.getKey().getId());
      return true;
    } catch (DatastoreFailureException e) {
        logger.atWarning().log("Exam questions cannot be added: %s", e);
        return false;
    }
  }
  private Entity getExam(final String ownerID, final String testName) {
    /* Function that returns the exam created by the user
    *  Arguments: ownerID - email of the user who's last exam we want to find
    *  Return : Returns the entity of the exam created by that user.
    */
    Query queryTest = new Query("Exam").setFilter(new FilterPredicate("ownerID",
        FilterOperator.EQUAL, ownerID)).setFilter(new FilterPredicate(
        "name", FilterOperator.EQUAL, testName)).addSort("date",
        SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(queryTest);
    Entity result = pq.asSingleEntity();
    return result;
  }
}