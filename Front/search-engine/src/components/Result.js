import React from 'react';
import './Result.css'; // Create this file for styling

const Result = ({ url, description }) => {
    return (
        <div className="result">
            <div className="url">{url}</div>
            <div className="description">{description}</div>
        </div>
    );
};

export default Result;
