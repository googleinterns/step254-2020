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

let examSubmitting = false;
let isDirty = false;
/**
 * Notes if exam has been submitted
 */
function setExamSubmitting() { 
    formSubmitting = true; 
}

/**
 * Notes if exam any of the exam has been completed
 */
function setDirty () {
    isDirty = true;
}

/**
 * Checks if user has unsaved changes before leaving page
 * @return {window.event} warning if user has unsaved changes
 * @return {undefined} allows user leave page without warning
 */
window.addEventListener("beforeunload", function (e) {
  if (examSubmitting || !isDirty) {
    return undefined;
  }
  const confirmationMessage = 'It looks like you have not submitted your exam'
                        + 'If you leave before submitting, your exam will be lost.'
                        + 'You may not be able to retake this exam.';

  (e || window.event).returnValue = confirmationMessage;
  return confirmationMessage;
});

// Export modules for testing
if (typeof exports !== 'undefined') {
  module.exports = {
    setExamSubmitting,
    setDirty,
    examSubmitting,
    isDirty,
  };
};
