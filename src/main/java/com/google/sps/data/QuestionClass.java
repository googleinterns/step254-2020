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

public final class QuestionClass{
  /*Class that creates a question */
  private final String question;
  private final long questionID;
  private final double marks;
  private final String ownerID;

  public QuestionClass(String question, long questionID, double marks, String ownerID)
  {
    /* Constructor for the Question Class
    * Arguments:
    *  question: the question being asked
    *  questionID: Unique ID of this question
    *  marks: How many marks this question is worth
    *  testID: ID of the test this question is part of
    *
    */
    this.question = question;
    this.questionID = questionID;
    this.marks = marks;
    this.ownerID = ownerID;
    // this.testID = testID;
  }
  public String getQuestion(){
    /*Getter method to get questions */
    return question;
  }
  public long getQuestionID(){
    /*Getter method to get the question ID */
    return questionID;
  }
  public double getQuestionMarks(){
    /*Getter method to get marks for the question */
    return marks;
  }
//   public long getTestID(){
//     /* Getter method to get the test id to which the question belongs to*/
//     return testID;
//   }
  public String getOwnerID(){
    /*Getter method for ownerID */
    return ownerID;
  }
}
