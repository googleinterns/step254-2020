const script = require('../script.js');
let {userAuth, authenticate, pageAccess} = require('../script.js');
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

test('check page access false', () => {
  document.body.innerHTML= setAccessHtml;
  userAuth=false;
  expect(userAuth).toBe(false);
  pageAccess();
  expect( document.getElementById('accessDenied').innerHTML)
      .toBe(`<p> Cannot access until you login</p>`);
});

test('check page access true', () => {
  userAuth = true;
  expect(userAuth).toBe(true);
  pageAccess();
  expect(window.location.href).toBe('http://localhost/');
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
  expect((await global.fetch()).json()).toEqual(validUser);
  expect(userAuth).toBeTruthy();
});

test('check authentication for invalid user', async () => {
  document.body.innerHTML= setAuthHtml;
  global.fetch = jest.fn().mockImplementation(() =>
    Promise.resolve({json: () => mockInvalidUser}));
  authenticate();
  expect(global.fetch).toHaveBeenCalledWith('/auth');
  expect((await global.fetch()).json()).toEqual(invalidUser);
  expect(script.userAuth).toBeFalsy();
});
