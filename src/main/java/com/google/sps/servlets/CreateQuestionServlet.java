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
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that creates and stores questions in the datastore.
* @author Klaudia Obieglo
*/
@WebServlet("/createQuestion")
public class CreateQuestionServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  @Override
  public void doPost(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    /* Servlet Receives information from the client about the question
    * they want to save */

    Long date = (new Date()).getTime();
    String testName = UtilityClass.getParameter(request, "testName", "");
    String question = UtilityClass.getParameter(request, "question", "");
    String checkbox[] = request.getParameterValues("type");
    String type = "Normal";
    String mcqAnswer = null;
    List<String> mcqPossibleAnswers = new ArrayList<>();

    if (checkbox != null) {
      type = "MCQ";
      mcqAnswer = request.getParameter("mcqAnswer");
      String mcqAnswers[] = request.getParameterValues("mcqField");
      for(int i=0; i<mcqAnswers.length; i++) {
        String answer = mcqAnswers[i];
        answer = answer.replaceAll("\\<.*?\\>", "");
        mcqPossibleAnswers.add(answer);
      }
    } 
    //Remove all html tags and trim the spaces in the questions.
    question = question.replaceAll("\\<.*?\\>", "");
    question = question.trim();
    String marks = UtilityClass.getParameter(request, "marks", "");

    if (testName == "" || question == "" || marks == "") {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "You have entered one or more null parameters");
      logger.atWarning().log("One or more null parameters testName:%s, question:%s, marks:%s",
          testName, question, marks);
      return;
    }

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "You are not authorised to view this page");
      return;
    }
    logger.atInfo().log("user=%s is logged in", userService.getCurrentUser());
    String ownerID = userService.getCurrentUser().getEmail();
    long id = UtilityClass.generateUniqueId();
    try {
      // Create a Question Entity with the parameters provided

      Entity questionEntity = new Entity("Question", id);
      questionEntity.setProperty("question", question);
      questionEntity.setProperty("marks", marks);
      questionEntity.setProperty("date", date);
      questionEntity.setProperty("ownerID", ownerID);
      questionEntity.setProperty("type", type);
      questionEntity.setProperty("mcqAnswer", mcqAnswer);
      questionEntity.setProperty("mcqPossibleAnswers", mcqPossibleAnswers);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(questionEntity);
      addQuestionToExamList(questionEntity.getKey().getId(), ownerID,
          testName);

      response.setContentType("application/json");
      logger.atInfo().log("question created=%s", questionEntity.getKey().getId());
      response.getWriter().println(UtilityClass.convertToJson(questionEntity));
      response.sendRedirect("/questionForm");
    } catch (DatastoreFailureException e) {
        logger.atSevere().log("Datastore Failure.Datastore is not responding:"
          + " %s", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Internal Error occurred when trying to create your question");
        return;
    }
  }
  private void addQuestionToExamList(final long questionEntityKey,
      final String ownerID, final String testName) {
    /*Function that adds the question id to the list of questions
    * in the exam entity
    * Arguments
    *  -QuestionEntityKey -id of the question entity we are adding to the list
    *  -ownerID - email of the user adding this question */

    //grab the exam created by the user and with the test name provided
    try {
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
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(latestExam);
      logger.atInfo().log("Added question: %s to test: %s", questionEntityKey,
          latestExam.getKey().getId());
    } catch (Exception e) {
        logger.atSevere().log("Exam questions cannot be added: %s", e);
        return;
    }

  }
  private Entity getExam(final String ownerId, final String testName) {
    /* Function that returns the exam created by the user
    *  Arguments: ownerId - email of the user who's test we want to find
    *  Return : Returns the entity of the test created by that user with
    *  that test name.*/
    Query queryExam = new Query("Exam").setFilter(new FilterPredicate("ownerId",
        FilterOperator.EQUAL, ownerId)).setFilter(new FilterPredicate("name",
        FilterOperator.EQUAL, testName));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(queryExam);
    Entity result = pq.asSingleEntity();
    return result;
  }
}