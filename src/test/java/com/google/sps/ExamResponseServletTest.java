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

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.UtilityClass;
import com.google.sps.servlets.ExamResponseServlet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for Exam Response Servlet to see if users exam responses are stored correctly.
 *
 * @author Aidan Molloy
 */

@RunWith(JUnit4.class)
public final class ExamResponseServletTest extends ExamResponseServlet {
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

  /* Test Storing a users answers to an exam */

  @Test
  public void doPostTest() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    List<String> parameterNames = new ArrayList<>();
    parameterNames.add("1");
    Enumeration<String> parameterNamesEnumerated = Collections.enumeration(parameterNames);
    String[] parameterValues = {"Answer1"};

    when(request.getParameterNames()).thenReturn(parameterNamesEnumerated);
    when(request.getParameterValues("1")).thenReturn(parameterValues);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    ExamResponseServlet servlet = new ExamResponseServlet();
    servlet.doPost(request, response);

    // Make query to datastore to make sure it was stored correctly
    Query query = new Query("1");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    // Convert the received entity into a json string to check content
    Map<String, String> examResponse = new HashMap<>();
    examResponse.put("answer", (String) entity.getProperty("answer"));
    String result = UtilityClass.convertToJson(examResponse);
    Assert.assertTrue(result.contains("\"answer\":\"Answer1\""));
  }
}
