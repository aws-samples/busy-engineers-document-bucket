using System.Text;
using Amazon.DynamoDBv2;
using Amazon.KeyManagementService;
using Amazon.S3;
using AWS.EncryptionSDK.Core;

namespace DocumentBucket
{

    public class App
    {
        private static volatile bool _continue = true;

        public static async Task Main()
        {
            string tableName = Config.TableName;
            string bucketName = Config.BucketName;
            string region = Config.Region;

            AmazonDynamoDBConfig amazonDynamoDBConfig = new();
            amazonDynamoDBConfig.RegionEndpoint = Amazon.RegionEndpoint.GetBySystemName(region);
            AmazonDynamoDBClient amazonDynamoDBClient = new(amazonDynamoDBConfig);

            AmazonS3Config amazonS3Config = new();
            amazonS3Config.RegionEndpoint = Amazon.RegionEndpoint.GetBySystemName(region);
            AmazonS3Client amazonS3Client = new(amazonS3Config);

            var materialProviders = AwsCryptographicMaterialProvidersFactory.CreateDefaultAwsCryptographicMaterialProviders();
            var keyring = materialProviders.CreateAwsKmsMultiKeyring(new CreateAwsKmsMultiKeyringInput
            {
                Generator = Config.FaytheKmsKeyId,
                KmsKeyIds = new List<string>() {Config.WalterKmsKeyId}
            });

            Api api = new(amazonDynamoDBClient, tableName, amazonS3Client, bucketName, keyring);

            Console.CancelKeyPress += new ConsoleCancelEventHandler(CancelHandler);
            while (_continue)
            {
                Console.WriteLine("Enter a number to chose a menu option:");
                Console.WriteLine("1. List");
                Console.WriteLine("2. Store");
                Console.WriteLine("3. Retrieve");
                try
                {
                    switch (Console.ReadLine())
                    {
                        case "1":
                            var items = await api.List();
                            await ListToConsole(api, items);
                            break;
                        case "2":
                            Console.WriteLine("Enter data to store:");
                            var data = Console.ReadLine();
                            if (data is null || data == "")
                            {
                                Console.WriteLine("You must enter a valid input!");
                                break;
                            }
                            Console.WriteLine($"Storing: {data}");
                            var context = ReadEncryptionContext();
                            if (context.Count > 0)
                            {
                                Console.WriteLine("Adding Encryption Context:");
                                Console.WriteLine(string.Join(Environment.NewLine, context));
                            }
                            await api.Store(Encoding.UTF8.GetBytes(data), context);
                            Console.WriteLine("Data stored successfully!");
                            break;
                        case "3":
                            Console.WriteLine("Enter key of item to retrieve:");
                            var key = Console.ReadLine();
                            if (key is null || key == "")
                            {
                                Console.WriteLine("You must enter a valid input!");
                                break;
                            }
                            Console.WriteLine($"Retrieving: {key}");
                            var documentBundle = await api.Retrieve(key);
                            Console.WriteLine("Contents: " + documentBundle.ToString());
                            break;
                        default:
                            Console.WriteLine("You must enter a valid number!");
                            break;
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Unexpected exception caught! Exiting...");
                    Console.WriteLine(ex);
                    _continue = false;
                }
            }
        }

        private static async Task ListToConsole(Api api, HashSet<PointerItem> items)
        {
            foreach (PointerItem item in items)
            {
                var document = await api.Retrieve(item.PartitionKey.S);
                Console.WriteLine(document.ToString());
            }
            Console.WriteLine("All done.");
        }

        private static void CancelHandler(object? sender, ConsoleCancelEventArgs? e)
        {
            Console.WriteLine("Exiting...");
            _continue = false;
        }

        private static Dictionary<string, string> ReadEncryptionContext()
        {
            Dictionary<string, string> context = new();
            while (true)
            {
                Console.WriteLine("Add Encryption Context? y/n?");
                var data = Console.ReadLine();
                if (data is null || data == "")
                {
                    Console.WriteLine("You must enter a valid input!");
                    break;
                }
                if (data.ToLower() != "y")
                {
                    break;
                }

                Console.WriteLine("Enter encryption context key: ");
                var key = Console.ReadLine();
                if (key is null || key == "" || !IsValidASCII(key))
                {
                    Console.WriteLine("You must enter valid ASCII text!");
                    break;
                }
                Console.WriteLine("Enter encryption context value: ");
                var ecValue = Console.ReadLine();
                if (ecValue is null || ecValue == "" || !IsValidASCII(ecValue))
                {
                    Console.WriteLine("You must enter valid ASCII text!");
                    break;
                }
                context.Add(key, ecValue);
            }
            return context;
        }

        private static bool IsValidASCII(string data)
        {
            return Encoding.ASCII.GetString(Encoding.ASCII.GetBytes(data)) == data;
        }
    }
}
