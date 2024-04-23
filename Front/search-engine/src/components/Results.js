import React from "react";
import { useState } from "react";
import Url from "./Url";


const Results=({resultData})=>{    

    const urlsPerPage = 8;
    const totalPages = Math.ceil(resultData.length / urlsPerPage);

    console.log(resultData);
 
    const [currentPage, setCurrentPage] = useState(1);

    const handleClickPage = (page) => {
        setCurrentPage(page);
    };

    const handleClickPrevious = () => {
        setCurrentPage(prevPage => Math.max(prevPage - 1, 1));
    };

    const handleClickNext = () => {
        setCurrentPage(prevPage => Math.min(prevPage + 1, totalPages));
    };

    const startIndex = (currentPage - 1) * urlsPerPage;
    const endIndex = Math.min(startIndex + urlsPerPage, resultData.length);

    const urlsForCurrentPage = resultData.slice(startIndex, endIndex);

    return( 
    <div>
        {/* Display urls for the current page */}
        {                    
            urlsForCurrentPage.map((url,index)=>{
            return ( 
                <div key={index}>
                    <Url url={url.url} description={url.description}/>
                </div> )
        })}

        <button onClick={handleClickPrevious} disabled={currentPage === 1}>&#x2B9C;</button>

        {/* Generate page number buttons */}
        {Array.from({ length: totalPages }, (_, i) => (
            <button 
                key={i + 1} 
                onClick={() => handleClickPage(i + 1)}
                style={{ fontWeight: currentPage === i + 1 ? 'bold' : 'normal' }}
            >
                {i + 1}
            </button>
        ))}

        <button onClick={handleClickNext} disabled={currentPage === totalPages}>&#x2B9E;</button>

    </div>);
};

export default Results;