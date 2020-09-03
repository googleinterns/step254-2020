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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Groups servlet, for when no group is requested, invalid group and real group.
 *
 * @author Aidan Molloy
 */
@RunWith(JUnit4.class)
public final class GroupsServletTest extends GroupsServlet {
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
    GroupsServlet servlet = new GroupsServlet();
    servlet.doGet(request, response);
    verify(response).sendRedirect("/");
  }

  /* Login user with email "test@google.com" */
  private void helperLogin() {
    helper.setEnvAuthDomain("google.com");
    helper.setEnvEmail("test@google.com");
    helper.setEnvIsLoggedIn(true);
  }

  /* Set up fake group */
  private void addGroup() {
    helperLogin();
    Entity groupEntity = new Entity("Group");
    groupEntity.setProperty("name", "Test Group");
    groupEntity.setProperty("description", "Test Description");
    groupEntity.setProperty("ownerID", "test@google.com");
    groupEntity.setProperty("members", new ArrayList<>());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(groupEntity);
  }

  /* When no groupID is provided. */
  @Test
  public void doGetTestNoGroupID() throws IOException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    GroupsServlet servlet = new GroupsServlet();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("Create a Group"));
  }

  /* When groupID that does not exist is provided */
  @Test
  public void doGetTestInvalidGroupID() throws IOException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    GroupsServlet servlet = new GroupsServlet();
    when(request.getParameter("groupID")).thenReturn("1");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("Selected group is not available."));
  }

  /**
   *  Get the GroupID of existing group.
   */
  public String getGroupID() {
    Query query = new Query("Group");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      return String.valueOf(entity.getKey().getId());
    }
    return "1";
  }

  /**
   * When a real group is requested.
   */
  @Test
  public void doGetTestGroup() throws IOException {
    helperLogin();
    addGroup();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    GroupsServlet servlet = new GroupsServlet();
    when(request.getParameter("groupID")).thenReturn(getGroupID());


    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("Group Name: Test Group"));
    Assert.assertTrue(result.contains("Description: Test Description"));
    Assert.assertTrue(result.contains("Owner: test@google.com"));
  }

}