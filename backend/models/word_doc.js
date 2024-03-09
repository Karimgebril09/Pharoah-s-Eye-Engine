const { Schema, model }=require('mongoose');
const WordDocsSchema = new Schema({
        wordid:{
            type:Schema.Types.ObjectId,
        },
        docid:{
            type:Schema.Types.ObjectId,
        },
        tf:{
            type:Number,
        },
        positions:{
            type:Array,
        }
});
const WordDocs = model('Worddoc', WordDocsSchema);
module.exports= { WordDocs};