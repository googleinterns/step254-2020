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
window.onload = async function authenticate() {
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
};

/**
 * Set user UI preferneces
 */
async function setPreference() {
  const response =  await fetch("/auth"); 
  const user_details =  await response.json();

  userFont = user_details.font;
  userFontSize = user_details.font_size;
  userFontColor = user_details.text_color;
  userBackgroundColor = user_details.bg_color;

  document.body.style.fontFamily = userFont;
  document.body.style.fontSize = userFontSize + 'px';
  document.body.style.color = userFontColor;
  document.body.style.backgroundColor = userBackgroundColor;
      
}
/* eslint-disable no-unused-vars */
/**
 * Check if user has access to page
 */
function pageAccess() {
  if (userAuth === true) {
    window.location.href = 'dashboard.html';
  } else {
    document.getElementById(
        'accessDenied',
    ).innerHTML = `<p> Cannot access until you login</p>`;
  };
}
/* eslint-enable no-unused-vars */
