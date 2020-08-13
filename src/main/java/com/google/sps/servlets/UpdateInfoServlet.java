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
import com.google.common.flogger.FluentLogger;

/**
 * Servlet to update users preferences, overwrites current preferences linked with users email
 * on the datastore.
 *
 * @author  Aidan Molloy
 */
@WebServlet("/updateInfo")
public class UpdateInfoServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  /**
   * Get passed paramaters for users new preferences and save to datastore.
   *
   * @param   request     provides request information from the HTTP servlet
   * @param   response    response object where servlet will write information to
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      logger.atInfo().log("User is not logged in.");
      response.sendRedirect("/");
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());

    String name = request.getParameter("name");
    String font = request.getParameter("font");
    String font_size = request.getParameter("font_size");
    String bg_color = request.getParameter("bg_color");
    String text_color = request.getParameter("text_color");
    String email = userService.getCurrentUser().getEmail();

    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity userInfoEntity = new Entity("UserInfo", email);
      userInfoEntity.setProperty("email", email);
      userInfoEntity.setProperty("name", name);
      userInfoEntity.setProperty("font", font);
      userInfoEntity.setProperty("font_size", font_size);
      userInfoEntity.setProperty("bg_color", bg_color);
      userInfoEntity.setProperty("text_color", text_color);
      // The put() function automatically inserts new data or updates existing data based on email
      datastore.put(userInfoEntity);
    } catch(Exception e) {
      logger.atInfo().log("There was an error: %s", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    response.sendRedirect("/dashboard.html");
  }
}
