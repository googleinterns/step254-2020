const script = require('../script.js');
let {userAuth, authenticate, pageAccess, newUser,
  isChecked} =
    require('../script.js');

const mockPreference = {
  font: 'arial',
  bg_color: 'white',
  font_size: '16',
  text_color: 'black',
};

const mockValidUser = {
  name: 'username',
  email: 'username@google.com',
};

const mockInvalidUser = {
  name: 'username',
};

const setAccessHtml = '<p id="accessDenied"></p>';
const setAuthHtml = '<p id="logInOut"></p>';
const setCheckedHtml =
  '<input type=\"checkbox\" name=\"question\" ' +
  'value=\"Question one\" checked={true} class=\"checkbox\"> <br>' +
  '<input type=\"checkbox\" name=\"question\" ' +
  'value=\"Question two\" class=\"checkbox\"> <br>' +
  '<input type=\"submit\" id=\"checkBoxSubmit\" value=\"Go\">';
const setNotCheckedHtml =
  '<input type=\"checkbox\" name=\"question\" ' +
  'value=\"Question one\" class=\"checkbox\"><br>' +
  '<input type=\"checkbox\" name=\"question\" ' +
  'value=\"Question two\" class=\"checkbox\"><br>' +
  '<input type="submit" id="checkBoxSubmit" value="Go">';

test('check page access false', () => {
  document.body.innerHTML= setAccessHtml;
  userAuth=false;
  pageAccess();
  expect(userAuth).toBeFalsy();
  expect( document.getElementById('accessDenied').innerHTML)
      .toBe(`<p> Cannot access until you login</p>`);
});

test('check page access true', () => {
  userAuth = true;
  pageAccess();
  expect(userAuth).toBeTruthy();
  expect(window.location.href).toBe('http://localhost/');
});

test('check old user', () => {
  const response = newUser('name');
  expect(response).toBe('dashboard.html');
});

test('check null user name', () => {
  name = null;
  const response = newUser(name);
  expect(response).toBe('userSetUp.html');
});

test('check undefined user name', () => {
  name = undefined;
  const response = newUser(name);
  expect(response).toBe('userSetUp.html');
});

test('check prefereneces can be set', async () => {
  global.fetch = jest.fn().mockImplementation(() =>
    Promise.resolve({json: () => mockPreference}));
  script.setPreference();
  expect(global.fetch).toHaveBeenCalledWith('/auth');
  expect((await global.fetch()).json()).toEqual(mockPreference);
});

test('check authentication for valid user', async () => {
  document.body.innerHTML= setAuthHtml;
  global.fetch = jest.fn().mockImplementation(() =>
    Promise.resolve({json: () => mockValidUser}));
  authenticate();
  expect(global.fetch).toHaveBeenCalledWith('/auth');
  expect((await global.fetch()).json()).toEqual(mockValidUser);
  expect(userAuth).toBeTruthy();
});

test('check authentication for invalid user', async () => {
  document.body.innerHTML= setAuthHtml;
  global.fetch = jest.fn().mockImplementation(() =>
    Promise.resolve({json: () => mockInvalidUser}));
  authenticate();
  expect(global.fetch).toHaveBeenCalledWith('/auth');
  expect((await global.fetch()).json()).toEqual(mockInvalidUser);
  expect(script.userAuth).toBeFalsy();
});

test('check checkBox when box is not checked', () => {
  document.body.innerHTML= setNotCheckedHtml;
  checkboxList = document.querySelectorAll('input[name="question"]');
  submitButton = document.getElementById('checkBoxSubmit');
  expect(isChecked(checkboxList, submitButton)).toBeFalsy();
  expect(submitButton.style.display).toBe('none');
});

test('check checkBox when box is checked', () => {
  document.body.innerHTML = setCheckedHtml;
  checkboxList = document.querySelectorAll('input[name="question"]');
  submitButton = document.getElementById('checkBoxSubmit');
  expect(isChecked(checkboxList, submitButton)).toBeTruthy();
  expect(submitButton.style.display).toBe('block');
});
