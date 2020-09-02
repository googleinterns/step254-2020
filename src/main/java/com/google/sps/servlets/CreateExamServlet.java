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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that creates and stores exams in the datastore and assigns exams to users.
* @author Klaudia Obieglo & Aidan Molloy.
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
    String groupID = UtilityClass.getParameter(request, "groupID", "");
    groupID = groupID.replaceAll("\\<.*?\\>", "");
    groupID = groupID.trim();
    logger.atInfo().log("group=%s", groupID);
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
    Long id = UtilityClass.generateUniqueId();
    Long examID = 0L;
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    //Set up the new Exam and save it in the datastore
    try {
      Entity examEntity = new Entity("Exam", id);
      examEntity.setProperty("name", name);
      examEntity.setProperty("duration", duration);
      examEntity.setProperty("ownerID", ownerID);
      examEntity.setProperty("date", date);
      examEntity.setProperty("questionsList", new ArrayList<>());
      datastore.put(examEntity);
      examID = examEntity.getKey().getId();
      logger.atInfo().log("Exam: %s , was saved successfully in the datastore",
          examEntity.getKey().getId());
      examID = examEntity.getKey().getId();
      response.setContentType("application/json");
      response.getWriter().println(UtilityClass.convertToJson(examEntity));

    } catch (DatastoreFailureException e) {
      logger.atSevere().log("Error with datastore: %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to create your exam");
      return;
    }

    // If a group is selected
    if (groupID != "") {
      Entity groupEntity = new Entity("Group");
      try {
        Key key = KeyFactory.createKey("Group", Long.parseLong(groupID));
        groupEntity = datastore.get(key);
      } catch (Exception e) {
        logger.atWarning().log("Group ID does not exist");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Group ID does not exist");
        return;
      }
      List<String> members = null;
      try {
        members = (List<String>) groupEntity.getProperty("members");
      } catch (Exception e) {
        logger.atWarning().log("There was an error getting the members list: %s", e);
      }
      try {
        if (members == null) {
          logger.atWarning().log("There are no members in this group");
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              "There are no members in this group please select anotehr group");
          return;
        } else {
          // Add exam to every member of the group
          for (int i = 0; i < members.size();i++){
            String email = members.get(i);
            try {
              Query getUserExams = new Query("UserExams").setFilter(new FilterPredicate("email",
                  FilterOperator.EQUAL, email));
              PreparedQuery pq = datastore.prepare(getUserExams);
              Entity userExamsEntity = pq.asSingleEntity();
              List<Long> available = null;
              if (userExamsEntity == null) {
                userExamsEntity = new Entity("UserExams", email);
                userExamsEntity.setProperty("email", email);
                userExamsEntity.setProperty("taken", new ArrayList<Long>());
                available = new ArrayList<Long>();
              } else {
                available = (List<Long>) userExamsEntity.getProperty("available");
                if (available == null) {
                  available = new ArrayList<Long>();
                }
              }
              available.add(examID);
              userExamsEntity.setProperty("available", available);
              datastore.put(userExamsEntity);
            } catch (Exception e) {
              logger.atWarning().log("Problem while giving exams to users: %s", e);
              response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                  "Problem while giving exams to users");
              return;
            }
          }
        }
      } catch (Exception e) {
        logger.atWarning().log("Problem while giving exams to users: %s", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Problem while giving exams to users");
        return;
      }
    }
    response.sendRedirect("/questionForm");
  }
}
