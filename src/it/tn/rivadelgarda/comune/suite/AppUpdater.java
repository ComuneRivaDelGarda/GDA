package it.tn.rivadelgarda.comune.suite;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

/**
 * User: tiziano
 * Date: 11/12/14
 * Time: 12:58
 *
 * From: http://reportmill.wordpress.com/2014/12/04/automatically-update-your-javapackager-applications/
 *
 */
public class AppUpdater {

    // Constants
    static final String JarName = "GDA.jar";


    public static Boolean checkForUpdates(String jarURL){
        // Get URL connection and lastModified time
        try {
            File jarFile = getAppFile(JarName);
            URL url = null;
            url = new URL(jarURL);
            URLConnection connection = url.openConnection();
            long mod0 = jarFile.lastModified(), mod1 = connection.getLastModified();
            if(mod0>=mod1) {
                System.out.println("No update available at " + jarURL + '(' + mod0 + '>' + mod1 + ')');
                return Boolean.FALSE;
            } else {
                System.out.println("Update available at " + jarURL + '(' + mod0 + '>' + mod1 + ')');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }

    public static Boolean update(String jarURL){

        // Invoke real main with exception handler
        try {
            return main1(jarURL);
        }
        catch(Throwable e) {
            //e.printStackTrace();
            System.out.println("Unable to perform update.");
        }
        return Boolean.FALSE;
    }

    /**
     * Main method:
     *     - Gets main Jar file from default, if missing
     *     - Updates main Jar file from local update file, if previously loaded
     *     - Load main Jar into URLClassLoader, load main class and invoke main method
     *     - Check for update from remove site in background
     */
    public static Boolean main1(String jarURL) throws Exception
    {
        // Make sure default jar is in place
        try {
            copyDefaultMainJar();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        updateJar();


        // Check for updates in background thread
        if( checkAndUpdate(jarURL) ){
            updateJar();
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }

    private static void updateJar() throws IOException {
        // If Update Jar exists, copy it into place
        File jar = getAppFile(JarName);
        File updateJar = getAppFile(JarName + ".update");
        if(updateJar.exists()) {
            copyFile(updateJar, jar);
            jar.setLastModified(updateJar.lastModified());
            updateJar.delete();
        }

        // If jar doesn't exist complain bitterly
        if(!jar.exists() || !jar.canRead())
            throw new RuntimeException("Main Jar not found!");
    }

    /**
     * Copies the default main jar into place for initial run.
     */
    private static void copyDefaultMainJar() throws IOException, ParseException
    {
        // Get main jar from app package and get location of working jar file
        URL url = AppUpdater.class.getProtectionDomain().getCodeSource().getLocation();
        String path0 = url.getPath(); path0 = URLDecoder.decode(path0, "UTF-8");
        File jar0 = getAppFile(JarName);
        File jar1 = new File(path0);

        // If app package main jar is newer, copy it into place and set time
        if(jar0.exists() && jar0.lastModified()>=jar1.lastModified()) return;
        copyFile(jar1, jar0);
    }

    /**
     * Check for updates.
     */
    private static void checkAndUpdateSilent(String jarURL)
    {
        try { checkAndUpdate(jarURL); }
        catch(Exception e) { e.printStackTrace(); }
    }

    /**
     * Check for updates.
     */
    private static Boolean checkAndUpdate(String jarURL) throws IOException, MalformedURLException
    {
        // Get URL connection and lastModified time
        File jarFile = getAppFile(JarName);
        URL url = new URL(jarURL);
        URLConnection connection = url.openConnection();
        long mod0 = jarFile.lastModified(), mod1 = connection.getLastModified();
        if(mod0>=mod1) {
            System.out.println("No update available at " + jarURL + '(' + mod0 + '>' + mod1 + ')');
            return Boolean.FALSE;
        }

        // Get update file and write to JarName.update
        System.out.println("Loading update from " + jarURL);
        byte bytes[] = getBytes(connection); System.out.println("Update loaded");
        File updatePacked = getAppFile(JarName + ".pack.gz"), updateFile = getAppFile(JarName + ".update");
        writeBytes(updatePacked, bytes);
        System.out.println("Update saved: " + updatePacked);
        unpack(updatePacked, updateFile);
        System.out.println("Update unpacked: " + updateFile);
        updateFile.setLastModified(mod1);
        updatePacked.delete();

        return Boolean.TRUE;
    }

    /**
     * Returns the Main jar file.
     */
    private static File getAppFile(String aName)  {
        return new File(getAppDir(), aName);
    }

    /**
     * Returns the Main jar file.
     */
    private static File getAppDir()  {
        String path = AppUpdater.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if( path.endsWith(JarName) ){
            String dir = path.substring(0, path.length()-JarName.length());
            File dfile = new File(dir);
            return dfile;
        } else {
            return null;
        }
    }

    /**
     *
     *  Utility Methods for AppLoader.
     *
     */


    /**
     * Copies a file from one location to another.
     */
    public static File copyFile(File aSource, File aDest) throws IOException
    {
        // Get input stream, output file and output stream
        FileInputStream fis = new FileInputStream(aSource);
        File out = aDest.isDirectory()? new File(aDest, aSource.getName()) : aDest;
        FileOutputStream fos = new FileOutputStream(out);

        // Iterate over read/write until all bytes written
        byte[] buf = new byte[8192];
        for(int i=fis.read(buf); i!=-1; i=fis.read(buf))
            fos.write(buf, 0, i);

        // Close in/out streams and return out file
        fis.close(); fos.close();
        return out;
    }

    /**
     * Writes the given bytes (within the specified range) to the given file.
     */
    public static void writeBytes(File aFile, byte theBytes[]) throws IOException
    {
        if(theBytes==null) { aFile.delete(); return; }
        FileOutputStream fileStream = new FileOutputStream(aFile);
        fileStream.write(theBytes);
        fileStream.close();
    }

    /**
     * Unpacks the given file into the destination file.
     */
    public static File unpack(File aFile, File aDestFile) throws IOException
    {
        // Get dest file - if already unpacked, return
        File destFile = getUnpackDestination(aFile, aDestFile);
        if(destFile.exists() && destFile.lastModified()>aFile.lastModified())
            return destFile;

        // Create streams: FileInputStream -> GZIPInputStream -> JarOutputStream -> FileOutputStream
        FileInputStream fileInput = new FileInputStream(aFile);
        GZIPInputStream gzipInput = new GZIPInputStream(fileInput);
        FileOutputStream fileOut = new FileOutputStream(destFile);
        JarOutputStream jarOut = new JarOutputStream(fileOut);

        // Unpack file
        Pack200.newUnpacker().unpack(gzipInput, jarOut);

        // Close streams
        fileInput.close(); gzipInput.close();
        jarOut.close(); fileOut.close();

        // Return destination file
        return destFile;
    }

    /**
     * Returns the file that given packed file would be saved to using the unpack method.
     */
    public static File getUnpackDestination(File aFile, File aDestFile)
    {
        // Get dest file - if null, create from packed file minus .pack.gz
        File destFile = aDestFile;
        if(destFile==null)
            destFile = new File(aFile.getPath().replace(".pack.gz", ""));

            // If dest file is directory, change to file inside with packed file minus .pack.gz
        else if(destFile.isDirectory())
            destFile = new File(destFile, aFile.getName().replace(".pack.gz", ""));

        // Return destination file
        return destFile;
    }

    /**
     * Returns the AppData or Application Support directory file.
     */
    public static File getAppDataDir(String aName, boolean doCreate)
    {
        // Get user home + AppDataDir (platform specific) + name (if provided)
        String dir = System.getProperty("user.home");
        if(isWindows) dir += File.separator + "AppData" + File.separator + "Local";
        else if(isMac) dir += File.separator + "Library" + File.separator + "Application Support";
        if(aName!=null) dir += File.separator + aName;

        // Create file, actual directory (if requested) and return
        File dfile = new File(dir);
        if(doCreate && aName!=null) dfile.mkdirs();
        return dfile;
    }

    /**
     * Returns bytes for connection.
     */
    public static byte[] getBytes(URLConnection aConnection) throws IOException
    {
        InputStream stream = aConnection.getInputStream(); // Get stream for connection
        byte bytes[] = getBytes(stream); // Get bytes for stream
        stream.close();  // Close stream
        return bytes;  // Return bytes
    }

    /**
     * Returns bytes for an input stream.
     */
    public static byte[] getBytes(InputStream aStream) throws IOException
    {
        ByteArrayOutputStream bs = new ByteArrayOutputStream(); byte chunk[] = new byte[8192];
        for(int len=aStream.read(chunk, 0, 8192); len>0; len=aStream.read(chunk, 0, 8192)) bs.write(chunk, 0, len);
        return bs.toByteArray();
    }

    // Whether Windows/Mac
    static boolean isWindows = (System.getProperty("os.name").indexOf("Windows") >= 0);
    static boolean isMac = (System.getProperty("os.name").indexOf("Mac OS X") >= 0);
}
