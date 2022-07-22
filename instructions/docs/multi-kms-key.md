# Exercise 2: Adding Multi-KMS-Key Support to the Document Bucket

In this section, you will configure the AWS Encryption SDK to use multiple KMS Keys that reside in different regions.

## Background

Now your Document Bucket will encrypt files when you `store` them, and will decrypt the files for you when you `retrieve` them.

You're using one of your KMS Keys, Faythe. But what if you want to use multiple KMS Keys?

You might want to use a partner team's KMS Key, so that they can access documents relevant to them.

Perhaps you want the Document Bucket to have two independent regions to access the contents, for high availability, or to put the contents closer to the recipients.

Configuring multiple KMS Keys this way does not require re-encryption of the document data. That's because the data is still encrypted with a single data key, used exclusively for that document. Configuring multiple KMS Keys causes the AWS Encryption SDK to encrypt that data key again using the additional KMS Keys, and store that additional version of the data key on the encrypted message format. As long as there is one available KMS Key to decrypt any encrypted version of the data key, the document will be accessible.

(There are ways to configure the Encryption SDK to be more restrictive about which KMS Keys it will try -- but for now you'll start with the simple case.)

There's many reasons why using more than one KMS Key can be useful. And in this exercise, you're going to see how to set that up with KMS and the Encryption SDK.

You already have another KMS Key waiting to be used. When you deployed the Faythe KMS Key, you also deployed a second KMS Key, nicknamed Walter. In this exercise, we're going to configure Walter, and then use some scripts in the repository to add and remove permission to use each of Faythe and Walter. Doing so will change how your document is encrypted -- if you remove permission to both Faythe and Walter, you won't be able to encrypt or decrypt anymore! -- and let you observe how the system behavior changes when keys are accessible or inaccessible.

Each attempt to use a KMS Key is checked against that KMS Key's permissions. An audit trail entry is also written to CloudTrail.

Decryption attempts will continue for each version of the encrypted data key until the Encryption SDK either succeds at decrypting an encrypted data key with its associated KMS Key, or runs out of encrypted data keys to try.

For encryption, the Encryption SDK will attempt to use every KMS Key it is configured to attempt to produce another encryption of that data key.

You'll get to see all of this in action in just a minute, after a couple small code changes.

## Let's Go!

### Starting Directory

If you just finished [Adding the Encryption SDK](./adding-the-encryption-sdk.md), you are all set.

If you aren't sure, or want to catch up, jump into the `multi-kms-key-start` directory for the language of your choice.

=== "Java"

    ```bash 
    cd ~/environment/workshop/exercises/java/multi-kms-key-start
    ```

=== "Typescript Node.JS"

    ```bash
    cd ~/environment/workshop/exercises/node-typescript/multi-kms-key-start
    ```

=== "JavaScript Node.JS"

    ```bash
    cd ~/environment/workshop/exercises/node-javascript/multi-kms-key-start
    ```

=== "Python"

    ```bash
    cd ~/environment/workshop/exercises/python/multi-kms-key-start
    ```

### Step 1: Configure Walter

=== "Java"

    ```{.java hl_lines="4"}
    // Edit ./src/main/java/sfw/example/esdkworkshop/App.java
        String faytheKmsKey = stateConfig.contents.state.faytheKmsKey;
        // MULTI-KMS-KEY-START: Configure Walter
        String walterKmsKey = stateConfig.contents.state.WalterKmsKey;
    ```

=== "JavaScript Node.JS"

    ```{.javascript hl_lines="3 7"}
    // Edit ./store.js
    // MULTI-KMS-KEY-START: Configure Walter
    const walterKmsKey = config.state.getWalterKmsKey();

    // Edit ./retrieve.js
    // MULTI-KMS-KEY-START: Configure Walter
    const walterKmsKey = config.state.getWalterKmsKey();
    ```

=== "Typescript Node.JS"

    ```{.typescript hl_lines="3 7"}
    // Edit ./src/store.ts
    // MULTI-KMS-KEY-START: Configure Walter
    const walterKmsKey = config.state.getWalterKmsKey();

    // Edit ./src/retrieve.ts
    // MULTI-KMS-KEY-START: Configure Walter
    const walterKmsKey = config.state.getWalterKmsKey();
    ```

=== "Python"

    ```{.python hl_lines="4"}
    # Edit src/document_bucket/__init__.py

        # MULTI-KMS-KEY-START: Configure Walter
        walter_kms_key = state["WalterKmsKey"]
    ```

#### What Happened?

When you launched your workshop stacks in [Getting Started](./getting-started.md), along with the Faythe KMS Key, you also launched a KMS Key called Walter. Walter's ARN was also plumbed through to the configuration state file that is set up for you by the workshop. Now that ARN is being pulled into a variable to use in the Encryption SDK configuration.

### Step 2: Add Walter to the KMS Keys to Use

=== "Java"

    ```{.java hl_lines="3"}
    // Edit ./src/main/java/sfw/example/esdkworkshop/App.java
        // MULTI-KMS-KEY-START: Add Walter to the KMS Keys to Use
        KmsMasterKeyProvider mkp = KmsMasterKeyProvider.builder().buildStrict(faytheKmsKey, walterKmsKey);
    ```

=== "JavaScript Node.JS"

    ```{.javascript hl_lines="4 5 6 7 13"}
    // Edit ./store.js
    // MULTI-KMS-KEY-START: Add Walter to the KMS Keys to Use
    ...
    const encryptKeyring = new KmsKeyringNode({
      generatorKeyId: faytheKmsKey,
      keyIds: [walterKmsKey]
    });

    // Save and exit
    // Edit ./retrieve.js
    // MULTI-KMS-KEY-START: Add Walter to the KMS Keys to Use
    ...
    const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheKmsKey, walterKmsKey] });

    // Save and exit
    ```

=== "Typescript Node.JS"

    ```{.typescript hl_lines="4 5 6 7 13"}
    // Edit ./src/store.ts
    // MULTI-KMS-KEY-START: Add Walter to the KMS Keys to Use
    ...
    const encryptKeyring = new KmsKeyringNode({
      generatorKeyId: faytheKmsKey,
      keyIds: [walterKmsKey]
    });

    // Save and exit
    // Edit ./src/retrieve.ts
    // MULTI-KMS-KEY-START: Add Walter to the KMS Keys to Use
    ...
    const decryptKeyring = new KmsKeyringNode({ keyIds: [faytheKmsKey, walterKmsKey] });

    // Save and exit
    ```

=== "Python"

    ```{.python hl_lines="4"}
    # Edit src/document_bucket/__init__.py

        # MULTI-KMS-KEY-START: Add Walter to the KMS Keys to Use
        kms_key = [faythe_kms_key, walter_kms_key]

    # Save and exit
    ```

#### What Happened?

In the previous exercise, you configured the Encryption SDK to use a list of KMS Keys that contained only Faythe. Configuring the Encryption SDK to also use Walter for encrypt, and to also try Walter for decrypt, required adding the ARN for Walter to the configuration list.

### Checking Your Work

If you want to check your progress, or compare what you've done versus a finished example, check out the code in one of the `-complete` folders to compare.

There is a `-complete` folder for each language.

=== "Java"

    ```bash 
    cd ~/environment/workshop/exercises/java/multi-kms-key-complete
    ```

=== "Typescript Node.JS"

    ```bash
    cd ~/environment/workshop/exercises/node-typescript/multi-kms-key-complete
    ```

=== "JavaScript Node.JS"

    ```bash
    cd ~/environment/workshop/exercises/node-javascript/multi-kms-key-complete
    ```

=== "Python"

    ```bash
    cd ~/environment/workshop/exercises/python/multi-kms-key-complete
    ```

## Try it Out

Adding the Walter KMS Key to the list of KMS Keys that the application will (attempt) to use was a couple of lines of code, but has powerful implications.

To help you explore the behavior of the system, there are some additional `make` targets to change the permissions configuration of Faythe and Walter.

Using these targets, you can add and remove permission for the application to use Faythe and Walter to generate data keys, encrypt, and decrypt, and observe how the application behavior changes -- as well as what is logged to CloudTrail.

In `~/environment/workshop/exercises`, you'll find a `Makefile` with several targets for you to experiment with:

* `make list_grants` will show you the current state of grants on your KMS Keys
* `make revoke_walter_grant` will remove the Grant providing permissions to use Walter in the application
* `make revoke_faythe_grant` will remove the Grant providing permissions to use Faythe in the application
* `make revoke_grants` will remove the Grants for both KMS Keys
* `make create_grants` will add Grants to use either or both KMS Key, as needed

Note: When you create or revoke a grant there might be a brief delay, usually less than five minutes, until the grant is available throughout AWS KMS.

**Important** when you revoke permissions to the first KMS Key in the list for a keyring (which is Faythe by default), you will need to change the keyring configuration to use Walter as your generator to resume operations. See <a href="https://docs.aws.amazon.com/encryption-sdk/latest/developer-guide/js-examples.html" target="_blank">documentation of Generator KMS Keys</a> for more.

You can also observe the impact of changing Granted permissions by monitoring CloudTrail. Note that log entries take a few minutes to propagate to CloudTrail, and that Faythe and Walter are in different regions, so you will need to look at CloudTrail in the region for each one.

* Faythe is in `us-east-2`, so check CloudTrail in that region with these links:
    * <a href="https://console.aws.amazon.com/cloudtrail/home?region=us-east-2#/events?EventName=GenerateDataKey" target="_blank">GenerateDataKey operations in CloudTrail in us-east-2</a>
    * <a href="https://us-east-2.console.aws.amazon.com/cloudtrail/home?region=us-east-2#/events?EventName=Decrypt" target="_blank">Decrypt operations in CloudTrail in us-east-2</a>
    * <a href="https://us-east-2.console.aws.amazon.com/cloudtrail/home?region=us-east-2#/events?EventName=Encrypt" target="_blank">Encrypt operations in CloudTrail in us-east-2</a>
        * Note: To observe Encrypt calls using Faythe, explore some of the configuration and permissions changes suggested below.
* Walter is in `us-east-1`, so check CloudTrail in that region with these links:
    * <a href="https://console.aws.amazon.com/cloudtrail/home?region=us-east-1#/events?EventName=GenerateDataKey" target="_blank">GenerateDataKey operations in CloudTrail in us-east-1</a>
    * <a href="https://us-east-1.console.aws.amazon.com/cloudtrail/home?region=us-east-1#/events?EventName=Encrypt" target="_blank">Encrypt operations in CloudTrail in us-east-1</a>
    * <a href="https://us-east-1.console.aws.amazon.com/cloudtrail/home?region=us-east-1#/events?EventName=Decrypt" target="_blank">Decrypt operations in CloudTrail in us-east-1</a>

Try out combinations of Grant permissions for your application and watch how the behavior changes:

* Revoke permission to use Faythe, and watch calls move to Walter in CloudTrail and in your application
* With permission to use Faythe revoked, try retrieving an older document protected by Faythe
* Revoke permissions to both Faythe and Walter -- now operations fail
* Encrypt some data with both Faythe and Walter active, and revoke permission to either one -- notice that application operations continue to work
* Change the configuration order of Faythe and Walter, and watch how call patterns change to use the two KMS Keys
* Revoke permission to Walter, and encrypt some data with Faythe. Then, add permission back to Walter, revoke permission to use Faythe, and try to decrypt that data
* What other interesting access patterns can you imagine?

=== "Java"

    ```java
    // Compile your code
    mvn compile

    // To use the API programmatically, use this target to launch jshell
    mvn jshell:run
    /open startup.jsh
    Api documentBucket = App.initializeDocumentBucket();
    documentBucket.list();
    PointerItem item = documentBucket.store("Store me in the Document Bucket!".getBytes());
    DocumentBundle document = documentBucket.retrieve(item.partitionKey().getS());
    System.out.println(document.getPointer().partitionKey().getS() + " : " + new String(document.getData(), java.nio.charset.StandardCharsets.UTF_8));
    // Ctrl+D to exit jshell

    // Use the make targets to change the Grants and see what happens!
    // To run logic that you write in App.java, use this target after compile
    mvn exec:java
    ```

=== "JavaScript Node.JS"

    ```javascript
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

=== "JavaScript Node.JS CLI"

    ```bash
    ./cli.js list
    ./cli.js store ./store.js
    # Note the "Key" value
    ./cli.js list
    # Note the "reference" value
    ./cli.js retrieve $KeyOrReferenceValue
    # Use the make targets to change the grants and see what happens!
    ```

=== "Typescript Node.JS"

    ```{.typescript}
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
    retrieve(key).pipe(process.stdout)
    // Ctrl-D when finished to exit the REPL
    // Use the make targets to change the Grants and see what happens!
    ```

=== "Typescript Node.JS CLI"

    ```bash
    ./cli.ts list
    ./cli.ts store ./src/store.ts
    # Note the "Key" value
    ./cli.ts list
    # Note the "reference" value
    ./cli.ts retrieve $KeyOrReferenceValue
    # Use the make targets to change the grants and see what happens!
    ```

=== "Python"

    ```python
    tox -e repl
    import document_bucket
    ops = document_bucket.initialize()
    ops.list()
    item = ops.store(b'some data')
    ops.retrieve(item.partition_key)
    # Use the make targets to change the grants and see what happens!
    # Ctrl-D when finished to exit the REPL
    ```

## Explore Further

Want to dive into more content related to this exercise? Try out these links.

* <a href="https://docs.aws.amazon.com/kms/latest/developerguide/grants.html" target="_blank">AWS KMS: Key Grants</a>
* <a href="https://docs.aws.amazon.com/kms/latest/developerguide/key-policies.html" target="_blank">AWS KMS: Key Policies</a>
* <a href="https://docs.aws.amazon.com/kms/latest/developerguide/key-policy-modifying-external-accounts.html" target="_blank">AWS KMS: Cross-account KMS Key Usage</a>
* <a href="https://aws.amazon.com/blogs/security/how-to-decrypt-ciphertexts-multiple-regions-aws-encryption-sdk-in-c/" target="_blank">Blog Post: How to decrypt ciphertexts in multiple regions with the AWS Encryption SDK in C</a>

# Next exercise

Ready for more? Next you will work with [Encryption Context](./encryption-context.md).
