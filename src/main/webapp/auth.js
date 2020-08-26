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
  * initAuth handles setting up UI event listeners and registering Firebase auth listeners:
  *  - firebase.auth().onAuthStateChanged: This listener is called when the user is signed in or
  *    out, and that is where we update the UI.
  */
function initAuth() {
  // Listening for auth state changes.
  firebase.auth().onAuthStateChanged(function(user) {
    if (user) {
      // User is signed in.
      var displayName = user.displayName;
      var email = user.email;
      var emailVerified = user.emailVerified;
      var photoURL = user.photoURL;
      var isAnonymous = user.isAnonymous;
      var uid = user.uid;
      var providerData = user.providerData;
      userAuth = true;
      document.getElementById('sign-in').textContent = 'Sign out';
      setPreference(email);
      setPreferenceForm(email);
    } else {
      // User is signed out.
      document.getElementById('sign-in').textContent = 'Sign in with Google';
    }
    document.getElementById('sign-in').disabled = false;
  });

  document.getElementById('sign-in').addEventListener('click', toggleSignIn, false);
}

/**
  * Function called when clicking the Login/Logout button.
  */
function toggleSignIn() {
  if (!firebase.auth().currentUser) {
    var provider = new firebase.auth.GoogleAuthProvider();
    // If we want access to contacts provider.addScope('https://www.googleapis.com/auth/contacts.readonly');
    firebase.auth().signInWithPopup(provider).then(function(result) {
      // This gives you a Google Access Token. You can use it to access the Google API.
      var token = result.credential.accessToken;
      // The signed-in user info.
      var user = result.user;
    }).catch(function(error) {
      // Handle Errors here.
      var errorCode = error.code;
      var errorMessage = error.message;
      // The email of the user's account used.
      var email = error.email;
      // The firebase.auth.AuthCredential type that was used.
      var credential = error.credential;
      if (errorCode === 'auth/account-exists-with-different-credential') {
        alert('You have already signed up with a different auth provider for that email.');
        // If you are using multiple auth providers on your app you should handle linking
        // the user's accounts here.
      } else {
        console.error(error);
      }
    });
  } else {
    firebase.auth().signOut();
  }
  document.getElementById('sign-in').disabled = true;
}

// Your web app's Firebase configuration
var firebaseConfig = {
  apiKey: "AIzaSyCoAlulWspRbOMxarMrvvh_GSnJk1fFodY",
  authDomain: "com-step-2020-capstone-exam-app.firebaseapp.com",
  databaseURL: "https://com-step-2020-capstone-exam-app.firebaseio.com",
  projectId: "google.com:step-2020-capstone-exam-app",
  storageBucket: "undefined",
  messagingSenderId: "428615328750",
  appId: "1:428615328750:web:995a6d8555c6b5e965a4ff",
  measurementId: "G-MCR77CT04G"
};