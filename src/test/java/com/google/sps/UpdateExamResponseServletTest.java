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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Map;
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
 * Tests for Update Exam Responses Servlet. Test is responses updated with expected marks,
 * if a user is not logged in check for an unauthorised error.
 *
 * @author Róisín O'Farrell
 */
@RunWith(JUnit4.class)
public final class UpdateExamResponseServletTest extends UpdateExamResponseServlet {
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
  public void testdoPostFunction() throws IOException, ServletException {
    /*Tests the doGet function to see if the questions that the
    * user owns get retrieved correctly */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(request.getParameter("examName")).thenReturn("Trial");
    when(request.getParameter("studentEmail")).thenReturn("student@google.com");
    when(request.getParameter("1")).thenReturn("100");
    when(request.getParameter("2")).thenReturn("200");
    setFakeTest();
    setFakeResponeses();
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
    
    
    UpdateExamResponseServlet servlet = new UpdateExamResponseServlet();
    servlet.init(config);
    servlet.doPost(request, response);

    // Make query to datastore to make sure it was stored correctly
    Query query =
        new Query("1")
            .setFilter(new Query
                .FilterPredicate("email", Query.FilterOperator.EQUAL, "student@google.com"));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    // Convert the received entity into a json string to check content
    Map<String, String> userResponseInfo = new HashMap<String, String>();
    userResponseInfo.put("email", (String) entity.getProperty("email"));
    userResponseInfo.put("answer", (String) entity.getProperty("answer"));
    userResponseInfo.put("marks", (String) entity.getProperty("marks"));
    String result = UtilityClass.convertToJson(userResponseInfo);
    Assert.assertTrue(result.contains("100"));
    Assert.assertTrue(result.contains("student@google.com"));
    Assert.assertTrue(result.contains("Tuesday"));
  }
  @Test
  public void testNotLoggedInUser() throws IOException {
    // test to see if a not logged in user will be able to
    // look at tests a user has created
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);

    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(false);
    
    UpdateExamResponseServlet servlet = new UpdateExamResponseServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "You are not authorised to view this page");
  }

  private void setFakeTest() {
    /*Set a fake test*/
    List<Long> fakeQuestionList = new ArrayList<Long>();
    fakeQuestionList.add(1L);
    fakeQuestionList.add(2L);
    Entity testEntity = new Entity("Exam");
    testEntity.setProperty("name", "Trial");
    testEntity.setProperty("duration", "30");
    testEntity.setProperty("ownerID", "test@google.com");
    Long date = (new Date()).getTime(); 
    testEntity.setProperty("date", date); 
    testEntity.setProperty("questionsList", fakeQuestionList);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(testEntity);
  }

   private void setFakeResponeses() {
    /*Set up two fake response entities for testing purposes */
    Entity responseEntity = new Entity("1", "student@google.com");
    responseEntity.setProperty("answer", "Tuesday");
    responseEntity.setProperty("marks", "2");
    responseEntity.setProperty("email", "student@google.com");

    Entity anotherResponseEntity = new Entity("2", "student@google.com");
    anotherResponseEntity.setProperty("answer", "2011");
    anotherResponseEntity.setProperty("marks", "5");
    anotherResponseEntity.setProperty("email", "student@google.com");
    
    Entity responseToDifferentUser = new Entity("4", "person@example.com");
    responseToDifferentUser.setProperty("answer", "6");
    responseToDifferentUser.setProperty("marks", "15");
    responseToDifferentUser.setProperty("email", "person@example.com");
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(responseEntity);
    datastore.put(anotherResponseEntity);
    datastore.put(responseToDifferentUser);
  }

  private void helperLogin() {
    /* Login user with email "test@example.com" */
    helper.setEnvAuthDomain("google.com");
    helper.setEnvEmail("test@google.com");
    helper.setEnvIsLoggedIn(true);
  }
}