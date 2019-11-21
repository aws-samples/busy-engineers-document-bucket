const { decryptStream, KmsKeyringNode } = require("@aws-crypto/client-node");
const { S3 } = require("aws-sdk");
const s3 = new S3();
const config = require("./config");
const Bucket = config.state.bucketName();

/* MULTI-CMK-START:
 * Add the WalterCMK to the KmsKeyringNode as an additional CMK for encryption.
 * Use the `keyIds` property.
 * And look in the ./config.js file to see how to pull the Walter CMK.
 */
const faytheCMK = config.state.getFaytheCMK();
const walterCMK = config.state.getWalterCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK, walterCMK] });

module.exports = retrieve;

function retrieve(Key, { expectedContext, expectedContextKeys } = {}) {
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));
}
