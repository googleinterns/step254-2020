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
 
import com.google.sps.data.ExamClass;
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
import java.util.*;
import javax.servlet.ServletException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that returns the exams owned by the user*/
@WebServlet("/returnExamsUserOwns")
public class ExamsUserOwnsServlet extends HttpServlet{
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /*Returns the exams that the user has created */
    UserService userService = UserServiceFactory.getUserService();
    String ownerID = userService.getCurrentUser().getEmail(); 

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Exam").setFilter(new FilterPredicate("ownerID",
      FilterOperator.EQUAL, ownerID));
    PreparedQuery results = datastore.prepare(query);

    List<ExamClass> examList = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long examID = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String duration = (String) entity.getProperty("duration");
      String ownerId = (String) entity.getProperty("ownerID");
      List<Long> list = (List<Long>) entity.getProperty("questionsList");
      ExamClass exam = new ExamClass(name,examID,Double.valueOf(duration),ownerID,list);
      examList.add(exam);
    }

    response.setContentType("application/json;");
    response.sendRedirect("/createExam.html");
    response.getWriter().println(convertToJsonUsingGson(examList));
  }
  private String convertToJsonUsingGson(List<ExamClass> questions) {
    /* Converts the exam List to a json string using Gson
    *
    *Arguments: Question examList that is populated with exams
    *
    *Returns: json string of the exams
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(questions);
    return json;
  }
}