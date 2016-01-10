# Simple Web Publisher

Simple web publisher for single page apps. Currently supports S3 targets. Useful for DR, backups etc.

## Usage 

    S3_BUCKET=<bucket> S3_KEY=<key> S3_SECRET=<secret> com.energizedwork.web.PublishWebsiteToS3 -PbaseUrl=<baseUrl> -Padditional=<additional resources...>"

or

    S3_BUCKET=<bucket> S3_KEY=<key> S3_SECRET=<secret> ./gradlew publish -PbaseUrl=<baseUrl> -Padditional=<additional resources...>"


## Travis

[![Build Status](https://travis-ci.org/guspower/publisher.svg?branch=master)](https://travis-ci.org/guspower/publisher)
