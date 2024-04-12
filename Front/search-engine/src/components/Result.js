import React, { useState } from 'react';
import axios from 'axios';
import './Result.css'; // Create this file for styling

const Result = () => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);

    const handleInputChange = (event) => {
        setQuery(event.target.value);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            console.log(query);
            const response = await axios.post('http://localhost:8080/search', { query }); // Adjust the URL and data as needed
            alert("Your Search Has been sent Successfully");
            setResults(response.data);
        } catch (error) {
            console.error('Error fetching search results:', error);
        }
    };

    return (
        <div className="result">
            <div className="form-container">
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    value={query}
                    onChange={handleInputChange}
                    placeholder="Search for photos"
                    className="search-input"
                />
                <button type="submit" className="submit-button">Search</button>
            </form>
        </div>
            
        </div>
    );
};

export default Result;