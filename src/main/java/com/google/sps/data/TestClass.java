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
public final class TestClass{
  /*Class that creates tests */
  private final String testName;
  private final long testID;
  private final double testDuration;
  private final String ownersID;
  private final List<Long> questionList;

  public TestClass(String testName, long testID, double testDuration, String ownersID, List<Long> questionList)
  {
    /* Constructor for the test Class
    * Arguments:
    *  testName: Name of the Test
    *  testID: Unique ID of this Test
    *  testDuration: How long the students have for this exam
    *  ownersID: email of the user who created the test
    *
    */
    this.testName = testName;
    this.testID = testID;
    this.testDuration = testDuration;
    this.ownersID = ownersID;
    this.questionList = questionList;
  }
  public String getTestName(){
    /*Getter method to get the test's Name*/
    return testName;
  }
  public long getTestID(){
    /*Getter method to get the test ID */
    return testID;
  }
  public double getTestDuration(){
    /*Getter method to get test duration */
    return testDuration;
  }
  public String getOwnersID(){
    /* Getter method to get the owners id*/
    return ownersID;
  }
  public List<Long> questionList(){
    /* Getter method to get questions list*/
    return questionList;
  }
}
