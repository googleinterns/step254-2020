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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.UtilityClass;
import com.google.sps.servlets.UpdateInfoServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Update Info Servlet to see if user preferences are stored correctly.
 *
 * @author Aidan Molloy
 */
@RunWith(JUnit4.class)
public final class UpdateInfoServletTest extends UpdateInfoServlet {
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

  /* Test logged out user exception. */
  @Test
  public void doGetTestLoggedOut() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    UpdateInfoServlet servlet = new UpdateInfoServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  /* Login user with email "test@google.com" */
  private void helperLogin() {
    helper.setEnvAuthDomain("google.com");
    helper.setEnvEmail("test@google.com");
    helper.setEnvIsLoggedIn(true);
  }
  
  /* Test what happens when nothing is passed */
  @Test
  public void testDoPostWithNullInput() throws IOException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);

    UpdateInfoServlet servlet = new UpdateInfoServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
  }

  /* Test updating user preferences */
  @Test
  public void doPostTest() throws IOException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("name")).thenReturn("Test User");
    when(request.getParameter("font")).thenReturn("Arial");
    when(request.getParameter("font_size")).thenReturn("16");
    when(request.getParameter("bg_color")).thenReturn("white");
    when(request.getParameter("text_color")).thenReturn("black");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    UpdateInfoServlet servlet = new UpdateInfoServlet();
    servlet.doPost(request, response);

    // Make query to datastore to make sure it was stored correctly
    Query query =
        new Query("UserInfo")
            .setFilter(new Query
                .FilterPredicate("email", Query.FilterOperator.EQUAL, "test@google.com"));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    // Convert the received entity into a json string to check content
    Map<String, String> userInfoResponse = new HashMap<String, String>();
    userInfoResponse.put("name", (String) entity.getProperty("name"));
    userInfoResponse.put("font", (String) entity.getProperty("font"));
    userInfoResponse.put("font_size", (String) entity.getProperty("font_size"));
    userInfoResponse.put("bg_color", (String) entity.getProperty("bg_color"));
    userInfoResponse.put("text_color", (String) entity.getProperty("text_color"));
    String result = UtilityClass.convertToJson(userInfoResponse);
    Assert.assertTrue(result.contains("\"bg_color\":\"white\""));
    Assert.assertTrue(result.contains("\"font_size\":\"16\""));
    Assert.assertTrue(result.contains("\"name\":\"Test User\""));
    Assert.assertTrue(result.contains("\"text_color\":\"black\""));
    Assert.assertTrue(result.contains("\"font\":\"Arial\""));
  }
}
