# Exercise 1: Add the AWS Encryption SDK

In this section, you will add client-side encryption to the Busy Engineer's Document Bucket using the AWS Encryption SDK and AWS KMS.

## Background

In [Getting Started](./getting-started.md), you set up your Busy Engineer's Document Bucket environment and selected a workshop language. 

Now you will add the AWS Encryption SDK to encrypt objects on the client, before they are transmitted off of the host machine to the internet. You will use AWS KMS to provide a `data key` for each object, using a CMK that you set up in [Getting Started](./getting-started.md).

## Let's Go!

### Starting Directory

Make sure you are in the `exercises` directory for the language of your choice:

```bash tab="Java"
cd ~/environment/workshop/exercises/java/add-esdk-start
```

```bash tab="Typescript Node.JS"
cd ~/environment/workshop/exercises/node-typescript/add-esdk-start
```

```bash tab="JavaScript Node.JS"
cd ~/environment/workshop/exercises/node-javascript/add-esdk-start
```

```bash tab="Python"
cd ~/environment/workshop/exercises/python/add-esdk-start
```

### Step 1: Add the ESDK Dependency

Look for `ADD-ESDK-START` comments in the code to help orient yourself.

Start by adding the Encryption SDK dependency to the code.

```java tab="Java" hl_lines="5 6 7 8 9 15 16 26 31 32 40"
// Edit ./src/main/java/sfw/example/esdkworkshop/Api.java
package sfw.example.esdkworkshop;

// ADD-ESDK-START: Add the ESDK Dependency
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.MasterKey;
import com.amazonaws.encryptionsdk.MasterKeyProvider;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;

...
private final String tableName;
private final String bucketName;
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
// Edit ./src/main/java/sfw/example/esdkworkshop/App.java
package sfw.example.esdkworkshop;

// ADD-ESDK-START: Add the ESDK Dependency
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
// Save and close.
```

```typescript tab="Typescript Node.JS" hl_lines="4 11"
// Edit ./src/store.ts

// ADD-ESDK-START: Add the ESDK Dependency
import { KmsKeyringNode, buildClient, CommitmentPolicy } from "@aws-crypto/client-node";
const { encryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)

// Save and exit

// Edit ./src/retrieve.ts

// ADD-ESDK-START: Add the ESDK Dependency
import { KmsKeyringNode, buildClient, CommitmentPolicy } from "@aws-crypto/client-node";
const { decryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)

// Save and exit
```

```javascript tab="JavaScript Node.JS" hl_lines="4 11"
// Edit ./store.js

// ADD-ESDK-START: Add the ESDK Dependency
const { KmsKeyringNode, buildClient, CommitmentPolicy } = require("@aws-crypto/client-node");
const { encryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)

// Save and exit

// Edit ./retrieve.js

// ADD-ESDK-START: Add the ESDK Dependency
const { KmsKeyringNode, buildClient, CommitmentPolicy } = require("@aws-crypto/client-node");
const { decryptStream } = buildClient(
    CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
)

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
from aws_encryption_sdk import StrictAwsKmsMasterKeyProvider  # type: ignore
from aws_encryption_sdk.identifiers import CommitmentPolicy

# Add a Master Key Provider to your __init__
# ADD-ESDK-START: Add the ESDK Dependency
def __init__(self, bucket, table, master_key_provider: StrictAwsKmsMasterKeyProvider):
    self.bucket = bucket
    self.table = table
    # ADD-ESDK-START: Add the ESDK Dependency
    self.master_key_provider : StrictAwsKmsMasterKeyProvider = master_key_provider

# Save and exit
```

#### What Happened?

1. You added a dependency on the AWS Encryption SDK library in your code
1. You changed the API to expect that a Keyring or Master Key Provider will be passed to your code to use in `store` and `retrieve` operations

### Step 2: Add Encryption to `store`

Now that you have the AWS Encryption SDK imported, start encrypting your data before storing it.

```java tab="Java" hl_lines="4 5 6"
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
// Edit ./src/store.ts

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
    client = aws_encryption_sdk.EncryptionSDKClient(
        commitment_policy=CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
    )
    encrypted_data, header = client.encrypt(
        source=data,
        key_provider=self.master_key_provider,
    )
    ...
    self._write_object(encrypted_data, item)
```

#### What Happened?

The application will use the AWS Encryption SDK to encrypt your data client-side under a CMK before storing it by:

1. Requesting a new data key using your Keyring or Master Key Provider
1. Encrypting your data with the returned data key
1. Returning your encrypted data in the AWS Encryption SDK message format
1. Extracting the ciphertext from the AWS Encryption SDK message
1. Passing the ciphertext to the AWS S3 SDK for storage in S3

### Step 3: Add Decryption to `retrieve`

Now that the application encypts your data before storing it, it will need to decrypt your data before returning it to the caller (at least for the data to be useful, anyway).

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
// Edit ./src/retrieve.ts

  // ADD-ESDK-START: Add Decryption to retrieve
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));

// Save and Exit
```

```javascript tab="JavaScript Node.JS" hl_lines="7"
// Edit ./retrieve.js

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
        client = aws_encryption_sdk.EncryptionSDKClient(
            commitment_policy=CommitmentPolicy.REQUIRE_ENCRYPT_REQUIRE_DECRYPT
        )
        plaintext, header = client.decrypt(
            source=encrypted_data, key_provider=self.master_key_provider
        )
        return DocumentBundle.from_data_and_context(
            plaintext, item.context
        )

# Save and exit
```

#### What Happened?

The application now decrypts data client-side, as well.

The data returned from S3 for `retrieve` is encrypted. Before returning that data to the user, you added a call to the AWS Encryption SDK to decrypt the data. Under the hood, the Encryption SDK is:

1. Reading the AWS Encryption SDK formatted encrypted message
1. Calling KMS to request to decrypt your message's encrypted data key using the Faythe CMK
1. Using the decrypted data key to decrypt the message
1. Returning the message plaintext and Encryption SDK headers to you


### Step 4: Configure the Faythe CMK in the Encryption SDK

Now that you have declared your dependencies and updated your code to encrypt and decrypt data, the final step is to pass through the configuration to the AWS Encryption SDK to start using your KMS CMKs to protect your data.

```java tab="Java" hl_lines="6 9 10 12"
// Edit ./src/main/java/sfw/example/esdkworkshop/Api.java
    AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    // ADD-ESDK-START: Configure the Faythe CMK in the Encryption SDK
    // Load configuration of KMS resources
    String faytheCMK = stateConfig.contents.state.FaytheCMK;

    // Set up the Master Key Provider to use KMS
    KmsMasterKeyProvider mkp =
        KmsMasterKeyProvider.builder().withKeysForEncryption(faytheCMK).build();

    return new Api(ddbClient, tableName, s3Client, bucketName, mkp);
```

```typescript tab="Typescript Node.JS"  hl_lines="4 5 6 7 14 15"

// Edit ./src/store.ts

// ADD-ESDK-START: Configure the Faythe CMK in the Encryption SDK
const faytheCMK = config.state.getFaytheCMK();
const encryptKeyring = new KmsKeyringNode({
  generatorKeyId: faytheCMK
});

// Save and exit

// Edit ./src/retrieve.ts

// ADD-ESDK-START: Set up a keyring to use Faythe's CMK for decrypting.
const faytheCMK = config.state.getFaytheCMK();
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK] });

// Save and exit
```

```javascript tab="JavaScript Node.JS"  hl_lines="4 5 6 7 14 15"

// Edit ./store.js

// ADD-ESDK-START: Configure the Faythe CMK in the Encryption SDK
const faytheCMK = config.state.getFaytheCMK();
const encryptKeyring = new KmsKeyringNode({
  generatorKeyId: faytheCMK
});

// Save and exit

// Edit ./retrieve.js

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
mkp = aws_encryption_sdk.StrictAwsKmsMasterKeyProvider(key_ids=cmk)

operations = DocumentBucketOperations(bucket, table, mkp)

# Save and exit
```

#### What Happened?

In [Getting Started](./getting-started.md), you launched CloudFormation stacks for CMKs. One of these CMKs was nicknamed Faythe. As part of launching these templates, the CMK's Amazon Resource Name (ARN) was written to a configuration file on disk, the `state` variable that is loaded and parsed.

Now Faythe's ARN is pulled into a variable, and used to initialize a Keyring or Master Key Provider that will use the Faythe CMK. That new Keyring/Master Key Provider is passed into your API, and you are set to start encrypting and decrypting with KMS and the Encryption SDK.

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

Experiment using the API as much as you like. 

To get started, here are some things to try:

* Compare <a href="https://us-east-2.console.aws.amazon.com/cloudtrail/home?region=us-east-2#" target="_blank">CloudTrail Logs for usages of Faythe</a> when you encrypt messages of different sizes (small, medium, large)
* Take a look at the <a href="https://s3.console.aws.amazon.com/s3/home" target="_blank">contents of your S3 Document Bucket</a> to inspect the raw object


For more things to try, check out [Explore Further](#explore-further), below.

```java tab="Java"
// Compile your code
mvn compile

// To use the API programmatically, use this target to launch jshell
mvn jshell:run
/open startup.jsh
Api documentBucket = App.initializeDocumentBucket();
documentBucket.list();
documentBucket.store("Store me in the Document Bucket!".getBytes());
for (PointerItem item : documentBucket.list()) {
    DocumentBundle document = documentBucket.retrieve(item.partitionKey().getS());
    System.out.println(document.getPointer().partitionKey().getS() + " : " + new String(document.getData(), java.nio.charset.StandardCharsets.UTF_8));
}
// Ctrl+D to exit jshell

// Or, to run logic that you write in App.java, use this target after compile
mvn exec:java
```

```javascript tab="JavaScript Node.JS"
node
list = require("./list.js")
store = require("./store.js")
retrieve = require("./retrieve")
list().then(console.log)
store(fs.createReadStream("./store.js")).then(r => {
  // Just storing the s3 key
  key = r.Key
  console.log(r)
})
list().then(console.log)
(() => {retrieve("PutYourKeyValue").pipe(process.stdout)})()
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
;({retrieve} = require("./src/retrieve.ts"))
list().then(console.log)
store(fs.createReadStream("./src/store.ts")).then(r => {
  // Just storing the s3 key
  key = r.Key
  console.log(r)
})
list().then(console.log)
(() => {retrieve("PutYourKeyValue").pipe(process.stdout)})()
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
ops.retrieve("PutYourKeyHere").data
# Ctrl-D when finished to exit the REPL
```

## Explore Further

* **AWS Cloud Development Kit** - Check out the `~/environment/workshop/cdk` directory to see how the workshop resources are described using CDK.
* **Alice, Bob, and Friends** - <a href="https://en.wikipedia.org/wiki/Alice_and_Bob#Cast_of_characters" target="_blank">Who are Faythe and Walter?</a>
* **Leveraging the Message Format** - The <a href="https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/message-format.html" target="_blank">AWS Encryption SDK Message Format</a> is an open standard. Can you write something to detect whether an entry in the Document Bucket has been encrypted in this format or not, and retrieve or decrypt appropriately?
* **More Test Content** - Small test strings are enough to get started, but you might be curious to see what the behavior and performance looks like with larger documents. What if you add support for loading files to and from disk to the Document Bucket?
* **Configuration Glue** - If you are curious how the Document Bucket is configured, take a peek at `~/environment/workshop/cdk/Makefile` and the `make state` target, as well as `config.toml` in the exercises root `~/environment/workshop/exercises/config.toml`. The Busy Engineer's Document Bucket uses a base <a href="https://github.com/toml-lang/toml" target="_blank">TOML</a> file to set standard names for all CloudFormation resources and a common place to discover the real deployed set. Then it uses the AWS Cloud Development Kit (CDK) to deploy the resources and write out their identifiers to the state file. Applications use the base TOML file `config.toml` to locate the state file and pull the expected resource names. And that's how the system bootstraps all the resources it needs!

# Next exercise

Now that you are encrypting and decrypting, how about [adding Multiple CMKs](./multi-cmk.md)?
