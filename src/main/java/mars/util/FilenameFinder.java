

package mars.util;

import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.filechooser.FileFilter;
import java.util.Enumeration;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class FilenameFinder
{
    private static final String JAR_EXTENSION = ".jar";
    private static final String FILE_URL = "file:";
    private static final String JAR_URI_PREFIX = "jar:";
    private static final boolean NO_DIRECTORIES = false;
    public static String MATCH_ALL_EXTENSIONS;
    
    public static ArrayList getFilenameList(final ClassLoader classLoader, final String directoryPath, String fileExtension) {
        fileExtension = checkFileExtension(fileExtension);
        final ArrayList filenameList = new ArrayList();
        try {
            final Enumeration urls = classLoader.getResources(directoryPath);
            while (urls.hasMoreElements()) {
                URI uri = new URI(urls.nextElement().toString());
                if (uri.toString().indexOf("jar:") == 0) {
                    uri = new URI(uri.toString().substring("jar:".length()));
                }
                final File f = new File(uri.getPath());
                final File[] files = f.listFiles();
                if (files == null) {
                    if (f.toString().toLowerCase().indexOf(".jar") <= 0) {
                        continue;
                    }
                    filenameList.addAll(getListFromJar(extractJarFilename(f.toString()), directoryPath, fileExtension));
                }
                else {
                    final FileFilter filter = getFileFilter(fileExtension, "", false);
                    for (int i = 0; i < files.length; ++i) {
                        if (filter.accept(files[i])) {
                            filenameList.add(files[i].getName());
                        }
                    }
                }
            }
            return filenameList;
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return filenameList;
        }
        catch (IOException e2) {
            e2.printStackTrace();
            return filenameList;
        }
    }
    
    public static ArrayList getFilenameList(final ClassLoader classLoader, final String directoryPath, final ArrayList<String> fileExtensions) {
        ArrayList filenameList = new ArrayList();
        if (fileExtensions == null || fileExtensions.size() == 0) {
            filenameList = getFilenameList(classLoader, directoryPath, "");
        }
        else {
            for (int i = 0; i < fileExtensions.size(); ++i) {
                final String fileExtension = checkFileExtension(fileExtensions.get(i));
                filenameList.addAll(getFilenameList(classLoader, directoryPath, fileExtension));
            }
        }
        return filenameList;
    }
    
    public static ArrayList getFilenameList(final String directoryPath, String fileExtension) {
        fileExtension = checkFileExtension(fileExtension);
        final ArrayList filenameList = new ArrayList();
        final File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            final File[] allFiles = directory.listFiles();
            final FileFilter filter = getFileFilter(fileExtension, "", false);
            for (int i = 0; i < allFiles.length; ++i) {
                if (filter.accept(allFiles[i])) {
                    filenameList.add(allFiles[i].getAbsolutePath());
                }
            }
        }
        return filenameList;
    }
    
    public static ArrayList getFilenameList(final String directoryPath, final ArrayList<String> fileExtensions) {
        ArrayList filenameList = new ArrayList();
        if (fileExtensions == null || fileExtensions.size() == 0) {
            filenameList = getFilenameList(directoryPath, "");
        }
        else {
            for (int i = 0; i < fileExtensions.size(); ++i) {
                final String fileExtension = checkFileExtension(fileExtensions.get(i));
                filenameList.addAll(getFilenameList(directoryPath, fileExtension));
            }
        }
        return filenameList;
    }
    
    public static ArrayList getFilenameList(final ArrayList<String> nameList, String fileExtension) {
        fileExtension = checkFileExtension(fileExtension);
        final ArrayList filenameList = new ArrayList();
        final FileFilter filter = getFileFilter(fileExtension, "", false);
        for (int i = 0; i < nameList.size(); ++i) {
            final File file = new File(nameList.get(i));
            if (filter.accept(file)) {
                filenameList.add(file.getAbsolutePath());
            }
        }
        return filenameList;
    }
    
    public static ArrayList getFilenameList(final ArrayList nameList, final ArrayList<String> fileExtensions) {
        ArrayList filenameList = new ArrayList();
        if (fileExtensions == null || fileExtensions.size() == 0) {
            filenameList = getFilenameList(nameList, "");
        }
        else {
            for (int i = 0; i < fileExtensions.size(); ++i) {
                final String fileExtension = checkFileExtension(fileExtensions.get(i));
                filenameList.addAll(getFilenameList(nameList, fileExtension));
            }
        }
        return filenameList;
    }
    
    public static String getExtension(final File file) {
        String ext = null;
        final String s = file.getName();
        final int i = s.lastIndexOf(46);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
    
    public static FileFilter getFileFilter(final ArrayList extensions, final String description, final boolean acceptDirectories) {
        return new MarsFileFilter(extensions, description, acceptDirectories);
    }
    
    public static FileFilter getFileFilter(final ArrayList extensions, final String description) {
        return getFileFilter(extensions, description, true);
    }
    
    public static FileFilter getFileFilter(final String extension, final String description, final boolean acceptDirectories) {
        final ArrayList extensions = new ArrayList();
        extensions.add(extension);
        return new MarsFileFilter(extensions, description, acceptDirectories);
    }
    
    public static FileFilter getFileFilter(final String extension, final String description) {
        final ArrayList extensions = new ArrayList();
        extensions.add(extension);
        return getFileFilter(extensions, description, true);
    }
    
    public static boolean fileExtensionMatch(final String name, final String extension) {
        return extension == null || extension.length() == 0 || name.endsWith((extension.startsWith(".") ? "" : ".") + extension);
    }
    
    private static ArrayList getListFromJar(final String jarName, final String directoryPath, String fileExtension) {
        fileExtension = checkFileExtension(fileExtension);
        final ArrayList nameList = new ArrayList();
        if (jarName == null) {
            return nameList;
        }
        try {
            final ZipFile zf = new ZipFile(new File(jarName));
            final Enumeration<? extends ZipEntry> list = zf.entries();
            while (list.hasMoreElements()) {
                final ZipEntry ze = list.nextElement();
                if (ze.getName().startsWith(directoryPath + "/") && fileExtensionMatch(ze.getName(), fileExtension)) {
                    nameList.add(ze.getName().substring(ze.getName().lastIndexOf(47) + 1));
                }
            }
        }
        catch (Exception e) {
            System.out.println("Exception occurred reading MarsTool list from JAR: " + e);
        }
        return nameList;
    }
    
    private static String extractJarFilename(String path) {
        final StringTokenizer findTheJar = new StringTokenizer(path, "\\/");
        if (path.toLowerCase().startsWith("file:")) {
            path = path.substring("file:".length());
        }
        final int jarPosition = path.toLowerCase().indexOf(".jar");
        return (jarPosition >= 0) ? path.substring(0, jarPosition + ".jar".length()) : path;
    }
    
    private static String checkFileExtension(final String fileExtension) {
        return (fileExtension == null || fileExtension.length() == 0 || !fileExtension.startsWith(".")) ? fileExtension : fileExtension.substring(1);
    }
    
    static {
        FilenameFinder.MATCH_ALL_EXTENSIONS = "*";
    }
    
    private static class MarsFileFilter extends FileFilter
    {
        private ArrayList<String> extensions;
        private String fullDescription;
        private boolean acceptDirectories;
        
        private MarsFileFilter(final ArrayList extensions, final String description, final boolean acceptDirectories) {
            this.extensions = extensions;
            this.fullDescription = this.buildFullDescription(description, extensions);
            this.acceptDirectories = acceptDirectories;
        }
        
        private String buildFullDescription(final String description, final ArrayList<String> extensions) {
            String result = (description == null) ? "" : description;
            if (extensions.size() > 0) {
                result += "  (";
            }
            for (int i = 0; i < extensions.size(); ++i) {
                final String extension = extensions.get(i);
                if (extension != null && extension.length() > 0) {
                    result = result + ((i == 0) ? "" : "; ") + "*" + ((extension.charAt(0) == '.') ? "" : ".") + extension;
                }
            }
            if (extensions.size() > 0) {
                result += ")";
            }
            return result;
        }
        
        @Override
        public String getDescription() {
            return this.fullDescription;
        }
        
        @Override
        public boolean accept(final File file) {
            if (file.isDirectory()) {
                return this.acceptDirectories;
            }
            final String fileExtension = FilenameFinder.getExtension(file);
            if (fileExtension != null) {
                for (int i = 0; i < this.extensions.size(); ++i) {
                    final String extension = checkFileExtension(this.extensions.get(i));
                    if (extension.equals(FilenameFinder.MATCH_ALL_EXTENSIONS) || fileExtension.equals(extension)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
