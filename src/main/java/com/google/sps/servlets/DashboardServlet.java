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
 
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import freemarker.template.Version;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns the users created exams, to-do exams and completed exams to the
* users dashboard
* @author Klaudia Obieglo
*/
@WebServlet("/dashboardServlet")
public class DashboardServlet extends HttpServlet{
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  Configuration cfg;
  Map dashboardData = new HashMap();
  //set up the configuration once
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
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /*Returns exams created, to be completed and completed by the user */
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn() 
      || !userService.getCurrentUser().getEmail().contains("@google.com")) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "You are not authorised to view this page");
      return;
    }
    String ownerID = userService.getCurrentUser().getEmail(); 
    //grab exams user owns
    getExamsOwnedByUser(ownerID);
    //get exams completed by the user
    try {
      getExamsCompletedByTheUser(ownerID);
    } catch (EntityNotFoundException e) {
      logger.atWarning().log("The exams completed by the user %s were not found",ownerID);
    }
    //get exams to be completed by the user
    try {
        getExamsToDoByTheUser(ownerID);
    } catch( EntityNotFoundException e) {
        logger.atWarning().log("The exams to be taken by the user %s were not found",ownerID);
    }
    // run to freemarker template
    try {
      Template template = cfg.getTemplate("Dashboard.ftl");
      PrintWriter out = response.getWriter();

      template.process(dashboardData, out);
      logger.atInfo().log("Dashboard was displayed correctly for the User:"
          + "%s", userService.getCurrentUser());
    } catch (TemplateException e) {
      logger.atWarning().log("There was a problem with processing the template %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to display the Question Form");
      return;
    }
  }
  public void getExamsOwnedByUser(String ownerID) {
    /* Saves the exams owned by the user in the Dashboard Data Map
    * Argument: OwnerID- email of the user that we are trying to find 
    *    the exams for.
    */
    Map<String,Long> examsOwnedMap = new LinkedHashMap<String, Long>();
    try {
      Query query = new Query("Exam").setFilter(new FilterPredicate("ownerID",
          FilterOperator.EQUAL, ownerID)).addSort("date", SortDirection.ASCENDING);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      for (Entity entity : results.asIterable()) {
        long id = entity.getKey().getId();
        String name = (String) entity.getProperty("name");
        examsOwnedMap.put(name, id);
        dashboardData.put("examOwned", examsOwnedMap);
      }
      logger.atInfo().log("Exams, if any, were found for the user %s", ownerID);
    } catch (DatastoreFailureException e) {
      logger.atSevere().log("Datastore error:%s" ,e);
      return;
    }
  }

  public void getExamsCompletedByTheUser(String email) throws EntityNotFoundException{
    /*Saves the exams that have been completed by the user in the Dashboard Data map
    * Argument: email - email of the user that we want to check for completion
    * of any exams.
    */
    Map<String, Long> examsCompletedMap = new LinkedHashMap<String, Long>();
    try {
      Query query = new Query("UserExams").setFilter(new FilterPredicate("email",
          FilterOperator.EQUAL, email));
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery result = datastore.prepare(query);
      Entity user = result.asSingleEntity();
      if (user == null){
        user = new Entity("UserExams", email);
        user.setProperty("email", email);
      } else if (user.getProperty("taken") != null) {
        List<Long> examsTakenList = (List<Long>) user.getProperty("taken");
        for(int i=0; i<examsTakenList.size(); i++) {
          Key key = KeyFactory.createKey("Exam", examsTakenList.get(i));
          Entity exam = datastore.get(key);
          String name = (String) exam.getProperty("name");
          examsCompletedMap.put(name, exam.getKey().getId());
          dashboardData.put("examCompleted", examsCompletedMap);
        }
      }
      logger.atInfo().log("Exams Completed, if any, were found for the user %s", email);
    } catch (DatastoreFailureException e) {
      logger.atSevere().log("Datastore error:%s" ,e);
      return;
    }
  }
  public void getExamsToDoByTheUser(String email) throws EntityNotFoundException {
    /*Saves the exams that the user still has to complete in the dashboardData map
    * Argument ownerID - email of the user who's exams we are looking for
    */
    Map<String, Long> examsToDoMap = new LinkedHashMap<String, Long>();
    try {
      Query query = new Query("UserExams").setFilter(new FilterPredicate("email",
          FilterOperator.EQUAL, email));
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery result = datastore.prepare(query);
      Entity user = result.asSingleEntity();
      if (user == null){
        user = new Entity("UserExams", email);
        user.setProperty("email", email);
      } else if (user.getProperty("available") != null) {
        List<Long> examsToTakeList = (List<Long>) user.getProperty("available");
        for(int i=0; i<examsToTakeList.size(); i++) {
          Key key = KeyFactory.createKey("Exam", examsToTakeList.get(i));
          Entity exam = datastore.get(key);
          String name = (String) exam.getProperty("name");
          examsToDoMap.put(name, exam.getKey().getId());
          dashboardData.put("examToComplete", examsToDoMap);
        }
      }
      logger.atInfo().log("Exams To Do, if any, were found for the user %s", email);
    } catch (DatastoreFailureException e) {
      logger.atSevere().log("Datastore error:%s" ,e);
      return;
    }

  }
}