const bleno = require('bleno');
const BlenoPrimaryService = bleno.PrimaryService;
const readline = require('readline');

const name = 'mosip try 1';
const serviceUuids = ['0000AB29-0000-1000-8000-00805f9b34fb']

bleno.on('stateChange', function(state) {
    console.log('on -> stateChange: ' + state);

    if (state === 'poweredOn') {
        bleno.startAdvertising(name, serviceUuids);
    } else {
        bleno.stopAdvertising();
    }
});
let _updateValueCallback = null

bleno.on('advertisingStart', function(error) {
    console.log('on -> advertisingStart: ' + (error ? 'error ' + error : 'success'));

    if (!error) {
        bleno.setServices([
            new BlenoPrimaryService({
                uuid: '0000AB29-0000-1000-8000-00805f9b34fb',
                characteristics: [readCharacteristic, writeCharacteristic]
            })
        ]);
    }

});

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false
});

rl.on('line', (line) => {
    _updateValueCallback(Buffer.from(line))
});

const readCharacteristic = new bleno.Characteristic({
    uuid: '00002032-0000-1000-8000-00805f9b34fb', // or 'fff1' for 16-bit
    properties: ['read', 'indicate'], // can be a combination of 'read', 'write', 'writeWithoutResponse', 'notify', 'indicate'
    secure: [], // enable security for properties, can be a combination of 'read', 'write', 'writeWithoutResponse', 'notify', 'indicate'
    value: "", // optional static value, must be of type Buffer - for read only characteristics
    descriptors: [],
    onReadRequest: function(offset, callback) {
        if(!this.textInUpperCase) {
            this.textInUpperCase = "DEFAULT VALUE"
        }

        callback(this.RESULT_SUCCESS, Buffer.from(this.textInUpperCase));
    },
    onWriteRequest: function(data, offset, withoutResponse, callback) {
        console.log("Received write request with data: " + data);
        this.textInUpperCase = data.toString().toUpperCase();

        callback(this.RESULT_SUCCESS);
    }, // optional write request handler, function(data, offset, withoutResponse, callback) { ...}
    onSubscribe: function(maxValueSize, updateValueCallback) {
        console.log('EchoCharacteristic - onSubscribe');
        updateValueCallback(Buffer.from("hello"))

        _updateValueCallback = updateValueCallback;
    },
    onUnsubscribe: null, // optional notify/indicate unsubscribe handler, function() { ...}
    onNotify: null, // optional notify sent handler, function() { ...}
    onIndicate: null // optional indicate confirmation received handler, function() { ...}
});


const writeCharacteristic = new bleno.Characteristic({
    uuid: '00002031-0000-1000-8000-00805f9b34fb', // or 'fff1' for 16-bit
    properties: ['writeWithoutResponse', 'write'], // can be a combination of 'read', 'write', 'writeWithoutResponse', 'notify', 'indicate'
    secure: [], // enable security for properties, can be a combination of 'read', 'write', 'writeWithoutResponse', 'notify', 'indicate'
    value: "", // optional static value, must be of type Buffer - for read only characteristics
    descriptors: [],
    onReadRequest: function(offset, callback) {
        if(!this.textInUpperCase) {
            this.textInUpperCase = "DEFAULT VALUE"
        }

        callback(this.RESULT_SUCCESS, Buffer.from(this.textInUpperCase));
    },
    onWriteRequest: function(data, offset, withoutResponse, callback) {
        console.log("Received write request with data: " + data);
        this.textInUpperCase = data.toString().toUpperCase();

        callback(this.RESULT_SUCCESS);
    }, // optional write request handler, function(data, offset, withoutResponse, callback) { ...}
    onUnsubscribe: null, // optional notify/indicate unsubscribe handler, function() { ...}
    onNotify: null, // optional notify sent handler, function() { ...}
    onIndicate: null // optional indicate confirmation received handler, function() { ...}
});
