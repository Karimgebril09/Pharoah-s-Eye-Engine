import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useHistory } from 'react-router-dom';
import videoSource from './vid4.mp4'; // Importing the video file

const LoadingPage = () => {
  const [loading, setLoading] = useState(true);
  const history = useHistory();

  const fetchData2 = async () => {
    try {
      await axios.get("http://localhost:8080/search");
    } catch (error) {
      console.error("Error executing fetch2:", error);
      throw error;
    }
  };

  useEffect(() => {
    const fetchData2AndNavigate = async () => {
      try {
        await fetchData2(); // Call fetchData2
        // Set loading to false after fetchData2 completes
        setLoading(false); 
      } catch (error) {
        console.error("Error:", error);
      }
    };

    fetchData2AndNavigate(); // Call the fetchData2AndNavigate function
  }, []); // No dependencies, so this effect runs only once when component mounts

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      history.push('/results'); // Navigate to another page after 7000 milliseconds
    }, 10000);

    // Clean up the timeout when component unmounts or when the timeout changes
    return () => clearTimeout(timeoutId);
  }, [history]); // Include history in the dependency array to avoid eslint warning

  return (
    <div className="loading">
      {loading ? (
        <div className="center-body">
          <div className="loader-circle-9">
            Loading
            <span></span>
          </div>
        </div>
      ) :null}
    </div>
);
};

export default LoadingPage;
