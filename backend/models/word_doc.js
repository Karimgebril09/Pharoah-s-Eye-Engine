import { Schema, model } from 'mongoose';

const Word_Document = new Schema({
    Docid: {
        type: Schema.Types.ObjectId,
    },
    tf: {
        type: Number,
    },
    Positions: {
        type: Array,
    }
});

const Word_Documentt = model('Word_Document', Word_Document,'Word_Document');

export { Word_Documentt };
