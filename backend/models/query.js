
import { Schema, model } from 'mongoose';

const querySchema = new Schema({
    Query: {
        type: String,
    }
});

const Query = model('query', querySchema, 'query'); // Specify the custom collection name as the third argument

export { Query };