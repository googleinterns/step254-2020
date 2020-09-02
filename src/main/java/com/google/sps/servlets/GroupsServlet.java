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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to display the form to create a group.
 *
 * @author Aidan Molloy
 */
@WebServlet("/groups")
public class GroupsServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * Display the form for creating a group.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Only logged in users should access this page.
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()
        || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendRedirect("/");
      return;
    }
    logger.atInfo().log("user=%s", userService.getCurrentUser());
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
    out.println("<title>Groups</title>");
    out.println("</head>");
    out.println("<body>");
    out.println("<header>");
    out.println("<div class=\"navtop\">");
    out.println("<p><a href=\"index.html\">Homepage</a></p>");
    out.println("<p><a href=\"dashboard.html\">Dashboard</a></p>");
    out.println("<p id=logInOut></p>");
    out.println("</div>");
    out.println("</header>");
    out.println("<main>");
    String groupID = UtilityClass.getParameter(request, "groupID", null);
    Entity groupEntity = null;

    if (groupID != null) {
      // If an exam has been selected remove html tags and trim the ID
      groupID = groupID.replaceAll("\\<.*?\\>", "");
      groupID = groupID.trim();
      try {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey("Group", Long.parseLong(groupID));
        groupEntity = datastore.get(key);
      } catch (Exception e) {
        out.println("<h3>Selected group is not available.</h3>");
        logger.atInfo().log("Group ID does not exist: %s", e);
      }
      if (groupEntity != null) {
        // If group exists, then display the group and members
        String name = (String) groupEntity.getProperty("name");
        String description = (String) groupEntity.getProperty("description");
        final String ownerID = (String) groupEntity.getProperty("ownerID");
        List<String> members = null;
        try {
          members = (List<String>) groupEntity.getProperty("members");
        } catch (Exception e) {
          logger.atWarning().log("There was an error getting the members list: %s", e);
        }
        out.println("<h1>Group Name: " + name + "</h1>");
        out.println("<h3>Description: " + description + "</h3>");
        out.println("<h3>Owner: " + ownerID + "</h3>");

        // If the current user is the owner they can add and remove members
        if (ownerID.equals(userService.getCurrentUser().getEmail())) {
          if (members != null) {
            out.println("<table>");
            out.println("<tr><th>User Email</th><th>Remove</th></tr>");
            for (int i = 0; i < members.size(); i++) {
              // For each member display their email and a button to remove
              out.println("<tr><td>" + members.get(i) + "</td><td>");
              out.println("<form action=\"/editGroup\" method=\"POST\">");
              out.println("<input type=\"hidden\" id=\"groupID\" name=\"groupID\" "
                  + "value=" + groupID + ">");
              out.println("<input type=\"hidden\" id=\"editType\" name=\"editType\" "
                  + "value=\"remove\">");
              out.println("<input type=\"hidden\" id=\"email\" name=\"email\" "
                  + "value=" + members.get(i) + ">");
              out.println("<input type=\"submit\" id=\"removeMember\" name=\"removeMember\""
                  + "value=\"Remove\"> </td></tr>");
              out.println("</form>");
            }
          } else {
            out.println("<p>There are no members in this group yet</p>");
          }
          out.println("</table><br>");
          out.println("<h2>Add Member</h2>");
          out.println("<form action=\"/editGroup\" method=\"POST\">");
          out.println("<input type=\"hidden\" id=\"groupID\" name=\"groupID\" "
              + "value=" + groupID + ">");
          out.println("<input type=\"hidden\" id=\"editType\" name=\"editType\" value=\"add\">");
          out.println("<label for=\"email\">Enter User's Email:</label><br>");
          out.println("<input type=\"text\" id=\"email\" name=\"email\" required>");
          out.println("<input type=\"submit\" id=\"addMember\" name=\"addMember\" "
              + "value=\"Add Member\">");
          out.println("</form>");

        } else {
          if (members != null) {
            out.println("<p>Number of members: " + members.size() + "</p>");
          } else {
            out.println("<p>There are no members in this group yet</p>");
          }
        }
        out.println("</main></body>");
        return;
      }
    }

    out.println("<h2>Create a Group</h2>");
    out.println("<form id=\"createGroup\" action=\"/editGroup\" method=\"POST\">");
    out.println("<input type=\"hidden\" id=\"editType\" name=\"editType\" value=\"create\">");
    out.println("<label for=\"name\">Enter Group Name:</label><br>");
    out.println("<input type=\"text\" id=\"name\" name=\"name\" required><br>");
    out.println("<label for=\"name\">Enter Group Description:</label><br>");
    out.println("<input type=\"text\" id=\"description\" name=\"description\"><br>");
    out.println("<input type=\"submit\" value=\"Submit\">");
    out.println("</form>");

    try {
      Query getUserGroups = new Query("UserGroup").setFilter(new FilterPredicate("email",
          FilterOperator.EQUAL, userService.getCurrentUser().getEmail()));
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery pq = datastore.prepare(getUserGroups);
      Entity userGroupEntity = pq.asSingleEntity();
      List<Long> owner = null;
      List<Long> member = null;
      if (userGroupEntity != null) {
        owner = (List<Long>) userGroupEntity.getProperty("owner");
        if (owner == null) {
          owner = new ArrayList<Long>();
        }
        member = (List<Long>) userGroupEntity.getProperty("member");
        if (member == null) {
          member = new ArrayList<Long>();
        }
      }
      out.println("<h3>Owned groups</h3>");
      if(owner == null){
        out.println("<p>You do not own any groups.</p>");
      } else {
        out.println("<table><tr><th>Group name</th><th>Members</th></tr>");
        for (int i = 0; i < owner.size(); i++) {
          try {
            Key key = KeyFactory.createKey("Group", owner.get(i));
            Entity gs = datastore.get(key);
            long id = gs.getKey().getId();
            String groupName = (String) gs.getProperty("name");
            List<String> members = (List<String>) gs.getProperty("members");
            if (members == null) {
              members = new ArrayList<String>();
            }
            out.println("<tr>");
            out.println("<td>" + groupName + "</td>");
            out.println("<td>" + members.size() + "</td>");
            out.println("<td><a href=\"/groups?groupID=" + id + "\">View/Edit</a></td>");
            out.println("</tr>");
          } catch (Exception e) {
            logger.atWarning().log("Problem while searching groups user owns: %s", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Problem while searching groups user owns");
            return;
          }
        }
        out.println("</table>");
      }
      out.println("<h3>Groups you are a member of</h3>");
      if(member == null){
        out.println("<p>You are not a member of any group.</p>");
      }else{
        out.println("<table><tr><th>Group name</th><th>Members</th></tr>");
        for (int i = 0; i < member.size(); i++) {
          try {
            Key key = KeyFactory.createKey("Group", member.get(i));
            Entity gs = datastore.get(key);
            long id = gs.getKey().getId();
            String groupName = (String) gs.getProperty("name");
            List<String> members = (List<String>) gs.getProperty("members");
            if (members == null) {
              members = new ArrayList<String>();
            }
            out.println("<tr>");
            out.println("<td>" + groupName + "</td>");
            out.println("<td>" + members.size() + "</td>");
            out.println("<td><a href=\"/groups?groupID=" + id + "\">View</a></td>");
            out.println("</tr>");
          } catch (Exception e) {
            logger.atWarning().log("Problem while searching groups user is a member of: %s", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Problem while searching groups user is a member of");
            return;
          }
        }
        out.println("</table>");
      }
    } catch (Exception e) {
      logger.atWarning().log("Problem while adding group to users groups: %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Problem while adding group to users groups");
      return;
    }
  }
}