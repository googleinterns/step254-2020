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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Exam Servlet for when user is logged out, when no exam is requested, 
 * invalid exam and real exams are requested.
 *
 * @author Aidan Molloy
 */
@RunWith(JUnit4.class)
public final class ExamServletTest extends ExamServlet {
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
    ExamServlet servlet = new ExamServlet();
    servlet.doGet(request, response);
    verify(response).sendRedirect("/");
  }

  /* Login user with email "test@google.com" */
  private void helperLogin() {
    helper.setEnvAuthDomain("google.com");
    helper.setEnvEmail("test@google.com");
    helper.setEnvIsLoggedIn(true);
  }

  /* Set up fake exam */
  private void addExam() {
    helperLogin();
    Entity examEntity = new Entity("Exam");
    examEntity.setProperty("name", "Test Exam");
    examEntity.setProperty("duration", "20");
    examEntity.setProperty("ownerID", "test@google.com");
    examEntity.setProperty("date", "20");
    examEntity.setProperty("questionsList", new ArrayList<>());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(examEntity);
  }

  /* When no examID is provided. */
  @Test
  public void doGetTestNoExamID() throws IOException, ServletException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ExamServlet servlet = new ExamServlet();

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    ServletConfig config = mock(ServletConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(config.getServletContext()).thenReturn(context);

    //Get the path to the target files were templates are stored for tests
    String filePath = new File(".").getCanonicalPath();
    String endPath = "/target/portfolio-1/WEB-INF/templates";
    String path = filePath + endPath;
    when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);

    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("Choose an exam to take"));
  }

  /* When examID that does not exist is provided */
  @Test
  public void doGetTestInvalidExamID() throws IOException, ServletException {
    helperLogin();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ExamServlet servlet = new ExamServlet();
    when(request.getParameter("examID")).thenReturn("1");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    ServletConfig config = mock(ServletConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(config.getServletContext()).thenReturn(context);

    //Get the path to the target files were templates are stored for tests
    String filePath = new File(".").getCanonicalPath();
    String endPath = "/target/portfolio-1/WEB-INF/templates";
    String path = filePath + endPath;
    when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);

    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("Selected exam is not available."));
    Assert.assertTrue(result.contains("Choose an exam to take"));
  }

  /**
   *  Get the ExamID of existing exam.
   */
  public String getExamID() {
    Query query = new Query("Exam");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      return String.valueOf(entity.getKey().getId());
    }
    return "1";
  }

  /**
   * When a real exam is requested.
   */
  @Test
  public void doGetTestExam() throws IOException, ServletException {
    helperLogin();
    addExam();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ExamServlet servlet = new ExamServlet();
    when(request.getParameter("examID")).thenReturn(getExamID());


    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    ServletConfig config = mock(ServletConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(config.getServletContext()).thenReturn(context);

    //Get the path to the target files were templates are stored for tests
    String filePath = new File(".").getCanonicalPath();
    String endPath = "/target/portfolio-1/WEB-INF/templates";
    String path = filePath + endPath;
    when(context.getRealPath("/WEB-INF/templates/")).thenReturn(path);

    servlet.init(config);
    servlet.doGet(request, response);
    String result = stringWriter.toString();
    Assert.assertTrue(result.contains("Exam Name: Test Exam"));
    Assert.assertTrue(result.contains("Length: 20"));
    Assert.assertTrue(result.contains("Created By: test@google.com"));
  }

}