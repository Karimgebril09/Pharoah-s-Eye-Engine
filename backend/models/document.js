const { Schema, model }=require('mongoose');
const docsschema = new Schema({
        url: {
        type: String,
        },
        popularity:{
        type:Number,
        },
        Words: {
        type:Array,
        unique: true,
        },
});
const Words = model('Words', docsschema);
module.exports= { Words};