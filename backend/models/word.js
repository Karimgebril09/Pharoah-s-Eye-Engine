const { Schema, model }=require('mongoose');
const Wordschema = new Schema({
        word: {
        type: String,
        },
        docscount:{
        type:Number,
        },
        idf:{
        type:Number
        }
});
const Words = model('Words', Wordschema);
module.exports= { Words};