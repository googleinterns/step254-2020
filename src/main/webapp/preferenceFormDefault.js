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

//Get current user prefernces to set as default value in preference form
function setPreferenceForm() {
  fetch("/auth")
    .then((response) => response.json())
    .then((authenticated) => {
      userFont = authenticated.font;
      userFontSize = authenticated.font_size;
      userFontColor = authenticated.text_color;
      userBackgroundColor = authenticated.bg_color;

      setValue("font", userFont);
      setValue("font_size", userFontSize);
      setValue("text_color", userFontColor);
      setValue("bg_color", userBackgroundColor);
    });
}

//Set current user prefernces as default value in preference form
function setValue(id, val) {
  document.getElementById(id).value = val;
}

//Call Functions
setPreferenceForm();
