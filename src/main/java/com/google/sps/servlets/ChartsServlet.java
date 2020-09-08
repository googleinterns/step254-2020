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

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
 * Servlet that processes users results to json in order to
 * create a pie chart of results
 *
 * @author Klaudia Obieglo
 */
@WebServlet("/charts")
public class ChartsServlet extends HttpServlet {
  static Map<String,Integer> results = null;
@Override
  public void doGet(final HttpServletRequest request,
       final HttpServletResponse response) throws IOException {
    /* Convert the results map to json and return it */
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(results);
    response.getWriter().println(json);
  } 
  public static void charts(Map<String,Integer> chart) {
    /* Set the results map to equal the chart */
    results = chart;
  }
}