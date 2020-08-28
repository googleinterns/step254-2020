
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
        <p><a  href="dashboard.html">Dashboard</a></p>
        <p id=logInOut></p>
      </div>
    </header>
    <main>
      <h3> Create New Question</h3>
      <form id="createQuestion" action="/createQuestion" method="POST">
        <label for="question">Enter Question:</label><br>
        <textarea name="question" rows="4" cols="50" maxlength="250" required></textarea><br>
        <label for="marks">Marks given for Question:</label><br>
        <input type="number" id="marks" name="marks" min="0" max="1000" step="0.01" required> 
        <h3>Please mark if this Question is an MCQ</h3>
        <input type="checkbox" id="mcqCheckBox" name="type" value="MCQ" onclick="getMcqChecked()">MCQ<br>
        <div style="display:none;" id="fieldsList" name ="fieldsList">
        </div>
        <span id="popup" style="display: none;">You have reached the limit of MCQ options</span>
        <button type="button" onclick="moreFields()"  id="addFields" style="display: none;" > Add Input Field </button>
        <button type="button" onclick="lessFields()"  id="removeFields" style="display: none;" > Remove Input Field </button>
        <h3 id="mcq" style="display: none;">Select which of the answers is the correct answer<h3>
        <select name ="mcqAnswer"  id="mcqAnswer" style="display: none;">
        </select/
        <h3> Select which test you want the questions added to</h1>
        <select name="testName">
          <#list tests as key, value>
            <option>${value}</option>
          </#list>
        </select>
        <button >Submit</button>
      </form>
      <h3> Add Previously Used Questions</h3>
      <form id="addQuestions" action="/returnQuestionsUserOwns" method="GET">
        <input type="submit" value="Click"> 
      </form>
    </main>
    <footer>
    </footer>
  </body>
</html>
