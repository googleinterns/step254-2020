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
  public void testGetCreatedExamsFunction() throws IOException, ServletException {
    /*Tests get ExamsOwnedByUser to see if all tests a user has created
    * get retrieved correctly
    */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    createTests();

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

    createTests();
    DashboardServlet servlet = new DashboardServlet();
    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("<td> Trial </td>"));
    Assert.assertTrue(result.contains("<td><a href=/showExam?examID=3>Look at Exam</a></td>"));
    Assert.assertTrue(result.contains("<td> Another Exam </td>"));
    Assert.assertTrue(result.contains("<td><a href=/showExam?examID=4>Look at Exam</a></td>"));
  }
//   @Test
//   public void testGetTakenExamsFunction() throws IOException, ServletException {
//     /*Tests the getExamsCompletedByUser to see if all tests a user has taken
//     * get retrieved correctly
//     */
//     HttpServletRequest request = mock(HttpServletRequest.class);       
//     HttpServletResponse response = mock(HttpServletResponse.class);
//     helperLogin();
//     UserService userService = mock(UserService.class);
//     when(userService.isUserLoggedIn()).thenReturn(true);
//     createTests();


//     StringWriter stringWriter = new StringWriter();
//     PrintWriter writer = new PrintWriter(stringWriter);
//     when(response.getWriter()).thenReturn(writer);

//     ServletConfig config = mock(ServletConfig.class);
//     ServletContext context = mock(ServletContext.class);
//     when(config.getServletContext()).thenReturn(context);

//     //Get the path to the target files were templates are stored for tests
//     String filePath = new File(".").getCanonicalPath();
//     String endPath = "/target/portfolio-1/WEB-INF/templates";
//     String path = filePath + endPath;
//     when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);

//     createTests();
//     DashboardServlet servlet = new DashboardServlet();
//     servlet.init(config);
//     servlet.doGet(request, response);
//     String result = stringWriter.toString();
//     Assert.assertTrue(result.contains("<td> Trial </td>"));
//     Assert.assertTrue(result.contains("<td><a href=/showExam?examID=3>Look at Exam</a></td>"));
//     Assert.assertTrue(result.contains("<td> Another Exam </td>"));
//     Assert.assertTrue(result.contains("<td><a href=/showExam?examID=4>Look at Exam</a></td>"));
//   }
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

  private void createTests() {
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
  }
}