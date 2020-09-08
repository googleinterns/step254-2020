<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Domine:wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
    <script src="https://www.gstatic.com/charts/loader.js"></script>
    <script src="script.js"></script>
    <style>
      main {
        padding: 20px;
      }
    </style>
  </head>
  <body>
    <header>
      <div class="navtop">
        <p><a  href="index.html">Homepage</a></p>
        <p><a  class="active" href="/dashboardServlet">Dashboard</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
      <section id="examReview">
        <h3> Review: ${exam} </h3>
         <#if responses??>
          <#list responses as response>
            <div class="reviewElement">
              <output class="questionOutput">Question:</output><br>
              <output class="questionValue"> ${response.questionValue}</output><br>
            </div>
            <div class="reviewElement">
              <output class="answerOutput">Answer:</output><br>
              <output class="answerValue">${response.answer}</output><br>
            </div>
            <div class="reviewElement">
              <output>Marks:</output><br>
              <output> ${response.givenMarks}/${response.possibleMarks}</output><br>
            </div>
            <hr>
          </#list>
         </#if>
         <div id="results-chart">
         </div>
      </section>
    </main>
    <footer>
    </footer>
  </body>
</html>