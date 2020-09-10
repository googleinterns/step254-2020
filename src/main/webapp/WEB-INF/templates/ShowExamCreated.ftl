<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Create Question</title>
    <link href="https://fonts.googleapis.com/css2?family=Domine:wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
    <script src="https://www.gstatic.com/charts/loader.js"></script>
    <script src="script.js"></script>
    <script src="chartMaker.js"></script>
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
        <p><a  href="/dashboardServlet">Dashboard</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
      <div>
        <#if exam??>
          <#list exam as name, duration>
            <h1 class="title"> Exam: ${name} , Duration: ${duration} minutes </h3>
          </#list>
          <#if question??>
            <h2> Questions: </h2>
            <#list question as qs, mark>
              <div class="questionsForExam">
                <tr>
                  <td> ${qs?counter}: ${qs} (${mark})</td>
                  <#if MCQ??>
                    <div class="mcqForExam">
                      <#list MCQ as key, answers>
                        <#if key == qs>
                          <#list answers as answer>
                            <td> Option ${answer?counter}: ${answer} <#sep>, </#sep></td>
                          </#list>
                        </#if>
                      </#list>
                    </div>
                  </#if> 
                </tr>
              </div>
            </#list>
          <#else>
            <h2> You did not create any questions for this exam </h2>
          </#if>
          <div id="chart-container">
          </div>
        <#else>
          <h2> Seems like this exam no longer exists </h2>
        </#if>
      </div>
    </main>
    <footer>
    </footer>
  </body>
</html>