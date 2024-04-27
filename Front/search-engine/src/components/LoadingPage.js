import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useHistory } from 'react-router-dom';

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
        fetchData2(); // Call fetchData2
        setLoading(false); // Set loading to false after fetchData2 completes

        // Set a timeout of 3 seconds before navigating to another page
        setTimeout(() => {
          history.push('/results'); // Navigate to another page
        }, 4000); // 3 seconds timeout
      } catch (error) {
        console.error("Error:", error);
      }
    };

    fetchData2AndNavigate(); // Call the fetchData2AndNavigate function
  }, [history]); // Include history in the dependency array to avoid eslint warning

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      {loading ? <p>Loading...</p> : <p>Content Loaded!</p>}
    </div>
  );
};

export default LoadingPage;
