const { DynamoDB } = require('aws-sdk')
const ddb = new DynamoDB.DocumentClient()
const config = require('./config')
const { sort_key, object_target } = config.document_bucket.document_table
const TableName = config.state.tableName()

module.exports = list

async function list({ ExclusiveStartKey, Limit } = {}) {

  return ddb
    .scan({
      TableName,
      FilterExpression: '#RangeKey = :rkey',
      ExpressionAttributeValues: {
        ':rkey': object_target
      },
      ExpressionAttributeNames: {
        '#RangeKey': sort_key
      },
      ExclusiveStartKey,
      Limit
    })
    .promise()
}

/* TODO Explorations
 * Pagination must be implemented by the caller here.
 * But for a CLI like this,
 * a stream is a nice interface
 * that can be used to drain all the pages.
 */
