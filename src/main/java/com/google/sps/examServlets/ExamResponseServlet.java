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
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that processes users responses to exam questions and stores them in datastore.
 *
 * @author Aidan Molloy
 * @author Róisín O'Farrell
 */
@WebServlet("/examResponse")
public class ExamResponseServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * doPost process the information from the exam form response and send it to the datastore.
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
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());
    PrintWriter out = response.getWriter();
    response.setContentType("text/html");
    String examID = request.getParameter("examID");

    Enumeration<String> parameterNames = request.getParameterNames();
    String possibleMarks = null;
    String expected = "none";
    String type = null;
    try {
      String email = userService.getCurrentUser().getEmail();
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      while (parameterNames.hasMoreElements()) {
        System.out.println("Has more elements");
        String questionID = parameterNames.nextElement();
        System.out.println(questionID);
        if(questionID.equals("examID")){
          continue;
        }
         System.out.println("good");
        String[] questionAnswer = request.getParameterValues(questionID);
        // Correct mcq questions with pre-defined answers
        try{
          Long questionIDL = Long.parseLong(questionID); 
          // Get questionType, mcqAnswer and possibleMarks using Key as Question has a known ID/Name
          Key questionKey = KeyFactory.createKey("Question", questionIDL); 
          Entity qs = datastore.get(questionKey);
           System.out.println("got question");
          type = (String) qs.getProperty("type");
          System.out.println(type);
          if (type.equals("MCQ")){
            String mcqAnswer = (String) qs.getProperty("mcqAnswer");
            int questionNum = Integer.parseInt(mcqAnswer);
            possibleMarks = (String) qs.getProperty("marks");
            System.out.println(possibleMarks);
            List<String> answerList = (List<String>) qs.getProperty("mcqPossibleAnswers");
            expected = (String) answerList.get(questionNum-1);
          }
        } catch (Exception e) {
          System.out.println("Cannot Find Question");
          logger.atInfo().log("Cannot find question: %s", e);
        }
        Entity examResponseEntity = new Entity(questionID, email);
        examResponseEntity.setProperty("email", email);
        examResponseEntity.setProperty("answer", questionAnswer[0]);
        System.out.println(questionAnswer[0]);
        if(expected.equals(questionAnswer[0]) && type.equals("MCQ")){
          examResponseEntity.setProperty("marks", possibleMarks);
        } else if(!expected.equals(questionAnswer[0]) && type.equals("MCQ")){
          examResponseEntity.setProperty("marks", "0");
        } else{
          examResponseEntity.setProperty("marks", null);
        }
        datastore.put(examResponseEntity); 
        System.out.println("in dataStore");
      }
      System.out.println("marking exam taken");
      if(email != null && examID != null){
        examTaken(email, Long.parseLong(examID));
      }
    } catch (Exception e) {
      logger.atSevere().log("There was an error: %s", e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    out.println("<h2>Responses Saved.</h2>");
    out.println("<a href=\"/dashboardServlet\">Return to dashboard</a>");
  }

  public void examTaken(String email, Long examID) {
    /*Marks what exam a user has taken by storing that exam id in their 
    * UserInfo.
    */
    System.out.println("in exam taken");
    Query queryUser = new Query("UserExams").setFilter(new FilterPredicate(
          "email", FilterOperator.EQUAL, email));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(queryUser);
    Entity user = pq.asSingleEntity();
    if (user != null){
      System.out.println("got entity");
      //add to examsTaken list
      if (user.getProperty("taken") == null) {
        System.out.println("taken property is null");
        List<Long> examsTakenList = new ArrayList<>();
        examsTakenList.add(examID);
         System.out.println("taken list has curr exam");
        user.setProperty("taken", examsTakenList);
         System.out.println("set as taken");
      } else {
        System.out.println("taken not null");
        List<Long> examsTakenList =
        (List<Long>) user.getProperty("taken");
        examsTakenList.add(examID);
        System.out.println("taken list has curr exam");
        user.setProperty("taken", examsTakenList);
        System.out.println("set as taken");
      }
      //remove examID from exams To Do list as exam has been taken
      if(user.getProperty("available") != null) {
        System.out.println("was available");
        List<Long> availableExams =
        (List<Long>) user.getProperty("available");
        availableExams.remove(Long.valueOf(examID));
        System.out.println("removed from available");
      }
      datastore.put(user);
      System.out.println("added to datastore");
    } else {
      Entity userExamEntity = new Entity("UserExams", email);
      userExamEntity.setProperty("available", null);
      userExamEntity.setProperty("created", new ArrayList<>());
      userExamEntity.setProperty("email", email);
      List<Long> examsTakenList = new ArrayList<>();
      examsTakenList.add(examID);
      userExamEntity.setProperty("taken", examsTakenList);
      datastore.put(userExamEntity); 
      System.out.println("added to datastore");
    }
  }
}

