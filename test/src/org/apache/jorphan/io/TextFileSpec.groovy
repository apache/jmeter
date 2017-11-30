package org.apache.jorphan.io

import org.apache.commons.io.FileUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@Unroll
class TextFileSpec extends Specification {

    def tempFile = File.createTempFile("TextFile", ".unittest")

    def setup() {
        tempFile.deleteOnExit()
    }

    def "read lines"() {
        given:
            def sut = new TextFile(tempFile.getAbsolutePath())
        expect:
            sut.getText() == ""
    }

    def "read lines returns exact content of file"() {
        given:
            def sut = new TextFile(tempFile.getAbsolutePath())
            FileUtils.write(tempFile, content, Charset.defaultCharset())
        expect:
            sut.getText() == content
        where:
            content << ["a\nb\nc", "\"a\nb\nc\n"]
    }

    def "read lines returns exact content of file other charset"() {
        given:
            def sut = new TextFile(tempFile.getAbsolutePath(), encoding)
            FileUtils.write(tempFile, content, charset)
        expect:
            sut.getText() == content
        where:
            content     | charset                     | encoding
            "a\nb\nc"   | StandardCharsets.UTF_16     | "UTF_16"
            "a\nb\nc\n" | StandardCharsets.UTF_16     | "UTF_16"
            "a\nb\nc"   | StandardCharsets.ISO_8859_1 | "ISO_8859_1"
            "a\nb\nc\n" | StandardCharsets.ISO_8859_1 | "ISO_8859_1"
    }
}
