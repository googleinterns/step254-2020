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

import com.google.sps.data.TestClass;
import com.google.sps.data.QuestionClass;
import java.io.IOException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.gson.Gson;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import javax.servlet.ServletException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that returns the test asked for*/
@WebServlet("/takeTest")
public class TakeTestServlet extends HttpServlet{
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    /*Gets the test that's id is in the request
    *
    * Arguments: 
    *   request: provides request information from the HTTP servlet
    *   response: response object where servlet will write information on
    */

    String testId = getParameter(request,"testID","");
    Entity latestTest= new Entity("Test");
    try{
      latestTest = getEntity(testId);
    }catch (EntityNotFoundException e){
      System.out.println("Entity was not found");
    }
    String testName = (String) latestTest.getProperty("testName");
    String testDuration = (String) latestTest.getProperty("testDuration");
    String ownerId = (String) latestTest.getProperty("ownerID");
    List<Long> list = (List<Long>) latestTest.getProperty("questionsList");
    TestClass test = new TestClass(testName,latestTest.getKey().getId(),
      Double.parseDouble(testDuration),ownerId,list);
    
    List<QuestionClass> listofQuestions=getQuestionsFromTest(list);

    response.setContentType("application/json;");
    response.getWriter().println(convertToJsonUsingGson(test));
    response.getWriter().println(convertToJsonUsingGson(listofQuestions));
    System.out.println(convertToJsonUsingGson(test));
  }
  private List<QuestionClass> getQuestionsFromTest(List<Long> list){
    /*Grab all the question id's from the list and creates instances of 
    *questions from them.
    */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<QuestionClass> questionList = new ArrayList<>();
    for(int i=0 ; i<list.size(); i++)
    {
      try{
        Key key = KeyFactory.createKey("Question", list.get(i));
        Entity qs = datastore.get(key);

        long questionID = qs.getKey().getId();
        String question = (String) qs.getProperty("question");
        String marks = (String) qs.getProperty("marks");
        String ownerID = (String) qs.getProperty("ownerID");
        QuestionClass question1 = new QuestionClass(question, questionID,
          Double.parseDouble(marks), ownerID);
        questionList.add(question1);
      } catch( EntityNotFoundException e)
      {  
        System.out.println("Entity was not found");
      }
    }
    return questionList;
  }
  private Entity getEntity(String entityId) throws EntityNotFoundException{
    /*Function that will return a test entity based on the entity ID */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey("Test", Long.parseLong(entityId));
    Entity entity = datastore.get(key);
    return entity;
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
   private String convertToJsonUsingGson(TestClass test) {
    /* Converts the test to a json string using Gson
    *
    *Arguments: Test instance
    *
    *Returns: json string of the test instance
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(test);
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