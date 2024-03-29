package com.crepz.rec;

/*
 * AudioEncoder.java
 *
 * This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2004 by Matthias Pfisterer All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * |<--- this code is formatted to fit into 80 columns --->|
 */
import java.io.IOException;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/*
 * If the compilation fails because this class is not available, get gnu.getopt
 * from the URL given in the comment below.
 */
import gnu.getopt.Getopt;

/**
 * <titleabbrev>AudioEncoder</titleabbrev> <title>Encoding an audio file</title>
 *
 * <formalpara><title>Purpose</title> <para> Encodes a PCM audio file, writes
 * the result as an encoded audio file. </para></formalpara>
 *
 * <formalpara> <title>Usage</title> <para> <cmdsynopsis> <command>java
 * AudioEncoder</command> <arg choice="plain"><replaceable
 * class="parameter">pcm_file</replaceable></arg> <arg
 * choice="plain"><replaceable
 * class="parameter">encoded_file</replaceable></arg> </cmdsynopsis> </para>
 * </formalpara>
 *
 * <formalpara><title>Parameters</title> <variablelist> <varlistentry>
 * <term><option><replaceable
 * class="parameter">pcm_file</replaceable></option></term> <listitem><para>the
 * name of the PCM input file.</para></listitem> </varlistentry> <varlistentry>
 * <term><option><replaceable
 * class="parameter">encoded_file</replaceable></option></term>
 * <listitem><para>the name of the encoded output file.</para></listitem>
 * </varlistentry> </variablelist> </formalpara>
 *
 * <formalpara><title>Bugs, limitations</title> <para> This program requires JDK
 * 1.5.0 or the latest version of Tritonus.
 *
 * Several formats, e.g. Ogg vorbis and GSM, can only be handled natively by
 * Tritonus. If you want to use this format with the Sun jdk1.3/1.4, you have to
 * install the respective plug-in from <ulink url
 * ="http://www.tritonus.org/plugins.html">Tritonus Plug-ins</ulink>. </para>
 * </formalpara>
 *
 * <formalpara><title>Source code</title> <para> <ulink
 * url="AudioEncoder.java.html">AudioEncoder.java</ulink>, <olink
 * targetdocent="getopt">gnu.getopt.Getopt</olink> </para> </formalpara>
 *
 */
public class AudioEncoder {

    private static boolean DEBUG = false;

    public static void main(String[] args) {
        AudioFormat.Encoding targetEncoding = null;
        String strFileTypeName = null;
        String strFileTypeExtension = null;
        AudioFileFormat.Type fileType = null;
        List<String> vorbisComments = new ArrayList<String>();

        int nQuality = -1;
        int nBitrate = -1;

        /*
         * Parsing of command-line options takes place...
         */
        /*java AudioEncoder [-D] -e <encoding> [-t <filetype> -T <fileextension>] [-q <quality> | -b <bitrate>] [-V "TAG=vorbis comment"]
          ... <pcm_file> <encoded_file>*/
        String fileNameIn = "/home/rudieri/Música/teste.wav";
        String fileNameOut = "/home/rudieri/Música/teste.ogg";
        args = new String[6];
        args[0] = "-D";
        args[1] = "-e VORBIS" ;
        args[2] = "-t ogg";
        args[2] = "-T ogg";
        args[3] = "-b64";
        args[4] = fileNameIn;
        args[5] = fileNameOut;
        Getopt g = new Getopt("AudioEncoder", args, "he:b:q:t:T:V:D");
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    printUsageAndExit();

                case 'e':
                    targetEncoding = new AudioFormat.Encoding(g.getOptarg());
                    if (DEBUG) {
                        out("AudioEncoder.main(): using encoding: " + targetEncoding);
                    }
                    break;

                case 'q':
                    nQuality = Integer.parseInt(g.getOptarg());
                    break;

                case 'b':
                    nBitrate = Integer.parseInt(g.getOptarg());
                    break;

                case 't':
                    strFileTypeName = g.getOptarg();
                    break;

                case 'T':
                    strFileTypeExtension = g.getOptarg();
                    break;

                case 'V':
                    vorbisComments.add(g.getOptarg());
                    if (DEBUG) {
                        out("adding comment: " + g.getOptarg());
                    }
                    break;

                case 'D':
                    DEBUG = true;
                    break;

                case '?':
                    printUsageAndExit();

                default:
                    out("getopt() returned " + c);
                    break;
            }
        }
        if (targetEncoding == null) {
            out("AudioEncoder.main(): no encoding specified!");
            printUsageAndExit();
        }

        if (strFileTypeName != null && strFileTypeExtension != null) {
            fileType = new AudioFileFormat.Type(strFileTypeName,
                    strFileTypeExtension);
        } else {
            fileType = AudioFileFormat.Type.WAVE;
        }
        if (DEBUG) {
            out("AudioEncoder.main(): using file type: " + fileType);
        }

        /*
         * We make shure that there are only two more arguments, which we take
         * as the filenames of the input and output soundfile.
         */
        int nOptionIndex = g.getOptind();
        if (args.length - nOptionIndex != 2) {
            printUsageAndExit();
        }
        File pcmFile = new File(args[nOptionIndex]);
        File encodedFile = new File(args[nOptionIndex + 1]);

        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(pcmFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ais == null) {
            out("cannot open audio file");
            System.exit(1);
        }
        AudioFormat targetAudioFormat = null;
        if (vorbisComments.size() > 0 || nQuality != -1 || nBitrate != -1) {
            Map<String, Object> properties = new HashMap<String, Object>();
            if (nQuality != -1) {
                properties.put("quality", new Integer(nQuality));
            }
            if (nBitrate != -1) {
                properties.put("bitrate", new Integer(nBitrate));
            }
            if (vorbisComments.size() > 0) {
                properties.put("vorbis.comments", vorbisComments);
                if (DEBUG) {
                    out("adding vorbis comments to properties map");
                }
            }
            AudioFormat sourceFormat = ais.getFormat();
            targetAudioFormat = new AudioFormat(targetEncoding,
                    sourceFormat.getSampleRate(),
                    AudioSystem.NOT_SPECIFIED,
                    sourceFormat.getChannels(),
                    AudioSystem.NOT_SPECIFIED,
                    AudioSystem.NOT_SPECIFIED,
                    sourceFormat.isBigEndian(),
                    properties);
        }
        AudioInputStream encodedAudioInputStream = null;
        if (targetAudioFormat != null) {
            encodedAudioInputStream = AudioSystem.getAudioInputStream(targetAudioFormat, ais);
        } else {
            encodedAudioInputStream = AudioSystem.getAudioInputStream(targetEncoding, ais);
        }
        if (DEBUG) {
            out("encoded stream: " + encodedAudioInputStream);
        }
        if (DEBUG) {
            out("encoded stream's format: " + encodedAudioInputStream.getFormat());
        }
        int nWrittenFrames = 0;
        try {
            nWrittenFrames = AudioSystem.write(encodedAudioInputStream, fileType, encodedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printUsageAndExit() {
        out("AudioEncoder: usage:");
        out("\tjava AudioEncoder [-D] -e <encoding> [-t <filetype> -T <fileextension>] [-q <quality> | -b <bitrate>] [-V \"TAG=vorbis comment\"] ... <pcm_file> <encoded_file>");
        System.exit(1);
    }

    private static void out(String strMessage) {
        System.out.println(strMessage);
    }
}
/**
 * * AudioEncoder.java **
 */
