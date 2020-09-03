<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Create Test</title>
    <link href="https://fonts.googleapis.com/css2?family=Domine:wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
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
        <p><a  href="dashboardServlet">Dashboard</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
      <section class="form">
        <h2>Create Exam</h2>
        <form id="makeExam" action="/createExam" method="POST">
          <label for="name">Enter Exam Name:</label><br>
          <input type="text" id="name" name="name" required><br>  
          <label for="duration">Enter Duration:</label><br>
          <input type="number" id="duration" name="duration" required><br>
          <select name="groupID">
          <#if groups??>
            <#list groups as key, value>
              <option>${value?c}</option>
            </#list>
          </#if>
          </select>
          <input type="submit" value="Submit">
        </form>
      </section>
    </main>
    <footer>
    </footer>
  </body>
</html>
