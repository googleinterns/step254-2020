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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that processes users responses to exam questions and stores them in datastore.
 *
 * @author Aidan Molloy
 */
@WebServlet("/examResponse")
public class ExamResponseServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * doPost process the information from the exam form response and send it to the datastore.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());
    PrintWriter out = response.getWriter();
    response.setContentType("text/html");

    Enumeration<String> parameterNames = request.getParameterNames();
    try {
      String email = userService.getCurrentUser().getEmail();
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
    } catch (Exception e) {
      logger.atSevere().log("There was an error: %s", e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    out.println("<h2>Responses Saved.</h2>");
    out.println("<a href=\"/dashboard.html\">Return to dashboard</a>");
  }
}
