const { decryptStream, KmsKeyringNode } = require('@aws-crypto/client-node')
const { S3 } = require('aws-sdk')
const s3  = new S3()
const config = require('./config')
const Bucket = config.state.bucketName()

const faytheCMK = config.state.getFaytheCMK()
const walterCMK = config.state.getWalterCMK()

const decryptKeyring = new KmsKeyringNode({ keyIds: [ faytheCMK, walterCMK ] })

module.exports = retrieve
module.exports.verifyFn = verifyFn

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  const verify = verifyFn(expectedContext, expectedContextKeys)
  return s3.getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring))
    .once('MessageHeader', function(header) {
      if (!verify(header)) {
        this.emit('error', new Error('Encryption context does not match'))
      }
    })
}

function verifyFn(expectedContext = {}, expectedContextKeys = []) {
  const pairs = Object.entries(expectedContext)
  const keys = expectedContextKeys.slice()

  return function verify({ encryptionContext }) {
    return pairs.every(([key, value]) => encryptionContext[key] === value) &&
      keys.every(key => encryptionContext.hasOwnProperty(key))
  }
}
