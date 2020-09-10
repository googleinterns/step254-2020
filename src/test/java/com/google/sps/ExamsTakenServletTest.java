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
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
/**
 * Tests for Exams Taken Servlet. Test are all exam taken by the user retrieved,
 * if a user is not logged in check for an unauthorised error.
 *
 * @author Róisín O'Farrell
 */
@RunWith(JUnit4.class)
public final class ExamsTakenServletTest extends ExamsTakenServlet {
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
  public void testdoGetFunction() throws IOException, ServletException{
    /*Tests the doGet function to see if the exam that the
    * user took get retrieved correctly */
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    ServletConfig config = mock(ServletConfig.class);
    ServletContext context = mock(ServletContext.class);
    helperLogin();
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(UtilityClass.getParameter(request, "examID", "")).thenReturn("7");
    setFakeTest();
    setFakeQuestions();
    setFakeResponeses();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    when(config.getServletContext()).thenReturn(context);
    //Get the path to the target files were templates are stored for tests
    String filePath = new File(".").getCanonicalPath();
    String endPath = "/target/portfolio-1/WEB-INF/templates";
    String path = filePath + endPath;
    when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);
    
    
    ExamsTakenServlet servlet= new ExamsTakenServlet();
    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();

    Assert.assertTrue(result.contains("Trial"));
    Assert.assertTrue(result.contains("What day is it?"));
    Assert.assertTrue(result.contains("Tuesday"));
    Assert.assertFalse(result.contains("person@example.com"));
    Assert.assertFalse(result.contains("How many pets do you have?"));
  }
  @Test
  public void testNotLoggedInUser() throws IOException {
    // test to see if a not logged in user will be able to
    // look at tests a user has created
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);
    UserService userService = mock(UserService.class);
    when(userService.isUserLoggedIn()).thenReturn(false);
    
    ExamsTakenServlet servlet= new ExamsTakenServlet();
    servlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "You are not authorised to view this page");
  }
  private void setFakeTest() {
    /*Set a fake test*/
    Long date = (new Date()).getTime(); 
    List<Long> fakeQuestionList = new ArrayList<Long>();
    fakeQuestionList.add(1L);
    fakeQuestionList.add(2L);
    Entity testEntity = new Entity("Exam", 7L);
    testEntity.setProperty("name", "Trial");
    testEntity.setProperty("duration", "30");
    testEntity.setProperty("ownerID", "test@google.com");
    testEntity.setProperty("date", date); 
    testEntity.setProperty("questionsList", fakeQuestionList);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(testEntity);
  }
   private void setFakeResponeses () {
    /*Set up two fake response entities for testing purposes */
    Entity responseEntity = new Entity("1", "test@google.com");
    responseEntity.setProperty("answer", "Tuesday");
    responseEntity.setProperty("marks", "5");
    responseEntity.setProperty("email", "test@google.com");
    Entity anotherResponseEntity = new Entity("2", "test@google.com");
    anotherResponseEntity.setProperty("answer", "2011");
    anotherResponseEntity.setProperty("marks", "5");
    anotherResponseEntity.setProperty("email", "test@google.com");
    
    Entity responseToDifferentUser = new Entity("4", "person@example.com");
    responseToDifferentUser.setProperty("answer", "6");
    responseToDifferentUser.setProperty("marks", "15");
    responseToDifferentUser.setProperty("email", "person@example.com");
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(responseEntity);
    datastore.put(anotherResponseEntity);
    datastore.put(responseToDifferentUser);
  }
   private void setFakeQuestions () {
    /*Set up two fake question entities for testing purposes */
    Long date = (new Date()).getTime(); 
    Entity questionEntity = new Entity("Question", 1L);
    questionEntity.setProperty("question", "What day is it?");
    questionEntity.setProperty("marks", "5");
    questionEntity.setProperty("date", date);
    questionEntity.setProperty("ownerID", "test@google.com");
    Entity anotherQuestionEntity = new Entity("Question", 2L);
    anotherQuestionEntity.setProperty("question", "What year is it?");
    anotherQuestionEntity.setProperty("marks", "10");
    anotherQuestionEntity.setProperty("date", date);
    anotherQuestionEntity.setProperty("ownerID", "test@google.com");
    
    Entity questionByDifferentUser = new Entity("Question", 4L);
    questionByDifferentUser.setProperty("question", "How many pets do you have?");
    questionByDifferentUser.setProperty("marks", "15");
    questionByDifferentUser.setProperty("date", date);
    questionByDifferentUser.setProperty("ownerID", "person@example.com");
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(questionEntity);
    datastore.put(anotherQuestionEntity);
    datastore.put(questionByDifferentUser);
  }
  private void helperLogin() {
    /* Login user with email "test@google.com" */
    helper.setEnvAuthDomain("google.com");
    helper.setEnvEmail("test@google.com");
    helper.setEnvIsLoggedIn(true);
  }
}
