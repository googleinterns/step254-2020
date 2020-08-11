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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import javax.servlet.ServletException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.PrintWriter;

/** Servlet that returns a checkbox form of all the questions owned by the user
* A user can then select the questions they want to reuse by clicking on the 
* checkboxes.
* @author Klaudia Obieglo
*/
@WebServlet("/returnQuestionsUserOwns")
public class QuestionsUserOwnsServlet extends HttpServlet{
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    /*Returns all the questions that the user has created */
    UserService userService = UserServiceFactory.getUserService();
    String ownerID = userService.getCurrentUser().getEmail(); 

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Question").setFilter(new FilterPredicate("ownerID",
      FilterOperator.EQUAL, ownerID)).addSort("date", SortDirection.ASCENDING);;
    PreparedQuery results = datastore.prepare(query);

    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<link href=\"https://fonts.googleapis.com/css2?family=Domine:wght@400;"+
      "700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap\""+
      " rel=\"stylesheet\">");
    out.println("<link rel=\"stylesheet\" href=\"style.css\">");
    out.println("<script src=\"script.js\"></script>");
    out.println("</head>");
    out.println("<body>");
    out.println("<header>");
    out.println("<div class=\"navtop\">");
    out.println("<a href=\"dashboard.html\">Navigation, will have login, click here to test rest"+
      " of page</a>");
    out.println("<p id=logInOut></p>");
    out.println("</div>");
    out.println("</header>");
    out.println("<main>");
    out.println("<body>");
    out.println("<h1>Check the boxes of questions you would like to reuse</h1>");
    out.println("<form action=\"/saveQuestionsFromBank\" method=\"POST\">");
    for (Entity entity : results.asIterable()) {
      long questionId = entity.getKey().getId();
      String question = (String) entity.getProperty("question");
      String marks = (String) entity.getProperty("marks");
      out.println("<input type=\"checkbox\" name=\"question\" value=\""+String.valueOf(questionId)+
        "\">"+question+" ("+marks+")<br>");
    }
    out.println("<br/>");
    out.println("<button>Submit</button>");
    out.println("</form>");
    out.println("</body>");
  }
}