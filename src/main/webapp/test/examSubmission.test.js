const script = require('../examSubmission.js');
let {setExamSubmitting, setDirty, isUnsubmitted} =
    require('../examSubmission.js');

test('check set exam submitting', () => {
  expect(setExamSubmitting()).toBe(true);
});

test('check set dirty', () => {
  expect(setDirty()).toBe(true);
});

test('isUnsubmitted when exam has not been submitted but filled in', () =>{
  const reply = 'It looks like you have not submitted your exam';
  expect(isUnsubmitted('event', false, true)).toBe(reply);
})

test('isUnsubmitted when exam has been submitted and filled in', () =>{
  expect(isUnsubmitted('event',true, true )).toBe(undefined);
})

test('isUnsubmitted when exam has not been submitted and not filled in', () =>{
  expect(isUnsubmitted('event', false, false)).toBe(undefined);
})

test('isUnsubmitted when exam has been submitted and not filled in', () =>{
  expect(isUnsubmitted('event', true, false)).toBe(undefined);
})

