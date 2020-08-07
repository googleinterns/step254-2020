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
import com.google.sps.servlets.TestsUserOwnsServlet;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import javax.servlet.http.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
@RunWith(JUnit4.class)
public final class TestsUserOwnsServletTest extends TestsUserOwnsServlet{
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
  public void testdoGetFunction() throws IOException{
    /*Tests the doGet function to see if the test gets retrieved correctly */
    TestsUserOwnsServlet servlet= new TestsUserOwnsServlet();
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    Long date = (new Date()).getTime(); 
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    List<Long> list = new ArrayList<>();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity testEntity = new Entity("Test");
    testEntity.setProperty("testName","Trial");
    testEntity.setProperty("testDuration","30");
    testEntity.setProperty("ownerID","test@example.com");
    testEntity.setProperty("date", date);
    testEntity.setProperty("questionsList",list);
    datastore.put(testEntity);

    Entity anotherEntity= new Entity("Test");
    anotherEntity.setProperty("testName","AnotherTest");
    anotherEntity.setProperty("testDuration","45");
    anotherEntity.setProperty("ownerID","test@example.com");
    anotherEntity.setProperty("date", date);
    anotherEntity.setProperty("questionsList",list);
    datastore.put(anotherEntity);


    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.doGet(request, response);
    String result = stringWriter.toString();
    System.out.println(result);
    Assert.assertTrue(result.contains("\"testName\":\"Trial\",\"testID\":1,"+
      "\"testDuration\":30.0,\"ownersID\":\"test@example.com\""));
    Assert.assertTrue(result.contains("\"testName\":\"AnotherTest\",\"testID\":2,"+
      "\"testDuration\":45.0,\"ownersID\":\"test@example.com\""));
  }
}