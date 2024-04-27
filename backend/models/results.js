
import { Schema, model } from 'mongoose';

const resultSchema = new Schema({
    url: {
        type: String,
    },
    title: {
        type: String,
    },
    paragraph: {
        type: String,
    }
});

const Results= model('result', resultSchema, 'result'); // Specify the custom collection name as the third argument

export { Results };