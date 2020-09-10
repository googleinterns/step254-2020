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

/** Import google charts */
google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawPersonalResults);
google.charts.setOnLoadCallback(drawStudentResults);

/** Draw the users result as  pie chart */
function drawPersonalResults() {
  fetch('/charts').then((response) => response.json())
      .then((results) => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Given Marks');
        data.addColumn('number', 'Possible Marks');
        Object.keys(results).forEach((mark) => {
          data.addRow([mark, results[mark]]);
        });
        const resultChart = {
          title: 'Your Results',
          width: 500,
          height: 400,
          colors: ['#d6add9', '#eda274'],
          is3D: true,
        };
        const chart = new google.visualization.PieChart(
            document.getElementById('results-chart'));
        chart.draw(data, resultChart);
      });
};

/** Draw the student result as  col chart */
function drawStudentResults() {
  // Gets exam data
  fetch('/charts').then((response) => response.json())
      .then((results) => {
        // Adds data from servlet to the chart
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Mark');
        data.addColumn('number', 'Frequency');
        data.addColumn({type: 'string', role: 'style'});

        const style = 'stroke-color: #6A706E; stroke-width:' +
              '4; fill-color: #82968C';
        Object.keys(results).forEach((finalMark) => {
          data.addRow([finalMark, results[finalMark], style]);
        });

        // Adds basic styling to the chart
        const options = {
          title: 'Student Exam Results',
          titleTextStyle: {
            fontSize: 20,
            bold: true,
          },
          width: 600,
          height: 500,
          hAxis: {
            title: 'Marks recieved by Student',
            titleTextStyle: {
              fontSize: 17,
              italic: true,
            },
          },
          vAxis: {
            title: 'No. of Students',
            titleTextStyle: {
              fontSize: 17,
              italic: true,
            },
          },
          legend: 'none',
        };

        /* creates chart instance and populates it with data and 
        adds the styling*/
        const chart = new google.visualization.ColumnChart(
            document.getElementById('chart-container'));
        chart.draw(data, options);
      });
}
