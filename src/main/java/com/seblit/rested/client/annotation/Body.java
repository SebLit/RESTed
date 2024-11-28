package com.seblit.rested.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter as the body of a request that will be processed by {@link com.seblit.rested.client.media.RequestBodyParser RequestBodyParser}
 * and sent as the body of the request.<br>
 * Only one per Method allowed.<br>
 * Null parameters will be ignored and no body is sent
 * */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {

    String AAC = "audio/aac";
    String APNG = "image/apng";
    String AVIF = "image/avif";
    String MS_VIDEO = "video/x-msvideo";
    String BINARY = "application/octet-stream";
    String BMP = "image/bmp";
    String BZIP = "application/x-bzip";
    String BZIP2 = "application/x-bzip2";
    String CDF = "application/x-cdf";
    String CSH = "application/x-csh";
    String CSS = "text/css";
    String CSV = "text/csv";
    String EPUB = "application/epub+zip";
    String GZIP = "application/gzip";
    String GIF = "image/gif";
    String HTML = "text/html";
    String CALENDAR = "text/calendar";
    String JAR = "application/java-archive";
    String JPEG = "image/jpeg";
    String JS = "text/javascript";
    String JSON = "application/json";
    String JSONLD = "application/ld+json";
    String MIDI = "audio/midi";
    String MPEG_AUDIO = "audio/mpeg";
    String MP4 = "video/mp4";
    String MPEG_VIDEO = "video/mpeg";
    String OGG_AUDIO = "audio/ogg";
    String OGG_VIDEO = "video/ogg";
    String OGG = "application/ogg";
    String OPUS = "audio/opus";
    String OTF = "font/otf";
    String PNG = "image/png";
    String PDF = "application/pdf";
    String PHP = "application/x-httpd-php";
    String RAR = "application/vnd.rar";
    String RTF = "application/rtf";
    String SH = "application/x-sh";
    String SVG = "image/svg+xml";
    String TIF = "image/tiff";
    String TS = "video/mp2t";
    String TEXT = "text/plain";
    String WAV = "audio/wav";
    String WEBM = "video/webm";
    String WEBP = "image/webp";
    String WOFF = "font/woff";
    String WOFF2 = "font/woff2";
    String XHTML = "application/xhtml+xml";
    String XML = "application/xml";
    String ZIP = "application/zip";
    String THREE_GPP_VIDEO = "video/3gpp";
    String THREE_GPP_AUDIO = "audio/3gpp";
    String THREE_GPP2_VIDEO = "video/3gpp2";
    String THREE_GPP2_AUDIO = "audio/3gpp2";

    /**
     * The media type the body should be transformed into. This class provides some constants for convenience.<br>
     * Default: {@link #JSON}
     * */
    String value() default JSON;
    /**
     * The charset the body should be encoded with for the request body.<br>
     * Default: UTF-8
     * */
    String charset() default "UTF-8";

}
