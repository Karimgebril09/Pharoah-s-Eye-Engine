import express from 'express';
import dotenv from 'dotenv';
import mongoose from 'mongoose';
import cors from "cors"
dotenv.config();
const app = express();
app.use(express.json())
app.use(cors())
const PORT = process.env.PORT || 8080;
//mongoose.connect(process.env.DB_URI)
//.then(() =>
console.log('Database is connected')
//).catch(err => {
//if (err) return console.error(err);
//});

app.use(express.urlencoded({ extended: true }));

app.get('/', (req, res) => {
res.send('Hello World!');
});
app.listen(PORT, err => {
if (err) return console.error(err);
console.log(`Server started listening at port ${PORT}`);
});