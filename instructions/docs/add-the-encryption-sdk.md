# Exercise 1: Add the AWS Encryption SDK

In this section, you will add client-side encryption using the AWS Encryption SDK and KMS to the Busy Engineer's Document Bucket.

## Background

The Busy Engineer's Document Bucket is an example application meant to show some high-level examples of real world AWS patterns. This includes how to integrate client-side encryption using AWS KMS and the AWS Encryption SDK in application code.

Right now, the Document Bucket supports storing files (or documents, or other blobs of data) in a private S3 bucket, and indexing them in DynamoDB. This allows Document Bucket users to share files with other users, or store them for retrieval later. The DynamoDB entries provide fast lookups to the content of the bucket, along with metadata context for each bucket item.

This context allows storing additional information about the S3 item. Perhaps the origin user, the destination fleet, the project, or any other tag that would be useful to know without downloading and examining the item.

DynamoDB is also configured to allow indexing on which documents have which keys. So, for example, it's a quick query to find out which documents in the bucket have been tagged with "configuration" as a piece of metadata about the object contents.

Here's the API the Document Bucket supports:

* `list`: This operation queries DynamoDB for all entries for all items in the bucket, and their metadata. It returns the `set` of items that have been stored.
* `store`: This operation accepts a blob of bytes and a `map` of metadata context. It generates a unique identifier for the document. The identifier and associated metadata are written to DynamoDB. The bytes of the data are written to S3 under a key of that unique identifier. Any context metadata keys in DynamoDB are updated to include that new object identifier.
* `retrieve`: This operation accepts a unique identifier as an argument. First it looks that identifier up in DynamoDB and pulls the identifier and its context out. Then it retrieves that object from S3. It bundles these items together and returns them to the caller.
* `search`: This operation accepts a metadata key to search. It then queries DynamoDB for the `set` of documents in the Document Bucket that have context matching that key. This operation then returns that set of document identifiers and their metadata.
    * Once the desired document or documents have been identified with the returned metadata, the document identifiers can be passed to `retrieve` to actually fetch the documents.

This is a start for sharing, storing, and searching documents of a variety of types. But what about sensitive documents? Or protecting, say, important configuration files from accidental corruption during storing or retrieving?

Now you will add the AWS Encryption SDK to encrypt close to where the data originates: client-side encrypting the Document Bucket document before it is transmitted off of the host machine to the internet. You will use KMS to provide a `data key` for each document, using a CMK that you set up in [Getting Started](./getting-started.md) when you deployed your stacks.

## Make the Change

### Starting Directory

Make sure you are in the `exercises` directory for the language of your choice:

```bash tab="Java"
cd ~/environment/workshop/exercises/java
```

```bash tab="Typescript Node.JS"
cd ~/environment/workshop/exercises/node-typescript
```

```bash tab="JavaScript Node.JS"
cd ~/environment/workshop/exercises/node-javascript
```

```bash tab="Python"
cd ~/environment/workshop/exercises/python
```

`cd` into the `add-esdk-start` directory.

### Step 1: Add the ESDK Dependency

Look for `ADD-ESDK-START` comments to help orient yourself in the code.

Start by adding the Encryption SDK dependency to the code.

```java tab="Java" hl_lines="5 6 7 8 9 14 15 25 30 31 36 39"
// Edit ./src/main/java/sfw/example/esdkworkshop/Api.java
package sfw.example.esdkworkshop;

// ADD-ESDK-START: Add the ESDK Dependency
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.MasterKey;
import com.amazonaws.encryptionsdk.MasterKeyProvider;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;

...

// ADD-ESDK-START: Add the ESDK Dependency
private final AwsCrypto awsEncryptionSdk;
private final MasterKeyProvider mkp;

...

public Api(
    AmazonDynamoDB ddbClient,
    String tableName,
    AmazonS3 s3Client,
    String bucketName,
    // ADD-ESDK-START: Add the ESDK Dependency
    MasterKeyProvider<? extends MasterKey> mkp) {
  this.ddbClient = ddbClient;
  this.tableName = tableName;
  this.s3Client = s3Client
  // ADD-ESDK-START: Add the ESDK Dependency
  this.awsEncryptionSdk = new AwsCrypto();
  this.mkp = mkp;
}

// Save and close.
// Edit ./src/main/java/sfw/example/esdkworkshop/Api.java
package sfw.example.esdkworkshop;

// ADD-ESDK-START: Add the ESDK Dependency
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
// Save and close.
```

```typescript tab="Typescript Node.JS" hl_lines="4 11"
// Edit ./store.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
import { encryptStream, KmsKeyringNode } from "@aws-crypto/client-node";

// Save and exit

// Edit ./retrieve.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
import { decryptStream, KmsKeyringNode } from "@aws-crypto/client-node";

// Save and exit
```

```javascript tab="JavaScript Node.JS" hl_lines="4 11"
// Edit ./store.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
const { encryptStream, KmsKeyringNode } = require("@aws-crypto/client-node");

// Save and exit

// Edit ./retrieve.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
const { decryptStream, KmsKeyringNode } = require("@aws-crypto/client-node");

// Save and exit
```

```python tab="Python" hl_lines="4 10 11 15 19"
# Edit src/document_bucket/__init__.py

# ADD-ESDK-START: Add the ESDK Dependency
import aws_encryption_sdk

# Save and exit
# Edit src/document_bucket/api.py

# ADD-ESDK-START: Add the ESDK Dependency
import aws_encryption_sdk
from aws_encryption_sdk import KMSMasterKeyProvider

# Add a Master Key Provider to your __init__
# ADD-ESDK-START: Add the ESDK Dependency
def __init__(self, bucket, table, master_key_provider: KMSMasterKeyProvider):
    self.bucket = bucket
    self.table = table
    # ADD-ESDK-START: Add the ESDK Dependency
    self.master_key_provider = master_key_provider

# Save and exit
```

#### What Just Happened

You just imported a dependency on the AWS Encryption SDK library in your code.

You also changed the API to expect that a Keyring or Master Key Provider will be passed to your code to use in `store` and `retrieve` operations.

### Step 2: Add Encryption to `store`

Now that you have the AWS Encryption SDK imported, start encrypting your data before storing it.

```java tab="Java" hl_lines="3 4 5"
// Edit ./src/main/java/sfw/example/esdkworkshop/Api.java
public PointerItem store(byte[] data, Map<String, String> context) {
    // ADD-ESDK-START: Add Encryption to store
    CryptoResult<byte[], KmsMasterKey> encryptedMessage = awsEncryptionSdk.encryptData(mkp, data);
    DocumentBundle bundle =
        DocumentBundle.fromDataAndContext(encryptedMessage.getResult(), context);
    writeItem(bundle.getPointer());
    ...
```

```typescript tab="Typescript Node.JS" hl_lines="4"
// Edit ./store.js

// ADD-ESDK-START: Add Encryption to store
const Body = fileStream.pipe(encryptStream(encryptKeyring));

// Save and exit

```

```javascript tab="JavaScript Node.JS" hl_lines="4"
// Edit ./store.js

// ADD-ESDK-START: Add Encryption to store
const Body = fileStream.pipe(encryptStream(encryptKeyring));

// Save and exit

```

```python tab="Python" hl_lines="5 6 7 8 10"
# Edit src/document_bucket/api.py
# Find the store function and edit it to add the Master Key Provider
# and to write the encrypted data
    # ADD-ESDK-START: Add Encryption to store
    encrypted_data, header = aws_encryption_sdk.encrypt(
        source=data,
        key_provider=self.master_key_provider,
    )
    ...
    self._write_object(encrypted_data, item)
```

#### What Just Happened

The application will now encrypt data client-side with the AWS Encryption SDK and KMS before storing it.

Now, before storing data in the Document Bucket, it uses the AWS Encryption SDK to:

1. Request a new data key using your keyring or Master Key Provider
1. Encrypt that data for you
1. Return the encrypted data in the AWS Encryption SDK message format
1. Extract the ciphertext to pass to the AWS S3 SDK to store in S3

### Step 3: Add Decryption to `retrieve`

Now that the application will encrypt data before storing it, it will need to decrypt the data before returning it to the caller. At least for the data to be useful, anyway.

```java tab="Java" hl_lines="5 7"
// Edit ./src/main/java/sfw/example/esdkworkshop/Api.java
// Find retrieve(...)
    byte[] data = getObjectData(key);
    // ADD-ESDK-START: Add Decryption to retrieve
    CryptoResult<byte[], KmsMasterKey> decryptedMessage = awsEncryptionSdk.decryptData(mkp, data);
    ...
    return DocumentBundle.fromDataAndPointer(decryptedMessage.getResult(), pointer);
```


```typescript tab="Typescript Node.JS" hl_lines="7"
// Edit retrieve.js

  // ADD-ESDK-START: Add Decryption to retrieve
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));

// Save and Exit
```

```javascript tab="JavaScript Node.JS" hl_lines="7"
// Edit retrieve.js

  // ADD-ESDK-START: Add Decryption to retrieve
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));

// Save and Exit
```

```python tab="Python" hl_lines="6 7 8 9 11"
# Edit src/document_bucket/api.py
# Find the retrieve function and edit it to add a call to decrypt the
# encrypted data before returning it
        item = self._get_pointer_item(PointerQuery.from_key(pointer_key))
        # ADD-ESDK-START: Add Decryption to retrieve
        encrypted_data = self._get_object(item)
        plaintext, header = aws_encryption_sdk.decrypt(
            source=encrypted_data, key_provider=self.master_key_provider
        )
        return DocumentBundle.from_data_and_context(
            plaintext, item.context
        )

# Save and exit
```

#### What Just Happened

The application now decrypts data client-side as well.

The data returned from S3 for `retrieve` is now encrypted. Before returning that data to the user, you added a call to the AWS Encryption SDK to decrypt the data. Under the hood, the Encryption SDK:

1. Read the AWS Encryption SDK formatted encrypted message
1. Called KMS to request to decrypt your message's encrypted data key using the Faythe CMK
1. Used the decrypted data key to decrypt the message
1. Returned the message plaintext and Encryption SDK headers to you


### Step 4: Configure the Faythe CMK in the Encryption SDK

Now that you have your dependencies declared and your code updated to encrypt and decrypt data, the final step is to pass through the configuration to the AWS Encryption SDK to start using your KMS CMKs to protect your data.

```java tab="Java" hl_lines="6 9 10 13"
// Edit ./src/main/java/sfw/example/esdkworkshop/Api.java
    AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    // ADD-ESDK-START: Configure the Faythe CMK in the Encryption SDK
    // Load configuration of KMS resources
    String faytheCMK = state.contents.FaytheCMK;

    // Set up the Master Key Provider to use KMS
    KmsMasterKeyProvider mkp =
        KmsMasterKeyProvider.builder().withKeysForEncryption(faytheCMK).build();

    // Construct the API
    return new Api(ddbClient, tableName, s3Client, bucketName, mkp);
```

```typescript tab="Typescript Node.JS"  hl_lines="4 5 6 7 14 15"

// Edit store.js

// ADD-ESDK-START: Set up a keyring to use Faythe's CMK for decrypting.
const faytheCMK = config.state.getFaytheCMK();
const encryptKeyring = new KmsKeyringNode({
  generatorKeyId: faytheCMK
});

// Save and exit

// Edit retrieve.js

// ADD-ESDK-START: Set up a keyring to use Faythe's CMK for decrypting.
const faytheCMK = config.state.getFaytheCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK] });

// Save and exit
```

```javascript tab="JavaScript Node.JS"  hl_lines="4 5 6 7 14 15"

// Edit store.js

// ADD-ESDK-START: Set up a keyring to use Faythe's CMK for decrypting.
const faytheCMK = config.state.getFaytheCMK();
const encryptKeyring = new KmsKeyringNode({
  generatorKeyId: faytheCMK
});

// Save and exit

// Edit retrieve.js

// ADD-ESDK-START: Set up a keyring to use Faythe's CMK for decrypting.
const faytheCMK = config.state.getFaytheCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK] });

// Save and exit
```

```python tab="Python" hl_lines="7 9 10 12"

# Edit src/document_bucket/__init__.py

...

# ADD-ESDK-START: Configure the Faythe CMK in the Encryption SDK
# Pull configuration of KMS resources
faythe_cmk = state["FaytheCMK"]
# And the Master Key Provider configuring how to use KMS
cmk = [faythe_cmk]
mkp = aws_encryption_sdk.KMSMasterKeyProvider(key_ids=cmk)

operations = DocumentBucketOperations(bucket, table, mkp)

# Save and exit
```

#### What Just Happened

In Getting Started, you launched CloudFormation stacks for CMKs. One of these CMKs was nicknamed Faythe. As part of launching these templates, the CMK's Amazon Resource Name (ARN) was written to a configuration file on disk, the `state` variable that is loaded and parsed.

Now Faythe's ARN is pulled into a variable, and used to initialize a keyring or master key provider that will use the Faythe CMK. That new keyring/master key provider is passed in to your API, and you are set to start encrypting and decrypting with KMS and the Encryption SDK.

### Checking Your Work

Want to check your progress, or compare what you've done versus a finished example?

Check out the code in one of the `-complete` folders to compare.

```bash tab="Java"
cd ~/environment/workshop/exercises/java/add-esdk-complete
```

```bash tab="Typescript Node.JS"
cd ~/environment/workshop/exercises/node-typescript/add-esdk-complete
```

```bash tab="JavaScript Node.JS"
cd ~/environment/workshop/exercises/node-javascript/add-esdk-complete
```

```bash tab="Python"
cd ~/environment/workshop/exercises/python/add-esdk-complete
```

## Try it Out

Now that the code is written, let's load it up and try it out.

If you'd like to try a finished example, use your language's `-complete` directory as described above.

Experiment using the API as much as you like. To get you started, here are some suggested things to try.

* Compare <a href="https://us-east-2.console.aws.amazon.com/cloudtrail/home?region=us-east-2#" target="_blank">CloudTrail Logs for usages of Faythe</a> when you encrypt messages of different sizes (small, medium, large).
* Take a look at the <a href="https://s3.console.aws.amazon.com/s3/home" target="_blank">contents of your S3 Document Bucket</a> to inspect the raw object.


If you want more ideas to extend, check out [Explore Further](#explore-further) below.

```java tab="Java"
// To use the API programmatically, use this target to launch jshell
mvn jshell:run
/open startup.jsh
Api documentBucket = App.initializeDocumentBucket();
documentBucket.list();
documentBucket.store("Store me in the Document Bucket!".getBytes());
for (PointerItem item : documentBucket.list()) {
    DocumentBundle document = documentBucket.retrieve(item.partitionKey());
    System.out.println(document.toString());
}
// Ctrl+D to exit jshell

// Or, to run logic that you write in App.java, use this target
mvn compile
```

```javascript tab="JavaScript Node.JS"
node
list = require("./list.js")
store = require("./store.js")
list().then(console.log)
store(fs.createReadStream("./store.js")).then(r => {
  // Just storing the s3 key
  key = r.Key
  console.log(r)
})
list().then(console.log)
retrieve(key).pipe(process.stdout)
// Ctrl-D when finished to exit the REPL
```

```bash tab="JavaScript Node.JS CLI"
./cli.js list
./cli.js store ./store.js
# Note the "Key" value
./cli.js list
# Note the "reference" value
./cli.js retrieve $KeyOrReferenceValue
```

```typescript tab="Typescript Node.JS"
node -r ts-node/register
;({list} = require("./src/list.ts"))
;({store} = require("./src/store.ts"))
list().then(console.log)
store(fs.createReadStream("./src/store.ts")).then(r => {
  // Just storing the s3 key
  key = r.Key
  console.log(r)
})
list().then(console.log)
retrieve(key).pipe(process.stdout)
// Ctrl-D when finished to exit the REPL
```

```bash tab="Typescript Node.JS CLI"
./cli.ts list
./cli.ts store ./store.js
# Note the "Key" value
./cli.ts list
# Note the "reference" value
./cli.ts retrieve $KeyOrReferenceValue
```

```python tab="Python"
tox -e repl
import document_bucket
ops = document_bucket.initialize()
ops.list()
ops.store(b'some data')
ops.list()
# Ctrl-D when finished to exit the REPL
```

## Explore Further

* **Leveraging the Message Format** - The <a href="https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/message-format.html" target="_blank">AWS Encryption SDK Message Format</a> is an open standard. Can you write something to detect whether an entry in the Document Bucket has been encrypted in this format or not, and retrieve or decrypt appropriately?
* **More Test Content** - Small test strings are enough to get started, but you might be curious to see what the behavior and performance looks like with larger documents. What if you add support for loading files to and from disk to the Document Bucket?
* **Configuration Glue** - If you are curious how the Document Bucket is configured, take a peek at `~/environment/workshop/cdk/Makefile` and the `make state` target, as well as `config.toml` in the exercises root `~/environment/workshop/exercises/config.toml`. The Busy Engineer's Document Bucket uses a base <a href="https://github.com/toml-lang/toml" target="_blank">TOML</a> file to set standard names for all CloudFormation resources and a common place to discover the real deployed set. Then it uses the AWS Cloud Development Kit (CDK) to deploy the resources and write out their identifiers to the state file. Applications use the base TOML file `config.toml` to locate the state file and pull the expected resource names. And that's how the system bootstraps all the resources it needs!

# Next exercise

Now that you are encrypting and decrypting, how about [adding Multiple CMKs](./multi-cmk.md)?
