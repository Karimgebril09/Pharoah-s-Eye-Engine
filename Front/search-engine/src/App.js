// App.js
import React from 'react';
import './App.css';
import Result from './components/Result'; // Import the Result component

function App() {
  // Sample data for testing
  const resultData = {
    url: 'https://example.com',
    description: 'A sample website',
  };

  return (
    <div className="App">
      <header className="App-header">
        {/* Render the Result component for testing */}
        <Result {...resultData} />
      </header>
    </div>
  );
}

export default App;
