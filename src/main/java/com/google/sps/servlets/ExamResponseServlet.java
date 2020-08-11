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
import java.util.Enumeration;

/**
 * Servlet that processes users responses to exam questions and stores them in datastore.
 *
 * @author  Aidan Molloy
 */
@WebServlet("/examResponse")
public class ExamResponseServlet extends HttpServlet {

  /**
   * doPost process the information from the exam form response and send it to the datastore
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    PrintWriter out = response.getWriter();
    response.setContentType("text/html");

    Enumeration<String> parameterNames = request.getParameterNames();
    String email = userService.getCurrentUser().getEmail();    
    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      while (parameterNames.hasMoreElements()) {
        String questionID = parameterNames.nextElement();
        String[] questionAnswer = request.getParameterValues(questionID);
        Entity examResponseEntity = new Entity(questionID, email);
        examResponseEntity.setProperty("email", email);
        examResponseEntity.setProperty("answer", questionAnswer[0]);
        // This is where I can correct questions with pre-defined answers
        examResponseEntity.setProperty("marks", null);
        datastore.put(examResponseEntity);
      }
    } catch(Exception e) {
      System.out.println("Something went wrong with Datastore. Please try again later.");
    }
    out.println("<h2>Responses Saved.</h2>");
    out.println("<a href=\"/dashboard.html\">Return to dashboard</a>");
  }
}
