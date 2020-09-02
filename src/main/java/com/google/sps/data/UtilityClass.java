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
import com.google.sps.data.*;
import com.google.appengine.api.datastore.Entity;
import java.util.Map;
import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

public final class UtilityClass{
  /*Class that has all the methods that we constantly reuse and rewrite
  * @author Klaudia Obieglo
  */  
  
  public static String getParameter(HttpServletRequest request, String name, String defaultValue){
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
  public static String convertToJson(Entity entity) {
    /* Converts the entity to a json string using Gson
    *
    *Arguments: Entity 
    *
    *Returns: json string of the entity
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(entity);
    return json;
  }
  public static String convertToJson(Map<String, String> authResponse) {
    /**
    * Converts data into a JSON string using the Gson library.
    *
    * @param     authResponse    map to be converted into a json string
    * @return                    a json string converted from authResponse map
    */
    Gson gson = new Gson();
    String json = gson.toJson(authResponse);
    return json;
  }
  public static String convertToJson(List listOfInstances) {
    /* Converts the list to a json string
    *
    *Arguments: List of some sort of instances
    *
    *Returns: json string of the list
    *
    */
    Gson gson = new Gson();
    String json = gson.toJson(listOfInstances);
    return json;
  }
  public static Long generateUniqueId() {
    //Return random ID
    long value = -1;
    while(value < 0) {
      value = UUID.randomUUID().getMostSignificantBits();
    }
    return value; 
  }
} 

