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
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Edit Groups servlet, for when a group is created, when a member is 
 * added and when a member is removed.
 *
 * @author Aidan Molloy
 */
@RunWith(JUnit4.class)
public final class EditGroupServletTest extends EditGroupServlet {
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
  public void doPostTestLoggedOut() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    EditGroupServlet servlet = new EditGroupServlet();
    servlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
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
  
  /* Add a fake member to the fake group */
  private void addMember() {
    helperLogin();
    Entity groupEntity = new Entity("Group");
    groupEntity.setProperty("name", "Test Group");
    groupEntity.setProperty("description", "Test Description");
    groupEntity.setProperty("ownerID", "test@google.com");
    List<String> members = new ArrayList<String>();
    members.add("test3@google.com");
    members.add("test4@google.com");
    groupEntity.setProperty("members", members);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(groupEntity);

    Entity userGroupEntity = new Entity("UserGroup", "test3@google.com");
    userGroupEntity.setProperty("email", "test3@google.com");
    userGroupEntity.setProperty("owner", new ArrayList<Long>());
    List<Long> member = new ArrayList<Long>();
    member.add(Long.parseLong(getGroupID()));
    userGroupEntity.setProperty("member", member);
    datastore.put(userGroupEntity);
  }

  /* When creating a group. */
  @Test
  public void doPostTestCreateGroup() throws IOException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    EditGroupServlet servlet = new EditGroupServlet();
    when(request.getParameter("editType")).thenReturn("create");
    when(request.getParameter("name")).thenReturn("Test Group");
    when(request.getParameter("description")).thenReturn("Test Description");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    // Make query to datastore to make sure it was stored correctly
    Query query = new Query("Group");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    // Convert the received entity into a json string to check content
    Map<String, String> groupResponse = new HashMap<String, String>();
    groupResponse.put("name", (String) entity.getProperty("name"));
    groupResponse.put("description", (String) entity.getProperty("description"));
    groupResponse.put("owner", (String) entity.getProperty("ownerID"));
    String result = UtilityClass.convertToJson(groupResponse);
    System.out.println(result);
    Assert.assertTrue(result.contains("\"name\":\"Test Group\""));
    Assert.assertTrue(result.contains("\"description\":\"Test Description\""));
    Assert.assertTrue(result.contains("\"owner\":\"test@google.com\""));
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

  /* When adding a member to a group. */
  @Test
  public void doPostTestAddMember() throws IOException {
    helperLogin();
    addGroup();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    EditGroupServlet servlet = new EditGroupServlet();
    when(request.getParameter("editType")).thenReturn("add");
    when(request.getParameter("groupID")).thenReturn(getGroupID());
    when(request.getParameter("email")).thenReturn("test2@google.com");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    // Make query to datastore to make sure it was stored correctly
    Query query = new Query("Group");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    List<String> members = (List<String>) entity.getProperty("members");
    Assert.assertTrue(members.contains("test2@google.com"));
  }

  /* When removing a member from a group. */
  @Test
  public void doPostTestRemoveMember() throws IOException {
    helperLogin();
    addMember();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    EditGroupServlet servlet = new EditGroupServlet();
    when(request.getParameter("editType")).thenReturn("remove");
    when(request.getParameter("groupID")).thenReturn(getGroupID());
    when(request.getParameter("email")).thenReturn("test3@google.com");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);

    // Make query to datastore to make sure it was stored correctly
    Query query = new Query("Group");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    List<String> members = (List<String>) entity.getProperty("members");
    Assert.assertTrue(!members.contains("test3@google.com"));
  }

}