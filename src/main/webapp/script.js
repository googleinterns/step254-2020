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
let counter = 1;
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
      if (userDetails.invalidLogin) {
        logInOut.innerHTML = `<a id= "login" href="${userDetails.logoutUrl}"
        >Logout</a>`;
        alert(`We can only allow @google.com users currently. Please logout 
        and make sure you are logged in with your @google.com account before 
        trying again.`);
      } else {
        userAuth = false;
        logInOut.innerHTML = `<a href="${userDetails.loginUrl}">Login</a>`;
      }
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
  if (userAuth) {
    const page = newUser(userName);
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
    return '/dashboardServlet';
  }
};

/**
 * Gets the list of checkbox items
 */
function getCheckBox() {
  const checkBoxList = document.querySelectorAll('#checkbox');
  const submitButton = document.getElementById('checkBoxSubmit');
  isChecked(checkBoxList, submitButton);
};

/**
 * Checks if any items in the list are checked
 * @param {NodeListOf<Element>} checkBoxList List of checkbox items
 * @param {HTMLElement} submitButton Html for submit button
 * @return {Boolean} returns if any boxes are checked
 */
function isChecked(checkBoxList, submitButton) {
  const checkBoxArray = [...checkBoxList];
  const areTheyChecked = checkBoxArray.some((box) => box.checked);
  if (areTheyChecked) {
    submitButton.style.display = 'block';
  } else {
    submitButton.style.display = 'none';
  }
  return areTheyChecked;
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
    isChecked,
  };
};
/* eslint-disable no-unused-vars */
/** Checks if the MCQ checkbox is checked */
function getMcqChecked() {
  const mcqCheck = document.getElementById('mcqCheckBox');
  const addFieldsButton = document.getElementById('addFields');
  const removeFieldsButton= document.getElementById('removeFields');
  const fieldsList = document.getElementById('fieldsList');
  const mcqAnswer = document.getElementById('mcqAnswer');
  const mcqLine = document.getElementById('mcq');
  if (mcqCheck.checked) {
    addFieldsButton.style.display = 'block';
    removeFieldsButton.style.display = 'block';
    fieldsList.style.display = 'block';
    mcqLine.style.display = 'block';
    mcqAnswer.style.display = 'block';
  } else {
    addFieldsButton.style.display = 'none';
    removeFieldsButton.style.display = 'none';
    mcqAnswer.style.display = 'none';
    mcqLine.style.display = 'none';
    fieldsList.style.display = 'none';
  }
};
/** Add more input fields for the MCQ answers */
function moreFields() {
  if (counter >= 5) {
    document.getElementById('popup').style.display = 'block';
  } else {
    const dropdown = document.createElement('OPTION');
    dropdown.innerHTML ='<option>' + counter + '</option';
    document.getElementById('mcqAnswer').append(dropdown);
    document.getElementById('mcqAnswer').style.display = 'block';
    document.getElementById('mcq').style.display = 'block';
    const number = document.createElement('SPAN');
    number.innerHTML = counter + '. ';
    const field = document.createElement('input');
    field.type ='text';
    field.name = 'mcqField';
    field.id = 'mcqField';
    field.cols = '50';
    field.rows = '3';
    field.style.display = 'block';
    field.required = true;
    number.append(field);
    document.getElementById('fieldsList').append(number);
    document.getElementById('removeFields').style.display = 'block';
    counter++;
  }
};
/** Remove the last field from the MCQ answers */
function lessFields() {
  if (counter > 1) {
    const listOfFields = document.getElementById('fieldsList').lastChild;
    listOfFields.parentNode.removeChild(listOfFields);
    document.getElementById('popup').style.display = 'none';
    counter--;
  }
};
/* eslint-disable no-unused-vars */
