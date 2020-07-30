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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

/**
 * The authentication servlet is responsible for authenticating users.
 */
@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Set response header
    response.setContentType("application/json;");

    // Reference to UserService
    UserService userService = UserServiceFactory.getUserService();

    // Create map to store the response
    Map<String, String> authResponse = new HashMap<String, String>();

    // Check if user is logged
    if (userService.isUserLoggedIn()) {
      // If logged in get email and create link to logout
      String userEmail = userService.getCurrentUser().getEmail();
      String logoutUrl = userService.createLogoutURL("/");

      // Enter information for response
      authResponse.put("email", userEmail);
      authResponse.put("logoutUrl", logoutUrl);
    } else {
      // If logged out get login link
      String loginUrl = userService.createLoginURL("/");

      // Enter information for response
      authResponse.put("loginUrl", loginUrl);
    }

    // Convert into JSON
    String json = convertToJson(authResponse);

    // Write response to /auth
    response.getWriter().println(json);
  }

  // Convert into a JSON string using the Gson library.
  private String convertToJson(Map<String, String> authResponse) {
    Gson gson = new Gson();
    String json = gson.toJson(authResponse);
    return json;
  }
}
