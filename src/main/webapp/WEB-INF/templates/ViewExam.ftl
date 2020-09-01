<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Domine:wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
    <script src="script.js"></script>
  </head>
  <body>
    <header>
      <div class="navtop">
        <p><a  href="index.html">Homepage</a></p>
        <p><a  class="active" href="dashboard.html">Dashboard</a></p>
        <p id=logInOut></p>
        <p><a href="/exam">Take Exam</a></p>
        <p><a href="createExam.html">Create Exam</a></p>
        <p><a href="preference.html">Set Preferences</a></p>
        <p><a href="/getExamResponses">Mark Exam</a></p>
      </div>
    </header>
    <main>
     <h3> Review: ${exam} </h3>
       <#list responses as response>
         <output>Question:</output><br>
         <output class="questionValue"> ${response.questionValue}</output><br>
         <output>Answer:</output><br>
         <output class="questionValue">${response.answer}</output><br>
         <output>Marks:</output><br>
         <output> ${response.givenMarks}/${response.possibleMarks}</output><br>
         <hr>
      </#list>
    </main>
    <footer>
    </footer>
  </body>
</html>