using Tomlyn;
using Tomlyn.Model;

namespace DocumentBucket
{
    public static class Config
    {

        // Config file config
        private static readonly TomlTable s_documentBucketConfigModel = Toml.ToModel(File.ReadAllText(@"../../config.toml"));
        private static readonly TomlTable s_documentBucket = (TomlTable)s_documentBucketConfigModel["document_bucket"];
        private static readonly TomlTable s_documentTable = (TomlTable)s_documentBucket["document_table"];

        private static readonly string s_objectTarget = (string)s_documentTable["object_target"];
        private static readonly string s_region = (string)s_documentBucket["region"];
        private static readonly string s_partitionKey = (string)s_documentTable["partition_key"];
        private static readonly string s_sortKey = (string)s_documentTable["sort_key"];

        public static string Region => s_region;
        public static string ObjectTarget => s_objectTarget;
        public static string PartitionKeyName => s_partitionKey;
        public static string SortKeyName => s_sortKey;

        // State file config
        private static string s_homePath = Environment.GetEnvironmentVariable("HOME");
        private static string s_stateTomlString = File.ReadAllText(Path.Combine(s_homePath, @".busy_engineers_state.toml"));
        private static TomlTable s_stateModel = Toml.ToModel(s_stateTomlString);
        private static TomlTable s_stateTable = ((TomlTable)s_stateModel["state"])!;

        private static readonly string s_tableName = (string)s_stateTable["DocumentTable"];
        private static readonly string s_bucketName = (string)s_stateTable["DocumentBucket"];
        private static readonly string s_faytheKmsKeyId = (string)s_stateTable["FaytheKmsKey"];
        // MULTI-KMS-KEY-START: Configure Walter
        private static readonly string s_walterKmsKeyId = (string)s_stateTable["WalterKmsKey"];

        public static string TableName => s_tableName;
        public static string BucketName => s_bucketName;
        public static string FaytheKmsKeyId => s_faytheKmsKeyId;
        // MULTI-KMS-KEY-START: Configure Walter
        public static string WalterKmsKeyId => s_walterKmsKeyId;
    }
}

