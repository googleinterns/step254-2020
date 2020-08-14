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
//

let userAuth = false;
/**
 * Authenticate user
 */
async function authenticate() {
  try{
    logInOut = document.getElementById('logInOut');
    const response =  await fetch('/auth'); 
    const user_details =  await response.json();

    if (user_details.email) {
      userAuth = true;
      logInOut.innerHTML = `<a id= "login" href="${user_details.logoutUrl}"
      >Logout</a>`;
      setPreference();
    } else {
      userAuth = false;
      logInOut.innerHTML = `<a href="${user_details.loginUrl}">Login</a>`;
    }
  }catch(e){
    console.log('Error: ', e.message);
  }
};

/**
 * Set user UI preferneces
 */
async function setPreference() {
  try{
    const response =  await fetch("/auth"); 
    const user_details =  await response.json();
    let userFont = user_details.font;
    let userFontSize = user_details.font_size;
    let userFontColor = user_details.text_color;
    let userBackgroundColor = user_details.bg_color;

    document.body.style.fontFamily = userFont;
    document.body.style.fontSize = userFontSize + 'px';
    document.body.style.color = userFontColor;
    document.body.style.backgroundColor = userBackgroundColor;
  }
  catch(e){
    console.log('Error: ', e.message);
  }     
}

/* eslint-disable no-unused-vars */
/**
 * Check if user has access to page
 */
function pageAccess(){
  if (userAuth === true) {
    window.location.href = 'dashboard.html';
    console.log(window.location.href );
  } else {
    document.getElementById(
        'accessDenied',
    ).innerHTML = `<p> Cannot access until you login</p>`;
  };
}
/* eslint-enable no-unused-vars */

// On load
window.onload = function(){
    authenticate();
}

// Export modules for testing
if (typeof exports !== 'undefined') {
    module.exports = {
        authenticate,
        setPreference,
        pageAccess,
        userAuth,
    }
}