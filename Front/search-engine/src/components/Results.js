import React, { useState, useEffect } from "react";
import axios from "axios";
import { FaArrowLeft, FaArrowRight } from 'react-icons/fa'; // Import arrow icons

const Results = () => {
  const [resultData, setResultData] = useState([]); // Set initial value to an empty array
  const [currentPage, setCurrentPage] = useState(1);
  const linksPerPage = 3; // Number of links per page
  const linkBlockHeight = 100; // Height of each link block in pixels
  const containerHeight = 500; // Height of the container div in pixels

  // useEffect hook to fetch data when the component mounts
useEffect(() => {
    fetchData();
}, []);

// Function to fetch data using Axios
const fetchData = async () => {
    try {
        const response = await axios.get("http://localhost:8080/searchh");
        console.log(response.data);
        setResultData(response.data);
    } catch (error) {
        console.error("Error fetching data:", error);
    }
};



  /// Calculate the total number of pages based on the total number of links and links per page
const totalLinks = resultData.length;
const totalPages = Math.ceil(totalLinks / linksPerPage);

// Calculate the starting index of the links for the current page
const startIndex = (currentPage - 1) * linksPerPage;

// Calculate the ending index of the links for the current page
let endIndex = startIndex + linksPerPage;
if (endIndex > totalLinks) {
  endIndex = totalLinks; // Ensure endIndex doesn't exceed the total number of links
}

// Slice the resultData array to get the links for the current page
const linksForCurrentPage = resultData.slice(startIndex, endIndex);

  const handleClickPage = (page) => {
    setCurrentPage(page);
  };

  return (
    <div className="containerr">
      {/* Display links for the current page */}
      {linksForCurrentPage.map((link, index) => (
        <div key={index} className="blockk">
          <div className="url-content">
            <h3>{link.title}</h3>
            <a href={link.url}>{link.url}</a>
            <p className="description">{truncateParagraph(link.paragraph)}</p>
          </div>
        </div>
      ))}

      {/* Generate page number buttons */}
{/* Generate page navigation buttons */}
<div className='Arrows'>
<button
  onClick={() => handleClickPage(currentPage - 1)}
  disabled={currentPage === 1} // Disable the button if on the first page
>
<div style={{ textAlign: 'center' }}>
      <FaArrowLeft style={{ fontSize: '20px', color: 'black' }} />
    </div>
</button>
<button
  onClick={() => handleClickPage(currentPage + 1)}
  disabled={currentPage === totalPages} // Disable the button if on the last page
>
<div style={{ textAlign: 'center' }}>
      <FaArrowRight style={{ fontSize: '20px', color: 'black' }} />
    </div>
</button>
</div>
    </div>
  );
};
const truncateParagraph = (paragraph) => {
  const words = paragraph.split(/\s+/);
  const truncatedWords = words.slice(0, 50);
  return truncatedWords.join(" ") + (words.length > 50 ? "..." : "");
};
export default Results;
