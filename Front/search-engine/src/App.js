// App.js
import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import './App.css';
import Result from './components/Result'; // Import the Result component

function App() {
  // Sample data for testing
  const resultData = {
    url: 'https://example.com',
    description: 'A sample website',
  };

  return (
    <Router>
     
    <div className="App">
      <header className="App-header">
        {/* Render the Result component for testing */}
        <div className="content">
          <Switch>
            <Route exact path="/"><Result /></Route>            
          </Switch>
        </div>
        
      </header>
    </div>
   </Router>
  );
}

export default App;
