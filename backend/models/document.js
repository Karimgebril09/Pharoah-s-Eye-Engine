import { Schema, model } from 'mongoose';

const docsschema = new Schema({
    url: {
        type: String,
    },
    popularity:{
        type: Number,
    },
    Words: {
        type: Array,
        unique: true,
    },
});

const Words = model('Words', docsschema);

export { Words };
