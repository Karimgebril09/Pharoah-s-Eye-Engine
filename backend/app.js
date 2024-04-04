import express from 'express';
import dotenv from 'dotenv';
import mongoose from 'mongoose';
import cors from 'cors';
import { Words } from './models/word.js';
import { WordDocs } from './models/word_doc.js';
import { document } from './models/document.js';

dotenv.config();
const app = express();
app.use(express.json());
app.use(cors());

const PORT = process.env.PORT || 8080;

mongoose.connect(process.env.connection_string)
    .then(() => console.log('Database is connected'))
    .catch(err => console.error(err));

app.use(express.urlencoded({ extended: true }));

app.get('/', (req, res) => {
    res.send('Hello World!');
});

app.get('/words', (req, res) => {
    Words.find({})
        .then(words => {
            console.log('Words:', words);
            res.json(words); // Send the retrieved words as JSON response
        })
        .catch(err => {
            console.error('Error finding documents:', err);
            res.status(500).json({ error: 'Internal server error' }); // Handle errors gracefully
        });
});
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
});
app.listen(PORT, err => {
    if (err) {
        console.error(err);
        return;
    }
    console.log(`Server started listening at port ${PORT}`);
});
