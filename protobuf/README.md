# im-chat protobuf
Instant Message

## How to use
- javascript
  - dependence
  ```
  npm install google-protobuf
  ```
  - compile
  ```
  protoc --js_out=import_style=commonjs,binary:../im-node/src/sdk/protobuf file.proto
  ```
  - use case
  ```
  var messages = require('./messages_pb');
  var message = new messages.MyMessage();
  message.setName("John Doe");
  message.setAge(25);
  message.setPhoneNumbers(["800-555-1212", "800-555-0000"]);
  // Serializes to a UInt8Array.
  var bytes = message.serializeBinary();
  var message2 = MyMessage.deserializeBinary(bytes);
  ```
- java
  - compile
  ```
  protoc --java_out=../im-server/src/main/java file.proto
  ```

## Reference
- [Developer Guide](https://developers.google.com/protocol-buffers/docs/overview?hl=zh-CN)
- [js](https://github.com/protocolbuffers/protobuf/blob/master/js/README.md)
