/* ADD-ESDK-START:
 * Add the @aws-crypto/client-node dependency.
 * You will need the encrypt stream as well as the KMS keyring.
 * Look at the ./config.js file to see how to pull the Faythe CMK.
 * Then pipe the data stream to the encrypt stream: Profit.
 */

const assert = require("assert");
const { S3, DynamoDB } = require("aws-sdk");
const uuid = require("uuid");
const s3 = new S3();
const ddb = new DynamoDB.DocumentClient();

const config = require("./config");
const {
  partition_key,
  sort_key,
  ctx_prefix,
  object_target
} = config.document_bucket.document_table;
const TableName = config.state.tableName();
const Bucket = config.state.bucketName();

const contextPrefix = ctx_prefix.toUpperCase();

module.exports = store;
module.exports.canonicalContextKey = canonicalContextKey;

async function store(fileStream, encryptionContext = {}) {
  /* The encryption context can not step on the Dynamo DB indexing structure. */
  assert(
    !encryptionContext[partition_key] && !encryptionContext[sort_key],
    `Can't use DB key names ${partition_key} or ${sort_key} as Encryption Context keys!`
  );

  const Key = uuid.v4();

  const PointerItem = ddbItem(Key, object_target, encryptionContext);
  const ContextItems = Object.keys(encryptionContext)
    .map(canonicalContextKey)
    /* TODO: Explorations
     * This can create hot keys when searching for a given ec key...
     * Better to write shard this out `${hash(Key)}#${contextKey}`
     * Where `hash(Key)` will return 0-n where n is the number of shards
     */
    .map(canonicalKey => ddbItem(canonicalKey, Key));

  const Body = fileStream;

  const file = await s3
    .upload({ Bucket, Key, Body, Metadata: encryptionContext })
    .promise();

  /* TODO: Explorations
   * Any amount of parallel writes will push the write limit of DDB.
   * This will be specific to your application.
   * How are failures handled?
   * How many retries are required?
   * This can be mitigated by offloading this process to a Lambda that fires on S3 puts.
   * This Lambda can throttle the DDB writes,
   * but will effect the eventual consistency of the system.
   */
  for (Item of [PointerItem, ...ContextItems]) {
    await ddb.put({ Item, TableName }).promise();
  }

  return file;
}

function canonicalContextKey(contextKey) {
  return `${contextPrefix}${contextKey}`;
}

function ddbItem(partitionValue, sortValue, op = {}) {
  return { ...op, [partition_key]: partitionValue, [sort_key]: sortValue };
}
