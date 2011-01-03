/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sprachupdate;

/**
 *
 * @author Matthias
 */
import java.io.*;
import java.util.*;
import java.util.jar.*;

public class FileInjector {

    File theFile;
    String jarLocation;
    String fileLocation;
    String subDirectory;

    public FileInjector(String jarLocation, String fileLocation, String subDirectory)
    {
        this.jarLocation = jarLocation;
        this.fileLocation = fileLocation;
        this.subDirectory = subDirectory;

        theFile = new File(fileLocation);
    }

    public boolean inject(javax.swing.JTextArea log) throws IOException {


      String jarName = jarLocation;
      String fileName = fileLocation;


      File jarFile = new File(jarName);
      File tempJarFile = new File(jarName + ".tmp");

      JarFile jar = new JarFile(jarFile);
      log.append("- " + jarName + " geöffnet\n");


      boolean jarUpdated = false;

      try {
         JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(tempJarFile));

         byte[] buffer = new byte[1024];
         int bytesRead;

         try {
            // FileInputStream file = new FileInputStream(fileName);
             InputStream file = this.getClass().getResourceAsStream(fileName);

            try {
               JarEntry entry = new JarEntry(subDirectory + "/" + theFile.getName());
               tempJar.putNextEntry(entry);

               while ((bytesRead = file.read(buffer)) != -1) {
                  tempJar.write(buffer, 0, bytesRead);
               }

               log.append("- " + entry.getName() + " hinzugefügt\n");
            }
            finally {
               file.close();
            }

            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) 
            {
               JarEntry entry = (JarEntry) entries.nextElement();

               //Überspringe vorhandene Datei
               if(entry.getName().equals(theFile.getName()) || entry.getName().equals(subDirectory + "/" + theFile.getName())){
                   log.append("- alte " + entry.getName() + " ignoriert\n");
                   continue;
               }

               if(entry.getName().equals("META-INF/MOJANG_C.SF") || entry.getName().equals("META-INF/MOJANG_C.DSA")){
                   log.append("- " + entry.getName() + " entfernt\n");
                   continue;
               }

               InputStream entryStream = jar.getInputStream(entry);

               // Schreibe in TempFile.
               tempJar.putNextEntry(entry);

               while ((bytesRead = entryStream.read(buffer)) != -1) {
                  tempJar.write(buffer, 0, bytesRead);
               }
            }
            jarUpdated = true;
         }
         catch (Exception ex) {
            log.append("Fehler: " + ex.getLocalizedMessage() + "\n");

            // Füge Dummy Eintrag hinzu
            tempJar.putNextEntry(new JarEntry("Dummy"));
         }
         finally {
            tempJar.close();
         }
      }
      finally {
         jar.close();
         log.append("- " + jarName + " geschlossen\n");


         if (! jarUpdated) {
            tempJarFile.delete();
         }
      }

      if (jarUpdated) {
         jarFile.delete();
         tempJarFile.renameTo(jarFile);
         return true;
      }
      
      return false;
   }
}

