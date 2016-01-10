package com.energizedwork.web

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectRequest
import ratpack.groovy.server.GroovyRatpackServerSpec
import ratpack.groovy.test.embed.GroovyEmbeddedApp
import ratpack.test.CloseableApplicationUnderTest
import spock.lang.Specification

class PublishWebsiteToS3Spec extends Specification {

    CloseableApplicationUnderTest aut

    def setup() {
        aut = GroovyEmbeddedApp.of { GroovyRatpackServerSpec server ->
            server.handlers {
                all { render '''
<html>
<body>
    <a href="/anchor">Anchor</a>
    <img src="/image"/>
    <script src="/script"/>
</body>
</html>
                ''' }
                get 'robots.txt', { render '<robots/>' }
            }
        }
    }

    def cleanup() { aut.close() }

    def 'puts objects onto S3'() {
        given:
            def s3 = Mock(AmazonS3)
            def s3Config = Mock(S3Config) {
                _ * getBucketName() >> bucketName
            }

        and:
            def publisher = new PublishWebsiteToS3(s3, s3Config)

        when:
            publisher.run aut.address.toString()

        then:
            1 * s3.putObject({ matchKey('index.html', it) })
            1 * s3.putObject({ matchKey('robots.txt', it) })
            1 * s3.putObject({ matchKey('anchor', it) })
            1 * s3.putObject({ matchKey('image', it) })
            1 * s3.putObject({ matchKey('script', it) })
            5 * s3.setObjectAcl(_)

        where:
            bucketName = 's3-bucket'
    }

    boolean matchKey(String key, PutObjectRequest put) {
        key == put.key
    }

}
