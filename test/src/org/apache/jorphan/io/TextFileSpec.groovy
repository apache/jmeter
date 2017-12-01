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

    def "getText of empty file is empty string"() {
        given:
            def sut = new TextFile(tempFile.getAbsolutePath())
        expect:
            sut.getText() == ""
    }

    def "getText returns exact content of file"() {
        given:
            def sut = new TextFile(tempFile.getAbsolutePath())
            FileUtils.write(tempFile, content, Charset.defaultCharset())
        expect:
            sut.getText() == content
        where:
            content << ["a\nb\nc", "\"a\nb\nc\n"]
    }

    def "getText returns exact content of file with specific charset"() {
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

    def "setText sets exact content of file"() {
        given:
            def sut = new TextFile(tempFile.getAbsolutePath())
        when:
            sut.setText(content)
        then:
            sut.getText() == content
        where:
            content << ["a\nb\nc", "\"a\nb\nc\n"]
    }

    def "setText sets exact content of file other charset"() {
        given:
            def sut = new TextFile(tempFile.getAbsolutePath(), encoding)
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
}
