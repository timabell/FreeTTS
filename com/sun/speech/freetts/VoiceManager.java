/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts;

import java.net.URL;

import java.util.Vector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URLClassLoader;
import java.net.MalformedURLException;

import java.net.JarURLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Enumeration;

/**
 * The VoiceManager is what provides access to voices for all of
 * FreeTTS.  There is only one instance of the VoiceManager.  
 * 
 * Each call to getVoices() creates a new instance of each voice.
 *
 * @see Voice
 * @see VoiceDirectory
 */
public class VoiceManager {

    private static final VoiceManager INSTANCE =
            new VoiceManager();

    private VoiceManager() {
        /* TODO: TEST code for experimenting with the dynamic
         * class-loader
        try {
            //TEST: load all jars.
            ClassLoader ploader = this.getClass().getClassLoader();

            URL[] urlList = {
                new URL("jar", "",
                    "file:/lab/speech/work/dvos/FreeTTS/lib/cmukal16.jar!/"),
                new URL("jar", "",
                    "file:/lab/speech/work/dvos/FreeTTS/lib/cmukal8.jar!/"),
                new URL("jar", "",
                    "file:/lab/speech/work/dvos/FreeTTS/lib/cmulex.jar!/"),
                new URL("jar", "",
                    "file:/lab/speech/work/dvos/FreeTTS/lib/jsapi.jar!/")};
            URLClassLoader loader = new URLClassLoader(urlList, ploader);

            for (int i = 0; i < urlList.length; i++) {
                JarURLConnection jarConnection =
                    (JarURLConnection)urlList[i].openConnection();
                JarFile jarFile = jarConnection.getJarFile();
                Enumeration entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().substring(0,
                            entry.getName().length() - 6); // chop off ".class"
                        Class c = loader.loadClass(className);
                    }
                }
            }
        } catch (MalformedURLException exp) {
            //TODO
            throw new Error("TEST Error: " + exp.getMessage());
        } catch (IOException exp) {
            //TODO
            throw new Error("TEST Error: " + exp.getMessage());
        } catch (ClassNotFoundException exp) {
            //TODO
            throw new Error("TEST Error: " + exp.getMessage());
        }
        */
        
    }

    public static VoiceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Provide an array of all voices available to FreeTTS.
     * [[[TODO: voice loading order]]]
     *
     * @return the array of new instances of all available voices
     */
    public Voice[] getVoices() {
        try {
            Vector voiceVector = new Vector();

            // check for internally defined voices
            try {
                String[] voiceDirectoryNames =
                    getVoiceDirectoryNamesFromInputStream(
                    this.getClass().getResourceAsStream("internal_voices.txt"));
                
                for (int i = 0; i < voiceDirectoryNames.length; i++) {
                    Class vdClass = Class.forName(voiceDirectoryNames[i]);
                    VoiceDirectory vd = (VoiceDirectory) vdClass.newInstance();
                    insertInto(vd.getVoices(), voiceVector);
                }
            } catch (InstantiationException exp) {
                throw new Error("Error reading voice data:" + exp.getMessage());
            } catch (ClassNotFoundException exp) {
                throw new Error("Error reading voice data:" + exp.getMessage());
            } catch (IllegalAccessException exp) {
                throw new Error("Error reading voice data:" + exp.getMessage());
            } catch (Exception exp) {
                // if there are problems, ignore them and move on
                // The file may not even exist
            }

            // try reading voices.txt in the same directory as freetts.jar
            try {
                insertInto(getVoicesFromFile(getBaseDirectory() + "voices.txt"),
                    voiceVector);
            } catch (FileNotFoundException exp) {
                // if voices.txt does not exist, move on
            } catch (IOException exp) {
                // if voices.txt does not exist, move on
            }

            // read voices from property freetts.voicesfile
            String voicesFile = System.getProperty("freetts.voicesfile");
            if (voicesFile != null) {
                insertInto(getVoicesFromFile(voicesFile), voiceVector);
            }


            //[[[TODO: dynamically load jars and read manifests]]]

            return (Voice[]) voiceVector.toArray(new Voice[0]);
        } catch (IOException exp) {
            throw new Error("Error reading voice data:" + exp.getMessage());
        }
    }

    /**
     * Provides a string representation of all voices available to
     * FreeTTS.
     *
     * @return a String which is a space-delimited list of voice
     * names, with the word "or" before the last one.
     */
    public String toString() {
        String names = "";
        Voice[] voices = getVoices();
        for (int i = 0; i < voices.length; i++) {
            if (i == voices.length - 1) {
                if (i == 0) {
                    names = voices[i].getName();
                } else {
                    names += "or " + voices[i].getName();
                }
            } else {
                names += voices[i].getName() + " ";
            }
        }
        return names;
    }

    /**
     * Check if there is a voice provides with the given name.
     *
     * @param voiceName the name of the voice to check
     *
     * @return <b>true</b> if FreeTTS has a voice available with the
     * name <b>voiceName</b>, else <b>false</b>.
     */
    public boolean contains(String voiceName) {
        return (getVoice(voiceName) != null);
    }

    /**
     * Get a Voice with a given name.
     *
     * @param voiceName the name of the voice to get.
     *
     * @return the Voice that has the same name as <b>voiceName</b>
     * if one exists, else <b>null</b>
     */
    public Voice getVoice(String voiceName) {
        Voice[] voices = getVoices();
        for (int i = 0; i < voices.length; i++) {
            if (voices[i].getName().equals(voiceName)) {
                return voices[i];
            }
        }
        return null;
    }

    /* Get the directory that the jar file containing this class
     * resides in.
     *
     * @return the name of the directory with a trailing "/" (or
     * equivalent for the particular operating system), or "" if
     * unable to determin.  (For example this class does not reside
     * inside a jar file).
     */
    protected String getBaseDirectory() {
        String name = this.getClass().getName();
        int lastdot = name.lastIndexOf('.');
        if (lastdot != -1) {
            name = name.substring(lastdot+1);
        }
        URL url = this.getClass().getResource(name + ".class");
        String classFileName = url.getPath();

        String fileSeparator = System.getProperty("file.separator");

        int i = classFileName.lastIndexOf('!');
        if (i == -1) {
            i = classFileName.length();
        }
        int dir = classFileName.lastIndexOf(fileSeparator, i);
        if (!classFileName.startsWith("file:")) {
            return "";
        }
        return classFileName.substring(5,dir) + fileSeparator;
    }

    /**
     * Get the names of the voice directories from a voices file.
     * Blank lines and lines beginning with "#" are ignored.
     *
     * @param fileName the name of the voices file to read from
     *
     * @return an array of the names of the VoiceDirectory subclasses
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected String[] getVoiceDirectoryNamesFromFile(String fileName) 
            throws FileNotFoundException, IOException {
        InputStream is = new FileInputStream(fileName);
        if (is == null) {
            throw new IOException();
        } else {
            return getVoiceDirectoryNamesFromInputStream(is);
        }
    }

    /**
     * Get the names of the voice directories from an input stream.
     * Blank lines and lines beginning with "#" are ignored.
     *
     * @param fileName the name of the voices file to read from
     *
     * @return an array of the names of the VoiceDirectory subclasses
     * @throws IOException
     */
    protected String[] getVoiceDirectoryNamesFromInputStream(InputStream is) 
            throws IOException {
        Vector nameVector = new Vector();
        BufferedReader reader = new
            BufferedReader(new InputStreamReader(is));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            } else if (!line.startsWith("#") && !line.trim().equals("")) {
                nameVector.add(line);
            }
        }
        return (String[]) nameVector.toArray(new String[0]);
    }

    /**
     * Get the voices listed in a voices file.
     *
     * @param fileName the name of the voices file to read the class
     * names from
     *
     * @return an array of new Voice instances of the voices provided
     * by the voice directories listed in the voices file
     */
    protected Voice[] getVoicesFromFile(String fileName) 
            throws FileNotFoundException, IOException {
        try {
            Vector voiceVector = new Vector();
            String[] voiceDirectoryNames =
                getVoiceDirectoryNamesFromFile(fileName);

            for (int i = 0; i < voiceDirectoryNames.length; i++) {
                Class vdClass = Class.forName(voiceDirectoryNames[i]);
                VoiceDirectory vd = (VoiceDirectory) vdClass.newInstance();
                insertInto(vd.getVoices(), voiceVector);
            }

            return (Voice[]) voiceVector.toArray(new Voice[0]);
        } catch (InstantiationException exp) {
            throw new Error("Error reading voice data:" + exp.getMessage());
        } catch (ClassNotFoundException exp) {
            throw new Error("Error reading voice data:" + exp.getMessage());
        } catch (IllegalAccessException exp) {
            throw new Error("Error reading voice data:" + exp.getMessage());
        }
    }

    /**
     * Simple helper function to append an array onto the end of a
     * Vector.
     *
     * @param src the source array
     * @param dest the destination Vector
     */
    protected void insertInto(Object[] src, Vector dest) {
        for (int i = 0; i < src.length; i++) {
            dest.add(src[i]);
        }
    }

}
