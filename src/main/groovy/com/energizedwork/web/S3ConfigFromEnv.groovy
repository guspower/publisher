package com.energizedwork.web

import groovy.transform.Canonical
import groovy.transform.Memoized

@Canonical
class S3ConfigFromEnv implements S3Config {

    @Memoized
    String getBucketName() {
        System.getenv()['S3_BUCKET']
    }

    @Memoized
    String getAWSAccessKeyId() {
        System.getenv()['S3_KEY']
    }

    @Memoized
    String getAWSSecretKey() {
        System.getenv()['S3_SECRET']
    }


}
