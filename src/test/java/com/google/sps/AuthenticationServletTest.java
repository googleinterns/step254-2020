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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Exam Servlet. Test when an examID is provided, if exam is available, unavailable and
 * test if no examID is provided
 *
 * @author Aidan Molloy
 */
@RunWith(JUnit4.class)
public final class AuthenticationServletTest extends AuthenticationServlet {
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

  /* Test logged out user. */
  @Test
  public void doGetTestLoggedOut() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationServlet servlet = new AuthenticationServlet();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("loginUrl\":\"/_ah/login?continue"));
  }

  /* Login user with email "test@example.com" */
  private void helperLogin() {
    helper.setEnvAuthDomain("example.com");
    helper.setEnvEmail("test@example.com");
    helper.setEnvIsLoggedIn(true);
  }

  /**
   * When logged in without preferences the users email and a logout link should be returned with
   * a UserInfoRequired key set to true.
   */
  @Test
  public void doGetTestLoggedIn() throws IOException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationServlet servlet = new AuthenticationServlet();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("\"email\":\"test@example.com\""));
    Assert.assertTrue(result.contains("\"logoutUrl\":\"/_ah/logout?continue"));
    Assert.assertTrue(result.contains("\"updateInfoRequired\":\"true\""));
  }

  /* Set up fake preferences for test user to test datastore */
  private void addPreference() {
    Entity userInfoEntity = new Entity("UserInfo");
    userInfoEntity.setProperty("email", "test@example.com");
    userInfoEntity.setProperty("name", "Test User");
    userInfoEntity.setProperty("font", "Arial");
    userInfoEntity.setProperty("font_size", "16");
    userInfoEntity.setProperty("bg_color", "white");
    userInfoEntity.setProperty("text_color", "black");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userInfoEntity);
  }

  /**
   * When logged in and preferences are set the users email, logout link and preferences should be
   * returned.
   */
  @Test
  public void doGetTestUserInfo() throws IOException {
    helperLogin();
    addPreference();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationServlet servlet = new AuthenticationServlet();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("\"email\":\"test@example.com\""));
    Assert.assertTrue(result.contains("\"logoutUrl\":\"/_ah/logout?continue"));
    Assert.assertTrue(result.contains("\"name\":\"Test User\""));
    Assert.assertTrue(result.contains("\"text_color\":\"black\""));
    Assert.assertTrue(result.contains("\"font\":\"Arial\""));
    Assert.assertTrue(result.contains("\"bg_color\":\"white\""));
    Assert.assertTrue(result.contains("\"font_size\":\"16\""));
  }

}