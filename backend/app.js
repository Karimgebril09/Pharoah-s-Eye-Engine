import express from 'express';
import dotenv from 'dotenv';
import mongoose from 'mongoose';
import cors from 'cors';
import { Query } from './models/query.js';
import { Results } from './models/results.js';
import child_process from 'child_process';
const javaFilePath = 'D:/Web Sessions/SearchEngine/java/Web-Crawler/src/main/java/';
dotenv.config();
const app = express();
app.use(express.json());
app.use(cors());

// Connect to MongoDB
console.log('Before connecting to the database...');
mongoose.connect(`mongodb://127.0.0.1:27017/Salma`)
    .then(() => {
        console.log('Database is connected');
        console.log('After connecting to the database...');
    })
    .catch(err => console.error('Database connection error:', err));

app.use(express.urlencoded({ extended: true }));

// Define routes
app.get('/', (req, res) => {
    res.send('Hello World!');
});

// Handle POST requests for search queries
app.post('/search', async (req, res) => {
    try {
        console.log(req.body.query);
        const { query } = req.body; // Extract the query from the request body

        // Create a new query document instance
        const newQuery = new Query({ Query: query });

        // Save the new query document to the database
        const savedQuery = await newQuery.save();

        // Send a success response with the saved query document
        res.status(201).json(savedQuery);
    } catch (error) {
        // Handle errors
        console.error('Error creating query document:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.get('/search', async (req, res) => {
    try {
      const javaProcess = child_process.spawn('java', [
        '-jar',
        './SearchEngine.jar'
      ]);
  
     // javaProcess.stdout.on('data', (data) => {
       // console.log(`Java stdout: ${data}`);
      //});
  
      javaProcess.stderr.on('data', (data) => {
        console.error(`Java stderr: ${data}`);
      });
  
      javaProcess.on('error', (err) => {
        console.error('Error executing Java file:', err);
        return res.status(500).json({ error: 'Internal server error' });
      });
  
      javaProcess.on('close', (code) => {
        console.log(`Java process exited with code: ${code}`);
        // Handle successful execution (if needed)
      });
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  });
app.get('/searchh', async (req, res) => {
    try {
        const searchResults = await Results.find({});
        res.status(200).json(searchResults);
    } catch (error) {
        console.error('Error fetching search results:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Start the server
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`Server started listening at port ${PORT}`);
});
