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

import java.io.IOException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import javax.servlet.ServletException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


/** Servlet that saves selected questions to the test
* @author Klaudia Obieglo
*/
@WebServlet("/saveQuestionsFromBank")
public class SaveQuestionsFromBankServlet extends HttpServlet{
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
    /*Saves the selected questions from the question bank to the latest test 
    * that the user has created
    */
    UserService userService = UserServiceFactory.getUserService();
    String ownerID = userService.getCurrentUser().getEmail(); 
    String[] questionsList = request.getParameterValues("question");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    for(int i=0; i<questionsList.length; i++){
      addQuestionToExamList(Long.valueOf(questionsList[i]),ownerID);
      response.getWriter().println("Successfully added Question " + questionsList[i]);
    }
    response.sendRedirect("/createQuestion.html");
  }
  private void addQuestionToExamList(long questionEntityKey,String ownerID)
  {
    /*Function that adds the question id to the list of questions to the latest
    *  test created by the user
    * Arguments : QuestionEntityKey - id of the question Entity we are adding to the list
    *           : ownerID - email of the person who is adding this question to their test
    */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    //grab the latest exam created by the user
    Entity latestExam = getExam(ownerID);
    if(latestExam.getProperty("questionsList") == null){
      List<Long> questionList = new ArrayList<>();
      questionList.add(questionEntityKey);
      latestExam.setProperty("questionsList",questionList);
    }else{
      List<Long> questionList = (List<Long>)latestExam.getProperty("questionsList");
      questionList.add(questionEntityKey);
      latestExam.setProperty("questionsList",questionList);
    }
    datastore.put(latestExam);
  }
  private Entity getExam(String ownerID){
    /* Function that returns the latest exam created by the user
    *  Arguments: ownerID - email of the user who's last exam we want to find
    *  Return : Returns the entity of the last exam created by that user.
    */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query queryTest = new Query("Exam").setFilter(new FilterPredicate("ownerID",
      FilterOperator.EQUAL, ownerID)).addSort("date", SortDirection.DESCENDING);
    PreparedQuery pq = datastore.prepare(queryTest);
    List<Entity> exams = pq.asList(FetchOptions.Builder.withLimit(1));
    return exams.get(0);
  }
}