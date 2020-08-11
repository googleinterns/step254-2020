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


import com.google.sps.data.*;
import java.io.IOException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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
    response.setContentType("text/html;");
    String examID = UtilityClass.getParameter(request, "examID", "1");

    Entity examEntity = null;

    try{
      examEntity = getEntity(examID);
    }catch (EntityNotFoundException e){
    }

    if(examEntity != null) {
      String name = (String) examEntity.getProperty("name");
      String duration = (String) examEntity.getProperty("duration");
      String ownerID = (String) examEntity.getProperty("ownerID");
      List<Long> questionsList = (List<Long>) examEntity.getProperty("questionsList");
      ExamClass exam = new ExamClass(name,examEntity.getKey().getId(),
        Double.parseDouble(duration),ownerID,questionsList);
        
      
      response.getWriter().println("<h1>Exam Name: " + exam.getName() + "</h1>");
      response.getWriter().println("<h2>Length: " + exam.getDuration() + "</h2>");
      response.getWriter().println("<h2>Created By: " + exam.getOwnerID() + "</h2>");
      if(questionsList != null){
        List<QuestionClass> listofQuestions=getQuestionsFromExam(questionsList);
        response.getWriter().println(UtilityClass.convertToJson(listofQuestions));
      }else{
        response.getWriter().println("<p>There are no questions associated with this exam.</p>");
      }
    } else {
      response.getWriter().println("<h1>Choose an exam to take.</h1>");
      response.getWriter().println("<table><tr><th>ID</th><th>Name</th><th>Duration</th></tr>");

      Query query = new Query("Exam");
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);
      for (Entity entity : results.asIterable()) {
        // Send back amount of results requested
        long id = entity.getKey().getId();
        String name = (String) entity.getProperty("name");
        String duration = (String) entity.getProperty("duration");

      response.getWriter().println("<tr>");
      response.getWriter().println("<td>" + id + "</td>");
      response.getWriter().println("<td>" + name + "</td>");
      response.getWriter().println("<td>" + duration + "</td>");
      response.getWriter().println("<td><a href=\"/exam?examID=" + id + "\">Take Exam</a></td>");
      response.getWriter().println("</tr>");
      }
      response.getWriter().println("</table>");
    }

  }
  private List<QuestionClass> getQuestionsFromExam(List<Long> list){
    /*Grab all the question id's from the list and creates instances of 
    *questions from them.
    */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<QuestionClass> questionList = new ArrayList<>();
    if (list == null){
      return null;
    }
    for(int i=0 ; i<list.size(); i++)
    {
      try{
        Key key = KeyFactory.createKey("Question", list.get(i));
        Entity qs = datastore.get(key);

        long questionID = qs.getKey().getId();
        String question = (String) qs.getProperty("question");
        String marks = (String) qs.getProperty("marks");
        String ownerID = (String) qs.getProperty("ownerID");
        QuestionClass questionToBeEntered = new QuestionClass(question, questionID,
          Double.parseDouble(marks), ownerID);
        questionList.add(questionToBeEntered);
      } catch( Exception e)
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
    if(entity == null) {
      return null;
    }
    return entity;
  }
}