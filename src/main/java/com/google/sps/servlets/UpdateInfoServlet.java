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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to update users preferences, overwrites current preferences linked with users email
 * on the datastore.
 *
 * @author Aidan Molloy
 */
@WebServlet("/updateInfo")
public class UpdateInfoServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * Get passed parameters for users new preferences and save to datastore.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      logger.atWarning().log("User is not logged in.");
      response.sendRedirect("/");
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());

    String name, font, font_size, bg_color, text_color, email;
    name = font = font_size = bg_color = text_color = email = null;

    try {
      name = request.getParameter("name");
      font = request.getParameter("font");
      font_size = request.getParameter("font_size");
      bg_color = request.getParameter("bg_color");
      text_color = request.getParameter("text_color");
      email = userService.getCurrentUser().getEmail();
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      logger.atSevere().log("One or more null parameters");
    }

    if (name == null || font == null || font_size == null || bg_color == null ||
        text_color == null || email == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      logger.atSevere().log("One or more null parameters");
      return;
    }

    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity userInfoEntity = new Entity("UserInfo", email);
      userInfoEntity.setProperty("email", email);
      userInfoEntity.setProperty("name", name);
      userInfoEntity.setProperty("font", font);
      userInfoEntity.setProperty("font_size", font_size);
      userInfoEntity.setProperty("bg_color", bg_color);
      userInfoEntity.setProperty("text_color", text_color);
      datastore.put(userInfoEntity);
    } catch (Exception e) {
      logger.atSevere().log("There was an error with datastore: %s", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    response.sendRedirect("/dashboard.html");
  }
}
