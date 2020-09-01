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
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that creates and stores exams in the datastore.
* @author Klaudia Obieglo.
*/
@WebServlet("/createExam")
public class CreateExamServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  @Override
  public void doPost(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    /*Servlet Receives information from the client about an exam they
    * want to create and saves it in the datastore
    */
    Long date = (new Date()).getTime();
    String name = UtilityClass.getParameter(request, "name", "");
    // Remove all html tags and trim the spaces in the exam name.
    name = name.replaceAll("\\<.*?\\>", "");
    name = name.trim();
    String duration = UtilityClass.getParameter(request, "duration", "");
    if (name == "" || duration == "") {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        "You have entered one or more null parameters");
      logger.atWarning().log("One or more null parameters, name:%s, duration:%s",
          name, duration);
      return;
    }
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn() || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "You are not authorised to view this page");
      return;
    }
    logger.atInfo().log("User =%s is logged in", userService.getCurrentUser());
    String ownerID = userService.getCurrentUser().getEmail();
    Random rd = new Random();
    Long id = rd.nextLong();
    //Set up the new Exam and save it in the datastore
    try {
      Entity examEntity = new Entity("Exam",rd.nextLong());
      examEntity.setProperty("name", name);
      examEntity.setProperty("duration", duration);
      examEntity.setProperty("ownerID", ownerID);
      examEntity.setProperty("date", date);
      examEntity.setProperty("questionsList", new ArrayList<>());
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(examEntity);
      logger.atInfo().log("Exam: %s , was saved successfully in the datastore",
          examEntity.getKey().getId());
      response.sendRedirect("/questionForm");
      response.setContentType("application/json");
      response.getWriter().println(UtilityClass.convertToJson(examEntity));

    } catch (DatastoreFailureException e) {
      logger.atSevere().log("Error with datastore: %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to create your exam");
      return;
    }
  }
}
