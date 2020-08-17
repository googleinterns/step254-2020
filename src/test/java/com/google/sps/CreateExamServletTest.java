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

package com.google.sps;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;

import com.google.sps.servlets.CreateExamServlet;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.junit.runners.JUnit4;
@RunWith(JUnit4.class)
public final class CreateExamServletTest extends CreateExamServlet {
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
    /*Tests the doPost function to see if the test gets stored correctly */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    //set the parameters that will be requested to test values
    when(request.getParameter("name")).thenReturn("Testing Exam");
    when(request.getParameter("duration")).thenReturn("30");


    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    CreateExamServlet servlet = new CreateExamServlet();
    servlet.doPost(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("\"ownerID\":\"test@example.com\""));
    Assert.assertTrue(result.contains("\"duration\":\"30\""));
    Assert.assertTrue(result.contains("\"name\":\"Testing Exam\""));
    //check if the correct status has been applied
    verify(response).setStatus(HttpServletResponse.SC_CREATED);
  }

  @Test
  public void testDoPostWithNullInputs() throws IOException {
    // Check if the correct status code will be applied when 
    // the input is null. 
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    //set the parameters that will be requested to test values
    when(request.getParameter("name")).thenReturn(null);
    when(request.getParameter("duration")).thenReturn("30");


    CreateExamServlet servlet = new CreateExamServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
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
    when(request.getParameter("name")).thenReturn("trial");
    when(request.getParameter("duration")).thenReturn("30");

    
    CreateExamServlet servlet = new CreateExamServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
  private void helperLogin() {
    /* Login user with email "test@example.com" */
    helper.setEnvAuthDomain("example.com");
    helper.setEnvEmail("test@example.com");
    helper.setEnvIsLoggedIn(true);
  }
}