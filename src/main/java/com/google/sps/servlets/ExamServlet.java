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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to display the requested exam questions and send responses to ExamResponseServlet.
 *
 * @author  Aidan Molloy
 */
@WebServlet("/exam")
public class ExamServlet extends HttpServlet{
  /**
   * Gets the exam ID from the httpRequest
   *
   * Arguments: 
   *  request: provides request information from the HTTP servlet.
   *  response: response object where servlet will write information to.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    String examID = getParameter(request,"examID","");
    Entity examEntity = new Entity("Exam");
    try{
      examEntity = getEntity(examID);
    }catch (EntityNotFoundException e){
      System.out.println("Entity was not found");
    }
    String name = (String) examEntity.getProperty("name");
    String duration = (String) examEntity.getProperty("duration");
    String ownerID = (String) examEntity.getProperty("ownerID");
    List<Long> list = (List<Long>) examEntity.getProperty("questionsList");
    ExamClass exam = new ExamClass(name,examEntity.getKey().getId(),
      Double.parseDouble(duration),ownerID,list);
    
    List<QuestionClass> listofQuestions=getQuestionsFromExam(list);

    response.setContentType("application/json;");
    response.getWriter().println(convertToJsonUsingGson(exam));
    response.getWriter().println(convertToJsonUsingGson(listofQuestions));
  }
  private List<QuestionClass> getQuestionsFromExam(List<Long> list){
    /*Grab all the question id's from the list and creates instances of 
    *questions from them.
    */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<QuestionClass> questionList = new ArrayList<>();
    for(int i=0 ; i<list.size(); i++)
    {
      try{
        Key key = KeyFactory.createKey("Question", list.get(i));
        Entity qs = datastore.get(key);

        long questionID = qs.getKey().getId();
        String question = (String) qs.getProperty("question");
        String marks = (String) qs.getProperty("marks");
        String ownerID = (String) qs.getProperty("ownerID");
        QuestionClass question1 = new QuestionClass(question, questionID,
          Double.parseDouble(marks), ownerID);
        questionList.add(question1);
      } catch( EntityNotFoundException e)
      {  
        System.out.println("Entity was not found");
      }
    }
    return questionList;
  }
  private Entity getEntity(String entityID) throws EntityNotFoundException{
    /*Function that will return a exam entity based on the entity ID */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey("Exam", Long.parseLong(entityID));
    Entity entity = datastore.get(key);
    return entity;
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
   private String convertToJsonUsingGson(ExamClass exam) {
    /* Converts the exam to a json string using Gson
    *
    *Arguments: Exam instance
    *
    *Returns: json string of the exam instance
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(exam);
    return json;
  }
    private String convertToJsonUsingGson(List<QuestionClass> questions) {
    /* Converts the question List to a json string using Gson
    *
    *Arguments: List of questions
    *
    *Returns: json string of the questions
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(questions);
    return json;
  }
}