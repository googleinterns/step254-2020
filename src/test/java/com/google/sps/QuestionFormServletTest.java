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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
 * Tests for Question Form Servlet. Test if the proper question form gets displayed to the user,
 * and if user is not logged in check for correct error status.
 *
 * @author Klaudia Obieglo
 */
@RunWith(JUnit4.class)
public final class QuestionFormServletTest extends QuestionFormServlet {
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
  public void testdoGetFunction() throws IOException {
    //check if the form gets build and returns the correct
    //status response.
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    
    QuestionFormServlet servlet= new QuestionFormServlet();
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("<label for=\"question\">Enter Question:</label><br>"));
  }

  @Test
  public void testNotLoggedInUser() throws IOException {
    // test to see if a not logged in user will be able to
    // look at tests a user has created
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);

    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(false);
    
    QuestionFormServlet servlet= new QuestionFormServlet();
    servlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
  private void helperLogin() {
    /* Login user with email "test@example.com" */
    helper.setEnvAuthDomain("example.com");
    helper.setEnvEmail("test@example.com");
    helper.setEnvIsLoggedIn(true);
  }

}