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

import com.google.sps.data.ExamClass;
import com.google.sps.data.QuestionClass;
import java.io.IOException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.gson.Gson;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import javax.servlet.ServletException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that stores and returns exams*/
@WebServlet("/createExam")
public class CreateExamServlet extends HttpServlet{
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //Servlet Recevies information from the client about a exam they want to create and saves it in
    //the datastore
    Long date = (new Date()).getTime(); 
    String name= getParameter(request, "name","");
    String duration = getParameter(request, "duration", "");

    UserService userService = UserServiceFactory.getUserService();
    String ownerID = userService.getCurrentUser().getEmail(); 
    List<Long> list = new ArrayList<>();

    //Set up the new Exam and save it in the datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity examEntity = new Entity("Exam");
    examEntity.setProperty("name",name);
    examEntity.setProperty("duration",duration);
    examEntity.setProperty("ownerID",ownerID);
    examEntity.setProperty("date", date);
    examEntity.setProperty("questionsList",list);
    try{
      datastore.put(examEntity);    
    }catch (DatastoreFailureException e){
      System.out.println("Datastore is not responding right now. Try Again Later");
    }

    response.sendRedirect("/createExam.html");
    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(examEntity));
  }
  private String getParameter(HttpServletRequest request, String name, String defaultValue){
    /* Gets Parameters from the Users Page
     *
     * Return: Returns the requested parameter or the default value if the parameter
     *  wasn't specified by the User.   
     */
    String value = request.getParameter(name);
    if(value == null){
        return defaultValue;
    }
    return value;
  }
  private String convertToJsonUsingGson(Entity exam) {
    /* Converts the exam entity to a json string using Gson
    *
    *Arguments: Exam entity
    *
    *Returns: json string of the exam entity
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(exam);
    return json;
  }
}
