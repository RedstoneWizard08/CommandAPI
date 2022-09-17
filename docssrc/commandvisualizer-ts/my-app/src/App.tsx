import React from 'react';
import logo from './logo.svg';
import './App.css';

function App() {
  return (
    <div className="App">
      <div className="bg">
        <div id="suggestions-box"></div>
        <div id="error-box"></div>
        <div id="valid-box"></div>
        <div id="chatbox">
          <span>
            <span contentEditable="true" spellCheck="false" id="cmd-input">/</span>
            <span id="cmd-input-autocomplete"></span>
          </span>
        </div>
      </div>
      <textarea id="commands"></textarea>
      <button id="register-commands-button">Press to register commands</button>
    </div>
  );
}

export default App;
