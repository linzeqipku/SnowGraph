package graphdb.extractors.parsers.word.entity.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class FileUtils {
    private static final String	SMB_ROMOTE_FILE_PREFIX	= "smb:";

    public static Object unzipFile(Object obj, Object desRootObj) {
        if (obj instanceof File && desRootObj instanceof File) {
            File file = (File) obj;
            File desRootDir = (File) desRootObj;
            File desDir = new File(desRootDir,
                    ((FileUtils.getFileType(file) == FileType.ZIP) ? "[unzip]" : "[unrar]")
                            + FileUtils.getNameWithoutExtension(file.getName()));
            if (!desDir.exists() || !desDir.isDirectory())
                desDir.mkdirs();
            /*try {
                if (getFileType(file) == FileType.ZIP) {

                    ZipUtil.unZipFile(file.getAbsolutePath(), desDir.getAbsolutePath());
//					System.out.println("unzip: from[" + file.getAbsolutePath() + "]to:"
//							+ desDir.getAbsolutePath());
                    unzipAndFilterSubDirectories(desDir);

                }
                else if (getFileType(file) == FileType.RAR) {
                    // 若在当前地址解压，可不填输出地址，第二个参数解压密码
                    // 注意！exe，iso，dll文件的解压会出现冗余校验码错误，catch掉了
                    RarDecompressionUtil
                            .unrar(file.getAbsolutePath(), desDir.getAbsolutePath(), "");
                    unzipAndFilterSubDirectories(desDir);
                }
                System.out.println(file.getAbsolutePath());
            }
            catch (Exception e) {
                String logerr=("["
                        + ((FileUtils.getFileType(file) == FileType.ZIP) ? "UNZIP" : "UNRAR")
                        + " ERROR!!] from: " + file.getAbsolutePath() + " to: "
                        + desDir.getAbsolutePath());
                //System.out.println(logerr);
                logger.error(logerr);
                //e.printStackTrace();
            }*/

            return desDir;

        }
        return null;
    }

    /*private static void unzipAndFilterSubDirectories(File dir) {
        if (null != dir && dir.isDirectory()) {
            for (File subFile : dir.listFiles()) {
                int validity = FileValidityFilter.checkFileOrDirectoryValidity(subFile);
                // 根据筛选策略，过滤掉一部分文件和文件夹
                if (FileValidityFilter.isValidOrGreyOrDefault(validity)) {
                    if (subFile.isDirectory()) {
                        unzipAndFilterSubDirectories(subFile);
                    }
                    else if (subFile.isFile()
                            && (getFileType(subFile) == FileType.ZIP || getFileType(subFile) == FileType.RAR)) {
                        unzipFile(subFile, dir);
                        // 原zip/rar文件删除

                        subFile.delete();
//						while(subFile.exists())
//						{
//							subFile.delete();
//							System.out.println(subFile.getAbsolutePath());
//						}
                        System.out.println(subFile.getName()+"   zip");
                    }
                }
                else {
                    // 无效文件删除
                    //System.out.println(subFile.getName()+"   useless");
                    subFile.delete();
                }
            }
        }
    }*/

    public static String getNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String pureName = lastDotIndex < 0 ? fileName : fileName.substring(0, lastDotIndex);
        return pureName;
    }

    public static String getExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        String extension = lastDotIndex < 0 ? "" : fileName.substring(lastDotIndex + 1);
        return extension.toLowerCase();
    }

    public static FileType getFileType(Object obj) {
        String extension = FileType.OTHER.getName();
        if (obj instanceof File) {
            File file = (File) obj;
            if (file.isFile()) {
                extension = getExtension(file.getName());
            }
        }
        /*else if (obj instanceof SmbFile) {
            // smb文件的逻辑，try块内copy上述的。
            SmbFile file = (SmbFile) obj;
            try {
                if (file.isFile()) {
                    extension = getExtension(file.getName());
                }
            }
            catch (SmbException e) {
                e.printStackTrace();
            }
        }*/

        FileType docType = FileType.getDocumentTypeByName(extension);

        return docType;
    }
    public static FileType getFileTypeByName(String fileName) {
        String extension = getExtension(fileName);

        FileType docType = FileType.getDocumentTypeByName(extension);

        return docType;
    }

    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath);
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            }
            else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);
                delFolder(path + "/" + tempList[i]);
                flag = true;
            }
        }
        return flag;
    }

    /*public static void unzipFile(String fileName) {
        if (fileName.startsWith(SMB_ROMOTE_FILE_PREFIX)) {
            SmbFile file = null;
            try {
                file = new SmbFile(fileName);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (file != null) {
                try {
                    // just return an input stream for a file
                    if (file.isFile()) {
                        unzipFile(file);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            File file = null;
            try {
                file = new File(fileName);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (file != null) {
                try {
                    // just return an input stream for a file
                    if (file.isFile()) {
                        unzipFile(file);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void unzipFile(String fileName, String desPath) {
        if (fileName.startsWith(SMB_ROMOTE_FILE_PREFIX)) {
            SmbFile file = null;
            try {
                file = new SmbFile(fileName);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (file != null) {
                try {
                    // just return an input stream for a file
                    if (file.isFile()) {
                        unzipFile(file, desPath);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            File file = null;
            try {
                file = new File(fileName);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (file != null) {
                try {
                    // just return an input stream for a file
                    if (file.isFile()) {
                        unzipFile(file, desPath);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void unzipFile(Object obj) {
        if (obj instanceof File) {
            File file = (File) obj;
            String path = file.getAbsolutePath();
            if (getExtension(path).equals("zip")) {
                // 若在当前地址解压，输出地址为null
                try {
                    ZipUtil.unZipFile(path, null);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else if (getExtension(path).equals("rar")) {
                // 若在当前地址解压，可不填输出地址，第二个参数解压密码
                try {
                    RarDecompressionUtil.unrar(path, "");
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        else if (obj instanceof SmbFile) {
            SmbFile file = (SmbFile) obj;
            String path = file.getPath();
            if (getExtension(path).equals("zip")) {
                // 若在当前地址解压，输出地址为null
                try {
                    ZipUtil.unZipFile(path, null);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else if (getExtension(path).equals("rar")) {
                // 若在当前地址解压，可不填输出地址，第二个参数解压密码
                try {
                    RarDecompressionUtil.unrar(path, "");
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }*/

    public static void main(String[] args) {
        List<String> s = new ArrayList<String>();
        s.add("sdfjksd.dsfsdf.sdds");
        s.add("fsgfwgr");
        s.add("sgfdgdf.tt");
        s.add("dfgfdgdfg.");
        for (String string : s) {
            System.out.print(string + "\t**");
            System.out.print(FileUtils.getExtension(string) + "**\t");
            System.out.print(FileUtils.getFileTypeByName(string) + "**\t");
            System.out.println(FileUtils.getNameWithoutExtension(string) + "**\t");
        }

        //test getDirectoryLayer
        System.out.println(getDirectoryLayer("c:\\"));
        System.out.println(getDirectoryLayer("c:/"));
        System.out.println(getDirectoryLayer("c:\\tmp"));
        System.out.println(getDirectoryLayer("c:/tmp"));
        System.out.println(getDirectoryLayer("c:\\tmp\\"));
        System.out.println(getDirectoryLayer("c:/tmp/"));
        System.out.println(getDirectoryLayer("c:/tmp/subdir"));
    }

    /*
     * 获取绝对路径docDirAbsolutePath的目录层次数
     * 	eg: "c:\"(或"c:/")     --> 0
     * 		"c:\tmp"(或"c:\tmp")  --> 1
     * 		"c:\tmp\"(或"c:/tmp/") --> 1
     */
    public static int getDirectoryLayer(String docDirAbsolutePath) {
        int layer = 0;

        //replace '\' to '/'
        docDirAbsolutePath = docDirAbsolutePath.replace('\\', '/');

        int len = docDirAbsolutePath.length();

        for (int i = 0; i < len; i++) {
            if (docDirAbsolutePath.charAt(i) == '/') {
                layer++;
            }
        }

        //如果路径以'/'或'\'(已替换为'/')，目录层次数减1
        if (len > 0 && docDirAbsolutePath.charAt(len - 1) == '/') {
            layer--;
        }

        return layer;
    }

    public static int getDirectoryRelativeLayer(String docDirAbsolutePath, String rootpath) {

        if(!docDirAbsolutePath.startsWith(rootpath))
            return getDirectoryLayer(docDirAbsolutePath);
        else
        {
            String subString=docDirAbsolutePath.replace(rootpath, "");
            return getDirectoryLayer(subString);
        }

    }
}
