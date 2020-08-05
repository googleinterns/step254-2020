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
 * Servlet to set users preferences linked with specific emails
 */
@WebServlet("/updateInfo")
public class UpdateInfoServlet extends HttpServlet {

  /**
   * doGet prints form to page for changing username
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<h1>Set Preferences</h1>");

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      out.println("<p>Set your preferences here:</p>");
      out.println("<form method=\"POST\" action=\"/updateInfo\">");
      out.println("<input type=\"text\" name=\"name\" />");
      out.println("<input type=\"text\" name=\"font\" />");
      out.println("<input type=\"text\" name=\"font_size\" />");
      out.println("<input type=\"text\" name=\"bg_color\" />");
      out.println("<input type=\"text\" name=\"text_color\" />");
      out.println("<br/>");
      out.println("<button>Submit</button>");
      out.println("</form>");
    } else {
      String loginUrl = userService.createLoginURL("/");
      out.println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
  }

  /**
   * doPost process the information from the form links the new preferences to 
   * the associated logged in email
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/");
      return;
    }

    String name = request.getParameter("name");
    String font = request.getParameter("font");
    String font_size = request.getParameter("font_size");
    String bg_color = request.getParameter("bg_color");
    String text_color = request.getParameter("text_color");
    String email = userService.getCurrentUser().getEmail();

    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity("UserInfo", email);
      entity.setProperty("email", email);
      entity.setProperty("name", name);
      entity.setProperty("font", font);
      entity.setProperty("font_size", font_size);
      entity.setProperty("bg_color", bg_color);
      entity.setProperty("text_color", text_color);
      // The put() function automatically inserts new data or updates existing data based on email
      datastore.put(entity);
    } catch(Exception e) {
      System.out.println("Something went wrong with Datastore. Please try again later.");
    }

    response.sendRedirect("/");
  }
}
