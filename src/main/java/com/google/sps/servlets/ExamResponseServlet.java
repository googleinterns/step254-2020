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
 * Servlet that processes users responses to exam questions and stores them in datastore.
 * NOT SETUP CORRECTLY YET
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
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/");
      return;
    }

    String question = request.getParameter("question");
    String email = userService.getCurrentUser().getEmail();

    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity("exam1", email);
      entity.setProperty("email", email);
      entity.setProperty("question", question);
      // The put() function automatically inserts new data or updates existing data based on email
      datastore.put(entity);
    } catch(Exception e) {
      System.out.println("Something went wrong with Datastore. Please try again later.");
    }

    response.sendRedirect("/");
  }
}
