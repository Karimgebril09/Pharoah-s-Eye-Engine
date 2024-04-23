// App.js
import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import './App.css';
import Result from './components/Result'; // Import the Result component
import Results from './components/Results';
import resultData from "./components/Urls.json"

function App() {

  return (
    <Router>
     
    <div className="App">
      <header className="App-header">
        {/* Render the Result component for testing */}
        <div className="content">
          <Switch>
            <Route exact path="/"><Result /></Route>
            <Route exact path="/results"> <Results resultData={resultData} /> </Route>                        
          </Switch>
        </div>
        
      </header>
    </div>
   </Router>
  );
  
}

export default App;
