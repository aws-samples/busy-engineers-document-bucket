using System.Text;
using Amazon.DynamoDBv2;
using Amazon.DynamoDBv2.Model;

namespace DocumentBucket
{
    public class DocumentBundle
    {
        private readonly PointerItem pointer;
        private readonly byte[] data;

        public PointerItem Pointer => pointer;
        public byte[] Data => data;

        DocumentBundle(byte[] data, PointerItem pointer)
        {
            this.data = data;
            this.pointer = pointer;
        }

        public static DocumentBundle FromData(byte[] data)
        {
            return FromDataAndContext(data, new Dictionary<string, string>());
        }

        public static DocumentBundle FromDataAndPointer(byte[] data, PointerItem pointer)
        {
            return new DocumentBundle(data, pointer);
        }

        public static DocumentBundle FromDataAndContext(byte[] data, Dictionary<string, string> context)
        {
            return new DocumentBundle(data, PointerItem.Generate(context));
        }

        public override string ToString()
        {
            return $"{pointer}: {Encoding.ASCII.GetString(data)}";
        }
    }

    public class GuidKey
    {
        private Guid _key;

        public GuidKey()
        {
            _key = Guid.NewGuid();
        }

        public GuidKey(string guid)
        {
            _key = Guid.Parse(guid);
        }

        public override string ToString()
        {
            return _key.ToString();
        }
    }

    public class BaseItem
    {
        protected readonly static string PartitionKeyName = Config.PartitionKeyName;
        protected readonly static string SortKeyName = Config.SortKeyName;

        private readonly AttributeValue _partitionKey;
        private readonly AttributeValue _sortKey;

        public AttributeValue PartitionKey => _partitionKey;
        public AttributeValue SortKey => _sortKey;

        public BaseItem(string partitionKey, string sortKey)
        {
            _partitionKey = new AttributeValue(partitionKey);
            _sortKey = new AttributeValue(sortKey);
        }

        public virtual Dictionary<string, AttributeValue> ToItem()
        {
            return new Dictionary<string, AttributeValue>
            {
                { PartitionKeyName, _partitionKey },
                { SortKeyName, SortKey }
            };
        }

        public override bool Equals(object? obj)
        {
            var otherItem = obj as BaseItem;
            if (otherItem is null)
            {
                return false;
            }
            return _partitionKey == otherItem._partitionKey && SortKey == otherItem.SortKey;
        }

        public override int GetHashCode()
        {
            return HashCode.Combine(_partitionKey, SortKey);
        }

        public override string ToString()
        {
            return $"PartitionKey: {_partitionKey.S}, SortKey: {_sortKey.S}";
        }
    }

    public class ContextItem : BaseItem
    {
        static private readonly string _prefix = Config.ObjectTarget;

        public ContextItem(string contextKey, GuidKey objectTarget) : base(contextKey, objectTarget.ToString()) { }

        public static string Canonicalize(string key)
        {
            if (!key.StartsWith(_prefix))
            {
                return key;
            }
            return _prefix + key;
        }

        public static QueryRequest QueryFor(string contextKey)
        {
            Condition keyIsContextKey = new();
            List<AttributeValue> attributeValues = new(1)
            {
                new AttributeValue(Canonicalize(contextKey))
            };
            keyIsContextKey.AttributeValueList = attributeValues;
            keyIsContextKey.ComparisonOperator = ComparisonOperator.EQ;

            QueryRequest query = new();
            query.KeyConditions.Add(PartitionKeyName, keyIsContextKey);

            return query;
        }

        public static ContextItem FromContext(string key, string objectTarget)
        {
            GuidKey target = new(objectTarget);
            return new ContextItem(Canonicalize(key), target);
        }

        public static ContextItem FromContext(string key, GuidKey objectTarget)
        {
            return new ContextItem(Canonicalize(key), objectTarget);
        }

        public static ContextItem FromContext(string key, AttributeValue objectTarget)
        {
            return FromContext(key, objectTarget.S);
        }

        public static ContextItem FromItem(Dictionary<string, AttributeValue> item)
        {
            var contextKey = item[PartitionKeyName].S;
            var objectTarget = item[SortKeyName].S;
            return FromContext(contextKey, objectTarget);
        }
    }

    public class PointerItem : BaseItem
    {
        private static readonly string s_target = Config.ObjectTarget;
        private static readonly AttributeValue s_attrTarget = new(s_target);

        private readonly Dictionary<string, AttributeValue> _context;

        public PointerItem(GuidKey pointerTarget, Dictionary<string, AttributeValue> context) : base(pointerTarget.ToString(), s_target)
        {
            if (context.ContainsKey(PartitionKeyName) ||
                context.ContainsKey(SortKeyName))
            {
                throw new Exception($"Can't create encryption context with reserved keys: ({PartitionKeyName}, {SortKeyName})");
            }
            _context = context;
        }

        public static Dictionary<string, Condition> FilterFor()
        {
            Dictionary<string, Condition> result = new();
            Condition condition = new();
            List<AttributeValue> attributeValues = new(1);

            attributeValues.Add(s_attrTarget);
            condition.AttributeValueList = attributeValues;
            condition.ComparisonOperator = ComparisonOperator.EQ;
            result.Add(SortKeyName, condition);
            return result;
        }

        public static PointerItem Generate()
        {
            return Generate(new Dictionary<string, string>());
        }

        public static PointerItem Generate(Dictionary<string, string> context)
        {
            return FromKeyAndContext(new GuidKey().ToString(), context);
        }

        public static PointerItem FromKeyAndContext(string key, Dictionary<string, string> context)
        {
            Dictionary<string, AttributeValue> attributeContext = new(context.Count);

            foreach (KeyValuePair<string, string> entry in context)
            {
                attributeContext.Add(entry.Key, new AttributeValue(entry.Value));
            }

            return new PointerItem(new GuidKey(key), attributeContext);
        }

        public Dictionary<string, string> GetContext()
        {
            Dictionary<string, string> result = new(_context.Count);

            foreach (KeyValuePair<string, AttributeValue> keyValuePair in _context)
            {
                result.Add(keyValuePair.Key, keyValuePair.Value.S);
            }

            return result;
        }

        public override Dictionary<string, AttributeValue> ToItem()
        {
            var result = base.ToItem();
            foreach (KeyValuePair<string, AttributeValue> keyValuePair in _context)
            {
                result.Add(keyValuePair.Key, keyValuePair.Value);
            }
            return result;
        }

        public static Dictionary<string, AttributeValue> AtKey(string key)
        {
            // Check that key is a valid Guid
            _ = new GuidKey(key);
            return AtKey(new AttributeValue(key));
        }

        public static Dictionary<string, AttributeValue> AtKey(AttributeValue key)
        {
            Dictionary<string, AttributeValue> result = new(2)
            {
                { PartitionKeyName, key },
                { SortKeyName, new AttributeValue(s_target) }
            };
            return result;
        }

        public static PointerItem FromItem(Dictionary<string, AttributeValue> item)
        {
            GuidKey partitionKey = new(item[PartitionKeyName].S);
            item.Remove(PartitionKeyName);
            var sortKey = item[SortKeyName].S;
            item.Remove(SortKeyName);
            if (sortKey != s_target)
            {
                throw new Exception(message: $"Unexpected sortKey value: {sortKey} for PointerItem!");
            }
            return new PointerItem(partitionKey, item);
        }

        public HashSet<ContextItem> ContextItems()
        {
            HashSet<ContextItem> contextItems = new(_context.Count);
            foreach (KeyValuePair<string, AttributeValue> keyValuePair in _context)
            {
                contextItems.Add(ContextItem.FromContext(keyValuePair.Key, PartitionKey));
            }
            return contextItems;
        }
    }
}

