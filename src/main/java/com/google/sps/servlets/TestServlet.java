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

/** Servlet that stores and returns tests*/
@WebServlet("/test")
public class TestServlet extends HttpServlet{
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //Servlet Recevies information from the client about a test they want to create and saves it in
    //the datastore
    Long date = (new Date()).getTime(); 
    String testName= getParameter(request, "testName","");
    String testDuration = getParameter(request, "duration", "");

    UserService userService = UserServiceFactory.getUserService();
    String ownerID = userService.getCurrentUser().getEmail(); 

    //set up the new Test and save it in the datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity testEntity = new Entity("Test");
    testEntity.setProperty("testName",testName);
    testEntity.setProperty("testDuration",testDuration);
    testEntity.setProperty("ownerID",ownerID);
    testEntity.setProperty("date", date);
    testEntity.setProperty("testid",testEntity.getKey());
    datastore.put(testEntity);    

    HttpSession session = request.getSession();
    session.setAttribute("testid",String.valueOf(testEntity.getKey().getId()));

    response.sendRedirect("/createTest.html");
    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(testEntity));
  }
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    /*Gets the last test that is saved in the users session
    *
    * Arguments: 
    *   request: provides request information from the HTTP servlet
    *   response: response object where servlet will write information on
    */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    HttpSession session = request.getSession();
    String testid = (String)session.getAttribute("testid");

    Entity latestTest= new Entity("Test");
    try{
      latestTest = getEntity("Test",testid);
    } catch( EntityNotFoundException e)
    {
      System.out.println("Entity was not found");
    }
    String testName = (String) latestTest.getProperty("testName");
    String testDuration = (String) latestTest.getProperty("testDuration");
    String ownerId = (String) latestTest.getProperty("ownerID");
    TestClass test = new TestClass(testName,Long.valueOf(String.valueOf(testid)),
      Double.parseDouble(testDuration),ownerId);

    response.setContentType("application/json;");
    // response.sendRedirect("/createTest.html");
    response.getWriter().println(convertToJsonUsingGson(test));
    }
    
    private Entity getEntity(String kind, String idName) throws EntityNotFoundException{
      //Function that will return an entity that is of specified kind and id
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Key key = KeyFactory.createKey(kind, Long.parseLong(idName));
      Entity test = datastore.get(key);
      return test;
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
    private String convertToJsonUsingGson(List<QuestionClass> questions) {
    /* Converts the question List to a json string using Gson
    *
    *Arguments: Question ArrayList that is populated with questions
    *
    *Returns: json string of the questions
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(questions);
    return json;
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
  private String convertToJsonUsingGson(Entity test) {
    /* Converts the test entity to a json string using Gson
    *
    *Arguments: Test entity
    *
    *Returns: json string of the test entity
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(test);
    return json;
  }
}
