<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
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
        <p><a  class="active" href="/dashboardServlet">Dashboard</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
     <section class="form">
       <h3> Grade an Exam </h3>
       <form id="getExam" action="/markExam" method="POST">
         <div class="formElement">
           <h4> Select which test you want to grade</h4>
           <select name="testName">
             <#list tests as key, value>
               <option class="exam">${value}</option>
             </#list>
            </select>
         </div>
         <div class="formElement">
           <h4> Select which student you want to grade</h4>
           <select name="studentName">
             <#list students as student>
               <option class="students">${student}</option>
             </#list>
           </select>
         </div>
         <button>Submit</button>
       </form>
     </section>
    </main>
    <footer>
    </footer>
  </body>
</html>