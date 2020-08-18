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
let userName = '';
/**
 * Authenticate user
 */
async function authenticate() {
  try {
    logInOut = document.getElementById('logInOut');
    const response = await fetch('/auth');
    const userDetails = await response.json();

    if (userDetails.email) {
      userAuth = true;
      logInOut.innerHTML = `<a id= "login" href="${userDetails.logoutUrl}"
      >Logout</a>`;
      setPreference();
    } else {
      userAuth = false;
      logInOut.innerHTML = `<a href="${userDetails.loginUrl}">Login</a>`;
    }
  } catch (e) {
    console.log('Error: ', e.message);
  }
};

/**
 * Set user UI preferneces
 */
async function setPreference() {
  try {
    const response = await fetch('/auth');
    const userDetails = await response.json();
    const userFont = userDetails.font;
    const userFontSize = userDetails.font_size;
    const userFontColor = userDetails.text_color;
    const userBackgroundColor = userDetails.bg_color;
    userName = userDetails.name;

    document.body.style.fontFamily = userFont;
    document.body.style.fontSize = userFontSize + 'px';
    document.body.style.color = userFontColor;
    document.body.style.backgroundColor = userBackgroundColor;
  } catch (e) {
    console.log('Error: ', e.message);
  }
}

/* eslint-disable no-unused-vars */
/**
 * Check if user has access to page
 */
function pageAccess() {
  console.log(userName);
  if (userAuth === true) {
    const page = newUser(userName);
    console.log(page);
    window.location.href = page;
  } else {
    document.getElementById(
        'accessDenied',
    ).innerHTML = `<p> Cannot access until you login</p>`;
  }
};

/**
 * Check if user is new
 * @param {string} name name of the user
 * @return {string} return href depending on user status
 */
function newUser(name) {
  if (name === null) {
    return 'userSetUp.html';
  } else if (name === undefined) {
    return 'userSetUp.html';
  } else {
    return 'dashboard.html';
  }
};
/* eslint-enable no-unused-vars */

// On load
window.onload = function() {
  authenticate();
};

// Export modules for testing
if (typeof exports !== 'undefined') {
  module.exports = {
    authenticate,
    setPreference,
    pageAccess,
    userAuth,
    userName,
    newUser,
  };
};
