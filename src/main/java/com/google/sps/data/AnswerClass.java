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

public final class AnswerClass{
  /*Class that creates exam question responses */
  private String questionID;
  private String answer;
  private String givenMarks;
  private String possibleMarks;
  private String questionValue;

  public AnswerClass(String questionID, String answer, String givenMarks, 
    String questionValue, String possibleMarks)
  {
    /* Constructor for the Answer Class
    * Arguments:
    *  questionID: Unique ID of the question
    *  answer: Answer given by student to question
    *  givenMarks: Marks given by exam owner for answer
    *  questionValue: The question being answered
    *  possibleMarks: Maximum marks that can be given for answer
    *
    */
    this.questionID = questionID;
    this.answer = answer;
    this.givenMarks = givenMarks;
    this.questionValue = questionValue;
    this.possibleMarks = possibleMarks;
  }
  public String getQuestionID(){
    /*Getter method to get the question ID*/
    return questionID;
  }
  public String getAnswer(){
    /*Getter method to get the answer*/
    return answer;
  }
  public String getGivenMarks(){
    /*Getter method to get the given marks */
    return givenMarks;
  }
  public String getQuestionValue(){
    /* Getter method to get the question*/
    return questionValue;
  }
  public String getPossibleMarks(){
    /* Getter method to get the possible marks*/
    return possibleMarks;
  }
}
