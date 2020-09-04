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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Exam User's Own. Test if exams by a user get retrieved correctly and if a user
 * is not logged in.
 *
 * @author Klaudia Obieglo
 */

@RunWith(JUnit4.class)
public final class DashboardServletTest extends DashboardServlet {
  private final LocalServiceTestHelper helper = 
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    
  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testExamsOwnedFunction() throws IOException, ServletException {
    /*Tests get ExamsOwnedByUser to see if all tests a user has created
    * get retrieved correctly
    */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    ServletConfig config = mock(ServletConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(config.getServletContext()).thenReturn(context);

    //Get the path to the target files were templates are stored for tests
    String filePath = new File(".").getCanonicalPath();
    String endPath = "/target/portfolio-1/WEB-INF/templates";
    String path = filePath + endPath;
    when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);

    List<Long> examIDs = createTests();
    setUpUserForCreatedExams(examIDs);
    DashboardServlet servlet = new DashboardServlet();
    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("<td> Trial </td>"));
    Assert.assertTrue(result.contains("<td><a href=/showExam?examID=" + examIDs.get(0)
        + ">Look at Exam</a></td>"));
    Assert.assertTrue(result.contains("<td> Another Exam </td>"));
    Assert.assertTrue(result.contains("<td><a href=/showExam?examID=" + examIDs.get(1)
        + ">Look at Exam</a></td>"));
  }

  @Test
  public void testGetCompletedExamsFunction() throws IOException, ServletException {
    /*Tests the getExamsCompletedByUser to see if all tests a user has taken
    * get retrieved correctly
    */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    ServletConfig config = mock(ServletConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(config.getServletContext()).thenReturn(context);

    //Get the path to the target files were templates are stored for tests
    String filePath = new File(".").getCanonicalPath();
    String endPath = "/target/portfolio-1/WEB-INF/templates";
    String path = filePath + endPath;
    when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);

    List<Long> examIDs = createTests();
    List<Long> taken = new ArrayList<Long>();
    taken.add(examIDs.get(0));
    setUpUserForTakenExams(taken);
    DashboardServlet servlet = new DashboardServlet();
    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("<td> Trial </td>"));
    Assert.assertTrue(result.contains("<td><a href=/examsTaken?examID=" + examIDs.get(0)
        + ">Look at Exam</a></td>"));
  }
  
  @Test
  public void testExamsToDoFunction() throws IOException, ServletException {
    /*Tests the getExamsToDoByUser to see if tests that are available for the
    * user to take get retrieved correctly
    */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    ServletConfig config = mock(ServletConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(config.getServletContext()).thenReturn(context);

    //Get the path to the target files were templates are stored for tests
    String filePath = new File(".").getCanonicalPath();
    String endPath = "/target/portfolio-1/WEB-INF/templates";
    String path = filePath + endPath;
    when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);

    List<Long> examIDs = createTests();
    List<Long> available = new ArrayList<Long>();
    available.add(examIDs.get(1));
    setUpUserForAvailableExams(available);
    DashboardServlet servlet = new DashboardServlet();
    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("<td> Another Exam </td>"));
    Assert.assertTrue(result.contains("<td><a href=/exam?examID=" + examIDs.get(1)
        + ">Look at Exam</a></td>"));
  }
  @Test
  public void testNotLoggedInUser() throws IOException {
    // test to see if a not logged in user will be able to
    // look at tests a user has created
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);

    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(false);
    
    DashboardServlet servlet= new DashboardServlet();
    servlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "You are not authorised to view this page");
  }

  private void helperLogin() {
    /* Login user with email "test@google.com" */
    helper.setEnvAuthDomain("google.com");
    helper.setEnvEmail("test@google.com");
    helper.setEnvIsLoggedIn(true);
  }

  private List createTests() {
    /*Create two fake TestEntities */
    final Long date = (new Date()).getTime(); 
    List<Long> list = new ArrayList<>();
    Entity testEntity = new Entity("Exam");
    testEntity.setProperty("name", "Trial");
    testEntity.setProperty("duration", "30");
    testEntity.setProperty("ownerID", "test@google.com");
    testEntity.setProperty("date", date);
    testEntity.setProperty("questionsList", list);

    Entity anotherEntity= new Entity("Exam");
    anotherEntity.setProperty("name", "Another Exam");
    anotherEntity.setProperty("duration", "45");
    anotherEntity.setProperty("ownerID", "test@google.com");
    anotherEntity.setProperty("date", date);
    anotherEntity.setProperty("questionsList", list);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(testEntity);
    datastore.put(anotherEntity);
    List<Long> taken = new ArrayList<Long>();
    taken.add(testEntity.getKey().getId());
    taken.add(anotherEntity.getKey().getId());
    return taken;
  }
  public void setUpUserForCreatedExams(List<Long> created) {
    // Set up UserExams to check if the taken created get stored and retrieved correctly
    Query getUserExams = new Query("UserExams").setFilter(new FilterPredicate("email",
          FilterOperator.EQUAL, "test@google.com"));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(getUserExams);
    Entity userExamsEntity = pq.asSingleEntity();
    userExamsEntity = new Entity("UserExams", "test@google.com");
    userExamsEntity.setProperty("email", "test@google.com");
    userExamsEntity.setProperty("created", created);
    datastore.put(userExamsEntity);
  }

  public void setUpUserForTakenExams(List<Long> taken) {
    // Set up UserExams to check if the taken exams get stored and retrieved correctly
    Query getUserExams = new Query("UserExams").setFilter(new FilterPredicate("email",
          FilterOperator.EQUAL, "test@google.com"));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(getUserExams);
    Entity userExamsEntity = pq.asSingleEntity();
    userExamsEntity = new Entity("UserExams", "test@google.com");
    userExamsEntity.setProperty("email", "test@google.com");
    userExamsEntity.setProperty("taken", taken);
    datastore.put(userExamsEntity);
  }

  public void setUpUserForAvailableExams(List<Long> available) {
    // Set up UserExams to check if the available exams get stored and retrieved correctly
    Query getUserExams = new Query("UserExams").setFilter(new FilterPredicate("email",
          FilterOperator.EQUAL, "test@google.com"));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(getUserExams);
    Entity userExamsEntity = pq.asSingleEntity();
    userExamsEntity = new Entity("UserExams", "test@google.com");
    userExamsEntity.setProperty("email", "test@google.com");
    userExamsEntity.setProperty("available", available);
    datastore.put(userExamsEntity);
  }
}
