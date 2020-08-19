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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Create Question Servlet. Test if a question gets saved correctly, if question does not
 * get created when paramters are null, and test what happens when a user is not logged in. 
 *
 * @author Klaudia Obieglo
 */
@RunWith(JUnit4.class)
public final class CreateQuestionServletTest extends CreateQuestionServlet {
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
  public void testdoPostFunction() throws IOException {
    /*Tests the doPost function to see if the question gets stored correctly */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    //set the parameters that will be requested to test values
    when(request.getParameter("question")).thenReturn("What does the fox say?");
    when(request.getParameter("marks")).thenReturn("5");
    when(request.getParameter("testName")).thenReturn("Trial");
    //create Fake Test
    createFakeTest();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    CreateQuestionServlet servlet = new CreateQuestionServlet();
    servlet.doPost(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("\"question\":\"What does the fox say?\","
      +"\"marks\":\"5\",\"ownerID\":\"test@example.com\""));
  }

  @Test
  public void testDoPostWithNullParameters() throws IOException {
    /*Test do Post function with null parameters to check if
    * the correct status code gets applied */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);

    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);

    //set the parameters that will be requested to test values
    when(request.getParameter("question")).thenReturn(null);
    when(request.getParameter("marks")).thenReturn("10");
    when(request.getParameter("testName")).thenReturn(null);
    
    CreateQuestionServlet servlet = new CreateQuestionServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,
        "You have entered one or more null parameters");
  }

  @Test
  public void testNotLoggedInUser() throws IOException {
    // test to see if a user that is not logged in will
    // be able to create a question
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);

    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(false);
    
    //set the parameters that will be requested to test values
    when(request.getParameter("question")).thenReturn("How are you?");
    when(request.getParameter("marks")).thenReturn("10");
    when(request.getParameter("testName")).thenReturn("Trial");
    CreateQuestionServlet servlet = new CreateQuestionServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
      "You are not authorised to view this page");
  }
  private void helperLogin() {
    /* Login user with email "test@example.com" */
    helper.setEnvAuthDomain("example.com");
    helper.setEnvEmail("test@example.com");
    helper.setEnvIsLoggedIn(true);
  }
  private void createFakeTest() {
    /*Create a Fake test*/
    Long date = (new Date()).getTime();
    Entity testEntity = new Entity("Exam");
    testEntity.setProperty("name", "Trial");
    testEntity.setProperty("duration", "30");
    testEntity.setProperty("ownerID", "test@example.com");
    testEntity.setProperty("date", date);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(testEntity); 
  }
}