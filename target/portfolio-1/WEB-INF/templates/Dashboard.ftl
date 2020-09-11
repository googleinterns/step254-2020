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
        <p><a  class="active" href="/dashboardServlet">Dashboard</a></p>
        <p><a href="/groups">Groups</a></p>
        <p><a href="/exam">Take Exam</a></p>
        <p><a href="/createExam">Create Exam</a></p>
        <p><a href="preference.html">Set Preferences</a></p>
        <p><a href="/getExamResponses">Mark Exams</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
      <h1 class="title"><u> Welcome to your dashboard</u></h1>
      <div class="testdash">
        <div class="done">
          <h2> <u> Exam's Completed </u> </h2>
          <#if examCompleted??>
            <#list examCompleted as name, id>
              <tr>
              <td> ${name} </td>
              <td><a href=/examsTaken?examID=${id?c}>Look at Exam</a></td>
              </tr>
              <br>
            </#list>
          <#else>
            <h3>You have not completed any exams yet </h3><br>
          </#if>
        </div>
        <div class="todo">
          <h2><u> Exam's To Do</u> </h2>
          <#if examToComplete??>
            <#list examToComplete as name, id>
              <tr>
              <td> ${name} </td>
              <td><a href=/exam?examID=${id?c}>Look at Exam</a></td>
              </tr>
              <br>
            </#list>
          <#else>
            <h4> You do not have any exams to take at the moment </h4><br>
          </#if>
        </div>
        <div class="created">
          <h2> <u> Exam's Created</u> </h2>
          <#if examOwned??>
            <#list examOwned as name, id >
              <tr>
              <td> ${name} </td>
              <td><a href=/showExam?examID=${id?c}>Look at Exam</a></td>
              </tr>
              <br>
            </#list>
          <#else>
            <h4>You have not created any exams yet </h4><br>
          </#if>
        </div>
      </div>
    </main>
    <footer>
    </footer>
  </body>
</html>