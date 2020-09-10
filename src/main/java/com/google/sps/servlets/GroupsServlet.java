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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
  Configuration cfg;

  /**
   * Set up the configuration once.
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    cfg = new Configuration(Configuration.VERSION_2_3_30);
    String path = getServletContext().getRealPath("/WEB-INF/templates/");
    try {
      cfg.setDirectoryForTemplateLoading(new File(path));
    } catch (IOException e) {
      logger.atWarning().log("Could not set directory for template loading: %s", e);
    }
    // Recommended settings for new projects:
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
  }

  /**
   * Display groups you own and are a member of,
   * Display form for creating groups,
   * If groupID is provided display specific group details.
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
    Map data = new HashMap();
    String groupID = UtilityClass.getParameter(request, "groupID", null);
    Entity groupEntity = null;
    String groupContent = "";

    if (groupID != null) {
      // If an exam has been selected remove html tags and trim the ID
      groupID = UtilityClass.processExternalText(groupID);
      try {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey("Group", Long.parseLong(groupID));
        groupEntity = datastore.get(key);
      } catch (Exception e) {
        groupContent += ("<h3>Selected group is not available.</h3>");
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
        groupContent += ("<h1>Group Name: " + name + "</h1>");
        groupContent += ("<h3>Description: " + description + "</h3>");
        groupContent += ("<h3>Owner: " + ownerID + "</h3>");

        // If the current user is the owner they can add and remove members
        if (ownerID.equals(userService.getCurrentUser().getEmail())) {
          if (members != null) {
            groupContent += ("<table>");
            groupContent += ("<tr><th>User Email</th><th>Remove</th></tr>");
            for (String member : members) {
              // For each member display their email and a button to remove
              groupContent += ("<tr><td>" + member + "</td><td>");
              groupContent += ("<form action=\"/editGroup\" method=\"POST\">");
              groupContent += ("<input type=\"hidden\" id=\"groupID\" name=\"groupID\" "
                  + "value=" + groupID + ">");
              groupContent += ("<input type=\"hidden\" id=\"editType\" name=\"editType\" "
                  + "value=\"remove\">");
              groupContent += ("<input type=\"hidden\" id=\"email\" name=\"email\" "
                  + "value=" + member + ">");
              groupContent += ("<input type=\"submit\" id=\"removeMember\" name=\"removeMember\""
                  + "value=\"Remove\"> </td></tr>");
              groupContent += ("</form>");
            }
          } else {
            groupContent += ("<p>There are no members in this group yet</p>");
          }
          groupContent += ("</table><br>");
          groupContent += ("<h2>Add Member</h2>");
          groupContent += ("<form action=\"/editGroup\" method=\"POST\">");
          groupContent += ("<input type=\"hidden\" id=\"groupID\" name=\"groupID\" "
              + "value=" + groupID + ">");
          groupContent += ("<input type=\"hidden\" id=\"editType\" name=\"editType\" value=\"add\">");
          groupContent += ("<label for=\"email\">Enter User's Email:</label><br>");
          groupContent += ("<input type=\"text\" id=\"email\" name=\"email\" required>");
          groupContent += ("<input type=\"submit\" id=\"addMember\" name=\"addMember\" "
              + "value=\"Add Member\">");
          groupContent += ("</form>");

        } else {
          if (members != null) {
            groupContent += ("<p>Number of members: " + members.size() + "</p>");
          } else {
            groupContent += ("<p>There are no members in this group yet</p>");
          }
        }
        groupContent += ("</main></body>");
      }
    }

    if (groupID == null || groupEntity == null) {
      groupContent += ("<h2>Create a Group</h2>");
      groupContent += ("<form id=\"createGroup\" action=\"/editGroup\" method=\"POST\">");
      groupContent += ("<input type=\"hidden\" id=\"editType\" name=\"editType\" value=\"create\">");
      groupContent += ("<label for=\"name\">Enter Group Name:</label><br>");
      groupContent += ("<input type=\"text\" id=\"name\" name=\"name\" onclick=\"startDictation(this.id)\" required><br>");
      groupContent += ("<label for=\"name\">Enter Group Description:</label><br>");
      groupContent += ("<input type=\"text\" id=\"description\" name=\"description\" onclick=\"startDictation(this.id)\"><br>");
      groupContent += ("<input type=\"submit\" value=\"Submit\">");
      groupContent += ("</form>");

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
            owner = new ArrayList<>();
          }
          member = (List<Long>) userGroupEntity.getProperty("member");
          if (member == null) {
            member = new ArrayList<>();
          }
        }
        groupContent += ("<h3>Owned groups</h3>");
        if (owner == null) {
          groupContent += ("<p>You do not own any groups.</p>");
        } else {
          groupContent += ("<table><tr><th>Group name</th><th>Members</th></tr>");
          for (Long ownerIndex : owner) {
            try {
              Key key = KeyFactory.createKey("Group", ownerIndex);
              Entity gs = datastore.get(key);
              String groupName = (String) gs.getProperty("name");
              List<String> members = (List<String>) gs.getProperty("members");
              if (members == null) {
                members = new ArrayList<String>();
              }
              long id = gs.getKey().getId();
              groupContent += ("<tr>");
              groupContent += ("<td>" + groupName + "</td>");
              groupContent += ("<td>" + members.size() + "</td>");
              groupContent += ("<td><a href=\"/groups?groupID=" + id + "\">View/Edit</a></td>");
              groupContent += ("</tr>");
            } catch (Exception e) {
              logger.atWarning().log("Problem while searching groups user owns: %s", e);
              response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                  "Problem while searching groups user owns");
              return;
            }
          }
          groupContent += ("</table>");
        }
        groupContent += ("<h3>Groups you are a member of</h3>");
        if (member == null) {
          groupContent += ("<p>You are not a member of any group.</p>");
        } else {
          groupContent += ("<table><tr><th>Group name</th><th>Members</th></tr>");
          for (Long memberIndex : member) {
            try {
              Key key = KeyFactory.createKey("Group", memberIndex);
              Entity gs = datastore.get(key);
              String groupName = (String) gs.getProperty("name");
              List<String> members = (List<String>) gs.getProperty("members");
              if (members == null) {
                members = new ArrayList<>();
              }
              long id = gs.getKey().getId();
              groupContent += ("<tr>");
              groupContent += ("<td>" + groupName + "</td>");
              groupContent += ("<td>" + members.size() + "</td>");
              groupContent += ("<td><a href=\"/groups?groupID=" + id + "\">View</a></td>");
              groupContent += ("</tr>");
            } catch (Exception e) {
              logger.atWarning().log("Problem while searching groups user is a member of: %s", e);
              response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                  "Problem while searching groups user is a member of");
              return;
            }
          }
          groupContent += ("</table>");
        }
      } catch (Exception e) {
        logger.atWarning().log("Problem while adding group to users groups: %s", e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Problem while adding group to users groups");
      }
    }
    data.put("groupContent", groupContent);
    // run to freemarker template
    try {
      Template template = cfg.getTemplate("Groups.ftl");
      PrintWriter out = response.getWriter();
      template.process(data, out);
      logger.atInfo().log("Groups page was displayed correctly for the User:"
          + "%s", userService.getCurrentUser());
    } catch (TemplateException e) {
      logger.atWarning().log("There was a problem with processing the template %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to display the Groups Page");
    }
  }
}