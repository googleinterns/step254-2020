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
/**
 * Get current user prefernces to set as default value in preference form.
 */
async function setPreferenceForm() {
  try {
    const response = await fetch('/auth');
    const userDetails = await response.json();
    //const userName = userDetails.name;
    const userFont = userDetails.font;
    const userFontSize = userDetails.font_size;
    const userFontColor = userDetails.text_color;
    const userBackgroundColor = userDetails.bg_color;

    //setValue('name', userName);
    setValue('font', userFont);
    setValue('font_size', userFontSize);
    setValue('text_color', userFontColor);
    setValue('bg_color', userBackgroundColor);
  } catch (e) {
    console.log('Error: ', e.message);
  }
}

/**
 * Set current user prefernces as default value in preference form
 * @param {string} id The id of the element being changed.
 * @param {string} val The value the element is being changed to.
 */
function setValue(id, val) {
  if (val == undefined) {
    document.getElementById(id).value = document.getElementById(id).value;
  } else {
    document.getElementById(id).value = val;
  }
}

// Call Functions
setPreferenceForm();

// Export modules for testing
if (typeof exports !== 'undefined') {
  module.exports = {
    setPreferenceForm,
    setValue,
  };
}
