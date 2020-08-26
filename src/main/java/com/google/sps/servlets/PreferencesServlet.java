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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The authentication servlet is responsible for authenticating users and retrieving user
 * preferences from datastore.
 *
 * @author Aidan Molloy
 */
 
@WebServlet("/preferences")
public class PreferencesServlet extends HttpServlet {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * Checks if the user is logged in with UserService and prints response in json.
   *
   * @param request  provides request information from the HTTP servlet
   * @param response response object where servlet will write information to
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    String email = request.getHeaders("email").nextElement();
    Map<String, String> authResponse = new HashMap<>();

    // Get User Info and add it to the response
    authResponse.putAll(getUserInfo(email));
  
    String json = UtilityClass.convertToJson(authResponse);

    // Write response to /auth
    response.getWriter().println(json);
  }

  /**
   * Returns the preferences of the user with email, or null if the user has not set their
   * preferences.
   *
   * @param email the email of logged in user to retrieve linked userInfo from datastore
   * @return a map of userInfo linked to the user email
   */
  private Map<String, String> getUserInfo(String email) {
    Map<String, String> userInfoResponse = new HashMap<>();
    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query =
          new Query("UserInfo")
              .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      if (entity == null) {
        userInfoResponse.put("updateInfoRequired", "true");
        userInfoResponse.put("email", email);
        return userInfoResponse;
      }
      userInfoResponse.put("name", (String) entity.getProperty("name"));
      userInfoResponse.put("font", (String) entity.getProperty("font"));
      userInfoResponse.put("font_size", (String) entity.getProperty("font_size"));
      userInfoResponse.put("bg_color", (String) entity.getProperty("bg_color"));
      userInfoResponse.put("text_color", (String) entity.getProperty("text_color"));
      userInfoResponse.put("email", email);
      return userInfoResponse;
    } catch (Exception e) {
      userInfoResponse.put("errorMsg", "Something went wrong. Please try again later.");
      logger.atSevere().log("There was an error: %s", e);
      return userInfoResponse;
    }
  }
}
