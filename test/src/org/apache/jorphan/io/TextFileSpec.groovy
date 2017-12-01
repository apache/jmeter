package org.apache.jorphan.io

import org.apache.commons.io.FileUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@Unroll
class TextFileSpec extends Specification {

    def tmpFile = File.createTempFile("TextFile", ".unittest")
    def tmpPath = tmpFile.getAbsolutePath()

    def setup() {
        tmpFile.deleteOnExit()
    }

    def "getText of empty file is empty string"() {
        given:
            def sut = new TextFile(tmpPath)
        expect:
            sut.getText() == ""
    }

    def "getText returns exact content of file"() {
        given:
            def sut = new TextFile(tmpPath)
            FileUtils.write(tmpFile, content, Charset.defaultCharset())
        expect:
            sut.getText() == content
        where:
            content << ["a\nb\nc", "\"a\nb\nc\n"]
    }

    def "getText returns exact content of file with specific charset"() {
        given:
            def sut = new TextFile(tmpPath, encoding)
            FileUtils.write(tmpFile, content, charset)
        expect:
            sut.getText() == content
        where:
            content     | charset                     | encoding
            "a\nb\nc"   | StandardCharsets.UTF_16     | "UTF_16"
            "a\nb\nc\n" | StandardCharsets.UTF_16     | "UTF_16"
            "a\nb\nc"   | StandardCharsets.ISO_8859_1 | "ISO_8859_1"
            "a\nb\nc\n" | StandardCharsets.ISO_8859_1 | "ISO_8859_1"
    }

    def "setText sets exact content of file"() {
        given:
            def sut = new TextFile(tmpPath)
        when:
            sut.setText(content)
        then:
            sut.getText() == content
        where:
            content << ["a\nb\nc", "\"a\nb\nc\n"]
    }

    def "setText sets exact content of file other charset"() {
        given:
            def sut = new TextFile(tmpPath, encoding)
        when:
            sut.setText(content, encoding)
        then:
            sut.getText(encoding) == content
        where:
            content     | encoding
            "a\nb\nc"   | "UTF_16"
            "a\nb\nc\n" | "UTF_16"
            "a\nb\nc"   | "ISO_8859_1"
            "a\nb\nc\n" | "ISO_8859_1"
    }

    def "getText throws exception with invalid encoding"() {
        given:
            def sut = new TextFile(tmpPath, invalidEncoding)
        when:
            sut.getText()
        then:
            thrown(IllegalArgumentException)
        where:
            invalidEncoding << ["", "invalid", "invalid encoding"]
    }
}
