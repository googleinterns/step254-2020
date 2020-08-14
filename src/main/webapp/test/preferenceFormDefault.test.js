const preferenceFormDefault = require('../preferenceFormDefault.js');

const exampleResponse = {
  font: 'arial',
  bg_color: 'white',
  font_size: '16',
  text_color: 'black',
};

const setValueHtml =
    '<p id="font">Arial</p>' +
    '<p id="font_size">10</p>' +
    '<p id="text_color">Red</p>' +
    '<p id="bg_color">Green</p>';

test('check set value works with font', () => {
  document.body.innerHTML = setValueHtml;
  preferenceFormDefault.setValue('font', 'helvetica');
  expect(document.getElementById('font').value).toBe('helvetica');
});

test('check set value works with font size', () => {
  document.body.innerHTML = setValueHtml;
  preferenceFormDefault.setValue('font_size', '17');
  expect(document.getElementById('font_size').value).toBe('17');
});

test('check set value works with text color', () => {
  document.body.innerHTML = setValueHtml;
  preferenceFormDefault.setValue('text_color', 'black');
  expect(document.getElementById('text_color').value).toBe('black');
});

test('check set value works with background color', () => {
  document.body.innerHTML = setValueHtml;
  preferenceFormDefault.setValue('bg_color', 'white');
  expect(document.getElementById('bg_color').value).toBe('white');
});

test('check preference form servlet call works', async () => {
  global.fetch = jest.fn().mockImplementation(() => 
  Promise.resolve({json: () => exampleResponse}));
  preferenceFormDefault.setPreferenceForm();
  expect(global.fetch).toHaveBeenCalledWith('/auth');    
  expect((await global.fetch()).json()).toEqual(exampleResponse);
});
