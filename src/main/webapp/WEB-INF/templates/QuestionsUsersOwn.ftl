<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Create Question</title>
    <link href="https://fonts.googleapis.com/css2?family=Domine:wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
    <script src="script.js"></script>
    <style>
    </style>
  </head>
  <body>
    <header>
      <div class="navtop">
        <p><a  href="index.html">Homepage</a></p>
        <p><a  href="/dashboardServlet">Dashboard</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
    <div class="questionsUserOwns">
    <#if questions??>
      <h1>Check the questions you would like to reuse</h1>
      <form action="/saveQuestionsFromBank" method="POST">
        <#list questions as key, v>
          <input onclick="getCheckBox()" id="checkbox" type="checkbox" name="question" value="${key}">${v} <br>
        </#list>
        <h3> Select which test you want the questions added to</h1>
        <select name="testName">
          <#if tests??>
            <#list tests as key, value>
              <option>${value}</option>
            </#list>
          </#if>
        </select>
        <button style="display: none;" id="checkBoxSubmit">Submit</button>
      </form>
      <h3>If you do not want to add any of these questions press Go Back to return to previous page </h3>
      <button onclick="location.href='/questionForm'" type="button"> Go Back </button>
    <#else>
      <h1>You have not created any questions yet! </h1>
      <button onclick="location.href='/questionForm'" type="button"> Go Back </button>
    </#if>
    </div>
    </main>
    <footer>
    </footer>
  </body>
</html>
