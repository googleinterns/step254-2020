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

package com.google.sps.data;
import com.google.sps.data.QuestionClass;
import java.util.List;
import com.google.appengine.api.datastore.Key;
public final class ExamClass{
  /*Class that creates exams */
  private final String name;
  private final long examID;
  private final double duration;
  private final String ownerID;
  private final List<Long> questionList;

  public ExamClass(String name, long examID, double duration, String ownerID,
    List<Long> questionList)
  {
    /* Constructor for the Exam Class
    * Arguments:
    *  name: Name of the Exam
    *  examID: Unique ID of this Exam
    *  duration: How long the students have for this exam
    *  ownerID: email of the user who created the test
    *  List<long> questionsList : list of questions id's that belong to this test
    *
    */
    this.name = name;
    this.examID = examID;
    this.duration = duration;
    this.ownerID = ownerID;
    this.questionList = questionList;
  }
  public String getName(){
    /*Getter method to get the exam's Name*/
    return name;
  }
  public long getExamID(){
    /*Getter method to get the test ID */
    return examID;
  }
  public double getDuration(){
    /*Getter method to get test duration */
    return duration;
  }
  public String getOwnerID(){
    /* Getter method to get the owner id*/
    return ownerID;
  }
  public List<Long> questionList(){
    /* Getter method to get questions list*/
    return questionList;
  }
}
