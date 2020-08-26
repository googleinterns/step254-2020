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
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    String email = request.getParameter("email");
    logger.atInfo().log("user=%s", email);

    String name;
    String font;
    String fontSize;
    String bgColor;
    String textColor;
    name = font = fontSize = bgColor = textColor = null;

    try {
      name = request.getParameter("name");
      font = request.getParameter("font");
      fontSize = request.getParameter("font_size");
      bgColor = request.getParameter("bg_color");
      textColor = request.getParameter("text_color");
    } catch (Exception e) {
      logger.atSevere().log("One or more null parameters in try/catch");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    if (font == null || fontSize == null || bgColor == null
        || textColor == null || email == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      logger.atSevere().log("One or more null parameters");
      return;
    }

    try {
      Entity userInfoEntity = new Entity("UserInfo", email);
      userInfoEntity.setProperty("email", email);
      userInfoEntity.setProperty("name", name);
      userInfoEntity.setProperty("font", font);
      userInfoEntity.setProperty("font_size", fontSize);
      userInfoEntity.setProperty("bg_color", bgColor);
      userInfoEntity.setProperty("text_color", textColor);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(userInfoEntity);
    } catch (Exception e) {
      logger.atSevere().log("There was an error with datastore: %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    response.sendRedirect("/dashboard.html");
  }
}
