import React, { useState, useRef } from 'react';
import axios from 'axios';
import { useHistory } from 'react-router-dom'; // Import useHistory from react-router-dom
import Autosuggest from 'react-autosuggest';
import './Result.css'; // Import your CSS file for styling

const Result = () => {
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const timeoutRef = useRef(null);
    const history = useHistory(); // Get the history object from React Router

    const onChange = (event, { newValue }) => {
        clearTimeout(timeoutRef.current); // Clear previous timeout
        setQuery(newValue);
        timeoutRef.current = setTimeout(() => {
            // Fetch suggestions after a delay
            fetchSuggestions(newValue);
        }, 300); // Adjust the delay as needed
    };

    const fetchSuggestions = async (value) => {
        try {
            const response = await axios.get(`https://nominatim.openstreetmap.org/search`, {
                params: {
                    q: value,
                    format: 'json',
                    limit: 5,
                    accept_language: 'en' // Specify English language
                }
            });
            const uniqueSuggestions = filterUniqueSuggestions(response.data.map(place => place.display_name.split(',')[0]));
            setSuggestions(uniqueSuggestions);
        } catch (error) {
            console.error('Error fetching suggestions:', error);
        }
    };

    const onSuggestionsClearRequested = () => {
        setSuggestions([]);
    };

    const getSuggestionValue = suggestion => suggestion;

    const renderSuggestion = suggestion => (
        <div className='container'>
        <div className="box" onClick={() => handleSuggestionClick(suggestion)}>
            {suggestion}
        </div>
        </div>
    );

    const inputProps = {
        placeholder: 'Search for photos',
        value: query,
        onChange: onChange
    };

    const filterUniqueSuggestions = suggestions => {
        const uniqueSuggestions = [];
        const lowerCaseSuggestions = suggestions.map(suggestion => suggestion.toLowerCase());
        lowerCaseSuggestions.forEach(suggestion => {
            if (!uniqueSuggestions.some(existing => existing.toLowerCase() === suggestion)) {
                uniqueSuggestions.push(suggestion);
            }
        });
        return uniqueSuggestions;
    };

    const handleSuggestionClick = suggestion => {
        setQuery(suggestion);
        // You can add additional logic here if needed
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            console.log('Submitting query:', query);
            // Send the query to your Java backend here
            await axios.post('http://localhost:8080/search', { query });
            alert("sent successfully");
            // Navigate to the results page after submitting the query
            history.push('/Loading'); // Navigate to the '/results' route
        } catch (error) {
            console.error('Error performing search:', error);
        }
    };

    return (
        <div className="result">
        <div className="input-container">
          <form onSubmit={handleSubmit} className="form">
            <div className="input-wrapper">
              <Autosuggest
                suggestions={suggestions}
                onSuggestionsFetchRequested={() => {}}
                onSuggestionsClearRequested={onSuggestionsClearRequested}
                getSuggestionValue={getSuggestionValue}
                renderSuggestion={renderSuggestion}
                inputProps={inputProps}
              />
              <button type="submit" className="submit-button">Search</button>
            </div>
          </form>
                     
          
        </div>
        </div>
       )
};

export default Result;
