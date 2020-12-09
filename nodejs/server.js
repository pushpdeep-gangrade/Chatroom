const express = require('express');
const http = require('http');
const port = process.env.PORT || 8080;
const app = express();
const cors = require('cors');

const axios = require('axios').default;
const { v4: uuidv4 } = require('uuid');

var subscriptionKey = "Translator Key Here";
var endpoint = "https://api.cognitive.microsofttranslator.com";

// Add your location, also known as region. The default is global.
// This is required if using a Cognitive Services resource.
var location = "eastus";

app.use(cors());
var bodyParser = require('body-parser');
const { log } = require('console');
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({
    extended: true
}));
app.use(express.json());

//Status encoded
const OK = 200;
const BAD_REQUEST = 400;
const UNAUTHORIZED = 401;
const CONFLICT = 403;
const NOT_FOUND = 404;
const INTERNAL_SERVER_ERROR = 500;

//API CODE HERE
// admin login without password encyption check
app.post('/translate/textToText', function (req, res) {
    if (typeof req.body.from === "undefined" || typeof req.body.to === "undefined" ||
        typeof req.body.message === "undefined") {
        res.status(BAD_REQUEST).send("Bad request Check request Body");
    } else {
        if (req.body.from == "") {
            axios({
                baseURL: endpoint,
                url: '/translate',
                method: 'post',
                headers: {
                    'Ocp-Apim-Subscription-Key': subscriptionKey,
                    'Ocp-Apim-Subscription-Region': location,
                    'Content-type': 'application/json',
                    'X-ClientTraceId': uuidv4().toString()
                },
                params: {
                    'api-version': '3.0',
                    'to': req.body.to
                },
                data: [{
                    'text': req.body.message
                }],
                responseType: 'json'
            }).then(function(response){
                //console.log(JSON.stringify(response.data, null, 4));
                res.status(OK).send(response.data[0]);
            })
        }
        else {
            axios({
                baseURL: endpoint,
                url: '/translate',
                method: 'post',
                headers: {
                    'Ocp-Apim-Subscription-Key': subscriptionKey,
                    'Ocp-Apim-Subscription-Region': location,
                    'Content-type': 'application/json',
                    'X-ClientTraceId': uuidv4().toString()
                },
                params: {
                    'api-version': '3.0',
                    'from': req.body.from,
                    'to': req.body.to
                },
                data: [{
                    'text': req.body.message
                }],
                responseType: 'json'
            }).then(function(response){
                //console.log(JSON.stringify(response.data, null, 4));
                res.status(OK).send(response.data[0]);
            })
        }
    }
});

//Listener Setup
app.listen(port, (req, res) => {
    console.log("listening..." + port);
  });


//EXAMPLE AZURE CALL BELOW

// axios({
//     baseURL: endpoint,
//     url: '/translate',
//     method: 'post',
//     headers: {
//         'Ocp-Apim-Subscription-Key': subscriptionKey,
//         'Ocp-Apim-Subscription-Region': location,
//         'Content-type': 'application/json',
//         'X-ClientTraceId': uuidv4().toString()
//     },
//     params: {
//         'api-version': '3.0',
//         'from': 'en',
//         'to': ['de', 'it']
//     },
//     data: [{
//         'text': 'Hello World!'
//     }],
//     responseType: 'json'
// }).then(function(response){
//     console.log(JSON.stringify(response.data, null, 4));
// })
