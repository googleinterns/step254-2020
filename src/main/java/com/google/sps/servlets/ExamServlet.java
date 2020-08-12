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

import com.google.sps.data.ExamClass;
import com.google.sps.data.UtilityClass;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to display the requested exam  and send responses to ExamResponseServlet or if no exam
 * is selected it will display a list of available exams.
 *
 * @author  Aidan Molloy
 */
@WebServlet("/exam")
public class ExamServlet extends HttpServlet{
  /**
   * Gets the exam ID from the httpRequest
   *
   * Arguments: 
   *  request: provides request information from the HTTP servlet.
   *  response: response object where servlet will write information to.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/");
      return;
    }
    response.setContentType("text/html;");
    PrintWriter out = response.getWriter();
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<link href=\"https://fonts.googleapis.com/css2?family=Domine:wght@400;"
      + "700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap\""
      + " rel=\"stylesheet\">");
    out.println("<link rel=\"stylesheet\" href=\"style.css\">");
    out.println("<script src=\"script.js\"></script>");
    out.println("<title>Take Exam</title>");
    out.println("</head>");
    out.println("<body>");
    out.println("<header>");
    out.println("<div class=\"navtop\">");
    out.println("<a href=\"dashboard.html\">Navigation, will have login, click here to test rest"
      + " of page</a>");
    out.println("<p id=logInOut></p>");
    out.println("</div>");
    out.println("</header>");
    out.println("<main>");
    String examID = UtilityClass.getParameter(request, "examID", null);
    Entity examEntity = null;

    if (examID != null) {
      // If an exam has been selected
      try {
        examEntity = getEntity(examID);
      } catch (EntityNotFoundException e) {
        out.println("<h3>Selected exam is not available.</h3>");
      }
      if (examEntity != null) {
        // If exam exists, then display the exam and questions
        String name = (String) examEntity.getProperty("name");
        String duration = (String) examEntity.getProperty("duration");
        String ownerID = (String) examEntity.getProperty("ownerID");
        List<Long> questionsList = (List<Long>) examEntity.getProperty("questionsList");
        ExamClass exam = new ExamClass(name,examEntity.getKey().getId()
          , Double.parseDouble(duration),ownerID,questionsList);
      
        out.println("<h1>Exam Name: " + exam.getName() + "</h1>");
        out.println("<h3>Length: " + exam.getDuration() + "</h3>");
        out.println("<h3>Created By: " + exam.getOwnerID() + "</h3>");
        if (questionsList != null) {
          DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
          out.println("<form action=\"/examResponse\" method=\"POST\">");
          for (int i=0; i < questionsList.size(); i++) {
            try {
              Key key = KeyFactory.createKey("Question", questionsList.get(i));
              Entity qs = datastore.get(key);

              long questionID = qs.getKey().getId();
              String question = (String) qs.getProperty("question");
              out.println("<label for=\"" + questionID + "\">" + (i+1) + ") " + 
                            question + ": </label>");
              out.println("<input type=\"text\" id=\"" + questionID + "\" name=\"" +
                            questionID + "\"><br><br>");

            } catch( Exception e) {
              out.println("<p>Question was not found</p><br>");
            }
          }
          out.println("<br><input type=\"submit\" value=\"Submit\">");
          out.println("</form>");
        }else{
          out.println("<p>There are no questions associated with this exam.</p>");
        }
        out.println("</main></body>");
        return;
      }
    }

    // If exam is not selected or unavailable display list of available exams
    out.println("<h1>Choose an exam to take.</h1>");
    out.println("<table><tr><th>ID</th><th>Name</th><th>Duration</th></tr>");

    Query query = new Query("Exam");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String duration = (String) entity.getProperty("duration");

      out.println("<tr>");
      out.println("<td>" + id + "</td>");
      out.println("<td>" + name + "</td>");
      out.println("<td>" + duration + "</td>");
      out.println("<td><a href=\"/exam?examID=" + id + "\">Take Exam</a></td>");
      out.println("</tr>");
    }
    out.println("</table>");
    out.println("</body>");
  }

  private Entity getEntity(String entityID) throws EntityNotFoundException{
    /*Function that will return a exam entity based on the entity ID */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey("Exam", Long.parseLong(entityID));
    Entity entity = datastore.get(key);
    if(entity == null) {
      return null;
    }
    return entity;
  }
}