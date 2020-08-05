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

/** Servlet that returns the tests owned by the user*/
@WebServlet("/returnTestsUserOwns")
public class TestsUserOwnsServlet extends HttpServlet{
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /*Returns the tests that the user has created */
    UserService userService = UserServiceFactory.getUserService();
    String ownerID = userService.getCurrentUser().getEmail(); 

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Test").setFilter(new FilterPredicate("ownerID",
      FilterOperator.EQUAL, ownerID));
    PreparedQuery results = datastore.prepare(query);

    List<TestClass> testList = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long testId = entity.getKey().getId();
      String testName = (String) entity.getProperty("testName");
      String testDuration = (String) entity.getProperty("testDuration");
      String ownerId = (String) entity.getProperty("ownerID");
      TestClass test = new TestClass(testName,testId,Double.valueOf(testDuration),ownerID);
      testList.add(test);
    }

    response.setContentType("application/json;");
    response.sendRedirect("/createTest.html");
    response.getWriter().println(convertToJsonUsingGson(testList));
  }
  private String convertToJsonUsingGson(List<TestClass> questions) {
    /* Converts the test List to a json string using Gson
    *
    *Arguments: Question testList that is populated with tests
    *
    *Returns: json string of the tests
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(questions);
    return json;
  }
}