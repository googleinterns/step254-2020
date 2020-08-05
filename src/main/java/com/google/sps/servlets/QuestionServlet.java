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

/** Servlet that stores questions*/
@WebServlet("/question")
public class QuestionServlet extends HttpServlet{
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /* Servlet Receives information from the client about the question they want to save */
    String question = getParameter(request, "question", "");
    String marks = getParameter(request, "marks", "");

    HttpSession session = request.getSession();
    Long testID = (Long) session.getAttribute("testid");

    // Create a Question Entity with the parameters provided
    Entity questionEntity = new Entity((String.valueOf(testID)));
    questionEntity.setProperty("question",question);
    questionEntity.setProperty("marks",marks);
    questionEntity.setProperty("questionID",questionEntity.getKey().getId());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(questionEntity);

    response.sendRedirect("/createTest.html");
    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(questionEntity));
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
}