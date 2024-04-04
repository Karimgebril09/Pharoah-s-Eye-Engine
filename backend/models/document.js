import { Schema, model } from 'mongoose';

import { Schema, model } from 'mongoose';

const docsschema = new Schema({
    url: {
    url: {
        type: String,
    },
    popularity: {
        type: Number,
    },
    Words: {
        type: Array,
        unique: true,
    },
    },
});

const document = model('document', docsschema);
//
export {document };
