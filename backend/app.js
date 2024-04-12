import express from 'express';
import dotenv from 'dotenv';
import mongoose from 'mongoose';
import cors from 'cors';
//import { Words } from './models/word.js';
//import { Word_Documentt } from './models/word_doc.js';
//import { document } from './models/document.js';
import { Query } from './models/query.js';

//dotenv.config();
const app = express();
app.use(express.json());
app.use(cors());

//const PORT = process.env.PORT || 8080;

console.log('Before connecting to the database...');
mongoose.connect(``)
    .then(() => {
        console.log('Database is connected');
        console.log('After connecting to the database...');
    })
    .catch(err => console.error('Database connection error:', err));


app.use(express.urlencoded({ extended: true }));

app.get('/', (req, res) => {
    res.send('Hello World!');
});

/*app.get('/wordsdoc', (req, res) => {
    // Find all documents in the Word_Document collection
    Word_DocumentModel.find({})
        .then(documents => {
            // Send the retrieved documents as a JSON response
            res.json(documents);
        })
        .catch(error => {
            // Handle error
            console.error('Error finding documents:', error);
            res.status(500).json({ error: 'Internal server error' });
        });
});*/

/*
app.post('/documents', async (req, res) => {
    try {
        const { url, popularity, words } = req.body; // Extract data from request body

        // Create a new document instance
        const newDocument = new document({ url, popularity, words });

        // Save the new document to the database
        const savedDocument = await newDocument.save();

        // Send a success response
        res.status(201).json(savedDocument);
    } catch (error) {
        // Handle errors
        console.error('Error creating document:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});*/
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
app.listen(8080, err => {
    if (err) {
        console.error(err);
        return;
    }
    console.log(`Server started listening at port ${8080}`);
});
