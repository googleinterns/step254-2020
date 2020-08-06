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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to send user responses to the datastore
 */

@WebServlet("/examResponse")
public class ExamResponseServlet extends HttpServlet {

  /**
   * doGet prints form to page for changing username
   */

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<h1>Answer a test exam here</h1>");

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      out.println("<p>Answer a test exam here:</p>");
      out.println("<form method=\"POST\" action=\"/examResponse\">");
      out.println("<input type=\"text\" name=\"question1\" />");
      out.println("<input type=\"text\" name=\"question2\" />");
      out.println("<input type=\"text\" name=\"question3\" />");
      out.println("<input type=\"text\" name=\"question4\" />");
      out.println("<input type=\"text\" name=\"question5\" />");
      out.println("<br/>");
      out.println("<button>Submit</button>");
      out.println("</form>");
    } else {
      String loginUrl = userService.createLoginURL("/");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
  }

  /**
   * doPost process the information from the exam form response and send it to the datastore
   */
   
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/");
      return;
    }

    String question1 = request.getParameter("question1");
    String question2 = request.getParameter("question2");
    String question3 = request.getParameter("question3");
    String question4 = request.getParameter("question4");
    String question5 = request.getParameter("question5");
    String email = userService.getCurrentUser().getEmail();

    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity("exam1", email);
      entity.setProperty("email", email);
      entity.setProperty("question1", question1);
      entity.setProperty("question2", question2);
      entity.setProperty("question3", question3);
      entity.setProperty("question4", question4);
      entity.setProperty("question5", question5);
      // The put() function automatically inserts new data or updates existing data based on email
      datastore.put(entity);
    } catch(Exception e) {
      System.out.println("Something went wrong with Datastore. Please try again later.");
    }

    response.sendRedirect("/");
  }
}
