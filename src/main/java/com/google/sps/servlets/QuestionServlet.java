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

/** Servlet that stores questions*/
@WebServlet("/question")
public class QuestionServlet extends HttpServlet{
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /* Servlet Receives information from the client about the question they want to save */
    Long date = (new Date()).getTime();
    String question = getParameter(request, "question", "");
    String marks = getParameter(request, "marks", "");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    HttpSession session = request.getSession();
    String testid = (String)session.getAttribute("testid");

    // Create a Question Entity with the parameters provided
    Entity questionEntity = new Entity(testid);
    questionEntity.setProperty("question",question);
    questionEntity.setProperty("marks",marks);
    questionEntity.setProperty("questionID",questionEntity.getKey().getId());
    questionEntity.setProperty("date",date);
    datastore.put(questionEntity);

    response.sendRedirect("/createTest.html");
    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(questionEntity));
  }
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    /*Gets the questions for the last test saved in a HTTP session
    *
    * Arguments: 
    *   request: provides request information from the HTTP servlet
    *   response: response object where servlet will write information on
    */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    HttpSession session = request.getSession();
    String testid = (String)session.getAttribute("testid");

    /* Look for all the questions that have the required test ID 
    * sort them in ascending order so that questions will appear in the order
    * that they were inserted in
    */
    Query queryQs = new Query(String.valueOf(testid)).addSort("date", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(queryQs);
    List<QuestionClass> questionList = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long questionID = entity.getKey().getId();
      String question = (String) entity.getProperty("question");
      String marks = (String) entity.getProperty("marks");
      QuestionClass question1 = new QuestionClass(question, questionID,
        Double.parseDouble(marks), Long.valueOf(testid));
      questionList.add(question1);
    }

    response.setContentType("application/json;");
    // response.sendRedirect("/createTest.html");
    response.getWriter().println(convertToJsonUsingGson(questionList));
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