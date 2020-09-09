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
 
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.flogger.FluentLogger;
import com.google.sps.data.UtilityClass;
import freemarker.template.Version;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that shows the exam a user has created
* @author Klaudia Obieglo
* @author Róisín O'Farrell
*/
@WebServlet("/showExam")
public class ShowExamServlet extends HttpServlet{
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  Configuration cfg;
  Map data = new HashMap();
  Map<String,Integer> examMarks = new LinkedHashMap<String, Integer>();
  //set up the configuration once
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    cfg = new Configuration(Configuration.VERSION_2_3_30);
    String path = getServletContext().getRealPath("/WEB-INF/templates/");
    try {
      cfg.setDirectoryForTemplateLoading(new File(path));
    } catch (IOException e) {
      logger.atWarning().log("Could not set directory for template loading: %s", e);
    }
    // Recommended settings for new projects:
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
  }
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /*Returns the exams that the user has created */
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      logger.atWarning().log("User is not logged in.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "You are not authorised to view this page");
      return;
    }
    examMarks.clear();
    String ownerID = userService.getCurrentUser().getEmail(); 
    String examID = UtilityClass.getParameter(request, "examID", "");
    //grab exams user owns
    try {
      getExam(ownerID, Long.parseLong(examID));
    } catch (EntityNotFoundException e){
      logger.atWarning().log("Exam entity was not found: %s",e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to find your exam");
      return;
    }
    //grab exam responses
    try {
      getResponses(ownerID, Long.parseLong(examID));
    } catch (EntityNotFoundException e){
      logger.atWarning().log("Exam responses were not found: %s",e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to find your responses");
      return;
    }
    
    if (examMarks != null){
      ChartsServlet.charts(examMarks);
    }
    // run to freemarker template
    try {
      Template template = cfg.getTemplate("ShowExamCreated.ftl");
      PrintWriter out = response.getWriter();

      template.process(data, out);
      logger.atInfo().log("Dashboard was displayed correctly for the User:"
          + "%s", userService.getCurrentUser());
    } catch (TemplateException e) {
      logger.atWarning().log("There was a problem with processing the template %s", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal Error occurred when trying to display the Question Form");
      return;
    }
  }
  public synchronized void getExam(String ownerID, Long examID) throws EntityNotFoundException{
    //Gets the requests exam and all the data needed to display on the Show Exam's
    //created page.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Map<String,String> examQuestions = new LinkedHashMap<String, String>();
    Map<String,String> examMap = new LinkedHashMap<String, String>();
    Map<String,List<String>> mcqAnswersMap = new LinkedHashMap<String, List<String>>();
    try {
      //Find the exam and store the details of that exam in the exam Map
      Key key = KeyFactory.createKey("Exam", examID);
      Entity exam = datastore.get(key);
      String examName = (String) exam.getProperty("name");
      String duration = (String) exam.getProperty("duration");
      examMap.put(examName, duration);
      data.put("exam", examMap);
      Entity questionEntity = null;
      //Check if questionList is null , if so return
      List<Long> questionList = (List<Long>) exam.getProperty("questionsList");
      if(questionList == null) {
        logger.atWarning().log("Question List was null for the exam: %s",examID);
        return;
      }
      // Go through the question List, find each question and marks for it
      for(int i=0; i<questionList.size(); i++) {
        key = KeyFactory.createKey("Question",questionList.get(i));
        questionEntity = datastore.get(key);
        String question = (String) questionEntity.getProperty("question");
        String marks = (String) questionEntity.getProperty("marks");
        String type = (String) questionEntity.getProperty("type");

        if(type.compareTo("MCQ") == 0) {
          List<String> answers = new ArrayList<>();
          List<String> answerList = (List<String>) questionEntity.getProperty("mcqPossibleAnswers");
          for(int j=0; j<answerList.size(); j++) {
            answers.add(answerList.get(j));
           }
           mcqAnswersMap.put(question, answers);
           data.put("MCQ", mcqAnswersMap);
        }
        examQuestions.put(question, marks);
        data.put("question", examQuestions);
      }
      logger.atInfo().log("Questions were found and added to the examQuestions Map");
    } catch (DatastoreFailureException e) {
      logger.atSevere().log("Datastore error:%s" ,e);
      return;
    }
  }

   public synchronized void getResponses(String ownerID, Long examID) throws EntityNotFoundException{
    //Gets the all the marks for an exam to display in a chart
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    int totalMark= 0;
    int givenMark = 0;
    List<String> studentList = new ArrayList();
    try {
      //Find the exam 
      Key key = KeyFactory.createKey("Exam", examID);
      Entity exam = datastore.get(key);
      //Check if questionList is null , if so return
      List<Long> questionList = (List<Long>) exam.getProperty("questionsList");
      if(questionList == null) {
        logger.atWarning().log("Question List was null for the exam: %s",examID);
        return;
      }
      Long questionID = questionList.get(0);
      String responseQuery = Long.toString(questionID);
      /* Get all responses using query as the question response does not 
      have a known ID/Name*/
      Query queryResponses = new Query(responseQuery);
      PreparedQuery questionResponses = datastore.prepare(queryResponses);
      for (Entity responders : questionResponses.asIterable()) {
        if(responders != null){
          String email = (String) responders.getProperty("email");
          studentList.add(email);
        }  
      }
      // Go through the student List, find marks given for each question
      for(String student : studentList) {
          totalMark = 0;
        for(Long question: questionList){
            try{
              String response = Long.toString(question);
              key = KeyFactory.createKey(response, student);
              Entity responseEntity = datastore.get(key);
              String marks = (String) responseEntity.getProperty("marks");
              if(marks == null){
                givenMark = 0;  
              } else {
                givenMark = Integer.parseInt(marks);
              } 
              totalMark = totalMark + givenMark;
            } catch (DatastoreFailureException e) {
              totalMark = 0;
              break;
            }
        }
        String finalMark = Integer.toString(totalMark);
        System.out.println(finalMark);
        if (examMarks.containsKey(finalMark)) {
          examMarks.put(finalMark, examMarks.get(finalMark) + 1);
        } else {
          examMarks.put(finalMark, 1);
        }
      }
      logger.atInfo().log("Marks were found and added to the examMarks Map");
    } catch (DatastoreFailureException e) {
      logger.atSevere().log("Datastore error:%s" ,e);
      return;
    }
  }
}
