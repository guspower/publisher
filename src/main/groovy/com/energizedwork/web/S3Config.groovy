package com.energizedwork.web

import com.amazonaws.auth.AWSCredentials

interface S3Config extends AWSCredentials {

    String getBucketName()

}