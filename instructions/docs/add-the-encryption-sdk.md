# Exercise 1: Add the AWS Encryption SDK

## Background

The Busy Engineer's Document Bucket is an example application meant to show some high-level examples of real world AWS patterns. This includes how to integrate client-side encryption using AWS KMS and the AWS Encryption SDK in application code.

Right now, the Document Bucket supports storing files (or documents, or other blobs of data) in a private S3 bucket, and indexing them in DynamoDB. This allows Document Bucket users to share files with other users, or store them for retrieval later. The DynamoDB entries provide fast lookups to the content of the bucket, along with metadata context for each bucket item.

This context allows storing additional information about the S3 item. Perhaps the origin user, the destination fleet, the project, or any other tag that would be useful to know without downloading and examining the item.

DynamoDB is also configured to allow indexing on which documents have which keys. So, for example, it's a quick query to find out which documents in the bucket have been tagged with "configuration" as a piece of metadata about the object contents.

Here's the API the Document Bucket supports:

* `list`: This operation queries DynamoDB for all entries for all items in the bucket, and their metadata. It returns the `set` of items that have been stored.
* `store`: This operation accepts a blob of bytes and a `map` of metadata context. It generates a unique identifier for the document. The identifier and associated metadata are written to DynamoDB. The bytes of the data are written to S3 under a key of that unique identifier. Any context metadata keys in DynamoDB are updated to include that new object identifier.
* `retrieve`: This operation accepts a unique identifier as an argument. First it looks that identifier up in DynamoDB and pulls the identifier and its context out. Then it retrieves that object from S3. It bundles these items together and returns them to the caller.
* `search`: This operation accepts a metadata key to search for. It then queries DynamoDB for the `set` of documents in the Document Bucket that have context matching that key. This operation then returns that set of document identifiers and their metadata.
    * Once the desired document or documents have been identified with the returned metadata, the document identifiers can be passed to `retrieve` to actually fetch the documents.

This is a start for sharing, storing, and searching documents of a variety of types. But what about sensitive documents? Or protecting, say, important configuration files from accidental corruption during storing or retrieving?

Now you will add the AWS Encryption SDK to encrypt close to where the data originates: client-side encrypting the Document Bucket document before it is transmitted off of the host machine to the internet. You will use KMS to provide a `data key` for each document, using a CMK that you set up in [Getting Started](./getting-started.md) when you deployed your stacks.

## Make the Change

### Starting Directory

Make sure you are in the `exercises` directory for the language of your choice:

```bash tab="Java"
cd ~/environment/workshop/exercises/java
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

```typescript tab="Typescript Node.JS"
// Edit ./store.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
import { encryptStream, KmsKeyringNode } from "@aws-crypto/client-node";

// Save and exit

// Edit ./retrieve.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
import { decryptStream, KmsKeyringNode } from "@aws-crypto/client-node";

// Save and exit
```

```javascript tab="JavaScript Node.JS"
// Edit ./store.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
const { encryptStream, KmsKeyringNode } = require("@aws-crypto/client-node");

// Save and exit

// Edit ./retrieve.js

// ADD-ESDK-START: Add the @aws-crypto/client-node dependency
const { decryptStream, KmsKeyringNode } = require("@aws-crypto/client-node");

// Save and exit
```

```python tab="Python"
# Edit src/document_bucket/__init__.py

import aws_encryption_sdk

# Save and exit
# Edit src/document_bucket/api.py

# ADD-ESDK-START
import aws_encryption_sdk
from aws_encryption_sdk import KMSMasterKeyProvider

# Add a Master Key Provider to your __init__
# ADD-ESDK-START
def __init__(self, bucket, table, master_key_provider: KMSMasterKeyProvider):
    self.bucket = bucket
    self.table = table
    # ADD-ESDK-START
    self.master_key_provider = master_key_provider

# Save and exit
```

#### What Just Happened

You just imported a dependency on the AWS Encryption SDK library in your code.

You also changed the API to expect that a Keyring or Master Key Provider will be passed to your code to use in `store` and `retrieve` operations.

### Step 2: Add Encryption to `store`

Now that you have the AWS Encryption SDK imported, start encrypting your data before storing it.

```typescript tab="Typescript Node.JS"
// Edit ./store.js

// ADD-ESDK-START: Encrypt the stream with a keyring
const Body = fileStream.pipe(encryptStream(encryptKeyring));

// Save and exit

```

```javascript tab="JavaScript Node.JS"
// Edit ./store.js

// ADD-ESDK-START: Encrypt the stream with a keyring
const Body = fileStream.pipe(encryptStream(encryptKeyring));

// Save and exit

```

```python tab="Python"
# Edit src/document_bucket/api.py
# Find the store function and edit it to add the Master Key Provider
# and to write the encrypted data
    # ADD-ESDK-START
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

```typescript tab="Typescript Node.JS"
// Edit retrieve.js

  // ADD-ESDK-START: Decrypt the stream with a keyring
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));

// Save and Exit
```

```javascript tab="JavaScript Node.JS"
// Edit retrieve.js

  // ADD-ESDK-START: Decrypt the stream with a keyring
  return s3
    .getObject({ Bucket, Key })
    .createReadStream()
    .pipe(decryptStream(decryptKeyring));

// Save and Exit
```

```python tab="Python"
# Edit src/document_bucket/api.py
# Find the retrieve function and edit it to add a call to decrypt the
# encrypted data before returning it
        item = self._get_pointer_item(PointerQuery.from_key(pointer_key))
        # ADD-ESDK-START
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

```typescript tab="Typescript Node.JS"

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

```javascript tab="JavaScript Node.JS"

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

```python tab="Python"

# Edit src/document_bucket/__init__.py

...

# ADD-ESDK-START
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

```Java
~/environment/workshop/exercises/java/add-esdk-complete
```

```javascript tab="JavaScript Node.JS"
~/environment/workshop/exercises/node-javascript/add-esdk-complete/store.js
~/environment/workshop/exercises/node-javascript/add-esdk-complete/retrieve.js
```

```python tab="Python"
~/environment/workshop/exercises/python/add-esdk-complete
```

## Try it Out

Now that the code is written, let's load it up and try it out.

If you'd like to try a finished example, use your language's `-complete` directory as described above.

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

* TODO Retrieve
* TODO Examine TOML files
* TODO Generate and load files to store/retrieve

# Next exercise

Now that you are encrypting and decrypting, how about [adding Multiple CMKs](./multi-cmk.md)?
