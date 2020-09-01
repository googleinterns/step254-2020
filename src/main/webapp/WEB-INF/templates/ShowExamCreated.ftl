<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Create Question</title>
    <link href="https://fonts.googleapis.com/css2?family=Domine:wght@400;700&family=Open+Sans:ital,wght@0,400;0,600;0,700;1,400;1,600;1,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="style.css">
    <script src="script.js"></script>
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
          <h1 class="title"> Exam: ${name} , Duration: ${duration}</h3>
          </#list>
          <#if question??>
            <#list question as qs, mark>
              <tr>
              <td> ${qs} (${mark})</td>
              <#if MCQ??>
                <#list MCQ as key, answers>
                   <#if key == qs>
                     <#list answers as answer>
                      <td> - ${answer}</td>
                      </#list>
                    </#if>
                  
                </#list>
              </#if> 
              </tr>
              <br>
            </#list>
          <#else>
          <h2> You did not create any questions for this exam <h3>
          </#if>

        <#else>
          <h2> Seems like this exam no longer exists </h2>
        </#if>
      </div>
      
    </main>
    <footer>
    </footer>
  </body>
</html>
