# Exercise 2: Adding Multi-CMK Support to the Document Bucket

In this section, you will configure the AWS Encryption SDK to use multiple CMKs that reside in different regions.

## Background

Now your Document Bucket will encrypt files when you `store` them, and will decrypt the files for you when you `retrieve` them.

You're using one of your CMKs, Faythe. But what if you want to use multiple CMKs?

You might want to use a partner team's CMK, so that they can access documents relevant to them.

Perhaps you want the Document Bucket to have two independent regions to access the contents, for high availability, or to put the contents closer to the recipients.

Configuring multiple CMKs this way does not require re-encryption of the document data. That's because the data is still encrypted with a single data key, used exclusively for that document. Configuring multiple CMKs causes the AWS Encryption SDK to encrypt that data key again using the additional CMKs, and store that additional version of the data key on the encrypted message format. As long as there is one available CMK to decrypt any encrypted version of the data key, the document will be accessible.

(There are ways to configure the Encryption SDK to be more restrictive about which CMKs it will try -- but for now you'll start with the simple case.)

There's many reasons why using more than one CMK can be useful. And in this exercise, you're going to see how to set that up with KMS and the Encryption SDK.

You already have another CMK waiting to be used. When you deployed the Faythe CMK, you also deployed a second CMK, nicknamed Walter. In this exercise, we're going to configure Walter, and then use some scripts in the repository to add and remove permission to use each of Faythe and Walter. Doing so will change how your document is encrypted -- if you remove permission to both Faythe and Walter, you won't be able to encrypt or decrypt anymore! -- and let you observe how the system behavior changes when keys are accessible or inaccessible.

Each attempt to use a CMK is checked against that CMK's permissions. An audit trail entry is also written to CloudTrail.

Decryption attempts will continue for each version of the encrypted data key until the Encryption SDK either succeds at decrypting an encrypted data key with its associated CMK, or runs out of encrypted data keys to try.

For encryption, the Encryption SDK will attempt to use every CMK it is configured to attempt to produce another encryption of that data key.

You'll get to see all of this in action in just a minute, after a couple small code changes.

## Make the Change

### Starting Directory

If you just finished [Adding the Encryption SDK](./add-the-encryption-sdk.md), you are all set.

If you aren't sure, or want to catch up, jump into the `multi-cmk-start` directory for the language of your choice.

```bash tab="Java"
cd ~/environment/workshop/exercises/java/multi-cmk-start
```

```bash tab="Typescript Node.JS"
cd ~/environment/workshop/exercises/node-typescript/multi-cmk-start
```

```bash tab="JavaScript Node.JS"
cd ~/environment/workshop/exercises/node-javascript/multi-cmk-start
```

```bash tab="Python"
cd ~/environment/workshop/exercises/python/multi-cmk-start
```

### Step 1: Configure Walter

```java tab="Java" hl_lines="4"
// Edit ./src/main/java/sfw/example/esdkworkshop/App.java
    String faytheCMK = state.contents.FaytheCMK;
    // MULTI-CMK-START: Configure Walter
    String walterCMK = state.contents.WalterCMK;
```

```javascript tab="JavaScript Node.JS" hl_lines="3 7"
// Edit store.js
// MULTI-CMK-START: Add the WalterCMK
const walterCMK = config.state.getWalterCMK();

// Edit retrieve.js
// MULTI-CMK-START: Add the WalterCMK
const walterCMK = config.state.getWalterCMK();
```

```typescript tab="Typescript Node.JS" hl_lines="3 7"
// Edit src/store.js
// MULTI-CMK-START: Add the WalterCMK
const walterCMK = config.state.getWalterCMK();

// Edit retrieve.js
// MULTI-CMK-START: Add the WalterCMK
const walterCMK = config.state.getWalterCMK();
```

```python tab="Python" hl_lines="4"
# Edit src/document_bucket/__init__.py

# MULTI-CMK-START: Configure Walter
walter_cmk = state["WalterCMK"]
```

#### What Just Happened

When you launched your workshop stacks in [Getting Started](./getting-started.md), along with the Faythe CMK, you also launched a CMK called Walter. Walter's ARN was also plumbed through to the configuration state file that is set up for you by the workshop. Now that ARN is being pulled into a variable to use in the Encryption SDK configuration.

### Step 2: Add Walter to the CMKs to Use

```java tab="Java" hl_lines="4"
// Edit ./src/main/java/sfw/example/esdkworkshop/App.java
    // MULTI-CMK-START: Add Walter to the CMKs to Use
    KmsMasterKeyProvider mkp =
        KmsMasterKeyProvider.builder().withKeysForEncryption(faytheCMK, walterCMK).build();
```

```javascript tab="JavaScript Node.JS" hl_lines="4 5 6 7 13"
// Edit store.js
// MULTI-CMK-START: Add the WalterCMK
...
const encryptKeyring = new KmsKeyringNode({
  generatorKeyId: faytheCMK,
  keyIds: [walterCMK]
});

// Save and exit
// Edit retrieve.js
// MULTI-CMK-START: Add the WalterCMK
...
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK, walterCMK] });

// Save and exit
```

```typescript tab="Typescript Node.JS" hl_lines="4 5 6 7 13"
// Edit src/store.js
// MULTI-CMK-START: Add the WalterCMK
...
const encryptKeyring = new KmsKeyringNode({
  generatorKeyId: faytheCMK,
  keyIds: [walterCMK]
});

// Save and exit
// Edit store.js
// MULTI-CMK-START: Add the WalterCMK
...
const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheCMK, walterCMK] });

// Save and exit
```

```python tab="Python" hl_lines="4"
# Edit src/document_bucket/__init__.py

# MULTI-CMK-START: Add Walter to the CMKs to Use
cmk = [faythe_cmk, walter_cmk]

# Save and exit
```

#### What Just Happened

In the previous exercise, you configured the Encryption SDK to use a list of CMKs that contained only Faythe. Configuring the Encryption SDK to also use Walter for encrypt, and to also try Walter for decrypt, required adding the ARN for Walter to the configuration list.

### Checking Your Work

If you want to check your progress, or compare what you've done versus a finished example, check out the code in one of the `-complete` folders to compare.

There is a `-complete` folder for each language.

```bash tab="Java"
cd ~/environment/workshop/exercises/java/multi-cmk-complete
```

```bash tab="Typescript Node.JS"
cd ~/environment/workshop/exercises/node-typescript/multi-cmk-complete
```

```bash tab="JavaScript Node.JS"
cd ~/environment/workshop/exercises/node-javascript/multi-cmk-complete
```

```bash tab="Python"
cd ~/environment/workshop/exercises/python/multi-cmk-complete
```

## Try it Out

Adding the Walter CMK to the list of CMKs that the application will (attempt) to use was a couple of lines of code, but has powerful implications.

To help you explore the behavior of the system, there are some additional `make` targets to change the permissions configuration of Faythe and Walter.

Using these targets, you can add and remove permission for the application to use Faythe and Walter to generate data keys, encrypt, and decrypt, and observe how the application behavior changes -- as well as what is logged to CloudTrail.

In `~/environment/workshop/exercises`, you'll find a `Makefile` with several targets for you to experiment with:

* `make revoke_walter_grant` will remove the Grant providing permissions to use Walter in the application
* `make revoke_faythe_grant` will remove the Grant providing permissions to use Faythe in the application
* `make revoke_grants` will remove the Grants for both CMKs
* `make create_grants` will add Grants to use either or both CMK, as needed

You can observe the impact of changing Granted permissions by monitoring CloudTrail.

* Faythe is in `us-east-2`, so <a href="https://us-east-2.console.aws.amazon.com/cloudtrail/home?region=us-east-2#" target="_blank">check CloudTrail in us-east-2</a>
* Walter is in `us-west-2`, so <a href="https://us-west-2.console.aws.amazon.com/cloudtrail/home?region=us-west-2#/dashboard" target="_blank">check CloudTrail in us-west-2</a>

Try out combinations of Grant permissions for your application and watch how the behavior changes:

* Revoke permission to use Faythe, and watch calls move to Walter in CloudTrail and in your application
* With permission to use Faythe revoked, try retrieving an older document protected by Faythe
* Revoke permissions to both Faythe and Walter -- now operations fail
* Encrypt some data with both Faythe and Walter active, and revoke permission to either one -- notice that application operations continue to work
* Change the configuration order of Faythe and Walter, and watch how call patterns change to use the two CMKs
* Revoke permission to Walter, and encrypt some data with Faythe. Then, add permission back to Walter, revoke permission to use Faythe, and try to decrypt that data
* What other interesting access patterns can you imagine?

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
    DocumentBundle document = documentBucket.retrieve(item.partitionKey());
    System.out.println(document.toString());
}
// Ctrl+D to exit jshell

// Use the make targets to change the Grants and see what happens!
// To run logic that you write in App.java, use this target
mvn compile
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
retrieve(key).pipe(process.stdout)
// Use the make targets to change the Grants and see what happens!
// Ctrl-D when finished to exit the REPL
```

```bash tab="JavaScript Node.JS CLI"
./cli.js list
./cli.js store ./store.js
# Note the "Key" value
./cli.js list
# Note the "reference" value
./cli.js retrieve $KeyOrReferenceValue
# Use the make targets to change the grants and see what happens!
```

```typescript tab="Typescript Node.JS"
node -r ts-node/register
;({list} = require("./src/list.ts"))
;({store} = require("./src/store.ts"))
retrieve = require("./retrieve")
list().then(console.log)
store(fs.createReadStream("./store.js")).then(r => {
  // Just storing the s3 key
  key = r.Key
  console.log(r)
})
list().then(console.log)
retrieve(key).pipe(process.stdout)
// Ctrl-D when finished to exit the REPL
// Use the make targets to change the Grants and see what happens!
```

```bash tab="Typescript Node.JS CLI"
./cli.ts list
./cli.ts store ./store.js
# Note the "Key" value
./cli.ts list
# Note the "reference" value
./cli.ts retrieve $KeyOrReferenceValue
# Use the make targets to change the grants and see what happens!
```

```python tab="Python"
tox -e repl
import document_bucket
ops = document_bucket.initialize()
ops.list()
ops.store(b'some data')
for item in ops.list():
    print(ops.retrieve(item.partition_key))
# Use the make targets to change the grants and see what happens!
# Ctrl-D when finished to exit the REPL
```

## Explore Further

Want to dive into more content related to this exercise? Try out these links.

* <a href="https://docs.aws.amazon.com/kms/latest/developerguide/grants.html" target="_blank">AWS KMS: Key Grants</a>
* <a href="https://docs.aws.amazon.com/kms/latest/developerguide/key-policies.html" target="_blank">AWS KMS: Key Policies</a>
* <a href="https://docs.aws.amazon.com/kms/latest/developerguide/key-policy-modifying-external-accounts.html" target="_blank">AWS KMS: Cross-account CMK Usage</a>
* <a href="https://aws.amazon.com/blogs/security/how-to-decrypt-ciphertexts-multiple-regions-aws-encryption-sdk-in-c/" target="_blank">Blog Post: How to decrypt ciphertexts in multiple regions with the AWS Encryption SDK in C</a>

# Next exercise

Ready for more? Next you will work with [Encryption Context](./encryption-context.md).
