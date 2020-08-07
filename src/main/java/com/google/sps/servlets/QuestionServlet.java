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
import com.google.sps.data.QuestionClass;
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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.gson.Gson;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.Date;
import javax.servlet.ServletException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that stores questions*/
@WebServlet("/question")
public class QuestionServlet extends HttpServlet{
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /* Servlet Receives information from the client about the question they want to save */
    Long date = (new Date()).getTime();
    String question = getParameter(request, "question", "");
    String marks = getParameter(request, "marks", "");
    
    UserService userService = UserServiceFactory.getUserService();
    String ownerID = userService.getCurrentUser().getEmail(); 

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Create a Question Entity with the parameters provided
    Entity questionEntity = new Entity("Question");
    questionEntity.setProperty("question",question);
    questionEntity.setProperty("marks",marks);
    questionEntity.setProperty("date",date);
    questionEntity.setProperty("ownerID",ownerID);
    try{
      datastore.put(questionEntity);    
    }catch (DatastoreFailureException e){
      System.out.println("Datastore is not responding right now. Try Again Later");
    }
    addQuestionToTestList(questionEntity.getKey().getId(),ownerID);

    response.sendRedirect("/createTest.html");
    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(questionEntity));
  }
  private void addQuestionToTestList(long questionEntityKey,String ownerID)
  {
    //Function that adds the question id to the list of questions in the test entity
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    //grab the latest test created by the user
    Entity latestTest = getTest(ownerID);
    if(latestTest.getProperty("questionsList") == null){
      List<Long> questionList = new ArrayList<>();
      questionList.add(questionEntityKey);
      latestTest.setProperty("questionsList",questionList);
    }else{
      List<Long> questionList = (List<Long>)latestTest.getProperty("questionsList");
      questionList.add(questionEntityKey);
      latestTest.setProperty("questionsList",questionList);
    }
    datastore.put(latestTest);
  }
  private Entity getTest(String ownerID){
    // Function that returns the latest test created by the user
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query queryTest = new Query("Test").setFilter(new FilterPredicate("ownerID",
      FilterOperator.EQUAL, ownerID)).addSort("date", SortDirection.DESCENDING);
    PreparedQuery pq = datastore.prepare(queryTest);
    List<Entity> tests = pq.asList(FetchOptions.Builder.withLimit(1));
    return tests.get(0);
  }
  private String getParameter(HttpServletRequest request, String name, String defaultValue){
    /* Gets Parameters from the Users Page
     *
     * Return: Returns the requested parameter or the default value if the parameter
     *  wasn't specified by the User.   
     */
    String value = request.getParameter(name);
    if(value == null){
        return defaultValue;
    }
    return value;
  }
  private String convertToJsonUsingGson(Entity question) {
    /* Converts the question to a json string using Gson
    *
    *Arguments: question Entity 
    *
    *Returns: json string of the question
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(question);
    return json;
  }
    private String convertToJsonUsingGson(List<QuestionClass> questions) {
    /* Converts the question List to a json string using Gson
    *
    *Arguments: List of questions
    *
    *Returns: json string of the questions
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(questions);
    return json;
  }
}