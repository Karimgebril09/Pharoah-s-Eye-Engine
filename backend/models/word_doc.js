import { Schema, model } from 'mongoose';

const WordDocsSchema = new Schema({
    wordid: {
        type: Schema.Types.ObjectId,
    },
    docid: {
        type: Schema.Types.ObjectId,
    },
    tf: {
        type: Number,
    },
    positions: {
        type: Array,
    }
});

const WordDocs = model('Worddoc', WordDocsSchema);

export { WordDocs };
