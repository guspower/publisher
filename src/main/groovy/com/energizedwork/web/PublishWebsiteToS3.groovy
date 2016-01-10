package com.energizedwork.web

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.SetObjectAclRequest
import jodd.http.HttpRequest
import jodd.http.HttpResponse
import jodd.jerry.Jerry

import static jodd.jerry.Jerry.jerry

class PublishWebsiteToS3 {

    final static List<String> DEFAULT_RESOURCES = ['/', '/robots.txt']

    Closure log = { String message ->
        println message
    }

    private final S3Config s3Config
    private final AmazonS3 s3

    PublishWebsiteToS3() {
        this.s3Config = new S3ConfigFromEnv()
        this.s3 = new AmazonS3Client(s3Config)
    }

    PublishWebsiteToS3(AmazonS3 s3, S3Config s3Config) {
        this.s3Config = s3Config
        this.s3 = s3
    }

    void run(String baseUrl, List<String> additionalResources = []) {
        publishWebsite formatUrlString(baseUrl), additionalResources, s3, s3Config
    }

    private String formatUrlString(String url) {
        url.endsWith('/') ? url.substring(0, url.length()-1): url
    }

    private void publishWebsite(String baseUrl, List<String> additionalResources = [], AmazonS3 s3, S3Config s3Config) {
        def html = jerry(new URL(baseUrl).text)

        List<String> resources = buildResourceList(html, additionalResources)

        resources.each { String path ->
            upload baseUrl, path
        }
    }

    private void upload(String baseUrl, String path) {
        String key = calculateS3KeyFromPath(path)
        def response = HttpRequest.get("$baseUrl$path").send()

        if (response.statusCode() < 300) {
            log "Uploading $key ..."

            s3.putObject buildPutRequest(response, s3Config.bucketName, key)
            s3.setObjectAcl buildAclRequest(key)
        } else {
            log "Error syncing $key [${response.statusCode()} ${response.statusPhrase()}]"
        }
    }

    private String calculateS3KeyFromPath(String path) {
        (path == '/') ? 'index.html' : path.substring(1)
    }

    private SetObjectAclRequest buildAclRequest(String key) {
        new SetObjectAclRequest(
            s3Config.bucketName, key,
            CannedAccessControlList.PublicRead
        )
    }

    private PutObjectRequest buildPutRequest(HttpResponse response, String bucketName, String key) {
        def metadata = new ObjectMetadata()
        metadata.contentType = response.contentType()
        metadata.contentLength = response.contentLength().toLong()

        def put = new PutObjectRequest(
            bucketName, key,
            new ByteArrayInputStream(response.bodyBytes()),
            metadata
        )
        put
    }

    private List<String> buildResourceList(Jerry html, List<String> additionalResources) {
        Closure linkFilter = { String link ->
            link && link.startsWith('/') && !link.startsWith('/#') && (link.size() > 1) }

        html.$('link')*.attr('href').findAll(linkFilter).unique() +
            html.$('a')*.attr('href').findAll(linkFilter).unique() +
            html.$('img')*.attr('src').findAll(linkFilter).unique() +
            html.$('script')*.attr('src').findAll(linkFilter).unique() +
            DEFAULT_RESOURCES + additionalResources
    }

    static void main(String[] args) {
        if(args) {
            String baseUrl = args[0]
            List additionalIncludes = (args.length > 1) ? args[1..-1].collect { it }: []
            new PublishWebsiteToS3().run(baseUrl, additionalIncludes)
        } else {
            showUsage()
        }
    }

    private static void showUsage() {
        println "Usage: S3_BUCKET=<bucket> S3_KEY=<key> S3_SECRET=<secret> ${PublishWebsiteToS3.simpleName} -PbaseUrl=<baseUrl> -Padditional=<additional resources...>"
        println "e.g. ${PublishWebsiteToS3.simpleName} -PbaseUrl=http://localhost:8080 \"-Padditional=['/assets/customfont.ttf']\""

        println "\nAWS credentials are provided via the following environment variables:"
        println "\tS3_KEY \n\tS3_SECRET and \n\tS3_BUCKET"
    }

}
