# Exercise 1: Add the AWS Encryption SDK

## Background

Right now, the Document Bucket supports storing files (or documents, or other blobs of data) in S3, and indexing them in DynamoDB. This allows Document Bucket users to share files with other users, or store them for retrieval later. The DynamoDB entries provide fast lookups to the content of the bucket, along with metadata context for each bucket item.

This context allows storing additional information about the S3 item. Perhaps the origin user, the destination fleet, the project, or any other tag that would be useful to know without downloading and examining the item.

DynamoDB is also configured to allow indexing on which documents have which keys. So, for example, it's a quick query to find out which documents in the bucket have been tagged with "configuration" as a piece of metadata about the object contents.

Here's the API the Document Bucket supports:

* `list`: This operation queries DynamoDB for all entries for all items in the bucket, and their metadata. It returns the `set` of items that have been stored.
* `store`: This operation accepts a blob of bytes and a `map` of metadata context. It generates a unique identifier for the document. The identifier and associated metadata are written to DynamoDB. The bytes of the data are written to S3 under a key of that unique identifier. Any context metadata keys in DynamoDB are updated to include that new object identifier.
* `retrieve`: This operation accepts a unique identifier as an argument. First it looks that identifier up in DynamoDB and pulls the identifier and its context out. Then it retrieves that object from S3. It bundles these items together and returns them to the caller.
* `search`: This operation accepts a metadata key to search for. It then queries DynamoDB for the `set` of documents in the Document Bucket that have context matching that key. This operation then returns that set of document identifiers and their metadata.
    * Once the desired document or documents have been identified with the returned metadata, the document identifiers can be passed to `retrieve` to actually fetch the documents.

This is a start for sharing, storing, and searching documents of a variety of types. But what about sensitive documents? Or protecting, say, important configuration files from accidental corruption during storing or retrieving?

Now you will add the AWS Encryption SDK to take an extra step of protection: client-side encrypting the Document Bucket document before it is transmitted off of the host machine. You will use KMS to provide a `data key` for each document, using a CMK that you set up in [Getting Started](./getting-started.md) when you deployed your stacks.

## Make the Change

### Starting Directory

Make sure you are in the `exercises` directory for the language of your choice:

```Java
~/environment/workshop/exercises/java
```

```NodeJS
~/environment/workshop/exercises/node-javascript
```

```Python
~/environment/workshop/exercises/python
```

`cd` into the `add-esdk-start` directory.

### Step 1: Add the ESDK Dependency

Look for `ADD-ESDK-START` comments to help orient yourself in the code.

Start by adding the Encryption SDK dependency to the code.

```NodeJS
TODO
```

```Python
# Edit src/document_bucket/__init__.py

import aws_encryption_sdk

# Save and exit
# Edit src/document_bucket/api.py

# ADD-ESDK-START
import aws_encryption_sdk
from aws_encryption_sdk import KMSMasterKeyProvider

# Add a Master Key Provider to your __init__
# ADD-ESDK-START
def __init__(self, bucket, table, mkp):
    self.bucket = bucket
    self.table = table
    # ADD-ESDK-START
    self.master_key_provider

# Save and exit
```

#### What Just Happened

You just imported a dependency on the AWS Encryption SDK library in your code.

You also changed the API to expect that a Keyring or Master Key Provider will be passed to your code to use in `store` and `retrieve` operations.

### Step 2: Add Encryption to `store`

Now that you have the AWS Encryption SDK imported, start encrypting your data before storing it.

```NodeJS
TODO
```

```Python
# Edit src/document_bucket/api.py
def store(self, data: bytes, context: Dict[str, str] = {}) -> PointerItem:
    # ADD-ESDK-START
    encrypted_data, header = aws_encryption_sdk.encrypt(
        source=data,
        key_provider=self.master_key_provider,
    )
```

#### What Just Happened

You just started encrypting data client-side with the AWS Encryption SDK and KMS!

Now, before storing your data in the Document Bucket, you use the AWS Encryption SDK to:

1. Request a new data key using your keyring or Master Key Provider
1. Encrypt that data for you
1. Return the encrypted data in the AWS Encryption SDK message format
1. Extract the ciphertext to pass to the AWS S3 SDK to store in S3

### Step 3: Add Decryption to `retrieve`

Now that you are encrypting data before storing it, you need to decrypt it before returning it to your caller. At least for it to be useful, anyway.

```NodeJS
TODO
```

#### What Just Happened
```Python
# Edit src/document_bucket/api.py
    def retrieve(
            self,
            pointer_key: str,
            expected_context_keys: Set[str] = set(),
            expected_context: Dict[str, str] = {},
    ) -> DocumentBundle:
        item = self._get_pointer_item(PointerQuery.from_key(pointer_key))
        # ADD-ESDK-START
        encrypted_data = self._get_object(item)
        plaintest, header = aws_encryption_sdk.decrypt(
            source=encrypted_data, key_provider=self.master_key_provider
        )
        return DocumentBundle.from_data_and_context(
            plaintext, item.context
        )
```


You just started decrypting data client-side as well!

The data returned from S3 for `retrieve` is now encrypted. Before returning that data to the user, you added a call to the AWS Encryption SDK to decrypt the data. Under the hood, the Encryption SDK:

1. Read the AWS Encryption SDK formatted encrypted message
1. Called KMS to request to decrypt your message's encrypted data key using the Faythe CMK
1. Used the decrypted data key to decrypt the message
1. Returned the message plaintext and Encryption SDK headers to you


### Step 4: Plumb In Your Config

Now that you have your dependencies declared and your code updated to encrypt and decrypt data, all you need to do is pass through the configuration to the AWS Encryption SDK to start using your KMS CMKs to protect your data.

```NodeJS

TODO

```

```Python

# Edit src/document_bucket/__init__.py

...

# ADD-ESDK-START
# Pull configuration of KMS resources
faythe_cmk = state["FaytheCMK"]
# And the Master Key Provider configuring how to use KMS
cmk = [faythe_cmk]
mkp = aws_encryption_sdk.KMSMasterKeyProvider(key_ids=[cmk])

...

operations = DocumentBucketOperations(bucket, table, mkp)
```

#### What Just Happened

In Getting Started, you launched CloudFormation stacks for CMKs. One of these CMKs was nicknamed Faythe. As part of launching these templates, the CMK's ARN was written to a configuration file on disk, the `state` variable that is loaded and parsed.

Now Faythe's ARN is pulled into a variable, and used to initialize a keyring or master key provider that will use the Faythe CMK. That new keyring/master key provider is passed in to your API, and you are set to start encrypting and decrypting with KMS and the Encryption SDK.

### Checking Your Work

Want to check your progress, or compare what you've done versus a finished example?

Check out the code in one of the `-complete` folders to compare.

```Java
~/environment/workshop/exercises/java/add-esdk-complete
```

```NodeJS
~/environment/workshop/exercises/node-javascript/add-esdk-complete
```

```Python
~/environment/workshop/exercises/python/add-esdk-complete
```

## Try it Out

Now that the code is written, let's load it up and try it out.

If you'd like to try a finished example, use your language's `-complete` directory as described above.

```NodeJS
TODO
```

```Python
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
