# busy-engineers-document-bucket/instructions

Source and configuration for building the Doument Bucket workshop website.

This documentation uses [MkDocs](https://www.mkdocs.org/) with a theme for AWS Security
Workshops from https://github.com/aws-samples/aws-security-workshops

## Requirements

To build locally or build and deploy the site, you need to install dependencies. Run following command to install requirements:
```
pip install -r requirements.txt
```

## Build Locally

You can locally build any changes by using the `mkdocs` command within this directory:

```
mkdocs build
```

You can then run a local server and verify any changes you've made locally:

```
mkdocs serve
```

## Deploy

If you have write permission in this repo, you can build and deploy the site to
`https://document-bucket.awssecworkshops.com/`.

Once you have verified your changes, just run:

```
mkdocs gh-deploy --remote-name <remote-name>
````

where `<remote-name>` is whatever you have named the remote for this repo.
This is likely `upstream` if you have been working from a fork.

## License

This project is licensed under the Apache-2.0 License.

