// ADD-ESDK-START: Add the ESDK Dependency
using Amazon.DynamoDBv2;
using Amazon.DynamoDBv2.Model;
using Amazon.KeyManagementService;
using Amazon.S3;
using Amazon.S3.Model;

namespace DocumentBucket
{
    public class Api
    {
        // ADD-ESDK-START: Add the ESDK Dependency
        private readonly AmazonDynamoDBClient amazonDynamoDBClient;
        private readonly string tableName;
        private readonly AmazonS3Client amazonS3Client;
        private readonly string bucketName;

        // ADD-ESDK-START: Add the ESDK Dependency
        public Api(AmazonDynamoDBClient amazonDynamoDBClient, string tableName, AmazonS3Client amazonS3Client, string bucketName)
        {
            this.amazonDynamoDBClient = amazonDynamoDBClient;
            this.tableName = tableName;
            this.amazonS3Client = amazonS3Client;
            this.bucketName = bucketName;

            // ADD-ESDK-START: Add the ESDK Dependency
        }

        protected async Task<Dictionary<string, AttributeValue>> WriteItem<T>(T modeledItem) where T : BaseItem
        {
            Dictionary<string, AttributeValue> ddbItem = modeledItem.ToItem();
            await amazonDynamoDBClient.PutItemAsync(tableName, ddbItem);
            return ddbItem;
        }

        protected async Task<PointerItem> GetPointerItem(string key)
        {
            GetItemResponse response = await amazonDynamoDBClient.GetItemAsync(tableName, PointerItem.AtKey(key));
            PointerItem pointerItem = PointerItem.FromItem(response.Item);
            return pointerItem;
        }

        protected async Task<PointerItem> GetPointerItem(ContextItem contextItem)
        {
            return await GetPointerItem(contextItem.SortKey.S);
        }

        protected async Task<HashSet<PointerItem>> QueryForContextKey(string contextKey)
        {
            QueryRequest request = ContextItem.QueryFor(contextKey);
            request.TableName = tableName;
            QueryResponse response = await amazonDynamoDBClient.QueryAsync(request);
            HashSet<ContextItem> contextItems = new(response.Items.Select(i => ContextItem.FromItem(i)));
            var pointerItems = await Task.WhenAll(contextItems.Select(async i => await GetPointerItem(i)));
            return new HashSet<PointerItem>(pointerItems);
        }

        protected async Task WriteObject(DocumentBundle bundle)
        {
            byte[] data = bundle.Data;
            //metadata
            var request = new PutObjectRequest
            {
                BucketName = bucketName,
                Key = bundle.Pointer.PartitionKey.S,
            };
            foreach (KeyValuePair<string, string> kvp in bundle.Pointer.GetContext())
            {
                request.Metadata.Add(kvp.Key, kvp.Value);
            }
            request.Headers.ContentLength = data.Length;
            request.InputStream = new MemoryStream(data);
            await amazonS3Client.PutObjectAsync(request);
        }

        protected async Task<byte[]> GetObjectData(string key)
        {
            using (GetObjectResponse response = await amazonS3Client.GetObjectAsync(bucketName, key))
            {

                using (var memoryStream = new MemoryStream())
                {
                    response.ResponseStream.CopyTo(memoryStream);
                    return memoryStream.ToArray();
                }
            }
        }

        public async Task<HashSet<PointerItem>> List()
        {
            ScanResponse response = await amazonDynamoDBClient.ScanAsync(tableName, PointerItem.FilterFor());
            return new HashSet<PointerItem>(response.Items.Select(i => PointerItem.FromItem(i)));
        }

        public async Task<PointerItem> Store(byte[] data)
        {
            return await Store(data, new());
        }

        public async Task<PointerItem> Store(byte[] data, Dictionary<string, string> context)
        {
            // ADD-ESDK-START: Add Encryption to store
            DocumentBundle bundle = DocumentBundle.FromDataAndContext(data, context);
            await WriteItem(bundle.Pointer);
            await WriteObject(bundle);
            return bundle.Pointer;
        }

        public async Task<DocumentBundle> Retrieve(string key)
        {
            return await Retrieve(key, new(), new());
        }

        public async Task<DocumentBundle> Retrieve(string key, HashSet<string> expectedContextKeys)
        {
            return await Retrieve(key, expectedContextKeys, new());
        }

        public async Task<DocumentBundle> Retrieve(string key, Dictionary<string, string> expectedContext)
        {
            return await Retrieve(key, new(), expectedContext);
        }

        public async Task<DocumentBundle> Retrieve(string key, HashSet<string> expectedContextKeys, Dictionary<string, string> expectedContext)
        {
            byte[] data = await GetObjectData(key);
            // ADD-ESDK-START: Add Decryption to retrieve
            PointerItem pointer = await GetPointerItem(key);
            // ADD-ESDK-START: Add Decryption to retrieve
            return DocumentBundle.FromDataAndPointer(data, pointer);
        }

        public async Task<HashSet<PointerItem>> SearchByContextKey(string contextKey)
        {
            return await QueryForContextKey(contextKey);
        }

    }
}

