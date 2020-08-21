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
 * @return {Boolean} examSubmitting submission state
 */
function setExamSubmitting() {
  examSubmitting = true;
  return examSubmitting;
}

/**
 * Notes if exam any of the exam has been completed
 * @return {Boolean} isDirty form state
 */
function setDirty() {
  isDirty = true;
  return isDirty;
}

/**
 * Checks if user has unsaved changes before leaving page
 * @param {Event} e BeforeUnloadEvent
 * @param {Boolean} submitted submission state
 * @param {Boolean} dirty form state
 * @return {window.event} warning if user has unsaved changes
 * @return {undefined} allows user leave page without warning
 */
function isUnsubmitted(e, submitted, dirty) {
  if (submitted || !dirty) {
    return undefined;
  }
  const confirmationMessage = 'It looks like you have not submitted your exam';
  (e || window.event).returnValue = confirmationMessage;
  return confirmationMessage;
}

/**
 * Calls isUnsubmitted before user leaves page
 */
window.addEventListener('beforeunload', function(e) {
  isUnsubmitted(e, examSubmitting, isDirty);
});

// Export modules for testing
if (typeof exports !== 'undefined') {
  module.exports = {
    setExamSubmitting,
    setDirty,
    examSubmitting,
    isDirty,
    isUnsubmitted,
  };
};
