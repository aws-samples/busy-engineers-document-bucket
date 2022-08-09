using System.Text;
using Amazon.DynamoDBv2;
using Amazon.S3;
// ADD-ESDK-START: Add the ESDK Dependency

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

            // ADD-ESDK-START: Configure the Faythe KMS Key in the Encryption SDK
            Api api = new(amazonDynamoDBClient, tableName, amazonS3Client, bucketName);

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
                            await api.Store(Encoding.ASCII.GetBytes(data));
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
    }
}
