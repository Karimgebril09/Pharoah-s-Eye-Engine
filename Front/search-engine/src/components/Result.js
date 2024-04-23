// Result.js
import React, { useState, useRef } from 'react';
import axios from 'axios';
import Autosuggest from 'react-autosuggest';
import './Result.css'; // Import your CSS file for styling

const Result = () => {
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const timeoutRef = useRef(null);

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
        <div className="suggestion" onClick={() => handleSuggestionClick(suggestion)}>
            {suggestion}
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
        } catch (error) {
            console.error('Error performing search:', error);
        }
    };

    return (
        <div className="result">
            <div className="form-container">
                <form onSubmit={handleSubmit}>
                    <Autosuggest
                        suggestions={suggestions}
                        onSuggestionsFetchRequested={() => {}}
                        onSuggestionsClearRequested={onSuggestionsClearRequested}
                        getSuggestionValue={getSuggestionValue}
                        renderSuggestion={renderSuggestion}
                        inputProps={inputProps}
                    />
                    <button type="submit" className="submit-button">Search</button>
                </form>
            </div>
        </div>
    );
};

export default Result;
