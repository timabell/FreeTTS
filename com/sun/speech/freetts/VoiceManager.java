/**
 * Copyright 2003 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts;

import java.util.Vector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.StringTokenizer;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.net.JarURLConnection;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

// [[[TODO: remove "TEST" marked debug statements after testing under Windows]]]
// [[[TODO: only create a single class loader instance to save space
// on static definitions]]]
// [[[TODO: remove conflict between voicesfile and voicespath methods]]]

/**
 * The VoiceManager is what provides access to voices for all of
 * FreeTTS.  There is only one instance of the VoiceManager.  
 *
 * [[[TODO: This API assumes that there are fewer than two or three
 * dozen jarfiles (the voice jars and their dependancies).  There are
 * some functions that have O(n^2) running time, but could be modified
 * to be O(n lg n).]]]
 * 
 * Each call to getVoices() creates a new instance of each voice.
 *
 * @see Voice
 * @see VoiceDirectory
 */
public class VoiceManager {

    private static final VoiceManager INSTANCE =
            new VoiceManager();

    private static final String fileSeparator =
            System.getProperty("file.separator");

    private static final String pathSeparator =
            System.getProperty("path.separator");

    private VoiceManager() {
        // do nothing
    }

    /**
     * Gets the instance of the VoiceManager
     *
     * @return a VoiceManager
     */
    public static VoiceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Provide an array of all voices available to FreeTTS.
     * First, the file internal_voices.txt is looked for in the
     * same directory as VoiceManager.class.  If the file does not
     * exist, the VoiceManager moves on.  Next, it looks for
     * voices.txt in the same directory as freetts.jar.  If the file
     * does not exist, the VoiceManager moves on.  Next, if the
     * property "freetts.voicesfile" is defined, then that file is
     * read in.  If the property is defined and the file does not
     * exist, then an error is raised.
     *
     * Every voices file that is read in contains a list of
     * VoiceDirectory class names.
     *
     * Next, the voice manager looks for freetts voice jarfiles that
     * may exist in well-known locations.  The directory that contains
     * freetts.jar is searched for voice jarfiles, then directories
     * specified by the "freetts.voicespath" system property.
     * Any jarfile whose Manifest contains
     * "FreeTTSVoiceDefinition: true" is assumed to be a FreeTTS
     * voice, and the Manifest's "Main-Class" entry is assumed to be
     * the name of the voice directory.  The dependancies of the voice
     * jarfiles specified by the "Class-Path" Manifest entry are also
     * loaded.
     *
     * The VoiceManager instantiates each voice directory
     * and calls getVoices() on each.
     *
     * @return the array of new instances of all available voices
     */
    public Voice[] getVoices() {
        Vector voices = new Vector();
        VoiceDirectory[] voiceDirectories = getVoiceDirectories();
        for (int i = 0; i < voiceDirectories.length; i++) {
            insertInto(voiceDirectories[i].getVoices(), voices);
        }

        Voice[] voiceArray = new Voice[voices.size()];
        return (Voice[]) voices.toArray(voiceArray);
    }

    /**
     * Prints detailed information about all available voices.
     *
     * @return a String containing the information
     */
    public String getVoiceInfo() {
        String infoString = "";
        VoiceDirectory[] voiceDirectories = getVoiceDirectories();
        for (int i = 0; i < voiceDirectories.length; i++) {
            infoString += voiceDirectories[i].toString();
        }
        return infoString;
    }

    /**
     * Creates an array of all voice directories of all available
     * voices using the criteria specified by the contract for
     * getVoices().
     *
     * @return the array of voice directories
     * @see getVoices()
     */
    private VoiceDirectory[] getVoiceDirectories() {
        try {
            // Get voice directory names
            Vector voiceDirectoryNames = getVoiceDirectoryNamesFromFiles();

            System.out.println("TEST: got " +
                    voiceDirectoryNames.size() + " names from voices files");

            Vector jarURLs = new Vector(); //TEST
            /* [[[TODO: Enable and test dynamic loader]]]
            Vector jarURLs = getVoiceJarURLs();
            mergeUnique(getVoiceDirectoryNamesFromJarURLs(jarURLs),
                    voiceDirectoryNames);
            */

            //TEST
            for (int i = 0; i < voiceDirectoryNames.size(); i++) {
                System.out.println("TEST vd: " + voiceDirectoryNames.get(i));
            }

            // Create a classloader
            URL[] jarURLArray = (URL[])
                    jarURLs.toArray(new URL[jarURLs.size()]);
            for (int i = 0; i < jarURLArray.length; i++) {
                getDependancyURLs(jarURLArray[i], jarURLs);
            }
            // urls may have been added
            jarURLArray = (URL[]) jarURLs.toArray(new URL[jarURLs.size()]);
            //TEST
            //URL[] voiceJarURLsArray = {
                        //new URL("jar", "",
                                //"file:/home/dv144877/cvs/freetts/FreeTTS/lib/cmu_us_kal.jar!/"),
                        //new URL("jar", "",
                                //"file:/home/dv144877/cvs/freetts/FreeTTS/lib/cmu_time_awb.jar!/"),
                        //new URL("jar", "",
                                //"file:/home/dv144877/cvs/freetts/FreeTTS/lib/cmutimelex.jar!/"),
                        //new URL("jar", "",
                                //"file:/home/dv144877/cvs/freetts/FreeTTS/lib/cmulex.jar!/"),
                        //new URL("jar", "",
                                //"file:/home/dv144877/cvs/freetts/FreeTTS/lib/en_us.jar!/"),
            //};
            URLClassLoader loader = new URLClassLoader(jarURLArray,
                    this.getClass().getClassLoader());

            //TEST
            System.out.println("TEST ClassLoader Urls:");
            for (int i = 0; i < jarURLArray.length; i++) {
                System.out.println(jarURLArray[i].toString());
            }

            //TEST
            //voiceDirectoryNames = new Vector();
            //voiceDirectoryNames.add("com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            //loader.loadClass("com.sun.speech.freetts.en.us.CMUDiphoneVoice");

            // Create an instance of each voice directory
            Vector voiceDirectories = new Vector();
            for (int i = 0; i < voiceDirectoryNames.size(); i++) {
                System.out.println("TEST About to try and load " + (String)
                        voiceDirectoryNames.get(i));
                Class c = Class.forName((String) voiceDirectoryNames.get(i),
                        true, loader);
                voiceDirectories.add(c.newInstance());
            }
        
            return (VoiceDirectory[]) voiceDirectories.toArray(new
                    VoiceDirectory[voiceDirectories.size()]);
        } catch (InstantiationException e) {
            throw new Error("Unable to load voice directory. " + e);
        } catch (ClassNotFoundException e) {
            throw new Error("Unable to load voice directory. " + e);
        } catch (IllegalAccessException e) {
            throw new Error("Unable to load voice directory. " + e);
        }

    }

    /**
     * Recursively gets the urls of the class paths that url is
     * dependant on.
     *
     * Conventions specified in
     * http://java.sun.com/j2se/1.4.1/docs/guide/extensions/spec.html#bundled
     * are followed.
     *
     * @param url the url to recursively check.  If it ends with a "/"
     * then it is presumed to be a directory, and is not checked.
     * Otherwise it is assumed to be a jar, and its manifest is read
     * to get the urls Class-Path entry.  These urls are passed to
     * this method recursively.
     *
     * @param dependancyURLs a vector containing all of the dependant
     * urls found.  This parameter is modified as urls are added to
     * it.
     */
    private void getDependancyURLs(URL url, Vector dependancyURLs) {
        try {
            String urlDirName = getURLDirName(url);
            if (url.getProtocol().equals("jar")) {
                JarURLConnection jarConnection =
                    (JarURLConnection) url.openConnection();
                Attributes attributes = jarConnection.getMainAttributes();
                String fullClassPath =
                    attributes.getValue(Attributes.Name.CLASS_PATH);
                if (fullClassPath == null) {
                    return; // no classpaths to add
                }
                StringTokenizer st = new StringTokenizer(fullClassPath);
                while (st.hasMoreTokens()) {
                    URL classPathURL;
                    String classPath = st.nextToken();
                    try {
                        if (classPath.endsWith("/")) {  // assume directory
                            classPathURL = new URL("file:" + urlDirName
                                    + classPath);
                        } else {                        // assume jar
                            classPathURL = new URL("jar", "", "file:" +
                                    urlDirName + classPath + "!/");
                        }
                    } catch (MalformedURLException e) {
                        // print warning?
                        continue;
                    }
                    System.out.println("TEST: adding dep url: " + classPathURL);
                    // don't get in a recursive loop if two jars
                    // are mutually dependant
                    if (addUnique(classPathURL, dependancyURLs)) {
                        getDependancyURLs(classPathURL, dependancyURLs);
                    }
                }
            }
        } catch (IOException e) {
            //TODO print warning?
        }
    }

    /**
     * Gets the names of the subclasses of VoiceDirectory that are
     * listed in the voices.txt files.
     *
     * @return a Vector containing the String names of the voice
     * directories
     */
    private Vector getVoiceDirectoryNamesFromFiles() {
        try {
            Vector voiceDirectoryNames = new Vector();

            // first, load internal_voices.txt
            InputStream is = 
                this.getClass().getResourceAsStream("internal_voices.txt");
            if (is != null) { // if it doesn't exist, move on
                voiceDirectoryNames = getVoiceDirectoryNamesFromInputStream(is);
            }

            // next, try loading voices.txt
            try {
                mergeUnique(
                    getVoiceDirectoryNamesFromFile(getBaseDirectory()
                            + "voices.txt"),
                        voiceDirectoryNames);
            } catch (FileNotFoundException e) {
                // do nothing
            } catch (IOException e) {
                // do nothing
            }

            // last, read voices from property freetts.voicesfile
            String voicesFile = System.getProperty("freetts.voicesfile");
            if (voicesFile != null) {
                mergeUnique(getVoiceDirectoryNamesFromFile(voicesFile),
                        voiceDirectoryNames);
            }
        
            return voiceDirectoryNames;
        } catch (IOException e) {
            throw new Error("Error reading voices files. " + e);
        }
    }

    /**
     * Gets the voice directory class names from a list of urls
     * specifying voice jarfiles.  The class name is specified as the
     * Main-Class in the manifest of the jarfiles.
     * 
     * @param urls a Vector of URLs that refer to the voice jarfiles
     *
     * @return a Vector of Strings representing the voice directory
     * class names
     */
    private Vector getVoiceDirectoryNamesFromJarURLs(Vector urls) {
        try {
            Vector voiceDirectoryNames = new Vector();
            for (int i = 0; i < urls.size(); i++) {
                System.out.println("TEST: reading manifest of " +
                        (URL)urls.get(i));
                JarURLConnection jarConnection =
                    (JarURLConnection) ((URL) urls.get(i)).openConnection();
                Attributes attributes = jarConnection.getMainAttributes();
                String mainClass =
                    attributes.getValue(Attributes.Name.MAIN_CLASS);
                System.out.println("TEST: Main-Class: " + mainClass);
                if (mainClass == null || mainClass.trim().equals("")) {
                    throw new Error("No Main-Class found in jar "
                            + (URL)urls.get(i));
                }
                addUnique(mainClass, voiceDirectoryNames);
            }
            return voiceDirectoryNames;
        } catch (IOException e) {
            throw new Error("Error reading jarfile manifests. ");
        }
    }

    /**
     * Gets the list of voice jarfiles.  Voice jarfiles are searched
     * for in the same directory as freetts.jar and the directories
     * specified by the freetts.voicespath system property.  Voice
     * jarfiles are defined by the manifest entry
     * "FreeTTSVoiceDefinition: true"
     *
     * @return a Vector of URLs refering to the voice jarfiles.
     */
    private Vector getVoiceJarURLs() {
        Vector voiceJarURLs = new Vector();

        // check in same directory as freetts.jar
        try {
            voiceJarURLs = getVoiceJarURLsFromDir(getBaseDirectory());
        } catch (FileNotFoundException e) {
            // do nothing
        }

        String voicesPath = System.getProperty("freetts.voicespath", "");

        // search voicespath
        String dirName = "";
        try {
            int start = 0;
            boolean stillLooping = !voicesPath.equals("");
            while (stillLooping) {
                int end = voicesPath.indexOf(pathSeparator);
                if (end == -1) {
                    dirName = voicesPath.substring(start);
                    stillLooping = false;
                } else {
                    dirName = voicesPath.substring(start, end);
                }

                System.out.println("TEST: adding voicepath " + dirName);
                if (!dirName.equals("")) {
                    mergeUnique(getVoiceJarURLsFromDir(dirName), voiceJarURLs);
                }
            }
        } catch (FileNotFoundException e) {
            throw new Error("Error loading jars from voicespath "
                    + dirName + ". ");
        }

        return voiceJarURLs;
    }

    /**
     * Gets the list of voice jarfiles in a specific directory.
     *
     * @return a Vector of URLs refering to the voice jarfiles
     * @see getVoiceJarURLs()
     */
    private Vector getVoiceJarURLsFromDir(String dirName)
            throws FileNotFoundException {
        try {
            Vector voiceJarURLs = new Vector();
            File dir = new File(dirName);
            if (!dir.isDirectory()) {
                throw new FileNotFoundException("File not directory: "
                        + dirName);
            }
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                System.out.println("TEST: checking url " + files[i].getName());
                if (files[i].isFile() && (!files[i].isHidden()) &&
                        files[i].getName().endsWith(".jar")) {
                    URL jarURL = files[i].toURL();
                    jarURL = new URL("jar", "",
                            "file:"+ jarURL.getPath() + "!/");
                    System.out.println("TEST: reading url " + jarURL);
                    JarURLConnection jarConnection = (JarURLConnection)
                        jarURL.openConnection();
                    Attributes attributes = jarConnection.getMainAttributes();
                    String isVoice =
                        attributes.getValue("FreeTTSVoiceDefinition");

                    if (isVoice != null && isVoice.trim().equals("true")) {
                        addUnique(jarURL, voiceJarURLs);
                    }
                }
            }
            return voiceJarURLs;
        } catch (MalformedURLException e) {
            throw new Error("Error reading jars from directory "
                    + dirName + ". ");
        } catch (IOException e) {
            throw new Error("Error reading jars from directory "
                    + dirName + ". ");
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

    /**
     * Get the directory that the jar file containing this class
     * resides in.
     *
     * @return the name of the directory with a trailing "/" (or
     * equivalent for the particular operating system), or "" if
     * unable to determin.  (For example this class does not reside
     * inside a jar file).
     */
    private String getBaseDirectory() {
        String name = this.getClass().getName();
        int lastdot = name.lastIndexOf('.');
        if (lastdot != -1) {  // remove package information
            name = name.substring(lastdot+1);
        }

        URL url = this.getClass().getResource(name + ".class");
        return getURLDirName(url);
    }

    /**
     * Gets the directory name from a URL
     *
     * @param url the url to parse
     * @return the String representation of the directory name in a
     * URL
     */
    private String getURLDirName(URL url) {
        String urlFileName = url.getPath();
        int i = urlFileName.lastIndexOf('!');
        if (i == -1) {
            i = urlFileName.length();
        }
        int dir = urlFileName.lastIndexOf("/", i);
        if (!urlFileName.startsWith("file:")) {
            return "";
        }
        return urlFileName.substring(5,dir) + "/";
    }

    /**
     * Get the names of the voice directories from a voices file.
     * Blank lines and lines beginning with "#" are ignored.
     * Beginning and trailing whitespace is ignored.
     *
     * @param fileName the name of the voices file to read from
     *
     * @return a vector of the names of the VoiceDirectory subclasses
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Vector getVoiceDirectoryNamesFromFile(String fileName) 
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
     * Beginning and trailing whitespace is ignored.
     *
     * @param is the input stream to read from
     *
     * @return a vector of the names of the VoiceDirectory subclasses
     * @throws IOException
     */
    private Vector getVoiceDirectoryNamesFromInputStream(InputStream is) 
            throws IOException {
        Vector names = new Vector();
        BufferedReader reader = new
            BufferedReader(new InputStreamReader(is));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (!line.startsWith("#") && !line.equals("")) {
                System.out.println("TEST Adding vd " + line);
                names.add(line);
            }
        }
        return names;
    }

    /**
     * Simple helper function to append an array onto the end of a
     * Vector.
     *
     * @param src the source array
     * @param dest the destination Vector
     */
    private void insertInto(Object[] src, Vector dest) {
        for (int i = 0; i < src.length; i++) {
            dest.add(src[i]);
        }
    }

    /**
     * Inserts an object into a vector if it is not already in the
     * vector.  Comparisons are made using equals()
     *
     * @param src the object to add
     * @param dest the Vector to append to
     *
     * @return true if src was unique (and was added), else false
     */
    private boolean addUnique(Object src, Vector dest) {
        for (int i = 0; i < dest.size(); i++) {
            if (dest.get(i).equals(src)) {
                return false;
            }
        }
        dest.add(src);
        return true;
    }

    /**
     * Merge two Vectors, adding only elements not already in dest.
     *
     * @param src the Vector to read in.
     * @param dest the Vector to append unique elements to
     */
    private void mergeUnique(Vector src, Vector dest) {
        for (int i = 0; i < src.size(); i++) {
            addUnique(src.get(i), dest);
        }
    }

}
