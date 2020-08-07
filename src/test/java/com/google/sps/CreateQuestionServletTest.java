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
import com.google.sps.data.QuestionClass;
import com.google.sps.servlets.CreateQuestionServlet;
import java.io.IOException;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import javax.servlet.http.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.Date;
import java.io.*;
import com.google.gson.Gson;
import static org.mockito.Mockito.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import static org.junit.Assert.assertTrue;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.Date;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
@RunWith(JUnit4.class)
public final class CreateQuestionServletTest extends CreateQuestionServlet{
  private final LocalServiceTestHelper helper = 
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
      .setEnvIsLoggedIn(true).setEnvEmail("test@example.com").setEnvAuthDomain("example.com");
    
  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testdoPostFunction() throws IOException{
    /*Tests the doPost function to see if the question gets stored correctly */
    Long date = (new Date()).getTime();
    CreateQuestionServlet servlet = new CreateQuestionServlet();
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    //set the parameters that will be requested to test values
    when(request.getParameter("question")).thenReturn("What does the fox say?");
    when(request.getParameter("marks")).thenReturn("5");

    //create Fake Test
    Entity testEntity = new Entity("Test");
    testEntity.setProperty("testName", "Trial");
    testEntity.setProperty("testDuration", "30");
    testEntity.setProperty("ownerID","test@example.com");
    testEntity.setProperty("date", date);
    datastore.put(testEntity); 

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doPost(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("\"question\":\"What does the fox say?\","+
      "\"marks\":\"5\",\"ownerID\":\"test@example.com\""));
  }
}