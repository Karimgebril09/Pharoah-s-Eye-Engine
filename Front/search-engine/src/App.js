import React from 'react';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        {/* Your search box with placeholder and search icon */}
        <div className="search-box">
          <input
            type="text"
            placeholder="Search Here"
            className="search-input"
          />
          {/* You can add a simple search icon or customize it later */}
          <span className="search-icon">🔍</span>
        </div>
      </header>
    </div>
  );
}

export default App;
